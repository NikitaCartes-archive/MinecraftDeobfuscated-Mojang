package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class DesertWellFeature extends Feature<NoneFeatureConfiguration> {
	private static final BlockStatePredicate IS_SAND = BlockStatePredicate.forBlock(Blocks.SAND);
	private final BlockState sandSlab = Blocks.SANDSTONE_SLAB.defaultBlockState();
	private final BlockState sandstone = Blocks.SANDSTONE.defaultBlockState();
	private final BlockState water = Blocks.WATER.defaultBlockState();

	public DesertWellFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos blockPos = featurePlaceContext.origin();
		blockPos = blockPos.above();

		while (worldGenLevel.isEmptyBlock(blockPos) && blockPos.getY() > worldGenLevel.getMinBuildHeight() + 2) {
			blockPos = blockPos.below();
		}

		if (!IS_SAND.test(worldGenLevel.getBlockState(blockPos))) {
			return false;
		} else {
			for (int i = -2; i <= 2; i++) {
				for (int j = -2; j <= 2; j++) {
					if (worldGenLevel.isEmptyBlock(blockPos.offset(i, -1, j)) && worldGenLevel.isEmptyBlock(blockPos.offset(i, -2, j))) {
						return false;
					}
				}
			}

			for (int i = -1; i <= 0; i++) {
				for (int jx = -2; jx <= 2; jx++) {
					for (int k = -2; k <= 2; k++) {
						worldGenLevel.setBlock(blockPos.offset(jx, i, k), this.sandstone, 2);
					}
				}
			}

			worldGenLevel.setBlock(blockPos, this.water, 2);

			for (Direction direction : Direction.Plane.HORIZONTAL) {
				worldGenLevel.setBlock(blockPos.relative(direction), this.water, 2);
			}

			for (int i = -2; i <= 2; i++) {
				for (int jx = -2; jx <= 2; jx++) {
					if (i == -2 || i == 2 || jx == -2 || jx == 2) {
						worldGenLevel.setBlock(blockPos.offset(i, 1, jx), this.sandstone, 2);
					}
				}
			}

			worldGenLevel.setBlock(blockPos.offset(2, 1, 0), this.sandSlab, 2);
			worldGenLevel.setBlock(blockPos.offset(-2, 1, 0), this.sandSlab, 2);
			worldGenLevel.setBlock(blockPos.offset(0, 1, 2), this.sandSlab, 2);
			worldGenLevel.setBlock(blockPos.offset(0, 1, -2), this.sandSlab, 2);

			for (int i = -1; i <= 1; i++) {
				for (int jxx = -1; jxx <= 1; jxx++) {
					if (i == 0 && jxx == 0) {
						worldGenLevel.setBlock(blockPos.offset(i, 4, jxx), this.sandstone, 2);
					} else {
						worldGenLevel.setBlock(blockPos.offset(i, 4, jxx), this.sandSlab, 2);
					}
				}
			}

			for (int i = 1; i <= 3; i++) {
				worldGenLevel.setBlock(blockPos.offset(-1, i, -1), this.sandstone, 2);
				worldGenLevel.setBlock(blockPos.offset(-1, i, 1), this.sandstone, 2);
				worldGenLevel.setBlock(blockPos.offset(1, i, -1), this.sandstone, 2);
				worldGenLevel.setBlock(blockPos.offset(1, i, 1), this.sandstone, 2);
			}

			return true;
		}
	}
}
