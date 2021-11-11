package net.minecraft.data.worldgen.features;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.placement.TreePlacements;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.BiasedToBottomInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.DualNoiseProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.NoiseProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.NoiseThresholdProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.Fluids;

public class VegetationFeatures {
	public static final ConfiguredFeature<ProbabilityFeatureConfiguration, ?> BAMBOO_NO_PODZOL = FeatureUtils.register(
		"bamboo_no_podzol", Feature.BAMBOO.configured(new ProbabilityFeatureConfiguration(0.0F))
	);
	public static final ConfiguredFeature<ProbabilityFeatureConfiguration, ?> BAMBOO_SOME_PODZOL = FeatureUtils.register(
		"bamboo_some_podzol", Feature.BAMBOO.configured(new ProbabilityFeatureConfiguration(0.2F))
	);
	public static final ConfiguredFeature<NoneFeatureConfiguration, ?> VINES = FeatureUtils.register("vines", Feature.VINES.configured(FeatureConfiguration.NONE));
	public static final ConfiguredFeature<?, ?> PATCH_BROWN_MUSHROOM = FeatureUtils.register(
		"patch_brown_mushroom",
		Feature.RANDOM_PATCH
			.configured(
				FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.BROWN_MUSHROOM))))
			)
	);
	public static final ConfiguredFeature<?, ?> PATCH_RED_MUSHROOM = FeatureUtils.register(
		"patch_red_mushroom",
		Feature.RANDOM_PATCH
			.configured(
				FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.RED_MUSHROOM))))
			)
	);
	public static final ConfiguredFeature<RandomPatchConfiguration, ?> PATCH_SUNFLOWER = FeatureUtils.register(
		"patch_sunflower",
		Feature.RANDOM_PATCH
			.configured(
				FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.SUNFLOWER))))
			)
	);
	public static final ConfiguredFeature<RandomPatchConfiguration, ?> PATCH_PUMPKIN = FeatureUtils.register(
		"patch_pumpkin",
		Feature.RANDOM_PATCH
			.configured(
				FeatureUtils.simplePatchConfiguration(
					Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.PUMPKIN))), List.of(Blocks.GRASS_BLOCK)
				)
			)
	);
	public static final ConfiguredFeature<?, ?> PATCH_BERRY_BUSH = FeatureUtils.register(
		"patch_berry_bush",
		Feature.RANDOM_PATCH
			.configured(
				FeatureUtils.simplePatchConfiguration(
					Feature.SIMPLE_BLOCK
						.configured(
							new SimpleBlockConfiguration(
								BlockStateProvider.simple(Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, Integer.valueOf(3)))
							)
						),
					List.of(Blocks.GRASS_BLOCK)
				)
			)
	);
	public static final ConfiguredFeature<?, ?> PATCH_TAIGA_GRASS = FeatureUtils.register(
		"patch_taiga_grass",
		Feature.RANDOM_PATCH
			.configured(
				grassPatch(
					new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder().add(Blocks.GRASS.defaultBlockState(), 1).add(Blocks.FERN.defaultBlockState(), 4)),
					32
				)
			)
	);
	public static final ConfiguredFeature<RandomPatchConfiguration, ?> PATCH_GRASS = FeatureUtils.register(
		"patch_grass", Feature.RANDOM_PATCH.configured(grassPatch(BlockStateProvider.simple(Blocks.GRASS), 32))
	);
	public static final ConfiguredFeature<RandomPatchConfiguration, ?> PATCH_GRASS_JUNGLE = FeatureUtils.register(
		"patch_grass_jungle",
		Feature.RANDOM_PATCH
			.configured(
				new RandomPatchConfiguration(
					32,
					7,
					3,
					() -> Feature.SIMPLE_BLOCK
							.configured(
								new SimpleBlockConfiguration(
									new WeightedStateProvider(
										SimpleWeightedRandomList.<BlockState>builder().add(Blocks.GRASS.defaultBlockState(), 3).add(Blocks.FERN.defaultBlockState(), 1)
									)
								)
							)
							.filtered(
								BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE, BlockPredicate.not(BlockPredicate.matchesBlock(Blocks.PODZOL, new BlockPos(0, -1, 0))))
							)
				)
			)
	);
	public static final ConfiguredFeature<SimpleBlockConfiguration, ?> SINGLE_PIECE_OF_GRASS = FeatureUtils.register(
		"single_piece_of_grass", Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.GRASS.defaultBlockState())))
	);
	public static final ConfiguredFeature<RandomPatchConfiguration, ?> PATCH_DEAD_BUSH = FeatureUtils.register(
		"patch_dead_bush", Feature.RANDOM_PATCH.configured(grassPatch(BlockStateProvider.simple(Blocks.DEAD_BUSH), 4))
	);
	public static final ConfiguredFeature<RandomPatchConfiguration, ?> PATCH_MELON = FeatureUtils.register(
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
	);
	public static final ConfiguredFeature<RandomPatchConfiguration, ?> PATCH_WATERLILY = FeatureUtils.register(
		"patch_waterlily",
		Feature.RANDOM_PATCH
			.configured(
				new RandomPatchConfiguration(
					10, 7, 3, () -> Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.LILY_PAD))).onlyWhenEmpty()
				)
			)
	);
	public static final ConfiguredFeature<RandomPatchConfiguration, ?> PATCH_TALL_GRASS = FeatureUtils.register(
		"patch_tall_grass",
		Feature.RANDOM_PATCH
			.configured(
				FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.TALL_GRASS))))
			)
	);
	public static final ConfiguredFeature<RandomPatchConfiguration, ?> PATCH_LARGE_FERN = FeatureUtils.register(
		"patch_large_fern",
		Feature.RANDOM_PATCH
			.configured(
				FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.LARGE_FERN))))
			)
	);
	public static final ConfiguredFeature<?, ?> PATCH_CACTUS = FeatureUtils.register(
		"patch_cactus",
		Feature.RANDOM_PATCH
			.configured(
				FeatureUtils.simpleRandomPatchConfiguration(
					10,
					Feature.BLOCK_COLUMN
						.configured(BlockColumnConfiguration.simple(BiasedToBottomInt.of(1, 3), BlockStateProvider.simple(Blocks.CACTUS)))
						.placed(
							BlockPredicateFilter.forPredicate(
								BlockPredicate.allOf(BlockPredicate.ONLY_IN_AIR_PREDICATE, BlockPredicate.wouldSurvive(Blocks.CACTUS.defaultBlockState(), BlockPos.ZERO))
							)
						)
				)
			)
	);
	public static final ConfiguredFeature<RandomPatchConfiguration, ?> PATCH_SUGAR_CANE = FeatureUtils.register(
		"patch_sugar_cane",
		Feature.RANDOM_PATCH
			.configured(
				new RandomPatchConfiguration(
					20,
					4,
					0,
					() -> Feature.BLOCK_COLUMN
							.configured(BlockColumnConfiguration.simple(BiasedToBottomInt.of(2, 4), BlockStateProvider.simple(Blocks.SUGAR_CANE)))
							.placed(
								BlockPredicateFilter.forPredicate(
									BlockPredicate.allOf(
										BlockPredicate.matchesBlock(Blocks.AIR, BlockPos.ZERO),
										BlockPredicate.wouldSurvive(Blocks.SUGAR_CANE.defaultBlockState(), BlockPos.ZERO),
										BlockPredicate.anyOf(
											BlockPredicate.matchesFluids(List.of(Fluids.WATER, Fluids.FLOWING_WATER), new BlockPos(1, -1, 0)),
											BlockPredicate.matchesFluids(List.of(Fluids.WATER, Fluids.FLOWING_WATER), new BlockPos(-1, -1, 0)),
											BlockPredicate.matchesFluids(List.of(Fluids.WATER, Fluids.FLOWING_WATER), new BlockPos(0, -1, 1)),
											BlockPredicate.matchesFluids(List.of(Fluids.WATER, Fluids.FLOWING_WATER), new BlockPos(0, -1, -1))
										)
									)
								)
							)
				)
			)
	);
	public static final ConfiguredFeature<RandomPatchConfiguration, ?> FLOWER_DEFAULT = FeatureUtils.register(
		"flower_default",
		Feature.FLOWER
			.configured(
				grassPatch(
					new WeightedStateProvider(
						SimpleWeightedRandomList.<BlockState>builder().add(Blocks.POPPY.defaultBlockState(), 2).add(Blocks.DANDELION.defaultBlockState(), 1)
					),
					64
				)
			)
	);
	public static final ConfiguredFeature<RandomPatchConfiguration, ?> FLOWER_FLOWER_FOREST = FeatureUtils.register(
		"flower_flower_forest",
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
	);
	public static final ConfiguredFeature<RandomPatchConfiguration, ?> FLOWER_SWAMP = FeatureUtils.register(
		"flower_swamp",
		Feature.FLOWER
			.configured(
				new RandomPatchConfiguration(
					64, 6, 2, () -> Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.BLUE_ORCHID))).onlyWhenEmpty()
				)
			)
	);
	public static final ConfiguredFeature<?, ?> FLOWER_PLAIN = FeatureUtils.register(
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
										List.of(
											Blocks.ORANGE_TULIP.defaultBlockState(),
											Blocks.RED_TULIP.defaultBlockState(),
											Blocks.PINK_TULIP.defaultBlockState(),
											Blocks.WHITE_TULIP.defaultBlockState()
										),
										List.of(
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
	public static final ConfiguredFeature<RandomPatchConfiguration, ?> FLOWER_MEADOW = FeatureUtils.register(
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
										List.of(
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
	);
	public static final ConfiguredFeature<SimpleRandomFeatureConfiguration, ?> FOREST_FLOWERS = FeatureUtils.register(
		"forest_flowers",
		Feature.SIMPLE_RANDOM_SELECTOR
			.configured(
				new SimpleRandomFeatureConfiguration(
					List.of(
						(Supplier)() -> Feature.RANDOM_PATCH
								.configured(
									FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.LILAC))))
								)
								.placed(),
						(Supplier)() -> Feature.RANDOM_PATCH
								.configured(
									FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.ROSE_BUSH))))
								)
								.placed(),
						(Supplier)() -> Feature.RANDOM_PATCH
								.configured(
									FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.PEONY))))
								)
								.placed(),
						(Supplier)() -> Feature.NO_BONEMEAL_FLOWER
								.configured(
									FeatureUtils.simplePatchConfiguration(
										Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.LILY_OF_THE_VALLEY)))
									)
								)
								.placed()
					)
				)
			)
	);
	public static final ConfiguredFeature<RandomFeatureConfiguration, ?> DARK_FOREST_VEGETATION = FeatureUtils.register(
		"dark_forest_vegetation",
		Feature.RANDOM_SELECTOR
			.configured(
				new RandomFeatureConfiguration(
					List.of(
						new WeightedPlacedFeature(TreeFeatures.HUGE_BROWN_MUSHROOM.placed(), 0.025F),
						new WeightedPlacedFeature(TreeFeatures.HUGE_RED_MUSHROOM.placed(), 0.05F),
						new WeightedPlacedFeature(TreePlacements.DARK_OAK_CHECKED, 0.6666667F),
						new WeightedPlacedFeature(TreePlacements.BIRCH_CHECKED, 0.2F),
						new WeightedPlacedFeature(TreePlacements.FANCY_OAK_CHECKED, 0.1F)
					),
					TreePlacements.OAK_CHECKED
				)
			)
	);
	public static final ConfiguredFeature<RandomFeatureConfiguration, ?> TREES_FLOWER_FOREST = FeatureUtils.register(
		"trees_flower_forest",
		Feature.RANDOM_SELECTOR
			.configured(
				new RandomFeatureConfiguration(
					List.of(new WeightedPlacedFeature(TreePlacements.BIRCH_BEES_002, 0.2F), new WeightedPlacedFeature(TreePlacements.FANCY_OAK_BEES_002, 0.1F)),
					TreePlacements.OAK_BEES_002
				)
			)
	);
	public static final ConfiguredFeature<RandomFeatureConfiguration, ?> MEADOW_TREES = FeatureUtils.register(
		"meadow_trees",
		Feature.RANDOM_SELECTOR
			.configured(new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(TreePlacements.FANCY_OAK_BEES, 0.5F)), TreePlacements.SUPER_BIRCH_BEES))
	);
	public static final ConfiguredFeature<RandomFeatureConfiguration, ?> TREES_TAIGA = FeatureUtils.register(
		"trees_taiga",
		Feature.RANDOM_SELECTOR
			.configured(new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(TreePlacements.PINE_CHECKED, 0.33333334F)), TreePlacements.SPRUCE_CHECKED))
	);
	public static final ConfiguredFeature<RandomFeatureConfiguration, ?> TREES_GROVE = FeatureUtils.register(
		"trees_grove",
		Feature.RANDOM_SELECTOR
			.configured(new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(TreePlacements.PINE_ON_SNOW, 0.33333334F)), TreePlacements.SPRUCE_ON_SNOW))
	);
	public static final ConfiguredFeature<RandomFeatureConfiguration, ?> TREES_SAVANNA = FeatureUtils.register(
		"trees_savanna",
		Feature.RANDOM_SELECTOR
			.configured(new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(TreePlacements.ACACIA_CHECKED, 0.8F)), TreePlacements.OAK_CHECKED))
	);
	public static final ConfiguredFeature<RandomFeatureConfiguration, ?> BIRCH_TALL = FeatureUtils.register(
		"birch_tall",
		Feature.RANDOM_SELECTOR
			.configured(
				new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(TreePlacements.SUPER_BIRCH_BEES_0002, 0.5F)), TreePlacements.BIRCH_BEES_0002_PLACED)
			)
	);
	public static final ConfiguredFeature<RandomFeatureConfiguration, ?> TREES_WINDSWEPT_HILLS = FeatureUtils.register(
		"trees_windswept_hills",
		Feature.RANDOM_SELECTOR
			.configured(
				new RandomFeatureConfiguration(
					List.of(new WeightedPlacedFeature(TreePlacements.SPRUCE_CHECKED, 0.666F), new WeightedPlacedFeature(TreePlacements.FANCY_OAK_CHECKED, 0.1F)),
					TreePlacements.OAK_CHECKED
				)
			)
	);
	public static final ConfiguredFeature<RandomFeatureConfiguration, ?> TREES_WATER = FeatureUtils.register(
		"trees_water",
		Feature.RANDOM_SELECTOR
			.configured(new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(TreePlacements.FANCY_OAK_CHECKED, 0.1F)), TreePlacements.OAK_CHECKED))
	);
	public static final ConfiguredFeature<RandomFeatureConfiguration, ?> TREES_BIRCH_AND_OAK = FeatureUtils.register(
		"trees_birch_and_oak",
		Feature.RANDOM_SELECTOR
			.configured(
				new RandomFeatureConfiguration(
					List.of(new WeightedPlacedFeature(TreePlacements.BIRCH_BEES_0002_PLACED, 0.2F), new WeightedPlacedFeature(TreePlacements.FANCY_OAK_BEES_0002, 0.1F)),
					TreePlacements.OAK_BEES_0002
				)
			)
	);
	public static final ConfiguredFeature<RandomFeatureConfiguration, ?> TREES_PLAINS = FeatureUtils.register(
		"trees_plains",
		Feature.RANDOM_SELECTOR
			.configured(
				new RandomFeatureConfiguration(
					List.of(new WeightedPlacedFeature(TreeFeatures.FANCY_OAK_BEES_005.placed(), 0.33333334F)), TreeFeatures.OAK_BEES_005.placed()
				)
			)
	);
	public static final ConfiguredFeature<RandomFeatureConfiguration, ?> TREES_SPARSE_JUNGLE = FeatureUtils.register(
		"trees_sparse_jungle",
		Feature.RANDOM_SELECTOR
			.configured(
				new RandomFeatureConfiguration(
					List.of(new WeightedPlacedFeature(TreePlacements.FANCY_OAK_CHECKED, 0.1F), new WeightedPlacedFeature(TreePlacements.JUNGLE_BUSH, 0.5F)),
					TreePlacements.JUNGLE_TREE_CHECKED
				)
			)
	);
	public static final ConfiguredFeature<RandomFeatureConfiguration, ?> TREES_OLD_GROWTH_SPRUCE_TAIGA = FeatureUtils.register(
		"trees_old_growth_spruce_taiga",
		Feature.RANDOM_SELECTOR
			.configured(
				new RandomFeatureConfiguration(
					List.of(new WeightedPlacedFeature(TreePlacements.MEGA_SPRUCE_CHECKED, 0.33333334F), new WeightedPlacedFeature(TreePlacements.PINE_CHECKED, 0.33333334F)),
					TreePlacements.SPRUCE_CHECKED
				)
			)
	);
	public static final ConfiguredFeature<RandomFeatureConfiguration, ?> TREES_OLD_GROWTH_PINE_TAIGA = FeatureUtils.register(
		"trees_old_growth_pine_taiga",
		Feature.RANDOM_SELECTOR
			.configured(
				new RandomFeatureConfiguration(
					List.of(
						new WeightedPlacedFeature(TreePlacements.MEGA_SPRUCE_CHECKED, 0.025641026F),
						new WeightedPlacedFeature(TreePlacements.MEGA_PINE_CHECKED, 0.30769232F),
						new WeightedPlacedFeature(TreePlacements.PINE_CHECKED, 0.33333334F)
					),
					TreePlacements.SPRUCE_CHECKED
				)
			)
	);
	public static final ConfiguredFeature<RandomFeatureConfiguration, ?> TREES_JUNGLE = FeatureUtils.register(
		"trees_jungle",
		Feature.RANDOM_SELECTOR
			.configured(
				new RandomFeatureConfiguration(
					List.of(
						new WeightedPlacedFeature(TreePlacements.FANCY_OAK_CHECKED, 0.1F),
						new WeightedPlacedFeature(TreePlacements.JUNGLE_BUSH, 0.5F),
						new WeightedPlacedFeature(TreePlacements.MEGA_JUNGLE_TREE_CHECKED, 0.33333334F)
					),
					TreePlacements.JUNGLE_TREE_CHECKED
				)
			)
	);
	public static final ConfiguredFeature<RandomFeatureConfiguration, ?> BAMBOO_VEGETATION = FeatureUtils.register(
		"bamboo_vegetation",
		Feature.RANDOM_SELECTOR
			.configured(
				new RandomFeatureConfiguration(
					List.of(
						new WeightedPlacedFeature(TreePlacements.FANCY_OAK_CHECKED, 0.05F),
						new WeightedPlacedFeature(TreePlacements.JUNGLE_BUSH, 0.15F),
						new WeightedPlacedFeature(TreePlacements.MEGA_JUNGLE_TREE_CHECKED, 0.7F)
					),
					PATCH_GRASS_JUNGLE.placed()
				)
			)
	);
	public static final ConfiguredFeature<RandomBooleanFeatureConfiguration, ?> MUSHROOM_ISLAND_VEGETATION = FeatureUtils.register(
		"mushroom_island_vegetation",
		Feature.RANDOM_BOOLEAN_SELECTOR
			.configured(new RandomBooleanFeatureConfiguration(() -> TreeFeatures.HUGE_RED_MUSHROOM, () -> TreeFeatures.HUGE_BROWN_MUSHROOM))
	);

	private static RandomPatchConfiguration grassPatch(BlockStateProvider blockStateProvider, int i) {
		return FeatureUtils.simpleRandomPatchConfiguration(i, Feature.SIMPLE_BLOCK.configured(new SimpleBlockConfiguration(blockStateProvider)).onlyWhenEmpty());
	}
}
