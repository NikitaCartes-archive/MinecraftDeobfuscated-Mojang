package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.feature.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.NoneFeatureConfiguration;

public abstract class AbstractMegaTreeGrower extends AbstractTreeGrower {
	@Override
	public boolean growTree(LevelAccessor levelAccessor, ChunkGenerator<?> chunkGenerator, BlockPos blockPos, BlockState blockState, Random random) {
		for (int i = 0; i >= -1; i--) {
			for (int j = 0; j >= -1; j--) {
				if (isTwoByTwoSapling(blockState, levelAccessor, blockPos, i, j)) {
					return this.placeMega(levelAccessor, chunkGenerator, blockPos, blockState, random, i, j);
				}
			}
		}

		return super.growTree(levelAccessor, chunkGenerator, blockPos, blockState, random);
	}

	@Nullable
	protected abstract AbstractTreeFeature<NoneFeatureConfiguration> getMegaFeature(Random random);

	public boolean placeMega(LevelAccessor levelAccessor, ChunkGenerator<?> chunkGenerator, BlockPos blockPos, BlockState blockState, Random random, int i, int j) {
		AbstractTreeFeature<NoneFeatureConfiguration> abstractTreeFeature = this.getMegaFeature(random);
		if (abstractTreeFeature == null) {
			return false;
		} else {
			BlockState blockState2 = Blocks.AIR.defaultBlockState();
			levelAccessor.setBlock(blockPos.offset(i, 0, j), blockState2, 4);
			levelAccessor.setBlock(blockPos.offset(i + 1, 0, j), blockState2, 4);
			levelAccessor.setBlock(blockPos.offset(i, 0, j + 1), blockState2, 4);
			levelAccessor.setBlock(blockPos.offset(i + 1, 0, j + 1), blockState2, 4);
			if (abstractTreeFeature.place(
				levelAccessor, (ChunkGenerator<? extends ChunkGeneratorSettings>)chunkGenerator, random, blockPos.offset(i, 0, j), FeatureConfiguration.NONE, false
			)) {
				return true;
			} else {
				levelAccessor.setBlock(blockPos.offset(i, 0, j), blockState, 4);
				levelAccessor.setBlock(blockPos.offset(i + 1, 0, j), blockState, 4);
				levelAccessor.setBlock(blockPos.offset(i, 0, j + 1), blockState, 4);
				levelAccessor.setBlock(blockPos.offset(i + 1, 0, j + 1), blockState, 4);
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
