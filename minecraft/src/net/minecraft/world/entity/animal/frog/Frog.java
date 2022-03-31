package net.minecraft.world.entity.animal.frog;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
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
import net.minecraft.util.Unit;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.AmphibiousNodeEvaluator;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;

public class Frog extends Animal {
	public static final Ingredient TEMPTATION_ITEM = Ingredient.of(Items.SLIME_BALL);
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
		MemoryModuleType.IS_PREGNANT
	);
	private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(Frog.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<OptionalInt> DATA_TONGUE_TARGET_ID = SynchedEntityData.defineId(
		Frog.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT
	);
	private static final int FROG_FALL_DAMAGE_REDUCTION = 5;
	public final AnimationState jumpAnimationState = new AnimationState();
	public final AnimationState croakAnimationState = new AnimationState();
	public final AnimationState tongueAnimationState = new AnimationState();
	public final AnimationState walkAnimationState = new AnimationState();
	public final AnimationState swimAnimationState = new AnimationState();
	public final AnimationState swimIdleAnimationState = new AnimationState();

	public Frog(EntityType<? extends Animal> entityType, Level level) {
		super(entityType, level);
		this.lookControl = new Frog.FrogLookControl(this);
		this.setPathfindingMalus(BlockPathTypes.WATER, 4.0F);
		this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02F, 0.1F, true);
		this.maxUpStep = 1.0F;
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
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_VARIANT, 0);
		this.entityData.define(DATA_TONGUE_TARGET_ID, OptionalInt.empty());
	}

	public void eraseTongueTarget() {
		this.entityData.set(DATA_TONGUE_TARGET_ID, OptionalInt.empty());
	}

	public Optional<Entity> getTongueTarget() {
		return this.entityData.get(DATA_TONGUE_TARGET_ID).stream().mapToObj(this.level::getEntity).filter(Objects::nonNull).findFirst();
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

	public Frog.Variant getVariant() {
		return Frog.Variant.byId(this.entityData.get(DATA_VARIANT));
	}

	public void setVariant(Frog.Variant variant) {
		this.entityData.set(DATA_VARIANT, variant.getId());
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("Variant", this.getVariant().getId());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setVariant(Frog.Variant.byId(compoundTag.getInt("Variant")));
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	private boolean isMovingOnLand() {
		return this.onGround && this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6 && !this.isInWaterOrBubble();
	}

	private boolean isMovingInWater() {
		return this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6 && this.isInWaterOrBubble();
	}

	@Override
	protected void customServerAiStep() {
		this.level.getProfiler().push("frogBrain");
		this.getBrain().tick((ServerLevel)this.level, this);
		this.level.getProfiler().pop();
		this.level.getProfiler().push("frogActivityUpdate");
		FrogAi.updateActivity(this);
		this.level.getProfiler().pop();
		super.customServerAiStep();
	}

	@Override
	public void tick() {
		if (this.level.isClientSide()) {
			if (this.isMovingOnLand()) {
				this.walkAnimationState.startIfStopped();
			} else {
				this.walkAnimationState.stop();
			}

			if (this.isMovingInWater()) {
				this.swimIdleAnimationState.stop();
				this.swimAnimationState.startIfStopped();
			} else if (this.isInWaterOrBubble()) {
				this.swimAnimationState.stop();
				this.swimIdleAnimationState.startIfStopped();
			} else {
				this.swimAnimationState.stop();
				this.swimIdleAnimationState.stop();
			}
		}

		super.tick();
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		if (DATA_POSE.equals(entityDataAccessor)) {
			Pose pose = this.getPose();
			if (pose == Pose.LONG_JUMPING) {
				this.jumpAnimationState.start();
			} else {
				this.jumpAnimationState.stop();
			}

			if (pose == Pose.CROAKING) {
				this.croakAnimationState.start();
			} else {
				this.croakAnimationState.stop();
			}

			if (pose == Pose.USING_TONGUE) {
				this.tongueAnimationState.start();
			} else {
				this.tongueAnimationState.stop();
			}
		}

		super.onSyncedDataUpdated(entityDataAccessor);
	}

	@Nullable
	@Override
	public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
		Frog frog = EntityType.FROG.create(serverLevel);
		if (frog != null) {
			FrogAi.initMemories(frog);
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
		ServerPlayer serverPlayer = this.getLoveCause();
		if (serverPlayer == null) {
			serverPlayer = animal.getLoveCause();
		}

		if (serverPlayer != null) {
			serverPlayer.awardStat(Stats.ANIMALS_BRED);
			CriteriaTriggers.BRED_ANIMALS.trigger(serverPlayer, this, animal, null);
		}

		this.setAge(6000);
		animal.setAge(6000);
		this.resetLove();
		animal.resetLove();
		this.getBrain().setMemory(MemoryModuleType.IS_PREGNANT, Unit.INSTANCE);
		serverLevel.broadcastEntityEvent(this, (byte)18);
		if (serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
			serverLevel.addFreshEntity(new ExperienceOrb(serverLevel, this.getX(), this.getY(), this.getZ(), this.getRandom().nextInt(7) + 1));
		}
	}

	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor,
		DifficultyInstance difficultyInstance,
		MobSpawnType mobSpawnType,
		@Nullable SpawnGroupData spawnGroupData,
		@Nullable CompoundTag compoundTag
	) {
		Holder<Biome> holder = serverLevelAccessor.getBiome(this.blockPosition());
		if (holder.is(BiomeTags.SPAWNS_COLD_VARIANT_FROGS)) {
			this.setVariant(Frog.Variant.COLD);
		} else if (holder.is(BiomeTags.SPAWNS_WARM_VARIANT_FROGS)) {
			this.setVariant(Frog.Variant.WARM);
		} else {
			this.setVariant(Frog.Variant.TEMPERATE);
		}

		FrogAi.initMemories(this);
		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 1.0).add(Attributes.MAX_HEALTH, 10.0);
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
		if (this.isEffectiveAi() && this.isInWater()) {
			this.moveRelative(this.getSpeed(), vec3);
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
		} else {
			super.travel(vec3);
		}
	}

	public static boolean canEat(LivingEntity livingEntity) {
		if (livingEntity instanceof Slime slime && slime.getSize() == 1) {
			return true;
		}

		return false;
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		return new Frog.FrogPathNavigation(this, level);
	}

	@Override
	public boolean isFood(ItemStack itemStack) {
		return TEMPTATION_ITEM.test(itemStack);
	}

	class FrogLookControl extends LookControl {
		FrogLookControl(Mob mob) {
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
		public BlockPathTypes getBlockPathType(BlockGetter blockGetter, int i, int j, int k) {
			this.belowPos.set(i, j - 1, k);
			BlockState blockState = blockGetter.getBlockState(this.belowPos);
			return blockState.is(BlockTags.FROG_PREFER_JUMP_TO) ? BlockPathTypes.OPEN : getBlockPathTypeStatic(blockGetter, this.belowPos.move(Direction.UP));
		}
	}

	static class FrogPathNavigation extends WaterBoundPathNavigation {
		FrogPathNavigation(Frog frog, Level level) {
			super(frog, level);
		}

		@Override
		protected PathFinder createPathFinder(int i) {
			this.nodeEvaluator = new Frog.FrogNodeEvaluator(true);
			return new PathFinder(this.nodeEvaluator, i);
		}

		@Override
		protected boolean canUpdatePath() {
			return true;
		}

		@Override
		public boolean isStableDestination(BlockPos blockPos) {
			return !this.level.getBlockState(blockPos.below()).isAir();
		}
	}

	public static enum Variant {
		TEMPERATE(0, "temperate"),
		WARM(1, "warm"),
		COLD(2, "cold");

		private static final Frog.Variant[] BY_ID = (Frog.Variant[])Arrays.stream(values())
			.sorted(Comparator.comparingInt(Frog.Variant::getId))
			.toArray(Frog.Variant[]::new);
		private final int id;
		private final String name;

		private Variant(int j, String string2) {
			this.id = j;
			this.name = string2;
		}

		public int getId() {
			return this.id;
		}

		public String getName() {
			return this.name;
		}

		public static Frog.Variant byId(int i) {
			if (i < 0 || i >= BY_ID.length) {
				i = 0;
			}

			return BY_ID[i];
		}
	}
}
