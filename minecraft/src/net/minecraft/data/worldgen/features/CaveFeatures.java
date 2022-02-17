package net.minecraft.data.worldgen.features;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ClampedNormalFloat;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.util.valueproviders.WeightedListInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.CaveVinesBlock;
import net.minecraft.world.level.block.SmallDripleafBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FossilFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.CentralBlockScatteredFeaturesConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DripstoneClusterConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.GlowLichenConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.LargeDripstoneConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.PointedDripstoneConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RootSystemConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SculkPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.UnderwaterMagmaConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RandomizedIntStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RandomOffsetPlacement;

public class CaveFeatures {
	public static final ConfiguredFeature<NoneFeatureConfiguration, ?> MONSTER_ROOM = FeatureUtils.register(
		"monster_room", Feature.MONSTER_ROOM.configured(FeatureConfiguration.NONE)
	);
	private static final List<ResourceLocation> FOSSIL_STRUCTURES = List.of(
		new ResourceLocation("fossil/spine_1"),
		new ResourceLocation("fossil/spine_2"),
		new ResourceLocation("fossil/spine_3"),
		new ResourceLocation("fossil/spine_4"),
		new ResourceLocation("fossil/skull_1"),
		new ResourceLocation("fossil/skull_2"),
		new ResourceLocation("fossil/skull_3"),
		new ResourceLocation("fossil/skull_4")
	);
	private static final List<ResourceLocation> FOSSIL_COAL_STRUCTURES = List.of(
		new ResourceLocation("fossil/spine_1_coal"),
		new ResourceLocation("fossil/spine_2_coal"),
		new ResourceLocation("fossil/spine_3_coal"),
		new ResourceLocation("fossil/spine_4_coal"),
		new ResourceLocation("fossil/skull_1_coal"),
		new ResourceLocation("fossil/skull_2_coal"),
		new ResourceLocation("fossil/skull_3_coal"),
		new ResourceLocation("fossil/skull_4_coal")
	);
	public static final ConfiguredFeature<FossilFeatureConfiguration, ?> FOSSIL_COAL = FeatureUtils.register(
		"fossil_coal",
		Feature.FOSSIL
			.configured(new FossilFeatureConfiguration(FOSSIL_STRUCTURES, FOSSIL_COAL_STRUCTURES, ProcessorLists.FOSSIL_ROT, ProcessorLists.FOSSIL_COAL, 4))
	);
	public static final ConfiguredFeature<FossilFeatureConfiguration, ?> FOSSIL_DIAMONDS = FeatureUtils.register(
		"fossil_diamonds",
		Feature.FOSSIL
			.configured(new FossilFeatureConfiguration(FOSSIL_STRUCTURES, FOSSIL_COAL_STRUCTURES, ProcessorLists.FOSSIL_ROT, ProcessorLists.FOSSIL_DIAMONDS, 4))
	);
	public static final ConfiguredFeature<DripstoneClusterConfiguration, ?> DRIPSTONE_CLUSTER = FeatureUtils.register(
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
	);
	public static final ConfiguredFeature<LargeDripstoneConfiguration, ?> LARGE_DRIPSTONE = FeatureUtils.register(
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
	);
	public static final ConfiguredFeature<SimpleRandomFeatureConfiguration, ?> POINTED_DRIPSTONE = FeatureUtils.register(
		"pointed_dripstone",
		Feature.SIMPLE_RANDOM_SELECTOR
			.configured(
				new SimpleRandomFeatureConfiguration(
					ImmutableList.of(
						() -> Feature.POINTED_DRIPSTONE
								.configured(new PointedDripstoneConfiguration(0.2F, 0.7F, 0.5F, 0.5F))
								.placed(
									EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.solid(), BlockPredicate.ONLY_IN_AIR_OR_WATER_PREDICATE, 12),
									RandomOffsetPlacement.vertical(ConstantInt.of(1))
								),
						() -> Feature.POINTED_DRIPSTONE
								.configured(new PointedDripstoneConfiguration(0.2F, 0.7F, 0.5F, 0.5F))
								.placed(
									EnvironmentScanPlacement.scanningFor(Direction.UP, BlockPredicate.solid(), BlockPredicate.ONLY_IN_AIR_OR_WATER_PREDICATE, 12),
									RandomOffsetPlacement.vertical(ConstantInt.of(-1))
								)
					)
				)
			)
	);
	public static final ConfiguredFeature<UnderwaterMagmaConfiguration, ?> UNDERWATER_MAGMA = FeatureUtils.register(
		"underwater_magma", Feature.UNDERWATER_MAGMA.configured(new UnderwaterMagmaConfiguration(5, 1, 0.5F))
	);
	public static final ConfiguredFeature<GlowLichenConfiguration, ?> GLOW_LICHEN = FeatureUtils.register(
		"glow_lichen",
		Feature.GLOW_LICHEN
			.configured(
				new GlowLichenConfiguration(
					Blocks.GLOW_LICHEN.defaultBlockState(),
					20,
					false,
					true,
					true,
					0.5F,
					List.of(Blocks.STONE, Blocks.ANDESITE, Blocks.DIORITE, Blocks.GRANITE, Blocks.DRIPSTONE_BLOCK, Blocks.CALCITE, Blocks.TUFF, Blocks.DEEPSLATE)
				)
			)
	);
	public static final ConfiguredFeature<RootSystemConfiguration, ?> ROOTED_AZALEA_TREE = FeatureUtils.register(
		"rooted_azalea_tree",
		Feature.ROOT_SYSTEM
			.configured(
				new RootSystemConfiguration(
					() -> TreeFeatures.AZALEA_TREE.placed(),
					3,
					3,
					BlockTags.AZALEA_ROOT_REPLACEABLE.getName(),
					BlockStateProvider.simple(Blocks.ROOTED_DIRT),
					20,
					100,
					3,
					2,
					BlockStateProvider.simple(Blocks.HANGING_ROOTS),
					20,
					2,
					BlockPredicate.allOf(
						BlockPredicate.anyOf(
							BlockPredicate.matchesBlocks(List.of(Blocks.AIR, Blocks.CAVE_AIR, Blocks.VOID_AIR, Blocks.WATER)),
							BlockPredicate.matchesTag(BlockTags.LEAVES),
							BlockPredicate.matchesTag(BlockTags.REPLACEABLE_PLANTS)
						),
						BlockPredicate.matchesTag(BlockTags.AZALEA_GROWS_ON, Direction.DOWN.getNormal())
					)
				)
			)
	);
	private static final WeightedStateProvider CAVE_VINES_BODY_PROVIDER = new WeightedStateProvider(
		SimpleWeightedRandomList.<BlockState>builder()
			.add(Blocks.CAVE_VINES_PLANT.defaultBlockState(), 4)
			.add(Blocks.CAVE_VINES_PLANT.defaultBlockState().setValue(CaveVines.BERRIES, Boolean.valueOf(true)), 1)
	);
	private static final RandomizedIntStateProvider CAVE_VINES_HEAD_PROVIDER = new RandomizedIntStateProvider(
		new WeightedStateProvider(
			SimpleWeightedRandomList.<BlockState>builder()
				.add(Blocks.CAVE_VINES.defaultBlockState(), 4)
				.add(Blocks.CAVE_VINES.defaultBlockState().setValue(CaveVines.BERRIES, Boolean.valueOf(true)), 1)
		),
		CaveVinesBlock.AGE,
		UniformInt.of(23, 25)
	);
	public static final ConfiguredFeature<BlockColumnConfiguration, ?> CAVE_VINE = FeatureUtils.register(
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
					BlockPredicate.ONLY_IN_AIR_PREDICATE,
					true
				)
			)
	);
	public static final ConfiguredFeature<BlockColumnConfiguration, ?> CAVE_VINE_IN_MOSS = FeatureUtils.register(
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
					BlockPredicate.ONLY_IN_AIR_PREDICATE,
					true
				)
			)
	);
	public static final ConfiguredFeature<SimpleBlockConfiguration, ?> MOSS_VEGETATION = FeatureUtils.register(
		"moss_vegetation",
		Feature.SIMPLE_BLOCK
			.configured(
				new SimpleBlockConfiguration(
					new WeightedStateProvider(
						SimpleWeightedRandomList.<BlockState>builder()
							.add(Blocks.FLOWERING_AZALEA.defaultBlockState(), 4)
							.add(Blocks.AZALEA.defaultBlockState(), 7)
							.add(Blocks.MOSS_CARPET.defaultBlockState(), 25)
							.add(Blocks.GRASS.defaultBlockState(), 50)
							.add(Blocks.TALL_GRASS.defaultBlockState(), 10)
					)
				)
			)
	);
	public static final ConfiguredFeature<VegetationPatchConfiguration, ?> MOSS_PATCH = FeatureUtils.register(
		"moss_patch",
		Feature.VEGETATION_PATCH
			.configured(
				new VegetationPatchConfiguration(
					BlockTags.MOSS_REPLACEABLE.getName(),
					BlockStateProvider.simple(Blocks.MOSS_BLOCK),
					() -> MOSS_VEGETATION.placed(),
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
	public static final ConfiguredFeature<VegetationPatchConfiguration, ?> MOSS_PATCH_BONEMEAL = FeatureUtils.register(
		"moss_patch_bonemeal",
		Feature.VEGETATION_PATCH
			.configured(
				new VegetationPatchConfiguration(
					BlockTags.MOSS_REPLACEABLE.getName(),
					BlockStateProvider.simple(Blocks.MOSS_BLOCK),
					() -> MOSS_VEGETATION.placed(),
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
	public static final ConfiguredFeature<SimpleRandomFeatureConfiguration, ?> DRIPLEAF = FeatureUtils.register(
		"dripleaf",
		Feature.SIMPLE_RANDOM_SELECTOR
			.configured(
				new SimpleRandomFeatureConfiguration(
					List.of(
						CaveFeatures::makeSmallDripleaf,
						(Supplier)() -> makeDripleaf(Direction.EAST),
						(Supplier)() -> makeDripleaf(Direction.WEST),
						(Supplier)() -> makeDripleaf(Direction.SOUTH),
						(Supplier)() -> makeDripleaf(Direction.NORTH)
					)
				)
			)
	);
	public static final ConfiguredFeature<?, ?> CLAY_WITH_DRIPLEAVES = FeatureUtils.register(
		"clay_with_dripleaves",
		Feature.VEGETATION_PATCH
			.configured(
				new VegetationPatchConfiguration(
					BlockTags.LUSH_GROUND_REPLACEABLE.getName(),
					BlockStateProvider.simple(Blocks.CLAY),
					() -> DRIPLEAF.placed(),
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
	public static final ConfiguredFeature<?, ?> CLAY_POOL_WITH_DRIPLEAVES = FeatureUtils.register(
		"clay_pool_with_dripleaves",
		Feature.WATERLOGGED_VEGETATION_PATCH
			.configured(
				new VegetationPatchConfiguration(
					BlockTags.LUSH_GROUND_REPLACEABLE.getName(),
					BlockStateProvider.simple(Blocks.CLAY),
					() -> DRIPLEAF.placed(),
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
	public static final ConfiguredFeature<RandomBooleanFeatureConfiguration, ?> LUSH_CAVES_CLAY = FeatureUtils.register(
		"lush_caves_clay",
		Feature.RANDOM_BOOLEAN_SELECTOR
			.configured(new RandomBooleanFeatureConfiguration(() -> CLAY_WITH_DRIPLEAVES.placed(), () -> CLAY_POOL_WITH_DRIPLEAVES.placed()))
	);
	public static final ConfiguredFeature<VegetationPatchConfiguration, ?> MOSS_PATCH_CEILING = FeatureUtils.register(
		"moss_patch_ceiling",
		Feature.VEGETATION_PATCH
			.configured(
				new VegetationPatchConfiguration(
					BlockTags.MOSS_REPLACEABLE.getName(),
					BlockStateProvider.simple(Blocks.MOSS_BLOCK),
					() -> CAVE_VINE_IN_MOSS.placed(),
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
	public static final ConfiguredFeature<SimpleBlockConfiguration, ?> SPORE_BLOSSOM = FeatureUtils.register(
		"spore_blossom", Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.SPORE_BLOSSOM)))
	);
	public static final ConfiguredFeature<GeodeConfiguration, ?> AMETHYST_GEODE = FeatureUtils.register(
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
						List.of(
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
	);
	public static final ConfiguredFeature<SimpleBlockConfiguration, ?> SCULK_GROWTHS = FeatureUtils.register(
		"sculk_growths",
		Feature.SIMPLE_BLOCK
			.configured(
				new SimpleBlockConfiguration(
					new WeightedStateProvider(
						SimpleWeightedRandomList.<BlockState>builder().add(Blocks.SCULK_SENSOR.defaultBlockState(), 3).add(Blocks.SCULK_SHRIEKER.defaultBlockState(), 1)
					)
				)
			)
	);
	public static final ConfiguredFeature<SculkPatchConfiguration, ?> SCULK_PATCH_LARGE = FeatureUtils.register(
		"sculk_patch_large",
		Feature.SCULK_PATCH
			.configured(
				new SculkPatchConfiguration(
					BlockTags.SCULK_REPLACEABLE_WORLD_GEN.getName(),
					BlockStateProvider.simple(Blocks.SCULK.defaultBlockState()),
					() -> SCULK_GROWTHS,
					0.04F,
					CaveSurface.FLOOR,
					5,
					UniformInt.of(4, 5)
				)
			)
	);
	public static final ConfiguredFeature<SculkPatchConfiguration, ?> SCULK_PATCH = FeatureUtils.register(
		"sculk_patch",
		Feature.SCULK_PATCH
			.configured(
				new SculkPatchConfiguration(
					BlockTags.SCULK_REPLACEABLE_WORLD_GEN.getName(),
					BlockStateProvider.simple(Blocks.SCULK.defaultBlockState()),
					() -> SCULK_GROWTHS,
					0.035F,
					CaveSurface.FLOOR,
					5,
					UniformInt.of(2, 3)
				)
			)
	);
	public static final ConfiguredFeature<?, ?> SCULK_VEINS = FeatureUtils.register(
		"sculk_veins",
		Feature.GLOW_LICHEN
			.configured(
				new GlowLichenConfiguration(
					Blocks.SCULK_VEIN.defaultBlockState(),
					20,
					true,
					true,
					true,
					0.8F,
					ImmutableList.of(
						Blocks.STONE,
						Blocks.ANDESITE,
						Blocks.DIORITE,
						Blocks.GRANITE,
						Blocks.DRIPSTONE_BLOCK,
						Blocks.CALCITE,
						Blocks.TUFF,
						Blocks.DEEPSLATE,
						Blocks.DEEPSLATE_BRICKS,
						Blocks.DEEPSLATE_TILES,
						Blocks.COBBLED_DEEPSLATE,
						Blocks.POLISHED_DEEPSLATE,
						Blocks.CHISELED_DEEPSLATE
					)
				)
			)
	);
	public static final ConfiguredFeature<?, ?> SCULK_CATALYST_WITH_PATCHES = FeatureUtils.register(
		"sculk_catalyst_with_patches",
		Feature.CENTRAL_BLOCK_SCATTERED_FEATURES
			.configured(
				new CentralBlockScatteredFeaturesConfiguration(
					BlockTags.SCULK_REPLACEABLE_WORLD_GEN.getName(),
					BlockStateProvider.simple(Blocks.SCULK_CATALYST.defaultBlockState()),
					() -> SCULK_PATCH,
					() -> SCULK_PATCH_LARGE,
					CaveSurface.FLOOR,
					13,
					3,
					7,
					14
				)
			)
	);

	private static PlacedFeature makeDripleaf(Direction direction) {
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
					BlockPredicate.ONLY_IN_AIR_OR_WATER_PREDICATE,
					true
				)
			)
			.placed();
	}

	private static PlacedFeature makeSmallDripleaf() {
		return Feature.SIMPLE_BLOCK
			.configured(
				new SimpleBlockConfiguration(
					new WeightedStateProvider(
						SimpleWeightedRandomList.<BlockState>builder()
							.add(Blocks.SMALL_DRIPLEAF.defaultBlockState().setValue(SmallDripleafBlock.FACING, Direction.EAST), 1)
							.add(Blocks.SMALL_DRIPLEAF.defaultBlockState().setValue(SmallDripleafBlock.FACING, Direction.WEST), 1)
							.add(Blocks.SMALL_DRIPLEAF.defaultBlockState().setValue(SmallDripleafBlock.FACING, Direction.NORTH), 1)
							.add(Blocks.SMALL_DRIPLEAF.defaultBlockState().setValue(SmallDripleafBlock.FACING, Direction.SOUTH), 1)
					)
				)
			)
			.placed();
	}
}
