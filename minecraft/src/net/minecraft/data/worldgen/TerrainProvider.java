package net.minecraft.data.worldgen;

import net.minecraft.util.CubicSpline;
import net.minecraft.util.Mth;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.level.levelgen.NoiseRouterData;

public class TerrainProvider {
	private static final float DEEP_OCEAN_CONTINENTALNESS = -0.51F;
	private static final float OCEAN_CONTINENTALNESS = -0.4F;
	private static final float PLAINS_CONTINENTALNESS = 0.1F;
	private static final float BEACH_CONTINENTALNESS = -0.15F;
	private static final ToFloatFunction<Float> NO_TRANSFORM = ToFloatFunction.IDENTITY;
	private static final ToFloatFunction<Float> AMPLIFIED_OFFSET = ToFloatFunction.createUnlimited(f -> f < 0.0F ? f : f * 2.0F);
	private static final ToFloatFunction<Float> AMPLIFIED_FACTOR = ToFloatFunction.createUnlimited(f -> 1.25F - 6.25F / (f + 5.0F));
	private static final ToFloatFunction<Float> AMPLIFIED_JAGGEDNESS = ToFloatFunction.createUnlimited(f -> f * 2.0F);

	public static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> overworldOffset(I toFloatFunction, I toFloatFunction2, I toFloatFunction3, boolean bl) {
		ToFloatFunction<Float> toFloatFunction4 = bl ? AMPLIFIED_OFFSET : NO_TRANSFORM;
		CubicSpline<C, I> cubicSpline = buildErosionOffsetSpline(
			toFloatFunction2, toFloatFunction3, -0.15F, 0.0F, 0.0F, 0.1F, 0.0F, -0.03F, false, false, toFloatFunction4
		);
		CubicSpline<C, I> cubicSpline2 = buildErosionOffsetSpline(
			toFloatFunction2, toFloatFunction3, -0.1F, 0.03F, 0.1F, 0.1F, 0.01F, -0.03F, false, false, toFloatFunction4
		);
		CubicSpline<C, I> cubicSpline3 = buildErosionOffsetSpline(
			toFloatFunction2, toFloatFunction3, -0.1F, 0.03F, 0.1F, 0.7F, 0.01F, -0.03F, true, true, toFloatFunction4
		);
		CubicSpline<C, I> cubicSpline4 = buildErosionOffsetSpline(
			toFloatFunction2, toFloatFunction3, -0.05F, 0.03F, 0.1F, 1.0F, 0.01F, 0.01F, true, true, toFloatFunction4
		);
		return CubicSpline.<C, I>builder(toFloatFunction, toFloatFunction4)
			.addPoint(-1.1F, 0.044F)
			.addPoint(-1.02F, -0.2222F)
			.addPoint(-0.51F, -0.2222F)
			.addPoint(-0.44F, -0.12F)
			.addPoint(-0.18F, -0.12F)
			.addPoint(-0.16F, cubicSpline)
			.addPoint(-0.15F, cubicSpline)
			.addPoint(-0.1F, cubicSpline2)
			.addPoint(0.25F, cubicSpline3)
			.addPoint(1.0F, cubicSpline4)
			.build();
	}

	public static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> overworldFactor(
		I toFloatFunction, I toFloatFunction2, I toFloatFunction3, I toFloatFunction4, boolean bl
	) {
		ToFloatFunction<Float> toFloatFunction5 = bl ? AMPLIFIED_FACTOR : NO_TRANSFORM;
		return CubicSpline.<C, I>builder(toFloatFunction, NO_TRANSFORM)
			.addPoint(-0.19F, 3.95F)
			.addPoint(-0.15F, getErosionFactor(toFloatFunction2, toFloatFunction3, toFloatFunction4, 6.25F, true, NO_TRANSFORM))
			.addPoint(-0.1F, getErosionFactor(toFloatFunction2, toFloatFunction3, toFloatFunction4, 5.47F, true, toFloatFunction5))
			.addPoint(0.03F, getErosionFactor(toFloatFunction2, toFloatFunction3, toFloatFunction4, 5.08F, true, toFloatFunction5))
			.addPoint(0.06F, getErosionFactor(toFloatFunction2, toFloatFunction3, toFloatFunction4, 4.69F, false, toFloatFunction5))
			.build();
	}

	public static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> overworldJaggedness(
		I toFloatFunction, I toFloatFunction2, I toFloatFunction3, I toFloatFunction4, boolean bl
	) {
		ToFloatFunction<Float> toFloatFunction5 = bl ? AMPLIFIED_JAGGEDNESS : NO_TRANSFORM;
		float f = 0.65F;
		return CubicSpline.<C, I>builder(toFloatFunction, toFloatFunction5)
			.addPoint(-0.11F, 0.0F)
			.addPoint(0.03F, buildErosionJaggednessSpline(toFloatFunction2, toFloatFunction3, toFloatFunction4, 1.0F, 0.5F, 0.0F, 0.0F, toFloatFunction5))
			.addPoint(0.65F, buildErosionJaggednessSpline(toFloatFunction2, toFloatFunction3, toFloatFunction4, 1.0F, 1.0F, 1.0F, 0.0F, toFloatFunction5))
			.build();
	}

