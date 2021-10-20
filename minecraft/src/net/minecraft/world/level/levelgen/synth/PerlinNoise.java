package net.minecraft.world.level.levelgen.synth;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.RandomSource;

public class PerlinNoise {
	private static final int ROUND_OFF = 33554432;
	private final ImprovedNoise[] noiseLevels;
	private final int firstOctave;
	private final DoubleList amplitudes;
	private final double lowestFreqValueFactor;
	private final double lowestFreqInputFactor;

	@Deprecated
	public static PerlinNoise createLegacyForBlendedNoise(RandomSource randomSource, IntStream intStream) {
		return new PerlinNoise(
			randomSource, makeAmplitudes(new IntRBTreeSet((Collection<? extends Integer>)intStream.boxed().collect(ImmutableList.toImmutableList()))), false
		);
	}

	@Deprecated
	public static PerlinNoise createLegacyForLegacyNormalNoise(RandomSource randomSource, int i, DoubleList doubleList) {
		return new PerlinNoise(randomSource, Pair.of(i, doubleList), false);
	}

	public static PerlinNoise create(RandomSource randomSource, IntStream intStream) {
		return create(randomSource, (List<Integer>)intStream.boxed().collect(ImmutableList.toImmutableList()));
	}

	public static PerlinNoise create(RandomSource randomSource, List<Integer> list) {
		return new PerlinNoise(randomSource, makeAmplitudes(new IntRBTreeSet(list)), true);
	}

	public static PerlinNoise create(RandomSource randomSource, int i, double d, double... ds) {
		DoubleArrayList doubleArrayList = new DoubleArrayList(ds);
		doubleArrayList.add(0, d);
		return new PerlinNoise(randomSource, Pair.of(i, doubleArrayList), true);
	}

	public static PerlinNoise create(RandomSource randomSource, int i, DoubleList doubleList) {
		return new PerlinNoise(randomSource, Pair.of(i, doubleList), true);
	}

	private static Pair<Integer, DoubleList> makeAmplitudes(IntSortedSet intSortedSet) {
		if (intSortedSet.isEmpty()) {
			throw new IllegalArgumentException("Need some octaves!");
		} else {
			int i = -intSortedSet.firstInt();
			int j = intSortedSet.lastInt();
			int k = i + j + 1;
			if (k < 1) {
				throw new IllegalArgumentException("Total number of octaves needs to be >= 1");
			} else {
				DoubleList doubleList = new DoubleArrayList(new double[k]);
				IntBidirectionalIterator intBidirectionalIterator = intSortedSet.iterator();

				while (intBidirectionalIterator.hasNext()) {
					int l = intBidirectionalIterator.nextInt();
					doubleList.set(l + i, 1.0);
				}

				return Pair.of(-i, doubleList);
			}
		}
	}

	protected PerlinNoise(RandomSource randomSource, Pair<Integer, DoubleList> pair, boolean bl) {
		this.firstOctave = pair.getFirst();
		this.amplitudes = pair.getSecond();
		int i = this.amplitudes.size();
		int j = -this.firstOctave;
		this.noiseLevels = new ImprovedNoise[i];
		if (bl) {
			PositionalRandomFactory positionalRandomFactory = randomSource.forkPositional();

			for (int k = 0; k < i; k++) {
				if (this.amplitudes.getDouble(k) != 0.0) {
					int l = this.firstOctave + k;
					this.noiseLevels[k] = new ImprovedNoise(positionalRandomFactory.fromHashOf("octave_" + l));
				}
			}
		} else {
			ImprovedNoise improvedNoise = new ImprovedNoise(randomSource);
			if (j >= 0 && j < i) {
				double d = this.amplitudes.getDouble(j);
				if (d != 0.0) {
					this.noiseLevels[j] = improvedNoise;
				}
			}

			for (int kx = j - 1; kx >= 0; kx--) {
				if (kx < i) {
					double e = this.amplitudes.getDouble(kx);
					if (e != 0.0) {
						this.noiseLevels[kx] = new ImprovedNoise(randomSource);
					} else {
						skipOctave(randomSource);
					}
				} else {
					skipOctave(randomSource);
				}
			}

			if (Arrays.stream(this.noiseLevels).filter(Objects::nonNull).count() != this.amplitudes.stream().filter(double_ -> double_ != 0.0).count()) {
				throw new IllegalStateException("Failed to create correct number of noise levels for given non-zero amplitudes");
			}

			if (j < i - 1) {
				throw new IllegalArgumentException("Positive octaves are temporarily disabled");
			}
		}

		this.lowestFreqInputFactor = Math.pow(2.0, (double)(-j));
		this.lowestFreqValueFactor = Math.pow(2.0, (double)(i - 1)) / (Math.pow(2.0, (double)i) - 1.0);
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

		for (int l = 0; l < this.noiseLevels.length; l++) {
			ImprovedNoise improvedNoise = this.noiseLevels[l];
			if (improvedNoise != null) {
				double m = improvedNoise.noise(wrap(d * j), bl ? -improvedNoise.yo : wrap(e * j), wrap(f * j), g * j, h * j);
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

	protected int firstOctave() {
		return this.firstOctave;
	}

	protected DoubleList amplitudes() {
		return this.amplitudes;
	}

	@VisibleForTesting
	public void parityConfigString(StringBuilder stringBuilder) {
		stringBuilder.append("PerlinNoise{");
		List<String> list = this.amplitudes.stream().map(double_ -> String.format("%.2f", double_)).toList();
		stringBuilder.append("first octave: ").append(this.firstOctave).append(", amplitudes: ").append(list).append(", noise levels: [");

		for (int i = 0; i < this.noiseLevels.length; i++) {
			stringBuilder.append(i).append(": ");
			ImprovedNoise improvedNoise = this.noiseLevels[i];
			if (improvedNoise == null) {
				stringBuilder.append("null");
			} else {
				improvedNoise.parityConfigString(stringBuilder);
			}

			stringBuilder.append(", ");
		}

		stringBuilder.append("]");
		stringBuilder.append("}");
	}
}
