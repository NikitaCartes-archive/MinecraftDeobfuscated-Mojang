package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.material.Material;

public class HugeFungusFeature extends Feature<HugeFungusConfiguration> {
	private static final float HUGE_PROBABILITY = 0.06F;

	public HugeFungusFeature(Codec<HugeFungusConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<HugeFungusConfiguration> featurePlaceContext) {
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		BlockPos blockPos = featurePlaceContext.origin();
		RandomSource randomSource = featurePlaceContext.random();
		ChunkGenerator chunkGenerator = featurePlaceContext.chunkGenerator();
		HugeFungusConfiguration hugeFungusConfiguration = featurePlaceContext.config();
		Block block = hugeFungusConfiguration.validBaseState.getBlock();
		BlockPos blockPos2 = null;
		BlockState blockState = worldGenLevel.getBlockState(blockPos.below());
		if (blockState.is(block)) {
			blockPos2 = blockPos;
		}

		if (blockPos2 == null) {
			return false;
		} else {
			int i = Mth.nextInt(randomSource, 4, 13);
			if (randomSource.nextInt(12) == 0) {
				i *= 2;
			}

			if (!hugeFungusConfiguration.planted) {
				int j = chunkGenerator.getGenDepth();
				if (blockPos2.getY() + i + 1 >= j) {
					return false;
				}
			}

			boolean bl = !hugeFungusConfiguration.planted && randomSource.nextFloat() < 0.06F;
			worldGenLevel.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 4);
			this.placeStem(worldGenLevel, randomSource, hugeFungusConfiguration, blockPos2, i, bl);
			this.placeHat(worldGenLevel, randomSource, hugeFungusConfiguration, blockPos2, i, bl);
			return true;
		}
	}

	private static boolean isReplaceable(LevelAccessor levelAccessor, BlockPos blockPos, boolean bl) {
		return levelAccessor.isStateAtPosition(blockPos, blockState -> {
			Material material = blockState.getMaterial();
			return blockState.canBeReplaced() || bl && material == Material.PLANT;
		});
	}

	private void placeStem(
		LevelAccessor levelAccessor, RandomSource randomSource, HugeFungusConfiguration hugeFungusConfiguration, BlockPos blockPos, int i, boolean bl
	) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		BlockState blockState = hugeFungusConfiguration.stemState;
		int j = bl ? 1 : 0;

		for (int k = -j; k <= j; k++) {
			for (int l = -j; l <= j; l++) {
				boolean bl2 = bl && Mth.abs(k) == j && Mth.abs(l) == j;

				for (int m = 0; m < i; m++) {
					mutableBlockPos.setWithOffset(blockPos, k, m, l);
					if (isReplaceable(levelAccessor, mutableBlockPos, true)) {
						if (hugeFungusConfiguration.planted) {
							if (!levelAccessor.getBlockState(mutableBlockPos.below()).isAir()) {
								levelAccessor.destroyBlock(mutableBlockPos, true);
							}

							levelAccessor.setBlock(mutableBlockPos, blockState, 3);
						} else if (bl2) {
							if (randomSource.nextFloat() < 0.1F) {
								this.setBlock(levelAccessor, mutableBlockPos, blockState);
							}
						} else {
							this.setBlock(levelAccessor, mutableBlockPos, blockState);
						}
					}
				}
			}
		}
	}

	private void placeHat(
		LevelAccessor levelAccessor, RandomSource randomSource, HugeFungusConfiguration hugeFungusConfiguration, BlockPos blockPos, int i, boolean bl
	) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
		boolean bl2 = hugeFungusConfiguration.hatState.is(Blocks.NETHER_WART_BLOCK);
		int j = Math.min(randomSource.nextInt(1 + i / 3) + 5, i);
		int k = i - j;

		for (int l = k; l <= i; l++) {
			int m = l < i - randomSource.nextInt(3) ? 2 : 1;
			if (j > 8 && l < k + 4) {
				m = 3;
			}

			if (bl) {
				m++;
			}

			for (int n = -m; n <= m; n++) {
				for (int o = -m; o <= m; o++) {
					boolean bl3 = n == -m || n == m;
					boolean bl4 = o == -m || o == m;
					boolean bl5 = !bl3 && !bl4 && l != i;
					boolean bl6 = bl3 && bl4;
					boolean bl7 = l < k + 3;
					mutableBlockPos.setWithOffset(blockPos, n, l, o);
					if (isReplaceable(levelAccessor, mutableBlockPos, false)) {
						if (hugeFungusConfiguration.planted && !levelAccessor.getBlockState(mutableBlockPos.below()).isAir()) {
							levelAccessor.destroyBlock(mutableBlockPos, true);
						}

						if (bl7) {
							if (!bl5) {
								this.placeHatDropBlock(levelAccessor, randomSource, mutableBlockPos, hugeFungusConfiguration.hatState, bl2);
							}
						} else if (bl5) {
							this.placeHatBlock(levelAccessor, randomSource, hugeFungusConfiguration, mutableBlockPos, 0.1F, 0.2F, bl2 ? 0.1F : 0.0F);
						} else if (bl6) {
							this.placeHatBlock(levelAccessor, randomSource, hugeFungusConfiguration, mutableBlockPos, 0.01F, 0.7F, bl2 ? 0.083F : 0.0F);
						} else {
							this.placeHatBlock(levelAccessor, randomSource, hugeFungusConfiguration, mutableBlockPos, 5.0E-4F, 0.98F, bl2 ? 0.07F : 0.0F);
						}
					}
				}
			}
		}
	}

	private void placeHatBlock(
		LevelAccessor levelAccessor,
		RandomSource randomSource,
		HugeFungusConfiguration hugeFungusConfiguration,
		BlockPos.MutableBlockPos mutableBlockPos,
		float f,
		float g,
		float h
	) {
		if (randomSource.nextFloat() < f) {
			this.setBlock(levelAccessor, mutableBlockPos, hugeFungusConfiguration.decorState);
		} else if (randomSource.nextFloat() < g) {
			this.setBlock(levelAccessor, mutableBlockPos, hugeFungusConfiguration.hatState);
			if (randomSource.nextFloat() < h) {
				tryPlaceWeepingVines(mutableBlockPos, levelAccessor, randomSource);
			}
		}
	}

	private void placeHatDropBlock(LevelAccessor levelAccessor, RandomSource randomSource, BlockPos blockPos, BlockState blockState, boolean bl) {
		if (levelAccessor.getBlockState(blockPos.below()).is(blockState.getBlock())) {
			this.setBlock(levelAccessor, blockPos, blockState);
		} else if ((double)randomSource.nextFloat() < 0.15) {
			this.setBlock(levelAccessor, blockPos, blockState);
			if (bl && randomSource.nextInt(11) == 0) {
				tryPlaceWeepingVines(blockPos, levelAccessor, randomSource);
			}
		}
	}

	private static void tryPlaceWeepingVines(BlockPos blockPos, LevelAccessor levelAccessor, RandomSource randomSource) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable().move(Direction.DOWN);
		if (levelAccessor.isEmptyBlock(mutableBlockPos)) {
			int i = Mth.nextInt(randomSource, 1, 5);
			if (randomSource.nextInt(7) == 0) {
				i *= 2;
			}

			int j = 23;
			int k = 25;
			WeepingVinesFeature.placeWeepingVinesColumn(levelAccessor, randomSource, mutableBlockPos, i, 23, 25);
		}
	}
}
