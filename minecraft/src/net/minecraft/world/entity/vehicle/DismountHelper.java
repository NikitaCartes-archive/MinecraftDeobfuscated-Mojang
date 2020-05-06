package net.minecraft.world.entity.vehicle;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DismountHelper {
	public static int[][] offsetsForDirection(Direction direction) {
		Direction direction2 = direction.getClockWise();
		Direction direction3 = direction2.getOpposite();
		Direction direction4 = direction.getOpposite();
		return new int[][]{
			{direction2.getStepX(), direction2.getStepZ()},
			{direction3.getStepX(), direction3.getStepZ()},
			{direction4.getStepX() + direction2.getStepX(), direction4.getStepZ() + direction2.getStepZ()},
			{direction4.getStepX() + direction3.getStepX(), direction4.getStepZ() + direction3.getStepZ()},
			{direction.getStepX() + direction2.getStepX(), direction.getStepZ() + direction2.getStepZ()},
			{direction.getStepX() + direction3.getStepX(), direction.getStepZ() + direction3.getStepZ()},
			{direction4.getStepX(), direction4.getStepZ()},
			{direction.getStepX(), direction.getStepZ()}
		};
	}

	public static boolean isFloorValid(double d) {
		return !Double.isInfinite(d) && d < 1.0;
	}

	public static boolean canDismountTo(Level level, LivingEntity livingEntity, AABB aABB) {
		return level.getBlockCollisions(livingEntity, aABB).allMatch(VoxelShape::isEmpty);
	}
}
