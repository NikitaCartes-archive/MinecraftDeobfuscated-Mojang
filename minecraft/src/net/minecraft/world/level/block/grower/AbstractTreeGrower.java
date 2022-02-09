package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public abstract class AbstractTreeGrower {
	@Nullable
	protected abstract Holder<? extends ConfiguredFeature<?, ?>> getConfiguredFeature(Random random, boolean bl);

	public boolean growTree(ServerLevel serverLevel, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockState blockState, Random random) {
		Holder<? extends ConfiguredFeature<?, ?>> holder = this.getConfiguredFeature(random, this.hasFlowers(serverLevel, blockPos));
		if (holder == null) {
			return false;
		} else {
			ConfiguredFeature<?, ?> configuredFeature = (ConfiguredFeature<?, ?>)holder.value();
			serverLevel.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 4);
			if (configuredFeature.place(serverLevel, chunkGenerator, random, blockPos)) {
				return true;
			} else {
				serverLevel.setBlock(blockPos, blockState, 4);
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
