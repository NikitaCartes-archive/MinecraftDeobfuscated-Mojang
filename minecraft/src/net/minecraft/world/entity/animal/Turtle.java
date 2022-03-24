package net.minecraft.world.entity.animal;

import java.util.Random;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.AmphibiousNodeEvaluator;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;

public class Turtle extends Animal {
	private static final EntityDataAccessor<BlockPos> HOME_POS = SynchedEntityData.defineId(Turtle.class, EntityDataSerializers.BLOCK_POS);
	private static final EntityDataAccessor<Boolean> HAS_EGG = SynchedEntityData.defineId(Turtle.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> LAYING_EGG = SynchedEntityData.defineId(Turtle.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<BlockPos> TRAVEL_POS = SynchedEntityData.defineId(Turtle.class, EntityDataSerializers.BLOCK_POS);
	private static final EntityDataAccessor<Boolean> GOING_HOME = SynchedEntityData.defineId(Turtle.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> TRAVELLING = SynchedEntityData.defineId(Turtle.class, EntityDataSerializers.BOOLEAN);
	public static final Ingredient FOOD_ITEMS = Ingredient.of(Blocks.SEAGRASS.asItem());
	int layEggCounter;
	public static final Predicate<LivingEntity> BABY_ON_LAND_SELECTOR = livingEntity -> livingEntity.isBaby() && !livingEntity.isInWater();

	public Turtle(EntityType<? extends Turtle> entityType, Level level) {
		super(entityType, level);
		this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
		this.setPathfindingMalus(BlockPathTypes.DOOR_IRON_CLOSED, -1.0F);
		this.setPathfindingMalus(BlockPathTypes.DOOR_WOOD_CLOSED, -1.0F);
		this.setPathfindingMalus(BlockPathTypes.DOOR_OPEN, -1.0F);
		this.moveControl = new Turtle.TurtleMoveControl(this);
		this.maxUpStep = 1.0F;
	}

	public void setHomePos(BlockPos blockPos) {
		this.entityData.set(HOME_POS, blockPos);
	}

	BlockPos getHomePos() {
		return this.entityData.get(HOME_POS);
	}

	void setTravelPos(BlockPos blockPos) {
		this.entityData.set(TRAVEL_POS, blockPos);
	}

	BlockPos getTravelPos() {
		return this.entityData.get(TRAVEL_POS);
	}

	public boolean hasEgg() {
		return this.entityData.get(HAS_EGG);
	}

	void setHasEgg(boolean bl) {
		this.entityData.set(HAS_EGG, bl);
	}

	public boolean isLayingEgg() {
		return this.entityData.get(LAYING_EGG);
	}

	void setLayingEgg(boolean bl) {
		this.layEggCounter = bl ? 1 : 0;
		this.entityData.set(LAYING_EGG, bl);
	}

	boolean isGoingHome() {
		return this.entityData.get(GOING_HOME);
	}

	void setGoingHome(boolean bl) {
		this.entityData.set(GOING_HOME, bl);
	}

	boolean isTravelling() {
		return this.entityData.get(TRAVELLING);
	}

	void setTravelling(boolean bl) {
		this.entityData.set(TRAVELLING, bl);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(HOME_POS, BlockPos.ZERO);
		this.entityData.define(HAS_EGG, false);
		this.entityData.define(TRAVEL_POS, BlockPos.ZERO);
		this.entityData.define(GOING_HOME, false);
		this.entityData.define(TRAVELLING, false);
		this.entityData.define(LAYING_EGG, false);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putInt("HomePosX", this.getHomePos().getX());
		compoundTag.putInt("HomePosY", this.getHomePos().getY());
		compoundTag.putInt("HomePosZ", this.getHomePos().getZ());
		compoundTag.putBoolean("HasEgg", this.hasEgg());
		compoundTag.putInt("TravelPosX", this.getTravelPos().getX());
		compoundTag.putInt("TravelPosY", this.getTravelPos().getY());
		compoundTag.putInt("TravelPosZ", this.getTravelPos().getZ());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		int i = compoundTag.getInt("HomePosX");
		int j = compoundTag.getInt("HomePosY");
		int k = compoundTag.getInt("HomePosZ");
		this.setHomePos(new BlockPos(i, j, k));
		super.readAdditionalSaveData(compoundTag);
		this.setHasEgg(compoundTag.getBoolean("HasEgg"));
		int l = compoundTag.getInt("TravelPosX");
		int m = compoundTag.getInt("TravelPosY");
		int n = compoundTag.getInt("TravelPosZ");
		this.setTravelPos(new BlockPos(l, m, n));
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(
		ServerLevelAccessor serverLevelAccessor,
		DifficultyInstance difficultyInstance,
		MobSpawnType mobSpawnType,
		@Nullable SpawnGroupData spawnGroupData,
		@Nullable CompoundTag compoundTag
	) {
		this.setHomePos(this.blockPosition());
		this.setTravelPos(BlockPos.ZERO);
		return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
	}

	public static boolean checkTurtleSpawnRules(
		EntityType<Turtle> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random
	) {
		return blockPos.getY() < levelAccessor.getSeaLevel() + 4 && TurtleEggBlock.onSand(levelAccessor, blockPos) && isBrightEnoughToSpawn(levelAccessor, blockPos);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new Turtle.TurtlePanicGoal(this, 1.2));
		this.goalSelector.addGoal(1, new Turtle.TurtleBreedGoal(this, 1.0));
		this.goalSelector.addGoal(1, new Turtle.TurtleLayEggGoal(this, 1.0));
		this.goalSelector.addGoal(2, new TemptGoal(this, 1.1, FOOD_ITEMS, false));
		this.goalSelector.addGoal(3, new Turtle.TurtleGoToWaterGoal(this, 1.0));
		this.goalSelector.addGoal(4, new Turtle.TurtleGoHomeGoal(this, 1.0));
		this.goalSelector.addGoal(7, new Turtle.TurtleTravelGoal(this, 1.0));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(9, new Turtle.TurtleRandomStrollGoal(this, 1.0, 100));
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.MOVEMENT_SPEED, 0.25);
	}

	@Override
	public boolean isPushedByFluid() {
		return false;
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	public MobType getMobType() {
		return MobType.WATER;
	}

	@Override
	public int getAmbientSoundInterval() {
		return 200;
	}

	@Nullable
	@Override
	protected SoundEvent getAmbientSound() {
		return !this.isInWater() && this.onGround && !this.isBaby() ? SoundEvents.TURTLE_AMBIENT_LAND : super.getAmbientSound();
	}

	@Override
	protected void playSwimSound(float f) {
		super.playSwimSound(f * 1.5F);
	}

	@Override
	protected SoundEvent getSwimSound() {
		return SoundEvents.TURTLE_SWIM;
	}

	@Nullable
	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return this.isBaby() ? SoundEvents.TURTLE_HURT_BABY : SoundEvents.TURTLE_HURT;
	}

	@Nullable
	@Override
	protected SoundEvent getDeathSound() {
		return this.isBaby() ? SoundEvents.TURTLE_DEATH_BABY : SoundEvents.TURTLE_DEATH;
	}

	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
		SoundEvent soundEvent = this.isBaby() ? SoundEvents.TURTLE_SHAMBLE_BABY : SoundEvents.TURTLE_SHAMBLE;
		this.playSound(soundEvent, 0.15F, 1.0F);
	}

	@Override
	public boolean canFallInLove() {
		return super.canFallInLove() && !this.hasEgg();
	}

	@Override
	protected float nextStep() {
		return this.moveDist + 0.15F;
	}

	@Override
	public float getScale() {
		return this.isBaby() ? 0.3F : 1.0F;
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		return new Turtle.TurtlePathNavigation(this, level);
	}

	@Nullable
	@Override
	public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
		return EntityType.TURTLE.create(serverLevel);
	}

