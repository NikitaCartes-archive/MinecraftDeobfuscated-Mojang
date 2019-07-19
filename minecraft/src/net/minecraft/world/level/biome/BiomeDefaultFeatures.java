package net.minecraft.world.level.biome;

import com.google.common.collect.Lists;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.feature.BlockBlobConfiguration;
import net.minecraft.world.level.levelgen.feature.BuriedTreasureConfiguration;
import net.minecraft.world.level.levelgen.feature.BushConfiguration;
import net.minecraft.world.level.levelgen.feature.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.DecoratorCountRange;
import net.minecraft.world.level.levelgen.feature.DecoratorNoiseDependant;
import net.minecraft.world.level.levelgen.feature.DiskConfiguration;
import net.minecraft.world.level.levelgen.feature.DoublePlantConfiguration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.GrassConfiguration;
import net.minecraft.world.level.levelgen.feature.HugeMushroomFeatureConfig;
import net.minecraft.world.level.levelgen.feature.IcebergConfiguration;
import net.minecraft.world.level.levelgen.feature.LakeConfiguration;
import net.minecraft.world.level.levelgen.feature.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;
import net.minecraft.world.level.levelgen.feature.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.feature.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.PillagerOutpostConfiguration;
import net.minecraft.world.level.levelgen.feature.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.RandomBooleanFeatureConfig;
import net.minecraft.world.level.levelgen.feature.RandomFeatureConfig;
import net.minecraft.world.level.levelgen.feature.RandomRandomFeatureConfig;
import net.minecraft.world.level.levelgen.feature.ReplaceBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.SeagrassFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.feature.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.SpringConfiguration;
import net.minecraft.world.level.levelgen.feature.VillageConfiguration;
import net.minecraft.world.level.levelgen.placement.DecoratorCarvingMaskConfig;
import net.minecraft.world.level.levelgen.placement.DecoratorChance;
import net.minecraft.world.level.levelgen.placement.DecoratorFrequency;
import net.minecraft.world.level.levelgen.placement.DecoratorFrequencyChance;
import net.minecraft.world.level.levelgen.placement.DecoratorFrequencyWithExtraChance;
import net.minecraft.world.level.levelgen.placement.DecoratorNoiseCountFactor;
import net.minecraft.world.level.levelgen.placement.DepthAverageConfigation;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.placement.LakeChanceDecoratorConfig;
import net.minecraft.world.level.levelgen.placement.MonsterRoomPlacementConfiguration;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;
import net.minecraft.world.level.material.Fluids;

public class BiomeDefaultFeatures {
	public static void addDefaultCarvers(Biome biome) {
		biome.addCarver(GenerationStep.Carving.AIR, Biome.makeCarver(WorldCarver.CAVE, new ProbabilityFeatureConfiguration(0.14285715F)));
		biome.addCarver(GenerationStep.Carving.AIR, Biome.makeCarver(WorldCarver.CANYON, new ProbabilityFeatureConfiguration(0.02F)));
	}

	public static void addOceanCarvers(Biome biome) {
		biome.addCarver(GenerationStep.Carving.AIR, Biome.makeCarver(WorldCarver.CAVE, new ProbabilityFeatureConfiguration(0.06666667F)));
		biome.addCarver(GenerationStep.Carving.AIR, Biome.makeCarver(WorldCarver.CANYON, new ProbabilityFeatureConfiguration(0.02F)));
		biome.addCarver(GenerationStep.Carving.LIQUID, Biome.makeCarver(WorldCarver.UNDERWATER_CANYON, new ProbabilityFeatureConfiguration(0.02F)));
		biome.addCarver(GenerationStep.Carving.LIQUID, Biome.makeCarver(WorldCarver.UNDERWATER_CAVE, new ProbabilityFeatureConfiguration(0.06666667F)));
	}

