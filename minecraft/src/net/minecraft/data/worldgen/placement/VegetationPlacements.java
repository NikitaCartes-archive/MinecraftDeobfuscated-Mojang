package net.minecraft.data.worldgen.placement;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ClampedInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
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
	public static final ResourceKey<PlacedFeature> BAMBOO_LIGHT = PlacementUtils.createKey("bamboo_light");
	public static final ResourceKey<PlacedFeature> BAMBOO = PlacementUtils.createKey("bamboo");
	public static final ResourceKey<PlacedFeature> VINES = PlacementUtils.createKey("vines");
	public static final ResourceKey<PlacedFeature> PATCH_SUNFLOWER = PlacementUtils.createKey("patch_sunflower");
	public static final ResourceKey<PlacedFeature> PATCH_PUMPKIN = PlacementUtils.createKey("patch_pumpkin");
	public static final ResourceKey<PlacedFeature> PATCH_GRASS_PLAIN = PlacementUtils.createKey("patch_grass_plain");
	public static final ResourceKey<PlacedFeature> PATCH_GRASS_FOREST = PlacementUtils.createKey("patch_grass_forest");
	public static final ResourceKey<PlacedFeature> PATCH_GRASS_BADLANDS = PlacementUtils.createKey("patch_grass_badlands");
	public static final ResourceKey<PlacedFeature> PATCH_GRASS_SAVANNA = PlacementUtils.createKey("patch_grass_savanna");
	public static final ResourceKey<PlacedFeature> PATCH_GRASS_NORMAL = PlacementUtils.createKey("patch_grass_normal");
	public static final ResourceKey<PlacedFeature> PATCH_GRASS_TAIGA_2 = PlacementUtils.createKey("patch_grass_taiga_2");
	public static final ResourceKey<PlacedFeature> PATCH_GRASS_TAIGA = PlacementUtils.createKey("patch_grass_taiga");
	public static final ResourceKey<PlacedFeature> PATCH_GRASS_JUNGLE = PlacementUtils.createKey("patch_grass_jungle");
	public static final ResourceKey<PlacedFeature> GRASS_BONEMEAL = PlacementUtils.createKey("grass_bonemeal");
	public static final ResourceKey<PlacedFeature> PATCH_DEAD_BUSH_2 = PlacementUtils.createKey("patch_dead_bush_2");
	public static final ResourceKey<PlacedFeature> PATCH_DEAD_BUSH = PlacementUtils.createKey("patch_dead_bush");
	public static final ResourceKey<PlacedFeature> PATCH_DEAD_BUSH_BADLANDS = PlacementUtils.createKey("patch_dead_bush_badlands");
	public static final ResourceKey<PlacedFeature> PATCH_MELON = PlacementUtils.createKey("patch_melon");
	public static final ResourceKey<PlacedFeature> PATCH_MELON_SPARSE = PlacementUtils.createKey("patch_melon_sparse");
	public static final ResourceKey<PlacedFeature> PATCH_BERRY_COMMON = PlacementUtils.createKey("patch_berry_common");
	public static final ResourceKey<PlacedFeature> PATCH_BERRY_RARE = PlacementUtils.createKey("patch_berry_rare");
	public static final ResourceKey<PlacedFeature> PATCH_WATERLILY = PlacementUtils.createKey("patch_waterlily");
	public static final ResourceKey<PlacedFeature> PATCH_TALL_GRASS_2 = PlacementUtils.createKey("patch_tall_grass_2");
	public static final ResourceKey<PlacedFeature> PATCH_TALL_GRASS = PlacementUtils.createKey("patch_tall_grass");
	public static final ResourceKey<PlacedFeature> PATCH_LARGE_FERN = PlacementUtils.createKey("patch_large_fern");
	public static final ResourceKey<PlacedFeature> PATCH_CACTUS_DESERT = PlacementUtils.createKey("patch_cactus_desert");
	public static final ResourceKey<PlacedFeature> PATCH_CACTUS_DECORATED = PlacementUtils.createKey("patch_cactus_decorated");
	public static final ResourceKey<PlacedFeature> PATCH_SUGAR_CANE_SWAMP = PlacementUtils.createKey("patch_sugar_cane_swamp");
	public static final ResourceKey<PlacedFeature> PATCH_SUGAR_CANE_DESERT = PlacementUtils.createKey("patch_sugar_cane_desert");
	public static final ResourceKey<PlacedFeature> PATCH_SUGAR_CANE_BADLANDS = PlacementUtils.createKey("patch_sugar_cane_badlands");
	public static final ResourceKey<PlacedFeature> PATCH_SUGAR_CANE = PlacementUtils.createKey("patch_sugar_cane");
	public static final ResourceKey<PlacedFeature> BROWN_MUSHROOM_NETHER = PlacementUtils.createKey("brown_mushroom_nether");
	public static final ResourceKey<PlacedFeature> RED_MUSHROOM_NETHER = PlacementUtils.createKey("red_mushroom_nether");
	public static final ResourceKey<PlacedFeature> BROWN_MUSHROOM_NORMAL = PlacementUtils.createKey("brown_mushroom_normal");
	public static final ResourceKey<PlacedFeature> RED_MUSHROOM_NORMAL = PlacementUtils.createKey("red_mushroom_normal");
	public static final ResourceKey<PlacedFeature> BROWN_MUSHROOM_TAIGA = PlacementUtils.createKey("brown_mushroom_taiga");
	public static final ResourceKey<PlacedFeature> RED_MUSHROOM_TAIGA = PlacementUtils.createKey("red_mushroom_taiga");
	public static final ResourceKey<PlacedFeature> BROWN_MUSHROOM_OLD_GROWTH = PlacementUtils.createKey("brown_mushroom_old_growth");
	public static final ResourceKey<PlacedFeature> RED_MUSHROOM_OLD_GROWTH = PlacementUtils.createKey("red_mushroom_old_growth");
	public static final ResourceKey<PlacedFeature> BROWN_MUSHROOM_SWAMP = PlacementUtils.createKey("brown_mushroom_swamp");
	public static final ResourceKey<PlacedFeature> RED_MUSHROOM_SWAMP = PlacementUtils.createKey("red_mushroom_swamp");
	public static final ResourceKey<PlacedFeature> FLOWER_WARM = PlacementUtils.createKey("flower_warm");
	public static final ResourceKey<PlacedFeature> FLOWER_DEFAULT = PlacementUtils.createKey("flower_default");
	public static final ResourceKey<PlacedFeature> FLOWER_FLOWER_FOREST = PlacementUtils.createKey("flower_flower_forest");
	public static final ResourceKey<PlacedFeature> FLOWER_SWAMP = PlacementUtils.createKey("flower_swamp");
	public static final ResourceKey<PlacedFeature> FLOWER_PLAINS = PlacementUtils.createKey("flower_plains");
	public static final ResourceKey<PlacedFeature> FLOWER_MEADOW = PlacementUtils.createKey("flower_meadow");
	public static final ResourceKey<PlacedFeature> TREES_PLAINS = PlacementUtils.createKey("trees_plains");
	public static final ResourceKey<PlacedFeature> DARK_FOREST_VEGETATION = PlacementUtils.createKey("dark_forest_vegetation");
	public static final ResourceKey<PlacedFeature> FLOWER_FOREST_FLOWERS = PlacementUtils.createKey("flower_forest_flowers");
	public static final ResourceKey<PlacedFeature> FOREST_FLOWERS = PlacementUtils.createKey("forest_flowers");
	public static final ResourceKey<PlacedFeature> TREES_FLOWER_FOREST = PlacementUtils.createKey("trees_flower_forest");
	public static final ResourceKey<PlacedFeature> TREES_MEADOW = PlacementUtils.createKey("trees_meadow");
	public static final ResourceKey<PlacedFeature> TREES_TAIGA = PlacementUtils.createKey("trees_taiga");
	public static final ResourceKey<PlacedFeature> TREES_GROVE = PlacementUtils.createKey("trees_grove");
	public static final ResourceKey<PlacedFeature> TREES_BADLANDS = PlacementUtils.createKey("trees_badlands");
	public static final ResourceKey<PlacedFeature> TREES_SNOWY = PlacementUtils.createKey("trees_snowy");
	public static final ResourceKey<PlacedFeature> TREES_SWAMP = PlacementUtils.createKey("trees_swamp");
	public static final ResourceKey<PlacedFeature> TREES_WINDSWEPT_SAVANNA = PlacementUtils.createKey("trees_windswept_savanna");
	public static final ResourceKey<PlacedFeature> TREES_SAVANNA = PlacementUtils.createKey("trees_savanna");
	public static final ResourceKey<PlacedFeature> BIRCH_TALL = PlacementUtils.createKey("birch_tall");
	public static final ResourceKey<PlacedFeature> TREES_BIRCH = PlacementUtils.createKey("trees_birch");
	public static final ResourceKey<PlacedFeature> TREES_WINDSWEPT_FOREST = PlacementUtils.createKey("trees_windswept_forest");
	public static final ResourceKey<PlacedFeature> TREES_WINDSWEPT_HILLS = PlacementUtils.createKey("trees_windswept_hills");
	public static final ResourceKey<PlacedFeature> TREES_WATER = PlacementUtils.createKey("trees_water");
	public static final ResourceKey<PlacedFeature> TREES_BIRCH_AND_OAK = PlacementUtils.createKey("trees_birch_and_oak");
	public static final ResourceKey<PlacedFeature> TREES_SPARSE_JUNGLE = PlacementUtils.createKey("trees_sparse_jungle");
	public static final ResourceKey<PlacedFeature> TREES_OLD_GROWTH_SPRUCE_TAIGA = PlacementUtils.createKey("trees_old_growth_spruce_taiga");
	public static final ResourceKey<PlacedFeature> TREES_OLD_GROWTH_PINE_TAIGA = PlacementUtils.createKey("trees_old_growth_pine_taiga");
	public static final ResourceKey<PlacedFeature> TREES_JUNGLE = PlacementUtils.createKey("trees_jungle");
	public static final ResourceKey<PlacedFeature> BAMBOO_VEGETATION = PlacementUtils.createKey("bamboo_vegetation");
	public static final ResourceKey<PlacedFeature> MUSHROOM_ISLAND_VEGETATION = PlacementUtils.createKey("mushroom_island_vegetation");
	public static final ResourceKey<PlacedFeature> TREES_MANGROVE = PlacementUtils.createKey("trees_mangrove");
	private static final PlacementModifier TREE_THRESHOLD = SurfaceWaterDepthFilter.forMaxDepth(0);

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

	public static void bootstrap(BootstapContext<PlacedFeature> bootstapContext) {
		HolderGetter<ConfiguredFeature<?, ?>> holderGetter = bootstapContext.lookup(Registry.CONFIGURED_FEATURE_REGISTRY);
		Holder<ConfiguredFeature<?, ?>> holder = holderGetter.getOrThrow(VegetationFeatures.BAMBOO_NO_PODZOL);
		Holder<ConfiguredFeature<?, ?>> holder2 = holderGetter.getOrThrow(VegetationFeatures.BAMBOO_SOME_PODZOL);
		Holder<ConfiguredFeature<?, ?>> holder3 = holderGetter.getOrThrow(VegetationFeatures.VINES);
		Holder<ConfiguredFeature<?, ?>> holder4 = holderGetter.getOrThrow(VegetationFeatures.PATCH_SUNFLOWER);
		Holder<ConfiguredFeature<?, ?>> holder5 = holderGetter.getOrThrow(VegetationFeatures.PATCH_PUMPKIN);
		Holder<ConfiguredFeature<?, ?>> holder6 = holderGetter.getOrThrow(VegetationFeatures.PATCH_GRASS);
		Holder<ConfiguredFeature<?, ?>> holder7 = holderGetter.getOrThrow(VegetationFeatures.PATCH_TAIGA_GRASS);
		Holder<ConfiguredFeature<?, ?>> holder8 = holderGetter.getOrThrow(VegetationFeatures.PATCH_GRASS_JUNGLE);
		Holder<ConfiguredFeature<?, ?>> holder9 = holderGetter.getOrThrow(VegetationFeatures.SINGLE_PIECE_OF_GRASS);
		Holder<ConfiguredFeature<?, ?>> holder10 = holderGetter.getOrThrow(VegetationFeatures.PATCH_DEAD_BUSH);
		Holder<ConfiguredFeature<?, ?>> holder11 = holderGetter.getOrThrow(VegetationFeatures.PATCH_MELON);
		Holder<ConfiguredFeature<?, ?>> holder12 = holderGetter.getOrThrow(VegetationFeatures.PATCH_BERRY_BUSH);
		Holder<ConfiguredFeature<?, ?>> holder13 = holderGetter.getOrThrow(VegetationFeatures.PATCH_WATERLILY);
		Holder<ConfiguredFeature<?, ?>> holder14 = holderGetter.getOrThrow(VegetationFeatures.PATCH_TALL_GRASS);
		Holder<ConfiguredFeature<?, ?>> holder15 = holderGetter.getOrThrow(VegetationFeatures.PATCH_LARGE_FERN);
		Holder<ConfiguredFeature<?, ?>> holder16 = holderGetter.getOrThrow(VegetationFeatures.PATCH_CACTUS);
		Holder<ConfiguredFeature<?, ?>> holder17 = holderGetter.getOrThrow(VegetationFeatures.PATCH_SUGAR_CANE);
		Holder<ConfiguredFeature<?, ?>> holder18 = holderGetter.getOrThrow(VegetationFeatures.PATCH_BROWN_MUSHROOM);
		Holder<ConfiguredFeature<?, ?>> holder19 = holderGetter.getOrThrow(VegetationFeatures.PATCH_RED_MUSHROOM);
		Holder<ConfiguredFeature<?, ?>> holder20 = holderGetter.getOrThrow(VegetationFeatures.FLOWER_DEFAULT);
		Holder<ConfiguredFeature<?, ?>> holder21 = holderGetter.getOrThrow(VegetationFeatures.FLOWER_FLOWER_FOREST);
		Holder<ConfiguredFeature<?, ?>> holder22 = holderGetter.getOrThrow(VegetationFeatures.FLOWER_SWAMP);
		Holder<ConfiguredFeature<?, ?>> holder23 = holderGetter.getOrThrow(VegetationFeatures.FLOWER_PLAIN);
		Holder<ConfiguredFeature<?, ?>> holder24 = holderGetter.getOrThrow(VegetationFeatures.FLOWER_MEADOW);
		Holder<ConfiguredFeature<?, ?>> holder25 = holderGetter.getOrThrow(VegetationFeatures.TREES_PLAINS);
		Holder<ConfiguredFeature<?, ?>> holder26 = holderGetter.getOrThrow(VegetationFeatures.DARK_FOREST_VEGETATION);
		Holder<ConfiguredFeature<?, ?>> holder27 = holderGetter.getOrThrow(VegetationFeatures.FOREST_FLOWERS);
		Holder<ConfiguredFeature<?, ?>> holder28 = holderGetter.getOrThrow(VegetationFeatures.TREES_FLOWER_FOREST);
		Holder<ConfiguredFeature<?, ?>> holder29 = holderGetter.getOrThrow(VegetationFeatures.MEADOW_TREES);
		Holder<ConfiguredFeature<?, ?>> holder30 = holderGetter.getOrThrow(VegetationFeatures.TREES_TAIGA);
		Holder<ConfiguredFeature<?, ?>> holder31 = holderGetter.getOrThrow(VegetationFeatures.TREES_GROVE);
		Holder<ConfiguredFeature<?, ?>> holder32 = holderGetter.getOrThrow(TreeFeatures.OAK);
		Holder<ConfiguredFeature<?, ?>> holder33 = holderGetter.getOrThrow(TreeFeatures.SPRUCE);
		Holder<ConfiguredFeature<?, ?>> holder34 = holderGetter.getOrThrow(TreeFeatures.SWAMP_OAK);
		Holder<ConfiguredFeature<?, ?>> holder35 = holderGetter.getOrThrow(VegetationFeatures.TREES_SAVANNA);
		Holder<ConfiguredFeature<?, ?>> holder36 = holderGetter.getOrThrow(VegetationFeatures.BIRCH_TALL);
		Holder<ConfiguredFeature<?, ?>> holder37 = holderGetter.getOrThrow(TreeFeatures.BIRCH_BEES_0002);
		Holder<ConfiguredFeature<?, ?>> holder38 = holderGetter.getOrThrow(VegetationFeatures.TREES_WINDSWEPT_HILLS);
		Holder<ConfiguredFeature<?, ?>> holder39 = holderGetter.getOrThrow(VegetationFeatures.TREES_WATER);
		Holder<ConfiguredFeature<?, ?>> holder40 = holderGetter.getOrThrow(VegetationFeatures.TREES_BIRCH_AND_OAK);
		Holder<ConfiguredFeature<?, ?>> holder41 = holderGetter.getOrThrow(VegetationFeatures.TREES_SPARSE_JUNGLE);
		Holder<ConfiguredFeature<?, ?>> holder42 = holderGetter.getOrThrow(VegetationFeatures.TREES_OLD_GROWTH_SPRUCE_TAIGA);
		Holder<ConfiguredFeature<?, ?>> holder43 = holderGetter.getOrThrow(VegetationFeatures.TREES_OLD_GROWTH_PINE_TAIGA);
		Holder<ConfiguredFeature<?, ?>> holder44 = holderGetter.getOrThrow(VegetationFeatures.TREES_JUNGLE);
		Holder<ConfiguredFeature<?, ?>> holder45 = holderGetter.getOrThrow(VegetationFeatures.BAMBOO_VEGETATION);
		Holder<ConfiguredFeature<?, ?>> holder46 = holderGetter.getOrThrow(VegetationFeatures.MUSHROOM_ISLAND_VEGETATION);
		Holder<ConfiguredFeature<?, ?>> holder47 = holderGetter.getOrThrow(VegetationFeatures.MANGROVE_VEGETATION);
		PlacementUtils.register(
			bootstapContext, BAMBOO_LIGHT, holder, RarityFilter.onAverageOnceEvery(4), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstapContext,
			BAMBOO,
			holder2,
			NoiseBasedCountPlacement.of(160, 80.0, 0.3),
			InSquarePlacement.spread(),
			PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
			BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstapContext,
			VINES,
			holder3,
			CountPlacement.of(127),
			InSquarePlacement.spread(),
			HeightRangePlacement.uniform(VerticalAnchor.absolute(64), VerticalAnchor.absolute(100)),
			BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstapContext, PATCH_SUNFLOWER, holder4, RarityFilter.onAverageOnceEvery(3), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstapContext, PATCH_PUMPKIN, holder5, RarityFilter.onAverageOnceEvery(300), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstapContext,
			PATCH_GRASS_PLAIN,
			holder6,
			NoiseThresholdCountPlacement.of(-0.8, 5, 10),
			InSquarePlacement.spread(),
			PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
			BiomeFilter.biome()
		);
		PlacementUtils.register(bootstapContext, PATCH_GRASS_FOREST, holder6, worldSurfaceSquaredWithCount(2));
		PlacementUtils.register(
			bootstapContext, PATCH_GRASS_BADLANDS, holder6, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome()
		);
		PlacementUtils.register(bootstapContext, PATCH_GRASS_SAVANNA, holder6, worldSurfaceSquaredWithCount(20));
		PlacementUtils.register(bootstapContext, PATCH_GRASS_NORMAL, holder6, worldSurfaceSquaredWithCount(5));
		PlacementUtils.register(
			bootstapContext, PATCH_GRASS_TAIGA_2, holder7, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome()
		);
		PlacementUtils.register(bootstapContext, PATCH_GRASS_TAIGA, holder7, worldSurfaceSquaredWithCount(7));
		PlacementUtils.register(bootstapContext, PATCH_GRASS_JUNGLE, holder8, worldSurfaceSquaredWithCount(25));
		PlacementUtils.register(bootstapContext, GRASS_BONEMEAL, holder9, PlacementUtils.isEmpty());
		PlacementUtils.register(bootstapContext, PATCH_DEAD_BUSH_2, holder10, worldSurfaceSquaredWithCount(2));
		PlacementUtils.register(bootstapContext, PATCH_DEAD_BUSH, holder10, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP_WORLD_SURFACE, BiomeFilter.biome());
		PlacementUtils.register(bootstapContext, PATCH_DEAD_BUSH_BADLANDS, holder10, worldSurfaceSquaredWithCount(20));
		PlacementUtils.register(
			bootstapContext, PATCH_MELON, holder11, RarityFilter.onAverageOnceEvery(6), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstapContext,
			PATCH_MELON_SPARSE,
			holder11,
			RarityFilter.onAverageOnceEvery(64),
			InSquarePlacement.spread(),
			PlacementUtils.HEIGHTMAP,
			BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstapContext,
			PATCH_BERRY_COMMON,
			holder12,
			RarityFilter.onAverageOnceEvery(32),
			InSquarePlacement.spread(),
			PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
			BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstapContext,
			PATCH_BERRY_RARE,
			holder12,
			RarityFilter.onAverageOnceEvery(384),
			InSquarePlacement.spread(),
			PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
			BiomeFilter.biome()
		);
		PlacementUtils.register(bootstapContext, PATCH_WATERLILY, holder13, worldSurfaceSquaredWithCount(4));
		PlacementUtils.register(
			bootstapContext,
			PATCH_TALL_GRASS_2,
			holder14,
			NoiseThresholdCountPlacement.of(-0.8, 0, 7),
			RarityFilter.onAverageOnceEvery(32),
			InSquarePlacement.spread(),
			PlacementUtils.HEIGHTMAP,
			BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstapContext, PATCH_TALL_GRASS, holder14, RarityFilter.onAverageOnceEvery(5), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstapContext, PATCH_LARGE_FERN, holder15, RarityFilter.onAverageOnceEvery(5), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstapContext,
			PATCH_CACTUS_DESERT,
			holder16,
			RarityFilter.onAverageOnceEvery(6),
			InSquarePlacement.spread(),
			PlacementUtils.HEIGHTMAP,
			BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstapContext,
			PATCH_CACTUS_DECORATED,
			holder16,
			RarityFilter.onAverageOnceEvery(13),
			InSquarePlacement.spread(),
			PlacementUtils.HEIGHTMAP,
			BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstapContext,
			PATCH_SUGAR_CANE_SWAMP,
			holder17,
			RarityFilter.onAverageOnceEvery(3),
			InSquarePlacement.spread(),
			PlacementUtils.HEIGHTMAP,
			BiomeFilter.biome()
		);
		PlacementUtils.register(bootstapContext, PATCH_SUGAR_CANE_DESERT, holder17, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
		PlacementUtils.register(
			bootstapContext,
			PATCH_SUGAR_CANE_BADLANDS,
			holder17,
			RarityFilter.onAverageOnceEvery(5),
			InSquarePlacement.spread(),
			PlacementUtils.HEIGHTMAP,
			BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstapContext, PATCH_SUGAR_CANE, holder17, RarityFilter.onAverageOnceEvery(6), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstapContext,
			BROWN_MUSHROOM_NETHER,
			holder18,
			RarityFilter.onAverageOnceEvery(2),
			InSquarePlacement.spread(),
			PlacementUtils.FULL_RANGE,
			BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstapContext,
			RED_MUSHROOM_NETHER,
			holder19,
			RarityFilter.onAverageOnceEvery(2),
			InSquarePlacement.spread(),
			PlacementUtils.FULL_RANGE,
			BiomeFilter.biome()
		);
		PlacementUtils.register(bootstapContext, BROWN_MUSHROOM_NORMAL, holder18, getMushroomPlacement(256, null));
		PlacementUtils.register(bootstapContext, RED_MUSHROOM_NORMAL, holder19, getMushroomPlacement(512, null));
		PlacementUtils.register(bootstapContext, BROWN_MUSHROOM_TAIGA, holder18, getMushroomPlacement(4, null));
		PlacementUtils.register(bootstapContext, RED_MUSHROOM_TAIGA, holder19, getMushroomPlacement(256, null));
		PlacementUtils.register(bootstapContext, BROWN_MUSHROOM_OLD_GROWTH, holder18, getMushroomPlacement(4, CountPlacement.of(3)));
		PlacementUtils.register(bootstapContext, RED_MUSHROOM_OLD_GROWTH, holder19, getMushroomPlacement(171, null));
		PlacementUtils.register(bootstapContext, BROWN_MUSHROOM_SWAMP, holder18, getMushroomPlacement(0, CountPlacement.of(2)));
		PlacementUtils.register(bootstapContext, RED_MUSHROOM_SWAMP, holder19, getMushroomPlacement(64, null));
		PlacementUtils.register(
			bootstapContext, FLOWER_WARM, holder20, RarityFilter.onAverageOnceEvery(16), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstapContext, FLOWER_DEFAULT, holder20, RarityFilter.onAverageOnceEvery(32), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstapContext,
			FLOWER_FLOWER_FOREST,
			holder21,
			CountPlacement.of(3),
			RarityFilter.onAverageOnceEvery(2),
			InSquarePlacement.spread(),
			PlacementUtils.HEIGHTMAP,
			BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstapContext, FLOWER_SWAMP, holder22, RarityFilter.onAverageOnceEvery(32), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstapContext,
			FLOWER_PLAINS,
			holder23,
			NoiseThresholdCountPlacement.of(-0.8, 15, 4),
			RarityFilter.onAverageOnceEvery(32),
			InSquarePlacement.spread(),
			PlacementUtils.HEIGHTMAP,
			BiomeFilter.biome()
		);
		PlacementUtils.register(bootstapContext, FLOWER_MEADOW, holder24, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
		PlacementModifier placementModifier = SurfaceWaterDepthFilter.forMaxDepth(0);
		PlacementUtils.register(
			bootstapContext,
			TREES_PLAINS,
			holder25,
			PlacementUtils.countExtra(0, 0.05F, 1),
			InSquarePlacement.spread(),
			placementModifier,
			PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
			BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(Blocks.OAK_SAPLING.defaultBlockState(), BlockPos.ZERO)),
			BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstapContext,
			DARK_FOREST_VEGETATION,
			holder26,
			CountPlacement.of(16),
			InSquarePlacement.spread(),
			placementModifier,
			PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
			BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstapContext,
			FLOWER_FOREST_FLOWERS,
			holder27,
			RarityFilter.onAverageOnceEvery(7),
			InSquarePlacement.spread(),
			PlacementUtils.HEIGHTMAP,
			CountPlacement.of(ClampedInt.of(UniformInt.of(-1, 3), 0, 3)),
			BiomeFilter.biome()
		);
		PlacementUtils.register(
			bootstapContext,
			FOREST_FLOWERS,
			holder27,
			RarityFilter.onAverageOnceEvery(7),
			InSquarePlacement.spread(),
			PlacementUtils.HEIGHTMAP,
			CountPlacement.of(ClampedInt.of(UniformInt.of(-3, 1), 0, 1)),
			BiomeFilter.biome()
		);
		PlacementUtils.register(bootstapContext, TREES_FLOWER_FOREST, holder28, treePlacement(PlacementUtils.countExtra(6, 0.1F, 1)));
		PlacementUtils.register(bootstapContext, TREES_MEADOW, holder29, treePlacement(RarityFilter.onAverageOnceEvery(100)));
		PlacementUtils.register(bootstapContext, TREES_TAIGA, holder30, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)));
		PlacementUtils.register(bootstapContext, TREES_GROVE, holder31, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)));
		PlacementUtils.register(bootstapContext, TREES_BADLANDS, holder32, treePlacement(PlacementUtils.countExtra(5, 0.1F, 1), Blocks.OAK_SAPLING));
		PlacementUtils.register(bootstapContext, TREES_SNOWY, holder33, treePlacement(PlacementUtils.countExtra(0, 0.1F, 1), Blocks.SPRUCE_SAPLING));
		PlacementUtils.register(
			bootstapContext,
			TREES_SWAMP,
			holder34,
			PlacementUtils.countExtra(2, 0.1F, 1),
			InSquarePlacement.spread(),
			SurfaceWaterDepthFilter.forMaxDepth(2),
			PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
			BiomeFilter.biome(),
			BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(Blocks.OAK_SAPLING.defaultBlockState(), BlockPos.ZERO))
		);
		PlacementUtils.register(bootstapContext, TREES_WINDSWEPT_SAVANNA, holder35, treePlacement(PlacementUtils.countExtra(2, 0.1F, 1)));
		PlacementUtils.register(bootstapContext, TREES_SAVANNA, holder35, treePlacement(PlacementUtils.countExtra(1, 0.1F, 1)));
		PlacementUtils.register(bootstapContext, BIRCH_TALL, holder36, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)));
		PlacementUtils.register(bootstapContext, TREES_BIRCH, holder37, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1), Blocks.BIRCH_SAPLING));
		PlacementUtils.register(bootstapContext, TREES_WINDSWEPT_FOREST, holder38, treePlacement(PlacementUtils.countExtra(3, 0.1F, 1)));
		PlacementUtils.register(bootstapContext, TREES_WINDSWEPT_HILLS, holder38, treePlacement(PlacementUtils.countExtra(0, 0.1F, 1)));
		PlacementUtils.register(bootstapContext, TREES_WATER, holder39, treePlacement(PlacementUtils.countExtra(0, 0.1F, 1)));
		PlacementUtils.register(bootstapContext, TREES_BIRCH_AND_OAK, holder40, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)));
		PlacementUtils.register(bootstapContext, TREES_SPARSE_JUNGLE, holder41, treePlacement(PlacementUtils.countExtra(2, 0.1F, 1)));
		PlacementUtils.register(bootstapContext, TREES_OLD_GROWTH_SPRUCE_TAIGA, holder42, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)));
		PlacementUtils.register(bootstapContext, TREES_OLD_GROWTH_PINE_TAIGA, holder43, treePlacement(PlacementUtils.countExtra(10, 0.1F, 1)));
		PlacementUtils.register(bootstapContext, TREES_JUNGLE, holder44, treePlacement(PlacementUtils.countExtra(50, 0.1F, 1)));
		PlacementUtils.register(bootstapContext, BAMBOO_VEGETATION, holder45, treePlacement(PlacementUtils.countExtra(30, 0.1F, 1)));
		PlacementUtils.register(bootstapContext, MUSHROOM_ISLAND_VEGETATION, holder46, InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
		PlacementUtils.register(
			bootstapContext,
			TREES_MANGROVE,
			holder47,
			CountPlacement.of(25),
			InSquarePlacement.spread(),
			SurfaceWaterDepthFilter.forMaxDepth(5),
			PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
			BiomeFilter.biome(),
			BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(Blocks.MANGROVE_PROPAGULE.defaultBlockState(), BlockPos.ZERO))
		);
	}
}
