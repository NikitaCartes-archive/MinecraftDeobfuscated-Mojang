package net.minecraft.world.level.levelgen.synth;

import java.util.stream.IntStream;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.RandomSource;

public class BlendedNoise {
	private final PerlinNoise minLimitNoise;
	private final PerlinNoise maxLimitNoise;
	private final PerlinNoise mainNoise;

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

		for (int o = 0; o < 8; o++) {
			ImprovedNoise improvedNoise = this.mainNoise.getOctaveNoise(o);
			if (improvedNoise != null) {
				m += improvedNoise.noise(
						PerlinNoise.wrap((double)i * f * n), PerlinNoise.wrap((double)j * g * n), PerlinNoise.wrap((double)k * f * n), g * n, (double)j * g * n
					)
					/ n;
			}

			n /= 2.0;
		}

		double p = (m / 10.0 + 1.0) / 2.0;
		boolean bl2 = p >= 1.0;
		boolean bl3 = p <= 0.0;
		n = 1.0;

		for (int q = 0; q < 16; q++) {
			double r = PerlinNoise.wrap((double)i * d * n);
			double s = PerlinNoise.wrap((double)j * e * n);
			double t = PerlinNoise.wrap((double)k * d * n);
			double u = e * n;
			if (!bl2) {
				ImprovedNoise improvedNoise2 = this.minLimitNoise.getOctaveNoise(q);
				if (improvedNoise2 != null) {
					h += improvedNoise2.noise(r, s, t, u, (double)j * u) / n;
				}
			}

			if (!bl3) {
				ImprovedNoise improvedNoise2 = this.maxLimitNoise.getOctaveNoise(q);
				if (improvedNoise2 != null) {
					l += improvedNoise2.noise(r, s, t, u, (double)j * u) / n;
				}
			}

			n /= 2.0;
		}

		return Mth.clampedLerp(h / 512.0, l / 512.0, p);
	}
}
