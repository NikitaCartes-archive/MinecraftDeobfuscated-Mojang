/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoiseData {
    public static Holder<NormalNoise.NoiseParameters> bootstrap() {
        NoiseData.registerBiomeNoises(0, Noises.TEMPERATURE, Noises.VEGETATION, Noises.CONTINENTALNESS, Noises.EROSION);
        NoiseData.registerBiomeNoises(-2, Noises.TEMPERATURE_LARGE, Noises.VEGETATION_LARGE, Noises.CONTINENTALNESS_LARGE, Noises.EROSION_LARGE);
        NoiseData.register(Noises.RIDGE, -7, 1.0, 2.0, 1.0, 0.0, 0.0, 0.0);
        NoiseData.register(Noises.SHIFT, -3, 1.0, 1.0, 1.0, 0.0);
        NoiseData.register(Noises.AQUIFER_BARRIER, -3, 1.0, new double[0]);
        NoiseData.register(Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS, -7, 1.0, new double[0]);
        NoiseData.register(Noises.AQUIFER_LAVA, -1, 1.0, new double[0]);
        NoiseData.register(Noises.AQUIFER_FLUID_LEVEL_SPREAD, -5, 1.0, new double[0]);
        NoiseData.register(Noises.PILLAR, -7, 1.0, 1.0);
        NoiseData.register(Noises.PILLAR_RARENESS, -8, 1.0, new double[0]);
        NoiseData.register(Noises.PILLAR_THICKNESS, -8, 1.0, new double[0]);
        NoiseData.register(Noises.SPAGHETTI_2D, -7, 1.0, new double[0]);
        NoiseData.register(Noises.SPAGHETTI_2D_ELEVATION, -8, 1.0, new double[0]);
        NoiseData.register(Noises.SPAGHETTI_2D_MODULATOR, -11, 1.0, new double[0]);
        NoiseData.register(Noises.SPAGHETTI_2D_THICKNESS, -11, 1.0, new double[0]);
        NoiseData.register(Noises.SPAGHETTI_3D_1, -7, 1.0, new double[0]);
        NoiseData.register(Noises.SPAGHETTI_3D_2, -7, 1.0, new double[0]);
        NoiseData.register(Noises.SPAGHETTI_3D_RARITY, -11, 1.0, new double[0]);
        NoiseData.register(Noises.SPAGHETTI_3D_THICKNESS, -8, 1.0, new double[0]);
        NoiseData.register(Noises.SPAGHETTI_ROUGHNESS, -5, 1.0, new double[0]);
        NoiseData.register(Noises.SPAGHETTI_ROUGHNESS_MODULATOR, -8, 1.0, new double[0]);
        NoiseData.register(Noises.CAVE_ENTRANCE, -7, 0.4, 0.5, 1.0);
        NoiseData.register(Noises.CAVE_LAYER, -8, 1.0, new double[0]);
        NoiseData.register(Noises.CAVE_CHEESE, -8, 0.5, 1.0, 2.0, 1.0, 2.0, 1.0, 0.0, 2.0, 0.0);
        NoiseData.register(Noises.ORE_VEININESS, -8, 1.0, new double[0]);
        NoiseData.register(Noises.ORE_VEIN_A, -7, 1.0, new double[0]);
        NoiseData.register(Noises.ORE_VEIN_B, -7, 1.0, new double[0]);
        NoiseData.register(Noises.ORE_GAP, -5, 1.0, new double[0]);
        NoiseData.register(Noises.NOODLE, -8, 1.0, new double[0]);
        NoiseData.register(Noises.NOODLE_THICKNESS, -8, 1.0, new double[0]);
        NoiseData.register(Noises.NOODLE_RIDGE_A, -7, 1.0, new double[0]);
        NoiseData.register(Noises.NOODLE_RIDGE_B, -7, 1.0, new double[0]);
        NoiseData.register(Noises.JAGGED, -16, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0);
        NoiseData.register(Noises.SURFACE, -6, 1.0, 1.0, 1.0);
        NoiseData.register(Noises.SURFACE_SECONDARY, -6, 1.0, 1.0, 0.0, 1.0);
        NoiseData.register(Noises.CLAY_BANDS_OFFSET, -8, 1.0, new double[0]);
        NoiseData.register(Noises.BADLANDS_PILLAR, -2, 1.0, 1.0, 1.0, 1.0);
        NoiseData.register(Noises.BADLANDS_PILLAR_ROOF, -8, 1.0, new double[0]);
        NoiseData.register(Noises.BADLANDS_SURFACE, -6, 1.0, 1.0, 1.0);
        NoiseData.register(Noises.ICEBERG_PILLAR, -6, 1.0, 1.0, 1.0, 1.0);
        NoiseData.register(Noises.ICEBERG_PILLAR_ROOF, -3, 1.0, new double[0]);
        NoiseData.register(Noises.ICEBERG_SURFACE, -6, 1.0, 1.0, 1.0);
        NoiseData.register(Noises.SWAMP, -2, 1.0, new double[0]);
        NoiseData.register(Noises.CALCITE, -9, 1.0, 1.0, 1.0, 1.0);
        NoiseData.register(Noises.GRAVEL, -8, 1.0, 1.0, 1.0, 1.0);
        NoiseData.register(Noises.POWDER_SNOW, -6, 1.0, 1.0, 1.0, 1.0);
        NoiseData.register(Noises.PACKED_ICE, -7, 1.0, 1.0, 1.0, 1.0);
        NoiseData.register(Noises.ICE, -4, 1.0, 1.0, 1.0, 1.0);
        NoiseData.register(Noises.SOUL_SAND_LAYER, -8, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.013333333333333334);
        NoiseData.register(Noises.GRAVEL_LAYER, -8, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.013333333333333334);
        NoiseData.register(Noises.PATCH, -5, 1.0, 0.0, 0.0, 0.0, 0.0, 0.013333333333333334);
        NoiseData.register(Noises.NETHERRACK, -3, 1.0, 0.0, 0.0, 0.35);
        NoiseData.register(Noises.NETHER_WART, -3, 1.0, 0.0, 0.0, 0.9);
        NoiseData.register(Noises.NETHER_STATE_SELECTOR, -4, 1.0, new double[0]);
        return (Holder)BuiltinRegistries.NOISE.holders().iterator().next();
    }

    private static void registerBiomeNoises(int i, ResourceKey<NormalNoise.NoiseParameters> resourceKey, ResourceKey<NormalNoise.NoiseParameters> resourceKey2, ResourceKey<NormalNoise.NoiseParameters> resourceKey3, ResourceKey<NormalNoise.NoiseParameters> resourceKey4) {
        NoiseData.register(resourceKey, -10 + i, 1.5, 0.0, 1.0, 0.0, 0.0, 0.0);
        NoiseData.register(resourceKey2, -8 + i, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0);
        NoiseData.register(resourceKey3, -9 + i, 1.0, 1.0, 2.0, 2.0, 2.0, 1.0, 1.0, 1.0, 1.0);
        NoiseData.register(resourceKey4, -9 + i, 1.0, 1.0, 0.0, 1.0, 1.0);
    }

    private static void register(ResourceKey<NormalNoise.NoiseParameters> resourceKey, int i, double d, double ... ds) {
        BuiltinRegistries.register(BuiltinRegistries.NOISE, resourceKey, new NormalNoise.NoiseParameters(i, d, ds));
    }
}

