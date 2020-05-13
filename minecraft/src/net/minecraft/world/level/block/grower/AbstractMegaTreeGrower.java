package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;

public abstract class AbstractMegaTreeGrower extends AbstractTreeGrower {
	@Override
	public boolean growTree(ServerLevel serverLevel, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockState blockState, Random random) {
		for (int i = 0; i >= -1; i--) {
			for (int j = 0; j >= -1; j--) {
				if (isTwoByTwoSapling(blockState, serverLevel, blockPos, i, j)) {
					return this.placeMega(serverLevel, chunkGenerator, blockPos, blockState, random, i, j);
				}
			}
		}

		return super.growTree(serverLevel, chunkGenerator, blockPos, blockState, random);
	}

	@Nullable
	protected abstract ConfiguredFeature<TreeConfiguration, ?> getConfiguredMegaFeature(Random random);

	public boolean placeMega(ServerLevel serverLevel, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockState blockState, Random random, int i, int j) {
		ConfiguredFeature<TreeConfiguration, ?> configuredFeature = this.getConfiguredMegaFeature(random);
		if (configuredFeature == null) {
			return false;
		} else {
			BlockState blockState2 = Blocks.AIR.defaultBlockState();
			serverLevel.setBlock(blockPos.offset(i, 0, j), blockState2, 4);
			serverLevel.setBlock(blockPos.offset(i + 1, 0, j), blockState2, 4);
			serverLevel.setBlock(blockPos.offset(i, 0, j + 1), blockState2, 4);
			serverLevel.setBlock(blockPos.offset(i + 1, 0, j + 1), blockState2, 4);
			if (configuredFeature.place(serverLevel, serverLevel.structureFeatureManager(), chunkGenerator, random, blockPos.offset(i, 0, j))) {
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

	public static boolean isTwoByTwoSapling(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, int i, int j) {
		Block block = blockState.getBlock();
		return block == blockGetter.getBlockState(blockPos.offset(i, 0, j)).getBlock()
			&& block == blockGetter.getBlockState(blockPos.offset(i + 1, 0, j)).getBlock()
			&& block == blockGetter.getBlockState(blockPos.offset(i, 0, j + 1)).getBlock()
			&& block == blockGetter.getBlockState(blockPos.offset(i + 1, 0, j + 1)).getBlock();
	}
}
