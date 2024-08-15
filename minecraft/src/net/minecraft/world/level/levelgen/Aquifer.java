package net.minecraft.world.level.levelgen;

import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import org.apache.commons.lang3.mutable.MutableDouble;

public interface Aquifer {
	static Aquifer create(
		NoiseChunk noiseChunk,
		ChunkPos chunkPos,
		NoiseRouter noiseRouter,
		PositionalRandomFactory positionalRandomFactory,
		int i,
		int j,
		Aquifer.FluidPicker fluidPicker
	) {
		return new Aquifer.NoiseBasedAquifer(noiseChunk, chunkPos, noiseRouter, positionalRandomFactory, i, j, fluidPicker);
	}

	static Aquifer createDisabled(Aquifer.FluidPicker fluidPicker) {
		return new Aquifer() {
			@Nullable
			@Override
			public BlockState computeSubstance(DensityFunction.FunctionContext functionContext, double d) {
				return d > 0.0 ? null : fluidPicker.computeFluid(functionContext.blockX(), functionContext.blockY(), functionContext.blockZ()).at(functionContext.blockY());
			}

			@Override
			public boolean shouldScheduleFluidUpdate() {
				return false;
			}
		};
	}

	@Nullable
	BlockState computeSubstance(DensityFunction.FunctionContext functionContext, double d);

	boolean shouldScheduleFluidUpdate();

	public interface FluidPicker {
		Aquifer.FluidStatus computeFluid(int i, int j, int k);
	}

	public static record FluidStatus(int fluidLevel, BlockState fluidType) {

		public BlockState at(int i) {
			return i < this.fluidLevel ? this.fluidType : Blocks.AIR.defaultBlockState();
		}
	}

	public static class NoiseBasedAquifer implements Aquifer {
		private static final int X_RANGE = 10;
		private static final int Y_RANGE = 9;
		private static final int Z_RANGE = 10;
		private static final int X_SEPARATION = 6;
		private static final int Y_SEPARATION = 3;
		private static final int Z_SEPARATION = 6;
		private static final int X_SPACING = 16;
		private static final int Y_SPACING = 12;
		private static final int Z_SPACING = 16;
		private static final int MAX_REASONABLE_DISTANCE_TO_AQUIFER_CENTER = 11;
		private static final double FLOWING_UPDATE_SIMULARITY = similarity(Mth.square(10), Mth.square(12));
		private final NoiseChunk noiseChunk;
		private final DensityFunction barrierNoise;
		private final DensityFunction fluidLevelFloodednessNoise;
		private final DensityFunction fluidLevelSpreadNoise;
		private final DensityFunction lavaNoise;
		private final PositionalRandomFactory positionalRandomFactory;
		private final Aquifer.FluidStatus[] aquiferCache;
		private final long[] aquiferLocationCache;
		private final Aquifer.FluidPicker globalFluidPicker;
		private final DensityFunction erosion;
		private final DensityFunction depth;
		private boolean shouldScheduleFluidUpdate;
		private final int minGridX;
		private final int minGridY;
		private final int minGridZ;
		private final int gridSizeX;
		private final int gridSizeZ;
		private static final int[][] SURFACE_SAMPLING_OFFSETS_IN_CHUNKS = new int[][]{
			{0, 0}, {-2, -1}, {-1, -1}, {0, -1}, {1, -1}, {-3, 0}, {-2, 0}, {-1, 0}, {1, 0}, {-2, 1}, {-1, 1}, {0, 1}, {1, 1}
		};

		NoiseBasedAquifer(
			NoiseChunk noiseChunk,
			ChunkPos chunkPos,
			NoiseRouter noiseRouter,
			PositionalRandomFactory positionalRandomFactory,
			int i,
			int j,
			Aquifer.FluidPicker fluidPicker
		) {
			this.noiseChunk = noiseChunk;
			this.barrierNoise = noiseRouter.barrierNoise();
			this.fluidLevelFloodednessNoise = noiseRouter.fluidLevelFloodednessNoise();
			this.fluidLevelSpreadNoise = noiseRouter.fluidLevelSpreadNoise();
			this.lavaNoise = noiseRouter.lavaNoise();
			this.erosion = noiseRouter.erosion();
			this.depth = noiseRouter.depth();
			this.positionalRandomFactory = positionalRandomFactory;
			this.minGridX = this.gridX(chunkPos.getMinBlockX()) - 1;
			this.globalFluidPicker = fluidPicker;
			int k = this.gridX(chunkPos.getMaxBlockX()) + 1;
			this.gridSizeX = k - this.minGridX + 1;
			this.minGridY = this.gridY(i) - 1;
			int l = this.gridY(i + j) + 1;
			int m = l - this.minGridY + 1;
			this.minGridZ = this.gridZ(chunkPos.getMinBlockZ()) - 1;
			int n = this.gridZ(chunkPos.getMaxBlockZ()) + 1;
			this.gridSizeZ = n - this.minGridZ + 1;
			int o = this.gridSizeX * m * this.gridSizeZ;
			this.aquiferCache = new Aquifer.FluidStatus[o];
			this.aquiferLocationCache = new long[o];
			Arrays.fill(this.aquiferLocationCache, Long.MAX_VALUE);
		}

