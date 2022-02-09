/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.worldgen.biome;

import net.minecraft.core.Holder;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.biome.EndBiomes;
import net.minecraft.data.worldgen.biome.NetherBiomes;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public abstract class Biomes {
    private static void register(ResourceKey<Biome> resourceKey, Biome biome) {
        BuiltinRegistries.register(BuiltinRegistries.BIOME, resourceKey, biome);
    }

    public static Holder<Biome> bootstrap() {
        return BuiltinRegistries.BIOME.getHolderOrThrow(net.minecraft.world.level.biome.Biomes.PLAINS);
    }

    static {
        Biomes.register(net.minecraft.world.level.biome.Biomes.THE_VOID, OverworldBiomes.theVoid());
        Biomes.register(net.minecraft.world.level.biome.Biomes.PLAINS, OverworldBiomes.plains(false, false, false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.SUNFLOWER_PLAINS, OverworldBiomes.plains(true, false, false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.SNOWY_PLAINS, OverworldBiomes.plains(false, true, false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.ICE_SPIKES, OverworldBiomes.plains(false, true, true));
        Biomes.register(net.minecraft.world.level.biome.Biomes.DESERT, OverworldBiomes.desert());
        Biomes.register(net.minecraft.world.level.biome.Biomes.SWAMP, OverworldBiomes.swamp());
        Biomes.register(net.minecraft.world.level.biome.Biomes.FOREST, OverworldBiomes.forest(false, false, false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.FLOWER_FOREST, OverworldBiomes.forest(false, false, true));
        Biomes.register(net.minecraft.world.level.biome.Biomes.BIRCH_FOREST, OverworldBiomes.forest(true, false, false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.DARK_FOREST, OverworldBiomes.darkForest());
        Biomes.register(net.minecraft.world.level.biome.Biomes.OLD_GROWTH_BIRCH_FOREST, OverworldBiomes.forest(true, true, false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.OLD_GROWTH_PINE_TAIGA, OverworldBiomes.oldGrowthTaiga(false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.OLD_GROWTH_SPRUCE_TAIGA, OverworldBiomes.oldGrowthTaiga(true));
        Biomes.register(net.minecraft.world.level.biome.Biomes.TAIGA, OverworldBiomes.taiga(false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.SNOWY_TAIGA, OverworldBiomes.taiga(true));
        Biomes.register(net.minecraft.world.level.biome.Biomes.SAVANNA, OverworldBiomes.savanna(false, false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.SAVANNA_PLATEAU, OverworldBiomes.savanna(false, true));
        Biomes.register(net.minecraft.world.level.biome.Biomes.WINDSWEPT_HILLS, OverworldBiomes.windsweptHills(false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.WINDSWEPT_GRAVELLY_HILLS, OverworldBiomes.windsweptHills(false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.WINDSWEPT_FOREST, OverworldBiomes.windsweptHills(true));
        Biomes.register(net.minecraft.world.level.biome.Biomes.WINDSWEPT_SAVANNA, OverworldBiomes.savanna(true, false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.JUNGLE, OverworldBiomes.jungle());
        Biomes.register(net.minecraft.world.level.biome.Biomes.SPARSE_JUNGLE, OverworldBiomes.sparseJungle());
        Biomes.register(net.minecraft.world.level.biome.Biomes.BAMBOO_JUNGLE, OverworldBiomes.bambooJungle());
        Biomes.register(net.minecraft.world.level.biome.Biomes.BADLANDS, OverworldBiomes.badlands(false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.ERODED_BADLANDS, OverworldBiomes.badlands(false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.WOODED_BADLANDS, OverworldBiomes.badlands(true));
        Biomes.register(net.minecraft.world.level.biome.Biomes.MEADOW, OverworldBiomes.meadow());
        Biomes.register(net.minecraft.world.level.biome.Biomes.GROVE, OverworldBiomes.grove());
        Biomes.register(net.minecraft.world.level.biome.Biomes.SNOWY_SLOPES, OverworldBiomes.snowySlopes());
        Biomes.register(net.minecraft.world.level.biome.Biomes.FROZEN_PEAKS, OverworldBiomes.frozenPeaks());
        Biomes.register(net.minecraft.world.level.biome.Biomes.JAGGED_PEAKS, OverworldBiomes.jaggedPeaks());
        Biomes.register(net.minecraft.world.level.biome.Biomes.STONY_PEAKS, OverworldBiomes.stonyPeaks());
        Biomes.register(net.minecraft.world.level.biome.Biomes.RIVER, OverworldBiomes.river(false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.FROZEN_RIVER, OverworldBiomes.river(true));
        Biomes.register(net.minecraft.world.level.biome.Biomes.BEACH, OverworldBiomes.beach(false, false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.SNOWY_BEACH, OverworldBiomes.beach(true, false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.STONY_SHORE, OverworldBiomes.beach(false, true));
        Biomes.register(net.minecraft.world.level.biome.Biomes.WARM_OCEAN, OverworldBiomes.warmOcean());
        Biomes.register(net.minecraft.world.level.biome.Biomes.LUKEWARM_OCEAN, OverworldBiomes.lukeWarmOcean(false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.DEEP_LUKEWARM_OCEAN, OverworldBiomes.lukeWarmOcean(true));
        Biomes.register(net.minecraft.world.level.biome.Biomes.OCEAN, OverworldBiomes.ocean(false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.DEEP_OCEAN, OverworldBiomes.ocean(true));
        Biomes.register(net.minecraft.world.level.biome.Biomes.COLD_OCEAN, OverworldBiomes.coldOcean(false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.DEEP_COLD_OCEAN, OverworldBiomes.coldOcean(true));
        Biomes.register(net.minecraft.world.level.biome.Biomes.FROZEN_OCEAN, OverworldBiomes.frozenOcean(false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.DEEP_FROZEN_OCEAN, OverworldBiomes.frozenOcean(true));
        Biomes.register(net.minecraft.world.level.biome.Biomes.MUSHROOM_FIELDS, OverworldBiomes.mushroomFields());
        Biomes.register(net.minecraft.world.level.biome.Biomes.DRIPSTONE_CAVES, OverworldBiomes.dripstoneCaves());
        Biomes.register(net.minecraft.world.level.biome.Biomes.LUSH_CAVES, OverworldBiomes.lushCaves());
        Biomes.register(net.minecraft.world.level.biome.Biomes.NETHER_WASTES, NetherBiomes.netherWastes());
        Biomes.register(net.minecraft.world.level.biome.Biomes.WARPED_FOREST, NetherBiomes.warpedForest());
        Biomes.register(net.minecraft.world.level.biome.Biomes.CRIMSON_FOREST, NetherBiomes.crimsonForest());
        Biomes.register(net.minecraft.world.level.biome.Biomes.SOUL_SAND_VALLEY, NetherBiomes.soulSandValley());
        Biomes.register(net.minecraft.world.level.biome.Biomes.BASALT_DELTAS, NetherBiomes.basaltDeltas());
        Biomes.register(net.minecraft.world.level.biome.Biomes.THE_END, EndBiomes.theEnd());
        Biomes.register(net.minecraft.world.level.biome.Biomes.END_HIGHLANDS, EndBiomes.endHighlands());
        Biomes.register(net.minecraft.world.level.biome.Biomes.END_MIDLANDS, EndBiomes.endMidlands());
        Biomes.register(net.minecraft.world.level.biome.Biomes.SMALL_END_ISLANDS, EndBiomes.smallEndIslands());
        Biomes.register(net.minecraft.world.level.biome.Biomes.END_BARRENS, EndBiomes.endBarrens());
    }
}

