package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.AbstractTreeFeature;
import net.minecraft.world.level.levelgen.feature.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.NoneFeatureConfiguration;

public abstract class AbstractTreeGrower {
	@Nullable
	protected abstract AbstractTreeFeature<NoneFeatureConfiguration> getFeature(Random random);

	public boolean growTree(LevelAccessor levelAccessor, ChunkGenerator<?> chunkGenerator, BlockPos blockPos, BlockState blockState, Random random) {
		AbstractTreeFeature<NoneFeatureConfiguration> abstractTreeFeature = this.getFeature(random);
		if (abstractTreeFeature == null) {
			return false;
		} else {
			levelAccessor.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 4);
			if (abstractTreeFeature.place(
				levelAccessor, (ChunkGenerator<? extends ChunkGeneratorSettings>)chunkGenerator, random, blockPos, FeatureConfiguration.NONE, false
			)) {
				return true;
			} else {
				levelAccessor.setBlock(blockPos, blockState, 4);
				return false;
			}
		}
	}
}
