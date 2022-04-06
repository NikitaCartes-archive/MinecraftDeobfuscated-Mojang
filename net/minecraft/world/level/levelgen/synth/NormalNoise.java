/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.synth;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;

public class NormalNoise {
    private static final double INPUT_FACTOR = 1.0181268882175227;
    private static final double TARGET_DEVIATION = 0.3333333333333333;
    private final double valueFactor;
    private final PerlinNoise first;
    private final PerlinNoise second;
    private final double maxValue;
    private final NoiseParameters parameters;

    @Deprecated
    public static NormalNoise createLegacyNetherBiome(RandomSource randomSource, NoiseParameters noiseParameters) {
        return new NormalNoise(randomSource, noiseParameters, false);
    }

    public static NormalNoise create(RandomSource randomSource, int i, double ... ds) {
        return NormalNoise.create(randomSource, new NoiseParameters(i, new DoubleArrayList(ds)));
    }

    public static NormalNoise create(RandomSource randomSource, NoiseParameters noiseParameters) {
        return new NormalNoise(randomSource, noiseParameters, true);
    }

    private NormalNoise(RandomSource randomSource, NoiseParameters noiseParameters, boolean bl) {
        int i = noiseParameters.firstOctave;
        DoubleList doubleList = noiseParameters.amplitudes;
        this.parameters = noiseParameters;
        if (bl) {
            this.first = PerlinNoise.create(randomSource, i, doubleList);
            this.second = PerlinNoise.create(randomSource, i, doubleList);
        } else {
            this.first = PerlinNoise.createLegacyForLegacyNetherBiome(randomSource, i, doubleList);
            this.second = PerlinNoise.createLegacyForLegacyNetherBiome(randomSource, i, doubleList);
        }
        int j = Integer.MAX_VALUE;
        int k = Integer.MIN_VALUE;
        DoubleListIterator doubleListIterator = doubleList.iterator();
        while (doubleListIterator.hasNext()) {
            int l = doubleListIterator.nextIndex();
            double d = doubleListIterator.nextDouble();
            if (d == 0.0) continue;
            j = Math.min(j, l);
            k = Math.max(k, l);
        }
        this.valueFactor = 0.16666666666666666 / NormalNoise.expectedDeviation(k - j);
        this.maxValue = (this.first.maxValue() + this.second.maxValue()) * this.valueFactor;
    }

    public double maxValue() {
        return this.maxValue;
    }

    private static double expectedDeviation(int i) {
        return 0.1 * (1.0 + 1.0 / (double)(i + 1));
    }

    public double getValue(double d, double e, double f) {
        double g = d * 1.0181268882175227;
        double h = e * 1.0181268882175227;
        double i = f * 1.0181268882175227;
        return (this.first.getValue(d, e, f) + this.second.getValue(g, h, i)) * this.valueFactor;
    }

    public NoiseParameters parameters() {
        return this.parameters;
    }

    @VisibleForTesting
    public void parityConfigString(StringBuilder stringBuilder) {
        stringBuilder.append("NormalNoise {");
        stringBuilder.append("first: ");
        this.first.parityConfigString(stringBuilder);
        stringBuilder.append(", second: ");
        this.second.parityConfigString(stringBuilder);
        stringBuilder.append("}");
    }

    public record NoiseParameters(int firstOctave, DoubleList amplitudes) {
        public static final Codec<NoiseParameters> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.INT.fieldOf("firstOctave")).forGetter(NoiseParameters::firstOctave), ((MapCodec)Codec.DOUBLE.listOf().fieldOf("amplitudes")).forGetter(NoiseParameters::amplitudes)).apply((Applicative<NoiseParameters, ?>)instance, NoiseParameters::new));
        public static final Codec<Holder<NoiseParameters>> CODEC = RegistryFileCodec.create(Registry.NOISE_REGISTRY, DIRECT_CODEC);

        public NoiseParameters(int i, List<Double> list) {
            this(i, new DoubleArrayList(list));
        }

        public NoiseParameters(int i, double d, double ... ds) {
            this(i, Util.make(new DoubleArrayList(ds), doubleArrayList -> doubleArrayList.add(0, d)));
        }
    }
}

