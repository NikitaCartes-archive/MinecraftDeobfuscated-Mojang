package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class KelpFeature extends Feature<NoneFeatureConfiguration> {
	public KelpFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
		int i = 0;
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos blockPos = featurePlaceContext.origin();
		RandomSource randomSource = featurePlaceContext.random();
		int j = worldGenLevel.getHeight(Heightmap.Types.OCEAN_FLOOR, blockPos.getX(), blockPos.getZ());
		BlockPos blockPos2 = new BlockPos(blockPos.getX(), j, blockPos.getZ());
		if (worldGenLevel.getBlockState(blockPos2).is(Blocks.WATER)) {
			BlockState blockState = Blocks.KELP.defaultBlockState();
			BlockState blockState2 = Blocks.KELP_PLANT.defaultBlockState();
			int k = 1 + randomSource.nextInt(10);

			for (int l = 0; l <= k; l++) {
				if (worldGenLevel.getBlockState(blockPos2).is(Blocks.WATER)
					&& worldGenLevel.getBlockState(blockPos2.above()).is(Blocks.WATER)
					&& blockState2.canSurvive(worldGenLevel, blockPos2)) {
					if (l == k) {
						worldGenLevel.setBlock(blockPos2, blockState.setValue(KelpBlock.AGE, Integer.valueOf(randomSource.nextInt(4) + 20)), 2);
						i++;
					} else {
						worldGenLevel.setBlock(blockPos2, blockState2, 2);
					}
				} else if (l > 0) {
					BlockPos blockPos3 = blockPos2.below();
					if (blockState.canSurvive(worldGenLevel, blockPos3) && !worldGenLevel.getBlockState(blockPos3.below()).is(Blocks.KELP)) {
						worldGenLevel.setBlock(blockPos3, blockState.setValue(KelpBlock.AGE, Integer.valueOf(randomSource.nextInt(4) + 20)), 2);
						i++;
					}
					break;
				}

				blockPos2 = blockPos2.above();
			}
		}

		return i > 0;
	}
}
