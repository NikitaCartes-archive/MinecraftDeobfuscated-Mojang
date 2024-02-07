package net.minecraft.world.entity.animal;

import com.google.common.collect.Lists;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.ClimbOnTopOfPowderSnowGoal;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.JumpGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.StrollThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

public class Fox extends Animal implements VariantHolder<Fox.Type> {
	private static final EntityDataAccessor<Integer> DATA_TYPE_ID = SynchedEntityData.defineId(Fox.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Fox.class, EntityDataSerializers.BYTE);
	private static final int FLAG_SITTING = 1;
	public static final int FLAG_CROUCHING = 4;
	public static final int FLAG_INTERESTED = 8;
	public static final int FLAG_POUNCING = 16;
	private static final int FLAG_SLEEPING = 32;
	private static final int FLAG_FACEPLANTED = 64;
	private static final int FLAG_DEFENDING = 128;
	private static final EntityDataAccessor<Optional<UUID>> DATA_TRUSTED_ID_0 = SynchedEntityData.defineId(Fox.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<Optional<UUID>> DATA_TRUSTED_ID_1 = SynchedEntityData.defineId(Fox.class, EntityDataSerializers.OPTIONAL_UUID);
	static final Predicate<ItemEntity> ALLOWED_ITEMS = itemEntity -> !itemEntity.hasPickUpDelay() && itemEntity.isAlive();
	private static final Predicate<Entity> TRUSTED_TARGET_SELECTOR = entity -> !(entity instanceof LivingEntity livingEntity)
			? false
			: livingEntity.getLastHurtMob() != null && livingEntity.getLastHurtMobTimestamp() < livingEntity.tickCount + 600;
	static final Predicate<Entity> STALKABLE_PREY = entity -> entity instanceof Chicken || entity instanceof Rabbit;
	private static final Predicate<Entity> AVOID_PLAYERS = entity -> !entity.isDiscrete() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity);
	private static final int MIN_TICKS_BEFORE_EAT = 600;
	private static final EntityDimensions BABY_DIMENSIONS = EntityType.FOX.getDimensions().scale(0.5F).withEyeHeight(0.2975F);
	private Goal landTargetGoal;
	private Goal turtleEggTargetGoal;
	private Goal fishTargetGoal;
	private float interestedAngle;
	private float interestedAngleO;
	float crouchAmount;
	float crouchAmountO;
	private int ticksSinceEaten;

