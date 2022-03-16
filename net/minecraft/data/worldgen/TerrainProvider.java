/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.worldgen;

import net.minecraft.util.CubicSpline;
import net.minecraft.util.Mth;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.level.levelgen.NoiseRouterData;

public class TerrainProvider {
    private static final float DEEP_OCEAN_CONTINENTALNESS = -0.51f;
    private static final float OCEAN_CONTINENTALNESS = -0.4f;
    private static final float PLAINS_CONTINENTALNESS = 0.1f;
    private static final float BEACH_CONTINENTALNESS = -0.15f;
    private static final ToFloatFunction<Float> NO_TRANSFORM = ToFloatFunction.IDENTITY;
    private static final ToFloatFunction<Float> AMPLIFIED_OFFSET = ToFloatFunction.createUnlimited(f -> f < 0.0f ? f : f * 2.0f);
    private static final ToFloatFunction<Float> AMPLIFIED_FACTOR = ToFloatFunction.createUnlimited(f -> 1.25f - 6.25f / (f + 5.0f));
    private static final ToFloatFunction<Float> AMPLIFIED_JAGGEDNESS = ToFloatFunction.createUnlimited(f -> f * 2.0f);

    public static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> overworldOffset(I toFloatFunction, I toFloatFunction2, I toFloatFunction3, boolean bl) {
        ToFloatFunction<Float> toFloatFunction4 = bl ? AMPLIFIED_OFFSET : NO_TRANSFORM;
        CubicSpline<C, I> cubicSpline = TerrainProvider.buildErosionOffsetSpline(toFloatFunction2, toFloatFunction3, -0.15f, 0.0f, 0.0f, 0.1f, 0.0f, -0.03f, false, false, toFloatFunction4);
        CubicSpline<C, I> cubicSpline2 = TerrainProvider.buildErosionOffsetSpline(toFloatFunction2, toFloatFunction3, -0.1f, 0.03f, 0.1f, 0.1f, 0.01f, -0.03f, false, false, toFloatFunction4);
        CubicSpline<C, I> cubicSpline3 = TerrainProvider.buildErosionOffsetSpline(toFloatFunction2, toFloatFunction3, -0.1f, 0.03f, 0.1f, 0.7f, 0.01f, -0.03f, true, true, toFloatFunction4);
        CubicSpline<C, I> cubicSpline4 = TerrainProvider.buildErosionOffsetSpline(toFloatFunction2, toFloatFunction3, -0.05f, 0.03f, 0.1f, 1.0f, 0.01f, 0.01f, true, true, toFloatFunction4);
        return CubicSpline.builder(toFloatFunction, toFloatFunction4).addPoint(-1.1f, 0.044f).addPoint(-1.02f, -0.2222f).addPoint(-0.51f, -0.2222f).addPoint(-0.44f, -0.12f).addPoint(-0.18f, -0.12f).addPoint(-0.16f, cubicSpline).addPoint(-0.15f, cubicSpline).addPoint(-0.1f, cubicSpline2).addPoint(0.25f, cubicSpline3).addPoint(1.0f, cubicSpline4).build();
    }

    public static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> overworldFactor(I toFloatFunction, I toFloatFunction2, I toFloatFunction3, I toFloatFunction4, boolean bl) {
        ToFloatFunction<Float> toFloatFunction5 = bl ? AMPLIFIED_FACTOR : NO_TRANSFORM;
        return CubicSpline.builder(toFloatFunction, NO_TRANSFORM).addPoint(-0.19f, 3.95f).addPoint(-0.15f, TerrainProvider.getErosionFactor(toFloatFunction2, toFloatFunction3, toFloatFunction4, 6.25f, true, NO_TRANSFORM)).addPoint(-0.1f, TerrainProvider.getErosionFactor(toFloatFunction2, toFloatFunction3, toFloatFunction4, 5.47f, true, toFloatFunction5)).addPoint(0.03f, TerrainProvider.getErosionFactor(toFloatFunction2, toFloatFunction3, toFloatFunction4, 5.08f, true, toFloatFunction5)).addPoint(0.06f, TerrainProvider.getErosionFactor(toFloatFunction2, toFloatFunction3, toFloatFunction4, 4.69f, false, toFloatFunction5)).build();
    }

