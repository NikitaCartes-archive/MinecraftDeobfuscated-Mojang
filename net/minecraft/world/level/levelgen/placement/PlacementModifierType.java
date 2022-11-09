/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CarvingMaskPlacement;
import net.minecraft.world.level.levelgen.placement.CountOnEveryLayerPlacement;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.NoiseBasedCountPlacement;
import net.minecraft.world.level.levelgen.placement.NoiseThresholdCountPlacement;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RandomOffsetPlacement;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.placement.SurfaceRelativeThresholdFilter;
import net.minecraft.world.level.levelgen.placement.SurfaceWaterDepthFilter;

public interface PlacementModifierType<P extends PlacementModifier> {
    public static final PlacementModifierType<BlockPredicateFilter> BLOCK_PREDICATE_FILTER = PlacementModifierType.register("block_predicate_filter", BlockPredicateFilter.CODEC);
    public static final PlacementModifierType<RarityFilter> RARITY_FILTER = PlacementModifierType.register("rarity_filter", RarityFilter.CODEC);
    public static final PlacementModifierType<SurfaceRelativeThresholdFilter> SURFACE_RELATIVE_THRESHOLD_FILTER = PlacementModifierType.register("surface_relative_threshold_filter", SurfaceRelativeThresholdFilter.CODEC);
    public static final PlacementModifierType<SurfaceWaterDepthFilter> SURFACE_WATER_DEPTH_FILTER = PlacementModifierType.register("surface_water_depth_filter", SurfaceWaterDepthFilter.CODEC);
    public static final PlacementModifierType<BiomeFilter> BIOME_FILTER = PlacementModifierType.register("biome", BiomeFilter.CODEC);
    public static final PlacementModifierType<CountPlacement> COUNT = PlacementModifierType.register("count", CountPlacement.CODEC);
    public static final PlacementModifierType<NoiseBasedCountPlacement> NOISE_BASED_COUNT = PlacementModifierType.register("noise_based_count", NoiseBasedCountPlacement.CODEC);
    public static final PlacementModifierType<NoiseThresholdCountPlacement> NOISE_THRESHOLD_COUNT = PlacementModifierType.register("noise_threshold_count", NoiseThresholdCountPlacement.CODEC);
    public static final PlacementModifierType<CountOnEveryLayerPlacement> COUNT_ON_EVERY_LAYER = PlacementModifierType.register("count_on_every_layer", CountOnEveryLayerPlacement.CODEC);
    public static final PlacementModifierType<EnvironmentScanPlacement> ENVIRONMENT_SCAN = PlacementModifierType.register("environment_scan", EnvironmentScanPlacement.CODEC);
    public static final PlacementModifierType<HeightmapPlacement> HEIGHTMAP = PlacementModifierType.register("heightmap", HeightmapPlacement.CODEC);
    public static final PlacementModifierType<HeightRangePlacement> HEIGHT_RANGE = PlacementModifierType.register("height_range", HeightRangePlacement.CODEC);
    public static final PlacementModifierType<InSquarePlacement> IN_SQUARE = PlacementModifierType.register("in_square", InSquarePlacement.CODEC);
    public static final PlacementModifierType<RandomOffsetPlacement> RANDOM_OFFSET = PlacementModifierType.register("random_offset", RandomOffsetPlacement.CODEC);
    public static final PlacementModifierType<CarvingMaskPlacement> CARVING_MASK_PLACEMENT = PlacementModifierType.register("carving_mask", CarvingMaskPlacement.CODEC);

    public Codec<P> codec();

    private static <P extends PlacementModifier> PlacementModifierType<P> register(String string, Codec<P> codec) {
        return Registry.register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, string, () -> codec);
    }
}

