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


    @Override
    public Biome getBiome(long l, int i, int j, int k, BiomeManager.NoiseBiomeSource noiseBiomeSource) {
        int y;
        int u;
        int t;
        int s;
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
        for (s = 0; s < 8; ++s) {
            boolean bl = (s & 4) == 0;
            boolean bl2 = (s & 2) == 0;
            boolean bl3 = (s & 1) == 0;
            t = bl ? p : p + 1;
            u = bl2 ? q : q + 1;
            int v = bl3 ? r : r + 1;
            double g = bl ? d : 1.0 - d;
            double h = bl2 ? e : 1.0 - e;
            double w = bl3 ? f : 1.0 - f;
            ds[s] = FuzzyOffsetBiomeZoomer.getFiddledDistance(l, t, u, v, g, h, w);
        }
        s = 0;
        double x = ds[0];
        for (y = 1; y < 8; ++y) {
            if (!(x > ds[y])) continue;
            s = y;
            x = ds[y];
        }
        y = (s & 4) == 0 ? p : p + 1;
        t = (s & 2) == 0 ? q : q + 1;
        u = (s & 1) == 0 ? r : r + 1;
        return noiseBiomeSource.getNoiseBiome(y, t, u);
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
        double d = (double)((int)Math.floorMod(l >> 24, 1024L)) / 1024.0;
        return (d - 0.5) * 0.9;
    }

    private static double sqr(double d) {
        return d * d;
    }
}

