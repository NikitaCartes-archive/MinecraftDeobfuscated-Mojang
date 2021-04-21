package net.minecraft.data.worldgen.biome;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.Features;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.data.worldgen.SurfaceBuilders;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
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
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderBaseConfiguration;

public class VanillaBiomes {
	private static int calculateSkyColor(float f) {
		float g = f / 3.0F;
		g = Mth.clamp(g, -1.0F, 1.0F);
		return Mth.hsvToRgb(0.62222224F - g * 0.05F, 0.5F + g * 0.1F, 1.0F);
	}

	public static Biome giantTreeTaiga(float f, float g, float h, boolean bl) {
		MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
		BiomeDefaultFeatures.farmAnimals(builder);
		builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 8, 4, 4));
		builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3));
		builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FOX, 8, 2, 4));
		if (bl) {
			BiomeDefaultFeatures.commonSpawns(builder);
		} else {
			BiomeDefaultFeatures.caveSpawns(builder);
			BiomeDefaultFeatures.monsters(builder, 100, 25, 100);
		}

		BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.GIANT_TREE_TAIGA);
		BiomeDefaultFeatures.addDefaultOverworldLandStructures(builder2);
		builder2.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
		BiomeDefaultFeatures.addDefaultCarvers(builder2);
		BiomeDefaultFeatures.addDefaultLakes(builder2);
		BiomeDefaultFeatures.addDefaultCrystalFormations(builder2);
		BiomeDefaultFeatures.addDefaultMonsterRoom(builder2);
		BiomeDefaultFeatures.addMossyStoneBlock(builder2);
		BiomeDefaultFeatures.addFerns(builder2);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(builder2);
		BiomeDefaultFeatures.addDefaultOres(builder2);
		BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
		builder2.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, bl ? Features.TREES_GIANT_SPRUCE : Features.TREES_GIANT);
		BiomeDefaultFeatures.addDefaultFlowers(builder2);
		BiomeDefaultFeatures.addGiantTaigaVegetation(builder2);
		BiomeDefaultFeatures.addDefaultMushrooms(builder2);
		BiomeDefaultFeatures.addDefaultExtraVegetation(builder2);
		BiomeDefaultFeatures.addDefaultSprings(builder2);
		BiomeDefaultFeatures.addSparseBerryBushes(builder2);
		BiomeDefaultFeatures.addSurfaceFreezing(builder2);
		return new Biome.BiomeBuilder()
			.precipitation(Biome.Precipitation.RAIN)
			.biomeCategory(Biome.BiomeCategory.TAIGA)
			.depth(f)
			.scale(g)
			.temperature(h)
			.downfall(0.8F)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(4159204)
					.waterFogColor(329011)
					.fogColor(12638463)
					.skyColor(calculateSkyColor(h))
					.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
					.build()
			)
			.mobSpawnSettings(builder.build())
			.generationSettings(builder2.build())
			.build();
	}

	public static Biome birchForestBiome(float f, float g, boolean bl) {
		MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
		BiomeDefaultFeatures.farmAnimals(builder);
		BiomeDefaultFeatures.commonSpawns(builder);
		BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.GRASS);
		BiomeDefaultFeatures.addDefaultOverworldLandStructures(builder2);
		builder2.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
		BiomeDefaultFeatures.addDefaultCarvers(builder2);
		BiomeDefaultFeatures.addDefaultLakes(builder2);
		BiomeDefaultFeatures.addDefaultCrystalFormations(builder2);
		BiomeDefaultFeatures.addDefaultMonsterRoom(builder2);
		BiomeDefaultFeatures.addForestFlowers(builder2);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(builder2);
		BiomeDefaultFeatures.addDefaultOres(builder2);
		BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
		if (bl) {
			BiomeDefaultFeatures.addTallBirchTrees(builder2);
		} else {
			BiomeDefaultFeatures.addBirchTrees(builder2);
		}

		BiomeDefaultFeatures.addDefaultFlowers(builder2);
		BiomeDefaultFeatures.addForestGrass(builder2);
		BiomeDefaultFeatures.addDefaultMushrooms(builder2);
		BiomeDefaultFeatures.addDefaultExtraVegetation(builder2);
		BiomeDefaultFeatures.addDefaultSprings(builder2);
		BiomeDefaultFeatures.addSurfaceFreezing(builder2);
		return new Biome.BiomeBuilder()
			.precipitation(Biome.Precipitation.RAIN)
			.biomeCategory(Biome.BiomeCategory.FOREST)
			.depth(f)
			.scale(g)
			.temperature(0.6F)
			.downfall(0.6F)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(4159204)
					.waterFogColor(329011)
					.fogColor(12638463)
					.skyColor(calculateSkyColor(0.6F))
					.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
					.build()
			)
			.mobSpawnSettings(builder.build())
			.generationSettings(builder2.build())
			.build();
	}

	public static Biome jungleBiome() {
		return jungleBiome(0.1F, 0.2F, 40, 2, 3);
	}

	public static Biome jungleEdgeBiome() {
		MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
		BiomeDefaultFeatures.baseJungleSpawns(builder);
		return baseJungleBiome(0.1F, 0.2F, 0.8F, false, true, false, builder);
	}

	public static Biome modifiedJungleEdgeBiome() {
		MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
		BiomeDefaultFeatures.baseJungleSpawns(builder);
		return baseJungleBiome(0.2F, 0.4F, 0.8F, false, true, true, builder);
	}

	public static Biome modifiedJungleBiome() {
		MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
		BiomeDefaultFeatures.baseJungleSpawns(builder);
		builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PARROT, 10, 1, 1))
			.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.OCELOT, 2, 1, 1));
		return baseJungleBiome(0.2F, 0.4F, 0.9F, false, false, true, builder);
	}

	public static Biome jungleHillsBiome() {
		return jungleBiome(0.45F, 0.3F, 10, 1, 1);
	}

	public static Biome bambooJungleBiome() {
		return bambooJungleBiome(0.1F, 0.2F, 40, 2);
	}

	public static Biome bambooJungleHillsBiome() {
		return bambooJungleBiome(0.45F, 0.3F, 10, 1);
	}

	private static Biome jungleBiome(float f, float g, int i, int j, int k) {
		MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
		BiomeDefaultFeatures.baseJungleSpawns(builder);
		builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PARROT, i, 1, j))
			.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.OCELOT, 2, 1, k))
			.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PANDA, 1, 1, 2));
		builder.setPlayerCanSpawn();
		return baseJungleBiome(f, g, 0.9F, false, false, false, builder);
	}

	private static Biome bambooJungleBiome(float f, float g, int i, int j) {
		MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
		BiomeDefaultFeatures.baseJungleSpawns(builder);
		builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PARROT, i, 1, j))
			.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PANDA, 80, 1, 2))
			.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.OCELOT, 2, 1, 1));
		return baseJungleBiome(f, g, 0.9F, true, false, false, builder);
	}

	private static Biome baseJungleBiome(float f, float g, float h, boolean bl, boolean bl2, boolean bl3, MobSpawnSettings.Builder builder) {
		BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.GRASS);
		if (!bl2 && !bl3) {
			builder2.addStructureStart(StructureFeatures.JUNGLE_TEMPLE);
		}

		BiomeDefaultFeatures.addDefaultOverworldLandStructures(builder2);
		builder2.addStructureStart(StructureFeatures.RUINED_PORTAL_JUNGLE);
		BiomeDefaultFeatures.addDefaultCarvers(builder2);
		BiomeDefaultFeatures.addDefaultLakes(builder2);
		BiomeDefaultFeatures.addDefaultCrystalFormations(builder2);
		BiomeDefaultFeatures.addDefaultMonsterRoom(builder2);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(builder2);
		BiomeDefaultFeatures.addDefaultOres(builder2);
		BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
		if (bl) {
			BiomeDefaultFeatures.addBambooVegetation(builder2);
		} else {
			if (!bl2 && !bl3) {
				BiomeDefaultFeatures.addLightBambooVegetation(builder2);
			}

			if (bl2) {
				BiomeDefaultFeatures.addJungleEdgeTrees(builder2);
			} else {
				BiomeDefaultFeatures.addJungleTrees(builder2);
			}
		}

		BiomeDefaultFeatures.addWarmFlowers(builder2);
		BiomeDefaultFeatures.addJungleGrass(builder2);
		BiomeDefaultFeatures.addDefaultMushrooms(builder2);
		BiomeDefaultFeatures.addDefaultExtraVegetation(builder2);
		BiomeDefaultFeatures.addDefaultSprings(builder2);
		BiomeDefaultFeatures.addJungleExtraVegetation(builder2);
		BiomeDefaultFeatures.addSurfaceFreezing(builder2);
		return new Biome.BiomeBuilder()
			.precipitation(Biome.Precipitation.RAIN)
			.biomeCategory(Biome.BiomeCategory.JUNGLE)
			.depth(f)
			.scale(g)
			.temperature(0.95F)
			.downfall(h)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(4159204)
					.waterFogColor(329011)
					.fogColor(12638463)
					.skyColor(calculateSkyColor(0.95F))
					.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
					.build()
			)
			.mobSpawnSettings(builder.build())
			.generationSettings(builder2.build())
			.build();
	}

	public static Biome mountainBiome(float f, float g, ConfiguredSurfaceBuilder<SurfaceBuilderBaseConfiguration> configuredSurfaceBuilder, boolean bl) {
		MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
		BiomeDefaultFeatures.farmAnimals(builder);
		builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.LLAMA, 5, 4, 6));
		builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GOAT, 10, 4, 6));
		BiomeDefaultFeatures.commonSpawns(builder);
		BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder().surfaceBuilder(configuredSurfaceBuilder);
		BiomeDefaultFeatures.addDefaultOverworldLandStructures(builder2);
		builder2.addStructureStart(StructureFeatures.RUINED_PORTAL_MOUNTAIN);
		BiomeDefaultFeatures.addDefaultCarvers(builder2);
		BiomeDefaultFeatures.addDefaultLakes(builder2);
		BiomeDefaultFeatures.addDefaultCrystalFormations(builder2);
		BiomeDefaultFeatures.addDefaultMonsterRoom(builder2);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(builder2);
		BiomeDefaultFeatures.addDefaultOres(builder2);
		BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
		if (bl) {
			BiomeDefaultFeatures.addMountainEdgeTrees(builder2);
		} else {
			BiomeDefaultFeatures.addMountainTrees(builder2);
		}

		BiomeDefaultFeatures.addDefaultFlowers(builder2);
		BiomeDefaultFeatures.addDefaultGrass(builder2);
		BiomeDefaultFeatures.addDefaultMushrooms(builder2);
		BiomeDefaultFeatures.addDefaultExtraVegetation(builder2);
		BiomeDefaultFeatures.addDefaultSprings(builder2);
		BiomeDefaultFeatures.addExtraEmeralds(builder2);
		BiomeDefaultFeatures.addInfestedStone(builder2);
		BiomeDefaultFeatures.addSurfaceFreezing(builder2);
		return new Biome.BiomeBuilder()
			.precipitation(Biome.Precipitation.RAIN)
			.biomeCategory(Biome.BiomeCategory.EXTREME_HILLS)
			.depth(f)
			.scale(g)
			.temperature(0.2F)
			.downfall(0.3F)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(4159204)
					.waterFogColor(329011)
					.fogColor(12638463)
					.skyColor(calculateSkyColor(0.2F))
					.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
					.build()
			)
			.mobSpawnSettings(builder.build())
			.generationSettings(builder2.build())
			.build();
	}

	public static Biome desertBiome(float f, float g, boolean bl, boolean bl2, boolean bl3) {
		MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
		BiomeDefaultFeatures.desertSpawns(builder);
		BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.DESERT);
		if (bl) {
			builder2.addStructureStart(StructureFeatures.VILLAGE_DESERT);
			builder2.addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
		}

		if (bl2) {
			builder2.addStructureStart(StructureFeatures.DESERT_PYRAMID);
		}

		if (bl3) {
			BiomeDefaultFeatures.addFossilDecoration(builder2);
		}

		BiomeDefaultFeatures.addDefaultOverworldLandStructures(builder2);
		builder2.addStructureStart(StructureFeatures.RUINED_PORTAL_DESERT);
		BiomeDefaultFeatures.addDefaultCarvers(builder2);
		BiomeDefaultFeatures.addDesertLakes(builder2);
		BiomeDefaultFeatures.addDefaultCrystalFormations(builder2);
		BiomeDefaultFeatures.addDefaultMonsterRoom(builder2);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(builder2);
		BiomeDefaultFeatures.addDefaultOres(builder2);
		BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
		BiomeDefaultFeatures.addDefaultFlowers(builder2);
		BiomeDefaultFeatures.addDefaultGrass(builder2);
		BiomeDefaultFeatures.addDesertVegetation(builder2);
		BiomeDefaultFeatures.addDefaultMushrooms(builder2);
		BiomeDefaultFeatures.addDesertExtraVegetation(builder2);
		BiomeDefaultFeatures.addDefaultSprings(builder2);
		BiomeDefaultFeatures.addDesertExtraDecoration(builder2);
		BiomeDefaultFeatures.addSurfaceFreezing(builder2);
		return new Biome.BiomeBuilder()
			.precipitation(Biome.Precipitation.NONE)
			.biomeCategory(Biome.BiomeCategory.DESERT)
			.depth(f)
			.scale(g)
			.temperature(2.0F)
			.downfall(0.0F)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(4159204)
					.waterFogColor(329011)
					.fogColor(12638463)
					.skyColor(calculateSkyColor(2.0F))
					.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
					.build()
			)
			.mobSpawnSettings(builder.build())
			.generationSettings(builder2.build())
			.build();
	}

	public static Biome plainsBiome(boolean bl) {
		MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
		BiomeDefaultFeatures.plainsSpawns(builder);
		if (!bl) {
			builder.setPlayerCanSpawn();
		}

		BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.GRASS);
		if (!bl) {
			builder2.addStructureStart(StructureFeatures.VILLAGE_PLAINS).addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
		}

		BiomeDefaultFeatures.addDefaultOverworldLandStructures(builder2);
		builder2.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
		BiomeDefaultFeatures.addDefaultCarvers(builder2);
		BiomeDefaultFeatures.addDefaultLakes(builder2);
		BiomeDefaultFeatures.addDefaultCrystalFormations(builder2);
		BiomeDefaultFeatures.addDefaultMonsterRoom(builder2);
		BiomeDefaultFeatures.addPlainGrass(builder2);
		if (bl) {
			builder2.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUNFLOWER);
		}

		BiomeDefaultFeatures.addDefaultUndergroundVariety(builder2);
		BiomeDefaultFeatures.addDefaultOres(builder2);
		BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
		BiomeDefaultFeatures.addPlainVegetation(builder2);
		if (bl) {
			builder2.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE);
		}

		BiomeDefaultFeatures.addDefaultMushrooms(builder2);
		if (bl) {
			builder2.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
		} else {
			BiomeDefaultFeatures.addDefaultExtraVegetation(builder2);
		}

		BiomeDefaultFeatures.addDefaultSprings(builder2);
		BiomeDefaultFeatures.addSurfaceFreezing(builder2);
		return new Biome.BiomeBuilder()
			.precipitation(Biome.Precipitation.RAIN)
			.biomeCategory(Biome.BiomeCategory.PLAINS)
			.depth(0.125F)
			.scale(0.05F)
			.temperature(0.8F)
			.downfall(0.4F)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(4159204)
					.waterFogColor(329011)
					.fogColor(12638463)
					.skyColor(calculateSkyColor(0.8F))
					.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
					.build()
			)
			.mobSpawnSettings(builder.build())
			.generationSettings(builder2.build())
			.build();
	}

	private static Biome baseEndBiome(BiomeGenerationSettings.Builder builder) {
		MobSpawnSettings.Builder builder2 = new MobSpawnSettings.Builder();
		BiomeDefaultFeatures.endSpawns(builder2);
		return new Biome.BiomeBuilder()
			.precipitation(Biome.Precipitation.NONE)
			.biomeCategory(Biome.BiomeCategory.THEEND)
			.depth(0.1F)
			.scale(0.2F)
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

	public static Biome endBarrensBiome() {
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.END);
		return baseEndBiome(builder);
	}

	public static Biome theEndBiome() {
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder()
			.surfaceBuilder(SurfaceBuilders.END)
			.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.END_SPIKE);
		return baseEndBiome(builder);
	}

	public static Biome endMidlandsBiome() {
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder()
			.surfaceBuilder(SurfaceBuilders.END)
			.addStructureStart(StructureFeatures.END_CITY);
		return baseEndBiome(builder);
	}

	public static Biome endHighlandsBiome() {
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder()
			.surfaceBuilder(SurfaceBuilders.END)
			.addStructureStart(StructureFeatures.END_CITY)
			.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.END_GATEWAY)
			.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.CHORUS_PLANT);
		return baseEndBiome(builder);
	}

	public static Biome smallEndIslandsBiome() {
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder()
			.surfaceBuilder(SurfaceBuilders.END)
			.addFeature(GenerationStep.Decoration.RAW_GENERATION, Features.END_ISLAND_DECORATED);
		return baseEndBiome(builder);
	}

	public static Biome mushroomFieldsBiome(float f, float g) {
		MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
		BiomeDefaultFeatures.mooshroomSpawns(builder);
		BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.MYCELIUM);
		BiomeDefaultFeatures.addDefaultOverworldLandStructures(builder2);
		builder2.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
		BiomeDefaultFeatures.addDefaultCarvers(builder2);
		BiomeDefaultFeatures.addDefaultLakes(builder2);
		BiomeDefaultFeatures.addDefaultCrystalFormations(builder2);
		BiomeDefaultFeatures.addDefaultMonsterRoom(builder2);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(builder2);
		BiomeDefaultFeatures.addDefaultOres(builder2);
		BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
		BiomeDefaultFeatures.addMushroomFieldVegetation(builder2);
		BiomeDefaultFeatures.addDefaultMushrooms(builder2);
		BiomeDefaultFeatures.addDefaultExtraVegetation(builder2);
		BiomeDefaultFeatures.addDefaultSprings(builder2);
		BiomeDefaultFeatures.addSurfaceFreezing(builder2);
		return new Biome.BiomeBuilder()
			.precipitation(Biome.Precipitation.RAIN)
			.biomeCategory(Biome.BiomeCategory.MUSHROOM)
			.depth(f)
			.scale(g)
			.temperature(0.9F)
			.downfall(1.0F)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(4159204)
					.waterFogColor(329011)
					.fogColor(12638463)
					.skyColor(calculateSkyColor(0.9F))
					.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
					.build()
			)
			.mobSpawnSettings(builder.build())
			.generationSettings(builder2.build())
			.build();
	}

	private static Biome baseSavannaBiome(float f, float g, float h, boolean bl, boolean bl2, MobSpawnSettings.Builder builder) {
		BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder()
			.surfaceBuilder(bl2 ? SurfaceBuilders.SHATTERED_SAVANNA : SurfaceBuilders.GRASS);
		if (!bl && !bl2) {
			builder2.addStructureStart(StructureFeatures.VILLAGE_SAVANNA).addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
		}

		BiomeDefaultFeatures.addDefaultOverworldLandStructures(builder2);
		builder2.addStructureStart(bl ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
		BiomeDefaultFeatures.addDefaultCarvers(builder2);
		BiomeDefaultFeatures.addDefaultLakes(builder2);
		BiomeDefaultFeatures.addDefaultCrystalFormations(builder2);
		BiomeDefaultFeatures.addDefaultMonsterRoom(builder2);
		if (!bl2) {
			BiomeDefaultFeatures.addSavannaGrass(builder2);
		}

		BiomeDefaultFeatures.addDefaultUndergroundVariety(builder2);
		BiomeDefaultFeatures.addDefaultOres(builder2);
		BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
		if (bl2) {
			BiomeDefaultFeatures.addShatteredSavannaTrees(builder2);
			BiomeDefaultFeatures.addDefaultFlowers(builder2);
			BiomeDefaultFeatures.addShatteredSavannaGrass(builder2);
		} else {
			BiomeDefaultFeatures.addSavannaTrees(builder2);
			BiomeDefaultFeatures.addWarmFlowers(builder2);
			BiomeDefaultFeatures.addSavannaExtraGrass(builder2);
		}

		BiomeDefaultFeatures.addDefaultMushrooms(builder2);
		BiomeDefaultFeatures.addDefaultExtraVegetation(builder2);
		BiomeDefaultFeatures.addDefaultSprings(builder2);
		BiomeDefaultFeatures.addSurfaceFreezing(builder2);
		return new Biome.BiomeBuilder()
			.precipitation(Biome.Precipitation.NONE)
			.biomeCategory(Biome.BiomeCategory.SAVANNA)
			.depth(f)
			.scale(g)
			.temperature(h)
			.downfall(0.0F)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(4159204)
					.waterFogColor(329011)
					.fogColor(12638463)
					.skyColor(calculateSkyColor(h))
					.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
					.build()
			)
			.mobSpawnSettings(builder.build())
			.generationSettings(builder2.build())
			.build();
	}

	public static Biome savannaBiome(float f, float g, float h, boolean bl, boolean bl2) {
		MobSpawnSettings.Builder builder = savannaMobs();
		return baseSavannaBiome(f, g, h, bl, bl2, builder);
	}

	private static MobSpawnSettings.Builder savannaMobs() {
		MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
		BiomeDefaultFeatures.farmAnimals(builder);
		builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.HORSE, 1, 2, 6))
			.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DONKEY, 1, 1, 1));
		BiomeDefaultFeatures.commonSpawns(builder);
		return builder;
	}

	public static Biome savanaPlateauBiome() {
		MobSpawnSettings.Builder builder = savannaMobs();
		builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.LLAMA, 8, 4, 4));
		return baseSavannaBiome(1.5F, 0.025F, 1.0F, true, false, builder);
	}

	private static Biome baseBadlandsBiome(
		ConfiguredSurfaceBuilder<SurfaceBuilderBaseConfiguration> configuredSurfaceBuilder, float f, float g, boolean bl, boolean bl2
	) {
		MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
		BiomeDefaultFeatures.commonSpawns(builder);
		BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder().surfaceBuilder(configuredSurfaceBuilder);
		BiomeDefaultFeatures.addDefaultOverworldLandMesaStructures(builder2);
		builder2.addStructureStart(bl ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
		BiomeDefaultFeatures.addDefaultCarvers(builder2);
		BiomeDefaultFeatures.addDefaultLakes(builder2);
		BiomeDefaultFeatures.addDefaultCrystalFormations(builder2);
		BiomeDefaultFeatures.addDefaultMonsterRoom(builder2);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(builder2);
		BiomeDefaultFeatures.addDefaultOres(builder2);
		BiomeDefaultFeatures.addExtraGold(builder2);
		BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
		if (bl2) {
			BiomeDefaultFeatures.addBadlandsTrees(builder2);
		}

		BiomeDefaultFeatures.addBadlandGrass(builder2);
		BiomeDefaultFeatures.addDefaultMushrooms(builder2);
		BiomeDefaultFeatures.addBadlandExtraVegetation(builder2);
		BiomeDefaultFeatures.addDefaultSprings(builder2);
		BiomeDefaultFeatures.addSurfaceFreezing(builder2);
		return new Biome.BiomeBuilder()
			.precipitation(Biome.Precipitation.NONE)
			.biomeCategory(Biome.BiomeCategory.MESA)
			.depth(f)
			.scale(g)
			.temperature(2.0F)
			.downfall(0.0F)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(4159204)
					.waterFogColor(329011)
					.fogColor(12638463)
					.skyColor(calculateSkyColor(2.0F))
					.foliageColorOverride(10387789)
					.grassColorOverride(9470285)
					.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
					.build()
			)
			.mobSpawnSettings(builder.build())
			.generationSettings(builder2.build())
			.build();
	}

	public static Biome badlandsBiome(float f, float g, boolean bl) {
		return baseBadlandsBiome(SurfaceBuilders.BADLANDS, f, g, bl, false);
	}

	public static Biome woodedBadlandsPlateauBiome(float f, float g) {
		return baseBadlandsBiome(SurfaceBuilders.WOODED_BADLANDS, f, g, true, true);
	}

	public static Biome erodedBadlandsBiome() {
		return baseBadlandsBiome(SurfaceBuilders.ERODED_BADLANDS, 0.1F, 0.2F, true, false);
	}

	private static Biome baseOceanBiome(MobSpawnSettings.Builder builder, int i, int j, boolean bl, BiomeGenerationSettings.Builder builder2) {
		return new Biome.BiomeBuilder()
			.precipitation(Biome.Precipitation.RAIN)
			.biomeCategory(Biome.BiomeCategory.OCEAN)
			.depth(bl ? -1.8F : -1.0F)
			.scale(0.1F)
			.temperature(0.5F)
			.downfall(0.5F)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(i)
					.waterFogColor(j)
					.fogColor(12638463)
					.skyColor(calculateSkyColor(0.5F))
					.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
					.build()
			)
			.mobSpawnSettings(builder.build())
			.generationSettings(builder2.build())
			.build();
	}

	private static BiomeGenerationSettings.Builder baseOceanGeneration(
		ConfiguredSurfaceBuilder<SurfaceBuilderBaseConfiguration> configuredSurfaceBuilder, boolean bl, boolean bl2, boolean bl3
	) {
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder().surfaceBuilder(configuredSurfaceBuilder);
		ConfiguredStructureFeature<?, ?> configuredStructureFeature = bl2 ? StructureFeatures.OCEAN_RUIN_WARM : StructureFeatures.OCEAN_RUIN_COLD;
		if (bl3) {
			if (bl) {
				builder.addStructureStart(StructureFeatures.OCEAN_MONUMENT);
			}

			BiomeDefaultFeatures.addDefaultOverworldOceanStructures(builder);
			builder.addStructureStart(configuredStructureFeature);
		} else {
			builder.addStructureStart(configuredStructureFeature);
			if (bl) {
				builder.addStructureStart(StructureFeatures.OCEAN_MONUMENT);
			}

			BiomeDefaultFeatures.addDefaultOverworldOceanStructures(builder);
		}

		builder.addStructureStart(StructureFeatures.RUINED_PORTAL_OCEAN);
		BiomeDefaultFeatures.addOceanCarvers(builder);
		BiomeDefaultFeatures.addDefaultLakes(builder);
		BiomeDefaultFeatures.addDefaultCrystalFormations(builder);
		BiomeDefaultFeatures.addDefaultMonsterRoom(builder);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(builder, true);
		BiomeDefaultFeatures.addDefaultOres(builder);
		BiomeDefaultFeatures.addDefaultSoftDisks(builder);
		BiomeDefaultFeatures.addWaterTrees(builder);
		BiomeDefaultFeatures.addDefaultFlowers(builder);
		BiomeDefaultFeatures.addDefaultGrass(builder);
		BiomeDefaultFeatures.addDefaultMushrooms(builder);
		BiomeDefaultFeatures.addDefaultExtraVegetation(builder);
		BiomeDefaultFeatures.addDefaultSprings(builder);
		return builder;
	}

	public static Biome coldOceanBiome(boolean bl) {
		MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
		BiomeDefaultFeatures.oceanSpawns(builder, 3, 4, 15);
		builder.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 15, 1, 5));
		boolean bl2 = !bl;
		BiomeGenerationSettings.Builder builder2 = baseOceanGeneration(SurfaceBuilders.GRASS, bl, false, bl2);
		builder2.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, bl ? Features.SEAGRASS_DEEP_COLD : Features.SEAGRASS_COLD);
		BiomeDefaultFeatures.addDefaultSeagrass(builder2);
		BiomeDefaultFeatures.addColdOceanExtraVegetation(builder2);
		BiomeDefaultFeatures.addSurfaceFreezing(builder2);
		return baseOceanBiome(builder, 4020182, 329011, bl, builder2);
	}

	public static Biome oceanBiome(boolean bl) {
		MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
		BiomeDefaultFeatures.oceanSpawns(builder, 1, 4, 10);
		builder.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DOLPHIN, 1, 1, 2));
		BiomeGenerationSettings.Builder builder2 = baseOceanGeneration(SurfaceBuilders.GRASS, bl, false, true);
		builder2.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, bl ? Features.SEAGRASS_DEEP : Features.SEAGRASS_NORMAL);
		BiomeDefaultFeatures.addDefaultSeagrass(builder2);
		BiomeDefaultFeatures.addColdOceanExtraVegetation(builder2);
		BiomeDefaultFeatures.addSurfaceFreezing(builder2);
		return baseOceanBiome(builder, 4159204, 329011, bl, builder2);
	}

	public static Biome lukeWarmOceanBiome(boolean bl) {
		MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
		if (bl) {
			BiomeDefaultFeatures.oceanSpawns(builder, 8, 4, 8);
		} else {
			BiomeDefaultFeatures.oceanSpawns(builder, 10, 2, 15);
		}

		builder.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.PUFFERFISH, 5, 1, 3))
			.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.TROPICAL_FISH, 25, 8, 8))
			.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DOLPHIN, 2, 1, 2));
		BiomeGenerationSettings.Builder builder2 = baseOceanGeneration(SurfaceBuilders.OCEAN_SAND, bl, true, false);
		builder2.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, bl ? Features.SEAGRASS_DEEP_WARM : Features.SEAGRASS_WARM);
		if (bl) {
			BiomeDefaultFeatures.addDefaultSeagrass(builder2);
		}

		BiomeDefaultFeatures.addLukeWarmKelp(builder2);
		BiomeDefaultFeatures.addSurfaceFreezing(builder2);
		return baseOceanBiome(builder, 4566514, 267827, bl, builder2);
	}

	public static Biome warmOceanBiome() {
		MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder()
			.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.PUFFERFISH, 15, 1, 3));
		BiomeDefaultFeatures.warmOceanSpawns(builder, 10, 4);
		BiomeGenerationSettings.Builder builder2 = baseOceanGeneration(SurfaceBuilders.FULL_SAND, false, true, false)
			.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.WARM_OCEAN_VEGETATION)
			.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_WARM)
			.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEA_PICKLE);
		BiomeDefaultFeatures.addSurfaceFreezing(builder2);
		return baseOceanBiome(builder, 4445678, 270131, false, builder2);
	}

	public static Biome deepWarmOceanBiome() {
		MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
		BiomeDefaultFeatures.warmOceanSpawns(builder, 5, 1);
		builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 5, 1, 1));
		BiomeGenerationSettings.Builder builder2 = baseOceanGeneration(SurfaceBuilders.FULL_SAND, true, true, false)
			.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_DEEP_WARM);
		BiomeDefaultFeatures.addDefaultSeagrass(builder2);
		BiomeDefaultFeatures.addSurfaceFreezing(builder2);
		return baseOceanBiome(builder, 4445678, 270131, true, builder2);
	}

	public static Biome frozenOceanBiome(boolean bl) {
		MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder()
			.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, 1, 1, 4))
			.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 15, 1, 5))
			.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.POLAR_BEAR, 1, 1, 2));
		BiomeDefaultFeatures.commonSpawns(builder);
		builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 5, 1, 1));
		float f = bl ? 0.5F : 0.0F;
		BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.FROZEN_OCEAN);
		builder2.addStructureStart(StructureFeatures.OCEAN_RUIN_COLD);
		if (bl) {
			builder2.addStructureStart(StructureFeatures.OCEAN_MONUMENT);
		}

		BiomeDefaultFeatures.addDefaultOverworldOceanStructures(builder2);
		builder2.addStructureStart(StructureFeatures.RUINED_PORTAL_OCEAN);
		BiomeDefaultFeatures.addOceanCarvers(builder2);
		BiomeDefaultFeatures.addDefaultLakes(builder2);
		BiomeDefaultFeatures.addIcebergs(builder2);
		BiomeDefaultFeatures.addDefaultCrystalFormations(builder2);
		BiomeDefaultFeatures.addDefaultMonsterRoom(builder2);
		BiomeDefaultFeatures.addBlueIce(builder2);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(builder2, true);
		BiomeDefaultFeatures.addDefaultOres(builder2);
		BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
		BiomeDefaultFeatures.addWaterTrees(builder2);
		BiomeDefaultFeatures.addDefaultFlowers(builder2);
		BiomeDefaultFeatures.addDefaultGrass(builder2);
		BiomeDefaultFeatures.addDefaultMushrooms(builder2);
		BiomeDefaultFeatures.addDefaultExtraVegetation(builder2);
		BiomeDefaultFeatures.addDefaultSprings(builder2);
		BiomeDefaultFeatures.addSurfaceFreezing(builder2);
		return new Biome.BiomeBuilder()
			.precipitation(bl ? Biome.Precipitation.RAIN : Biome.Precipitation.SNOW)
			.biomeCategory(Biome.BiomeCategory.OCEAN)
			.depth(bl ? -1.8F : -1.0F)
			.scale(0.1F)
			.temperature(f)
			.temperatureAdjustment(Biome.TemperatureModifier.FROZEN)
			.downfall(0.5F)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(3750089)
					.waterFogColor(329011)
					.fogColor(12638463)
					.skyColor(calculateSkyColor(f))
					.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
					.build()
			)
			.mobSpawnSettings(builder.build())
			.generationSettings(builder2.build())
			.build();
	}

	private static Biome baseForestBiome(float f, float g, boolean bl, MobSpawnSettings.Builder builder) {
		BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.GRASS);
		BiomeDefaultFeatures.addDefaultOverworldLandStructures(builder2);
		builder2.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
		BiomeDefaultFeatures.addDefaultCarvers(builder2);
		BiomeDefaultFeatures.addDefaultLakes(builder2);
		BiomeDefaultFeatures.addDefaultCrystalFormations(builder2);
		BiomeDefaultFeatures.addDefaultMonsterRoom(builder2);
		if (bl) {
			builder2.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FOREST_FLOWER_VEGETATION_COMMON);
		} else {
			BiomeDefaultFeatures.addForestFlowers(builder2);
		}

		BiomeDefaultFeatures.addDefaultUndergroundVariety(builder2);
		BiomeDefaultFeatures.addDefaultOres(builder2);
		BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
		if (bl) {
			builder2.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FOREST_FLOWER_TREES);
			builder2.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_FOREST);
			BiomeDefaultFeatures.addDefaultGrass(builder2);
		} else {
			BiomeDefaultFeatures.addOtherBirchTrees(builder2);
			BiomeDefaultFeatures.addDefaultFlowers(builder2);
			BiomeDefaultFeatures.addForestGrass(builder2);
		}

		BiomeDefaultFeatures.addDefaultMushrooms(builder2);
		BiomeDefaultFeatures.addDefaultExtraVegetation(builder2);
		BiomeDefaultFeatures.addDefaultSprings(builder2);
		BiomeDefaultFeatures.addSurfaceFreezing(builder2);
		return new Biome.BiomeBuilder()
			.precipitation(Biome.Precipitation.RAIN)
			.biomeCategory(Biome.BiomeCategory.FOREST)
			.depth(f)
			.scale(g)
			.temperature(0.7F)
			.downfall(0.8F)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(4159204)
					.waterFogColor(329011)
					.fogColor(12638463)
					.skyColor(calculateSkyColor(0.7F))
					.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
					.build()
			)
			.mobSpawnSettings(builder.build())
			.generationSettings(builder2.build())
			.build();
	}

	private static MobSpawnSettings.Builder defaultSpawns() {
		MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
		BiomeDefaultFeatures.farmAnimals(builder);
		BiomeDefaultFeatures.commonSpawns(builder);
		return builder;
	}

	public static Biome forestBiome(float f, float g) {
		MobSpawnSettings.Builder builder = defaultSpawns()
			.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 5, 4, 4))
			.setPlayerCanSpawn();
		return baseForestBiome(f, g, false, builder);
	}

	public static Biome flowerForestBiome() {
		MobSpawnSettings.Builder builder = defaultSpawns().addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3));
		return baseForestBiome(0.1F, 0.4F, true, builder);
	}

	public static Biome taigaBiome(float f, float g, boolean bl, boolean bl2, boolean bl3, boolean bl4) {
		MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
		BiomeDefaultFeatures.farmAnimals(builder);
		builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 8, 4, 4))
			.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3))
			.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.FOX, 8, 2, 4));
		if (!bl && !bl2) {
			builder.setPlayerCanSpawn();
		}

		BiomeDefaultFeatures.commonSpawns(builder);
		float h = bl ? -0.5F : 0.25F;
		BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.GRASS);
		if (bl3) {
			builder2.addStructureStart(StructureFeatures.VILLAGE_TAIGA);
			builder2.addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
		}

		if (bl4) {
			builder2.addStructureStart(StructureFeatures.IGLOO);
		}

		BiomeDefaultFeatures.addDefaultOverworldLandStructures(builder2);
		builder2.addStructureStart(bl2 ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
		BiomeDefaultFeatures.addDefaultCarvers(builder2);
		BiomeDefaultFeatures.addDefaultLakes(builder2);
		BiomeDefaultFeatures.addDefaultCrystalFormations(builder2);
		BiomeDefaultFeatures.addDefaultMonsterRoom(builder2);
		BiomeDefaultFeatures.addFerns(builder2);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(builder2);
		BiomeDefaultFeatures.addDefaultOres(builder2);
		BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
		BiomeDefaultFeatures.addTaigaTrees(builder2);
		BiomeDefaultFeatures.addDefaultFlowers(builder2);
		BiomeDefaultFeatures.addTaigaGrass(builder2);
		BiomeDefaultFeatures.addDefaultMushrooms(builder2);
		BiomeDefaultFeatures.addDefaultExtraVegetation(builder2);
		BiomeDefaultFeatures.addDefaultSprings(builder2);
		if (bl) {
			BiomeDefaultFeatures.addBerryBushes(builder2);
		} else {
			BiomeDefaultFeatures.addSparseBerryBushes(builder2);
		}

		BiomeDefaultFeatures.addSurfaceFreezing(builder2);
		return new Biome.BiomeBuilder()
			.precipitation(bl ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN)
			.biomeCategory(Biome.BiomeCategory.TAIGA)
			.depth(f)
			.scale(g)
			.temperature(h)
			.downfall(bl ? 0.4F : 0.8F)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(bl ? 4020182 : 4159204)
					.waterFogColor(329011)
					.fogColor(12638463)
					.skyColor(calculateSkyColor(h))
					.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
					.build()
			)
			.mobSpawnSettings(builder.build())
			.generationSettings(builder2.build())
			.build();
	}

	public static Biome darkForestBiome(float f, float g, boolean bl) {
		MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
		BiomeDefaultFeatures.farmAnimals(builder);
		BiomeDefaultFeatures.commonSpawns(builder);
		BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.GRASS);
		builder2.addStructureStart(StructureFeatures.WOODLAND_MANSION);
		BiomeDefaultFeatures.addDefaultOverworldLandStructures(builder2);
		builder2.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
		BiomeDefaultFeatures.addDefaultCarvers(builder2);
		BiomeDefaultFeatures.addDefaultLakes(builder2);
		BiomeDefaultFeatures.addDefaultCrystalFormations(builder2);
		BiomeDefaultFeatures.addDefaultMonsterRoom(builder2);
		builder2.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, bl ? Features.DARK_FOREST_VEGETATION_RED : Features.DARK_FOREST_VEGETATION_BROWN);
		BiomeDefaultFeatures.addForestFlowers(builder2);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(builder2);
		BiomeDefaultFeatures.addDefaultOres(builder2);
		BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
		BiomeDefaultFeatures.addDefaultFlowers(builder2);
		BiomeDefaultFeatures.addForestGrass(builder2);
		BiomeDefaultFeatures.addDefaultMushrooms(builder2);
		BiomeDefaultFeatures.addDefaultExtraVegetation(builder2);
		BiomeDefaultFeatures.addDefaultSprings(builder2);
		BiomeDefaultFeatures.addSurfaceFreezing(builder2);
		return new Biome.BiomeBuilder()
			.precipitation(Biome.Precipitation.RAIN)
			.biomeCategory(Biome.BiomeCategory.FOREST)
			.depth(f)
			.scale(g)
			.temperature(0.7F)
			.downfall(0.8F)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(4159204)
					.waterFogColor(329011)
					.fogColor(12638463)
					.skyColor(calculateSkyColor(0.7F))
					.grassColorModifier(BiomeSpecialEffects.GrassColorModifier.DARK_FOREST)
					.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
					.build()
			)
			.mobSpawnSettings(builder.build())
			.generationSettings(builder2.build())
			.build();
	}

	public static Biome swampBiome(float f, float g, boolean bl) {
		MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
		BiomeDefaultFeatures.farmAnimals(builder);
		BiomeDefaultFeatures.commonSpawns(builder);
		builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SLIME, 1, 1, 1));
		BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.SWAMP);
		if (!bl) {
			builder2.addStructureStart(StructureFeatures.SWAMP_HUT);
		}

		builder2.addStructureStart(StructureFeatures.MINESHAFT);
		builder2.addStructureStart(StructureFeatures.RUINED_PORTAL_SWAMP);
		BiomeDefaultFeatures.addDefaultCarvers(builder2);
		if (!bl) {
			BiomeDefaultFeatures.addFossilDecoration(builder2);
		}

		BiomeDefaultFeatures.addDefaultLakes(builder2);
		BiomeDefaultFeatures.addDefaultCrystalFormations(builder2);
		BiomeDefaultFeatures.addDefaultMonsterRoom(builder2);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(builder2);
		BiomeDefaultFeatures.addDefaultOres(builder2);
		BiomeDefaultFeatures.addSwampClayDisk(builder2);
		BiomeDefaultFeatures.addSwampVegetation(builder2);
		BiomeDefaultFeatures.addDefaultMushrooms(builder2);
		BiomeDefaultFeatures.addSwampExtraVegetation(builder2);
		BiomeDefaultFeatures.addDefaultSprings(builder2);
		if (bl) {
			BiomeDefaultFeatures.addFossilDecoration(builder2);
		} else {
			builder2.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_SWAMP);
		}

		BiomeDefaultFeatures.addSurfaceFreezing(builder2);
		return new Biome.BiomeBuilder()
			.precipitation(Biome.Precipitation.RAIN)
			.biomeCategory(Biome.BiomeCategory.SWAMP)
			.depth(f)
			.scale(g)
			.temperature(0.8F)
			.downfall(0.9F)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(6388580)
					.waterFogColor(2302743)
					.fogColor(12638463)
					.skyColor(calculateSkyColor(0.8F))
					.foliageColorOverride(6975545)
					.grassColorModifier(BiomeSpecialEffects.GrassColorModifier.SWAMP)
					.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
					.build()
			)
			.mobSpawnSettings(builder.build())
			.generationSettings(builder2.build())
			.build();
	}

	public static Biome tundraBiome(float f, float g, boolean bl, boolean bl2) {
		MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder().creatureGenerationProbability(0.07F);
		BiomeDefaultFeatures.snowySpawns(builder);
		BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder().surfaceBuilder(bl ? SurfaceBuilders.ICE_SPIKES : SurfaceBuilders.GRASS);
		if (!bl && !bl2) {
			builder2.addStructureStart(StructureFeatures.VILLAGE_SNOWY).addStructureStart(StructureFeatures.IGLOO);
		}

		BiomeDefaultFeatures.addDefaultOverworldLandStructures(builder2);
		if (!bl && !bl2) {
			builder2.addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
		}

		builder2.addStructureStart(bl2 ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
		BiomeDefaultFeatures.addDefaultCarvers(builder2);
		BiomeDefaultFeatures.addDefaultLakes(builder2);
		BiomeDefaultFeatures.addDefaultCrystalFormations(builder2);
		BiomeDefaultFeatures.addDefaultMonsterRoom(builder2);
		if (bl) {
			builder2.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.ICE_SPIKE);
			builder2.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.ICE_PATCH);
		}

		BiomeDefaultFeatures.addDefaultUndergroundVariety(builder2);
		BiomeDefaultFeatures.addDefaultOres(builder2);
		BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
		BiomeDefaultFeatures.addSnowyTrees(builder2);
		BiomeDefaultFeatures.addDefaultFlowers(builder2);
		BiomeDefaultFeatures.addDefaultGrass(builder2);
		BiomeDefaultFeatures.addDefaultMushrooms(builder2);
		BiomeDefaultFeatures.addDefaultExtraVegetation(builder2);
		BiomeDefaultFeatures.addDefaultSprings(builder2);
		BiomeDefaultFeatures.addSurfaceFreezing(builder2);
		return new Biome.BiomeBuilder()
			.precipitation(Biome.Precipitation.SNOW)
			.biomeCategory(Biome.BiomeCategory.ICY)
			.depth(f)
			.scale(g)
			.temperature(0.0F)
			.downfall(0.5F)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(4159204)
					.waterFogColor(329011)
					.fogColor(12638463)
					.skyColor(calculateSkyColor(0.0F))
					.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
					.build()
			)
			.mobSpawnSettings(builder.build())
			.generationSettings(builder2.build())
			.build();
	}

	public static Biome riverBiome(float f, float g, float h, int i, boolean bl) {
		MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder()
			.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, 2, 1, 4))
			.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 5, 1, 5));
		BiomeDefaultFeatures.commonSpawns(builder);
		builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, bl ? 1 : 100, 1, 1));
		BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.GRASS);
		builder2.addStructureStart(StructureFeatures.MINESHAFT);
		builder2.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
		BiomeDefaultFeatures.addDefaultCarvers(builder2);
		BiomeDefaultFeatures.addDefaultLakes(builder2);
		BiomeDefaultFeatures.addDefaultCrystalFormations(builder2);
		BiomeDefaultFeatures.addDefaultMonsterRoom(builder2);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(builder2);
		BiomeDefaultFeatures.addDefaultOres(builder2);
		BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
		BiomeDefaultFeatures.addWaterTrees(builder2);
		BiomeDefaultFeatures.addDefaultFlowers(builder2);
		BiomeDefaultFeatures.addDefaultGrass(builder2);
		BiomeDefaultFeatures.addDefaultMushrooms(builder2);
		BiomeDefaultFeatures.addDefaultExtraVegetation(builder2);
		BiomeDefaultFeatures.addDefaultSprings(builder2);
		if (!bl) {
			builder2.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_RIVER);
		}

		BiomeDefaultFeatures.addSurfaceFreezing(builder2);
		return new Biome.BiomeBuilder()
			.precipitation(bl ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN)
			.biomeCategory(Biome.BiomeCategory.RIVER)
			.depth(f)
			.scale(g)
			.temperature(h)
			.downfall(0.5F)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(i)
					.waterFogColor(329011)
					.fogColor(12638463)
					.skyColor(calculateSkyColor(h))
					.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
					.build()
			)
			.mobSpawnSettings(builder.build())
			.generationSettings(builder2.build())
			.build();
	}

	public static Biome beachBiome(float f, float g, float h, float i, int j, boolean bl, boolean bl2) {
		MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
		if (!bl2 && !bl) {
			builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.TURTLE, 5, 2, 5));
		}

		BiomeDefaultFeatures.commonSpawns(builder);
		BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder().surfaceBuilder(bl2 ? SurfaceBuilders.STONE : SurfaceBuilders.DESERT);
		if (bl2) {
			BiomeDefaultFeatures.addDefaultOverworldLandStructures(builder2);
		} else {
			builder2.addStructureStart(StructureFeatures.MINESHAFT);
			builder2.addStructureStart(StructureFeatures.BURIED_TREASURE);
			builder2.addStructureStart(StructureFeatures.SHIPWRECH_BEACHED);
		}

		builder2.addStructureStart(bl2 ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
		BiomeDefaultFeatures.addDefaultCarvers(builder2);
		BiomeDefaultFeatures.addDefaultLakes(builder2);
		BiomeDefaultFeatures.addDefaultCrystalFormations(builder2);
		BiomeDefaultFeatures.addDefaultMonsterRoom(builder2);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(builder2);
		BiomeDefaultFeatures.addDefaultOres(builder2);
		BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
		BiomeDefaultFeatures.addDefaultFlowers(builder2);
		BiomeDefaultFeatures.addDefaultGrass(builder2);
		BiomeDefaultFeatures.addDefaultMushrooms(builder2);
		BiomeDefaultFeatures.addDefaultExtraVegetation(builder2);
		BiomeDefaultFeatures.addDefaultSprings(builder2);
		BiomeDefaultFeatures.addSurfaceFreezing(builder2);
		return new Biome.BiomeBuilder()
			.precipitation(bl ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN)
			.biomeCategory(bl2 ? Biome.BiomeCategory.NONE : Biome.BiomeCategory.BEACH)
			.depth(f)
			.scale(g)
			.temperature(h)
			.downfall(i)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(j)
					.waterFogColor(329011)
					.fogColor(12638463)
					.skyColor(calculateSkyColor(h))
					.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
					.build()
			)
			.mobSpawnSettings(builder.build())
			.generationSettings(builder2.build())
			.build();
	}

	public static Biome theVoidBiome() {
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.NOPE);
		builder.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, Features.VOID_START_PLATFORM);
		return new Biome.BiomeBuilder()
			.precipitation(Biome.Precipitation.NONE)
			.biomeCategory(Biome.BiomeCategory.NONE)
			.depth(0.1F)
			.scale(0.2F)
			.temperature(0.5F)
			.downfall(0.5F)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(4159204)
					.waterFogColor(329011)
					.fogColor(12638463)
					.skyColor(calculateSkyColor(0.5F))
					.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
					.build()
			)
			.mobSpawnSettings(MobSpawnSettings.EMPTY)
			.generationSettings(builder.build())
			.build();
	}

	public static Biome netherWastesBiome() {
		MobSpawnSettings mobSpawnSettings = new MobSpawnSettings.Builder()
			.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.GHAST, 50, 4, 4))
			.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 100, 4, 4))
			.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.MAGMA_CUBE, 2, 4, 4))
			.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 1, 4, 4))
			.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.PIGLIN, 15, 4, 4))
			.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2))
			.build();
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder()
			.surfaceBuilder(SurfaceBuilders.NETHER)
			.addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER)
			.addStructureStart(StructureFeatures.NETHER_BRIDGE)
			.addStructureStart(StructureFeatures.BASTION_REMNANT)
			.addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE)
			.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
		BiomeDefaultFeatures.addDefaultMushrooms(builder);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.BROWN_MUSHROOM_NETHER)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.RED_MUSHROOM_NETHER)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED);
		BiomeDefaultFeatures.addNetherDefaultOres(builder);
		return new Biome.BiomeBuilder()
			.precipitation(Biome.Precipitation.NONE)
			.biomeCategory(Biome.BiomeCategory.NETHER)
			.depth(0.1F)
			.scale(0.2F)
			.temperature(2.0F)
			.downfall(0.0F)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(4159204)
					.waterFogColor(329011)
					.fogColor(3344392)
					.skyColor(calculateSkyColor(2.0F))
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

	public static Biome soulSandValleyBiome() {
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
			.surfaceBuilder(SurfaceBuilders.SOUL_SAND_VALLEY)
			.addStructureStart(StructureFeatures.NETHER_BRIDGE)
			.addStructureStart(StructureFeatures.NETHER_FOSSIL)
			.addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER)
			.addStructureStart(StructureFeatures.BASTION_REMNANT)
			.addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE)
			.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA)
			.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.BASALT_PILLAR)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_CRIMSON_ROOTS)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_SOUL_SAND);
		BiomeDefaultFeatures.addNetherDefaultOres(builder);
		return new Biome.BiomeBuilder()
			.precipitation(Biome.Precipitation.NONE)
			.biomeCategory(Biome.BiomeCategory.NETHER)
			.depth(0.1F)
			.scale(0.2F)
			.temperature(2.0F)
			.downfall(0.0F)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(4159204)
					.waterFogColor(329011)
					.fogColor(1787717)
					.skyColor(calculateSkyColor(2.0F))
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

	public static Biome basaltDeltasBiome() {
		MobSpawnSettings mobSpawnSettings = new MobSpawnSettings.Builder()
			.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.GHAST, 40, 1, 1))
			.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.MAGMA_CUBE, 100, 2, 5))
			.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2))
			.build();
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder()
			.surfaceBuilder(SurfaceBuilders.BASALT_DELTAS)
			.addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER)
			.addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE)
			.addStructureStart(StructureFeatures.NETHER_BRIDGE)
			.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.DELTA)
			.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA_DOUBLE)
			.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.SMALL_BASALT_COLUMNS)
			.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.LARGE_BASALT_COLUMNS)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.BASALT_BLOBS)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.BLACKSTONE_BLOBS)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_DELTA)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.BROWN_MUSHROOM_NETHER)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.RED_MUSHROOM_NETHER)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED_DOUBLE)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_GOLD_DELTAS)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_QUARTZ_DELTAS);
		BiomeDefaultFeatures.addAncientDebris(builder);
		return new Biome.BiomeBuilder()
			.precipitation(Biome.Precipitation.NONE)
			.biomeCategory(Biome.BiomeCategory.NETHER)
			.depth(0.1F)
			.scale(0.2F)
			.temperature(2.0F)
			.downfall(0.0F)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(4159204)
					.waterFogColor(4341314)
					.fogColor(6840176)
					.skyColor(calculateSkyColor(2.0F))
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

	public static Biome crimsonForestBiome() {
		MobSpawnSettings mobSpawnSettings = new MobSpawnSettings.Builder()
			.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 1, 2, 4))
			.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.HOGLIN, 9, 3, 4))
			.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.PIGLIN, 5, 3, 4))
			.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2))
			.build();
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder()
			.surfaceBuilder(SurfaceBuilders.CRIMSON_FOREST)
			.addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER)
			.addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE)
			.addStructureStart(StructureFeatures.NETHER_BRIDGE)
			.addStructureStart(StructureFeatures.BASTION_REMNANT)
			.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
		BiomeDefaultFeatures.addDefaultMushrooms(builder);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED)
			.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.WEEPING_VINES)
			.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.CRIMSON_FUNGI)
			.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.CRIMSON_FOREST_VEGETATION);
		BiomeDefaultFeatures.addNetherDefaultOres(builder);
		return new Biome.BiomeBuilder()
			.precipitation(Biome.Precipitation.NONE)
			.biomeCategory(Biome.BiomeCategory.NETHER)
			.depth(0.1F)
			.scale(0.2F)
			.temperature(2.0F)
			.downfall(0.0F)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(4159204)
					.waterFogColor(329011)
					.fogColor(3343107)
					.skyColor(calculateSkyColor(2.0F))
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

	public static Biome warpedForestBiome() {
		MobSpawnSettings mobSpawnSettings = new MobSpawnSettings.Builder()
			.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 1, 4, 4))
			.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2))
			.addMobCharge(EntityType.ENDERMAN, 1.0, 0.12)
			.build();
		BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder()
			.surfaceBuilder(SurfaceBuilders.WARPED_FOREST)
			.addStructureStart(StructureFeatures.NETHER_BRIDGE)
			.addStructureStart(StructureFeatures.BASTION_REMNANT)
			.addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER)
			.addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE)
			.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
		BiomeDefaultFeatures.addDefaultMushrooms(builder);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA)
			.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED)
			.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.WARPED_FUNGI)
			.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.WARPED_FOREST_VEGETATION)
			.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.NETHER_SPROUTS)
			.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TWISTING_VINES);
		BiomeDefaultFeatures.addNetherDefaultOres(builder);
		return new Biome.BiomeBuilder()
			.precipitation(Biome.Precipitation.NONE)
			.biomeCategory(Biome.BiomeCategory.NETHER)
			.depth(0.1F)
			.scale(0.2F)
			.temperature(2.0F)
			.downfall(0.0F)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(4159204)
					.waterFogColor(329011)
					.fogColor(1705242)
					.skyColor(calculateSkyColor(2.0F))
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

	public static Biome lushCaves() {
		MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
		BiomeDefaultFeatures.commonSpawns(builder);
		BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.GRASS);
		BiomeDefaultFeatures.addDefaultOverworldLandStructures(builder2);
		builder2.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
		BiomeDefaultFeatures.addDefaultCarvers(builder2);
		BiomeDefaultFeatures.addDefaultLakes(builder2);
		BiomeDefaultFeatures.addDefaultCrystalFormations(builder2);
		BiomeDefaultFeatures.addDefaultMonsterRoom(builder2);
		BiomeDefaultFeatures.addPlainGrass(builder2);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(builder2);
		BiomeDefaultFeatures.addDefaultOres(builder2);
		BiomeDefaultFeatures.addLushCavesSpecialOres(builder2);
		BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
		BiomeDefaultFeatures.addLushCavesVegetationFeatures(builder2);
		return new Biome.BiomeBuilder()
			.precipitation(Biome.Precipitation.RAIN)
			.biomeCategory(Biome.BiomeCategory.UNDERGROUND)
			.depth(0.1F)
			.scale(0.2F)
			.temperature(0.5F)
			.downfall(0.5F)
			.specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(calculateSkyColor(0.5F)).build())
			.mobSpawnSettings(builder.build())
			.generationSettings(builder2.build())
			.build();
	}

	public static Biome dripstoneCaves() {
		MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
		BiomeDefaultFeatures.commonSpawns(builder);
		BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder().surfaceBuilder(SurfaceBuilders.GRASS);
		BiomeDefaultFeatures.addDefaultOverworldLandStructures(builder2);
		builder2.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
		BiomeDefaultFeatures.addDefaultCarvers(builder2);
		BiomeDefaultFeatures.addDefaultLakes(builder2);
		BiomeDefaultFeatures.addDefaultCrystalFormations(builder2);
		BiomeDefaultFeatures.addDefaultMonsterRoom(builder2);
		BiomeDefaultFeatures.addPlainGrass(builder2);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(builder2);
		BiomeDefaultFeatures.addDefaultOres(builder2);
		BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
		BiomeDefaultFeatures.addPlainVegetation(builder2);
		BiomeDefaultFeatures.addDefaultMushrooms(builder2);
		BiomeDefaultFeatures.addDefaultExtraVegetation(builder2);
		BiomeDefaultFeatures.addDefaultSprings(builder2);
		BiomeDefaultFeatures.addSurfaceFreezing(builder2);
		BiomeDefaultFeatures.addDripstone(builder2);
		return new Biome.BiomeBuilder()
			.precipitation(Biome.Precipitation.RAIN)
			.biomeCategory(Biome.BiomeCategory.UNDERGROUND)
			.depth(0.125F)
			.scale(0.05F)
			.temperature(0.8F)
			.downfall(0.4F)
			.specialEffects(
				new BiomeSpecialEffects.Builder()
					.waterColor(4159204)
					.waterFogColor(329011)
					.fogColor(12638463)
					.skyColor(calculateSkyColor(0.8F))
					.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
					.build()
			)
			.mobSpawnSettings(builder.build())
			.generationSettings(builder2.build())
			.build();
	}
}