    public static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> overworldJaggedness(I toFloatFunction, I toFloatFunction2, I toFloatFunction3, I toFloatFunction4, boolean bl) {
        ToFloatFunction<Float> toFloatFunction5 = bl ? AMPLIFIED_JAGGEDNESS : NO_TRANSFORM;
        float f = 0.65f;
        return CubicSpline.builder(toFloatFunction, toFloatFunction5).addPoint(-0.11f, 0.0f).addPoint(0.03f, TerrainProvider.buildErosionJaggednessSpline(toFloatFunction2, toFloatFunction3, toFloatFunction4, 1.0f, 0.5f, 0.0f, 0.0f, toFloatFunction5)).addPoint(0.65f, TerrainProvider.buildErosionJaggednessSpline(toFloatFunction2, toFloatFunction3, toFloatFunction4, 1.0f, 1.0f, 1.0f, 0.0f, toFloatFunction5)).build();
    }

    private static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> buildErosionJaggednessSpline(I toFloatFunction, I toFloatFunction2, I toFloatFunction3, float f, float g, float h, float i, ToFloatFunction<Float> toFloatFunction4) {
        float j = -0.5775f;
        CubicSpline<C, I> cubicSpline = TerrainProvider.buildRidgeJaggednessSpline(toFloatFunction2, toFloatFunction3, f, h, toFloatFunction4);
        CubicSpline<C, I> cubicSpline2 = TerrainProvider.buildRidgeJaggednessSpline(toFloatFunction2, toFloatFunction3, g, i, toFloatFunction4);
        return CubicSpline.builder(toFloatFunction, toFloatFunction4).addPoint(-1.0f, cubicSpline).addPoint(-0.78f, cubicSpline2).addPoint(-0.5775f, cubicSpline2).addPoint(-0.375f, 0.0f).build();
    }

    private static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> buildRidgeJaggednessSpline(I toFloatFunction, I toFloatFunction2, float f, float g, ToFloatFunction<Float> toFloatFunction3) {
        float h = NoiseRouterData.peaksAndValleys(0.4f);
        float i = NoiseRouterData.peaksAndValleys(0.56666666f);
        float j = (h + i) / 2.0f;
        CubicSpline.Builder<C, I> builder = CubicSpline.builder(toFloatFunction2, toFloatFunction3);
        builder.addPoint(h, 0.0f);
        if (g > 0.0f) {
            builder.addPoint(j, TerrainProvider.buildWeirdnessJaggednessSpline(toFloatFunction, g, toFloatFunction3));
        } else {
            builder.addPoint(j, 0.0f);
        }
        if (f > 0.0f) {
            builder.addPoint(1.0f, TerrainProvider.buildWeirdnessJaggednessSpline(toFloatFunction, f, toFloatFunction3));
        } else {
            builder.addPoint(1.0f, 0.0f);
        }
        return builder.build();
    }

    private static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> buildWeirdnessJaggednessSpline(I toFloatFunction, float f, ToFloatFunction<Float> toFloatFunction2) {
        float g = 0.63f * f;
        float h = 0.3f * f;
        return CubicSpline.builder(toFloatFunction, toFloatFunction2).addPoint(-0.01f, g).addPoint(0.01f, h).build();
    }

