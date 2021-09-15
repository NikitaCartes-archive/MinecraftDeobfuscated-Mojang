/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import com.mojang.datafixers.util.Pair;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.CubicSpline;
import net.minecraft.util.Mth;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;

public final class TerrainShaper {
    private static final float GLOBAL_OFFSET = 0.015f;
    static final ToFloatFunction<Point> CONTINENTS_EXTRACTOR = new ToFloatFunction<Point>(){

        @Override
        public float apply(Point point) {
            return point.continents;
        }

        public String toString() {
            return "continents";
        }

        @Override
        public /* synthetic */ float apply(Object object) {
            return this.apply((Point)object);
        }
    };
    static final ToFloatFunction<Point> EROSION_EXTRACTOR = new ToFloatFunction<Point>(){

        @Override
        public float apply(Point point) {
            return point.erosion;
        }

        public String toString() {
            return "erosion";
        }

        @Override
        public /* synthetic */ float apply(Object object) {
            return this.apply((Point)object);
        }
    };
    static final ToFloatFunction<Point> WEIRDNESS_EXTRACTOR = new ToFloatFunction<Point>(){

        @Override
        public float apply(Point point) {
            return point.weirdness;
        }

        public String toString() {
            return "weirdness";
        }

        @Override
        public /* synthetic */ float apply(Object object) {
            return this.apply((Point)object);
        }
    };
    static final ToFloatFunction<Point> RIDGES_EXTRACTOR = new ToFloatFunction<Point>(){

        @Override
        public float apply(Point point) {
            return point.ridges;
        }

        public String toString() {
            return "ridges";
        }

        @Override
        public /* synthetic */ float apply(Object object) {
            return this.apply((Point)object);
        }
    };
    @VisibleForDebug
    public CubicSpline<Point> offsetSampler;
    @VisibleForDebug
    public CubicSpline<Point> factorSampler;
    @VisibleForDebug
    public CubicSpline<Point> peakNoiseBlockAmplitudeSampler;

    public TerrainShaper() {
        CubicSpline<Point> cubicSpline = TerrainShaper.buildErosionOffsetSpline(-0.15f, 0.0f, 0.0f, 0.1f, 0.0f, -0.03f, false, false);
        CubicSpline<Point> cubicSpline2 = TerrainShaper.buildErosionOffsetSpline(-0.1f, 0.03f, 0.1f, 0.1f, 0.01f, -0.03f, false, false);
        CubicSpline<Point> cubicSpline3 = TerrainShaper.buildErosionOffsetSpline(-0.1f, 0.03f, 0.1f, 0.7f, 0.01f, -0.03f, true, true);
        CubicSpline<Point> cubicSpline4 = TerrainShaper.buildErosionOffsetSpline(-0.05f, 0.03f, 0.1f, 1.0f, 0.01f, 0.01f, true, true);
        float f = -0.51f;
        float g = -0.4f;
        float h = 0.1f;
        float i = -0.15f;
        this.offsetSampler = CubicSpline.builder(CONTINENTS_EXTRACTOR).addPoint(-1.1f, 0.044f, 0.0f).addPoint(-1.02f, -0.2222f, 0.0f).addPoint(-0.51f, -0.2222f, 0.0f).addPoint(-0.44f, -0.12f, 0.0f).addPoint(-0.18f, -0.12f, 0.0f).addPoint(-0.16f, cubicSpline, 0.0f).addPoint(-0.15f, cubicSpline, 0.0f).addPoint(-0.1f, cubicSpline2, 0.0f).addPoint(0.25f, cubicSpline3, 0.0f).addPoint(1.0f, cubicSpline4, 0.0f).build();
        this.factorSampler = CubicSpline.builder(CONTINENTS_EXTRACTOR).addPoint(-0.19f, 3.95f, 0.0f).addPoint(-0.15f, TerrainShaper.getErosionFactor(6.25f, true), 0.0f).addPoint(-0.1f, TerrainShaper.getErosionFactor(5.47f, true), 0.0f).addPoint(0.03f, TerrainShaper.getErosionFactor(5.08f, true), 0.0f).addPoint(0.06f, TerrainShaper.getErosionFactor(4.69f, false), 0.0f).build();
        this.peakNoiseBlockAmplitudeSampler = CubicSpline.builder(CONTINENTS_EXTRACTOR).addPoint(0.1f, 0.0f, 0.0f).addPoint(0.2f, CubicSpline.builder(EROSION_EXTRACTOR).addPoint(-0.8f, CubicSpline.builder(RIDGES_EXTRACTOR).addPoint(-1.0f, 0.0f, 0.0f).addPoint(0.2f, 0.0f, 0.0f).addPoint(1.0f, CubicSpline.builder(WEIRDNESS_EXTRACTOR).addPoint(-0.01f, 0.625f, 0.0f).addPoint(0.01f, 0.15625f, 0.0f).build(), 0.0f).build(), 0.0f).addPoint(-0.4f, 0.0f, 0.0f).build(), 0.0f).build();
    }

