/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.synth;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.util.List;
import java.util.function.LongFunction;
import java.util.stream.IntStream;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.world.level.levelgen.synth.SurfaceNoise;
import org.jetbrains.annotations.Nullable;

public class PerlinNoise
implements SurfaceNoise {
    private static final int ROUND_OFF = 0x2000000;
    private final ImprovedNoise[] noiseLevels;
    private final int firstOctave;
    private final DoubleList amplitudes;
    private final double lowestFreqValueFactor;
    private final double lowestFreqInputFactor;

    public PerlinNoise(RandomSource randomSource, IntStream intStream) {
        this(randomSource, intStream.boxed().collect(ImmutableList.toImmutableList()));
    }

    public PerlinNoise(RandomSource randomSource, List<Integer> list) {
        this(randomSource, new IntRBTreeSet(list));
    }

    public static PerlinNoise create(RandomSource randomSource, int i, double ... ds) {
        return PerlinNoise.create(randomSource, i, new DoubleArrayList(ds));
    }

    public static PerlinNoise create(RandomSource randomSource, int i, DoubleList doubleList) {
        return new PerlinNoise(randomSource, Pair.of(i, doubleList));
    }

    private static Pair<Integer, DoubleList> makeAmplitudes(IntSortedSet intSortedSet) {
        int j;
        if (intSortedSet.isEmpty()) {
            throw new IllegalArgumentException("Need some octaves!");
        }
        int i = -intSortedSet.firstInt();
        int k = i + (j = intSortedSet.lastInt()) + 1;
        if (k < 1) {
            throw new IllegalArgumentException("Total number of octaves needs to be >= 1");
        }
        DoubleArrayList doubleList = new DoubleArrayList(new double[k]);
        IntBidirectionalIterator intBidirectionalIterator = intSortedSet.iterator();
        while (intBidirectionalIterator.hasNext()) {
            int l = intBidirectionalIterator.nextInt();
            doubleList.set(l + i, 1.0);
        }
        return Pair.of(-i, doubleList);
    }

    private PerlinNoise(RandomSource randomSource, IntSortedSet intSortedSet) {
        this(randomSource, intSortedSet, (long l) -> new WorldgenRandom(new LegacyRandomSource(l)));
    }

    private PerlinNoise(RandomSource randomSource, IntSortedSet intSortedSet, LongFunction<RandomSource> longFunction) {
        this(randomSource, PerlinNoise.makeAmplitudes(intSortedSet), longFunction);
    }

    protected PerlinNoise(RandomSource randomSource, Pair<Integer, DoubleList> pair) {
        this(randomSource, pair, (long l) -> new WorldgenRandom(new LegacyRandomSource(l)));
    }

    protected PerlinNoise(RandomSource randomSource, Pair<Integer, DoubleList> pair, LongFunction<RandomSource> longFunction) {
        double d;
        this.firstOctave = pair.getFirst();
        this.amplitudes = pair.getSecond();
        ImprovedNoise improvedNoise = new ImprovedNoise(randomSource);
        int i = this.amplitudes.size();
        int j = -this.firstOctave;
        this.noiseLevels = new ImprovedNoise[i];
        if (j >= 0 && j < i && (d = this.amplitudes.getDouble(j)) != 0.0) {
            this.noiseLevels[j] = improvedNoise;
        }
        for (int k = j - 1; k >= 0; --k) {
            if (k < i) {
                double e = this.amplitudes.getDouble(k);
                if (e != 0.0) {
                    this.noiseLevels[k] = new ImprovedNoise(randomSource);
                    continue;
                }
                PerlinNoise.skipOctave(randomSource);
                continue;
            }
            PerlinNoise.skipOctave(randomSource);
        }
        if (j < i - 1) {
            throw new IllegalArgumentException("Positive octaves are temporarily disabled");
        }
        this.lowestFreqInputFactor = Math.pow(2.0, -j);
        this.lowestFreqValueFactor = Math.pow(2.0, i - 1) / (Math.pow(2.0, i) - 1.0);
    }

    private static void skipOctave(RandomSource randomSource) {
        randomSource.consumeCount(262);
    }

    public double getValue(double d, double e, double f) {
        return this.getValue(d, e, f, 0.0, 0.0, false);
    }

    @Deprecated
    public double getValue(double d, double e, double f, double g, double h, boolean bl) {
        double i = 0.0;
        double j = this.lowestFreqInputFactor;
        double k = this.lowestFreqValueFactor;
        for (int l = 0; l < this.noiseLevels.length; ++l) {
            ImprovedNoise improvedNoise = this.noiseLevels[l];
            if (improvedNoise != null) {
                double m = improvedNoise.noise(PerlinNoise.wrap(d * j), bl ? -improvedNoise.yo : PerlinNoise.wrap(e * j), PerlinNoise.wrap(f * j), g * j, h * j);
                i += this.amplitudes.getDouble(l) * m * k;
            }
            j *= 2.0;
            k /= 2.0;
        }
        return i;
    }

    @Nullable
    public ImprovedNoise getOctaveNoise(int i) {
        return this.noiseLevels[this.noiseLevels.length - 1 - i];
    }

    public static double wrap(double d) {
        return d - (double)Mth.lfloor(d / 3.3554432E7 + 0.5) * 3.3554432E7;
    }

    @Override
    public double getSurfaceNoiseValue(double d, double e, double f, double g) {
        return this.getValue(d, e, 0.0, f, g, false);
    }

    protected int firstOctave() {
        return this.firstOctave;
    }

    protected DoubleList amplitudes() {
        return this.amplitudes;
    }
}

