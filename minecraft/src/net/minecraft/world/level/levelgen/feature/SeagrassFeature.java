package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TallSeagrass;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;

public class SeagrassFeature extends Feature<ProbabilityFeatureConfiguration> {
	public SeagrassFeature(Codec<ProbabilityFeatureConfiguration> codec) {
		super(codec);
	}

	public boolean place(
		WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, ProbabilityFeatureConfiguration probabilityFeatureConfiguration
	) {
		boolean bl = false;
		int i = random.nextInt(8) - random.nextInt(8);
		int j = random.nextInt(8) - random.nextInt(8);
		int k = worldGenLevel.getHeight(Heightmap.Types.OCEAN_FLOOR, blockPos.getX() + i, blockPos.getZ() + j);
		BlockPos blockPos2 = new BlockPos(blockPos.getX() + i, k, blockPos.getZ() + j);
		if (worldGenLevel.getBlockState(blockPos2).is(Blocks.WATER)) {
			boolean bl2 = random.nextDouble() < (double)probabilityFeatureConfiguration.probability;
			BlockState blockState = bl2 ? Blocks.TALL_SEAGRASS.defaultBlockState() : Blocks.SEAGRASS.defaultBlockState();
			if (blockState.canSurvive(worldGenLevel, blockPos2)) {
				if (bl2) {
					BlockState blockState2 = blockState.setValue(TallSeagrass.HALF, DoubleBlockHalf.UPPER);
					BlockPos blockPos3 = blockPos2.above();
					if (worldGenLevel.getBlockState(blockPos3).is(Blocks.WATER)) {
						worldGenLevel.setBlock(blockPos2, blockState, 2);
						worldGenLevel.setBlock(blockPos3, blockState2, 2);
					}
				} else {
					worldGenLevel.setBlock(blockPos2, blockState, 2);
				}

				bl = true;
			}
		}

		return bl;
	}
}
