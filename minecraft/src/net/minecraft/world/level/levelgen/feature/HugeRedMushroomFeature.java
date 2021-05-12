package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

public class HugeRedMushroomFeature extends AbstractHugeMushroomFeature {
	public HugeRedMushroomFeature(Codec<HugeMushroomFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	protected void makeCap(
		LevelAccessor levelAccessor,
		Random random,
		BlockPos blockPos,
		int i,
		BlockPos.MutableBlockPos mutableBlockPos,
		HugeMushroomFeatureConfiguration hugeMushroomFeatureConfiguration
	) {
		for (int j = i - 3; j <= i; j++) {
			int k = j < i ? hugeMushroomFeatureConfiguration.foliageRadius : hugeMushroomFeatureConfiguration.foliageRadius - 1;
			int l = hugeMushroomFeatureConfiguration.foliageRadius - 2;

			for (int m = -k; m <= k; m++) {
				for (int n = -k; n <= k; n++) {
					boolean bl = m == -k;
					boolean bl2 = m == k;
					boolean bl3 = n == -k;
					boolean bl4 = n == k;
					boolean bl5 = bl || bl2;
					boolean bl6 = bl3 || bl4;
					if (j >= i || bl5 != bl6) {
						mutableBlockPos.setWithOffset(blockPos, m, j, n);
						if (!levelAccessor.getBlockState(mutableBlockPos).isSolidRender(levelAccessor, mutableBlockPos)) {
							BlockState blockState = hugeMushroomFeatureConfiguration.capProvider.getState(random, blockPos);
							if (blockState.hasProperty(HugeMushroomBlock.WEST)
								&& blockState.hasProperty(HugeMushroomBlock.EAST)
								&& blockState.hasProperty(HugeMushroomBlock.NORTH)
								&& blockState.hasProperty(HugeMushroomBlock.SOUTH)
								&& blockState.hasProperty(HugeMushroomBlock.UP)) {
								blockState = blockState.setValue(HugeMushroomBlock.UP, Boolean.valueOf(j >= i - 1))
									.setValue(HugeMushroomBlock.WEST, Boolean.valueOf(m < -l))
									.setValue(HugeMushroomBlock.EAST, Boolean.valueOf(m > l))
									.setValue(HugeMushroomBlock.NORTH, Boolean.valueOf(n < -l))
									.setValue(HugeMushroomBlock.SOUTH, Boolean.valueOf(n > l));
							}

							this.setBlock(levelAccessor, mutableBlockPos, blockState);
						}
					}
				}
			}
		}
	}

	@Override
	protected int getTreeRadiusForHeight(int i, int j, int k, int l) {
		int m = 0;
		if (l < j && l >= j - 3) {
			m = k;
		} else if (l == j) {
			m = k;
		}

		return m;
	}
}
