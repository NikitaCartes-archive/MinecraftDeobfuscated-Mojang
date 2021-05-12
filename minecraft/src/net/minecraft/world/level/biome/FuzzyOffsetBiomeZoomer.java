package net.minecraft.world.level.biome;

import net.minecraft.util.LinearCongruentialGenerator;

public enum FuzzyOffsetBiomeZoomer implements BiomeZoomer {
	INSTANCE;

	private static final int ZOOM_BITS = 2;
	private static final int ZOOM = 4;
	private static final int ZOOM_MASK = 3;

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
		int s = 0;
		double g = Double.POSITIVE_INFINITY;

		for (int t = 0; t < 8; t++) {
			boolean bl = (t & 4) == 0;
			boolean bl2 = (t & 2) == 0;
			boolean bl3 = (t & 1) == 0;
			int u = bl ? p : p + 1;
			int v = bl2 ? q : q + 1;
			int w = bl3 ? r : r + 1;
			double h = bl ? d : d - 1.0;
			double x = bl2 ? e : e - 1.0;
			double y = bl3 ? f : f - 1.0;
			double z = getFiddledDistance(l, u, v, w, h, x, y);
			if (g > z) {
				s = t;
				g = z;
			}
		}

		int tx = (s & 4) == 0 ? p : p + 1;
		int aa = (s & 2) == 0 ? q : q + 1;
		int ab = (s & 1) == 0 ? r : r + 1;
		return noiseBiomeSource.getNoiseBiome(tx, aa, ab);
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
		double d = (double)Math.floorMod(l >> 24, 1024) / 1024.0;
		return (d - 0.5) * 0.9;
	}

	private static double sqr(double d) {
		return d * d;
	}
}
