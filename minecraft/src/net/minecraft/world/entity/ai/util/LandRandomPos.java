package net.minecraft.world.entity.ai.util;

import java.util.function.ToDoubleFunction;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class LandRandomPos {
	@Nullable
	public static Vec3 getPos(PathfinderMob pathfinderMob, int i, int j) {
		return getPos(pathfinderMob, i, j, pathfinderMob::getWalkTargetValue);
	}

	@Nullable
	public static Vec3 getPos(PathfinderMob pathfinderMob, int i, int j, ToDoubleFunction<BlockPos> toDoubleFunction) {
		boolean bl = GoalUtils.mobRestricted(pathfinderMob, i);
		return RandomPos.generateRandomPos(() -> {
			BlockPos blockPos = RandomPos.generateRandomDirection(pathfinderMob.getRandom(), i, j);
			BlockPos blockPos2 = generateRandomPosTowardDirection(pathfinderMob, i, bl, blockPos);
			return blockPos2 == null ? null : movePosUpOutOfSolid(pathfinderMob, blockPos2);
		}, toDoubleFunction);
	}

	@Nullable
	public static Vec3 getPosTowards(PathfinderMob pathfinderMob, int i, int j, Vec3 vec3) {
		Vec3 vec32 = vec3.subtract(pathfinderMob.getX(), pathfinderMob.getY(), pathfinderMob.getZ());
		boolean bl = GoalUtils.mobRestricted(pathfinderMob, i);
		return getPosInDirection(pathfinderMob, i, j, vec32, bl);
	}

	@Nullable
	public static Vec3 getPosAway(PathfinderMob pathfinderMob, int i, int j, Vec3 vec3) {
		Vec3 vec32 = pathfinderMob.position().subtract(vec3);
		boolean bl = GoalUtils.mobRestricted(pathfinderMob, i);
		return getPosInDirection(pathfinderMob, i, j, vec32, bl);
	}

	@Nullable
	private static Vec3 getPosInDirection(PathfinderMob pathfinderMob, int i, int j, Vec3 vec3, boolean bl) {
		return RandomPos.generateRandomPos(pathfinderMob, () -> {
			BlockPos blockPos = RandomPos.generateRandomDirectionWithinRadians(pathfinderMob.getRandom(), i, j, 0, vec3.x, vec3.z, (float) (Math.PI / 2));
			if (blockPos == null) {
				return null;
			} else {
				BlockPos blockPos2 = generateRandomPosTowardDirection(pathfinderMob, i, bl, blockPos);
				return blockPos2 == null ? null : movePosUpOutOfSolid(pathfinderMob, blockPos2);
			}
		});
	}

	@Nullable
	public static BlockPos movePosUpOutOfSolid(PathfinderMob pathfinderMob, BlockPos blockPos) {
		blockPos = RandomPos.moveUpOutOfSolid(blockPos, pathfinderMob.level().getMaxBuildHeight(), blockPosx -> GoalUtils.isSolid(pathfinderMob, blockPosx));
		return !GoalUtils.isWater(pathfinderMob, blockPos) && !GoalUtils.hasMalus(pathfinderMob, blockPos) ? blockPos : null;
	}

	@Nullable
	public static BlockPos generateRandomPosTowardDirection(PathfinderMob pathfinderMob, int i, boolean bl, BlockPos blockPos) {
		BlockPos blockPos2 = RandomPos.generateRandomPosTowardDirection(pathfinderMob, i, pathfinderMob.getRandom(), blockPos);
		return !GoalUtils.isOutsideLimits(blockPos2, pathfinderMob)
				&& !GoalUtils.isRestricted(bl, pathfinderMob, blockPos2)
				&& !GoalUtils.isNotStable(pathfinderMob.getNavigation(), blockPos2)
			? blockPos2
			: null;
	}
}
