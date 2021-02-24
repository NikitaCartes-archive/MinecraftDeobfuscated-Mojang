package net.minecraft.world.level.levelgen;

import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class Aquifer {
	private final NormalNoise barrierNoise;
	private final NormalNoise waterLevelNoise;
	private final NoiseGeneratorSettings noiseGeneratorSettings;
	private final int[] aquiferCache;
	private final long[] aquiferLocationCache;
	private double lastBarrierDensity;
	private int lastWaterLevel;
	private boolean shouldScheduleWaterUpdate;
	private final NoiseSampler sampler;
	private final int minGridX;
	private final int minGridY;
	private final int minGridZ;
	private final int gridSizeX;
	private final int gridSizeZ;

	public Aquifer(
		int i, int j, NormalNoise normalNoise, NormalNoise normalNoise2, NoiseGeneratorSettings noiseGeneratorSettings, NoiseSampler noiseSampler, int k
	) {
		this.barrierNoise = normalNoise;
		this.waterLevelNoise = normalNoise2;
		this.noiseGeneratorSettings = noiseGeneratorSettings;
		this.sampler = noiseSampler;
		ChunkPos chunkPos = new ChunkPos(i, j);
		this.minGridX = this.gridX(chunkPos.getMinBlockX()) - 1;
		int l = this.gridX(chunkPos.getMaxBlockX()) + 1;
		this.gridSizeX = l - this.minGridX + 1;
		int m = noiseGeneratorSettings.noiseSettings().minY();
		this.minGridY = this.gridY(m) - 1;
		int n = this.gridY(m + k) + 1;
		int o = n - this.minGridY + 1;
		this.minGridZ = this.gridZ(chunkPos.getMinBlockZ()) - 1;
		int p = this.gridZ(chunkPos.getMaxBlockZ()) + 1;
		this.gridSizeZ = p - this.minGridZ + 1;
		int q = this.gridSizeX * o * this.gridSizeZ;
		this.aquiferCache = new int[q];
		Arrays.fill(this.aquiferCache, Integer.MAX_VALUE);
		this.aquiferLocationCache = new long[q];
		Arrays.fill(this.aquiferLocationCache, Long.MAX_VALUE);
	}

	private int getIndex(int i, int j, int k) {
		int l = i - this.minGridX;
		int m = j - this.minGridY;
		int n = k - this.minGridZ;
		return (m * this.gridSizeZ + n) * this.gridSizeX + l;
	}

	protected void computeAt(int i, int j, int k) {
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

		int u = this.getWaterLevel(r);
		int v = this.getWaterLevel(s);
		int w = this.getWaterLevel(t);
		double d = this.similarity(o, p);
		double e = this.similarity(o, q);
		double f = this.similarity(p, q);
		this.lastWaterLevel = u;
		this.shouldScheduleWaterUpdate = d > 0.0;
		if (d > -1.0) {
			double g = 1.0 + (this.barrierNoise.getValue((double)i, (double)j, (double)k) + 0.1) / 4.0;
			double h = this.calculatePressure(j, g, u, v);
			double ah = this.calculatePressure(j, g, u, w);
			double ai = this.calculatePressure(j, g, v, w);
			double aj = Math.max(0.0, d);
			double ak = Math.max(0.0, e);
			double al = Math.max(0.0, f);
			double am = 2.0 * aj * Math.max(h, Math.max(ah * ak, ai * al));
			this.lastBarrierDensity = Math.max(0.0, am);
		} else {
			this.lastBarrierDensity = 0.0;
		}
	}

	private double similarity(int i, int j) {
		double d = 25.0;
		return 1.0 - (double)Math.abs(j - i) / 25.0;
	}

	private double calculatePressure(int i, double d, int j, int k) {
		return 0.5 * (double)Math.abs(j - k) * d - Math.abs(0.5 * (double)(j + k) - (double)i - 0.5);
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

	private int getWaterLevel(long l) {
		int i = BlockPos.getX(l);
		int j = BlockPos.getY(l);
		int k = BlockPos.getZ(l);
		int m = this.gridX(i);
		int n = this.gridY(j);
		int o = this.gridZ(k);
		int p = this.getIndex(m, n, o);
		int q = this.aquiferCache[p];
		if (q != Integer.MAX_VALUE) {
			return q;
		} else {
			int r = this.computeAquifer(i, j, k);
			this.aquiferCache[p] = r;
			return r;
		}
	}

	private int computeAquifer(int i, int j, int k) {
		int l = this.noiseGeneratorSettings.seaLevel();
		if (j > 30) {
			return l;
		} else {
			int m = 64;
			int n = -10;
			int o = 40;
			double d = this.waterLevelNoise.getValue((double)Math.floorDiv(i, 64), (double)Math.floorDiv(j, 40) / 1.4, (double)Math.floorDiv(k, 64)) * 30.0 + -10.0;
			if (Math.abs(d) > 8.0) {
				d *= 4.0;
			}

			int p = Math.floorDiv(j, 40) * 40 + 20;
			int q = p + Mth.floor(d);
			return Math.min(56, q);
		}
	}

	public int getLastWaterLevel() {
		return this.lastWaterLevel;
	}

	public double getLastBarrierDensity() {
		return this.lastBarrierDensity;
	}

	public boolean shouldScheduleWaterUpdate() {
		return this.shouldScheduleWaterUpdate;
	}
}
