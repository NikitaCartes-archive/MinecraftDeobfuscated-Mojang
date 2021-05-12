package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

public class HugeBrownMushroomFeature extends AbstractHugeMushroomFeature {
	public HugeBrownMushroomFeature(Codec<HugeMushroomFeatureConfiguration> codec) {
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
		int j = hugeMushroomFeatureConfiguration.foliageRadius;

		for (int k = -j; k <= j; k++) {
			for (int l = -j; l <= j; l++) {
				boolean bl = k == -j;
				boolean bl2 = k == j;
				boolean bl3 = l == -j;
				boolean bl4 = l == j;
				boolean bl5 = bl || bl2;
				boolean bl6 = bl3 || bl4;
				if (!bl5 || !bl6) {
					mutableBlockPos.setWithOffset(blockPos, k, i, l);
					if (!levelAccessor.getBlockState(mutableBlockPos).isSolidRender(levelAccessor, mutableBlockPos)) {
						boolean bl7 = bl || bl6 && k == 1 - j;
						boolean bl8 = bl2 || bl6 && k == j - 1;
						boolean bl9 = bl3 || bl5 && l == 1 - j;
						boolean bl10 = bl4 || bl5 && l == j - 1;
						BlockState blockState = hugeMushroomFeatureConfiguration.capProvider.getState(random, blockPos);
						if (blockState.hasProperty(HugeMushroomBlock.WEST)
							&& blockState.hasProperty(HugeMushroomBlock.EAST)
							&& blockState.hasProperty(HugeMushroomBlock.NORTH)
							&& blockState.hasProperty(HugeMushroomBlock.SOUTH)) {
							blockState = blockState.setValue(HugeMushroomBlock.WEST, Boolean.valueOf(bl7))
								.setValue(HugeMushroomBlock.EAST, Boolean.valueOf(bl8))
								.setValue(HugeMushroomBlock.NORTH, Boolean.valueOf(bl9))
								.setValue(HugeMushroomBlock.SOUTH, Boolean.valueOf(bl10));
						}

						this.setBlock(levelAccessor, mutableBlockPos, blockState);
					}
				}
			}
		}
	}

	@Override
	protected int getTreeRadiusForHeight(int i, int j, int k, int l) {
		return l <= 3 ? 0 : k;
	}
}
