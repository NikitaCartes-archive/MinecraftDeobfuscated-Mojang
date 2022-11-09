/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.worldgen.placement;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

public class OrePlacements {
    public static final ResourceKey<PlacedFeature> ORE_MAGMA = PlacementUtils.createKey("ore_magma");
    public static final ResourceKey<PlacedFeature> ORE_SOUL_SAND = PlacementUtils.createKey("ore_soul_sand");
    public static final ResourceKey<PlacedFeature> ORE_GOLD_DELTAS = PlacementUtils.createKey("ore_gold_deltas");
    public static final ResourceKey<PlacedFeature> ORE_QUARTZ_DELTAS = PlacementUtils.createKey("ore_quartz_deltas");
    public static final ResourceKey<PlacedFeature> ORE_GOLD_NETHER = PlacementUtils.createKey("ore_gold_nether");
    public static final ResourceKey<PlacedFeature> ORE_QUARTZ_NETHER = PlacementUtils.createKey("ore_quartz_nether");
    public static final ResourceKey<PlacedFeature> ORE_GRAVEL_NETHER = PlacementUtils.createKey("ore_gravel_nether");
    public static final ResourceKey<PlacedFeature> ORE_BLACKSTONE = PlacementUtils.createKey("ore_blackstone");
    public static final ResourceKey<PlacedFeature> ORE_DIRT = PlacementUtils.createKey("ore_dirt");
    public static final ResourceKey<PlacedFeature> ORE_GRAVEL = PlacementUtils.createKey("ore_gravel");
    public static final ResourceKey<PlacedFeature> ORE_GRANITE_UPPER = PlacementUtils.createKey("ore_granite_upper");
    public static final ResourceKey<PlacedFeature> ORE_GRANITE_LOWER = PlacementUtils.createKey("ore_granite_lower");
    public static final ResourceKey<PlacedFeature> ORE_DIORITE_UPPER = PlacementUtils.createKey("ore_diorite_upper");
    public static final ResourceKey<PlacedFeature> ORE_DIORITE_LOWER = PlacementUtils.createKey("ore_diorite_lower");
    public static final ResourceKey<PlacedFeature> ORE_ANDESITE_UPPER = PlacementUtils.createKey("ore_andesite_upper");
    public static final ResourceKey<PlacedFeature> ORE_ANDESITE_LOWER = PlacementUtils.createKey("ore_andesite_lower");
    public static final ResourceKey<PlacedFeature> ORE_TUFF = PlacementUtils.createKey("ore_tuff");
    public static final ResourceKey<PlacedFeature> ORE_COAL_UPPER = PlacementUtils.createKey("ore_coal_upper");
    public static final ResourceKey<PlacedFeature> ORE_COAL_LOWER = PlacementUtils.createKey("ore_coal_lower");
    public static final ResourceKey<PlacedFeature> ORE_IRON_UPPER = PlacementUtils.createKey("ore_iron_upper");
    public static final ResourceKey<PlacedFeature> ORE_IRON_MIDDLE = PlacementUtils.createKey("ore_iron_middle");
    public static final ResourceKey<PlacedFeature> ORE_IRON_SMALL = PlacementUtils.createKey("ore_iron_small");
    public static final ResourceKey<PlacedFeature> ORE_GOLD_EXTRA = PlacementUtils.createKey("ore_gold_extra");
    public static final ResourceKey<PlacedFeature> ORE_GOLD = PlacementUtils.createKey("ore_gold");
    public static final ResourceKey<PlacedFeature> ORE_GOLD_LOWER = PlacementUtils.createKey("ore_gold_lower");
    public static final ResourceKey<PlacedFeature> ORE_REDSTONE = PlacementUtils.createKey("ore_redstone");
    public static final ResourceKey<PlacedFeature> ORE_REDSTONE_LOWER = PlacementUtils.createKey("ore_redstone_lower");
    public static final ResourceKey<PlacedFeature> ORE_DIAMOND = PlacementUtils.createKey("ore_diamond");
    public static final ResourceKey<PlacedFeature> ORE_DIAMOND_LARGE = PlacementUtils.createKey("ore_diamond_large");
    public static final ResourceKey<PlacedFeature> ORE_DIAMOND_BURIED = PlacementUtils.createKey("ore_diamond_buried");
    public static final ResourceKey<PlacedFeature> ORE_LAPIS = PlacementUtils.createKey("ore_lapis");
    public static final ResourceKey<PlacedFeature> ORE_LAPIS_BURIED = PlacementUtils.createKey("ore_lapis_buried");
    public static final ResourceKey<PlacedFeature> ORE_INFESTED = PlacementUtils.createKey("ore_infested");
    public static final ResourceKey<PlacedFeature> ORE_EMERALD = PlacementUtils.createKey("ore_emerald");
    public static final ResourceKey<PlacedFeature> ORE_ANCIENT_DEBRIS_LARGE = PlacementUtils.createKey("ore_ancient_debris_large");
    public static final ResourceKey<PlacedFeature> ORE_ANCIENT_DEBRIS_SMALL = PlacementUtils.createKey("ore_debris_small");
    public static final ResourceKey<PlacedFeature> ORE_COPPER = PlacementUtils.createKey("ore_copper");
    public static final ResourceKey<PlacedFeature> ORE_COPPER_LARGE = PlacementUtils.createKey("ore_copper_large");
    public static final ResourceKey<PlacedFeature> ORE_CLAY = PlacementUtils.createKey("ore_clay");

