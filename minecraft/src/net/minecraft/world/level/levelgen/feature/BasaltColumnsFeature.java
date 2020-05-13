package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
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

	public BasaltColumnsFeature(Function<Dynamic<?>, ? extends ColumnFeatureConfiguration> function) {
		super(function);
	}

	public boolean place(
		WorldGenLevel worldGenLevel,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator chunkGenerator,
		Random random,
		BlockPos blockPos,
		ColumnFeatureConfiguration columnFeatureConfiguration
	) {
		int i = chunkGenerator.getSeaLevel();
		BlockPos blockPos2 = findSurface(worldGenLevel, i, blockPos.mutable().clamp(Direction.Axis.Y, 1, worldGenLevel.getMaxBuildHeight() - 1), Integer.MAX_VALUE);
		if (blockPos2 == null) {
			return false;
		} else {
			int j = calculateHeight(random, columnFeatureConfiguration);
			boolean bl = random.nextFloat() < 0.9F;
			int k = Math.min(j, bl ? 5 : 8);
			int l = bl ? 50 : 15;
			boolean bl2 = false;

			for (BlockPos blockPos3 : BlockPos.randomBetweenClosed(
				random, l, blockPos2.getX() - k, blockPos2.getY(), blockPos2.getZ() - k, blockPos2.getX() + k, blockPos2.getY(), blockPos2.getZ() + k
			)) {
				int m = j - blockPos3.distManhattan(blockPos2);
				if (m >= 0) {
					bl2 |= this.placeColumn(worldGenLevel, i, blockPos3, m, calculateReach(random, columnFeatureConfiguration));
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
		while (mutableBlockPos.getY() > 1 && j > 0) {
			j--;
			if (isAirOrLavaOcean(levelAccessor, i, mutableBlockPos)) {
				BlockState blockState = levelAccessor.getBlockState(mutableBlockPos.move(Direction.DOWN));
				mutableBlockPos.move(Direction.UP);
				if (!blockState.isAir() && !CANNOT_PLACE_ON.contains(blockState.getBlock())) {
					return mutableBlockPos;
				}
			}

			mutableBlockPos.move(Direction.DOWN);
		}

		return null;
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

	private static int calculateHeight(Random random, ColumnFeatureConfiguration columnFeatureConfiguration) {
		return columnFeatureConfiguration.minimumHeight + random.nextInt(columnFeatureConfiguration.maximumHeight - columnFeatureConfiguration.minimumHeight + 1);
	}

	private static int calculateReach(Random random, ColumnFeatureConfiguration columnFeatureConfiguration) {
		return columnFeatureConfiguration.minimumReach + random.nextInt(columnFeatureConfiguration.maximumReach - columnFeatureConfiguration.minimumReach + 1);
	}

	private static boolean isAirOrLavaOcean(LevelAccessor levelAccessor, int i, BlockPos blockPos) {
		BlockState blockState = levelAccessor.getBlockState(blockPos);
		return blockState.isAir() || blockState.is(Blocks.LAVA) && blockPos.getY() <= i;
	}
}
