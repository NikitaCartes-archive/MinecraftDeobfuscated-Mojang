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

	public static void parityNoiseOctaveConfigString(StringBuilder stringBuilder, double d, double e, double f, byte[] bs) {
		stringBuilder.append(String.format("xo=%.3f, yo=%.3f, zo=%.3f, p0=%d, p255=%d", (float)d, (float)e, (float)f, bs[0], bs[255]));
	}

	public static void parityNoiseOctaveConfigString(StringBuilder stringBuilder, double d, double e, double f, int[] is) {
		stringBuilder.append(String.format("xo=%.3f, yo=%.3f, zo=%.3f, p0=%d, p255=%d", (float)d, (float)e, (float)f, is[0], is[255]));
	}
}
