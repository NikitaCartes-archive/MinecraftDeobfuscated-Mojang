package net.minecraft.world.entity.ai.util;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class HoverRandomPos {
	@Nullable
	public static Vec3 getPos(PathfinderMob pathfinderMob, int i, int j, double d, double e, float f, int k, int l) {
		boolean bl = GoalUtils.mobRestricted(pathfinderMob, i);
		return RandomPos.generateRandomPos(
			pathfinderMob,
			() -> {
				BlockPos blockPos = RandomPos.generateRandomDirectionWithinRadians(pathfinderMob.getRandom(), i, j, 0, d, e, (double)f);
				if (blockPos == null) {
					return null;
				} else {
					BlockPos blockPos2 = LandRandomPos.generateRandomPosTowardDirection(pathfinderMob, i, bl, blockPos);
					if (blockPos2 == null) {
						return null;
					} else {
						blockPos2 = RandomPos.moveUpToAboveSolid(
							blockPos2,
							pathfinderMob.getRandom().nextInt(k - l + 1) + l,
							pathfinderMob.level().getMaxBuildHeight(),
							blockPosx -> GoalUtils.isSolid(pathfinderMob, blockPosx)
						);
						return !GoalUtils.isWater(pathfinderMob, blockPos2) && !GoalUtils.hasMalus(pathfinderMob, blockPos2) ? blockPos2 : null;
					}
				}
			}
		);
	}
}