    private static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> getErosionFactor(I toFloatFunction, I toFloatFunction2, I toFloatFunction3, float f, boolean bl, ToFloatFunction<Float> toFloatFunction4) {
        CubicSpline cubicSpline = CubicSpline.builder(toFloatFunction2, toFloatFunction4).addPoint(-0.2f, 6.3f).addPoint(0.2f, f).build();
        CubicSpline.Builder builder = CubicSpline.builder(toFloatFunction, toFloatFunction4).addPoint(-0.6f, cubicSpline).addPoint(-0.5f, CubicSpline.builder(toFloatFunction2, toFloatFunction4).addPoint(-0.05f, 6.3f).addPoint(0.05f, 2.67f).build()).addPoint(-0.35f, cubicSpline).addPoint(-0.25f, cubicSpline).addPoint(-0.1f, CubicSpline.builder(toFloatFunction2, toFloatFunction4).addPoint(-0.05f, 2.67f).addPoint(0.05f, 6.3f).build()).addPoint(0.03f, cubicSpline);
        if (bl) {
            CubicSpline cubicSpline2 = CubicSpline.builder(toFloatFunction2, toFloatFunction4).addPoint(0.0f, f).addPoint(0.1f, 0.625f).build();
            CubicSpline cubicSpline3 = CubicSpline.builder(toFloatFunction3, toFloatFunction4).addPoint(-0.9f, f).addPoint(-0.69f, cubicSpline2).build();
            builder.addPoint(0.35f, f).addPoint(0.45f, cubicSpline3).addPoint(0.55f, cubicSpline3).addPoint(0.62f, f);
        } else {
            CubicSpline cubicSpline2 = CubicSpline.builder(toFloatFunction3, toFloatFunction4).addPoint(-0.7f, cubicSpline).addPoint(-0.15f, 1.37f).build();
            CubicSpline cubicSpline3 = CubicSpline.builder(toFloatFunction3, toFloatFunction4).addPoint(0.45f, cubicSpline).addPoint(0.7f, 1.56f).build();
            builder.addPoint(0.05f, cubicSpline3).addPoint(0.4f, cubicSpline3).addPoint(0.45f, cubicSpline2).addPoint(0.55f, cubicSpline2).addPoint(0.58f, f);
        }
        return builder.build();
    }

    private static float calculateSlope(float f, float g, float h, float i) {
        return (g - f) / (i - h);
    }

    private static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> buildMountainRidgeSplineWithPoints(I toFloatFunction, float f, boolean bl, ToFloatFunction<Float> toFloatFunction2) {
        CubicSpline.Builder builder = CubicSpline.builder(toFloatFunction, toFloatFunction2);
        float g = -0.7f;
        float h = -1.0f;
        float i = TerrainProvider.mountainContinentalness(-1.0f, f, -0.7f);
        float j = 1.0f;
        float k = TerrainProvider.mountainContinentalness(1.0f, f, -0.7f);
        float l = TerrainProvider.calculateMountainRidgeZeroContinentalnessPoint(f);
        float m = -0.65f;
        if (-0.65f < l && l < 1.0f) {
            float n = TerrainProvider.mountainContinentalness(-0.65f, f, -0.7f);
            float o = -0.75f;
            float p = TerrainProvider.mountainContinentalness(-0.75f, f, -0.7f);
            float q = TerrainProvider.calculateSlope(i, p, -1.0f, -0.75f);
            builder.addPoint(-1.0f, i, q);
            builder.addPoint(-0.75f, p);
            builder.addPoint(-0.65f, n);
            float r = TerrainProvider.mountainContinentalness(l, f, -0.7f);
            float s = TerrainProvider.calculateSlope(r, k, l, 1.0f);
            float t = 0.01f;
            builder.addPoint(l - 0.01f, r);
            builder.addPoint(l, r, s);
            builder.addPoint(1.0f, k, s);
        } else {
            float n = TerrainProvider.calculateSlope(i, k, -1.0f, 1.0f);
            if (bl) {
                builder.addPoint(-1.0f, Math.max(0.2f, i));
                builder.addPoint(0.0f, Mth.lerp(0.5f, i, k), n);
            } else {
                builder.addPoint(-1.0f, i, n);
            }
            builder.addPoint(1.0f, k, n);
        }
        return builder.build();
    }

    private static float mountainContinentalness(float f, float g, float h) {
        float i = 1.17f;
        float j = 0.46082947f;
        float k = 1.0f - (1.0f - g) * 0.5f;
        float l = 0.5f * (1.0f - g);
        float m = (f + 1.17f) * 0.46082947f;
        float n = m * k - l;
        if (f < h) {
            return Math.max(n, -0.2222f);
        }
        return Math.max(n, 0.0f);
    }

