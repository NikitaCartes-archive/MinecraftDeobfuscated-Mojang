/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.biome.TerrainShaper;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseRouterWithOnlyNoises;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.OreVeinifier;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoiseRouterData {
    private static final float ORE_THICKNESS = 0.08f;
    private static final double VEININESS_FREQUENCY = 1.5;
    private static final double NOODLE_SPACING_AND_STRAIGHTNESS = 1.5;
    private static final double SURFACE_DENSITY_THRESHOLD = 1.5625;
    private static final DensityFunction BLENDING_FACTOR = DensityFunctions.constant(10.0);
    private static final DensityFunction BLENDING_JAGGEDNESS = DensityFunctions.zero();
    private static final ResourceKey<DensityFunction> ZERO = NoiseRouterData.createKey("zero");
    private static final ResourceKey<DensityFunction> Y = NoiseRouterData.createKey("y");
    private static final ResourceKey<DensityFunction> SHIFT_X = NoiseRouterData.createKey("shift_x");
    private static final ResourceKey<DensityFunction> SHIFT_Z = NoiseRouterData.createKey("shift_z");
    private static final ResourceKey<DensityFunction> BASE_3D_NOISE = NoiseRouterData.createKey("overworld/base_3d_noise");
    private static final ResourceKey<DensityFunction> CONTINENTS = NoiseRouterData.createKey("overworld/continents");
    private static final ResourceKey<DensityFunction> EROSION = NoiseRouterData.createKey("overworld/erosion");
    private static final ResourceKey<DensityFunction> RIDGES = NoiseRouterData.createKey("overworld/ridges");
    private static final ResourceKey<DensityFunction> FACTOR = NoiseRouterData.createKey("overworld/factor");
    private static final ResourceKey<DensityFunction> DEPTH = NoiseRouterData.createKey("overworld/depth");
    private static final ResourceKey<DensityFunction> SLOPED_CHEESE = NoiseRouterData.createKey("overworld/sloped_cheese");
    private static final ResourceKey<DensityFunction> CONTINENTS_LARGE = NoiseRouterData.createKey("overworld_large_biomes/continents");
    private static final ResourceKey<DensityFunction> EROSION_LARGE = NoiseRouterData.createKey("overworld_large_biomes/erosion");
    private static final ResourceKey<DensityFunction> FACTOR_LARGE = NoiseRouterData.createKey("overworld_large_biomes/factor");
    private static final ResourceKey<DensityFunction> DEPTH_LARGE = NoiseRouterData.createKey("overworld_large_biomes/depth");
    private static final ResourceKey<DensityFunction> SLOPED_CHEESE_LARGE = NoiseRouterData.createKey("overworld_large_biomes/sloped_cheese");
    private static final ResourceKey<DensityFunction> SLOPED_CHEESE_END = NoiseRouterData.createKey("end/sloped_cheese");
    private static final ResourceKey<DensityFunction> SPAGHETTI_ROUGHNESS_FUNCTION = NoiseRouterData.createKey("overworld/caves/spaghetti_roughness_function");
    private static final ResourceKey<DensityFunction> ENTRANCES = NoiseRouterData.createKey("overworld/caves/entrances");
    private static final ResourceKey<DensityFunction> NOODLE = NoiseRouterData.createKey("overworld/caves/noodle");
    private static final ResourceKey<DensityFunction> PILLARS = NoiseRouterData.createKey("overworld/caves/pillars");
    private static final ResourceKey<DensityFunction> SPAGHETTI_2D_THICKNESS_MODULATOR = NoiseRouterData.createKey("overworld/caves/spaghetti_2d_thickness_modulator");
    private static final ResourceKey<DensityFunction> SPAGHETTI_2D = NoiseRouterData.createKey("overworld/caves/spaghetti_2d");

    protected static NoiseRouterWithOnlyNoises overworld(NoiseSettings noiseSettings, boolean bl) {
        return NoiseRouterData.overworldWithNewCaves(noiseSettings, bl);
    }

    private static ResourceKey<DensityFunction> createKey(String string) {
        return ResourceKey.create(Registry.DENSITY_FUNCTION_REGISTRY, new ResourceLocation(string));
    }

    public static Holder<? extends DensityFunction> bootstrap() {
        NoiseRouterData.register(ZERO, DensityFunctions.zero());
        int i = DimensionType.MIN_Y * 2;
        int j = DimensionType.MAX_Y * 2;
        NoiseRouterData.register(Y, DensityFunctions.yClampedGradient(i, j, i, j));
        DensityFunction densityFunction = NoiseRouterData.register(SHIFT_X, DensityFunctions.flatCache(DensityFunctions.cache2d(DensityFunctions.shiftA(NoiseRouterData.getNoise(Noises.SHIFT)))));
        DensityFunction densityFunction2 = NoiseRouterData.register(SHIFT_Z, DensityFunctions.flatCache(DensityFunctions.cache2d(DensityFunctions.shiftB(NoiseRouterData.getNoise(Noises.SHIFT)))));
        NoiseRouterData.register(BASE_3D_NOISE, BlendedNoise.UNSEEDED);
        DensityFunction densityFunction3 = NoiseRouterData.register(CONTINENTS, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(densityFunction, densityFunction2, 0.25, NoiseRouterData.getNoise(Noises.CONTINENTALNESS))));
        DensityFunction densityFunction4 = NoiseRouterData.register(EROSION, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(densityFunction, densityFunction2, 0.25, NoiseRouterData.getNoise(Noises.EROSION))));
        DensityFunction densityFunction5 = NoiseRouterData.register(RIDGES, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(densityFunction, densityFunction2, 0.25, NoiseRouterData.getNoise(Noises.RIDGE))));
        DensityFunction densityFunction6 = DensityFunctions.noise(NoiseRouterData.getNoise(Noises.JAGGED), 1500.0, 0.0);
        DensityFunction densityFunction7 = NoiseRouterData.splineWithBlending(densityFunction3, densityFunction4, densityFunction5, DensityFunctions.TerrainShaperSpline.SplineType.OFFSET, -0.81, 2.5, DensityFunctions.blendOffset());
        DensityFunction densityFunction8 = NoiseRouterData.register(FACTOR, NoiseRouterData.splineWithBlending(densityFunction3, densityFunction4, densityFunction5, DensityFunctions.TerrainShaperSpline.SplineType.FACTOR, 0.0, 8.0, BLENDING_FACTOR));
        DensityFunction densityFunction9 = NoiseRouterData.register(DEPTH, DensityFunctions.add(DensityFunctions.yClampedGradient(-64, 320, 1.5, -1.5), densityFunction7));
        NoiseRouterData.register(SLOPED_CHEESE, NoiseRouterData.slopedCheese(densityFunction3, densityFunction4, densityFunction5, densityFunction8, densityFunction9, densityFunction6));
        DensityFunction densityFunction10 = NoiseRouterData.register(CONTINENTS_LARGE, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(densityFunction, densityFunction2, 0.25, NoiseRouterData.getNoise(Noises.CONTINENTALNESS_LARGE))));
        DensityFunction densityFunction11 = NoiseRouterData.register(EROSION_LARGE, DensityFunctions.flatCache(DensityFunctions.shiftedNoise2d(densityFunction, densityFunction2, 0.25, NoiseRouterData.getNoise(Noises.EROSION_LARGE))));
        DensityFunction densityFunction12 = NoiseRouterData.splineWithBlending(densityFunction10, densityFunction11, densityFunction5, DensityFunctions.TerrainShaperSpline.SplineType.OFFSET, -0.81, 2.5, DensityFunctions.blendOffset());
        DensityFunction densityFunction13 = NoiseRouterData.register(FACTOR_LARGE, NoiseRouterData.splineWithBlending(densityFunction10, densityFunction11, densityFunction5, DensityFunctions.TerrainShaperSpline.SplineType.FACTOR, 0.0, 8.0, BLENDING_FACTOR));
        DensityFunction densityFunction14 = NoiseRouterData.register(DEPTH_LARGE, DensityFunctions.add(DensityFunctions.yClampedGradient(-64, 320, 1.5, -1.5), densityFunction12));
        NoiseRouterData.register(SLOPED_CHEESE_LARGE, NoiseRouterData.slopedCheese(densityFunction10, densityFunction11, densityFunction5, densityFunction13, densityFunction14, densityFunction6));
        NoiseRouterData.register(SLOPED_CHEESE_END, DensityFunctions.add(DensityFunctions.endIslands(0L), NoiseRouterData.getFunction(BASE_3D_NOISE)));
        NoiseRouterData.register(SPAGHETTI_ROUGHNESS_FUNCTION, NoiseRouterData.spaghettiRoughnessFunction());
        NoiseRouterData.register(SPAGHETTI_2D_THICKNESS_MODULATOR, DensityFunctions.cacheOnce(DensityFunctions.mappedNoise(NoiseRouterData.getNoise(Noises.SPAGHETTI_2D_THICKNESS), 2.0, 1.0, -0.6, -1.3)));
        NoiseRouterData.register(SPAGHETTI_2D, NoiseRouterData.spaghetti2D());
        NoiseRouterData.register(ENTRANCES, NoiseRouterData.entrances());
        NoiseRouterData.register(NOODLE, NoiseRouterData.noodle());
        NoiseRouterData.register(PILLARS, NoiseRouterData.pillars());
        return (Holder)BuiltinRegistries.DENSITY_FUNCTION.holders().iterator().next();
    }

    private static DensityFunction register(ResourceKey<DensityFunction> resourceKey, DensityFunction densityFunction) {
        return new DensityFunctions.HolderHolder(BuiltinRegistries.register(BuiltinRegistries.DENSITY_FUNCTION, resourceKey, densityFunction));
    }

    private static Holder<NormalNoise.NoiseParameters> getNoise(ResourceKey<NormalNoise.NoiseParameters> resourceKey) {
        return BuiltinRegistries.NOISE.getHolderOrThrow(resourceKey);
    }

    private static DensityFunction getFunction(ResourceKey<DensityFunction> resourceKey) {
        return new DensityFunctions.HolderHolder(BuiltinRegistries.DENSITY_FUNCTION.getHolderOrThrow(resourceKey));
    }

    private static DensityFunction slopedCheese(DensityFunction densityFunction, DensityFunction densityFunction2, DensityFunction densityFunction3, DensityFunction densityFunction4, DensityFunction densityFunction5, DensityFunction densityFunction6) {
        DensityFunction densityFunction7 = NoiseRouterData.splineWithBlending(densityFunction, densityFunction2, densityFunction3, DensityFunctions.TerrainShaperSpline.SplineType.JAGGEDNESS, 0.0, 1.28, BLENDING_JAGGEDNESS);
        DensityFunction densityFunction8 = DensityFunctions.mul(densityFunction7, densityFunction6.halfNegative());
        DensityFunction densityFunction9 = NoiseRouterData.noiseGradientDensity(densityFunction4, DensityFunctions.add(densityFunction5, densityFunction8));
        return DensityFunctions.add(densityFunction9, NoiseRouterData.getFunction(BASE_3D_NOISE));
    }

    private static DensityFunction spaghettiRoughnessFunction() {
        DensityFunction densityFunction = DensityFunctions.noise(NoiseRouterData.getNoise(Noises.SPAGHETTI_ROUGHNESS));
        DensityFunction densityFunction2 = DensityFunctions.mappedNoise(NoiseRouterData.getNoise(Noises.SPAGHETTI_ROUGHNESS_MODULATOR), 0.0, -0.1);
        return DensityFunctions.cacheOnce(DensityFunctions.mul(densityFunction2, DensityFunctions.add(densityFunction.abs(), DensityFunctions.constant(-0.4))));
    }

    private static DensityFunction entrances() {
        DensityFunction densityFunction = DensityFunctions.cacheOnce(DensityFunctions.noise(NoiseRouterData.getNoise(Noises.SPAGHETTI_3D_RARITY), 2.0, 1.0));
        DensityFunction densityFunction2 = DensityFunctions.mappedNoise(NoiseRouterData.getNoise(Noises.SPAGHETTI_3D_THICKNESS), -0.065, -0.088);
        DensityFunction densityFunction3 = DensityFunctions.weirdScaledSampler(densityFunction, NoiseRouterData.getNoise(Noises.SPAGHETTI_3D_1), DensityFunctions.WeirdScaledSampler.RarityValueMapper.TYPE1);
        DensityFunction densityFunction4 = DensityFunctions.weirdScaledSampler(densityFunction, NoiseRouterData.getNoise(Noises.SPAGHETTI_3D_2), DensityFunctions.WeirdScaledSampler.RarityValueMapper.TYPE1);
        DensityFunction densityFunction5 = DensityFunctions.add(DensityFunctions.max(densityFunction3, densityFunction4), densityFunction2).clamp(-1.0, 1.0);
        DensityFunction densityFunction6 = NoiseRouterData.getFunction(SPAGHETTI_ROUGHNESS_FUNCTION);
        DensityFunction densityFunction7 = DensityFunctions.noise(NoiseRouterData.getNoise(Noises.CAVE_ENTRANCE), 0.75, 0.5);
        DensityFunction densityFunction8 = DensityFunctions.add(DensityFunctions.add(densityFunction7, DensityFunctions.constant(0.37)), DensityFunctions.yClampedGradient(-10, 30, 0.3, 0.0));
        return DensityFunctions.cacheOnce(DensityFunctions.min(densityFunction8, DensityFunctions.add(densityFunction6, densityFunction5)));
    }

    private static DensityFunction noodle() {
        DensityFunction densityFunction = NoiseRouterData.getFunction(Y);
        int i = -64;
        int j = -60;
        int k = 320;
        DensityFunction densityFunction2 = NoiseRouterData.yLimitedInterpolatable(densityFunction, DensityFunctions.noise(NoiseRouterData.getNoise(Noises.NOODLE), 1.0, 1.0), -60, 320, -1);
        DensityFunction densityFunction3 = NoiseRouterData.yLimitedInterpolatable(densityFunction, DensityFunctions.mappedNoise(NoiseRouterData.getNoise(Noises.NOODLE_THICKNESS), 1.0, 1.0, -0.05, -0.1), -60, 320, 0);
        double d = 2.6666666666666665;
        DensityFunction densityFunction4 = NoiseRouterData.yLimitedInterpolatable(densityFunction, DensityFunctions.noise(NoiseRouterData.getNoise(Noises.NOODLE_RIDGE_A), 2.6666666666666665, 2.6666666666666665), -60, 320, 0);
        DensityFunction densityFunction5 = NoiseRouterData.yLimitedInterpolatable(densityFunction, DensityFunctions.noise(NoiseRouterData.getNoise(Noises.NOODLE_RIDGE_B), 2.6666666666666665, 2.6666666666666665), -60, 320, 0);
        DensityFunction densityFunction6 = DensityFunctions.mul(DensityFunctions.constant(1.5), DensityFunctions.max(densityFunction4.abs(), densityFunction5.abs()));
        return DensityFunctions.rangeChoice(densityFunction2, -1000000.0, 0.0, DensityFunctions.constant(64.0), DensityFunctions.add(densityFunction3, densityFunction6));
    }

    private static DensityFunction pillars() {
        double d = 25.0;
        double e = 0.3;
        DensityFunction densityFunction = DensityFunctions.noise(NoiseRouterData.getNoise(Noises.PILLAR), 25.0, 0.3);
        DensityFunction densityFunction2 = DensityFunctions.mappedNoise(NoiseRouterData.getNoise(Noises.PILLAR_RARENESS), 0.0, -2.0);
        DensityFunction densityFunction3 = DensityFunctions.mappedNoise(NoiseRouterData.getNoise(Noises.PILLAR_THICKNESS), 0.0, 1.1);
        DensityFunction densityFunction4 = DensityFunctions.add(DensityFunctions.mul(densityFunction, DensityFunctions.constant(2.0)), densityFunction2);
        return DensityFunctions.cacheOnce(DensityFunctions.mul(densityFunction4, densityFunction3.cube()));
    }

    private static DensityFunction spaghetti2D() {
        DensityFunction densityFunction = DensityFunctions.noise(NoiseRouterData.getNoise(Noises.SPAGHETTI_2D_MODULATOR), 2.0, 1.0);
        DensityFunction densityFunction2 = DensityFunctions.weirdScaledSampler(densityFunction, NoiseRouterData.getNoise(Noises.SPAGHETTI_2D), DensityFunctions.WeirdScaledSampler.RarityValueMapper.TYPE2);
        DensityFunction densityFunction3 = DensityFunctions.mappedNoise(NoiseRouterData.getNoise(Noises.SPAGHETTI_2D_ELEVATION), 0.0, Math.floorDiv(-64, 8), 8.0);
        DensityFunction densityFunction4 = NoiseRouterData.getFunction(SPAGHETTI_2D_THICKNESS_MODULATOR);
        DensityFunction densityFunction5 = DensityFunctions.add(densityFunction3, DensityFunctions.yClampedGradient(-64, 320, 8.0, -40.0)).abs();
        DensityFunction densityFunction6 = DensityFunctions.add(densityFunction5, densityFunction4).cube();
        double d = 0.083;
        DensityFunction densityFunction7 = DensityFunctions.add(densityFunction2, DensityFunctions.mul(DensityFunctions.constant(0.083), densityFunction4));
        return DensityFunctions.max(densityFunction7, densityFunction6).clamp(-1.0, 1.0);
    }

    private static DensityFunction underground(DensityFunction densityFunction) {
        DensityFunction densityFunction2 = NoiseRouterData.getFunction(SPAGHETTI_2D);
        DensityFunction densityFunction3 = NoiseRouterData.getFunction(SPAGHETTI_ROUGHNESS_FUNCTION);
        DensityFunction densityFunction4 = DensityFunctions.noise(NoiseRouterData.getNoise(Noises.CAVE_LAYER), 8.0);
        DensityFunction densityFunction5 = DensityFunctions.mul(DensityFunctions.constant(4.0), densityFunction4.square());
        DensityFunction densityFunction6 = DensityFunctions.noise(NoiseRouterData.getNoise(Noises.CAVE_CHEESE), 0.6666666666666666);
        DensityFunction densityFunction7 = DensityFunctions.add(DensityFunctions.add(DensityFunctions.constant(0.27), densityFunction6).clamp(-1.0, 1.0), DensityFunctions.add(DensityFunctions.constant(1.5), DensityFunctions.mul(DensityFunctions.constant(-0.64), densityFunction)).clamp(0.0, 0.5));
        DensityFunction densityFunction8 = DensityFunctions.add(densityFunction5, densityFunction7);
        DensityFunction densityFunction9 = DensityFunctions.min(DensityFunctions.min(densityFunction8, NoiseRouterData.getFunction(ENTRANCES)), DensityFunctions.add(densityFunction2, densityFunction3));
        DensityFunction densityFunction10 = NoiseRouterData.getFunction(PILLARS);
        DensityFunction densityFunction11 = DensityFunctions.rangeChoice(densityFunction10, -1000000.0, 0.03, DensityFunctions.constant(-1000000.0), densityFunction10);
        return DensityFunctions.max(densityFunction9, densityFunction11);
    }

    private static DensityFunction postProcess(NoiseSettings noiseSettings, DensityFunction densityFunction) {
        DensityFunction densityFunction2 = DensityFunctions.slide(noiseSettings, densityFunction);
        DensityFunction densityFunction3 = DensityFunctions.blendDensity(densityFunction2);
        return DensityFunctions.mul(DensityFunctions.interpolated(densityFunction3), DensityFunctions.constant(0.64)).squeeze();
    }

    private static NoiseRouterWithOnlyNoises overworldWithNewCaves(NoiseSettings noiseSettings, boolean bl) {
        DensityFunction densityFunction = DensityFunctions.noise(NoiseRouterData.getNoise(Noises.AQUIFER_BARRIER), 0.5);
        DensityFunction densityFunction2 = DensityFunctions.noise(NoiseRouterData.getNoise(Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS), 0.67);
        DensityFunction densityFunction3 = DensityFunctions.noise(NoiseRouterData.getNoise(Noises.AQUIFER_FLUID_LEVEL_SPREAD), 0.7142857142857143);
        DensityFunction densityFunction4 = DensityFunctions.noise(NoiseRouterData.getNoise(Noises.AQUIFER_LAVA));
        DensityFunction densityFunction5 = NoiseRouterData.getFunction(SHIFT_X);
        DensityFunction densityFunction6 = NoiseRouterData.getFunction(SHIFT_Z);
        DensityFunction densityFunction7 = DensityFunctions.shiftedNoise2d(densityFunction5, densityFunction6, 0.25, NoiseRouterData.getNoise(bl ? Noises.TEMPERATURE_LARGE : Noises.TEMPERATURE));
        DensityFunction densityFunction8 = DensityFunctions.shiftedNoise2d(densityFunction5, densityFunction6, 0.25, NoiseRouterData.getNoise(bl ? Noises.VEGETATION_LARGE : Noises.VEGETATION));
        DensityFunction densityFunction9 = NoiseRouterData.getFunction(bl ? FACTOR_LARGE : FACTOR);
        DensityFunction densityFunction10 = NoiseRouterData.getFunction(bl ? DEPTH_LARGE : DEPTH);
        DensityFunction densityFunction11 = NoiseRouterData.noiseGradientDensity(DensityFunctions.cache2d(densityFunction9), densityFunction10);
        DensityFunction densityFunction12 = NoiseRouterData.getFunction(bl ? SLOPED_CHEESE_LARGE : SLOPED_CHEESE);
        DensityFunction densityFunction13 = DensityFunctions.min(densityFunction12, DensityFunctions.mul(DensityFunctions.constant(5.0), NoiseRouterData.getFunction(ENTRANCES)));
        DensityFunction densityFunction14 = DensityFunctions.rangeChoice(densityFunction12, -1000000.0, 1.5625, densityFunction13, NoiseRouterData.underground(densityFunction12));
        DensityFunction densityFunction15 = DensityFunctions.min(NoiseRouterData.postProcess(noiseSettings, densityFunction14), NoiseRouterData.getFunction(NOODLE));
        DensityFunction densityFunction16 = NoiseRouterData.getFunction(Y);
        int i = noiseSettings.minY();
        int j = Stream.of(OreVeinifier.VeinType.values()).mapToInt(veinType -> veinType.minY).min().orElse(i);
        int k = Stream.of(OreVeinifier.VeinType.values()).mapToInt(veinType -> veinType.maxY).max().orElse(i);
        DensityFunction densityFunction17 = NoiseRouterData.yLimitedInterpolatable(densityFunction16, DensityFunctions.noise(NoiseRouterData.getNoise(Noises.ORE_VEININESS), 1.5, 1.5), j, k, 0);
        float f = 4.0f;
        DensityFunction densityFunction18 = NoiseRouterData.yLimitedInterpolatable(densityFunction16, DensityFunctions.noise(NoiseRouterData.getNoise(Noises.ORE_VEIN_A), 4.0, 4.0), j, k, 0).abs();
        DensityFunction densityFunction19 = NoiseRouterData.yLimitedInterpolatable(densityFunction16, DensityFunctions.noise(NoiseRouterData.getNoise(Noises.ORE_VEIN_B), 4.0, 4.0), j, k, 0).abs();
        DensityFunction densityFunction20 = DensityFunctions.add(DensityFunctions.constant(-0.08f), DensityFunctions.max(densityFunction18, densityFunction19));
        DensityFunction densityFunction21 = DensityFunctions.noise(NoiseRouterData.getNoise(Noises.ORE_GAP));
        return new NoiseRouterWithOnlyNoises(densityFunction, densityFunction2, densityFunction3, densityFunction4, densityFunction7, densityFunction8, NoiseRouterData.getFunction(bl ? CONTINENTS_LARGE : CONTINENTS), NoiseRouterData.getFunction(bl ? EROSION_LARGE : EROSION), NoiseRouterData.getFunction(bl ? DEPTH_LARGE : DEPTH), NoiseRouterData.getFunction(RIDGES), densityFunction11, densityFunction15, densityFunction17, densityFunction20, densityFunction21);
    }

    private static NoiseRouterWithOnlyNoises noNewCaves(NoiseSettings noiseSettings) {
        DensityFunction densityFunction = NoiseRouterData.getFunction(SHIFT_X);
        DensityFunction densityFunction2 = NoiseRouterData.getFunction(SHIFT_Z);
        DensityFunction densityFunction3 = DensityFunctions.shiftedNoise2d(densityFunction, densityFunction2, 0.25, NoiseRouterData.getNoise(Noises.TEMPERATURE));
        DensityFunction densityFunction4 = DensityFunctions.shiftedNoise2d(densityFunction, densityFunction2, 0.25, NoiseRouterData.getNoise(Noises.VEGETATION));
        DensityFunction densityFunction5 = NoiseRouterData.noiseGradientDensity(DensityFunctions.cache2d(NoiseRouterData.getFunction(FACTOR)), NoiseRouterData.getFunction(DEPTH));
        DensityFunction densityFunction6 = NoiseRouterData.postProcess(noiseSettings, NoiseRouterData.getFunction(SLOPED_CHEESE));
        return new NoiseRouterWithOnlyNoises(DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), densityFunction3, densityFunction4, NoiseRouterData.getFunction(CONTINENTS), NoiseRouterData.getFunction(EROSION), NoiseRouterData.getFunction(DEPTH), NoiseRouterData.getFunction(RIDGES), densityFunction5, densityFunction6, DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero());
    }

    protected static NoiseRouterWithOnlyNoises overworldWithoutCaves(NoiseSettings noiseSettings) {
        return NoiseRouterData.noNewCaves(noiseSettings);
    }

    protected static NoiseRouterWithOnlyNoises nether(NoiseSettings noiseSettings) {
        return NoiseRouterData.noNewCaves(noiseSettings);
    }

    protected static NoiseRouterWithOnlyNoises end(NoiseSettings noiseSettings) {
        DensityFunction densityFunction = DensityFunctions.cache2d(DensityFunctions.endIslands(0L));
        DensityFunction densityFunction2 = NoiseRouterData.postProcess(noiseSettings, NoiseRouterData.getFunction(SLOPED_CHEESE_END));
        return new NoiseRouterWithOnlyNoises(DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero(), densityFunction, densityFunction2, DensityFunctions.zero(), DensityFunctions.zero(), DensityFunctions.zero());
    }

    private static NormalNoise seedNoise(PositionalRandomFactory positionalRandomFactory, Registry<NormalNoise.NoiseParameters> registry, Holder<NormalNoise.NoiseParameters> holder) {
        return Noises.instantiate(positionalRandomFactory, holder.unwrapKey().flatMap(registry::getHolder).orElse(holder));
    }

    public static NoiseRouter createNoiseRouter(NoiseSettings noiseSettings, long l, Registry<NormalNoise.NoiseParameters> registry, WorldgenRandom.Algorithm algorithm, NoiseRouterWithOnlyNoises noiseRouterWithOnlyNoises) {
        boolean bl = algorithm == WorldgenRandom.Algorithm.LEGACY;
        PositionalRandomFactory positionalRandomFactory = algorithm.newInstance(l).forkPositional();
        HashMap map = new HashMap();
        DensityFunction.Visitor visitor = densityFunction -> {
            if (densityFunction instanceof DensityFunctions.Noise) {
                DensityFunctions.Noise noise = (DensityFunctions.Noise)densityFunction;
                Holder<NormalNoise.NoiseParameters> holder = noise.noiseData();
                return new DensityFunctions.Noise(holder, NoiseRouterData.seedNoise(positionalRandomFactory, registry, holder), noise.xzScale(), noise.yScale());
            }
            if (densityFunction instanceof DensityFunctions.ShiftNoise) {
                DensityFunctions.ShiftNoise shiftNoise = (DensityFunctions.ShiftNoise)densityFunction;
                Holder<NormalNoise.NoiseParameters> holder2 = shiftNoise.noiseData();
                NormalNoise normalNoise = bl ? NormalNoise.create(positionalRandomFactory.fromHashOf(Noises.SHIFT.location()), new NormalNoise.NoiseParameters(0, 0.0, new double[0])) : NoiseRouterData.seedNoise(positionalRandomFactory, registry, holder2);
                return shiftNoise.withNewNoise(normalNoise);
            }
            if (densityFunction instanceof DensityFunctions.ShiftedNoise) {
                Holder<NormalNoise.NoiseParameters> holder;
                DensityFunctions.ShiftedNoise shiftedNoise = (DensityFunctions.ShiftedNoise)densityFunction;
                if (bl) {
                    holder = shiftedNoise.noiseData();
                    if (Objects.equals(holder.unwrapKey(), Optional.of(Noises.TEMPERATURE))) {
                        NormalNoise normalNoise2 = NormalNoise.createLegacyNetherBiome(algorithm.newInstance(l), new NormalNoise.NoiseParameters(-7, 1.0, 1.0));
                        return new DensityFunctions.ShiftedNoise(shiftedNoise.shiftX(), shiftedNoise.shiftY(), shiftedNoise.shiftZ(), shiftedNoise.xzScale(), shiftedNoise.yScale(), holder, normalNoise2);
                    }
                    if (Objects.equals(holder.unwrapKey(), Optional.of(Noises.VEGETATION))) {
                        NormalNoise normalNoise2 = NormalNoise.createLegacyNetherBiome(algorithm.newInstance(l + 1L), new NormalNoise.NoiseParameters(-7, 1.0, 1.0));
                        return new DensityFunctions.ShiftedNoise(shiftedNoise.shiftX(), shiftedNoise.shiftY(), shiftedNoise.shiftZ(), shiftedNoise.xzScale(), shiftedNoise.yScale(), holder, normalNoise2);
                    }
                }
                holder = shiftedNoise.noiseData();
                return new DensityFunctions.ShiftedNoise(shiftedNoise.shiftX(), shiftedNoise.shiftY(), shiftedNoise.shiftZ(), shiftedNoise.xzScale(), shiftedNoise.yScale(), holder, NoiseRouterData.seedNoise(positionalRandomFactory, registry, holder));
            }
            if (densityFunction instanceof DensityFunctions.WeirdScaledSampler) {
                DensityFunctions.WeirdScaledSampler weirdScaledSampler = (DensityFunctions.WeirdScaledSampler)densityFunction;
                return new DensityFunctions.WeirdScaledSampler(weirdScaledSampler.input(), weirdScaledSampler.noiseData(), NoiseRouterData.seedNoise(positionalRandomFactory, registry, weirdScaledSampler.noiseData()), weirdScaledSampler.rarityValueMapper());
            }
            if (densityFunction instanceof BlendedNoise) {
                if (bl) {
                    return new BlendedNoise(algorithm.newInstance(l), noiseSettings.noiseSamplingSettings(), noiseSettings.getCellWidth(), noiseSettings.getCellHeight());
                }
                return new BlendedNoise(positionalRandomFactory.fromHashOf(new ResourceLocation("terrain")), noiseSettings.noiseSamplingSettings(), noiseSettings.getCellWidth(), noiseSettings.getCellHeight());
            }
            if (densityFunction instanceof DensityFunctions.EndIslandDensityFunction) {
                return new DensityFunctions.EndIslandDensityFunction(l);
            }
            if (densityFunction instanceof DensityFunctions.TerrainShaperSpline) {
                DensityFunctions.TerrainShaperSpline terrainShaperSpline = (DensityFunctions.TerrainShaperSpline)densityFunction;
                TerrainShaper terrainShaper = noiseSettings.terrainShaper();
                return new DensityFunctions.TerrainShaperSpline(terrainShaperSpline.continentalness(), terrainShaperSpline.erosion(), terrainShaperSpline.weirdness(), terrainShaper, terrainShaperSpline.spline(), terrainShaperSpline.minValue(), terrainShaperSpline.maxValue());
            }
            if (densityFunction instanceof DensityFunctions.Slide) {
                DensityFunctions.Slide slide = (DensityFunctions.Slide)densityFunction;
                return new DensityFunctions.Slide(noiseSettings, slide.input());
            }
            return densityFunction;
        };
        DensityFunction.Visitor visitor2 = densityFunction -> map.computeIfAbsent(densityFunction, visitor);
        NoiseRouterWithOnlyNoises noiseRouterWithOnlyNoises2 = noiseRouterWithOnlyNoises.mapAll(visitor2);
        PositionalRandomFactory positionalRandomFactory2 = positionalRandomFactory.fromHashOf(new ResourceLocation("aquifer")).forkPositional();
        PositionalRandomFactory positionalRandomFactory3 = positionalRandomFactory.fromHashOf(new ResourceLocation("ore")).forkPositional();
        return new NoiseRouter(noiseRouterWithOnlyNoises2.barrierNoise(), noiseRouterWithOnlyNoises2.fluidLevelFloodednessNoise(), noiseRouterWithOnlyNoises2.fluidLevelSpreadNoise(), noiseRouterWithOnlyNoises2.lavaNoise(), positionalRandomFactory2, positionalRandomFactory3, noiseRouterWithOnlyNoises2.temperature(), noiseRouterWithOnlyNoises2.vegetation(), noiseRouterWithOnlyNoises2.continents(), noiseRouterWithOnlyNoises2.erosion(), noiseRouterWithOnlyNoises2.depth(), noiseRouterWithOnlyNoises2.ridges(), noiseRouterWithOnlyNoises2.initialDensityWithoutJaggedness(), noiseRouterWithOnlyNoises2.finalDensity(), noiseRouterWithOnlyNoises2.veinToggle(), noiseRouterWithOnlyNoises2.veinRidged(), noiseRouterWithOnlyNoises2.veinGap(), new OverworldBiomeBuilder().spawnTarget());
    }

    private static DensityFunction splineWithBlending(DensityFunction densityFunction, DensityFunction densityFunction2, DensityFunction densityFunction3, DensityFunctions.TerrainShaperSpline.SplineType splineType, double d, double e, DensityFunction densityFunction4) {
        DensityFunction densityFunction5 = DensityFunctions.terrainShaperSpline(densityFunction, densityFunction2, densityFunction3, splineType, d, e);
        DensityFunction densityFunction6 = DensityFunctions.lerp(DensityFunctions.blendAlpha(), densityFunction4, densityFunction5);
        return DensityFunctions.flatCache(DensityFunctions.cache2d(densityFunction6));
    }

    private static DensityFunction noiseGradientDensity(DensityFunction densityFunction, DensityFunction densityFunction2) {
        DensityFunction densityFunction3 = DensityFunctions.mul(densityFunction2, densityFunction);
        return DensityFunctions.mul(DensityFunctions.constant(4.0), densityFunction3.quarterNegative());
    }

    private static DensityFunction yLimitedInterpolatable(DensityFunction densityFunction, DensityFunction densityFunction2, int i, int j, int k) {
        return DensityFunctions.interpolated(DensityFunctions.rangeChoice(densityFunction, i, j + 1, densityFunction2, DensityFunctions.constant(k)));
    }

    protected static double applySlide(NoiseSettings noiseSettings, double d, double e) {
        double f = (int)e / noiseSettings.getCellHeight() - noiseSettings.getMinCellY();
        d = noiseSettings.topSlideSettings().applySlide(d, (double)noiseSettings.getCellCountY() - f);
        d = noiseSettings.bottomSlideSettings().applySlide(d, f);
        return d;
    }

    protected static double computePreliminarySurfaceLevelScanning(NoiseSettings noiseSettings, DensityFunction densityFunction, int i, int j) {
        for (int k = noiseSettings.getMinCellY() + noiseSettings.getCellCountY(); k >= noiseSettings.getMinCellY(); --k) {
            int l = k * noiseSettings.getCellHeight();
            double d = -0.703125;
            double e = densityFunction.compute(new DensityFunction.SinglePointContext(i, l, j)) + -0.703125;
            double f = Mth.clamp(e, -64.0, 64.0);
            if (!((f = NoiseRouterData.applySlide(noiseSettings, f, l)) > 0.390625)) continue;
            return l;
        }
        return 2.147483647E9;
    }

    protected static final class QuantizedSpaghettiRarity {
        protected QuantizedSpaghettiRarity() {
        }

        protected static double getSphaghettiRarity2D(double d) {
            if (d < -0.75) {
                return 0.5;
            }
            if (d < -0.5) {
                return 0.75;
            }
            if (d < 0.5) {
                return 1.0;
            }
            if (d < 0.75) {
                return 2.0;
            }
            return 3.0;
        }

        protected static double getSpaghettiRarity3D(double d) {
            if (d < -0.5) {
                return 0.75;
            }
            if (d < 0.0) {
                return 1.0;
            }
            if (d < 0.5) {
                return 1.5;
            }
            return 2.0;
        }
    }
}

