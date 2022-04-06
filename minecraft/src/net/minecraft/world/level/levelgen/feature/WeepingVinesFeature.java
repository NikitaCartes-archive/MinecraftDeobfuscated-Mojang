package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class WeepingVinesFeature extends Feature<NoneFeatureConfiguration> {
	private static final Direction[] DIRECTIONS = Direction.values();

	public WeepingVinesFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos blockPos = featurePlaceContext.origin();
		RandomSource randomSource = featurePlaceContext.random();
		if (!worldGenLevel.isEmptyBlock(blockPos)) {
			return false;
		} else {
			BlockState blockState = worldGenLevel.getBlockState(blockPos.above());
			if (!blockState.is(Blocks.NETHERRACK) && !blockState.is(Blocks.NETHER_WART_BLOCK)) {
				return false;
			} else {
				this.placeRoofNetherWart(worldGenLevel, randomSource, blockPos);
				this.placeRoofWeepingVines(worldGenLevel, randomSource, blockPos);
				return true;
			}
		}
	}

	private void placeRoofNetherWart(LevelAccessor levelAccessor, RandomSource randomSource, BlockPos blockPos) {
		levelAccessor.setBlock(blockPos, Blocks.NETHER_WART_BLOCK.defaultBlockState(), 2);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();

		for (int i = 0; i < 200; i++) {
			mutableBlockPos.setWithOffset(
				blockPos,
				randomSource.nextInt(6) - randomSource.nextInt(6),
				randomSource.nextInt(2) - randomSource.nextInt(5),
				randomSource.nextInt(6) - randomSource.nextInt(6)
			);
			if (levelAccessor.isEmptyBlock(mutableBlockPos)) {
				int j = 0;

				for (Direction direction : DIRECTIONS) {
					BlockState blockState = levelAccessor.getBlockState(mutableBlockPos2.setWithOffset(mutableBlockPos, direction));
					if (blockState.is(Blocks.NETHERRACK) || blockState.is(Blocks.NETHER_WART_BLOCK)) {
						j++;
					}

					if (j > 1) {
						break;
					}
				}

				if (j == 1) {
					levelAccessor.setBlock(mutableBlockPos, Blocks.NETHER_WART_BLOCK.defaultBlockState(), 2);
				}
			}
		}
	}

	private void placeRoofWeepingVines(LevelAccessor levelAccessor, RandomSource randomSource, BlockPos blockPos) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (int i = 0; i < 100; i++) {
			mutableBlockPos.setWithOffset(
				blockPos,
				randomSource.nextInt(8) - randomSource.nextInt(8),
				randomSource.nextInt(2) - randomSource.nextInt(7),
				randomSource.nextInt(8) - randomSource.nextInt(8)
			);
			if (levelAccessor.isEmptyBlock(mutableBlockPos)) {
				BlockState blockState = levelAccessor.getBlockState(mutableBlockPos.above());
				if (blockState.is(Blocks.NETHERRACK) || blockState.is(Blocks.NETHER_WART_BLOCK)) {
					int j = Mth.nextInt(randomSource, 1, 8);
					if (randomSource.nextInt(6) == 0) {
						j *= 2;
					}

					if (randomSource.nextInt(5) == 0) {
						j = 1;
					}

					int k = 17;
					int l = 25;
					placeWeepingVinesColumn(levelAccessor, randomSource, mutableBlockPos, j, 17, 25);
				}
			}
		}
	}

	public static void placeWeepingVinesColumn(
		LevelAccessor levelAccessor, RandomSource randomSource, BlockPos.MutableBlockPos mutableBlockPos, int i, int j, int k
	) {
		for (int l = 0; l <= i; l++) {
			if (levelAccessor.isEmptyBlock(mutableBlockPos)) {
				if (l == i || !levelAccessor.isEmptyBlock(mutableBlockPos.below())) {
					levelAccessor.setBlock(
						mutableBlockPos, Blocks.WEEPING_VINES.defaultBlockState().setValue(GrowingPlantHeadBlock.AGE, Integer.valueOf(Mth.nextInt(randomSource, j, k))), 2
					);
					break;
				}

				levelAccessor.setBlock(mutableBlockPos, Blocks.WEEPING_VINES_PLANT.defaultBlockState(), 2);
			}

			mutableBlockPos.move(Direction.DOWN);
		}
	}
}