    private static float calculateMountainRidgeZeroContinentalnessPoint(float f) {
        float g = 1.17f;
        float h = 0.46082947f;
        float i = 1.0f - (1.0f - f) * 0.5f;
        float j = 0.5f * (1.0f - f);
        return j / (0.46082947f * i) - 1.17f;
    }

    public static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> buildErosionOffsetSpline(I toFloatFunction, I toFloatFunction2, float f, float g, float h, float i, float j, float k, boolean bl, boolean bl2, ToFloatFunction<Float> toFloatFunction3) {
        float l = 0.6f;
        float m = 0.5f;
        float n = 0.5f;
        CubicSpline<C, I> cubicSpline = TerrainProvider.buildMountainRidgeSplineWithPoints(toFloatFunction2, Mth.lerp(i, 0.6f, 1.5f), bl2, toFloatFunction3);
        CubicSpline<C, I> cubicSpline2 = TerrainProvider.buildMountainRidgeSplineWithPoints(toFloatFunction2, Mth.lerp(i, 0.6f, 1.0f), bl2, toFloatFunction3);
        CubicSpline<C, I> cubicSpline3 = TerrainProvider.buildMountainRidgeSplineWithPoints(toFloatFunction2, i, bl2, toFloatFunction3);
        CubicSpline<C, I> cubicSpline4 = TerrainProvider.ridgeSpline(toFloatFunction2, f - 0.15f, 0.5f * i, Mth.lerp(0.5f, 0.5f, 0.5f) * i, 0.5f * i, 0.6f * i, 0.5f, toFloatFunction3);
        CubicSpline<C, I> cubicSpline5 = TerrainProvider.ridgeSpline(toFloatFunction2, f, j * i, g * i, 0.5f * i, 0.6f * i, 0.5f, toFloatFunction3);
        CubicSpline<C, I> cubicSpline6 = TerrainProvider.ridgeSpline(toFloatFunction2, f, j, j, g, h, 0.5f, toFloatFunction3);
        CubicSpline<C, I> cubicSpline7 = TerrainProvider.ridgeSpline(toFloatFunction2, f, j, j, g, h, 0.5f, toFloatFunction3);
        CubicSpline cubicSpline8 = CubicSpline.builder(toFloatFunction2, toFloatFunction3).addPoint(-1.0f, f).addPoint(-0.4f, cubicSpline6).addPoint(0.0f, h + 0.07f).build();
        CubicSpline<C, I> cubicSpline9 = TerrainProvider.ridgeSpline(toFloatFunction2, -0.02f, k, k, g, h, 0.0f, toFloatFunction3);
        CubicSpline.Builder<C, I> builder = CubicSpline.builder(toFloatFunction, toFloatFunction3).addPoint(-0.85f, cubicSpline).addPoint(-0.7f, cubicSpline2).addPoint(-0.4f, cubicSpline3).addPoint(-0.35f, cubicSpline4).addPoint(-0.1f, cubicSpline5).addPoint(0.2f, cubicSpline6);
        if (bl) {
            builder.addPoint(0.4f, cubicSpline7).addPoint(0.45f, cubicSpline8).addPoint(0.55f, cubicSpline8).addPoint(0.58f, cubicSpline7);
        }
        builder.addPoint(0.7f, cubicSpline9);
        return builder.build();
    }

    private static <C, I extends ToFloatFunction<C>> CubicSpline<C, I> ridgeSpline(I toFloatFunction, float f, float g, float h, float i, float j, float k, ToFloatFunction<Float> toFloatFunction2) {
        float l = Math.max(0.5f * (g - f), k);
        float m = 5.0f * (h - g);
        return CubicSpline.builder(toFloatFunction, toFloatFunction2).addPoint(-1.0f, f, l).addPoint(-0.4f, g, Math.min(l, m)).addPoint(0.0f, h, m).addPoint(0.4f, i, 2.0f * (i - h)).addPoint(1.0f, j, 0.7f * (j - i)).build();
    }
}