		private int getIndex(int i, int j, int k) {
			int l = i - this.minGridX;
			int m = j - this.minGridY;
			int n = k - this.minGridZ;
			return (m * this.gridSizeZ + n) * this.gridSizeX + l;
		}

		@Nullable
		@Override
		public BlockState computeSubstance(DensityFunction.FunctionContext functionContext, double d) {
			int i = functionContext.blockX();
			int j = functionContext.blockY();
			int k = functionContext.blockZ();
			if (d > 0.0) {
				this.shouldScheduleFluidUpdate = false;
				return null;
			} else {
				Aquifer.FluidStatus fluidStatus = this.globalFluidPicker.computeFluid(i, j, k);
				if (fluidStatus.at(j).is(Blocks.LAVA)) {
					this.shouldScheduleFluidUpdate = false;
					return Blocks.LAVA.defaultBlockState();
				} else {
					int l = Math.floorDiv(i - 5, 16);
					int m = Math.floorDiv(j + 1, 12);
					int n = Math.floorDiv(k - 5, 16);
					int o = Integer.MAX_VALUE;
					int p = Integer.MAX_VALUE;
					int q = Integer.MAX_VALUE;
					int r = Integer.MAX_VALUE;
					long s = 0L;
					long t = 0L;
					long u = 0L;
					long v = 0L;

					for (int w = 0; w <= 1; w++) {
						for (int x = -1; x <= 1; x++) {
							for (int y = 0; y <= 1; y++) {
								int z = l + w;
								int aa = m + x;
								int ab = n + y;
								int ac = this.getIndex(z, aa, ab);
								long ad = this.aquiferLocationCache[ac];
								long ae;
								if (ad != Long.MAX_VALUE) {
									ae = ad;
								} else {
									RandomSource randomSource = this.positionalRandomFactory.at(z, aa, ab);
									ae = BlockPos.asLong(z * 16 + randomSource.nextInt(10), aa * 12 + randomSource.nextInt(9), ab * 16 + randomSource.nextInt(10));
									this.aquiferLocationCache[ac] = ae;
								}

								int af = BlockPos.getX(ae) - i;
								int ag = BlockPos.getY(ae) - j;
								int ah = BlockPos.getZ(ae) - k;
								int ai = af * af + ag * ag + ah * ah;
								if (o >= ai) {
									v = u;
									u = t;
									t = s;
									s = ae;
									r = q;
									q = p;
									p = o;
									o = ai;
								} else if (p >= ai) {
									v = u;
									u = t;
									t = ae;
									r = q;
									q = p;
									p = ai;
								} else if (q >= ai) {
									v = u;
									u = ae;
									r = q;
									q = ai;
								} else if (r >= ai) {
									v = ae;
									r = ai;
								}
							}
						}
					}

					Aquifer.FluidStatus fluidStatus2 = this.getAquiferStatus(s);
					double e = similarity(o, p);
					BlockState blockState = fluidStatus2.at(j);
					if (e <= 0.0) {
						if (e >= FLOWING_UPDATE_SIMULARITY) {
							Aquifer.FluidStatus fluidStatus3 = this.getAquiferStatus(t);
							this.shouldScheduleFluidUpdate = !fluidStatus2.equals(fluidStatus3);
						} else {
							this.shouldScheduleFluidUpdate = false;
						}

						return blockState;
					} else if (blockState.is(Blocks.WATER) && this.globalFluidPicker.computeFluid(i, j - 1, k).at(j - 1).is(Blocks.LAVA)) {
						this.shouldScheduleFluidUpdate = true;
						return blockState;
					} else {
						MutableDouble mutableDouble = new MutableDouble(Double.NaN);
						Aquifer.FluidStatus fluidStatus4 = this.getAquiferStatus(t);
						double f = e * this.calculatePressure(functionContext, mutableDouble, fluidStatus2, fluidStatus4);
						if (d + f > 0.0) {
							this.shouldScheduleFluidUpdate = false;
							return null;
						} else {
							Aquifer.FluidStatus fluidStatus5 = this.getAquiferStatus(u);
							double g = similarity(o, q);
							if (g > 0.0) {
								double h = e * g * this.calculatePressure(functionContext, mutableDouble, fluidStatus2, fluidStatus5);
								if (d + h > 0.0) {
									this.shouldScheduleFluidUpdate = false;
									return null;
								}
							}

							double h = similarity(p, q);
							if (h > 0.0) {
								double aj = e * h * this.calculatePressure(functionContext, mutableDouble, fluidStatus4, fluidStatus5);
								if (d + aj > 0.0) {
									this.shouldScheduleFluidUpdate = false;
									return null;
								}
							}

							boolean bl = !fluidStatus2.equals(fluidStatus4);
							boolean bl2 = h >= FLOWING_UPDATE_SIMULARITY && !fluidStatus4.equals(fluidStatus5);
							boolean bl3 = g >= FLOWING_UPDATE_SIMULARITY && !fluidStatus2.equals(fluidStatus5);
							if (!bl && !bl2 && !bl3) {
								this.shouldScheduleFluidUpdate = g >= FLOWING_UPDATE_SIMULARITY
									&& similarity(o, r) >= FLOWING_UPDATE_SIMULARITY
									&& !fluidStatus2.equals(this.getAquiferStatus(v));
							} else {
								this.shouldScheduleFluidUpdate = true;
							}

							return blockState;
						}
					}
				}
			}
		}

