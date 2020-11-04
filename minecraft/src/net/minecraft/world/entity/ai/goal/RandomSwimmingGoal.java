package net.minecraft.world.entity.ai.goal;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;

public class RandomSwimmingGoal extends RandomStrollGoal {
	public RandomSwimmingGoal(PathfinderMob pathfinderMob, double d, int i) {
		super(pathfinderMob, d, i);
	}

	@Nullable
	@Override
	protected Vec3 getPosition() {
		Vec3 vec3 = DefaultRandomPos.getPos(this.mob, 10, 7);
		int i = 0;

		while (
			vec3 != null && !this.mob.level.getBlockState(new BlockPos(vec3)).isPathfindable(this.mob.level, new BlockPos(vec3), PathComputationType.WATER) && i++ < 10
		) {
			vec3 = DefaultRandomPos.getPos(this.mob, 10, 7);
		}

		return vec3;
	}
}
