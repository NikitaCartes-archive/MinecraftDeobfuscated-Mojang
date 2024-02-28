package net.minecraft.world.entity.animal.horse;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

public class Donkey extends AbstractChestedHorse {
	public Donkey(EntityType<? extends Donkey> entityType, Level level) {
		super(entityType, level);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.DONKEY_AMBIENT;
	}

	@Override
	protected SoundEvent getAngrySound() {
		return SoundEvents.DONKEY_ANGRY;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.DONKEY_DEATH;
	}

	@Nullable
	@Override
	protected SoundEvent getEatingSound() {
		return SoundEvents.DONKEY_EAT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.DONKEY_HURT;
	}

	@Override
	public boolean canMate(Animal animal) {
		if (animal == this) {
			return false;
		} else {
			return !(animal instanceof Donkey) && !(animal instanceof Horse) ? false : this.canParent() && ((AbstractHorse)animal).canParent();
		}
	}

	@Override
	protected void playJumpSound() {
		this.playSound(SoundEvents.DONKEY_JUMP, 0.4F, 1.0F);
	}

	@Nullable
	@Override
	public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
		EntityType<? extends AbstractHorse> entityType = ageableMob instanceof Horse ? EntityType.MULE : EntityType.DONKEY;
		AbstractHorse abstractHorse = entityType.create(serverLevel);
		if (abstractHorse != null) {
			this.setOffspringAttributes(ageableMob, abstractHorse);
		}

		return abstractHorse;
	}
}
