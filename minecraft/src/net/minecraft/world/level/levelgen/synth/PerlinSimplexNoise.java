package net.minecraft.world.level.levelgen.synth;

import java.util.Random;

public class PerlinSimplexNoise implements SurfaceNoise {
	private final SimplexNoise[] noiseLevels;
	private final int levels;

	public PerlinSimplexNoise(Random random, int i) {
		this.levels = i;
		this.noiseLevels = new SimplexNoise[i];

		for (int j = 0; j < i; j++) {
			this.noiseLevels[j] = new SimplexNoise(random);
		}
	}

	public double getValue(double d, double e) {
		return this.getValue(d, e, false);
	}

	public double getValue(double d, double e, boolean bl) {
		double f = 0.0;
		double g = 1.0;

		for (int i = 0; i < this.levels; i++) {
			f += this.noiseLevels[i].getValue(d * g + (bl ? this.noiseLevels[i].xo : 0.0), e * g + (bl ? this.noiseLevels[i].yo : 0.0)) / g;
			g /= 2.0;
		}

		return f;
	}

	@Override
	public double getSurfaceNoiseValue(double d, double e, double f, double g) {
		return this.getValue(d, e, true) * 0.55;
	}
}
