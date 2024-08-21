package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TrappedChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;

public class TrappedChestBlockEntity extends ChestBlockEntity {
	public TrappedChestBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.TRAPPED_CHEST, blockPos, blockState);
	}

	@Override
	protected void signalOpenCount(Level level, BlockPos blockPos, BlockState blockState, int i, int j) {
		super.signalOpenCount(level, blockPos, blockState, i, j);
		if (i != j) {
			Orientation orientation = ExperimentalRedstoneUtils.initialOrientation(
				level, ((Direction)blockState.getValue(TrappedChestBlock.FACING)).getOpposite(), Direction.UP
			);
			Block block = blockState.getBlock();
			level.updateNeighborsAt(blockPos, block, orientation);
			level.updateNeighborsAt(blockPos.below(), block, orientation);
		}
	}
}
