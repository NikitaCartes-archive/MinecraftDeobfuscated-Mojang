package net.minecraft.world.level.block.grower;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public abstract class AbstractMegaTreeGrower extends AbstractTreeGrower {
	@Override
	public boolean growTree(ServerLevel serverLevel, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockState blockState, RandomSource randomSource) {
		for (int i = 0; i >= -1; i--) {
			for (int j = 0; j >= -1; j--) {
				if (isTwoByTwoSapling(blockState, serverLevel, blockPos, i, j)) {
					return this.placeMega(serverLevel, chunkGenerator, blockPos, blockState, randomSource, i, j);
				}
			}
		}

		return super.growTree(serverLevel, chunkGenerator, blockPos, blockState, randomSource);
	}

	@Nullable
	protected abstract ResourceKey<ConfiguredFeature<?, ?>> getConfiguredMegaFeature(RandomSource randomSource);

	public boolean placeMega(
		ServerLevel serverLevel, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockState blockState, RandomSource randomSource, int i, int j
	) {
		ResourceKey<ConfiguredFeature<?, ?>> resourceKey = this.getConfiguredMegaFeature(randomSource);
		if (resourceKey == null) {
			return false;
		} else {
			Holder<ConfiguredFeature<?, ?>> holder = (Holder<ConfiguredFeature<?, ?>>)serverLevel.registryAccess()
				.registryOrThrow(Registries.CONFIGURED_FEATURE)
				.getHolder(resourceKey)
				.orElse(null);
			if (holder == null) {
				return false;
			} else {
				ConfiguredFeature<?, ?> configuredFeature = holder.value();
				BlockState blockState2 = Blocks.AIR.defaultBlockState();
				serverLevel.setBlock(blockPos.offset(i, 0, j), blockState2, 4);
				serverLevel.setBlock(blockPos.offset(i + 1, 0, j), blockState2, 4);
				serverLevel.setBlock(blockPos.offset(i, 0, j + 1), blockState2, 4);
				serverLevel.setBlock(blockPos.offset(i + 1, 0, j + 1), blockState2, 4);
				if (configuredFeature.place(serverLevel, chunkGenerator, randomSource, blockPos.offset(i, 0, j))) {
					return true;
				} else {
					serverLevel.setBlock(blockPos.offset(i, 0, j), blockState, 4);
					serverLevel.setBlock(blockPos.offset(i + 1, 0, j), blockState, 4);
					serverLevel.setBlock(blockPos.offset(i, 0, j + 1), blockState, 4);
					serverLevel.setBlock(blockPos.offset(i + 1, 0, j + 1), blockState, 4);
					return false;
				}
			}
		}
	}

	public static boolean isTwoByTwoSapling(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, int i, int j) {
		Block block = blockState.getBlock();
		return blockGetter.getBlockState(blockPos.offset(i, 0, j)).is(block)
			&& blockGetter.getBlockState(blockPos.offset(i + 1, 0, j)).is(block)
			&& blockGetter.getBlockState(blockPos.offset(i, 0, j + 1)).is(block)
			&& blockGetter.getBlockState(blockPos.offset(i + 1, 0, j + 1)).is(block);
	}
}