		@Override
		public boolean shouldScheduleFluidUpdate() {
			return this.shouldScheduleFluidUpdate;
		}

		private static double similarity(int i, int j) {
			double d = 25.0;
			return 1.0 - (double)Math.abs(j - i) / 25.0;
		}

		private double calculatePressure(
			DensityFunction.FunctionContext functionContext, MutableDouble mutableDouble, Aquifer.FluidStatus fluidStatus, Aquifer.FluidStatus fluidStatus2
		) {
			int i = functionContext.blockY();
			BlockState blockState = fluidStatus.at(i);
			BlockState blockState2 = fluidStatus2.at(i);
			if ((!blockState.is(Blocks.LAVA) || !blockState2.is(Blocks.WATER)) && (!blockState.is(Blocks.WATER) || !blockState2.is(Blocks.LAVA))) {
				int j = Math.abs(fluidStatus.fluidLevel - fluidStatus2.fluidLevel);
				if (j == 0) {
					return 0.0;
				} else {
					double d = 0.5 * (double)(fluidStatus.fluidLevel + fluidStatus2.fluidLevel);
					double e = (double)i + 0.5 - d;
					double f = (double)j / 2.0;
					double g = 0.0;
					double h = 2.5;
					double k = 1.5;
					double l = 3.0;
					double m = 10.0;
					double n = 3.0;
					double o = f - Math.abs(e);
					double q;
					if (e > 0.0) {
						double p = 0.0 + o;
						if (p > 0.0) {
							q = p / 1.5;
						} else {
							q = p / 2.5;
						}
					} else {
						double p = 3.0 + o;
						if (p > 0.0) {
							q = p / 3.0;
						} else {
							q = p / 10.0;
						}
					}

					double px = 2.0;
					double r;
					if (!(q < -2.0) && !(q > 2.0)) {
						double s = mutableDouble.getValue();
						if (Double.isNaN(s)) {
							double t = this.barrierNoise.compute(functionContext);
							mutableDouble.setValue(t);
							r = t;
						} else {
							r = s;
						}
					} else {
						r = 0.0;
					}

					return 2.0 * (r + q);
				}
			} else {
				return 2.0;
			}
		}

		private int gridX(int i) {
			return Math.floorDiv(i, 16);
		}

		private int gridY(int i) {
			return Math.floorDiv(i, 12);
		}

		private int gridZ(int i) {
			return Math.floorDiv(i, 16);
		}

