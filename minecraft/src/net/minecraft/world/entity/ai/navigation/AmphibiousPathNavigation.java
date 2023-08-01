package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.AmphibiousNodeEvaluator;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;

public class AmphibiousPathNavigation extends PathNavigation {
	public AmphibiousPathNavigation(Mob mob, Level level) {
		super(mob, level);
	}

	@Override
	protected PathFinder createPathFinder(int i) {
		this.nodeEvaluator = new AmphibiousNodeEvaluator(false);
		this.nodeEvaluator.setCanPassDoors(true);
		return new PathFinder(this.nodeEvaluator, i);
	}

	@Override
	protected boolean canUpdatePath() {
		return true;
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
		return this.mob.isInLiquid() ? isClearForMovementBetween(this.mob, vec3, vec32, false) : false;
	}

	@Override
	public boolean isStableDestination(BlockPos blockPos) {
		return !this.level.getBlockState(blockPos.below()).isAir();
	}

	@Override
	public void setCanFloat(boolean bl) {
	}
}
