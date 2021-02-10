package net.minecraft.world.level.levelgen;

import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class NoiseSampler {
	private static final float[] BIOME_WEIGHTS = Util.make(new float[25], fs -> {
		for (int i = -2; i <= 2; i++) {
			for (int j = -2; j <= 2; j++) {
				float f = 10.0F / Mth.sqrt((float)(i * i + j * j) + 0.2F);
				fs[i + 2 + (j + 2) * 5] = f;
			}
		}
	});
	private final BiomeSource biomeSource;
	private final int cellWidth;
	private final int cellHeight;
	private final int cellCountY;
	private final NoiseSettings noiseSettings;
	private final BlendedNoise blendedNoise;
	@Nullable
	private final SimplexNoise islandNoise;
	private final PerlinNoise depthNoise;
	private final double topSlideTarget;
	private final double topSlideSize;
	private final double topSlideOffset;
	private final double bottomSlideTarget;
	private final double bottomSlideSize;
	private final double bottomSlideOffset;
	private final double dimensionDensityFactor;
	private final double dimensionDensityOffset;
	@Nullable
	private final Cavifier cavifier;

	public NoiseSampler(
		BiomeSource biomeSource,
		int i,
		int j,
		int k,
		NoiseSettings noiseSettings,
		BlendedNoise blendedNoise,
		@Nullable SimplexNoise simplexNoise,
		PerlinNoise perlinNoise,
		@Nullable Cavifier cavifier
	) {
		this.cellWidth = i;
		this.cellHeight = j;
		this.biomeSource = biomeSource;
		this.cellCountY = k;
		this.noiseSettings = noiseSettings;
		this.blendedNoise = blendedNoise;
		this.islandNoise = simplexNoise;
		this.depthNoise = perlinNoise;
		this.topSlideTarget = (double)noiseSettings.topSlideSettings().target();
		this.topSlideSize = (double)noiseSettings.topSlideSettings().size();
		this.topSlideOffset = (double)noiseSettings.topSlideSettings().offset();
		this.bottomSlideTarget = (double)noiseSettings.bottomSlideSettings().target();
		this.bottomSlideSize = (double)noiseSettings.bottomSlideSettings().size();
		this.bottomSlideOffset = (double)noiseSettings.bottomSlideSettings().offset();
		this.dimensionDensityFactor = noiseSettings.densityFactor();
		this.dimensionDensityOffset = noiseSettings.densityOffset();
		this.cavifier = cavifier;
	}

	public void fillNoiseColumn(double[] ds, int i, int j, NoiseSettings noiseSettings, int k, int l, int m) {
		double d;
		double e;
		if (this.islandNoise != null) {
			d = (double)(TheEndBiomeSource.getHeightValue(this.islandNoise, i, j) - 8.0F);
			if (d > 0.0) {
				e = 0.25;
			} else {
				e = 1.0;
			}
		} else {
			float f = 0.0F;
			float g = 0.0F;
			float h = 0.0F;
			int n = 2;
			int o = k;
			float p = this.biomeSource.getNoiseBiome(i, k, j).getDepth();

			for (int q = -2; q <= 2; q++) {
				for (int r = -2; r <= 2; r++) {
					Biome biome = this.biomeSource.getNoiseBiome(i + q, o, j + r);
					float s = biome.getDepth();
					float t = biome.getScale();
					float u;
					float v;
					if (noiseSettings.isAmplified() && s > 0.0F) {
						u = 1.0F + s * 2.0F;
						v = 1.0F + t * 4.0F;
					} else {
						u = s;
						v = t;
					}

					float w = s > p ? 0.5F : 1.0F;
					float x = w * BIOME_WEIGHTS[q + 2 + (r + 2) * 5] / (u + 2.0F);
					f += v * x;
					g += u * x;
					h += x;
				}
			}

			float y = g / h;
			float z = f / h;
			double aa = (double)(y * 0.5F - 0.125F);
			double ab = (double)(z * 0.9F + 0.1F);
			d = aa * 0.265625;
			e = 96.0 / ab;
		}

		double ac = 684.412 * noiseSettings.noiseSamplingSettings().xzScale();
		double ad = 684.412 * noiseSettings.noiseSamplingSettings().yScale();
		double ae = ac / noiseSettings.noiseSamplingSettings().xzFactor();
		double af = ad / noiseSettings.noiseSamplingSettings().yFactor();
		double aa = noiseSettings.randomDensityOffset() ? this.getRandomDensity(i, j) : 0.0;

		for (int ag = 0; ag <= m; ag++) {
			int ah = ag + l;
			double ai = this.blendedNoise.sampleAndClampNoise(i, ah, j, ac, ad, ae, af);
			double aj = this.computeInitialDensity(ah, d, e, aa) + ai;
			aj = this.cavify(i * this.cellWidth, ah * this.cellHeight, j * this.cellWidth, ai, aj);
			aj = this.applySlide(aj, ah);
			ds[ag] = aj;
		}
	}

	private double cavify(int i, int j, int k, double d, double e) {
		return this.cavifier != null ? this.cavifier.cavify(i, j, k, d, e) : e;
	}

	private double computeInitialDensity(int i, double d, double e, double f) {
		double g = 1.0 - (double)i * 2.0 / 32.0 + f;
		double h = g * this.dimensionDensityFactor + this.dimensionDensityOffset;
		double j = (h + d) * e;
		return j * (double)(j > 0.0 ? 4 : 1);
	}

	private double applySlide(double d, int i) {
		int j = Mth.intFloorDiv(this.noiseSettings.minY(), this.cellHeight);
		int k = i - j;
		if (this.topSlideSize > 0.0) {
			double e = ((double)(this.cellCountY - k) - this.topSlideOffset) / this.topSlideSize;
			d = Mth.clampedLerp(this.topSlideTarget, d, e);
		}

		if (this.bottomSlideSize > 0.0) {
			double e = ((double)k - this.bottomSlideOffset) / this.bottomSlideSize;
			d = Mth.clampedLerp(this.bottomSlideTarget, d, e);
		}

		return d;
	}

	private double getRandomDensity(int i, int j) {
		double d = this.depthNoise.getValue((double)(i * 200), 10.0, (double)(j * 200), 1.0, 0.0, true);
		double e;
		if (d < 0.0) {
			e = -d * 0.3;
		} else {
			e = d;
		}

		double f = e * 24.575625 - 2.0;
		return f < 0.0 ? f * 0.009486607142857142 : Math.min(f, 1.0) * 0.006640625;
	}
}
