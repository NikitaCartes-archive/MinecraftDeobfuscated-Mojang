package net.minecraft.world.level.biome;

import com.google.common.hash.Hashing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.util.LinearCongruentialGenerator;
import net.minecraft.util.Mth;

public class BiomeManager {
	public static final int CHUNK_CENTER_QUART = QuartPos.fromBlock(8);
	private static final int ZOOM_BITS = 2;
	private static final int ZOOM = 4;
	private static final int ZOOM_MASK = 3;
	private final BiomeManager.NoiseBiomeSource noiseBiomeSource;
	private final long biomeZoomSeed;

	public BiomeManager(BiomeManager.NoiseBiomeSource noiseBiomeSource, long l) {
		this.noiseBiomeSource = noiseBiomeSource;
		this.biomeZoomSeed = l;
	}

	public static long obfuscateSeed(long l) {
		return Hashing.sha256().hashLong(l).asLong();
	}

	public BiomeManager withDifferentSource(BiomeManager.NoiseBiomeSource noiseBiomeSource) {
		return new BiomeManager(noiseBiomeSource, this.biomeZoomSeed);
	}

	public Biome getBiome(BlockPos blockPos) {
		int i = blockPos.getX() - 2;
		int j = blockPos.getY() - 2;
		int k = blockPos.getZ() - 2;
		int l = i >> 2;
		int m = j >> 2;
		int n = k >> 2;
		double d = (double)(i & 3) / 4.0;
		double e = (double)(j & 3) / 4.0;
		double f = (double)(k & 3) / 4.0;
		int o = 0;
		double g = Double.POSITIVE_INFINITY;

		for (int p = 0; p < 8; p++) {
			boolean bl = (p & 4) == 0;
			boolean bl2 = (p & 2) == 0;
			boolean bl3 = (p & 1) == 0;
			int q = bl ? l : l + 1;
			int r = bl2 ? m : m + 1;
			int s = bl3 ? n : n + 1;
			double h = bl ? d : d - 1.0;
			double t = bl2 ? e : e - 1.0;
			double u = bl3 ? f : f - 1.0;
			double v = getFiddledDistance(this.biomeZoomSeed, q, r, s, h, t, u);
			if (g > v) {
				o = p;
				g = v;
			}
		}

		int px = (o & 4) == 0 ? l : l + 1;
		int w = (o & 2) == 0 ? m : m + 1;
		int x = (o & 1) == 0 ? n : n + 1;
		return this.noiseBiomeSource.getNoiseBiome(px, w, x);
	}

	public Biome getNoiseBiomeAtPosition(double d, double e, double f) {
		int i = QuartPos.fromBlock(Mth.floor(d));
		int j = QuartPos.fromBlock(Mth.floor(e));
		int k = QuartPos.fromBlock(Mth.floor(f));
		return this.getNoiseBiomeAtQuart(i, j, k);
	}

	public Biome getNoiseBiomeAtPosition(BlockPos blockPos) {
		int i = QuartPos.fromBlock(blockPos.getX());
		int j = QuartPos.fromBlock(blockPos.getY());
		int k = QuartPos.fromBlock(blockPos.getZ());
		return this.getNoiseBiomeAtQuart(i, j, k);
	}

	public Biome getNoiseBiomeAtQuart(int i, int j, int k) {
		return this.noiseBiomeSource.getNoiseBiome(i, j, k);
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
		return Mth.square(f + n) + Mth.square(e + h) + Mth.square(d + g);
	}

	private static double getFiddle(long l) {
		double d = (double)Math.floorMod(l >> 24, 1024) / 1024.0;
		return (d - 0.5) * 0.9;
	}

	public interface NoiseBiomeSource {
		Biome getNoiseBiome(int i, int j, int k);
	}
}
