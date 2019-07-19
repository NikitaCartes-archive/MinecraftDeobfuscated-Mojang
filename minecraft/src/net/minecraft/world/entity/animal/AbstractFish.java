package net.minecraft.world.entity.animal;

import java.util.Random;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractFish extends WaterAnimal {
	private static final EntityDataAccessor<Boolean> FROM_BUCKET = SynchedEntityData.defineId(AbstractFish.class, EntityDataSerializers.BOOLEAN);

	public AbstractFish(EntityType<? extends AbstractFish> entityType, Level level) {
		super(entityType, level);
		this.moveControl = new AbstractFish.FishMoveControl(this);
	}

	@Override
	protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
		return entityDimensions.height * 0.65F;
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(3.0);
	}

	@Override
	public boolean requiresCustomPersistence() {
		return this.fromBucket();
	}

	public static boolean checkFishSpawnRules(
		EntityType<? extends AbstractFish> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random
	) {
		return levelAccessor.getBlockState(blockPos).getBlock() == Blocks.WATER && levelAccessor.getBlockState(blockPos.above()).getBlock() == Blocks.WATER;
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

	private boolean fromBucket() {
		return this.entityData.get(FROM_BUCKET);
	}

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
		if (!this.isInWater() && this.onGround && this.verticalCollision) {
			this.setDeltaMovement(
				this.getDeltaMovement().add((double)((this.random.nextFloat() * 2.0F - 1.0F) * 0.05F), 0.4F, (double)((this.random.nextFloat() * 2.0F - 1.0F) * 0.05F))
			);
			this.onGround = false;
			this.hasImpulse = true;
			this.playSound(this.getFlopSound(), this.getSoundVolume(), this.getVoicePitch());
		}

		super.aiStep();
	}

	@Override
	protected boolean mobInteract(Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (itemStack.getItem() == Items.WATER_BUCKET && this.isAlive()) {
			this.playSound(SoundEvents.BUCKET_FILL_FISH, 1.0F, 1.0F);
			itemStack.shrink(1);
			ItemStack itemStack2 = this.getBucketItemStack();
			this.saveToBucketTag(itemStack2);
			if (!this.level.isClientSide) {
				CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer)player, itemStack2);
			}

			if (itemStack.isEmpty()) {
				player.setItemInHand(interactionHand, itemStack2);
			} else if (!player.inventory.add(itemStack2)) {
				player.drop(itemStack2, false);
			}

			this.remove();
			return true;
		} else {
			return super.mobInteract(player, interactionHand);
		}
	}

	protected void saveToBucketTag(ItemStack itemStack) {
		if (this.hasCustomName()) {
			itemStack.setHoverName(this.getCustomName());
		}
	}

	protected abstract ItemStack getBucketItemStack();

	protected boolean canRandomSwim() {
		return true;
	}

	protected abstract SoundEvent getFlopSound();

	@Override
	protected SoundEvent getSwimSound() {
		return SoundEvents.FISH_SWIM;
	}

	static class FishMoveControl extends MoveControl {
		private final AbstractFish fish;

		FishMoveControl(AbstractFish abstractFish) {
			super(abstractFish);
			this.fish = abstractFish;
		}

		@Override
		public void tick() {
			if (this.fish.isUnderLiquid(FluidTags.WATER)) {
				this.fish.setDeltaMovement(this.fish.getDeltaMovement().add(0.0, 0.005, 0.0));
			}

			if (this.operation == MoveControl.Operation.MOVE_TO && !this.fish.getNavigation().isDone()) {
				double d = this.wantedX - this.fish.x;
				double e = this.wantedY - this.fish.y;
				double f = this.wantedZ - this.fish.z;
				double g = (double)Mth.sqrt(d * d + e * e + f * f);
				e /= g;
				float h = (float)(Mth.atan2(f, d) * 180.0F / (float)Math.PI) - 90.0F;
				this.fish.yRot = this.rotlerp(this.fish.yRot, h, 90.0F);
				this.fish.yBodyRot = this.fish.yRot;
				float i = (float)(this.speedModifier * this.fish.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue());
				this.fish.setSpeed(Mth.lerp(0.125F, this.fish.getSpeed(), i));
				this.fish.setDeltaMovement(this.fish.getDeltaMovement().add(0.0, (double)this.fish.getSpeed() * e * 0.1, 0.0));
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
