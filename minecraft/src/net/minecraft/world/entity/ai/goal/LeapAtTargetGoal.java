package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

public class LeapAtTargetGoal extends Goal {
	private final Mob mob;
	private LivingEntity target;
	private final float yd;

	public LeapAtTargetGoal(Mob mob, float f) {
		this.mob = mob;
		this.yd = f;
		this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (this.mob.isVehicle()) {
			return false;
		} else {
			this.target = this.mob.getTarget();
			if (this.target == null) {
				return false;
			} else {
				double d = this.mob.distanceToSqr(this.target);
				if (d < 4.0 || d > 16.0) {
					return false;
				} else {
					return !this.mob.isOnGround() ? false : this.mob.getRandom().nextInt(5) == 0;
				}
			}
		}
	}

	@Override
	public boolean canContinueToUse() {
		return !this.mob.isOnGround();
	}

	@Override
	public void start() {
		Vec3 vec3 = this.mob.getDeltaMovement();
		Vec3 vec32 = new Vec3(this.target.getX() - this.mob.getX(), 0.0, this.target.getZ() - this.mob.getZ());
		if (vec32.lengthSqr() > 1.0E-7) {
			vec32 = vec32.normalize().scale(0.4).add(vec3.scale(0.2));
		}

		this.mob.setDeltaMovement(vec32.x, (double)this.yd, vec32.z);
	}
}
