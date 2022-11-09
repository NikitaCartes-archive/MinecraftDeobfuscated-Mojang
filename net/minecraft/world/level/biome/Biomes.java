/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.biome;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public abstract class Biomes {
    public static final ResourceKey<Biome> THE_VOID = Biomes.register("the_void");
    public static final ResourceKey<Biome> PLAINS = Biomes.register("plains");
    public static final ResourceKey<Biome> SUNFLOWER_PLAINS = Biomes.register("sunflower_plains");
    public static final ResourceKey<Biome> SNOWY_PLAINS = Biomes.register("snowy_plains");
    public static final ResourceKey<Biome> ICE_SPIKES = Biomes.register("ice_spikes");
    public static final ResourceKey<Biome> DESERT = Biomes.register("desert");
    public static final ResourceKey<Biome> SWAMP = Biomes.register("swamp");
    public static final ResourceKey<Biome> MANGROVE_SWAMP = Biomes.register("mangrove_swamp");
    public static final ResourceKey<Biome> FOREST = Biomes.register("forest");
    public static final ResourceKey<Biome> FLOWER_FOREST = Biomes.register("flower_forest");
    public static final ResourceKey<Biome> BIRCH_FOREST = Biomes.register("birch_forest");
    public static final ResourceKey<Biome> DARK_FOREST = Biomes.register("dark_forest");
    public static final ResourceKey<Biome> OLD_GROWTH_BIRCH_FOREST = Biomes.register("old_growth_birch_forest");
    public static final ResourceKey<Biome> OLD_GROWTH_PINE_TAIGA = Biomes.register("old_growth_pine_taiga");
    public static final ResourceKey<Biome> OLD_GROWTH_SPRUCE_TAIGA = Biomes.register("old_growth_spruce_taiga");
    public static final ResourceKey<Biome> TAIGA = Biomes.register("taiga");
    public static final ResourceKey<Biome> SNOWY_TAIGA = Biomes.register("snowy_taiga");
    public static final ResourceKey<Biome> SAVANNA = Biomes.register("savanna");
    public static final ResourceKey<Biome> SAVANNA_PLATEAU = Biomes.register("savanna_plateau");
    public static final ResourceKey<Biome> WINDSWEPT_HILLS = Biomes.register("windswept_hills");
    public static final ResourceKey<Biome> WINDSWEPT_GRAVELLY_HILLS = Biomes.register("windswept_gravelly_hills");
    public static final ResourceKey<Biome> WINDSWEPT_FOREST = Biomes.register("windswept_forest");
    public static final ResourceKey<Biome> WINDSWEPT_SAVANNA = Biomes.register("windswept_savanna");
    public static final ResourceKey<Biome> JUNGLE = Biomes.register("jungle");
    public static final ResourceKey<Biome> SPARSE_JUNGLE = Biomes.register("sparse_jungle");
    public static final ResourceKey<Biome> BAMBOO_JUNGLE = Biomes.register("bamboo_jungle");
    public static final ResourceKey<Biome> BADLANDS = Biomes.register("badlands");
    public static final ResourceKey<Biome> ERODED_BADLANDS = Biomes.register("eroded_badlands");
    public static final ResourceKey<Biome> WOODED_BADLANDS = Biomes.register("wooded_badlands");
    public static final ResourceKey<Biome> MEADOW = Biomes.register("meadow");
    public static final ResourceKey<Biome> GROVE = Biomes.register("grove");
    public static final ResourceKey<Biome> SNOWY_SLOPES = Biomes.register("snowy_slopes");
    public static final ResourceKey<Biome> FROZEN_PEAKS = Biomes.register("frozen_peaks");
    public static final ResourceKey<Biome> JAGGED_PEAKS = Biomes.register("jagged_peaks");
    public static final ResourceKey<Biome> STONY_PEAKS = Biomes.register("stony_peaks");
    public static final ResourceKey<Biome> RIVER = Biomes.register("river");
    public static final ResourceKey<Biome> FROZEN_RIVER = Biomes.register("frozen_river");
    public static final ResourceKey<Biome> BEACH = Biomes.register("beach");
    public static final ResourceKey<Biome> SNOWY_BEACH = Biomes.register("snowy_beach");
    public static final ResourceKey<Biome> STONY_SHORE = Biomes.register("stony_shore");
    public static final ResourceKey<Biome> WARM_OCEAN = Biomes.register("warm_ocean");
    public static final ResourceKey<Biome> LUKEWARM_OCEAN = Biomes.register("lukewarm_ocean");
    public static final ResourceKey<Biome> DEEP_LUKEWARM_OCEAN = Biomes.register("deep_lukewarm_ocean");
    public static final ResourceKey<Biome> OCEAN = Biomes.register("ocean");
    public static final ResourceKey<Biome> DEEP_OCEAN = Biomes.register("deep_ocean");
    public static final ResourceKey<Biome> COLD_OCEAN = Biomes.register("cold_ocean");
    public static final ResourceKey<Biome> DEEP_COLD_OCEAN = Biomes.register("deep_cold_ocean");
    public static final ResourceKey<Biome> FROZEN_OCEAN = Biomes.register("frozen_ocean");
    public static final ResourceKey<Biome> DEEP_FROZEN_OCEAN = Biomes.register("deep_frozen_ocean");
    public static final ResourceKey<Biome> MUSHROOM_FIELDS = Biomes.register("mushroom_fields");
    public static final ResourceKey<Biome> DRIPSTONE_CAVES = Biomes.register("dripstone_caves");
    public static final ResourceKey<Biome> LUSH_CAVES = Biomes.register("lush_caves");
    public static final ResourceKey<Biome> DEEP_DARK = Biomes.register("deep_dark");
    public static final ResourceKey<Biome> NETHER_WASTES = Biomes.register("nether_wastes");
    public static final ResourceKey<Biome> WARPED_FOREST = Biomes.register("warped_forest");
    public static final ResourceKey<Biome> CRIMSON_FOREST = Biomes.register("crimson_forest");
    public static final ResourceKey<Biome> SOUL_SAND_VALLEY = Biomes.register("soul_sand_valley");
    public static final ResourceKey<Biome> BASALT_DELTAS = Biomes.register("basalt_deltas");
    public static final ResourceKey<Biome> THE_END = Biomes.register("the_end");
    public static final ResourceKey<Biome> END_HIGHLANDS = Biomes.register("end_highlands");
    public static final ResourceKey<Biome> END_MIDLANDS = Biomes.register("end_midlands");
    public static final ResourceKey<Biome> SMALL_END_ISLANDS = Biomes.register("small_end_islands");
    public static final ResourceKey<Biome> END_BARRENS = Biomes.register("end_barrens");

    private static ResourceKey<Biome> register(String string) {
        return ResourceKey.create(Registries.BIOME, new ResourceLocation(string));
    }
}

