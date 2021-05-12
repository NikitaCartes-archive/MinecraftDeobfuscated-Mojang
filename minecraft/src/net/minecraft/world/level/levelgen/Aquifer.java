package net.minecraft.world.level.levelgen;

import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public interface Aquifer {
	int ALWAYS_LAVA_AT_OR_BELOW_Y_INDEX = 9;
	int ALWAYS_USE_SEA_LEVEL_WHEN_ABOVE = 30;

	static Aquifer create(
		ChunkPos chunkPos,
		NormalNoise normalNoise,
		NormalNoise normalNoise2,
		NormalNoise normalNoise3,
		NoiseGeneratorSettings noiseGeneratorSettings,
		NoiseSampler noiseSampler,
		int i,
		int j
	) {
		return new Aquifer.NoiseBasedAquifer(chunkPos, normalNoise, normalNoise2, normalNoise3, noiseGeneratorSettings, noiseSampler, i, j);
	}

	static Aquifer createDisabled(int i, BlockState blockState) {
		return new Aquifer() {
			@Override
			public BlockState computeState(BaseStoneSource baseStoneSource, int i, int j, int k, double d) {
				if (d > 0.0) {
					return baseStoneSource.getBaseBlock(i, j, k);
				} else {
					return j >= i ? Blocks.AIR.defaultBlockState() : blockState;
				}
			}

			@Override
			public boolean shouldScheduleFluidUpdate() {
				return false;
			}
		};
	}

	BlockState computeState(BaseStoneSource baseStoneSource, int i, int j, int k, double d);

	boolean shouldScheduleFluidUpdate();

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
		private final NormalNoise barrierNoise;
		private final NormalNoise waterLevelNoise;
		private final NormalNoise lavaNoise;
		private final NoiseGeneratorSettings noiseGeneratorSettings;
		private final Aquifer.NoiseBasedAquifer.AquiferStatus[] aquiferCache;
		private final long[] aquiferLocationCache;
		private boolean shouldScheduleFluidUpdate;
		private final NoiseSampler sampler;
		private final int minGridX;
		private final int minGridY;
		private final int minGridZ;
		private final int gridSizeX;
		private final int gridSizeZ;

		NoiseBasedAquifer(
			ChunkPos chunkPos,
			NormalNoise normalNoise,
			NormalNoise normalNoise2,
			NormalNoise normalNoise3,
			NoiseGeneratorSettings noiseGeneratorSettings,
			NoiseSampler noiseSampler,
			int i,
			int j
		) {
			this.barrierNoise = normalNoise;
			this.waterLevelNoise = normalNoise2;
			this.lavaNoise = normalNoise3;
			this.noiseGeneratorSettings = noiseGeneratorSettings;
			this.sampler = noiseSampler;
			this.minGridX = this.gridX(chunkPos.getMinBlockX()) - 1;
			int k = this.gridX(chunkPos.getMaxBlockX()) + 1;
			this.gridSizeX = k - this.minGridX + 1;
			this.minGridY = this.gridY(i) - 1;
			int l = this.gridY(i + j) + 1;
			int m = l - this.minGridY + 1;
			this.minGridZ = this.gridZ(chunkPos.getMinBlockZ()) - 1;
			int n = this.gridZ(chunkPos.getMaxBlockZ()) + 1;
			this.gridSizeZ = n - this.minGridZ + 1;
			int o = this.gridSizeX * m * this.gridSizeZ;
			this.aquiferCache = new Aquifer.NoiseBasedAquifer.AquiferStatus[o];
			this.aquiferLocationCache = new long[o];
			Arrays.fill(this.aquiferLocationCache, Long.MAX_VALUE);
		}

		private int getIndex(int i, int j, int k) {
			int l = i - this.minGridX;
			int m = j - this.minGridY;
			int n = k - this.minGridZ;
			return (m * this.gridSizeZ + n) * this.gridSizeX + l;
		}

		@Override
		public BlockState computeState(BaseStoneSource baseStoneSource, int i, int j, int k, double d) {
			if (d <= 0.0) {
				double e;
				BlockState blockState;
				boolean bl;
				if (this.isLavaLevel(j)) {
					blockState = Blocks.LAVA.defaultBlockState();
					e = 0.0;
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
									WorldgenRandom worldgenRandom = new WorldgenRandom(Mth.getSeed(x, y * 3, z) + 1L);
									ac = BlockPos.asLong(x * 16 + worldgenRandom.nextInt(10), y * 12 + worldgenRandom.nextInt(9), z * 16 + worldgenRandom.nextInt(10));
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

					Aquifer.NoiseBasedAquifer.AquiferStatus aquiferStatus = this.getAquiferStatus(r);
					Aquifer.NoiseBasedAquifer.AquiferStatus aquiferStatus2 = this.getAquiferStatus(s);
					Aquifer.NoiseBasedAquifer.AquiferStatus aquiferStatus3 = this.getAquiferStatus(t);
					double f = this.similarity(o, p);
					double g = this.similarity(o, q);
					double h = this.similarity(p, q);
					bl = f > 0.0;
					if (aquiferStatus.fluidLevel >= j && aquiferStatus.fluidType.is(Blocks.WATER) && this.isLavaLevel(j - 1)) {
						e = 1.0;
					} else if (f > -1.0) {
						double ah = 1.0 + (this.barrierNoise.getValue((double)i, (double)j, (double)k) + 0.05) / 4.0;
						double ai = this.calculatePressure(j, ah, aquiferStatus, aquiferStatus2);
						double aj = this.calculatePressure(j, ah, aquiferStatus, aquiferStatus3);
						double ak = this.calculatePressure(j, ah, aquiferStatus2, aquiferStatus3);
						double al = Math.max(0.0, f);
						double am = Math.max(0.0, g);
						double an = Math.max(0.0, h);
						double ao = 2.0 * al * Math.max(ai, Math.max(aj * am, ak * an));
						e = Math.max(0.0, ao);
					} else {
						e = 0.0;
					}

					blockState = j >= aquiferStatus.fluidLevel ? Blocks.AIR.defaultBlockState() : aquiferStatus.fluidType;
				}

				if (d + e <= 0.0) {
					this.shouldScheduleFluidUpdate = bl;
					return blockState;
				}
			}

			this.shouldScheduleFluidUpdate = false;
			return baseStoneSource.getBaseBlock(i, j, k);
		}

		@Override
		public boolean shouldScheduleFluidUpdate() {
			return this.shouldScheduleFluidUpdate;
		}

		private boolean isLavaLevel(int i) {
			return i - this.noiseGeneratorSettings.noiseSettings().minY() <= 9;
		}

		private double similarity(int i, int j) {
			double d = 25.0;
			return 1.0 - (double)Math.abs(j - i) / 25.0;
		}

		private double calculatePressure(
			int i, double d, Aquifer.NoiseBasedAquifer.AquiferStatus aquiferStatus, Aquifer.NoiseBasedAquifer.AquiferStatus aquiferStatus2
		) {
			if (i <= aquiferStatus.fluidLevel && i <= aquiferStatus2.fluidLevel && aquiferStatus.fluidType != aquiferStatus2.fluidType) {
				return 1.0;
			} else {
				int j = Math.abs(aquiferStatus.fluidLevel - aquiferStatus2.fluidLevel);
				double e = 0.5 * (double)(aquiferStatus.fluidLevel + aquiferStatus2.fluidLevel);
				double f = Math.abs(e - (double)i - 0.5);
				return 0.5 * (double)j * d - f;
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

		private Aquifer.NoiseBasedAquifer.AquiferStatus getAquiferStatus(long l) {
			int i = BlockPos.getX(l);
			int j = BlockPos.getY(l);
			int k = BlockPos.getZ(l);
			int m = this.gridX(i);
			int n = this.gridY(j);
			int o = this.gridZ(k);
			int p = this.getIndex(m, n, o);
			Aquifer.NoiseBasedAquifer.AquiferStatus aquiferStatus = this.aquiferCache[p];
			if (aquiferStatus != null) {
				return aquiferStatus;
			} else {
				Aquifer.NoiseBasedAquifer.AquiferStatus aquiferStatus2 = this.computeAquifer(i, j, k);
				this.aquiferCache[p] = aquiferStatus2;
				return aquiferStatus2;
			}
		}

		private Aquifer.NoiseBasedAquifer.AquiferStatus computeAquifer(int i, int j, int k) {
			int l = this.noiseGeneratorSettings.seaLevel();
			if (j > 30) {
				return new Aquifer.NoiseBasedAquifer.AquiferStatus(l, Blocks.WATER.defaultBlockState());
			} else {
				int m = 64;
				int n = -10;
				int o = 40;
				double d = this.waterLevelNoise.getValue((double)Math.floorDiv(i, 64), (double)Math.floorDiv(j, 40) / 1.4, (double)Math.floorDiv(k, 64)) * 30.0 + -10.0;
				boolean bl = false;
				if (Math.abs(d) > 8.0) {
					d *= 4.0;
				}

				int p = Math.floorDiv(j, 40) * 40 + 20;
				int q = p + Mth.floor(d);
				if (p == -20) {
					double e = this.lavaNoise.getValue((double)Math.floorDiv(i, 64), (double)Math.floorDiv(j, 40) / 1.4, (double)Math.floorDiv(k, 64));
					bl = Math.abs(e) > 0.22F;
				}

				return new Aquifer.NoiseBasedAquifer.AquiferStatus(Math.min(56, q), bl ? Blocks.LAVA.defaultBlockState() : Blocks.WATER.defaultBlockState());
			}
		}

		static final class AquiferStatus {
			final int fluidLevel;
			final BlockState fluidType;

			public AquiferStatus(int i, BlockState blockState) {
				this.fluidLevel = i;
				this.fluidType = blockState;
			}
		}
	}
}
