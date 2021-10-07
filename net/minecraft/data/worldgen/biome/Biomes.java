/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.worldgen.biome;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.SurfaceBuilders;
import net.minecraft.data.worldgen.biome.VanillaBiomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public abstract class Biomes {
    public static final Biome PLAINS;
    public static final Biome THE_VOID;

    private static Biome register(ResourceKey<Biome> resourceKey, Biome biome) {
        return BuiltinRegistries.registerMapping(BuiltinRegistries.BIOME, resourceKey, biome);
    }

    static {
        Biomes.register(net.minecraft.world.level.biome.Biomes.OCEAN, VanillaBiomes.oceanBiome(false));
        PLAINS = Biomes.register(net.minecraft.world.level.biome.Biomes.PLAINS, VanillaBiomes.plainsBiome(false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.DESERT, VanillaBiomes.desertBiome(true));
        Biomes.register(net.minecraft.world.level.biome.Biomes.WINDSWEPT_HILLS, VanillaBiomes.mountainBiome(SurfaceBuilders.MOUNTAIN, false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.FOREST, VanillaBiomes.forestBiome());
        Biomes.register(net.minecraft.world.level.biome.Biomes.TAIGA, VanillaBiomes.taigaBiome(false, false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.SWAMP, VanillaBiomes.swampBiome(false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.RIVER, VanillaBiomes.riverBiome(0.5f, 4159204, false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.NETHER_WASTES, VanillaBiomes.netherWastesBiome());
        Biomes.register(net.minecraft.world.level.biome.Biomes.THE_END, VanillaBiomes.theEndBiome());
        Biomes.register(net.minecraft.world.level.biome.Biomes.FROZEN_OCEAN, VanillaBiomes.frozenOceanBiome(false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.FROZEN_RIVER, VanillaBiomes.riverBiome(0.0f, 3750089, true));
        Biomes.register(net.minecraft.world.level.biome.Biomes.SNOWY_PLAINS, VanillaBiomes.tundraBiome(false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.MUSHROOM_FIELDS, VanillaBiomes.mushroomFieldsBiome());
        Biomes.register(net.minecraft.world.level.biome.Biomes.BEACH, VanillaBiomes.beachBiome(0.8f, 0.4f, 4159204, false, false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.JUNGLE, VanillaBiomes.jungleBiome());
        Biomes.register(net.minecraft.world.level.biome.Biomes.SPARSE_JUNGLE, VanillaBiomes.jungleEdgeBiome());
        Biomes.register(net.minecraft.world.level.biome.Biomes.DEEP_OCEAN, VanillaBiomes.oceanBiome(true));
        Biomes.register(net.minecraft.world.level.biome.Biomes.STONY_SHORE, VanillaBiomes.beachBiome(0.2f, 0.3f, 4159204, false, true));
        Biomes.register(net.minecraft.world.level.biome.Biomes.SNOWY_BEACH, VanillaBiomes.beachBiome(0.05f, 0.3f, 4020182, true, false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.BIRCH_FOREST, VanillaBiomes.birchForestBiome(false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.DARK_FOREST, VanillaBiomes.darkForestBiome(false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.SNOWY_TAIGA, VanillaBiomes.taigaBiome(true, false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.OLD_GROWTH_PINE_TAIGA, VanillaBiomes.giantTreeTaiga(0.3f, false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.WINDSWEPT_FOREST, VanillaBiomes.mountainBiome(SurfaceBuilders.GRASS, true));
        Biomes.register(net.minecraft.world.level.biome.Biomes.SAVANNA, VanillaBiomes.savannaBiome(1.2f, false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.SAVANNA_PLATEAU, VanillaBiomes.savanaPlateauBiome());
        Biomes.register(net.minecraft.world.level.biome.Biomes.WOODED_BADLANDS, VanillaBiomes.woodedBadlandsPlateauBiome());
        Biomes.register(net.minecraft.world.level.biome.Biomes.BADLANDS, VanillaBiomes.badlandsBiome());
        Biomes.register(net.minecraft.world.level.biome.Biomes.SMALL_END_ISLANDS, VanillaBiomes.smallEndIslandsBiome());
        Biomes.register(net.minecraft.world.level.biome.Biomes.END_MIDLANDS, VanillaBiomes.endMidlandsBiome());
        Biomes.register(net.minecraft.world.level.biome.Biomes.END_HIGHLANDS, VanillaBiomes.endHighlandsBiome());
        Biomes.register(net.minecraft.world.level.biome.Biomes.END_BARRENS, VanillaBiomes.endBarrensBiome());
        Biomes.register(net.minecraft.world.level.biome.Biomes.WARM_OCEAN, VanillaBiomes.warmOceanBiome());
        Biomes.register(net.minecraft.world.level.biome.Biomes.LUKEWARM_OCEAN, VanillaBiomes.lukeWarmOceanBiome(false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.COLD_OCEAN, VanillaBiomes.coldOceanBiome(false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.DEEP_WARM_OCEAN, VanillaBiomes.deepWarmOceanBiome());
        Biomes.register(net.minecraft.world.level.biome.Biomes.DEEP_LUKEWARM_OCEAN, VanillaBiomes.lukeWarmOceanBiome(true));
        Biomes.register(net.minecraft.world.level.biome.Biomes.DEEP_COLD_OCEAN, VanillaBiomes.coldOceanBiome(true));
        Biomes.register(net.minecraft.world.level.biome.Biomes.DEEP_FROZEN_OCEAN, VanillaBiomes.frozenOceanBiome(true));
        THE_VOID = Biomes.register(net.minecraft.world.level.biome.Biomes.THE_VOID, VanillaBiomes.theVoidBiome());
        Biomes.register(net.minecraft.world.level.biome.Biomes.SUNFLOWER_PLAINS, VanillaBiomes.plainsBiome(true));
        Biomes.register(net.minecraft.world.level.biome.Biomes.WINDSWEPT_GRAVELLY_HILLS, VanillaBiomes.mountainBiome(SurfaceBuilders.GRAVELLY_MOUNTAIN, false));
        Biomes.register(net.minecraft.world.level.biome.Biomes.FLOWER_FOREST, VanillaBiomes.flowerForestBiome());
        Biomes.register(net.minecraft.world.level.biome.Biomes.ICE_SPIKES, VanillaBiomes.tundraBiome(true));
        Biomes.register(net.minecraft.world.level.biome.Biomes.OLD_GROWTH_BIRCH_FOREST, VanillaBiomes.birchForestBiome(true));
        Biomes.register(net.minecraft.world.level.biome.Biomes.OLD_GROWTH_SPRUCE_TAIGA, VanillaBiomes.giantTreeTaiga(0.25f, true));
        Biomes.register(net.minecraft.world.level.biome.Biomes.WINDSWEPT_SAVANNA, VanillaBiomes.savannaBiome(1.1f, true));
        Biomes.register(net.minecraft.world.level.biome.Biomes.ERODED_BADLANDS, VanillaBiomes.erodedBadlandsBiome());
        Biomes.register(net.minecraft.world.level.biome.Biomes.BAMBOO_JUNGLE, VanillaBiomes.bambooJungleBiome());
        Biomes.register(net.minecraft.world.level.biome.Biomes.SOUL_SAND_VALLEY, VanillaBiomes.soulSandValleyBiome());
        Biomes.register(net.minecraft.world.level.biome.Biomes.CRIMSON_FOREST, VanillaBiomes.crimsonForestBiome());
        Biomes.register(net.minecraft.world.level.biome.Biomes.WARPED_FOREST, VanillaBiomes.warpedForestBiome());
        Biomes.register(net.minecraft.world.level.biome.Biomes.BASALT_DELTAS, VanillaBiomes.basaltDeltasBiome());
        Biomes.register(net.minecraft.world.level.biome.Biomes.DRIPSTONE_CAVES, VanillaBiomes.dripstoneCaves());
        Biomes.register(net.minecraft.world.level.biome.Biomes.LUSH_CAVES, VanillaBiomes.lushCaves());
        Biomes.register(net.minecraft.world.level.biome.Biomes.MEADOW, VanillaBiomes.meadowBiome());
        Biomes.register(net.minecraft.world.level.biome.Biomes.GROVE, VanillaBiomes.groveBiome());
        Biomes.register(net.minecraft.world.level.biome.Biomes.SNOWY_SLOPES, VanillaBiomes.snowySlopesBiome());
        Biomes.register(net.minecraft.world.level.biome.Biomes.FROZEN_PEAKS, VanillaBiomes.snowcappedPeaksBiome());
        Biomes.register(net.minecraft.world.level.biome.Biomes.JAGGED_PEAKS, VanillaBiomes.loftyPeaksBiome());
        Biomes.register(net.minecraft.world.level.biome.Biomes.STONY_PEAKS, VanillaBiomes.stonyPeaksBiome());
    }
}

