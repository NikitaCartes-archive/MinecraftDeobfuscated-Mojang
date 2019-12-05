package net.minecraft.world.level.biome;

import net.minecraft.util.LinearCongruentialGenerator;

public enum FuzzyOffsetBiomeZoomer implements BiomeZoomer {
	INSTANCE;

	@Override
	public Biome getBiome(long l, int i, int j, int k, BiomeManager.NoiseBiomeSource noiseBiomeSource) {
		int m = i - 2;
		int n = j - 2;
		int o = k - 2;
		int p = m >> 2;
		int q = n >> 2;
		int r = o >> 2;
		double d = (double)(m & 3) / 4.0;
		double e = (double)(n & 3) / 4.0;
		double f = (double)(o & 3) / 4.0;
		double[] ds = new double[8];

		for (int s = 0; s < 8; s++) {
			boolean bl = (s & 4) == 0;
			boolean bl2 = (s & 2) == 0;
			boolean bl3 = (s & 1) == 0;
			int t = bl ? p : p + 1;
			int u = bl2 ? q : q + 1;
			int v = bl3 ? r : r + 1;
			double g = bl ? d : d - 1.0;
			double h = bl2 ? e : e - 1.0;
			double w = bl3 ? f : f - 1.0;
			ds[s] = getFiddledDistance(l, t, u, v, g, h, w);
		}

		int s = 0;
		double x = ds[0];

		for (int y = 1; y < 8; y++) {
			if (x > ds[y]) {
				s = y;
				x = ds[y];
			}
		}

		int yx = (s & 4) == 0 ? p : p + 1;
		int t = (s & 2) == 0 ? q : q + 1;
		int u = (s & 1) == 0 ? r : r + 1;
		return noiseBiomeSource.getNoiseBiome(yx, t, u);
	}

	private static double getFiddledDistance(long l, int i, int j, int k, double d, double e, double f) {
		long m = LinearCongruentialGenerator.next(l, (long)i);
		m = LinearCongruentialGenerator.next(m, (long)j);
		m = LinearCongruentialGenerator.next(m, (long)k);
		m = LinearCongruentialGenerator.next(m, (long)i);
		m = LinearCongruentialGenerator.next(m, (long)j);
		m = LinearCongruentialGenerator.next(m, (long)k);
		double g = getFiddle(m);
		m = LinearCongruentialGenerator.next(m, l);
		double h = getFiddle(m);
		m = LinearCongruentialGenerator.next(m, l);
		double n = getFiddle(m);
		return sqr(f + n) + sqr(e + h) + sqr(d + g);
	}

	private static double getFiddle(long l) {
		double d = (double)((int)Math.floorMod(l >> 24, 1024L)) / 1024.0;
		return (d - 0.5) * 0.9;
	}

	private static double sqr(double d) {
		return d * d;
	}
}
