package net.minecraft.world.entity.ai.goal;

import net.minecraft.world.entity.PathfinderMob;

public class ZombieAttackGoal<T extends PathfinderMob> extends MeleeAttackGoal {
	private final T zombie;
	private int raiseArmTicks;

	public ZombieAttackGoal(T pathfinderMob, double d, boolean bl) {
		super(pathfinderMob, d, bl);
		this.zombie = pathfinderMob;
	}

	@Override
	public void start() {
		super.start();
		this.raiseArmTicks = 0;
	}

	@Override
	public void stop() {
		super.stop();
		this.zombie.setAggressive(false);
	}

	@Override
	public void tick() {
		super.tick();
		this.raiseArmTicks++;
		if (this.raiseArmTicks >= 5 && this.getTicksUntilNextAttack() < this.getAttackInterval() / 2) {
			this.zombie.setAggressive(true);
		} else {
			this.zombie.setAggressive(false);
		}
	}
}
