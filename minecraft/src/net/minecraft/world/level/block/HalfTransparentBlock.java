package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class HalfTransparentBlock extends Block {
	public static final MapCodec<HalfTransparentBlock> CODEC = simpleCodec(HalfTransparentBlock::new);

	@Override
	protected MapCodec<? extends HalfTransparentBlock> codec() {
		return CODEC;
	}

	protected HalfTransparentBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	protected boolean skipRendering(BlockState blockState, BlockState blockState2, Direction direction) {
		return blockState2.is(this) ? true : super.skipRendering(blockState, blockState2, direction);
	}
}
