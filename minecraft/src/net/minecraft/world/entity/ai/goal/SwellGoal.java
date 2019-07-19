package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;

public class SwellGoal extends Goal {
	private final Creeper creeper;
	private LivingEntity target;

	public SwellGoal(Creeper creeper) {
		this.creeper = creeper;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		LivingEntity livingEntity = this.creeper.getTarget();
		return this.creeper.getSwellDir() > 0 || livingEntity != null && this.creeper.distanceToSqr(livingEntity) < 9.0;
	}

	@Override
	public void start() {
		this.creeper.getNavigation().stop();
		this.target = this.creeper.getTarget();
	}

	@Override
	public void stop() {
		this.target = null;
	}

	@Override
	public void tick() {
		if (this.target == null) {
			this.creeper.setSwellDir(-1);
		} else if (this.creeper.distanceToSqr(this.target) > 49.0) {
			this.creeper.setSwellDir(-1);
		} else if (!this.creeper.getSensing().canSee(this.target)) {
			this.creeper.setSwellDir(-1);
		} else {
			this.creeper.setSwellDir(1);
		}
	}
}
