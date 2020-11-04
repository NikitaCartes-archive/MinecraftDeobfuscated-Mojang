package net.minecraft.world.entity.ai.util;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class DefaultRandomPos {
	@Nullable
	public static Vec3 getPos(PathfinderMob pathfinderMob, int i, int j) {
		boolean bl = GoalUtils.mobRestricted(pathfinderMob, i);
		return RandomPos.generateRandomPos(pathfinderMob, () -> {
			BlockPos blockPos = RandomPos.generateRandomDirection(pathfinderMob.getRandom(), i, j);
			return generateRandomPosTowardDirection(pathfinderMob, i, bl, blockPos);
		});
	}

	@Nullable
	public static Vec3 getPosTowards(PathfinderMob pathfinderMob, int i, int j, Vec3 vec3, double d) {
		Vec3 vec32 = vec3.subtract(pathfinderMob.getX(), pathfinderMob.getY(), pathfinderMob.getZ());
		boolean bl = GoalUtils.mobRestricted(pathfinderMob, i);
		return RandomPos.generateRandomPos(pathfinderMob, () -> {
			BlockPos blockPos = RandomPos.generateRandomDirectionWithinRadians(pathfinderMob.getRandom(), i, j, 0, vec32.x, vec32.z, d);
			return blockPos == null ? null : generateRandomPosTowardDirection(pathfinderMob, i, bl, blockPos);
		});
	}

	@Nullable
	public static Vec3 getPosAway(PathfinderMob pathfinderMob, int i, int j, Vec3 vec3) {
		Vec3 vec32 = pathfinderMob.position().subtract(vec3);
		boolean bl = GoalUtils.mobRestricted(pathfinderMob, i);
		return RandomPos.generateRandomPos(pathfinderMob, () -> {
			BlockPos blockPos = RandomPos.generateRandomDirectionWithinRadians(pathfinderMob.getRandom(), i, j, 0, vec32.x, vec32.z, (float) (Math.PI / 2));
			return blockPos == null ? null : generateRandomPosTowardDirection(pathfinderMob, i, bl, blockPos);
		});
	}

	@Nullable
	private static BlockPos generateRandomPosTowardDirection(PathfinderMob pathfinderMob, int i, boolean bl, BlockPos blockPos) {
		BlockPos blockPos2 = RandomPos.generateRandomPosTowardDirection(pathfinderMob, i, pathfinderMob.getRandom(), blockPos);
		return !GoalUtils.isOutsideLimits(blockPos2, pathfinderMob)
				&& !GoalUtils.isRestricted(bl, pathfinderMob, blockPos2)
				&& !GoalUtils.isNotStable(pathfinderMob.getNavigation(), blockPos2)
				&& !GoalUtils.hasMalus(pathfinderMob, blockPos2)
			? blockPos2
			: null;
	}
}
