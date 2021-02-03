package net.minecraft.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class AzaleaBlock extends BushBlock {
	protected AzaleaBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
		return blockState.is(Blocks.CLAY) || super.mayPlaceOn(blockState, blockGetter, blockPos);
	}
}
