package net.minecraft.world.level.levelgen.synth;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import net.minecraft.world.level.levelgen.RandomSource;

public class NormalNoise {
	private static final double INPUT_FACTOR = 1.0181268882175227;
	private static final double TARGET_DEVIATION = 0.3333333333333333;
	private final double valueFactor;
	private final PerlinNoise first;
	private final PerlinNoise second;

	public static NormalNoise create(RandomSource randomSource, int i, double... ds) {
		return new NormalNoise(randomSource, i, new DoubleArrayList(ds));
	}

	public static NormalNoise create(RandomSource randomSource, int i, DoubleList doubleList) {
		return new NormalNoise(randomSource, i, doubleList);
	}

	private NormalNoise(RandomSource randomSource, int i, DoubleList doubleList) {
		this.first = PerlinNoise.create(randomSource, i, doubleList);
		this.second = PerlinNoise.create(randomSource, i, doubleList);
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
}
