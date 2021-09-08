package net.minecraft.world.level.biome;

import net.minecraft.util.Mth;

public final class TerrainShaper {
	public static final float GLOBAL_OFFSET = 0.015F;
	public static final float COASTAL_CONTINENTALNESS_MAX_THRESHOLD = -0.05F;
	public static final float COASTAL_WEIRDNESS_ABS_MAX_THRESHOLD = 0.15F;
	static ToFloatFunction<TerrainShaper.Point> offsetSampler;
	static ToFloatFunction<TerrainShaper.Point> factorSampler;
	static ToFloatFunction<TerrainShaper.Point> peakNoiseBlockAmplitudeSampler;

	public TerrainShaper() {
		init();
	}

	public static void init() {
		Spline<TerrainShaper.Point> spline = buildErosionOffsetSpline("beachSpline", -0.15F, -0.05F, 0.0F, 0.0F, 0.1F, 0.0F, -0.03F, false, false);
		Spline<TerrainShaper.Point> spline2 = buildErosionOffsetSpline("lowSpline", -0.1F, -0.1F, 0.03F, 0.1F, 0.1F, 0.01F, -0.03F, false, false);
		Spline<TerrainShaper.Point> spline3 = buildErosionOffsetSpline("midSpline", -0.1F, -0.1F, 0.03F, 0.1F, 0.7F, 0.01F, -0.03F, true, true);
		Spline<TerrainShaper.Point> spline4 = buildErosionOffsetSpline("highSpline", -0.05F, 0.3F, 0.03F, 0.1F, 1.0F, 0.01F, 0.01F, true, true);
		float f = -0.51F;
		float g = -0.4F;
		float h = 0.1F;
		float i = -0.15F;
		offsetSampler = Spline.builder(TerrainShaper.Point::continents)
			.named("offsetSampler")
			.addPoint(-1.1F, 0.044F, 0.0F)
			.addPoint(-1.02F, -0.2222F, 0.0F)
			.addPoint(-0.51F, -0.2222F, 0.0F)
			.addPoint(-0.44F, -0.12F, 0.0F)
			.addPoint(-0.18F, -0.12F, 0.0F)
			.addPoint(-0.16F, spline, 0.0F)
			.addPoint(-0.15F, spline, 0.0F)
			.addPoint(-0.1F, spline2, 0.0F)
			.addPoint(0.25F, spline3, 0.0F)
			.addPoint(1.0F, spline4, 0.0F)
			.build()
			.sampler();
		factorSampler = Spline.builder(TerrainShaper.Point::continents)
			.named("Factor-Continents")
			.addPoint(-0.19F, 505.0F, 0.0F)
			.addPoint(-0.15F, getErosionFactor("erosionCoast", 800.0F, true, "ridgeCoast-OldMountains"), 0.0F)
			.addPoint(-0.1F, getErosionFactor("erosionInland", 700.0F, true, "ridgeInland-OldMountains"), 0.0F)
			.addPoint(0.03F, getErosionFactor("erosionMidInland", 650.0F, true, "ridgeMidInland-OldMountains"), 0.0F)
			.addPoint(0.06F, getErosionFactor("erosionFarInland", 600.0F, false, "ridgeFarInland-OldMountains"), 0.0F)
			.build()
			.sampler();
		peakNoiseBlockAmplitudeSampler = Spline.builder(TerrainShaper.Point::continents)
			.named("Peaks")
			.addPoint(0.1F, 0.0F, 0.0F)
			.addPoint(
				0.2F,
				Spline.builder(TerrainShaper.Point::erosion)
					.named("Peaks-erosion")
					.addPoint(
						-0.8F,
						Spline.builder(TerrainShaper.Point::ridges)
							.named("Peaks-erosion-ridges")
							.addPoint(-1.0F, 0.0F, 0.0F)
							.addPoint(0.2F, 0.0F, 0.0F)
							.addPoint(
								1.0F,
								Spline.builder(TerrainShaper.Point::weirdness)
									.named("Peaks-erosion-ridges-weirdness")
									.addPoint(-0.01F, 80.0F, 0.0F)
									.addPoint(0.01F, 20.0F, 0.0F)
									.build(),
								0.0F
							)
							.build(),
						0.0F
					)
					.addPoint(-0.4F, 0.0F, 0.0F)
					.build(),
				0.0F
			)
			.build();
	}

