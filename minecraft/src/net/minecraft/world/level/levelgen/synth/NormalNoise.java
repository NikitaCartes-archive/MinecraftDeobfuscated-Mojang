package net.minecraft.world.level.levelgen.synth;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class NormalNoise {
	private final double valueFactor;
	private final PerlinNoise first;
	private final PerlinNoise second;

	public static NormalNoise create(WorldgenRandom worldgenRandom, int i, DoubleList doubleList) {
		return new NormalNoise(worldgenRandom, i, doubleList);
	}

	private NormalNoise(WorldgenRandom worldgenRandom, int i, DoubleList doubleList) {
		this.first = PerlinNoise.create(worldgenRandom, i, doubleList);
		this.second = PerlinNoise.create(worldgenRandom, i, doubleList);
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
