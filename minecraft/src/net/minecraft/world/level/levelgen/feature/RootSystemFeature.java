package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.RootSystemConfiguration;

public class RootSystemFeature extends Feature<RootSystemConfiguration> {
	public RootSystemFeature(Codec<RootSystemConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<RootSystemConfiguration> featurePlaceContext) {
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos blockPos = featurePlaceContext.origin();
		if (!worldGenLevel.getBlockState(blockPos).isAir()) {
			return false;
		} else {
			Random random = featurePlaceContext.random();
			BlockPos blockPos2 = featurePlaceContext.origin();
			RootSystemConfiguration rootSystemConfiguration = featurePlaceContext.config();
			BlockPos.MutableBlockPos mutableBlockPos = blockPos2.mutable();
			if (this.placeDirtAndTree(worldGenLevel, featurePlaceContext.chunkGenerator(), rootSystemConfiguration, random, mutableBlockPos, blockPos2)) {
				this.placeRoots(worldGenLevel, rootSystemConfiguration, random, blockPos2, mutableBlockPos);
			}

			return true;
		}
	}

	private boolean spaceForTree(WorldGenLevel worldGenLevel, RootSystemConfiguration rootSystemConfiguration, BlockPos blockPos) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

		for (int i = 1; i <= rootSystemConfiguration.requiredVerticalSpaceForTree; i++) {
			mutableBlockPos.move(Direction.UP);
			BlockState blockState = worldGenLevel.getBlockState(mutableBlockPos);
			if (!isAllowedTreeSpace(blockState, i, rootSystemConfiguration.allowedVerticalWaterForTree)) {
				return false;
			}
		}

		return true;
	}

	private static boolean isAllowedTreeSpace(BlockState blockState, int i, int j) {
		return blockState.isAir() || i <= j && blockState.getFluidState().is(FluidTags.WATER);
	}

	private boolean placeDirtAndTree(
		WorldGenLevel worldGenLevel,
		ChunkGenerator chunkGenerator,
		RootSystemConfiguration rootSystemConfiguration,
		Random random,
		BlockPos.MutableBlockPos mutableBlockPos,
		BlockPos blockPos
	) {
		int i = blockPos.getX();
		int j = blockPos.getZ();

		for (int k = 0; k < rootSystemConfiguration.rootColumnMaxHeight; k++) {
			mutableBlockPos.move(Direction.UP);
			if (TreeFeature.validTreePos(worldGenLevel, mutableBlockPos)) {
				if (this.spaceForTree(worldGenLevel, rootSystemConfiguration, mutableBlockPos)) {
					BlockPos blockPos2 = mutableBlockPos.below();
					if (worldGenLevel.getFluidState(blockPos2).is(FluidTags.LAVA) || !worldGenLevel.getBlockState(blockPos2).getMaterial().isSolid()) {
						return false;
					}

					if (this.tryPlaceAzaleaTree(worldGenLevel, chunkGenerator, rootSystemConfiguration, random, mutableBlockPos)) {
						return true;
					}
				}
			} else {
				this.placeRootedDirt(worldGenLevel, rootSystemConfiguration, random, i, j, mutableBlockPos);
			}
		}

		return false;
	}

	private boolean tryPlaceAzaleaTree(
		WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, RootSystemConfiguration rootSystemConfiguration, Random random, BlockPos blockPos
	) {
		return ((ConfiguredFeature)rootSystemConfiguration.treeFeature.get()).place(worldGenLevel, chunkGenerator, random, blockPos);
	}

	private void placeRootedDirt(
		WorldGenLevel worldGenLevel, RootSystemConfiguration rootSystemConfiguration, Random random, int i, int j, BlockPos.MutableBlockPos mutableBlockPos
	) {
		int k = rootSystemConfiguration.rootRadius;
		Tag<Block> tag = BlockTags.getAllTags().getTag(rootSystemConfiguration.rootReplaceable);
		Predicate<BlockState> predicate = tag == null ? blockState -> true : blockState -> blockState.is(tag);

		for (int l = 0; l < rootSystemConfiguration.rootPlacementAttempts; l++) {
			mutableBlockPos.setWithOffset(mutableBlockPos, random.nextInt(k) - random.nextInt(k), 0, random.nextInt(k) - random.nextInt(k));
			if (predicate.test(worldGenLevel.getBlockState(mutableBlockPos))) {
				worldGenLevel.setBlock(mutableBlockPos, rootSystemConfiguration.rootStateProvider.getState(random, mutableBlockPos), 2);
			}

			mutableBlockPos.setX(i);
			mutableBlockPos.setZ(j);
		}
	}

	private void placeRoots(
		WorldGenLevel worldGenLevel, RootSystemConfiguration rootSystemConfiguration, Random random, BlockPos blockPos, BlockPos.MutableBlockPos mutableBlockPos
	) {
		int i = rootSystemConfiguration.hangingRootRadius;
		int j = rootSystemConfiguration.hangingRootsVerticalSpan;

		for (int k = 0; k < rootSystemConfiguration.hangingRootPlacementAttempts; k++) {
			mutableBlockPos.setWithOffset(blockPos, random.nextInt(i) - random.nextInt(i), random.nextInt(j) - random.nextInt(j), random.nextInt(i) - random.nextInt(i));
			if (worldGenLevel.isEmptyBlock(mutableBlockPos)) {
				BlockState blockState = rootSystemConfiguration.hangingRootStateProvider.getState(random, mutableBlockPos);
				if (blockState.canSurvive(worldGenLevel, mutableBlockPos)
					&& worldGenLevel.getBlockState(mutableBlockPos.above()).isFaceSturdy(worldGenLevel, mutableBlockPos, Direction.DOWN)) {
					worldGenLevel.setBlock(mutableBlockPos, blockState, 2);
				}
			}
		}
	}
}