	@Override
	public boolean isFood(ItemStack itemStack) {
		return itemStack.is(Blocks.SEAGRASS.asItem());
	}

	@Override
	public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
		if (!this.isGoingHome() && levelReader.getFluidState(blockPos).is(FluidTags.WATER)) {
			return 10.0F;
		} else {
			return TurtleEggBlock.onSand(levelReader, blockPos) ? 10.0F : levelReader.getPathfindingCostFromLightLevels(blockPos);
		}
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.isAlive() && this.isLayingEgg() && this.layEggCounter >= 1 && this.layEggCounter % 5 == 0) {
			BlockPos blockPos = this.blockPosition();
			if (TurtleEggBlock.onSand(this.level, blockPos)) {
				this.level.levelEvent(2001, blockPos, Block.getId(this.level.getBlockState(blockPos.below())));
			}
		}
	}

	@Override
	protected void ageBoundaryReached() {
		super.ageBoundaryReached();
		if (!this.isBaby() && this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
			this.spawnAtLocation(Items.SCUTE, 1);
		}
	}

	@Override
	public void travel(Vec3 vec3) {
		if (this.isEffectiveAi() && this.isInWater()) {
			this.moveRelative(0.1F, vec3);
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
			if (this.getTarget() == null && (!this.isGoingHome() || !this.getHomePos().closerToCenterThan(this.position(), 20.0))) {
				this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.005, 0.0));
			}
		} else {
			super.travel(vec3);
		}
	}

	@Override
	public boolean canBeLeashed(Player player) {
		return false;
	}

	@Override
	public void thunderHit(ServerLevel serverLevel, LightningBolt lightningBolt) {
		this.hurt(DamageSource.LIGHTNING_BOLT, Float.MAX_VALUE);
	}

	static class TurtleBreedGoal extends BreedGoal {
		private final Turtle turtle;

		TurtleBreedGoal(Turtle turtle, double d) {
			super(turtle, d);
			this.turtle = turtle;
		}

		@Override
		public boolean canUse() {
			return super.canUse() && !this.turtle.hasEgg();
		}

		@Override
		protected void breed() {
			ServerPlayer serverPlayer = this.animal.getLoveCause();
			if (serverPlayer == null && this.partner.getLoveCause() != null) {
				serverPlayer = this.partner.getLoveCause();
			}

			if (serverPlayer != null) {
				serverPlayer.awardStat(Stats.ANIMALS_BRED);
				CriteriaTriggers.BRED_ANIMALS.trigger(serverPlayer, this.animal, this.partner, null);
			}

			this.turtle.setHasEgg(true);
			this.animal.resetLove();
			this.partner.resetLove();
			Random random = this.animal.getRandom();
			if (this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
				this.level.addFreshEntity(new ExperienceOrb(this.level, this.animal.getX(), this.animal.getY(), this.animal.getZ(), random.nextInt(7) + 1));
			}
		}
	}

	static class TurtleGoHomeGoal extends Goal {
		private final Turtle turtle;
		private final double speedModifier;
		private boolean stuck;
		private int closeToHomeTryTicks;
		private static final int GIVE_UP_TICKS = 600;

		TurtleGoHomeGoal(Turtle turtle, double d) {
			this.turtle = turtle;
			this.speedModifier = d;
		}

		@Override
		public boolean canUse() {
			if (this.turtle.isBaby()) {
				return false;
			} else if (this.turtle.hasEgg()) {
				return true;
			} else {
				return this.turtle.getRandom().nextInt(reducedTickDelay(700)) != 0 ? false : !this.turtle.getHomePos().closerToCenterThan(this.turtle.position(), 64.0);
			}
		}

		@Override
		public void start() {
			this.turtle.setGoingHome(true);
			this.stuck = false;
			this.closeToHomeTryTicks = 0;
		}

		@Override
		public void stop() {
			this.turtle.setGoingHome(false);
		}

		@Override
		public boolean canContinueToUse() {
			return !this.turtle.getHomePos().closerToCenterThan(this.turtle.position(), 7.0) && !this.stuck && this.closeToHomeTryTicks <= this.adjustedTickDelay(600);
		}

		@Override
		public void tick() {
			BlockPos blockPos = this.turtle.getHomePos();
			boolean bl = blockPos.closerToCenterThan(this.turtle.position(), 16.0);
			if (bl) {
				this.closeToHomeTryTicks++;
			}

			if (this.turtle.getNavigation().isDone()) {
				Vec3 vec3 = Vec3.atBottomCenterOf(blockPos);
				Vec3 vec32 = DefaultRandomPos.getPosTowards(this.turtle, 16, 3, vec3, (float) (Math.PI / 10));
				if (vec32 == null) {
					vec32 = DefaultRandomPos.getPosTowards(this.turtle, 8, 7, vec3, (float) (Math.PI / 2));
				}

				if (vec32 != null && !bl && !this.turtle.level.getBlockState(new BlockPos(vec32)).is(Blocks.WATER)) {
					vec32 = DefaultRandomPos.getPosTowards(this.turtle, 16, 5, vec3, (float) (Math.PI / 2));
				}

				if (vec32 == null) {
					this.stuck = true;
					return;
				}

				this.turtle.getNavigation().moveTo(vec32.x, vec32.y, vec32.z, this.speedModifier);
			}
		}
	}

	static class TurtleGoToWaterGoal extends MoveToBlockGoal {
		private static final int GIVE_UP_TICKS = 1200;
		private final Turtle turtle;

		TurtleGoToWaterGoal(Turtle turtle, double d) {
			super(turtle, turtle.isBaby() ? 2.0 : d, 24);
			this.turtle = turtle;
			this.verticalSearchStart = -1;
		}

		@Override
		public boolean canContinueToUse() {
			return !this.turtle.isInWater() && this.tryTicks <= 1200 && this.isValidTarget(this.turtle.level, this.blockPos);
		}

		@Override
		public boolean canUse() {
			if (this.turtle.isBaby() && !this.turtle.isInWater()) {
				return super.canUse();
			} else {
				return !this.turtle.isGoingHome() && !this.turtle.isInWater() && !this.turtle.hasEgg() ? super.canUse() : false;
			}
		}

		@Override
		public boolean shouldRecalculatePath() {
			return this.tryTicks % 160 == 0;
		}

		@Override
		protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
			return levelReader.getBlockState(blockPos).is(Blocks.WATER);
		}
	}

	static class TurtleLayEggGoal extends MoveToBlockGoal {
		private final Turtle turtle;

		TurtleLayEggGoal(Turtle turtle, double d) {
			super(turtle, d, 16);
			this.turtle = turtle;
		}

		@Override
		public boolean canUse() {
			return this.turtle.hasEgg() && this.turtle.getHomePos().closerToCenterThan(this.turtle.position(), 9.0) ? super.canUse() : false;
		}

		@Override
		public boolean canContinueToUse() {
			return super.canContinueToUse() && this.turtle.hasEgg() && this.turtle.getHomePos().closerToCenterThan(this.turtle.position(), 9.0);
		}

		@Override
		public void tick() {
			super.tick();
			BlockPos blockPos = this.turtle.blockPosition();
			if (!this.turtle.isInWater() && this.isReachedTarget()) {
				if (this.turtle.layEggCounter < 1) {
					this.turtle.setLayingEgg(true);
				} else if (this.turtle.layEggCounter > this.adjustedTickDelay(200)) {
					Level level = this.turtle.level;
					level.playSound(null, blockPos, SoundEvents.TURTLE_LAY_EGG, SoundSource.BLOCKS, 0.3F, 0.9F + level.random.nextFloat() * 0.2F);
					level.setBlock(
						this.blockPos.above(), Blocks.TURTLE_EGG.defaultBlockState().setValue(TurtleEggBlock.EGGS, Integer.valueOf(this.turtle.random.nextInt(4) + 1)), 3
					);
					this.turtle.setHasEgg(false);
					this.turtle.setLayingEgg(false);
					this.turtle.setInLoveTime(600);
				}

				if (this.turtle.isLayingEgg()) {
					this.turtle.layEggCounter++;
				}
			}
		}

		@Override
		protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
			return !levelReader.isEmptyBlock(blockPos.above()) ? false : TurtleEggBlock.isSand(levelReader, blockPos);
		}
	}

	static class TurtleMoveControl extends MoveControl {
		private final Turtle turtle;

		TurtleMoveControl(Turtle turtle) {
			super(turtle);
			this.turtle = turtle;
		}

		private void updateSpeed() {
			if (this.turtle.isInWater()) {
				this.turtle.setDeltaMovement(this.turtle.getDeltaMovement().add(0.0, 0.005, 0.0));
				if (!this.turtle.getHomePos().closerToCenterThan(this.turtle.position(), 16.0)) {
					this.turtle.setSpeed(Math.max(this.turtle.getSpeed() / 2.0F, 0.08F));
				}

				if (this.turtle.isBaby()) {
					this.turtle.setSpeed(Math.max(this.turtle.getSpeed() / 3.0F, 0.06F));
				}
			} else if (this.turtle.onGround) {
				this.turtle.setSpeed(Math.max(this.turtle.getSpeed() / 2.0F, 0.06F));
			}
		}

		@Override
		public void tick() {
			this.updateSpeed();
			if (this.operation == MoveControl.Operation.MOVE_TO && !this.turtle.getNavigation().isDone()) {
				double d = this.wantedX - this.turtle.getX();
				double e = this.wantedY - this.turtle.getY();
				double f = this.wantedZ - this.turtle.getZ();
				double g = Math.sqrt(d * d + e * e + f * f);
				e /= g;
				float h = (float)(Mth.atan2(f, d) * 180.0F / (float)Math.PI) - 90.0F;
				this.turtle.setYRot(this.rotlerp(this.turtle.getYRot(), h, 90.0F));
				this.turtle.yBodyRot = this.turtle.getYRot();
				float i = (float)(this.speedModifier * this.turtle.getAttributeValue(Attributes.MOVEMENT_SPEED));
				this.turtle.setSpeed(Mth.lerp(0.125F, this.turtle.getSpeed(), i));
				this.turtle.setDeltaMovement(this.turtle.getDeltaMovement().add(0.0, (double)this.turtle.getSpeed() * e * 0.1, 0.0));
			} else {
				this.turtle.setSpeed(0.0F);
			}
		}
	}

	static class TurtlePanicGoal extends PanicGoal {
		TurtlePanicGoal(Turtle turtle, double d) {
			super(turtle, d);
		}

		@Override
		public boolean canUse() {
			if (!this.shouldPanic()) {
				return false;
			} else {
				BlockPos blockPos = this.lookForWater(this.mob.level, this.mob, 7);
				if (blockPos != null) {
					this.posX = (double)blockPos.getX();
					this.posY = (double)blockPos.getY();
					this.posZ = (double)blockPos.getZ();
					return true;
				} else {
					return this.findRandomPosition();
				}
			}
		}
	}

	static class TurtlePathNavigation extends WaterBoundPathNavigation {
		TurtlePathNavigation(Turtle turtle, Level level) {
			super(turtle, level);
		}

		@Override
		protected boolean canUpdatePath() {
			return true;
		}

		@Override
		protected PathFinder createPathFinder(int i) {
			this.nodeEvaluator = new AmphibiousNodeEvaluator(true);
			return new PathFinder(this.nodeEvaluator, i);
		}

		@Override
		public boolean isStableDestination(BlockPos blockPos) {
			if (this.mob instanceof Turtle turtle && turtle.isTravelling()) {
				return this.level.getBlockState(blockPos).is(Blocks.WATER);
			}

			return !this.level.getBlockState(blockPos.below()).isAir();
		}
	}

	static class TurtleRandomStrollGoal extends RandomStrollGoal {
		private final Turtle turtle;

		TurtleRandomStrollGoal(Turtle turtle, double d, int i) {
			super(turtle, d, i);
			this.turtle = turtle;
		}

		@Override
		public boolean canUse() {
			return !this.mob.isInWater() && !this.turtle.isGoingHome() && !this.turtle.hasEgg() ? super.canUse() : false;
		}
	}

	static class TurtleTravelGoal extends Goal {
		private final Turtle turtle;
		private final double speedModifier;
		private boolean stuck;

		TurtleTravelGoal(Turtle turtle, double d) {
			this.turtle = turtle;
			this.speedModifier = d;
		}

		@Override
		public boolean canUse() {
			return !this.turtle.isGoingHome() && !this.turtle.hasEgg() && this.turtle.isInWater();
		}

		@Override
		public void start() {
			int i = 512;
			int j = 4;
			Random random = this.turtle.random;
			int k = random.nextInt(1025) - 512;
			int l = random.nextInt(9) - 4;
			int m = random.nextInt(1025) - 512;
			if ((double)l + this.turtle.getY() > (double)(this.turtle.level.getSeaLevel() - 1)) {
				l = 0;
			}

			BlockPos blockPos = new BlockPos((double)k + this.turtle.getX(), (double)l + this.turtle.getY(), (double)m + this.turtle.getZ());
			this.turtle.setTravelPos(blockPos);
			this.turtle.setTravelling(true);
			this.stuck = false;
		}

		@Override
		public void tick() {
			if (this.turtle.getNavigation().isDone()) {
				Vec3 vec3 = Vec3.atBottomCenterOf(this.turtle.getTravelPos());
				Vec3 vec32 = DefaultRandomPos.getPosTowards(this.turtle, 16, 3, vec3, (float) (Math.PI / 10));
				if (vec32 == null) {
					vec32 = DefaultRandomPos.getPosTowards(this.turtle, 8, 7, vec3, (float) (Math.PI / 2));
				}

				if (vec32 != null) {
					int i = Mth.floor(vec32.x);
					int j = Mth.floor(vec32.z);
					int k = 34;
					if (!this.turtle.level.hasChunksAt(i - 34, j - 34, i + 34, j + 34)) {
						vec32 = null;
					}
				}

				if (vec32 == null) {
					this.stuck = true;
					return;
				}

				this.turtle.getNavigation().moveTo(vec32.x, vec32.y, vec32.z, this.speedModifier);
			}
		}

		@Override
		public boolean canContinueToUse() {
			return !this.turtle.getNavigation().isDone() && !this.stuck && !this.turtle.isGoingHome() && !this.turtle.isInLove() && !this.turtle.hasEgg();
		}

		@Override
		public void stop() {
			this.turtle.setTravelling(false);
			super.stop();
		}
	}
}
