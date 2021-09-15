package net.minecraft.world.level.levelgen.synth;

import java.util.stream.IntStream;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseSamplingSettings;
import net.minecraft.world.level.levelgen.RandomSource;

public class BlendedNoise implements NoiseChunk.NoiseFiller {
	private final PerlinNoise minLimitNoise;
	private final PerlinNoise maxLimitNoise;
	private final PerlinNoise mainNoise;
	private final double xzScale;
	private final double yScale;
	private final double xzMainScale;
	private final double yMainScale;
	private final int cellWidth;
	private final int cellHeight;

	public BlendedNoise(PerlinNoise perlinNoise, PerlinNoise perlinNoise2, PerlinNoise perlinNoise3, NoiseSamplingSettings noiseSamplingSettings, int i, int j) {
		this.minLimitNoise = perlinNoise;
		this.maxLimitNoise = perlinNoise2;
		this.mainNoise = perlinNoise3;
		this.xzScale = 684.412 * noiseSamplingSettings.xzScale();
		this.yScale = 684.412 * noiseSamplingSettings.yScale();
		this.xzMainScale = this.xzScale / noiseSamplingSettings.xzFactor();
		this.yMainScale = this.yScale / noiseSamplingSettings.yFactor();
		this.cellWidth = i;
		this.cellHeight = j;
	}

	public BlendedNoise(RandomSource randomSource, NoiseSamplingSettings noiseSamplingSettings, int i, int j) {
		this(
			new PerlinNoise(randomSource, IntStream.rangeClosed(-15, 0)),
			new PerlinNoise(randomSource, IntStream.rangeClosed(-15, 0)),
			new PerlinNoise(randomSource, IntStream.rangeClosed(-7, 0)),
			noiseSamplingSettings,
			i,
			j
		);
	}

	@Override
	public double calculateNoise(int i, int j, int k) {
		int l = Math.floorDiv(i, this.cellWidth);
		int m = Math.floorDiv(j, this.cellHeight);
		int n = Math.floorDiv(k, this.cellWidth);
		double d = 0.0;
		double e = 0.0;
		double f = 0.0;
		boolean bl = true;
		double g = 1.0;

		for (int o = 0; o < 8; o++) {
			ImprovedNoise improvedNoise = this.mainNoise.getOctaveNoise(o);
			if (improvedNoise != null) {
				f += improvedNoise.noise(
						PerlinNoise.wrap((double)l * this.xzMainScale * g),
						PerlinNoise.wrap((double)m * this.yMainScale * g),
						PerlinNoise.wrap((double)n * this.xzMainScale * g),
						this.yMainScale * g,
						(double)m * this.yMainScale * g
					)
					/ g;
			}

			g /= 2.0;
		}

		double h = (f / 10.0 + 1.0) / 2.0;
		boolean bl2 = h >= 1.0;
		boolean bl3 = h <= 0.0;
		g = 1.0;

		for (int p = 0; p < 16; p++) {
			double q = PerlinNoise.wrap((double)l * this.xzScale * g);
			double r = PerlinNoise.wrap((double)m * this.yScale * g);
			double s = PerlinNoise.wrap((double)n * this.xzScale * g);
			double t = this.yScale * g;
			if (!bl2) {
				ImprovedNoise improvedNoise2 = this.minLimitNoise.getOctaveNoise(p);
				if (improvedNoise2 != null) {
					d += improvedNoise2.noise(q, r, s, t, (double)m * t) / g;
				}
			}

			if (!bl3) {
				ImprovedNoise improvedNoise2 = this.maxLimitNoise.getOctaveNoise(p);
				if (improvedNoise2 != null) {
					e += improvedNoise2.noise(q, r, s, t, (double)m * t) / g;
				}
			}

			g /= 2.0;
		}

		return Mth.clampedLerp(d / 512.0, e / 512.0, h) / 128.0;
	}
}
