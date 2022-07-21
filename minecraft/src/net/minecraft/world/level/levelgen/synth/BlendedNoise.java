package net.minecraft.world.level.levelgen.synth;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import java.util.stream.IntStream;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

public class BlendedNoise implements DensityFunction.SimpleFunction {
	private static final Codec<Double> SCALE_RANGE = Codec.doubleRange(0.001, 1000.0);
	private static final MapCodec<BlendedNoise> DATA_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					SCALE_RANGE.fieldOf("xz_scale").forGetter(blendedNoise -> blendedNoise.xzScale),
					SCALE_RANGE.fieldOf("y_scale").forGetter(blendedNoise -> blendedNoise.yScale),
					SCALE_RANGE.fieldOf("xz_factor").forGetter(blendedNoise -> blendedNoise.xzFactor),
					SCALE_RANGE.fieldOf("y_factor").forGetter(blendedNoise -> blendedNoise.yFactor),
					Codec.doubleRange(1.0, 8.0).fieldOf("smear_scale_multiplier").forGetter(blendedNoise -> blendedNoise.smearScaleMultiplier)
				)
				.apply(instance, BlendedNoise::createUnseeded)
	);
	public static final KeyDispatchDataCodec<BlendedNoise> CODEC = KeyDispatchDataCodec.of(DATA_CODEC);
	private final PerlinNoise minLimitNoise;
	private final PerlinNoise maxLimitNoise;
	private final PerlinNoise mainNoise;
	private final double xzMultiplier;
	private final double yMultiplier;
	private final double xzFactor;
	private final double yFactor;
	private final double smearScaleMultiplier;
	private final double maxValue;
	private final double xzScale;
	private final double yScale;

	public static BlendedNoise createUnseeded(double d, double e, double f, double g, double h) {
		return new BlendedNoise(new XoroshiroRandomSource(0L), d, e, f, g, h);
	}

	private BlendedNoise(PerlinNoise perlinNoise, PerlinNoise perlinNoise2, PerlinNoise perlinNoise3, double d, double e, double f, double g, double h) {
		this.minLimitNoise = perlinNoise;
		this.maxLimitNoise = perlinNoise2;
		this.mainNoise = perlinNoise3;
		this.xzScale = d;
		this.yScale = e;
		this.xzFactor = f;
		this.yFactor = g;
		this.smearScaleMultiplier = h;
		this.xzMultiplier = 684.412 * this.xzScale;
		this.yMultiplier = 684.412 * this.yScale;
		this.maxValue = perlinNoise.maxBrokenValue(this.yMultiplier);
	}

	@VisibleForTesting
	public BlendedNoise(RandomSource randomSource, double d, double e, double f, double g, double h) {
		this(
			PerlinNoise.createLegacyForBlendedNoise(randomSource, IntStream.rangeClosed(-15, 0)),
			PerlinNoise.createLegacyForBlendedNoise(randomSource, IntStream.rangeClosed(-15, 0)),
			PerlinNoise.createLegacyForBlendedNoise(randomSource, IntStream.rangeClosed(-7, 0)),
			d,
			e,
			f,
			g,
			h
		);
	}

	public BlendedNoise withNewRandom(RandomSource randomSource) {
		return new BlendedNoise(randomSource, this.xzScale, this.yScale, this.xzFactor, this.yFactor, this.smearScaleMultiplier);
	}

	@Override
	public double compute(DensityFunction.FunctionContext functionContext) {
		double d = (double)functionContext.blockX() * this.xzMultiplier;
		double e = (double)functionContext.blockY() * this.yMultiplier;
		double f = (double)functionContext.blockZ() * this.xzMultiplier;
		double g = d / this.xzFactor;
		double h = e / this.yFactor;
		double i = f / this.xzFactor;
		double j = this.yMultiplier * this.smearScaleMultiplier;
		double k = j / this.yFactor;
		double l = 0.0;
		double m = 0.0;
		double n = 0.0;
		boolean bl = true;
		double o = 1.0;

		for (int p = 0; p < 8; p++) {
			ImprovedNoise improvedNoise = this.mainNoise.getOctaveNoise(p);
			if (improvedNoise != null) {
				n += improvedNoise.noise(PerlinNoise.wrap(g * o), PerlinNoise.wrap(h * o), PerlinNoise.wrap(i * o), k * o, h * o) / o;
			}

			o /= 2.0;
		}

		double q = (n / 10.0 + 1.0) / 2.0;
		boolean bl2 = q >= 1.0;
		boolean bl3 = q <= 0.0;
		o = 1.0;

		for (int r = 0; r < 16; r++) {
			double s = PerlinNoise.wrap(d * o);
			double t = PerlinNoise.wrap(e * o);
			double u = PerlinNoise.wrap(f * o);
			double v = j * o;
			if (!bl2) {
				ImprovedNoise improvedNoise2 = this.minLimitNoise.getOctaveNoise(r);
				if (improvedNoise2 != null) {
					l += improvedNoise2.noise(s, t, u, v, e * o) / o;
				}
			}

			if (!bl3) {
				ImprovedNoise improvedNoise2 = this.maxLimitNoise.getOctaveNoise(r);
				if (improvedNoise2 != null) {
					m += improvedNoise2.noise(s, t, u, v, e * o) / o;
				}
			}

			o /= 2.0;
		}

		return Mth.clampedLerp(l / 512.0, m / 512.0, q) / 128.0;
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
					Locale.ROOT,
					", xzScale=%.3f, yScale=%.3f, xzMainScale=%.3f, yMainScale=%.3f, cellWidth=4, cellHeight=8",
					684.412,
					684.412,
					8.555150000000001,
					4.277575000000001
				)
			)
			.append('}');
	}

	@Override
	public KeyDispatchDataCodec<? extends DensityFunction> codec() {
		return CODEC;
	}
}