	public static void addStructureFeaturePlacement(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.UNDERGROUND_STRUCTURES,
			Biome.makeComposite(Feature.MINESHAFT, new MineshaftConfiguration(0.004F, MineshaftFeature.Type.NORMAL), FeatureDecorator.NOPE, DecoratorConfiguration.NONE)
		);
		biome.addFeature(
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			Biome.makeComposite(Feature.PILLAGER_OUTPOST, new PillagerOutpostConfiguration(0.004), FeatureDecorator.NOPE, DecoratorConfiguration.NONE)
		);
		biome.addFeature(
			GenerationStep.Decoration.UNDERGROUND_STRUCTURES,
			Biome.makeComposite(Feature.STRONGHOLD, FeatureConfiguration.NONE, FeatureDecorator.NOPE, DecoratorConfiguration.NONE)
		);
		biome.addFeature(
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			Biome.makeComposite(Feature.SWAMP_HUT, FeatureConfiguration.NONE, FeatureDecorator.NOPE, DecoratorConfiguration.NONE)
		);
		biome.addFeature(
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			Biome.makeComposite(Feature.DESERT_PYRAMID, FeatureConfiguration.NONE, FeatureDecorator.NOPE, DecoratorConfiguration.NONE)
		);
		biome.addFeature(
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			Biome.makeComposite(Feature.JUNGLE_TEMPLE, FeatureConfiguration.NONE, FeatureDecorator.NOPE, DecoratorConfiguration.NONE)
		);
		biome.addFeature(
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			Biome.makeComposite(Feature.IGLOO, FeatureConfiguration.NONE, FeatureDecorator.NOPE, DecoratorConfiguration.NONE)
		);
		biome.addFeature(
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			Biome.makeComposite(Feature.SHIPWRECK, new ShipwreckConfiguration(false), FeatureDecorator.NOPE, DecoratorConfiguration.NONE)
		);
		biome.addFeature(
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			Biome.makeComposite(Feature.OCEAN_MONUMENT, FeatureConfiguration.NONE, FeatureDecorator.NOPE, DecoratorConfiguration.NONE)
		);
		biome.addFeature(
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			Biome.makeComposite(Feature.WOODLAND_MANSION, FeatureConfiguration.NONE, FeatureDecorator.NOPE, DecoratorConfiguration.NONE)
		);
		biome.addFeature(
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			Biome.makeComposite(
				Feature.OCEAN_RUIN, new OceanRuinConfiguration(OceanRuinFeature.Type.COLD, 0.3F, 0.9F), FeatureDecorator.NOPE, DecoratorConfiguration.NONE
			)
		);
		biome.addFeature(
			GenerationStep.Decoration.UNDERGROUND_STRUCTURES,
			Biome.makeComposite(Feature.BURIED_TREASURE, new BuriedTreasureConfiguration(0.01F), FeatureDecorator.NOPE, DecoratorConfiguration.NONE)
		);
		biome.addFeature(
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			Biome.makeComposite(Feature.VILLAGE, new VillageConfiguration("village/plains/town_centers", 6), FeatureDecorator.NOPE, DecoratorConfiguration.NONE)
		);
	}

	public static void addDefaultLakes(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.LOCAL_MODIFICATIONS,
			Biome.makeComposite(Feature.LAKE, new LakeConfiguration(Blocks.WATER.defaultBlockState()), FeatureDecorator.WATER_LAKE, new LakeChanceDecoratorConfig(4))
		);
		biome.addFeature(
			GenerationStep.Decoration.LOCAL_MODIFICATIONS,
			Biome.makeComposite(Feature.LAKE, new LakeConfiguration(Blocks.LAVA.defaultBlockState()), FeatureDecorator.LAVA_LAKE, new LakeChanceDecoratorConfig(80))
		);
	}

	public static void addDesertLakes(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.LOCAL_MODIFICATIONS,
			Biome.makeComposite(Feature.LAKE, new LakeConfiguration(Blocks.LAVA.defaultBlockState()), FeatureDecorator.LAVA_LAKE, new LakeChanceDecoratorConfig(80))
		);
	}

	public static void addDefaultMonsterRoom(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.UNDERGROUND_STRUCTURES,
			Biome.makeComposite(Feature.MONSTER_ROOM, FeatureConfiguration.NONE, FeatureDecorator.DUNGEONS, new MonsterRoomPlacementConfiguration(8))
		);
	}

	public static void addDefaultUndergroundVariety(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.UNDERGROUND_ORES,
			Biome.makeComposite(
				Feature.ORE,
				new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.DIRT.defaultBlockState(), 33),
				FeatureDecorator.COUNT_RANGE,
				new DecoratorCountRange(10, 0, 0, 256)
			)
		);
		biome.addFeature(
			GenerationStep.Decoration.UNDERGROUND_ORES,
			Biome.makeComposite(
				Feature.ORE,
				new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.GRAVEL.defaultBlockState(), 33),
				FeatureDecorator.COUNT_RANGE,
				new DecoratorCountRange(8, 0, 0, 256)
			)
		);
		biome.addFeature(
			GenerationStep.Decoration.UNDERGROUND_ORES,
			Biome.makeComposite(
				Feature.ORE,
				new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.GRANITE.defaultBlockState(), 33),
				FeatureDecorator.COUNT_RANGE,
				new DecoratorCountRange(10, 0, 0, 80)
			)
		);
		biome.addFeature(
			GenerationStep.Decoration.UNDERGROUND_ORES,
			Biome.makeComposite(
				Feature.ORE,
				new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.DIORITE.defaultBlockState(), 33),
				FeatureDecorator.COUNT_RANGE,
				new DecoratorCountRange(10, 0, 0, 80)
			)
		);
		biome.addFeature(
			GenerationStep.Decoration.UNDERGROUND_ORES,
			Biome.makeComposite(
				Feature.ORE,
				new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.ANDESITE.defaultBlockState(), 33),
				FeatureDecorator.COUNT_RANGE,
				new DecoratorCountRange(10, 0, 0, 80)
			)
		);
	}

	public static void addDefaultOres(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.UNDERGROUND_ORES,
			Biome.makeComposite(
				Feature.ORE,
				new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.COAL_ORE.defaultBlockState(), 17),
				FeatureDecorator.COUNT_RANGE,
				new DecoratorCountRange(20, 0, 0, 128)
			)
		);
		biome.addFeature(
			GenerationStep.Decoration.UNDERGROUND_ORES,
			Biome.makeComposite(
				Feature.ORE,
				new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.IRON_ORE.defaultBlockState(), 9),
				FeatureDecorator.COUNT_RANGE,
				new DecoratorCountRange(20, 0, 0, 64)
			)
		);
		biome.addFeature(
			GenerationStep.Decoration.UNDERGROUND_ORES,
			Biome.makeComposite(
				Feature.ORE,
				new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.GOLD_ORE.defaultBlockState(), 9),
				FeatureDecorator.COUNT_RANGE,
				new DecoratorCountRange(2, 0, 0, 32)
			)
		);
		biome.addFeature(
			GenerationStep.Decoration.UNDERGROUND_ORES,
			Biome.makeComposite(
				Feature.ORE,
				new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.REDSTONE_ORE.defaultBlockState(), 8),
				FeatureDecorator.COUNT_RANGE,
				new DecoratorCountRange(8, 0, 0, 16)
			)
		);
		biome.addFeature(
			GenerationStep.Decoration.UNDERGROUND_ORES,
			Biome.makeComposite(
				Feature.ORE,
				new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.DIAMOND_ORE.defaultBlockState(), 8),
				FeatureDecorator.COUNT_RANGE,
				new DecoratorCountRange(1, 0, 0, 16)
			)
		);
		biome.addFeature(
			GenerationStep.Decoration.UNDERGROUND_ORES,
			Biome.makeComposite(
				Feature.ORE,
				new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.LAPIS_ORE.defaultBlockState(), 7),
				FeatureDecorator.COUNT_DEPTH_AVERAGE,
				new DepthAverageConfigation(1, 16, 16)
			)
		);
	}

	public static void addExtraGold(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.UNDERGROUND_ORES,
			Biome.makeComposite(
				Feature.ORE,
				new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.GOLD_ORE.defaultBlockState(), 9),
				FeatureDecorator.COUNT_RANGE,
				new DecoratorCountRange(20, 32, 32, 80)
			)
		);
	}

	public static void addExtraEmeralds(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.UNDERGROUND_ORES,
			Biome.makeComposite(
				Feature.EMERALD_ORE,
				new ReplaceBlockConfiguration(Blocks.STONE.defaultBlockState(), Blocks.EMERALD_ORE.defaultBlockState()),
				FeatureDecorator.EMERALD_ORE,
				DecoratorConfiguration.NONE
			)
		);
	}

	public static void addInfestedStone(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			Biome.makeComposite(
				Feature.ORE,
				new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.INFESTED_STONE.defaultBlockState(), 9),
				FeatureDecorator.COUNT_RANGE,
				new DecoratorCountRange(7, 0, 0, 64)
			)
		);
	}

	public static void addDefaultSoftDisks(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.UNDERGROUND_ORES,
			Biome.makeComposite(
				Feature.DISK,
				new DiskConfiguration(
					Blocks.SAND.defaultBlockState(), 7, 2, Lists.<BlockState>newArrayList(Blocks.DIRT.defaultBlockState(), Blocks.GRASS_BLOCK.defaultBlockState())
				),
				FeatureDecorator.COUNT_TOP_SOLID,
				new DecoratorFrequency(3)
			)
		);
		biome.addFeature(
			GenerationStep.Decoration.UNDERGROUND_ORES,
			Biome.makeComposite(
				Feature.DISK,
				new DiskConfiguration(
					Blocks.CLAY.defaultBlockState(), 4, 1, Lists.<BlockState>newArrayList(Blocks.DIRT.defaultBlockState(), Blocks.CLAY.defaultBlockState())
				),
				FeatureDecorator.COUNT_TOP_SOLID,
				new DecoratorFrequency(1)
			)
		);
		biome.addFeature(
			GenerationStep.Decoration.UNDERGROUND_ORES,
			Biome.makeComposite(
				Feature.DISK,
				new DiskConfiguration(
					Blocks.GRAVEL.defaultBlockState(), 6, 2, Lists.<BlockState>newArrayList(Blocks.DIRT.defaultBlockState(), Blocks.GRASS_BLOCK.defaultBlockState())
				),
				FeatureDecorator.COUNT_TOP_SOLID,
				new DecoratorFrequency(1)
			)
		);
	}

	public static void addSwampClayDisk(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.UNDERGROUND_ORES,
			Biome.makeComposite(
				Feature.DISK,
				new DiskConfiguration(
					Blocks.CLAY.defaultBlockState(), 4, 1, Lists.<BlockState>newArrayList(Blocks.DIRT.defaultBlockState(), Blocks.CLAY.defaultBlockState())
				),
				FeatureDecorator.COUNT_TOP_SOLID,
				new DecoratorFrequency(1)
			)
		);
	}

	public static void addMossyStoneBlock(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.LOCAL_MODIFICATIONS,
			Biome.makeComposite(
				Feature.FOREST_ROCK, new BlockBlobConfiguration(Blocks.MOSSY_COBBLESTONE.defaultBlockState(), 0), FeatureDecorator.FOREST_ROCK, new DecoratorFrequency(3)
			)
		);
	}

	public static void addFerns(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.DOUBLE_PLANT, new DoublePlantConfiguration(Blocks.LARGE_FERN.defaultBlockState()), FeatureDecorator.COUNT_HEIGHTMAP_32, new DecoratorFrequency(7)
			)
		);
	}

	public static void addBerryBushes(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(Feature.SWEET_BERRY_BUSH, FeatureConfiguration.NONE, FeatureDecorator.CHANCE_HEIGHTMAP_DOUBLE, new DecoratorChance(12))
		);
	}

	public static void addSparseBerryBushes(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(Feature.SWEET_BERRY_BUSH, FeatureConfiguration.NONE, FeatureDecorator.COUNT_HEIGHTMAP_DOUBLE, new DecoratorFrequency(1))
		);
	}

	public static void addLightBambooVegetation(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(Feature.BAMBOO, new ProbabilityFeatureConfiguration(0.0F), FeatureDecorator.COUNT_HEIGHTMAP_DOUBLE, new DecoratorFrequency(16))
		);
	}

	public static void addBambooVegetation(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.BAMBOO,
				new ProbabilityFeatureConfiguration(0.2F),
				FeatureDecorator.TOP_SOLID_HEIGHTMAP_NOISE_BIASED,
				new DecoratorNoiseCountFactor(160, 80.0, 0.3, Heightmap.Types.WORLD_SURFACE_WG)
			)
		);
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.RANDOM_SELECTOR,
				new RandomFeatureConfig(
					new Feature[]{Feature.FANCY_TREE, Feature.JUNGLE_GROUND_BUSH, Feature.MEGA_JUNGLE_TREE},
					new FeatureConfiguration[]{FeatureConfiguration.NONE, FeatureConfiguration.NONE, FeatureConfiguration.NONE},
					new float[]{0.05F, 0.15F, 0.7F},
					Feature.JUNGLE_GRASS,
					FeatureConfiguration.NONE
				),
				FeatureDecorator.COUNT_EXTRA_HEIGHTMAP,
				new DecoratorFrequencyWithExtraChance(30, 0.1F, 1)
			)
		);
	}

	public static void addTaigaTrees(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.RANDOM_SELECTOR,
				new RandomFeatureConfig(
					new Feature[]{Feature.PINE_TREE},
					new FeatureConfiguration[]{FeatureConfiguration.NONE},
					new float[]{0.33333334F},
					Feature.SPRUCE_TREE,
					FeatureConfiguration.NONE
				),
				FeatureDecorator.COUNT_EXTRA_HEIGHTMAP,
				new DecoratorFrequencyWithExtraChance(10, 0.1F, 1)
			)
		);
	}

	public static void addWaterTrees(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.RANDOM_SELECTOR,
				new RandomFeatureConfig(
					new Feature[]{Feature.FANCY_TREE},
					new FeatureConfiguration[]{FeatureConfiguration.NONE},
					new float[]{0.1F},
					Feature.NORMAL_TREE,
					FeatureConfiguration.NONE
				),
				FeatureDecorator.COUNT_EXTRA_HEIGHTMAP,
				new DecoratorFrequencyWithExtraChance(0, 0.1F, 1)
			)
		);
	}

	public static void addBirchTrees(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.BIRCH_TREE, FeatureConfiguration.NONE, FeatureDecorator.COUNT_EXTRA_HEIGHTMAP, new DecoratorFrequencyWithExtraChance(10, 0.1F, 1)
			)
		);
	}

	public static void addOtherBirchTrees(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.RANDOM_SELECTOR,
				new RandomFeatureConfig(
					new Feature[]{Feature.BIRCH_TREE, Feature.FANCY_TREE},
					new FeatureConfiguration[]{FeatureConfiguration.NONE, FeatureConfiguration.NONE},
					new float[]{0.2F, 0.1F},
					Feature.NORMAL_TREE,
					FeatureConfiguration.NONE
				),
				FeatureDecorator.COUNT_EXTRA_HEIGHTMAP,
				new DecoratorFrequencyWithExtraChance(10, 0.1F, 1)
			)
		);
	}

	public static void addTallBirchTrees(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.RANDOM_SELECTOR,
				new RandomFeatureConfig(
					new Feature[]{Feature.SUPER_BIRCH_TREE},
					new FeatureConfiguration[]{FeatureConfiguration.NONE},
					new float[]{0.5F},
					Feature.BIRCH_TREE,
					FeatureConfiguration.NONE
				),
				FeatureDecorator.COUNT_EXTRA_HEIGHTMAP,
				new DecoratorFrequencyWithExtraChance(10, 0.1F, 1)
			)
		);
	}

	public static void addSavannaTrees(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.RANDOM_SELECTOR,
				new RandomFeatureConfig(
					new Feature[]{Feature.SAVANNA_TREE},
					new FeatureConfiguration[]{FeatureConfiguration.NONE},
					new float[]{0.8F},
					Feature.NORMAL_TREE,
					FeatureConfiguration.NONE
				),
				FeatureDecorator.COUNT_EXTRA_HEIGHTMAP,
				new DecoratorFrequencyWithExtraChance(1, 0.1F, 1)
			)
		);
	}

	public static void addShatteredSavannaTrees(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.RANDOM_SELECTOR,
				new RandomFeatureConfig(
					new Feature[]{Feature.SAVANNA_TREE},
					new FeatureConfiguration[]{FeatureConfiguration.NONE},
					new float[]{0.8F},
					Feature.NORMAL_TREE,
					FeatureConfiguration.NONE
				),
				FeatureDecorator.COUNT_EXTRA_HEIGHTMAP,
				new DecoratorFrequencyWithExtraChance(2, 0.1F, 1)
			)
		);
	}

	public static void addMountainTrees(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.RANDOM_SELECTOR,
				new RandomFeatureConfig(
					new Feature[]{Feature.SPRUCE_TREE, Feature.FANCY_TREE},
					new FeatureConfiguration[]{FeatureConfiguration.NONE, FeatureConfiguration.NONE},
					new float[]{0.666F, 0.1F},
					Feature.NORMAL_TREE,
					FeatureConfiguration.NONE
				),
				FeatureDecorator.COUNT_EXTRA_HEIGHTMAP,
				new DecoratorFrequencyWithExtraChance(0, 0.1F, 1)
			)
		);
	}

	public static void addMountainEdgeTrees(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.RANDOM_SELECTOR,
				new RandomFeatureConfig(
					new Feature[]{Feature.SPRUCE_TREE, Feature.FANCY_TREE},
					new FeatureConfiguration[]{FeatureConfiguration.NONE, FeatureConfiguration.NONE},
					new float[]{0.666F, 0.1F},
					Feature.NORMAL_TREE,
					FeatureConfiguration.NONE
				),
				FeatureDecorator.COUNT_EXTRA_HEIGHTMAP,
				new DecoratorFrequencyWithExtraChance(3, 0.1F, 1)
			)
		);
	}

	public static void addJungleTrees(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.RANDOM_SELECTOR,
				new RandomFeatureConfig(
					new Feature[]{Feature.FANCY_TREE, Feature.JUNGLE_GROUND_BUSH, Feature.MEGA_JUNGLE_TREE},
					new FeatureConfiguration[]{FeatureConfiguration.NONE, FeatureConfiguration.NONE, FeatureConfiguration.NONE},
					new float[]{0.1F, 0.5F, 0.33333334F},
					Feature.JUNGLE_TREE,
					FeatureConfiguration.NONE
				),
				FeatureDecorator.COUNT_EXTRA_HEIGHTMAP,
				new DecoratorFrequencyWithExtraChance(50, 0.1F, 1)
			)
		);
	}

	public static void addJungleEdgeTrees(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.RANDOM_SELECTOR,
				new RandomFeatureConfig(
					new Feature[]{Feature.FANCY_TREE, Feature.JUNGLE_GROUND_BUSH},
					new FeatureConfiguration[]{FeatureConfiguration.NONE, FeatureConfiguration.NONE},
					new float[]{0.1F, 0.5F},
					Feature.JUNGLE_TREE,
					FeatureConfiguration.NONE
				),
				FeatureDecorator.COUNT_EXTRA_HEIGHTMAP,
				new DecoratorFrequencyWithExtraChance(2, 0.1F, 1)
			)
		);
	}

	public static void addBadlandsTrees(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.NORMAL_TREE, FeatureConfiguration.NONE, FeatureDecorator.COUNT_EXTRA_HEIGHTMAP, new DecoratorFrequencyWithExtraChance(5, 0.1F, 1)
			)
		);
	}

	public static void addSnowyTrees(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.SPRUCE_TREE, FeatureConfiguration.NONE, FeatureDecorator.COUNT_EXTRA_HEIGHTMAP, new DecoratorFrequencyWithExtraChance(0, 0.1F, 1)
			)
		);
	}

	public static void addGiantSpruceTrees(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.RANDOM_SELECTOR,
				new RandomFeatureConfig(
					new Feature[]{Feature.MEGA_SPRUCE_TREE, Feature.PINE_TREE},
					new FeatureConfiguration[]{FeatureConfiguration.NONE, FeatureConfiguration.NONE},
					new float[]{0.33333334F, 0.33333334F},
					Feature.SPRUCE_TREE,
					FeatureConfiguration.NONE
				),
				FeatureDecorator.COUNT_EXTRA_HEIGHTMAP,
				new DecoratorFrequencyWithExtraChance(10, 0.1F, 1)
			)
		);
	}

	public static void addGiantTrees(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.RANDOM_SELECTOR,
				new RandomFeatureConfig(
					new Feature[]{Feature.MEGA_SPRUCE_TREE, Feature.MEGA_PINE_TREE, Feature.PINE_TREE},
					new FeatureConfiguration[]{FeatureConfiguration.NONE, FeatureConfiguration.NONE, FeatureConfiguration.NONE},
					new float[]{0.025641026F, 0.30769232F, 0.33333334F},
					Feature.SPRUCE_TREE,
					FeatureConfiguration.NONE
				),
				FeatureDecorator.COUNT_EXTRA_HEIGHTMAP,
				new DecoratorFrequencyWithExtraChance(10, 0.1F, 1)
			)
		);
	}

	public static void addJungleGrass(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(Feature.JUNGLE_GRASS, FeatureConfiguration.NONE, FeatureDecorator.COUNT_HEIGHTMAP_DOUBLE, new DecoratorFrequency(25))
		);
	}

	public static void addSavannaGrass(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.DOUBLE_PLANT, new DoublePlantConfiguration(Blocks.TALL_GRASS.defaultBlockState()), FeatureDecorator.COUNT_HEIGHTMAP_32, new DecoratorFrequency(7)
			)
		);
	}

	public static void addShatteredSavannaGrass(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.GRASS, new GrassConfiguration(Blocks.GRASS.defaultBlockState()), FeatureDecorator.COUNT_HEIGHTMAP_DOUBLE, new DecoratorFrequency(5)
			)
		);
	}

	public static void addSavannaExtraGrass(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.GRASS, new GrassConfiguration(Blocks.GRASS.defaultBlockState()), FeatureDecorator.COUNT_HEIGHTMAP_DOUBLE, new DecoratorFrequency(20)
			)
		);
	}

	public static void addBadlandGrass(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.GRASS, new GrassConfiguration(Blocks.GRASS.defaultBlockState()), FeatureDecorator.COUNT_HEIGHTMAP_DOUBLE, new DecoratorFrequency(1)
			)
		);
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(Feature.DEAD_BUSH, FeatureConfiguration.NONE, FeatureDecorator.COUNT_HEIGHTMAP_DOUBLE, new DecoratorFrequency(20))
		);
	}

	public static void addForestFlowers(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.RANDOM_RANDOM_SELECTOR,
				new RandomRandomFeatureConfig(
					new Feature[]{Feature.DOUBLE_PLANT, Feature.DOUBLE_PLANT, Feature.DOUBLE_PLANT, Feature.GENERAL_FOREST_FLOWER},
					new FeatureConfiguration[]{
						new DoublePlantConfiguration(Blocks.LILAC.defaultBlockState()),
						new DoublePlantConfiguration(Blocks.ROSE_BUSH.defaultBlockState()),
						new DoublePlantConfiguration(Blocks.PEONY.defaultBlockState()),
						FeatureConfiguration.NONE
					},
					0
				),
				FeatureDecorator.COUNT_HEIGHTMAP_32,
				new DecoratorFrequency(5)
			)
		);
	}

	public static void addForestGrass(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.GRASS, new GrassConfiguration(Blocks.GRASS.defaultBlockState()), FeatureDecorator.COUNT_HEIGHTMAP_DOUBLE, new DecoratorFrequency(2)
			)
		);
	}

	public static void addSwampVegetation(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(Feature.SWAMP_TREE, FeatureConfiguration.NONE, FeatureDecorator.COUNT_EXTRA_HEIGHTMAP, new DecoratorFrequencyWithExtraChance(2, 0.1F, 1))
		);
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(Feature.SWAMP_FLOWER, FeatureConfiguration.NONE, FeatureDecorator.COUNT_HEIGHTMAP_32, new DecoratorFrequency(1))
		);
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.GRASS, new GrassConfiguration(Blocks.GRASS.defaultBlockState()), FeatureDecorator.COUNT_HEIGHTMAP_DOUBLE, new DecoratorFrequency(5)
			)
		);
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(Feature.DEAD_BUSH, FeatureConfiguration.NONE, FeatureDecorator.COUNT_HEIGHTMAP_DOUBLE, new DecoratorFrequency(1))
		);
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(Feature.WATERLILY, FeatureConfiguration.NONE, FeatureDecorator.COUNT_HEIGHTMAP_DOUBLE, new DecoratorFrequency(4))
		);
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.BUSH,
				new BushConfiguration(Blocks.BROWN_MUSHROOM.defaultBlockState()),
				FeatureDecorator.COUNT_CHANCE_HEIGHTMAP,
				new DecoratorFrequencyChance(8, 0.25F)
			)
		);
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.BUSH,
				new BushConfiguration(Blocks.RED_MUSHROOM.defaultBlockState()),
				FeatureDecorator.COUNT_CHANCE_HEIGHTMAP_DOUBLE,
				new DecoratorFrequencyChance(8, 0.125F)
			)
		);
	}

	public static void addMushroomFieldVegetation(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.RANDOM_BOOLEAN_SELECTOR,
				new RandomBooleanFeatureConfig(
					Feature.HUGE_RED_MUSHROOM, new HugeMushroomFeatureConfig(false), Feature.HUGE_BROWN_MUSHROOM, new HugeMushroomFeatureConfig(false)
				),
				FeatureDecorator.COUNT_HEIGHTMAP,
				new DecoratorFrequency(1)
			)
		);
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.BUSH,
				new BushConfiguration(Blocks.BROWN_MUSHROOM.defaultBlockState()),
				FeatureDecorator.COUNT_CHANCE_HEIGHTMAP,
				new DecoratorFrequencyChance(1, 0.25F)
			)
		);
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.BUSH,
				new BushConfiguration(Blocks.RED_MUSHROOM.defaultBlockState()),
				FeatureDecorator.COUNT_CHANCE_HEIGHTMAP_DOUBLE,
				new DecoratorFrequencyChance(1, 0.125F)
			)
		);
	}

	public static void addPlainVegetation(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.RANDOM_SELECTOR,
				new RandomFeatureConfig(
					new Feature[]{Feature.FANCY_TREE},
					new FeatureConfiguration[]{FeatureConfiguration.NONE},
					new float[]{0.33333334F},
					Feature.NORMAL_TREE,
					FeatureConfiguration.NONE
				),
				FeatureDecorator.COUNT_EXTRA_HEIGHTMAP,
				new DecoratorFrequencyWithExtraChance(0, 0.05F, 1)
			)
		);
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(Feature.PLAIN_FLOWER, FeatureConfiguration.NONE, FeatureDecorator.NOISE_HEIGHTMAP_32, new DecoratorNoiseDependant(-0.8, 15, 4))
		);
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.GRASS, new GrassConfiguration(Blocks.GRASS.defaultBlockState()), FeatureDecorator.NOISE_HEIGHTMAP_DOUBLE, new DecoratorNoiseDependant(-0.8, 5, 10)
			)
		);
	}

	public static void addDesertVegetation(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(Feature.DEAD_BUSH, FeatureConfiguration.NONE, FeatureDecorator.COUNT_HEIGHTMAP_DOUBLE, new DecoratorFrequency(2))
		);
	}

	public static void addGiantTaigaVegetation(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(Feature.TAIGA_GRASS, FeatureConfiguration.NONE, FeatureDecorator.COUNT_HEIGHTMAP_DOUBLE, new DecoratorFrequency(7))
		);
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(Feature.DEAD_BUSH, FeatureConfiguration.NONE, FeatureDecorator.COUNT_HEIGHTMAP_DOUBLE, new DecoratorFrequency(1))
		);
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.BUSH,
				new BushConfiguration(Blocks.BROWN_MUSHROOM.defaultBlockState()),
				FeatureDecorator.COUNT_CHANCE_HEIGHTMAP,
				new DecoratorFrequencyChance(3, 0.25F)
			)
		);
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.BUSH,
				new BushConfiguration(Blocks.RED_MUSHROOM.defaultBlockState()),
				FeatureDecorator.COUNT_CHANCE_HEIGHTMAP_DOUBLE,
				new DecoratorFrequencyChance(3, 0.125F)
			)
		);
	}

	public static void addDefaultFlowers(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(Feature.DEFAULT_FLOWER, FeatureConfiguration.NONE, FeatureDecorator.COUNT_HEIGHTMAP_32, new DecoratorFrequency(2))
		);
	}

	public static void addWarmFlowers(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(Feature.DEFAULT_FLOWER, FeatureConfiguration.NONE, FeatureDecorator.COUNT_HEIGHTMAP_32, new DecoratorFrequency(4))
		);
	}

	public static void addDefaultGrass(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.GRASS, new GrassConfiguration(Blocks.GRASS.defaultBlockState()), FeatureDecorator.COUNT_HEIGHTMAP_DOUBLE, new DecoratorFrequency(1)
			)
		);
	}

	public static void addTaigaGrass(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(Feature.TAIGA_GRASS, FeatureConfiguration.NONE, FeatureDecorator.COUNT_HEIGHTMAP_DOUBLE, new DecoratorFrequency(1))
		);
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.BUSH,
				new BushConfiguration(Blocks.BROWN_MUSHROOM.defaultBlockState()),
				FeatureDecorator.COUNT_CHANCE_HEIGHTMAP,
				new DecoratorFrequencyChance(1, 0.25F)
			)
		);
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.BUSH,
				new BushConfiguration(Blocks.RED_MUSHROOM.defaultBlockState()),
				FeatureDecorator.COUNT_CHANCE_HEIGHTMAP_DOUBLE,
				new DecoratorFrequencyChance(1, 0.125F)
			)
		);
	}

	public static void addPlainGrass(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.DOUBLE_PLANT,
				new DoublePlantConfiguration(Blocks.TALL_GRASS.defaultBlockState()),
				FeatureDecorator.NOISE_HEIGHTMAP_32,
				new DecoratorNoiseDependant(-0.8, 0, 7)
			)
		);
	}

	public static void addDefaultMushrooms(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.BUSH, new BushConfiguration(Blocks.BROWN_MUSHROOM.defaultBlockState()), FeatureDecorator.CHANCE_HEIGHTMAP_DOUBLE, new DecoratorChance(4)
			)
		);
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.BUSH, new BushConfiguration(Blocks.RED_MUSHROOM.defaultBlockState()), FeatureDecorator.CHANCE_HEIGHTMAP_DOUBLE, new DecoratorChance(8)
			)
		);
	}

	public static void addDefaultExtraVegetation(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(Feature.REED, FeatureConfiguration.NONE, FeatureDecorator.COUNT_HEIGHTMAP_DOUBLE, new DecoratorFrequency(10))
		);
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(Feature.PUMPKIN, FeatureConfiguration.NONE, FeatureDecorator.CHANCE_HEIGHTMAP_DOUBLE, new DecoratorChance(32))
		);
	}

	public static void addBadlandExtraVegetation(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(Feature.REED, FeatureConfiguration.NONE, FeatureDecorator.COUNT_HEIGHTMAP_DOUBLE, new DecoratorFrequency(13))
		);
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(Feature.PUMPKIN, FeatureConfiguration.NONE, FeatureDecorator.CHANCE_HEIGHTMAP_DOUBLE, new DecoratorChance(32))
		);
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(Feature.CACTUS, FeatureConfiguration.NONE, FeatureDecorator.COUNT_HEIGHTMAP_DOUBLE, new DecoratorFrequency(5))
		);
	}

	public static void addJungleExtraVegetation(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(Feature.MELON, FeatureConfiguration.NONE, FeatureDecorator.COUNT_HEIGHTMAP_DOUBLE, new DecoratorFrequency(1))
		);
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(Feature.VINES, FeatureConfiguration.NONE, FeatureDecorator.COUNT_HEIGHT_64, new DecoratorFrequency(50))
		);
	}

	public static void addDesertExtraVegetation(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(Feature.REED, FeatureConfiguration.NONE, FeatureDecorator.COUNT_HEIGHTMAP_DOUBLE, new DecoratorFrequency(60))
		);
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(Feature.PUMPKIN, FeatureConfiguration.NONE, FeatureDecorator.CHANCE_HEIGHTMAP_DOUBLE, new DecoratorChance(32))
		);
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(Feature.CACTUS, FeatureConfiguration.NONE, FeatureDecorator.COUNT_HEIGHTMAP_DOUBLE, new DecoratorFrequency(10))
		);
	}

	public static void addSwampExtraVegetation(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(Feature.REED, FeatureConfiguration.NONE, FeatureDecorator.COUNT_HEIGHTMAP_DOUBLE, new DecoratorFrequency(20))
		);
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(Feature.PUMPKIN, FeatureConfiguration.NONE, FeatureDecorator.CHANCE_HEIGHTMAP_DOUBLE, new DecoratorChance(32))
		);
	}

	public static void addDesertExtraDecoration(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			Biome.makeComposite(Feature.DESERT_WELL, FeatureConfiguration.NONE, FeatureDecorator.CHANCE_HEIGHTMAP, new DecoratorChance(1000))
		);
		biome.addFeature(
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			Biome.makeComposite(Feature.FOSSIL, FeatureConfiguration.NONE, FeatureDecorator.CHANCE_PASSTHROUGH, new DecoratorChance(64))
		);
	}

	public static void addSwampExtraDecoration(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			Biome.makeComposite(Feature.FOSSIL, FeatureConfiguration.NONE, FeatureDecorator.CHANCE_PASSTHROUGH, new DecoratorChance(64))
		);
	}

	public static void addColdOceanExtraVegetation(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.KELP,
				FeatureConfiguration.NONE,
				FeatureDecorator.TOP_SOLID_HEIGHTMAP_NOISE_BIASED,
				new DecoratorNoiseCountFactor(120, 80.0, 0.0, Heightmap.Types.OCEAN_FLOOR_WG)
			)
		);
	}

	public static void addDefaultSeagrass(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.SIMPLE_BLOCK,
				new SimpleBlockConfiguration(
					Blocks.SEAGRASS.defaultBlockState(),
					new BlockState[]{Blocks.STONE.defaultBlockState()},
					new BlockState[]{Blocks.WATER.defaultBlockState()},
					new BlockState[]{Blocks.WATER.defaultBlockState()}
				),
				FeatureDecorator.CARVING_MASK,
				new DecoratorCarvingMaskConfig(GenerationStep.Carving.LIQUID, 0.1F)
			)
		);
	}

	public static void addWarmSeagrass(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(Feature.SEAGRASS, new SeagrassFeatureConfiguration(80, 0.3), FeatureDecorator.TOP_SOLID_HEIGHTMAP, DecoratorConfiguration.NONE)
		);
	}

	public static void addDeepWarmSeagrass(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(Feature.SEAGRASS, new SeagrassFeatureConfiguration(80, 0.8), FeatureDecorator.TOP_SOLID_HEIGHTMAP, DecoratorConfiguration.NONE)
		);
	}

	public static void addLukeWarmKelp(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.KELP,
				FeatureConfiguration.NONE,
				FeatureDecorator.TOP_SOLID_HEIGHTMAP_NOISE_BIASED,
				new DecoratorNoiseCountFactor(80, 80.0, 0.0, Heightmap.Types.OCEAN_FLOOR_WG)
			)
		);
	}

	public static void addDefaultSprings(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.SPRING, new SpringConfiguration(Fluids.WATER.defaultFluidState()), FeatureDecorator.COUNT_BIASED_RANGE, new DecoratorCountRange(50, 8, 8, 256)
			)
		);
		biome.addFeature(
			GenerationStep.Decoration.VEGETAL_DECORATION,
			Biome.makeComposite(
				Feature.SPRING, new SpringConfiguration(Fluids.LAVA.defaultFluidState()), FeatureDecorator.COUNT_VERY_BIASED_RANGE, new DecoratorCountRange(20, 8, 16, 256)
			)
		);
	}

	public static void addIcebergs(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.LOCAL_MODIFICATIONS,
			Biome.makeComposite(Feature.ICEBERG, new IcebergConfiguration(Blocks.PACKED_ICE.defaultBlockState()), FeatureDecorator.ICEBERG, new DecoratorChance(16))
		);
		biome.addFeature(
			GenerationStep.Decoration.LOCAL_MODIFICATIONS,
			Biome.makeComposite(Feature.ICEBERG, new IcebergConfiguration(Blocks.BLUE_ICE.defaultBlockState()), FeatureDecorator.ICEBERG, new DecoratorChance(200))
		);
	}

	public static void addBlueIce(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			Biome.makeComposite(Feature.BLUE_ICE, FeatureConfiguration.NONE, FeatureDecorator.RANDOM_COUNT_RANGE, new DecoratorCountRange(20, 30, 32, 64))
		);
	}

	public static void addSurfaceFreezing(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.TOP_LAYER_MODIFICATION,
			Biome.makeComposite(Feature.FREEZE_TOP_LAYER, FeatureConfiguration.NONE, FeatureDecorator.NOPE, DecoratorConfiguration.NONE)
		);
	}

	public static void addEndCity(Biome biome) {
		biome.addFeature(
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			Biome.makeComposite(Feature.END_CITY, FeatureConfiguration.NONE, FeatureDecorator.NOPE, DecoratorConfiguration.NONE)
		);
	}
}
