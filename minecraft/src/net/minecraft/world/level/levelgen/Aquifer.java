package net.minecraft.world.level.levelgen;

import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.apache.commons.lang3.mutable.MutableDouble;

public interface Aquifer {
	static Aquifer create(
		NoiseChunk noiseChunk,
		ChunkPos chunkPos,
		NormalNoise normalNoise,
		NormalNoise normalNoise2,
		NormalNoise normalNoise3,
		NormalNoise normalNoise4,
		PositionalRandomFactory positionalRandomFactory,
		NoiseSampler noiseSampler,
		int i,
		int j,
		Aquifer.FluidPicker fluidPicker
	) {
		return new Aquifer.NoiseBasedAquifer(
			noiseChunk, chunkPos, normalNoise, normalNoise2, normalNoise3, normalNoise4, positionalRandomFactory, noiseSampler, i, j, fluidPicker
		);
	}

	static Aquifer createDisabled(Aquifer.FluidPicker fluidPicker) {
		return new Aquifer() {
			@Nullable
			@Override
			public BlockState computeSubstance(int i, int j, int k, double d, double e) {
				return e > 0.0 ? null : fluidPicker.computeFluid(i, j, k).at(j);
			}

			@Override
			public boolean shouldScheduleFluidUpdate() {
				return false;
			}
		};
	}

	@Nullable
	BlockState computeSubstance(int i, int j, int k, double d, double e);

	boolean shouldScheduleFluidUpdate();

	public interface FluidPicker {
		Aquifer.FluidStatus computeFluid(int i, int j, int k);
	}

	public static final class FluidStatus {
		final int fluidLevel;
		final BlockState fluidType;

		public FluidStatus(int i, BlockState blockState) {
			this.fluidLevel = i;
			this.fluidType = blockState;
		}

		public BlockState at(int i) {
			return i < this.fluidLevel ? this.fluidType : Blocks.AIR.defaultBlockState();
		}
	}

	public static class NoiseBasedAquifer implements Aquifer, Aquifer.FluidPicker {
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
		private final NormalNoise barrierNoise;
		private final NormalNoise fluidLevelFloodednessNoise;
		private final NormalNoise fluidLevelSpreadNoise;
		private final NormalNoise lavaNoise;
		private final PositionalRandomFactory positionalRandomFactory;
		private final Aquifer.FluidStatus[] aquiferCache;
		private final long[] aquiferLocationCache;
		private final Aquifer.FluidPicker globalFluidPicker;
		private boolean shouldScheduleFluidUpdate;
		private final NoiseSampler sampler;
		private final int minGridX;
		private final int minGridY;
		private final int minGridZ;
		private final int gridSizeX;
		private final int gridSizeZ;
		private static final int[][] SURFACE_SAMPLING_OFFSETS_IN_CHUNKS = new int[][]{
			{-2, -1}, {-1, -1}, {0, -1}, {1, -1}, {-3, 0}, {-2, 0}, {-1, 0}, {0, 0}, {1, 0}, {-2, 1}, {-1, 1}, {0, 1}, {1, 1}
		};

