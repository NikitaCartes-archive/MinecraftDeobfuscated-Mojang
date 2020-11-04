package net.minecraft.world.entity.ai.util;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

public class AirRandomPos {
	@Nullable
	public static Vec3 getPosTowards(PathfinderMob pathfinderMob, int i, int j, int k, Vec3 vec3, double d) {
		Vec3 vec32 = vec3.subtract(pathfinderMob.getX(), pathfinderMob.getY(), pathfinderMob.getZ());
		boolean bl = GoalUtils.mobRestricted(pathfinderMob, i);
		return RandomPos.generateRandomPos(pathfinderMob, () -> {
			BlockPos blockPos = AirAndWaterRandomPos.generateRandomPos(pathfinderMob, i, j, k, vec32.x, vec32.z, d, bl);
			return blockPos != null && !GoalUtils.isWater(pathfinderMob, blockPos) ? blockPos : null;
		});
	}
}
