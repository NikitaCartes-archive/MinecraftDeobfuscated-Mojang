package net.minecraft.world.level.levelgen.synth;

import java.util.Random;
import net.minecraft.util.Mth;

public class PerlinNoise implements SurfaceNoise {
	private final ImprovedNoise[] noiseLevels;

	public PerlinNoise(Random random, int i) {
		this.noiseLevels = new ImprovedNoise[i];

		for (int j = 0; j < i; j++) {
			this.noiseLevels[j] = new ImprovedNoise(random);
		}
	}

	public double getValue(double d, double e, double f) {
		return this.getValue(d, e, f, 0.0, 0.0, false);
	}

	public double getValue(double d, double e, double f, double g, double h, boolean bl) {
		double i = 0.0;
		double j = 1.0;

		for (ImprovedNoise improvedNoise : this.noiseLevels) {
			i += improvedNoise.noise(wrap(d * j), bl ? -improvedNoise.yo : wrap(e * j), wrap(f * j), g * j, h * j) / j;
			j /= 2.0;
		}

		return i;
	}

	public ImprovedNoise getOctaveNoise(int i) {
		return this.noiseLevels[i];
	}

	public static double wrap(double d) {
		return d - (double)Mth.lfloor(d / 3.3554432E7 + 0.5) * 3.3554432E7;
	}

	@Override
	public double getSurfaceNoiseValue(double d, double e, double f, double g) {
		return this.getValue(d, e, 0.0, f, g, false);
	}
}
