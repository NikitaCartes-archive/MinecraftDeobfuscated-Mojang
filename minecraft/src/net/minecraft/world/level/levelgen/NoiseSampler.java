package net.minecraft.world.level.levelgen;

import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

public class NoiseSampler {
	private static final int OLD_CELL_COUNT_Y = 32;
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
	private final NoiseModifier caveNoiseModifier;

	public NoiseSampler(
		BiomeSource biomeSource,
		int i,
		int j,
		int k,
		NoiseSettings noiseSettings,
		BlendedNoise blendedNoise,
		@Nullable SimplexNoise simplexNoise,
		PerlinNoise perlinNoise,
		NoiseModifier noiseModifier
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
		this.caveNoiseModifier = noiseModifier;
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
			int n = i * this.cellWidth >> 2;
			int o = j * this.cellWidth >> 2;
			BiomeSource.TerrainShape terrainShape = this.biomeSource.getTerrainShape(n, o);
			d = terrainShape.offset;
			e = terrainShape.factor;
		}

		double f = 684.412 * noiseSettings.noiseSamplingSettings().xzScale();
		double g = 684.412 * noiseSettings.noiseSamplingSettings().yScale();
		double h = f / noiseSettings.noiseSamplingSettings().xzFactor();
		double p = g / noiseSettings.noiseSamplingSettings().yFactor();

		for (int q = 0; q <= m; q++) {
			int r = q + l;
			double s = (double)(r * this.cellHeight);
			double t = this.blendedNoise.sampleAndClampNoise(i, r, j, f, g, h, p);
			double u = this.computeInitialDensity(s, d, e, 0.0) + t;
			u = this.caveNoiseModifier.modifyNoise(u, i * this.cellWidth, r * this.cellHeight, j * this.cellWidth);
			u = this.applySlide(u, r);
			ds[q] = u;
		}
	}

	private double computeInitialDensity(double d, double e, double f, double g) {
		double h = computeDimensionDensity(this.dimensionDensityFactor, this.dimensionDensityOffset, d, g);
		double i = (h + e) * f;
		return i * (double)(i > 0.0 ? 4 : 1);
	}

	public static double computeDimensionDensity(double d, double e, double f) {
		return computeDimensionDensity(d, e, f, 0.0);
	}

	public static double computeDimensionDensity(double d, double e, double f, double g) {
		double h = 1.0 - f / 128.0 + g;
		return h * d + e;
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

	protected NoiseSampler.SurfaceInfo getSurfaceInfo(int i, int j) {
		int k = Math.floorDiv(i, this.cellWidth);
		int l = Math.floorDiv(j, this.cellWidth);
		int m = Mth.intFloorDiv(this.noiseSettings.minY(), this.cellHeight);
		int n = Mth.intFloorDiv(this.noiseSettings.height(), this.cellHeight);
		int o = 2;
		int p = 1;
		boolean bl = false;
		int q = Integer.MAX_VALUE;

		for (int r = k - 2; r <= k + 2; r += 2) {
			for (int s = l - 2; s <= l + 2; s += 2) {
				int t = r * this.cellWidth >> 2;
				int u = s * this.cellWidth >> 2;
				BiomeSource.TerrainShape terrainShape = this.biomeSource.getTerrainShape(t, u);
				double d = terrainShape.offset;
				double e = terrainShape.factor;
				if (terrainShape.coastal) {
					bl = true;
				}

				for (int v = m; v <= m + n; v++) {
					int w = v - m;
					double f = (double)(v * this.cellHeight);
					double g = -70.0;
					double h = this.computeInitialDensity(f, d, e, 0.0) + -70.0;
					double x = this.applySlide(h, w);
					if (this.isAbovePreliminarySurfaceLevel(x)) {
						q = Math.min(v * this.cellHeight, q);
						break;
					}
				}
			}
		}

		return new NoiseSampler.SurfaceInfo(q, bl);
	}

	protected int getPreliminarySurfaceLevel(int i, int j) {
		return this.getSurfaceInfo(i, j).preliminarySurfaceLevel;
	}

	private boolean isAbovePreliminarySurfaceLevel(double d) {
		return d < 50.0;
	}

	public static class SurfaceInfo {
		public final int preliminarySurfaceLevel;
		public final boolean coastal;

		SurfaceInfo(int i, boolean bl) {
			this.preliminarySurfaceLevel = i;
			this.coastal = bl;
		}
	}
}
