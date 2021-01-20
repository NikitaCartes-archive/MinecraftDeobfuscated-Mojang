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
import net.minecraft.world.level.block.GlowLichenBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.GlowLichenConfiguration;

public class GlowLichenFeature extends Feature<GlowLichenConfiguration> {
	public GlowLichenFeature(Codec<GlowLichenConfiguration> codec) {
		super(codec);
	}

	public boolean place(
		WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, GlowLichenConfiguration glowLichenConfiguration
	) {
		if (!isAirOrWater(worldGenLevel.getBlockState(blockPos))) {
			return false;
		} else {
			List<Direction> list = getShuffledDirections(glowLichenConfiguration, random);
			if (placeGlowLichenIfPossible(worldGenLevel, blockPos, worldGenLevel.getBlockState(blockPos), glowLichenConfiguration, random, list)) {
				return true;
			} else {
				BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

				for (Direction direction : list) {
					mutableBlockPos.set(blockPos);
					List<Direction> list2 = getShuffledDirectionsExcept(glowLichenConfiguration, random, direction.getOpposite());

					for (int i = 0; i < glowLichenConfiguration.searchRange; i++) {
						mutableBlockPos.setWithOffset(blockPos, direction);
						BlockState blockState = worldGenLevel.getBlockState(mutableBlockPos);
						if (!isAirOrWater(blockState) && !blockState.is(Blocks.GLOW_LICHEN)) {
							break;
						}

						if (placeGlowLichenIfPossible(worldGenLevel, mutableBlockPos, blockState, glowLichenConfiguration, random, list2)) {
							return true;
						}
					}
				}

				return false;
			}
		}
	}

	public static boolean placeGlowLichenIfPossible(
		WorldGenLevel worldGenLevel, BlockPos blockPos, BlockState blockState, GlowLichenConfiguration glowLichenConfiguration, Random random, List<Direction> list
	) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

		for (Direction direction : list) {
			BlockState blockState2 = worldGenLevel.getBlockState(mutableBlockPos.setWithOffset(blockPos, direction));
			if (glowLichenConfiguration.canBePlacedOn(blockState2.getBlock())) {
				GlowLichenBlock glowLichenBlock = (GlowLichenBlock)Blocks.GLOW_LICHEN;
				BlockState blockState3 = glowLichenBlock.getStateForPlacement(blockState, worldGenLevel, blockPos, direction);
				if (blockState3 == null) {
					return false;
				}

				worldGenLevel.setBlock(blockPos, blockState3, 3);
				if (random.nextFloat() < glowLichenConfiguration.chanceOfSpreading) {
					glowLichenBlock.spreadFromFaceTowardRandomDirection(blockState3, worldGenLevel, blockPos, direction, random);
				}

				return true;
			}
		}

		return false;
	}

	public static List<Direction> getShuffledDirections(GlowLichenConfiguration glowLichenConfiguration, Random random) {
		List<Direction> list = Lists.<Direction>newArrayList(glowLichenConfiguration.validDirections);
		Collections.shuffle(list, random);
		return list;
	}

	public static List<Direction> getShuffledDirectionsExcept(GlowLichenConfiguration glowLichenConfiguration, Random random, Direction direction) {
		List<Direction> list = (List<Direction>)glowLichenConfiguration.validDirections
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