	private static Spline<TerrainShaper.Point> getErosionFactor(String string, float f, boolean bl, String string2) {
		Spline<TerrainShaper.Point> spline = Spline.builder(TerrainShaper.Point::weirdness)
			.named("-base")
			.addPoint(-0.2F, 800.0F, 0.0F)
			.addPoint(0.2F, f, 0.0F)
			.build();
		Spline.Builder<TerrainShaper.Point> builder = Spline.builder(TerrainShaper.Point::erosion)
			.named(string)
			.addPoint(-0.6F, spline, 0.0F)
			.addPoint(
				-0.5F,
				Spline.builder(TerrainShaper.Point::weirdness).named(string + "-variation-1").addPoint(-0.05F, 800.0F, 0.0F).addPoint(0.05F, 342.0F, 0.0F).build(),
				0.0F
			)
			.addPoint(-0.35F, spline, 0.0F)
			.addPoint(-0.25F, spline, 0.0F)
			.addPoint(
				-0.1F,
				Spline.builder(TerrainShaper.Point::weirdness).named(string + "-variation-2").addPoint(-0.05F, 342.0F, 0.0F).addPoint(0.05F, 800.0F, 0.0F).build(),
				0.0F
			)
			.addPoint(0.03F, spline, 0.0F);
		Spline<TerrainShaper.Point> spline2 = Spline.builder(TerrainShaper.Point::ridges)
			.named(string2)
			.addPoint(-0.7F, spline, 0.0F)
			.addPoint(-0.15F, 175.0F, 0.0F)
			.build();
		Spline<TerrainShaper.Point> spline3 = Spline.builder(TerrainShaper.Point::ridges)
			.named(string2)
			.addPoint(0.45F, spline, 0.0F)
			.addPoint(0.7F, 200.0F, 0.0F)
			.build();
		if (bl) {
			Spline<TerrainShaper.Point> spline4 = Spline.builder(TerrainShaper.Point::weirdness)
				.named("weirdnessShattered")
				.addPoint(0.0F, f, 0.0F)
				.addPoint(0.1F, 80.0F, 0.0F)
				.build();
			Spline<TerrainShaper.Point> spline5 = Spline.builder(TerrainShaper.Point::ridges)
				.named("ridgesShattered")
				.addPoint(-0.9F, f, 0.0F)
				.addPoint(-0.69F, spline4, 0.0F)
				.build();
			builder.addPoint(0.35F, f, 0.0F).addPoint(0.45F, spline5, 0.0F).addPoint(0.55F, spline5, 0.0F).addPoint(0.62F, f, 0.0F);
		} else {
			builder.addPoint(0.05F, spline3, 0.0F).addPoint(0.4F, spline3, 0.0F).addPoint(0.45F, spline2, 0.0F).addPoint(0.55F, spline2, 0.0F).addPoint(0.58F, f, 0.0F);
		}

		return builder.build();
	}

	private Spline<TerrainShaper.Point> mimickBedrockSpline() {
		Spline.Builder<TerrainShaper.Point> builder = Spline.builder(TerrainShaper.Point::continents);
		float f = 0.1F;

		for (float g = -1.0F; g < 1.0F + f; g += f) {
			if (g < 0.0F) {
				builder.addPoint(g, 0.0F, 0.0F);
			} else {
				builder.addPoint(g, buildMountainRidgeSplineThroughIteration(g), 0.0F);
			}
		}

		return builder.named("").build();
	}

	private static float calculateSlope(float f, float g, float h, float i) {
		return (g - f) / (i - h);
	}