    private static CubicSpline<Point> getErosionFactor(float f, boolean bl) {
        CubicSpline<Point> cubicSpline = CubicSpline.builder(WEIRDNESS_EXTRACTOR).addPoint(-0.2f, 6.3f, 0.0f).addPoint(0.2f, f, 0.0f).build();
        CubicSpline.Builder<Point> builder = CubicSpline.builder(EROSION_EXTRACTOR).addPoint(-0.6f, cubicSpline, 0.0f).addPoint(-0.5f, CubicSpline.builder(WEIRDNESS_EXTRACTOR).addPoint(-0.05f, 6.3f, 0.0f).addPoint(0.05f, 2.67f, 0.0f).build(), 0.0f).addPoint(-0.35f, cubicSpline, 0.0f).addPoint(-0.25f, cubicSpline, 0.0f).addPoint(-0.1f, CubicSpline.builder(WEIRDNESS_EXTRACTOR).addPoint(-0.05f, 2.67f, 0.0f).addPoint(0.05f, 6.3f, 0.0f).build(), 0.0f).addPoint(0.03f, cubicSpline, 0.0f);
        if (bl) {
            CubicSpline<Point> cubicSpline2 = CubicSpline.builder(WEIRDNESS_EXTRACTOR).addPoint(0.0f, f, 0.0f).addPoint(0.1f, 0.625f, 0.0f).build();
            CubicSpline<Point> cubicSpline3 = CubicSpline.builder(RIDGES_EXTRACTOR).addPoint(-0.9f, f, 0.0f).addPoint(-0.69f, cubicSpline2, 0.0f).build();
            builder.addPoint(0.35f, f, 0.0f).addPoint(0.45f, cubicSpline3, 0.0f).addPoint(0.55f, cubicSpline3, 0.0f).addPoint(0.62f, f, 0.0f);
        } else {
            CubicSpline<Point> cubicSpline2 = CubicSpline.builder(RIDGES_EXTRACTOR).addPoint(-0.7f, cubicSpline, 0.0f).addPoint(-0.15f, 1.37f, 0.0f).build();
            CubicSpline<Point> cubicSpline3 = CubicSpline.builder(RIDGES_EXTRACTOR).addPoint(0.45f, cubicSpline, 0.0f).addPoint(0.7f, 1.56f, 0.0f).build();
            builder.addPoint(0.05f, cubicSpline3, 0.0f).addPoint(0.4f, cubicSpline3, 0.0f).addPoint(0.45f, cubicSpline2, 0.0f).addPoint(0.55f, cubicSpline2, 0.0f).addPoint(0.58f, f, 0.0f);
        }
        return builder.build();
    }

    private static float calculateSlope(float f, float g, float h, float i) {
        return (g - f) / (i - h);
    }

