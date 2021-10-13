package net.minecraft.data.worldgen;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;

public class BiomeDefaultFeatures {
	public static void addDefaultCarversAndLakes(BiomeGenerationSettings.Builder builder) {
		builder.addCarver(GenerationStep.Carving.AIR, Carvers.CAVE);
		builder.addCarver(GenerationStep.Carving.AIR, Carvers.CAVE_EXTRA_UNDERGROUND);
		builder.addCarver(GenerationStep.Carving.AIR, Carvers.CANYON);
		builder.addFeature(GenerationStep.Decoration.LAKES, Features.LAKE_LAVA);
	}

	public static void addDefaultMonsterRoom(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, Features.MONSTER_ROOM);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, Features.MONSTER_ROOM_DEEP);
	}

	public static void addDefaultUndergroundVariety(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_DIRT);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_GRAVEL);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_GRANITE_UPPER);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_GRANITE_LOWER);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_DIORITE_UPPER);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_DIORITE_LOWER);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_ANDESITE_UPPER);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_ANDESITE_LOWER);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_TUFF);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.GLOW_LICHEN);
	}

	public static void addDripstone(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.LARGE_DRIPSTONE_FEATURE);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.DRIPSTONE_CLUSTER_FEATURE);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.SMALL_DRIPSTONE_FEATURE);
	}

	public static void addDefaultOres(BiomeGenerationSettings.Builder builder) {
		addDefaultOres(builder, false);
	}

	public static void addDefaultOres(BiomeGenerationSettings.Builder builder, boolean bl) {
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_COAL_UPPER);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_COAL_LOWER);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_IRON_UPPER);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_IRON_MIDDLE);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_IRON_SMALL);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_GOLD);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_GOLD_LOWER);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_REDSTONE);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_REDSTONE_LOWER);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_DIAMOND);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_DIAMOND_LARGE);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_DIAMOND_BURIED);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_LAPIS);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_LAPIS_BURIED);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, bl ? Features.ORE_COPPER_LARGE : Features.ORE_COPPER);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.UNDERWATER_MAGMA);
	}

	public static void addExtraGold(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_GOLD_EXTRA);
	}

	public static void addExtraEmeralds(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_EMERALD);
	}

	public static void addInfestedStone(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_INFESTED);
	}

	public static void addDefaultSoftDisks(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.DISK_SAND);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.DISK_CLAY);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.DISK_GRAVEL);
	}

	public static void addSwampClayDisk(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.DISK_CLAY);
	}

	public static void addMossyStoneBlock(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.FOREST_ROCK);
	}

	public static void addFerns(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_LARGE_FERN);
	}

	public static void addRareBerryBushes(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_BERRY_RARE);
	}

	public static void addCommonBerryBushes(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_BERRY_COMMON);
	}

	public static void addLightBambooVegetation(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BAMBOO_LIGHT);
	}

	public static void addBambooVegetation(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BAMBOO);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BAMBOO_VEGETATION);
	}

	public static void addTaigaTrees(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TAIGA_VEGETATION);
	}

	public static void addGroveTrees(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.GROVE_VEGETATION);
	}

	public static void addWaterTrees(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_WATER);
	}

	public static void addBirchTrees(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_BIRCH);
	}

	public static void addOtherBirchTrees(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BIRCH_OTHER);
	}

	public static void addTallBirchTrees(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BIRCH_TALL);
	}

	public static void addSavannaTrees(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_SAVANNA);
	}

	public static void addShatteredSavannaTrees(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_SHATTERED_SAVANNA);
	}

	public static void addLushCavesVegetationFeatures(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.LUSH_CAVES_CEILING_VEGETATION);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.CAVE_VINES);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.LUSH_CAVES_CLAY);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.LUSH_CAVES_VEGETATION);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.ROOTED_AZALEA_TREES);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPORE_BLOSSOM_FEATURE);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.CLASSIC_VINES_CAVE_FEATURE);
	}

	public static void addLushCavesSpecialOres(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_CLAY);
	}

	public static void addMountainTrees(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_MOUNTAIN);
	}

	public static void addMountainEdgeTrees(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_MOUNTAIN_EDGE);
	}

	public static void addJungleTrees(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_JUNGLE);
	}

	public static void addJungleEdgeTrees(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_JUNGLE_EDGE);
	}

	public static void addBadlandsTrees(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_BADLANDS);
	}

	public static void addSnowyTrees(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_SNOWY);
	}

	public static void addJungleGrass(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_JUNGLE);
	}

	public static void addSavannaGrass(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_TALL_GRASS);
	}

	public static void addShatteredSavannaGrass(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_NORMAL);
	}

	public static void addSavannaExtraGrass(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_SAVANNA);
	}

	public static void addBadlandGrass(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_BADLANDS);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_DEAD_BUSH_BADLANDS);
	}

	public static void addForestFlowers(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FOREST_FLOWER_VEGETATION);
	}

	public static void addForestGrass(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_FOREST);
	}

	public static void addSwampVegetation(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_SWAMP);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_SWAMP);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_NORMAL);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_DEAD_BUSH);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_WATERLILLY);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_SWAMP);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_SWAMP);
	}

	public static void addMushroomFieldVegetation(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.MUSHROOM_FIELD_VEGETATION);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_TAIGA);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_TAIGA);
	}

	public static void addPlainVegetation(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PLAIN_VEGETATION);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_PLAIN_DECORATED);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_PLAIN);
	}

	public static void addDesertVegetation(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_DEAD_BUSH_2);
	}

	public static void addGiantTaigaVegetation(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_TAIGA);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_DEAD_BUSH);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_GIANT);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_GIANT);
	}

	public static void addDefaultFlowers(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_DEFAULT);
	}

	public static void addMeadowVegetation(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_PLAIN);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_MEADOW);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.MEADOW_TREES);
	}

	public static void addWarmFlowers(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_WARM);
	}

	public static void addDefaultGrass(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_BADLANDS);
	}

	public static void addTaigaGrass(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_TAIGA_2);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_TAIGA);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_TAIGA);
	}

	public static void addPlainGrass(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_TALL_GRASS_2);
	}

	public static void addDefaultMushrooms(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_NORMAL);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_NORMAL);
	}

	public static void addDefaultExtraVegetation(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
	}

	public static void addBadlandExtraVegetation(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE_BADLANDS);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_CACTUS_DECORATED);
	}

	public static void addJungleExtraVegetation(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_MELON);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.VINES);
	}

	public static void addDesertExtraVegetation(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE_DESERT);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_CACTUS_DESERT);
	}

	public static void addSwampExtraVegetation(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE_SWAMP);
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
	}

	public static void addDesertExtraDecoration(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.WELL);
	}

	public static void addFossilDecoration(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, Features.FOSSIL_UPPER);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, Features.FOSSIL_LOWER);
	}

	public static void addColdOceanExtraVegetation(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.KELP_COLD);
	}

	public static void addDefaultSeagrass(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_SIMPLE);
	}

	public static void addLukeWarmKelp(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.KELP_WARM);
	}

	public static void addDefaultSprings(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.FLUID_SPRINGS, Features.SPRING_WATER);
		builder.addFeature(GenerationStep.Decoration.FLUID_SPRINGS, Features.SPRING_LAVA);
	}

	public static void addIcebergs(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.ICEBERG_PACKED);
		builder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.ICEBERG_BLUE);
	}

	public static void addBlueIce(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.BLUE_ICE);
	}

	public static void addSurfaceFreezing(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, Features.FREEZE_TOP_LAYER);
	}

	public static void addNetherDefaultOres(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_GRAVEL_NETHER);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_BLACKSTONE);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_GOLD_NETHER);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_QUARTZ_NETHER);
		addAncientDebris(builder);
	}

	public static void addAncientDebris(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_DEBRIS_LARGE);
		builder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_DEBRIS_SMALL);
	}

	public static void addDefaultCrystalFormations(BiomeGenerationSettings.Builder builder) {
		builder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.AMETHYST_GEODE);
	}

	public static void farmAnimals(MobSpawnSettings.Builder builder) {
		builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SHEEP, 12, 4, 4));
		builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.PIG, 10, 4, 4));
		builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.CHICKEN, 10, 4, 4));
		builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.COW, 8, 4, 4));
	}

	public static void caveSpawns(MobSpawnSettings.Builder builder) {
		builder.addSpawn(MobCategory.AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.BAT, 10, 8, 8));
		builder.addSpawn(MobCategory.UNDERGROUND_WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.GLOW_SQUID, 10, 4, 6));
	}

	public static void commonSpawns(MobSpawnSettings.Builder builder) {
		caveSpawns(builder);
		monsters(builder, 95, 5, 100, false);
	}

	public static void oceanSpawns(MobSpawnSettings.Builder builder, int i, int j, int k) {
		builder.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, i, 1, j));
		builder.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.COD, k, 3, 6));
		commonSpawns(builder);
		builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 5, 1, 1));
	}

	public static void warmOceanSpawns(MobSpawnSettings.Builder builder, int i, int j) {
		builder.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.SQUID, i, j, 4));
		builder.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.TROPICAL_FISH, 25, 8, 8));
		builder.addSpawn(MobCategory.WATER_CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DOLPHIN, 2, 1, 2));
		builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 5, 1, 1));
		commonSpawns(builder);
	}

	public static void plainsSpawns(MobSpawnSettings.Builder builder) {
		farmAnimals(builder);
		builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.HORSE, 5, 2, 6));
		builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.DONKEY, 1, 1, 3));
		commonSpawns(builder);
	}

	public static void snowySpawns(MobSpawnSettings.Builder builder) {
		builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 10, 2, 3));
		builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.POLAR_BEAR, 1, 1, 2));
		caveSpawns(builder);
		monsters(builder, 95, 5, 20, false);
		builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.STRAY, 80, 4, 4));
	}

	public static void desertSpawns(MobSpawnSettings.Builder builder) {
		builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 4, 2, 3));
		caveSpawns(builder);
		monsters(builder, 19, 1, 100, false);
		builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.HUSK, 80, 4, 4));
	}

	public static void dripstoneCavesSpawns(MobSpawnSettings.Builder builder) {
		caveSpawns(builder);
		int i = 95;
		monsters(builder, 95, 5, 100, false);
		builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 95, 4, 4));
	}

	public static void monsters(MobSpawnSettings.Builder builder, int i, int j, int k, boolean bl) {
		builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SPIDER, 100, 4, 4));
		builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(bl ? EntityType.DROWNED : EntityType.ZOMBIE, i, 4, 4));
		builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE_VILLAGER, j, 1, 1));
		builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SKELETON, k, 4, 4));
		builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.CREEPER, 100, 4, 4));
		builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.SLIME, 100, 4, 4));
		builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 10, 1, 4));
		builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.WITCH, 5, 1, 1));
	}

	public static void mooshroomSpawns(MobSpawnSettings.Builder builder) {
		builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.MOOSHROOM, 8, 4, 8));
		caveSpawns(builder);
	}

	public static void baseJungleSpawns(MobSpawnSettings.Builder builder) {
		farmAnimals(builder);
		builder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.CHICKEN, 10, 4, 4));
		commonSpawns(builder);
	}

	public static void endSpawns(MobSpawnSettings.Builder builder) {
		builder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ENDERMAN, 10, 4, 4));
	}
}