		private Aquifer.FluidStatus getAquiferStatus(long l) {
			int i = BlockPos.getX(l);
			int j = BlockPos.getY(l);
			int k = BlockPos.getZ(l);
			int m = this.gridX(i);
			int n = this.gridY(j);
			int o = this.gridZ(k);
			int p = this.getIndex(m, n, o);
			Aquifer.FluidStatus fluidStatus = this.aquiferCache[p];
			if (fluidStatus != null) {
				return fluidStatus;
			} else {
				Aquifer.FluidStatus fluidStatus2 = this.computeFluid(i, j, k);
				this.aquiferCache[p] = fluidStatus2;
				return fluidStatus2;
			}
		}

		private Aquifer.FluidStatus computeFluid(int i, int j, int k) {
			Aquifer.FluidStatus fluidStatus = this.globalFluidPicker.computeFluid(i, j, k);
			int l = Integer.MAX_VALUE;
			int m = j + 12;
			int n = j - 12;
			boolean bl = false;

			for (int[] is : SURFACE_SAMPLING_OFFSETS_IN_CHUNKS) {
				int o = i + SectionPos.sectionToBlockCoord(is[0]);
				int p = k + SectionPos.sectionToBlockCoord(is[1]);
				int q = this.noiseChunk.preliminarySurfaceLevel(o, p);
				int r = q + 8;
				boolean bl2 = is[0] == 0 && is[1] == 0;
				if (bl2 && n > r) {
					return fluidStatus;
				}

				boolean bl3 = m > r;
				if (bl3 || bl2) {
					Aquifer.FluidStatus fluidStatus2 = this.globalFluidPicker.computeFluid(o, r, p);
					if (!fluidStatus2.at(r).isAir()) {
						if (bl2) {
							bl = true;
						}

						if (bl3) {
							return fluidStatus2;
						}
					}
				}

				l = Math.min(l, q);
			}

			int s = this.computeSurfaceLevel(i, j, k, fluidStatus, l, bl);
			return new Aquifer.FluidStatus(s, this.computeFluidType(i, j, k, fluidStatus, s));
		}

		private int computeSurfaceLevel(int i, int j, int k, Aquifer.FluidStatus fluidStatus, int l, boolean bl) {
			DensityFunction.SinglePointContext singlePointContext = new DensityFunction.SinglePointContext(i, j, k);
			double d;
			double e;
			if (OverworldBiomeBuilder.isDeepDarkRegion(this.erosion, this.depth, singlePointContext)) {
				d = -1.0;
				e = -1.0;
			} else {
				int m = l + 8 - j;
				int n = 64;
				double f = bl ? Mth.clampedMap((double)m, 0.0, 64.0, 1.0, 0.0) : 0.0;
				double g = Mth.clamp(this.fluidLevelFloodednessNoise.compute(singlePointContext), -1.0, 1.0);
				double h = Mth.map(f, 1.0, 0.0, -0.3, 0.8);
				double o = Mth.map(f, 1.0, 0.0, -0.8, 0.4);
				d = g - o;
				e = g - h;
			}

			int m;
			if (e > 0.0) {
				m = fluidStatus.fluidLevel;
			} else if (d > 0.0) {
				m = this.computeRandomizedFluidSurfaceLevel(i, j, k, l);
			} else {
				m = DimensionType.WAY_BELOW_MIN_Y;
			}

			return m;
		}

		private int computeRandomizedFluidSurfaceLevel(int i, int j, int k, int l) {
			int m = 16;
			int n = 40;
			int o = Math.floorDiv(i, 16);
			int p = Math.floorDiv(j, 40);
			int q = Math.floorDiv(k, 16);
			int r = p * 40 + 20;
			int s = 10;
			double d = this.fluidLevelSpreadNoise.compute(new DensityFunction.SinglePointContext(o, p, q)) * 10.0;
			int t = Mth.quantize(d, 3);
			int u = r + t;
			return Math.min(l, u);
		}

		private BlockState computeFluidType(int i, int j, int k, Aquifer.FluidStatus fluidStatus, int l) {
			BlockState blockState = fluidStatus.fluidType;
			if (l <= -10 && l != DimensionType.WAY_BELOW_MIN_Y && fluidStatus.fluidType != Blocks.LAVA.defaultBlockState()) {
				int m = 64;
				int n = 40;
				int o = Math.floorDiv(i, 64);
				int p = Math.floorDiv(j, 40);
				int q = Math.floorDiv(k, 64);
				double d = this.lavaNoise.compute(new DensityFunction.SinglePointContext(o, p, q));
				if (Math.abs(d) > 0.3) {
					blockState = Blocks.LAVA.defaultBlockState();
				}
			}

			return blockState;
		}
	}
}
