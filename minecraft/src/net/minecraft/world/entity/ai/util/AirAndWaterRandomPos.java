package net.minecraft.world.entity.ai.util;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class AirAndWaterRandomPos {
	@Nullable
	public static Vec3 getPos(PathfinderMob pathfinderMob, int i, int j, int k, double d, double e, double f) {
		boolean bl = GoalUtils.mobRestricted(pathfinderMob, i);
		return RandomPos.generateRandomPos(pathfinderMob, () -> generateRandomPos(pathfinderMob, i, j, k, d, e, f, bl));
	}

	@Nullable
	public static BlockPos generateRandomPos(PathfinderMob pathfinderMob, int i, int j, int k, double d, double e, double f, boolean bl) {
		BlockPos blockPos = RandomPos.generateRandomDirectionWithinRadians(pathfinderMob.getRandom(), i, j, k, d, e, f);
		if (blockPos == null) {
			return null;
		} else {
			BlockPos blockPos2 = RandomPos.generateRandomPosTowardDirection(pathfinderMob, i, pathfinderMob.getRandom(), blockPos);
			if (!GoalUtils.isOutsideLimits(blockPos2, pathfinderMob) && !GoalUtils.isRestricted(bl, pathfinderMob, blockPos2)) {
				blockPos2 = RandomPos.moveUpOutOfSolid(blockPos2, pathfinderMob.level.getMaxBuildHeight(), blockPosx -> GoalUtils.isSolid(pathfinderMob, blockPosx));
				return GoalUtils.hasMalus(pathfinderMob, blockPos2) ? null : blockPos2;
			} else {
				return null;
			}
		}
	}
}
