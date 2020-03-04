package net.minecraft.world.entity.animal;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.CatLieOnBedGoal;
import net.minecraft.world.entity.ai.goal.CatSitOnBlockGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OcelotAttackGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;

public class Cat extends TamableAnimal {
	private static final Ingredient TEMPT_INGREDIENT = Ingredient.of(Items.COD, Items.SALMON);
	private static final EntityDataAccessor<Integer> DATA_TYPE_ID = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> IS_LYING = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> RELAX_STATE_ONE = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> DATA_COLLAR_COLOR = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.INT);
	public static final Map<Integer, ResourceLocation> TEXTURE_BY_TYPE = Util.make(Maps.<Integer, ResourceLocation>newHashMap(), hashMap -> {
		hashMap.put(0, new ResourceLocation("textures/entity/cat/tabby.png"));
		hashMap.put(1, new ResourceLocation("textures/entity/cat/black.png"));
		hashMap.put(2, new ResourceLocation("textures/entity/cat/red.png"));
		hashMap.put(3, new ResourceLocation("textures/entity/cat/siamese.png"));
		hashMap.put(4, new ResourceLocation("textures/entity/cat/british_shorthair.png"));
		hashMap.put(5, new ResourceLocation("textures/entity/cat/calico.png"));
		hashMap.put(6, new ResourceLocation("textures/entity/cat/persian.png"));
		hashMap.put(7, new ResourceLocation("textures/entity/cat/ragdoll.png"));
		hashMap.put(8, new ResourceLocation("textures/entity/cat/white.png"));
		hashMap.put(9, new ResourceLocation("textures/entity/cat/jellie.png"));
		hashMap.put(10, new ResourceLocation("textures/entity/cat/all_black.png"));
	});
	private Cat.CatAvoidEntityGoal<Player> avoidPlayersGoal;
	private TemptGoal temptGoal;
	private float lieDownAmount;
	private float lieDownAmountO;
	private float lieDownAmountTail;
	private float lieDownAmountOTail;
	private float relaxStateOneAmount;
	private float relaxStateOneAmountO;

	public Cat(EntityType<? extends Cat> entityType, Level level) {
		super(entityType, level);
	}

	public ResourceLocation getResourceLocation() {
		return (ResourceLocation)TEXTURE_BY_TYPE.getOrDefault(this.getCatType(), TEXTURE_BY_TYPE.get(0));
	}

	@Override
	protected void registerGoals() {
		this.temptGoal = new Cat.CatTemptGoal(this, 0.6, TEMPT_INGREDIENT, true);
		this.goalSelector.addGoal(1, new FloatGoal(this));
		this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
		this.goalSelector.addGoal(2, new Cat.CatRelaxOnOwnerGoal(this));
		this.goalSelector.addGoal(3, this.temptGoal);
		this.goalSelector.addGoal(5, new CatLieOnBedGoal(this, 1.1, 8));
		this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0, 10.0F, 5.0F, false));
		this.goalSelector.addGoal(7, new CatSitOnBlockGoal(this, 0.8));
		this.goalSelector.addGoal(8, new LeapAtTargetGoal(this, 0.3F));
		this.goalSelector.addGoal(9, new OcelotAttackGoal(this));
		this.goalSelector.addGoal(10, new BreedGoal(this, 0.8));
		this.goalSelector.addGoal(11, new WaterAvoidingRandomStrollGoal(this, 0.8, 1.0000001E-5F));
		this.goalSelector.addGoal(12, new LookAtPlayerGoal(this, Player.class, 10.0F));
		this.targetSelector.addGoal(1, new NonTameRandomTargetGoal(this, Rabbit.class, false, null));
		this.targetSelector.addGoal(1, new NonTameRandomTargetGoal(this, Turtle.class, false, Turtle.BABY_ON_LAND_SELECTOR));
	}

	public int getCatType() {
		return this.entityData.get(DATA_TYPE_ID);
	}

	public void setCatType(int i) {
		if (i < 0 || i >= 11) {
			i = this.random.nextInt(10);
		}

		this.entityData.set(DATA_TYPE_ID, i);
	}

	public void setLying(boolean bl) {
		this.entityData.set(IS_LYING, bl);
	}

	public boolean isLying() {
		return this.entityData.get(IS_LYING);
	}

	public void setRelaxStateOne(boolean bl) {
		this.entityData.set(RELAX_STATE_ONE, bl);
	}

	public boolean isRelaxStateOne() {
		return this.entityData.get(RELAX_STATE_ONE);
	}

	public DyeColor getCollarColor() {
		return DyeColor.byId(this.entityData.get(DATA_COLLAR_COLOR));
	}

	public void setCollarColor(DyeColor dyeColor) {
		this.entityData.set(DATA_COLLAR_COLOR, dyeColor.getId());
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_TYPE_ID, 1);
		this.entityData.define(IS_LYING, false);
		this.entityData.define(RELAX_STATE_ONE, false);
		this.entityData.define(DATA_COLLAR_COLOR, DyeColor.RED.getId());
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("CatType", this.getCatType());
		compoundTag.putByte("CollarColor", (byte)this.getCollarColor().getId());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setCatType(compoundTag.getInt("CatType"));
		if (compoundTag.contains("CollarColor", 99)) {
			this.setCollarColor(DyeColor.byId(compoundTag.getInt("CollarColor")));
		}
	}

	@Override
	public void customServerAiStep() {
		if (this.getMoveControl().hasWanted()) {
			double d = this.getMoveControl().getSpeedModifier();
			if (d == 0.6) {
				this.setPose(Pose.CROUCHING);
				this.setSprinting(false);
			} else if (d == 1.33) {
				this.setPose(Pose.STANDING);
				this.setSprinting(true);
			} else {
				this.setPose(Pose.STANDING);
				this.setSprinting(false);
			}
		} else {
			this.setPose(Pose.STANDING);
			this.setSprinting(false);
		}
	}

	@Nullable
	@Override
	protected SoundEvent getAmbientSound() {
		if (this.isTame()) {
			if (this.isInLove()) {
				return SoundEvents.CAT_PURR;
			} else {
				return this.random.nextInt(4) == 0 ? SoundEvents.CAT_PURREOW : SoundEvents.CAT_AMBIENT;
			}
		} else {
			return SoundEvents.CAT_STRAY_AMBIENT;
		}
	}

	@Override
	public int getAmbientSoundInterval() {
		return 120;
	}

	public void hiss() {
		this.playSound(SoundEvents.CAT_HISS, this.getSoundVolume(), this.getVoicePitch());
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.CAT_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.CAT_DEATH;
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0);
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3F);
		this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0);
	}

	@Override
	public boolean causeFallDamage(float f, float g) {
		return false;
	}

	@Override
	protected void usePlayerItem(Player player, ItemStack itemStack) {
		if (this.isFood(itemStack)) {
			this.playSound(SoundEvents.CAT_EAT, 1.0F, 1.0F);
		}

		super.usePlayerItem(player, itemStack);
	}

	private float getAttackDamage() {
		return (float)this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue();
	}

	@Override
	public boolean doHurtTarget(Entity entity) {
		return entity.hurt(DamageSource.mobAttack(this), this.getAttackDamage());
	}

	@Override
	public void tick() {
		super.tick();
		if (this.temptGoal != null && this.temptGoal.isRunning() && !this.isTame() && this.tickCount % 100 == 0) {
			this.playSound(SoundEvents.CAT_BEG_FOR_FOOD, 1.0F, 1.0F);
		}

		this.handleLieDown();
	}

	private void handleLieDown() {
		if ((this.isLying() || this.isRelaxStateOne()) && this.tickCount % 5 == 0) {
			this.playSound(SoundEvents.CAT_PURR, 0.6F + 0.4F * (this.random.nextFloat() - this.random.nextFloat()), 1.0F);
		}

		this.updateLieDownAmount();
		this.updateRelaxStateOneAmount();
	}

	private void updateLieDownAmount() {
		this.lieDownAmountO = this.lieDownAmount;
		this.lieDownAmountOTail = this.lieDownAmountTail;
		if (this.isLying()) {
			this.lieDownAmount = Math.min(1.0F, this.lieDownAmount + 0.15F);
			this.lieDownAmountTail = Math.min(1.0F, this.lieDownAmountTail + 0.08F);
		} else {
			this.lieDownAmount = Math.max(0.0F, this.lieDownAmount - 0.22F);
			this.lieDownAmountTail = Math.max(0.0F, this.lieDownAmountTail - 0.13F);
		}
	}

	private void updateRelaxStateOneAmount() {
		this.relaxStateOneAmountO = this.relaxStateOneAmount;
		if (this.isRelaxStateOne()) {
			this.relaxStateOneAmount = Math.min(1.0F, this.relaxStateOneAmount + 0.1F);
		} else {
			this.relaxStateOneAmount = Math.max(0.0F, this.relaxStateOneAmount - 0.13F);
		}
	}

	@Environment(EnvType.CLIENT)
	public float getLieDownAmount(float f) {
		return Mth.lerp(f, this.lieDownAmountO, this.lieDownAmount);
	}

	@Environment(EnvType.CLIENT)
	public float getLieDownAmountTail(float f) {
		return Mth.lerp(f, this.lieDownAmountOTail, this.lieDownAmountTail);
	}

	@Environment(EnvType.CLIENT)
	public float getRelaxStateOneAmount(float f) {
		return Mth.lerp(f, this.relaxStateOneAmountO, this.relaxStateOneAmount);
	}

	public Cat getBreedOffspring(AgableMob agableMob) {
		Cat cat = EntityType.CAT.create(this.level);
		if (agableMob instanceof Cat) {
			if (this.random.nextBoolean()) {
				cat.setCatType(this.getCatType());
			} else {
				cat.setCatType(((Cat)agableMob).getCatType());
			}

			if (this.isTame()) {
				cat.setOwnerUUID(this.getOwnerUUID());
				cat.setTame(true);
				if (this.random.nextBoolean()) {
					cat.setCollarColor(this.getCollarColor());
				} else {
					cat.setCollarColor(((Cat)agableMob).getCollarColor());
				}
			}
		}

		return cat;
	}

	@Override
	public boolean canMate(Animal animal) {
		if (!this.isTame()) {
			return false;
		} else if (!(animal instanceof Cat)) {
			return false;
		} else {
			Cat cat = (Cat)animal;
			return cat.isTame() && super.canMate(animal);
		}
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
		if (levelAccessor.getMoonBrightness() > 0.9F) {
			this.setCatType(this.random.nextInt(11));
		} else {
			this.setCatType(this.random.nextInt(10));
		}

		if (Feature.SWAMP_HUT.isInsideFeature(levelAccessor, this.blockPosition())) {
			this.setCatType(10);
			this.setPersistenceRequired();
		}

		return spawnGroupData;
	}

	@Override
	public boolean mobInteract(Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		Item item = itemStack.getItem();
		if (itemStack.getItem() instanceof SpawnEggItem) {
			return super.mobInteract(player, interactionHand);
		} else if (this.level.isClientSide) {
			return this.isTame() && this.isOwnedBy(player) || this.isFood(itemStack);
		} else {
			if (this.isTame()) {
				if (this.isOwnedBy(player)) {
					if (!(item instanceof DyeItem)) {
						if (item.isEdible() && this.isFood(itemStack) && this.getHealth() < this.getMaxHealth()) {
							this.usePlayerItem(player, itemStack);
							this.heal((float)item.getFoodProperties().getNutrition());
							return true;
						}

						boolean bl = super.mobInteract(player, interactionHand);
						if (!bl || this.isBaby()) {
							this.setOrderedToSit(!this.isOrderedToSit());
						}

						return bl;
					}

					DyeColor dyeColor = ((DyeItem)item).getDyeColor();
					if (dyeColor != this.getCollarColor()) {
						this.setCollarColor(dyeColor);
						if (!player.abilities.instabuild) {
							itemStack.shrink(1);
						}

						this.setPersistenceRequired();
						return true;
					}
				}
			} else if (this.isFood(itemStack)) {
				this.usePlayerItem(player, itemStack);
				if (this.random.nextInt(3) == 0) {
					this.tame(player);
					this.setOrderedToSit(true);
					this.level.broadcastEntityEvent(this, (byte)7);
				} else {
					this.level.broadcastEntityEvent(this, (byte)6);
				}

				this.setPersistenceRequired();
				return true;
			}

			boolean bl = super.mobInteract(player, interactionHand);
			if (bl) {
				this.setPersistenceRequired();
			}

			return bl;
		}
	}

	@Override
	public boolean isFood(ItemStack itemStack) {
		return TEMPT_INGREDIENT.test(itemStack);
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return entityDimensions.height * 0.5F;
	}

	@Override
	public boolean removeWhenFarAway(double d) {
		return !this.isTame() && this.tickCount > 2400;
	}

	@Override
	protected void reassessTameGoals() {
		if (this.avoidPlayersGoal == null) {
			this.avoidPlayersGoal = new Cat.CatAvoidEntityGoal<>(this, Player.class, 16.0F, 0.8, 1.33);
		}

		this.goalSelector.removeGoal(this.avoidPlayersGoal);
		if (!this.isTame()) {
			this.goalSelector.addGoal(4, this.avoidPlayersGoal);
		}
	}

	static class CatAvoidEntityGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
		private final Cat cat;

		public CatAvoidEntityGoal(Cat cat, Class<T> class_, float f, double d, double e) {
			super(cat, class_, f, d, e, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);
			this.cat = cat;
		}

		@Override
		public boolean canUse() {
			return !this.cat.isTame() && super.canUse();
		}

		@Override
		public boolean canContinueToUse() {
			return !this.cat.isTame() && super.canContinueToUse();
		}
	}

	static class CatRelaxOnOwnerGoal extends Goal {
		private final Cat cat;
		private Player ownerPlayer;
		private BlockPos goalPos;
		private int onBedTicks;

		public CatRelaxOnOwnerGoal(Cat cat) {
			this.cat = cat;
		}

		@Override
		public boolean canUse() {
			if (!this.cat.isTame()) {
				return false;
			} else if (this.cat.isOrderedToSit()) {
				return false;
			} else {
				LivingEntity livingEntity = this.cat.getOwner();
				if (livingEntity instanceof Player) {
					this.ownerPlayer = (Player)livingEntity;
					if (!livingEntity.isSleeping()) {
						return false;
					}

					if (this.cat.distanceToSqr(this.ownerPlayer) > 100.0) {
						return false;
					}

					BlockPos blockPos = this.ownerPlayer.blockPosition();
					BlockState blockState = this.cat.level.getBlockState(blockPos);
					if (blockState.getBlock().is(BlockTags.BEDS)) {
						Direction direction = blockState.getValue(BedBlock.FACING);
						this.goalPos = new BlockPos(blockPos.getX() - direction.getStepX(), blockPos.getY(), blockPos.getZ() - direction.getStepZ());
						return !this.spaceIsOccupied();
					}
				}

				return false;
			}
		}

		private boolean spaceIsOccupied() {
			for (Cat cat : this.cat.level.getEntitiesOfClass(Cat.class, new AABB(this.goalPos).inflate(2.0))) {
				if (cat != this.cat && (cat.isLying() || cat.isRelaxStateOne())) {
					return true;
				}
			}

			return false;
		}

		@Override
		public boolean canContinueToUse() {
			return this.cat.isTame()
				&& !this.cat.isOrderedToSit()
				&& this.ownerPlayer != null
				&& this.ownerPlayer.isSleeping()
				&& this.goalPos != null
				&& !this.spaceIsOccupied();
		}

		@Override
		public void start() {
			if (this.goalPos != null) {
				this.cat.setInSittingPose(false);
				this.cat.getNavigation().moveTo((double)this.goalPos.getX(), (double)this.goalPos.getY(), (double)this.goalPos.getZ(), 1.1F);
			}
		}

		@Override
		public void stop() {
			this.cat.setLying(false);
			float f = this.cat.level.getTimeOfDay(1.0F);
			if (this.ownerPlayer.getSleepTimer() >= 100 && (double)f > 0.77 && (double)f < 0.8 && (double)this.cat.level.getRandom().nextFloat() < 0.7) {
				this.giveMorningGift();
			}

			this.onBedTicks = 0;
			this.cat.setRelaxStateOne(false);
			this.cat.getNavigation().stop();
		}

		private void giveMorningGift() {
			Random random = this.cat.getRandom();
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
			mutableBlockPos.set(this.cat.blockPosition());
			this.cat
				.randomTeleport(
					(double)(mutableBlockPos.getX() + random.nextInt(11) - 5),
					(double)(mutableBlockPos.getY() + random.nextInt(5) - 2),
					(double)(mutableBlockPos.getZ() + random.nextInt(11) - 5),
					false
				);
			mutableBlockPos.set(this.cat.blockPosition());
			LootTable lootTable = this.cat.level.getServer().getLootTables().get(BuiltInLootTables.CAT_MORNING_GIFT);
			LootContext.Builder builder = new LootContext.Builder((ServerLevel)this.cat.level)
				.withParameter(LootContextParams.BLOCK_POS, mutableBlockPos)
				.withParameter(LootContextParams.THIS_ENTITY, this.cat)
				.withRandom(random);

			for (ItemStack itemStack : lootTable.getRandomItems(builder.create(LootContextParamSets.GIFT))) {
				this.cat
					.level
					.addFreshEntity(
						new ItemEntity(
							this.cat.level,
							(double)((float)mutableBlockPos.getX() - Mth.sin(this.cat.yBodyRot * (float) (Math.PI / 180.0))),
							(double)mutableBlockPos.getY(),
							(double)((float)mutableBlockPos.getZ() + Mth.cos(this.cat.yBodyRot * (float) (Math.PI / 180.0))),
							itemStack
						)
					);
			}
		}

		@Override
		public void tick() {
			if (this.ownerPlayer != null && this.goalPos != null) {
				this.cat.setInSittingPose(false);
				this.cat.getNavigation().moveTo((double)this.goalPos.getX(), (double)this.goalPos.getY(), (double)this.goalPos.getZ(), 1.1F);
				if (this.cat.distanceToSqr(this.ownerPlayer) < 2.5) {
					this.onBedTicks++;
					if (this.onBedTicks > 16) {
						this.cat.setLying(true);
						this.cat.setRelaxStateOne(false);
					} else {
						this.cat.lookAt(this.ownerPlayer, 45.0F, 45.0F);
						this.cat.setRelaxStateOne(true);
					}
				} else {
					this.cat.setLying(false);
				}
			}
		}
	}

	static class CatTemptGoal extends TemptGoal {
		@Nullable
		private Player selectedPlayer;
		private final Cat cat;

		public CatTemptGoal(Cat cat, double d, Ingredient ingredient, boolean bl) {
			super(cat, d, ingredient, bl);
			this.cat = cat;
		}

		@Override
		public void tick() {
			super.tick();
			if (this.selectedPlayer == null && this.mob.getRandom().nextInt(600) == 0) {
				this.selectedPlayer = this.player;
			} else if (this.mob.getRandom().nextInt(500) == 0) {
				this.selectedPlayer = null;
			}
		}

		@Override
		protected boolean canScare() {
			return this.selectedPlayer != null && this.selectedPlayer.equals(this.player) ? false : super.canScare();
		}

		@Override
		public boolean canUse() {
			return super.canUse() && !this.cat.isTame();
		}
	}
}
