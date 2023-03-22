package net.minecraft.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.state.BlockState;

public class HangingSignItem extends SignItem {
	public HangingSignItem(Block block, Block block2, Item.Properties properties) {
		super(properties, block, block2, Direction.UP);
	}

	@Override
	protected boolean canPlace(LevelReader levelReader, BlockState blockState, BlockPos blockPos) {
		if (blockState.getBlock() instanceof WallHangingSignBlock wallHangingSignBlock && !wallHangingSignBlock.canPlace(blockState, levelReader, blockPos)) {
			return false;
		}

		return super.canPlace(levelReader, blockState, blockPos);
	}
}
