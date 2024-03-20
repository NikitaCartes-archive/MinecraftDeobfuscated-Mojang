package net.minecraft.world.entity.monster;

import com.google.common.collect.Sets;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ItemBasedSteering;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class Strider extends Animal implements ItemSteerable, Saddleable {
	private static final UUID SUFFOCATING_MODIFIER_UUID = UUID.fromString("9e362924-01de-4ddd-a2b2-d0f7a405a174");
	private static final AttributeModifier SUFFOCATING_MODIFIER = new AttributeModifier(
		SUFFOCATING_MODIFIER_UUID, "Strider suffocating modifier", -0.34F, AttributeModifier.Operation.ADD_MULTIPLIED_BASE
	);
	private static final float SUFFOCATE_STEERING_MODIFIER = 0.35F;
	private static final float STEERING_MODIFIER = 0.55F;
	private static final EntityDataAccessor<Integer> DATA_BOOST_TIME = SynchedEntityData.defineId(Strider.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_SUFFOCATING = SynchedEntityData.defineId(Strider.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_SADDLE_ID = SynchedEntityData.defineId(Strider.class, EntityDataSerializers.BOOLEAN);
	private final ItemBasedSteering steering = new ItemBasedSteering(this.entityData, DATA_BOOST_TIME, DATA_SADDLE_ID);
	@Nullable
	private TemptGoal temptGoal;

	public Strider(EntityType<? extends Strider> entityType, Level level) {
		super(entityType, level);
		this.blocksBuilding = true;
		this.setPathfindingMalus(PathType.WATER, -1.0F);
		this.setPathfindingMalus(PathType.LAVA, 0.0F);
		this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0F);
		this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0F);
	}

	public static boolean checkStriderSpawnRules(
		EntityType<Strider> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource
	) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

		do {
			mutableBlockPos.move(Direction.UP);
		} while (levelAccessor.getFluidState(mutableBlockPos).is(FluidTags.LAVA));

		return levelAccessor.getBlockState(mutableBlockPos).isAir();
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		if (DATA_BOOST_TIME.equals(entityDataAccessor) && this.level().isClientSide) {
			this.steering.onSynced();
		}

		super.onSyncedDataUpdated(entityDataAccessor);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_BOOST_TIME, 0);
		builder.define(DATA_SUFFOCATING, false);
		builder.define(DATA_SADDLE_ID, false);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		this.steering.addAdditionalSaveData(compoundTag);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.steering.readAdditionalSaveData(compoundTag);
	}

	@Override
	public boolean isSaddled() {
		return this.steering.hasSaddle();
	}

	@Override
	public boolean isSaddleable() {
		return this.isAlive() && !this.isBaby();
	}

	@Override
	public void equipSaddle(@Nullable SoundSource soundSource) {
		this.steering.setSaddle(true);
		if (soundSource != null) {
			this.level().playSound(null, this, SoundEvents.STRIDER_SADDLE, soundSource, 0.5F, 1.0F);
		}
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new PanicGoal(this, 1.65));
		this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
		this.temptGoal = new TemptGoal(this, 1.4, itemStack -> itemStack.is(ItemTags.STRIDER_TEMPT_ITEMS), false);
		this.goalSelector.addGoal(3, this.temptGoal);
		this.goalSelector.addGoal(4, new Strider.StriderGoToLavaGoal(this, 1.0));
		this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.0));
		this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0, 60));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Strider.class, 8.0F));
	}

	public void setSuffocating(boolean bl) {
		this.entityData.set(DATA_SUFFOCATING, bl);
		AttributeInstance attributeInstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
		if (attributeInstance != null) {
			if (bl) {
				attributeInstance.addOrUpdateTransientModifier(SUFFOCATING_MODIFIER);
			} else {
				attributeInstance.removeModifier(SUFFOCATING_MODIFIER);
			}
		}
	}

	public boolean isSuffocating() {
		return this.entityData.get(DATA_SUFFOCATING);
	}

	@Override
	public boolean canStandOnFluid(FluidState fluidState) {
		return fluidState.is(FluidTags.LAVA);
	}

	@Override
	protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions entityDimensions, float f) {
		float g = Math.min(0.25F, this.walkAnimation.speed());
		float h = this.walkAnimation.position();
		float i = 0.12F * Mth.cos(h * 1.5F) * 2.0F * g;
		return super.getPassengerAttachmentPoint(entity, entityDimensions, f).add(0.0, (double)(i * f), 0.0);
	}

	@Override
	public boolean checkSpawnObstruction(LevelReader levelReader) {
		return levelReader.isUnobstructed(this);
	}

	@Nullable
	@Override
	public LivingEntity getControllingPassenger() {
		return (LivingEntity)(this.isSaddled() && this.getFirstPassenger() instanceof Player player && player.isHolding(Items.WARPED_FUNGUS_ON_A_STICK)
			? player
			: super.getControllingPassenger());
	}

	@Override
	public Vec3 getDismountLocationForPassenger(LivingEntity livingEntity) {
		Vec3[] vec3s = new Vec3[]{
			getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)livingEntity.getBbWidth(), livingEntity.getYRot()),
			getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)livingEntity.getBbWidth(), livingEntity.getYRot() - 22.5F),
			getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)livingEntity.getBbWidth(), livingEntity.getYRot() + 22.5F),
			getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)livingEntity.getBbWidth(), livingEntity.getYRot() - 45.0F),
			getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)livingEntity.getBbWidth(), livingEntity.getYRot() + 45.0F)
		};
		Set<BlockPos> set = Sets.<BlockPos>newLinkedHashSet();
		double d = this.getBoundingBox().maxY;
		double e = this.getBoundingBox().minY - 0.5;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (Vec3 vec3 : vec3s) {
			mutableBlockPos.set(this.getX() + vec3.x, d, this.getZ() + vec3.z);

			for (double f = d; f > e; f--) {
				set.add(mutableBlockPos.immutable());
				mutableBlockPos.move(Direction.DOWN);
			}
		}

		for (BlockPos blockPos : set) {
			if (!this.level().getFluidState(blockPos).is(FluidTags.LAVA)) {
				double g = this.level().getBlockFloorHeight(blockPos);
				if (DismountHelper.isBlockFloorValid(g)) {
					Vec3 vec32 = Vec3.upFromBottomCenterOf(blockPos, g);

					for (Pose pose : livingEntity.getDismountPoses()) {
						AABB aABB = livingEntity.getLocalBoundsForPose(pose);
						if (DismountHelper.canDismountTo(this.level(), livingEntity, aABB.move(vec32))) {
							livingEntity.setPose(pose);
							return vec32;
						}
					}
				}
			}
		}

		return new Vec3(this.getX(), this.getBoundingBox().maxY, this.getZ());
	}

	@Override
	protected void tickRidden(Player player, Vec3 vec3) {
		this.setRot(player.getYRot(), player.getXRot() * 0.5F);
		this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
		this.steering.tickBoost();
		super.tickRidden(player, vec3);
	}

	@Override
	protected Vec3 getRiddenInput(Player player, Vec3 vec3) {
		return new Vec3(0.0, 0.0, 1.0);
	}

	@Override
	protected float getRiddenSpeed(Player player) {
		return (float)(this.getAttributeValue(Attributes.MOVEMENT_SPEED) * (double)(this.isSuffocating() ? 0.35F : 0.55F) * (double)this.steering.boostFactor());
	}

	@Override
	protected float nextStep() {
		return this.moveDist + 0.6F;
	}

	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
		this.playSound(this.isInLava() ? SoundEvents.STRIDER_STEP_LAVA : SoundEvents.STRIDER_STEP, 1.0F, 1.0F);
	}

	@Override
	public boolean boost() {
		return this.steering.boost(this.getRandom());
	}

	@Override
	protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {
		this.checkInsideBlocks();
		if (this.isInLava()) {
			this.resetFallDistance();
		} else {
			super.checkFallDamage(d, bl, blockState, blockPos);
		}
	}

	@Override
	public void tick() {
		if (this.isBeingTempted() && this.random.nextInt(140) == 0) {
			this.makeSound(SoundEvents.STRIDER_HAPPY);
		} else if (this.isPanicking() && this.random.nextInt(60) == 0) {
			this.makeSound(SoundEvents.STRIDER_RETREAT);
		}

		if (!this.isNoAi()) {
			boolean bl;
			boolean var10000;
			label36: {
				BlockState blockState = this.level().getBlockState(this.blockPosition());
				BlockState blockState2 = this.getBlockStateOnLegacy();
				bl = blockState.is(BlockTags.STRIDER_WARM_BLOCKS) || blockState2.is(BlockTags.STRIDER_WARM_BLOCKS) || this.getFluidHeight(FluidTags.LAVA) > 0.0;
				if (this.getVehicle() instanceof Strider strider && strider.isSuffocating()) {
					var10000 = true;
					break label36;
				}

				var10000 = false;
			}

			boolean bl2 = var10000;
			this.setSuffocating(!bl || bl2);
		}

		super.tick();
		this.floatStrider();
		this.checkInsideBlocks();
	}

	private boolean isBeingTempted() {
		return this.temptGoal != null && this.temptGoal.isRunning();
	}

	@Override
	protected boolean shouldPassengersInheritMalus() {
		return true;
	}

	private void floatStrider() {
		if (this.isInLava()) {
			CollisionContext collisionContext = CollisionContext.of(this);
			if (collisionContext.isAbove(LiquidBlock.STABLE_SHAPE, this.blockPosition(), true)
				&& !this.level().getFluidState(this.blockPosition().above()).is(FluidTags.LAVA)) {
				this.setOnGround(true);
			} else {
				this.setDeltaMovement(this.getDeltaMovement().scale(0.5).add(0.0, 0.05, 0.0));
			}
		}
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.175F).add(Attributes.FOLLOW_RANGE, 16.0);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return !this.isPanicking() && !this.isBeingTempted() ? SoundEvents.STRIDER_AMBIENT : null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.STRIDER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.STRIDER_DEATH;
	}

	@Override
	protected boolean canAddPassenger(Entity entity) {
		return !this.isVehicle() && !this.isEyeInFluid(FluidTags.LAVA);
	}

	@Override
	public boolean isSensitiveToWater() {
		return true;
	}

	@Override
	public boolean isOnFire() {
		return false;
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		return new Strider.StriderPathNavigation(this, level);
	}

	@Override
	public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
		if (levelReader.getBlockState(blockPos).getFluidState().is(FluidTags.LAVA)) {
			return 10.0F;
		} else {
			return this.isInLava() ? Float.NEGATIVE_INFINITY : 0.0F;
		}
	}

	@Nullable
	public Strider getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
		return EntityType.STRIDER.create(serverLevel);
	}

	@Override
	public boolean isFood(ItemStack itemStack) {
		return itemStack.is(ItemTags.STRIDER_FOOD);
	}

	@Override
	protected void dropEquipment() {
		super.dropEquipment();
		if (this.isSaddled()) {
			this.spawnAtLocation(Items.SADDLE);
		}
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
		boolean bl = this.isFood(player.getItemInHand(interactionHand));
		if (!bl && this.isSaddled() && !this.isVehicle() && !player.isSecondaryUseActive()) {
			if (!this.level().isClientSide) {
				player.startRiding(this);
			}

			return InteractionResult.sidedSuccess(this.level().isClientSide);
		} else {
			InteractionResult interactionResult = super.mobInteract(player, interactionHand);
			if (!interactionResult.consumesAction()) {
				ItemStack itemStack = player.getItemInHand(interactionHand);
				return itemStack.is(Items.SADDLE) ? itemStack.interactLivingEntity(player, this, interactionHand) : InteractionResult.PASS;
			} else {
				if (bl && !this.isSilent()) {
					this.level()
						.playSound(
							null,
							this.getX(),
							this.getY(),
							this.getZ(),
							SoundEvents.STRIDER_EAT,
							this.getSoundSource(),
							1.0F,
							1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
						);
				}

				return interactionResult;
			}
		}
	}

	@Override
	public Vec3 getLeashOffset() {
		return new Vec3(0.0, (double)(0.6F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData
	) {
		if (this.isBaby()) {
			return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
		} else {
			RandomSource randomSource = serverLevelAccessor.getRandom();
			if (randomSource.nextInt(30) == 0) {
				Mob mob = EntityType.ZOMBIFIED_PIGLIN.create(serverLevelAccessor.getLevel());
				if (mob != null) {
					spawnGroupData = this.spawnJockey(serverLevelAccessor, difficultyInstance, mob, new Zombie.ZombieGroupData(Zombie.getSpawnAsBabyOdds(randomSource), false));
					mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.WARPED_FUNGUS_ON_A_STICK));
					this.equipSaddle(null);
				}
			} else if (randomSource.nextInt(10) == 0) {
				AgeableMob ageableMob = EntityType.STRIDER.create(serverLevelAccessor.getLevel());
				if (ageableMob != null) {
					ageableMob.setAge(-24000);
					spawnGroupData = this.spawnJockey(serverLevelAccessor, difficultyInstance, ageableMob, null);
				}
			} else {
				spawnGroupData = new AgeableMob.AgeableMobGroupData(0.5F);
			}

			return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
		}
	}

	private SpawnGroupData spawnJockey(
		ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, Mob mob, @Nullable SpawnGroupData spawnGroupData
	) {
		mob.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
		mob.finalizeSpawn(serverLevelAccessor, difficultyInstance, MobSpawnType.JOCKEY, spawnGroupData);
		mob.startRiding(this, true);
		return new AgeableMob.AgeableMobGroupData(0.0F);
	}

	static class StriderGoToLavaGoal extends MoveToBlockGoal {
		private final Strider strider;

		StriderGoToLavaGoal(Strider strider, double d) {
			super(strider, d, 8, 2);
			this.strider = strider;
		}

		@Override
		public BlockPos getMoveToTarget() {
			return this.blockPos;
		}

		@Override
		public boolean canContinueToUse() {
			return !this.strider.isInLava() && this.isValidTarget(this.strider.level(), this.blockPos);
		}

		@Override
		public boolean canUse() {
			return !this.strider.isInLava() && super.canUse();
		}

		@Override
		public boolean shouldRecalculatePath() {
			return this.tryTicks % 20 == 0;
		}

		@Override
		protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
			return levelReader.getBlockState(blockPos).is(Blocks.LAVA) && levelReader.getBlockState(blockPos.above()).isPathfindable(PathComputationType.LAND);
		}
	}

	static class StriderPathNavigation extends GroundPathNavigation {
		StriderPathNavigation(Strider strider, Level level) {
			super(strider, level);
		}

		@Override
		protected PathFinder createPathFinder(int i) {
			this.nodeEvaluator = new WalkNodeEvaluator();
			this.nodeEvaluator.setCanPassDoors(true);
			return new PathFinder(this.nodeEvaluator, i);
		}

		@Override
		protected boolean hasValidPathType(PathType pathType) {
			return pathType != PathType.LAVA && pathType != PathType.DAMAGE_FIRE && pathType != PathType.DANGER_FIRE ? super.hasValidPathType(pathType) : true;
		}

		@Override
		public boolean isStableDestination(BlockPos blockPos) {
			return this.level.getBlockState(blockPos).is(Blocks.LAVA) || super.isStableDestination(blockPos);
		}
	}
}
