package net.minecraft.world.entity.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractFish extends WaterAnimal implements Bucketable {
	private static final EntityDataAccessor<Boolean> FROM_BUCKET = SynchedEntityData.defineId(AbstractFish.class, EntityDataSerializers.BOOLEAN);

	public AbstractFish(EntityType<? extends AbstractFish> entityType, Level level) {
		super(entityType, level);
		this.moveControl = new AbstractFish.FishMoveControl(this);
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return entityDimensions.height * 0.65F;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 3.0);
	}

	@Override
	public boolean requiresCustomPersistence() {
		return super.requiresCustomPersistence() || this.fromBucket();
	}

	@Override
	public boolean removeWhenFarAway(double d) {
		return !this.fromBucket() && !this.hasCustomName();
	}

	@Override
	public int getMaxSpawnClusterSize() {
		return 8;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(FROM_BUCKET, false);
	}

	@Override
	public boolean fromBucket() {
		return this.entityData.get(FROM_BUCKET);
	}

	@Override
	public void setFromBucket(boolean bl) {
		this.entityData.set(FROM_BUCKET, bl);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putBoolean("FromBucket", this.fromBucket());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setFromBucket(compoundTag.getBoolean("FromBucket"));
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(0, new PanicGoal(this, 1.25));
		this.goalSelector.addGoal(2, new AvoidEntityGoal(this, Player.class, 8.0F, 1.6, 1.4, EntitySelector.NO_SPECTATORS::test));
		this.goalSelector.addGoal(4, new AbstractFish.FishSwimGoal(this));
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		return new WaterBoundPathNavigation(this, level);
	}

	@Override
	public void travel(Vec3 vec3) {
		if (this.isEffectiveAi() && this.isInWater()) {
			this.moveRelative(0.01F, vec3);
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
			if (this.getTarget() == null) {
				this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.005, 0.0));
			}
		} else {
			super.travel(vec3);
		}
	}

	@Override
	public void aiStep() {
		if (!this.isInWater() && this.onGround() && this.verticalCollision) {
			this.setDeltaMovement(
				this.getDeltaMovement().add((double)((this.random.nextFloat() * 2.0F - 1.0F) * 0.05F), 0.4F, (double)((this.random.nextFloat() * 2.0F - 1.0F) * 0.05F))
			);
			this.setOnGround(false);
			this.hasImpulse = true;
			this.playSound(this.getFlopSound(), this.getSoundVolume(), this.getVoicePitch());
		}

		super.aiStep();
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
		return (InteractionResult)Bucketable.bucketMobPickup(player, interactionHand, this).orElse(super.mobInteract(player, interactionHand));
	}

	@Override
	public void saveToBucketTag(ItemStack itemStack) {
		Bucketable.saveDefaultDataToBucketTag(this, itemStack);
	}

	@Override
	public void loadFromBucketTag(CompoundTag compoundTag) {
		Bucketable.loadDefaultDataFromBucketTag(this, compoundTag);
	}

	@Override
	public SoundEvent getPickupSound() {
		return SoundEvents.BUCKET_FILL_FISH;
	}

	protected boolean canRandomSwim() {
		return true;
	}

	protected abstract SoundEvent getFlopSound();

	@Override
	protected SoundEvent getSwimSound() {
		return SoundEvents.FISH_SWIM;
	}

	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
	}

	static class FishMoveControl extends MoveControl {
		private final AbstractFish fish;

		FishMoveControl(AbstractFish abstractFish) {
			super(abstractFish);
			this.fish = abstractFish;
		}

		@Override
		public void tick() {
			if (this.fish.isEyeInFluid(FluidTags.WATER)) {
				this.fish.setDeltaMovement(this.fish.getDeltaMovement().add(0.0, 0.005, 0.0));
			}

			if (this.operation == MoveControl.Operation.MOVE_TO && !this.fish.getNavigation().isDone()) {
				float f = (float)(this.speedModifier * this.fish.getAttributeValue(Attributes.MOVEMENT_SPEED));
				this.fish.setSpeed(Mth.lerp(0.125F, this.fish.getSpeed(), f));
				double d = this.wantedX - this.fish.getX();
				double e = this.wantedY - this.fish.getY();
				double g = this.wantedZ - this.fish.getZ();
				if (e != 0.0) {
					double h = Math.sqrt(d * d + e * e + g * g);
					this.fish.setDeltaMovement(this.fish.getDeltaMovement().add(0.0, (double)this.fish.getSpeed() * (e / h) * 0.1, 0.0));
				}

				if (d != 0.0 || g != 0.0) {
					float i = (float)(Mth.atan2(g, d) * 180.0F / (float)Math.PI) - 90.0F;
					this.fish.setYRot(this.rotlerp(this.fish.getYRot(), i, 90.0F));
					this.fish.yBodyRot = this.fish.getYRot();
				}
			} else {
				this.fish.setSpeed(0.0F);
			}
		}
	}

	static class FishSwimGoal extends RandomSwimmingGoal {
		private final AbstractFish fish;

		public FishSwimGoal(AbstractFish abstractFish) {
			super(abstractFish, 1.0, 40);
			this.fish = abstractFish;
		}

		@Override
		public boolean canUse() {
			return this.fish.canRandomSwim() && super.canUse();
		}
	}
}
