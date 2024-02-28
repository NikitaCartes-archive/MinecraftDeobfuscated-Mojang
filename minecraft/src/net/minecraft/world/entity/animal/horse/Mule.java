package net.minecraft.world.entity.animal.horse;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class Mule extends AbstractChestedHorse {
	public Mule(EntityType<? extends Mule> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.MULE_AMBIENT;
	}

	@Override
	protected SoundEvent getAngrySound() {
		return SoundEvents.MULE_ANGRY;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.MULE_DEATH;
	}

	@Nullable
	@Override
	protected SoundEvent getEatingSound() {
		return SoundEvents.MULE_EAT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.MULE_HURT;
	}

	@Override
	protected void playJumpSound() {
		this.playSound(SoundEvents.MULE_JUMP, 0.4F, 1.0F);
	}

	@Override
	protected void playChestEquipsSound() {
		this.playSound(SoundEvents.MULE_CHEST, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
	}

	@Nullable
	@Override
	public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
		return EntityType.MULE.create(serverLevel);
	}
}
