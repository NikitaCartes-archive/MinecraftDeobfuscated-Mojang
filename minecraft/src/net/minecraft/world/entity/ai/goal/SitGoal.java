package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;

public class SitGoal extends Goal {
	private final TamableAnimal mob;
	private boolean wantToSit;

	public SitGoal(TamableAnimal tamableAnimal) {
		this.mob = tamableAnimal;
		this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
	}

	@Override
	public boolean canContinueToUse() {
		return this.wantToSit;
	}

	@Override
	public boolean canUse() {
		if (!this.mob.isTame()) {
			return false;
		} else if (this.mob.isInWaterOrBubble()) {
			return false;
		} else if (!this.mob.onGround) {
			return false;
		} else {
			LivingEntity livingEntity = this.mob.getOwner();
			if (livingEntity == null) {
				return true;
			} else {
				return this.mob.distanceToSqr(livingEntity) < 144.0 && livingEntity.getLastHurtByMob() != null ? false : this.wantToSit;
			}
		}
	}

	@Override
	public void start() {
		this.mob.getNavigation().stop();
		this.mob.setSitting(true);
	}

	@Override
	public void stop() {
		this.mob.setSitting(false);
	}

	public void wantToSit(boolean bl) {
		this.wantToSit = bl;
	}
}
