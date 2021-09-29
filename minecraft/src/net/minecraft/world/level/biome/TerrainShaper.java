package net.minecraft.world.level.biome;

import com.mojang.datafixers.util.Pair;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.Mth;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.util.VisibleForDebug;

public final class TerrainShaper {
	private static final float GLOBAL_OFFSET = 0.015F;
	static final ToFloatFunction<TerrainShaper.Point> CONTINENTS_EXTRACTOR = new ToFloatFunction<TerrainShaper.Point>() {
		public float apply(TerrainShaper.Point point) {
			return point.continents;
		}

		public String toString() {
			return "continents";
		}
	};
	static final ToFloatFunction<TerrainShaper.Point> EROSION_EXTRACTOR = new ToFloatFunction<TerrainShaper.Point>() {
		public float apply(TerrainShaper.Point point) {
			return point.erosion;
		}

		public String toString() {
			return "erosion";
		}
	};
	static final ToFloatFunction<TerrainShaper.Point> WEIRDNESS_EXTRACTOR = new ToFloatFunction<TerrainShaper.Point>() {
		public float apply(TerrainShaper.Point point) {
			return point.weirdness;
		}

		public String toString() {
			return "weirdness";
		}
	};
	static final ToFloatFunction<TerrainShaper.Point> RIDGES_EXTRACTOR = new ToFloatFunction<TerrainShaper.Point>() {
		public float apply(TerrainShaper.Point point) {
			return point.ridges;
		}

		public String toString() {
			return "ridges";
		}
	};
	@VisibleForDebug
	public CubicSpline<TerrainShaper.Point> offsetSampler;
	@VisibleForDebug
	public CubicSpline<TerrainShaper.Point> factorSampler;
	@VisibleForDebug
	public CubicSpline<TerrainShaper.Point> jaggednessSampler;

	public TerrainShaper() {
		CubicSpline<TerrainShaper.Point> cubicSpline = buildErosionOffsetSpline(-0.15F, 0.0F, 0.0F, 0.1F, 0.0F, -0.03F, false, false);
		CubicSpline<TerrainShaper.Point> cubicSpline2 = buildErosionOffsetSpline(-0.1F, 0.03F, 0.1F, 0.1F, 0.01F, -0.03F, false, false);
		CubicSpline<TerrainShaper.Point> cubicSpline3 = buildErosionOffsetSpline(-0.1F, 0.03F, 0.1F, 0.7F, 0.01F, -0.03F, true, true);
		CubicSpline<TerrainShaper.Point> cubicSpline4 = buildErosionOffsetSpline(-0.05F, 0.03F, 0.1F, 1.0F, 0.01F, 0.01F, true, true);
		float f = -0.51F;
		float g = -0.4F;
		float h = 0.1F;
		float i = -0.15F;
		this.offsetSampler = CubicSpline.builder(CONTINENTS_EXTRACTOR)
			.addPoint(-1.1F, 0.044F, 0.0F)
			.addPoint(-1.02F, -0.2222F, 0.0F)
			.addPoint(-0.51F, -0.2222F, 0.0F)
			.addPoint(-0.44F, -0.12F, 0.0F)
			.addPoint(-0.18F, -0.12F, 0.0F)
			.addPoint(-0.16F, cubicSpline, 0.0F)
			.addPoint(-0.15F, cubicSpline, 0.0F)
			.addPoint(-0.1F, cubicSpline2, 0.0F)
			.addPoint(0.25F, cubicSpline3, 0.0F)
			.addPoint(1.0F, cubicSpline4, 0.0F)
			.build();
		this.factorSampler = CubicSpline.builder(CONTINENTS_EXTRACTOR)
			.addPoint(-0.19F, 3.95F, 0.0F)
			.addPoint(-0.15F, getErosionFactor(6.25F, true), 0.0F)
			.addPoint(-0.1F, getErosionFactor(5.47F, true), 0.0F)
			.addPoint(0.03F, getErosionFactor(5.08F, true), 0.0F)
			.addPoint(0.06F, getErosionFactor(4.69F, false), 0.0F)
			.build();
		float j = 0.65F;
		this.jaggednessSampler = CubicSpline.builder(CONTINENTS_EXTRACTOR)
			.addPoint(-0.11F, 0.0F, 0.0F)
			.addPoint(0.03F, this.buildErosionJaggednessSpline(1.0F, 0.5F, 0.0F, 0.0F), 0.0F)
			.addPoint(0.65F, this.buildErosionJaggednessSpline(1.0F, 1.0F, 1.0F, 0.0F), 0.0F)
			.build();
	}

