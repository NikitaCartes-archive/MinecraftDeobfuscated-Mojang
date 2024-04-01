package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TwistingVinesConfig;

public class PotatoBudsFeature extends Feature<TwistingVinesConfig> {
	public PotatoBudsFeature(Codec<TwistingVinesConfig> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<TwistingVinesConfig> featurePlaceContext) {
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos blockPos = featurePlaceContext.origin();
		if (isInvalidPlacementLocation(worldGenLevel, blockPos)) {
			return false;
		} else {
			RandomSource randomSource = featurePlaceContext.random();
			TwistingVinesConfig twistingVinesConfig = featurePlaceContext.config();
			int i = twistingVinesConfig.spreadWidth();
			int j = twistingVinesConfig.spreadHeight();
			int k = twistingVinesConfig.maxHeight();
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (int l = 0; l < i * i; l++) {
				mutableBlockPos.set(blockPos).move(Mth.nextInt(randomSource, -i, i), Mth.nextInt(randomSource, -j, j), Mth.nextInt(randomSource, -i, i));
				if (findFirstAirBlockAboveGround(worldGenLevel, mutableBlockPos) && !isInvalidPlacementLocation(worldGenLevel, mutableBlockPos)) {
					int m = Mth.nextInt(randomSource, 1, k);
					if (randomSource.nextInt(6) == 0) {
						m *= 2;
					}

					if (randomSource.nextInt(5) == 0) {
						m = 1;
					}

					placeWeepingVinesColumn(worldGenLevel, randomSource, mutableBlockPos, m);
				}
			}

			return true;
		}
	}

	private static boolean findFirstAirBlockAboveGround(LevelAccessor levelAccessor, BlockPos.MutableBlockPos mutableBlockPos) {
		do {
			mutableBlockPos.move(0, -1, 0);
			if (levelAccessor.isOutsideBuildHeight(mutableBlockPos)) {
				return false;
			}
		} while (levelAccessor.getBlockState(mutableBlockPos).isAir());

		mutableBlockPos.move(0, 1, 0);
		return true;
	}

	public static void placeWeepingVinesColumn(LevelAccessor levelAccessor, RandomSource randomSource, BlockPos.MutableBlockPos mutableBlockPos, int i) {
		levelAccessor.setBlock(mutableBlockPos, Blocks.POTATO_BUD.defaultBlockState(), 3);
		DripstoneUtilsFlex.growPointedDripstone(Blocks.POTATO_BUD, levelAccessor, mutableBlockPos, Direction.UP, i, false);
	}

	private static boolean isInvalidPlacementLocation(LevelAccessor levelAccessor, BlockPos blockPos) {
		if (!levelAccessor.isEmptyBlock(blockPos)) {
			return true;
		} else {
			BlockState blockState = levelAccessor.getBlockState(blockPos.below());
			return !blockState.is(Blocks.CORRUPTED_PEELGRASS_BLOCK);
		}
	}
}