		NoiseBasedAquifer(
			NoiseChunk noiseChunk,
			ChunkPos chunkPos,
			NormalNoise normalNoise,
			NormalNoise normalNoise2,
			NormalNoise normalNoise3,
			NormalNoise normalNoise4,
			PositionalRandomFactory positionalRandomFactory,
			NoiseSampler noiseSampler,
			int i,
			int j,
			Aquifer.FluidPicker fluidPicker
		) {
			this.noiseChunk = noiseChunk;
			this.barrierNoise = normalNoise;
			this.fluidLevelFloodednessNoise = normalNoise2;
			this.fluidLevelSpreadNoise = normalNoise3;
			this.lavaNoise = normalNoise4;
			this.positionalRandomFactory = positionalRandomFactory;
			this.sampler = noiseSampler;
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
		public BlockState computeSubstance(int i, int j, int k, double d, double e) {
			if (d <= -64.0) {
				return this.globalFluidPicker.computeFluid(i, j, k).at(j);
			} else {
				if (e <= 0.0) {
					Aquifer.FluidStatus fluidStatus = this.globalFluidPicker.computeFluid(i, j, k);
					double f;
					BlockState blockState;
					boolean bl;
					if (fluidStatus.at(j).is(Blocks.LAVA)) {
						blockState = Blocks.LAVA.defaultBlockState();
						f = 0.0;
						bl = false;
					} else {
						int l = Math.floorDiv(i - 5, 16);
						int m = Math.floorDiv(j + 1, 12);
						int n = Math.floorDiv(k - 5, 16);
						int o = Integer.MAX_VALUE;
						int p = Integer.MAX_VALUE;
						int q = Integer.MAX_VALUE;
						long r = 0L;
						long s = 0L;
						long t = 0L;

						for (int u = 0; u <= 1; u++) {
							for (int v = -1; v <= 1; v++) {
								for (int w = 0; w <= 1; w++) {
									int x = l + u;
									int y = m + v;
									int z = n + w;
									int aa = this.getIndex(x, y, z);
									long ab = this.aquiferLocationCache[aa];
									long ac;
									if (ab != Long.MAX_VALUE) {
										ac = ab;
									} else {
										RandomSource randomSource = this.positionalRandomFactory.at(x, y, z);
										ac = BlockPos.asLong(x * 16 + randomSource.nextInt(10), y * 12 + randomSource.nextInt(9), z * 16 + randomSource.nextInt(10));
										this.aquiferLocationCache[aa] = ac;
									}

									int ad = BlockPos.getX(ac) - i;
									int ae = BlockPos.getY(ac) - j;
									int af = BlockPos.getZ(ac) - k;
									int ag = ad * ad + ae * ae + af * af;
									if (o >= ag) {
										t = s;
										s = r;
										r = ac;
										q = p;
										p = o;
										o = ag;
									} else if (p >= ag) {
										t = s;
										s = ac;
										q = p;
										p = ag;
									} else if (q >= ag) {
										t = ac;
										q = ag;
									}
								}
							}
						}

						Aquifer.FluidStatus fluidStatus2 = this.getAquiferStatus(r);
						Aquifer.FluidStatus fluidStatus3 = this.getAquiferStatus(s);
						Aquifer.FluidStatus fluidStatus4 = this.getAquiferStatus(t);
						double g = similarity(o, p);
						double h = similarity(o, q);
						double ah = similarity(p, q);
						bl = g >= FLOWING_UPDATE_SIMULARITY;
						if (fluidStatus2.at(j).is(Blocks.WATER) && this.globalFluidPicker.computeFluid(i, j - 1, k).at(j - 1).is(Blocks.LAVA)) {
							f = 1.0;
						} else if (g > -1.0) {
							MutableDouble mutableDouble = new MutableDouble(Double.NaN);
							double ai = this.calculatePressure(i, j, k, mutableDouble, fluidStatus2, fluidStatus3);
							double aj = this.calculatePressure(i, j, k, mutableDouble, fluidStatus2, fluidStatus4);
							double ak = this.calculatePressure(i, j, k, mutableDouble, fluidStatus3, fluidStatus4);
							double al = Math.max(0.0, g);
							double am = Math.max(0.0, h);
							double an = Math.max(0.0, ah);
							double ao = 2.0 * al * Math.max(ai, Math.max(aj * am, ak * an));
							f = Math.max(0.0, ao);
						} else {
							f = 0.0;
						}

						blockState = fluidStatus2.at(j);
					}

					if (e + f <= 0.0) {
						this.shouldScheduleFluidUpdate = bl;
						return blockState;
					}
				}

				this.shouldScheduleFluidUpdate = false;
				return null;
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

		private double calculatePressure(int i, int j, int k, MutableDouble mutableDouble, Aquifer.FluidStatus fluidStatus, Aquifer.FluidStatus fluidStatus2) {
			BlockState blockState = fluidStatus.at(j);
			BlockState blockState2 = fluidStatus2.at(j);
			if ((!blockState.is(Blocks.LAVA) || !blockState2.is(Blocks.WATER)) && (!blockState.is(Blocks.WATER) || !blockState2.is(Blocks.LAVA))) {
				int l = Math.abs(fluidStatus.fluidLevel - fluidStatus2.fluidLevel);
				if (l == 0) {
					return 0.0;
				} else {
					double d = 0.5 * (double)(fluidStatus.fluidLevel + fluidStatus2.fluidLevel);
					double e = (double)j + 0.5 - d;
					double f = (double)l / 2.0;
					double g = 0.0;
					double h = 2.5;
					double m = 1.5;
					double n = 3.0;
					double o = 10.0;
					double p = 3.0;
					double q = f - Math.abs(e);
					double s;
					if (e > 0.0) {
						double r = 0.0 + q;
						if (r > 0.0) {
							s = r / 1.5;
						} else {
							s = r / 2.5;
						}
					} else {
						double r = 3.0 + q;
						if (r > 0.0) {
							s = r / 3.0;
						} else {
							s = r / 10.0;
						}
					}

					if (!(s < -2.0) && !(s > 2.0)) {
						double rx = mutableDouble.getValue();
						if (Double.isNaN(rx)) {
							double t = 0.5;
							double u = this.barrierNoise.getValue((double)i, (double)j * 0.5, (double)k);
							mutableDouble.setValue(u);
							return u + s;
						} else {
							return rx + s;
						}
					} else {
						return s;
					}
				}
			} else {
				return 1.0;
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

		@Override
		public Aquifer.FluidStatus computeFluid(int i, int j, int k) {
			Aquifer.FluidStatus fluidStatus = this.globalFluidPicker.computeFluid(i, j, k);
			int l = Integer.MAX_VALUE;
			int m = j + 12;
			int n = j - 12;
			boolean bl = false;

			for (int[] is : SURFACE_SAMPLING_OFFSETS_IN_CHUNKS) {
				int o = i + SectionPos.sectionToBlockCoord(is[0]);
				int p = k + SectionPos.sectionToBlockCoord(is[1]);
				int q = this.sampler.getPreliminarySurfaceLevel(o, p, this.noiseChunk.terrainInfoWide(this.sampler, QuartPos.fromBlock(o), QuartPos.fromBlock(p)));
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

			int s = l + 8 - j;
			int t = 64;
			double d = bl ? Mth.clampedMap((double)s, 0.0, 64.0, 1.0, 0.0) : 0.0;
			double e = 0.67;
			double f = Mth.clamp(this.fluidLevelFloodednessNoise.getValue((double)i, (double)j * 0.67, (double)k), -1.0, 1.0);
			double g = Mth.map(d, 1.0, 0.0, -0.3, 0.8);
			if (f > g) {
				return fluidStatus;
			} else {
				double h = Mth.map(d, 1.0, 0.0, -0.8, 0.4);
				if (f <= h) {
					return new Aquifer.FluidStatus(DimensionType.WAY_BELOW_MIN_Y, fluidStatus.fluidType);
				} else {
					int u = 16;
					int v = 40;
					int w = Math.floorDiv(i, 16);
					int x = Math.floorDiv(j, 40);
					int y = Math.floorDiv(k, 16);
					int z = x * 40 + 20;
					int aa = 10;
					double ab = this.fluidLevelSpreadNoise.getValue((double)w, (double)x / 1.4, (double)y) * 10.0;
					int ac = Mth.quantize(ab, 3);
					int ad = z + ac;
					int ae = Math.min(l, ad);
					BlockState blockState = this.getFluidType(i, j, k, fluidStatus, ad);
					return new Aquifer.FluidStatus(ae, blockState);
				}
			}
		}

		private BlockState getFluidType(int i, int j, int k, Aquifer.FluidStatus fluidStatus, int l) {
			if (l <= -10) {
				int m = 64;
				int n = 40;
				int o = Math.floorDiv(i, 64);
				int p = Math.floorDiv(j, 40);
				int q = Math.floorDiv(k, 64);
				double d = this.lavaNoise.getValue((double)o, (double)p, (double)q);
				if (Math.abs(d) > 0.3) {
					return Blocks.LAVA.defaultBlockState();
				}
			}

			return fluidStatus.fluidType;
		}
	}
}