	private static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> buildErosionJaggednessSpline(
		I toFloatFunction, I toFloatFunction2, I toFloatFunction3, float f, float g, float h, float i, ToFloatFunction<Float> toFloatFunction4
	) {
		float j = -0.5775F;
		CubicSpline<C, I> cubicSpline = buildRidgeJaggednessSpline(toFloatFunction2, toFloatFunction3, f, h, toFloatFunction4);
		CubicSpline<C, I> cubicSpline2 = buildRidgeJaggednessSpline(toFloatFunction2, toFloatFunction3, g, i, toFloatFunction4);
		return CubicSpline.<C, I>builder(toFloatFunction, toFloatFunction4)
			.addPoint(-1.0F, cubicSpline)
			.addPoint(-0.78F, cubicSpline2)
			.addPoint(-0.5775F, cubicSpline2)
			.addPoint(-0.375F, 0.0F)
			.build();
	}

	private static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> buildRidgeJaggednessSpline(
		I toFloatFunction, I toFloatFunction2, float f, float g, ToFloatFunction<Float> toFloatFunction3
	) {
		float h = NoiseRouterData.peaksAndValleys(0.4F);
		float i = NoiseRouterData.peaksAndValleys(0.56666666F);
		float j = (h + i) / 2.0F;
		CubicSpline.Builder<C, I> builder = CubicSpline.builder(toFloatFunction2, toFloatFunction3);
		builder.addPoint(h, 0.0F);
		if (g > 0.0F) {
			builder.addPoint(j, buildWeirdnessJaggednessSpline(toFloatFunction, g, toFloatFunction3));
		} else {
			builder.addPoint(j, 0.0F);
		}

		if (f > 0.0F) {
			builder.addPoint(1.0F, buildWeirdnessJaggednessSpline(toFloatFunction, f, toFloatFunction3));
		} else {
			builder.addPoint(1.0F, 0.0F);
		}

		return builder.build();
	}

	private static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> buildWeirdnessJaggednessSpline(
		I toFloatFunction, float f, ToFloatFunction<Float> toFloatFunction2
	) {
		float g = 0.63F * f;
		float h = 0.3F * f;
		return CubicSpline.<C, I>builder(toFloatFunction, toFloatFunction2).addPoint(-0.01F, g).addPoint(0.01F, h).build();
	}

	private static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> getErosionFactor(
		I toFloatFunction, I toFloatFunction2, I toFloatFunction3, float f, boolean bl, ToFloatFunction<Float> toFloatFunction4
	) {
		CubicSpline<C, I> cubicSpline = CubicSpline.<C, I>builder(toFloatFunction2, toFloatFunction4).addPoint(-0.2F, 6.3F).addPoint(0.2F, f).build();
		CubicSpline.Builder<C, I> builder = CubicSpline.<C, I>builder(toFloatFunction, toFloatFunction4)
			.addPoint(-0.6F, cubicSpline)
			.addPoint(-0.5F, CubicSpline.<C, I>builder(toFloatFunction2, toFloatFunction4).addPoint(-0.05F, 6.3F).addPoint(0.05F, 2.67F).build())
			.addPoint(-0.35F, cubicSpline)
			.addPoint(-0.25F, cubicSpline)
			.addPoint(-0.1F, CubicSpline.<C, I>builder(toFloatFunction2, toFloatFunction4).addPoint(-0.05F, 2.67F).addPoint(0.05F, 6.3F).build())
			.addPoint(0.03F, cubicSpline);
		if (bl) {
			CubicSpline<C, I> cubicSpline2 = CubicSpline.<C, I>builder(toFloatFunction2, toFloatFunction4).addPoint(0.0F, f).addPoint(0.1F, 0.625F).build();
			CubicSpline<C, I> cubicSpline3 = CubicSpline.<C, I>builder(toFloatFunction3, toFloatFunction4).addPoint(-0.9F, f).addPoint(-0.69F, cubicSpline2).build();
			builder.addPoint(0.35F, f).addPoint(0.45F, cubicSpline3).addPoint(0.55F, cubicSpline3).addPoint(0.62F, f);
		} else {
			CubicSpline<C, I> cubicSpline2 = CubicSpline.<C, I>builder(toFloatFunction3, toFloatFunction4).addPoint(-0.7F, cubicSpline).addPoint(-0.15F, 1.37F).build();
			CubicSpline<C, I> cubicSpline3 = CubicSpline.<C, I>builder(toFloatFunction3, toFloatFunction4).addPoint(0.45F, cubicSpline).addPoint(0.7F, 1.56F).build();
			builder.addPoint(0.05F, cubicSpline3).addPoint(0.4F, cubicSpline3).addPoint(0.45F, cubicSpline2).addPoint(0.55F, cubicSpline2).addPoint(0.58F, f);
		}

		return builder.build();
	}

