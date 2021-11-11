package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TwistingVinesConfig;

public class TwistingVinesFeature extends Feature<TwistingVinesConfig> {
	public TwistingVinesFeature(Codec<TwistingVinesConfig> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<TwistingVinesConfig> featurePlaceContext) {
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos blockPos = featurePlaceContext.origin();
		if (isInvalidPlacementLocation(worldGenLevel, blockPos)) {
			return false;
		} else {
			Random random = featurePlaceContext.random();
			TwistingVinesConfig twistingVinesConfig = featurePlaceContext.config();
			int i = twistingVinesConfig.spreadWidth();
			int j = twistingVinesConfig.spreadHeight();
			int k = twistingVinesConfig.maxHeight();
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

			for (int l = 0; l < i * i; l++) {
				mutableBlockPos.set(blockPos).move(Mth.nextInt(random, -i, i), Mth.nextInt(random, -j, j), Mth.nextInt(random, -i, i));
				if (findFirstAirBlockAboveGround(worldGenLevel, mutableBlockPos) && !isInvalidPlacementLocation(worldGenLevel, mutableBlockPos)) {
					int m = Mth.nextInt(random, 1, k);
					if (random.nextInt(6) == 0) {
						m *= 2;
					}

					if (random.nextInt(5) == 0) {
						m = 1;
					}

					int n = 17;
					int o = 25;
					placeWeepingVinesColumn(worldGenLevel, random, mutableBlockPos, m, 17, 25);
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

	public static void placeWeepingVinesColumn(LevelAccessor levelAccessor, Random random, BlockPos.MutableBlockPos mutableBlockPos, int i, int j, int k) {
		for (int l = 1; l <= i; l++) {
			if (levelAccessor.isEmptyBlock(mutableBlockPos)) {
				if (l == i || !levelAccessor.isEmptyBlock(mutableBlockPos.above())) {
					levelAccessor.setBlock(
						mutableBlockPos, Blocks.TWISTING_VINES.defaultBlockState().setValue(GrowingPlantHeadBlock.AGE, Integer.valueOf(Mth.nextInt(random, j, k))), 2
					);
					break;
				}

				levelAccessor.setBlock(mutableBlockPos, Blocks.TWISTING_VINES_PLANT.defaultBlockState(), 2);
			}

			mutableBlockPos.move(Direction.UP);
		}
	}

	private static boolean isInvalidPlacementLocation(LevelAccessor levelAccessor, BlockPos blockPos) {
		if (!levelAccessor.isEmptyBlock(blockPos)) {
			return true;
		} else {
			BlockState blockState = levelAccessor.getBlockState(blockPos.below());
			return !blockState.is(Blocks.NETHERRACK) && !blockState.is(Blocks.WARPED_NYLIUM) && !blockState.is(Blocks.WARPED_WART_BLOCK);
		}
	}
}