	public Fox(EntityType<? extends Fox> entityType, Level level) {
		super(entityType, level);
		this.lookControl = new Fox.FoxLookControl();
		this.moveControl = new Fox.FoxMoveControl();
		this.setPathfindingMalus(BlockPathTypes.DANGER_OTHER, 0.0F);
		this.setPathfindingMalus(BlockPathTypes.DAMAGE_OTHER, 0.0F);
		this.setCanPickUpLoot(true);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_TRUSTED_ID_0, Optional.empty());
		builder.define(DATA_TRUSTED_ID_1, Optional.empty());
		builder.define(DATA_TYPE_ID, 0);
		builder.define(DATA_FLAGS_ID, (byte)0);
	}

	@Override
	protected void registerGoals() {
		this.landTargetGoal = new NearestAttackableTargetGoal(
			this, Animal.class, 10, false, false, livingEntity -> livingEntity instanceof Chicken || livingEntity instanceof Rabbit
		);
		this.turtleEggTargetGoal = new NearestAttackableTargetGoal(this, Turtle.class, 10, false, false, Turtle.BABY_ON_LAND_SELECTOR);
		this.fishTargetGoal = new NearestAttackableTargetGoal(
			this, AbstractFish.class, 20, false, false, livingEntity -> livingEntity instanceof AbstractSchoolingFish
		);
		this.goalSelector.addGoal(0, new Fox.FoxFloatGoal());
		this.goalSelector.addGoal(0, new ClimbOnTopOfPowderSnowGoal(this, this.level()));
		this.goalSelector.addGoal(1, new Fox.FaceplantGoal());
		this.goalSelector.addGoal(2, new Fox.FoxPanicGoal(2.2));
		this.goalSelector.addGoal(3, new Fox.FoxBreedGoal(1.0));
		this.goalSelector
			.addGoal(
				4,
				new AvoidEntityGoal(
					this, Player.class, 16.0F, 1.6, 1.4, livingEntity -> AVOID_PLAYERS.test(livingEntity) && !this.trusts(livingEntity.getUUID()) && !this.isDefending()
				)
			);
		this.goalSelector.addGoal(4, new AvoidEntityGoal(this, Wolf.class, 8.0F, 1.6, 1.4, livingEntity -> !((Wolf)livingEntity).isTame() && !this.isDefending()));
		this.goalSelector.addGoal(4, new AvoidEntityGoal(this, PolarBear.class, 8.0F, 1.6, 1.4, livingEntity -> !this.isDefending()));
		this.goalSelector.addGoal(5, new Fox.StalkPreyGoal());
		this.goalSelector.addGoal(6, new Fox.FoxPounceGoal());
		this.goalSelector.addGoal(6, new Fox.SeekShelterGoal(1.25));
		this.goalSelector.addGoal(7, new Fox.FoxMeleeAttackGoal(1.2F, true));
		this.goalSelector.addGoal(7, new Fox.SleepGoal());
		this.goalSelector.addGoal(8, new Fox.FoxFollowParentGoal(this, 1.25));
		this.goalSelector.addGoal(9, new Fox.FoxStrollThroughVillageGoal(32, 200));
		this.goalSelector.addGoal(10, new Fox.FoxEatBerriesGoal(1.2F, 12, 1));
		this.goalSelector.addGoal(10, new LeapAtTargetGoal(this, 0.4F));
		this.goalSelector.addGoal(11, new WaterAvoidingRandomStrollGoal(this, 1.0));
		this.goalSelector.addGoal(11, new Fox.FoxSearchForItemsGoal());
		this.goalSelector.addGoal(12, new Fox.FoxLookAtPlayerGoal(this, Player.class, 24.0F));
		this.goalSelector.addGoal(13, new Fox.PerchAndSearchGoal());
		this.targetSelector
			.addGoal(
				3,
				new Fox.DefendTrustedTargetGoal(
					LivingEntity.class, false, false, livingEntity -> TRUSTED_TARGET_SELECTOR.test(livingEntity) && !this.trusts(livingEntity.getUUID())
				)
			);
	}

	@Override
	public SoundEvent getEatingSound(ItemStack itemStack) {
		return SoundEvents.FOX_EAT;
	}

	@Override
	public void aiStep() {
		if (!this.level().isClientSide && this.isAlive() && this.isEffectiveAi()) {
			this.ticksSinceEaten++;
			ItemStack itemStack = this.getItemBySlot(EquipmentSlot.MAINHAND);
			if (this.canEat(itemStack)) {
				if (this.ticksSinceEaten > 600) {
					ItemStack itemStack2 = itemStack.finishUsingItem(this.level(), this);
					if (!itemStack2.isEmpty()) {
						this.setItemSlot(EquipmentSlot.MAINHAND, itemStack2);
					}

					this.ticksSinceEaten = 0;
				} else if (this.ticksSinceEaten > 560 && this.random.nextFloat() < 0.1F) {
					this.playSound(this.getEatingSound(itemStack), 1.0F, 1.0F);
					this.level().broadcastEntityEvent(this, (byte)45);
				}
			}

			LivingEntity livingEntity = this.getTarget();
			if (livingEntity == null || !livingEntity.isAlive()) {
				this.setIsCrouching(false);
				this.setIsInterested(false);
			}
		}

		if (this.isSleeping() || this.isImmobile()) {
			this.jumping = false;
			this.xxa = 0.0F;
			this.zza = 0.0F;
		}

		super.aiStep();
		if (this.isDefending() && this.random.nextFloat() < 0.05F) {
			this.playSound(SoundEvents.FOX_AGGRO, 1.0F, 1.0F);
		}
	}

	@Override
	protected boolean isImmobile() {
		return this.isDeadOrDying();
	}

	private boolean canEat(ItemStack itemStack) {
		return itemStack.getItem().isEdible() && this.getTarget() == null && this.onGround() && !this.isSleeping();
	}

	@Override
	protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
		if (randomSource.nextFloat() < 0.2F) {
			float f = randomSource.nextFloat();
			ItemStack itemStack;
			if (f < 0.05F) {
				itemStack = new ItemStack(Items.EMERALD);
			} else if (f < 0.2F) {
				itemStack = new ItemStack(Items.EGG);
			} else if (f < 0.4F) {
				itemStack = randomSource.nextBoolean() ? new ItemStack(Items.RABBIT_FOOT) : new ItemStack(Items.RABBIT_HIDE);
			} else if (f < 0.6F) {
				itemStack = new ItemStack(Items.WHEAT);
			} else if (f < 0.8F) {
				itemStack = new ItemStack(Items.LEATHER);
			} else {
				itemStack = new ItemStack(Items.FEATHER);
			}

			this.setItemSlot(EquipmentSlot.MAINHAND, itemStack);
		}
	}

	@Override
	public void handleEntityEvent(byte b) {
		if (b == 45) {
			ItemStack itemStack = this.getItemBySlot(EquipmentSlot.MAINHAND);
			if (!itemStack.isEmpty()) {
				for (int i = 0; i < 8; i++) {
					Vec3 vec3 = new Vec3(((double)this.random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0)
						.xRot(-this.getXRot() * (float) (Math.PI / 180.0))
						.yRot(-this.getYRot() * (float) (Math.PI / 180.0));
					this.level()
						.addParticle(
							new ItemParticleOption(ParticleTypes.ITEM, itemStack),
							this.getX() + this.getLookAngle().x / 2.0,
							this.getY(),
							this.getZ() + this.getLookAngle().z / 2.0,
							vec3.x,
							vec3.y + 0.05,
							vec3.z
						);
				}
			}
		} else {
			super.handleEntityEvent(b);
		}
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
			.add(Attributes.MOVEMENT_SPEED, 0.3F)
			.add(Attributes.MAX_HEALTH, 10.0)
			.add(Attributes.FOLLOW_RANGE, 32.0)
			.add(Attributes.ATTACK_DAMAGE, 2.0);
	}

	@Nullable
	public Fox getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
		Fox fox = EntityType.FOX.create(serverLevel);
		if (fox != null) {
			fox.setVariant(this.random.nextBoolean() ? this.getVariant() : ((Fox)ageableMob).getVariant());
		}

		return fox;
	}

	public static boolean checkFoxSpawnRules(
		EntityType<Fox> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource
	) {
		return levelAccessor.getBlockState(blockPos.below()).is(BlockTags.FOXES_SPAWNABLE_ON) && isBrightEnoughToSpawn(levelAccessor, blockPos);
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData
	) {
		Holder<Biome> holder = serverLevelAccessor.getBiome(this.blockPosition());
		Fox.Type type = Fox.Type.byBiome(holder);
		boolean bl = false;
		if (spawnGroupData instanceof Fox.FoxGroupData foxGroupData) {
			type = foxGroupData.type;
			if (foxGroupData.getGroupSize() >= 2) {
				bl = true;
			}
		} else {
			spawnGroupData = new Fox.FoxGroupData(type);
		}

		this.setVariant(type);
		if (bl) {
			this.setAge(-24000);
		}

		if (serverLevelAccessor instanceof ServerLevel) {
			this.setTargetGoals();
		}

		this.populateDefaultEquipmentSlots(serverLevelAccessor.getRandom(), difficultyInstance);
		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
	}

	private void setTargetGoals() {
		if (this.getVariant() == Fox.Type.RED) {
			this.targetSelector.addGoal(4, this.landTargetGoal);
			this.targetSelector.addGoal(4, this.turtleEggTargetGoal);
			this.targetSelector.addGoal(6, this.fishTargetGoal);
		} else {
			this.targetSelector.addGoal(4, this.fishTargetGoal);
			this.targetSelector.addGoal(6, this.landTargetGoal);
			this.targetSelector.addGoal(6, this.turtleEggTargetGoal);
		}
	}

	@Override
	protected void usePlayerItem(Player player, InteractionHand interactionHand, ItemStack itemStack) {
		if (this.isFood(itemStack)) {
			this.playSound(this.getEatingSound(itemStack), 1.0F, 1.0F);
		}

		super.usePlayerItem(player, interactionHand, itemStack);
	}

	@Override
	public EntityDimensions getDefaultDimensions(Pose pose) {
		return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions(pose);
	}

	public Fox.Type getVariant() {
		return Fox.Type.byId(this.entityData.get(DATA_TYPE_ID));
	}

	public void setVariant(Fox.Type type) {
		this.entityData.set(DATA_TYPE_ID, type.getId());
	}

	List<UUID> getTrustedUUIDs() {
		List<UUID> list = Lists.<UUID>newArrayList();
		list.add((UUID)this.entityData.get(DATA_TRUSTED_ID_0).orElse(null));
		list.add((UUID)this.entityData.get(DATA_TRUSTED_ID_1).orElse(null));
		return list;
	}

	void addTrustedUUID(@Nullable UUID uUID) {
		if (this.entityData.get(DATA_TRUSTED_ID_0).isPresent()) {
			this.entityData.set(DATA_TRUSTED_ID_1, Optional.ofNullable(uUID));
		} else {
			this.entityData.set(DATA_TRUSTED_ID_0, Optional.ofNullable(uUID));
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		List<UUID> list = this.getTrustedUUIDs();
		ListTag listTag = new ListTag();

		for (UUID uUID : list) {
			if (uUID != null) {
				listTag.add(NbtUtils.createUUID(uUID));
			}
		}

		compoundTag.put("Trusted", listTag);
		compoundTag.putBoolean("Sleeping", this.isSleeping());
		compoundTag.putString("Type", this.getVariant().getSerializedName());
		compoundTag.putBoolean("Sitting", this.isSitting());
		compoundTag.putBoolean("Crouching", this.isCrouching());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);

		for (Tag tag : compoundTag.getList("Trusted", 11)) {
			this.addTrustedUUID(NbtUtils.loadUUID(tag));
		}

		this.setSleeping(compoundTag.getBoolean("Sleeping"));
		this.setVariant(Fox.Type.byName(compoundTag.getString("Type")));
		this.setSitting(compoundTag.getBoolean("Sitting"));
		this.setIsCrouching(compoundTag.getBoolean("Crouching"));
		if (this.level() instanceof ServerLevel) {
			this.setTargetGoals();
		}
	}

	public boolean isSitting() {
		return this.getFlag(1);
	}

	public void setSitting(boolean bl) {
		this.setFlag(1, bl);
	}

	public boolean isFaceplanted() {
		return this.getFlag(64);
	}

	void setFaceplanted(boolean bl) {
		this.setFlag(64, bl);
	}

	boolean isDefending() {
		return this.getFlag(128);
	}

	void setDefending(boolean bl) {
		this.setFlag(128, bl);
	}

	@Override
	public boolean isSleeping() {
		return this.getFlag(32);
	}

	void setSleeping(boolean bl) {
		this.setFlag(32, bl);
	}

	private void setFlag(int i, boolean bl) {
		if (bl) {
			this.entityData.set(DATA_FLAGS_ID, (byte)(this.entityData.get(DATA_FLAGS_ID) | i));
		} else {
			this.entityData.set(DATA_FLAGS_ID, (byte)(this.entityData.get(DATA_FLAGS_ID) & ~i));
		}
	}

	private boolean getFlag(int i) {
		return (this.entityData.get(DATA_FLAGS_ID) & i) != 0;
	}

	@Override
	public boolean canTakeItem(ItemStack itemStack) {
		EquipmentSlot equipmentSlot = Mob.getEquipmentSlotForItem(itemStack);
		return !this.getItemBySlot(equipmentSlot).isEmpty() ? false : equipmentSlot == EquipmentSlot.MAINHAND && super.canTakeItem(itemStack);
	}

	@Override
	public boolean canHoldItem(ItemStack itemStack) {
		Item item = itemStack.getItem();
		ItemStack itemStack2 = this.getItemBySlot(EquipmentSlot.MAINHAND);
		return itemStack2.isEmpty() || this.ticksSinceEaten > 0 && item.isEdible() && !itemStack2.getItem().isEdible();
	}

	private void spitOutItem(ItemStack itemStack) {
		if (!itemStack.isEmpty() && !this.level().isClientSide) {
			ItemEntity itemEntity = new ItemEntity(this.level(), this.getX() + this.getLookAngle().x, this.getY() + 1.0, this.getZ() + this.getLookAngle().z, itemStack);
			itemEntity.setPickUpDelay(40);
			itemEntity.setThrower(this);
			this.playSound(SoundEvents.FOX_SPIT, 1.0F, 1.0F);
			this.level().addFreshEntity(itemEntity);
		}
	}

	private void dropItemStack(ItemStack itemStack) {
		ItemEntity itemEntity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), itemStack);
		this.level().addFreshEntity(itemEntity);
	}

	@Override
	protected void pickUpItem(ItemEntity itemEntity) {
		ItemStack itemStack = itemEntity.getItem();
		if (this.canHoldItem(itemStack)) {
			int i = itemStack.getCount();
			if (i > 1) {
				this.dropItemStack(itemStack.split(i - 1));
			}

			this.spitOutItem(this.getItemBySlot(EquipmentSlot.MAINHAND));
			this.onItemPickup(itemEntity);
			this.setItemSlot(EquipmentSlot.MAINHAND, itemStack.split(1));
			this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
			this.take(itemEntity, itemStack.getCount());
			itemEntity.discard();
			this.ticksSinceEaten = 0;
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (this.isEffectiveAi()) {
			boolean bl = this.isInWater();
			if (bl || this.getTarget() != null || this.level().isThundering()) {
				this.wakeUp();
			}

			if (bl || this.isSleeping()) {
				this.setSitting(false);
			}

			if (this.isFaceplanted() && this.level().random.nextFloat() < 0.2F) {
				BlockPos blockPos = this.blockPosition();
				BlockState blockState = this.level().getBlockState(blockPos);
				this.level().levelEvent(2001, blockPos, Block.getId(blockState));
			}
		}

		this.interestedAngleO = this.interestedAngle;
		if (this.isInterested()) {
			this.interestedAngle = this.interestedAngle + (1.0F - this.interestedAngle) * 0.4F;
		} else {
			this.interestedAngle = this.interestedAngle + (0.0F - this.interestedAngle) * 0.4F;
		}

		this.crouchAmountO = this.crouchAmount;
		if (this.isCrouching()) {
			this.crouchAmount += 0.2F;
			if (this.crouchAmount > 3.0F) {
				this.crouchAmount = 3.0F;
			}
		} else {
			this.crouchAmount = 0.0F;
		}
	}

	@Override
	public boolean isFood(ItemStack itemStack) {
		return itemStack.is(ItemTags.FOX_FOOD);
	}

	@Override
	protected void onOffspringSpawnedFromEgg(Player player, Mob mob) {
		((Fox)mob).addTrustedUUID(player.getUUID());
	}

	public boolean isPouncing() {
		return this.getFlag(16);
	}

	public void setIsPouncing(boolean bl) {
		this.setFlag(16, bl);
	}

	public boolean isJumping() {
		return this.jumping;
	}

	public boolean isFullyCrouched() {
		return this.crouchAmount == 3.0F;
	}

	public void setIsCrouching(boolean bl) {
		this.setFlag(4, bl);
	}

	@Override
	public boolean isCrouching() {
		return this.getFlag(4);
	}

	public void setIsInterested(boolean bl) {
		this.setFlag(8, bl);
	}

	public boolean isInterested() {
		return this.getFlag(8);
	}

	public float getHeadRollAngle(float f) {
		return Mth.lerp(f, this.interestedAngleO, this.interestedAngle) * 0.11F * (float) Math.PI;
	}

	public float getCrouchAmount(float f) {
		return Mth.lerp(f, this.crouchAmountO, this.crouchAmount);
	}

	@Override
	public void setTarget(@Nullable LivingEntity livingEntity) {
		if (this.isDefending() && livingEntity == null) {
			this.setDefending(false);
		}

		super.setTarget(livingEntity);
	}

	@Override
	protected int calculateFallDamage(float f, float g) {
		return Mth.ceil((f - 5.0F) * g);
	}

	void wakeUp() {
		this.setSleeping(false);
	}

	void clearStates() {
		this.setIsInterested(false);
		this.setIsCrouching(false);
		this.setSitting(false);
		this.setSleeping(false);
		this.setDefending(false);
		this.setFaceplanted(false);
	}

	boolean canMove() {
		return !this.isSleeping() && !this.isSitting() && !this.isFaceplanted();
	}

	@Override
	public void playAmbientSound() {
		SoundEvent soundEvent = this.getAmbientSound();
		if (soundEvent == SoundEvents.FOX_SCREECH) {
			this.playSound(soundEvent, 2.0F, this.getVoicePitch());
		} else {
			super.playAmbientSound();
		}
	}

	@Nullable
	@Override
	protected SoundEvent getAmbientSound() {
		if (this.isSleeping()) {
			return SoundEvents.FOX_SLEEP;
		} else {
			if (!this.level().isDay() && this.random.nextFloat() < 0.1F) {
				List<Player> list = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(16.0, 16.0, 16.0), EntitySelector.NO_SPECTATORS);
				if (list.isEmpty()) {
					return SoundEvents.FOX_SCREECH;
				}
			}

			return SoundEvents.FOX_AMBIENT;
		}
	}

	@Nullable
	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.FOX_HURT;
	}

	@Nullable
	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.FOX_DEATH;
	}

	boolean trusts(UUID uUID) {
		return this.getTrustedUUIDs().contains(uUID);
	}

	@Override
	protected void dropAllDeathLoot(DamageSource damageSource) {
		ItemStack itemStack = this.getItemBySlot(EquipmentSlot.MAINHAND);
		if (!itemStack.isEmpty()) {
			this.spawnAtLocation(itemStack);
			this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
		}

		super.dropAllDeathLoot(damageSource);
	}

	public static boolean isPathClear(Fox fox, LivingEntity livingEntity) {
		double d = livingEntity.getZ() - fox.getZ();
		double e = livingEntity.getX() - fox.getX();
		double f = d / e;
		int i = 6;

		for (int j = 0; j < 6; j++) {
			double g = f == 0.0 ? 0.0 : d * (double)((float)j / 6.0F);
			double h = f == 0.0 ? e * (double)((float)j / 6.0F) : g / f;

			for (int k = 1; k < 4; k++) {
				if (!fox.level().getBlockState(BlockPos.containing(fox.getX() + h, fox.getY() + (double)k, fox.getZ() + g)).canBeReplaced()) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public Vec3 getLeashOffset() {
		return new Vec3(0.0, (double)(0.55F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
	}

	class DefendTrustedTargetGoal extends NearestAttackableTargetGoal<LivingEntity> {
		@Nullable
		private LivingEntity trustedLastHurtBy;
		@Nullable
		private LivingEntity trustedLastHurt;
		private int timestamp;

		public DefendTrustedTargetGoal(Class<LivingEntity> class_, boolean bl, boolean bl2, @Nullable Predicate<LivingEntity> predicate) {
			super(Fox.this, class_, 10, bl, bl2, predicate);
		}

		@Override
		public boolean canUse() {
			if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
				return false;
			} else {
				for (UUID uUID : Fox.this.getTrustedUUIDs()) {
					if (uUID != null && Fox.this.level() instanceof ServerLevel && ((ServerLevel)Fox.this.level()).getEntity(uUID) instanceof LivingEntity livingEntity) {
						this.trustedLastHurt = livingEntity;
						this.trustedLastHurtBy = livingEntity.getLastHurtByMob();
						int i = livingEntity.getLastHurtByMobTimestamp();
						return i != this.timestamp && this.canAttack(this.trustedLastHurtBy, this.targetConditions);
					}
				}

				return false;
			}
		}

		@Override
		public void start() {
			this.setTarget(this.trustedLastHurtBy);
			this.target = this.trustedLastHurtBy;
			if (this.trustedLastHurt != null) {
				this.timestamp = this.trustedLastHurt.getLastHurtByMobTimestamp();
			}

			Fox.this.playSound(SoundEvents.FOX_AGGRO, 1.0F, 1.0F);
			Fox.this.setDefending(true);
			Fox.this.wakeUp();
			super.start();
		}
	}

	class FaceplantGoal extends Goal {
		int countdown;

		public FaceplantGoal() {
			this.setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.JUMP, Goal.Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			return Fox.this.isFaceplanted();
		}

		@Override
		public boolean canContinueToUse() {
			return this.canUse() && this.countdown > 0;
		}

		@Override
		public void start() {
			this.countdown = this.adjustedTickDelay(40);
		}

		@Override
		public void stop() {
			Fox.this.setFaceplanted(false);
		}

		@Override
		public void tick() {
			this.countdown--;
		}
	}

	public class FoxAlertableEntitiesSelector implements Predicate<LivingEntity> {
		public boolean test(LivingEntity livingEntity) {
			if (livingEntity instanceof Fox) {
				return false;
			} else if (livingEntity instanceof Chicken || livingEntity instanceof Rabbit || livingEntity instanceof Monster) {
				return true;
			} else if (livingEntity instanceof TamableAnimal) {
				return !((TamableAnimal)livingEntity).isTame();
			} else if (!(livingEntity instanceof Player) || !livingEntity.isSpectator() && !((Player)livingEntity).isCreative()) {
				return Fox.this.trusts(livingEntity.getUUID()) ? false : !livingEntity.isSleeping() && !livingEntity.isDiscrete();
			} else {
				return false;
			}
		}
	}

	abstract class FoxBehaviorGoal extends Goal {
		private final TargetingConditions alertableTargeting = TargetingConditions.forCombat()
			.range(12.0)
			.ignoreLineOfSight()
			.selector(Fox.this.new FoxAlertableEntitiesSelector());

		protected boolean hasShelter() {
			BlockPos blockPos = BlockPos.containing(Fox.this.getX(), Fox.this.getBoundingBox().maxY, Fox.this.getZ());
			return !Fox.this.level().canSeeSky(blockPos) && Fox.this.getWalkTargetValue(blockPos) >= 0.0F;
		}

		protected boolean alertable() {
			return !Fox.this.level()
				.getNearbyEntities(LivingEntity.class, this.alertableTargeting, Fox.this, Fox.this.getBoundingBox().inflate(12.0, 6.0, 12.0))
				.isEmpty();
		}
	}

	class FoxBreedGoal extends BreedGoal {
		public FoxBreedGoal(double d) {
			super(Fox.this, d);
		}

		@Override
		public void start() {
			((Fox)this.animal).clearStates();
			((Fox)this.partner).clearStates();
			super.start();
		}

		@Override
		protected void breed() {
			ServerLevel serverLevel = (ServerLevel)this.level;
			Fox fox = (Fox)this.animal.getBreedOffspring(serverLevel, this.partner);
			if (fox != null) {
				ServerPlayer serverPlayer = this.animal.getLoveCause();
				ServerPlayer serverPlayer2 = this.partner.getLoveCause();
				ServerPlayer serverPlayer3 = serverPlayer;
				if (serverPlayer != null) {
					fox.addTrustedUUID(serverPlayer.getUUID());
				} else {
					serverPlayer3 = serverPlayer2;
				}

				if (serverPlayer2 != null && serverPlayer != serverPlayer2) {
					fox.addTrustedUUID(serverPlayer2.getUUID());
				}

				if (serverPlayer3 != null) {
					serverPlayer3.awardStat(Stats.ANIMALS_BRED);
					CriteriaTriggers.BRED_ANIMALS.trigger(serverPlayer3, this.animal, this.partner, fox);
				}

				this.animal.setAge(6000);
				this.partner.setAge(6000);
				this.animal.resetLove();
				this.partner.resetLove();
				fox.setAge(-24000);
				fox.moveTo(this.animal.getX(), this.animal.getY(), this.animal.getZ(), 0.0F, 0.0F);
				serverLevel.addFreshEntityWithPassengers(fox);
				this.level.broadcastEntityEvent(this.animal, (byte)18);
				if (this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
					this.level
						.addFreshEntity(new ExperienceOrb(this.level, this.animal.getX(), this.animal.getY(), this.animal.getZ(), this.animal.getRandom().nextInt(7) + 1));
				}
			}
		}
	}

	public class FoxEatBerriesGoal extends MoveToBlockGoal {
		private static final int WAIT_TICKS = 40;
		protected int ticksWaited;

		public FoxEatBerriesGoal(double d, int i, int j) {
			super(Fox.this, d, i, j);
		}

		@Override
		public double acceptedDistance() {
			return 2.0;
		}

		@Override
		public boolean shouldRecalculatePath() {
			return this.tryTicks % 100 == 0;
		}

		@Override
		protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
			BlockState blockState = levelReader.getBlockState(blockPos);
			return blockState.is(Blocks.SWEET_BERRY_BUSH) && (Integer)blockState.getValue(SweetBerryBushBlock.AGE) >= 2 || CaveVines.hasGlowBerries(blockState);
		}

		@Override
		public void tick() {
			if (this.isReachedTarget()) {
				if (this.ticksWaited >= 40) {
					this.onReachedTarget();
				} else {
					this.ticksWaited++;
				}
			} else if (!this.isReachedTarget() && Fox.this.random.nextFloat() < 0.05F) {
				Fox.this.playSound(SoundEvents.FOX_SNIFF, 1.0F, 1.0F);
			}

			super.tick();
		}

		protected void onReachedTarget() {
			if (Fox.this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
				BlockState blockState = Fox.this.level().getBlockState(this.blockPos);
				if (blockState.is(Blocks.SWEET_BERRY_BUSH)) {
					this.pickSweetBerries(blockState);
				} else if (CaveVines.hasGlowBerries(blockState)) {
					this.pickGlowBerry(blockState);
				}
			}
		}

		private void pickGlowBerry(BlockState blockState) {
			CaveVines.use(Fox.this, blockState, Fox.this.level(), this.blockPos);
		}

		private void pickSweetBerries(BlockState blockState) {
			int i = (Integer)blockState.getValue(SweetBerryBushBlock.AGE);
			blockState.setValue(SweetBerryBushBlock.AGE, Integer.valueOf(1));
			int j = 1 + Fox.this.level().random.nextInt(2) + (i == 3 ? 1 : 0);
			ItemStack itemStack = Fox.this.getItemBySlot(EquipmentSlot.MAINHAND);
			if (itemStack.isEmpty()) {
				Fox.this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.SWEET_BERRIES));
				j--;
			}

			if (j > 0) {
				Block.popResource(Fox.this.level(), this.blockPos, new ItemStack(Items.SWEET_BERRIES, j));
			}

			Fox.this.playSound(SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, 1.0F, 1.0F);
			Fox.this.level().setBlock(this.blockPos, blockState.setValue(SweetBerryBushBlock.AGE, Integer.valueOf(1)), 2);
			Fox.this.level().gameEvent(GameEvent.BLOCK_CHANGE, this.blockPos, GameEvent.Context.of(Fox.this));
		}

		@Override
		public boolean canUse() {
			return !Fox.this.isSleeping() && super.canUse();
		}

		@Override
		public void start() {
			this.ticksWaited = 0;
			Fox.this.setSitting(false);
			super.start();
		}
	}

	class FoxFloatGoal extends FloatGoal {
		public FoxFloatGoal() {
			super(Fox.this);
		}

		@Override
		public void start() {
			super.start();
			Fox.this.clearStates();
		}

		@Override
		public boolean canUse() {
			return Fox.this.isInWater() && Fox.this.getFluidHeight(FluidTags.WATER) > 0.25 || Fox.this.isInLava();
		}
	}

	class FoxFollowParentGoal extends FollowParentGoal {
		private final Fox fox;

		public FoxFollowParentGoal(Fox fox2, double d) {
			super(fox2, d);
			this.fox = fox2;
		}

		@Override
		public boolean canUse() {
			return !this.fox.isDefending() && super.canUse();
		}

		@Override
		public boolean canContinueToUse() {
			return !this.fox.isDefending() && super.canContinueToUse();
		}

		@Override
		public void start() {
			this.fox.clearStates();
			super.start();
		}
	}

	public static class FoxGroupData extends AgeableMob.AgeableMobGroupData {
		public final Fox.Type type;

		public FoxGroupData(Fox.Type type) {
			super(false);
			this.type = type;
		}
	}

	class FoxLookAtPlayerGoal extends LookAtPlayerGoal {
		public FoxLookAtPlayerGoal(Mob mob, Class<? extends LivingEntity> class_, float f) {
			super(mob, class_, f);
		}

		@Override
		public boolean canUse() {
			return super.canUse() && !Fox.this.isFaceplanted() && !Fox.this.isInterested();
		}

		@Override
		public boolean canContinueToUse() {
			return super.canContinueToUse() && !Fox.this.isFaceplanted() && !Fox.this.isInterested();
		}
	}

	public class FoxLookControl extends LookControl {
		public FoxLookControl() {
			super(Fox.this);
		}

		@Override
		public void tick() {
			if (!Fox.this.isSleeping()) {
				super.tick();
			}
		}

		@Override
		protected boolean resetXRotOnTick() {
			return !Fox.this.isPouncing() && !Fox.this.isCrouching() && !Fox.this.isInterested() && !Fox.this.isFaceplanted();
		}
	}

	class FoxMeleeAttackGoal extends MeleeAttackGoal {
		public FoxMeleeAttackGoal(double d, boolean bl) {
			super(Fox.this, d, bl);
		}

		@Override
		protected void checkAndPerformAttack(LivingEntity livingEntity) {
			if (this.canPerformAttack(livingEntity)) {
				this.resetAttackCooldown();
				this.mob.doHurtTarget(livingEntity);
				Fox.this.playSound(SoundEvents.FOX_BITE, 1.0F, 1.0F);
			}
		}

		@Override
		public void start() {
			Fox.this.setIsInterested(false);
			super.start();
		}

		@Override
		public boolean canUse() {
			return !Fox.this.isSitting() && !Fox.this.isSleeping() && !Fox.this.isCrouching() && !Fox.this.isFaceplanted() && super.canUse();
		}
	}

	class FoxMoveControl extends MoveControl {
		public FoxMoveControl() {
			super(Fox.this);
		}

		@Override
		public void tick() {
			if (Fox.this.canMove()) {
				super.tick();
			}
		}
	}

	class FoxPanicGoal extends PanicGoal {
		public FoxPanicGoal(double d) {
			super(Fox.this, d);
		}

		@Override
		public boolean shouldPanic() {
			return !Fox.this.isDefending() && super.shouldPanic();
		}
	}

	public class FoxPounceGoal extends JumpGoal {
		@Override
		public boolean canUse() {
			if (!Fox.this.isFullyCrouched()) {
				return false;
			} else {
				LivingEntity livingEntity = Fox.this.getTarget();
				if (livingEntity != null && livingEntity.isAlive()) {
					if (livingEntity.getMotionDirection() != livingEntity.getDirection()) {
						return false;
					} else {
						boolean bl = Fox.isPathClear(Fox.this, livingEntity);
						if (!bl) {
							Fox.this.getNavigation().createPath(livingEntity, 0);
							Fox.this.setIsCrouching(false);
							Fox.this.setIsInterested(false);
						}

						return bl;
					}
				} else {
					return false;
				}
			}
		}

		@Override
		public boolean canContinueToUse() {
			LivingEntity livingEntity = Fox.this.getTarget();
			if (livingEntity != null && livingEntity.isAlive()) {
				double d = Fox.this.getDeltaMovement().y;
				return (!(d * d < 0.05F) || !(Math.abs(Fox.this.getXRot()) < 15.0F) || !Fox.this.onGround()) && !Fox.this.isFaceplanted();
			} else {
				return false;
			}
		}

		@Override
		public boolean isInterruptable() {
			return false;
		}

		@Override
		public void start() {
			Fox.this.setJumping(true);
			Fox.this.setIsPouncing(true);
			Fox.this.setIsInterested(false);
			LivingEntity livingEntity = Fox.this.getTarget();
			if (livingEntity != null) {
				Fox.this.getLookControl().setLookAt(livingEntity, 60.0F, 30.0F);
				Vec3 vec3 = new Vec3(livingEntity.getX() - Fox.this.getX(), livingEntity.getY() - Fox.this.getY(), livingEntity.getZ() - Fox.this.getZ()).normalize();
				Fox.this.setDeltaMovement(Fox.this.getDeltaMovement().add(vec3.x * 0.8, 0.9, vec3.z * 0.8));
			}

			Fox.this.getNavigation().stop();
		}

		@Override
		public void stop() {
			Fox.this.setIsCrouching(false);
			Fox.this.crouchAmount = 0.0F;
			Fox.this.crouchAmountO = 0.0F;
			Fox.this.setIsInterested(false);
			Fox.this.setIsPouncing(false);
		}

		@Override
		public void tick() {
			LivingEntity livingEntity = Fox.this.getTarget();
			if (livingEntity != null) {
				Fox.this.getLookControl().setLookAt(livingEntity, 60.0F, 30.0F);
			}

			if (!Fox.this.isFaceplanted()) {
				Vec3 vec3 = Fox.this.getDeltaMovement();
				if (vec3.y * vec3.y < 0.03F && Fox.this.getXRot() != 0.0F) {
					Fox.this.setXRot(Mth.rotLerp(0.2F, Fox.this.getXRot(), 0.0F));
				} else {
					double d = vec3.horizontalDistance();
					double e = Math.signum(-vec3.y) * Math.acos(d / vec3.length()) * 180.0F / (float)Math.PI;
					Fox.this.setXRot((float)e);
				}
			}

			if (livingEntity != null && Fox.this.distanceTo(livingEntity) <= 2.0F) {
				Fox.this.doHurtTarget(livingEntity);
			} else if (Fox.this.getXRot() > 0.0F
				&& Fox.this.onGround()
				&& (float)Fox.this.getDeltaMovement().y != 0.0F
				&& Fox.this.level().getBlockState(Fox.this.blockPosition()).is(Blocks.SNOW)) {
				Fox.this.setXRot(60.0F);
				Fox.this.setTarget(null);
				Fox.this.setFaceplanted(true);
			}
		}
	}

	class FoxSearchForItemsGoal extends Goal {
		public FoxSearchForItemsGoal() {
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			if (!Fox.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
				return false;
			} else if (Fox.this.getTarget() != null || Fox.this.getLastHurtByMob() != null) {
				return false;
			} else if (!Fox.this.canMove()) {
				return false;
			} else if (Fox.this.getRandom().nextInt(reducedTickDelay(10)) != 0) {
				return false;
			} else {
				List<ItemEntity> list = Fox.this.level().getEntitiesOfClass(ItemEntity.class, Fox.this.getBoundingBox().inflate(8.0, 8.0, 8.0), Fox.ALLOWED_ITEMS);
				return !list.isEmpty() && Fox.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty();
			}
		}

		@Override
		public void tick() {
			List<ItemEntity> list = Fox.this.level().getEntitiesOfClass(ItemEntity.class, Fox.this.getBoundingBox().inflate(8.0, 8.0, 8.0), Fox.ALLOWED_ITEMS);
			ItemStack itemStack = Fox.this.getItemBySlot(EquipmentSlot.MAINHAND);
			if (itemStack.isEmpty() && !list.isEmpty()) {
				Fox.this.getNavigation().moveTo((Entity)list.get(0), 1.2F);
			}
		}

		@Override
		public void start() {
			List<ItemEntity> list = Fox.this.level().getEntitiesOfClass(ItemEntity.class, Fox.this.getBoundingBox().inflate(8.0, 8.0, 8.0), Fox.ALLOWED_ITEMS);
			if (!list.isEmpty()) {
				Fox.this.getNavigation().moveTo((Entity)list.get(0), 1.2F);
			}
		}
	}

	class FoxStrollThroughVillageGoal extends StrollThroughVillageGoal {
		public FoxStrollThroughVillageGoal(int i, int j) {
			super(Fox.this, j);
		}

		@Override
		public void start() {
			Fox.this.clearStates();
			super.start();
		}

		@Override
		public boolean canUse() {
			return super.canUse() && this.canFoxMove();
		}

		@Override
		public boolean canContinueToUse() {
			return super.canContinueToUse() && this.canFoxMove();
		}

		private boolean canFoxMove() {
			return !Fox.this.isSleeping() && !Fox.this.isSitting() && !Fox.this.isDefending() && Fox.this.getTarget() == null;
		}
	}

	class PerchAndSearchGoal extends Fox.FoxBehaviorGoal {
		private double relX;
		private double relZ;
		private int lookTime;
		private int looksRemaining;

		public PerchAndSearchGoal() {
			this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			return Fox.this.getLastHurtByMob() == null
				&& Fox.this.getRandom().nextFloat() < 0.02F
				&& !Fox.this.isSleeping()
				&& Fox.this.getTarget() == null
				&& Fox.this.getNavigation().isDone()
				&& !this.alertable()
				&& !Fox.this.isPouncing()
				&& !Fox.this.isCrouching();
		}

		@Override
		public boolean canContinueToUse() {
			return this.looksRemaining > 0;
		}

		@Override
		public void start() {
			this.resetLook();
			this.looksRemaining = 2 + Fox.this.getRandom().nextInt(3);
			Fox.this.setSitting(true);
			Fox.this.getNavigation().stop();
		}

		@Override
		public void stop() {
			Fox.this.setSitting(false);
		}

		@Override
		public void tick() {
			this.lookTime--;
			if (this.lookTime <= 0) {
				this.looksRemaining--;
				this.resetLook();
			}

			Fox.this.getLookControl()
				.setLookAt(Fox.this.getX() + this.relX, Fox.this.getEyeY(), Fox.this.getZ() + this.relZ, (float)Fox.this.getMaxHeadYRot(), (float)Fox.this.getMaxHeadXRot());
		}

		private void resetLook() {
			double d = (Math.PI * 2) * Fox.this.getRandom().nextDouble();
			this.relX = Math.cos(d);
			this.relZ = Math.sin(d);
			this.lookTime = this.adjustedTickDelay(80 + Fox.this.getRandom().nextInt(20));
		}
	}

	class SeekShelterGoal extends FleeSunGoal {
		private int interval = reducedTickDelay(100);

		public SeekShelterGoal(double d) {
			super(Fox.this, d);
		}

		@Override
		public boolean canUse() {
			if (!Fox.this.isSleeping() && this.mob.getTarget() == null) {
				if (Fox.this.level().isThundering() && Fox.this.level().canSeeSky(this.mob.blockPosition())) {
					return this.setWantedPos();
				} else if (this.interval > 0) {
					this.interval--;
					return false;
				} else {
					this.interval = 100;
					BlockPos blockPos = this.mob.blockPosition();
					return Fox.this.level().isDay() && Fox.this.level().canSeeSky(blockPos) && !((ServerLevel)Fox.this.level()).isVillage(blockPos) && this.setWantedPos();
				}
			} else {
				return false;
			}
		}

		@Override
		public void start() {
			Fox.this.clearStates();
			super.start();
		}
	}

	class SleepGoal extends Fox.FoxBehaviorGoal {
		private static final int WAIT_TIME_BEFORE_SLEEP = reducedTickDelay(140);
		private int countdown = Fox.this.random.nextInt(WAIT_TIME_BEFORE_SLEEP);

		public SleepGoal() {
			this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
		}

		@Override
		public boolean canUse() {
			return Fox.this.xxa == 0.0F && Fox.this.yya == 0.0F && Fox.this.zza == 0.0F ? this.canSleep() || Fox.this.isSleeping() : false;
		}

		@Override
		public boolean canContinueToUse() {
			return this.canSleep();
		}

		private boolean canSleep() {
			if (this.countdown > 0) {
				this.countdown--;
				return false;
			} else {
				return Fox.this.level().isDay() && this.hasShelter() && !this.alertable() && !Fox.this.isInPowderSnow;
			}
		}

		@Override
		public void stop() {
			this.countdown = Fox.this.random.nextInt(WAIT_TIME_BEFORE_SLEEP);
			Fox.this.clearStates();
		}

		@Override
		public void start() {
			Fox.this.setSitting(false);
			Fox.this.setIsCrouching(false);
			Fox.this.setIsInterested(false);
			Fox.this.setJumping(false);
			Fox.this.setSleeping(true);
			Fox.this.getNavigation().stop();
			Fox.this.getMoveControl().setWantedPosition(Fox.this.getX(), Fox.this.getY(), Fox.this.getZ(), 0.0);
		}
	}

	class StalkPreyGoal extends Goal {
		public StalkPreyGoal() {
			this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			if (Fox.this.isSleeping()) {
				return false;
			} else {
				LivingEntity livingEntity = Fox.this.getTarget();
				return livingEntity != null
					&& livingEntity.isAlive()
					&& Fox.STALKABLE_PREY.test(livingEntity)
					&& Fox.this.distanceToSqr(livingEntity) > 36.0
					&& !Fox.this.isCrouching()
					&& !Fox.this.isInterested()
					&& !Fox.this.jumping;
			}
		}

		@Override
		public void start() {
			Fox.this.setSitting(false);
			Fox.this.setFaceplanted(false);
		}

		@Override
		public void stop() {
			LivingEntity livingEntity = Fox.this.getTarget();
			if (livingEntity != null && Fox.isPathClear(Fox.this, livingEntity)) {
				Fox.this.setIsInterested(true);
				Fox.this.setIsCrouching(true);
				Fox.this.getNavigation().stop();
				Fox.this.getLookControl().setLookAt(livingEntity, (float)Fox.this.getMaxHeadYRot(), (float)Fox.this.getMaxHeadXRot());
			} else {
				Fox.this.setIsInterested(false);
				Fox.this.setIsCrouching(false);
			}
		}

		@Override
		public void tick() {
			LivingEntity livingEntity = Fox.this.getTarget();
			if (livingEntity != null) {
				Fox.this.getLookControl().setLookAt(livingEntity, (float)Fox.this.getMaxHeadYRot(), (float)Fox.this.getMaxHeadXRot());
				if (Fox.this.distanceToSqr(livingEntity) <= 36.0) {
					Fox.this.setIsInterested(true);
					Fox.this.setIsCrouching(true);
					Fox.this.getNavigation().stop();
				} else {
					Fox.this.getNavigation().moveTo(livingEntity, 1.5);
				}
			}
		}
	}

	public static enum Type implements StringRepresentable {
		RED(0, "red"),
		SNOW(1, "snow");

		public static final StringRepresentable.EnumCodec<Fox.Type> CODEC = StringRepresentable.fromEnum(Fox.Type::values);
		private static final IntFunction<Fox.Type> BY_ID = ByIdMap.continuous(Fox.Type::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
		private final int id;
		private final String name;

		private Type(int j, String string2) {
			this.id = j;
			this.name = string2;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public int getId() {
			return this.id;
		}

		public static Fox.Type byName(String string) {
			return (Fox.Type)CODEC.byName(string, RED);
		}

		public static Fox.Type byId(int i) {
			return (Fox.Type)BY_ID.apply(i);
		}

		public static Fox.Type byBiome(Holder<Biome> holder) {
			return holder.is(BiomeTags.SPAWNS_SNOW_FOXES) ? SNOW : RED;
		}
	}
}
