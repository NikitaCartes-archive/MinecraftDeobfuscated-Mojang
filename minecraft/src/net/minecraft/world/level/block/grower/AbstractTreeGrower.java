package net.minecraft.world.level.block.grower;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public abstract class AbstractTreeGrower {
	@Nullable
	protected abstract ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource randomSource, boolean bl);

	public boolean growTree(ServerLevel serverLevel, ChunkGenerator chunkGenerator, BlockPos blockPos, BlockState blockState, RandomSource randomSource) {
		ResourceKey<ConfiguredFeature<?, ?>> resourceKey = this.getConfiguredFeature(randomSource, this.hasFlowers(serverLevel, blockPos));
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
				BlockState blockState2 = serverLevel.getFluidState(blockPos).createLegacyBlock();
				serverLevel.setBlock(blockPos, blockState2, 4);
				if (configuredFeature.place(serverLevel, chunkGenerator, randomSource, blockPos)) {
					if (serverLevel.getBlockState(blockPos) == blockState2) {
						serverLevel.sendBlockUpdated(blockPos, blockState, blockState2, 2);
					}

					return true;
				} else {
					serverLevel.setBlock(blockPos, blockState, 4);
					return false;
				}
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
