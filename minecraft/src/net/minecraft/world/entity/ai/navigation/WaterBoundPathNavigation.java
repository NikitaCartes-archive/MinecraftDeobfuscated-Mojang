package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.SwimNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class WaterBoundPathNavigation extends PathNavigation {
	private boolean allowBreaching;

	public WaterBoundPathNavigation(Mob mob, Level level) {
		super(mob, level);
	}

	@Override
	protected PathFinder createPathFinder(int i) {
		this.allowBreaching = this.mob.getType() == EntityType.DOLPHIN;
		this.nodeEvaluator = new SwimNodeEvaluator(this.allowBreaching);
		return new PathFinder(this.nodeEvaluator, i);
	}

	@Override
	protected boolean canUpdatePath() {
		return this.allowBreaching || this.mob.isInLiquid();
	}

	@Override
	protected Vec3 getTempMobPos() {
		return new Vec3(this.mob.getX(), this.mob.getY(0.5), this.mob.getZ());
	}

	@Override
	protected double getGroundY(Vec3 vec3) {
		return vec3.y;
	}

	@Override
	protected boolean canMoveDirectly(Vec3 vec3, Vec3 vec32) {
		return isClearForMovementBetween(this.mob, vec3, vec32, false);
	}

	@Override
	public boolean isStableDestination(BlockPos blockPos) {
		return !this.level.getBlockState(blockPos).isSolidRender(this.level, blockPos);
	}

	@Override
	public void setCanFloat(boolean bl) {
	}
}
