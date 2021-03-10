/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.worldgen.biome;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.SurfaceBuilders;
import net.minecraft.data.worldgen.biome.VanillaBiomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public abstract class Biomes {
    private static final Int2ObjectMap<ResourceKey<Biome>> TO_NAME = new Int2ObjectArrayMap<ResourceKey<Biome>>();
    public static final Biome PLAINS;
    public static final Biome THE_VOID;

    private static Biome register(int i, ResourceKey<Biome> resourceKey, Biome biome) {
        TO_NAME.put(i, resourceKey);
        return BuiltinRegistries.registerMapping(BuiltinRegistries.BIOME, i, resourceKey, biome);
    }

    public static ResourceKey<Biome> byId(int i) {
        return (ResourceKey)TO_NAME.get(i);
    }

    static {
        Biomes.register(0, net.minecraft.world.level.biome.Biomes.OCEAN, VanillaBiomes.oceanBiome(false));
        PLAINS = Biomes.register(1, net.minecraft.world.level.biome.Biomes.PLAINS, VanillaBiomes.plainsBiome(false));
        Biomes.register(2, net.minecraft.world.level.biome.Biomes.DESERT, VanillaBiomes.desertBiome(0.125f, 0.05f, true, true, true));
        Biomes.register(3, net.minecraft.world.level.biome.Biomes.MOUNTAINS, VanillaBiomes.mountainBiome(1.0f, 0.5f, SurfaceBuilders.MOUNTAIN, false));
        Biomes.register(4, net.minecraft.world.level.biome.Biomes.FOREST, VanillaBiomes.forestBiome(0.1f, 0.2f));
        Biomes.register(5, net.minecraft.world.level.biome.Biomes.TAIGA, VanillaBiomes.taigaBiome(0.2f, 0.2f, false, false, true, false));
        Biomes.register(6, net.minecraft.world.level.biome.Biomes.SWAMP, VanillaBiomes.swampBiome(-0.2f, 0.1f, false));
        Biomes.register(7, net.minecraft.world.level.biome.Biomes.RIVER, VanillaBiomes.riverBiome(-0.5f, 0.0f, 0.5f, 4159204, false));
        Biomes.register(8, net.minecraft.world.level.biome.Biomes.NETHER_WASTES, VanillaBiomes.netherWastesBiome());
        Biomes.register(9, net.minecraft.world.level.biome.Biomes.THE_END, VanillaBiomes.theEndBiome());
        Biomes.register(10, net.minecraft.world.level.biome.Biomes.FROZEN_OCEAN, VanillaBiomes.frozenOceanBiome(false));
        Biomes.register(11, net.minecraft.world.level.biome.Biomes.FROZEN_RIVER, VanillaBiomes.riverBiome(-0.5f, 0.0f, 0.0f, 3750089, true));
        Biomes.register(12, net.minecraft.world.level.biome.Biomes.SNOWY_TUNDRA, VanillaBiomes.tundraBiome(0.125f, 0.05f, false, false));
        Biomes.register(13, net.minecraft.world.level.biome.Biomes.SNOWY_MOUNTAINS, VanillaBiomes.tundraBiome(0.45f, 0.3f, false, true));
        Biomes.register(14, net.minecraft.world.level.biome.Biomes.MUSHROOM_FIELDS, VanillaBiomes.mushroomFieldsBiome(0.2f, 0.3f));
        Biomes.register(15, net.minecraft.world.level.biome.Biomes.MUSHROOM_FIELD_SHORE, VanillaBiomes.mushroomFieldsBiome(0.0f, 0.025f));
        Biomes.register(16, net.minecraft.world.level.biome.Biomes.BEACH, VanillaBiomes.beachBiome(0.0f, 0.025f, 0.8f, 0.4f, 4159204, false, false));
        Biomes.register(17, net.minecraft.world.level.biome.Biomes.DESERT_HILLS, VanillaBiomes.desertBiome(0.45f, 0.3f, false, true, false));
        Biomes.register(18, net.minecraft.world.level.biome.Biomes.WOODED_HILLS, VanillaBiomes.forestBiome(0.45f, 0.3f));
        Biomes.register(19, net.minecraft.world.level.biome.Biomes.TAIGA_HILLS, VanillaBiomes.taigaBiome(0.45f, 0.3f, false, false, false, false));
        Biomes.register(20, net.minecraft.world.level.biome.Biomes.MOUNTAIN_EDGE, VanillaBiomes.mountainBiome(0.8f, 0.3f, SurfaceBuilders.GRASS, true));
        Biomes.register(21, net.minecraft.world.level.biome.Biomes.JUNGLE, VanillaBiomes.jungleBiome());
        Biomes.register(22, net.minecraft.world.level.biome.Biomes.JUNGLE_HILLS, VanillaBiomes.jungleHillsBiome());
        Biomes.register(23, net.minecraft.world.level.biome.Biomes.JUNGLE_EDGE, VanillaBiomes.jungleEdgeBiome());
        Biomes.register(24, net.minecraft.world.level.biome.Biomes.DEEP_OCEAN, VanillaBiomes.oceanBiome(true));
        Biomes.register(25, net.minecraft.world.level.biome.Biomes.STONE_SHORE, VanillaBiomes.beachBiome(0.1f, 0.8f, 0.2f, 0.3f, 4159204, false, true));
        Biomes.register(26, net.minecraft.world.level.biome.Biomes.SNOWY_BEACH, VanillaBiomes.beachBiome(0.0f, 0.025f, 0.05f, 0.3f, 4020182, true, false));
        Biomes.register(27, net.minecraft.world.level.biome.Biomes.BIRCH_FOREST, VanillaBiomes.birchForestBiome(0.1f, 0.2f, false));
        Biomes.register(28, net.minecraft.world.level.biome.Biomes.BIRCH_FOREST_HILLS, VanillaBiomes.birchForestBiome(0.45f, 0.3f, false));
        Biomes.register(29, net.minecraft.world.level.biome.Biomes.DARK_FOREST, VanillaBiomes.darkForestBiome(0.1f, 0.2f, false));
        Biomes.register(30, net.minecraft.world.level.biome.Biomes.SNOWY_TAIGA, VanillaBiomes.taigaBiome(0.2f, 0.2f, true, false, false, true));
        Biomes.register(31, net.minecraft.world.level.biome.Biomes.SNOWY_TAIGA_HILLS, VanillaBiomes.taigaBiome(0.45f, 0.3f, true, false, false, false));
        Biomes.register(32, net.minecraft.world.level.biome.Biomes.GIANT_TREE_TAIGA, VanillaBiomes.giantTreeTaiga(0.2f, 0.2f, 0.3f, false));
        Biomes.register(33, net.minecraft.world.level.biome.Biomes.GIANT_TREE_TAIGA_HILLS, VanillaBiomes.giantTreeTaiga(0.45f, 0.3f, 0.3f, false));
        Biomes.register(34, net.minecraft.world.level.biome.Biomes.WOODED_MOUNTAINS, VanillaBiomes.mountainBiome(1.0f, 0.5f, SurfaceBuilders.GRASS, true));
        Biomes.register(35, net.minecraft.world.level.biome.Biomes.SAVANNA, VanillaBiomes.savannaBiome(0.125f, 0.05f, 1.2f, false, false));
        Biomes.register(36, net.minecraft.world.level.biome.Biomes.SAVANNA_PLATEAU, VanillaBiomes.savanaPlateauBiome());
        Biomes.register(37, net.minecraft.world.level.biome.Biomes.BADLANDS, VanillaBiomes.badlandsBiome(0.1f, 0.2f, false));
        Biomes.register(38, net.minecraft.world.level.biome.Biomes.WOODED_BADLANDS_PLATEAU, VanillaBiomes.woodedBadlandsPlateauBiome(1.5f, 0.025f));
        Biomes.register(39, net.minecraft.world.level.biome.Biomes.BADLANDS_PLATEAU, VanillaBiomes.badlandsBiome(1.5f, 0.025f, true));
        Biomes.register(40, net.minecraft.world.level.biome.Biomes.SMALL_END_ISLANDS, VanillaBiomes.smallEndIslandsBiome());
        Biomes.register(41, net.minecraft.world.level.biome.Biomes.END_MIDLANDS, VanillaBiomes.endMidlandsBiome());
        Biomes.register(42, net.minecraft.world.level.biome.Biomes.END_HIGHLANDS, VanillaBiomes.endHighlandsBiome());
        Biomes.register(43, net.minecraft.world.level.biome.Biomes.END_BARRENS, VanillaBiomes.endBarrensBiome());
        Biomes.register(44, net.minecraft.world.level.biome.Biomes.WARM_OCEAN, VanillaBiomes.warmOceanBiome());
        Biomes.register(45, net.minecraft.world.level.biome.Biomes.LUKEWARM_OCEAN, VanillaBiomes.lukeWarmOceanBiome(false));
        Biomes.register(46, net.minecraft.world.level.biome.Biomes.COLD_OCEAN, VanillaBiomes.coldOceanBiome(false));
        Biomes.register(47, net.minecraft.world.level.biome.Biomes.DEEP_WARM_OCEAN, VanillaBiomes.deepWarmOceanBiome());
        Biomes.register(48, net.minecraft.world.level.biome.Biomes.DEEP_LUKEWARM_OCEAN, VanillaBiomes.lukeWarmOceanBiome(true));
        Biomes.register(49, net.minecraft.world.level.biome.Biomes.DEEP_COLD_OCEAN, VanillaBiomes.coldOceanBiome(true));
        Biomes.register(50, net.minecraft.world.level.biome.Biomes.DEEP_FROZEN_OCEAN, VanillaBiomes.frozenOceanBiome(true));
        THE_VOID = Biomes.register(127, net.minecraft.world.level.biome.Biomes.THE_VOID, VanillaBiomes.theVoidBiome());
        Biomes.register(129, net.minecraft.world.level.biome.Biomes.SUNFLOWER_PLAINS, VanillaBiomes.plainsBiome(true));
        Biomes.register(130, net.minecraft.world.level.biome.Biomes.DESERT_LAKES, VanillaBiomes.desertBiome(0.225f, 0.25f, false, false, false));
        Biomes.register(131, net.minecraft.world.level.biome.Biomes.GRAVELLY_MOUNTAINS, VanillaBiomes.mountainBiome(1.0f, 0.5f, SurfaceBuilders.GRAVELLY_MOUNTAIN, false));
        Biomes.register(132, net.minecraft.world.level.biome.Biomes.FLOWER_FOREST, VanillaBiomes.flowerForestBiome());
        Biomes.register(133, net.minecraft.world.level.biome.Biomes.TAIGA_MOUNTAINS, VanillaBiomes.taigaBiome(0.3f, 0.4f, false, true, false, false));
        Biomes.register(134, net.minecraft.world.level.biome.Biomes.SWAMP_HILLS, VanillaBiomes.swampBiome(-0.1f, 0.3f, true));
        Biomes.register(140, net.minecraft.world.level.biome.Biomes.ICE_SPIKES, VanillaBiomes.tundraBiome(0.425f, 0.45000002f, true, false));
        Biomes.register(149, net.minecraft.world.level.biome.Biomes.MODIFIED_JUNGLE, VanillaBiomes.modifiedJungleBiome());
        Biomes.register(151, net.minecraft.world.level.biome.Biomes.MODIFIED_JUNGLE_EDGE, VanillaBiomes.modifiedJungleEdgeBiome());
        Biomes.register(155, net.minecraft.world.level.biome.Biomes.TALL_BIRCH_FOREST, VanillaBiomes.birchForestBiome(0.2f, 0.4f, true));
        Biomes.register(156, net.minecraft.world.level.biome.Biomes.TALL_BIRCH_HILLS, VanillaBiomes.birchForestBiome(0.55f, 0.5f, true));
        Biomes.register(157, net.minecraft.world.level.biome.Biomes.DARK_FOREST_HILLS, VanillaBiomes.darkForestBiome(0.2f, 0.4f, true));
        Biomes.register(158, net.minecraft.world.level.biome.Biomes.SNOWY_TAIGA_MOUNTAINS, VanillaBiomes.taigaBiome(0.3f, 0.4f, true, true, false, false));
        Biomes.register(160, net.minecraft.world.level.biome.Biomes.GIANT_SPRUCE_TAIGA, VanillaBiomes.giantTreeTaiga(0.2f, 0.2f, 0.25f, true));
        Biomes.register(161, net.minecraft.world.level.biome.Biomes.GIANT_SPRUCE_TAIGA_HILLS, VanillaBiomes.giantTreeTaiga(0.2f, 0.2f, 0.25f, true));
        Biomes.register(162, net.minecraft.world.level.biome.Biomes.MODIFIED_GRAVELLY_MOUNTAINS, VanillaBiomes.mountainBiome(1.0f, 0.5f, SurfaceBuilders.GRAVELLY_MOUNTAIN, false));
        Biomes.register(163, net.minecraft.world.level.biome.Biomes.SHATTERED_SAVANNA, VanillaBiomes.savannaBiome(0.3625f, 1.225f, 1.1f, true, true));
        Biomes.register(164, net.minecraft.world.level.biome.Biomes.SHATTERED_SAVANNA_PLATEAU, VanillaBiomes.savannaBiome(1.05f, 1.2125001f, 1.0f, true, true));
        Biomes.register(165, net.minecraft.world.level.biome.Biomes.ERODED_BADLANDS, VanillaBiomes.erodedBadlandsBiome());
        Biomes.register(166, net.minecraft.world.level.biome.Biomes.MODIFIED_WOODED_BADLANDS_PLATEAU, VanillaBiomes.woodedBadlandsPlateauBiome(0.45f, 0.3f));
        Biomes.register(167, net.minecraft.world.level.biome.Biomes.MODIFIED_BADLANDS_PLATEAU, VanillaBiomes.badlandsBiome(0.45f, 0.3f, true));
        Biomes.register(168, net.minecraft.world.level.biome.Biomes.BAMBOO_JUNGLE, VanillaBiomes.bambooJungleBiome());
        Biomes.register(169, net.minecraft.world.level.biome.Biomes.BAMBOO_JUNGLE_HILLS, VanillaBiomes.bambooJungleHillsBiome());
        Biomes.register(170, net.minecraft.world.level.biome.Biomes.SOUL_SAND_VALLEY, VanillaBiomes.soulSandValleyBiome());
        Biomes.register(171, net.minecraft.world.level.biome.Biomes.CRIMSON_FOREST, VanillaBiomes.crimsonForestBiome());
        Biomes.register(172, net.minecraft.world.level.biome.Biomes.WARPED_FOREST, VanillaBiomes.warpedForestBiome());
        Biomes.register(173, net.minecraft.world.level.biome.Biomes.BASALT_DELTAS, VanillaBiomes.basaltDeltasBiome());
        Biomes.register(174, net.minecraft.world.level.biome.Biomes.DRIPSTONE_CAVES, VanillaBiomes.dripstoneCaves());
        Biomes.register(175, net.minecraft.world.level.biome.Biomes.LUSH_CAVES, VanillaBiomes.lushCaves());
    }
}

