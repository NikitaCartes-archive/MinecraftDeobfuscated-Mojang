package net.minecraft.world.level.levelgen.synth;

import java.util.List;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class NormalNoise {
	private final double valueFactor;
	private final PerlinNoise first;
	private final PerlinNoise second;

	public NormalNoise(WorldgenRandom worldgenRandom, List<Integer> list) {
		this.first = new PerlinNoise(worldgenRandom, list);
		this.second = new PerlinNoise(worldgenRandom, list);
		int i = (Integer)list.stream().min(Integer::compareTo).orElse(0);
		int j = (Integer)list.stream().max(Integer::compareTo).orElse(0);
		this.valueFactor = 0.16666666666666666 / expectedDeviation(j - i);
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
