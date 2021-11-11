package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NetherForestVegetationConfig;

public class NetherForestVegetationFeature extends Feature<NetherForestVegetationConfig> {
	public NetherForestVegetationFeature(Codec<NetherForestVegetationConfig> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NetherForestVegetationConfig> featurePlaceContext) {
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos blockPos = featurePlaceContext.origin();
		BlockState blockState = worldGenLevel.getBlockState(blockPos.below());
		NetherForestVegetationConfig netherForestVegetationConfig = featurePlaceContext.config();
		Random random = featurePlaceContext.random();
		if (!blockState.is(BlockTags.NYLIUM)) {
			return false;
		} else {
			int i = blockPos.getY();
			if (i >= worldGenLevel.getMinBuildHeight() + 1 && i + 1 < worldGenLevel.getMaxBuildHeight()) {
				int j = 0;

				for (int k = 0; k < netherForestVegetationConfig.spreadWidth * netherForestVegetationConfig.spreadWidth; k++) {
					BlockPos blockPos2 = blockPos.offset(
						random.nextInt(netherForestVegetationConfig.spreadWidth) - random.nextInt(netherForestVegetationConfig.spreadWidth),
						random.nextInt(netherForestVegetationConfig.spreadHeight) - random.nextInt(netherForestVegetationConfig.spreadHeight),
						random.nextInt(netherForestVegetationConfig.spreadWidth) - random.nextInt(netherForestVegetationConfig.spreadWidth)
					);
					BlockState blockState2 = netherForestVegetationConfig.stateProvider.getState(random, blockPos2);
					if (worldGenLevel.isEmptyBlock(blockPos2) && blockPos2.getY() > worldGenLevel.getMinBuildHeight() && blockState2.canSurvive(worldGenLevel, blockPos2)) {
						worldGenLevel.setBlock(blockPos2, blockState2, 2);
						j++;
					}
				}

				return j > 0;
			} else {
				return false;
			}
		}
	}
}