	private static Spline<TerrainShaper.Point> buildMountainRidgeSplineWithPoints(float f, boolean bl) {
		Spline.Builder<TerrainShaper.Point> builder = Spline.builder(TerrainShaper.Point::ridges).named(String.format("M-spline for continentalness: %.02f", f));
		float g = -0.7F;
		float h = -1.0F;
		float i = mountainContinentalness(-1.0F, f, -0.7F);
		float j = 1.0F;
		float k = mountainContinentalness(1.0F, f, -0.7F);
		float l = calculateMountainRidgeZeroContinentalnessPoint(f);
		float m = -0.65F;
		if (-0.65F < l && l < 1.0F) {
			float n = mountainContinentalness(-0.65F, f, -0.7F);
			float o = -0.75F;
			float p = mountainContinentalness(-0.75F, f, -0.7F);
			float q = calculateSlope(i, p, -1.0F, -0.75F);
			builder.addPoint(-1.0F, i, q);
			builder.addPoint(-0.75F, p, 0.0F);
			builder.addPoint(-0.65F, n, 0.0F);
			float r = mountainContinentalness(l, f, -0.7F);
			float s = calculateSlope(r, k, l, 1.0F);
			float t = 0.01F;
			builder.addPoint(l - 0.01F, r, 0.0F);
			builder.addPoint(l, r, s);
			builder.addPoint(1.0F, k, s);
		} else {
			float n = calculateSlope(i, k, -1.0F, 1.0F);
			if (bl) {
				builder.addPoint(-1.0F, Math.max(0.2F, i), 0.0F);
				builder.addPoint(0.0F, Mth.lerp(0.5F, i, k), n);
			} else {
				builder.addPoint(-1.0F, i, n);
			}

			builder.addPoint(1.0F, k, n);
		}

		return builder.build();
	}

	private static Spline<TerrainShaper.Point> buildMountainRidgeSplineThroughIteration(float f) {
		Spline.Builder<TerrainShaper.Point> builder = Spline.builder(TerrainShaper.Point::ridges).named(String.format("M-spline for continentalness: %.02f", f));
		float g = 0.1F;
		float h = 0.7F;

		for (float i = -1.0F; i < 1.1F; i += 0.1F) {
			builder.addPoint(i, mountainContinentalness(i, f, 0.7F), 0.0F);
		}

		return builder.build();
	}

	private static float mountainContinentalness(float f, float g, float h) {
		float i = 1.17F;
		float j = 0.46082947F;
		float k = 1.0F - (1.0F - g) * 0.5F;
		float l = 0.5F * (1.0F - g);
		float m = (f + 1.17F) * 0.46082947F;
		float n = m * k - l;
		return f < h ? Math.max(n, -0.2222F) : Math.max(n, 0.0F);
	}

	private static float calculateMountainRidgeZeroContinentalnessPoint(float f) {
		float g = 1.17F;
		float h = 0.46082947F;
		float i = 1.0F - (1.0F - f) * 0.5F;
		float j = 0.5F * (1.0F - f);
		return j / (0.46082947F * i) - 1.17F;
	}

