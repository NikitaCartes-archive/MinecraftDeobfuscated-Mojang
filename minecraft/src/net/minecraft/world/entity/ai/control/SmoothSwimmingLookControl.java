package net.minecraft.world.entity.ai.control;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

public class SmoothSwimmingLookControl extends LookControl {
	private final int maxYRotFromCenter;
	private static final int HEAD_TILT_X = 10;
	private static final int HEAD_TILT_Y = 20;

	public SmoothSwimmingLookControl(Mob mob, int i) {
		super(mob);
		this.maxYRotFromCenter = i;
	}

	@Override
	public void tick() {
		if (this.hasWanted) {
			this.hasWanted = false;
			this.getYRotD().ifPresent(float_ -> this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, float_ + 20.0F, this.yMaxRotSpeed));
			this.getXRotD().ifPresent(float_ -> this.mob.setXRot(this.rotateTowards(this.mob.getXRot(), float_ + 10.0F, this.xMaxRotAngle)));
		} else {
			if (this.mob.getNavigation().isDone()) {
				this.mob.setXRot(this.rotateTowards(this.mob.getXRot(), 0.0F, 5.0F));
			}

			this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, this.mob.yBodyRot, this.yMaxRotSpeed);
		}

		float f = Mth.wrapDegrees(this.mob.yHeadRot - this.mob.yBodyRot);
		if (f < (float)(-this.maxYRotFromCenter)) {
			this.mob.yBodyRot -= 4.0F;
		} else if (f > (float)this.maxYRotFromCenter) {
			this.mob.yBodyRot += 4.0F;
		}
	}
}
