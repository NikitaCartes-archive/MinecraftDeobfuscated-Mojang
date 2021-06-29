package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.OptionalInt;
import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.BiasedToBottomInt;
import net.minecraft.util.valueproviders.ClampedInt;
import net.minecraft.util.valueproviders.ClampedNormalFloat;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.BigDripleafBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.CaveVinesBlock;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.SmallDripleafBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FossilFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.HugeFungusConfiguration;
import net.minecraft.world.level.levelgen.feature.blockplacers.ColumnPlacer;
import net.minecraft.world.level.levelgen.feature.blockplacers.DoublePlantPlacer;
import net.minecraft.world.level.levelgen.feature.blockplacers.SimpleBlockPlacer;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ColumnFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DripstoneClusterConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.EndGatewayConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.GlowLichenConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.GrowingPlantConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.HeightmapConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.LargeDripstoneConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoiseDependantDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceSphereConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RootSystemConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SmallDripstoneConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.UnderwaterMagmaConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.ThreeLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.AcaciaFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BushFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.DarkOakFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FancyFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.MegaJungleFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.MegaPineFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.PineFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.RandomSpreadFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.SpruceFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.ForestFlowerProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.PlainFlowerProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RandomizedIntStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RotatedBlockProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.AlterGroundDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.BeehiveDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.CocoaDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.LeaveVineDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TrunkVineDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.BendingTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.DarkOakTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.FancyTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.ForkingTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.GiantTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.MegaJungleTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.heightproviders.BiasedToBottomHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.heightproviders.VeryBiasedToBottomHeight;
import net.minecraft.world.level.levelgen.placement.CarvingMaskDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.CaveDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.ChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.ConfiguredDecorator;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.placement.FrequencyWithExtraChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.NoiseCountFactorDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.WaterDepthThresholdConfiguration;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class Features {
	public static final ConfiguredFeature<?, ?> END_SPIKE = register(
		"end_spike", Feature.END_SPIKE.configured(new SpikeConfiguration(false, ImmutableList.of(), null))
	);
	public static final ConfiguredFeature<?, ?> END_GATEWAY = register(
		"end_gateway",
		Feature.END_GATEWAY
			.configured(EndGatewayConfiguration.knownExit(ServerLevel.END_SPAWN_POINT, true))
			.decorated(FeatureDecorator.END_GATEWAY.configured(DecoratorConfiguration.NONE))
			.decorated(Features.Decorators.HEIGHTMAP)
			.squared()
			.rarity(700)
	);
	public static final ConfiguredFeature<?, ?> END_GATEWAY_DELAYED = register(
		"end_gateway_delayed", Feature.END_GATEWAY.configured(EndGatewayConfiguration.delayedExitSearch())
	);
	public static final ConfiguredFeature<?, ?> CHORUS_PLANT = register(
		"chorus_plant", Feature.CHORUS_PLANT.configured(FeatureConfiguration.NONE).decorated(Features.Decorators.HEIGHTMAP_SQUARE).countRandom(4)
	);
	public static final ConfiguredFeature<?, ?> END_ISLAND = register("end_island", Feature.END_ISLAND.configured(FeatureConfiguration.NONE));
	public static final ConfiguredFeature<?, ?> END_ISLAND_DECORATED = register(
		"end_island_decorated",
		END_ISLAND.rangeUniform(VerticalAnchor.absolute(55), VerticalAnchor.absolute(70))
			.squared()
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(1, 0.25F, 1)))
			.rarity(14)
	);
	public static final ConfiguredFeature<?, ?> DELTA = register(
		"delta",
		Feature.DELTA_FEATURE
			.configured(new DeltaFeatureConfiguration(Features.States.LAVA, Features.States.MAGMA_BLOCK, UniformInt.of(3, 7), UniformInt.of(0, 2)))
			.decorated(FeatureDecorator.COUNT_MULTILAYER.configured(new CountConfiguration(40)))
	);
	public static final ConfiguredFeature<?, ?> SMALL_BASALT_COLUMNS = register(
		"small_basalt_columns",
		Feature.BASALT_COLUMNS
			.configured(new ColumnFeatureConfiguration(ConstantInt.of(1), UniformInt.of(1, 4)))
			.decorated(FeatureDecorator.COUNT_MULTILAYER.configured(new CountConfiguration(4)))
	);
	public static final ConfiguredFeature<?, ?> LARGE_BASALT_COLUMNS = register(
		"large_basalt_columns",
		Feature.BASALT_COLUMNS
			.configured(new ColumnFeatureConfiguration(UniformInt.of(2, 3), UniformInt.of(5, 10)))
			.decorated(FeatureDecorator.COUNT_MULTILAYER.configured(new CountConfiguration(2)))
	);
	public static final ConfiguredFeature<?, ?> BASALT_BLOBS = register(
		"basalt_blobs",
		Feature.REPLACE_BLOBS
			.configured(new ReplaceSphereConfiguration(Features.States.NETHERRACK, Features.States.BASALT, UniformInt.of(3, 7)))
			.range(Features.Decorators.FULL_RANGE)
			.squared()
			.count(75)
	);
	public static final ConfiguredFeature<?, ?> BLACKSTONE_BLOBS = register(
		"blackstone_blobs",
		Feature.REPLACE_BLOBS
			.configured(new ReplaceSphereConfiguration(Features.States.NETHERRACK, Features.States.BLACKSTONE, UniformInt.of(3, 7)))
			.range(Features.Decorators.FULL_RANGE)
			.squared()
			.count(25)
	);
	public static final ConfiguredFeature<?, ?> GLOWSTONE_EXTRA = register(
		"glowstone_extra",
		Feature.GLOWSTONE_BLOB.configured(FeatureConfiguration.NONE).range(Features.Decorators.RANGE_4_4).squared().count(BiasedToBottomInt.of(0, 9))
	);
	public static final ConfiguredFeature<?, ?> GLOWSTONE = register(
		"glowstone", Feature.GLOWSTONE_BLOB.configured(FeatureConfiguration.NONE).range(Features.Decorators.FULL_RANGE).squared().count(10)
	);
	public static final ConfiguredFeature<?, ?> CRIMSON_FOREST_VEGETATION = register(
		"crimson_forest_vegetation",
		Feature.NETHER_FOREST_VEGETATION
			.configured(Features.Configs.CRIMSON_FOREST_CONFIG)
			.decorated(FeatureDecorator.COUNT_MULTILAYER.configured(new CountConfiguration(6)))
	);
	public static final ConfiguredFeature<?, ?> WARPED_FOREST_VEGETATION = register(
		"warped_forest_vegetation",
		Feature.NETHER_FOREST_VEGETATION
			.configured(Features.Configs.WARPED_FOREST_CONFIG)
			.decorated(FeatureDecorator.COUNT_MULTILAYER.configured(new CountConfiguration(5)))
	);
	public static final ConfiguredFeature<?, ?> NETHER_SPROUTS = register(
		"nether_sprouts",
		Feature.NETHER_FOREST_VEGETATION
			.configured(Features.Configs.NETHER_SPROUTS_CONFIG)
			.decorated(FeatureDecorator.COUNT_MULTILAYER.configured(new CountConfiguration(4)))
	);
	public static final ConfiguredFeature<?, ?> TWISTING_VINES = register(
		"twisting_vines", Feature.TWISTING_VINES.configured(FeatureConfiguration.NONE).range(Features.Decorators.FULL_RANGE).squared().count(10)
	);
	public static final ConfiguredFeature<?, ?> WEEPING_VINES = register(
		"weeping_vines", Feature.WEEPING_VINES.configured(FeatureConfiguration.NONE).range(Features.Decorators.FULL_RANGE).squared().count(10)
	);
	public static final ConfiguredFeature<?, ?> BASALT_PILLAR = register(
		"basalt_pillar", Feature.BASALT_PILLAR.configured(FeatureConfiguration.NONE).range(Features.Decorators.FULL_RANGE).squared().count(10)
	);
	public static final ConfiguredFeature<?, ?> SEAGRASS_COLD = register(
		"seagrass_cold", Feature.SEAGRASS.configured(new ProbabilityFeatureConfiguration(0.3F)).count(32).decorated(Features.Decorators.TOP_SOLID_HEIGHTMAP_SQUARE)
	);
	public static final ConfiguredFeature<?, ?> SEAGRASS_DEEP_COLD = register(
		"seagrass_deep_cold",
		Feature.SEAGRASS.configured(new ProbabilityFeatureConfiguration(0.8F)).count(40).decorated(Features.Decorators.TOP_SOLID_HEIGHTMAP_SQUARE)
	);
	public static final ConfiguredFeature<?, ?> SEAGRASS_NORMAL = register(
		"seagrass_normal", Feature.SEAGRASS.configured(new ProbabilityFeatureConfiguration(0.3F)).count(48).decorated(Features.Decorators.TOP_SOLID_HEIGHTMAP_SQUARE)
	);
	public static final ConfiguredFeature<?, ?> SEAGRASS_RIVER = register(
		"seagrass_river", Feature.SEAGRASS.configured(new ProbabilityFeatureConfiguration(0.4F)).count(48).decorated(Features.Decorators.TOP_SOLID_HEIGHTMAP_SQUARE)
	);
	public static final ConfiguredFeature<?, ?> SEAGRASS_DEEP = register(
		"seagrass_deep", Feature.SEAGRASS.configured(new ProbabilityFeatureConfiguration(0.8F)).count(48).decorated(Features.Decorators.TOP_SOLID_HEIGHTMAP_SQUARE)
	);
	public static final ConfiguredFeature<?, ?> SEAGRASS_SWAMP = register(
		"seagrass_swamp", Feature.SEAGRASS.configured(new ProbabilityFeatureConfiguration(0.6F)).count(64).decorated(Features.Decorators.TOP_SOLID_HEIGHTMAP_SQUARE)
	);
	public static final ConfiguredFeature<?, ?> SEAGRASS_WARM = register(
		"seagrass_warm", Feature.SEAGRASS.configured(new ProbabilityFeatureConfiguration(0.3F)).count(80).decorated(Features.Decorators.TOP_SOLID_HEIGHTMAP_SQUARE)
	);
	public static final ConfiguredFeature<?, ?> SEAGRASS_DEEP_WARM = register(
		"seagrass_deep_warm",
		Feature.SEAGRASS.configured(new ProbabilityFeatureConfiguration(0.8F)).count(80).decorated(Features.Decorators.TOP_SOLID_HEIGHTMAP_SQUARE)
	);
	public static final ConfiguredFeature<?, ?> SEA_PICKLE = register(
		"sea_pickle", Feature.SEA_PICKLE.configured(new CountConfiguration(20)).decorated(Features.Decorators.TOP_SOLID_HEIGHTMAP_SQUARE).rarity(16)
	);
	public static final ConfiguredFeature<?, ?> ICE_SPIKE = register(
		"ice_spike", Feature.ICE_SPIKE.configured(FeatureConfiguration.NONE).decorated(Features.Decorators.HEIGHTMAP_SQUARE).count(3)
	);
	public static final ConfiguredFeature<?, ?> ICE_PATCH = register(
		"ice_patch",
		Feature.ICE_PATCH
			.configured(
				new DiskConfiguration(
					Features.States.PACKED_ICE,
					UniformInt.of(2, 3),
					1,
					ImmutableList.of(
						Features.States.DIRT,
						Features.States.GRASS_BLOCK,
						Features.States.PODZOL,
						Features.States.COARSE_DIRT,
						Features.States.MYCELIUM,
						Features.States.SNOW_BLOCK,
						Features.States.ICE
					)
				)
			)
			.decorated(Features.Decorators.HEIGHTMAP_SQUARE)
			.count(2)
	);
	public static final ConfiguredFeature<?, ?> FOREST_ROCK = register(
		"forest_rock",
		Feature.FOREST_ROCK.configured(new BlockStateConfiguration(Features.States.MOSSY_COBBLESTONE)).decorated(Features.Decorators.HEIGHTMAP_SQUARE).countRandom(2)
	);
	public static final ConfiguredFeature<?, ?> SEAGRASS_SIMPLE = register(
		"seagrass_simple",
		Feature.SIMPLE_BLOCK
			.configured(
				new SimpleBlockConfiguration(
					new SimpleStateProvider(Features.States.SEAGRASS),
					ImmutableList.of(Features.States.STONE),
					ImmutableList.of(Features.States.WATER),
					ImmutableList.of(Features.States.WATER)
				)
			)
			.rarity(10)
			.decorated(FeatureDecorator.CARVING_MASK.configured(new CarvingMaskDecoratorConfiguration(GenerationStep.Carving.LIQUID)))
	);
	public static final ConfiguredFeature<?, ?> ICEBERG_PACKED = register(
		"iceberg_packed",
		Feature.ICEBERG
			.configured(new BlockStateConfiguration(Features.States.PACKED_ICE))
			.decorated(FeatureDecorator.ICEBERG.configured(NoneDecoratorConfiguration.INSTANCE))
			.rarity(16)
	);
	public static final ConfiguredFeature<?, ?> ICEBERG_BLUE = register(
		"iceberg_blue",
		Feature.ICEBERG
			.configured(new BlockStateConfiguration(Features.States.BLUE_ICE))
			.decorated(FeatureDecorator.ICEBERG.configured(NoneDecoratorConfiguration.INSTANCE))
			.rarity(200)
	);
	public static final ConfiguredFeature<?, ?> KELP_COLD = register(
		"kelp_cold",
		Feature.KELP
			.configured(FeatureConfiguration.NONE)
			.decorated(Features.Decorators.HEIGHTMAP_TOP_SOLID)
			.squared()
			.decorated(FeatureDecorator.COUNT_NOISE_BIASED.configured(new NoiseCountFactorDecoratorConfiguration(120, 80.0, 0.0)))
	);
	public static final ConfiguredFeature<?, ?> KELP_WARM = register(
		"kelp_warm",
		Feature.KELP
			.configured(FeatureConfiguration.NONE)
			.decorated(Features.Decorators.HEIGHTMAP_TOP_SOLID)
			.squared()
			.decorated(FeatureDecorator.COUNT_NOISE_BIASED.configured(new NoiseCountFactorDecoratorConfiguration(80, 80.0, 0.0)))
	);
	public static final ConfiguredFeature<?, ?> BLUE_ICE = register(
		"blue_ice",
		Feature.BLUE_ICE.configured(FeatureConfiguration.NONE).rangeUniform(VerticalAnchor.absolute(30), VerticalAnchor.absolute(61)).squared().countRandom(19)
	);
	public static final ConfiguredFeature<?, ?> BAMBOO_LIGHT = register(
		"bamboo_light", Feature.BAMBOO.configured(new ProbabilityFeatureConfiguration(0.0F)).decorated(Features.Decorators.HEIGHTMAP_DOUBLE_SQUARE).count(16)
	);
	public static final ConfiguredFeature<?, ?> BAMBOO = register(
		"bamboo",
		Feature.BAMBOO
			.configured(new ProbabilityFeatureConfiguration(0.2F))
			.decorated(Features.Decorators.HEIGHTMAP_WORLD_SURFACE)
			.squared()
			.decorated(FeatureDecorator.COUNT_NOISE_BIASED.configured(new NoiseCountFactorDecoratorConfiguration(160, 80.0, 0.3)))
	);
	public static final ConfiguredFeature<?, ?> VINES = register("vines", Feature.VINES.configured(FeatureConfiguration.NONE).squared().count(50));
	public static final ConfiguredFeature<?, ?> PROTOTYPE_VINES = register(
		"prototype_vines",
		Feature.VINES.configured(FeatureConfiguration.NONE).rangeUniform(VerticalAnchor.absolute(64), VerticalAnchor.absolute(100)).squared().count(127)
	);
	public static final ConfiguredFeature<?, ?> LAKE_WATER = register(
		"lake_water", Feature.LAKE.configured(new BlockStateConfiguration(Features.States.WATER)).range(Features.Decorators.FULL_RANGE).squared().rarity(4)
	);
	public static final ConfiguredFeature<?, ?> LAKE_LAVA = register(
		"lake_lava",
		Feature.LAKE
			.configured(new BlockStateConfiguration(Features.States.LAVA))
			.decorated(FeatureDecorator.LAVA_LAKE.configured(new ChanceDecoratorConfiguration(80)))
			.range(new RangeDecoratorConfiguration(BiasedToBottomHeight.of(VerticalAnchor.bottom(), VerticalAnchor.top(), 8)))
			.squared()
			.rarity(8)
	);
	public static final ConfiguredFeature<?, ?> DISK_CLAY = register(
		"disk_clay",
		Feature.DISK
			.configured(new DiskConfiguration(Features.States.CLAY, UniformInt.of(2, 3), 1, ImmutableList.of(Features.States.DIRT, Features.States.CLAY)))
			.decorated(Features.Decorators.TOP_SOLID_HEIGHTMAP_SQUARE)
	);
	public static final ConfiguredFeature<?, ?> DISK_GRAVEL = register(
		"disk_gravel",
		Feature.DISK
			.configured(new DiskConfiguration(Features.States.GRAVEL, UniformInt.of(2, 5), 2, ImmutableList.of(Features.States.DIRT, Features.States.GRASS_BLOCK)))
			.decorated(Features.Decorators.TOP_SOLID_HEIGHTMAP_SQUARE)
	);
	public static final ConfiguredFeature<?, ?> DISK_SAND = register(
		"disk_sand",
		Feature.DISK
			.configured(new DiskConfiguration(Features.States.SAND, UniformInt.of(2, 6), 2, ImmutableList.of(Features.States.DIRT, Features.States.GRASS_BLOCK)))
			.decorated(Features.Decorators.TOP_SOLID_HEIGHTMAP_SQUARE)
			.count(3)
	);
	public static final ConfiguredFeature<?, ?> FREEZE_TOP_LAYER = register("freeze_top_layer", Feature.FREEZE_TOP_LAYER.configured(FeatureConfiguration.NONE));
	public static final ConfiguredFeature<?, ?> BONUS_CHEST = register("bonus_chest", Feature.BONUS_CHEST.configured(FeatureConfiguration.NONE));
	public static final ConfiguredFeature<?, ?> VOID_START_PLATFORM = register(
		"void_start_platform", Feature.VOID_START_PLATFORM.configured(FeatureConfiguration.NONE)
	);
	public static final ConfiguredFeature<?, ?> MONSTER_ROOM = register(
		"monster_room", Feature.MONSTER_ROOM.configured(FeatureConfiguration.NONE).range(Features.Decorators.FULL_RANGE).squared().count(8)
	);
	public static final ConfiguredFeature<?, ?> WELL = register(
		"desert_well", Feature.DESERT_WELL.configured(FeatureConfiguration.NONE).decorated(Features.Decorators.HEIGHTMAP_SQUARE).rarity(1000)
	);
	private static final ImmutableList<ResourceLocation> FOSSIL_STRUCTURES = ImmutableList.of(
		new ResourceLocation("fossil/spine_1"),
		new ResourceLocation("fossil/spine_2"),
		new ResourceLocation("fossil/spine_3"),
		new ResourceLocation("fossil/spine_4"),
		new ResourceLocation("fossil/skull_1"),
		new ResourceLocation("fossil/skull_2"),
		new ResourceLocation("fossil/skull_3"),
		new ResourceLocation("fossil/skull_4")
	);
	private static final ImmutableList<ResourceLocation> FOSSIL_COAL_STRUCTURES = ImmutableList.of(
		new ResourceLocation("fossil/spine_1_coal"),
		new ResourceLocation("fossil/spine_2_coal"),
		new ResourceLocation("fossil/spine_3_coal"),
		new ResourceLocation("fossil/spine_4_coal"),
		new ResourceLocation("fossil/skull_1_coal"),
		new ResourceLocation("fossil/skull_2_coal"),
		new ResourceLocation("fossil/skull_3_coal"),
		new ResourceLocation("fossil/skull_4_coal")
	);
	public static final ConfiguredFeature<?, ?> FOSSIL = register(
		"fossil",
		Feature.FOSSIL
			.configured(new FossilFeatureConfiguration(FOSSIL_STRUCTURES, FOSSIL_COAL_STRUCTURES, ProcessorLists.FOSSIL_ROT, ProcessorLists.FOSSIL_COAL, 4))
			.rarity(64)
	);
	public static final ConfiguredFeature<?, ?> PROTOTYPE_FOSSIL_UPPER = register(
		"prototype_fossil_upper",
		Feature.FOSSIL
			.configured(new FossilFeatureConfiguration(FOSSIL_STRUCTURES, FOSSIL_COAL_STRUCTURES, ProcessorLists.FOSSIL_ROT, ProcessorLists.FOSSIL_COAL, 4))
			.rangeUniform(VerticalAnchor.absolute(0), VerticalAnchor.top())
			.rarity(64)
	);
	public static final ConfiguredFeature<?, ?> PROTOTYPE_FOSSIL_LOWER = register(
		"prototype_fossil_lower",
		Feature.FOSSIL
			.configured(new FossilFeatureConfiguration(FOSSIL_STRUCTURES, FOSSIL_COAL_STRUCTURES, ProcessorLists.FOSSIL_ROT, ProcessorLists.FOSSIL_DIAMONDS, 4))
			.rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(-8))
			.rarity(64)
	);
	public static final ConfiguredFeature<?, ?> SPRING_LAVA_DOUBLE = register(
		"spring_lava_double",
		Feature.SPRING
			.configured(Features.Configs.LAVA_SPRING_CONFIG)
			.range(new RangeDecoratorConfiguration(VeryBiasedToBottomHeight.of(VerticalAnchor.bottom(), VerticalAnchor.belowTop(8), 8)))
			.squared()
			.count(40)
	);
	public static final ConfiguredFeature<?, ?> SPRING_LAVA = register(
		"spring_lava",
		Feature.SPRING
			.configured(Features.Configs.LAVA_SPRING_CONFIG)
			.range(new RangeDecoratorConfiguration(VeryBiasedToBottomHeight.of(VerticalAnchor.bottom(), VerticalAnchor.belowTop(8), 8)))
			.squared()
			.count(20)
	);
	public static final ConfiguredFeature<?, ?> SPRING_DELTA = register(
		"spring_delta",
		Feature.SPRING
			.configured(
				new SpringConfiguration(
					Features.States.LAVA_STATE, true, 4, 1, ImmutableSet.of(Blocks.NETHERRACK, Blocks.SOUL_SAND, Blocks.GRAVEL, Blocks.MAGMA_BLOCK, Blocks.BLACKSTONE)
				)
			)
			.range(Features.Decorators.RANGE_4_4)
			.squared()
			.count(16)
	);
	public static final ConfiguredFeature<?, ?> SPRING_CLOSED = register(
		"spring_closed", Feature.SPRING.configured(Features.Configs.CLOSED_NETHER_SPRING_CONFIG).range(Features.Decorators.RANGE_10_10).squared().count(16)
	);
	public static final ConfiguredFeature<?, ?> SPRING_CLOSED_DOUBLE = register(
		"spring_closed_double", Feature.SPRING.configured(Features.Configs.CLOSED_NETHER_SPRING_CONFIG).range(Features.Decorators.RANGE_10_10).squared().count(32)
	);
	public static final ConfiguredFeature<?, ?> SPRING_OPEN = register(
		"spring_open",
		Feature.SPRING
			.configured(new SpringConfiguration(Features.States.LAVA_STATE, false, 4, 1, ImmutableSet.of(Blocks.NETHERRACK)))
			.range(Features.Decorators.RANGE_4_4)
			.squared()
			.count(8)
	);
	public static final ConfiguredFeature<?, ?> SPRING_WATER = register(
		"spring_water",
		Feature.SPRING
			.configured(new SpringConfiguration(Features.States.WATER_STATE, true, 4, 1, ImmutableSet.of(Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE)))
			.range(new RangeDecoratorConfiguration(BiasedToBottomHeight.of(VerticalAnchor.bottom(), VerticalAnchor.belowTop(8), 8)))
			.squared()
			.count(50)
	);
	public static final ConfiguredFeature<?, ?> PROTOTYPE_SPRING_WATER = register(
		"prototype_spring_water",
		Feature.SPRING
			.configured(
				new SpringConfiguration(
					Features.States.WATER_STATE, true, 4, 1, ImmutableSet.of(Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE, Blocks.DEEPSLATE, Blocks.TUFF)
				)
			)
			.rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.top())
			.squared()
			.count(50)
	);
	public static final ConfiguredFeature<?, ?> PILE_HAY = register(
		"pile_hay", Feature.BLOCK_PILE.configured(new BlockPileConfiguration(new RotatedBlockProvider(Blocks.HAY_BLOCK)))
	);
	public static final ConfiguredFeature<?, ?> PILE_MELON = register(
		"pile_melon", Feature.BLOCK_PILE.configured(new BlockPileConfiguration(new SimpleStateProvider(Features.States.MELON)))
	);
	public static final ConfiguredFeature<?, ?> PILE_SNOW = register(
		"pile_snow", Feature.BLOCK_PILE.configured(new BlockPileConfiguration(new SimpleStateProvider(Features.States.SNOW)))
	);
	public static final ConfiguredFeature<?, ?> PILE_ICE = register(
		"pile_ice",
		Feature.BLOCK_PILE
			.configured(
				new BlockPileConfiguration(new WeightedStateProvider(weightedBlockStateBuilder().add(Features.States.BLUE_ICE, 1).add(Features.States.PACKED_ICE, 5)))
			)
	);
	public static final ConfiguredFeature<?, ?> PILE_PUMPKIN = register(
		"pile_pumpkin",
		Feature.BLOCK_PILE
			.configured(
				new BlockPileConfiguration(new WeightedStateProvider(weightedBlockStateBuilder().add(Features.States.PUMPKIN, 19).add(Features.States.JACK_O_LANTERN, 1)))
			)
	);
	public static final ConfiguredFeature<?, ?> PATCH_FIRE = register(
		"patch_fire",
		Feature.RANDOM_PATCH
			.configured(
				new RandomPatchConfiguration.GrassConfigurationBuilder(new SimpleStateProvider(Features.States.FIRE), SimpleBlockPlacer.INSTANCE)
					.tries(64)
					.whitelist(ImmutableSet.of(Features.States.NETHERRACK.getBlock()))
					.noProjection()
					.build()
			)
			.decorated(Features.Decorators.FIRE)
	);
	public static final ConfiguredFeature<?, ?> PATCH_SOUL_FIRE = register(
		"patch_soul_fire",
		Feature.RANDOM_PATCH
			.configured(
				new RandomPatchConfiguration.GrassConfigurationBuilder(new SimpleStateProvider(Features.States.SOUL_FIRE), new SimpleBlockPlacer())
					.tries(64)
					.whitelist(ImmutableSet.of(Features.States.SOUL_SOIL.getBlock()))
					.noProjection()
					.build()
			)
			.decorated(Features.Decorators.FIRE)
	);
	public static final ConfiguredFeature<?, ?> PATCH_BROWN_MUSHROOM = register(
		"patch_brown_mushroom",
		Feature.RANDOM_PATCH
			.configured(
				new RandomPatchConfiguration.GrassConfigurationBuilder(new SimpleStateProvider(Features.States.BROWN_MUSHROOM), SimpleBlockPlacer.INSTANCE)
					.tries(64)
					.noProjection()
					.build()
			)
	);
	public static final ConfiguredFeature<?, ?> PATCH_RED_MUSHROOM = register(
		"patch_red_mushroom",
		Feature.RANDOM_PATCH
			.configured(
				new RandomPatchConfiguration.GrassConfigurationBuilder(new SimpleStateProvider(Features.States.RED_MUSHROOM), SimpleBlockPlacer.INSTANCE)
					.tries(64)
					.noProjection()
					.build()
			)
	);
	public static final ConfiguredFeature<?, ?> PATCH_CRIMSON_ROOTS = register(
		"patch_crimson_roots",
		Feature.RANDOM_PATCH
			.configured(
				new RandomPatchConfiguration.GrassConfigurationBuilder(new SimpleStateProvider(Features.States.CRIMSON_ROOTS), new SimpleBlockPlacer())
					.tries(64)
					.noProjection()
					.build()
			)
			.range(Features.Decorators.FULL_RANGE)
	);
	public static final ConfiguredFeature<?, ?> PATCH_SUNFLOWER = register(
		"patch_sunflower",
		Feature.RANDOM_PATCH
			.configured(
				new RandomPatchConfiguration.GrassConfigurationBuilder(new SimpleStateProvider(Features.States.SUNFLOWER), new DoublePlantPlacer())
					.tries(64)
					.noProjection()
					.build()
			)
			.decorated(Features.Decorators.ADD_32)
			.decorated(Features.Decorators.HEIGHTMAP_SQUARE)
			.count(10)
	);
	public static final ConfiguredFeature<?, ?> PATCH_PUMPKIN = register(
		"patch_pumpkin",
		Feature.RANDOM_PATCH
			.configured(
				new RandomPatchConfiguration.GrassConfigurationBuilder(new SimpleStateProvider(Features.States.PUMPKIN), SimpleBlockPlacer.INSTANCE)
					.tries(64)
					.whitelist(ImmutableSet.of(Features.States.GRASS_BLOCK.getBlock()))
					.noProjection()
					.build()
			)
			.decorated(Features.Decorators.HEIGHTMAP_DOUBLE_SQUARE)
			.rarity(32)
	);
	public static final ConfiguredFeature<?, ?> PATCH_TAIGA_GRASS = register(
		"patch_taiga_grass", Feature.RANDOM_PATCH.configured(Features.Configs.TAIGA_GRASS_CONFIG)
	);
	public static final ConfiguredFeature<?, ?> PATCH_BERRY_BUSH = register(
		"patch_berry_bush", Feature.RANDOM_PATCH.configured(Features.Configs.SWEET_BERRY_BUSH_CONFIG)
	);
	public static final ConfiguredFeature<?, ?> PATCH_GRASS_PLAIN = register(
		"patch_grass_plain",
		Feature.RANDOM_PATCH
			.configured(Features.Configs.DEFAULT_GRASS_CONFIG)
			.decorated(Features.Decorators.HEIGHTMAP_DOUBLE_SQUARE)
			.decorated(FeatureDecorator.COUNT_NOISE.configured(new NoiseDependantDecoratorConfiguration(-0.8, 5, 10)))
	);
	public static final ConfiguredFeature<?, ?> PATCH_GRASS_FOREST = register(
		"patch_grass_forest", Feature.RANDOM_PATCH.configured(Features.Configs.DEFAULT_GRASS_CONFIG).decorated(Features.Decorators.HEIGHTMAP_DOUBLE_SQUARE).count(2)
	);
	public static final ConfiguredFeature<?, ?> PATCH_GRASS_BADLANDS = register(
		"patch_grass_badlands", Feature.RANDOM_PATCH.configured(Features.Configs.DEFAULT_GRASS_CONFIG).decorated(Features.Decorators.HEIGHTMAP_DOUBLE_SQUARE)
	);
	public static final ConfiguredFeature<?, ?> PATCH_GRASS_SAVANNA = register(
		"patch_grass_savanna",
		Feature.RANDOM_PATCH.configured(Features.Configs.DEFAULT_GRASS_CONFIG).decorated(Features.Decorators.HEIGHTMAP_DOUBLE_SQUARE).count(20)
	);
	public static final ConfiguredFeature<?, ?> PATCH_GRASS_NORMAL = register(
		"patch_grass_normal", Feature.RANDOM_PATCH.configured(Features.Configs.DEFAULT_GRASS_CONFIG).decorated(Features.Decorators.HEIGHTMAP_DOUBLE_SQUARE).count(5)
	);
	public static final ConfiguredFeature<?, ?> PATCH_GRASS_TAIGA_2 = register(
		"patch_grass_taiga_2", Feature.RANDOM_PATCH.configured(Features.Configs.TAIGA_GRASS_CONFIG).decorated(Features.Decorators.HEIGHTMAP_DOUBLE_SQUARE)
	);
	public static final ConfiguredFeature<?, ?> PATCH_GRASS_TAIGA = register(
		"patch_grass_taiga", Feature.RANDOM_PATCH.configured(Features.Configs.TAIGA_GRASS_CONFIG).decorated(Features.Decorators.HEIGHTMAP_DOUBLE_SQUARE).count(7)
	);
	public static final ConfiguredFeature<?, ?> PATCH_GRASS_JUNGLE = register(
		"patch_grass_jungle", Feature.RANDOM_PATCH.configured(Features.Configs.JUNGLE_GRASS_CONFIG).decorated(Features.Decorators.HEIGHTMAP_DOUBLE_SQUARE).count(25)
	);
	public static final ConfiguredFeature<?, ?> PATCH_DEAD_BUSH_2 = register(
		"patch_dead_bush_2", Feature.RANDOM_PATCH.configured(Features.Configs.DEAD_BUSH_CONFIG).decorated(Features.Decorators.HEIGHTMAP_DOUBLE_SQUARE).count(2)
	);
	public static final ConfiguredFeature<?, ?> PATCH_DEAD_BUSH = register(
		"patch_dead_bush", Feature.RANDOM_PATCH.configured(Features.Configs.DEAD_BUSH_CONFIG).decorated(Features.Decorators.HEIGHTMAP_DOUBLE_SQUARE)
	);
	public static final ConfiguredFeature<?, ?> PATCH_DEAD_BUSH_BADLANDS = register(
		"patch_dead_bush_badlands",
		Feature.RANDOM_PATCH.configured(Features.Configs.DEAD_BUSH_CONFIG).decorated(Features.Decorators.HEIGHTMAP_DOUBLE_SQUARE).count(20)
	);
	public static final ConfiguredFeature<?, ?> PATCH_MELON = register(
		"patch_melon",
		Feature.RANDOM_PATCH
			.configured(
				new RandomPatchConfiguration.GrassConfigurationBuilder(new SimpleStateProvider(Features.States.MELON), SimpleBlockPlacer.INSTANCE)
					.tries(64)
					.whitelist(ImmutableSet.of(Features.States.GRASS_BLOCK.getBlock()))
					.canReplace()
					.noProjection()
					.build()
			)
			.decorated(Features.Decorators.HEIGHTMAP_DOUBLE_SQUARE)
	);
	public static final ConfiguredFeature<?, ?> PATCH_BERRY_SPARSE = register(
		"patch_berry_sparse", PATCH_BERRY_BUSH.decorated(Features.Decorators.HEIGHTMAP_DOUBLE_SQUARE)
	);
	public static final ConfiguredFeature<?, ?> PATCH_BERRY_DECORATED = register(
		"patch_berry_decorated", PATCH_BERRY_BUSH.decorated(Features.Decorators.HEIGHTMAP_DOUBLE_SQUARE).rarity(12)
	);
	public static final ConfiguredFeature<?, ?> PATCH_WATERLILLY = register(
		"patch_waterlilly",
		Feature.RANDOM_PATCH
			.configured(
				new RandomPatchConfiguration.GrassConfigurationBuilder(new SimpleStateProvider(Features.States.LILY_PAD), SimpleBlockPlacer.INSTANCE).tries(10).build()
			)
			.decorated(Features.Decorators.HEIGHTMAP_DOUBLE_SQUARE)
			.count(4)
	);
	public static final ConfiguredFeature<?, ?> PATCH_TALL_GRASS_2 = register(
		"patch_tall_grass_2",
		Feature.RANDOM_PATCH
			.configured(Features.Configs.TALL_GRASS_CONFIG)
			.decorated(Features.Decorators.ADD_32)
			.decorated(Features.Decorators.HEIGHTMAP)
			.squared()
			.decorated(FeatureDecorator.COUNT_NOISE.configured(new NoiseDependantDecoratorConfiguration(-0.8, 0, 7)))
	);
	public static final ConfiguredFeature<?, ?> PATCH_TALL_GRASS = register(
		"patch_tall_grass",
		Feature.RANDOM_PATCH
			.configured(Features.Configs.TALL_GRASS_CONFIG)
			.decorated(Features.Decorators.ADD_32)
			.decorated(Features.Decorators.HEIGHTMAP_SQUARE)
			.count(7)
	);
	public static final ConfiguredFeature<?, ?> PATCH_LARGE_FERN = register(
		"patch_large_fern",
		Feature.RANDOM_PATCH
			.configured(
				new RandomPatchConfiguration.GrassConfigurationBuilder(new SimpleStateProvider(Features.States.LARGE_FERN), new DoublePlantPlacer())
					.tries(64)
					.noProjection()
					.build()
			)
			.decorated(Features.Decorators.ADD_32)
			.decorated(Features.Decorators.HEIGHTMAP_SQUARE)
			.count(7)
	);
	public static final ConfiguredFeature<?, ?> PATCH_CACTUS = register(
		"patch_cactus",
		Feature.RANDOM_PATCH
			.configured(
				new RandomPatchConfiguration.GrassConfigurationBuilder(new SimpleStateProvider(Features.States.CACTUS), new ColumnPlacer(BiasedToBottomInt.of(1, 3)))
					.tries(10)
					.noProjection()
					.build()
			)
	);
	public static final ConfiguredFeature<?, ?> PATCH_CACTUS_DESERT = register(
		"patch_cactus_desert", PATCH_CACTUS.decorated(Features.Decorators.HEIGHTMAP_DOUBLE_SQUARE).count(10)
	);
	public static final ConfiguredFeature<?, ?> PATCH_CACTUS_DECORATED = register(
		"patch_cactus_decorated", PATCH_CACTUS.decorated(Features.Decorators.HEIGHTMAP_DOUBLE_SQUARE).count(5)
	);
	public static final ConfiguredFeature<?, ?> PATCH_SUGAR_CANE_SWAMP = register(
		"patch_sugar_cane_swamp",
		Feature.RANDOM_PATCH.configured(Features.Configs.SUGAR_CANE_CONFIG).decorated(Features.Decorators.HEIGHTMAP_DOUBLE_SQUARE).count(20)
	);
	public static final ConfiguredFeature<?, ?> PATCH_SUGAR_CANE_DESERT = register(
		"patch_sugar_cane_desert",
		Feature.RANDOM_PATCH.configured(Features.Configs.SUGAR_CANE_CONFIG).decorated(Features.Decorators.HEIGHTMAP_DOUBLE_SQUARE).count(60)
	);
	public static final ConfiguredFeature<?, ?> PATCH_SUGAR_CANE_BADLANDS = register(
		"patch_sugar_cane_badlands",
		Feature.RANDOM_PATCH.configured(Features.Configs.SUGAR_CANE_CONFIG).decorated(Features.Decorators.HEIGHTMAP_DOUBLE_SQUARE).count(13)
	);
	public static final ConfiguredFeature<?, ?> PATCH_SUGAR_CANE = register(
		"patch_sugar_cane", Feature.RANDOM_PATCH.configured(Features.Configs.SUGAR_CANE_CONFIG).decorated(Features.Decorators.HEIGHTMAP_DOUBLE_SQUARE).count(10)
	);
	public static final ConfiguredFeature<?, ?> BROWN_MUSHROOM_NETHER = register(
		"brown_mushroom_nether", PATCH_BROWN_MUSHROOM.range(Features.Decorators.FULL_RANGE).rarity(2)
	);
	public static final ConfiguredFeature<?, ?> RED_MUSHROOM_NETHER = register(
		"red_mushroom_nether", PATCH_RED_MUSHROOM.range(Features.Decorators.FULL_RANGE).rarity(2)
	);
	public static final ConfiguredFeature<?, ?> BROWN_MUSHROOM_NORMAL = register(
		"brown_mushroom_normal", PATCH_BROWN_MUSHROOM.decorated(Features.Decorators.HEIGHTMAP_DOUBLE_SQUARE).rarity(4)
	);
	public static final ConfiguredFeature<?, ?> RED_MUSHROOM_NORMAL = register(
		"red_mushroom_normal", PATCH_RED_MUSHROOM.decorated(Features.Decorators.HEIGHTMAP_DOUBLE_SQUARE).rarity(8)
	);
	public static final ConfiguredFeature<?, ?> BROWN_MUSHROOM_TAIGA = register(
		"brown_mushroom_taiga", PATCH_BROWN_MUSHROOM.rarity(4).decorated(Features.Decorators.HEIGHTMAP_SQUARE)
	);
	public static final ConfiguredFeature<?, ?> RED_MUSHROOM_TAIGA = register(
		"red_mushroom_taiga", PATCH_RED_MUSHROOM.rarity(8).decorated(Features.Decorators.HEIGHTMAP_DOUBLE_SQUARE)
	);
	public static final ConfiguredFeature<?, ?> BROWN_MUSHROOM_GIANT = register("brown_mushroom_giant", BROWN_MUSHROOM_TAIGA.count(3));
	public static final ConfiguredFeature<?, ?> RED_MUSHROOM_GIANT = register("red_mushroom_giant", RED_MUSHROOM_TAIGA.count(3));
	public static final ConfiguredFeature<?, ?> BROWN_MUSHROOM_SWAMP = register("brown_mushroom_swamp", BROWN_MUSHROOM_TAIGA.count(8));
	public static final ConfiguredFeature<?, ?> RED_MUSHROOM_SWAMP = register("red_mushroom_swamp", RED_MUSHROOM_TAIGA.count(8));
	public static final ImmutableList<OreConfiguration.TargetBlockState> ORE_IRON_TARGET_LIST = ImmutableList.of(
		OreConfiguration.target(OreConfiguration.Predicates.STONE_ORE_REPLACEABLES, Features.States.IRON_ORE),
		OreConfiguration.target(OreConfiguration.Predicates.DEEPSLATE_ORE_REPLACEABLES, Features.States.DEEPSLATE_IRON_ORE)
	);
	public static final ImmutableList<OreConfiguration.TargetBlockState> ORE_REDSTONE_TARGET_LIST = ImmutableList.of(
		OreConfiguration.target(OreConfiguration.Predicates.STONE_ORE_REPLACEABLES, Features.States.REDSTONE_ORE),
		OreConfiguration.target(OreConfiguration.Predicates.DEEPSLATE_ORE_REPLACEABLES, Features.States.DEEPSLATE_REDSTONE_ORE)
	);
	public static final ImmutableList<OreConfiguration.TargetBlockState> ORE_GOLD_TARGET_LIST = ImmutableList.of(
		OreConfiguration.target(OreConfiguration.Predicates.STONE_ORE_REPLACEABLES, Features.States.GOLD_ORE),
		OreConfiguration.target(OreConfiguration.Predicates.DEEPSLATE_ORE_REPLACEABLES, Features.States.DEEPSLATE_GOLD_ORE)
	);
	public static final ImmutableList<OreConfiguration.TargetBlockState> ORE_DIAMOND_TARGET_LIST = ImmutableList.of(
		OreConfiguration.target(OreConfiguration.Predicates.STONE_ORE_REPLACEABLES, Features.States.DIAMOND_ORE),
		OreConfiguration.target(OreConfiguration.Predicates.DEEPSLATE_ORE_REPLACEABLES, Features.States.DEEPSLATE_DIAMOND_ORE)
	);
	public static final ImmutableList<OreConfiguration.TargetBlockState> ORE_LAPIS_TARGET_LIST = ImmutableList.of(
		OreConfiguration.target(OreConfiguration.Predicates.STONE_ORE_REPLACEABLES, Features.States.LAPIS_ORE),
		OreConfiguration.target(OreConfiguration.Predicates.DEEPSLATE_ORE_REPLACEABLES, Features.States.DEEPSLATE_LAPIS_ORE)
	);
	public static final ImmutableList<OreConfiguration.TargetBlockState> ORE_EMERALD_TARGET_LIST = ImmutableList.of(
		OreConfiguration.target(OreConfiguration.Predicates.STONE_ORE_REPLACEABLES, Features.States.EMERALD_ORE),
		OreConfiguration.target(OreConfiguration.Predicates.DEEPSLATE_ORE_REPLACEABLES, Features.States.DEEPSLATE_EMERALD_ORE)
	);
	public static final ImmutableList<OreConfiguration.TargetBlockState> ORE_COPPER_TARGET_LIST = ImmutableList.of(
		OreConfiguration.target(OreConfiguration.Predicates.STONE_ORE_REPLACEABLES, Features.States.COPPER_ORE),
		OreConfiguration.target(OreConfiguration.Predicates.DEEPSLATE_ORE_REPLACEABLES, Features.States.DEEPSLATE_COPPER_ORE)
	);
	public static final ImmutableList<OreConfiguration.TargetBlockState> ORE_COAL_TARGET_LIST = ImmutableList.of(
		OreConfiguration.target(OreConfiguration.Predicates.STONE_ORE_REPLACEABLES, Features.States.COAL_ORE),
		OreConfiguration.target(OreConfiguration.Predicates.DEEPSLATE_ORE_REPLACEABLES, Features.States.DEEPSLATE_COAL_ORE)
	);
	public static final ImmutableList<OreConfiguration.TargetBlockState> ORE_INFESTED_TARGET_LIST = ImmutableList.of(
		OreConfiguration.target(OreConfiguration.Predicates.STONE_ORE_REPLACEABLES, Features.States.INFESTED_STONE),
		OreConfiguration.target(OreConfiguration.Predicates.DEEPSLATE_ORE_REPLACEABLES, Features.States.INFESTED_DEEPSLATE)
	);
	public static final OreConfiguration ORE_IRON_CONFIG = new OreConfiguration(ORE_IRON_TARGET_LIST, 9);
	public static final OreConfiguration ORE_REDSTONE_CONFIG = new OreConfiguration(ORE_REDSTONE_TARGET_LIST, 8);
	public static final ConfiguredFeature<?, ?> ORE_MAGMA = register(
		"ore_magma",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NETHERRACK, Features.States.MAGMA_BLOCK, 33))
			.rangeUniform(VerticalAnchor.absolute(27), VerticalAnchor.absolute(36))
			.squared()
			.count(4)
	);
	public static final ConfiguredFeature<?, ?> ORE_SOUL_SAND = register(
		"ore_soul_sand",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NETHERRACK, Features.States.SOUL_SAND, 12))
			.rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(31))
			.squared()
			.count(12)
	);
	public static final ConfiguredFeature<?, ?> ORE_GOLD_DELTAS = register(
		"ore_gold_deltas",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NETHERRACK, Features.States.NETHER_GOLD_ORE, 10))
			.range(Features.Decorators.RANGE_10_10)
			.squared()
			.count(20)
	);
	public static final ConfiguredFeature<?, ?> ORE_QUARTZ_DELTAS = register(
		"ore_quartz_deltas",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NETHERRACK, Features.States.NETHER_QUARTZ_ORE, 14))
			.range(Features.Decorators.RANGE_10_10)
			.squared()
			.count(32)
	);
	public static final ConfiguredFeature<?, ?> ORE_GOLD_NETHER = register(
		"ore_gold_nether",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NETHERRACK, Features.States.NETHER_GOLD_ORE, 10))
			.range(Features.Decorators.RANGE_10_10)
			.squared()
			.count(10)
	);
	public static final ConfiguredFeature<?, ?> ORE_QUARTZ_NETHER = register(
		"ore_quartz_nether",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NETHERRACK, Features.States.NETHER_QUARTZ_ORE, 14))
			.range(Features.Decorators.RANGE_10_10)
			.squared()
			.count(16)
	);
	public static final ConfiguredFeature<?, ?> ORE_GRAVEL_NETHER = register(
		"ore_gravel_nether",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NETHERRACK, Features.States.GRAVEL, 33))
			.rangeUniform(VerticalAnchor.absolute(5), VerticalAnchor.absolute(41))
			.squared()
			.count(2)
	);
	public static final ConfiguredFeature<?, ?> ORE_BLACKSTONE = register(
		"ore_blackstone",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NETHERRACK, Features.States.BLACKSTONE, 33))
			.rangeUniform(VerticalAnchor.absolute(5), VerticalAnchor.absolute(31))
			.squared()
			.count(2)
	);
	public static final ConfiguredFeature<?, ?> ORE_DIRT = register(
		"ore_dirt",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Features.States.DIRT, 33))
			.rangeUniform(VerticalAnchor.absolute(0), VerticalAnchor.top())
			.squared()
			.count(10)
	);
	public static final ConfiguredFeature<?, ?> PROTOTYPE_ORE_DIRT = register(
		"prototype_ore_dirt",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Features.States.DIRT, 33))
			.rangeUniform(VerticalAnchor.absolute(0), VerticalAnchor.top())
			.squared()
			.count(15)
	);
	public static final ConfiguredFeature<?, ?> ORE_GRAVEL = register(
		"ore_gravel",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Features.States.GRAVEL, 33))
			.rangeUniform(VerticalAnchor.absolute(0), VerticalAnchor.top())
			.squared()
			.count(8)
	);
	public static final ConfiguredFeature<?, ?> PROTOTYPE_ORE_GRAVEL = register(
		"prototype_ore_gravel",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Features.States.GRAVEL, 33))
			.rangeUniform(VerticalAnchor.absolute(0), VerticalAnchor.top())
			.squared()
			.count(12)
	);
	public static final ConfiguredFeature<?, ?> ORE_GRANITE = register(
		"ore_granite",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Features.States.GRANITE, 33))
			.rangeUniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(79))
			.squared()
			.count(10)
	);
	public static final ConfiguredFeature<?, ?> PROTOTYPE_ORE_GRANITE = register(
		"prototype_ore_granite",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Features.States.GRANITE, 64))
			.rangeUniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(79))
			.squared()
			.count(2)
	);
	public static final ConfiguredFeature<?, ?> ORE_DIORITE = register(
		"ore_diorite",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Features.States.DIORITE, 33))
			.rangeUniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(79))
			.squared()
			.count(10)
	);
	public static final ConfiguredFeature<?, ?> PROTOTYPE_ORE_DIORITE = register(
		"prototype_ore_diorite",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Features.States.DIORITE, 64))
			.rangeUniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(79))
			.squared()
			.count(2)
	);
	public static final ConfiguredFeature<?, ?> ORE_ANDESITE = register(
		"ore_andesite",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Features.States.ANDESITE, 33))
			.rangeUniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(79))
			.squared()
			.count(10)
	);
	public static final ConfiguredFeature<?, ?> PROTOTYPE_ORE_ANDESITE = register(
		"prototype_ore_andesite",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Features.States.ANDESITE, 64))
			.rangeUniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(79))
			.squared()
			.count(2)
	);
	public static final ConfiguredFeature<?, ?> ORE_TUFF = register(
		"ore_tuff",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Features.States.TUFF, 33))
			.rangeUniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(16))
			.squared()
			.count(1)
	);
	public static final ConfiguredFeature<?, ?> PROTOTYPE_ORE_TUFF = register(
		"prototype_ore_tuff",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Features.States.TUFF, 64))
			.rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(0))
			.squared()
			.count(2)
	);
	public static final ConfiguredFeature<?, ?> ORE_DEEPSLATE = register(
		"ore_deepslate",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Features.States.DEEPSLATE, 64))
			.rangeUniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(16))
			.squared()
			.count(2)
	);
	public static final ConfiguredFeature<?, ?> ORE_COAL = register(
		"ore_coal",
		Feature.ORE
			.configured(new OreConfiguration(ORE_COAL_TARGET_LIST, 17))
			.rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(127))
			.squared()
			.count(20)
	);
	public static final ConfiguredFeature<?, ?> PROTOTYPE_ORE_COAL_UPPER = register(
		"prototype_ore_coal_upper",
		Feature.ORE.configured(new OreConfiguration(ORE_COAL_TARGET_LIST, 17)).rangeUniform(VerticalAnchor.absolute(136), VerticalAnchor.top()).squared().count(30)
	);
	public static final ConfiguredFeature<?, ?> PROTOTYPE_ORE_COAL_LOWER = register(
		"prototype_ore_coal_lower",
		Feature.ORE
			.configured(new OreConfiguration(ORE_COAL_TARGET_LIST, 17, 0.5F))
			.rangeTriangle(VerticalAnchor.absolute(0), VerticalAnchor.absolute(192))
			.squared()
			.count(20)
	);
	public static final ConfiguredFeature<?, ?> ORE_IRON = register(
		"ore_iron",
		Feature.ORE.configured(new OreConfiguration(ORE_IRON_TARGET_LIST, 9)).rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(63)).squared().count(20)
	);
	public static final ConfiguredFeature<?, ?> PROTOTYPE_ORE_IRON_UPPER = register(
		"prototype_ore_iron_upper",
		Feature.ORE.configured(ORE_IRON_CONFIG).rangeTriangle(VerticalAnchor.absolute(128), VerticalAnchor.absolute(384)).squared().count(40)
	);
	public static final ConfiguredFeature<?, ?> PROTOTYPE_ORE_IRON_MIDDLE = register(
		"prototype_ore_iron_middle",
		Feature.ORE.configured(ORE_IRON_CONFIG).rangeTriangle(VerticalAnchor.absolute(-24), VerticalAnchor.absolute(56)).squared().count(5)
	);
	public static final ConfiguredFeature<?, ?> PROTOTYPE_ORE_IRON_SMALL = register(
		"prototype_ore_iron_small",
		Feature.ORE.configured(new OreConfiguration(ORE_IRON_TARGET_LIST, 4)).rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(64)).squared().count(10)
	);
	public static final ConfiguredFeature<?, ?> ORE_GOLD_EXTRA = register(
		"ore_gold_extra",
		Feature.ORE
			.configured(new OreConfiguration(ORE_GOLD_TARGET_LIST, 9))
			.rangeUniform(VerticalAnchor.absolute(32), VerticalAnchor.absolute(79))
			.squared()
			.count(20)
	);
	public static final ConfiguredFeature<?, ?> ORE_GOLD = register(
		"ore_gold",
		Feature.ORE.configured(new OreConfiguration(ORE_GOLD_TARGET_LIST, 9)).rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(31)).squared().count(2)
	);
	public static final ConfiguredFeature<?, ?> PROTOTYPE_ORE_GOLD = register(
		"prototype_ore_gold",
		Feature.ORE
			.configured(new OreConfiguration(ORE_GOLD_TARGET_LIST, 9, 0.5F))
			.rangeTriangle(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(32))
			.squared()
			.count(4)
	);
	public static final ConfiguredFeature<?, ?> ORE_REDSTONE = register(
		"ore_redstone", Feature.ORE.configured(ORE_REDSTONE_CONFIG).rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(15)).squared().count(8)
	);
	public static final ConfiguredFeature<?, ?> PROTOTYPE_ORE_REDSTONE = register(
		"prototype_ore_redstone", Feature.ORE.configured(ORE_REDSTONE_CONFIG).rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(15)).squared().count(4)
	);
	public static final ConfiguredFeature<?, ?> PROTOTYPE_ORE_REDSTONE_LOWER = register(
		"prototype_ore_redstone_lower",
		Feature.ORE.configured(ORE_REDSTONE_CONFIG).rangeTriangle(VerticalAnchor.aboveBottom(-32), VerticalAnchor.aboveBottom(32)).squared().count(8)
	);
	public static final ConfiguredFeature<?, ?> ORE_DIAMOND = register(
		"ore_diamond",
		Feature.ORE.configured(new OreConfiguration(ORE_DIAMOND_TARGET_LIST, 8)).rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(15)).squared()
	);
	public static final ConfiguredFeature<?, ?> PROTOTYPE_ORE_DIAMOND = register(
		"prototype_ore_diamond",
		Feature.ORE
			.configured(new OreConfiguration(ORE_DIAMOND_TARGET_LIST, 4, 0.5F))
			.rangeTriangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80))
			.squared()
			.count(6)
	);
	public static final ConfiguredFeature<?, ?> PROTOTYPE_ORE_DIAMOND_LARGE = register(
		"prototype_ore_diamond_large",
		Feature.ORE
			.configured(new OreConfiguration(ORE_DIAMOND_TARGET_LIST, 12, 0.7F))
			.rangeTriangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80))
			.squared()
			.rarity(9)
	);
	public static final ConfiguredFeature<?, ?> ORE_LAPIS = register(
		"ore_lapis",
		Feature.ORE.configured(new OreConfiguration(ORE_LAPIS_TARGET_LIST, 7)).rangeTriangle(VerticalAnchor.absolute(0), VerticalAnchor.absolute(30)).squared()
	);
	public static final ConfiguredFeature<?, ?> PROTOTYPE_ORE_LAPIS = register(
		"prototype_ore_lapis",
		Feature.ORE
			.configured(new OreConfiguration(ORE_LAPIS_TARGET_LIST, 7))
			.rangeTriangle(VerticalAnchor.absolute(-32), VerticalAnchor.absolute(32))
			.squared()
			.count(2)
	);
	public static final ConfiguredFeature<?, ?> PROTOTYPE_ORE_LAPIS_BURIED = register(
		"prototype_ore_lapis_buried",
		Feature.SCATTERED_ORE
			.configured(new OreConfiguration(ORE_LAPIS_TARGET_LIST, 7, 1.0F))
			.rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(64))
			.squared()
			.count(4)
	);
	public static final ConfiguredFeature<?, ?> ORE_INFESTED = register(
		"ore_infested",
		Feature.ORE
			.configured(new OreConfiguration(ORE_INFESTED_TARGET_LIST, 9))
			.rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(63))
			.squared()
			.count(7)
	);
	public static final ConfiguredFeature<?, ?> PROTOTYPE_ORE_INFESTED = register(
		"prototype_ore_infested",
		Feature.ORE
			.configured(new OreConfiguration(ORE_INFESTED_TARGET_LIST, 9))
			.rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(63))
			.squared()
			.count(14)
	);
	public static final ConfiguredFeature<?, ?> ORE_EMERALD = register(
		"ore_emerald",
		Feature.REPLACE_SINGLE_BLOCK
			.configured(new ReplaceBlockConfiguration(ORE_EMERALD_TARGET_LIST))
			.rangeUniform(VerticalAnchor.absolute(4), VerticalAnchor.absolute(31))
			.squared()
			.count(UniformInt.of(3, 8))
	);
	public static final ConfiguredFeature<?, ?> PROTOTYPE_ORE_EMERALD = register(
		"prototype_ore_emerald",
		Feature.ORE
			.configured(new OreConfiguration(ORE_EMERALD_TARGET_LIST, 3))
			.rangeTriangle(VerticalAnchor.absolute(32), VerticalAnchor.absolute(480))
			.squared()
			.count(50)
	);
	public static final ConfiguredFeature<?, ?> ORE_DEBRIS_LARGE = register(
		"ore_debris_large",
		Feature.SCATTERED_ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NETHER_ORE_REPLACEABLES, Features.States.ANCIENT_DEBRIS, 3, 1.0F))
			.rangeTriangle(VerticalAnchor.absolute(8), VerticalAnchor.absolute(24))
			.squared()
	);
	public static final ConfiguredFeature<?, ?> ORE_DEBRIS_SMALL = register(
		"ore_debris_small",
		Feature.SCATTERED_ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NETHER_ORE_REPLACEABLES, Features.States.ANCIENT_DEBRIS, 2, 1.0F))
			.range(Features.Decorators.RANGE_8_8)
			.squared()
	);
	public static final ConfiguredFeature<?, ?> ORE_COPPER = register(
		"ore_copper",
		Feature.ORE
			.configured(new OreConfiguration(ORE_COPPER_TARGET_LIST, 10))
			.rangeTriangle(VerticalAnchor.absolute(0), VerticalAnchor.absolute(96))
			.squared()
			.count(6)
	);
	public static final ConfiguredFeature<?, ?> PROTOTYPE_ORE_COPPER = register(
		"prototype_ore_copper",
		Feature.ORE
			.configured(new OreConfiguration(ORE_COPPER_TARGET_LIST, 10))
			.rangeUniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(63))
			.squared()
			.count(8)
	);
	public static final ConfiguredFeature<?, ?> ORE_CLAY = register(
		"ore_clay",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Features.States.CLAY, 33))
			.range(Features.Decorators.RANGE_BOTTOM_TO_60)
			.squared()
			.count(15)
	);
	public static final ConfiguredFeature<?, ?> DRIPSTONE_CLUSTER_FEATURE = register(
		"dripstone_cluster",
		Feature.DRIPSTONE_CLUSTER
			.configured(
				new DripstoneClusterConfiguration(
					12,
					UniformInt.of(3, 6),
					UniformInt.of(2, 8),
					1,
					3,
					UniformInt.of(2, 4),
					UniformFloat.of(0.3F, 0.7F),
					ClampedNormalFloat.of(0.1F, 0.3F, 0.1F, 0.9F),
					0.1F,
					3,
					8
				)
			)
			.rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(59))
			.squared()
			.count(UniformInt.of(10, 20))
	);
	public static final ConfiguredFeature<?, ?> LARGE_DRIPSTONE_FEATURE = register(
		"large_dripstone",
		Feature.LARGE_DRIPSTONE
			.configured(
				new LargeDripstoneConfiguration(
					30,
					UniformInt.of(3, 19),
					UniformFloat.of(0.4F, 2.0F),
					0.33F,
					UniformFloat.of(0.3F, 0.9F),
					UniformFloat.of(0.4F, 1.0F),
					UniformFloat.of(0.0F, 0.3F),
					4,
					0.6F
				)
			)
			.rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(59))
			.squared()
			.count(UniformInt.of(2, 10))
	);
	public static final ConfiguredFeature<?, ?> SMALL_DRIPSTONE_FEATURE = register(
		"small_dripstone",
		Feature.SMALL_DRIPSTONE
			.configured(new SmallDripstoneConfiguration(5, 10, 2, 0.2F))
			.rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(59))
			.squared()
			.count(UniformInt.of(40, 120))
	);
	public static final ConfiguredFeature<?, ?> RARE_DRIPSTONE_CLUSTER_FEATURE = register(
		"rare_dripstone_cluster",
		Feature.DRIPSTONE_CLUSTER
			.configured(
				new DripstoneClusterConfiguration(
					12,
					UniformInt.of(3, 3),
					UniformInt.of(2, 6),
					1,
					3,
					UniformInt.of(2, 2),
					UniformFloat.of(0.3F, 0.4F),
					ClampedNormalFloat.of(0.1F, 0.3F, 0.1F, 0.9F),
					0.1F,
					3,
					8
				)
			)
			.rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(59))
			.squared()
			.count(UniformInt.of(10, 10))
			.rarity(25)
	);
	public static final ConfiguredFeature<?, ?> RARE_SMALL_DRIPSTONE_FEATURE = register(
		"rare_small_dripstone",
		Feature.SMALL_DRIPSTONE
			.configured(new SmallDripstoneConfiguration(5, 10, 2, 0.2F))
			.rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(59))
			.squared()
			.count(UniformInt.of(40, 80))
			.rarity(30)
	);
	public static final ConfiguredFeature<?, ?> PROTOTYPE_UNDERWATER_MAGMA = register(
		"prototype_underwater_magma",
		Feature.UNDERWATER_MAGMA
			.configured(new UnderwaterMagmaConfiguration(5, 1, 0.5F))
			.squared()
			.rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(39))
			.count(UniformInt.of(4, 10))
	);
	public static final ConfiguredFeature<?, ?> GLOW_LICHEN = register(
		"glow_lichen",
		Feature.GLOW_LICHEN
			.configured(
				new GlowLichenConfiguration(
					20,
					false,
					true,
					true,
					0.5F,
					ImmutableList.of(
						Blocks.STONE.defaultBlockState(),
						Blocks.ANDESITE.defaultBlockState(),
						Blocks.DIORITE.defaultBlockState(),
						Blocks.GRANITE.defaultBlockState(),
						Blocks.DRIPSTONE_BLOCK.defaultBlockState(),
						Blocks.CALCITE.defaultBlockState(),
						Blocks.TUFF.defaultBlockState(),
						Blocks.DEEPSLATE.defaultBlockState()
					)
				)
			)
			.squared()
			.rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(54))
			.count(UniformInt.of(20, 30))
	);
	public static final ConfiguredFeature<?, ?> PROTOTYPE_GLOW_LICHEN = register(
		"prototype_glow_lichen",
		Feature.GLOW_LICHEN
			.configured(
				new GlowLichenConfiguration(
					20,
					false,
					true,
					true,
					0.5F,
					ImmutableList.of(
						Blocks.STONE.defaultBlockState(),
						Blocks.ANDESITE.defaultBlockState(),
						Blocks.DIORITE.defaultBlockState(),
						Blocks.GRANITE.defaultBlockState(),
						Blocks.DRIPSTONE_BLOCK.defaultBlockState(),
						Blocks.CALCITE.defaultBlockState(),
						Blocks.TUFF.defaultBlockState(),
						Blocks.DEEPSLATE.defaultBlockState()
					)
				)
			)
			.squared()
			.rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(54))
			.count(UniformInt.of(40, 60))
	);
	public static final ConfiguredFeature<?, ?> CRIMSON_FUNGI = register(
		"crimson_fungi",
		Feature.HUGE_FUNGUS
			.configured(HugeFungusConfiguration.HUGE_CRIMSON_FUNGI_NOT_PLANTED_CONFIG)
			.decorated(FeatureDecorator.COUNT_MULTILAYER.configured(new CountConfiguration(8)))
	);
	public static final ConfiguredFeature<HugeFungusConfiguration, ?> CRIMSON_FUNGI_PLANTED = register(
		"crimson_fungi_planted", Feature.HUGE_FUNGUS.configured(HugeFungusConfiguration.HUGE_CRIMSON_FUNGI_PLANTED_CONFIG)
	);
	public static final ConfiguredFeature<?, ?> WARPED_FUNGI = register(
		"warped_fungi",
		Feature.HUGE_FUNGUS
			.configured(HugeFungusConfiguration.HUGE_WARPED_FUNGI_NOT_PLANTED_CONFIG)
			.decorated(FeatureDecorator.COUNT_MULTILAYER.configured(new CountConfiguration(8)))
	);
	public static final ConfiguredFeature<HugeFungusConfiguration, ?> WARPED_FUNGI_PLANTED = register(
		"warped_fungi_planted", Feature.HUGE_FUNGUS.configured(HugeFungusConfiguration.HUGE_WARPED_FUNGI_PLANTED_CONFIG)
	);
	public static final ConfiguredFeature<?, ?> HUGE_BROWN_MUSHROOM = register(
		"huge_brown_mushroom",
		Feature.HUGE_BROWN_MUSHROOM
			.configured(
				new HugeMushroomFeatureConfiguration(
					new SimpleStateProvider(Features.States.HUGE_BROWN_MUSHROOM), new SimpleStateProvider(Features.States.HUGE_MUSHROOM_STEM), 3
				)
			)
	);
	public static final ConfiguredFeature<?, ?> HUGE_RED_MUSHROOM = register(
		"huge_red_mushroom",
		Feature.HUGE_RED_MUSHROOM
			.configured(
				new HugeMushroomFeatureConfiguration(
					new SimpleStateProvider(Features.States.HUGE_RED_MUSHROOM), new SimpleStateProvider(Features.States.HUGE_MUSHROOM_STEM), 2
				)
			)
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> OAK = register(
		"oak",
		Feature.TREE
			.configured(
				new TreeConfiguration.TreeConfigurationBuilder(
						new SimpleStateProvider(Features.States.OAK_LOG),
						new StraightTrunkPlacer(4, 2, 0),
						new SimpleStateProvider(Features.States.OAK_LEAVES),
						new SimpleStateProvider(Features.States.OAK_SAPLING),
						new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3),
						new TwoLayersFeatureSize(1, 0, 1)
					)
					.ignoreVines()
					.build()
			)
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> DARK_OAK = register(
		"dark_oak",
		Feature.TREE
			.configured(
				new TreeConfiguration.TreeConfigurationBuilder(
						new SimpleStateProvider(Features.States.DARK_OAK_LOG),
						new DarkOakTrunkPlacer(6, 2, 1),
						new SimpleStateProvider(Features.States.DARK_OAK_LEAVES),
						new SimpleStateProvider(Features.States.DARK_OAK_SAPLING),
						new DarkOakFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0)),
						new ThreeLayersFeatureSize(1, 1, 0, 1, 2, OptionalInt.empty())
					)
					.ignoreVines()
					.build()
			)
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> BIRCH = register(
		"birch",
		Feature.TREE
			.configured(
				new TreeConfiguration.TreeConfigurationBuilder(
						new SimpleStateProvider(Features.States.BIRCH_LOG),
						new StraightTrunkPlacer(5, 2, 0),
						new SimpleStateProvider(Features.States.BIRCH_LEAVES),
						new SimpleStateProvider(Features.States.BIRCH_SAPLING),
						new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3),
						new TwoLayersFeatureSize(1, 0, 1)
					)
					.ignoreVines()
					.build()
			)
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> ACACIA = register(
		"acacia",
		Feature.TREE
			.configured(
				new TreeConfiguration.TreeConfigurationBuilder(
						new SimpleStateProvider(Features.States.ACACIA_LOG),
						new ForkingTrunkPlacer(5, 2, 2),
						new SimpleStateProvider(Features.States.ACACIA_LEAVES),
						new SimpleStateProvider(Features.States.ACACIA_SAPLING),
						new AcaciaFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0)),
						new TwoLayersFeatureSize(1, 0, 2)
					)
					.ignoreVines()
					.build()
			)
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> SPRUCE = register(
		"spruce",
		Feature.TREE
			.configured(
				new TreeConfiguration.TreeConfigurationBuilder(
						new SimpleStateProvider(Features.States.SPRUCE_LOG),
						new StraightTrunkPlacer(5, 2, 1),
						new SimpleStateProvider(Features.States.SPRUCE_LEAVES),
						new SimpleStateProvider(Features.States.SPRUCE_SAPLING),
						new SpruceFoliagePlacer(UniformInt.of(2, 3), UniformInt.of(0, 2), UniformInt.of(1, 2)),
						new TwoLayersFeatureSize(2, 0, 2)
					)
					.ignoreVines()
					.build()
			)
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> PINE = register(
		"pine",
		Feature.TREE
			.configured(
				new TreeConfiguration.TreeConfigurationBuilder(
						new SimpleStateProvider(Features.States.SPRUCE_LOG),
						new StraightTrunkPlacer(6, 4, 0),
						new SimpleStateProvider(Features.States.SPRUCE_LEAVES),
						new SimpleStateProvider(Features.States.SPRUCE_SAPLING),
						new PineFoliagePlacer(ConstantInt.of(1), ConstantInt.of(1), UniformInt.of(3, 4)),
						new TwoLayersFeatureSize(2, 0, 2)
					)
					.ignoreVines()
					.build()
			)
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> JUNGLE_TREE = register(
		"jungle_tree",
		Feature.TREE
			.configured(
				new TreeConfiguration.TreeConfigurationBuilder(
						new SimpleStateProvider(Features.States.JUNGLE_LOG),
						new StraightTrunkPlacer(4, 8, 0),
						new SimpleStateProvider(Features.States.JUNGLE_LEAVES),
						new SimpleStateProvider(Features.States.JUNGLE_SAPLING),
						new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3),
						new TwoLayersFeatureSize(1, 0, 1)
					)
					.decorators(ImmutableList.of(new CocoaDecorator(0.2F), TrunkVineDecorator.INSTANCE, LeaveVineDecorator.INSTANCE))
					.ignoreVines()
					.build()
			)
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> FANCY_OAK = register(
		"fancy_oak",
		Feature.TREE
			.configured(
				new TreeConfiguration.TreeConfigurationBuilder(
						new SimpleStateProvider(Features.States.OAK_LOG),
						new FancyTrunkPlacer(3, 11, 0),
						new SimpleStateProvider(Features.States.OAK_LEAVES),
						new SimpleStateProvider(Features.States.OAK_SAPLING),
						new FancyFoliagePlacer(ConstantInt.of(2), ConstantInt.of(4), 4),
						new TwoLayersFeatureSize(0, 0, 0, OptionalInt.of(4))
					)
					.ignoreVines()
					.build()
			)
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> JUNGLE_TREE_NO_VINE = register(
		"jungle_tree_no_vine",
		Feature.TREE
			.configured(
				new TreeConfiguration.TreeConfigurationBuilder(
						new SimpleStateProvider(Features.States.JUNGLE_LOG),
						new StraightTrunkPlacer(4, 8, 0),
						new SimpleStateProvider(Features.States.JUNGLE_LEAVES),
						new SimpleStateProvider(Features.States.JUNGLE_SAPLING),
						new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3),
						new TwoLayersFeatureSize(1, 0, 1)
					)
					.ignoreVines()
					.build()
			)
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> MEGA_JUNGLE_TREE = register(
		"mega_jungle_tree",
		Feature.TREE
			.configured(
				new TreeConfiguration.TreeConfigurationBuilder(
						new SimpleStateProvider(Features.States.JUNGLE_LOG),
						new MegaJungleTrunkPlacer(10, 2, 19),
						new SimpleStateProvider(Features.States.JUNGLE_LEAVES),
						new SimpleStateProvider(Features.States.JUNGLE_SAPLING),
						new MegaJungleFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 2),
						new TwoLayersFeatureSize(1, 1, 2)
					)
					.decorators(ImmutableList.of(TrunkVineDecorator.INSTANCE, LeaveVineDecorator.INSTANCE))
					.build()
			)
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> MEGA_SPRUCE = register(
		"mega_spruce",
		Feature.TREE
			.configured(
				new TreeConfiguration.TreeConfigurationBuilder(
						new SimpleStateProvider(Features.States.SPRUCE_LOG),
						new GiantTrunkPlacer(13, 2, 14),
						new SimpleStateProvider(Features.States.SPRUCE_LEAVES),
						new SimpleStateProvider(Features.States.SPRUCE_SAPLING),
						new MegaPineFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0), UniformInt.of(13, 17)),
						new TwoLayersFeatureSize(1, 1, 2)
					)
					.decorators(ImmutableList.of(new AlterGroundDecorator(new SimpleStateProvider(Features.States.PODZOL))))
					.build()
			)
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> MEGA_PINE = register(
		"mega_pine",
		Feature.TREE
			.configured(
				new TreeConfiguration.TreeConfigurationBuilder(
						new SimpleStateProvider(Features.States.SPRUCE_LOG),
						new GiantTrunkPlacer(13, 2, 14),
						new SimpleStateProvider(Features.States.SPRUCE_LEAVES),
						new SimpleStateProvider(Features.States.SPRUCE_SAPLING),
						new MegaPineFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0), UniformInt.of(3, 7)),
						new TwoLayersFeatureSize(1, 1, 2)
					)
					.decorators(ImmutableList.of(new AlterGroundDecorator(new SimpleStateProvider(Features.States.PODZOL))))
					.build()
			)
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> SUPER_BIRCH_BEES_0002 = register(
		"super_birch_bees_0002",
		Feature.TREE
			.configured(
				new TreeConfiguration.TreeConfigurationBuilder(
						new SimpleStateProvider(Features.States.BIRCH_LOG),
						new StraightTrunkPlacer(5, 2, 6),
						new SimpleStateProvider(Features.States.BIRCH_LEAVES),
						new SimpleStateProvider(Features.States.BIRCH_SAPLING),
						new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 3),
						new TwoLayersFeatureSize(1, 0, 1)
					)
					.ignoreVines()
					.decorators(ImmutableList.of(Features.Decorators.BEEHIVE_0002))
					.build()
			)
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> SWAMP_OAK = register(
		"swamp_oak",
		Feature.TREE
			.configured(
				new TreeConfiguration.TreeConfigurationBuilder(
						new SimpleStateProvider(Features.States.OAK_LOG),
						new StraightTrunkPlacer(5, 3, 0),
						new SimpleStateProvider(Features.States.OAK_LEAVES),
						new SimpleStateProvider(Features.States.OAK_SAPLING),
						new BlobFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0), 3),
						new TwoLayersFeatureSize(1, 0, 1)
					)
					.decorators(ImmutableList.of(LeaveVineDecorator.INSTANCE))
					.build()
			)
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> JUNGLE_BUSH = register(
		"jungle_bush",
		Feature.TREE
			.configured(
				new TreeConfiguration.TreeConfigurationBuilder(
						new SimpleStateProvider(Features.States.JUNGLE_LOG),
						new StraightTrunkPlacer(1, 0, 0),
						new SimpleStateProvider(Features.States.OAK_LEAVES),
						new SimpleStateProvider(Features.States.OAK_SAPLING),
						new BushFoliagePlacer(ConstantInt.of(2), ConstantInt.of(1), 2),
						new TwoLayersFeatureSize(0, 0, 0)
					)
					.build()
			)
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> AZALEA_TREE = register(
		"azalea_tree",
		Feature.TREE
			.configured(
				new TreeConfiguration.TreeConfigurationBuilder(
						new SimpleStateProvider(Features.States.OAK_LOG),
						new BendingTrunkPlacer(4, 2, 0, 3, UniformInt.of(1, 2)),
						new WeightedStateProvider(weightedBlockStateBuilder().add(Features.States.AZALEA_LEAVES, 3).add(Features.States.FLOWERING_AZALEA_LEAVES, 1)),
						new SimpleStateProvider(Features.States.AZALEA),
						new RandomSpreadFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0), ConstantInt.of(2), 50),
						new TwoLayersFeatureSize(1, 0, 1)
					)
					.dirt(new SimpleStateProvider(Blocks.ROOTED_DIRT.defaultBlockState()))
					.forceDirt()
					.build()
			)
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> OAK_BEES_0002 = register(
		"oak_bees_0002", Feature.TREE.configured(OAK.config().withDecorators(ImmutableList.of(Features.Decorators.BEEHIVE_0002)))
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> OAK_BEES_002 = register(
		"oak_bees_002", Feature.TREE.configured(OAK.config().withDecorators(ImmutableList.of(Features.Decorators.BEEHIVE_002)))
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> OAK_BEES_005 = register(
		"oak_bees_005", Feature.TREE.configured(OAK.config().withDecorators(ImmutableList.of(Features.Decorators.BEEHIVE_005)))
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> BIRCH_BEES_0002 = register(
		"birch_bees_0002", Feature.TREE.configured(BIRCH.config().withDecorators(ImmutableList.of(Features.Decorators.BEEHIVE_0002)))
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> BIRCH_BEES_002 = register(
		"birch_bees_002", Feature.TREE.configured(BIRCH.config().withDecorators(ImmutableList.of(Features.Decorators.BEEHIVE_002)))
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> BIRCH_BEES_005 = register(
		"birch_bees_005", Feature.TREE.configured(BIRCH.config().withDecorators(ImmutableList.of(Features.Decorators.BEEHIVE_005)))
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> FANCY_OAK_BEES_0002 = register(
		"fancy_oak_bees_0002", Feature.TREE.configured(FANCY_OAK.config().withDecorators(ImmutableList.of(Features.Decorators.BEEHIVE_0002)))
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> FANCY_OAK_BEES_002 = register(
		"fancy_oak_bees_002", Feature.TREE.configured(FANCY_OAK.config().withDecorators(ImmutableList.of(Features.Decorators.BEEHIVE_002)))
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> FANCY_OAK_BEES_005 = register(
		"fancy_oak_bees_005", Feature.TREE.configured(FANCY_OAK.config().withDecorators(ImmutableList.of(Features.Decorators.BEEHIVE_005)))
	);
	public static final ConfiguredFeature<?, ?> FLOWER_WARM = register(
		"flower_warm",
		Feature.FLOWER
			.configured(Features.Configs.DEFAULT_FLOWER_CONFIG)
			.decorated(Features.Decorators.ADD_32)
			.decorated(Features.Decorators.HEIGHTMAP_SQUARE)
			.count(4)
	);
	public static final ConfiguredFeature<?, ?> FLOWER_DEFAULT = register(
		"flower_default",
		Feature.FLOWER
			.configured(Features.Configs.DEFAULT_FLOWER_CONFIG)
			.decorated(Features.Decorators.ADD_32)
			.decorated(Features.Decorators.HEIGHTMAP_SQUARE)
			.count(2)
	);
	public static final ConfiguredFeature<?, ?> FLOWER_FOREST = register(
		"flower_forest",
		Feature.FLOWER
			.configured(new RandomPatchConfiguration.GrassConfigurationBuilder(ForestFlowerProvider.INSTANCE, SimpleBlockPlacer.INSTANCE).tries(64).build())
			.decorated(Features.Decorators.ADD_32)
			.decorated(Features.Decorators.HEIGHTMAP_SQUARE)
			.count(100)
	);
	public static final ConfiguredFeature<?, ?> FLOWER_SWAMP = register(
		"flower_swamp",
		Feature.FLOWER
			.configured(
				new RandomPatchConfiguration.GrassConfigurationBuilder(new SimpleStateProvider(Features.States.BLUE_ORCHID), SimpleBlockPlacer.INSTANCE).tries(64).build()
			)
			.decorated(Features.Decorators.ADD_32)
			.decorated(Features.Decorators.HEIGHTMAP_SQUARE)
	);
	public static final ConfiguredFeature<?, ?> FLOWER_PLAIN = register(
		"flower_plain",
		Feature.FLOWER.configured(new RandomPatchConfiguration.GrassConfigurationBuilder(PlainFlowerProvider.INSTANCE, SimpleBlockPlacer.INSTANCE).tries(64).build())
	);
	public static final ConfiguredFeature<?, ?> FLOWER_PLAIN_DECORATED = register(
		"flower_plain_decorated",
		FLOWER_PLAIN.decorated(Features.Decorators.ADD_32)
			.decorated(Features.Decorators.HEIGHTMAP)
			.squared()
			.decorated(FeatureDecorator.COUNT_NOISE.configured(new NoiseDependantDecoratorConfiguration(-0.8, 15, 4)))
	);
	private static final ImmutableList<Supplier<ConfiguredFeature<?, ?>>> FOREST_FLOWER_FEATURES = ImmutableList.of(
		() -> Feature.RANDOM_PATCH
				.configured(
					new RandomPatchConfiguration.GrassConfigurationBuilder(new SimpleStateProvider(Features.States.LILAC), new DoublePlantPlacer())
						.tries(64)
						.noProjection()
						.build()
				),
		() -> Feature.RANDOM_PATCH
				.configured(
					new RandomPatchConfiguration.GrassConfigurationBuilder(new SimpleStateProvider(Features.States.ROSE_BUSH), new DoublePlantPlacer())
						.tries(64)
						.noProjection()
						.build()
				),
		() -> Feature.RANDOM_PATCH
				.configured(
					new RandomPatchConfiguration.GrassConfigurationBuilder(new SimpleStateProvider(Features.States.PEONY), new DoublePlantPlacer())
						.tries(64)
						.noProjection()
						.build()
				),
		() -> Feature.NO_BONEMEAL_FLOWER
				.configured(
					new RandomPatchConfiguration.GrassConfigurationBuilder(new SimpleStateProvider(Features.States.LILY_OF_THE_VALLEY), SimpleBlockPlacer.INSTANCE)
						.tries(64)
						.build()
				)
	);
	public static final ConfiguredFeature<?, ?> FOREST_FLOWER_VEGETATION_COMMON = register(
		"forest_flower_vegetation_common",
		Feature.SIMPLE_RANDOM_SELECTOR
			.configured(new SimpleRandomFeatureConfiguration(FOREST_FLOWER_FEATURES))
			.count(ClampedInt.of(UniformInt.of(-1, 3), 0, 3))
			.decorated(Features.Decorators.ADD_32)
			.decorated(Features.Decorators.HEIGHTMAP_SQUARE)
			.count(5)
	);
	public static final ConfiguredFeature<?, ?> FOREST_FLOWER_VEGETATION = register(
		"forest_flower_vegetation",
		Feature.SIMPLE_RANDOM_SELECTOR
			.configured(new SimpleRandomFeatureConfiguration(FOREST_FLOWER_FEATURES))
			.count(ClampedInt.of(UniformInt.of(-3, 1), 0, 1))
			.decorated(Features.Decorators.ADD_32)
			.decorated(Features.Decorators.HEIGHTMAP_SQUARE)
			.count(5)
	);
	public static final ConfiguredFeature<?, ?> DARK_FOREST_VEGETATION_BROWN = register(
		"dark_forest_vegetation_brown",
		Feature.RANDOM_SELECTOR
			.configured(
				new RandomFeatureConfiguration(
					ImmutableList.of(
						HUGE_BROWN_MUSHROOM.weighted(0.025F), HUGE_RED_MUSHROOM.weighted(0.05F), DARK_OAK.weighted(0.6666667F), BIRCH.weighted(0.2F), FANCY_OAK.weighted(0.1F)
					),
					OAK
				)
			)
			.decorated(Features.Decorators.DARK_OAK_DECORATOR)
	);
	public static final ConfiguredFeature<?, ?> DARK_FOREST_VEGETATION_RED = register(
		"dark_forest_vegetation_red",
		Feature.RANDOM_SELECTOR
			.configured(
				new RandomFeatureConfiguration(
					ImmutableList.of(
						HUGE_RED_MUSHROOM.weighted(0.025F), HUGE_BROWN_MUSHROOM.weighted(0.05F), DARK_OAK.weighted(0.6666667F), BIRCH.weighted(0.2F), FANCY_OAK.weighted(0.1F)
					),
					OAK
				)
			)
			.decorated(Features.Decorators.DARK_OAK_DECORATOR)
	);
	public static final ConfiguredFeature<?, ?> WARM_OCEAN_VEGETATION = register(
		"warm_ocean_vegetation",
		Feature.SIMPLE_RANDOM_SELECTOR
			.configured(
				new SimpleRandomFeatureConfiguration(
					ImmutableList.of(
						() -> Feature.CORAL_TREE.configured(FeatureConfiguration.NONE),
						() -> Feature.CORAL_CLAW.configured(FeatureConfiguration.NONE),
						() -> Feature.CORAL_MUSHROOM.configured(FeatureConfiguration.NONE)
					)
				)
			)
			.decorated(Features.Decorators.HEIGHTMAP_TOP_SOLID)
			.squared()
			.decorated(FeatureDecorator.COUNT_NOISE_BIASED.configured(new NoiseCountFactorDecoratorConfiguration(20, 400.0, 0.0)))
	);
	public static final ConfiguredFeature<?, ?> FOREST_FLOWER_TREES = register(
		"forest_flower_trees",
		Feature.RANDOM_SELECTOR
			.configured(new RandomFeatureConfiguration(ImmutableList.of(BIRCH_BEES_002.weighted(0.2F), FANCY_OAK_BEES_002.weighted(0.1F)), OAK_BEES_002))
			.decorated(Features.Decorators.HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED)
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(6, 0.1F, 1)))
	);
	public static final ConfiguredFeature<?, ?> TAIGA_VEGETATION = register(
		"taiga_vegetation",
		Feature.RANDOM_SELECTOR
			.configured(new RandomFeatureConfiguration(ImmutableList.of(PINE.weighted(0.33333334F)), SPRUCE))
			.decorated(Features.Decorators.HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED)
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(10, 0.1F, 1)))
	);
	public static final ConfiguredFeature<?, ?> TREES_BADLANDS = register(
		"trees_badlands",
		OAK.decorated(Features.Decorators.HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED)
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(5, 0.1F, 1)))
	);
	public static final ConfiguredFeature<?, ?> TREES_SNOWY = register(
		"trees_snowy",
		SPRUCE.decorated(Features.Decorators.HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED)
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(0, 0.1F, 1)))
	);
	public static final ConfiguredFeature<?, ?> TREES_SWAMP = register(
		"trees_swamp",
		SWAMP_OAK.decorated(Features.Decorators.HEIGHTMAP_OCEAN_FLOOR)
			.decorated(FeatureDecorator.WATER_DEPTH_THRESHOLD.configured(new WaterDepthThresholdConfiguration(1)))
			.squared()
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(2, 0.1F, 1)))
	);
	public static final ConfiguredFeature<?, ?> TREES_SHATTERED_SAVANNA = register(
		"trees_shattered_savanna",
		Feature.RANDOM_SELECTOR
			.configured(new RandomFeatureConfiguration(ImmutableList.of(ACACIA.weighted(0.8F)), OAK))
			.decorated(Features.Decorators.HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED)
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(2, 0.1F, 1)))
	);
	public static final ConfiguredFeature<?, ?> TREES_SAVANNA = register(
		"trees_savanna",
		Feature.RANDOM_SELECTOR
			.configured(new RandomFeatureConfiguration(ImmutableList.of(ACACIA.weighted(0.8F)), OAK))
			.decorated(Features.Decorators.HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED)
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(1, 0.1F, 1)))
	);
	public static final ConfiguredFeature<?, ?> BIRCH_TALL = register(
		"birch_tall",
		Feature.RANDOM_SELECTOR
			.configured(new RandomFeatureConfiguration(ImmutableList.of(SUPER_BIRCH_BEES_0002.weighted(0.5F)), BIRCH_BEES_0002))
			.decorated(Features.Decorators.HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED)
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(10, 0.1F, 1)))
	);
	public static final ConfiguredFeature<?, ?> TREES_BIRCH = register(
		"trees_birch",
		BIRCH_BEES_0002.decorated(Features.Decorators.HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED)
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(10, 0.1F, 1)))
	);
	public static final ConfiguredFeature<?, ?> TREES_MOUNTAIN_EDGE = register(
		"trees_mountain_edge",
		Feature.RANDOM_SELECTOR
			.configured(new RandomFeatureConfiguration(ImmutableList.of(SPRUCE.weighted(0.666F), FANCY_OAK.weighted(0.1F)), OAK))
			.decorated(Features.Decorators.HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED)
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(3, 0.1F, 1)))
	);
	public static final ConfiguredFeature<?, ?> TREES_MOUNTAIN = register(
		"trees_mountain",
		Feature.RANDOM_SELECTOR
			.configured(new RandomFeatureConfiguration(ImmutableList.of(SPRUCE.weighted(0.666F), FANCY_OAK.weighted(0.1F)), OAK))
			.decorated(Features.Decorators.HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED)
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(0, 0.1F, 1)))
	);
	public static final ConfiguredFeature<?, ?> TREES_WATER = register(
		"trees_water",
		Feature.RANDOM_SELECTOR
			.configured(new RandomFeatureConfiguration(ImmutableList.of(FANCY_OAK.weighted(0.1F)), OAK))
			.decorated(Features.Decorators.HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED)
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(0, 0.1F, 1)))
	);
	public static final ConfiguredFeature<?, ?> BIRCH_OTHER = register(
		"birch_other",
		Feature.RANDOM_SELECTOR
			.configured(new RandomFeatureConfiguration(ImmutableList.of(BIRCH_BEES_0002.weighted(0.2F), FANCY_OAK_BEES_0002.weighted(0.1F)), OAK_BEES_0002))
			.decorated(Features.Decorators.HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED)
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(10, 0.1F, 1)))
	);
	public static final ConfiguredFeature<?, ?> PLAIN_VEGETATION = register(
		"plain_vegetation",
		Feature.RANDOM_SELECTOR
			.configured(new RandomFeatureConfiguration(ImmutableList.of(FANCY_OAK_BEES_005.weighted(0.33333334F)), OAK_BEES_005))
			.decorated(Features.Decorators.HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED)
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(0, 0.05F, 1)))
	);
	public static final ConfiguredFeature<?, ?> TREES_JUNGLE_EDGE = register(
		"trees_jungle_edge",
		Feature.RANDOM_SELECTOR
			.configured(new RandomFeatureConfiguration(ImmutableList.of(FANCY_OAK.weighted(0.1F), JUNGLE_BUSH.weighted(0.5F)), JUNGLE_TREE))
			.decorated(Features.Decorators.HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED)
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(2, 0.1F, 1)))
	);
	public static final ConfiguredFeature<?, ?> TREES_GIANT_SPRUCE = register(
		"trees_giant_spruce",
		Feature.RANDOM_SELECTOR
			.configured(new RandomFeatureConfiguration(ImmutableList.of(MEGA_SPRUCE.weighted(0.33333334F), PINE.weighted(0.33333334F)), SPRUCE))
			.decorated(Features.Decorators.HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED)
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(10, 0.1F, 1)))
	);
	public static final ConfiguredFeature<?, ?> TREES_GIANT = register(
		"trees_giant",
		Feature.RANDOM_SELECTOR
			.configured(
				new RandomFeatureConfiguration(ImmutableList.of(MEGA_SPRUCE.weighted(0.025641026F), MEGA_PINE.weighted(0.30769232F), PINE.weighted(0.33333334F)), SPRUCE)
			)
			.decorated(Features.Decorators.HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED)
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(10, 0.1F, 1)))
	);
	public static final ConfiguredFeature<?, ?> TREES_JUNGLE = register(
		"trees_jungle",
		Feature.RANDOM_SELECTOR
			.configured(
				new RandomFeatureConfiguration(ImmutableList.of(FANCY_OAK.weighted(0.1F), JUNGLE_BUSH.weighted(0.5F), MEGA_JUNGLE_TREE.weighted(0.33333334F)), JUNGLE_TREE)
			)
			.decorated(Features.Decorators.HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED)
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(50, 0.1F, 1)))
	);
	public static final ConfiguredFeature<?, ?> BAMBOO_VEGETATION = register(
		"bamboo_vegetation",
		Feature.RANDOM_SELECTOR
			.configured(
				new RandomFeatureConfiguration(
					ImmutableList.of(FANCY_OAK.weighted(0.05F), JUNGLE_BUSH.weighted(0.15F), MEGA_JUNGLE_TREE.weighted(0.7F)),
					Feature.RANDOM_PATCH.configured(Features.Configs.JUNGLE_GRASS_CONFIG)
				)
			)
			.decorated(Features.Decorators.HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED)
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(30, 0.1F, 1)))
	);
	public static final ConfiguredFeature<?, ?> MUSHROOM_FIELD_VEGETATION = register(
		"mushroom_field_vegetation",
		Feature.RANDOM_BOOLEAN_SELECTOR
			.configured(new RandomBooleanFeatureConfiguration(() -> HUGE_RED_MUSHROOM, () -> HUGE_BROWN_MUSHROOM))
			.decorated(Features.Decorators.HEIGHTMAP_SQUARE)
	);
	public static final ConfiguredFeature<?, ?> ROOTED_AZALEA_TREES = register(
		"rooted_azalea_trees",
		Feature.ROOT_SYSTEM
			.configured(
				new RootSystemConfiguration(
					() -> AZALEA_TREE,
					3,
					3,
					BlockTags.LUSH_GROUND_REPLACEABLE.getName(),
					new SimpleStateProvider(Blocks.ROOTED_DIRT.defaultBlockState()),
					20,
					100,
					3,
					2,
					new SimpleStateProvider(Blocks.HANGING_ROOTS.defaultBlockState()),
					20,
					2
				)
			)
			.decorated(FeatureDecorator.CAVE_SURFACE.configured(new CaveDecoratorConfiguration(CaveSurface.CEILING, 12)))
			.range(Features.Decorators.RANGE_BOTTOM_TO_60)
			.squared()
			.rarity(2)
	);
	private static final WeightedStateProvider CAVE_VINES_BODY_PROVIDER = new WeightedStateProvider(
		weightedBlockStateBuilder()
			.add(Blocks.CAVE_VINES_PLANT.defaultBlockState(), 4)
			.add(Blocks.CAVE_VINES_PLANT.defaultBlockState().setValue(CaveVines.BERRIES, Boolean.valueOf(true)), 1)
	);
	private static final RandomizedIntStateProvider CAVE_VINES_HEAD_PROVIDER = new RandomizedIntStateProvider(
		new WeightedStateProvider(
			weightedBlockStateBuilder()
				.add(Blocks.CAVE_VINES.defaultBlockState(), 4)
				.add(Blocks.CAVE_VINES.defaultBlockState().setValue(CaveVines.BERRIES, Boolean.valueOf(true)), 1)
		),
		CaveVinesBlock.AGE,
		UniformInt.of(17, 25)
	);
	public static final ConfiguredFeature<GrowingPlantConfiguration, ?> CAVE_VINE = register(
		"cave_vine",
		Feature.GROWING_PLANT
			.configured(
				new GrowingPlantConfiguration(
					SimpleWeightedRandomList.<IntProvider>builder().add(UniformInt.of(1, 20), 2).add(UniformInt.of(1, 3), 3).add(UniformInt.of(1, 7), 10).build(),
					Direction.DOWN,
					CAVE_VINES_BODY_PROVIDER,
					CAVE_VINES_HEAD_PROVIDER,
					false
				)
			)
	);
	public static final ConfiguredFeature<GrowingPlantConfiguration, ?> CAVE_VINE_IN_MOSS = register(
		"cave_vine_in_moss",
		Feature.GROWING_PLANT
			.configured(
				new GrowingPlantConfiguration(
					SimpleWeightedRandomList.<IntProvider>builder().add(UniformInt.of(1, 4), 5).add(UniformInt.of(2, 8), 1).build(),
					Direction.DOWN,
					CAVE_VINES_BODY_PROVIDER,
					CAVE_VINES_HEAD_PROVIDER,
					false
				)
			)
	);
	public static final ConfiguredFeature<?, ?> CAVE_VINES = register(
		"cave_vines",
		CAVE_VINE.decorated(FeatureDecorator.CAVE_SURFACE.configured(new CaveDecoratorConfiguration(CaveSurface.CEILING, 12)))
			.range(Features.Decorators.RANGE_BOTTOM_TO_60)
			.squared()
			.count(60)
	);
	public static final ConfiguredFeature<SimpleBlockConfiguration, ?> MOSS_VEGETATION = register(
		"moss_vegetation",
		Feature.SIMPLE_BLOCK
			.configured(
				new SimpleBlockConfiguration(
					new WeightedStateProvider(
						weightedBlockStateBuilder()
							.add(Blocks.FLOWERING_AZALEA.defaultBlockState(), 4)
							.add(Blocks.AZALEA.defaultBlockState(), 7)
							.add(Blocks.MOSS_CARPET.defaultBlockState(), 25)
							.add(Blocks.GRASS.defaultBlockState(), 50)
							.add(Blocks.TALL_GRASS.defaultBlockState(), 10)
					)
				)
			)
	);
	public static final ConfiguredFeature<VegetationPatchConfiguration, ?> MOSS_PATCH = register(
		"moss_patch",
		Feature.VEGETATION_PATCH
			.configured(
				new VegetationPatchConfiguration(
					BlockTags.MOSS_REPLACEABLE.getName(),
					new SimpleStateProvider(Blocks.MOSS_BLOCK.defaultBlockState()),
					() -> MOSS_VEGETATION,
					CaveSurface.FLOOR,
					ConstantInt.of(1),
					0.0F,
					5,
					0.8F,
					UniformInt.of(4, 7),
					0.3F
				)
			)
	);
	public static final ConfiguredFeature<VegetationPatchConfiguration, ?> MOSS_PATCH_BONEMEAL = register(
		"moss_patch_bonemeal",
		Feature.VEGETATION_PATCH
			.configured(
				new VegetationPatchConfiguration(
					BlockTags.MOSS_REPLACEABLE.getName(),
					new SimpleStateProvider(Blocks.MOSS_BLOCK.defaultBlockState()),
					() -> MOSS_VEGETATION,
					CaveSurface.FLOOR,
					ConstantInt.of(1),
					0.0F,
					5,
					0.6F,
					UniformInt.of(1, 2),
					0.75F
				)
			)
	);
	public static final ConfiguredFeature<?, ?> LUSH_CAVES_VEGETATION = register(
		"lush_caves_vegetation",
		MOSS_PATCH.decorated(FeatureDecorator.CAVE_SURFACE.configured(new CaveDecoratorConfiguration(CaveSurface.FLOOR, 12)))
			.range(Features.Decorators.RANGE_BOTTOM_TO_60)
			.squared()
			.count(40)
	);
	public static final ConfiguredFeature<SimpleRandomFeatureConfiguration, ?> DRIPLEAF = register(
		"dripleaf",
		Feature.SIMPLE_RANDOM_SELECTOR
			.configured(
				new SimpleRandomFeatureConfiguration(
					ImmutableList.of(
						Features::makeSmallDripleaf,
						() -> makeDripleaf(Direction.EAST),
						() -> makeDripleaf(Direction.WEST),
						() -> makeDripleaf(Direction.SOUTH),
						() -> makeDripleaf(Direction.NORTH)
					)
				)
			)
	);
	public static final ConfiguredFeature<?, ?> CLAY_WITH_DRIPLEAVES = register(
		"clay_with_dripleaves",
		Feature.VEGETATION_PATCH
			.configured(
				new VegetationPatchConfiguration(
					BlockTags.LUSH_GROUND_REPLACEABLE.getName(),
					new SimpleStateProvider(Blocks.CLAY.defaultBlockState()),
					() -> DRIPLEAF,
					CaveSurface.FLOOR,
					ConstantInt.of(3),
					0.8F,
					2,
					0.05F,
					UniformInt.of(4, 7),
					0.7F
				)
			)
	);
	public static final ConfiguredFeature<?, ?> CLAY_POOL_WITH_DRIPLEAVES = register(
		"clay_pool_with_dripleaves",
		Feature.WATERLOGGED_VEGETATION_PATCH
			.configured(
				new VegetationPatchConfiguration(
					BlockTags.LUSH_GROUND_REPLACEABLE.getName(),
					new SimpleStateProvider(Blocks.CLAY.defaultBlockState()),
					() -> DRIPLEAF,
					CaveSurface.FLOOR,
					ConstantInt.of(3),
					0.8F,
					5,
					0.1F,
					UniformInt.of(4, 7),
					0.7F
				)
			)
	);
	public static final ConfiguredFeature<?, ?> LUSH_CAVES_CLAY = register(
		"lush_caves_clay",
		Feature.RANDOM_BOOLEAN_SELECTOR
			.configured(new RandomBooleanFeatureConfiguration(() -> CLAY_WITH_DRIPLEAVES, () -> CLAY_POOL_WITH_DRIPLEAVES))
			.decorated(FeatureDecorator.CAVE_SURFACE.configured(new CaveDecoratorConfiguration(CaveSurface.FLOOR, 12)))
			.range(Features.Decorators.RANGE_BOTTOM_TO_60)
			.squared()
			.count(20)
	);
	public static final ConfiguredFeature<VegetationPatchConfiguration, ?> MOSS_PATCH_CEILING = register(
		"moss_patch_ceiling",
		Feature.VEGETATION_PATCH
			.configured(
				new VegetationPatchConfiguration(
					BlockTags.MOSS_REPLACEABLE.getName(),
					new SimpleStateProvider(Blocks.MOSS_BLOCK.defaultBlockState()),
					() -> CAVE_VINE_IN_MOSS,
					CaveSurface.CEILING,
					UniformInt.of(1, 2),
					0.0F,
					5,
					0.08F,
					UniformInt.of(4, 7),
					0.3F
				)
			)
	);
	public static final ConfiguredFeature<?, ?> LUSH_CAVES_CEILING_VEGETATION = register(
		"lush_caves_ceiling_vegetation",
		MOSS_PATCH_CEILING.decorated(FeatureDecorator.CAVE_SURFACE.configured(new CaveDecoratorConfiguration(CaveSurface.CEILING, 12)))
			.range(Features.Decorators.RANGE_BOTTOM_TO_60)
			.squared()
			.count(40)
	);
	public static final ConfiguredFeature<?, ?> SPORE_BLOSSOM_FEATURE = register(
		"spore_blossom",
		Feature.SIMPLE_BLOCK
			.configured(new SimpleBlockConfiguration(new SimpleStateProvider(Features.States.SPORE_BLOSSOM)))
			.decorated(FeatureDecorator.CAVE_SURFACE.configured(new CaveDecoratorConfiguration(CaveSurface.CEILING, 12)))
			.range(Features.Decorators.RANGE_BOTTOM_TO_60)
			.squared()
			.count(8)
	);
	public static final ConfiguredFeature<?, ?> CLASSIC_VINES_CAVE_FEATURE = register(
		"classic_vines_cave_feature", Feature.VINES.configured(FeatureConfiguration.NONE).range(Features.Decorators.RANGE_BOTTOM_TO_60).squared().count(127)
	);
	public static final ConfiguredFeature<?, ?> AMETHYST_GEODE = register(
		"amethyst_geode",
		Feature.GEODE
			.configured(
				new GeodeConfiguration(
					new GeodeBlockSettings(
						new SimpleStateProvider(Features.States.AIR),
						new SimpleStateProvider(Features.States.AMETHYST_BLOCK),
						new SimpleStateProvider(Features.States.BUDDING_AMETHYST),
						new SimpleStateProvider(Features.States.CALCITE),
						new SimpleStateProvider(Features.States.SMOOTH_BASALT),
						ImmutableList.of(
							Blocks.SMALL_AMETHYST_BUD.defaultBlockState(),
							Blocks.MEDIUM_AMETHYST_BUD.defaultBlockState(),
							Blocks.LARGE_AMETHYST_BUD.defaultBlockState(),
							Blocks.AMETHYST_CLUSTER.defaultBlockState()
						),
						BlockTags.FEATURES_CANNOT_REPLACE.getName(),
						BlockTags.GEODE_INVALID_BLOCKS.getName()
					),
					new GeodeLayerSettings(1.7, 2.2, 3.2, 4.2),
					new GeodeCrackSettings(0.95, 2.0, 2),
					0.35,
					0.083,
					true,
					UniformInt.of(4, 6),
					UniformInt.of(3, 4),
					UniformInt.of(1, 2),
					-16,
					16,
					0.05,
					1
				)
			)
			.rangeUniform(VerticalAnchor.aboveBottom(6), VerticalAnchor.absolute(46))
			.squared()
			.rarity(53)
	);

	static SimpleWeightedRandomList.Builder<BlockState> weightedBlockStateBuilder() {
		return SimpleWeightedRandomList.builder();
	}

	private static ConfiguredFeature<GrowingPlantConfiguration, ?> makeDripleaf(Direction direction) {
		return Feature.GROWING_PLANT
			.configured(
				new GrowingPlantConfiguration(
					SimpleWeightedRandomList.<IntProvider>builder().add(UniformInt.of(1, 5), 2).add(ConstantInt.of(1), 1).build(),
					Direction.UP,
					new SimpleStateProvider(Blocks.BIG_DRIPLEAF_STEM.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, direction)),
					new SimpleStateProvider(Blocks.BIG_DRIPLEAF.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, direction)),
					true
				)
			);
	}

	private static ConfiguredFeature<SimpleBlockConfiguration, ?> makeSmallDripleaf() {
		return Feature.SIMPLE_BLOCK
			.configured(
				new SimpleBlockConfiguration(
					new WeightedStateProvider(
						weightedBlockStateBuilder()
							.add(Features.States.SMALL_DRIPLEAF_EAST, 1)
							.add(Features.States.SMALL_DRIPLEAF_WEST, 1)
							.add(Features.States.SMALL_DRIPLEAF_NORTH, 1)
							.add(Features.States.SMALL_DRIPLEAF_SOUTH, 1)
					)
				)
			);
	}

	private static <FC extends FeatureConfiguration> ConfiguredFeature<FC, ?> register(String string, ConfiguredFeature<FC, ?> configuredFeature) {
		return Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, string, configuredFeature);
	}

	public static final class Configs {
		public static final RandomPatchConfiguration DEFAULT_GRASS_CONFIG = new RandomPatchConfiguration.GrassConfigurationBuilder(
				new SimpleStateProvider(Features.States.GRASS), SimpleBlockPlacer.INSTANCE
			)
			.tries(32)
			.build();
		public static final RandomPatchConfiguration TAIGA_GRASS_CONFIG = new RandomPatchConfiguration.GrassConfigurationBuilder(
				new WeightedStateProvider(Features.weightedBlockStateBuilder().add(Features.States.GRASS, 1).add(Features.States.FERN, 4)), SimpleBlockPlacer.INSTANCE
			)
			.tries(32)
			.build();
		public static final RandomPatchConfiguration JUNGLE_GRASS_CONFIG = new RandomPatchConfiguration.GrassConfigurationBuilder(
				new WeightedStateProvider(Features.weightedBlockStateBuilder().add(Features.States.GRASS, 3).add(Features.States.FERN, 1)), SimpleBlockPlacer.INSTANCE
			)
			.blacklist(ImmutableSet.of(Features.States.PODZOL))
			.tries(32)
			.build();
		public static final RandomPatchConfiguration DEFAULT_FLOWER_CONFIG = new RandomPatchConfiguration.GrassConfigurationBuilder(
				new WeightedStateProvider(Features.weightedBlockStateBuilder().add(Features.States.POPPY, 2).add(Features.States.DANDELION, 1)), SimpleBlockPlacer.INSTANCE
			)
			.tries(64)
			.build();
		public static final RandomPatchConfiguration DEAD_BUSH_CONFIG = new RandomPatchConfiguration.GrassConfigurationBuilder(
				new SimpleStateProvider(Features.States.DEAD_BUSH), SimpleBlockPlacer.INSTANCE
			)
			.tries(4)
			.build();
		public static final RandomPatchConfiguration SWEET_BERRY_BUSH_CONFIG = new RandomPatchConfiguration.GrassConfigurationBuilder(
				new SimpleStateProvider(Features.States.SWEET_BERRY_BUSH), SimpleBlockPlacer.INSTANCE
			)
			.tries(64)
			.whitelist(ImmutableSet.of(Features.States.GRASS_BLOCK.getBlock()))
			.noProjection()
			.build();
		public static final RandomPatchConfiguration TALL_GRASS_CONFIG = new RandomPatchConfiguration.GrassConfigurationBuilder(
				new SimpleStateProvider(Features.States.TALL_GRASS), new DoublePlantPlacer()
			)
			.tries(64)
			.noProjection()
			.build();
		public static final RandomPatchConfiguration SUGAR_CANE_CONFIG = new RandomPatchConfiguration.GrassConfigurationBuilder(
				new SimpleStateProvider(Features.States.SUGAR_CANE), new ColumnPlacer(BiasedToBottomInt.of(2, 4))
			)
			.tries(20)
			.xspread(4)
			.yspread(0)
			.zspread(4)
			.noProjection()
			.needWater()
			.build();
		public static final SpringConfiguration LAVA_SPRING_CONFIG = new SpringConfiguration(
			Features.States.LAVA_STATE, true, 4, 1, ImmutableSet.of(Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE, Blocks.DEEPSLATE, Blocks.TUFF)
		);
		public static final SpringConfiguration CLOSED_NETHER_SPRING_CONFIG = new SpringConfiguration(
			Features.States.LAVA_STATE, false, 5, 0, ImmutableSet.of(Blocks.NETHERRACK)
		);
		public static final BlockPileConfiguration CRIMSON_FOREST_CONFIG = new BlockPileConfiguration(
			new WeightedStateProvider(
				Features.weightedBlockStateBuilder().add(Features.States.CRIMSON_ROOTS, 87).add(Features.States.CRIMSON_FUNGUS, 11).add(Features.States.WARPED_FUNGUS, 1)
			)
		);
		public static final BlockPileConfiguration WARPED_FOREST_CONFIG = new BlockPileConfiguration(
			new WeightedStateProvider(
				Features.weightedBlockStateBuilder()
					.add(Features.States.WARPED_ROOTS, 85)
					.add(Features.States.CRIMSON_ROOTS, 1)
					.add(Features.States.WARPED_FUNGUS, 13)
					.add(Features.States.CRIMSON_FUNGUS, 1)
			)
		);
		public static final BlockPileConfiguration NETHER_SPROUTS_CONFIG = new BlockPileConfiguration(new SimpleStateProvider(Features.States.NETHER_SPROUTS));
	}

	protected static final class Decorators {
		public static final BeehiveDecorator BEEHIVE_0002 = new BeehiveDecorator(0.002F);
		public static final BeehiveDecorator BEEHIVE_002 = new BeehiveDecorator(0.02F);
		public static final BeehiveDecorator BEEHIVE_005 = new BeehiveDecorator(0.05F);
		public static final ConfiguredDecorator<HeightmapConfiguration> HEIGHTMAP = FeatureDecorator.HEIGHTMAP
			.configured(new HeightmapConfiguration(Heightmap.Types.MOTION_BLOCKING));
		public static final ConfiguredDecorator<HeightmapConfiguration> HEIGHTMAP_TOP_SOLID = FeatureDecorator.HEIGHTMAP
			.configured(new HeightmapConfiguration(Heightmap.Types.OCEAN_FLOOR_WG));
		public static final ConfiguredDecorator<HeightmapConfiguration> HEIGHTMAP_WORLD_SURFACE = FeatureDecorator.HEIGHTMAP
			.configured(new HeightmapConfiguration(Heightmap.Types.WORLD_SURFACE_WG));
		public static final ConfiguredDecorator<HeightmapConfiguration> HEIGHTMAP_OCEAN_FLOOR = FeatureDecorator.HEIGHTMAP
			.configured(new HeightmapConfiguration(Heightmap.Types.OCEAN_FLOOR));
		public static final ConfiguredDecorator<HeightmapConfiguration> HEIGHTMAP_DOUBLE = FeatureDecorator.HEIGHTMAP_SPREAD_DOUBLE
			.configured(new HeightmapConfiguration(Heightmap.Types.MOTION_BLOCKING));
		public static final RangeDecoratorConfiguration FULL_RANGE = new RangeDecoratorConfiguration(UniformHeight.of(VerticalAnchor.bottom(), VerticalAnchor.top()));
		public static final RangeDecoratorConfiguration RANGE_10_10 = new RangeDecoratorConfiguration(
			UniformHeight.of(VerticalAnchor.aboveBottom(10), VerticalAnchor.belowTop(10))
		);
		public static final RangeDecoratorConfiguration RANGE_8_8 = new RangeDecoratorConfiguration(
			UniformHeight.of(VerticalAnchor.aboveBottom(8), VerticalAnchor.belowTop(8))
		);
		public static final RangeDecoratorConfiguration RANGE_4_4 = new RangeDecoratorConfiguration(
			UniformHeight.of(VerticalAnchor.aboveBottom(4), VerticalAnchor.belowTop(4))
		);
		public static final RangeDecoratorConfiguration RANGE_BOTTOM_TO_60 = new RangeDecoratorConfiguration(
			UniformHeight.of(VerticalAnchor.bottom(), VerticalAnchor.absolute(60))
		);
		public static final ConfiguredDecorator<?> FIRE = FeatureDecorator.RANGE.configured(RANGE_4_4).squared().countRandom(5);
		public static final ConfiguredDecorator<?> ADD_32 = FeatureDecorator.SPREAD_32_ABOVE.configured(NoneDecoratorConfiguration.INSTANCE);
		public static final ConfiguredDecorator<?> HEIGHTMAP_WITH_TREE_THRESHOLD = HEIGHTMAP_OCEAN_FLOOR.decorated(
			FeatureDecorator.WATER_DEPTH_THRESHOLD.configured(new WaterDepthThresholdConfiguration(0))
		);
		public static final ConfiguredDecorator<?> HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED = HEIGHTMAP_WITH_TREE_THRESHOLD.squared();
		public static final ConfiguredDecorator<?> HEIGHTMAP_SQUARE = HEIGHTMAP.squared();
		public static final ConfiguredDecorator<?> HEIGHTMAP_DOUBLE_SQUARE = HEIGHTMAP_DOUBLE.squared();
		public static final ConfiguredDecorator<?> TOP_SOLID_HEIGHTMAP_SQUARE = HEIGHTMAP_TOP_SOLID.squared();
		public static final ConfiguredDecorator<?> DARK_OAK_DECORATOR = HEIGHTMAP_WITH_TREE_THRESHOLD.decorated(
			FeatureDecorator.DARK_OAK_TREE.configured(DecoratorConfiguration.NONE)
		);
	}

	public static final class States {
		protected static final BlockState GRASS = Blocks.GRASS.defaultBlockState();
		protected static final BlockState FERN = Blocks.FERN.defaultBlockState();
		protected static final BlockState PODZOL = Blocks.PODZOL.defaultBlockState();
		protected static final BlockState COARSE_DIRT = Blocks.COARSE_DIRT.defaultBlockState();
		protected static final BlockState MYCELIUM = Blocks.MYCELIUM.defaultBlockState();
		protected static final BlockState SNOW_BLOCK = Blocks.SNOW_BLOCK.defaultBlockState();
		protected static final BlockState ICE = Blocks.ICE.defaultBlockState();
		protected static final BlockState OAK_LOG = Blocks.OAK_LOG.defaultBlockState();
		protected static final BlockState OAK_LEAVES = Blocks.OAK_LEAVES.defaultBlockState();
		protected static final BlockState OAK_SAPLING = Blocks.OAK_SAPLING.defaultBlockState();
		protected static final BlockState JUNGLE_LOG = Blocks.JUNGLE_LOG.defaultBlockState();
		protected static final BlockState JUNGLE_LEAVES = Blocks.JUNGLE_LEAVES.defaultBlockState();
		protected static final BlockState JUNGLE_SAPLING = Blocks.JUNGLE_SAPLING.defaultBlockState();
		protected static final BlockState SPRUCE_LOG = Blocks.SPRUCE_LOG.defaultBlockState();
		protected static final BlockState SPRUCE_LEAVES = Blocks.SPRUCE_LEAVES.defaultBlockState();
		protected static final BlockState SPRUCE_SAPLING = Blocks.SPRUCE_SAPLING.defaultBlockState();
		protected static final BlockState ACACIA_LOG = Blocks.ACACIA_LOG.defaultBlockState();
		protected static final BlockState ACACIA_LEAVES = Blocks.ACACIA_LEAVES.defaultBlockState();
		protected static final BlockState ACACIA_SAPLING = Blocks.ACACIA_SAPLING.defaultBlockState();
		protected static final BlockState BIRCH_LOG = Blocks.BIRCH_LOG.defaultBlockState();
		protected static final BlockState BIRCH_LEAVES = Blocks.BIRCH_LEAVES.defaultBlockState();
		protected static final BlockState BIRCH_SAPLING = Blocks.BIRCH_SAPLING.defaultBlockState();
		protected static final BlockState DARK_OAK_LOG = Blocks.DARK_OAK_LOG.defaultBlockState();
		protected static final BlockState DARK_OAK_LEAVES = Blocks.DARK_OAK_LEAVES.defaultBlockState();
		protected static final BlockState DARK_OAK_SAPLING = Blocks.DARK_OAK_SAPLING.defaultBlockState();
		protected static final BlockState GRASS_BLOCK = Blocks.GRASS_BLOCK.defaultBlockState();
		protected static final BlockState LARGE_FERN = Blocks.LARGE_FERN.defaultBlockState();
		protected static final BlockState TALL_GRASS = Blocks.TALL_GRASS.defaultBlockState();
		protected static final BlockState LILAC = Blocks.LILAC.defaultBlockState();
		protected static final BlockState ROSE_BUSH = Blocks.ROSE_BUSH.defaultBlockState();
		protected static final BlockState PEONY = Blocks.PEONY.defaultBlockState();
		protected static final BlockState BROWN_MUSHROOM = Blocks.BROWN_MUSHROOM.defaultBlockState();
		protected static final BlockState RED_MUSHROOM = Blocks.RED_MUSHROOM.defaultBlockState();
		protected static final BlockState PACKED_ICE = Blocks.PACKED_ICE.defaultBlockState();
		protected static final BlockState BLUE_ICE = Blocks.BLUE_ICE.defaultBlockState();
		protected static final BlockState LILY_OF_THE_VALLEY = Blocks.LILY_OF_THE_VALLEY.defaultBlockState();
		protected static final BlockState BLUE_ORCHID = Blocks.BLUE_ORCHID.defaultBlockState();
		protected static final BlockState POPPY = Blocks.POPPY.defaultBlockState();
		protected static final BlockState DANDELION = Blocks.DANDELION.defaultBlockState();
		protected static final BlockState DEAD_BUSH = Blocks.DEAD_BUSH.defaultBlockState();
		protected static final BlockState MELON = Blocks.MELON.defaultBlockState();
		protected static final BlockState PUMPKIN = Blocks.PUMPKIN.defaultBlockState();
		protected static final BlockState SWEET_BERRY_BUSH = Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, Integer.valueOf(3));
		protected static final BlockState FIRE = Blocks.FIRE.defaultBlockState();
		protected static final BlockState SOUL_FIRE = Blocks.SOUL_FIRE.defaultBlockState();
		protected static final BlockState NETHERRACK = Blocks.NETHERRACK.defaultBlockState();
		protected static final BlockState SOUL_SOIL = Blocks.SOUL_SOIL.defaultBlockState();
		protected static final BlockState CRIMSON_ROOTS = Blocks.CRIMSON_ROOTS.defaultBlockState();
		protected static final BlockState LILY_PAD = Blocks.LILY_PAD.defaultBlockState();
		protected static final BlockState SNOW = Blocks.SNOW.defaultBlockState();
		protected static final BlockState JACK_O_LANTERN = Blocks.JACK_O_LANTERN.defaultBlockState();
		protected static final BlockState SUNFLOWER = Blocks.SUNFLOWER.defaultBlockState();
		protected static final BlockState CACTUS = Blocks.CACTUS.defaultBlockState();
		protected static final BlockState SUGAR_CANE = Blocks.SUGAR_CANE.defaultBlockState();
		protected static final BlockState HUGE_RED_MUSHROOM = Blocks.RED_MUSHROOM_BLOCK.defaultBlockState().setValue(HugeMushroomBlock.DOWN, Boolean.valueOf(false));
		protected static final BlockState HUGE_BROWN_MUSHROOM = Blocks.BROWN_MUSHROOM_BLOCK
			.defaultBlockState()
			.setValue(HugeMushroomBlock.UP, Boolean.valueOf(true))
			.setValue(HugeMushroomBlock.DOWN, Boolean.valueOf(false));
		protected static final BlockState HUGE_MUSHROOM_STEM = Blocks.MUSHROOM_STEM
			.defaultBlockState()
			.setValue(HugeMushroomBlock.UP, Boolean.valueOf(false))
			.setValue(HugeMushroomBlock.DOWN, Boolean.valueOf(false));
		protected static final FluidState WATER_STATE = Fluids.WATER.defaultFluidState();
		protected static final FluidState LAVA_STATE = Fluids.LAVA.defaultFluidState();
		protected static final BlockState WATER = Blocks.WATER.defaultBlockState();
		protected static final BlockState LAVA = Blocks.LAVA.defaultBlockState();
		protected static final BlockState DIRT = Blocks.DIRT.defaultBlockState();
		protected static final BlockState GRAVEL = Blocks.GRAVEL.defaultBlockState();
		protected static final BlockState GRANITE = Blocks.GRANITE.defaultBlockState();
		protected static final BlockState DIORITE = Blocks.DIORITE.defaultBlockState();
		protected static final BlockState ANDESITE = Blocks.ANDESITE.defaultBlockState();
		protected static final BlockState COAL_ORE = Blocks.COAL_ORE.defaultBlockState();
		protected static final BlockState DEEPSLATE_COAL_ORE = Blocks.DEEPSLATE_COAL_ORE.defaultBlockState();
		protected static final BlockState COPPER_ORE = Blocks.COPPER_ORE.defaultBlockState();
		protected static final BlockState DEEPSLATE_COPPER_ORE = Blocks.DEEPSLATE_COPPER_ORE.defaultBlockState();
		protected static final BlockState IRON_ORE = Blocks.IRON_ORE.defaultBlockState();
		protected static final BlockState DEEPSLATE_IRON_ORE = Blocks.DEEPSLATE_IRON_ORE.defaultBlockState();
		protected static final BlockState GOLD_ORE = Blocks.GOLD_ORE.defaultBlockState();
		protected static final BlockState DEEPSLATE_GOLD_ORE = Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState();
		protected static final BlockState REDSTONE_ORE = Blocks.REDSTONE_ORE.defaultBlockState();
		protected static final BlockState DEEPSLATE_REDSTONE_ORE = Blocks.DEEPSLATE_REDSTONE_ORE.defaultBlockState();
		protected static final BlockState DIAMOND_ORE = Blocks.DIAMOND_ORE.defaultBlockState();
		protected static final BlockState DEEPSLATE_DIAMOND_ORE = Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState();
		protected static final BlockState LAPIS_ORE = Blocks.LAPIS_ORE.defaultBlockState();
		protected static final BlockState DEEPSLATE_LAPIS_ORE = Blocks.DEEPSLATE_LAPIS_ORE.defaultBlockState();
		protected static final BlockState STONE = Blocks.STONE.defaultBlockState();
		protected static final BlockState EMERALD_ORE = Blocks.EMERALD_ORE.defaultBlockState();
		protected static final BlockState DEEPSLATE_EMERALD_ORE = Blocks.DEEPSLATE_EMERALD_ORE.defaultBlockState();
		protected static final BlockState INFESTED_STONE = Blocks.INFESTED_STONE.defaultBlockState();
		protected static final BlockState INFESTED_DEEPSLATE = Blocks.INFESTED_DEEPSLATE.defaultBlockState();
		protected static final BlockState SAND = Blocks.SAND.defaultBlockState();
		protected static final BlockState CLAY = Blocks.CLAY.defaultBlockState();
		protected static final BlockState MOSSY_COBBLESTONE = Blocks.MOSSY_COBBLESTONE.defaultBlockState();
		protected static final BlockState SEAGRASS = Blocks.SEAGRASS.defaultBlockState();
		protected static final BlockState MAGMA_BLOCK = Blocks.MAGMA_BLOCK.defaultBlockState();
		protected static final BlockState SOUL_SAND = Blocks.SOUL_SAND.defaultBlockState();
		protected static final BlockState NETHER_GOLD_ORE = Blocks.NETHER_GOLD_ORE.defaultBlockState();
		protected static final BlockState NETHER_QUARTZ_ORE = Blocks.NETHER_QUARTZ_ORE.defaultBlockState();
		protected static final BlockState BLACKSTONE = Blocks.BLACKSTONE.defaultBlockState();
		protected static final BlockState ANCIENT_DEBRIS = Blocks.ANCIENT_DEBRIS.defaultBlockState();
		protected static final BlockState BASALT = Blocks.BASALT.defaultBlockState();
		protected static final BlockState CRIMSON_FUNGUS = Blocks.CRIMSON_FUNGUS.defaultBlockState();
		protected static final BlockState WARPED_FUNGUS = Blocks.WARPED_FUNGUS.defaultBlockState();
		protected static final BlockState WARPED_ROOTS = Blocks.WARPED_ROOTS.defaultBlockState();
		protected static final BlockState NETHER_SPROUTS = Blocks.NETHER_SPROUTS.defaultBlockState();
		protected static final BlockState AIR = Blocks.AIR.defaultBlockState();
		protected static final BlockState AMETHYST_BLOCK = Blocks.AMETHYST_BLOCK.defaultBlockState();
		protected static final BlockState BUDDING_AMETHYST = Blocks.BUDDING_AMETHYST.defaultBlockState();
		protected static final BlockState CALCITE = Blocks.CALCITE.defaultBlockState();
		protected static final BlockState SMOOTH_BASALT = Blocks.SMOOTH_BASALT.defaultBlockState();
		protected static final BlockState TUFF = Blocks.TUFF.defaultBlockState();
		protected static final BlockState SPORE_BLOSSOM = Blocks.SPORE_BLOSSOM.defaultBlockState();
		protected static final BlockState SMALL_DRIPLEAF_EAST = Blocks.SMALL_DRIPLEAF.defaultBlockState().setValue(SmallDripleafBlock.FACING, Direction.EAST);
		protected static final BlockState SMALL_DRIPLEAF_WEST = Blocks.SMALL_DRIPLEAF.defaultBlockState().setValue(SmallDripleafBlock.FACING, Direction.WEST);
		protected static final BlockState SMALL_DRIPLEAF_NORTH = Blocks.SMALL_DRIPLEAF.defaultBlockState().setValue(SmallDripleafBlock.FACING, Direction.NORTH);
		protected static final BlockState SMALL_DRIPLEAF_SOUTH = Blocks.SMALL_DRIPLEAF.defaultBlockState().setValue(SmallDripleafBlock.FACING, Direction.SOUTH);
		protected static final BlockState BIG_DRIPLEAF_EAST = Blocks.BIG_DRIPLEAF.defaultBlockState().setValue(BigDripleafBlock.FACING, Direction.EAST);
		protected static final BlockState BIG_DRIPLEAF_WEST = Blocks.BIG_DRIPLEAF.defaultBlockState().setValue(BigDripleafBlock.FACING, Direction.WEST);
		protected static final BlockState BIG_DRIPLEAF_NORTH = Blocks.BIG_DRIPLEAF.defaultBlockState().setValue(BigDripleafBlock.FACING, Direction.NORTH);
		protected static final BlockState BIG_DRIPLEAF_SOUTH = Blocks.BIG_DRIPLEAF.defaultBlockState().setValue(BigDripleafBlock.FACING, Direction.SOUTH);
		protected static final BlockState DEEPSLATE = Blocks.DEEPSLATE.defaultBlockState();
		protected static final BlockState AZALEA_LEAVES = Blocks.AZALEA_LEAVES.defaultBlockState();
		protected static final BlockState FLOWERING_AZALEA_LEAVES = Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState();
		protected static final BlockState AZALEA = Blocks.AZALEA.defaultBlockState();
	}
}
