package net.minecraft.data.worldgen.biome;

import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.Features;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;

public class EndBiomes {
	private static Biome baseEndBiome(BiomeGenerationSettings.Builder builder) {
		MobSpawnSettings.Builder builder2 = new MobSpawnSettings.Builder();
		BiomeDefaultFeatures.endSpawns(builder2);
		return new Biome.BiomeBuilder()
			.precipitation(Biome.Precipitation.NONE)
			.biomeCategory(Biome.BiomeCategory.THEEND)
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

	public static Biome endBarrens() {
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder();
		return baseEndBiome(builder);
	}

	public static Biome theEnd() {
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder().addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.END_SPIKE);
		return baseEndBiome(builder);
	}

	public static Biome endMidlands() {
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder();
		return baseEndBiome(builder);
	}

	public static Biome endHighlands() {
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder()
			.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.END_GATEWAY)
			.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.CHORUS_PLANT);
		return baseEndBiome(builder);
	}

	public static Biome smallEndIslands() {
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder()
			.addFeature(GenerationStep.Decoration.RAW_GENERATION, Features.END_ISLAND_DECORATED);
		return baseEndBiome(builder);
	}
}