	private CubicSpline<TerrainShaper.Point> buildErosionJaggednessSpline(float f, float g, float h, float i) {
		float j = -0.5775F;
		CubicSpline<TerrainShaper.Point> cubicSpline = this.buildRidgeJaggednessSpline(f, h);
		CubicSpline<TerrainShaper.Point> cubicSpline2 = this.buildRidgeJaggednessSpline(g, i);
		return CubicSpline.builder(EROSION_EXTRACTOR)
			.addPoint(-1.0F, cubicSpline, 0.0F)
			.addPoint(-0.78F, cubicSpline2, 0.0F)
			.addPoint(-0.5775F, cubicSpline2, 0.0F)
			.addPoint(-0.375F, 0.0F, 0.0F)
			.build();
	}

	private CubicSpline<TerrainShaper.Point> buildRidgeJaggednessSpline(float f, float g) {
		float h = peaksAndValleys(0.4F);
		float i = peaksAndValleys(0.56666666F);
		float j = (h + i) / 2.0F;
		CubicSpline.Builder<TerrainShaper.Point> builder = CubicSpline.builder(RIDGES_EXTRACTOR);
		builder.addPoint(h, 0.0F, 0.0F);
		if (g > 0.0F) {
			builder.addPoint(j, this.buildWeirdnessJaggednessSpline(g), 0.0F);
		} else {
			builder.addPoint(j, 0.0F, 0.0F);
		}

		if (f > 0.0F) {
			builder.addPoint(1.0F, this.buildWeirdnessJaggednessSpline(f), 0.0F);
		} else {
			builder.addPoint(1.0F, 0.0F, 0.0F);
		}

		return builder.build();
	}

	private CubicSpline<TerrainShaper.Point> buildWeirdnessJaggednessSpline(float f) {
		float g = 0.63F * f;
		float h = 0.3F * f;
		return CubicSpline.builder(WEIRDNESS_EXTRACTOR).addPoint(-0.01F, g, 0.0F).addPoint(0.01F, h, 0.0F).build();
	}

