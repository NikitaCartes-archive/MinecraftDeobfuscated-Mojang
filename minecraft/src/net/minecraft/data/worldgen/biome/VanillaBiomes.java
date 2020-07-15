package net.minecraft.data.worldgen.biome;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.Features;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.data.worldgen.SurfaceBuilders;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilderBaseConfiguration;

public class VanillaBiomes {
	public static Biome giantTreeTaiga(float f, float g, float h, boolean bl, @Nullable String string) {
		Biome biome = new Biome(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilders.GIANT_TREE_TAIGA)
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
						.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
						.build()
				)
				.parent(string)
		);
		BiomeDefaultFeatures.addDefaultOverworldLandStructures(biome);
		biome.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
		BiomeDefaultFeatures.addDefaultCarvers(biome);
		BiomeDefaultFeatures.addDefaultLakes(biome);
		BiomeDefaultFeatures.addDefaultMonsterRoom(biome);
		BiomeDefaultFeatures.addMossyStoneBlock(biome);
		BiomeDefaultFeatures.addFerns(biome);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(biome);
		BiomeDefaultFeatures.addDefaultOres(biome);
		BiomeDefaultFeatures.addDefaultSoftDisks(biome);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, bl ? Features.TREES_GIANT_SPRUCE : Features.TREES_GIANT);
		BiomeDefaultFeatures.addDefaultFlowers(biome);
		BiomeDefaultFeatures.addGiantTaigaVegetation(biome);
		BiomeDefaultFeatures.addDefaultMushrooms(biome);
		BiomeDefaultFeatures.addDefaultExtraVegetation(biome);
		BiomeDefaultFeatures.addDefaultSprings(biome);
		BiomeDefaultFeatures.addSparseBerryBushes(biome);
		BiomeDefaultFeatures.addSurfaceFreezing(biome);
		BiomeDefaultFeatures.farmAnimals(biome);
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.WOLF, 8, 4, 4));
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.RABBIT, 4, 2, 3));
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.FOX, 8, 2, 4));
		if (bl) {
			BiomeDefaultFeatures.commonSpawns(biome);
		} else {
			BiomeDefaultFeatures.ambientSpawns(biome);
			BiomeDefaultFeatures.monsters(biome, 100, 25, 100);
		}

		return biome;
	}

	public static Biome birchForestBiome(float f, float g, @Nullable String string, boolean bl) {
		Biome biome = new Biome(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilders.GRASS)
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
						.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
						.build()
				)
				.parent(string)
		);
		BiomeDefaultFeatures.addDefaultOverworldLandStructures(biome);
		biome.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
		BiomeDefaultFeatures.addDefaultCarvers(biome);
		BiomeDefaultFeatures.addDefaultLakes(biome);
		BiomeDefaultFeatures.addDefaultMonsterRoom(biome);
		BiomeDefaultFeatures.addForestFlowers(biome);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(biome);
		BiomeDefaultFeatures.addDefaultOres(biome);
		BiomeDefaultFeatures.addDefaultSoftDisks(biome);
		if (bl) {
			BiomeDefaultFeatures.addTallBirchTrees(biome);
		} else {
			BiomeDefaultFeatures.addBirchTrees(biome);
		}

		BiomeDefaultFeatures.addDefaultFlowers(biome);
		BiomeDefaultFeatures.addForestGrass(biome);
		BiomeDefaultFeatures.addDefaultMushrooms(biome);
		BiomeDefaultFeatures.addDefaultExtraVegetation(biome);
		BiomeDefaultFeatures.addDefaultSprings(biome);
		BiomeDefaultFeatures.addSurfaceFreezing(biome);
		BiomeDefaultFeatures.farmAnimals(biome);
		BiomeDefaultFeatures.commonSpawns(biome);
		return biome;
	}

	public static Biome jungleBiome() {
		return jungleBiome(0.1F, 0.2F, 40, 2, 3);
	}

	public static Biome jungleEdgeBiome() {
		return baseJungleBiome(null, 0.1F, 0.2F, 0.8F, false, true, false);
	}

	public static Biome modifiedJungleEdgeBiome() {
		return baseJungleBiome("jungle_edge", 0.2F, 0.4F, 0.8F, false, true, true);
	}

	public static Biome modifiedJungleBiome() {
		Biome biome = baseJungleBiome("jungle", 0.2F, 0.4F, 0.9F, false, false, true);
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.PARROT, 10, 1, 1));
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.OCELOT, 2, 1, 1));
		return biome;
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
		Biome biome = baseJungleBiome(null, f, g, 0.9F, false, false, false);
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.PARROT, i, 1, j));
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.OCELOT, 2, 1, k));
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.PANDA, 1, 1, 2));
		return biome;
	}

	private static Biome bambooJungleBiome(float f, float g, int i, int j) {
		Biome biome = baseJungleBiome(null, f, g, 0.9F, true, false, false);
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.PARROT, i, 1, j));
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.PANDA, 80, 1, 2));
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.OCELOT, 2, 1, 1));
		return biome;
	}

	private static Biome baseJungleBiome(@Nullable String string, float f, float g, float h, boolean bl, boolean bl2, boolean bl3) {
		Biome biome = new Biome(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilders.GRASS)
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
						.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
						.build()
				)
				.parent(string)
		);
		if (!bl2 && !bl3) {
			biome.addStructureStart(StructureFeatures.JUNGLE_TEMPLE);
		}

		BiomeDefaultFeatures.addDefaultOverworldLandStructures(biome);
		biome.addStructureStart(StructureFeatures.RUINED_PORTAL_JUNGLE);
		BiomeDefaultFeatures.addDefaultCarvers(biome);
		BiomeDefaultFeatures.addDefaultLakes(biome);
		BiomeDefaultFeatures.addDefaultMonsterRoom(biome);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(biome);
		BiomeDefaultFeatures.addDefaultOres(biome);
		BiomeDefaultFeatures.addDefaultSoftDisks(biome);
		if (bl) {
			BiomeDefaultFeatures.addBambooVegetation(biome);
		} else {
			if (!bl2 && !bl3) {
				BiomeDefaultFeatures.addLightBambooVegetation(biome);
			}

			if (bl2) {
				BiomeDefaultFeatures.addJungleEdgeTrees(biome);
			} else {
				BiomeDefaultFeatures.addJungleTrees(biome);
			}
		}

		BiomeDefaultFeatures.addWarmFlowers(biome);
		BiomeDefaultFeatures.addJungleGrass(biome);
		BiomeDefaultFeatures.addDefaultMushrooms(biome);
		BiomeDefaultFeatures.addDefaultExtraVegetation(biome);
		BiomeDefaultFeatures.addDefaultSprings(biome);
		BiomeDefaultFeatures.addJungleExtraVegetation(biome);
		BiomeDefaultFeatures.addSurfaceFreezing(biome);
		BiomeDefaultFeatures.baseJungleSpawns(biome);
		return biome;
	}

	public static Biome mountainBiome(
		float f, float g, ConfiguredSurfaceBuilder<SurfaceBuilderBaseConfiguration> configuredSurfaceBuilder, boolean bl, @Nullable String string
	) {
		Biome biome = new Biome(
			new Biome.BiomeBuilder()
				.surfaceBuilder(configuredSurfaceBuilder)
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
						.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
						.build()
				)
				.parent(string)
		);
		BiomeDefaultFeatures.addDefaultOverworldLandStructures(biome);
		biome.addStructureStart(StructureFeatures.RUINED_PORTAL_MOUNTAIN);
		BiomeDefaultFeatures.addDefaultCarvers(biome);
		BiomeDefaultFeatures.addDefaultLakes(biome);
		BiomeDefaultFeatures.addDefaultMonsterRoom(biome);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(biome);
		BiomeDefaultFeatures.addDefaultOres(biome);
		BiomeDefaultFeatures.addDefaultSoftDisks(biome);
		if (bl) {
			BiomeDefaultFeatures.addMountainEdgeTrees(biome);
		} else {
			BiomeDefaultFeatures.addMountainTrees(biome);
		}

		BiomeDefaultFeatures.addDefaultFlowers(biome);
		BiomeDefaultFeatures.addDefaultGrass(biome);
		BiomeDefaultFeatures.addDefaultMushrooms(biome);
		BiomeDefaultFeatures.addDefaultExtraVegetation(biome);
		BiomeDefaultFeatures.addDefaultSprings(biome);
		BiomeDefaultFeatures.addExtraEmeralds(biome);
		BiomeDefaultFeatures.addInfestedStone(biome);
		BiomeDefaultFeatures.addSurfaceFreezing(biome);
		BiomeDefaultFeatures.farmAnimals(biome);
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.LLAMA, 5, 4, 6));
		BiomeDefaultFeatures.commonSpawns(biome);
		return biome;
	}

	public static Biome desertBiome(@Nullable String string, float f, float g, boolean bl, boolean bl2, boolean bl3) {
		Biome biome = new Biome(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilders.DESERT)
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
						.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
						.build()
				)
				.parent(string)
		);
		if (bl) {
			biome.addStructureStart(StructureFeatures.VILLAGE_DESERT);
			biome.addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
		}

		if (bl2) {
			biome.addStructureStart(StructureFeatures.DESERT_PYRAMID);
		}

		if (bl3) {
			BiomeDefaultFeatures.addFossilDecoration(biome);
		}

		BiomeDefaultFeatures.addDefaultOverworldLandStructures(biome);
		biome.addStructureStart(StructureFeatures.RUINED_PORTAL_DESERT);
		BiomeDefaultFeatures.addDefaultCarvers(biome);
		BiomeDefaultFeatures.addDesertLakes(biome);
		BiomeDefaultFeatures.addDefaultMonsterRoom(biome);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(biome);
		BiomeDefaultFeatures.addDefaultOres(biome);
		BiomeDefaultFeatures.addDefaultSoftDisks(biome);
		BiomeDefaultFeatures.addDefaultFlowers(biome);
		BiomeDefaultFeatures.addDefaultGrass(biome);
		BiomeDefaultFeatures.addDesertVegetation(biome);
		BiomeDefaultFeatures.addDefaultMushrooms(biome);
		BiomeDefaultFeatures.addDesertExtraVegetation(biome);
		BiomeDefaultFeatures.addDefaultSprings(biome);
		BiomeDefaultFeatures.addDesertExtraDecoration(biome);
		BiomeDefaultFeatures.addSurfaceFreezing(biome);
		BiomeDefaultFeatures.desertSpawns(biome);
		return biome;
	}

	public static Biome plainsBiome(@Nullable String string, boolean bl) {
		Biome biome = new Biome(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilders.GRASS)
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
						.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
						.build()
				)
				.parent(string)
		);
		if (!bl) {
			biome.addStructureStart(StructureFeatures.VILLAGE_PLAINS);
			biome.addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
		}

		BiomeDefaultFeatures.addDefaultOverworldLandStructures(biome);
		biome.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
		BiomeDefaultFeatures.addDefaultCarvers(biome);
		BiomeDefaultFeatures.addDefaultLakes(biome);
		BiomeDefaultFeatures.addDefaultMonsterRoom(biome);
		BiomeDefaultFeatures.addPlainGrass(biome);
		if (bl) {
			biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUNFLOWER);
		}

		BiomeDefaultFeatures.addDefaultUndergroundVariety(biome);
		BiomeDefaultFeatures.addDefaultOres(biome);
		BiomeDefaultFeatures.addDefaultSoftDisks(biome);
		BiomeDefaultFeatures.addPlainVegetation(biome);
		if (bl) {
			biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE);
		}

		BiomeDefaultFeatures.addDefaultMushrooms(biome);
		if (bl) {
			biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
		} else {
			BiomeDefaultFeatures.addDefaultExtraVegetation(biome);
		}

		BiomeDefaultFeatures.addDefaultSprings(biome);
		BiomeDefaultFeatures.addSurfaceFreezing(biome);
		BiomeDefaultFeatures.plainsSpawns(biome);
		return biome;
	}

	public static Biome endBarrensBiome() {
		Biome biome = new Biome(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilders.END)
				.precipitation(Biome.Precipitation.NONE)
				.biomeCategory(Biome.BiomeCategory.THEEND)
				.depth(0.1F)
				.scale(0.2F)
				.temperature(0.5F)
				.downfall(0.5F)
				.skyColor(0)
				.specialEffects(
					new BiomeSpecialEffects.Builder()
						.waterColor(4159204)
						.waterFogColor(329011)
						.fogColor(10518688)
						.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
						.build()
				)
				.parent(null)
		);
		BiomeDefaultFeatures.endSpawns(biome);
		return biome;
	}

	public static Biome theEndBiome() {
		Biome biome = endBarrensBiome();
		biome.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.END_SPIKE);
		return biome;
	}

	public static Biome endMidlandsBiome() {
		Biome biome = endBarrensBiome();
		biome.addStructureStart(StructureFeatures.END_CITY);
		return biome;
	}

	public static Biome endHighlandsBiome() {
		Biome biome = endMidlandsBiome();
		biome.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.END_GATEWAY);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.CHORUS_PLANT);
		return biome;
	}

	public static Biome smallEndIslandsBiome() {
		Biome biome = endBarrensBiome();
		biome.addFeature(GenerationStep.Decoration.RAW_GENERATION, Features.END_ISLAND_DECORATED);
		return biome;
	}

	public static Biome mushroomFieldsBiome(float f, float g) {
		Biome biome = new Biome(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilders.MYCELIUM)
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
						.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
						.build()
				)
				.parent(null)
		);
		BiomeDefaultFeatures.addDefaultOverworldLandStructures(biome);
		biome.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
		BiomeDefaultFeatures.addDefaultCarvers(biome);
		BiomeDefaultFeatures.addDefaultLakes(biome);
		BiomeDefaultFeatures.addDefaultMonsterRoom(biome);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(biome);
		BiomeDefaultFeatures.addDefaultOres(biome);
		BiomeDefaultFeatures.addDefaultSoftDisks(biome);
		BiomeDefaultFeatures.addMushroomFieldVegetation(biome);
		BiomeDefaultFeatures.addDefaultMushrooms(biome);
		BiomeDefaultFeatures.addDefaultExtraVegetation(biome);
		BiomeDefaultFeatures.addDefaultSprings(biome);
		BiomeDefaultFeatures.addSurfaceFreezing(biome);
		BiomeDefaultFeatures.mooshroomSpawns(biome);
		return biome;
	}

	public static Biome savannaBiome(@Nullable String string, float f, float g, float h, boolean bl, boolean bl2) {
		Biome biome = new Biome(
			new Biome.BiomeBuilder()
				.surfaceBuilder(bl2 ? SurfaceBuilders.SHATTERED_SAVANNA : SurfaceBuilders.GRASS)
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
						.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
						.build()
				)
				.parent(string)
		);
		if (!bl && !bl2) {
			biome.addStructureStart(StructureFeatures.VILLAGE_SAVANNA);
			biome.addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
		}

		BiomeDefaultFeatures.addDefaultOverworldLandStructures(biome);
		biome.addStructureStart(bl ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
		BiomeDefaultFeatures.addDefaultCarvers(biome);
		BiomeDefaultFeatures.addDefaultLakes(biome);
		BiomeDefaultFeatures.addDefaultMonsterRoom(biome);
		if (!bl2) {
			BiomeDefaultFeatures.addSavannaGrass(biome);
		}

		BiomeDefaultFeatures.addDefaultUndergroundVariety(biome);
		BiomeDefaultFeatures.addDefaultOres(biome);
		BiomeDefaultFeatures.addDefaultSoftDisks(biome);
		if (bl2) {
			BiomeDefaultFeatures.addShatteredSavannaTrees(biome);
			BiomeDefaultFeatures.addDefaultFlowers(biome);
			BiomeDefaultFeatures.addShatteredSavannaGrass(biome);
		} else {
			BiomeDefaultFeatures.addSavannaTrees(biome);
			BiomeDefaultFeatures.addWarmFlowers(biome);
			BiomeDefaultFeatures.addSavannaExtraGrass(biome);
		}

		BiomeDefaultFeatures.addDefaultMushrooms(biome);
		BiomeDefaultFeatures.addDefaultExtraVegetation(biome);
		BiomeDefaultFeatures.addDefaultSprings(biome);
		BiomeDefaultFeatures.addSurfaceFreezing(biome);
		BiomeDefaultFeatures.farmAnimals(biome);
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.HORSE, 1, 2, 6));
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.DONKEY, 1, 1, 1));
		BiomeDefaultFeatures.commonSpawns(biome);
		return biome;
	}

	public static Biome savanaPlateauBiome() {
		Biome biome = savannaBiome(null, 1.5F, 0.025F, 1.0F, true, false);
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.LLAMA, 8, 4, 4));
		return biome;
	}

	private static Biome baseBadlandsBiome(
		@Nullable String string, ConfiguredSurfaceBuilder<SurfaceBuilderBaseConfiguration> configuredSurfaceBuilder, float f, float g, boolean bl, boolean bl2
	) {
		Biome biome = new BadlandsBiome(
			new Biome.BiomeBuilder()
				.surfaceBuilder(configuredSurfaceBuilder)
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
						.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
						.build()
				)
				.parent(string)
		);
		BiomeDefaultFeatures.addDefaultOverworldLandMesaStructures(biome);
		biome.addStructureStart(bl ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
		BiomeDefaultFeatures.addDefaultCarvers(biome);
		BiomeDefaultFeatures.addDefaultLakes(biome);
		BiomeDefaultFeatures.addDefaultMonsterRoom(biome);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(biome);
		BiomeDefaultFeatures.addDefaultOres(biome);
		BiomeDefaultFeatures.addExtraGold(biome);
		BiomeDefaultFeatures.addDefaultSoftDisks(biome);
		if (bl2) {
			BiomeDefaultFeatures.addBadlandsTrees(biome);
		}

		BiomeDefaultFeatures.addBadlandGrass(biome);
		BiomeDefaultFeatures.addDefaultMushrooms(biome);
		BiomeDefaultFeatures.addBadlandExtraVegetation(biome);
		BiomeDefaultFeatures.addDefaultSprings(biome);
		BiomeDefaultFeatures.addSurfaceFreezing(biome);
		BiomeDefaultFeatures.commonSpawns(biome);
		return biome;
	}

	public static Biome badlandsBiome(@Nullable String string, float f, float g, boolean bl) {
		return baseBadlandsBiome(string, SurfaceBuilders.BADLANDS, f, g, bl, false);
	}

	public static Biome woodedBadlandsPlateauBiome(@Nullable String string, float f, float g) {
		return baseBadlandsBiome(string, SurfaceBuilders.WOODED_BADLANDS, f, g, true, true);
	}

	public static Biome erodedBadlandsBiome() {
		return baseBadlandsBiome("badlands", SurfaceBuilders.ERODED_BADLANDS, 0.1F, 0.2F, true, false);
	}

	private static Biome baseOceanBiome(
		ConfiguredSurfaceBuilder<SurfaceBuilderBaseConfiguration> configuredSurfaceBuilder, int i, int j, boolean bl, boolean bl2, boolean bl3
	) {
		Biome biome = new Biome(
			new Biome.BiomeBuilder()
				.surfaceBuilder(configuredSurfaceBuilder)
				.precipitation(Biome.Precipitation.RAIN)
				.biomeCategory(Biome.BiomeCategory.OCEAN)
				.depth(bl ? -1.8F : -1.0F)
				.scale(0.1F)
				.temperature(0.5F)
				.downfall(0.5F)
				.specialEffects(
					new BiomeSpecialEffects.Builder().waterColor(i).waterFogColor(j).fogColor(12638463).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()
				)
				.parent(null)
		);
		ConfiguredStructureFeature<?, ?> configuredStructureFeature = bl2 ? StructureFeatures.OCEAN_RUIN_WARM : StructureFeatures.OCEAN_RUIN_COLD;
		if (bl3) {
			if (bl) {
				biome.addStructureStart(StructureFeatures.OCEAN_MONUMENT);
			}

			BiomeDefaultFeatures.addDefaultOverworldOceanStructures(biome);
			biome.addStructureStart(configuredStructureFeature);
		} else {
			biome.addStructureStart(configuredStructureFeature);
			if (bl) {
				biome.addStructureStart(StructureFeatures.OCEAN_MONUMENT);
			}

			BiomeDefaultFeatures.addDefaultOverworldOceanStructures(biome);
		}

		biome.addStructureStart(StructureFeatures.RUINED_PORTAL_OCEAN);
		BiomeDefaultFeatures.addOceanCarvers(biome);
		BiomeDefaultFeatures.addDefaultLakes(biome);
		BiomeDefaultFeatures.addDefaultMonsterRoom(biome);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(biome);
		BiomeDefaultFeatures.addDefaultOres(biome);
		BiomeDefaultFeatures.addDefaultSoftDisks(biome);
		BiomeDefaultFeatures.addWaterTrees(biome);
		BiomeDefaultFeatures.addDefaultFlowers(biome);
		BiomeDefaultFeatures.addDefaultGrass(biome);
		BiomeDefaultFeatures.addDefaultMushrooms(biome);
		BiomeDefaultFeatures.addDefaultExtraVegetation(biome);
		BiomeDefaultFeatures.addDefaultSprings(biome);
		return biome;
	}

	public static Biome coldOceanBiome(boolean bl) {
		Biome biome = baseOceanBiome(SurfaceBuilders.GRASS, 4020182, 329011, bl, false, !bl);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, bl ? Features.SEAGRASS_DEEP_COLD : Features.SEAGRASS_COLD);
		BiomeDefaultFeatures.addDefaultSeagrass(biome);
		BiomeDefaultFeatures.addColdOceanExtraVegetation(biome);
		BiomeDefaultFeatures.addSurfaceFreezing(biome);
		BiomeDefaultFeatures.oceanSpawns(biome, 3, 4, 15);
		biome.addSpawn(MobCategory.WATER_AMBIENT, new Biome.SpawnerData(EntityType.SALMON, 15, 1, 5));
		return biome;
	}

	public static Biome oceanBiome(boolean bl) {
		Biome biome = baseOceanBiome(SurfaceBuilders.GRASS, 4159204, 329011, bl, false, true);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, bl ? Features.SEAGRASS_DEEP : Features.SEAGRASS_NORMAL);
		BiomeDefaultFeatures.addDefaultSeagrass(biome);
		BiomeDefaultFeatures.addColdOceanExtraVegetation(biome);
		BiomeDefaultFeatures.addSurfaceFreezing(biome);
		BiomeDefaultFeatures.oceanSpawns(biome, 1, 4, 10);
		biome.addSpawn(MobCategory.WATER_CREATURE, new Biome.SpawnerData(EntityType.DOLPHIN, 1, 1, 2));
		return biome;
	}

	public static Biome lukeWarmOceanBiome(boolean bl) {
		Biome biome = baseOceanBiome(SurfaceBuilders.OCEAN_SAND, 4566514, 267827, bl, true, false);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, bl ? Features.SEAGRASS_DEEP_WARM : Features.SEAGRASS_WARM);
		if (bl) {
			BiomeDefaultFeatures.addDefaultSeagrass(biome);
		}

		BiomeDefaultFeatures.addLukeWarmKelp(biome);
		BiomeDefaultFeatures.addSurfaceFreezing(biome);
		if (bl) {
			BiomeDefaultFeatures.oceanSpawns(biome, 8, 4, 8);
		} else {
			BiomeDefaultFeatures.oceanSpawns(biome, 10, 2, 15);
		}

		biome.addSpawn(MobCategory.WATER_AMBIENT, new Biome.SpawnerData(EntityType.PUFFERFISH, 5, 1, 3));
		biome.addSpawn(MobCategory.WATER_AMBIENT, new Biome.SpawnerData(EntityType.TROPICAL_FISH, 25, 8, 8));
		biome.addSpawn(MobCategory.WATER_CREATURE, new Biome.SpawnerData(EntityType.DOLPHIN, 2, 1, 2));
		return biome;
	}

	public static Biome warmOceanBiome() {
		Biome biome = baseOceanBiome(SurfaceBuilders.FULL_SAND, 4445678, 270131, false, true, false);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.WARM_OCEAN_VEGETATION);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_WARM);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEA_PICKLE);
		BiomeDefaultFeatures.addSurfaceFreezing(biome);
		biome.addSpawn(MobCategory.WATER_AMBIENT, new Biome.SpawnerData(EntityType.PUFFERFISH, 15, 1, 3));
		BiomeDefaultFeatures.warmOceanSpawns(biome, 10, 4);
		return biome;
	}

	public static Biome deepWarmOceanBiome() {
		Biome biome = baseOceanBiome(SurfaceBuilders.FULL_SAND, 4445678, 270131, true, true, false);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_DEEP_WARM);
		BiomeDefaultFeatures.addDefaultSeagrass(biome);
		BiomeDefaultFeatures.addSurfaceFreezing(biome);
		BiomeDefaultFeatures.warmOceanSpawns(biome, 5, 1);
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.DROWNED, 5, 1, 1));
		return biome;
	}

	public static Biome frozenOceanBiome(boolean bl) {
		Biome biome = new FrozenOceanBiome(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilders.FROZEN_OCEAN)
				.precipitation(bl ? Biome.Precipitation.RAIN : Biome.Precipitation.SNOW)
				.biomeCategory(Biome.BiomeCategory.OCEAN)
				.depth(bl ? -1.8F : -1.0F)
				.scale(0.1F)
				.temperature(bl ? 0.5F : 0.0F)
				.downfall(0.5F)
				.specialEffects(
					new BiomeSpecialEffects.Builder()
						.waterColor(3750089)
						.waterFogColor(329011)
						.fogColor(12638463)
						.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
						.build()
				)
				.parent(null)
		);
		biome.addStructureStart(StructureFeatures.OCEAN_RUIN_COLD);
		if (bl) {
			biome.addStructureStart(StructureFeatures.OCEAN_MONUMENT);
		}

		BiomeDefaultFeatures.addDefaultOverworldOceanStructures(biome);
		biome.addStructureStart(StructureFeatures.RUINED_PORTAL_OCEAN);
		BiomeDefaultFeatures.addOceanCarvers(biome);
		BiomeDefaultFeatures.addDefaultLakes(biome);
		BiomeDefaultFeatures.addIcebergs(biome);
		BiomeDefaultFeatures.addDefaultMonsterRoom(biome);
		BiomeDefaultFeatures.addBlueIce(biome);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(biome);
		BiomeDefaultFeatures.addDefaultOres(biome);
		BiomeDefaultFeatures.addDefaultSoftDisks(biome);
		BiomeDefaultFeatures.addWaterTrees(biome);
		BiomeDefaultFeatures.addDefaultFlowers(biome);
		BiomeDefaultFeatures.addDefaultGrass(biome);
		BiomeDefaultFeatures.addDefaultMushrooms(biome);
		BiomeDefaultFeatures.addDefaultExtraVegetation(biome);
		BiomeDefaultFeatures.addDefaultSprings(biome);
		BiomeDefaultFeatures.addSurfaceFreezing(biome);
		biome.addSpawn(MobCategory.WATER_CREATURE, new Biome.SpawnerData(EntityType.SQUID, 1, 1, 4));
		biome.addSpawn(MobCategory.WATER_AMBIENT, new Biome.SpawnerData(EntityType.SALMON, 15, 1, 5));
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.POLAR_BEAR, 1, 1, 2));
		BiomeDefaultFeatures.commonSpawns(biome);
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.DROWNED, 5, 1, 1));
		return biome;
	}

	private static Biome baseForestBiome(@Nullable String string, float f, float g, boolean bl) {
		Biome biome = new Biome(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilders.GRASS)
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
						.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
						.build()
				)
				.parent(string)
		);
		BiomeDefaultFeatures.addDefaultOverworldLandStructures(biome);
		biome.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
		BiomeDefaultFeatures.addDefaultCarvers(biome);
		BiomeDefaultFeatures.addDefaultLakes(biome);
		BiomeDefaultFeatures.addDefaultMonsterRoom(biome);
		if (bl) {
			biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FOREST_FLOWER_VEGETATION_COMMON);
		} else {
			BiomeDefaultFeatures.addForestFlowers(biome);
		}

		BiomeDefaultFeatures.addDefaultUndergroundVariety(biome);
		BiomeDefaultFeatures.addDefaultOres(biome);
		BiomeDefaultFeatures.addDefaultSoftDisks(biome);
		if (bl) {
			biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FOREST_FLOWER_TREES);
			biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_FOREST);
			BiomeDefaultFeatures.addDefaultGrass(biome);
		} else {
			BiomeDefaultFeatures.addOtherBirchTrees(biome);
			BiomeDefaultFeatures.addDefaultFlowers(biome);
			BiomeDefaultFeatures.addForestGrass(biome);
		}

		BiomeDefaultFeatures.addDefaultMushrooms(biome);
		BiomeDefaultFeatures.addDefaultExtraVegetation(biome);
		BiomeDefaultFeatures.addDefaultSprings(biome);
		BiomeDefaultFeatures.addSurfaceFreezing(biome);
		BiomeDefaultFeatures.farmAnimals(biome);
		BiomeDefaultFeatures.commonSpawns(biome);
		return biome;
	}

	public static Biome forestBiome(float f, float g) {
		Biome biome = baseForestBiome(null, f, g, false);
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.WOLF, 5, 4, 4));
		return biome;
	}

	public static Biome flowerForestBiome() {
		Biome biome = baseForestBiome("forest", 0.1F, 0.4F, true);
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.RABBIT, 4, 2, 3));
		return biome;
	}

	public static Biome taigaBiome(@Nullable String string, float f, float g, boolean bl, boolean bl2, boolean bl3, boolean bl4) {
		Biome biome = new Biome(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilders.GRASS)
				.precipitation(bl ? Biome.Precipitation.SNOW : Biome.Precipitation.RAIN)
				.biomeCategory(Biome.BiomeCategory.TAIGA)
				.depth(f)
				.scale(g)
				.temperature(bl ? -0.5F : 0.25F)
				.downfall(bl ? 0.4F : 0.8F)
				.specialEffects(
					new BiomeSpecialEffects.Builder()
						.waterColor(bl ? 4020182 : 4159204)
						.waterFogColor(329011)
						.fogColor(12638463)
						.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
						.build()
				)
				.parent(string)
		);
		if (bl3) {
			biome.addStructureStart(StructureFeatures.VILLAGE_TAIGA);
			biome.addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
		}

		if (bl4) {
			biome.addStructureStart(StructureFeatures.IGLOO);
		}

		BiomeDefaultFeatures.addDefaultOverworldLandStructures(biome);
		biome.addStructureStart(bl2 ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
		BiomeDefaultFeatures.addDefaultCarvers(biome);
		BiomeDefaultFeatures.addDefaultLakes(biome);
		BiomeDefaultFeatures.addDefaultMonsterRoom(biome);
		BiomeDefaultFeatures.addFerns(biome);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(biome);
		BiomeDefaultFeatures.addDefaultOres(biome);
		BiomeDefaultFeatures.addDefaultSoftDisks(biome);
		BiomeDefaultFeatures.addTaigaTrees(biome);
		BiomeDefaultFeatures.addDefaultFlowers(biome);
		BiomeDefaultFeatures.addTaigaGrass(biome);
		BiomeDefaultFeatures.addDefaultMushrooms(biome);
		BiomeDefaultFeatures.addDefaultExtraVegetation(biome);
		BiomeDefaultFeatures.addDefaultSprings(biome);
		if (bl) {
			BiomeDefaultFeatures.addBerryBushes(biome);
		} else {
			BiomeDefaultFeatures.addSparseBerryBushes(biome);
		}

		BiomeDefaultFeatures.addSurfaceFreezing(biome);
		BiomeDefaultFeatures.farmAnimals(biome);
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.WOLF, 8, 4, 4));
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.RABBIT, 4, 2, 3));
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.FOX, 8, 2, 4));
		BiomeDefaultFeatures.commonSpawns(biome);
		return biome;
	}

	public static Biome darkForestBiome(@Nullable String string, float f, float g, boolean bl) {
		Biome biome = new DarkForestBiome(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilders.GRASS)
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
						.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
						.build()
				)
				.parent(string)
		);
		biome.addStructureStart(StructureFeatures.WOODLAND_MANSION);
		BiomeDefaultFeatures.addDefaultOverworldLandStructures(biome);
		biome.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
		BiomeDefaultFeatures.addDefaultCarvers(biome);
		BiomeDefaultFeatures.addDefaultLakes(biome);
		BiomeDefaultFeatures.addDefaultMonsterRoom(biome);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, bl ? Features.DARK_FOREST_VEGETATION_RED : Features.DARK_FOREST_VEGETATION_BROWN);
		BiomeDefaultFeatures.addForestFlowers(biome);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(biome);
		BiomeDefaultFeatures.addDefaultOres(biome);
		BiomeDefaultFeatures.addDefaultSoftDisks(biome);
		BiomeDefaultFeatures.addDefaultFlowers(biome);
		BiomeDefaultFeatures.addForestGrass(biome);
		BiomeDefaultFeatures.addDefaultMushrooms(biome);
		BiomeDefaultFeatures.addDefaultExtraVegetation(biome);
		BiomeDefaultFeatures.addDefaultSprings(biome);
		BiomeDefaultFeatures.addSurfaceFreezing(biome);
		BiomeDefaultFeatures.farmAnimals(biome);
		BiomeDefaultFeatures.commonSpawns(biome);
		return biome;
	}

	public static Biome swampBiome(@Nullable String string, float f, float g, boolean bl) {
		Biome biome = new SwampBiome(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilders.SWAMP)
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
						.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
						.build()
				)
				.parent(string)
		);
		if (!bl) {
			biome.addStructureStart(StructureFeatures.SWAMP_HUT);
		}

		biome.addStructureStart(StructureFeatures.MINESHAFT);
		biome.addStructureStart(StructureFeatures.RUINED_PORTAL_SWAMP);
		BiomeDefaultFeatures.addDefaultCarvers(biome);
		if (!bl) {
			BiomeDefaultFeatures.addFossilDecoration(biome);
		}

		BiomeDefaultFeatures.addDefaultLakes(biome);
		BiomeDefaultFeatures.addDefaultMonsterRoom(biome);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(biome);
		BiomeDefaultFeatures.addDefaultOres(biome);
		BiomeDefaultFeatures.addSwampClayDisk(biome);
		BiomeDefaultFeatures.addSwampVegetation(biome);
		BiomeDefaultFeatures.addDefaultMushrooms(biome);
		BiomeDefaultFeatures.addSwampExtraVegetation(biome);
		BiomeDefaultFeatures.addDefaultSprings(biome);
		if (bl) {
			BiomeDefaultFeatures.addFossilDecoration(biome);
		} else {
			biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_SWAMP);
		}

		BiomeDefaultFeatures.addSurfaceFreezing(biome);
		BiomeDefaultFeatures.farmAnimals(biome);
		BiomeDefaultFeatures.commonSpawns(biome);
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SLIME, 1, 1, 1));
		return biome;
	}

	public static Biome tundraBiome(@Nullable String string, float f, float g, boolean bl, boolean bl2) {
		Biome biome = new TundraBiome(
			new Biome.BiomeBuilder()
				.surfaceBuilder(bl ? SurfaceBuilders.ICE_SPIKES : SurfaceBuilders.GRASS)
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
						.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
						.build()
				)
				.parent(string)
		);
		if (!bl && !bl2) {
			biome.addStructureStart(StructureFeatures.VILLAGE_SNOWY);
			biome.addStructureStart(StructureFeatures.IGLOO);
		}

		BiomeDefaultFeatures.addDefaultOverworldLandStructures(biome);
		if (!bl && !bl2) {
			biome.addStructureStart(StructureFeatures.PILLAGER_OUTPOST);
		}

		biome.addStructureStart(bl2 ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
		BiomeDefaultFeatures.addDefaultCarvers(biome);
		BiomeDefaultFeatures.addDefaultLakes(biome);
		BiomeDefaultFeatures.addDefaultMonsterRoom(biome);
		if (bl) {
			biome.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.ICE_SPIKE);
			biome.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.ICE_PATCH);
		}

		BiomeDefaultFeatures.addDefaultUndergroundVariety(biome);
		BiomeDefaultFeatures.addDefaultOres(biome);
		BiomeDefaultFeatures.addDefaultSoftDisks(biome);
		BiomeDefaultFeatures.addSnowyTrees(biome);
		BiomeDefaultFeatures.addDefaultFlowers(biome);
		BiomeDefaultFeatures.addDefaultGrass(biome);
		BiomeDefaultFeatures.addDefaultMushrooms(biome);
		BiomeDefaultFeatures.addDefaultExtraVegetation(biome);
		BiomeDefaultFeatures.addDefaultSprings(biome);
		BiomeDefaultFeatures.addSurfaceFreezing(biome);
		BiomeDefaultFeatures.snowySpawns(biome);
		return biome;
	}

	public static Biome riverBiome(float f, float g, float h, int i, boolean bl) {
		Biome biome = new Biome(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilders.GRASS)
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
						.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
						.build()
				)
				.parent(null)
		);
		biome.addStructureStart(StructureFeatures.MINESHAFT);
		biome.addStructureStart(StructureFeatures.RUINED_PORTAL_STANDARD);
		BiomeDefaultFeatures.addDefaultCarvers(biome);
		BiomeDefaultFeatures.addDefaultLakes(biome);
		BiomeDefaultFeatures.addDefaultMonsterRoom(biome);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(biome);
		BiomeDefaultFeatures.addDefaultOres(biome);
		BiomeDefaultFeatures.addDefaultSoftDisks(biome);
		BiomeDefaultFeatures.addWaterTrees(biome);
		BiomeDefaultFeatures.addDefaultFlowers(biome);
		BiomeDefaultFeatures.addDefaultGrass(biome);
		BiomeDefaultFeatures.addDefaultMushrooms(biome);
		BiomeDefaultFeatures.addDefaultExtraVegetation(biome);
		BiomeDefaultFeatures.addDefaultSprings(biome);
		if (!bl) {
			biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_RIVER);
		}

		BiomeDefaultFeatures.addSurfaceFreezing(biome);
		biome.addSpawn(MobCategory.WATER_CREATURE, new Biome.SpawnerData(EntityType.SQUID, 2, 1, 4));
		biome.addSpawn(MobCategory.WATER_AMBIENT, new Biome.SpawnerData(EntityType.SALMON, 5, 1, 5));
		BiomeDefaultFeatures.commonSpawns(biome);
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.DROWNED, bl ? 1 : 100, 1, 1));
		return biome;
	}

	public static Biome beachBiome(float f, float g, float h, float i, int j, boolean bl, boolean bl2) {
		Biome biome = new Biome(
			new Biome.BiomeBuilder()
				.surfaceBuilder(bl2 ? SurfaceBuilders.STONE : SurfaceBuilders.DESERT)
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
						.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
						.build()
				)
				.parent(null)
		);
		if (bl2) {
			BiomeDefaultFeatures.addDefaultOverworldLandStructures(biome);
		} else {
			biome.addStructureStart(StructureFeatures.MINESHAFT);
			biome.addStructureStart(StructureFeatures.BURIED_TREASURE);
			biome.addStructureStart(StructureFeatures.SHIPWRECH_BEACHED);
		}

		biome.addStructureStart(bl2 ? StructureFeatures.RUINED_PORTAL_MOUNTAIN : StructureFeatures.RUINED_PORTAL_STANDARD);
		BiomeDefaultFeatures.addDefaultCarvers(biome);
		BiomeDefaultFeatures.addDefaultLakes(biome);
		BiomeDefaultFeatures.addDefaultMonsterRoom(biome);
		BiomeDefaultFeatures.addDefaultUndergroundVariety(biome);
		BiomeDefaultFeatures.addDefaultOres(biome);
		BiomeDefaultFeatures.addDefaultSoftDisks(biome);
		BiomeDefaultFeatures.addDefaultFlowers(biome);
		BiomeDefaultFeatures.addDefaultGrass(biome);
		BiomeDefaultFeatures.addDefaultMushrooms(biome);
		BiomeDefaultFeatures.addDefaultExtraVegetation(biome);
		BiomeDefaultFeatures.addDefaultSprings(biome);
		BiomeDefaultFeatures.addSurfaceFreezing(biome);
		if (!bl2 && !bl) {
			biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.TURTLE, 5, 2, 5));
		}

		BiomeDefaultFeatures.commonSpawns(biome);
		return biome;
	}

	public static Biome theVoidBiome() {
		Biome biome = new Biome(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilders.NOPE)
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
						.ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
						.build()
				)
				.parent(null)
		);
		biome.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, Features.VOID_START_PLATFORM);
		return biome;
	}

	public static Biome netherWastesBiome() {
		Biome biome = new Biome(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilders.NETHER)
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
						.ambientLoopSound(SoundEvents.AMBIENT_NETHER_WASTES_LOOP)
						.ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_NETHER_WASTES_MOOD, 6000, 8, 2.0))
						.ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_NETHER_WASTES_ADDITIONS, 0.0111))
						.backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_NETHER_WASTES))
						.build()
				)
				.parent(null)
		);
		biome.addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER);
		biome.addStructureStart(StructureFeatures.NETHER_BRIDGE);
		biome.addStructureStart(StructureFeatures.BASTION_REMNANT);
		biome.addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
		BiomeDefaultFeatures.addDefaultMushrooms(biome);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.BROWN_MUSHROOM_NETHER);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.RED_MUSHROOM_NETHER);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED);
		BiomeDefaultFeatures.addNetherDefaultOres(biome);
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.GHAST, 50, 4, 4));
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 100, 4, 4));
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.MAGMA_CUBE, 2, 4, 4));
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ENDERMAN, 1, 4, 4));
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.PIGLIN, 15, 4, 4));
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.STRIDER, 60, 1, 2));
		return biome;
	}

	public static Biome soulSandValleyBiome() {
		Biome biome = new Biome(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilders.SOUL_SAND_VALLEY)
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
						.ambientParticle(new AmbientParticleSettings(ParticleTypes.ASH, 0.00625F))
						.ambientLoopSound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_LOOP)
						.ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD, 6000, 8, 2.0))
						.ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS, 0.0111))
						.backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_SOUL_SAND_VALLEY))
						.build()
				)
				.parent(null)
		);
		biome.addStructureStart(StructureFeatures.NETHER_BRIDGE);
		biome.addStructureStart(StructureFeatures.NETHER_FOSSIL);
		biome.addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER);
		biome.addStructureStart(StructureFeatures.BASTION_REMNANT);
		biome.addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
		biome.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.BASALT_PILLAR);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_CRIMSON_ROOTS);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_SOUL_SAND);
		BiomeDefaultFeatures.addNetherDefaultOres(biome);
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SKELETON, 20, 5, 5));
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.GHAST, 50, 4, 4));
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ENDERMAN, 1, 4, 4));
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.STRIDER, 60, 1, 2));
		double d = 0.7;
		double e = 0.15;
		biome.addMobCharge(EntityType.SKELETON, 0.7, 0.15);
		biome.addMobCharge(EntityType.GHAST, 0.7, 0.15);
		biome.addMobCharge(EntityType.ENDERMAN, 0.7, 0.15);
		biome.addMobCharge(EntityType.STRIDER, 0.7, 0.15);
		return biome;
	}

	public static Biome basaltDeltasBiome() {
		Biome biome = new Biome(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilders.BASALT_DELTAS)
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
						.ambientParticle(new AmbientParticleSettings(ParticleTypes.WHITE_ASH, 0.118093334F))
						.ambientLoopSound(SoundEvents.AMBIENT_BASALT_DELTAS_LOOP)
						.ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_BASALT_DELTAS_MOOD, 6000, 8, 2.0))
						.ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_BASALT_DELTAS_ADDITIONS, 0.0111))
						.backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_BASALT_DELTAS))
						.build()
				)
				.parent(null)
		);
		biome.addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER);
		biome.addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE);
		biome.addStructureStart(StructureFeatures.NETHER_BRIDGE);
		biome.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.DELTA);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA_DOUBLE);
		biome.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.SMALL_BASALT_COLUMNS);
		biome.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.LARGE_BASALT_COLUMNS);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.BASALT_BLOBS);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.BLACKSTONE_BLOBS);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_DELTA);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.BROWN_MUSHROOM_NETHER);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.RED_MUSHROOM_NETHER);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED_DOUBLE);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_GOLD_DELTAS);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_QUARTZ_DELTAS);
		BiomeDefaultFeatures.addAncientDebris(biome);
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.GHAST, 40, 1, 1));
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.MAGMA_CUBE, 100, 2, 5));
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.STRIDER, 60, 1, 2));
		return biome;
	}

	public static Biome crimsonForestBiome() {
		Biome biome = new Biome(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilders.CRIMSON_FOREST)
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
						.ambientParticle(new AmbientParticleSettings(ParticleTypes.CRIMSON_SPORE, 0.025F))
						.ambientLoopSound(SoundEvents.AMBIENT_CRIMSON_FOREST_LOOP)
						.ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_CRIMSON_FOREST_MOOD, 6000, 8, 2.0))
						.ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_CRIMSON_FOREST_ADDITIONS, 0.0111))
						.backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_CRIMSON_FOREST))
						.build()
				)
				.parent(null)
		);
		biome.addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER);
		biome.addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE);
		biome.addStructureStart(StructureFeatures.NETHER_BRIDGE);
		biome.addStructureStart(StructureFeatures.BASTION_REMNANT);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
		BiomeDefaultFeatures.addDefaultMushrooms(biome);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.WEEPING_VINES);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.CRIMSON_FUNGI);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.CRIMSON_FOREST_VEGETATION);
		BiomeDefaultFeatures.addNetherDefaultOres(biome);
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 1, 2, 4));
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.HOGLIN, 9, 3, 4));
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.PIGLIN, 5, 3, 4));
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.STRIDER, 60, 1, 2));
		return biome;
	}

	public static Biome warpedForestBiome() {
		Biome biome = new Biome(
			new Biome.BiomeBuilder()
				.surfaceBuilder(SurfaceBuilders.WARPED_FOREST)
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
						.ambientParticle(new AmbientParticleSettings(ParticleTypes.WARPED_SPORE, 0.01428F))
						.ambientLoopSound(SoundEvents.AMBIENT_WARPED_FOREST_LOOP)
						.ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_WARPED_FOREST_MOOD, 6000, 8, 2.0))
						.ambientAdditionsSound(new AmbientAdditionsSettings(SoundEvents.AMBIENT_WARPED_FOREST_ADDITIONS, 0.0111))
						.backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_WARPED_FOREST))
						.build()
				)
				.parent(null)
		);
		biome.addStructureStart(StructureFeatures.NETHER_BRIDGE);
		biome.addStructureStart(StructureFeatures.BASTION_REMNANT);
		biome.addStructureStart(StructureFeatures.RUINED_PORTAL_NETHER);
		biome.addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
		BiomeDefaultFeatures.addDefaultMushrooms(biome);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_OPEN);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_FIRE);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.PATCH_SOUL_FIRE);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE_EXTRA);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.GLOWSTONE);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_MAGMA);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SPRING_CLOSED);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.WARPED_FUNGI);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.WARPED_FOREST_VEGETATION);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.NETHER_SPROUTS);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TWISTING_VINES);
		BiomeDefaultFeatures.addNetherDefaultOres(biome);
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ENDERMAN, 1, 4, 4));
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.STRIDER, 60, 1, 2));
		biome.addMobCharge(EntityType.ENDERMAN, 1.0, 0.12);
		return biome;
	}
}