	private static float calculateSlope(float f, float g, float h, float i) {
		return (g - f) / (i - h);
	}

	private static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> buildMountainRidgeSplineWithPoints(
		I toFloatFunction, float f, boolean bl, ToFloatFunction<Float> toFloatFunction2
	) {
		CubicSpline.Builder<C, I> builder = CubicSpline.builder(toFloatFunction, toFloatFunction2);
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
			builder.addPoint(-0.75F, p);
			builder.addPoint(-0.65F, n);
			float r = mountainContinentalness(l, f, -0.7F);
			float s = calculateSlope(r, k, l, 1.0F);
			float t = 0.01F;
			builder.addPoint(l - 0.01F, r);
			builder.addPoint(l, r, s);
			builder.addPoint(1.0F, k, s);
		} else {
			float n = calculateSlope(i, k, -1.0F, 1.0F);
			if (bl) {
				builder.addPoint(-1.0F, Math.max(0.2F, i));
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

	public static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> buildErosionOffsetSpline(
		I toFloatFunction, I toFloatFunction2, float f, float g, float h, float i, float j, float k, boolean bl, boolean bl2, ToFloatFunction<Float> toFloatFunction3
	) {
		float l = 0.6F;
		float m = 0.5F;
		float n = 0.5F;
		CubicSpline<C, I> cubicSpline = buildMountainRidgeSplineWithPoints(toFloatFunction2, Mth.lerp(i, 0.6F, 1.5F), bl2, toFloatFunction3);
		CubicSpline<C, I> cubicSpline2 = buildMountainRidgeSplineWithPoints(toFloatFunction2, Mth.lerp(i, 0.6F, 1.0F), bl2, toFloatFunction3);
		CubicSpline<C, I> cubicSpline3 = buildMountainRidgeSplineWithPoints(toFloatFunction2, i, bl2, toFloatFunction3);
		CubicSpline<C, I> cubicSpline4 = ridgeSpline(
			toFloatFunction2, f - 0.15F, 0.5F * i, Mth.lerp(0.5F, 0.5F, 0.5F) * i, 0.5F * i, 0.6F * i, 0.5F, toFloatFunction3
		);
		CubicSpline<C, I> cubicSpline5 = ridgeSpline(toFloatFunction2, f, j * i, g * i, 0.5F * i, 0.6F * i, 0.5F, toFloatFunction3);
		CubicSpline<C, I> cubicSpline6 = ridgeSpline(toFloatFunction2, f, j, j, g, h, 0.5F, toFloatFunction3);
		CubicSpline<C, I> cubicSpline7 = ridgeSpline(toFloatFunction2, f, j, j, g, h, 0.5F, toFloatFunction3);
		CubicSpline<C, I> cubicSpline8 = CubicSpline.<C, I>builder(toFloatFunction2, toFloatFunction3)
			.addPoint(-1.0F, f)
			.addPoint(-0.4F, cubicSpline6)
			.addPoint(0.0F, h + 0.07F)
			.build();
		CubicSpline<C, I> cubicSpline9 = ridgeSpline(toFloatFunction2, -0.02F, k, k, g, h, 0.0F, toFloatFunction3);
		CubicSpline.Builder<C, I> builder = CubicSpline.<C, I>builder(toFloatFunction, toFloatFunction3)
			.addPoint(-0.85F, cubicSpline)
			.addPoint(-0.7F, cubicSpline2)
			.addPoint(-0.4F, cubicSpline3)
			.addPoint(-0.35F, cubicSpline4)
			.addPoint(-0.1F, cubicSpline5)
			.addPoint(0.2F, cubicSpline6);
		if (bl) {
			builder.addPoint(0.4F, cubicSpline7).addPoint(0.45F, cubicSpline8).addPoint(0.55F, cubicSpline8).addPoint(0.58F, cubicSpline7);
		}

		builder.addPoint(0.7F, cubicSpline9);
		return builder.build();
	}

	private static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> ridgeSpline(
		I toFloatFunction, float f, float g, float h, float i, float j, float k, ToFloatFunction<Float> toFloatFunction2
	) {
		float l = Math.max(0.5F * (g - f), k);
		float m = 5.0F * (h - g);
		return CubicSpline.<C, I>builder(toFloatFunction, toFloatFunction2)
			.addPoint(-1.0F, f, l)
			.addPoint(-0.4F, g, Math.min(l, m))
			.addPoint(0.0F, h, m)
			.addPoint(0.4F, i, 2.0F * (i - h))
			.addPoint(1.0F, j, 0.7F * (j - i))
			.build();
	}
}