	private static CubicSpline<TerrainShaper.Point> getErosionFactor(float f, boolean bl) {
		CubicSpline<TerrainShaper.Point> cubicSpline = CubicSpline.builder(WEIRDNESS_EXTRACTOR).addPoint(-0.2F, 6.3F, 0.0F).addPoint(0.2F, f, 0.0F).build();
		CubicSpline.Builder<TerrainShaper.Point> builder = CubicSpline.builder(EROSION_EXTRACTOR)
			.addPoint(-0.6F, cubicSpline, 0.0F)
			.addPoint(-0.5F, CubicSpline.builder(WEIRDNESS_EXTRACTOR).addPoint(-0.05F, 6.3F, 0.0F).addPoint(0.05F, 2.67F, 0.0F).build(), 0.0F)
			.addPoint(-0.35F, cubicSpline, 0.0F)
			.addPoint(-0.25F, cubicSpline, 0.0F)
			.addPoint(-0.1F, CubicSpline.builder(WEIRDNESS_EXTRACTOR).addPoint(-0.05F, 2.67F, 0.0F).addPoint(0.05F, 6.3F, 0.0F).build(), 0.0F)
			.addPoint(0.03F, cubicSpline, 0.0F);
		if (bl) {
			CubicSpline<TerrainShaper.Point> cubicSpline2 = CubicSpline.builder(WEIRDNESS_EXTRACTOR).addPoint(0.0F, f, 0.0F).addPoint(0.1F, 0.625F, 0.0F).build();
			CubicSpline<TerrainShaper.Point> cubicSpline3 = CubicSpline.builder(RIDGES_EXTRACTOR).addPoint(-0.9F, f, 0.0F).addPoint(-0.69F, cubicSpline2, 0.0F).build();
			builder.addPoint(0.35F, f, 0.0F).addPoint(0.45F, cubicSpline3, 0.0F).addPoint(0.55F, cubicSpline3, 0.0F).addPoint(0.62F, f, 0.0F);
		} else {
			CubicSpline<TerrainShaper.Point> cubicSpline2 = CubicSpline.builder(RIDGES_EXTRACTOR)
				.addPoint(-0.7F, cubicSpline, 0.0F)
				.addPoint(-0.15F, 1.37F, 0.0F)
				.build();
			CubicSpline<TerrainShaper.Point> cubicSpline3 = CubicSpline.builder(RIDGES_EXTRACTOR).addPoint(0.45F, cubicSpline, 0.0F).addPoint(0.7F, 1.56F, 0.0F).build();
			builder.addPoint(0.05F, cubicSpline3, 0.0F)
				.addPoint(0.4F, cubicSpline3, 0.0F)
				.addPoint(0.45F, cubicSpline2, 0.0F)
				.addPoint(0.55F, cubicSpline2, 0.0F)
				.addPoint(0.58F, f, 0.0F);
		}

		return builder.build();
	}

	private static float calculateSlope(float f, float g, float h, float i) {
		return (g - f) / (i - h);
	}

