package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;

public abstract class AbstractTreeGrower {
	@Nullable
	protected abstract ConfiguredFeature<SmallTreeConfiguration, ?> getConfiguredFeature(Random random, boolean bl);

	public boolean growTree(LevelAccessor levelAccessor, ChunkGenerator<?> chunkGenerator, BlockPos blockPos, BlockState blockState, Random random) {
		ConfiguredFeature<SmallTreeConfiguration, ?> configuredFeature = this.getConfiguredFeature(random, this.hasFlowers(levelAccessor, blockPos));
		if (configuredFeature == null) {
			return false;
		} else {
			levelAccessor.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 4);
			configuredFeature.config.setFromSapling();
			if (configuredFeature.place(levelAccessor, (ChunkGenerator<? extends ChunkGeneratorSettings>)chunkGenerator, random, blockPos)) {
				return true;
			} else {
				levelAccessor.setBlock(blockPos, blockState, 4);
				return false;
			}
		}
	}

	private boolean hasFlowers(LevelAccessor levelAccessor, BlockPos blockPos) {
		for (BlockPos blockPos2 : BlockPos.MutableBlockPos.betweenClosed(blockPos.below().north(2).west(2), blockPos.above().south(2).east(2))) {
			if (levelAccessor.getBlockState(blockPos2).is(BlockTags.FLOWERS)) {
				return true;
			}
		}

		return false;
	}
}
