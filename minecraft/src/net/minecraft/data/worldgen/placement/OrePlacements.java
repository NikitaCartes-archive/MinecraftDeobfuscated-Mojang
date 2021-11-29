package net.minecraft.data.worldgen.placement;

import java.util.List;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

public class OrePlacements {
	public static final PlacedFeature ORE_MAGMA = PlacementUtils.register(
		"ore_magma", OreFeatures.ORE_MAGMA.placed(commonOrePlacement(4, HeightRangePlacement.uniform(VerticalAnchor.absolute(27), VerticalAnchor.absolute(36))))
	);
	public static final PlacedFeature ORE_SOUL_SAND = PlacementUtils.register(
		"ore_soul_sand", OreFeatures.ORE_SOUL_SAND.placed(commonOrePlacement(12, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(31))))
	);
	public static final PlacedFeature ORE_GOLD_DELTAS = PlacementUtils.register(
		"ore_gold_deltas", OreFeatures.ORE_NETHER_GOLD.placed(commonOrePlacement(20, PlacementUtils.RANGE_10_10))
	);
	public static final PlacedFeature ORE_QUARTZ_DELTAS = PlacementUtils.register(
		"ore_quartz_deltas", OreFeatures.ORE_QUARTZ.placed(commonOrePlacement(32, PlacementUtils.RANGE_10_10))
	);
	public static final PlacedFeature ORE_GOLD_NETHER = PlacementUtils.register(
		"ore_gold_nether", OreFeatures.ORE_NETHER_GOLD.placed(commonOrePlacement(10, PlacementUtils.RANGE_10_10))
	);
	public static final PlacedFeature ORE_QUARTZ_NETHER = PlacementUtils.register(
		"ore_quartz_nether", OreFeatures.ORE_QUARTZ.placed(commonOrePlacement(16, PlacementUtils.RANGE_10_10))
	);
	public static final PlacedFeature ORE_GRAVEL_NETHER = PlacementUtils.register(
		"ore_gravel_nether",
		OreFeatures.ORE_GRAVEL_NETHER.placed(commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.absolute(5), VerticalAnchor.absolute(41))))
	);
	public static final PlacedFeature ORE_BLACKSTONE = PlacementUtils.register(
		"ore_blackstone",
		OreFeatures.ORE_BLACKSTONE.placed(commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.absolute(5), VerticalAnchor.absolute(31))))
	);
	public static final PlacedFeature ORE_DIRT = PlacementUtils.register(
		"ore_dirt", OreFeatures.ORE_DIRT.placed(commonOrePlacement(7, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(160))))
	);
	public static final PlacedFeature ORE_GRAVEL = PlacementUtils.register(
		"ore_gravel", OreFeatures.ORE_GRAVEL.placed(commonOrePlacement(14, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.top())))
	);
	public static final PlacedFeature ORE_GRANITE_UPPER = PlacementUtils.register(
		"ore_granite_upper",
		OreFeatures.ORE_GRANITE.placed(rareOrePlacement(6, HeightRangePlacement.uniform(VerticalAnchor.absolute(64), VerticalAnchor.absolute(128))))
	);
	public static final PlacedFeature ORE_GRANITE_LOWER = PlacementUtils.register(
		"ore_granite_lower",
		OreFeatures.ORE_GRANITE.placed(commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(60))))
	);
	public static final PlacedFeature ORE_DIORITE_UPPER = PlacementUtils.register(
		"ore_diorite_upper",
		OreFeatures.ORE_DIORITE.placed(rareOrePlacement(6, HeightRangePlacement.uniform(VerticalAnchor.absolute(64), VerticalAnchor.absolute(128))))
	);
	public static final PlacedFeature ORE_DIORITE_LOWER = PlacementUtils.register(
		"ore_diorite_lower",
		OreFeatures.ORE_DIORITE.placed(commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(60))))
	);
	public static final PlacedFeature ORE_ANDESITE_UPPER = PlacementUtils.register(
		"ore_andesite_upper",
		OreFeatures.ORE_ANDESITE.placed(rareOrePlacement(6, HeightRangePlacement.uniform(VerticalAnchor.absolute(64), VerticalAnchor.absolute(128))))
	);
	public static final PlacedFeature ORE_ANDESITE_LOWER = PlacementUtils.register(
		"ore_andesite_lower",
		OreFeatures.ORE_ANDESITE.placed(commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(60))))
	);
	public static final PlacedFeature ORE_TUFF = PlacementUtils.register(
		"ore_tuff", OreFeatures.ORE_TUFF.placed(commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(0))))
	);
	public static final PlacedFeature ORE_COAL_UPPER = PlacementUtils.register(
		"ore_coal_upper", OreFeatures.ORE_COAL.placed(commonOrePlacement(30, HeightRangePlacement.uniform(VerticalAnchor.absolute(136), VerticalAnchor.top())))
	);
	public static final PlacedFeature ORE_COAL_LOWER = PlacementUtils.register(
		"ore_coal_lower",
		OreFeatures.ORE_COAL_BURIED.placed(commonOrePlacement(20, HeightRangePlacement.triangle(VerticalAnchor.absolute(0), VerticalAnchor.absolute(192))))
	);
	public static final PlacedFeature ORE_IRON_UPPER = PlacementUtils.register(
		"ore_iron_upper",
		OreFeatures.ORE_IRON.placed(commonOrePlacement(90, HeightRangePlacement.triangle(VerticalAnchor.absolute(80), VerticalAnchor.absolute(384))))
	);
	public static final PlacedFeature ORE_IRON_MIDDLE = PlacementUtils.register(
		"ore_iron_middle",
		OreFeatures.ORE_IRON.placed(commonOrePlacement(10, HeightRangePlacement.triangle(VerticalAnchor.absolute(-24), VerticalAnchor.absolute(56))))
	);
	public static final PlacedFeature ORE_IRON_SMALL = PlacementUtils.register(
		"ore_iron_small",
		OreFeatures.ORE_IRON_SMALL.placed(commonOrePlacement(10, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(72))))
	);
	public static final PlacedFeature ORE_GOLD_EXTRA = PlacementUtils.register(
		"ore_gold_extra",
		OreFeatures.ORE_GOLD.placed(commonOrePlacement(50, HeightRangePlacement.uniform(VerticalAnchor.absolute(32), VerticalAnchor.absolute(256))))
	);
	public static final PlacedFeature ORE_GOLD = PlacementUtils.register(
		"ore_gold",
		OreFeatures.ORE_GOLD_BURIED.placed(commonOrePlacement(4, HeightRangePlacement.triangle(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(32))))
	);
	public static final PlacedFeature ORE_GOLD_LOWER = PlacementUtils.register(
		"ore_gold_lower",
		OreFeatures.ORE_GOLD_BURIED
			.placed(orePlacement(CountPlacement.of(UniformInt.of(0, 1)), HeightRangePlacement.uniform(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(-48))))
	);
	public static final PlacedFeature ORE_REDSTONE = PlacementUtils.register(
		"ore_redstone", OreFeatures.ORE_REDSTONE.placed(commonOrePlacement(4, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(15))))
	);
	public static final PlacedFeature ORE_REDSTONE_LOWER = PlacementUtils.register(
		"ore_redstone_lower",
		OreFeatures.ORE_REDSTONE.placed(commonOrePlacement(8, HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-32), VerticalAnchor.aboveBottom(32))))
	);
	public static final PlacedFeature ORE_DIAMOND = PlacementUtils.register(
		"ore_diamond",
		OreFeatures.ORE_DIAMOND_SMALL.placed(commonOrePlacement(7, HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80))))
	);
	public static final PlacedFeature ORE_DIAMOND_LARGE = PlacementUtils.register(
		"ore_diamond_large",
		OreFeatures.ORE_DIAMOND_LARGE.placed(rareOrePlacement(9, HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80))))
	);
	public static final PlacedFeature ORE_DIAMOND_BURIED = PlacementUtils.register(
		"ore_diamond_buried",
		OreFeatures.ORE_DIAMOND_BURIED.placed(commonOrePlacement(4, HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80))))
	);
	public static final PlacedFeature ORE_LAPIS = PlacementUtils.register(
		"ore_lapis", OreFeatures.ORE_LAPIS.placed(commonOrePlacement(2, HeightRangePlacement.triangle(VerticalAnchor.absolute(-32), VerticalAnchor.absolute(32))))
	);
	public static final PlacedFeature ORE_LAPIS_BURIED = PlacementUtils.register(
		"ore_lapis_buried",
		OreFeatures.ORE_LAPIS_BURIED.placed(commonOrePlacement(4, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(64))))
	);
	public static final PlacedFeature ORE_INFESTED = PlacementUtils.register(
		"ore_infested", OreFeatures.ORE_INFESTED.placed(commonOrePlacement(14, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(63))))
	);
	public static final PlacedFeature ORE_EMERALD = PlacementUtils.register(
		"ore_emerald",
		OreFeatures.ORE_EMERALD.placed(commonOrePlacement(100, HeightRangePlacement.triangle(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(480))))
	);
	public static final PlacedFeature ORE_ANCIENT_DEBRIS_LARGE = PlacementUtils.register(
		"ore_ancient_debris_large",
		OreFeatures.ORE_ANCIENT_DEBRIS_LARGE
			.placed(InSquarePlacement.spread(), HeightRangePlacement.triangle(VerticalAnchor.absolute(8), VerticalAnchor.absolute(24)), BiomeFilter.biome())
	);
	public static final PlacedFeature ORE_ANCIENT_DEBRIS_SMALL = PlacementUtils.register(
		"ore_debris_small", OreFeatures.ORE_ANCIENT_DEBRIS_SMALL.placed(InSquarePlacement.spread(), PlacementUtils.RANGE_8_8, BiomeFilter.biome())
	);
	public static final PlacedFeature ORE_COPPER = PlacementUtils.register(
		"ore_copper",
		OreFeatures.ORE_COPPPER_SMALL.placed(commonOrePlacement(16, HeightRangePlacement.triangle(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(112))))
	);
	public static final PlacedFeature ORE_COPPER_LARGE = PlacementUtils.register(
		"ore_copper_large",
		OreFeatures.ORE_COPPER_LARGE.placed(commonOrePlacement(16, HeightRangePlacement.triangle(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(112))))
	);
	public static final PlacedFeature ORE_CLAY = PlacementUtils.register(
		"ore_clay", OreFeatures.ORE_CLAY.placed(commonOrePlacement(46, PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT))
	);

	private static List<PlacementModifier> orePlacement(PlacementModifier placementModifier, PlacementModifier placementModifier2) {
		return List.of(placementModifier, InSquarePlacement.spread(), placementModifier2, BiomeFilter.biome());
	}

	private static List<PlacementModifier> commonOrePlacement(int i, PlacementModifier placementModifier) {
		return orePlacement(CountPlacement.of(i), placementModifier);
	}

	private static List<PlacementModifier> rareOrePlacement(int i, PlacementModifier placementModifier) {
		return orePlacement(RarityFilter.onAverageOnceEvery(i), placementModifier);
	}
}
