package net.minecraft.world.level.redstone;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class InstantNeighborUpdater implements NeighborUpdater {
	private final ServerLevel level;

	public InstantNeighborUpdater(ServerLevel serverLevel) {
		this.level = serverLevel;
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
