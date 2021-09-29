package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.Mob;

public class RandomLookAroundGoal extends Goal {
	private final Mob mob;
	private double relX;
	private double relZ;
	private int lookTime;

	public RandomLookAroundGoal(Mob mob) {
		this.mob = mob;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		return this.mob.getRandom().nextFloat() < 0.02F;
	}

	@Override
	public boolean canContinueToUse() {
		return this.lookTime >= 0;
	}

	@Override
	public void start() {
		double d = (Math.PI * 2) * this.mob.getRandom().nextDouble();
		this.relX = Math.cos(d);
		this.relZ = Math.sin(d);
		this.lookTime = 20 + this.mob.getRandom().nextInt(20);
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		this.lookTime--;
		this.mob.getLookControl().setLookAt(this.mob.getX() + this.relX, this.mob.getEyeY(), this.mob.getZ() + this.relZ);
	}
}
