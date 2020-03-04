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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
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
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Zombie extends Monster {
	protected static final Attribute SPAWN_REINFORCEMENTS_CHANCE = new RangedAttribute(null, "zombie.spawnReinforcements", 0.0, 0.0, 1.0)
		.importLegacyName("Spawn Reinforcements Chance");
	private static final UUID SPEED_MODIFIER_BABY_UUID = UUID.fromString("B9766B59-9566-4402-BC1F-2EE2A276D836");
	private static final AttributeModifier SPEED_MODIFIER_BABY = new AttributeModifier(
		SPEED_MODIFIER_BABY_UUID, "Baby speed boost", 0.5, AttributeModifier.Operation.MULTIPLY_BASE
	);
	private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(Zombie.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> DATA_SPECIAL_TYPE_ID = SynchedEntityData.defineId(Zombie.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_DROWNED_CONVERSION_ID = SynchedEntityData.defineId(Zombie.class, EntityDataSerializers.BOOLEAN);
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

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(35.0);
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23F);
		this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0);
		this.getAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(2.0);
		this.getAttributes().registerAttribute(SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(this.random.nextDouble() * 0.1F);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.getEntityData().define(DATA_BABY_ID, false);
		this.getEntityData().define(DATA_SPECIAL_TYPE_ID, 0);
		this.getEntityData().define(DATA_DROWNED_CONVERSION_ID, false);
	}

	public boolean isUnderWaterConverting() {
		return this.getEntityData().get(DATA_DROWNED_CONVERSION_ID);
	}

	public boolean canBreakDoors() {
		return this.canBreakDoors;
	}

	public void setCanBreakDoors(boolean bl) {
		if (this.supportsBreakDoorGoal()) {
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
	protected int getExperienceReward(Player player) {
		if (this.isBaby()) {
			this.xpReward = (int)((float)this.xpReward * 2.5F);
		}

		return super.getExperienceReward(player);
	}

	@Override
	public void setBaby(boolean bl) {
		this.getEntityData().set(DATA_BABY_ID, bl);
		if (this.level != null && !this.level.isClientSide) {
			AttributeInstance attributeInstance = this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
			attributeInstance.removeModifier(SPEED_MODIFIER_BABY);
			if (bl) {
				attributeInstance.addModifier(SPEED_MODIFIER_BABY);
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
		if (!this.level.isClientSide && this.isAlive()) {
			if (this.isUnderWaterConverting()) {
				this.conversionTime--;
				if (this.conversionTime < 0) {
					this.doUnderWaterConversion();
				}
			} else if (this.convertsInWater()) {
				if (this.isUnderLiquid(FluidTags.WATER)) {
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
					this.setSecondsOnFire(8);
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
		this.convertTo(EntityType.DROWNED);
		this.level.levelEvent(null, 1040, this.blockPosition(), 0);
	}

	protected void convertTo(EntityType<? extends Zombie> entityType) {
		if (!this.removed) {
			Zombie zombie = entityType.create(this.level);
			zombie.copyPosition(this);
			zombie.setCanPickUpLoot(this.canPickUpLoot());
			zombie.setCanBreakDoors(zombie.supportsBreakDoorGoal() && this.canBreakDoors());
			zombie.handleAttributes(zombie.level.getCurrentDifficultyAt(zombie.blockPosition()).getSpecialMultiplier());
			zombie.setBaby(this.isBaby());
			zombie.setNoAi(this.isNoAi());

			for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
				ItemStack itemStack = this.getItemBySlot(equipmentSlot);
				if (!itemStack.isEmpty()) {
					zombie.setItemSlot(equipmentSlot, itemStack.copy());
					zombie.setDropChance(equipmentSlot, this.getEquipmentDropChance(equipmentSlot));
					itemStack.setCount(0);
				}
			}

			if (this.hasCustomName()) {
				zombie.setCustomName(this.getCustomName());
				zombie.setCustomNameVisible(this.isCustomNameVisible());
			}

			if (this.isPersistenceRequired()) {
				zombie.setPersistenceRequired();
			}

			zombie.setInvulnerable(this.isInvulnerable());
			this.level.addFreshEntity(zombie);
			this.remove();
		}
	}

	protected boolean isSunSensitive() {
		return true;
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		if (super.hurt(damageSource, f)) {
			LivingEntity livingEntity = this.getTarget();
			if (livingEntity == null && damageSource.getEntity() instanceof LivingEntity) {
				livingEntity = (LivingEntity)damageSource.getEntity();
			}

			if (livingEntity != null
				&& this.level.getDifficulty() == Difficulty.HARD
				&& (double)this.random.nextFloat() < this.getAttribute(SPAWN_REINFORCEMENTS_CHANCE).getValue()
				&& this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
				int i = Mth.floor(this.getX());
				int j = Mth.floor(this.getY());
				int k = Mth.floor(this.getZ());
				Zombie zombie = new Zombie(this.level);

				for (int l = 0; l < 50; l++) {
					int m = i + Mth.nextInt(this.random, 7, 40) * Mth.nextInt(this.random, -1, 1);
					int n = j + Mth.nextInt(this.random, 7, 40) * Mth.nextInt(this.random, -1, 1);
					int o = k + Mth.nextInt(this.random, 7, 40) * Mth.nextInt(this.random, -1, 1);
					BlockPos blockPos = new BlockPos(m, n - 1, o);
					if (this.level.getBlockState(blockPos).entityCanStandOn(this.level, blockPos, zombie) && this.level.getMaxLocalRawBrightness(new BlockPos(m, n, o)) < 10) {
						zombie.setPos((double)m, (double)n, (double)o);
						if (!this.level.hasNearbyAlivePlayer((double)m, (double)n, (double)o, 7.0)
							&& this.level.isUnobstructed(zombie)
							&& this.level.noCollision(zombie)
							&& !this.level.containsAnyLiquid(zombie.getBoundingBox())) {
							this.level.addFreshEntity(zombie);
							zombie.setTarget(livingEntity);
							zombie.finalizeSpawn(this.level, this.level.getCurrentDifficultyAt(zombie.blockPosition()), MobSpawnType.REINFORCEMENT, null, null);
							this.getAttribute(SPAWN_REINFORCEMENTS_CHANCE)
								.addModifier(new AttributeModifier("Zombie reinforcement caller charge", -0.05F, AttributeModifier.Operation.ADDITION));
							zombie.getAttribute(SPAWN_REINFORCEMENTS_CHANCE)
								.addModifier(new AttributeModifier("Zombie reinforcement callee charge", -0.05F, AttributeModifier.Operation.ADDITION));
							break;
						}
					}
				}
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean doHurtTarget(Entity entity) {
		boolean bl = super.doHurtTarget(entity);
		if (bl) {
			float f = this.level.getCurrentDifficultyAt(this.blockPosition()).getEffectiveDifficulty();
			if (this.getMainHandItem().isEmpty() && this.isOnFire() && this.random.nextFloat() < f * 0.3F) {
				entity.setSecondsOnFire(2 * (int)f);
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
	public MobType getMobType() {
		return MobType.UNDEAD;
	}

	@Override
	protected void populateDefaultEquipmentSlots(DifficultyInstance difficultyInstance) {
		super.populateDefaultEquipmentSlots(difficultyInstance);
		if (this.random.nextFloat() < (this.level.getDifficulty() == Difficulty.HARD ? 0.05F : 0.01F)) {
			int i = this.random.nextInt(3);
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
		if (this.isBaby()) {
			compoundTag.putBoolean("IsBaby", true);
		}

		compoundTag.putBoolean("CanBreakDoors", this.canBreakDoors());
		compoundTag.putInt("InWaterTime", this.isInWater() ? this.inWaterTime : -1);
		compoundTag.putInt("DrownedConversionTime", this.isUnderWaterConverting() ? this.conversionTime : -1);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		if (compoundTag.getBoolean("IsBaby")) {
			this.setBaby(true);
		}

		this.setCanBreakDoors(compoundTag.getBoolean("CanBreakDoors"));
		this.inWaterTime = compoundTag.getInt("InWaterTime");
		if (compoundTag.contains("DrownedConversionTime", 99) && compoundTag.getInt("DrownedConversionTime") > -1) {
			this.startUnderWaterConversion(compoundTag.getInt("DrownedConversionTime"));
		}
	}

	@Override
	public void killed(LivingEntity livingEntity) {
		super.killed(livingEntity);
		if ((this.level.getDifficulty() == Difficulty.NORMAL || this.level.getDifficulty() == Difficulty.HARD) && livingEntity instanceof Villager) {
			if (this.level.getDifficulty() != Difficulty.HARD && this.random.nextBoolean()) {
				return;
			}

			Villager villager = (Villager)livingEntity;
			ZombieVillager zombieVillager = EntityType.ZOMBIE_VILLAGER.create(this.level);
			zombieVillager.copyPosition(villager);
			villager.remove();
			zombieVillager.finalizeSpawn(
				this.level, this.level.getCurrentDifficultyAt(zombieVillager.blockPosition()), MobSpawnType.CONVERSION, new Zombie.ZombieGroupData(false), null
			);
			zombieVillager.setVillagerData(villager.getVillagerData());
			zombieVillager.setGossips(villager.getGossips().store(NbtOps.INSTANCE).getValue());
			zombieVillager.setTradeOffers(villager.getOffers().createTag());
			zombieVillager.setVillagerXp(villager.getVillagerXp());
			zombieVillager.setBaby(villager.isBaby());
			zombieVillager.setNoAi(villager.isNoAi());
			if (villager.hasCustomName()) {
				zombieVillager.setCustomName(villager.getCustomName());
				zombieVillager.setCustomNameVisible(villager.isCustomNameVisible());
			}

			if (this.isPersistenceRequired()) {
				zombieVillager.setPersistenceRequired();
			}

			zombieVillager.setInvulnerable(this.isInvulnerable());
			this.level.addFreshEntity(zombieVillager);
			this.level.levelEvent(null, 1026, this.blockPosition(), 0);
		}
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return this.isBaby() ? 0.93F : 1.74F;
	}

	@Override
	public boolean canHoldItem(ItemStack itemStack) {
		return itemStack.getItem() == Items.EGG && this.isBaby() && this.isPassenger() ? false : super.canHoldItem(itemStack);
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
		LevelAccessor levelAccessor,
		DifficultyInstance difficultyInstance,
		MobSpawnType mobSpawnType,
		@Nullable SpawnGroupData spawnGroupData,
		@Nullable CompoundTag compoundTag
	) {
		spawnGroupData = super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
		float f = difficultyInstance.getSpecialMultiplier();
		this.setCanPickUpLoot(this.random.nextFloat() < 0.55F * f);
		if (spawnGroupData == null) {
			spawnGroupData = new Zombie.ZombieGroupData(levelAccessor.getRandom().nextFloat() < 0.05F);
		}

		if (spawnGroupData instanceof Zombie.ZombieGroupData) {
			Zombie.ZombieGroupData zombieGroupData = (Zombie.ZombieGroupData)spawnGroupData;
			if (zombieGroupData.isBaby) {
				this.setBaby(true);
				if ((double)levelAccessor.getRandom().nextFloat() < 0.05) {
					List<Chicken> list = levelAccessor.getEntitiesOfClass(Chicken.class, this.getBoundingBox().inflate(5.0, 3.0, 5.0), EntitySelector.ENTITY_NOT_BEING_RIDDEN);
					if (!list.isEmpty()) {
						Chicken chicken = (Chicken)list.get(0);
						chicken.setChickenJockey(true);
						this.startRiding(chicken);
					}
				} else if ((double)levelAccessor.getRandom().nextFloat() < 0.05) {
					Chicken chicken2 = EntityType.CHICKEN.create(this.level);
					chicken2.moveTo(this.getX(), this.getY(), this.getZ(), this.yRot, 0.0F);
					chicken2.finalizeSpawn(levelAccessor, difficultyInstance, MobSpawnType.JOCKEY, null, null);
					chicken2.setChickenJockey(true);
					levelAccessor.addFreshEntity(chicken2);
					this.startRiding(chicken2);
				}
			}

			this.setCanBreakDoors(this.supportsBreakDoorGoal() && this.random.nextFloat() < f * 0.1F);
			this.populateDefaultEquipmentSlots(difficultyInstance);
			this.populateDefaultEquipmentEnchantments(difficultyInstance);
		}

		if (this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
			LocalDate localDate = LocalDate.now();
			int i = localDate.get(ChronoField.DAY_OF_MONTH);
			int j = localDate.get(ChronoField.MONTH_OF_YEAR);
			if (j == 10 && i == 31 && this.random.nextFloat() < 0.25F) {
				this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(this.random.nextFloat() < 0.1F ? Blocks.JACK_O_LANTERN : Blocks.CARVED_PUMPKIN));
				this.armorDropChances[EquipmentSlot.HEAD.getIndex()] = 0.0F;
			}
		}

		this.handleAttributes(f);
		return spawnGroupData;
	}

	protected void handleAttributes(float f) {
		this.getAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE)
			.addModifier(new AttributeModifier("Random spawn bonus", this.random.nextDouble() * 0.05F, AttributeModifier.Operation.ADDITION));
		double d = this.random.nextDouble() * 1.5 * (double)f;
		if (d > 1.0) {
			this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE)
				.addModifier(new AttributeModifier("Random zombie-spawn bonus", d, AttributeModifier.Operation.MULTIPLY_TOTAL));
		}

		if (this.random.nextFloat() < f * 0.05F) {
			this.getAttribute(SPAWN_REINFORCEMENTS_CHANCE)
				.addModifier(new AttributeModifier("Leader zombie bonus", this.random.nextDouble() * 0.25 + 0.5, AttributeModifier.Operation.ADDITION));
			this.getAttribute(SharedMonsterAttributes.MAX_HEALTH)
				.addModifier(new AttributeModifier("Leader zombie bonus", this.random.nextDouble() * 3.0 + 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
			this.setCanBreakDoors(this.supportsBreakDoorGoal());
		}
	}

	@Override
	public double getRidingHeight() {
		return this.isBaby() ? 0.0 : -0.45;
	}

	@Override
	protected void dropCustomDeathLoot(DamageSource damageSource, int i, boolean bl) {
		super.dropCustomDeathLoot(damageSource, i, bl);
		Entity entity = damageSource.getEntity();
		if (entity instanceof Creeper) {
			Creeper creeper = (Creeper)entity;
			if (creeper.canDropMobsSkull()) {
				creeper.increaseDroppedSkulls();
				ItemStack itemStack = this.getSkull();
				if (!itemStack.isEmpty()) {
					this.spawnAtLocation(itemStack);
				}
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

	public class ZombieGroupData implements SpawnGroupData {
		public final boolean isBaby;

		private ZombieGroupData(boolean bl) {
			this.isBaby = bl;
		}
	}
}
