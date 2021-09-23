package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockFilterDecorator extends FeatureDecorator<BlockFilterConfiguration> {
	public BlockFilterDecorator(Codec<BlockFilterConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> getPositions(DecorationContext decorationContext, Random random, BlockFilterConfiguration blockFilterConfiguration, BlockPos blockPos) {
		BlockState blockState = decorationContext.getLevel().getBlockState(blockPos.offset(blockFilterConfiguration.offset()));

		for (Block block : blockFilterConfiguration.disallowed()) {
			if (blockState.is(block)) {
				return Stream.of();
			}
		}

		for (Block blockx : blockFilterConfiguration.allowed()) {
			if (blockState.is(blockx)) {
				return Stream.of(blockPos);
			}
		}

		return blockFilterConfiguration.allowed().isEmpty() ? Stream.of(blockPos) : Stream.of();
	}
}
