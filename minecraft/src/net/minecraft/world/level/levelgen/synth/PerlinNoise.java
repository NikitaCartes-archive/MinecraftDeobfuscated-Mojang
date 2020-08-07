package net.minecraft.world.level.levelgen.synth;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntBidirectionalIterator;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class PerlinNoise implements SurfaceNoise {
	private final ImprovedNoise[] noiseLevels;
	private final DoubleList amplitudes;
	private final double lowestFreqValueFactor;
	private final double lowestFreqInputFactor;

	public PerlinNoise(WorldgenRandom worldgenRandom, IntStream intStream) {
		this(worldgenRandom, (List<Integer>)intStream.boxed().collect(ImmutableList.toImmutableList()));
	}

	public PerlinNoise(WorldgenRandom worldgenRandom, List<Integer> list) {
		this(worldgenRandom, new IntRBTreeSet(list));
	}

	public static PerlinNoise create(WorldgenRandom worldgenRandom, int i, DoubleList doubleList) {
		return new PerlinNoise(worldgenRandom, Pair.of(i, doubleList));
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

	private PerlinNoise(WorldgenRandom worldgenRandom, IntSortedSet intSortedSet) {
		this(worldgenRandom, makeAmplitudes(intSortedSet));
	}

	private PerlinNoise(WorldgenRandom worldgenRandom, Pair<Integer, DoubleList> pair) {
		int i = pair.getFirst();
		this.amplitudes = pair.getSecond();
		ImprovedNoise improvedNoise = new ImprovedNoise(worldgenRandom);
		int j = this.amplitudes.size();
		int k = -i;
		this.noiseLevels = new ImprovedNoise[j];
		if (k >= 0 && k < j) {
			double d = this.amplitudes.getDouble(k);
			if (d != 0.0) {
				this.noiseLevels[k] = improvedNoise;
			}
		}

		for (int l = k - 1; l >= 0; l--) {
			if (l < j) {
				double e = this.amplitudes.getDouble(l);
				if (e != 0.0) {
					this.noiseLevels[l] = new ImprovedNoise(worldgenRandom);
				} else {
					worldgenRandom.consumeCount(262);
				}
			} else {
				worldgenRandom.consumeCount(262);
			}
		}

		if (k < j - 1) {
			long m = (long)(improvedNoise.noise(0.0, 0.0, 0.0, 0.0, 0.0) * 9.223372E18F);
			WorldgenRandom worldgenRandom2 = new WorldgenRandom(m);

			for (int n = k + 1; n < j; n++) {
				if (n >= 0) {
					double f = this.amplitudes.getDouble(n);
					if (f != 0.0) {
						this.noiseLevels[n] = new ImprovedNoise(worldgenRandom2);
					} else {
						worldgenRandom2.consumeCount(262);
					}
				} else {
					worldgenRandom2.consumeCount(262);
				}
			}
		}

		this.lowestFreqInputFactor = Math.pow(2.0, (double)(-k));
		this.lowestFreqValueFactor = Math.pow(2.0, (double)(j - 1)) / (Math.pow(2.0, (double)j) - 1.0);
	}

	public double getValue(double d, double e, double f) {
		return this.getValue(d, e, f, 0.0, 0.0, false);
	}

	public double getValue(double d, double e, double f, double g, double h, boolean bl) {
		double i = 0.0;
		double j = this.lowestFreqInputFactor;
		double k = this.lowestFreqValueFactor;

		for (int l = 0; l < this.noiseLevels.length; l++) {
			ImprovedNoise improvedNoise = this.noiseLevels[l];
			if (improvedNoise != null) {
				i += this.amplitudes.getDouble(l) * improvedNoise.noise(wrap(d * j), bl ? -improvedNoise.yo : wrap(e * j), wrap(f * j), g * j, h * j) * k;
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
}
