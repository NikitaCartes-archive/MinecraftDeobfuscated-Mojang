package net.minecraft.data.worldgen.placement;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.util.valueproviders.ClampedInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.NoiseBasedCountPlacement;
import net.minecraft.world.level.levelgen.placement.NoiseThresholdCountPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.placement.SurfaceWaterDepthFilter;

public class VegetationPlacements {
	public static final PlacedFeature BAMBOO_LIGHT = PlacementUtils.register(
		"bamboo_light",
		VegetationFeatures.BAMBOO_NO_PODZOL.placed(RarityFilter.onAverageOnceEvery(4), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome())
	);
	public static final PlacedFeature BAMBOO = PlacementUtils.register(
		"bamboo",
		VegetationFeatures.BAMBOO_SOME_PODZOL
			.placed(NoiseBasedCountPlacement.of(160, 80.0, 0.3), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome())
	);
	public static final PlacedFeature VINES = PlacementUtils.register(
		"vines",
		VegetationFeatures.VINES
			.placed(
				CountPlacement.of(127),
				InSquarePlacement.spread(),
				HeightRangePlacement.uniform(VerticalAnchor.absolute(64), VerticalAnchor.absolute(100)),
				BiomeFilter.biome()
			)
	);
	public static final PlacedFeature PATCH_SUNFLOWER = PlacementUtils.register(
		"patch_sunflower",
		VegetationFeatures.PATCH_SUNFLOWER.placed(RarityFilter.onAverageOnceEvery(3), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome())
	);
	public static final PlacedFeature PATCH_PUMPKIN = PlacementUtils.register(
		"patch_pumpkin",
		VegetationFeatures.PATCH_PUMPKIN.placed(RarityFilter.onAverageOnceEvery(2048), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome())
	);
	public static final PlacedFeature PATCH_GRASS_PLAIN = PlacementUtils.register(
		"patch_grass_plain",
		VegetationFeatures.PATCH_GRASS
			.placed(NoiseThresholdCountPlacement.of(-0.8, 5, 10), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome())
	);
	public static final PlacedFeature PATCH_GRASS_FOREST = PlacementUtils.register(
		"patch_grass_forest", VegetationFeatures.PATCH_GRASS.placed(worldSurfaceSquaredWithCount(2))
	);
	public static final PlacedFeature PATCH_GRASS_BADLANDS = PlacementUtils.register(
		"patch_grass_badlands", VegetationFeatures.PATCH_GRASS.placed(InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome())
	);
	public static final PlacedFeature PATCH_GRASS_SAVANNA = PlacementUtils.register(
		"patch_grass_savanna", VegetationFeatures.PATCH_GRASS.placed(worldSurfaceSquaredWithCount(20))
	);
	public static final PlacedFeature PATCH_GRASS_NORMAL = PlacementUtils.register(
		"patch_grass_normal", VegetationFeatures.PATCH_GRASS.placed(worldSurfaceSquaredWithCount(5))
	);
	public static final PlacedFeature PATCH_GRASS_TAIGA_2 = PlacementUtils.register(
		"patch_grass_taiga_2", VegetationFeatures.PATCH_TAIGA_GRASS.placed(InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome())
	);
	public static final PlacedFeature PATCH_GRASS_TAIGA = PlacementUtils.register(
		"patch_grass_taiga", VegetationFeatures.PATCH_TAIGA_GRASS.placed(worldSurfaceSquaredWithCount(7))
	);
	public static final PlacedFeature PATCH_GRASS_JUNGLE = PlacementUtils.register(
		"patch_grass_jungle", VegetationFeatures.PATCH_GRASS_JUNGLE.placed(worldSurfaceSquaredWithCount(25))
	);
	public static final PlacedFeature GRASS_BONEMEAL = PlacementUtils.register("grass_bonemeal", VegetationFeatures.SINGLE_PIECE_OF_GRASS.onlyWhenEmpty());
	public static final PlacedFeature PATCH_DEAD_BUSH_2 = PlacementUtils.register(
		"patch_dead_bush_2", VegetationFeatures.PATCH_DEAD_BUSH.placed(worldSurfaceSquaredWithCount(2))
	);
	public static final PlacedFeature PATCH_DEAD_BUSH = PlacementUtils.register(
		"patch_dead_bush", VegetationFeatures.PATCH_DEAD_BUSH.placed(InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome())
	);
	public static final PlacedFeature PATCH_DEAD_BUSH_BADLANDS = PlacementUtils.register(
		"patch_dead_bush_badlands", VegetationFeatures.PATCH_DEAD_BUSH.placed(worldSurfaceSquaredWithCount(20))
	);
	public static final PlacedFeature PATCH_MELON = PlacementUtils.register(
		"patch_melon",
		VegetationFeatures.PATCH_MELON.placed(RarityFilter.onAverageOnceEvery(64), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome())
	);
	public static final PlacedFeature PATCH_BERRY_COMMON = PlacementUtils.register(
		"patch_berry_common",
		VegetationFeatures.PATCH_BERRY_BUSH
			.placed(RarityFilter.onAverageOnceEvery(128), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome())
	);
	public static final PlacedFeature PATCH_BERRY_RARE = PlacementUtils.register(
		"patch_berry_rare",
		VegetationFeatures.PATCH_BERRY_BUSH
			.placed(RarityFilter.onAverageOnceEvery(1536), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome())
	);
	public static final PlacedFeature PATCH_WATERLILY = PlacementUtils.register(
		"patch_waterlily", VegetationFeatures.PATCH_WATERLILY.placed(worldSurfaceSquaredWithCount(4))
	);
	public static final PlacedFeature PATCH_TALL_GRASS_2 = PlacementUtils.register(
		"patch_tall_grass_2",
		VegetationFeatures.PATCH_TALL_GRASS
			.placed(
				NoiseThresholdCountPlacement.of(-0.8, 0, 7), RarityFilter.onAverageOnceEvery(32), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
			)
	);
	public static final PlacedFeature PATCH_TALL_GRASS = PlacementUtils.register(
		"patch_tall_grass",
		VegetationFeatures.PATCH_TALL_GRASS.placed(RarityFilter.onAverageOnceEvery(5), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome())
	);
	public static final PlacedFeature PATCH_LARGE_FERN = PlacementUtils.register(
		"patch_large_fern",
		VegetationFeatures.PATCH_LARGE_FERN.placed(RarityFilter.onAverageOnceEvery(5), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome())
	);
	public static final PlacedFeature PATCH_CACTUS_DESERT = PlacementUtils.register(
		"patch_cactus_desert",
		VegetationFeatures.PATCH_CACTUS.placed(RarityFilter.onAverageOnceEvery(6), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome())
	);
	public static final PlacedFeature PATCH_CACTUS_DECORATED = PlacementUtils.register(
		"patch_cactus_decorated",
		VegetationFeatures.PATCH_CACTUS.placed(RarityFilter.onAverageOnceEvery(13), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome())
	);
	public static final PlacedFeature PATCH_SUGAR_CANE_SWAMP = PlacementUtils.register(
		"patch_sugar_cane_swamp",
		VegetationFeatures.PATCH_SUGAR_CANE.placed(RarityFilter.onAverageOnceEvery(3), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome())
	);
	public static final PlacedFeature PATCH_SUGAR_CANE_DESERT = PlacementUtils.register(
		"patch_sugar_cane_desert", VegetationFeatures.PATCH_SUGAR_CANE.placed(InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome())
	);
	public static final PlacedFeature PATCH_SUGAR_CANE_BADLANDS = PlacementUtils.register(
		"patch_sugar_cane_badlands",
		VegetationFeatures.PATCH_SUGAR_CANE.placed(RarityFilter.onAverageOnceEvery(5), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome())
	);
	public static final PlacedFeature PATCH_SUGAR_CANE = PlacementUtils.register(
		"patch_sugar_cane",
		VegetationFeatures.PATCH_SUGAR_CANE.placed(RarityFilter.onAverageOnceEvery(6), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome())
	);
	public static final PlacedFeature BROWN_MUSHROOM_NETHER = PlacementUtils.register(
		"brown_mushroom_nether",
		VegetationFeatures.PATCH_BROWN_MUSHROOM
			.placed(RarityFilter.onAverageOnceEvery(2), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome())
	);
	public static final PlacedFeature RED_MUSHROOM_NETHER = PlacementUtils.register(
		"red_mushroom_nether",
		VegetationFeatures.PATCH_RED_MUSHROOM.placed(RarityFilter.onAverageOnceEvery(2), InSquarePlacement.spread(), PlacementUtils.FULL_RANGE, BiomeFilter.biome())
	);
	public static final PlacedFeature BROWN_MUSHROOM_NORMAL = PlacementUtils.register(
		"brown_mushroom_normal", VegetationFeatures.PATCH_BROWN_MUSHROOM.placed(getMushroomPlacement(256, null))
	);
	public static final PlacedFeature RED_MUSHROOM_NORMAL = PlacementUtils.register(
		"red_mushroom_normal", VegetationFeatures.PATCH_RED_MUSHROOM.placed(getMushroomPlacement(512, null))
	);
	public static final PlacedFeature BROWN_MUSHROOM_TAIGA = PlacementUtils.register(
		"brown_mushroom_taiga", VegetationFeatures.PATCH_BROWN_MUSHROOM.placed(getMushroomPlacement(4, null))
	);
	public static final PlacedFeature RED_MUSHROOM_TAIGA = PlacementUtils.register(
		"red_mushroom_taiga", VegetationFeatures.PATCH_RED_MUSHROOM.placed(getMushroomPlacement(256, null))
	);
	public static final PlacedFeature BROWN_MUSHROOM_OLD_GROWTH = PlacementUtils.register(
		"brown_mushroom_old_growth", VegetationFeatures.PATCH_BROWN_MUSHROOM.placed(getMushroomPlacement(4, CountPlacement.of(3)))
	);
	public static final PlacedFeature RED_MUSHROOM_OLD_GROWTH = PlacementUtils.register(
		"red_mushroom_old_growth", VegetationFeatures.PATCH_RED_MUSHROOM.placed(getMushroomPlacement(171, null))
	);
	public static final PlacedFeature BROWN_MUSHROOM_SWAMP = PlacementUtils.register(
		"brown_mushroom_swamp", VegetationFeatures.PATCH_BROWN_MUSHROOM.placed(getMushroomPlacement(0, CountPlacement.of(2)))
	);
	public static final PlacedFeature RED_MUSHROOM_SWAMP = PlacementUtils.register(
		"red_mushroom_swamp", VegetationFeatures.PATCH_RED_MUSHROOM.placed(getMushroomPlacement(64, null))
	);
	public static final PlacedFeature FLOWER_WARM = PlacementUtils.register(
		"flower_warm",
		VegetationFeatures.FLOWER_DEFAULT.placed(RarityFilter.onAverageOnceEvery(16), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome())
	);
	public static final PlacedFeature FLOWER_DEFAULT = PlacementUtils.register(
		"flower_default",
		VegetationFeatures.FLOWER_DEFAULT.placed(RarityFilter.onAverageOnceEvery(32), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome())
	);
	public static final PlacedFeature FLOWER_FLOWER_FOREST = PlacementUtils.register(
		"flower_flower_forest",
		VegetationFeatures.FLOWER_FLOWER_FOREST
			.placed(CountPlacement.of(3), RarityFilter.onAverageOnceEvery(2), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome())
	);
	public static final PlacedFeature FLOWER_SWAMP = PlacementUtils.register(
		"flower_swamp",
		VegetationFeatures.FLOWER_SWAMP.placed(RarityFilter.onAverageOnceEvery(32), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome())
	);
	public static final PlacedFeature FLOWER_PLAIN = PlacementUtils.register(
		"flower_plain",
		VegetationFeatures.FLOWER_PLAIN
			.placed(
				NoiseThresholdCountPlacement.of(-0.8, 15, 4),
				RarityFilter.onAverageOnceEvery(32),
				InSquarePlacement.spread(),
				PlacementUtils.HEIGHTMAP,
				BiomeFilter.biome()
			)
	);
	public static final PlacedFeature FLOWER_MEADOW = PlacementUtils.register(
		"flower_meadow", VegetationFeatures.FLOWER_MEADOW.placed(InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome())
	);
	public static final PlacementModifier TREE_THRESHOLD = SurfaceWaterDepthFilter.forMaxDepth(0);
	public static final PlacedFeature TREES_PLAINS = PlacementUtils.register(
		"trees_plains",
		VegetationFeatures.TREES_PLAINS
			.placed(
				PlacementUtils.countExtra(0, 0.05F, 1),
				InSquarePlacement.spread(),
				TREE_THRESHOLD,
				PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
				BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(Blocks.OAK_SAPLING.defaultBlockState(), BlockPos.ZERO)),
				BiomeFilter.biome()
			)
	);
	public static final PlacedFeature DARK_FOREST_VEGETATION = PlacementUtils.register(
		"dark_forest_vegetation",
		VegetationFeatures.DARK_FOREST_VEGETATION
			.placed(CountPlacement.of(16), InSquarePlacement.spread(), TREE_THRESHOLD, PlacementUtils.HEIGHTMAP_OCEAN_FLOOR, BiomeFilter.biome())
	);
	public static final PlacedFeature FLOWER_FOREST_FLOWERS = PlacementUtils.register(
		"flower_forest_flowers",
		VegetationFeatures.FOREST_FLOWERS
			.placed(
				RarityFilter.onAverageOnceEvery(7),
				InSquarePlacement.spread(),
				PlacementUtils.HEIGHTMAP,
				CountPlacement.of(ClampedInt.of(UniformInt.of(-1, 3), 0, 3)),
				BiomeFilter.biome()
			)
	);
	public static final PlacedFeature FOREST_FLOWERS = PlacementUtils.register(
		"forest_flowers",
		VegetationFeatures.FOREST_FLOWERS
			.placed(
				RarityFilter.onAverageOnceEvery(7),
				InSquarePlacement.spread(),
				PlacementUtils.HEIGHTMAP,
				CountPlacement.of(ClampedInt.of(UniformInt.of(-3, 1), 0, 1)),
				BiomeFilter.biome()
			)
	);
	public static final PlacedFeature TREES_FLOWER_FOREST = PlacementUtils.register(
		"trees_flower_forest", VegetationFeatures.TREES_FLOWER_FOREST.placed(treePlacement(PlacementUtils.countExtra(6, 0.1F, 1)))
	);
	public static final PlacedFeature TREES_MEADOW = PlacementUtils.register(
		"trees_meadow", VegetationFeatures.MEADOW_TREES.placed(treePlacement(RarityFilter.onAverageOnceEvery(100)))
	);
	public static final PlacedFeature TREES_TAIGA = PlacementUtils.register(
		"trees_taiga", VegetationFeatures.TREES_TAIGA.placed(treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)))
	);
	public static final PlacedFeature TREES_GROVE = PlacementUtils.register(
		"trees_grove", VegetationFeatures.TREES_GROVE.placed(treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)))
	);
	public static final PlacedFeature TREES_BADLANDS = PlacementUtils.register(
		"trees_badlands", TreeFeatures.OAK.placed(treePlacement(PlacementUtils.countExtra(5, 0.1F, 1), Blocks.OAK_SAPLING))
	);
	public static final PlacedFeature TREES_SNOWY = PlacementUtils.register(
		"trees_snowy", TreeFeatures.SPRUCE.placed(treePlacement(PlacementUtils.countExtra(0, 0.1F, 1), Blocks.SPRUCE_SAPLING))
	);
	public static final PlacedFeature TREES_SWAMP = PlacementUtils.register(
		"trees_swamp",
		TreeFeatures.SWAMP_OAK
			.placed(
				PlacementUtils.countExtra(2, 0.1F, 1),
				InSquarePlacement.spread(),
				SurfaceWaterDepthFilter.forMaxDepth(2),
				PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
				BiomeFilter.biome(),
				BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(Blocks.OAK_SAPLING.defaultBlockState(), BlockPos.ZERO))
			)
	);
	public static final PlacedFeature TREES_WINDSWEPT_SAVANNA = PlacementUtils.register(
		"trees_windswept_savanna", VegetationFeatures.TREES_SAVANNA.placed(treePlacement(PlacementUtils.countExtra(2, 0.1F, 1)))
	);
	public static final PlacedFeature TREES_SAVANNA = PlacementUtils.register(
		"trees_savanna", VegetationFeatures.TREES_SAVANNA.placed(treePlacement(PlacementUtils.countExtra(1, 0.1F, 1)))
	);
	public static final PlacedFeature BIRCH_TALL = PlacementUtils.register(
		"birch_tall", VegetationFeatures.BIRCH_TALL.placed(treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)))
	);
	public static final PlacedFeature TREES_BIRCH = PlacementUtils.register(
		"trees_birch", TreeFeatures.BIRCH_BEES_0002.placed(treePlacement(PlacementUtils.countExtra(10, 0.1F, 1), Blocks.BIRCH_SAPLING))
	);
	public static final PlacedFeature TREES_WINDSWEPT_FOREST = PlacementUtils.register(
		"trees_windswept_forest", VegetationFeatures.TREES_WINDSWEPT_HILLS.placed(treePlacement(PlacementUtils.countExtra(3, 0.1F, 1)))
	);
	public static final PlacedFeature TREES_WINDSWEPT_HILLS = PlacementUtils.register(
		"trees_windswept_hills", VegetationFeatures.TREES_WINDSWEPT_HILLS.placed(treePlacement(PlacementUtils.countExtra(0, 0.1F, 1)))
	);
	public static final PlacedFeature TREES_WATER = PlacementUtils.register(
		"trees_water", VegetationFeatures.TREES_WATER.placed(treePlacement(PlacementUtils.countExtra(0, 0.1F, 1)))
	);
	public static final PlacedFeature TREES_BIRCH_AND_OAK = PlacementUtils.register(
		"trees_birch_and_oak", VegetationFeatures.TREES_BIRCH_AND_OAK.placed(treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)))
	);
	public static final PlacedFeature TREES_SPARSE_JUNGLE = PlacementUtils.register(
		"trees_sparse_jungle", VegetationFeatures.TREES_SPARSE_JUNGLE.placed(treePlacement(PlacementUtils.countExtra(2, 0.1F, 1)))
	);
	public static final PlacedFeature TREES_OLD_GROWTH_SPRUCE_TAIGA = PlacementUtils.register(
		"trees_old_growth_spruce_taiga", VegetationFeatures.TREES_OLD_GROWTH_SPRUCE_TAIGA.placed(treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)))
	);
	public static final PlacedFeature TREES_OLD_GROWTH_PINE_TAIGA = PlacementUtils.register(
		"trees_old_growth_pine_taiga", VegetationFeatures.TREES_OLD_GROWTH_PINE_TAIGA.placed(treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)))
	);
	public static final PlacedFeature TREES_JUNGLE = PlacementUtils.register(
		"trees_jungle", VegetationFeatures.TREES_JUNGLE.placed(treePlacement(PlacementUtils.countExtra(50, 0.1F, 1)))
	);
	public static final PlacedFeature BAMBOO_VEGETATION = PlacementUtils.register(
		"bamboo_vegetation", VegetationFeatures.BAMBOO_VEGETATION.placed(treePlacement(PlacementUtils.countExtra(30, 0.1F, 1)))
	);
	public static final PlacedFeature MUSHROOM_ISLAND_VEGETATION = PlacementUtils.register(
		"mushroom_island_vegetation", VegetationFeatures.MUSHROOM_ISLAND_VEGETATION.placed(InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome())
	);

	public static List<PlacementModifier> worldSurfaceSquaredWithCount(int i) {
		return List.of(CountPlacement.of(i), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
	}

	private static List<PlacementModifier> getMushroomPlacement(int i, @Nullable PlacementModifier placementModifier) {
		Builder<PlacementModifier> builder = ImmutableList.builder();
		if (placementModifier != null) {
			builder.add(placementModifier);
		}

		if (i != 0) {
			builder.add(RarityFilter.onAverageOnceEvery(i));
		}

		builder.add(InSquarePlacement.spread());
		builder.add(PlacementUtils.HEIGHTMAP);
		builder.add(BiomeFilter.biome());
		return builder.build();
	}

	private static Builder<PlacementModifier> treePlacementBase(PlacementModifier placementModifier) {
		return ImmutableList.<PlacementModifier>builder()
			.add(placementModifier)
			.add(InSquarePlacement.spread())
			.add(TREE_THRESHOLD)
			.add(PlacementUtils.HEIGHTMAP_OCEAN_FLOOR)
			.add(BiomeFilter.biome());
	}

	public static List<PlacementModifier> treePlacement(PlacementModifier placementModifier) {
		return treePlacementBase(placementModifier).build();
	}

	public static List<PlacementModifier> treePlacement(PlacementModifier placementModifier, Block block) {
		return treePlacementBase(placementModifier)
			.add(BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(block.defaultBlockState(), BlockPos.ZERO)))
			.build();
	}
}
