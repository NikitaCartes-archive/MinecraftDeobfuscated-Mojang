package net.minecraft.data.worldgen;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;

public class BiomeDefaultFeatures {
	public static void addDefaultOverworldLandMesaStructures(Biome biome) {
		biome.addStructureStart(StructureFeatures.MINESHAFT_MESA);
		biome.addStructureStart(StructureFeatures.STRONGHOLD);
	}

	public static void addDefaultOverworldLandStructures(Biome biome) {
		biome.addStructureStart(StructureFeatures.MINESHAFT);
		biome.addStructureStart(StructureFeatures.STRONGHOLD);
	}

	public static void addDefaultOverworldOceanStructures(Biome biome) {
		biome.addStructureStart(StructureFeatures.MINESHAFT);
		biome.addStructureStart(StructureFeatures.SHIPWRECK);
	}

	public static void addDefaultCarvers(Biome biome) {
		biome.addCarver(GenerationStep.Carving.AIR, Carvers.CAVE);
		biome.addCarver(GenerationStep.Carving.AIR, Carvers.CANYON);
	}

	public static void addOceanCarvers(Biome biome) {
		biome.addCarver(GenerationStep.Carving.AIR, Carvers.OCEAN_CAVE);
		biome.addCarver(GenerationStep.Carving.AIR, Carvers.CANYON);
		biome.addCarver(GenerationStep.Carving.LIQUID, Carvers.UNDERWATER_CANYON);
		biome.addCarver(GenerationStep.Carving.LIQUID, Carvers.UNDERWATER_CAVE);
	}

