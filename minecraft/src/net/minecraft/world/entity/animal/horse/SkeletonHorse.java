package net.minecraft.world.entity.animal.horse;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.joml.Vector3f;

public class SkeletonHorse extends AbstractHorse {
	private final SkeletonTrapGoal skeletonTrapGoal = new SkeletonTrapGoal(this);
	private static final int TRAP_MAX_LIFE = 18000;
	private boolean isTrap;
	private int trapTime;

	public SkeletonHorse(EntityType<? extends SkeletonHorse> entityType, Level level) {
		super(entityType, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return createBaseHorseAttributes().add(Attributes.MAX_HEALTH, 15.0).add(Attributes.MOVEMENT_SPEED, 0.2F);
	}

	public static boolean checkSkeletonHorseSpawnRules(
		EntityType<? extends Animal> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, RandomSource randomSource
	) {
		return !MobSpawnType.isSpawner(mobSpawnType)
			? Animal.checkAnimalSpawnRules(entityType, levelAccessor, mobSpawnType, blockPos, randomSource)
			: MobSpawnType.ignoresLightRequirements(mobSpawnType) || isBrightEnoughToSpawn(levelAccessor, blockPos);
	}

	@Override
	protected void randomizeAttributes(RandomSource randomSource) {
		this.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(generateJumpStrength(randomSource::nextDouble));
	}

	@Override
	protected void addBehaviourGoals() {
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return this.isEyeInFluid(FluidTags.WATER) ? SoundEvents.SKELETON_HORSE_AMBIENT_WATER : SoundEvents.SKELETON_HORSE_AMBIENT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.SKELETON_HORSE_DEATH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.SKELETON_HORSE_HURT;
	}

	@Override
	protected SoundEvent getSwimSound() {
		if (this.onGround()) {
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
		if (this.onGround()) {
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
	protected Vector3f getPassengerAttachmentPoint(Entity entity, EntityDimensions entityDimensions, float f) {
		return new Vector3f(0.0F, entityDimensions.height - (this.isBaby() ? 0.03125F : 0.28125F) * f, 0.0F);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.isTrap() && this.trapTime++ >= 18000) {
			this.discard();
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
	public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
		return EntityType.SKELETON_HORSE.create(serverLevel);
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
		return !this.isTamed() ? InteractionResult.PASS : super.mobInteract(player, interactionHand);
	}
}
