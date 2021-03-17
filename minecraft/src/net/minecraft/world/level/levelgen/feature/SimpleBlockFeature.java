package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;

public class SimpleBlockFeature extends Feature<SimpleBlockConfiguration> {
	public SimpleBlockFeature(Codec<SimpleBlockConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<SimpleBlockConfiguration> featurePlaceContext) {
		SimpleBlockConfiguration simpleBlockConfiguration = featurePlaceContext.config();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos blockPos = featurePlaceContext.origin();
		if ((simpleBlockConfiguration.placeOn.isEmpty() || simpleBlockConfiguration.placeOn.contains(worldGenLevel.getBlockState(blockPos.below())))
			&& (simpleBlockConfiguration.placeIn.isEmpty() || simpleBlockConfiguration.placeIn.contains(worldGenLevel.getBlockState(blockPos)))
			&& (simpleBlockConfiguration.placeUnder.isEmpty() || simpleBlockConfiguration.placeUnder.contains(worldGenLevel.getBlockState(blockPos.above())))) {
			BlockState blockState = simpleBlockConfiguration.toPlace.getState(featurePlaceContext.random(), blockPos);
			if (blockState.canSurvive(worldGenLevel, blockPos)) {
				if (blockState.getBlock() instanceof DoublePlantBlock) {
					if (!worldGenLevel.isEmptyBlock(blockPos.above())) {
						return false;
					}

					DoublePlantBlock doublePlantBlock = (DoublePlantBlock)blockState.getBlock();
					doublePlantBlock.placeAt(worldGenLevel, blockState, blockPos, 2);
				} else {
					worldGenLevel.setBlock(blockPos, blockState, 2);
				}

				return true;
			}
		}

		return false;
	}
}
