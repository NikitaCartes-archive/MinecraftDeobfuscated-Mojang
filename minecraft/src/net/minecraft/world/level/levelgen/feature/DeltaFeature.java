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
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration;

public class DeltaFeature extends Feature<DeltaFeatureConfiguration> {
	private static final ImmutableList<Block> CANNOT_REPLACE = ImmutableList.of(
		Blocks.BEDROCK, Blocks.NETHER_BRICKS, Blocks.NETHER_BRICK_FENCE, Blocks.NETHER_BRICK_STAIRS, Blocks.NETHER_WART, Blocks.CHEST, Blocks.SPAWNER
	);
	private static final Direction[] DIRECTIONS = Direction.values();

	private static int calculateRadius(Random random, DeltaFeatureConfiguration deltaFeatureConfiguration) {
		return deltaFeatureConfiguration.minimumRadius + random.nextInt(deltaFeatureConfiguration.maximumRadius - deltaFeatureConfiguration.minimumRadius + 1);
	}

	private static int calculateRimSize(Random random, DeltaFeatureConfiguration deltaFeatureConfiguration) {
		return random.nextInt(deltaFeatureConfiguration.maximumRimSize + 1);
	}

	public DeltaFeature(Codec<DeltaFeatureConfiguration> codec) {
		super(codec);
	}

	public boolean place(
		WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, DeltaFeatureConfiguration deltaFeatureConfiguration
	) {
		BlockPos blockPos2 = findDeltaLevel(worldGenLevel, blockPos.mutable().clamp(Direction.Axis.Y, 1, worldGenLevel.getMaxBuildHeight() - 1));
		if (blockPos2 == null) {
			return false;
		} else {
			boolean bl = false;
			boolean bl2 = random.nextDouble() < 0.9;
			int i = bl2 ? calculateRimSize(random, deltaFeatureConfiguration) : 0;
			int j = bl2 ? calculateRimSize(random, deltaFeatureConfiguration) : 0;
			boolean bl3 = bl2 && i != 0 && j != 0;
			int k = calculateRadius(random, deltaFeatureConfiguration);
			int l = calculateRadius(random, deltaFeatureConfiguration);
			int m = Math.max(k, l);

			for (BlockPos blockPos3 : BlockPos.withinManhattan(blockPos2, k, 0, l)) {
				if (blockPos3.distManhattan(blockPos2) > m) {
					break;
				}

				if (isClear(worldGenLevel, blockPos3, deltaFeatureConfiguration)) {
					if (bl3) {
						bl = true;
						this.setBlock(worldGenLevel, blockPos3, deltaFeatureConfiguration.rim);
					}

					BlockPos blockPos4 = blockPos3.offset(i, 0, j);
					if (isClear(worldGenLevel, blockPos4, deltaFeatureConfiguration)) {
						bl = true;
						this.setBlock(worldGenLevel, blockPos4, deltaFeatureConfiguration.contents);
					}
				}
			}

			return bl;
		}
	}

	private static boolean isClear(LevelAccessor levelAccessor, BlockPos blockPos, DeltaFeatureConfiguration deltaFeatureConfiguration) {
		BlockState blockState = levelAccessor.getBlockState(blockPos);
		if (blockState.is(deltaFeatureConfiguration.contents.getBlock())) {
			return false;
		} else if (CANNOT_REPLACE.contains(blockState.getBlock())) {
			return false;
		} else {
			for (Direction direction : DIRECTIONS) {
				boolean bl = levelAccessor.getBlockState(blockPos.relative(direction)).isAir();
				if (bl && direction != Direction.UP || !bl && direction == Direction.UP) {
					return false;
				}
			}

			return true;
		}
	}

	@Nullable
	private static BlockPos findDeltaLevel(LevelAccessor levelAccessor, BlockPos.MutableBlockPos mutableBlockPos) {
		while (mutableBlockPos.getY() > 1) {
			if (levelAccessor.getBlockState(mutableBlockPos).isAir()) {
				BlockState blockState = levelAccessor.getBlockState(mutableBlockPos.move(Direction.DOWN));
				mutableBlockPos.move(Direction.UP);
				if (!blockState.is(Blocks.LAVA) && !blockState.is(Blocks.BEDROCK) && !blockState.isAir()) {
					return mutableBlockPos;
				}
			}

			mutableBlockPos.move(Direction.DOWN);
		}

		return null;
	}
}
