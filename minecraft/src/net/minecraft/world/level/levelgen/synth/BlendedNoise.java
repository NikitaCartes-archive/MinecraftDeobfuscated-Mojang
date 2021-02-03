package net.minecraft.world.level.levelgen.synth;

import java.util.stream.IntStream;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.RandomSource;

public class BlendedNoise {
	private PerlinNoise minLimitNoise;
	private PerlinNoise maxLimitNoise;
	private PerlinNoise mainNoise;

	public BlendedNoise(PerlinNoise perlinNoise, PerlinNoise perlinNoise2, PerlinNoise perlinNoise3) {
		this.minLimitNoise = perlinNoise;
		this.maxLimitNoise = perlinNoise2;
		this.mainNoise = perlinNoise3;
	}

	public BlendedNoise(RandomSource randomSource) {
		this(
			new PerlinNoise(randomSource, IntStream.rangeClosed(-15, 0)),
			new PerlinNoise(randomSource, IntStream.rangeClosed(-15, 0)),
			new PerlinNoise(randomSource, IntStream.rangeClosed(-7, 0))
		);
	}

	public double sampleAndClampNoise(int i, int j, int k, double d, double e, double f, double g) {
		double h = 0.0;
		double l = 0.0;
		double m = 0.0;
		boolean bl = true;
		double n = 1.0;

		for (int o = 0; o < 16; o++) {
			double p = PerlinNoise.wrap((double)i * d * n);
			double q = PerlinNoise.wrap((double)j * e * n);
			double r = PerlinNoise.wrap((double)k * d * n);
			double s = e * n;
			ImprovedNoise improvedNoise = this.minLimitNoise.getOctaveNoise(o);
			if (improvedNoise != null) {
				h += improvedNoise.noise(p, q, r, s, (double)j * s) / n;
			}

			ImprovedNoise improvedNoise2 = this.maxLimitNoise.getOctaveNoise(o);
			if (improvedNoise2 != null) {
				l += improvedNoise2.noise(p, q, r, s, (double)j * s) / n;
			}

			if (o < 8) {
				ImprovedNoise improvedNoise3 = this.mainNoise.getOctaveNoise(o);
				if (improvedNoise3 != null) {
					m += improvedNoise3.noise(
							PerlinNoise.wrap((double)i * f * n), PerlinNoise.wrap((double)j * g * n), PerlinNoise.wrap((double)k * f * n), g * n, (double)j * g * n
						)
						/ n;
				}
			}

			n /= 2.0;
		}

		return Mth.clampedLerp(h / 512.0, l / 512.0, (m / 10.0 + 1.0) / 2.0);
	}
}
