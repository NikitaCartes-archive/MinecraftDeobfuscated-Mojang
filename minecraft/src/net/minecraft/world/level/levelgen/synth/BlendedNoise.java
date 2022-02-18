package net.minecraft.world.level.levelgen.synth;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import java.util.stream.IntStream;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseSamplingSettings;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

public class BlendedNoise implements DensityFunction.SimpleFunction {
	public static final BlendedNoise UNSEEDED = new BlendedNoise(new XoroshiroRandomSource(0L), new NoiseSamplingSettings(1.0, 1.0, 80.0, 160.0), 4, 8);
	public static final Codec<BlendedNoise> CODEC = Codec.unit(UNSEEDED);
	private final PerlinNoise minLimitNoise;
	private final PerlinNoise maxLimitNoise;
	private final PerlinNoise mainNoise;
	private final double xzScale;
	private final double yScale;
	private final double xzMainScale;
	private final double yMainScale;
	private final int cellWidth;
	private final int cellHeight;
	private final double maxValue;

	private BlendedNoise(PerlinNoise perlinNoise, PerlinNoise perlinNoise2, PerlinNoise perlinNoise3, NoiseSamplingSettings noiseSamplingSettings, int i, int j) {
		this.minLimitNoise = perlinNoise;
		this.maxLimitNoise = perlinNoise2;
		this.mainNoise = perlinNoise3;
		this.xzScale = 684.412 * noiseSamplingSettings.xzScale();
		this.yScale = 684.412 * noiseSamplingSettings.yScale();
		this.xzMainScale = this.xzScale / noiseSamplingSettings.xzFactor();
		this.yMainScale = this.yScale / noiseSamplingSettings.yFactor();
		this.cellWidth = i;
		this.cellHeight = j;
		this.maxValue = perlinNoise.maxBrokenValue(this.yScale);
	}

	public BlendedNoise(RandomSource randomSource, NoiseSamplingSettings noiseSamplingSettings, int i, int j) {
		this(
			PerlinNoise.createLegacyForBlendedNoise(randomSource, IntStream.rangeClosed(-15, 0)),
			PerlinNoise.createLegacyForBlendedNoise(randomSource, IntStream.rangeClosed(-15, 0)),
			PerlinNoise.createLegacyForBlendedNoise(randomSource, IntStream.rangeClosed(-7, 0)),
			noiseSamplingSettings,
			i,
			j
		);
	}

	@Override
	public double compute(DensityFunction.FunctionContext functionContext) {
		int i = Math.floorDiv(functionContext.blockX(), this.cellWidth);
		int j = Math.floorDiv(functionContext.blockY(), this.cellHeight);
		int k = Math.floorDiv(functionContext.blockZ(), this.cellWidth);
		double d = 0.0;
		double e = 0.0;
		double f = 0.0;
		boolean bl = true;
		double g = 1.0;

		for (int l = 0; l < 8; l++) {
			ImprovedNoise improvedNoise = this.mainNoise.getOctaveNoise(l);
			if (improvedNoise != null) {
				f += improvedNoise.noise(
						PerlinNoise.wrap((double)i * this.xzMainScale * g),
						PerlinNoise.wrap((double)j * this.yMainScale * g),
						PerlinNoise.wrap((double)k * this.xzMainScale * g),
						this.yMainScale * g,
						(double)j * this.yMainScale * g
					)
					/ g;
			}

			g /= 2.0;
		}

		double h = (f / 10.0 + 1.0) / 2.0;
		boolean bl2 = h >= 1.0;
		boolean bl3 = h <= 0.0;
		g = 1.0;

		for (int m = 0; m < 16; m++) {
			double n = PerlinNoise.wrap((double)i * this.xzScale * g);
			double o = PerlinNoise.wrap((double)j * this.yScale * g);
			double p = PerlinNoise.wrap((double)k * this.xzScale * g);
			double q = this.yScale * g;
			if (!bl2) {
				ImprovedNoise improvedNoise2 = this.minLimitNoise.getOctaveNoise(m);
				if (improvedNoise2 != null) {
					d += improvedNoise2.noise(n, o, p, q, (double)j * q) / g;
				}
			}

			if (!bl3) {
				ImprovedNoise improvedNoise2 = this.maxLimitNoise.getOctaveNoise(m);
				if (improvedNoise2 != null) {
					e += improvedNoise2.noise(n, o, p, q, (double)j * q) / g;
				}
			}

			g /= 2.0;
		}

		return Mth.clampedLerp(d / 512.0, e / 512.0, h) / 128.0;
	}

	@Override
	public double minValue() {
		return -this.maxValue();
	}

	@Override
	public double maxValue() {
		return this.maxValue;
	}

	@VisibleForTesting
	public void parityConfigString(StringBuilder stringBuilder) {
		stringBuilder.append("BlendedNoise{minLimitNoise=");
		this.minLimitNoise.parityConfigString(stringBuilder);
		stringBuilder.append(", maxLimitNoise=");
		this.maxLimitNoise.parityConfigString(stringBuilder);
		stringBuilder.append(", mainNoise=");
		this.mainNoise.parityConfigString(stringBuilder);
		stringBuilder.append(
				String.format(
					", xzScale=%.3f, yScale=%.3f, xzMainScale=%.3f, yMainScale=%.3f, cellWidth=%d, cellHeight=%d",
					this.xzScale,
					this.yScale,
					this.xzMainScale,
					this.yMainScale,
					this.cellWidth,
					this.cellHeight
				)
			)
			.append('}');
	}

	@Override
	public Codec<? extends DensityFunction> codec() {
		return CODEC;
	}
}
