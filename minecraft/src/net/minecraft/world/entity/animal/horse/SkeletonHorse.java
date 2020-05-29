package net.minecraft.world.entity.animal.horse;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;

public class SkeletonHorse extends AbstractHorse {
	private final SkeletonTrapGoal skeletonTrapGoal = new SkeletonTrapGoal(this);
	private boolean isTrap;
	private int trapTime;

	public SkeletonHorse(EntityType<? extends SkeletonHorse> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return createBaseHorseAttributes().add(Attributes.MAX_HEALTH, 15.0).add(Attributes.MOVEMENT_SPEED, 0.2F);
	}

	@Override
	protected void randomizeAttributes() {
		this.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(this.generateRandomJumpStrength());
	}

	@Override
	protected void addBehaviourGoals() {
	}

	@Override
	protected SoundEvent getAmbientSound() {
		super.getAmbientSound();
		return this.isUnderLiquid(FluidTags.WATER) ? SoundEvents.SKELETON_HORSE_AMBIENT_WATER : SoundEvents.SKELETON_HORSE_AMBIENT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		super.getDeathSound();
		return SoundEvents.SKELETON_HORSE_DEATH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		super.getHurtSound(damageSource);
		return SoundEvents.SKELETON_HORSE_HURT;
	}

	@Override
	protected SoundEvent getSwimSound() {
		if (this.onGround) {
			if (!this.isVehicle()) {
				return SoundEvents.SKELETON_HORSE_STEP_WATER;
			}

			this.gallopSoundCounter++;
			if (this.gallopSoundCounter > 5 && this.gallopSoundCounter % 3 == 0) {
				return SoundEvents.SKELETON_HORSE_GALLOP_WATER;
			}

			if (this.gallopSoundCounter <= 5) {
				return SoundEvents.SKELETON_HORSE_STEP_WATER;
			}
		}

		return SoundEvents.SKELETON_HORSE_SWIM;
	}

	@Override
	protected void playSwimSound(float f) {
		if (this.onGround) {
			super.playSwimSound(0.3F);
		} else {
			super.playSwimSound(Math.min(0.1F, f * 25.0F));
		}
	}

	@Override
	protected void playJumpSound() {
		if (this.isInWater()) {
			this.playSound(SoundEvents.SKELETON_HORSE_JUMP_WATER, 0.4F, 1.0F);
		} else {
			super.playJumpSound();
		}
	}

	@Override
	public MobType getMobType() {
		return MobType.UNDEAD;
	}

	@Override
	public double getPassengersRidingOffset() {
		return super.getPassengersRidingOffset() - 0.1875;
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.isTrap() && this.trapTime++ >= 18000) {
			this.remove();
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putBoolean("SkeletonTrap", this.isTrap());
		compoundTag.putInt("SkeletonTrapTime", this.trapTime);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.setTrap(compoundTag.getBoolean("SkeletonTrap"));
		this.trapTime = compoundTag.getInt("SkeletonTrapTime");
	}

	@Override
	public boolean rideableUnderWater() {
		return true;
	}

	@Override
	protected float getWaterSlowDown() {
		return 0.96F;
	}

	public boolean isTrap() {
		return this.isTrap;
	}

	public void setTrap(boolean bl) {
		if (bl != this.isTrap) {
			this.isTrap = bl;
			if (bl) {
				this.goalSelector.addGoal(1, this.skeletonTrapGoal);
			} else {
				this.goalSelector.removeGoal(this.skeletonTrapGoal);
			}
		}
	}

	@Nullable
	@Override
	public AgableMob getBreedOffspring(AgableMob agableMob) {
		return EntityType.SKELETON_HORSE.create(this.level);
	}

	@Override
	public boolean mobInteract(Player player, InteractionHand interactionHand) {
		ItemStack itemStack = player.getItemInHand(interactionHand);
		if (itemStack.getItem() instanceof SpawnEggItem) {
			return super.mobInteract(player, interactionHand);
		} else if (!this.isTamed()) {
			return false;
		} else if (this.isBaby()) {
			return super.mobInteract(player, interactionHand);
		} else if (player.isSecondaryUseActive()) {
			this.openInventory(player);
			return true;
		} else if (this.isVehicle()) {
			return super.mobInteract(player, interactionHand);
		} else {
			if (!itemStack.isEmpty()) {
				if (itemStack.getItem() == Items.SADDLE && !this.isSaddled()) {
					this.openInventory(player);
					return true;
				}

				if (itemStack.interactEnemy(player, this, interactionHand)) {
					return true;
				}
			}

			this.doPlayerRide(player);
			return true;
		}
	}
}
