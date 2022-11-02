package net.minecraft.data.worldgen;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NoiseData {
	@Deprecated
	public static final NormalNoise.NoiseParameters DEFAULT_SHIFT = new NormalNoise.NoiseParameters(-3, 1.0, 1.0, 1.0, 0.0);

	public static void bootstrap(BootstapContext<NormalNoise.NoiseParameters> bootstapContext) {
		registerBiomeNoises(bootstapContext, 0, Noises.TEMPERATURE, Noises.VEGETATION, Noises.CONTINENTALNESS, Noises.EROSION);
		registerBiomeNoises(bootstapContext, -2, Noises.TEMPERATURE_LARGE, Noises.VEGETATION_LARGE, Noises.CONTINENTALNESS_LARGE, Noises.EROSION_LARGE);
		register(bootstapContext, Noises.RIDGE, -7, 1.0, 2.0, 1.0, 0.0, 0.0, 0.0);
		bootstapContext.register(Noises.SHIFT, DEFAULT_SHIFT);
		register(bootstapContext, Noises.AQUIFER_BARRIER, -3, 1.0);
		register(bootstapContext, Noises.AQUIFER_FLUID_LEVEL_FLOODEDNESS, -7, 1.0);
		register(bootstapContext, Noises.AQUIFER_LAVA, -1, 1.0);
		register(bootstapContext, Noises.AQUIFER_FLUID_LEVEL_SPREAD, -5, 1.0);
		register(bootstapContext, Noises.PILLAR, -7, 1.0, 1.0);
		register(bootstapContext, Noises.PILLAR_RARENESS, -8, 1.0);
		register(bootstapContext, Noises.PILLAR_THICKNESS, -8, 1.0);
		register(bootstapContext, Noises.SPAGHETTI_2D, -7, 1.0);
		register(bootstapContext, Noises.SPAGHETTI_2D_ELEVATION, -8, 1.0);
		register(bootstapContext, Noises.SPAGHETTI_2D_MODULATOR, -11, 1.0);
		register(bootstapContext, Noises.SPAGHETTI_2D_THICKNESS, -11, 1.0);
		register(bootstapContext, Noises.SPAGHETTI_3D_1, -7, 1.0);
		register(bootstapContext, Noises.SPAGHETTI_3D_2, -7, 1.0);
		register(bootstapContext, Noises.SPAGHETTI_3D_RARITY, -11, 1.0);
		register(bootstapContext, Noises.SPAGHETTI_3D_THICKNESS, -8, 1.0);
		register(bootstapContext, Noises.SPAGHETTI_ROUGHNESS, -5, 1.0);
		register(bootstapContext, Noises.SPAGHETTI_ROUGHNESS_MODULATOR, -8, 1.0);
		register(bootstapContext, Noises.CAVE_ENTRANCE, -7, 0.4, 0.5, 1.0);
		register(bootstapContext, Noises.CAVE_LAYER, -8, 1.0);
		register(bootstapContext, Noises.CAVE_CHEESE, -8, 0.5, 1.0, 2.0, 1.0, 2.0, 1.0, 0.0, 2.0, 0.0);
		register(bootstapContext, Noises.ORE_VEININESS, -8, 1.0);
		register(bootstapContext, Noises.ORE_VEIN_A, -7, 1.0);
		register(bootstapContext, Noises.ORE_VEIN_B, -7, 1.0);
		register(bootstapContext, Noises.ORE_GAP, -5, 1.0);
		register(bootstapContext, Noises.NOODLE, -8, 1.0);
		register(bootstapContext, Noises.NOODLE_THICKNESS, -8, 1.0);
		register(bootstapContext, Noises.NOODLE_RIDGE_A, -7, 1.0);
		register(bootstapContext, Noises.NOODLE_RIDGE_B, -7, 1.0);
		register(bootstapContext, Noises.JAGGED, -16, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0);
		register(bootstapContext, Noises.SURFACE, -6, 1.0, 1.0, 1.0);
		register(bootstapContext, Noises.SURFACE_SECONDARY, -6, 1.0, 1.0, 0.0, 1.0);
		register(bootstapContext, Noises.CLAY_BANDS_OFFSET, -8, 1.0);
		register(bootstapContext, Noises.BADLANDS_PILLAR, -2, 1.0, 1.0, 1.0, 1.0);
		register(bootstapContext, Noises.BADLANDS_PILLAR_ROOF, -8, 1.0);
		register(bootstapContext, Noises.BADLANDS_SURFACE, -6, 1.0, 1.0, 1.0);
		register(bootstapContext, Noises.ICEBERG_PILLAR, -6, 1.0, 1.0, 1.0, 1.0);
		register(bootstapContext, Noises.ICEBERG_PILLAR_ROOF, -3, 1.0);
		register(bootstapContext, Noises.ICEBERG_SURFACE, -6, 1.0, 1.0, 1.0);
		register(bootstapContext, Noises.SWAMP, -2, 1.0);
		register(bootstapContext, Noises.CALCITE, -9, 1.0, 1.0, 1.0, 1.0);
		register(bootstapContext, Noises.GRAVEL, -8, 1.0, 1.0, 1.0, 1.0);
		register(bootstapContext, Noises.POWDER_SNOW, -6, 1.0, 1.0, 1.0, 1.0);
		register(bootstapContext, Noises.PACKED_ICE, -7, 1.0, 1.0, 1.0, 1.0);
		register(bootstapContext, Noises.ICE, -4, 1.0, 1.0, 1.0, 1.0);
		register(bootstapContext, Noises.SOUL_SAND_LAYER, -8, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.013333333333333334);
		register(bootstapContext, Noises.GRAVEL_LAYER, -8, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.013333333333333334);
		register(bootstapContext, Noises.PATCH, -5, 1.0, 0.0, 0.0, 0.0, 0.0, 0.013333333333333334);
		register(bootstapContext, Noises.NETHERRACK, -3, 1.0, 0.0, 0.0, 0.35);
		register(bootstapContext, Noises.NETHER_WART, -3, 1.0, 0.0, 0.0, 0.9);
		register(bootstapContext, Noises.NETHER_STATE_SELECTOR, -4, 1.0);
	}

	private static void registerBiomeNoises(
		BootstapContext<NormalNoise.NoiseParameters> bootstapContext,
		int i,
		ResourceKey<NormalNoise.NoiseParameters> resourceKey,
		ResourceKey<NormalNoise.NoiseParameters> resourceKey2,
		ResourceKey<NormalNoise.NoiseParameters> resourceKey3,
		ResourceKey<NormalNoise.NoiseParameters> resourceKey4
	) {
		register(bootstapContext, resourceKey, -10 + i, 1.5, 0.0, 1.0, 0.0, 0.0, 0.0);
		register(bootstapContext, resourceKey2, -8 + i, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0);
		register(bootstapContext, resourceKey3, -9 + i, 1.0, 1.0, 2.0, 2.0, 2.0, 1.0, 1.0, 1.0, 1.0);
		register(bootstapContext, resourceKey4, -9 + i, 1.0, 1.0, 0.0, 1.0, 1.0);
	}

	private static void register(
		BootstapContext<NormalNoise.NoiseParameters> bootstapContext, ResourceKey<NormalNoise.NoiseParameters> resourceKey, int i, double d, double... ds
	) {
		bootstapContext.register(resourceKey, new NormalNoise.NoiseParameters(i, d, ds));
	}
}
