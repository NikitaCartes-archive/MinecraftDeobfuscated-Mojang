package net.minecraft.world.level.biome;

import net.minecraft.util.Mth;

public final class TerrainShaper {
	public static final float GLOBAL_OFFSET = 0.015F;
	static ToFloatFunction<TerrainShaper.Point> offsetSampler;
	static ToFloatFunction<TerrainShaper.Point> factorSampler;

	public TerrainShaper() {
		init();
	}

	public static void init() {
		Spline<TerrainShaper.Point> spline = buildErosionOffsetSpline("beachSpline", -0.15F, -0.05F, 0.0F, 0.0F, 0.1F, 0.0F, -0.03F);
		Spline<TerrainShaper.Point> spline2 = buildErosionOffsetSpline("lowSpline", -0.1F, -0.1F, 0.03F, 0.1F, 0.1F, 0.01F, -0.03F);
		Spline<TerrainShaper.Point> spline3 = buildErosionOffsetSpline("midSpline", -0.1F, -0.1F, 0.03F, 0.1F, 0.7F, 0.01F, -0.03F);
		Spline<TerrainShaper.Point> spline4 = buildErosionOffsetSpline("highSpline", -0.1F, 0.3F, 0.03F, 0.1F, 1.0F, 0.01F, 0.01F);
		float f = -0.51F;
		float g = -0.4F;
		float h = 0.1F;
		float i = -0.15F;
		offsetSampler = Spline.builder(TerrainShaper.Point::continents)
			.named("offsetSampler")
			.addPoint(-1.1F, 0.044F, 0.0F)
			.addPoint(-1.005F, -0.2222F, 0.0F)
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
			.addPoint(-0.15F, getErosionCoast("erosionCoast", 783.0F, true, "ridgeCoast-OldMountains"), 0.0F)
			.addPoint(-0.1F, getErosionCoast("erosionInland", 600.0F, true, "ridgeInlande-OldMountains"), 0.0F)
			.addPoint(0.03F, getErosionCoast("erosionMidInland", 600.0F, true, "ridgeInlande-OldMountains"), 0.0F)
			.addPoint(0.06F, getErosionCoast("erosionFarInland", 600.0F, false, "ridgeInlande-OldMountains"), 0.0F)
			.build()
			.sampler();
	}

	private static Spline<TerrainShaper.Point> getErosionCoast(String string, float f, boolean bl, String string2) {
		Spline.Builder<TerrainShaper.Point> builder = Spline.builder(TerrainShaper.Point::erosion)
			.named(string)
			.addPoint(-1.0F, f, 0.0F)
			.addPoint(-0.5F, 342.0F, 0.0F)
			.addPoint(0.05F, f, 0.0F);
		Spline<TerrainShaper.Point> spline = Spline.builder(TerrainShaper.Point::ridges).named(string2).addPoint(0.45F, f, 0.0F).addPoint(0.6F, 175.0F, 0.0F).build();
		if (bl) {
			Spline<TerrainShaper.Point> spline2 = Spline.builder(TerrainShaper.Point::ridges)
				.named("ridgesShattered")
				.addPoint(-0.72F, f, 0.0F)
				.addPoint(-0.69F, 80.0F, 0.0F)
				.build();
			builder.addPoint(0.051F, spline, 0.0F)
				.addPoint(0.45F, spline, 0.0F)
				.addPoint(0.51F, spline2, 0.0F)
				.addPoint(0.59F, spline2, 0.0F)
				.addPoint(0.65F, spline, 0.0F);
		} else {
			builder.addPoint(0.051F, spline, 0.0F);
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

	private static Spline<TerrainShaper.Point> buildMountainRidgeSplineWithPoints(float f) {
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
			builder.addPoint(-1.0F, i, n);
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

	public static Spline<TerrainShaper.Point> buildErosionOffsetSpline(String string, float f, float g, float h, float i, float j, float k, float l) {
		float m = 0.6F;
		float n = 1.5F;
		float o = 0.5F;
		float p = 0.5F;
		float q = 1.2F;
		float r = Mth.lerp(j, 0.6F, 1.0F);
		Spline<TerrainShaper.Point> spline = buildMountainRidgeSplineWithPoints(r);
		Spline<TerrainShaper.Point> spline2 = buildMountainRidgeSplineWithPoints(j);
		Spline<TerrainShaper.Point> spline3 = ridgeSpline(string + "-widePlateau", Mth.lerp(0.5F, f, g), 0.5F * j, Mth.lerp(0.5F, 0.5F, 0.5F) * j, 0.5F * j, 0.6F * j);
		Spline<TerrainShaper.Point> spline4 = ridgeSpline(string + "-narrowPlateau", f, k * j, h * j, 0.5F * j, 0.6F * j);
		Spline<TerrainShaper.Point> spline5 = ridgeSpline(string + "-plains", f, k, k, h, i);
		Spline<TerrainShaper.Point> spline6 = ridgeSpline(string + "-swamps", f, l, l, h, i);
		return Spline.builder(TerrainShaper.Point::erosion)
			.named(string)
			.addPoint(-0.9F, spline, 0.0F)
			.addPoint(-0.4F, spline2, 0.0F)
			.addPoint(-0.35F, spline3, 0.0F)
			.addPoint(-0.1F, spline4, 0.0F)
			.addPoint(0.2F, spline5, 0.0F)
			.addPoint(1.0F, spline6, 0.0F)
			.build();
	}

	private static Spline<TerrainShaper.Point> ridgeSpline(String string, float f, float g, float h, float i, float j) {
		float k = Math.max(0.5F * (g - f), 0.7F);
		float l = 5.0F * (h - g);
		return Spline.builder(TerrainShaper.Point::ridges)
			.named(string)
			.addPoint(-1.0F, f, k)
			.addPoint(-0.4F, g, Math.min(k, l))
			.addPoint(0.0F, h, l)
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

	public TerrainShaper.Point makePoint(float f, float g, float h) {
		return new TerrainShaper.Point(f, g, peaksAndValleys(h));
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

		public Point(float f, float g, float h) {
			this.continents = f;
			this.erosion = g;
			this.ridges = h;
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
	}
}