	public static Spline<TerrainShaper.Point> buildErosionOffsetSpline(
		String string, float f, float g, float h, float i, float j, float k, float l, boolean bl, boolean bl2
	) {
		float m = 0.6F;
		float n = 1.5F;
		float o = 0.5F;
		float p = 0.5F;
		float q = 1.2F;
		Spline<TerrainShaper.Point> spline = buildMountainRidgeSplineWithPoints(Mth.lerp(j, 0.6F, 1.5F), bl2);
		Spline<TerrainShaper.Point> spline2 = buildMountainRidgeSplineWithPoints(Mth.lerp(j, 0.6F, 1.0F), bl2);
		Spline<TerrainShaper.Point> spline3 = buildMountainRidgeSplineWithPoints(j, bl2);
		Spline<TerrainShaper.Point> spline4 = ridgeSpline(string + "-widePlateau", f - 0.15F, 0.5F * j, Mth.lerp(0.5F, 0.5F, 0.5F) * j, 0.5F * j, 0.6F * j, 0.5F);
		Spline<TerrainShaper.Point> spline5 = ridgeSpline(string + "-narrowPlateau", f, k * j, h * j, 0.5F * j, 0.6F * j, 0.5F);
		Spline<TerrainShaper.Point> spline6 = ridgeSpline(string + "-plains", f, k, k, h, i, 0.5F);
		Spline<TerrainShaper.Point> spline7 = ridgeSpline(string + "-plainsFarInland", f, k, k, h, i, 0.5F);
		Spline<TerrainShaper.Point> spline8 = Spline.builder(TerrainShaper.Point::ridges)
			.named(string)
			.addPoint(-1.0F, f, 0.0F)
			.addPoint(-0.4F, spline6, 0.0F)
			.addPoint(0.0F, i + 0.07F, 0.0F)
			.build();
		Spline<TerrainShaper.Point> spline9 = ridgeSpline(string + "-swamps", -0.02F, l, l, h, i, 0.0F);
		Spline.Builder<TerrainShaper.Point> builder = Spline.builder(TerrainShaper.Point::erosion)
			.named(string)
			.addPoint(-0.85F, spline, 0.0F)
			.addPoint(-0.7F, spline2, 0.0F)
			.addPoint(-0.4F, spline3, 0.0F)
			.addPoint(-0.35F, spline4, 0.0F)
			.addPoint(-0.1F, spline5, 0.0F)
			.addPoint(0.2F, spline6, 0.0F);
		if (bl) {
			builder.addPoint(0.4F, spline7, 0.0F).addPoint(0.45F, spline8, 0.0F).addPoint(0.55F, spline8, 0.0F).addPoint(0.58F, spline7, 0.0F);
		}

		builder.addPoint(0.7F, spline9, 0.0F);
		return builder.build();
	}

	private static Spline<TerrainShaper.Point> ridgeSpline(String string, float f, float g, float h, float i, float j, float k) {
		float l = Math.max(0.5F * (g - f), k);
		float m = 5.0F * (h - g);
		return Spline.builder(TerrainShaper.Point::ridges)
			.named(string)
			.addPoint(-1.0F, f, l)
			.addPoint(-0.4F, g, Math.min(l, m))
			.addPoint(0.0F, h, m)
			.addPoint(0.4F, i, 2.0F * (i - h))
			.addPoint(1.0F, j, 0.7F * (j - i))
			.build();
	}

	public float offset(TerrainShaper.Point point) {
		return offsetSampler.apply(point) + 0.015F;
	}

	public float factor(TerrainShaper.Point point) {
		return factorSampler.apply(point);
	}

	public float peaks(TerrainShaper.Point point) {
		return peakNoiseBlockAmplitudeSampler.apply(point);
	}

	public TerrainShaper.Point makePoint(float f, float g, float h) {
		return new TerrainShaper.Point(f, g, peaksAndValleys(h), h);
	}

	public static boolean isNearWater(float f, float g) {
		return f < -0.05F ? true : Math.abs(g) < 0.15F;
	}

	public static float peaksAndValleys(float f) {
		return -(Math.abs(Math.abs(f) - 0.6666667F) - 0.33333334F) * 3.0F;
	}

	public static void main(String[] strings) {
		init();
		System.out.println(offsetSampler.toString());
	}

	public static final class Point {
		private final float continents;
		private final float erosion;
		private final float ridges;
		private final float weirdness;

		public Point(float f, float g, float h, float i) {
			this.continents = f;
			this.erosion = g;
			this.ridges = h;
			this.weirdness = i;
		}

		public float continents() {
			return this.continents;
		}

		public float erosion() {
			return this.erosion;
		}

		public float ridges() {
			return this.ridges;
		}

		public float weirdness() {
			return this.weirdness;
		}
	}
}
