package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.Mob;

public class FloatGoal extends Goal {
	private final Mob mob;

	public FloatGoal(Mob mob) {
		this.mob = mob;
		this.setFlags(EnumSet.of(Goal.Flag.JUMP));
		mob.getNavigation().setCanFloat(true);
	}

	@Override
	public boolean canUse() {
		double d = (double)this.mob.getEyeHeight() < 0.4 ? 0.2 : 0.4;
		return this.mob.isInWater() && this.mob.getFluidHeight() > d || this.mob.isInLava();
	}

	@Override
	public void tick() {
		if (this.mob.getRandom().nextFloat() < 0.8F) {
			this.mob.getJumpControl().jump();
		}
	}
}
