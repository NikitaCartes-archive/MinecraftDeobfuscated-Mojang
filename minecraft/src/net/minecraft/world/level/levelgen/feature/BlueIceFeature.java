package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class BlueIceFeature extends Feature<NoneFeatureConfiguration> {
	public BlueIceFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
		BlockPos blockPos = featurePlaceContext.origin();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		RandomSource randomSource = featurePlaceContext.random();
		if (blockPos.getY() > worldGenLevel.getSeaLevel() - 1) {
			return false;
		} else if (!worldGenLevel.getBlockState(blockPos).is(Blocks.WATER) && !worldGenLevel.getBlockState(blockPos.below()).is(Blocks.WATER)) {
			return false;
		} else {
			boolean bl = false;

			for (Direction direction : Direction.values()) {
				if (direction != Direction.DOWN && worldGenLevel.getBlockState(blockPos.relative(direction)).is(Blocks.PACKED_ICE)) {
					bl = true;
					break;
				}
			}

			if (!bl) {
				return false;
			} else {
				worldGenLevel.setBlock(blockPos, Blocks.BLUE_ICE.defaultBlockState(), 2);

				for (int i = 0; i < 200; i++) {
					int j = randomSource.nextInt(5) - randomSource.nextInt(6);
					int k = 3;
					if (j < 2) {
						k += j / 2;
					}

					if (k >= 1) {
						BlockPos blockPos2 = blockPos.offset(randomSource.nextInt(k) - randomSource.nextInt(k), j, randomSource.nextInt(k) - randomSource.nextInt(k));
						BlockState blockState = worldGenLevel.getBlockState(blockPos2);
						if (blockState.isAir() || blockState.is(Blocks.WATER) || blockState.is(Blocks.PACKED_ICE) || blockState.is(Blocks.ICE)) {
							for (Direction direction2 : Direction.values()) {
								BlockState blockState2 = worldGenLevel.getBlockState(blockPos2.relative(direction2));
								if (blockState2.is(Blocks.BLUE_ICE)) {
									worldGenLevel.setBlock(blockPos2, Blocks.BLUE_ICE.defaultBlockState(), 2);
									break;
								}
							}
						}
					}
				}

				return true;
			}
		}
	}
}
