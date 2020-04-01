package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.util.Serializable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.special.ColoredBlocks;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;

public abstract class BlockStateProvider implements Serializable {
	protected final BlockStateProviderType<?> type;
	public static final List<BlockState> ROTATABLE_BLOCKS = (List<BlockState>)Registry.BLOCK
		.stream()
		.map(Block::defaultBlockState)
		.filter(blockState -> blockState.hasProperty(RotatedPillarBlock.AXIS))
		.collect(ImmutableList.toImmutableList());

	protected BlockStateProvider(BlockStateProviderType<?> blockStateProviderType) {
		this.type = blockStateProviderType;
	}

	public abstract BlockState getState(Random random, BlockPos blockPos);

	public static BlockStateProvider random(Random random) {
		if (random.nextInt(20) != 0) {
			if (random.nextBoolean()) {
				return random.nextInt(5) == 0
					? new SimpleStateProvider(Blocks.AIR.defaultBlockState())
					: new SimpleStateProvider(Util.randomObject(random, OverworldGeneratorSettings.SAFE_BLOCKS));
			} else if (random.nextBoolean()) {
				WeightedStateProvider weightedStateProvider = new WeightedStateProvider();
				Util.randomObjectStream(random, 1, 5, OverworldGeneratorSettings.SAFE_BLOCKS)
					.forEach(blockState -> weightedStateProvider.add(blockState, random.nextInt(5)));
				return weightedStateProvider;
			} else {
				return new RainbowBlockProvider(
					(List<BlockState>)Stream.of(Util.randomObject(random, ColoredBlocks.COLORED_BLOCKS))
						.map(Block::defaultBlockState)
						.collect(ImmutableList.toImmutableList())
				);
			}
		} else {
			return new RotatedBlockProvider(((BlockState)ROTATABLE_BLOCKS.get(random.nextInt(ROTATABLE_BLOCKS.size()))).getBlock());
		}
	}

	public static BlockStateProvider random(Random random, List<BlockState> list) {
		if (random.nextBoolean()) {
			return new SimpleStateProvider(Util.randomObject(random, list));
		} else {
			WeightedStateProvider weightedStateProvider = new WeightedStateProvider();
			Util.randomObjectStream(random, 1, 5, list).forEach(blockState -> weightedStateProvider.add(blockState, random.nextInt(5)));
			return weightedStateProvider;
		}
	}
}
