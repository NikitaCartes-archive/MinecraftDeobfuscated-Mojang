/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class Noises {
    public static final ResourceKey<NormalNoise.NoiseParameters> TEMPERATURE = Noises.createKey("temperature");
    public static final ResourceKey<NormalNoise.NoiseParameters> VEGETATION = Noises.createKey("vegetation");
    public static final ResourceKey<NormalNoise.NoiseParameters> CONTINENTALNESS = Noises.createKey("continentalness");
    public static final ResourceKey<NormalNoise.NoiseParameters> EROSION = Noises.createKey("erosion");
    public static final ResourceKey<NormalNoise.NoiseParameters> TEMPERATURE_LARGE = Noises.createKey("temperature_large");
    public static final ResourceKey<NormalNoise.NoiseParameters> VEGETATION_LARGE = Noises.createKey("vegetation_large");
    public static final ResourceKey<NormalNoise.NoiseParameters> CONTINENTALNESS_LARGE = Noises.createKey("continentalness_large");
    public static final ResourceKey<NormalNoise.NoiseParameters> EROSION_LARGE = Noises.createKey("erosion_large");
    public static final ResourceKey<NormalNoise.NoiseParameters> RIDGE = Noises.createKey("ridge");
    public static final ResourceKey<NormalNoise.NoiseParameters> SHIFT = Noises.createKey("offset");
    public static final ResourceKey<NormalNoise.NoiseParameters> AQUIFER_BARRIER = Noises.createKey("aquifer_barrier");
    public static final ResourceKey<NormalNoise.NoiseParameters> AQUIFER_FLUID_LEVEL_FLOODEDNESS = Noises.createKey("aquifer_fluid_level_floodedness");
    public static final ResourceKey<NormalNoise.NoiseParameters> AQUIFER_LAVA = Noises.createKey("aquifer_lava");
    public static final ResourceKey<NormalNoise.NoiseParameters> AQUIFER_FLUID_LEVEL_SPREAD = Noises.createKey("aquifer_fluid_level_spread");
    public static final ResourceKey<NormalNoise.NoiseParameters> PILLAR = Noises.createKey("pillar");
    public static final ResourceKey<NormalNoise.NoiseParameters> PILLAR_RARENESS = Noises.createKey("pillar_rareness");
    public static final ResourceKey<NormalNoise.NoiseParameters> PILLAR_THICKNESS = Noises.createKey("pillar_thickness");
    public static final ResourceKey<NormalNoise.NoiseParameters> SPAGHETTI_2D = Noises.createKey("spaghetti_2d");
    public static final ResourceKey<NormalNoise.NoiseParameters> SPAGHETTI_2D_ELEVATION = Noises.createKey("spaghetti_2d_elevation");
    public static final ResourceKey<NormalNoise.NoiseParameters> SPAGHETTI_2D_MODULATOR = Noises.createKey("spaghetti_2d_modulator");
    public static final ResourceKey<NormalNoise.NoiseParameters> SPAGHETTI_2D_THICKNESS = Noises.createKey("spaghetti_2d_thickness");
    public static final ResourceKey<NormalNoise.NoiseParameters> SPAGHETTI_3D_1 = Noises.createKey("spaghetti_3d_1");
    public static final ResourceKey<NormalNoise.NoiseParameters> SPAGHETTI_3D_2 = Noises.createKey("spaghetti_3d_2");
    public static final ResourceKey<NormalNoise.NoiseParameters> SPAGHETTI_3D_RARITY = Noises.createKey("spaghetti_3d_rarity");
    public static final ResourceKey<NormalNoise.NoiseParameters> SPAGHETTI_3D_THICKNESS = Noises.createKey("spaghetti_3d_thickness");
    public static final ResourceKey<NormalNoise.NoiseParameters> SPAGHETTI_ROUGHNESS = Noises.createKey("spaghetti_roughness");
    public static final ResourceKey<NormalNoise.NoiseParameters> SPAGHETTI_ROUGHNESS_MODULATOR = Noises.createKey("spaghetti_roughness_modulator");
    public static final ResourceKey<NormalNoise.NoiseParameters> CAVE_ENTRANCE = Noises.createKey("cave_entrance");
    public static final ResourceKey<NormalNoise.NoiseParameters> CAVE_LAYER = Noises.createKey("cave_layer");
    public static final ResourceKey<NormalNoise.NoiseParameters> CAVE_CHEESE = Noises.createKey("cave_cheese");
    public static final ResourceKey<NormalNoise.NoiseParameters> ORE_VEININESS = Noises.createKey("ore_veininess");
    public static final ResourceKey<NormalNoise.NoiseParameters> ORE_VEIN_A = Noises.createKey("ore_vein_a");
    public static final ResourceKey<NormalNoise.NoiseParameters> ORE_VEIN_B = Noises.createKey("ore_vein_b");
    public static final ResourceKey<NormalNoise.NoiseParameters> ORE_GAP = Noises.createKey("ore_gap");
    public static final ResourceKey<NormalNoise.NoiseParameters> NOODLE = Noises.createKey("noodle");
    public static final ResourceKey<NormalNoise.NoiseParameters> NOODLE_THICKNESS = Noises.createKey("noodle_thickness");
    public static final ResourceKey<NormalNoise.NoiseParameters> NOODLE_RIDGE_A = Noises.createKey("noodle_ridge_a");
    public static final ResourceKey<NormalNoise.NoiseParameters> NOODLE_RIDGE_B = Noises.createKey("noodle_ridge_b");
    public static final ResourceKey<NormalNoise.NoiseParameters> JAGGED = Noises.createKey("jagged");
    public static final ResourceKey<NormalNoise.NoiseParameters> SURFACE = Noises.createKey("surface");
    public static final ResourceKey<NormalNoise.NoiseParameters> SURFACE_SECONDARY = Noises.createKey("surface_secondary");
    public static final ResourceKey<NormalNoise.NoiseParameters> CLAY_BANDS_OFFSET = Noises.createKey("clay_bands_offset");
    public static final ResourceKey<NormalNoise.NoiseParameters> BADLANDS_PILLAR = Noises.createKey("badlands_pillar");
    public static final ResourceKey<NormalNoise.NoiseParameters> BADLANDS_PILLAR_ROOF = Noises.createKey("badlands_pillar_roof");
    public static final ResourceKey<NormalNoise.NoiseParameters> BADLANDS_SURFACE = Noises.createKey("badlands_surface");
    public static final ResourceKey<NormalNoise.NoiseParameters> ICEBERG_PILLAR = Noises.createKey("iceberg_pillar");
    public static final ResourceKey<NormalNoise.NoiseParameters> ICEBERG_PILLAR_ROOF = Noises.createKey("iceberg_pillar_roof");
    public static final ResourceKey<NormalNoise.NoiseParameters> ICEBERG_SURFACE = Noises.createKey("iceberg_surface");
    public static final ResourceKey<NormalNoise.NoiseParameters> SWAMP = Noises.createKey("surface_swamp");
    public static final ResourceKey<NormalNoise.NoiseParameters> CALCITE = Noises.createKey("calcite");
    public static final ResourceKey<NormalNoise.NoiseParameters> GRAVEL = Noises.createKey("gravel");
    public static final ResourceKey<NormalNoise.NoiseParameters> POWDER_SNOW = Noises.createKey("powder_snow");
    public static final ResourceKey<NormalNoise.NoiseParameters> PACKED_ICE = Noises.createKey("packed_ice");
    public static final ResourceKey<NormalNoise.NoiseParameters> ICE = Noises.createKey("ice");
    public static final ResourceKey<NormalNoise.NoiseParameters> SOUL_SAND_LAYER = Noises.createKey("soul_sand_layer");
    public static final ResourceKey<NormalNoise.NoiseParameters> GRAVEL_LAYER = Noises.createKey("gravel_layer");
    public static final ResourceKey<NormalNoise.NoiseParameters> PATCH = Noises.createKey("patch");
    public static final ResourceKey<NormalNoise.NoiseParameters> NETHERRACK = Noises.createKey("netherrack");
    public static final ResourceKey<NormalNoise.NoiseParameters> NETHER_WART = Noises.createKey("nether_wart");
    public static final ResourceKey<NormalNoise.NoiseParameters> NETHER_STATE_SELECTOR = Noises.createKey("nether_state_selector");

    private static ResourceKey<NormalNoise.NoiseParameters> createKey(String string) {
        return ResourceKey.create(Registry.NOISE_REGISTRY, new ResourceLocation(string));
    }

    public static NormalNoise instantiate(Registry<NormalNoise.NoiseParameters> registry, PositionalRandomFactory positionalRandomFactory, ResourceKey<NormalNoise.NoiseParameters> resourceKey) {
        return NormalNoise.create(positionalRandomFactory.fromHashOf(resourceKey.location()), registry.getOrThrow(resourceKey));
    }
}

