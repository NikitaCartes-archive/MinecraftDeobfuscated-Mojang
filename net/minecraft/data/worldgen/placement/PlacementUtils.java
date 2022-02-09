/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.worldgen.placement;

import java.util.List;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.placement.AquaticPlacements;
import net.minecraft.data.worldgen.placement.CavePlacements;
import net.minecraft.data.worldgen.placement.EndPlacements;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.data.worldgen.placement.NetherPlacements;
import net.minecraft.data.worldgen.placement.OrePlacements;
import net.minecraft.data.worldgen.placement.TreePlacements;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.data.worldgen.placement.VillagePlacements;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.WeightedListInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

public class PlacementUtils {
    public static final PlacementModifier HEIGHTMAP = HeightmapPlacement.onHeightmap(Heightmap.Types.MOTION_BLOCKING);
    public static final PlacementModifier HEIGHTMAP_TOP_SOLID = HeightmapPlacement.onHeightmap(Heightmap.Types.OCEAN_FLOOR_WG);
    public static final PlacementModifier HEIGHTMAP_WORLD_SURFACE = HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG);
    public static final PlacementModifier HEIGHTMAP_OCEAN_FLOOR = HeightmapPlacement.onHeightmap(Heightmap.Types.OCEAN_FLOOR);
    public static final PlacementModifier FULL_RANGE = HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.top());
    public static final PlacementModifier RANGE_10_10 = HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(10), VerticalAnchor.belowTop(10));
    public static final PlacementModifier RANGE_8_8 = HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(8), VerticalAnchor.belowTop(8));
    public static final PlacementModifier RANGE_4_4 = HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(4), VerticalAnchor.belowTop(4));
    public static final PlacementModifier RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT = HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(256));

    public static Holder<PlacedFeature> bootstrap() {
        List<Holder<PlacedFeature>> list = List.of(AquaticPlacements.KELP_COLD, CavePlacements.CAVE_VINES, EndPlacements.CHORUS_PLANT, MiscOverworldPlacements.BLUE_ICE, NetherPlacements.BASALT_BLOBS, OrePlacements.ORE_ANCIENT_DEBRIS_LARGE, TreePlacements.ACACIA_CHECKED, VegetationPlacements.BAMBOO_VEGETATION, VillagePlacements.PILE_HAY_VILLAGE);
        return Util.getRandom(list, new Random());
    }

    public static Holder<PlacedFeature> register(String string, Holder<? extends ConfiguredFeature<?, ?>> holder, List<PlacementModifier> list) {
        return BuiltinRegistries.register(BuiltinRegistries.PLACED_FEATURE, string, new PlacedFeature(Holder.hackyErase(holder), List.copyOf(list)));
    }

    public static Holder<PlacedFeature> register(String string, Holder<? extends ConfiguredFeature<?, ?>> holder, PlacementModifier ... placementModifiers) {
        return PlacementUtils.register(string, holder, List.of(placementModifiers));
    }

    public static PlacementModifier countExtra(int i, float f, int j) {
        float g = 1.0f / f;
        if (Math.abs(g - (float)((int)g)) > 1.0E-5f) {
            throw new IllegalStateException("Chance data cannot be represented as list weight");
        }
        SimpleWeightedRandomList<IntProvider> simpleWeightedRandomList = SimpleWeightedRandomList.builder().add(ConstantInt.of(i), (int)g - 1).add(ConstantInt.of(i + j), 1).build();
        return CountPlacement.of(new WeightedListInt(simpleWeightedRandomList));
    }

    public static PlacementFilter isEmpty() {
        return BlockPredicateFilter.forPredicate(BlockPredicate.matchesBlock(Blocks.AIR, BlockPos.ZERO));
    }

    public static BlockPredicateFilter filteredByBlockSurvival(Block block) {
        return BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(block.defaultBlockState(), BlockPos.ZERO));
    }

    public static Holder<PlacedFeature> inlinePlaced(Holder<? extends ConfiguredFeature<?, ?>> holder, PlacementModifier ... placementModifiers) {
        return Holder.direct(new PlacedFeature(Holder.hackyErase(holder), List.of(placementModifiers)));
    }

    public static <FC extends FeatureConfiguration, F extends Feature<FC>> Holder<PlacedFeature> inlinePlaced(F feature, FC featureConfiguration, PlacementModifier ... placementModifiers) {
        return PlacementUtils.inlinePlaced(Holder.direct(new ConfiguredFeature<FC, F>(feature, featureConfiguration)), placementModifiers);
    }

    public static <FC extends FeatureConfiguration, F extends Feature<FC>> Holder<PlacedFeature> onlyWhenEmpty(F feature, FC featureConfiguration) {
        return PlacementUtils.filtered(feature, featureConfiguration, BlockPredicate.matchesBlock(Blocks.AIR, BlockPos.ZERO));
    }

    public static <FC extends FeatureConfiguration, F extends Feature<FC>> Holder<PlacedFeature> filtered(F feature, FC featureConfiguration, BlockPredicate blockPredicate) {
        return PlacementUtils.inlinePlaced(feature, featureConfiguration, BlockPredicateFilter.forPredicate(blockPredicate));
    }
}

