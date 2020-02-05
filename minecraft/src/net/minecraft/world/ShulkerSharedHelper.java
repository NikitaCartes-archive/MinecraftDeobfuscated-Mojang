package net.minecraft.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;

public class ShulkerSharedHelper {
	public static AABB openBoundingBox(BlockPos blockPos, Direction direction) {
		return Shapes.block()
			.bounds()
			.expandTowards((double)(0.5F * (float)direction.getStepX()), (double)(0.5F * (float)direction.getStepY()), (double)(0.5F * (float)direction.getStepZ()))
			.contract((double)direction.getStepX(), (double)direction.getStepY(), (double)direction.getStepZ())
			.move(blockPos.relative(direction));
	}
}