	private static CubicSpline<TerrainShaper.Point> buildMountainRidgeSplineWithPoints(float f, boolean bl) {
		CubicSpline.Builder<TerrainShaper.Point> builder = CubicSpline.builder(RIDGES_EXTRACTOR);
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

	private static CubicSpline<TerrainShaper.Point> buildErosionOffsetSpline(float f, float g, float h, float i, float j, float k, boolean bl, boolean bl2) {
		float l = 0.6F;
		float m = 0.5F;
		float n = 0.5F;
		CubicSpline<TerrainShaper.Point> cubicSpline = buildMountainRidgeSplineWithPoints(Mth.lerp(i, 0.6F, 1.5F), bl2);
		CubicSpline<TerrainShaper.Point> cubicSpline2 = buildMountainRidgeSplineWithPoints(Mth.lerp(i, 0.6F, 1.0F), bl2);
		CubicSpline<TerrainShaper.Point> cubicSpline3 = buildMountainRidgeSplineWithPoints(i, bl2);
		CubicSpline<TerrainShaper.Point> cubicSpline4 = ridgeSpline(f - 0.15F, 0.5F * i, Mth.lerp(0.5F, 0.5F, 0.5F) * i, 0.5F * i, 0.6F * i, 0.5F);
		CubicSpline<TerrainShaper.Point> cubicSpline5 = ridgeSpline(f, j * i, g * i, 0.5F * i, 0.6F * i, 0.5F);
		CubicSpline<TerrainShaper.Point> cubicSpline6 = ridgeSpline(f, j, j, g, h, 0.5F);
		CubicSpline<TerrainShaper.Point> cubicSpline7 = ridgeSpline(f, j, j, g, h, 0.5F);
		CubicSpline<TerrainShaper.Point> cubicSpline8 = CubicSpline.builder(RIDGES_EXTRACTOR)
			.addPoint(-1.0F, f, 0.0F)
			.addPoint(-0.4F, cubicSpline6, 0.0F)
			.addPoint(0.0F, h + 0.07F, 0.0F)
			.build();
		CubicSpline<TerrainShaper.Point> cubicSpline9 = ridgeSpline(-0.02F, k, k, g, h, 0.0F);
		CubicSpline.Builder<TerrainShaper.Point> builder = CubicSpline.builder(EROSION_EXTRACTOR)
			.addPoint(-0.85F, cubicSpline, 0.0F)
			.addPoint(-0.7F, cubicSpline2, 0.0F)
			.addPoint(-0.4F, cubicSpline3, 0.0F)
			.addPoint(-0.35F, cubicSpline4, 0.0F)
			.addPoint(-0.1F, cubicSpline5, 0.0F)
			.addPoint(0.2F, cubicSpline6, 0.0F);
		if (bl) {
			builder.addPoint(0.4F, cubicSpline7, 0.0F).addPoint(0.45F, cubicSpline8, 0.0F).addPoint(0.55F, cubicSpline8, 0.0F).addPoint(0.58F, cubicSpline7, 0.0F);
		}

		builder.addPoint(0.7F, cubicSpline9, 0.0F);
		return builder.build();
	}

	private static CubicSpline<TerrainShaper.Point> ridgeSpline(float f, float g, float h, float i, float j, float k) {
		float l = Math.max(0.5F * (g - f), k);
		float m = 5.0F * (h - g);
		return CubicSpline.builder(RIDGES_EXTRACTOR)
			.addPoint(-1.0F, f, l)
			.addPoint(-0.4F, g, Math.min(l, m))
			.addPoint(0.0F, h, m)
			.addPoint(0.4F, i, 2.0F * (i - h))
			.addPoint(1.0F, j, 0.7F * (j - i))
			.build();
	}

	public void addDebugBiomesToVisualizeSplinePoints(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer) {
		Climate.Parameter parameter = Climate.Parameter.span(-1.0F, 1.0F);
		consumer.accept(Pair.of(Climate.parameters(parameter, parameter, parameter, parameter, Climate.Parameter.point(0.0F), parameter, 0.01F), Biomes.PLAINS));
		CubicSpline<TerrainShaper.Point> cubicSpline = buildErosionOffsetSpline(-0.15F, 0.0F, 0.0F, 0.1F, 0.0F, -0.03F, false, false);
		ResourceKey<Biome> resourceKey = Biomes.DESERT;

		for (Float float_ : cubicSpline.debugLocations()) {
			consumer.accept(
				Pair.of(Climate.parameters(parameter, parameter, parameter, Climate.Parameter.point(float_), Climate.Parameter.point(0.0F), parameter, 0.0F), resourceKey)
			);
			resourceKey = resourceKey == Biomes.DESERT ? Biomes.BADLANDS : Biomes.DESERT;
		}

		for (Float float_ : this.offsetSampler.debugLocations()) {
			consumer.accept(
				Pair.of(
					Climate.parameters(parameter, parameter, Climate.Parameter.point(float_), parameter, Climate.Parameter.point(0.0F), parameter, 0.0F), Biomes.SNOWY_TAIGA
				)
			);
		}
	}

	@VisibleForDebug
	public CubicSpline<TerrainShaper.Point> offsetSampler() {
		return this.offsetSampler;
	}

	@VisibleForDebug
	public CubicSpline<TerrainShaper.Point> factorSampler() {
		return this.factorSampler;
	}

	public float offset(TerrainShaper.Point point) {
		return this.offsetSampler.apply(point) + 0.015F;
	}

	public float factor(TerrainShaper.Point point) {
		return this.factorSampler.apply(point);
	}

	public float jaggedness(TerrainShaper.Point point) {
		return this.jaggednessSampler.apply(point);
	}

	public TerrainShaper.Point makePoint(float f, float g, float h) {
		return new TerrainShaper.Point(f, g, peaksAndValleys(h), h);
	}

	public static float peaksAndValleys(float f) {
		return -(Math.abs(Math.abs(f) - 0.6666667F) - 0.33333334F) * 3.0F;
	}

	public static final class Point {
		final float continents;
		final float erosion;
		final float ridges;
		final float weirdness;

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
