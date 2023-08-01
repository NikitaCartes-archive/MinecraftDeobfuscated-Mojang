package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class RandomStrollGoal extends Goal {
	public static final int DEFAULT_INTERVAL = 120;
	protected final PathfinderMob mob;
	protected double wantedX;
	protected double wantedY;
	protected double wantedZ;
	protected final double speedModifier;
	protected int interval;
	protected boolean forceTrigger;
	private final boolean checkNoActionTime;

	public RandomStrollGoal(PathfinderMob pathfinderMob, double d) {
		this(pathfinderMob, d, 120);
	}

	public RandomStrollGoal(PathfinderMob pathfinderMob, double d, int i) {
		this(pathfinderMob, d, i, true);
	}

	public RandomStrollGoal(PathfinderMob pathfinderMob, double d, int i, boolean bl) {
		this.mob = pathfinderMob;
		this.speedModifier = d;
		this.interval = i;
		this.checkNoActionTime = bl;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (this.mob.hasControllingPassenger()) {
			return false;
		} else {
			if (!this.forceTrigger) {
				if (this.checkNoActionTime && this.mob.getNoActionTime() >= 100) {
					return false;
				}

				if (this.mob.getRandom().nextInt(reducedTickDelay(this.interval)) != 0) {
					return false;
				}
			}

			Vec3 vec3 = this.getPosition();
			if (vec3 == null) {
				return false;
			} else {
				this.wantedX = vec3.x;
				this.wantedY = vec3.y;
				this.wantedZ = vec3.z;
				this.forceTrigger = false;
				return true;
			}
		}
	}

	@Nullable
	protected Vec3 getPosition() {
		return DefaultRandomPos.getPos(this.mob, 10, 7);
	}

	@Override
	public boolean canContinueToUse() {
		return !this.mob.getNavigation().isDone() && !this.mob.hasControllingPassenger();
	}

	@Override
	public void start() {
		this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
	}

	@Override
	public void stop() {
		this.mob.getNavigation().stop();
		super.stop();
	}

	public void trigger() {
		this.forceTrigger = true;
	}

	public void setInterval(int i) {
		this.interval = i;
	}
}
