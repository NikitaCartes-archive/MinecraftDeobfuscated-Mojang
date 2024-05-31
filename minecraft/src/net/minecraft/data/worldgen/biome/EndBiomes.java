package net.minecraft.data.worldgen.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.placement.EndPlacements;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class EndBiomes {
	private static Biome baseEndBiome(BiomeGenerationSettings.Builder builder) {
		MobSpawnSettings.Builder builder2 = new MobSpawnSettings.Builder();
		BiomeDefaultFeatures.endSpawns(builder2);
		return new Biome.BiomeBuilder()
			.hasPrecipitation(false)
			.temperature(0.5F)
			.downfall(0.5F)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(4159204)
					.waterFogColor(329011)
					.fogColor(10518688)
					.skyColor(0)
					.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
					.build()
			)
			.mobSpawnSettings(builder2.build())
			.generationSettings(builder.build())
			.build();
	}

	public static Biome endBarrens(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
		return baseEndBiome(builder);
	}

	public static Biome theEnd(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2)
			.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, EndPlacements.END_SPIKE)
			.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, EndPlacements.END_PLATFORM);
		return baseEndBiome(builder);
	}

	public static Biome endMidlands(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
		return baseEndBiome(builder);
	}

	public static Biome endHighlands(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2)
			.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, EndPlacements.END_GATEWAY_RETURN)
			.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, EndPlacements.CHORUS_PLANT);
		return baseEndBiome(builder);
	}

	public static Biome smallEndIslands(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2)
			.addFeature(GenerationStep.Decoration.RAW_GENERATION, EndPlacements.END_ISLAND_DECORATED);
		return baseEndBiome(builder);
	}
}
