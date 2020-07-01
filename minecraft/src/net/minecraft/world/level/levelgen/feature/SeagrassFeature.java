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
import net.minecraft.world.level.levelgen.feature.configurations.SeagrassFeatureConfiguration;

public class SeagrassFeature extends Feature<SeagrassFeatureConfiguration> {
	public SeagrassFeature(Codec<SeagrassFeatureConfiguration> codec) {
		super(codec);
	}

	public boolean place(
		WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, SeagrassFeatureConfiguration seagrassFeatureConfiguration
	) {
		int i = 0;

		for (int j = 0; j < seagrassFeatureConfiguration.count; j++) {
			int k = random.nextInt(8) - random.nextInt(8);
			int l = random.nextInt(8) - random.nextInt(8);
			int m = worldGenLevel.getHeight(Heightmap.Types.OCEAN_FLOOR, blockPos.getX() + k, blockPos.getZ() + l);
			BlockPos blockPos2 = new BlockPos(blockPos.getX() + k, m, blockPos.getZ() + l);
			if (worldGenLevel.getBlockState(blockPos2).is(Blocks.WATER)) {
				boolean bl = random.nextDouble() < seagrassFeatureConfiguration.tallSeagrassProbability;
				BlockState blockState = bl ? Blocks.TALL_SEAGRASS.defaultBlockState() : Blocks.SEAGRASS.defaultBlockState();
				if (blockState.canSurvive(worldGenLevel, blockPos2)) {
					if (bl) {
						BlockState blockState2 = blockState.setValue(TallSeagrass.HALF, DoubleBlockHalf.UPPER);
						BlockPos blockPos3 = blockPos2.above();
						if (worldGenLevel.getBlockState(blockPos3).is(Blocks.WATER)) {
							worldGenLevel.setBlock(blockPos2, blockState, 2);
							worldGenLevel.setBlock(blockPos3, blockState2, 2);
						}
					} else {
						worldGenLevel.setBlock(blockPos2, blockState, 2);
					}

					i++;
				}
			}
		}

		return i > 0;
	}
}
