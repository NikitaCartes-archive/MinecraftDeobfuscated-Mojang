package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;

public class SpringFeature extends Feature<SpringConfiguration> {
	public SpringFeature(Codec<SpringConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<SpringConfiguration> featurePlaceContext) {
		SpringConfiguration springConfiguration = featurePlaceContext.config();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos blockPos = featurePlaceContext.origin();
		if (!worldGenLevel.getBlockState(blockPos.above()).is(springConfiguration.validBlocks)) {
			return false;
		} else if (springConfiguration.requiresBlockBelow && !worldGenLevel.getBlockState(blockPos.below()).is(springConfiguration.validBlocks)) {
			return false;
		} else {
			BlockState blockState = worldGenLevel.getBlockState(blockPos);
			if (!blockState.isAir() && !blockState.is(springConfiguration.validBlocks)) {
				return false;
			} else {
				int i = 0;
				int j = 0;
				if (worldGenLevel.getBlockState(blockPos.west()).is(springConfiguration.validBlocks)) {
					j++;
				}

				if (worldGenLevel.getBlockState(blockPos.east()).is(springConfiguration.validBlocks)) {
					j++;
				}

				if (worldGenLevel.getBlockState(blockPos.north()).is(springConfiguration.validBlocks)) {
					j++;
				}

				if (worldGenLevel.getBlockState(blockPos.south()).is(springConfiguration.validBlocks)) {
					j++;
				}

				if (worldGenLevel.getBlockState(blockPos.below()).is(springConfiguration.validBlocks)) {
					j++;
				}

				int k = 0;
				if (worldGenLevel.isEmptyBlock(blockPos.west())) {
					k++;
				}

				if (worldGenLevel.isEmptyBlock(blockPos.east())) {
					k++;
				}

				if (worldGenLevel.isEmptyBlock(blockPos.north())) {
					k++;
				}

				if (worldGenLevel.isEmptyBlock(blockPos.south())) {
					k++;
				}

				if (worldGenLevel.isEmptyBlock(blockPos.below())) {
					k++;
				}

				if (j == springConfiguration.rockCount && k == springConfiguration.holeCount) {
					worldGenLevel.setBlock(blockPos, springConfiguration.state.createLegacyBlock(), 2);
					worldGenLevel.scheduleTick(blockPos, springConfiguration.state.getType(), 0);
					i++;
				}

				return i > 0;
			}
		}
	}
}
