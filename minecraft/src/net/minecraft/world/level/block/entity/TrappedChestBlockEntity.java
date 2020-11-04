package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TrappedChestBlockEntity extends ChestBlockEntity {
	public TrappedChestBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.TRAPPED_CHEST, blockPos, blockState);
	}

	@Override
	protected void signalOpenCount(Level level, BlockPos blockPos, BlockState blockState, int i, int j) {
		super.signalOpenCount(level, blockPos, blockState, i, j);
		if (i != j) {
			Block block = blockState.getBlock();
			level.updateNeighborsAt(blockPos, block);
			level.updateNeighborsAt(blockPos.below(), block);
		}
	}
}
