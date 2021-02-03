package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.ColumnFeatureConfiguration;

public class BasaltColumnsFeature extends Feature<ColumnFeatureConfiguration> {
	private static final ImmutableList<Block> CANNOT_PLACE_ON = ImmutableList.of(
		Blocks.LAVA,
		Blocks.BEDROCK,
		Blocks.MAGMA_BLOCK,
		Blocks.SOUL_SAND,
		Blocks.NETHER_BRICKS,
		Blocks.NETHER_BRICK_FENCE,
		Blocks.NETHER_BRICK_STAIRS,
		Blocks.NETHER_WART,
		Blocks.CHEST,
		Blocks.SPAWNER
	);

	public BasaltColumnsFeature(Codec<ColumnFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<ColumnFeatureConfiguration> featurePlaceContext) {
		int i = featurePlaceContext.chunkGenerator().getSeaLevel();
		BlockPos blockPos = featurePlaceContext.origin();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		Random random = featurePlaceContext.random();
		ColumnFeatureConfiguration columnFeatureConfiguration = featurePlaceContext.config();
		if (!canPlaceAt(worldGenLevel, i, blockPos.mutable())) {
			return false;
		} else {
			int j = columnFeatureConfiguration.height().sample(random);
			boolean bl = random.nextFloat() < 0.9F;
			int k = Math.min(j, bl ? 5 : 8);
			int l = bl ? 50 : 15;
			boolean bl2 = false;

			for (BlockPos blockPos2 : BlockPos.randomBetweenClosed(
				random, l, blockPos.getX() - k, blockPos.getY(), blockPos.getZ() - k, blockPos.getX() + k, blockPos.getY(), blockPos.getZ() + k
			)) {
				int m = j - blockPos2.distManhattan(blockPos);
				if (m >= 0) {
					bl2 |= this.placeColumn(worldGenLevel, i, blockPos2, m, columnFeatureConfiguration.reach().sample(random));
				}
			}

			return bl2;
		}
	}

	private boolean placeColumn(LevelAccessor levelAccessor, int i, BlockPos blockPos, int j, int k) {
		boolean bl = false;

		for (BlockPos blockPos2 : BlockPos.betweenClosed(
			blockPos.getX() - k, blockPos.getY(), blockPos.getZ() - k, blockPos.getX() + k, blockPos.getY(), blockPos.getZ() + k
		)) {
			int l = blockPos2.distManhattan(blockPos);
			BlockPos blockPos3 = isAirOrLavaOcean(levelAccessor, i, blockPos2)
				? findSurface(levelAccessor, i, blockPos2.mutable(), l)
				: findAir(levelAccessor, blockPos2.mutable(), l);
			if (blockPos3 != null) {
				int m = j - l / 2;

				for (BlockPos.MutableBlockPos mutableBlockPos = blockPos3.mutable(); m >= 0; m--) {
					if (isAirOrLavaOcean(levelAccessor, i, mutableBlockPos)) {
						this.setBlock(levelAccessor, mutableBlockPos, Blocks.BASALT.defaultBlockState());
						mutableBlockPos.move(Direction.UP);
						bl = true;
					} else {
						if (!levelAccessor.getBlockState(mutableBlockPos).is(Blocks.BASALT)) {
							break;
						}

						mutableBlockPos.move(Direction.UP);
					}
				}
			}
		}

		return bl;
	}

	@Nullable
	private static BlockPos findSurface(LevelAccessor levelAccessor, int i, BlockPos.MutableBlockPos mutableBlockPos, int j) {
		while (mutableBlockPos.getY() > levelAccessor.getMinBuildHeight() + 1 && j > 0) {
			j--;
			if (canPlaceAt(levelAccessor, i, mutableBlockPos)) {
				return mutableBlockPos;
			}

			mutableBlockPos.move(Direction.DOWN);
		}

		return null;
	}

	private static boolean canPlaceAt(LevelAccessor levelAccessor, int i, BlockPos.MutableBlockPos mutableBlockPos) {
		if (!isAirOrLavaOcean(levelAccessor, i, mutableBlockPos)) {
			return false;
		} else {
			BlockState blockState = levelAccessor.getBlockState(mutableBlockPos.move(Direction.DOWN));
			mutableBlockPos.move(Direction.UP);
			return !blockState.isAir() && !CANNOT_PLACE_ON.contains(blockState.getBlock());
		}
	}

	@Nullable
	private static BlockPos findAir(LevelAccessor levelAccessor, BlockPos.MutableBlockPos mutableBlockPos, int i) {
		while (mutableBlockPos.getY() < levelAccessor.getMaxBuildHeight() && i > 0) {
			i--;
			BlockState blockState = levelAccessor.getBlockState(mutableBlockPos);
			if (CANNOT_PLACE_ON.contains(blockState.getBlock())) {
				return null;
			}

			if (blockState.isAir()) {
				return mutableBlockPos;
			}

			mutableBlockPos.move(Direction.UP);
		}

		return null;
	}

	private static boolean isAirOrLavaOcean(LevelAccessor levelAccessor, int i, BlockPos blockPos) {
		BlockState blockState = levelAccessor.getBlockState(blockPos);
		return blockState.isAir() || blockState.is(Blocks.LAVA) && blockPos.getY() <= i;
	}
}
