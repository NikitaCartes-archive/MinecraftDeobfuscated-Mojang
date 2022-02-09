package net.minecraft.world.level.levelgen.synth;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.levelgen.RandomSource;

public class NormalNoise {
	private static final double INPUT_FACTOR = 1.0181268882175227;
	private static final double TARGET_DEVIATION = 0.3333333333333333;
	private final double valueFactor;
	private final PerlinNoise first;
	private final PerlinNoise second;

	@Deprecated
	public static NormalNoise createLegacyNetherBiome(RandomSource randomSource, NormalNoise.NoiseParameters noiseParameters) {
		return new NormalNoise(randomSource, noiseParameters.firstOctave(), noiseParameters.amplitudes(), false);
	}

	public static NormalNoise create(RandomSource randomSource, int i, double... ds) {
		return new NormalNoise(randomSource, i, new DoubleArrayList(ds), true);
	}

	public static NormalNoise create(RandomSource randomSource, NormalNoise.NoiseParameters noiseParameters) {
		return new NormalNoise(randomSource, noiseParameters.firstOctave(), noiseParameters.amplitudes(), true);
	}

	public static NormalNoise create(RandomSource randomSource, int i, DoubleList doubleList) {
		return new NormalNoise(randomSource, i, doubleList, true);
	}

	private NormalNoise(RandomSource randomSource, int i, DoubleList doubleList, boolean bl) {
		if (bl) {
			this.first = PerlinNoise.create(randomSource, i, doubleList);
			this.second = PerlinNoise.create(randomSource, i, doubleList);
		} else {
			this.first = PerlinNoise.createLegacyForLegacyNormalNoise(randomSource, i, doubleList);
			this.second = PerlinNoise.createLegacyForLegacyNormalNoise(randomSource, i, doubleList);
		}

		int j = Integer.MAX_VALUE;
		int k = Integer.MIN_VALUE;
		DoubleListIterator doubleListIterator = doubleList.iterator();

		while (doubleListIterator.hasNext()) {
			int l = doubleListIterator.nextIndex();
			double d = doubleListIterator.nextDouble();
			if (d != 0.0) {
				j = Math.min(j, l);
				k = Math.max(k, l);
			}
		}

		this.valueFactor = 0.16666666666666666 / expectedDeviation(k - j);
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

	public NormalNoise.NoiseParameters parameters() {
		return new NormalNoise.NoiseParameters(this.first.firstOctave(), this.first.amplitudes());
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

	public static class NoiseParameters {
		private final int firstOctave;
		private final DoubleList amplitudes;
		public static final Codec<NormalNoise.NoiseParameters> DIRECT_CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.INT.fieldOf("firstOctave").forGetter(NormalNoise.NoiseParameters::firstOctave),
						Codec.DOUBLE.listOf().fieldOf("amplitudes").forGetter(NormalNoise.NoiseParameters::amplitudes)
					)
					.apply(instance, NormalNoise.NoiseParameters::new)
		);
		public static final Codec<Holder<NormalNoise.NoiseParameters>> CODEC = RegistryFileCodec.create(Registry.NOISE_REGISTRY, DIRECT_CODEC);

		public NoiseParameters(int i, List<Double> list) {
			this.firstOctave = i;
			this.amplitudes = new DoubleArrayList(list);
		}

		public NoiseParameters(int i, double d, double... ds) {
			this.firstOctave = i;
			this.amplitudes = new DoubleArrayList(ds);
			this.amplitudes.add(0, d);
		}

		public int firstOctave() {
			return this.firstOctave;
		}

		public DoubleList amplitudes() {
			return this.amplitudes;
		}
	}
}
