package net.minecraft.world.level.redstone;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class InstantNeighborUpdater implements NeighborUpdater {
	private final Level level;

	public InstantNeighborUpdater(Level level) {
		this.level = level;
	}

	@Override
	public void shapeUpdate(Direction direction, BlockState blockState, BlockPos blockPos, BlockPos blockPos2, int i, int j) {
		NeighborUpdater.executeShapeUpdate(this.level, direction, blockState, blockPos, blockPos2, i, j - 1);
	}

	@Override
	public void neighborChanged(BlockPos blockPos, Block block, BlockPos blockPos2) {
		BlockState blockState = this.level.getBlockState(blockPos);
		this.neighborChanged(blockState, blockPos, block, blockPos2, false);
	}

	@Override
	public void neighborChanged(BlockState blockState, BlockPos blockPos, Block block, BlockPos blockPos2, boolean bl) {
		NeighborUpdater.executeUpdate(this.level, blockState, blockPos, block, blockPos2, bl);
	}
}