    private static List<PlacementModifier> orePlacement(PlacementModifier placementModifier, PlacementModifier placementModifier2) {
        return List.of(placementModifier, InSquarePlacement.spread(), placementModifier2, BiomeFilter.biome());
    }

    private static List<PlacementModifier> commonOrePlacement(int i, PlacementModifier placementModifier) {
        return OrePlacements.orePlacement(CountPlacement.of(i), placementModifier);
    }

    private static List<PlacementModifier> rareOrePlacement(int i, PlacementModifier placementModifier) {
        return OrePlacements.orePlacement(RarityFilter.onAverageOnceEvery(i), placementModifier);
    }

    public static void bootstrap(BootstapContext<PlacedFeature> bootstapContext) {
        HolderGetter<ConfiguredFeature<?, ?>> holderGetter = bootstapContext.lookup(Registries.CONFIGURED_FEATURE);
        Holder.Reference<ConfiguredFeature<?, ?>> holder = holderGetter.getOrThrow(OreFeatures.ORE_MAGMA);
        Holder.Reference<ConfiguredFeature<?, ?>> holder2 = holderGetter.getOrThrow(OreFeatures.ORE_SOUL_SAND);
        Holder.Reference<ConfiguredFeature<?, ?>> holder3 = holderGetter.getOrThrow(OreFeatures.ORE_NETHER_GOLD);
        Holder.Reference<ConfiguredFeature<?, ?>> holder4 = holderGetter.getOrThrow(OreFeatures.ORE_QUARTZ);
        Holder.Reference<ConfiguredFeature<?, ?>> holder5 = holderGetter.getOrThrow(OreFeatures.ORE_GRAVEL_NETHER);
        Holder.Reference<ConfiguredFeature<?, ?>> holder6 = holderGetter.getOrThrow(OreFeatures.ORE_BLACKSTONE);
        Holder.Reference<ConfiguredFeature<?, ?>> holder7 = holderGetter.getOrThrow(OreFeatures.ORE_DIRT);
        Holder.Reference<ConfiguredFeature<?, ?>> holder8 = holderGetter.getOrThrow(OreFeatures.ORE_GRAVEL);
        Holder.Reference<ConfiguredFeature<?, ?>> holder9 = holderGetter.getOrThrow(OreFeatures.ORE_GRANITE);
        Holder.Reference<ConfiguredFeature<?, ?>> holder10 = holderGetter.getOrThrow(OreFeatures.ORE_DIORITE);
        Holder.Reference<ConfiguredFeature<?, ?>> holder11 = holderGetter.getOrThrow(OreFeatures.ORE_ANDESITE);
        Holder.Reference<ConfiguredFeature<?, ?>> holder12 = holderGetter.getOrThrow(OreFeatures.ORE_TUFF);
        Holder.Reference<ConfiguredFeature<?, ?>> holder13 = holderGetter.getOrThrow(OreFeatures.ORE_COAL);
        Holder.Reference<ConfiguredFeature<?, ?>> holder14 = holderGetter.getOrThrow(OreFeatures.ORE_COAL_BURIED);
        Holder.Reference<ConfiguredFeature<?, ?>> holder15 = holderGetter.getOrThrow(OreFeatures.ORE_IRON);
        Holder.Reference<ConfiguredFeature<?, ?>> holder16 = holderGetter.getOrThrow(OreFeatures.ORE_IRON_SMALL);
        Holder.Reference<ConfiguredFeature<?, ?>> holder17 = holderGetter.getOrThrow(OreFeatures.ORE_GOLD);
        Holder.Reference<ConfiguredFeature<?, ?>> holder18 = holderGetter.getOrThrow(OreFeatures.ORE_GOLD_BURIED);
        Holder.Reference<ConfiguredFeature<?, ?>> holder19 = holderGetter.getOrThrow(OreFeatures.ORE_REDSTONE);
        Holder.Reference<ConfiguredFeature<?, ?>> holder20 = holderGetter.getOrThrow(OreFeatures.ORE_DIAMOND_SMALL);
        Holder.Reference<ConfiguredFeature<?, ?>> holder21 = holderGetter.getOrThrow(OreFeatures.ORE_DIAMOND_LARGE);
        Holder.Reference<ConfiguredFeature<?, ?>> holder22 = holderGetter.getOrThrow(OreFeatures.ORE_DIAMOND_BURIED);
        Holder.Reference<ConfiguredFeature<?, ?>> holder23 = holderGetter.getOrThrow(OreFeatures.ORE_LAPIS);
        Holder.Reference<ConfiguredFeature<?, ?>> holder24 = holderGetter.getOrThrow(OreFeatures.ORE_LAPIS_BURIED);
        Holder.Reference<ConfiguredFeature<?, ?>> holder25 = holderGetter.getOrThrow(OreFeatures.ORE_INFESTED);
        Holder.Reference<ConfiguredFeature<?, ?>> holder26 = holderGetter.getOrThrow(OreFeatures.ORE_EMERALD);
        Holder.Reference<ConfiguredFeature<?, ?>> holder27 = holderGetter.getOrThrow(OreFeatures.ORE_ANCIENT_DEBRIS_LARGE);
        Holder.Reference<ConfiguredFeature<?, ?>> holder28 = holderGetter.getOrThrow(OreFeatures.ORE_ANCIENT_DEBRIS_SMALL);
        Holder.Reference<ConfiguredFeature<?, ?>> holder29 = holderGetter.getOrThrow(OreFeatures.ORE_COPPPER_SMALL);
        Holder.Reference<ConfiguredFeature<?, ?>> holder30 = holderGetter.getOrThrow(OreFeatures.ORE_COPPER_LARGE);
        Holder.Reference<ConfiguredFeature<?, ?>> holder31 = holderGetter.getOrThrow(OreFeatures.ORE_CLAY);
        PlacementUtils.register(bootstapContext, ORE_MAGMA, holder, OrePlacements.commonOrePlacement(4, HeightRangePlacement.uniform(VerticalAnchor.absolute(27), VerticalAnchor.absolute(36))));
        PlacementUtils.register(bootstapContext, ORE_SOUL_SAND, holder2, OrePlacements.commonOrePlacement(12, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(31))));
        PlacementUtils.register(bootstapContext, ORE_GOLD_DELTAS, holder3, OrePlacements.commonOrePlacement(20, PlacementUtils.RANGE_10_10));
        PlacementUtils.register(bootstapContext, ORE_QUARTZ_DELTAS, holder4, OrePlacements.commonOrePlacement(32, PlacementUtils.RANGE_10_10));
        PlacementUtils.register(bootstapContext, ORE_GOLD_NETHER, holder3, OrePlacements.commonOrePlacement(10, PlacementUtils.RANGE_10_10));
        PlacementUtils.register(bootstapContext, ORE_QUARTZ_NETHER, holder4, OrePlacements.commonOrePlacement(16, PlacementUtils.RANGE_10_10));
        PlacementUtils.register(bootstapContext, ORE_GRAVEL_NETHER, holder5, OrePlacements.commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.absolute(5), VerticalAnchor.absolute(41))));
        PlacementUtils.register(bootstapContext, ORE_BLACKSTONE, holder6, OrePlacements.commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.absolute(5), VerticalAnchor.absolute(31))));
        PlacementUtils.register(bootstapContext, ORE_DIRT, holder7, OrePlacements.commonOrePlacement(7, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(160))));
        PlacementUtils.register(bootstapContext, ORE_GRAVEL, holder8, OrePlacements.commonOrePlacement(14, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.top())));
        PlacementUtils.register(bootstapContext, ORE_GRANITE_UPPER, holder9, OrePlacements.rareOrePlacement(6, HeightRangePlacement.uniform(VerticalAnchor.absolute(64), VerticalAnchor.absolute(128))));
        PlacementUtils.register(bootstapContext, ORE_GRANITE_LOWER, holder9, OrePlacements.commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(60))));
        PlacementUtils.register(bootstapContext, ORE_DIORITE_UPPER, holder10, OrePlacements.rareOrePlacement(6, HeightRangePlacement.uniform(VerticalAnchor.absolute(64), VerticalAnchor.absolute(128))));
        PlacementUtils.register(bootstapContext, ORE_DIORITE_LOWER, holder10, OrePlacements.commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(60))));
        PlacementUtils.register(bootstapContext, ORE_ANDESITE_UPPER, holder11, OrePlacements.rareOrePlacement(6, HeightRangePlacement.uniform(VerticalAnchor.absolute(64), VerticalAnchor.absolute(128))));
        PlacementUtils.register(bootstapContext, ORE_ANDESITE_LOWER, holder11, OrePlacements.commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(60))));
        PlacementUtils.register(bootstapContext, ORE_TUFF, holder12, OrePlacements.commonOrePlacement(2, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(0))));
        PlacementUtils.register(bootstapContext, ORE_COAL_UPPER, holder13, OrePlacements.commonOrePlacement(30, HeightRangePlacement.uniform(VerticalAnchor.absolute(136), VerticalAnchor.top())));
        PlacementUtils.register(bootstapContext, ORE_COAL_LOWER, holder14, OrePlacements.commonOrePlacement(20, HeightRangePlacement.triangle(VerticalAnchor.absolute(0), VerticalAnchor.absolute(192))));
        PlacementUtils.register(bootstapContext, ORE_IRON_UPPER, holder15, OrePlacements.commonOrePlacement(90, HeightRangePlacement.triangle(VerticalAnchor.absolute(80), VerticalAnchor.absolute(384))));
        PlacementUtils.register(bootstapContext, ORE_IRON_MIDDLE, holder15, OrePlacements.commonOrePlacement(10, HeightRangePlacement.triangle(VerticalAnchor.absolute(-24), VerticalAnchor.absolute(56))));
        PlacementUtils.register(bootstapContext, ORE_IRON_SMALL, holder16, OrePlacements.commonOrePlacement(10, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(72))));
        PlacementUtils.register(bootstapContext, ORE_GOLD_EXTRA, holder17, OrePlacements.commonOrePlacement(50, HeightRangePlacement.uniform(VerticalAnchor.absolute(32), VerticalAnchor.absolute(256))));
        PlacementUtils.register(bootstapContext, ORE_GOLD, holder18, OrePlacements.commonOrePlacement(4, HeightRangePlacement.triangle(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(32))));
        PlacementUtils.register(bootstapContext, ORE_GOLD_LOWER, holder18, OrePlacements.orePlacement(CountPlacement.of(UniformInt.of(0, 1)), HeightRangePlacement.uniform(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(-48))));
        PlacementUtils.register(bootstapContext, ORE_REDSTONE, holder19, OrePlacements.commonOrePlacement(4, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(15))));
        PlacementUtils.register(bootstapContext, ORE_REDSTONE_LOWER, holder19, OrePlacements.commonOrePlacement(8, HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-32), VerticalAnchor.aboveBottom(32))));
        PlacementUtils.register(bootstapContext, ORE_DIAMOND, holder20, OrePlacements.commonOrePlacement(7, HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80))));
        PlacementUtils.register(bootstapContext, ORE_DIAMOND_LARGE, holder21, OrePlacements.rareOrePlacement(9, HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80))));
        PlacementUtils.register(bootstapContext, ORE_DIAMOND_BURIED, holder22, OrePlacements.commonOrePlacement(4, HeightRangePlacement.triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80))));
        PlacementUtils.register(bootstapContext, ORE_LAPIS, holder23, OrePlacements.commonOrePlacement(2, HeightRangePlacement.triangle(VerticalAnchor.absolute(-32), VerticalAnchor.absolute(32))));
        PlacementUtils.register(bootstapContext, ORE_LAPIS_BURIED, holder24, OrePlacements.commonOrePlacement(4, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(64))));
        PlacementUtils.register(bootstapContext, ORE_INFESTED, holder25, OrePlacements.commonOrePlacement(14, HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(63))));
        PlacementUtils.register(bootstapContext, ORE_EMERALD, holder26, OrePlacements.commonOrePlacement(100, HeightRangePlacement.triangle(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(480))));
        PlacementUtils.register(bootstapContext, ORE_ANCIENT_DEBRIS_LARGE, holder27, InSquarePlacement.spread(), HeightRangePlacement.triangle(VerticalAnchor.absolute(8), VerticalAnchor.absolute(24)), BiomeFilter.biome());
        PlacementUtils.register(bootstapContext, ORE_ANCIENT_DEBRIS_SMALL, holder28, InSquarePlacement.spread(), PlacementUtils.RANGE_8_8, BiomeFilter.biome());
        PlacementUtils.register(bootstapContext, ORE_COPPER, holder29, OrePlacements.commonOrePlacement(16, HeightRangePlacement.triangle(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(112))));
        PlacementUtils.register(bootstapContext, ORE_COPPER_LARGE, holder30, OrePlacements.commonOrePlacement(16, HeightRangePlacement.triangle(VerticalAnchor.absolute(-16), VerticalAnchor.absolute(112))));
        PlacementUtils.register(bootstapContext, ORE_CLAY, holder31, OrePlacements.commonOrePlacement(46, PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT));
    }
}

