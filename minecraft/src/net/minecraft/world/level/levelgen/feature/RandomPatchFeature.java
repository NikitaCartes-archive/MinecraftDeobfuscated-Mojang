package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;

public class RandomPatchFeature extends Feature<RandomPatchConfiguration> {
	public RandomPatchFeature(Codec<RandomPatchConfiguration> codec) {
		super(codec);
	}

	public boolean place(
		WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, RandomPatchConfiguration randomPatchConfiguration
	) {
		BlockState blockState = randomPatchConfiguration.stateProvider.getState(random, blockPos);
		BlockPos blockPos2;
		if (randomPatchConfiguration.project) {
			blockPos2 = worldGenLevel.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, blockPos);
		} else {
			blockPos2 = blockPos;
		}

		int i = 0;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int j = 0; j < randomPatchConfiguration.tries; j++) {
			mutableBlockPos.setWithOffset(
				blockPos2,
				random.nextInt(randomPatchConfiguration.xspread + 1) - random.nextInt(randomPatchConfiguration.xspread + 1),
				random.nextInt(randomPatchConfiguration.yspread + 1) - random.nextInt(randomPatchConfiguration.yspread + 1),
				random.nextInt(randomPatchConfiguration.zspread + 1) - random.nextInt(randomPatchConfiguration.zspread + 1)
			);
			BlockPos blockPos3 = mutableBlockPos.below();
			BlockState blockState2 = worldGenLevel.getBlockState(blockPos3);
			if ((
					worldGenLevel.isEmptyBlock(mutableBlockPos)
						|| randomPatchConfiguration.canReplace && worldGenLevel.getBlockState(mutableBlockPos).getMaterial().isReplaceable()
				)
				&& blockState.canSurvive(worldGenLevel, mutableBlockPos)
				&& (randomPatchConfiguration.whitelist.isEmpty() || randomPatchConfiguration.whitelist.contains(blockState2.getBlock()))
				&& !randomPatchConfiguration.blacklist.contains(blockState2)
				&& (
					!randomPatchConfiguration.needWater
						|| worldGenLevel.getFluidState(blockPos3.west()).is(FluidTags.WATER)
						|| worldGenLevel.getFluidState(blockPos3.east()).is(FluidTags.WATER)
						|| worldGenLevel.getFluidState(blockPos3.north()).is(FluidTags.WATER)
						|| worldGenLevel.getFluidState(blockPos3.south()).is(FluidTags.WATER)
				)) {
				randomPatchConfiguration.blockPlacer.place(worldGenLevel, mutableBlockPos, blockState, random);
				i++;
			}
		}

		return i > 0;
	}
}
