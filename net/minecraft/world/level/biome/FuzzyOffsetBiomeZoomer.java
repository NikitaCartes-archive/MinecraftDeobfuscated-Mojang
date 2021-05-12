/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import net.minecraft.util.LinearCongruentialGenerator;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeZoomer;

public enum FuzzyOffsetBiomeZoomer implements BiomeZoomer
{
    INSTANCE;

    private static final int ZOOM_BITS = 2;
    private static final int ZOOM = 4;
    private static final int ZOOM_MASK = 3;

    @Override
    public Biome getBiome(long l, int i, int j, int k, BiomeManager.NoiseBiomeSource noiseBiomeSource) {
        int t;
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
        for (t = 0; t < 8; ++t) {
            double y;
            double x;
            double h;
            boolean bl3;
            int w;
            boolean bl2;
            int v;
            boolean bl = (t & 4) == 0;
            int u = bl ? p : p + 1;
            double z = FuzzyOffsetBiomeZoomer.getFiddledDistance(l, u, v = (bl2 = (t & 2) == 0) ? q : q + 1, w = (bl3 = (t & 1) == 0) ? r : r + 1, h = bl ? d : d - 1.0, x = bl2 ? e : e - 1.0, y = bl3 ? f : f - 1.0);
            if (!(g > z)) continue;
            s = t;
            g = z;
        }
        t = (s & 4) == 0 ? p : p + 1;
        int aa = (s & 2) == 0 ? q : q + 1;
        int ab = (s & 1) == 0 ? r : r + 1;
        return noiseBiomeSource.getNoiseBiome(t, aa, ab);
    }

    private static double getFiddledDistance(long l, int i, int j, int k, double d, double e, double f) {
        long m = l;
        m = LinearCongruentialGenerator.next(m, i);
        m = LinearCongruentialGenerator.next(m, j);
        m = LinearCongruentialGenerator.next(m, k);
        m = LinearCongruentialGenerator.next(m, i);
        m = LinearCongruentialGenerator.next(m, j);
        m = LinearCongruentialGenerator.next(m, k);
        double g = FuzzyOffsetBiomeZoomer.getFiddle(m);
        m = LinearCongruentialGenerator.next(m, l);
        double h = FuzzyOffsetBiomeZoomer.getFiddle(m);
        m = LinearCongruentialGenerator.next(m, l);
        double n = FuzzyOffsetBiomeZoomer.getFiddle(m);
        return FuzzyOffsetBiomeZoomer.sqr(f + n) + FuzzyOffsetBiomeZoomer.sqr(e + h) + FuzzyOffsetBiomeZoomer.sqr(d + g);
    }

    private static double getFiddle(long l) {
        double d = (double)Math.floorMod(l >> 24, 1024) / 1024.0;
        return (d - 0.5) * 0.9;
    }

    private static double sqr(double d) {
        return d * d;
    }
}

