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
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public interface Aquifer {
	static Aquifer create(
		NoiseChunk noiseChunk,
		ChunkPos chunkPos,
		NormalNoise normalNoise,
		NormalNoise normalNoise2,
		NormalNoise normalNoise3,
		PositionalRandomFactory positionalRandomFactory,
		NoiseSampler noiseSampler,
		int i,
		int j,
		Aquifer.FluidPicker fluidPicker
	) {
		return new Aquifer.NoiseBasedAquifer(noiseChunk, chunkPos, normalNoise, normalNoise2, normalNoise3, positionalRandomFactory, noiseSampler, i, j, fluidPicker);
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
		private final NoiseChunk noiseChunk;
		private final NormalNoise barrierNoise;
		private final NormalNoise waterLevelNoise;
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
			{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {3, 0}, {-3, 0}, {0, 3}, {0, -3}, {2, 2}, {2, -2}, {-2, 2}, {-2, 2}
		};

		NoiseBasedAquifer(
			NoiseChunk noiseChunk,
			ChunkPos chunkPos,
			NormalNoise normalNoise,
			NormalNoise normalNoise2,
			NormalNoise normalNoise3,
			PositionalRandomFactory positionalRandomFactory,
			NoiseSampler noiseSampler,
			int i,
			int j,
			Aquifer.FluidPicker fluidPicker
		) {
			this.noiseChunk = noiseChunk;
			this.barrierNoise = normalNoise;
			this.waterLevelNoise = normalNoise2;
			this.lavaNoise = normalNoise3;
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
						double g = this.similarity(o, p);
						double h = this.similarity(o, q);
						double ah = this.similarity(p, q);
						bl = g > 0.0;
						if (fluidStatus2.at(j).is(Blocks.WATER) && this.globalFluidPicker.computeFluid(i, j - 1, k).at(j - 1).is(Blocks.LAVA)) {
							f = 1.0;
						} else if (g > -1.0) {
							double ai = 1.0 + (this.barrierNoise.getValue((double)i, (double)j, (double)k) + 0.05) / 4.0;
							double aj = this.calculatePressure(j, ai, fluidStatus2, fluidStatus3);
							double ak = this.calculatePressure(j, ai, fluidStatus2, fluidStatus4);
							double al = this.calculatePressure(j, ai, fluidStatus3, fluidStatus4);
							double am = Math.max(0.0, g);
							double an = Math.max(0.0, h);
							double ao = Math.max(0.0, ah);
							double ap = 2.0 * am * Math.max(aj, Math.max(ak * an, al * ao));
							float aq = 0.5F;
							if (j <= fluidStatus2.fluidLevel
								&& j <= fluidStatus3.fluidLevel
								&& fluidStatus2.fluidLevel != fluidStatus3.fluidLevel
								&& Math.abs(this.barrierNoise.getValue((double)((float)i * 0.5F), (double)((float)j * 0.5F), (double)((float)k * 0.5F))) < 0.3) {
								f = 1.0;
							} else {
								f = Math.max(0.0, ap);
							}
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

		private double similarity(int i, int j) {
			double d = 25.0;
			return 1.0 - (double)Math.abs(j - i) / 25.0;
		}

		private double calculatePressure(int i, double d, Aquifer.FluidStatus fluidStatus, Aquifer.FluidStatus fluidStatus2) {
			BlockState blockState = fluidStatus.at(i);
			BlockState blockState2 = fluidStatus2.at(i);
			if ((!blockState.is(Blocks.LAVA) || !blockState2.is(Blocks.WATER)) && (!blockState.is(Blocks.WATER) || !blockState2.is(Blocks.LAVA))) {
				int j = Math.abs(fluidStatus.fluidLevel - fluidStatus2.fluidLevel);
				double e = 0.5 * (double)(fluidStatus.fluidLevel + fluidStatus2.fluidLevel);
				double f = Math.abs(e - (double)i - 0.5);
				return 0.5 * (double)j * d - f;
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
			int m = j + 6;
			boolean bl = false;

			for (int[] is : SURFACE_SAMPLING_OFFSETS_IN_CHUNKS) {
				int n = i + SectionPos.sectionToBlockCoord(is[0]);
				int o = k + SectionPos.sectionToBlockCoord(is[1]);
				int p = this.sampler.getPreliminarySurfaceLevel(n, o, this.noiseChunk.terrainInfoWide(this.sampler, QuartPos.fromBlock(n), QuartPos.fromBlock(o)));
				int q = p + 8;
				boolean bl2 = is[0] == 0 && is[1] == 0;
				boolean bl3 = m > q;
				if (bl3 || bl2) {
					Aquifer.FluidStatus fluidStatus2 = this.globalFluidPicker.computeFluid(n, q, o);
					if (!fluidStatus2.at(q).isAir()) {
						if (bl2) {
							bl = true;
						}

						if (bl3) {
							return fluidStatus2;
						}
					}
				}

				l = Math.min(l, p);
			}

			int r = j - 6;
			if (r > l) {
				return fluidStatus;
			} else {
				int s = 40;
				int t = Math.floorDiv(i, 64);
				int u = Math.floorDiv(j, 40);
				int n = Math.floorDiv(k, 64);
				int o = -20;
				int p = 50;
				double d = this.waterLevelNoise.getValue((double)t, (double)u / 1.4, (double)n) * 50.0 + -20.0;
				int v = u * 40 + 20;
				if (bl && v >= l - 30 && v < fluidStatus.fluidLevel) {
					if (d > -12.0) {
						return fluidStatus;
					}

					if (d > -20.0) {
						return new Aquifer.FluidStatus(l - 12 + (int)d, Blocks.WATER.defaultBlockState());
					}

					d = -40.0;
				} else {
					if (d > 4.0) {
						d *= 4.0;
					}

					if (d < -10.0) {
						d = -40.0;
					}
				}

				int w = v + Mth.floor(d);
				int x = Math.min(l, w);
				boolean bl4 = false;
				if (v == -20 && !bl) {
					double e = this.lavaNoise.getValue((double)t, (double)u / 1.4, (double)n);
					bl4 = Math.abs(e) > 0.22F;
				}

				return new Aquifer.FluidStatus(x, bl4 ? Blocks.LAVA.defaultBlockState() : fluidStatus.fluidType);
			}
		}
	}
}
