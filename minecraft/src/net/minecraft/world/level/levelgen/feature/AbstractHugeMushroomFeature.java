package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

public abstract class AbstractHugeMushroomFeature extends Feature<HugeMushroomFeatureConfiguration> {
	public AbstractHugeMushroomFeature(Codec<HugeMushroomFeatureConfiguration> codec) {
		super(codec);
	}

	protected void placeTrunk(
		LevelAccessor levelAccessor,
		RandomSource randomSource,
		BlockPos blockPos,
		HugeMushroomFeatureConfiguration hugeMushroomFeatureConfiguration,
		int i,
		BlockPos.MutableBlockPos mutableBlockPos
	) {
		for (int j = 0; j < i; j++) {
			mutableBlockPos.set(blockPos).move(Direction.UP, j);
			if (!levelAccessor.getBlockState(mutableBlockPos).isSolidRender()) {
				this.setBlock(levelAccessor, mutableBlockPos, hugeMushroomFeatureConfiguration.stemProvider.getState(randomSource, blockPos));
			}
		}
	}

	protected int getTreeHeight(RandomSource randomSource) {
		int i = randomSource.nextInt(3) + 4;
		if (randomSource.nextInt(12) == 0) {
			i *= 2;
		}

		return i;
	}

	protected boolean isValidPosition(
		LevelAccessor levelAccessor,
		BlockPos blockPos,
		int i,
		BlockPos.MutableBlockPos mutableBlockPos,
		HugeMushroomFeatureConfiguration hugeMushroomFeatureConfiguration
	) {
		int j = blockPos.getY();
		if (j >= levelAccessor.getMinY() + 1 && j + i + 1 <= levelAccessor.getMaxY()) {
			BlockState blockState = levelAccessor.getBlockState(blockPos.below());
			if (!isDirt(blockState) && !blockState.is(BlockTags.MUSHROOM_GROW_BLOCK)) {
				return false;
			} else {
				for (int k = 0; k <= i; k++) {
					int l = this.getTreeRadiusForHeight(-1, -1, hugeMushroomFeatureConfiguration.foliageRadius, k);

					for (int m = -l; m <= l; m++) {
						for (int n = -l; n <= l; n++) {
							BlockState blockState2 = levelAccessor.getBlockState(mutableBlockPos.setWithOffset(blockPos, m, k, n));
							if (!blockState2.isAir() && !blockState2.is(BlockTags.LEAVES)) {
								return false;
							}
						}
					}
				}

				return true;
			}
		} else {
			return false;
		}
	}

	@Override
	public boolean place(FeaturePlaceContext<HugeMushroomFeatureConfiguration> featurePlaceContext) {
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos blockPos = featurePlaceContext.origin();
		RandomSource randomSource = featurePlaceContext.random();
		HugeMushroomFeatureConfiguration hugeMushroomFeatureConfiguration = featurePlaceContext.config();
		int i = this.getTreeHeight(randomSource);
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		if (!this.isValidPosition(worldGenLevel, blockPos, i, mutableBlockPos, hugeMushroomFeatureConfiguration)) {
			return false;
		} else {
			this.makeCap(worldGenLevel, randomSource, blockPos, i, mutableBlockPos, hugeMushroomFeatureConfiguration);
			this.placeTrunk(worldGenLevel, randomSource, blockPos, hugeMushroomFeatureConfiguration, i, mutableBlockPos);
			return true;
		}
	}

	protected abstract int getTreeRadiusForHeight(int i, int j, int k, int l);

	protected abstract void makeCap(
		LevelAccessor levelAccessor,
		RandomSource randomSource,
		BlockPos blockPos,
		int i,
		BlockPos.MutableBlockPos mutableBlockPos,
		HugeMushroomFeatureConfiguration hugeMushroomFeatureConfiguration
	);
}
