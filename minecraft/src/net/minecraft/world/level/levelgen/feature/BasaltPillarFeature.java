package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class BasaltPillarFeature extends Feature<NoneFeatureConfiguration> {
	public BasaltPillarFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
		BlockPos blockPos = featurePlaceContext.origin();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		RandomSource randomSource = featurePlaceContext.random();
		if (worldGenLevel.isEmptyBlock(blockPos) && !worldGenLevel.isEmptyBlock(blockPos.above())) {
			BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
			BlockPos.MutableBlockPos mutableBlockPos2 = blockPos.mutable();
			boolean bl = true;
			boolean bl2 = true;
			boolean bl3 = true;
			boolean bl4 = true;

			while (worldGenLevel.isEmptyBlock(mutableBlockPos)) {
				if (worldGenLevel.isOutsideBuildHeight(mutableBlockPos)) {
					return true;
				}

				worldGenLevel.setBlock(mutableBlockPos, Blocks.BASALT.defaultBlockState(), 2);
				bl = bl && this.placeHangOff(worldGenLevel, randomSource, mutableBlockPos2.setWithOffset(mutableBlockPos, Direction.NORTH));
				bl2 = bl2 && this.placeHangOff(worldGenLevel, randomSource, mutableBlockPos2.setWithOffset(mutableBlockPos, Direction.SOUTH));
				bl3 = bl3 && this.placeHangOff(worldGenLevel, randomSource, mutableBlockPos2.setWithOffset(mutableBlockPos, Direction.WEST));
				bl4 = bl4 && this.placeHangOff(worldGenLevel, randomSource, mutableBlockPos2.setWithOffset(mutableBlockPos, Direction.EAST));
				mutableBlockPos.move(Direction.DOWN);
			}

			mutableBlockPos.move(Direction.UP);
			this.placeBaseHangOff(worldGenLevel, randomSource, mutableBlockPos2.setWithOffset(mutableBlockPos, Direction.NORTH));
			this.placeBaseHangOff(worldGenLevel, randomSource, mutableBlockPos2.setWithOffset(mutableBlockPos, Direction.SOUTH));
			this.placeBaseHangOff(worldGenLevel, randomSource, mutableBlockPos2.setWithOffset(mutableBlockPos, Direction.WEST));
			this.placeBaseHangOff(worldGenLevel, randomSource, mutableBlockPos2.setWithOffset(mutableBlockPos, Direction.EAST));
			mutableBlockPos.move(Direction.DOWN);
			BlockPos.MutableBlockPos mutableBlockPos3 = new BlockPos.MutableBlockPos();

			for (int i = -3; i < 4; i++) {
				for (int j = -3; j < 4; j++) {
					int k = Mth.abs(i) * Mth.abs(j);
					if (randomSource.nextInt(10) < 10 - k) {
						mutableBlockPos3.set(mutableBlockPos.offset(i, 0, j));
						int l = 3;

						while (worldGenLevel.isEmptyBlock(mutableBlockPos2.setWithOffset(mutableBlockPos3, Direction.DOWN))) {
							mutableBlockPos3.move(Direction.DOWN);
							if (--l <= 0) {
								break;
							}
						}

						if (!worldGenLevel.isEmptyBlock(mutableBlockPos2.setWithOffset(mutableBlockPos3, Direction.DOWN))) {
							worldGenLevel.setBlock(mutableBlockPos3, Blocks.BASALT.defaultBlockState(), 2);
						}
					}
				}
			}

			return true;
		} else {
			return false;
		}
	}

	private void placeBaseHangOff(LevelAccessor levelAccessor, RandomSource randomSource, BlockPos blockPos) {
		if (randomSource.nextBoolean()) {
			levelAccessor.setBlock(blockPos, Blocks.BASALT.defaultBlockState(), 2);
		}
	}

	private boolean placeHangOff(LevelAccessor levelAccessor, RandomSource randomSource, BlockPos blockPos) {
		if (randomSource.nextInt(10) != 0) {
			levelAccessor.setBlock(blockPos, Blocks.BASALT.defaultBlockState(), 2);
			return true;
		} else {
			return false;
		}
	}
}
