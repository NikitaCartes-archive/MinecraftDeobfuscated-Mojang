package net.minecraft.world.level.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class HalfTransparentBlock extends Block {
	protected HalfTransparentBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public boolean skipRendering(BlockState blockState, BlockState blockState2, Direction direction) {
		return blockState2.is(this) ? true : super.skipRendering(blockState, blockState2, direction);
	}
}
