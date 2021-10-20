package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.BiasedToBottomInt;
import net.minecraft.util.valueproviders.ClampedInt;
import net.minecraft.util.valueproviders.ClampedNormalFloat;
import net.minecraft.util.valueproviders.ClampedNormalInt;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.util.valueproviders.WeightedListInt;
import net.minecraft.world.level.block.Block;
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
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FossilFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.HugeFungusConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;
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
import net.minecraft.world.level.levelgen.feature.configurations.HeightmapConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.LargeDripstoneConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoiseDependantDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.PointedDripstoneConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceSphereConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RootSystemConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ScatterDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;
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
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.DualNoiseProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.NoiseProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.NoiseThresholdProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RandomizedIntStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RotatedBlockProvider;
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
import net.minecraft.world.level.levelgen.placement.BlockFilterConfiguration;
import net.minecraft.world.level.levelgen.placement.CarvingMaskDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.CaveDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.ChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.ConfiguredDecorator;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.placement.FrequencyWithExtraChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.NoiseCountFactorDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.SurfaceRelativeThresholdConfiguration;
import net.minecraft.world.level.levelgen.placement.WaterDepthThresholdConfiguration;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.Fluids;

public class Features {
	public static final BlockPredicate ONLY_IN_AIR_PREDICATE = BlockPredicate.matchesBlock(Blocks.AIR, BlockPos.ZERO);
	public static final BlockPredicate ONLY_IN_AIR_OR_WATER_PREDICATE = BlockPredicate.matchesBlocks(List.of(Blocks.AIR, Blocks.WATER), BlockPos.ZERO);
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
			.configured(new DeltaFeatureConfiguration(Blocks.LAVA.defaultBlockState(), Blocks.MAGMA_BLOCK.defaultBlockState(), UniformInt.of(3, 7), UniformInt.of(0, 2)))
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
			.configured(new ReplaceSphereConfiguration(Blocks.NETHERRACK.defaultBlockState(), Blocks.BASALT.defaultBlockState(), UniformInt.of(3, 7)))
			.range(Features.Decorators.FULL_RANGE)
			.squared()
			.count(75)
	);
	public static final ConfiguredFeature<?, ?> BLACKSTONE_BLOBS = register(
		"blackstone_blobs",
		Feature.REPLACE_BLOBS
			.configured(new ReplaceSphereConfiguration(Blocks.NETHERRACK.defaultBlockState(), Blocks.BLACKSTONE.defaultBlockState(), UniformInt.of(3, 7)))
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
			.configured(Features.Configs.CRIMSON_FOREST)
			.decorated(FeatureDecorator.COUNT_MULTILAYER.configured(new CountConfiguration(6)))
	);
	public static final ConfiguredFeature<?, ?> WARPED_FOREST_VEGETATION = register(
		"warped_forest_vegetation",
		Feature.NETHER_FOREST_VEGETATION
			.configured(Features.Configs.WARPED_FOREST)
			.decorated(FeatureDecorator.COUNT_MULTILAYER.configured(new CountConfiguration(5)))
	);
	public static final ConfiguredFeature<?, ?> NETHER_SPROUTS = register(
		"nether_sprouts",
		Feature.NETHER_FOREST_VEGETATION
			.configured(Features.Configs.NETHER_SPROUTS)
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
					Blocks.PACKED_ICE.defaultBlockState(),
					UniformInt.of(2, 3),
					1,
					ImmutableList.of(
						Blocks.DIRT.defaultBlockState(),
						Blocks.GRASS_BLOCK.defaultBlockState(),
						Blocks.PODZOL.defaultBlockState(),
						Blocks.COARSE_DIRT.defaultBlockState(),
						Blocks.MYCELIUM.defaultBlockState(),
						Blocks.SNOW_BLOCK.defaultBlockState(),
						Blocks.ICE.defaultBlockState()
					)
				)
			)
			.decorated(Features.Decorators.HEIGHTMAP_SQUARE)
			.count(2)
	);
	public static final ConfiguredFeature<?, ?> FOREST_ROCK = register(
		"forest_rock",
		Feature.FOREST_ROCK
			.configured(new BlockStateConfiguration(Blocks.MOSSY_COBBLESTONE.defaultBlockState()))
			.decorated(Features.Decorators.HEIGHTMAP_SQUARE)
			.countRandom(2)
	);
	public static final ConfiguredFeature<?, ?> SEAGRASS_SIMPLE = register(
		"seagrass_simple",
		Feature.SIMPLE_BLOCK
			.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.SEAGRASS)))
			.decorated(
				FeatureDecorator.BLOCK_FILTER
					.configured(
						new BlockFilterConfiguration(
							BlockPredicate.allOf(
								BlockPredicate.matchesBlock(Blocks.STONE, new BlockPos(0, -1, 0)),
								BlockPredicate.matchesBlock(Blocks.WATER, BlockPos.ZERO),
								BlockPredicate.matchesBlock(Blocks.WATER, new BlockPos(0, 1, 0))
							)
						)
					)
			)
			.rarity(10)
			.decorated(FeatureDecorator.CARVING_MASK.configured(new CarvingMaskDecoratorConfiguration(GenerationStep.Carving.LIQUID)))
	);
	public static final ConfiguredFeature<?, ?> ICEBERG_PACKED = register(
		"iceberg_packed",
		Feature.ICEBERG
			.configured(new BlockStateConfiguration(Blocks.PACKED_ICE.defaultBlockState()))
			.decorated(FeatureDecorator.ICEBERG.configured(NoneDecoratorConfiguration.INSTANCE))
			.rarity(16)
	);
	public static final ConfiguredFeature<?, ?> ICEBERG_BLUE = register(
		"iceberg_blue",
		Feature.ICEBERG
			.configured(new BlockStateConfiguration(Blocks.BLUE_ICE.defaultBlockState()))
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
		"bamboo_light", Feature.BAMBOO.configured(new ProbabilityFeatureConfiguration(0.0F)).decorated(Features.Decorators.HEIGHTMAP_SQUARE).rarity(4)
	);
	public static final ConfiguredFeature<?, ?> BAMBOO = register(
		"bamboo",
		Feature.BAMBOO
			.configured(new ProbabilityFeatureConfiguration(0.2F))
			.decorated(Features.Decorators.HEIGHTMAP_WORLD_SURFACE)
			.squared()
			.decorated(FeatureDecorator.COUNT_NOISE_BIASED.configured(new NoiseCountFactorDecoratorConfiguration(160, 80.0, 0.3)))
	);
	public static final ConfiguredFeature<?, ?> VINES = register(
		"vines", Feature.VINES.configured(FeatureConfiguration.NONE).rangeUniform(VerticalAnchor.absolute(64), VerticalAnchor.absolute(100)).squared().count(127)
	);
	public static final ConfiguredFeature<?, ?> LAKE_LAVA = register(
		"lake_lava",
		Feature.LAKE
			.configured(new BlockStateConfiguration(Blocks.LAVA.defaultBlockState()))
			.decorated(FeatureDecorator.LAVA_LAKE.configured(new ChanceDecoratorConfiguration(80)))
			.range(new RangeDecoratorConfiguration(BiasedToBottomHeight.of(VerticalAnchor.bottom(), VerticalAnchor.top(), 8)))
			.squared()
			.rarity(8)
	);
	public static final ConfiguredFeature<?, ?> DISK_CLAY = register(
		"disk_clay",
		Feature.DISK
			.configured(
				new DiskConfiguration(
					Blocks.CLAY.defaultBlockState(), UniformInt.of(2, 3), 1, ImmutableList.of(Blocks.DIRT.defaultBlockState(), Blocks.CLAY.defaultBlockState())
				)
			)
			.decorated(Features.Decorators.TOP_SOLID_HEIGHTMAP_SQUARE)
	);
	public static final ConfiguredFeature<?, ?> DISK_GRAVEL = register(
		"disk_gravel",
		Feature.DISK
			.configured(
				new DiskConfiguration(
					Blocks.GRAVEL.defaultBlockState(), UniformInt.of(2, 5), 2, ImmutableList.of(Blocks.DIRT.defaultBlockState(), Blocks.GRASS_BLOCK.defaultBlockState())
				)
			)
			.decorated(Features.Decorators.TOP_SOLID_HEIGHTMAP_SQUARE)
	);
	public static final ConfiguredFeature<?, ?> DISK_SAND = register(
		"disk_sand",
		Feature.DISK
			.configured(
				new DiskConfiguration(
					Blocks.SAND.defaultBlockState(), UniformInt.of(2, 6), 2, ImmutableList.of(Blocks.DIRT.defaultBlockState(), Blocks.GRASS_BLOCK.defaultBlockState())
				)
			)
			.decorated(Features.Decorators.TOP_SOLID_HEIGHTMAP_SQUARE)
			.count(3)
	);
	public static final ConfiguredFeature<?, ?> FREEZE_TOP_LAYER = register("freeze_top_layer", Feature.FREEZE_TOP_LAYER.configured(FeatureConfiguration.NONE));
	public static final ConfiguredFeature<?, ?> BONUS_CHEST = register("bonus_chest", Feature.BONUS_CHEST.configured(FeatureConfiguration.NONE));
	public static final ConfiguredFeature<?, ?> VOID_START_PLATFORM = register(
		"void_start_platform", Feature.VOID_START_PLATFORM.configured(FeatureConfiguration.NONE)
	);
	public static final ConfiguredFeature<?, ?> MONSTER_ROOM = register(
		"monster_room",
		Feature.MONSTER_ROOM
			.configured(FeatureConfiguration.NONE)
			.range(new RangeDecoratorConfiguration(UniformHeight.of(VerticalAnchor.absolute(0), VerticalAnchor.top())))
			.squared()
			.count(10)
	);
	public static final ConfiguredFeature<?, ?> MONSTER_ROOM_DEEP = register(
		"monster_room_deep",
		Feature.MONSTER_ROOM
			.configured(FeatureConfiguration.NONE)
			.range(new RangeDecoratorConfiguration(UniformHeight.of(VerticalAnchor.aboveBottom(6), VerticalAnchor.absolute(-1))))
			.squared()
			.count(4)
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
	public static final ConfiguredFeature<?, ?> FOSSIL_UPPER = register(
		"fossil_upper",
		Feature.FOSSIL
			.configured(new FossilFeatureConfiguration(FOSSIL_STRUCTURES, FOSSIL_COAL_STRUCTURES, ProcessorLists.FOSSIL_ROT, ProcessorLists.FOSSIL_COAL, 4))
			.rangeUniform(VerticalAnchor.absolute(0), VerticalAnchor.top())
			.rarity(64)
	);
	public static final ConfiguredFeature<?, ?> FOSSIL_LOWER = register(
		"fossil_lower",
		Feature.FOSSIL
			.configured(new FossilFeatureConfiguration(FOSSIL_STRUCTURES, FOSSIL_COAL_STRUCTURES, ProcessorLists.FOSSIL_ROT, ProcessorLists.FOSSIL_DIAMONDS, 4))
			.rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(-8))
			.rarity(64)
	);
	public static final ConfiguredFeature<?, ?> SPRING_LAVA_DOUBLE = register(
		"spring_lava_double",
		Feature.SPRING
			.configured(Features.Configs.LAVA_SPRING)
			.range(new RangeDecoratorConfiguration(VeryBiasedToBottomHeight.of(VerticalAnchor.bottom(), VerticalAnchor.belowTop(8), 8)))
			.squared()
			.count(40)
	);
	public static final ConfiguredFeature<?, ?> SPRING_LAVA = register(
		"spring_lava",
		Feature.SPRING
			.configured(Features.Configs.LAVA_SPRING)
			.range(new RangeDecoratorConfiguration(VeryBiasedToBottomHeight.of(VerticalAnchor.bottom(), VerticalAnchor.belowTop(8), 8)))
			.squared()
			.count(20)
	);
	public static final ConfiguredFeature<?, ?> SPRING_DELTA = register(
		"spring_delta",
		Feature.SPRING
			.configured(
				new SpringConfiguration(
					Fluids.LAVA.defaultFluidState(), true, 4, 1, ImmutableSet.of(Blocks.NETHERRACK, Blocks.SOUL_SAND, Blocks.GRAVEL, Blocks.MAGMA_BLOCK, Blocks.BLACKSTONE)
				)
			)
			.range(Features.Decorators.RANGE_4_4)
			.squared()
			.count(16)
	);
	public static final ConfiguredFeature<?, ?> SPRING_CLOSED = register(
		"spring_closed", Feature.SPRING.configured(Features.Configs.CLOSED_NETHER_SPRING).range(Features.Decorators.RANGE_10_10).squared().count(16)
	);
	public static final ConfiguredFeature<?, ?> SPRING_CLOSED_DOUBLE = register(
		"spring_closed_double", Feature.SPRING.configured(Features.Configs.CLOSED_NETHER_SPRING).range(Features.Decorators.RANGE_10_10).squared().count(32)
	);
	public static final ConfiguredFeature<?, ?> SPRING_OPEN = register(
		"spring_open",
		Feature.SPRING
			.configured(new SpringConfiguration(Fluids.LAVA.defaultFluidState(), false, 4, 1, ImmutableSet.of(Blocks.NETHERRACK)))
			.range(Features.Decorators.RANGE_4_4)
			.squared()
			.count(8)
	);
	public static final ConfiguredFeature<?, ?> SPRING_WATER = register(
		"spring_water",
		Feature.SPRING
			.configured(
				new SpringConfiguration(
					Fluids.WATER.defaultFluidState(),
					true,
					4,
					1,
					ImmutableSet.of(
						Blocks.STONE,
						Blocks.GRANITE,
						Blocks.DIORITE,
						Blocks.ANDESITE,
						Blocks.DEEPSLATE,
						Blocks.TUFF,
						Blocks.CALCITE,
						Blocks.DIRT,
						Blocks.SNOW_BLOCK,
						Blocks.POWDER_SNOW,
						Blocks.PACKED_ICE
					)
				)
			)
			.rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(192))
			.squared()
			.count(25)
	);
	public static final ConfiguredFeature<?, ?> PILE_HAY = register(
		"pile_hay", Feature.BLOCK_PILE.configured(new BlockPileConfiguration(new RotatedBlockProvider(Blocks.HAY_BLOCK)))
	);
	public static final ConfiguredFeature<?, ?> PILE_MELON = register(
		"pile_melon", Feature.BLOCK_PILE.configured(new BlockPileConfiguration(BlockStateProvider.simple(Blocks.MELON)))
	);
	public static final ConfiguredFeature<?, ?> PILE_SNOW = register(
		"pile_snow", Feature.BLOCK_PILE.configured(new BlockPileConfiguration(BlockStateProvider.simple(Blocks.SNOW)))
	);
	public static final ConfiguredFeature<?, ?> PILE_ICE = register(
		"pile_ice",
		Feature.BLOCK_PILE
			.configured(
				new BlockPileConfiguration(
					new WeightedStateProvider(weightedBlockStateBuilder().add(Blocks.BLUE_ICE.defaultBlockState(), 1).add(Blocks.PACKED_ICE.defaultBlockState(), 5))
				)
			)
	);
	public static final ConfiguredFeature<?, ?> PILE_PUMPKIN = register(
		"pile_pumpkin",
		Feature.BLOCK_PILE
			.configured(
				new BlockPileConfiguration(
					new WeightedStateProvider(weightedBlockStateBuilder().add(Blocks.PUMPKIN.defaultBlockState(), 19).add(Blocks.JACK_O_LANTERN.defaultBlockState(), 1))
				)
			)
	);
	public static final ConfiguredFeature<?, ?> PATCH_FIRE = register(
		"patch_fire",
		Feature.RANDOM_PATCH
			.configured(
				simplePatchConfiguration(Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.FIRE))), List.of(Blocks.NETHERRACK))
			)
			.decorated(Features.Decorators.FIRE)
	);
	public static final ConfiguredFeature<?, ?> PATCH_SOUL_FIRE = register(
		"patch_soul_fire",
		Feature.RANDOM_PATCH
			.configured(
				simplePatchConfiguration(
					Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.SOUL_FIRE))), List.of(Blocks.SOUL_SOIL)
				)
			)
			.decorated(Features.Decorators.FIRE)
	);
	public static final ConfiguredFeature<?, ?> PATCH_BROWN_MUSHROOM = register(
		"patch_brown_mushroom",
		Feature.RANDOM_PATCH
			.configured(simplePatchConfiguration(Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.BROWN_MUSHROOM)))))
	);
	public static final ConfiguredFeature<?, ?> PATCH_RED_MUSHROOM = register(
		"patch_red_mushroom",
		Feature.RANDOM_PATCH
			.configured(simplePatchConfiguration(Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.RED_MUSHROOM)))))
	);
	public static final ConfiguredFeature<?, ?> PATCH_CRIMSON_ROOTS = register(
		"patch_crimson_roots",
		Feature.RANDOM_PATCH
			.configured(simplePatchConfiguration(Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.CRIMSON_ROOTS)))))
			.range(Features.Decorators.FULL_RANGE)
	);
	public static final ConfiguredFeature<?, ?> PATCH_SUNFLOWER = register(
		"patch_sunflower",
		Feature.RANDOM_PATCH
			.configured(simplePatchConfiguration(Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.SUNFLOWER)))))
			.decorated(Features.Decorators.HEIGHTMAP_SQUARE)
			.rarity(3)
	);
	public static final ConfiguredFeature<?, ?> PATCH_PUMPKIN = register(
		"patch_pumpkin",
		Feature.RANDOM_PATCH
			.configured(
				simplePatchConfiguration(
					Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.PUMPKIN))), List.of(Blocks.GRASS_BLOCK)
				)
			)
			.decorated(Features.Decorators.HEIGHTMAP_SQUARE)
			.rarity(2048)
	);
	public static final ConfiguredFeature<?, ?> PATCH_TAIGA_GRASS = register("patch_taiga_grass", Feature.RANDOM_PATCH.configured(Features.Configs.TAIGA_GRASS));
	public static final ConfiguredFeature<?, ?> PATCH_BERRY_BUSH = register("patch_berry_bush", Feature.RANDOM_PATCH.configured(Features.Configs.SWEET_BERRY_BUSH));
	public static final ConfiguredFeature<?, ?> PATCH_GRASS_PLAIN = register(
		"patch_grass_plain",
		Feature.RANDOM_PATCH
			.configured(Features.Configs.DEFAULT_GRASS)
			.decorated(Features.Decorators.HEIGHTMAP_WORLD_SURFACE)
			.squared()
			.decorated(FeatureDecorator.COUNT_NOISE.configured(new NoiseDependantDecoratorConfiguration(-0.8, 5, 10)))
	);
	public static final ConfiguredFeature<?, ?> PATCH_GRASS_FOREST = register(
		"patch_grass_forest",
		Feature.RANDOM_PATCH.configured(Features.Configs.DEFAULT_GRASS).decorated(Features.Decorators.HEIGHTMAP_WORLD_SURFACE).squared().count(2)
	);
	public static final ConfiguredFeature<?, ?> PATCH_GRASS_BADLANDS = register(
		"patch_grass_badlands", Feature.RANDOM_PATCH.configured(Features.Configs.DEFAULT_GRASS).decorated(Features.Decorators.HEIGHTMAP_WORLD_SURFACE).squared()
	);
	public static final ConfiguredFeature<?, ?> PATCH_GRASS_SAVANNA = register(
		"patch_grass_savanna",
		Feature.RANDOM_PATCH.configured(Features.Configs.DEFAULT_GRASS).decorated(Features.Decorators.HEIGHTMAP_WORLD_SURFACE).squared().count(20)
	);
	public static final ConfiguredFeature<?, ?> PATCH_GRASS_NORMAL = register(
		"patch_grass_normal",
		Feature.RANDOM_PATCH.configured(Features.Configs.DEFAULT_GRASS).decorated(Features.Decorators.HEIGHTMAP_WORLD_SURFACE).squared().count(5)
	);
	public static final ConfiguredFeature<?, ?> PATCH_GRASS_TAIGA_2 = register(
		"patch_grass_taiga_2", Feature.RANDOM_PATCH.configured(Features.Configs.TAIGA_GRASS).decorated(Features.Decorators.HEIGHTMAP_WORLD_SURFACE).squared()
	);
	public static final ConfiguredFeature<?, ?> PATCH_GRASS_TAIGA = register(
		"patch_grass_taiga", Feature.RANDOM_PATCH.configured(Features.Configs.TAIGA_GRASS).decorated(Features.Decorators.HEIGHTMAP_WORLD_SURFACE).squared().count(7)
	);
	public static final ConfiguredFeature<?, ?> PATCH_GRASS_JUNGLE = register(
		"patch_grass_jungle",
		Feature.RANDOM_PATCH.configured(Features.Configs.JUNGLE_GRASS).decorated(Features.Decorators.HEIGHTMAP_WORLD_SURFACE).squared().count(25)
	);
	public static final ConfiguredFeature<?, ?> PATCH_DEAD_BUSH_2 = register(
		"patch_dead_bush_2", Feature.RANDOM_PATCH.configured(Features.Configs.DEAD_BUSH).decorated(Features.Decorators.HEIGHTMAP_WORLD_SURFACE).squared().count(2)
	);
	public static final ConfiguredFeature<?, ?> PATCH_DEAD_BUSH = register(
		"patch_dead_bush", Feature.RANDOM_PATCH.configured(Features.Configs.DEAD_BUSH).decorated(Features.Decorators.HEIGHTMAP_WORLD_SURFACE).squared()
	);
	public static final ConfiguredFeature<?, ?> PATCH_DEAD_BUSH_BADLANDS = register(
		"patch_dead_bush_badlands",
		Feature.RANDOM_PATCH.configured(Features.Configs.DEAD_BUSH).decorated(Features.Decorators.HEIGHTMAP_WORLD_SURFACE).squared().count(20)
	);
	public static final ConfiguredFeature<?, ?> PATCH_MELON = register(
		"patch_melon",
		Feature.RANDOM_PATCH
			.configured(
				new RandomPatchConfiguration(
					64,
					7,
					3,
					() -> Feature.SIMPLE_BLOCK
							.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.MELON)))
							.filtered(BlockPredicate.allOf(BlockPredicate.replaceable(), BlockPredicate.matchesBlock(Blocks.GRASS_BLOCK, new BlockPos(0, -1, 0))))
				)
			)
			.decorated(Features.Decorators.HEIGHTMAP_SQUARE)
			.rarity(64)
	);
	public static final ConfiguredFeature<?, ?> PATCH_BERRY_COMMON = register(
		"patch_berry_common", PATCH_BERRY_BUSH.decorated(Features.Decorators.HEIGHTMAP_WORLD_SURFACE).squared().rarity(128)
	);
	public static final ConfiguredFeature<?, ?> PATCH_BERRY_RARE = register(
		"patch_berry_rare", PATCH_BERRY_BUSH.decorated(Features.Decorators.HEIGHTMAP_WORLD_SURFACE).squared().rarity(1536)
	);
	public static final ConfiguredFeature<?, ?> PATCH_WATERLILLY = register(
		"patch_waterlilly",
		Feature.RANDOM_PATCH
			.configured(
				new RandomPatchConfiguration(
					10, 7, 3, () -> Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.LILY_PAD))).onlyWhenEmpty()
				)
			)
			.decorated(Features.Decorators.HEIGHTMAP_WORLD_SURFACE)
			.squared()
			.count(4)
	);
	public static final ConfiguredFeature<?, ?> PATCH_TALL_GRASS_2 = register(
		"patch_tall_grass_2",
		Feature.RANDOM_PATCH
			.configured(Features.Configs.TALL_GRASS)
			.decorated(Features.Decorators.HEIGHTMAP)
			.squared()
			.rarity(32)
			.decorated(FeatureDecorator.COUNT_NOISE.configured(new NoiseDependantDecoratorConfiguration(-0.8, 0, 7)))
	);
	public static final ConfiguredFeature<?, ?> PATCH_TALL_GRASS = register(
		"patch_tall_grass", Feature.RANDOM_PATCH.configured(Features.Configs.TALL_GRASS).decorated(Features.Decorators.HEIGHTMAP_SQUARE).rarity(5)
	);
	public static final ConfiguredFeature<?, ?> PATCH_LARGE_FERN = register(
		"patch_large_fern",
		Feature.RANDOM_PATCH
			.configured(simplePatchConfiguration(Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.LARGE_FERN)))))
			.decorated(Features.Decorators.HEIGHTMAP_SQUARE)
			.rarity(5)
	);
	public static final ConfiguredFeature<?, ?> PATCH_CACTUS = register(
		"patch_cactus",
		Feature.RANDOM_PATCH
			.configured(
				simplePatchConfiguration(
					Feature.BLOCK_COLUMN
						.configured(BlockColumnConfiguration.simple(BiasedToBottomInt.of(1, 3), BlockStateProvider.simple(Blocks.CACTUS)))
						.filteredByBlockSurvival(Blocks.CACTUS),
					List.of(),
					10
				)
			)
	);
	public static final ConfiguredFeature<?, ?> PATCH_CACTUS_DESERT = register(
		"patch_cactus_desert", PATCH_CACTUS.decorated(Features.Decorators.HEIGHTMAP_SQUARE).rarity(6)
	);
	public static final ConfiguredFeature<?, ?> PATCH_CACTUS_DECORATED = register(
		"patch_cactus_decorated", PATCH_CACTUS.decorated(Features.Decorators.HEIGHTMAP_SQUARE).rarity(13)
	);
	public static final ConfiguredFeature<?, ?> PATCH_SUGAR_CANE_SWAMP = register(
		"patch_sugar_cane_swamp", Feature.RANDOM_PATCH.configured(Features.Configs.SUGAR_CANE).decorated(Features.Decorators.HEIGHTMAP_SQUARE).rarity(3)
	);
	public static final ConfiguredFeature<?, ?> PATCH_SUGAR_CANE_DESERT = register(
		"patch_sugar_cane_desert", Feature.RANDOM_PATCH.configured(Features.Configs.SUGAR_CANE).decorated(Features.Decorators.HEIGHTMAP_SQUARE)
	);
	public static final ConfiguredFeature<?, ?> PATCH_SUGAR_CANE_BADLANDS = register(
		"patch_sugar_cane_badlands", Feature.RANDOM_PATCH.configured(Features.Configs.SUGAR_CANE).decorated(Features.Decorators.HEIGHTMAP_SQUARE).rarity(5)
	);
	public static final ConfiguredFeature<?, ?> PATCH_SUGAR_CANE = register(
		"patch_sugar_cane", Feature.RANDOM_PATCH.configured(Features.Configs.SUGAR_CANE).decorated(Features.Decorators.HEIGHTMAP_SQUARE).rarity(6)
	);
	public static final ConfiguredFeature<?, ?> BROWN_MUSHROOM_NETHER = register(
		"brown_mushroom_nether", PATCH_BROWN_MUSHROOM.range(Features.Decorators.FULL_RANGE).rarity(2)
	);
	public static final ConfiguredFeature<?, ?> RED_MUSHROOM_NETHER = register(
		"red_mushroom_nether", PATCH_RED_MUSHROOM.range(Features.Decorators.FULL_RANGE).rarity(2)
	);
	public static final ConfiguredFeature<?, ?> BROWN_MUSHROOM_NORMAL = register(
		"brown_mushroom_normal", PATCH_BROWN_MUSHROOM.decorated(Features.Decorators.HEIGHTMAP_SQUARE).rarity(256)
	);
	public static final ConfiguredFeature<?, ?> RED_MUSHROOM_NORMAL = register(
		"red_mushroom_normal", PATCH_RED_MUSHROOM.decorated(Features.Decorators.HEIGHTMAP_SQUARE).rarity(512)
	);
	public static final ConfiguredFeature<?, ?> BROWN_MUSHROOM_TAIGA = register(
		"brown_mushroom_taiga", PATCH_BROWN_MUSHROOM.rarity(4).decorated(Features.Decorators.HEIGHTMAP_SQUARE)
	);
	public static final ConfiguredFeature<?, ?> RED_MUSHROOM_TAIGA = register(
		"red_mushroom_taiga", PATCH_RED_MUSHROOM.decorated(Features.Decorators.HEIGHTMAP_SQUARE).rarity(512)
	);
	public static final ConfiguredFeature<?, ?> BROWN_MUSHROOM_GIANT = register("brown_mushroom_giant", BROWN_MUSHROOM_TAIGA.count(3));
	public static final ConfiguredFeature<?, ?> RED_MUSHROOM_GIANT = register("red_mushroom_giant", RED_MUSHROOM_TAIGA.count(3));
	public static final ConfiguredFeature<?, ?> BROWN_MUSHROOM_SWAMP = register("brown_mushroom_swamp", BROWN_MUSHROOM_TAIGA.count(8));
	public static final ConfiguredFeature<?, ?> RED_MUSHROOM_SWAMP = register("red_mushroom_swamp", RED_MUSHROOM_TAIGA.count(8));
	public static final ImmutableList<OreConfiguration.TargetBlockState> ORE_IRON_TARGET_LIST = ImmutableList.of(
		OreConfiguration.target(OreConfiguration.Predicates.STONE_ORE_REPLACEABLES, Blocks.IRON_ORE.defaultBlockState()),
		OreConfiguration.target(OreConfiguration.Predicates.DEEPSLATE_ORE_REPLACEABLES, Blocks.DEEPSLATE_IRON_ORE.defaultBlockState())
	);
	public static final ImmutableList<OreConfiguration.TargetBlockState> ORE_REDSTONE_TARGET_LIST = ImmutableList.of(
		OreConfiguration.target(OreConfiguration.Predicates.STONE_ORE_REPLACEABLES, Blocks.REDSTONE_ORE.defaultBlockState()),
		OreConfiguration.target(OreConfiguration.Predicates.DEEPSLATE_ORE_REPLACEABLES, Blocks.DEEPSLATE_REDSTONE_ORE.defaultBlockState())
	);
	public static final ImmutableList<OreConfiguration.TargetBlockState> ORE_GOLD_TARGET_LIST = ImmutableList.of(
		OreConfiguration.target(OreConfiguration.Predicates.STONE_ORE_REPLACEABLES, Blocks.GOLD_ORE.defaultBlockState()),
		OreConfiguration.target(OreConfiguration.Predicates.DEEPSLATE_ORE_REPLACEABLES, Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState())
	);
	public static final ImmutableList<OreConfiguration.TargetBlockState> ORE_DIAMOND_TARGET_LIST = ImmutableList.of(
		OreConfiguration.target(OreConfiguration.Predicates.STONE_ORE_REPLACEABLES, Blocks.DIAMOND_ORE.defaultBlockState()),
		OreConfiguration.target(OreConfiguration.Predicates.DEEPSLATE_ORE_REPLACEABLES, Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState())
	);
	public static final ImmutableList<OreConfiguration.TargetBlockState> ORE_LAPIS_TARGET_LIST = ImmutableList.of(
		OreConfiguration.target(OreConfiguration.Predicates.STONE_ORE_REPLACEABLES, Blocks.LAPIS_ORE.defaultBlockState()),
		OreConfiguration.target(OreConfiguration.Predicates.DEEPSLATE_ORE_REPLACEABLES, Blocks.DEEPSLATE_LAPIS_ORE.defaultBlockState())
	);
	public static final ImmutableList<OreConfiguration.TargetBlockState> ORE_EMERALD_TARGET_LIST = ImmutableList.of(
		OreConfiguration.target(OreConfiguration.Predicates.STONE_ORE_REPLACEABLES, Blocks.EMERALD_ORE.defaultBlockState()),
		OreConfiguration.target(OreConfiguration.Predicates.DEEPSLATE_ORE_REPLACEABLES, Blocks.DEEPSLATE_EMERALD_ORE.defaultBlockState())
	);
	public static final ImmutableList<OreConfiguration.TargetBlockState> ORE_COPPER_TARGET_LIST = ImmutableList.of(
		OreConfiguration.target(OreConfiguration.Predicates.STONE_ORE_REPLACEABLES, Blocks.COPPER_ORE.defaultBlockState()),
		OreConfiguration.target(OreConfiguration.Predicates.DEEPSLATE_ORE_REPLACEABLES, Blocks.DEEPSLATE_COPPER_ORE.defaultBlockState())
	);
	public static final ImmutableList<OreConfiguration.TargetBlockState> ORE_COAL_TARGET_LIST = ImmutableList.of(
		OreConfiguration.target(OreConfiguration.Predicates.STONE_ORE_REPLACEABLES, Blocks.COAL_ORE.defaultBlockState()),
		OreConfiguration.target(OreConfiguration.Predicates.DEEPSLATE_ORE_REPLACEABLES, Blocks.DEEPSLATE_COAL_ORE.defaultBlockState())
	);
	public static final ImmutableList<OreConfiguration.TargetBlockState> ORE_INFESTED_TARGET_LIST = ImmutableList.of(
		OreConfiguration.target(OreConfiguration.Predicates.STONE_ORE_REPLACEABLES, Blocks.INFESTED_STONE.defaultBlockState()),
		OreConfiguration.target(OreConfiguration.Predicates.DEEPSLATE_ORE_REPLACEABLES, Blocks.INFESTED_DEEPSLATE.defaultBlockState())
	);
	public static final OreConfiguration ORE_IRON_CONFIG = new OreConfiguration(ORE_IRON_TARGET_LIST, 9);
	public static final OreConfiguration ORE_REDSTONE_CONFIG = new OreConfiguration(ORE_REDSTONE_TARGET_LIST, 8);
	public static final ConfiguredFeature<?, ?> ORE_MAGMA = register(
		"ore_magma",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NETHERRACK, Blocks.MAGMA_BLOCK.defaultBlockState(), 33))
			.rangeUniform(VerticalAnchor.absolute(27), VerticalAnchor.absolute(36))
			.squared()
			.count(4)
	);
	public static final ConfiguredFeature<?, ?> ORE_SOUL_SAND = register(
		"ore_soul_sand",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NETHERRACK, Blocks.SOUL_SAND.defaultBlockState(), 12))
			.rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(31))
			.squared()
			.count(12)
	);
	public static final ConfiguredFeature<?, ?> ORE_GOLD_DELTAS = register(
		"ore_gold_deltas",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NETHERRACK, Blocks.NETHER_GOLD_ORE.defaultBlockState(), 10))
			.range(Features.Decorators.RANGE_10_10)
			.squared()
			.count(20)
	);
	public static final ConfiguredFeature<?, ?> ORE_QUARTZ_DELTAS = register(
		"ore_quartz_deltas",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NETHERRACK, Blocks.NETHER_QUARTZ_ORE.defaultBlockState(), 14))
			.range(Features.Decorators.RANGE_10_10)
			.squared()
			.count(32)
	);
	public static final ConfiguredFeature<?, ?> ORE_GOLD_NETHER = register(
		"ore_gold_nether",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NETHERRACK, Blocks.NETHER_GOLD_ORE.defaultBlockState(), 10))
			.range(Features.Decorators.RANGE_10_10)
			.squared()
			.count(10)
	);
	public static final ConfiguredFeature<?, ?> ORE_QUARTZ_NETHER = register(
		"ore_quartz_nether",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NETHERRACK, Blocks.NETHER_QUARTZ_ORE.defaultBlockState(), 14))
			.range(Features.Decorators.RANGE_10_10)
			.squared()
			.count(16)
	);
	public static final ConfiguredFeature<?, ?> ORE_GRAVEL_NETHER = register(
		"ore_gravel_nether",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NETHERRACK, Blocks.GRAVEL.defaultBlockState(), 33))
			.rangeUniform(VerticalAnchor.absolute(5), VerticalAnchor.absolute(41))
			.squared()
			.count(2)
	);
	public static final ConfiguredFeature<?, ?> ORE_BLACKSTONE = register(
		"ore_blackstone",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NETHERRACK, Blocks.BLACKSTONE.defaultBlockState(), 33))
			.rangeUniform(VerticalAnchor.absolute(5), VerticalAnchor.absolute(31))
			.squared()
			.count(2)
	);
	public static final ConfiguredFeature<?, ?> ORE_DIRT = register(
		"ore_dirt",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.DIRT.defaultBlockState(), 33))
			.rangeUniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(160))
			.squared()
			.count(7)
	);
	public static final ConfiguredFeature<?, ?> ORE_GRAVEL = register(
		"ore_gravel",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.GRAVEL.defaultBlockState(), 33))
			.rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.top())
			.squared()
			.count(14)
	);
	public static final ConfiguredFeature<?, ?> ORE_GRANITE_UPPER = register(
		"ore_granite_upper",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.GRANITE.defaultBlockState(), 64))
			.rangeUniform(VerticalAnchor.absolute(64), VerticalAnchor.absolute(128))
			.squared()
			.rarity(6)
	);
	public static final ConfiguredFeature<?, ?> ORE_GRANITE_LOWER = register(
		"ore_granite_lower",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.GRANITE.defaultBlockState(), 64))
			.rangeUniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(60))
			.squared()
			.count(2)
	);
	public static final ConfiguredFeature<?, ?> ORE_DIORITE_UPPER = register(
		"ore_diorite_upper",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.DIORITE.defaultBlockState(), 64))
			.rangeUniform(VerticalAnchor.absolute(64), VerticalAnchor.absolute(128))
			.squared()
			.rarity(6)
	);
	public static final ConfiguredFeature<?, ?> ORE_DIORITE_LOWER = register(
		"ore_diorite_lower",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.DIORITE.defaultBlockState(), 64))
			.rangeUniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(60))
			.squared()
			.count(2)
	);
	public static final ConfiguredFeature<?, ?> ORE_ANDESITE_UPPER = register(
		"ore_andesite_upper",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.ANDESITE.defaultBlockState(), 64))
			.rangeUniform(VerticalAnchor.absolute(64), VerticalAnchor.absolute(128))
			.squared()
			.rarity(6)
	);
	public static final ConfiguredFeature<?, ?> ORE_ANDESITE_LOWER = register(
		"ore_andesite_lower",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.ANDESITE.defaultBlockState(), 64))
			.rangeUniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(60))
			.squared()
			.count(2)
	);
	public static final ConfiguredFeature<?, ?> ORE_TUFF = register(
		"ore_tuff",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.TUFF.defaultBlockState(), 64))
			.rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(0))
			.squared()
			.count(2)
	);
	public static final ConfiguredFeature<?, ?> ORE_COAL_UPPER = register(
		"ore_coal_upper",
		Feature.ORE.configured(new OreConfiguration(ORE_COAL_TARGET_LIST, 17)).rangeUniform(VerticalAnchor.absolute(136), VerticalAnchor.top()).squared().count(30)
	);
	public static final ConfiguredFeature<?, ?> ORE_COAL_LOWER = register(
		"ore_coal_lower",
		Feature.ORE
			.configured(new OreConfiguration(ORE_COAL_TARGET_LIST, 17, 0.5F))
			.rangeTriangle(VerticalAnchor.absolute(0), VerticalAnchor.absolute(192))
			.squared()
			.count(20)
	);
	public static final ConfiguredFeature<?, ?> ORE_IRON_UPPER = register(
		"ore_iron_upper", Feature.ORE.configured(ORE_IRON_CONFIG).rangeTriangle(VerticalAnchor.absolute(80), VerticalAnchor.absolute(384)).squared().count(90)
	);
	public static final ConfiguredFeature<?, ?> ORE_IRON_MIDDLE = register(
		"ore_iron_middle", Feature.ORE.configured(ORE_IRON_CONFIG).rangeTriangle(VerticalAnchor.absolute(-24), VerticalAnchor.absolute(56)).squared().count(10)
	);
	public static final ConfiguredFeature<?, ?> ORE_IRON_SMALL = register(
		"ore_iron_small",
		Feature.ORE.configured(new OreConfiguration(ORE_IRON_TARGET_LIST, 4)).rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(72)).squared().count(10)
	);
	public static final ConfiguredFeature<?, ?> ORE_GOLD_EXTRA = register(
		"ore_gold_extra",
		Feature.ORE
			.configured(new OreConfiguration(ORE_GOLD_TARGET_LIST, 9))
			.rangeUniform(VerticalAnchor.absolute(32), VerticalAnchor.absolute(256))
			.squared()
			.count(50)
	);
	public static final ConfiguredFeature<?, ?> ORE_GOLD = register(
		"ore_gold",
		Feature.ORE
			.configured(new OreConfiguration(ORE_GOLD_TARGET_LIST, 9, 0.5F))
			.rangeTriangle(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(32))
			.squared()
			.count(4)
	);
	public static final ConfiguredFeature<?, ?> ORE_GOLD_LOWER = register(
		"ore_gold_lower",
		Feature.ORE
			.configured(new OreConfiguration(ORE_GOLD_TARGET_LIST, 9, 0.5F))
			.rangeUniform(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(-48))
			.squared()
			.countRandom(1)
	);
	public static final ConfiguredFeature<?, ?> ORE_REDSTONE = register(
		"ore_redstone", Feature.ORE.configured(ORE_REDSTONE_CONFIG).rangeUniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(15)).squared().count(4)
	);
	public static final ConfiguredFeature<?, ?> ORE_REDSTONE_LOWER = register(
		"ore_redstone_lower",
		Feature.ORE.configured(ORE_REDSTONE_CONFIG).rangeTriangle(VerticalAnchor.aboveBottom(-32), VerticalAnchor.aboveBottom(32)).squared().count(8)
	);
	public static final ConfiguredFeature<?, ?> ORE_DIAMOND = register(
		"ore_diamond",
		Feature.ORE
			.configured(new OreConfiguration(ORE_DIAMOND_TARGET_LIST, 4, 0.5F))
			.rangeTriangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80))
			.squared()
			.count(7)
	);
	public static final ConfiguredFeature<?, ?> ORE_DIAMOND_LARGE = register(
		"ore_diamond_large",
		Feature.ORE
			.configured(new OreConfiguration(ORE_DIAMOND_TARGET_LIST, 12, 0.7F))
			.rangeTriangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80))
			.squared()
			.rarity(9)
	);
	public static final ConfiguredFeature<?, ?> ORE_DIAMOND_BURIED = register(
		"ore_diamond_buried",
		Feature.ORE
			.configured(new OreConfiguration(ORE_DIAMOND_TARGET_LIST, 8, 1.0F))
			.rangeTriangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80))
			.squared()
			.count(4)
	);
	public static final ConfiguredFeature<?, ?> ORE_LAPIS = register(
		"ore_lapis",
		Feature.ORE
			.configured(new OreConfiguration(ORE_LAPIS_TARGET_LIST, 7))
			.rangeTriangle(VerticalAnchor.absolute(-32), VerticalAnchor.absolute(32))
			.squared()
			.count(2)
	);
	public static final ConfiguredFeature<?, ?> ORE_LAPIS_BURIED = register(
		"ore_lapis_buried",
		Feature.ORE
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
			.count(14)
	);
	public static final ConfiguredFeature<?, ?> ORE_EMERALD = register(
		"ore_emerald",
		Feature.ORE
			.configured(new OreConfiguration(ORE_EMERALD_TARGET_LIST, 3))
			.rangeTriangle(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(480))
			.squared()
			.count(100)
	);
	public static final ConfiguredFeature<?, ?> ORE_DEBRIS_LARGE = register(
		"ore_debris_large",
		Feature.SCATTERED_ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NETHER_ORE_REPLACEABLES, Blocks.ANCIENT_DEBRIS.defaultBlockState(), 3, 1.0F))
			.rangeTriangle(VerticalAnchor.absolute(8), VerticalAnchor.absolute(24))
			.squared()
	);
	public static final ConfiguredFeature<?, ?> ORE_DEBRIS_SMALL = register(
		"ore_debris_small",
		Feature.SCATTERED_ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NETHER_ORE_REPLACEABLES, Blocks.ANCIENT_DEBRIS.defaultBlockState(), 2, 1.0F))
			.range(Features.Decorators.RANGE_8_8)
			.squared()
	);
	public static final ConfiguredFeature<?, ?> ORE_COPPER = register(
		"ore_copper",
		Feature.ORE
			.configured(new OreConfiguration(ORE_COPPER_TARGET_LIST, 10))
			.rangeTriangle(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(112))
			.squared()
			.count(16)
	);
	public static final ConfiguredFeature<?, ?> ORE_COPPER_LARGE = register(
		"ore_copper_large",
		Feature.ORE
			.configured(new OreConfiguration(ORE_COPPER_TARGET_LIST, 20))
			.rangeTriangle(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(112))
			.squared()
			.count(16)
	);
	public static final ConfiguredFeature<?, ?> ORE_CLAY = register(
		"ore_clay",
		Feature.ORE
			.configured(new OreConfiguration(OreConfiguration.Predicates.NATURAL_STONE, Blocks.CLAY.defaultBlockState(), 33))
			.range(Features.Decorators.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT)
			.squared()
			.count(38)
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
			.range(Features.Decorators.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT)
			.squared()
			.count(UniformInt.of(35, 70))
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
			.range(Features.Decorators.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT)
			.squared()
			.count(UniformInt.of(7, 35))
	);
	public static final ConfiguredFeature<?, ?> POINTED_DRIPSTONE_FEATURE = register(
		"pointed_dripstone",
		Feature.SIMPLE_RANDOM_SELECTOR
			.configured(
				new SimpleRandomFeatureConfiguration(
					ImmutableList.of(
						() -> Feature.POINTED_DRIPSTONE
								.configured(new PointedDripstoneConfiguration(0.2F, 0.7F, 0.5F, 0.5F))
								.decorated(FeatureDecorator.CAVE_SURFACE.configured(new CaveDecoratorConfiguration(CaveSurface.FLOOR, 12, true))),
						() -> Feature.POINTED_DRIPSTONE
								.configured(new PointedDripstoneConfiguration(0.2F, 0.7F, 0.5F, 0.5F))
								.decorated(FeatureDecorator.CAVE_SURFACE.configured(new CaveDecoratorConfiguration(CaveSurface.CEILING, 12, true)))
					)
				)
			)
			.decorated(
				FeatureDecorator.SCATTER.configured(new ScatterDecoratorConfiguration(ClampedNormalInt.of(0.0F, 3.0F, -10, 10), ClampedNormalInt.of(0.0F, 0.6F, -2, 2)))
			)
			.count(UniformInt.of(1, 5))
			.range(Features.Decorators.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT)
			.squared()
			.count(UniformInt.of(140, 220))
	);
	public static final ConfiguredFeature<?, ?> UNDERWATER_MAGMA = register(
		"underwater_magma",
		Feature.UNDERWATER_MAGMA
			.configured(new UnderwaterMagmaConfiguration(5, 1, 0.5F))
			.decorated(
				FeatureDecorator.SURFACE_RELATIVE_THRESHOLD.configured(new SurfaceRelativeThresholdConfiguration(Heightmap.Types.OCEAN_FLOOR_WG, Integer.MIN_VALUE, -2))
			)
			.squared()
			.range(Features.Decorators.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT)
			.count(UniformInt.of(44, 52))
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
			.decorated(
				FeatureDecorator.SURFACE_RELATIVE_THRESHOLD.configured(new SurfaceRelativeThresholdConfiguration(Heightmap.Types.OCEAN_FLOOR_WG, Integer.MIN_VALUE, -13))
			)
			.squared()
			.range(Features.Decorators.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT)
			.count(UniformInt.of(104, 157))
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
					BlockStateProvider.simple(
						Blocks.BROWN_MUSHROOM_BLOCK
							.defaultBlockState()
							.setValue(HugeMushroomBlock.UP, Boolean.valueOf(true))
							.setValue(HugeMushroomBlock.DOWN, Boolean.valueOf(false))
					),
					BlockStateProvider.simple(
						Blocks.MUSHROOM_STEM.defaultBlockState().setValue(HugeMushroomBlock.UP, Boolean.valueOf(false)).setValue(HugeMushroomBlock.DOWN, Boolean.valueOf(false))
					),
					3
				)
			)
	);
	public static final ConfiguredFeature<?, ?> HUGE_RED_MUSHROOM = register(
		"huge_red_mushroom",
		Feature.HUGE_RED_MUSHROOM
			.configured(
				new HugeMushroomFeatureConfiguration(
					BlockStateProvider.simple(Blocks.RED_MUSHROOM_BLOCK.defaultBlockState().setValue(HugeMushroomBlock.DOWN, Boolean.valueOf(false))),
					BlockStateProvider.simple(
						Blocks.MUSHROOM_STEM.defaultBlockState().setValue(HugeMushroomBlock.UP, Boolean.valueOf(false)).setValue(HugeMushroomBlock.DOWN, Boolean.valueOf(false))
					),
					2
				)
			)
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> OAK = register("oak", Feature.TREE.configured(createOak().build()));
	public static final ConfiguredFeature<?, ?> OAK_CHECKED = register("oak_checked", OAK.filteredByBlockSurvival(Blocks.OAK_SAPLING));
	public static final ConfiguredFeature<TreeConfiguration, ?> DARK_OAK = register(
		"dark_oak",
		Feature.TREE
			.configured(
				new TreeConfiguration.TreeConfigurationBuilder(
						BlockStateProvider.simple(Blocks.DARK_OAK_LOG),
						new DarkOakTrunkPlacer(6, 2, 1),
						BlockStateProvider.simple(Blocks.DARK_OAK_LEAVES),
						new DarkOakFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0)),
						new ThreeLayersFeatureSize(1, 1, 0, 1, 2, OptionalInt.empty())
					)
					.ignoreVines()
					.build()
			)
	);
	public static final ConfiguredFeature<?, ?> DARK_OAK_CHECKED = register("dark_oak_checked", DARK_OAK.filteredByBlockSurvival(Blocks.DARK_OAK_SAPLING));
	public static final ConfiguredFeature<TreeConfiguration, ?> BIRCH = register("birch", Feature.TREE.configured(createBirch().build()));
	public static final ConfiguredFeature<?, ?> BIRCH_CHECKED = register("birch_checked", BIRCH.filteredByBlockSurvival(Blocks.BIRCH_SAPLING));
	public static final ConfiguredFeature<TreeConfiguration, ?> ACACIA = register(
		"acacia",
		Feature.TREE
			.configured(
				new TreeConfiguration.TreeConfigurationBuilder(
						BlockStateProvider.simple(Blocks.ACACIA_LOG),
						new ForkingTrunkPlacer(5, 2, 2),
						BlockStateProvider.simple(Blocks.ACACIA_LEAVES),
						new AcaciaFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0)),
						new TwoLayersFeatureSize(1, 0, 2)
					)
					.ignoreVines()
					.build()
			)
	);
	public static final ConfiguredFeature<?, ?> ACACIA_CHECKED = register("acacia_checked", ACACIA.filteredByBlockSurvival(Blocks.ACACIA_SAPLING));
	public static final ConfiguredFeature<TreeConfiguration, ?> SPRUCE = register(
		"spruce",
		Feature.TREE
			.configured(
				new TreeConfiguration.TreeConfigurationBuilder(
						BlockStateProvider.simple(Blocks.SPRUCE_LOG),
						new StraightTrunkPlacer(5, 2, 1),
						BlockStateProvider.simple(Blocks.SPRUCE_LEAVES),
						new SpruceFoliagePlacer(UniformInt.of(2, 3), UniformInt.of(0, 2), UniformInt.of(1, 2)),
						new TwoLayersFeatureSize(2, 0, 2)
					)
					.ignoreVines()
					.build()
			)
	);
	public static final ConfiguredFeature<?, ?> SPRUCE_CHECKED = register("spruce_checked", SPRUCE.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING));
	public static final BlockPredicate SNOW_TREE_PREDICATE = BlockPredicate.matchesBlocks(List.of(Blocks.SNOW_BLOCK, Blocks.POWDER_SNOW), new BlockPos(0, -1, 0));
	public static final ConfiguredDecorator<?> SNOW_TREE_FILTER_DECORATOR = FeatureDecorator.BLOCK_FILTER
		.configured(new BlockFilterConfiguration(SNOW_TREE_PREDICATE))
		.decorated(
			FeatureDecorator.ENVIRONMENT_SCAN
				.configured(new EnvironmentScanConfiguration(Direction.UP, BlockPredicate.not(BlockPredicate.matchesBlock(Blocks.POWDER_SNOW, BlockPos.ZERO)), 8))
		);
	public static final ConfiguredFeature<?, ?> SPRUCE_ON_SNOW = register("spruce_on_snow", SPRUCE.decorated(SNOW_TREE_FILTER_DECORATOR));
	public static final ConfiguredFeature<TreeConfiguration, ?> PINE = register(
		"pine",
		Feature.TREE
			.configured(
				new TreeConfiguration.TreeConfigurationBuilder(
						BlockStateProvider.simple(Blocks.SPRUCE_LOG),
						new StraightTrunkPlacer(6, 4, 0),
						BlockStateProvider.simple(Blocks.SPRUCE_LEAVES),
						new PineFoliagePlacer(ConstantInt.of(1), ConstantInt.of(1), UniformInt.of(3, 4)),
						new TwoLayersFeatureSize(2, 0, 2)
					)
					.ignoreVines()
					.build()
			)
	);
	public static final ConfiguredFeature<?, ?> PINE_CHECKED = register("pine_checked", PINE.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING));
	public static final ConfiguredFeature<?, ?> PINE_ON_SNOW = register("pine_on_snow", PINE.decorated(SNOW_TREE_FILTER_DECORATOR));
	public static final ConfiguredFeature<?, ?> JUNGLE_TREE_CHECKED = register(
		"jungle_tree",
		Feature.TREE
			.configured(
				createJungleTree().decorators(ImmutableList.of(new CocoaDecorator(0.2F), TrunkVineDecorator.INSTANCE, LeaveVineDecorator.INSTANCE)).ignoreVines().build()
			)
			.filteredByBlockSurvival(Blocks.JUNGLE_SAPLING)
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> FANCY_OAK = register("fancy_oak", Feature.TREE.configured(createFancyOak().build()));
	public static final ConfiguredFeature<?, ?> FANCY_OAK_CHECKED = register("fancy_oak_checked", FANCY_OAK.filteredByBlockSurvival(Blocks.OAK_SAPLING));
	public static final ConfiguredFeature<?, ?> JUNGLE_TREE_NO_VINE = register(
		"jungle_tree_no_vine", Feature.TREE.configured(createJungleTree().ignoreVines().build())
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> MEGA_JUNGLE_TREE = register(
		"mega_jungle_tree",
		Feature.TREE
			.configured(
				new TreeConfiguration.TreeConfigurationBuilder(
						BlockStateProvider.simple(Blocks.JUNGLE_LOG),
						new MegaJungleTrunkPlacer(10, 2, 19),
						BlockStateProvider.simple(Blocks.JUNGLE_LEAVES),
						new MegaJungleFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 2),
						new TwoLayersFeatureSize(1, 1, 2)
					)
					.decorators(ImmutableList.of(TrunkVineDecorator.INSTANCE, LeaveVineDecorator.INSTANCE))
					.build()
			)
	);
	public static final ConfiguredFeature<?, ?> MEGA_JUNGLE_TREE_CHECKED = register(
		"mega_jungle_tree_checked", MEGA_JUNGLE_TREE.filteredByBlockSurvival(Blocks.JUNGLE_SAPLING)
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> MEGA_SPRUCE = register(
		"mega_spruce",
		Feature.TREE
			.configured(
				new TreeConfiguration.TreeConfigurationBuilder(
						BlockStateProvider.simple(Blocks.SPRUCE_LOG),
						new GiantTrunkPlacer(13, 2, 14),
						BlockStateProvider.simple(Blocks.SPRUCE_LEAVES),
						new MegaPineFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0), UniformInt.of(13, 17)),
						new TwoLayersFeatureSize(1, 1, 2)
					)
					.decorators(ImmutableList.of(new AlterGroundDecorator(BlockStateProvider.simple(Blocks.PODZOL))))
					.build()
			)
	);
	public static final ConfiguredFeature<?, ?> MEGA_SPRUCE_CHECKED = register("mega_spruce_checked", MEGA_SPRUCE.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING));
	public static final ConfiguredFeature<TreeConfiguration, ?> MEGA_PINE = register(
		"mega_pine",
		Feature.TREE
			.configured(
				new TreeConfiguration.TreeConfigurationBuilder(
						BlockStateProvider.simple(Blocks.SPRUCE_LOG),
						new GiantTrunkPlacer(13, 2, 14),
						BlockStateProvider.simple(Blocks.SPRUCE_LEAVES),
						new MegaPineFoliagePlacer(ConstantInt.of(0), ConstantInt.of(0), UniformInt.of(3, 7)),
						new TwoLayersFeatureSize(1, 1, 2)
					)
					.decorators(ImmutableList.of(new AlterGroundDecorator(BlockStateProvider.simple(Blocks.PODZOL))))
					.build()
			)
	);
	public static final ConfiguredFeature<?, ?> MEGA_PINE_CHECKED = register("mega_pine_checked", MEGA_PINE.filteredByBlockSurvival(Blocks.SPRUCE_SAPLING));
	public static final ConfiguredFeature<?, ?> SUPER_BIRCH_BEES_0002 = register(
		"super_birch_bees_0002",
		Feature.TREE
			.configured(createSuperBirch().decorators(ImmutableList.of(Features.Decorators.BEEHIVE_0002)).build())
			.filteredByBlockSurvival(Blocks.BIRCH_SAPLING)
	);
	public static final ConfiguredFeature<?, ?> SUPER_BIRCH_BEES = register(
		"super_birch_bees",
		Feature.TREE.configured(createSuperBirch().decorators(ImmutableList.of(Features.Decorators.BEEHIVE)).build()).filteredByBlockSurvival(Blocks.BIRCH_SAPLING)
	);
	public static final ConfiguredFeature<?, ?> SWAMP_OAK = register(
		"swamp_oak",
		Feature.TREE
			.configured(createStraightBlobTree(Blocks.OAK_LOG, Blocks.OAK_LEAVES, 5, 3, 0, 3).decorators(ImmutableList.of(LeaveVineDecorator.INSTANCE)).build())
			.filteredByBlockSurvival(Blocks.OAK_SAPLING)
	);
	public static final ConfiguredFeature<?, ?> JUNGLE_BUSH = register(
		"jungle_bush",
		Feature.TREE
			.configured(
				new TreeConfiguration.TreeConfigurationBuilder(
						BlockStateProvider.simple(Blocks.JUNGLE_LOG),
						new StraightTrunkPlacer(1, 0, 0),
						BlockStateProvider.simple(Blocks.OAK_LEAVES),
						new BushFoliagePlacer(ConstantInt.of(2), ConstantInt.of(1), 2),
						new TwoLayersFeatureSize(0, 0, 0)
					)
					.build()
			)
			.filteredByBlockSurvival(Blocks.OAK_SAPLING)
	);
	public static final ConfiguredFeature<?, ?> AZALEA_TREE = register(
		"azalea_tree",
		Feature.TREE
			.configured(
				new TreeConfiguration.TreeConfigurationBuilder(
						BlockStateProvider.simple(Blocks.OAK_LOG),
						new BendingTrunkPlacer(4, 2, 0, 3, UniformInt.of(1, 2)),
						new WeightedStateProvider(
							weightedBlockStateBuilder().add(Blocks.AZALEA_LEAVES.defaultBlockState(), 3).add(Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState(), 1)
						),
						new RandomSpreadFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0), ConstantInt.of(2), 50),
						new TwoLayersFeatureSize(1, 0, 1)
					)
					.dirt(BlockStateProvider.simple(Blocks.ROOTED_DIRT))
					.forceDirt()
					.build()
			)
	);
	public static final ConfiguredFeature<?, ?> OAK_BEES_0002 = register(
		"oak_bees_0002",
		Feature.TREE.configured(createOak().decorators(List.of(Features.Decorators.BEEHIVE_0002)).build()).filteredByBlockSurvival(Blocks.OAK_SAPLING)
	);
	public static final ConfiguredFeature<?, ?> OAK_BEES_002 = register(
		"oak_bees_002", Feature.TREE.configured(createOak().decorators(List.of(Features.Decorators.BEEHIVE_002)).build()).filteredByBlockSurvival(Blocks.OAK_SAPLING)
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> OAK_BEES_005 = register(
		"oak_bees_005", Feature.TREE.configured(createOak().decorators(List.of(Features.Decorators.BEEHIVE_005)).build())
	);
	public static final ConfiguredFeature<?, ?> BIRCH_BEES_0002 = register(
		"birch_bees_0002",
		Feature.TREE.configured(createBirch().decorators(List.of(Features.Decorators.BEEHIVE_0002)).build()).filteredByBlockSurvival(Blocks.BIRCH_SAPLING)
	);
	public static final ConfiguredFeature<?, ?> BIRCH_BEES_002 = register(
		"birch_bees_002",
		Feature.TREE.configured(createBirch().decorators(List.of(Features.Decorators.BEEHIVE_002)).build()).filteredByBlockSurvival(Blocks.BIRCH_SAPLING)
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> BIRCH_BEES_005 = register(
		"birch_bees_005", Feature.TREE.configured(createBirch().decorators(List.of(Features.Decorators.BEEHIVE_005)).build())
	);
	public static final ConfiguredFeature<?, ?> FANCY_OAK_BEES_0002 = register(
		"fancy_oak_bees_0002",
		Feature.TREE.configured(createFancyOak().decorators(List.of(Features.Decorators.BEEHIVE_0002)).build()).filteredByBlockSurvival(Blocks.OAK_SAPLING)
	);
	public static final ConfiguredFeature<?, ?> FANCY_OAK_BEES_002 = register(
		"fancy_oak_bees_002",
		Feature.TREE.configured(createFancyOak().decorators(List.of(Features.Decorators.BEEHIVE_002)).build()).filteredByBlockSurvival(Blocks.OAK_SAPLING)
	);
	public static final ConfiguredFeature<TreeConfiguration, ?> FANCY_OAK_BEES_005 = register(
		"fancy_oak_bees_005", Feature.TREE.configured(createFancyOak().decorators(List.of(Features.Decorators.BEEHIVE_005)).build())
	);
	public static final ConfiguredFeature<?, ?> FANCY_OAK_BEES = register(
		"fancy_oak_bees",
		Feature.TREE.configured(createFancyOak().decorators(List.of(Features.Decorators.BEEHIVE)).build()).filteredByBlockSurvival(Blocks.OAK_SAPLING)
	);
	public static final ConfiguredFeature<?, ?> FLOWER_WARM = register(
		"flower_warm", Feature.FLOWER.configured(Features.Configs.DEFAULT_FLOWER).decorated(Features.Decorators.HEIGHTMAP_SQUARE).rarity(16)
	);
	public static final ConfiguredFeature<?, ?> FLOWER_DEFAULT = register(
		"flower_default", Feature.FLOWER.configured(Features.Configs.DEFAULT_FLOWER).decorated(Features.Decorators.HEIGHTMAP_SQUARE).rarity(32)
	);
	public static final ConfiguredFeature<?, ?> FLOWER_FOREST = register(
		"flower_forest",
		Feature.FLOWER
			.configured(
				new RandomPatchConfiguration(
					96,
					6,
					2,
					() -> Feature.SIMPLE_BLOCK
							.configured(
								new SimpleBlockConfiguration(
									new NoiseProvider(
										2345L,
										new NormalNoise.NoiseParameters(0, 1.0),
										0.020833334F,
										List.of(
											Blocks.DANDELION.defaultBlockState(),
											Blocks.POPPY.defaultBlockState(),
											Blocks.ALLIUM.defaultBlockState(),
											Blocks.AZURE_BLUET.defaultBlockState(),
											Blocks.RED_TULIP.defaultBlockState(),
											Blocks.ORANGE_TULIP.defaultBlockState(),
											Blocks.WHITE_TULIP.defaultBlockState(),
											Blocks.PINK_TULIP.defaultBlockState(),
											Blocks.OXEYE_DAISY.defaultBlockState(),
											Blocks.CORNFLOWER.defaultBlockState(),
											Blocks.LILY_OF_THE_VALLEY.defaultBlockState()
										)
									)
								)
							)
							.onlyWhenEmpty()
				)
			)
			.decorated(Features.Decorators.HEIGHTMAP_SQUARE)
			.rarity(2)
			.count(3)
	);
	public static final ConfiguredFeature<?, ?> FLOWER_SWAMP = register(
		"flower_swamp",
		Feature.FLOWER
			.configured(
				new RandomPatchConfiguration(
					64, 6, 2, () -> Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.BLUE_ORCHID))).onlyWhenEmpty()
				)
			)
			.decorated(Features.Decorators.HEIGHTMAP_SQUARE)
			.rarity(32)
	);
	public static final ConfiguredFeature<?, ?> FLOWER_PLAIN = register(
		"flower_plain",
		Feature.FLOWER
			.configured(
				new RandomPatchConfiguration(
					64,
					6,
					2,
					() -> Feature.SIMPLE_BLOCK
							.configured(
								new SimpleBlockConfiguration(
									new NoiseThresholdProvider(
										2345L,
										new NormalNoise.NoiseParameters(0, 1.0),
										0.005F,
										-0.8F,
										0.33333334F,
										Blocks.DANDELION.defaultBlockState(),
										ImmutableList.of(
											Blocks.ORANGE_TULIP.defaultBlockState(),
											Blocks.RED_TULIP.defaultBlockState(),
											Blocks.PINK_TULIP.defaultBlockState(),
											Blocks.WHITE_TULIP.defaultBlockState()
										),
										ImmutableList.of(
											Blocks.POPPY.defaultBlockState(),
											Blocks.AZURE_BLUET.defaultBlockState(),
											Blocks.OXEYE_DAISY.defaultBlockState(),
											Blocks.CORNFLOWER.defaultBlockState()
										)
									)
								)
							)
							.onlyWhenEmpty()
				)
			)
	);
	public static final ConfiguredFeature<?, ?> FLOWER_PLAIN_DECORATED = register(
		"flower_plain_decorated",
		FLOWER_PLAIN.decorated(Features.Decorators.HEIGHTMAP_SQUARE)
			.rarity(32)
			.decorated(FeatureDecorator.COUNT_NOISE.configured(new NoiseDependantDecoratorConfiguration(-0.8, 15, 4)))
	);
	public static final ConfiguredFeature<?, ?> FLOWER_MEADOW = register(
		"flower_meadow",
		Feature.FLOWER
			.configured(
				new RandomPatchConfiguration(
					96,
					6,
					2,
					() -> Feature.SIMPLE_BLOCK
							.configured(
								new SimpleBlockConfiguration(
									new DualNoiseProvider(
										new InclusiveRange(1, 3),
										new NormalNoise.NoiseParameters(-10, 1.0),
										1.0F,
										2345L,
										new NormalNoise.NoiseParameters(-3, 1.0),
										1.0F,
										ImmutableList.of(
											Blocks.TALL_GRASS.defaultBlockState(),
											Blocks.ALLIUM.defaultBlockState(),
											Blocks.POPPY.defaultBlockState(),
											Blocks.AZURE_BLUET.defaultBlockState(),
											Blocks.DANDELION.defaultBlockState(),
											Blocks.CORNFLOWER.defaultBlockState(),
											Blocks.OXEYE_DAISY.defaultBlockState(),
											Blocks.GRASS.defaultBlockState()
										)
									)
								)
							)
							.onlyWhenEmpty()
				)
			)
			.decorated(Features.Decorators.HEIGHTMAP_SQUARE)
	);
	public static final ConfiguredFeature<?, ?> GRASS_BONEMEAL = Feature.SIMPLE_BLOCK
		.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.GRASS.defaultBlockState())))
		.onlyWhenEmpty();
	private static final ImmutableList<Supplier<ConfiguredFeature<?, ?>>> FOREST_FLOWER_FEATURES = ImmutableList.of(
		() -> Feature.RANDOM_PATCH
				.configured(simplePatchConfiguration(Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.LILAC))))),
		() -> Feature.RANDOM_PATCH
				.configured(simplePatchConfiguration(Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.ROSE_BUSH))))),
		() -> Feature.RANDOM_PATCH
				.configured(simplePatchConfiguration(Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.PEONY))))),
		() -> Feature.NO_BONEMEAL_FLOWER
				.configured(simplePatchConfiguration(Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.LILY_OF_THE_VALLEY)))))
	);
	public static final ConfiguredFeature<?, ?> FOREST_FLOWER_VEGETATION_COMMON = register(
		"forest_flower_vegetation_common",
		Feature.SIMPLE_RANDOM_SELECTOR
			.configured(new SimpleRandomFeatureConfiguration(FOREST_FLOWER_FEATURES))
			.count(ClampedInt.of(UniformInt.of(-1, 3), 0, 3))
			.decorated(Features.Decorators.HEIGHTMAP_SQUARE)
			.rarity(7)
	);
	public static final ConfiguredFeature<?, ?> FOREST_FLOWER_VEGETATION = register(
		"forest_flower_vegetation",
		Feature.SIMPLE_RANDOM_SELECTOR
			.configured(new SimpleRandomFeatureConfiguration(FOREST_FLOWER_FEATURES))
			.count(ClampedInt.of(UniformInt.of(-3, 1), 0, 1))
			.decorated(Features.Decorators.HEIGHTMAP_SQUARE)
			.rarity(7)
	);
	public static final ConfiguredFeature<?, ?> DARK_FOREST_VEGETATION = register(
		"dark_forest_vegetation",
		Feature.RANDOM_SELECTOR
			.configured(
				new RandomFeatureConfiguration(
					ImmutableList.of(
						HUGE_BROWN_MUSHROOM.weighted(0.025F),
						HUGE_RED_MUSHROOM.weighted(0.05F),
						DARK_OAK_CHECKED.weighted(0.6666667F),
						BIRCH_CHECKED.weighted(0.2F),
						FANCY_OAK_CHECKED.weighted(0.1F)
					),
					OAK_CHECKED
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
	public static final ConfiguredFeature<?, ?> MEADOW_TREES = register(
		"meadow_trees",
		Feature.RANDOM_SELECTOR
			.configured(new RandomFeatureConfiguration(ImmutableList.of(FANCY_OAK_BEES.weighted(0.5F)), SUPER_BIRCH_BEES))
			.decorated(Features.Decorators.HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED)
			.rarity(100)
	);
	public static final ConfiguredFeature<?, ?> TAIGA_VEGETATION = register(
		"taiga_vegetation",
		Feature.RANDOM_SELECTOR
			.configured(new RandomFeatureConfiguration(ImmutableList.of(PINE_CHECKED.weighted(0.33333334F)), SPRUCE_CHECKED))
			.decorated(Features.Decorators.HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED)
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(10, 0.1F, 1)))
	);
	public static final ConfiguredFeature<?, ?> GROVE_VEGETATION = register(
		"grove_vegetation",
		Feature.RANDOM_SELECTOR
			.configured(new RandomFeatureConfiguration(ImmutableList.of(PINE_ON_SNOW.weighted(0.33333334F)), SPRUCE_ON_SNOW))
			.decorated(Features.Decorators.HEIGHTMAP_SQUARE)
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(10, 0.1F, 1)))
	);
	public static final ConfiguredFeature<?, ?> TREES_BADLANDS = register(
		"trees_badlands",
		OAK_CHECKED.decorated(Features.Decorators.HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED)
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(5, 0.1F, 1)))
	);
	public static final ConfiguredFeature<?, ?> TREES_SNOWY = register(
		"trees_snowy",
		SPRUCE_CHECKED.decorated(Features.Decorators.HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED)
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(0, 0.1F, 1)))
	);
	public static final ConfiguredFeature<?, ?> TREES_SWAMP = register(
		"trees_swamp",
		SWAMP_OAK.decorated(Features.Decorators.HEIGHTMAP_OCEAN_FLOOR)
			.decorated(FeatureDecorator.WATER_DEPTH_THRESHOLD.configured(new WaterDepthThresholdConfiguration(2)))
			.squared()
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(2, 0.1F, 1)))
	);
	public static final ConfiguredFeature<?, ?> TREES_SHATTERED_SAVANNA = register(
		"trees_shattered_savanna",
		Feature.RANDOM_SELECTOR
			.configured(new RandomFeatureConfiguration(ImmutableList.of(ACACIA_CHECKED.weighted(0.8F)), OAK_CHECKED))
			.decorated(Features.Decorators.HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED)
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(2, 0.1F, 1)))
	);
	public static final ConfiguredFeature<?, ?> TREES_SAVANNA = register(
		"trees_savanna",
		Feature.RANDOM_SELECTOR
			.configured(new RandomFeatureConfiguration(ImmutableList.of(ACACIA_CHECKED.weighted(0.8F)), OAK_CHECKED))
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
			.configured(new RandomFeatureConfiguration(ImmutableList.of(SPRUCE_CHECKED.weighted(0.666F), FANCY_OAK_CHECKED.weighted(0.1F)), OAK_CHECKED))
			.decorated(Features.Decorators.HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED)
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(3, 0.1F, 1)))
	);
	public static final ConfiguredFeature<?, ?> TREES_MOUNTAIN = register(
		"trees_mountain",
		Feature.RANDOM_SELECTOR
			.configured(new RandomFeatureConfiguration(ImmutableList.of(SPRUCE_CHECKED.weighted(0.666F), FANCY_OAK_CHECKED.weighted(0.1F)), OAK_CHECKED))
			.decorated(Features.Decorators.HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED)
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(0, 0.1F, 1)))
	);
	public static final ConfiguredFeature<?, ?> TREES_WATER = register(
		"trees_water",
		Feature.RANDOM_SELECTOR
			.configured(new RandomFeatureConfiguration(ImmutableList.of(FANCY_OAK_CHECKED.weighted(0.1F)), OAK_CHECKED))
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
			.configured(new RandomFeatureConfiguration(List.of(FANCY_OAK_BEES_005.weighted(0.33333334F)), OAK_BEES_005))
			.filteredByBlockSurvival(Blocks.OAK_SAPLING)
			.decorated(Features.Decorators.HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED)
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(0, 0.05F, 1)))
	);
	public static final ConfiguredFeature<?, ?> TREES_JUNGLE_EDGE = register(
		"trees_jungle_edge",
		Feature.RANDOM_SELECTOR
			.configured(new RandomFeatureConfiguration(ImmutableList.of(FANCY_OAK_CHECKED.weighted(0.1F), JUNGLE_BUSH.weighted(0.5F)), JUNGLE_TREE_CHECKED))
			.decorated(Features.Decorators.HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED)
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(2, 0.1F, 1)))
	);
	public static final ConfiguredFeature<?, ?> TREES_GIANT_SPRUCE = register(
		"trees_giant_spruce",
		Feature.RANDOM_SELECTOR
			.configured(new RandomFeatureConfiguration(ImmutableList.of(MEGA_SPRUCE_CHECKED.weighted(0.33333334F), PINE_CHECKED.weighted(0.33333334F)), SPRUCE_CHECKED))
			.decorated(Features.Decorators.HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED)
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(10, 0.1F, 1)))
	);
	public static final ConfiguredFeature<?, ?> TREES_GIANT = register(
		"trees_giant",
		Feature.RANDOM_SELECTOR
			.configured(
				new RandomFeatureConfiguration(
					ImmutableList.of(MEGA_SPRUCE_CHECKED.weighted(0.025641026F), MEGA_PINE_CHECKED.weighted(0.30769232F), PINE_CHECKED.weighted(0.33333334F)), SPRUCE_CHECKED
				)
			)
			.decorated(Features.Decorators.HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED)
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(10, 0.1F, 1)))
	);
	public static final ConfiguredFeature<?, ?> TREES_JUNGLE = register(
		"trees_jungle",
		Feature.RANDOM_SELECTOR
			.configured(
				new RandomFeatureConfiguration(
					ImmutableList.of(FANCY_OAK_CHECKED.weighted(0.1F), JUNGLE_BUSH.weighted(0.5F), MEGA_JUNGLE_TREE_CHECKED.weighted(0.33333334F)), JUNGLE_TREE_CHECKED
				)
			)
			.decorated(Features.Decorators.HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED)
			.decorated(FeatureDecorator.COUNT_EXTRA.configured(new FrequencyWithExtraChanceDecoratorConfiguration(50, 0.1F, 1)))
	);
	public static final ConfiguredFeature<?, ?> BAMBOO_VEGETATION = register(
		"bamboo_vegetation",
		Feature.RANDOM_SELECTOR
			.configured(
				new RandomFeatureConfiguration(
					ImmutableList.of(FANCY_OAK_CHECKED.weighted(0.05F), JUNGLE_BUSH.weighted(0.15F), MEGA_JUNGLE_TREE_CHECKED.weighted(0.7F)),
					Feature.RANDOM_PATCH.configured(Features.Configs.JUNGLE_GRASS)
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
					BlockStateProvider.simple(Blocks.ROOTED_DIRT),
					20,
					100,
					3,
					2,
					BlockStateProvider.simple(Blocks.HANGING_ROOTS),
					20,
					2
				)
			)
			.decorated(FeatureDecorator.CAVE_SURFACE.configured(new CaveDecoratorConfiguration(CaveSurface.CEILING, 12, false)))
			.range(Features.Decorators.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT)
			.squared()
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
		UniformInt.of(23, 25)
	);
	public static final ConfiguredFeature<BlockColumnConfiguration, ?> CAVE_VINE = register(
		"cave_vine",
		Feature.BLOCK_COLUMN
			.configured(
				new BlockColumnConfiguration(
					List.of(
						BlockColumnConfiguration.layer(
							new WeightedListInt(
								SimpleWeightedRandomList.<IntProvider>builder().add(UniformInt.of(0, 19), 2).add(UniformInt.of(0, 2), 3).add(UniformInt.of(0, 6), 10).build()
							),
							CAVE_VINES_BODY_PROVIDER
						),
						BlockColumnConfiguration.layer(ConstantInt.of(1), CAVE_VINES_HEAD_PROVIDER)
					),
					Direction.DOWN,
					ONLY_IN_AIR_PREDICATE,
					true
				)
			)
	);
	public static final ConfiguredFeature<BlockColumnConfiguration, ?> CAVE_VINE_IN_MOSS = register(
		"cave_vine_in_moss",
		Feature.BLOCK_COLUMN
			.configured(
				new BlockColumnConfiguration(
					List.of(
						BlockColumnConfiguration.layer(
							new WeightedListInt(SimpleWeightedRandomList.<IntProvider>builder().add(UniformInt.of(0, 3), 5).add(UniformInt.of(1, 7), 1).build()),
							CAVE_VINES_BODY_PROVIDER
						),
						BlockColumnConfiguration.layer(ConstantInt.of(1), CAVE_VINES_HEAD_PROVIDER)
					),
					Direction.DOWN,
					ONLY_IN_AIR_PREDICATE,
					true
				)
			)
	);
	public static final ConfiguredFeature<?, ?> CAVE_VINES = register(
		"cave_vines",
		CAVE_VINE.decorated(FeatureDecorator.CAVE_SURFACE.configured(new CaveDecoratorConfiguration(CaveSurface.CEILING, 12, false)))
			.range(Features.Decorators.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT)
			.squared()
			.count(157)
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
					BlockStateProvider.simple(Blocks.MOSS_BLOCK),
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
					BlockStateProvider.simple(Blocks.MOSS_BLOCK),
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
		MOSS_PATCH.decorated(FeatureDecorator.CAVE_SURFACE.configured(new CaveDecoratorConfiguration(CaveSurface.FLOOR, 12, false)))
			.range(Features.Decorators.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT)
			.squared()
			.count(104)
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
					BlockStateProvider.simple(Blocks.CLAY),
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
					BlockStateProvider.simple(Blocks.CLAY),
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
			.decorated(FeatureDecorator.CAVE_SURFACE.configured(new CaveDecoratorConfiguration(CaveSurface.FLOOR, 12, false)))
			.range(Features.Decorators.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT)
			.squared()
			.count(52)
	);
	public static final ConfiguredFeature<VegetationPatchConfiguration, ?> MOSS_PATCH_CEILING = register(
		"moss_patch_ceiling",
		Feature.VEGETATION_PATCH
			.configured(
				new VegetationPatchConfiguration(
					BlockTags.MOSS_REPLACEABLE.getName(),
					BlockStateProvider.simple(Blocks.MOSS_BLOCK),
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
		MOSS_PATCH_CEILING.decorated(FeatureDecorator.CAVE_SURFACE.configured(new CaveDecoratorConfiguration(CaveSurface.CEILING, 12, false)))
			.range(Features.Decorators.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT)
			.squared()
			.count(104)
	);
	public static final ConfiguredFeature<?, ?> SPORE_BLOSSOM_FEATURE = register(
		"spore_blossom",
		Feature.SIMPLE_BLOCK
			.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.SPORE_BLOSSOM)))
			.decorated(FeatureDecorator.CAVE_SURFACE.configured(new CaveDecoratorConfiguration(CaveSurface.CEILING, 12, false)))
			.range(Features.Decorators.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT)
			.squared()
			.count(21)
	);
	public static final ConfiguredFeature<?, ?> CLASSIC_VINES_CAVE_FEATURE = register(
		"classic_vines_cave_feature",
		Feature.VINES.configured(FeatureConfiguration.NONE).range(Features.Decorators.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT).squared().count(216)
	);
	public static final ConfiguredFeature<?, ?> AMETHYST_GEODE = register(
		"amethyst_geode",
		Feature.GEODE
			.configured(
				new GeodeConfiguration(
					new GeodeBlockSettings(
						BlockStateProvider.simple(Blocks.AIR),
						BlockStateProvider.simple(Blocks.AMETHYST_BLOCK),
						BlockStateProvider.simple(Blocks.BUDDING_AMETHYST),
						BlockStateProvider.simple(Blocks.CALCITE),
						BlockStateProvider.simple(Blocks.SMOOTH_BASALT),
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
			.rangeUniform(VerticalAnchor.aboveBottom(6), VerticalAnchor.absolute(30))
			.squared()
			.rarity(24)
	);

	static SimpleWeightedRandomList.Builder<BlockState> weightedBlockStateBuilder() {
		return SimpleWeightedRandomList.builder();
	}

	private static RandomPatchConfiguration simplePatchConfiguration(ConfiguredFeature<?, ?> configuredFeature, List<Block> list, int i) {
		ConfiguredFeature<?, ?> configuredFeature2;
		if (!list.isEmpty()) {
			configuredFeature2 = configuredFeature.filtered(BlockPredicate.allOf(ONLY_IN_AIR_PREDICATE, BlockPredicate.matchesBlocks(list, new BlockPos(0, -1, 0))));
		} else {
			configuredFeature2 = configuredFeature.filtered(ONLY_IN_AIR_PREDICATE);
		}

		return new RandomPatchConfiguration(i, 7, 3, () -> configuredFeature2);
	}

	static RandomPatchConfiguration simplePatchConfiguration(ConfiguredFeature<?, ?> configuredFeature, List<Block> list) {
		return simplePatchConfiguration(configuredFeature, list, 96);
	}

	static RandomPatchConfiguration simplePatchConfiguration(ConfiguredFeature<?, ?> configuredFeature) {
		return simplePatchConfiguration(configuredFeature, List.of(), 96);
	}

	private static TreeConfiguration.TreeConfigurationBuilder createStraightBlobTree(Block block, Block block2, int i, int j, int k, int l) {
		return new TreeConfiguration.TreeConfigurationBuilder(
			BlockStateProvider.simple(block),
			new StraightTrunkPlacer(i, j, k),
			BlockStateProvider.simple(block2),
			new BlobFoliagePlacer(ConstantInt.of(l), ConstantInt.of(0), 3),
			new TwoLayersFeatureSize(1, 0, 1)
		);
	}

	private static TreeConfiguration.TreeConfigurationBuilder createOak() {
		return createStraightBlobTree(Blocks.OAK_LOG, Blocks.OAK_LEAVES, 4, 2, 0, 2).ignoreVines();
	}

	private static TreeConfiguration.TreeConfigurationBuilder createBirch() {
		return createStraightBlobTree(Blocks.BIRCH_LOG, Blocks.BIRCH_LEAVES, 5, 2, 0, 2).ignoreVines();
	}

	private static TreeConfiguration.TreeConfigurationBuilder createSuperBirch() {
		return createStraightBlobTree(Blocks.BIRCH_LOG, Blocks.BIRCH_LEAVES, 5, 2, 6, 2).ignoreVines();
	}

	private static TreeConfiguration.TreeConfigurationBuilder createJungleTree() {
		return createStraightBlobTree(Blocks.JUNGLE_LOG, Blocks.JUNGLE_LEAVES, 4, 8, 0, 2);
	}

	private static TreeConfiguration.TreeConfigurationBuilder createFancyOak() {
		return new TreeConfiguration.TreeConfigurationBuilder(
				BlockStateProvider.simple(Blocks.OAK_LOG),
				new FancyTrunkPlacer(3, 11, 0),
				BlockStateProvider.simple(Blocks.OAK_LEAVES),
				new FancyFoliagePlacer(ConstantInt.of(2), ConstantInt.of(4), 4),
				new TwoLayersFeatureSize(0, 0, 0, OptionalInt.of(4))
			)
			.ignoreVines();
	}

	private static ConfiguredFeature<BlockColumnConfiguration, ?> makeDripleaf(Direction direction) {
		return Feature.BLOCK_COLUMN
			.configured(
				new BlockColumnConfiguration(
					List.of(
						BlockColumnConfiguration.layer(
							new WeightedListInt(SimpleWeightedRandomList.<IntProvider>builder().add(UniformInt.of(0, 4), 2).add(ConstantInt.of(0), 1).build()),
							BlockStateProvider.simple(Blocks.BIG_DRIPLEAF_STEM.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, direction))
						),
						BlockColumnConfiguration.layer(
							ConstantInt.of(1), BlockStateProvider.simple(Blocks.BIG_DRIPLEAF.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, direction))
						)
					),
					Direction.UP,
					ONLY_IN_AIR_OR_WATER_PREDICATE,
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
							.add(Blocks.SMALL_DRIPLEAF.defaultBlockState().setValue(SmallDripleafBlock.FACING, Direction.EAST), 1)
							.add(Blocks.SMALL_DRIPLEAF.defaultBlockState().setValue(SmallDripleafBlock.FACING, Direction.WEST), 1)
							.add(Blocks.SMALL_DRIPLEAF.defaultBlockState().setValue(SmallDripleafBlock.FACING, Direction.NORTH), 1)
							.add(Blocks.SMALL_DRIPLEAF.defaultBlockState().setValue(SmallDripleafBlock.FACING, Direction.SOUTH), 1)
					)
				)
			);
	}

	private static <FC extends FeatureConfiguration> ConfiguredFeature<FC, ?> register(String string, ConfiguredFeature<FC, ?> configuredFeature) {
		return Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, string, configuredFeature);
	}

	static RandomPatchConfiguration grassPatch(BlockStateProvider blockStateProvider, int i) {
		return new RandomPatchConfiguration(i, 7, 3, () -> Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(blockStateProvider)).onlyWhenEmpty());
	}

	public static final class Configs {
		public static final RandomPatchConfiguration DEFAULT_GRASS = Features.grassPatch(BlockStateProvider.simple(Blocks.GRASS), 32);
		public static final RandomPatchConfiguration TAIGA_GRASS = Features.grassPatch(
			new WeightedStateProvider(Features.weightedBlockStateBuilder().add(Blocks.GRASS.defaultBlockState(), 1).add(Blocks.FERN.defaultBlockState(), 4)), 32
		);
		public static final RandomPatchConfiguration JUNGLE_GRASS = new RandomPatchConfiguration(
			32,
			7,
			3,
			() -> Feature.SIMPLE_BLOCK
					.configured(
						new SimpleBlockConfiguration(
							new WeightedStateProvider(Features.weightedBlockStateBuilder().add(Blocks.GRASS.defaultBlockState(), 3).add(Blocks.FERN.defaultBlockState(), 1))
						)
					)
					.filtered(BlockPredicate.allOf(Features.ONLY_IN_AIR_PREDICATE, BlockPredicate.not(BlockPredicate.matchesBlock(Blocks.PODZOL, new BlockPos(0, -1, 0)))))
		);
		public static final RandomPatchConfiguration DEFAULT_FLOWER = Features.grassPatch(
			new WeightedStateProvider(Features.weightedBlockStateBuilder().add(Blocks.POPPY.defaultBlockState(), 2).add(Blocks.DANDELION.defaultBlockState(), 1)), 64
		);
		public static final RandomPatchConfiguration DEAD_BUSH = Features.grassPatch(BlockStateProvider.simple(Blocks.DEAD_BUSH), 4);
		public static final RandomPatchConfiguration SWEET_BERRY_BUSH = Features.simplePatchConfiguration(
			Feature.SIMPLE_BLOCK
				.configured(
					new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, Integer.valueOf(3))))
				),
			List.of(Blocks.GRASS_BLOCK)
		);
		public static final RandomPatchConfiguration TALL_GRASS = Features.simplePatchConfiguration(
			Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.TALL_GRASS)))
		);
		public static final RandomPatchConfiguration SUGAR_CANE = new RandomPatchConfiguration(
			20,
			4,
			0,
			() -> Feature.BLOCK_COLUMN
					.configured(BlockColumnConfiguration.simple(BiasedToBottomInt.of(2, 4), BlockStateProvider.simple(Blocks.SUGAR_CANE)))
					.onlyWhenEmpty()
					.filteredByBlockSurvival(Blocks.SUGAR_CANE)
					.filtered(
						BlockPredicate.anyOf(
							BlockPredicate.matchesFluids(List.of(Fluids.WATER, Fluids.FLOWING_WATER), new BlockPos(1, -1, 0)),
							BlockPredicate.matchesFluids(List.of(Fluids.WATER, Fluids.FLOWING_WATER), new BlockPos(-1, -1, 0)),
							BlockPredicate.matchesFluids(List.of(Fluids.WATER, Fluids.FLOWING_WATER), new BlockPos(0, -1, 1)),
							BlockPredicate.matchesFluids(List.of(Fluids.WATER, Fluids.FLOWING_WATER), new BlockPos(0, -1, -1))
						)
					)
		);
		public static final SpringConfiguration LAVA_SPRING = new SpringConfiguration(
			Fluids.LAVA.defaultFluidState(),
			true,
			4,
			1,
			ImmutableSet.of(
				Blocks.STONE,
				Blocks.GRANITE,
				Blocks.DIORITE,
				Blocks.ANDESITE,
				Blocks.DEEPSLATE,
				Blocks.TUFF,
				Blocks.CALCITE,
				Blocks.DIRT,
				Blocks.SNOW_BLOCK,
				Blocks.POWDER_SNOW,
				Blocks.PACKED_ICE
			)
		);
		public static final SpringConfiguration CLOSED_NETHER_SPRING = new SpringConfiguration(
			Fluids.LAVA.defaultFluidState(), false, 5, 0, ImmutableSet.of(Blocks.NETHERRACK)
		);
		public static final BlockPileConfiguration CRIMSON_FOREST = new BlockPileConfiguration(
			new WeightedStateProvider(
				Features.weightedBlockStateBuilder()
					.add(Blocks.CRIMSON_ROOTS.defaultBlockState(), 87)
					.add(Blocks.CRIMSON_FUNGUS.defaultBlockState(), 11)
					.add(Blocks.WARPED_FUNGUS.defaultBlockState(), 1)
			)
		);
		public static final BlockPileConfiguration WARPED_FOREST = new BlockPileConfiguration(
			new WeightedStateProvider(
				Features.weightedBlockStateBuilder()
					.add(Blocks.WARPED_ROOTS.defaultBlockState(), 85)
					.add(Blocks.CRIMSON_ROOTS.defaultBlockState(), 1)
					.add(Blocks.WARPED_FUNGUS.defaultBlockState(), 13)
					.add(Blocks.CRIMSON_FUNGUS.defaultBlockState(), 1)
			)
		);
		public static final BlockPileConfiguration NETHER_SPROUTS = new BlockPileConfiguration(BlockStateProvider.simple(Blocks.NETHER_SPROUTS));
	}

	static final class Decorators {
		public static final BeehiveDecorator BEEHIVE_0002 = new BeehiveDecorator(0.002F);
		public static final BeehiveDecorator BEEHIVE_002 = new BeehiveDecorator(0.02F);
		public static final BeehiveDecorator BEEHIVE_005 = new BeehiveDecorator(0.05F);
		public static final BeehiveDecorator BEEHIVE = new BeehiveDecorator(1.0F);
		public static final ConfiguredDecorator<HeightmapConfiguration> HEIGHTMAP = FeatureDecorator.HEIGHTMAP
			.configured(new HeightmapConfiguration(Heightmap.Types.MOTION_BLOCKING));
		public static final ConfiguredDecorator<HeightmapConfiguration> HEIGHTMAP_TOP_SOLID = FeatureDecorator.HEIGHTMAP
			.configured(new HeightmapConfiguration(Heightmap.Types.OCEAN_FLOOR_WG));
		public static final ConfiguredDecorator<HeightmapConfiguration> HEIGHTMAP_WORLD_SURFACE = FeatureDecorator.HEIGHTMAP
			.configured(new HeightmapConfiguration(Heightmap.Types.WORLD_SURFACE_WG));
		public static final ConfiguredDecorator<HeightmapConfiguration> HEIGHTMAP_OCEAN_FLOOR = FeatureDecorator.HEIGHTMAP
			.configured(new HeightmapConfiguration(Heightmap.Types.OCEAN_FLOOR));
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
		public static final RangeDecoratorConfiguration RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT = new RangeDecoratorConfiguration(
			UniformHeight.of(VerticalAnchor.bottom(), VerticalAnchor.absolute(256))
		);
		public static final ConfiguredDecorator<?> FIRE = FeatureDecorator.RANGE.configured(RANGE_4_4).squared().countRandom(5);
		public static final ConfiguredDecorator<?> HEIGHTMAP_WITH_TREE_THRESHOLD = HEIGHTMAP_OCEAN_FLOOR.decorated(
			FeatureDecorator.WATER_DEPTH_THRESHOLD.configured(new WaterDepthThresholdConfiguration(0))
		);
		public static final ConfiguredDecorator<?> HEIGHTMAP_WITH_TREE_THRESHOLD_SQUARED = HEIGHTMAP_WITH_TREE_THRESHOLD.squared();
		public static final ConfiguredDecorator<?> HEIGHTMAP_SQUARE = HEIGHTMAP.squared();
		public static final ConfiguredDecorator<?> TOP_SOLID_HEIGHTMAP_SQUARE = HEIGHTMAP_TOP_SOLID.squared();
		public static final ConfiguredDecorator<?> DARK_OAK_DECORATOR = HEIGHTMAP_WITH_TREE_THRESHOLD.decorated(
			FeatureDecorator.DARK_OAK_TREE.configured(DecoratorConfiguration.NONE)
		);

		private Decorators() {
		}
	}
}
