package net.minecraft.world.entity.monster;

import com.google.common.collect.Sets;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ItemBasedSteering;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class Strider extends Animal implements ItemSteerable, Saddleable {
	private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.WARPED_FUNGUS);
	private static final Ingredient TEMPT_ITEMS = Ingredient.of(Items.WARPED_FUNGUS, Items.WARPED_FUNGUS_ON_A_STICK);
	private static final EntityDataAccessor<Integer> DATA_BOOST_TIME = SynchedEntityData.defineId(Strider.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_SUFFOCATING = SynchedEntityData.defineId(Strider.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_SADDLE_ID = SynchedEntityData.defineId(Strider.class, EntityDataSerializers.BOOLEAN);
	private final ItemBasedSteering steering = new ItemBasedSteering(this.entityData, DATA_BOOST_TIME, DATA_SADDLE_ID);
	private TemptGoal temptGoal;
	private PanicGoal panicGoal;

	public Strider(EntityType<? extends Strider> entityType, Level level) {
		super(entityType, level);
		this.blocksBuilding = true;
		this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
		this.setPathfindingMalus(BlockPathTypes.LAVA, 0.0F);
		this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 0.0F);
		this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, 0.0F);
	}

	public static boolean checkStriderSpawnRules(
		EntityType<Strider> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random
	) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

		do {
			mutableBlockPos.move(Direction.UP);
		} while (levelAccessor.getFluidState(mutableBlockPos).is(FluidTags.LAVA));

		return levelAccessor.getBlockState(mutableBlockPos).isAir();
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
		if (DATA_BOOST_TIME.equals(entityDataAccessor) && this.level.isClientSide) {
			this.steering.onSynced();
		}

		super.onSyncedDataUpdated(entityDataAccessor);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_BOOST_TIME, 0);
		this.entityData.define(DATA_SUFFOCATING, false);
		this.entityData.define(DATA_SADDLE_ID, false);
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
			this.level.playSound(null, this, SoundEvents.STRIDER_SADDLE, soundSource, 0.5F, 1.0F);
		}
	}

	@Override
	protected void registerGoals() {
		this.panicGoal = new PanicGoal(this, 1.65);
		this.goalSelector.addGoal(1, this.panicGoal);
		this.goalSelector.addGoal(3, new BreedGoal(this, 1.0));
		this.temptGoal = new TemptGoal(this, 1.4, false, TEMPT_ITEMS);
		this.goalSelector.addGoal(4, this.temptGoal);
		this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1));
		this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0, 60));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
		this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Strider.class, 8.0F));
	}

	public void setSuffocating(boolean bl) {
		this.entityData.set(DATA_SUFFOCATING, bl);
	}

	public boolean isSuffocating() {
		return this.getVehicle() instanceof Strider ? ((Strider)this.getVehicle()).isSuffocating() : this.entityData.get(DATA_SUFFOCATING);
	}

	@Override
	public boolean canStandOnFluid(Fluid fluid) {
		return fluid.is(FluidTags.LAVA);
	}

	@Nullable
	@Override
	public AABB getCollideAgainstBox(Entity entity) {
		return entity.isPushable() ? entity.getBoundingBox() : null;
	}

	@Override
	public boolean isPushable() {
		return true;
	}

	@Override
	public double getPassengersRidingOffset() {
		float f = Math.min(0.25F, this.animationSpeed);
		float g = this.animationPosition;
		return (double)this.getBbHeight() - 0.2 + (double)(0.12F * Mth.cos(g * 1.5F) * 2.0F * f);
	}

	@Override
	public boolean canBeControlledByRider() {
		Entity entity = this.getControllingPassenger();
		if (!(entity instanceof Player)) {
			return false;
		} else {
			Player player = (Player)entity;
			return player.getMainHandItem().getItem() == Items.WARPED_FUNGUS_ON_A_STICK || player.getOffhandItem().getItem() == Items.WARPED_FUNGUS_ON_A_STICK;
		}
	}

	@Override
	public boolean checkSpawnObstruction(LevelReader levelReader) {
		return levelReader.isUnobstructed(this);
	}

	@Nullable
	@Override
	public Entity getControllingPassenger() {
		return this.getPassengers().isEmpty() ? null : (Entity)this.getPassengers().get(0);
	}

	@Override
	public Vec3 getDismountLocationForPassenger(LivingEntity livingEntity) {
		Vec3[] vec3s = new Vec3[]{
			getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)livingEntity.getBbWidth(), livingEntity.yRot),
			getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)livingEntity.getBbWidth(), livingEntity.yRot - 22.5F),
			getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)livingEntity.getBbWidth(), livingEntity.yRot + 22.5F),
			getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)livingEntity.getBbWidth(), livingEntity.yRot - 45.0F),
			getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)livingEntity.getBbWidth(), livingEntity.yRot + 45.0F)
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
			if (!this.level.getFluidState(blockPos).is(FluidTags.LAVA)) {
				for (Pose pose : livingEntity.getDismountPoses()) {
					double f = this.level.getRelativeFloorHeight(blockPos);
					if (DismountHelper.isFloorValid(f)) {
						AABB aABB = livingEntity.getLocalBoundsForPose(pose);
						Vec3 vec32 = Vec3.upFromBottomCenterOf(blockPos, f);
						if (DismountHelper.canDismountTo(this.level, livingEntity, aABB.move(vec32))) {
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
	public void travel(Vec3 vec3) {
		this.setSpeed(this.getMoveSpeed());
		this.travel(this, this.steering, vec3);
	}

	public float getMoveSpeed() {
		return (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) * (this.isSuffocating() ? 0.66F : 1.0F);
	}

	@Override
	public float getSteeringSpeed() {
		return (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) * (this.isSuffocating() ? 0.23F : 0.55F);
	}

	@Override
	public void travelWithInput(Vec3 vec3) {
		super.travel(vec3);
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
			this.fallDistance = 0.0F;
		} else {
			super.checkFallDamage(d, bl, blockState, blockPos);
		}
	}

	@Override
	public void tick() {
		if (this.temptGoal != null && this.temptGoal.isRunning() && this.random.nextInt(100) == 0) {
			this.playSound(SoundEvents.STRIDER_HAPPY, 1.0F, this.getVoicePitch());
		}

		if (this.panicGoal != null && this.panicGoal.isRunning() && this.random.nextInt(60) == 0) {
			this.playSound(SoundEvents.STRIDER_RETREAT, 1.0F, this.getVoicePitch());
		}

		BlockState blockState = this.level.getBlockState(this.blockPosition());
		BlockState blockState2 = this.getBlockStateOn();
		boolean bl = blockState.is(BlockTags.STRIDER_WARM_BLOCKS) || blockState2.is(BlockTags.STRIDER_WARM_BLOCKS) || this.getFluidHeight(FluidTags.LAVA) > 0.0;
		this.setSuffocating(!bl);
		super.tick();
		this.floatStrider();
		this.checkInsideBlocks();
	}

	@Override
	protected boolean shouldPassengersInheritMalus() {
		return true;
	}

	private void floatStrider() {
		if (this.isInLava()) {
			CollisionContext collisionContext = CollisionContext.of(this);
			if (collisionContext.isAbove(LiquidBlock.STABLE_SHAPE, this.blockPosition(), true)
				&& !this.level.getFluidState(this.blockPosition().above()).is(FluidTags.LAVA)) {
				this.onGround = true;
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
		return SoundEvents.STRIDER_AMBIENT;
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
		return this.getPassengers().isEmpty() && !this.isEyeInFluid(FluidTags.LAVA);
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
		return levelReader.getBlockState(blockPos).getFluidState().is(FluidTags.LAVA) ? 10.0F : 0.0F;
	}

	public Strider getBreedOffspring(AgableMob agableMob) {
		return EntityType.STRIDER.create(this.level);
	}

	@Override
	public boolean isFood(ItemStack itemStack) {
		return FOOD_ITEMS.test(itemStack);
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
		if (!bl && this.isSaddled() && !this.isVehicle()) {
			if (!this.level.isClientSide) {
				player.startRiding(this);
			}

			return InteractionResult.sidedSuccess(this.level.isClientSide);
		} else {
			InteractionResult interactionResult = super.mobInteract(player, interactionHand);
			if (!interactionResult.consumesAction()) {
				ItemStack itemStack = player.getItemInHand(interactionHand);
				return itemStack.getItem() == Items.SADDLE ? itemStack.interactLivingEntity(player, this, interactionHand) : InteractionResult.PASS;
			} else {
				if (bl && !this.isSilent()) {
					this.level
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

	@Environment(EnvType.CLIENT)
	@Override
	public Vec3 getLeashOffset() {
		return new Vec3(0.0, (double)(0.6F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
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
		SpawnGroupData spawnGroupData2 = null;
		Strider.StriderGroupData.Rider rider;
		if (spawnGroupData instanceof Strider.StriderGroupData) {
			rider = ((Strider.StriderGroupData)spawnGroupData).rider;
		} else if (!this.isBaby()) {
			if (this.random.nextInt(30) == 0) {
				rider = Strider.StriderGroupData.Rider.PIGLIN_RIDER;
				spawnGroupData2 = new Zombie.ZombieGroupData(Zombie.getSpawnAsBabyOdds(this.random), false);
			} else if (this.random.nextInt(10) == 0) {
				rider = Strider.StriderGroupData.Rider.BABY_RIDER;
			} else {
				rider = Strider.StriderGroupData.Rider.NO_RIDER;
			}

			spawnGroupData = new Strider.StriderGroupData(rider);
			((AgableMob.AgableMobGroupData)spawnGroupData).setBabySpawnChance(rider == Strider.StriderGroupData.Rider.NO_RIDER ? 0.5F : 0.0F);
		} else {
			rider = Strider.StriderGroupData.Rider.NO_RIDER;
		}

		Mob mob = null;
		if (rider == Strider.StriderGroupData.Rider.BABY_RIDER) {
			Strider strider = EntityType.STRIDER.create(levelAccessor.getLevel());
			if (strider != null) {
				mob = strider;
				strider.setAge(-24000);
			}
		} else if (rider == Strider.StriderGroupData.Rider.PIGLIN_RIDER) {
			ZombifiedPiglin zombifiedPiglin = EntityType.ZOMBIFIED_PIGLIN.create(levelAccessor.getLevel());
			if (zombifiedPiglin != null) {
				mob = zombifiedPiglin;
				this.equipSaddle(null);
			}
		}

		if (mob != null) {
			mob.moveTo(this.getX(), this.getY(), this.getZ(), this.yRot, 0.0F);
			mob.finalizeSpawn(levelAccessor, difficultyInstance, MobSpawnType.JOCKEY, spawnGroupData2, null);
			mob.startRiding(this, true);
			levelAccessor.addFreshEntity(mob);
		}

		return super.finalizeSpawn(levelAccessor, difficultyInstance, mobSpawnType, spawnGroupData, compoundTag);
	}

	public static class StriderGroupData extends AgableMob.AgableMobGroupData {
		public final Strider.StriderGroupData.Rider rider;

		public StriderGroupData(Strider.StriderGroupData.Rider rider) {
			this.rider = rider;
		}

		public static enum Rider {
			NO_RIDER,
			BABY_RIDER,
			PIGLIN_RIDER;
		}
	}

	static class StriderPathNavigation extends GroundPathNavigation {
		StriderPathNavigation(Strider strider, Level level) {
			super(strider, level);
		}

		@Override
		protected PathFinder createPathFinder(int i) {
			this.nodeEvaluator = new WalkNodeEvaluator();
			return new PathFinder(this.nodeEvaluator, i);
		}

		@Override
		protected boolean hasValidPathType(BlockPathTypes blockPathTypes) {
			return blockPathTypes != BlockPathTypes.LAVA && blockPathTypes != BlockPathTypes.DAMAGE_FIRE && blockPathTypes != BlockPathTypes.DANGER_FIRE
				? super.hasValidPathType(blockPathTypes)
				: true;
		}

		@Override
		public boolean isStableDestination(BlockPos blockPos) {
			return this.level.getBlockState(blockPos).is(Blocks.LAVA) || super.isStableDestination(blockPos);
		}
	}
}
