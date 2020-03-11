package net.minecraft.world.level.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class NetherVines {
	public static final VoxelShape SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

	public static boolean isValidGrowthState(BlockState blockState) {
		return blockState.isAir();
	}
}
