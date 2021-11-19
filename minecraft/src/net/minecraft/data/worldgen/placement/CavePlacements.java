package net.minecraft.data.worldgen.placement;

import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.features.CaveFeatures;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.util.valueproviders.ClampedNormalInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RandomOffsetPlacement;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.placement.SurfaceRelativeThresholdFilter;

public class CavePlacements {
	public static final PlacedFeature MONSTER_ROOM = PlacementUtils.register(
		"monster_room",
		CaveFeatures.MONSTER_ROOM
			.placed(
				CountPlacement.of(10), InSquarePlacement.spread(), HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.top()), BiomeFilter.biome()
			)
	);
	public static final PlacedFeature MONSTER_ROOM_DEEP = PlacementUtils.register(
		"monster_room_deep",
		CaveFeatures.MONSTER_ROOM
			.placed(
				CountPlacement.of(4),
				InSquarePlacement.spread(),
				HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(6), VerticalAnchor.absolute(-1)),
				BiomeFilter.biome()
			)
	);
	public static final PlacedFeature FOSSIL_UPPER = PlacementUtils.register(
		"fossil_upper",
		CaveFeatures.FOSSIL_COAL
			.placed(
				RarityFilter.onAverageOnceEvery(64),
				InSquarePlacement.spread(),
				HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.top()),
				BiomeFilter.biome()
			)
	);
	public static final PlacedFeature FOSSIL_LOWER = PlacementUtils.register(
		"fossil_lower",
		CaveFeatures.FOSSIL_DIAMONDS
			.placed(
				RarityFilter.onAverageOnceEvery(64),
				InSquarePlacement.spread(),
				HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(-8)),
				BiomeFilter.biome()
			)
	);
	public static final PlacedFeature DRIPSTONE_CLUSTER = PlacementUtils.register(
		"dripstone_cluster",
		CaveFeatures.DRIPSTONE_CLUSTER
			.placed(CountPlacement.of(UniformInt.of(35, 70)), InSquarePlacement.spread(), PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT, BiomeFilter.biome())
	);
	public static final PlacedFeature LARGE_DRIPSTONE = PlacementUtils.register(
		"large_dripstone",
		CaveFeatures.LARGE_DRIPSTONE
			.placed(CountPlacement.of(UniformInt.of(7, 35)), InSquarePlacement.spread(), PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT, BiomeFilter.biome())
	);
	public static final PlacedFeature POINTED_DRIPSTONE = PlacementUtils.register(
		"pointed_dripstone",
		CaveFeatures.POINTED_DRIPSTONE
			.placed(
				CountPlacement.of(UniformInt.of(140, 220)),
				InSquarePlacement.spread(),
				PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT,
				CountPlacement.of(UniformInt.of(1, 5)),
				RandomOffsetPlacement.of(ClampedNormalInt.of(0.0F, 3.0F, -10, 10), ClampedNormalInt.of(0.0F, 0.6F, -2, 2)),
				BiomeFilter.biome()
			)
	);
	public static final PlacedFeature UNDERWATER_MAGMA = PlacementUtils.register(
		"underwater_magma",
		CaveFeatures.UNDERWATER_MAGMA
			.placed(
				CountPlacement.of(UniformInt.of(44, 52)),
				InSquarePlacement.spread(),
				PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT,
				SurfaceRelativeThresholdFilter.of(Heightmap.Types.OCEAN_FLOOR_WG, Integer.MIN_VALUE, -2),
				BiomeFilter.biome()
			)
	);
	public static final PlacedFeature GLOW_LICHEN = PlacementUtils.register(
		"glow_lichen",
		CaveFeatures.GLOW_LICHEN
			.placed(
				CountPlacement.of(UniformInt.of(104, 157)),
				PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT,
				InSquarePlacement.spread(),
				SurfaceRelativeThresholdFilter.of(Heightmap.Types.OCEAN_FLOOR_WG, Integer.MIN_VALUE, -13),
				BiomeFilter.biome()
			)
	);
	public static final PlacedFeature ROOTED_AZALEA_TREE = PlacementUtils.register(
		"rooted_azalea_tree",
		CaveFeatures.ROOTED_AZALEA_TREE
			.placed(
				InSquarePlacement.spread(),
				PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT,
				EnvironmentScanPlacement.scanningFor(Direction.UP, BlockPredicate.solid(Direction.UP.getNormal()), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
				BiomeFilter.biome()
			)
	);
	public static final PlacedFeature CAVE_VINES = PlacementUtils.register(
		"cave_vines",
		CaveFeatures.CAVE_VINE
			.placed(
				CountPlacement.of(157),
				InSquarePlacement.spread(),
				PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT,
				EnvironmentScanPlacement.scanningFor(
					Direction.UP, BlockPredicate.hasSturdyFace(Direction.UP.getNormal(), Direction.DOWN), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12
				),
				BiomeFilter.biome()
			)
	);
	public static final PlacedFeature LUSH_CAVES_VEGETATION = PlacementUtils.register(
		"lush_caves_vegetation",
		CaveFeatures.MOSS_PATCH
			.placed(
				CountPlacement.of(104),
				InSquarePlacement.spread(),
				PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT,
				EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.solid(Direction.DOWN.getNormal()), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
				BiomeFilter.biome()
			)
	);
	public static final PlacedFeature LUSH_CAVES_CLAY = PlacementUtils.register(
		"lush_caves_clay",
		CaveFeatures.LUSH_CAVES_CLAY
			.placed(
				CountPlacement.of(52),
				InSquarePlacement.spread(),
				PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT,
				EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.solid(Direction.DOWN.getNormal()), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
				BiomeFilter.biome()
			)
	);
	public static final PlacedFeature LUSH_CAVES_CEILING_VEGETATION = PlacementUtils.register(
		"lush_caves_ceiling_vegetation",
		CaveFeatures.MOSS_PATCH_CEILING
			.placed(
				CountPlacement.of(104),
				InSquarePlacement.spread(),
				PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT,
				EnvironmentScanPlacement.scanningFor(Direction.UP, BlockPredicate.solid(Direction.UP.getNormal()), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
				BiomeFilter.biome()
			)
	);
	public static final PlacedFeature SPORE_BLOSSOM = PlacementUtils.register(
		"spore_blossom",
		CaveFeatures.SPORE_BLOSSOM
			.placed(
				CountPlacement.of(21),
				InSquarePlacement.spread(),
				PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT,
				EnvironmentScanPlacement.scanningFor(Direction.UP, BlockPredicate.solid(Direction.UP.getNormal()), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
				BiomeFilter.biome()
			)
	);
	public static final PlacedFeature CLASSIC_VINES = PlacementUtils.register(
		"classic_vines_cave_feature",
		VegetationFeatures.VINES.placed(CountPlacement.of(216), InSquarePlacement.spread(), PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT, BiomeFilter.biome())
	);
	public static final PlacedFeature AMETHYST_GEODE = PlacementUtils.register(
		"amethyst_geode",
		CaveFeatures.AMETHYST_GEODE
			.placed(
				RarityFilter.onAverageOnceEvery(24),
				InSquarePlacement.spread(),
				HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(6), VerticalAnchor.absolute(30)),
				BiomeFilter.biome()
			)
	);
}
