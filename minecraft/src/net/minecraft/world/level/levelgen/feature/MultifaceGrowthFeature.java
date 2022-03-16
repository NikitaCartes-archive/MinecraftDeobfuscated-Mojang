package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
		Random random = featurePlaceContext.random();
		MultifaceGrowthConfiguration multifaceGrowthConfiguration = featurePlaceContext.config();
		if (!isAirOrWater(worldGenLevel.getBlockState(blockPos))) {
			return false;
		} else {
			List<Direction> list = getShuffledDirections(multifaceGrowthConfiguration, random);
			if (placeGrowthIfPossible(worldGenLevel, blockPos, worldGenLevel.getBlockState(blockPos), multifaceGrowthConfiguration, random, list)) {
				return true;
			} else {
				BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

				for (Direction direction : list) {
					mutableBlockPos.set(blockPos);
					List<Direction> list2 = getShuffledDirectionsExcept(multifaceGrowthConfiguration, random, direction.getOpposite());

					for (int i = 0; i < multifaceGrowthConfiguration.searchRange; i++) {
						mutableBlockPos.setWithOffset(blockPos, direction);
						BlockState blockState = worldGenLevel.getBlockState(mutableBlockPos);
						if (!isAirOrWater(blockState) && !blockState.is(multifaceGrowthConfiguration.placeBlock)) {
							break;
						}

						if (placeGrowthIfPossible(worldGenLevel, mutableBlockPos, blockState, multifaceGrowthConfiguration, random, list2)) {
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
		Random random,
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
				if (random.nextFloat() < multifaceGrowthConfiguration.chanceOfSpreading) {
					multifaceGrowthConfiguration.placeBlock.getSpreader().spreadFromFaceTowardRandomDirection(blockState3, worldGenLevel, blockPos, direction, random, true);
				}

				return true;
			}
		}

		return false;
	}

	public static List<Direction> getShuffledDirections(MultifaceGrowthConfiguration multifaceGrowthConfiguration, Random random) {
		List<Direction> list = Lists.<Direction>newArrayList(multifaceGrowthConfiguration.validDirections);
		Collections.shuffle(list, random);
		return list;
	}

	public static List<Direction> getShuffledDirectionsExcept(MultifaceGrowthConfiguration multifaceGrowthConfiguration, Random random, Direction direction) {
		List<Direction> list = (List<Direction>)multifaceGrowthConfiguration.validDirections
			.stream()
			.filter(direction2 -> direction2 != direction)
			.collect(Collectors.toList());
		Collections.shuffle(list, random);
		return list;
	}

	private static boolean isAirOrWater(BlockState blockState) {
		return blockState.isAir() || blockState.is(Blocks.WATER);
	}
}
