/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.synth;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.levelgen.synth.SurfaceNoise;
import org.jetbrains.annotations.Nullable;

public class PerlinNoise
implements SurfaceNoise {
    private final ImprovedNoise[] noiseLevels;
    private final double highestFreqValueFactor;
    private final double highestFreqInputFactor;

    public PerlinNoise(WorldgenRandom worldgenRandom, IntStream intStream) {
        this(worldgenRandom, intStream.boxed().collect(ImmutableList.toImmutableList()));
    }

    public PerlinNoise(WorldgenRandom worldgenRandom, List<Integer> list) {
        this(worldgenRandom, new IntRBTreeSet(list));
    }

    private PerlinNoise(WorldgenRandom worldgenRandom, IntSortedSet intSortedSet) {
        int j;
        if (intSortedSet.isEmpty()) {
            throw new IllegalArgumentException("Need some octaves!");
        }
        int i = -intSortedSet.firstInt();
        int k = i + (j = intSortedSet.lastInt()) + 1;
        if (k < 1) {
            throw new IllegalArgumentException("Total number of octaves needs to be >= 1");
        }
        ImprovedNoise improvedNoise = new ImprovedNoise(worldgenRandom);
        int l = j;
        this.noiseLevels = new ImprovedNoise[k];
        if (l >= 0 && l < k && intSortedSet.contains(0)) {
            this.noiseLevels[l] = improvedNoise;
        }
        for (int m = l + 1; m < k; ++m) {
            if (m >= 0 && intSortedSet.contains(l - m)) {
                this.noiseLevels[m] = new ImprovedNoise(worldgenRandom);
                continue;
            }
            worldgenRandom.consumeCount(262);
        }
        if (j > 0) {
            long n = (long)(improvedNoise.noise(0.0, 0.0, 0.0, 0.0, 0.0) * 9.223372036854776E18);
            WorldgenRandom worldgenRandom2 = new WorldgenRandom(n);
            for (int o = l - 1; o >= 0; --o) {
                if (o < k && intSortedSet.contains(l - o)) {
                    this.noiseLevels[o] = new ImprovedNoise(worldgenRandom2);
                    continue;
                }
                worldgenRandom2.consumeCount(262);
            }
        }
        this.highestFreqInputFactor = Math.pow(2.0, j);
        this.highestFreqValueFactor = 1.0 / (Math.pow(2.0, k) - 1.0);
    }

    public double getValue(double d, double e, double f) {
        return this.getValue(d, e, f, 0.0, 0.0, false);
    }

    public double getValue(double d, double e, double f, double g, double h, boolean bl) {
        double i = 0.0;
        double j = this.highestFreqInputFactor;
        double k = this.highestFreqValueFactor;
        for (ImprovedNoise improvedNoise : this.noiseLevels) {
            if (improvedNoise != null) {
                i += improvedNoise.noise(PerlinNoise.wrap(d * j), bl ? -improvedNoise.yo : PerlinNoise.wrap(e * j), PerlinNoise.wrap(f * j), g * j, h * j) * k;
            }
            j /= 2.0;
            k *= 2.0;
        }
        return i;
    }

    @Nullable
    public ImprovedNoise getOctaveNoise(int i) {
        return this.noiseLevels[i];
    }

    public static double wrap(double d) {
        return d - (double)Mth.lfloor(d / 3.3554432E7 + 0.5) * 3.3554432E7;
    }

    @Override
    public double getSurfaceNoiseValue(double d, double e, double f, double g) {
        return this.getValue(d, e, 0.0, f, g, false);
    }
}

