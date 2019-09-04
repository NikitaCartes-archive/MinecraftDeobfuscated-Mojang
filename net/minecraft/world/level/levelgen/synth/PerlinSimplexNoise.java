/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.synth;

import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.util.stream.IntStream;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraft.world.level.levelgen.synth.SurfaceNoise;

public class PerlinSimplexNoise
implements SurfaceNoise {
    private final SimplexNoise[] noiseLevels;
    private final double highestFreqValueFactor;
    private final double highestFreqInputFactor;

    public PerlinSimplexNoise(WorldgenRandom worldgenRandom, int i, int j) {
        this(worldgenRandom, new IntRBTreeSet(IntStream.rangeClosed(-i, j).toArray()));
    }

    public PerlinSimplexNoise(WorldgenRandom worldgenRandom, IntSortedSet intSortedSet) {
        int j;
        if (intSortedSet.isEmpty()) {
            throw new IllegalArgumentException("Need some octaves!");
        }
        int i = -intSortedSet.firstInt();
        int k = i + (j = intSortedSet.lastInt()) + 1;
        if (k < 1) {
            throw new IllegalArgumentException("Total number of octaves needs to be >= 1");
        }
        SimplexNoise simplexNoise = new SimplexNoise(worldgenRandom);
        int l = j;
        this.noiseLevels = new SimplexNoise[k];
        if (l >= 0 && l < k && intSortedSet.contains(0)) {
            this.noiseLevels[l] = simplexNoise;
        }
        for (int m = l + 1; m < k; ++m) {
            if (m >= 0 && intSortedSet.contains(l - m)) {
                this.noiseLevels[m] = new SimplexNoise(worldgenRandom);
                continue;
            }
            worldgenRandom.consumeCount(262);
        }
        if (j > 0) {
            long n = (long)(simplexNoise.getValue(simplexNoise.xo, simplexNoise.yo, simplexNoise.zo) * 9.223372036854776E18);
            WorldgenRandom worldgenRandom2 = new WorldgenRandom(n);
            for (int o = l - 1; o >= 0; --o) {
                if (o < k && intSortedSet.contains(l - o)) {
                    this.noiseLevels[o] = new SimplexNoise(worldgenRandom2);
                    continue;
                }
                worldgenRandom2.consumeCount(262);
            }
        }
        this.highestFreqInputFactor = Math.pow(2.0, j);
        this.highestFreqValueFactor = 1.0 / (Math.pow(2.0, k) - 1.0);
    }

    public double getValue(double d, double e, boolean bl) {
        double f = 0.0;
        double g = this.highestFreqInputFactor;
        double h = this.highestFreqValueFactor;
        for (SimplexNoise simplexNoise : this.noiseLevels) {
            if (simplexNoise != null) {
                f += simplexNoise.getValue(d * g + (bl ? simplexNoise.xo : 0.0), e * g + (bl ? simplexNoise.yo : 0.0)) * h;
            }
            g /= 2.0;
            h *= 2.0;
        }
        return f;
    }

    @Override
    public double getSurfaceNoiseValue(double d, double e, double f, double g) {
        return this.getValue(d, e, true) * 0.55;
    }
}

