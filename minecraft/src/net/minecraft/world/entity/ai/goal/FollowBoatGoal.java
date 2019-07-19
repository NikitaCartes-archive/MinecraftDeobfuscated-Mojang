package net.minecraft.world.entity.ai.goal;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;

public class FollowBoatGoal extends Goal {
	private int timeToRecalcPath;
	private final PathfinderMob mob;
	private LivingEntity following;
	private BoatGoals currentGoal;

	public FollowBoatGoal(PathfinderMob pathfinderMob) {
		this.mob = pathfinderMob;
	}

	@Override
	public boolean canUse() {
		List<Boat> list = this.mob.level.getEntitiesOfClass(Boat.class, this.mob.getBoundingBox().inflate(5.0));
		boolean bl = false;

		for (Boat boat : list) {
			if (boat.getControllingPassenger() != null
				&& (Mth.abs(((LivingEntity)boat.getControllingPassenger()).xxa) > 0.0F || Mth.abs(((LivingEntity)boat.getControllingPassenger()).zza) > 0.0F)) {
				bl = true;
				break;
			}
		}

		return this.following != null && (Mth.abs(this.following.xxa) > 0.0F || Mth.abs(this.following.zza) > 0.0F) || bl;
	}

	@Override
	public boolean isInterruptable() {
		return true;
	}

	@Override
	public boolean canContinueToUse() {
		return this.following != null && this.following.isPassenger() && (Mth.abs(this.following.xxa) > 0.0F || Mth.abs(this.following.zza) > 0.0F);
	}

	@Override
	public void start() {
		for (Boat boat : this.mob.level.getEntitiesOfClass(Boat.class, this.mob.getBoundingBox().inflate(5.0))) {
			if (boat.getControllingPassenger() != null && boat.getControllingPassenger() instanceof LivingEntity) {
				this.following = (LivingEntity)boat.getControllingPassenger();
				break;
			}
		}

		this.timeToRecalcPath = 0;
		this.currentGoal = BoatGoals.GO_TO_BOAT;
	}

	@Override
	public void stop() {
		this.following = null;
	}

	@Override
	public void tick() {
		boolean bl = Mth.abs(this.following.xxa) > 0.0F || Mth.abs(this.following.zza) > 0.0F;
		float f = this.currentGoal == BoatGoals.GO_IN_BOAT_DIRECTION ? (bl ? 0.17999999F : 0.0F) : 0.135F;
		this.mob.moveRelative(f, new Vec3((double)this.mob.xxa, (double)this.mob.yya, (double)this.mob.zza));
		this.mob.move(MoverType.SELF, this.mob.getDeltaMovement());
		if (--this.timeToRecalcPath <= 0) {
			this.timeToRecalcPath = 10;
			if (this.currentGoal == BoatGoals.GO_TO_BOAT) {
				BlockPos blockPos = new BlockPos(this.following).relative(this.following.getDirection().getOpposite());
				blockPos = blockPos.offset(0, -1, 0);
				this.mob.getNavigation().moveTo((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), 1.0);
				if (this.mob.distanceTo(this.following) < 4.0F) {
					this.timeToRecalcPath = 0;
					this.currentGoal = BoatGoals.GO_IN_BOAT_DIRECTION;
				}
			} else if (this.currentGoal == BoatGoals.GO_IN_BOAT_DIRECTION) {
				Direction direction = this.following.getMotionDirection();
				BlockPos blockPos2 = new BlockPos(this.following).relative(direction, 10);
				this.mob.getNavigation().moveTo((double)blockPos2.getX(), (double)(blockPos2.getY() - 1), (double)blockPos2.getZ(), 1.0);
				if (this.mob.distanceTo(this.following) > 12.0F) {
					this.timeToRecalcPath = 0;
					this.currentGoal = BoatGoals.GO_TO_BOAT;
				}
			}
		}
	}
}
