package net.minecraft.world.entity.ai.goal;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public class RandomStandGoal extends Goal {
	private final AbstractHorse horse;
	private int nextStand;

	public RandomStandGoal(AbstractHorse abstractHorse) {
		this.horse = abstractHorse;
		this.resetStandInterval(abstractHorse);
	}

	@Override
	public void start() {
		this.horse.standIfPossible();
		this.playStandSound();
	}

	private void playStandSound() {
		SoundEvent soundEvent = this.horse.getAmbientStandSound();
		if (soundEvent != null) {
			this.horse.playSound(soundEvent);
		}
	}

	@Override
	public boolean canContinueToUse() {
		return false;
	}

	@Override
	public boolean canUse() {
		this.nextStand++;
		if (this.nextStand > 0 && this.horse.getRandom().nextInt(1000) < this.nextStand) {
			this.resetStandInterval(this.horse);
			return !this.horse.isImmobile() && this.horse.getRandom().nextInt(10) == 0;
		} else {
			return false;
		}
	}

	private void resetStandInterval(AbstractHorse abstractHorse) {
		this.nextStand = -abstractHorse.getAmbientStandInterval();
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}
}