	public static void addDefaultLakes(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.LAKES, Features.LAKE_WATER);
		biome.addFeature(GenerationStep.Decoration.LAKES, Features.LAKE_LAVA);
	}

	public static void addDesertLakes(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.LAKES, Features.LAKE_LAVA);
	}

	public static void addDefaultMonsterRoom(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, Features.MONSTER_ROOM);
	}

	public static void addDefaultUndergroundVariety(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_DIRT);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_GRAVEL);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_GRANITE);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_DIORITE);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_ANDESITE);
	}

	public static void addDefaultOres(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_COAL);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_IRON);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_GOLD);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_REDSTONE);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_DIAMOND);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_LAPIS);
	}

	public static void addExtraGold(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_GOLD_EXTRA);
	}

	public static void addExtraEmeralds(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.ORE_EMERALD);
	}

	public static void addInfestedStone(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_INFESTED);
	}

	public static void addDefaultSoftDisks(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.DISK_SAND);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.DISK_CLAY);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.DISK_GRAVEL);
	}

	public static void addSwampClayDisk(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, Features.DISK_CLAY);
	}

	public static void addMossyStoneBlock(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.FOREST_ROCK);
	}

	public static void addFerns(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_LARGE_FERN);
	}

	public static void addBerryBushes(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_BERRY_DECORATED);
	}

	public static void addSparseBerryBushes(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_BERRY_SPARSE);
	}

	public static void addLightBambooVegetation(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BAMBOO_LIGHT);
	}

	public static void addBambooVegetation(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BAMBOO);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BAMBOO_VEGETATION);
	}

	public static void addTaigaTrees(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TAIGA_VEGETATION);
	}

	public static void addWaterTrees(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_WATER);
	}

	public static void addBirchTrees(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_BIRCH);
	}

	public static void addOtherBirchTrees(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BIRCH_OTHER);
	}

	public static void addTallBirchTrees(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BIRCH_TALL);
	}

	public static void addSavannaTrees(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_SAVANNA);
	}

	public static void addShatteredSavannaTrees(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_SHATTERED_SAVANNA);
	}

	public static void addMountainTrees(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_MOUNTAIN);
	}

	public static void addMountainEdgeTrees(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_MOUNTAIN_EDGE);
	}

	public static void addJungleTrees(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_JUNGLE);
	}

	public static void addJungleEdgeTrees(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.TREES_JUNGLE_EDGE);
	}

	public static void addBadlandsTrees(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.OAK_BADLANDS);
	}

	public static void addSnowyTrees(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRUCE_SNOWY);
	}

	public static void addJungleGrass(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_JUNGLE);
	}

	public static void addSavannaGrass(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_TALL_GRASS);
	}

	public static void addShatteredSavannaGrass(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_NORMAL);
	}

	public static void addSavannaExtraGrass(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_SAVANNA);
	}

	public static void addBadlandGrass(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_BADLANDS);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_DEAD_BUSH_BADLANDS);
	}

	public static void addForestFlowers(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FOREST_FLOWER_VEGETATION);
	}

	public static void addForestGrass(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_FOREST);
	}

	public static void addSwampVegetation(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SWAMP_TREE);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_SWAMP);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_NORMAL);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_DEAD_BUSH);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_WATERLILLY);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_SWAMP);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_SWAMP);
	}

	public static void addMushroomFieldVegetation(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.MUSHROOM_FIELD_VEGETATION);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_TAIGA);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_TAIGA);
	}

	public static void addPlainVegetation(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PLAIN_VEGETATION);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_PLAIN_DECORATED);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_PLAIN);
	}

	public static void addDesertVegetation(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_DEAD_BUSH_2);
	}

	public static void addGiantTaigaVegetation(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_TAIGA);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_DEAD_BUSH);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_GIANT);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_GIANT);
	}

	public static void addDefaultFlowers(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_DEFAULT);
	}

	public static void addWarmFlowers(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.FLOWER_WARM);
	}

	public static void addDefaultGrass(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_BADLANDS);
	}

	public static void addTaigaGrass(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_GRASS_TAIGA_2);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_TAIGA);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_TAIGA);
	}

	public static void addPlainGrass(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_TALL_GRASS_2);
	}

	public static void addDefaultMushrooms(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.BROWN_MUSHROOM_NORMAL);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.RED_MUSHROOM_NORMAL);
	}

	public static void addDefaultExtraVegetation(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
	}

	public static void addBadlandExtraVegetation(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE_BADLANDS);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_CACTUS_DECORATED);
	}

	public static void addJungleExtraVegetation(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_MELON);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.VINES);
	}

	public static void addDesertExtraVegetation(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE_DESERT);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_CACTUS_DESERT);
	}

	public static void addSwampExtraVegetation(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_SUGAR_CANE_SWAMP);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.PATCH_PUMPKIN);
	}

	public static void addDesertExtraDecoration(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.WELL);
	}

	public static void addFossilDecoration(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, Features.FOSSIL);
	}

	public static void addColdOceanExtraVegetation(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.KELP_COLD);
	}

	public static void addDefaultSeagrass(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SEAGRASS_SIMPLE);
	}

	public static void addLukeWarmKelp(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.KELP_WARM);
	}

	public static void addDefaultSprings(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_WATER);
		biome.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, Features.SPRING_LAVA);
	}

	public static void addIcebergs(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.ICEBERG_PACKED);
		biome.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, Features.ICEBERG_BLUE);
	}

	public static void addBlueIce(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, Features.BLUE_ICE);
	}

	public static void addSurfaceFreezing(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, Features.FREEZE_TOP_LAYER);
	}

	public static void addNetherDefaultOres(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_GRAVEL_NETHER);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_BLACKSTONE);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_GOLD_NETHER);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_QUARTZ_NETHER);
		addAncientDebris(biome);
	}

	public static void addAncientDebris(Biome biome) {
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_DEBRIS_LARGE);
		biome.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, Features.ORE_DEBRIS_SMALL);
	}

	public static void farmAnimals(Biome biome) {
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.SHEEP, 12, 4, 4));
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.PIG, 10, 4, 4));
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.CHICKEN, 10, 4, 4));
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.COW, 8, 4, 4));
	}

	public static void ambientSpawns(Biome biome) {
		biome.addSpawn(MobCategory.AMBIENT, new Biome.SpawnerData(EntityType.BAT, 10, 8, 8));
	}

	public static void commonSpawns(Biome biome) {
		ambientSpawns(biome);
		monsters(biome, 95, 5, 100);
	}

	public static void oceanSpawns(Biome biome, int i, int j, int k) {
		biome.addSpawn(MobCategory.WATER_CREATURE, new Biome.SpawnerData(EntityType.SQUID, i, 1, j));
		biome.addSpawn(MobCategory.WATER_AMBIENT, new Biome.SpawnerData(EntityType.COD, k, 3, 6));
		commonSpawns(biome);
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.DROWNED, 5, 1, 1));
	}

	public static void warmOceanSpawns(Biome biome, int i, int j) {
		biome.addSpawn(MobCategory.WATER_CREATURE, new Biome.SpawnerData(EntityType.SQUID, i, j, 4));
		biome.addSpawn(MobCategory.WATER_AMBIENT, new Biome.SpawnerData(EntityType.TROPICAL_FISH, 25, 8, 8));
		biome.addSpawn(MobCategory.WATER_CREATURE, new Biome.SpawnerData(EntityType.DOLPHIN, 2, 1, 2));
		commonSpawns(biome);
	}

	public static void plainsSpawns(Biome biome) {
		farmAnimals(biome);
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.HORSE, 5, 2, 6));
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.DONKEY, 1, 1, 3));
		commonSpawns(biome);
	}

	public static void snowySpawns(Biome biome) {
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.RABBIT, 10, 2, 3));
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.POLAR_BEAR, 1, 1, 2));
		ambientSpawns(biome);
		monsters(biome, 95, 5, 20);
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.STRAY, 80, 4, 4));
	}

	public static void desertSpawns(Biome biome) {
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.RABBIT, 4, 2, 3));
		ambientSpawns(biome);
		monsters(biome, 19, 1, 100);
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.HUSK, 80, 4, 4));
	}

	public static void monsters(Biome biome, int i, int j, int k) {
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SPIDER, 100, 4, 4));
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ZOMBIE, i, 4, 4));
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ZOMBIE_VILLAGER, j, 1, 1));
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SKELETON, k, 4, 4));
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.CREEPER, 100, 4, 4));
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.SLIME, 100, 4, 4));
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ENDERMAN, 10, 1, 4));
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.WITCH, 5, 1, 1));
	}

	public static void mooshroomSpawns(Biome biome) {
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.MOOSHROOM, 8, 4, 8));
		ambientSpawns(biome);
	}

	public static void baseJungleSpawns(Biome biome) {
		farmAnimals(biome);
		biome.addSpawn(MobCategory.CREATURE, new Biome.SpawnerData(EntityType.CHICKEN, 10, 4, 4));
		commonSpawns(biome);
	}

	public static void endSpawns(Biome biome) {
		biome.addSpawn(MobCategory.MONSTER, new Biome.SpawnerData(EntityType.ENDERMAN, 10, 4, 4));
	}
}
