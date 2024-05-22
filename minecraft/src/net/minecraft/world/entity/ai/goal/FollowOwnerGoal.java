package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.PathType;

public class FollowOwnerGoal extends Goal {
	private final TamableAnimal tamable;
	@Nullable
	private LivingEntity owner;
	private final double speedModifier;
	private final PathNavigation navigation;
	private int timeToRecalcPath;
	private final float stopDistance;
	private final float startDistance;
	private float oldWaterCost;

	public FollowOwnerGoal(TamableAnimal tamableAnimal, double d, float f, float g) {
		this.tamable = tamableAnimal;
		this.speedModifier = d;
		this.navigation = tamableAnimal.getNavigation();
		this.startDistance = f;
		this.stopDistance = g;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		if (!(tamableAnimal.getNavigation() instanceof GroundPathNavigation) && !(tamableAnimal.getNavigation() instanceof FlyingPathNavigation)) {
			throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
		}
	}

	@Override
	public boolean canUse() {
		LivingEntity livingEntity = this.tamable.getOwner();
		if (livingEntity == null) {
			return false;
		} else if (this.tamable.unableToMoveToOwner()) {
			return false;
		} else if (this.tamable.distanceToSqr(livingEntity) < (double)(this.startDistance * this.startDistance)) {
			return false;
		} else {
			this.owner = livingEntity;
			return true;
		}
	}

	@Override
	public boolean canContinueToUse() {
		if (this.navigation.isDone()) {
			return false;
		} else {
			return this.tamable.unableToMoveToOwner() ? false : !(this.tamable.distanceToSqr(this.owner) <= (double)(this.stopDistance * this.stopDistance));
		}
	}

	@Override
	public void start() {
		this.timeToRecalcPath = 0;
		this.oldWaterCost = this.tamable.getPathfindingMalus(PathType.WATER);
		this.tamable.setPathfindingMalus(PathType.WATER, 0.0F);
	}

	@Override
	public void stop() {
		this.owner = null;
		this.navigation.stop();
		this.tamable.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
	}

	@Override
	public void tick() {
		this.tamable.getLookControl().setLookAt(this.owner, 10.0F, (float)this.tamable.getMaxHeadXRot());
		if (--this.timeToRecalcPath <= 0) {
			this.timeToRecalcPath = this.adjustedTickDelay(10);
			if (this.tamable.shouldTryTeleportToOwner()) {
				this.tamable.tryToTeleportToOwner();
			} else {
				this.navigation.moveTo(this.owner, this.speedModifier);
			}
		}
	}
}
