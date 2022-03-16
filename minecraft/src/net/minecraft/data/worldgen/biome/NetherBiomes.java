package net.minecraft.data.worldgen.biome;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.data.worldgen.placement.NetherPlacements;
import net.minecraft.data.worldgen.placement.OrePlacements;
import net.minecraft.data.worldgen.placement.TreePlacements;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;

public class NetherBiomes {
	public static Biome netherWastes() {
		MobSpawnSettings mobSpawnSettings = new MobSpawnSettings.Builder()
			.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.GHAST, 50, 4, 4))
			.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 100, 4, 4))
			.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.MAGMA_CUBE, 2, 4, 4))
			.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 1, 4, 4))
			.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.PIGLIN, 15, 4, 4))
			.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2))
			.build();
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder()
			.addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE)
			.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiscOverworldPlacements.SPRING_LAVA);
		BiomeDefaultFeatures.addDefaultMushrooms(builder);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.SPRING_OPEN)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.PATCH_FIRE)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.PATCH_SOUL_FIRE)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.GLOWSTONE_EXTRA)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.GLOWSTONE)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, VegetationPlacements.BROWN_MUSHROOM_NETHER)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, VegetationPlacements.RED_MUSHROOM_NETHER)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_MAGMA)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.SPRING_CLOSED);
		BiomeDefaultFeatures.addNetherDefaultOres(builder);
		return new Biome.BiomeBuilder()
			.precipitation(Biome.Precipitation.NONE)
			.temperature(2.0F)
			.downfall(0.0F)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(4159204)
					.waterFogColor(329011)
					.fogColor(3344392)
					.skyColor(OverworldBiomes.calculateSkyColor(2.0F))
					.ambientLoopSound(SoundEvents.AMBIENT_NETHER_WASTES_LOOP)
					.ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_NETHER_WASTES_MOOD, 6000, 8, 2.0))
					.ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_NETHER_WASTES_ADDITIONS, 0.0111))
					.backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_NETHER_WASTES))
					.build()
			)
			.mobSpawnSettings(mobSpawnSettings)
			.generationSettings(builder.build())
			.build();
	}

	public static Biome soulSandValley() {
		double d = 0.7;
		double e = 0.15;
		MobSpawnSettings mobSpawnSettings = new MobSpawnSettings.Builder()
			.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 20, 5, 5))
			.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.GHAST, 50, 4, 4))
			.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 1, 4, 4))
			.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2))
			.addMobCharge(EntityType.SKELETON, 0.7, 0.15)
			.addMobCharge(EntityType.GHAST, 0.7, 0.15)
			.addMobCharge(EntityType.ENDERMAN, 0.7, 0.15)
			.addMobCharge(EntityType.STRIDER, 0.7, 0.15)
			.build();
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder()
			.addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE)
			.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiscOverworldPlacements.SPRING_LAVA)
			.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, NetherPlacements.BASALT_PILLAR)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.SPRING_OPEN)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.PATCH_FIRE)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.PATCH_SOUL_FIRE)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.GLOWSTONE_EXTRA)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.GLOWSTONE)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.PATCH_CRIMSON_ROOTS)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_MAGMA)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.SPRING_CLOSED)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_SOUL_SAND);
		BiomeDefaultFeatures.addNetherDefaultOres(builder);
		return new Biome.BiomeBuilder()
			.precipitation(Biome.Precipitation.NONE)
			.temperature(2.0F)
			.downfall(0.0F)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(4159204)
					.waterFogColor(329011)
					.fogColor(1787717)
					.skyColor(OverworldBiomes.calculateSkyColor(2.0F))
					.ambientParticle(new AmbientParticleSettings(ParticleTypes.ASH, 0.00625F))
					.ambientLoopSound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_LOOP)
					.ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD, 6000, 8, 2.0))
					.ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS, 0.0111))
					.backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_SOUL_SAND_VALLEY))
					.build()
			)
			.mobSpawnSettings(mobSpawnSettings)
			.generationSettings(builder.build())
			.build();
	}

	public static Biome basaltDeltas() {
		MobSpawnSettings mobSpawnSettings = new MobSpawnSettings.Builder()
			.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.GHAST, 40, 1, 1))
			.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.MAGMA_CUBE, 100, 2, 5))
			.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2))
			.build();
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder()
			.addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE)
			.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, NetherPlacements.DELTA)
			.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, NetherPlacements.SMALL_BASALT_COLUMNS)
			.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, NetherPlacements.LARGE_BASALT_COLUMNS)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.BASALT_BLOBS)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.BLACKSTONE_BLOBS)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.SPRING_DELTA)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.PATCH_FIRE)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.PATCH_SOUL_FIRE)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.GLOWSTONE_EXTRA)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.GLOWSTONE)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, VegetationPlacements.BROWN_MUSHROOM_NETHER)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, VegetationPlacements.RED_MUSHROOM_NETHER)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_MAGMA)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.SPRING_CLOSED_DOUBLE)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_GOLD_DELTAS)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_QUARTZ_DELTAS);
		BiomeDefaultFeatures.addAncientDebris(builder);
		return new Biome.BiomeBuilder()
			.precipitation(Biome.Precipitation.NONE)
			.temperature(2.0F)
			.downfall(0.0F)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(4159204)
					.waterFogColor(329011)
					.fogColor(6840176)
					.skyColor(OverworldBiomes.calculateSkyColor(2.0F))
					.ambientParticle(new AmbientParticleSettings(ParticleTypes.WHITE_ASH, 0.118093334F))
					.ambientLoopSound(SoundEvents.AMBIENT_BASALT_DELTAS_LOOP)
					.ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_BASALT_DELTAS_MOOD, 6000, 8, 2.0))
					.ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_BASALT_DELTAS_ADDITIONS, 0.0111))
					.backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_BASALT_DELTAS))
					.build()
			)
			.mobSpawnSettings(mobSpawnSettings)
			.generationSettings(builder.build())
			.build();
	}

	public static Biome crimsonForest() {
		MobSpawnSettings mobSpawnSettings = new MobSpawnSettings.Builder()
			.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 1, 2, 4))
			.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.HOGLIN, 9, 3, 4))
			.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.PIGLIN, 5, 3, 4))
			.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2))
			.build();
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder()
			.addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE)
			.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiscOverworldPlacements.SPRING_LAVA);
		BiomeDefaultFeatures.addDefaultMushrooms(builder);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.SPRING_OPEN)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.PATCH_FIRE)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.GLOWSTONE_EXTRA)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.GLOWSTONE)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_MAGMA)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.SPRING_CLOSED)
			.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, NetherPlacements.WEEPING_VINES)
			.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, TreePlacements.CRIMSON_FUNGI)
			.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, NetherPlacements.CRIMSON_FOREST_VEGETATION);
		BiomeDefaultFeatures.addNetherDefaultOres(builder);
		return new Biome.BiomeBuilder()
			.precipitation(Biome.Precipitation.NONE)
			.temperature(2.0F)
			.downfall(0.0F)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(4159204)
					.waterFogColor(329011)
					.fogColor(3343107)
					.skyColor(OverworldBiomes.calculateSkyColor(2.0F))
					.ambientParticle(new AmbientParticleSettings(ParticleTypes.CRIMSON_SPORE, 0.025F))
					.ambientLoopSound(SoundEvents.AMBIENT_CRIMSON_FOREST_LOOP)
					.ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_CRIMSON_FOREST_MOOD, 6000, 8, 2.0))
					.ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_CRIMSON_FOREST_ADDITIONS, 0.0111))
					.backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_CRIMSON_FOREST))
					.build()
			)
			.mobSpawnSettings(mobSpawnSettings)
			.generationSettings(builder.build())
			.build();
	}

	public static Biome warpedForest() {
		MobSpawnSettings mobSpawnSettings = new MobSpawnSettings.Builder()
			.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 1, 4, 4))
			.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2))
			.addMobCharge(EntityType.ENDERMAN, 1.0, 0.12)
			.build();
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder()
			.addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE)
			.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiscOverworldPlacements.SPRING_LAVA);
		BiomeDefaultFeatures.addDefaultMushrooms(builder);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.SPRING_OPEN)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.PATCH_FIRE)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.PATCH_SOUL_FIRE)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.GLOWSTONE_EXTRA)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.GLOWSTONE)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_MAGMA)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.SPRING_CLOSED)
			.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, TreePlacements.WARPED_FUNGI)
			.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, NetherPlacements.WARPED_FOREST_VEGETATION)
			.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, NetherPlacements.NETHER_SPROUTS)
			.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, NetherPlacements.TWISTING_VINES);
		BiomeDefaultFeatures.addNetherDefaultOres(builder);
		return new Biome.BiomeBuilder()
			.precipitation(Biome.Precipitation.NONE)
			.temperature(2.0F)
			.downfall(0.0F)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(4159204)
					.waterFogColor(329011)
					.fogColor(1705242)
					.skyColor(OverworldBiomes.calculateSkyColor(2.0F))
					.ambientParticle(new AmbientParticleSettings(ParticleTypes.WARPED_SPORE, 0.01428F))
					.ambientLoopSound(SoundEvents.AMBIENT_WARPED_FOREST_LOOP)
					.ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_WARPED_FOREST_MOOD, 6000, 8, 2.0))
					.ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_WARPED_FOREST_ADDITIONS, 0.0111))
					.backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_WARPED_FOREST))
					.build()
			)
			.mobSpawnSettings(mobSpawnSettings)
			.generationSettings(builder.build())
			.build();
	}
}
