package net.minecraft.data.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoiseData {
	public static Holder<NormalNoise.NoiseParameters> bootstrap(Registry<NormalNoise.NoiseParameters> registry) {
		registerBiomeNoises(registry, 0, Noises.TEMPERATURE, Noises.VEGETATION, Noises.CONTINENTALNESS, Noises.EROSION);
		registerBiomeNoises(registry, -2, Noises.TEMPERATURE_LARGE, Noises.VEGETATION_LARGE, Noises.CONTINENTALNESS_LARGE, Noises.EROSION_LARGE);
		register(registry, Noises.RIDGE, -7, 1.0, 2.0, 1.0, 0.0, 0.0, 0.0);
		register(registry, Noises.SHIFT, -3, 1.0, 1.0, 1.0, 0.0);
		register(registry, Noises.AQUIFER_BARRIER, -3, 1.0);
		register(registry, Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS, -7, 1.0);
		register(registry, Noises.AQUIFER_LAVA, -1, 1.0);
		register(registry, Noises.AQUIFER_FLUID_LEVEL_SPREAD, -5, 1.0);
		register(registry, Noises.PILLAR, -7, 1.0, 1.0);
		register(registry, Noises.PILLAR_RARENESS, -8, 1.0);
		register(registry, Noises.PILLAR_THICKNESS, -8, 1.0);
		register(registry, Noises.SPAGHETTI_2D, -7, 1.0);
		register(registry, Noises.SPAGHETTI_2D_ELEVATION, -8, 1.0);
		register(registry, Noises.SPAGHETTI_2D_MODULATOR, -11, 1.0);
		register(registry, Noises.SPAGHETTI_2D_THICKNESS, -11, 1.0);
		register(registry, Noises.SPAGHETTI_3D_1, -7, 1.0);
		register(registry, Noises.SPAGHETTI_3D_2, -7, 1.0);
		register(registry, Noises.SPAGHETTI_3D_RARITY, -11, 1.0);
		register(registry, Noises.SPAGHETTI_3D_THICKNESS, -8, 1.0);
		register(registry, Noises.SPAGHETTI_ROUGHNESS, -5, 1.0);
		register(registry, Noises.SPAGHETTI_ROUGHNESS_MODULATOR, -8, 1.0);
		register(registry, Noises.CAVE_ENTRANCE, -7, 0.4, 0.5, 1.0);
		register(registry, Noises.CAVE_LAYER, -8, 1.0);
		register(registry, Noises.CAVE_CHEESE, -8, 0.5, 1.0, 2.0, 1.0, 2.0, 1.0, 0.0, 2.0, 0.0);
		register(registry, Noises.ORE_VEININESS, -8, 1.0);
		register(registry, Noises.ORE_VEIN_A, -7, 1.0);
		register(registry, Noises.ORE_VEIN_B, -7, 1.0);
		register(registry, Noises.ORE_GAP, -5, 1.0);
		register(registry, Noises.NOODLE, -8, 1.0);
		register(registry, Noises.NOODLE_THICKNESS, -8, 1.0);
		register(registry, Noises.NOODLE_RIDGE_A, -7, 1.0);
		register(registry, Noises.NOODLE_RIDGE_B, -7, 1.0);
		register(registry, Noises.JAGGED, -16, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0);
		register(registry, Noises.SURFACE, -6, 1.0, 1.0, 1.0);
		register(registry, Noises.SURFACE_SECONDARY, -6, 1.0, 1.0, 0.0, 1.0);
		register(registry, Noises.CLAY_BANDS_OFFSET, -8, 1.0);
		register(registry, Noises.BADLANDS_PILLAR, -2, 1.0, 1.0, 1.0, 1.0);
		register(registry, Noises.BADLANDS_PILLAR_ROOF, -8, 1.0);
		register(registry, Noises.BADLANDS_SURFACE, -6, 1.0, 1.0, 1.0);
		register(registry, Noises.ICEBERG_PILLAR, -6, 1.0, 1.0, 1.0, 1.0);
		register(registry, Noises.ICEBERG_PILLAR_ROOF, -3, 1.0);
		register(registry, Noises.ICEBERG_SURFACE, -6, 1.0, 1.0, 1.0);
		register(registry, Noises.SWAMP, -2, 1.0);
		register(registry, Noises.CALCITE, -9, 1.0, 1.0, 1.0, 1.0);
		register(registry, Noises.GRAVEL, -8, 1.0, 1.0, 1.0, 1.0);
		register(registry, Noises.POWDER_SNOW, -6, 1.0, 1.0, 1.0, 1.0);
		register(registry, Noises.PACKED_ICE, -7, 1.0, 1.0, 1.0, 1.0);
		register(registry, Noises.ICE, -4, 1.0, 1.0, 1.0, 1.0);
		register(registry, Noises.SOUL_SAND_LAYER, -8, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.013333333333333334);
		register(registry, Noises.GRAVEL_LAYER, -8, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.013333333333333334);
		register(registry, Noises.PATCH, -5, 1.0, 0.0, 0.0, 0.0, 0.0, 0.013333333333333334);
		register(registry, Noises.NETHERRACK, -3, 1.0, 0.0, 0.0, 0.35);
		register(registry, Noises.NETHER_WART, -3, 1.0, 0.0, 0.0, 0.9);
		return register(registry, Noises.NETHER_STATE_SELECTOR, -4, 1.0);
	}

	private static void registerBiomeNoises(
		Registry<NormalNoise.NoiseParameters> registry,
		int i,
		ResourceKey<NormalNoise.NoiseParameters> resourceKey,
		ResourceKey<NormalNoise.NoiseParameters> resourceKey2,
		ResourceKey<NormalNoise.NoiseParameters> resourceKey3,
		ResourceKey<NormalNoise.NoiseParameters> resourceKey4
	) {
		register(registry, resourceKey, -10 + i, 1.5, 0.0, 1.0, 0.0, 0.0, 0.0);
		register(registry, resourceKey2, -8 + i, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0);
		register(registry, resourceKey3, -9 + i, 1.0, 1.0, 2.0, 2.0, 2.0, 1.0, 1.0, 1.0, 1.0);
		register(registry, resourceKey4, -9 + i, 1.0, 1.0, 0.0, 1.0, 1.0);
	}

	private static Holder<NormalNoise.NoiseParameters> register(
		Registry<NormalNoise.NoiseParameters> registry, ResourceKey<NormalNoise.NoiseParameters> resourceKey, int i, double d, double... ds
	) {
		return BuiltinRegistries.register(registry, resourceKey, new NormalNoise.NoiseParameters(i, d, ds));
	}
}
