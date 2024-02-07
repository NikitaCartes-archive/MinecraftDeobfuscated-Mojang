package net.minecraft.world.entity.monster;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RemoveBlockGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Zombie extends Monster {
	private static final UUID SPEED_MODIFIER_BABY_UUID = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
	private static final AttributeModifier SPEED_MODIFIER_BABY = new AttributeModifier(
		SPEED_MODIFIER_BABY_UUID, "Baby speed boost", 0.5, AttributeModifier.Operation.MULTIPLY_BASE
	);
	private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(Zombie.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> DATA_SPECIAL_TYPE_ID = SynchedEntityData.defineId(Zombie.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_DROWNED_CONVERSION_ID = SynchedEntityData.defineId(Zombie.class, EntityDataSerializers.BOOLEAN);
	public static final float ZOMBIE_LEADER_CHANCE = 0.05F;
	public static final int REINFORCEMENT_ATTEMPTS = 50;
	public static final int REINFORCEMENT_RANGE_MAX = 40;
	public static final int REINFORCEMENT_RANGE_MIN = 7;
	private static final EntityDimensions BABY_DIMENSIONS = EntityType.ZOMBIE.getDimensions().scale(0.5F).withEyeHeight(0.93F);
	private static final float BREAK_DOOR_CHANCE = 0.1F;
	private static final Predicate<Difficulty> DOOR_BREAKING_PREDICATE = difficulty -> difficulty == Difficulty.HARD;
	private final BreakDoorGoal breakDoorGoal = new BreakDoorGoal(this, DOOR_BREAKING_PREDICATE);
	private boolean canBreakDoors;
	private int inWaterTime;
	private int conversionTime;

	public Zombie(EntityType<? extends Zombie> entityType, Level level) {
		super(entityType, level);
	}

	public Zombie(Level level) {
		this(EntityType.ZOMBIE, level);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(4, new Zombie.ZombieAttackTurtleEggGoal(this, 1.0, 3));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		this.addBehaviourGoals();
	}

	protected void addBehaviourGoals() {
		this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0, false));
		this.goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0, true, 4, this::canBreakDoors));
		this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
		this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(ZombifiedPiglin.class));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, AbstractVillager.class, false));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, IronGolem.class, true));
		this.targetSelector.addGoal(5, new NearestAttackableTargetGoal(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.FOLLOW_RANGE, 35.0)
			.add(Attributes.MOVEMENT_SPEED, 0.23F)
			.add(Attributes.ATTACK_DAMAGE, 3.0)
			.add(Attributes.ARMOR, 2.0)
			.add(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_BABY_ID, false);
		builder.define(DATA_SPECIAL_TYPE_ID, 0);
		builder.define(DATA_DROWNED_CONVERSION_ID, false);
	}

	public boolean isUnderWaterConverting() {
		return this.getEntityData().get(DATA_DROWNED_CONVERSION_ID);
	}

	public boolean canBreakDoors() {
		return this.canBreakDoors;
	}

	public void setCanBreakDoors(boolean bl) {
		if (this.supportsBreakDoorGoal() && GoalUtils.hasGroundPathNavigation(this)) {
			if (this.canBreakDoors != bl) {
				this.canBreakDoors = bl;
				((GroundPathNavigation)this.getNavigation()).setCanOpenDoors(bl);
				if (bl) {
					this.goalSelector.addGoal(1, this.breakDoorGoal);
				} else {
					this.goalSelector.removeGoal(this.breakDoorGoal);
				}
			}
		} else if (this.canBreakDoors) {
			this.goalSelector.removeGoal(this.breakDoorGoal);
			this.canBreakDoors = false;
		}
	}

	protected boolean supportsBreakDoorGoal() {
		return true;
	}

	@Override
	public boolean isBaby() {
		return this.getEntityData().get(DATA_BABY_ID);
	}

	@Override
	public int getExperienceReward() {
		if (this.isBaby()) {
			this.xpReward = (int)((double)this.xpReward * 2.5);
		}

		return super.getExperienceReward();
	}

	@Override
	public void setBaby(boolean bl) {
		this.getEntityData().set(DATA_BABY_ID, bl);
		if (this.level() != null && !this.level().isClientSide) {
			AttributeInstance attributeInstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
			attributeInstance.removeModifier(SPEED_MODIFIER_BABY.getId());
			if (bl) {
				attributeInstance.addTransientModifier(SPEED_MODIFIER_BABY);
			}
		}
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		if (DATA_BABY_ID.equals(entityDataAccessor)) {
			this.refreshDimensions();
		}

		super.onSyncedDataUpdated(entityDataAccessor);
	}

	protected boolean convertsInWater() {
		return true;
	}

	@Override
	public void tick() {
		if (!this.level().isClientSide && this.isAlive() && !this.isNoAi()) {
			if (this.isUnderWaterConverting()) {
				this.conversionTime--;
				if (this.conversionTime < 0) {
					this.doUnderWaterConversion();
				}
			} else if (this.convertsInWater()) {
				if (this.isEyeInFluid(FluidTags.WATER)) {
					this.inWaterTime++;
					if (this.inWaterTime >= 600) {
						this.startUnderWaterConversion(300);
					}
				} else {
					this.inWaterTime = -1;
				}
			}
		}

		super.tick();
	}

	@Override
	public void aiStep() {
		if (this.isAlive()) {
			boolean bl = this.isSunSensitive() && this.isSunBurnTick();
			if (bl) {
				ItemStack itemStack = this.getItemBySlot(EquipmentSlot.HEAD);
				if (!itemStack.isEmpty()) {
					if (itemStack.isDamageableItem()) {
						itemStack.setDamageValue(itemStack.getDamageValue() + this.random.nextInt(2));
						if (itemStack.getDamageValue() >= itemStack.getMaxDamage()) {
							this.broadcastBreakEvent(EquipmentSlot.HEAD);
							this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
						}
					}

					bl = false;
				}

				if (bl) {
					this.igniteForSeconds(8);
				}
			}
		}

		super.aiStep();
	}

	private void startUnderWaterConversion(int i) {
		this.conversionTime = i;
		this.getEntityData().set(DATA_DROWNED_CONVERSION_ID, true);
	}

	protected void doUnderWaterConversion() {
		this.convertToZombieType(EntityType.DROWNED);
		if (!this.isSilent()) {
			this.level().levelEvent(null, 1040, this.blockPosition(), 0);
		}
	}

	protected void convertToZombieType(EntityType<? extends Zombie> entityType) {
		Zombie zombie = this.convertTo(entityType, true);
		if (zombie != null) {
			zombie.handleAttributes(zombie.level().getCurrentDifficultyAt(zombie.blockPosition()).getSpecialMultiplier());
			zombie.setCanBreakDoors(zombie.supportsBreakDoorGoal() && this.canBreakDoors());
		}
	}

	protected boolean isSunSensitive() {
		return true;
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (!super.hurt(damageSource, f)) {
			return false;
		} else if (!(this.level() instanceof ServerLevel)) {
			return false;
		} else {
			ServerLevel serverLevel = (ServerLevel)this.level();
			LivingEntity livingEntity = this.getTarget();
			if (livingEntity == null && damageSource.getEntity() instanceof LivingEntity) {
				livingEntity = (LivingEntity)damageSource.getEntity();
			}

			if (livingEntity != null
				&& this.level().getDifficulty() == Difficulty.HARD
				&& (double)this.random.nextFloat() < this.getAttributeValue(Attributes.SPAWN_REINFORCEMENTS_CHANCE)
				&& this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
				int i = Mth.floor(this.getX());
				int j = Mth.floor(this.getY());
				int k = Mth.floor(this.getZ());
				Zombie zombie = new Zombie(this.level());

				for (int l = 0; l < 50; l++) {
					int m = i + Mth.nextInt(this.random, 7, 40) * Mth.nextInt(this.random, -1, 1);
					int n = j + Mth.nextInt(this.random, 7, 40) * Mth.nextInt(this.random, -1, 1);
					int o = k + Mth.nextInt(this.random, 7, 40) * Mth.nextInt(this.random, -1, 1);
					BlockPos blockPos = new BlockPos(m, n, o);
					EntityType<?> entityType = zombie.getType();
					if (SpawnPlacements.isSpawnPositionOk(entityType, this.level(), blockPos)
						&& SpawnPlacements.checkSpawnRules(entityType, serverLevel, MobSpawnType.REINFORCEMENT, blockPos, this.level().random)) {
						zombie.setPos((double)m, (double)n, (double)o);
						if (!this.level().hasNearbyAlivePlayer((double)m, (double)n, (double)o, 7.0)
							&& this.level().isUnobstructed(zombie)
							&& this.level().noCollision(zombie)
							&& !this.level().containsAnyLiquid(zombie.getBoundingBox())) {
							zombie.setTarget(livingEntity);
							zombie.finalizeSpawn(serverLevel, this.level().getCurrentDifficultyAt(zombie.blockPosition()), MobSpawnType.REINFORCEMENT, null);
							serverLevel.addFreshEntityWithPassengers(zombie);
							this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE)
								.addPermanentModifier(new AttributeModifier("Zombie reinforcement caller charge", -0.05F, AttributeModifier.Operation.ADDITION));
							zombie.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE)
								.addPermanentModifier(new AttributeModifier("Zombie reinforcement callee charge", -0.05F, AttributeModifier.Operation.ADDITION));
							break;
						}
					}
				}
			}

			return true;
		}
	}

	@Override
	public boolean doHurtTarget(Entity entity) {
		boolean bl = super.doHurtTarget(entity);
		if (bl) {
			float f = this.level().getCurrentDifficultyAt(this.blockPosition()).getEffectiveDifficulty();
			if (this.getMainHandItem().isEmpty() && this.isOnFire() && this.random.nextFloat() < f * 0.3F) {
				entity.igniteForSeconds(2 * (int)f);
			}
		}

		return bl;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ZOMBIE_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.ZOMBIE_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ZOMBIE_DEATH;
	}

	protected SoundEvent getStepSound() {
		return SoundEvents.ZOMBIE_STEP;
	}

	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
		this.playSound(this.getStepSound(), 0.15F, 1.0F);
	}

	@Override
	protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
		super.populateDefaultEquipmentSlots(randomSource, difficultyInstance);
		if (randomSource.nextFloat() < (this.level().getDifficulty() == Difficulty.HARD ? 0.05F : 0.01F)) {
			int i = randomSource.nextInt(3);
			if (i == 0) {
				this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
			} else {
				this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SHOVEL));
			}
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putBoolean("IsBaby", this.isBaby());
		compoundTag.putBoolean("CanBreakDoors", this.canBreakDoors());
		compoundTag.putInt("InWaterTime", this.isInWater() ? this.inWaterTime : -1);
		compoundTag.putInt("DrownedConversionTime", this.isUnderWaterConverting() ? this.conversionTime : -1);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setBaby(compoundTag.getBoolean("IsBaby"));
		this.setCanBreakDoors(compoundTag.getBoolean("CanBreakDoors"));
		this.inWaterTime = compoundTag.getInt("InWaterTime");
		if (compoundTag.contains("DrownedConversionTime", 99) && compoundTag.getInt("DrownedConversionTime") > -1) {
			this.startUnderWaterConversion(compoundTag.getInt("DrownedConversionTime"));
		}
	}

	@Override
	public boolean killedEntity(ServerLevel serverLevel, LivingEntity livingEntity) {
		boolean bl = super.killedEntity(serverLevel, livingEntity);
		if ((serverLevel.getDifficulty() == Difficulty.NORMAL || serverLevel.getDifficulty() == Difficulty.HARD) && livingEntity instanceof Villager villager) {
			if (serverLevel.getDifficulty() != Difficulty.HARD && this.random.nextBoolean()) {
				return bl;
			}

			ZombieVillager zombieVillager = villager.convertTo(EntityType.ZOMBIE_VILLAGER, false);
			if (zombieVillager != null) {
				zombieVillager.finalizeSpawn(
					serverLevel, serverLevel.getCurrentDifficultyAt(zombieVillager.blockPosition()), MobSpawnType.CONVERSION, new Zombie.ZombieGroupData(false, true)
				);
				zombieVillager.setVillagerData(villager.getVillagerData());
				zombieVillager.setGossips(villager.getGossips().store(NbtOps.INSTANCE));
				zombieVillager.setTradeOffers(villager.getOffers().copy());
				zombieVillager.setVillagerXp(villager.getVillagerXp());
				if (!this.isSilent()) {
					serverLevel.levelEvent(null, 1026, this.blockPosition(), 0);
				}

				bl = false;
			}
		}

		return bl;
	}

	@Override
	public EntityDimensions getDefaultDimensions(Pose pose) {
		return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions(pose);
	}

	@Override
	public boolean canHoldItem(ItemStack itemStack) {
		return itemStack.is(Items.EGG) && this.isBaby() && this.isPassenger() ? false : super.canHoldItem(itemStack);
	}

	@Override
	public boolean wantsToPickUp(ItemStack itemStack) {
		return itemStack.is(Items.GLOW_INK_SAC) ? false : super.wantsToPickUp(itemStack);
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData
	) {
		RandomSource randomSource = serverLevelAccessor.getRandom();
		spawnGroupData = super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
		float f = difficultyInstance.getSpecialMultiplier();
		this.setCanPickUpLoot(randomSource.nextFloat() < 0.55F * f);
		if (spawnGroupData == null) {
			spawnGroupData = new Zombie.ZombieGroupData(getSpawnAsBabyOdds(randomSource), true);
		}

		if (spawnGroupData instanceof Zombie.ZombieGroupData zombieGroupData) {
			if (zombieGroupData.isBaby) {
				this.setBaby(true);
				if (zombieGroupData.canSpawnJockey) {
					if ((double)randomSource.nextFloat() < 0.05) {
						List<Chicken> list = serverLevelAccessor.getEntitiesOfClass(
							Chicken.class, this.getBoundingBox().inflate(5.0, 3.0, 5.0), EntitySelector.ENTITY_NOT_BEING_RIDDEN
						);
						if (!list.isEmpty()) {
							Chicken chicken = (Chicken)list.get(0);
							chicken.setChickenJockey(true);
							this.startRiding(chicken);
						}
					} else if ((double)randomSource.nextFloat() < 0.05) {
						Chicken chicken2 = EntityType.CHICKEN.create(this.level());
						if (chicken2 != null) {
							chicken2.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
							chicken2.finalizeSpawn(serverLevelAccessor, difficultyInstance, MobSpawnType.JOCKEY, null);
							chicken2.setChickenJockey(true);
							this.startRiding(chicken2);
							serverLevelAccessor.addFreshEntity(chicken2);
						}
					}
				}
			}

			this.setCanBreakDoors(this.supportsBreakDoorGoal() && randomSource.nextFloat() < f * 0.1F);
			this.populateDefaultEquipmentSlots(randomSource, difficultyInstance);
			this.populateDefaultEquipmentEnchantments(randomSource, difficultyInstance);
		}

		if (this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
			LocalDate localDate = LocalDate.now();
			int i = localDate.get(ChronoField.DAY_OF_MONTH);
			int j = localDate.get(ChronoField.MONTH_OF_YEAR);
			if (j == 10 && i == 31 && randomSource.nextFloat() < 0.25F) {
				this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(randomSource.nextFloat() < 0.1F ? Blocks.JACK_O_LANTERN : Blocks.CARVED_PUMPKIN));
				this.armorDropChances[EquipmentSlot.HEAD.getIndex()] = 0.0F;
			}
		}

		this.handleAttributes(f);
		return spawnGroupData;
	}

	public static boolean getSpawnAsBabyOdds(RandomSource randomSource) {
		return randomSource.nextFloat() < 0.05F;
	}

	protected void handleAttributes(float f) {
		this.randomizeReinforcementsChance();
		this.getAttribute(Attributes.KNOCKBACK_RESISTANCE)
			.addPermanentModifier(new AttributeModifier("Random spawn bonus", this.random.nextDouble() * 0.05F, AttributeModifier.Operation.ADDITION));
		double d = this.random.nextDouble() * 1.5 * (double)f;
		if (d > 1.0) {
			this.getAttribute(Attributes.FOLLOW_RANGE)
				.addPermanentModifier(new AttributeModifier("Random zombie-spawn bonus", d, AttributeModifier.Operation.MULTIPLY_TOTAL));
		}

		if (this.random.nextFloat() < f * 0.05F) {
			this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE)
				.addPermanentModifier(new AttributeModifier("Leader zombie bonus", this.random.nextDouble() * 0.25 + 0.5, AttributeModifier.Operation.ADDITION));
			this.getAttribute(Attributes.MAX_HEALTH)
				.addPermanentModifier(new AttributeModifier("Leader zombie bonus", this.random.nextDouble() * 3.0 + 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
			this.setCanBreakDoors(this.supportsBreakDoorGoal());
		}
	}

	protected void randomizeReinforcementsChance() {
		this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(this.random.nextDouble() * 0.1F);
	}

	@Override
	protected void dropCustomDeathLoot(DamageSource damageSource, int i, boolean bl) {
		super.dropCustomDeathLoot(damageSource, i, bl);
		if (damageSource.getEntity() instanceof Creeper creeper && creeper.canDropMobsSkull()) {
			ItemStack itemStack = this.getSkull();
			if (!itemStack.isEmpty()) {
				creeper.increaseDroppedSkulls();
				this.spawnAtLocation(itemStack);
			}
		}
	}

	protected ItemStack getSkull() {
		return new ItemStack(Items.ZOMBIE_HEAD);
	}

	class ZombieAttackTurtleEggGoal extends RemoveBlockGoal {
		ZombieAttackTurtleEggGoal(PathfinderMob pathfinderMob, double d, int i) {
			super(Blocks.TURTLE_EGG, pathfinderMob, d, i);
		}

		@Override
		public void playDestroyProgressSound(LevelAccessor levelAccessor, BlockPos blockPos) {
			levelAccessor.playSound(null, blockPos, SoundEvents.ZOMBIE_DESTROY_EGG, SoundSource.HOSTILE, 0.5F, 0.9F + Zombie.this.random.nextFloat() * 0.2F);
		}

		@Override
		public void playBreakSound(Level level, BlockPos blockPos) {
			level.playSound(null, blockPos, SoundEvents.TURTLE_EGG_BREAK, SoundSource.BLOCKS, 0.7F, 0.9F + level.random.nextFloat() * 0.2F);
		}

		@Override
		public double acceptedDistance() {
			return 1.14;
		}
	}

	public static class ZombieGroupData implements SpawnGroupData {
		public final boolean isBaby;
		public final boolean canSpawnJockey;

		public ZombieGroupData(boolean bl, boolean bl2) {
			this.isBaby = bl;
			this.canSpawnJockey = bl2;
		}
	}
}
