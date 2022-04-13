package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.MultifaceGrowthConfiguration;

public class MultifaceGrowthFeature extends Feature<MultifaceGrowthConfiguration> {
	public MultifaceGrowthFeature(Codec<MultifaceGrowthConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<MultifaceGrowthConfiguration> featurePlaceContext) {
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos blockPos = featurePlaceContext.origin();
		RandomSource randomSource = featurePlaceContext.random();
		MultifaceGrowthConfiguration multifaceGrowthConfiguration = featurePlaceContext.config();
		if (!isAirOrWater(worldGenLevel.getBlockState(blockPos))) {
			return false;
		} else {
			List<Direction> list = multifaceGrowthConfiguration.getShuffledDirections(randomSource);
			if (placeGrowthIfPossible(worldGenLevel, blockPos, worldGenLevel.getBlockState(blockPos), multifaceGrowthConfiguration, randomSource, list)) {
				return true;
			} else {
				BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

				for (Direction direction : list) {
					mutableBlockPos.set(blockPos);
					List<Direction> list2 = multifaceGrowthConfiguration.getShuffledDirectionsExcept(randomSource, direction.getOpposite());

					for (int i = 0; i < multifaceGrowthConfiguration.searchRange; i++) {
						mutableBlockPos.setWithOffset(blockPos, direction);
						BlockState blockState = worldGenLevel.getBlockState(mutableBlockPos);
						if (!isAirOrWater(blockState) && !blockState.is(multifaceGrowthConfiguration.placeBlock)) {
							break;
						}

						if (placeGrowthIfPossible(worldGenLevel, mutableBlockPos, blockState, multifaceGrowthConfiguration, randomSource, list2)) {
							return true;
						}
					}
				}

				return false;
			}
		}
	}

	public static boolean placeGrowthIfPossible(
		WorldGenLevel worldGenLevel,
		BlockPos blockPos,
		BlockState blockState,
		MultifaceGrowthConfiguration multifaceGrowthConfiguration,
		RandomSource randomSource,
		List<Direction> list
	) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

		for (Direction direction : list) {
			BlockState blockState2 = worldGenLevel.getBlockState(mutableBlockPos.setWithOffset(blockPos, direction));
			if (blockState2.is(multifaceGrowthConfiguration.canBePlacedOn)) {
				BlockState blockState3 = multifaceGrowthConfiguration.placeBlock.getStateForPlacement(blockState, worldGenLevel, blockPos, direction);
				if (blockState3 == null) {
					return false;
				}

				worldGenLevel.setBlock(blockPos, blockState3, 3);
				worldGenLevel.getChunk(blockPos).markPosForPostprocessing(blockPos);
				if (randomSource.nextFloat() < multifaceGrowthConfiguration.chanceOfSpreading) {
					multifaceGrowthConfiguration.placeBlock
						.getSpreader()
						.spreadFromFaceTowardRandomDirection(blockState3, worldGenLevel, blockPos, direction, randomSource, true);
				}

				return true;
			}
		}

		return false;
	}

	private static boolean isAirOrWater(BlockState blockState) {
		return blockState.isAir() || blockState.is(Blocks.WATER);
	}
}
