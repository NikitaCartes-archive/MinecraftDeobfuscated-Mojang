package net.minecraft.world.level.levelgen.synth;

import net.minecraft.util.Mth;

public class NoiseUtils {
	public static double sampleNoiseAndMapToRange(NormalNoise normalNoise, double d, double e, double f, double g, double h) {
		double i = normalNoise.getValue(d, e, f);
		return Mth.map(i, -1.0, 1.0, g, h);
	}

	public static double biasTowardsExtreme(double d, double e) {
		return d + Math.sin(Math.PI * d) * e / Math.PI;
	}
}