    private static CubicSpline<Point> buildMountainRidgeSplineWithPoints(float f, boolean bl) {
        CubicSpline.Builder<Point> builder = CubicSpline.builder(RIDGES_EXTRACTOR);
        float g = -0.7f;
        float h = -1.0f;
        float i = TerrainShaper.mountainContinentalness(-1.0f, f, -0.7f);
        float j = 1.0f;
        float k = TerrainShaper.mountainContinentalness(1.0f, f, -0.7f);
        float l = TerrainShaper.calculateMountainRidgeZeroContinentalnessPoint(f);
        float m = -0.65f;
        if (-0.65f < l && l < 1.0f) {
            float n = TerrainShaper.mountainContinentalness(-0.65f, f, -0.7f);
            float o = -0.75f;
            float p = TerrainShaper.mountainContinentalness(-0.75f, f, -0.7f);
            float q = TerrainShaper.calculateSlope(i, p, -1.0f, -0.75f);
            builder.addPoint(-1.0f, i, q);
            builder.addPoint(-0.75f, p, 0.0f);
            builder.addPoint(-0.65f, n, 0.0f);
            float r = TerrainShaper.mountainContinentalness(l, f, -0.7f);
            float s = TerrainShaper.calculateSlope(r, k, l, 1.0f);
            float t = 0.01f;
            builder.addPoint(l - 0.01f, r, 0.0f);
            builder.addPoint(l, r, s);
            builder.addPoint(1.0f, k, s);
        } else {
            float n = TerrainShaper.calculateSlope(i, k, -1.0f, 1.0f);
            if (bl) {
                builder.addPoint(-1.0f, Math.max(0.2f, i), 0.0f);
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

    private static CubicSpline<Point> buildErosionOffsetSpline(float f, float g, float h, float i, float j, float k, boolean bl, boolean bl2) {
        float l = 0.6f;
        float m = 0.5f;
        float n = 0.5f;
        CubicSpline<Point> cubicSpline = TerrainShaper.buildMountainRidgeSplineWithPoints(Mth.lerp(i, 0.6f, 1.5f), bl2);
        CubicSpline<Point> cubicSpline2 = TerrainShaper.buildMountainRidgeSplineWithPoints(Mth.lerp(i, 0.6f, 1.0f), bl2);
        CubicSpline<Point> cubicSpline3 = TerrainShaper.buildMountainRidgeSplineWithPoints(i, bl2);
        CubicSpline<Point> cubicSpline4 = TerrainShaper.ridgeSpline(f - 0.15f, 0.5f * i, Mth.lerp(0.5f, 0.5f, 0.5f) * i, 0.5f * i, 0.6f * i, 0.5f);
        CubicSpline<Point> cubicSpline5 = TerrainShaper.ridgeSpline(f, j * i, g * i, 0.5f * i, 0.6f * i, 0.5f);
        CubicSpline<Point> cubicSpline6 = TerrainShaper.ridgeSpline(f, j, j, g, h, 0.5f);
        CubicSpline<Point> cubicSpline7 = TerrainShaper.ridgeSpline(f, j, j, g, h, 0.5f);
        CubicSpline<Point> cubicSpline8 = CubicSpline.builder(RIDGES_EXTRACTOR).addPoint(-1.0f, f, 0.0f).addPoint(-0.4f, cubicSpline6, 0.0f).addPoint(0.0f, h + 0.07f, 0.0f).build();
        CubicSpline<Point> cubicSpline9 = TerrainShaper.ridgeSpline(-0.02f, k, k, g, h, 0.0f);
        CubicSpline.Builder<Point> builder = CubicSpline.builder(EROSION_EXTRACTOR).addPoint(-0.85f, cubicSpline, 0.0f).addPoint(-0.7f, cubicSpline2, 0.0f).addPoint(-0.4f, cubicSpline3, 0.0f).addPoint(-0.35f, cubicSpline4, 0.0f).addPoint(-0.1f, cubicSpline5, 0.0f).addPoint(0.2f, cubicSpline6, 0.0f);
        if (bl) {
            builder.addPoint(0.4f, cubicSpline7, 0.0f).addPoint(0.45f, cubicSpline8, 0.0f).addPoint(0.55f, cubicSpline8, 0.0f).addPoint(0.58f, cubicSpline7, 0.0f);
        }
        builder.addPoint(0.7f, cubicSpline9, 0.0f);
        return builder.build();
    }

    private static CubicSpline<Point> ridgeSpline(float f, float g, float h, float i, float j, float k) {
        float l = Math.max(0.5f * (g - f), k);
        float m = 5.0f * (h - g);
        return CubicSpline.builder(RIDGES_EXTRACTOR).addPoint(-1.0f, f, l).addPoint(-0.4f, g, Math.min(l, m)).addPoint(0.0f, h, m).addPoint(0.4f, i, 2.0f * (i - h)).addPoint(1.0f, j, 0.7f * (j - i)).build();
    }

    public void addDebugBiomesToVisualizeSplinePoints(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer) {
        Climate.Parameter parameter = Climate.Parameter.span(-1.0f, 1.0f);
        consumer.accept(Pair.of(Climate.parameters(parameter, parameter, parameter, parameter, Climate.Parameter.point(0.0f), parameter, 0.01f), Biomes.PLAINS));
        CubicSpline<Point> cubicSpline = TerrainShaper.buildErosionOffsetSpline(-0.15f, 0.0f, 0.0f, 0.1f, 0.0f, -0.03f, false, false);
        ResourceKey<Biome> resourceKey = Biomes.DESERT;
        for (Float float_ : cubicSpline.debugLocations()) {
            consumer.accept(Pair.of(Climate.parameters(parameter, parameter, parameter, Climate.Parameter.point(float_.floatValue()), Climate.Parameter.point(0.0f), parameter, 0.0f), resourceKey));
            resourceKey = resourceKey == Biomes.DESERT ? Biomes.BADLANDS : Biomes.DESERT;
        }
        for (Float float_ : this.offsetSampler.debugLocations()) {
            consumer.accept(Pair.of(Climate.parameters(parameter, parameter, Climate.Parameter.point(float_.floatValue()), parameter, Climate.Parameter.point(0.0f), parameter, 0.0f), Biomes.SNOWY_TAIGA));
        }
    }

    @VisibleForDebug
    public CubicSpline<Point> offsetSampler() {
        return this.offsetSampler;
    }

    @VisibleForDebug
    public CubicSpline<Point> factorSampler() {
        return this.factorSampler;
    }

    public float offset(Point point) {
        return this.offsetSampler.apply(point) + 0.015f;
    }

    public float factor(Point point) {
        return this.factorSampler.apply(point);
    }

    public float peaks(Point point) {
        return this.peakNoiseBlockAmplitudeSampler.apply(point);
    }

    public Point makePoint(float f, float g, float h) {
        return new Point(f, g, TerrainShaper.peaksAndValleys(h), h);
    }

    public static float peaksAndValleys(float f) {
        return -(Math.abs(Math.abs(f) - 0.6666667f) - 0.33333334f) * 3.0f;
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

