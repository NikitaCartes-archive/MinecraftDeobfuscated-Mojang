package net.minecraft.world.entity.ai.util;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class GoalUtils {
	public static boolean hasGroundPathNavigation(Mob mob) {
		return mob.getNavigation() instanceof GroundPathNavigation;
	}

	public static boolean mobRestricted(PathfinderMob pathfinderMob, int i) {
		return pathfinderMob.hasRestriction()
			&& pathfinderMob.getRestrictCenter().closerToCenterThan(pathfinderMob.position(), (double)(pathfinderMob.getRestrictRadius() + (float)i) + 1.0);
	}

	public static boolean isOutsideLimits(BlockPos blockPos, PathfinderMob pathfinderMob) {
		return blockPos.getY() < pathfinderMob.level.getMinBuildHeight() || blockPos.getY() > pathfinderMob.level.getMaxBuildHeight();
	}

	public static boolean isRestricted(boolean bl, PathfinderMob pathfinderMob, BlockPos blockPos) {
		return bl && !pathfinderMob.isWithinRestriction(blockPos);
	}

	public static boolean isNotStable(PathNavigation pathNavigation, BlockPos blockPos) {
		return !pathNavigation.isStableDestination(blockPos);
	}

	public static boolean isWater(PathfinderMob pathfinderMob, BlockPos blockPos) {
		return pathfinderMob.level.getFluidState(blockPos).is(FluidTags.WATER);
	}

	public static boolean hasMalus(PathfinderMob pathfinderMob, BlockPos blockPos) {
		return pathfinderMob.getPathfindingMalus(WalkNodeEvaluator.getBlockPathTypeStatic(pathfinderMob.level, blockPos.mutable())) != 0.0F;
	}

	public static boolean isSolid(PathfinderMob pathfinderMob, BlockPos blockPos) {
		return pathfinderMob.level.getBlockState(blockPos).getMaterial().isSolid();
	}
}
