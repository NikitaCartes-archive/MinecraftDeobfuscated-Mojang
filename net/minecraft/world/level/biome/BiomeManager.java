/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.google.common.hash.Hashing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.util.LinearCongruentialGenerator;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;

public class BiomeManager {
    public static final int CHUNK_CENTER_QUART = QuartPos.fromBlock(8);
    private static final int ZOOM_BITS = 2;
    private static final int ZOOM = 4;
    private static final int ZOOM_MASK = 3;
    private final NoiseBiomeSource noiseBiomeSource;
    private final long biomeZoomSeed;

    public BiomeManager(NoiseBiomeSource noiseBiomeSource, long l) {
        this.noiseBiomeSource = noiseBiomeSource;
        this.biomeZoomSeed = l;
    }

    public static long obfuscateSeed(long l) {
        return Hashing.sha256().hashLong(l).asLong();
    }

    public BiomeManager withDifferentSource(NoiseBiomeSource noiseBiomeSource) {
        return new BiomeManager(noiseBiomeSource, this.biomeZoomSeed);
    }

    public Holder<Biome> getBiome(BlockPos blockPos) {
        int p;
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
        for (p = 0; p < 8; ++p) {
            double u;
            double t;
            double h;
            boolean bl3;
            int s;
            boolean bl2;
            int r;
            boolean bl = (p & 4) == 0;
            int q = bl ? l : l + 1;
            double v = BiomeManager.getFiddledDistance(this.biomeZoomSeed, q, r = (bl2 = (p & 2) == 0) ? m : m + 1, s = (bl3 = (p & 1) == 0) ? n : n + 1, h = bl ? d : d - 1.0, t = bl2 ? e : e - 1.0, u = bl3 ? f : f - 1.0);
            if (!(g > v)) continue;
            o = p;
            g = v;
        }
        p = (o & 4) == 0 ? l : l + 1;
        int w = (o & 2) == 0 ? m : m + 1;
        int x = (o & 1) == 0 ? n : n + 1;
        return this.noiseBiomeSource.getNoiseBiome(p, w, x);
    }

    public Holder<Biome> getNoiseBiomeAtPosition(double d, double e, double f) {
        int i = QuartPos.fromBlock(Mth.floor(d));
        int j = QuartPos.fromBlock(Mth.floor(e));
        int k = QuartPos.fromBlock(Mth.floor(f));
        return this.getNoiseBiomeAtQuart(i, j, k);
    }

    public Holder<Biome> getNoiseBiomeAtPosition(BlockPos blockPos) {
        int i = QuartPos.fromBlock(blockPos.getX());
        int j = QuartPos.fromBlock(blockPos.getY());
        int k = QuartPos.fromBlock(blockPos.getZ());
        return this.getNoiseBiomeAtQuart(i, j, k);
    }

    public Holder<Biome> getNoiseBiomeAtQuart(int i, int j, int k) {
        return this.noiseBiomeSource.getNoiseBiome(i, j, k);
    }

    private static double getFiddledDistance(long l, int i, int j, int k, double d, double e, double f) {
        long m = l;
        m = LinearCongruentialGenerator.next(m, i);
        m = LinearCongruentialGenerator.next(m, j);
        m = LinearCongruentialGenerator.next(m, k);
        m = LinearCongruentialGenerator.next(m, i);
        m = LinearCongruentialGenerator.next(m, j);
        m = LinearCongruentialGenerator.next(m, k);
        double g = BiomeManager.getFiddle(m);
        m = LinearCongruentialGenerator.next(m, l);
        double h = BiomeManager.getFiddle(m);
        m = LinearCongruentialGenerator.next(m, l);
        double n = BiomeManager.getFiddle(m);
        return Mth.square(f + n) + Mth.square(e + h) + Mth.square(d + g);
    }

    private static double getFiddle(long l) {
        double d = (double)Math.floorMod(l >> 24, 1024) / 1024.0;
        return (d - 0.5) * 0.9;
    }

    public static interface NoiseBiomeSource {
        public Holder<Biome> getNoiseBiome(int var1, int var2, int var3);
    }
}

