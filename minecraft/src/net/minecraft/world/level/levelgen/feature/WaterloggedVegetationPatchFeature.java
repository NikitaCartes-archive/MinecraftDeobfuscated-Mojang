package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;

public class WaterloggedVegetationPatchFeature extends VegetationPatchFeature {
	public WaterloggedVegetationPatchFeature(Codec<VegetationPatchConfiguration> codec) {
		super(codec);
	}

	@Override
	protected Set<BlockPos> placeGroundPatch(
		WorldGenLevel worldGenLevel,
		VegetationPatchConfiguration vegetationPatchConfiguration,
		Random random,
		BlockPos blockPos,
		Predicate<BlockState> predicate,
		int i,
		int j
	) {
		Set<BlockPos> set = super.placeGroundPatch(worldGenLevel, vegetationPatchConfiguration, random, blockPos, predicate, i, j);
		Set<BlockPos> set2 = new HashSet();
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (BlockPos blockPos2 : set) {
			if (!isExposed(worldGenLevel, set, blockPos2, mutableBlockPos)) {
				set2.add(blockPos2);
			}
		}

		for (BlockPos blockPos2x : set2) {
			worldGenLevel.setBlock(blockPos2x, Blocks.WATER.defaultBlockState(), 2);
		}

		return set2;
	}

	private static boolean isExposed(WorldGenLevel worldGenLevel, Set<BlockPos> set, BlockPos blockPos, BlockPos.MutableBlockPos mutableBlockPos) {
		return isExposedDirection(worldGenLevel, blockPos, mutableBlockPos, Direction.NORTH)
			|| isExposedDirection(worldGenLevel, blockPos, mutableBlockPos, Direction.EAST)
			|| isExposedDirection(worldGenLevel, blockPos, mutableBlockPos, Direction.SOUTH)
			|| isExposedDirection(worldGenLevel, blockPos, mutableBlockPos, Direction.WEST)
			|| isExposedDirection(worldGenLevel, blockPos, mutableBlockPos, Direction.DOWN);
	}

	private static boolean isExposedDirection(WorldGenLevel worldGenLevel, BlockPos blockPos, BlockPos.MutableBlockPos mutableBlockPos, Direction direction) {
		mutableBlockPos.setWithOffset(blockPos, direction);
		return !worldGenLevel.getBlockState(mutableBlockPos).isFaceSturdy(worldGenLevel, mutableBlockPos, direction.getOpposite());
	}

	@Override
	protected boolean placeVegetation(
		WorldGenLevel worldGenLevel, VegetationPatchConfiguration vegetationPatchConfiguration, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos
	) {
		if (super.placeVegetation(worldGenLevel, vegetationPatchConfiguration, chunkGenerator, random, blockPos.below())) {
			BlockState blockState = worldGenLevel.getBlockState(blockPos);
			if (blockState.hasProperty(BlockStateProperties.WATERLOGGED) && !(Boolean)blockState.getValue(BlockStateProperties.WATERLOGGED)) {
				worldGenLevel.setBlock(blockPos, blockState.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true)), 2);
			}

			return true;
		} else {
			return false;
		}
	}
}
