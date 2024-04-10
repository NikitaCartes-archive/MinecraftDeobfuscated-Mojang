package net.minecraft.world.entity.animal.frog;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.AmphibiousNodeEvaluator;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.phys.Vec3;

public class Frog extends Animal implements VariantHolder<Holder<FrogVariant>> {
	protected static final ImmutableList<SensorType<? extends Sensor<? super Frog>>> SENSOR_TYPES = ImmutableList.of(
		SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, SensorType.FROG_ATTACKABLES, SensorType.FROG_TEMPTATIONS, SensorType.IS_IN_WATER
	);
	protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
		MemoryModuleType.LOOK_TARGET,
		MemoryModuleType.NEAREST_LIVING_ENTITIES,
		MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
		MemoryModuleType.WALK_TARGET,
		MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
		MemoryModuleType.PATH,
		MemoryModuleType.BREED_TARGET,
		MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS,
		MemoryModuleType.LONG_JUMP_MID_JUMP,
		MemoryModuleType.ATTACK_TARGET,
		MemoryModuleType.TEMPTING_PLAYER,
		MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
		MemoryModuleType.IS_TEMPTED,
		MemoryModuleType.HURT_BY,
		MemoryModuleType.HURT_BY_ENTITY,
		MemoryModuleType.NEAREST_ATTACKABLE,
		MemoryModuleType.IS_IN_WATER,
		MemoryModuleType.IS_PREGNANT,
		MemoryModuleType.IS_PANICKING,
		MemoryModuleType.UNREACHABLE_TONGUE_TARGETS
	);
	private static final EntityDataAccessor<Holder<FrogVariant>> DATA_VARIANT_ID = SynchedEntityData.defineId(Frog.class, EntityDataSerializers.FROG_VARIANT);
	private static final EntityDataAccessor<OptionalInt> DATA_TONGUE_TARGET_ID = SynchedEntityData.defineId(
		Frog.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT
	);
	private static final int FROG_FALL_DAMAGE_REDUCTION = 5;
	public static final String VARIANT_KEY = "variant";
	private static final ResourceKey<FrogVariant> DEFAULT_VARIANT = FrogVariant.TEMPERATE;
	public final AnimationState jumpAnimationState = new AnimationState();
	public final AnimationState croakAnimationState = new AnimationState();
	public final AnimationState tongueAnimationState = new AnimationState();
	public final AnimationState swimIdleAnimationState = new AnimationState();

	public Frog(EntityType<? extends Animal> entityType, Level level) {
		super(entityType, level);
		this.lookControl = new Frog.FrogLookControl(this);
		this.setPathfindingMalus(PathType.WATER, 4.0F);
		this.setPathfindingMalus(PathType.TRAPDOOR, -1.0F);
		this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02F, 0.1F, true);
	}

	@Override
	protected Brain.Provider<Frog> brainProvider() {
		return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
	}

	@Override
	protected Brain<?> makeBrain(Dynamic<?> dynamic) {
		return FrogAi.makeBrain(this.brainProvider().makeBrain(dynamic));
	}

	@Override
	public Brain<Frog> getBrain() {
		return (Brain<Frog>)super.getBrain();
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_VARIANT_ID, BuiltInRegistries.FROG_VARIANT.getHolderOrThrow(DEFAULT_VARIANT));
		builder.define(DATA_TONGUE_TARGET_ID, OptionalInt.empty());
	}

	public void eraseTongueTarget() {
		this.entityData.set(DATA_TONGUE_TARGET_ID, OptionalInt.empty());
	}

	public Optional<Entity> getTongueTarget() {
		return this.entityData.get(DATA_TONGUE_TARGET_ID).stream().mapToObj(this.level()::getEntity).filter(Objects::nonNull).findFirst();
	}

	public void setTongueTarget(Entity entity) {
		this.entityData.set(DATA_TONGUE_TARGET_ID, OptionalInt.of(entity.getId()));
	}

	@Override
	public int getHeadRotSpeed() {
		return 35;
	}

	@Override
	public int getMaxHeadYRot() {
		return 5;
	}

	public Holder<FrogVariant> getVariant() {
		return this.entityData.get(DATA_VARIANT_ID);
	}

	public void setVariant(Holder<FrogVariant> holder) {
		this.entityData.set(DATA_VARIANT_ID, holder);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putString("variant", ((ResourceKey)this.getVariant().unwrapKey().orElse(DEFAULT_VARIANT)).location().toString());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		Optional.ofNullable(ResourceLocation.tryParse(compoundTag.getString("variant")))
			.map(resourceLocation -> ResourceKey.create(Registries.FROG_VARIANT, resourceLocation))
			.flatMap(BuiltInRegistries.FROG_VARIANT::getHolder)
			.ifPresent(this::setVariant);
	}

	@Override
	protected void customServerAiStep() {
		this.level().getProfiler().push("frogBrain");
		this.getBrain().tick((ServerLevel)this.level(), this);
		this.level().getProfiler().pop();
		this.level().getProfiler().push("frogActivityUpdate");
		FrogAi.updateActivity(this);
		this.level().getProfiler().pop();
		super.customServerAiStep();
	}

	@Override
	public void tick() {
		if (this.level().isClientSide()) {
			this.swimIdleAnimationState.animateWhen(this.isInWaterOrBubble() && !this.walkAnimation.isMoving(), this.tickCount);
		}

		super.tick();
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		if (DATA_POSE.equals(entityDataAccessor)) {
			Pose pose = this.getPose();
			if (pose == Pose.LONG_JUMPING) {
				this.jumpAnimationState.start(this.tickCount);
			} else {
				this.jumpAnimationState.stop();
			}

			if (pose == Pose.CROAKING) {
				this.croakAnimationState.start(this.tickCount);
			} else {
				this.croakAnimationState.stop();
			}

			if (pose == Pose.USING_TONGUE) {
				this.tongueAnimationState.start(this.tickCount);
			} else {
				this.tongueAnimationState.stop();
			}
		}

		super.onSyncedDataUpdated(entityDataAccessor);
	}

	@Override
	protected void updateWalkAnimation(float f) {
		float g;
		if (this.jumpAnimationState.isStarted()) {
			g = 0.0F;
		} else {
			g = Math.min(f * 25.0F, 1.0F);
		}

		this.walkAnimation.update(g, 0.4F);
	}

	@Nullable
	@Override
	public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
		Frog frog = EntityType.FROG.create(serverLevel);
		if (frog != null) {
			FrogAi.initMemories(frog, serverLevel.getRandom());
		}

		return frog;
	}

	@Override
	public boolean isBaby() {
		return false;
	}

	@Override
	public void setBaby(boolean bl) {
	}

	@Override
	public void spawnChildFromBreeding(ServerLevel serverLevel, Animal animal) {
		this.finalizeSpawnChildFromBreeding(serverLevel, animal, null);
		this.getBrain().setMemory(MemoryModuleType.IS_PREGNANT, Unit.INSTANCE);
	}

	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData
	) {
		Holder<Biome> holder = serverLevelAccessor.getBiome(this.blockPosition());
		if (holder.is(BiomeTags.SPAWNS_COLD_VARIANT_FROGS)) {
			this.setVariant(BuiltInRegistries.FROG_VARIANT.getHolderOrThrow(FrogVariant.COLD));
		} else if (holder.is(BiomeTags.SPAWNS_WARM_VARIANT_FROGS)) {
			this.setVariant(BuiltInRegistries.FROG_VARIANT.getHolderOrThrow(FrogVariant.WARM));
		} else {
			this.setVariant(BuiltInRegistries.FROG_VARIANT.getHolderOrThrow(DEFAULT_VARIANT));
		}

		FrogAi.initMemories(this, serverLevelAccessor.getRandom());
		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
			.add(Attributes.MOVEMENT_SPEED, 1.0)
			.add(Attributes.MAX_HEALTH, 10.0)
			.add(Attributes.ATTACK_DAMAGE, 10.0)
			.add(Attributes.STEP_HEIGHT, 1.0);
	}

	@Nullable
	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.FROG_AMBIENT;
	}

	@Nullable
	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.FROG_HURT;
	}

	@Nullable
	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.FROG_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
		this.playSound(SoundEvents.FROG_STEP, 0.15F, 1.0F);
	}

	@Override
	public boolean isPushedByFluid() {
		return false;
	}

	@Override
	protected void sendDebugPackets() {
		super.sendDebugPackets();
		DebugPackets.sendEntityBrain(this);
	}

	@Override
	protected int calculateFallDamage(float f, float g) {
		return super.calculateFallDamage(f, g) - 5;
	}

	@Override
	public void travel(Vec3 vec3) {
		if (this.isControlledByLocalInstance() && this.isInWater()) {
			this.moveRelative(this.getSpeed(), vec3);
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
		} else {
			super.travel(vec3);
		}
	}

	public static boolean canEat(LivingEntity livingEntity) {
		if (livingEntity instanceof Slime slime && slime.getSize() != 1) {
			return false;
		}

		return livingEntity.getType().is(EntityTypeTags.FROG_FOOD);
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		return new Frog.FrogPathNavigation(this, level);
	}

	@Nullable
	@Override
	public LivingEntity getTarget() {
		return this.getTargetFromBrain();
	}

	@Override
	public boolean isFood(ItemStack itemStack) {
		return itemStack.is(ItemTags.FROG_FOOD);
	}

	public static boolean checkFrogSpawnRules(
		EntityType<? extends Animal> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource
	) {
		return levelAccessor.getBlockState(blockPos.below()).is(BlockTags.FROGS_SPAWNABLE_ON) && isBrightEnoughToSpawn(levelAccessor, blockPos);
	}

	class FrogLookControl extends LookControl {
		FrogLookControl(final Mob mob) {
			super(mob);
		}

		@Override
		protected boolean resetXRotOnTick() {
			return Frog.this.getTongueTarget().isEmpty();
		}
	}

	static class FrogNodeEvaluator extends AmphibiousNodeEvaluator {
		private final BlockPos.MutableBlockPos belowPos = new BlockPos.MutableBlockPos();

		public FrogNodeEvaluator(boolean bl) {
			super(bl);
		}

		@Override
		public Node getStart() {
			return !this.mob.isInWater()
				? super.getStart()
				: this.getStartNode(
					new BlockPos(Mth.floor(this.mob.getBoundingBox().minX), Mth.floor(this.mob.getBoundingBox().minY), Mth.floor(this.mob.getBoundingBox().minZ))
				);
		}

		@Override
		public PathType getPathType(PathfindingContext pathfindingContext, int i, int j, int k) {
			this.belowPos.set(i, j - 1, k);
			BlockState blockState = pathfindingContext.getBlockState(this.belowPos);
			return blockState.is(BlockTags.FROG_PREFER_JUMP_TO) ? PathType.OPEN : super.getPathType(pathfindingContext, i, j, k);
		}
	}

	static class FrogPathNavigation extends AmphibiousPathNavigation {
		FrogPathNavigation(Frog frog, Level level) {
			super(frog, level);
		}

		@Override
		public boolean canCutCorner(PathType pathType) {
			return pathType != PathType.WATER_BORDER && super.canCutCorner(pathType);
		}

		@Override
		protected PathFinder createPathFinder(int i) {
			this.nodeEvaluator = new Frog.FrogNodeEvaluator(true);
			this.nodeEvaluator.setCanPassDoors(true);
			return new PathFinder(this.nodeEvaluator, i);
		}
	}
}
