package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.material.Material;

public class IcebergFeature extends Feature<BlockStateConfiguration> {
	public IcebergFeature(Codec<BlockStateConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<BlockStateConfiguration> featurePlaceContext) {
		BlockPos blockPos = featurePlaceContext.origin();
		WorldGenLevel worldGenLevel = featurePlaceContext.level();
		blockPos = new BlockPos(blockPos.getX(), featurePlaceContext.chunkGenerator().getSeaLevel(), blockPos.getZ());
		Random random = featurePlaceContext.random();
		boolean bl = random.nextDouble() > 0.7;
		BlockState blockState = featurePlaceContext.config().state;
		double d = random.nextDouble() * 2.0 * Math.PI;
		int i = 11 - random.nextInt(5);
		int j = 3 + random.nextInt(3);
		boolean bl2 = random.nextDouble() > 0.7;
		int k = 11;
		int l = bl2 ? random.nextInt(6) + 6 : random.nextInt(15) + 3;
		if (!bl2 && random.nextDouble() > 0.9) {
			l += random.nextInt(19) + 7;
		}

		int m = Math.min(l + random.nextInt(11), 18);
		int n = Math.min(l + random.nextInt(7) - random.nextInt(5), 11);
		int o = bl2 ? i : 11;

		for (int p = -o; p < o; p++) {
			for (int q = -o; q < o; q++) {
				for (int r = 0; r < l; r++) {
					int s = bl2 ? this.heightDependentRadiusEllipse(r, l, n) : this.heightDependentRadiusRound(random, r, l, n);
					if (bl2 || p < s) {
						this.generateIcebergBlock(worldGenLevel, random, blockPos, l, p, r, q, s, o, bl2, j, d, bl, blockState);
					}
				}
			}
		}

		this.smooth(worldGenLevel, blockPos, n, l, bl2, i);

		for (int p = -o; p < o; p++) {
			for (int q = -o; q < o; q++) {
				for (int rx = -1; rx > -m; rx--) {
					int s = bl2 ? Mth.ceil((float)o * (1.0F - (float)Math.pow((double)rx, 2.0) / ((float)m * 8.0F))) : o;
					int t = this.heightDependentRadiusSteep(random, -rx, m, n);
					if (p < t) {
						this.generateIcebergBlock(worldGenLevel, random, blockPos, m, p, rx, q, t, s, bl2, j, d, bl, blockState);
					}
				}
			}
		}

		boolean bl3 = bl2 ? random.nextDouble() > 0.1 : random.nextDouble() > 0.7;
		if (bl3) {
			this.generateCutOut(random, worldGenLevel, n, l, blockPos, bl2, i, d, j);
		}

		return true;
	}

	private void generateCutOut(Random random, LevelAccessor levelAccessor, int i, int j, BlockPos blockPos, boolean bl, int k, double d, int l) {
		int m = random.nextBoolean() ? -1 : 1;
		int n = random.nextBoolean() ? -1 : 1;
		int o = random.nextInt(Math.max(i / 2 - 2, 1));
		if (random.nextBoolean()) {
			o = i / 2 + 1 - random.nextInt(Math.max(i - i / 2 - 1, 1));
		}

		int p = random.nextInt(Math.max(i / 2 - 2, 1));
		if (random.nextBoolean()) {
			p = i / 2 + 1 - random.nextInt(Math.max(i - i / 2 - 1, 1));
		}

		if (bl) {
			o = p = random.nextInt(Math.max(k - 5, 1));
		}

		BlockPos blockPos2 = new BlockPos(m * o, 0, n * p);
		double e = bl ? d + (Math.PI / 2) : random.nextDouble() * 2.0 * Math.PI;

		for (int q = 0; q < j - 3; q++) {
			int r = this.heightDependentRadiusRound(random, q, j, i);
			this.carve(r, q, blockPos, levelAccessor, false, e, blockPos2, k, l);
		}

		for (int q = -1; q > -j + random.nextInt(5); q--) {
			int r = this.heightDependentRadiusSteep(random, -q, j, i);
			this.carve(r, q, blockPos, levelAccessor, true, e, blockPos2, k, l);
		}
	}

	private void carve(int i, int j, BlockPos blockPos, LevelAccessor levelAccessor, boolean bl, double d, BlockPos blockPos2, int k, int l) {
		int m = i + 1 + k / 3;
		int n = Math.min(i - 3, 3) + l / 2 - 1;

		for (int o = -m; o < m; o++) {
			for (int p = -m; p < m; p++) {
				double e = this.signedDistanceEllipse(o, p, blockPos2, m, n, d);
				if (e < 0.0) {
					BlockPos blockPos3 = blockPos.offset(o, j, p);
					BlockState blockState = levelAccessor.getBlockState(blockPos3);
					if (isIcebergState(blockState) || blockState.is(Blocks.SNOW_BLOCK)) {
						if (bl) {
							this.setBlock(levelAccessor, blockPos3, Blocks.WATER.defaultBlockState());
						} else {
							this.setBlock(levelAccessor, blockPos3, Blocks.AIR.defaultBlockState());
							this.removeFloatingSnowLayer(levelAccessor, blockPos3);
						}
					}
				}
			}
		}
	}

	private void removeFloatingSnowLayer(LevelAccessor levelAccessor, BlockPos blockPos) {
		if (levelAccessor.getBlockState(blockPos.above()).is(Blocks.SNOW)) {
			this.setBlock(levelAccessor, blockPos.above(), Blocks.AIR.defaultBlockState());
		}
	}

	private void generateIcebergBlock(
		LevelAccessor levelAccessor,
		Random random,
		BlockPos blockPos,
		int i,
		int j,
		int k,
		int l,
		int m,
		int n,
		boolean bl,
		int o,
		double d,
		boolean bl2,
		BlockState blockState
	) {
		double e = bl ? this.signedDistanceEllipse(j, l, BlockPos.ZERO, n, this.getEllipseC(k, i, o), d) : this.signedDistanceCircle(j, l, BlockPos.ZERO, m, random);
		if (e < 0.0) {
			BlockPos blockPos2 = blockPos.offset(j, k, l);
			double f = bl ? -0.5 : (double)(-6 - random.nextInt(3));
			if (e > f && random.nextDouble() > 0.9) {
				return;
			}

			this.setIcebergBlock(blockPos2, levelAccessor, random, i - k, i, bl, bl2, blockState);
		}
	}

	private void setIcebergBlock(BlockPos blockPos, LevelAccessor levelAccessor, Random random, int i, int j, boolean bl, boolean bl2, BlockState blockState) {
		BlockState blockState2 = levelAccessor.getBlockState(blockPos);
		if (blockState2.getMaterial() == Material.AIR || blockState2.is(Blocks.SNOW_BLOCK) || blockState2.is(Blocks.ICE) || blockState2.is(Blocks.WATER)) {
			boolean bl3 = !bl || random.nextDouble() > 0.05;
			int k = bl ? 3 : 2;
			if (bl2 && !blockState2.is(Blocks.WATER) && (double)i <= (double)random.nextInt(Math.max(1, j / k)) + (double)j * 0.6 && bl3) {
				this.setBlock(levelAccessor, blockPos, Blocks.SNOW_BLOCK.defaultBlockState());
			} else {
				this.setBlock(levelAccessor, blockPos, blockState);
			}
		}
	}

	private int getEllipseC(int i, int j, int k) {
		int l = k;
		if (i > 0 && j - i <= 3) {
			l = k - (4 - (j - i));
		}

		return l;
	}

	private double signedDistanceCircle(int i, int j, BlockPos blockPos, int k, Random random) {
		float f = 10.0F * Mth.clamp(random.nextFloat(), 0.2F, 0.8F) / (float)k;
		return (double)f + Math.pow((double)(i - blockPos.getX()), 2.0) + Math.pow((double)(j - blockPos.getZ()), 2.0) - Math.pow((double)k, 2.0);
	}

	private double signedDistanceEllipse(int i, int j, BlockPos blockPos, int k, int l, double d) {
		return Math.pow(((double)(i - blockPos.getX()) * Math.cos(d) - (double)(j - blockPos.getZ()) * Math.sin(d)) / (double)k, 2.0)
			+ Math.pow(((double)(i - blockPos.getX()) * Math.sin(d) + (double)(j - blockPos.getZ()) * Math.cos(d)) / (double)l, 2.0)
			- 1.0;
	}

	private int heightDependentRadiusRound(Random random, int i, int j, int k) {
		float f = 3.5F - random.nextFloat();
		float g = (1.0F - (float)Math.pow((double)i, 2.0) / ((float)j * f)) * (float)k;
		if (j > 15 + random.nextInt(5)) {
			int l = i < 3 + random.nextInt(6) ? i / 2 : i;
			g = (1.0F - (float)l / ((float)j * f * 0.4F)) * (float)k;
		}

		return Mth.ceil(g / 2.0F);
	}

	private int heightDependentRadiusEllipse(int i, int j, int k) {
		float f = 1.0F;
		float g = (1.0F - (float)Math.pow((double)i, 2.0) / ((float)j * 1.0F)) * (float)k;
		return Mth.ceil(g / 2.0F);
	}

	private int heightDependentRadiusSteep(Random random, int i, int j, int k) {
		float f = 1.0F + random.nextFloat() / 2.0F;
		float g = (1.0F - (float)i / ((float)j * f)) * (float)k;
		return Mth.ceil(g / 2.0F);
	}

	private static boolean isIcebergState(BlockState blockState) {
		return blockState.is(Blocks.PACKED_ICE) || blockState.is(Blocks.SNOW_BLOCK) || blockState.is(Blocks.BLUE_ICE);
	}

	private boolean belowIsAir(BlockGetter blockGetter, BlockPos blockPos) {
		return blockGetter.getBlockState(blockPos.below()).getMaterial() == Material.AIR;
	}

	private void smooth(LevelAccessor levelAccessor, BlockPos blockPos, int i, int j, boolean bl, int k) {
		int l = bl ? k : i / 2;

		for (int m = -l; m <= l; m++) {
			for (int n = -l; n <= l; n++) {
				for (int o = 0; o <= j; o++) {
					BlockPos blockPos2 = blockPos.offset(m, o, n);
					BlockState blockState = levelAccessor.getBlockState(blockPos2);
					if (isIcebergState(blockState) || blockState.is(Blocks.SNOW)) {
						if (this.belowIsAir(levelAccessor, blockPos2)) {
							this.setBlock(levelAccessor, blockPos2, Blocks.AIR.defaultBlockState());
							this.setBlock(levelAccessor, blockPos2.above(), Blocks.AIR.defaultBlockState());
						} else if (isIcebergState(blockState)) {
							BlockState[] blockStates = new BlockState[]{
								levelAccessor.getBlockState(blockPos2.west()),
								levelAccessor.getBlockState(blockPos2.east()),
								levelAccessor.getBlockState(blockPos2.north()),
								levelAccessor.getBlockState(blockPos2.south())
							};
							int p = 0;

							for (BlockState blockState2 : blockStates) {
								if (!isIcebergState(blockState2)) {
									p++;
								}
							}

							if (p >= 3) {
								this.setBlock(levelAccessor, blockPos2, Blocks.AIR.defaultBlockState());
							}
						}
					}
				}
			}
		}
	}
}
