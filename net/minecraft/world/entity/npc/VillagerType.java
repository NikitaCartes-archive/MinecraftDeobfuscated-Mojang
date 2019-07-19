/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.npc;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public interface VillagerType {
    public static final VillagerType DESERT = VillagerType.register("desert");
    public static final VillagerType JUNGLE = VillagerType.register("jungle");
    public static final VillagerType PLAINS = VillagerType.register("plains");
    public static final VillagerType SAVANNA = VillagerType.register("savanna");
    public static final VillagerType SNOW = VillagerType.register("snow");
    public static final VillagerType SWAMP = VillagerType.register("swamp");
    public static final VillagerType TAIGA = VillagerType.register("taiga");
    public static final Map<Biome, VillagerType> BY_BIOME = Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put(Biomes.BADLANDS, DESERT);
        hashMap.put(Biomes.BADLANDS_PLATEAU, DESERT);
        hashMap.put(Biomes.DESERT, DESERT);
        hashMap.put(Biomes.DESERT_HILLS, DESERT);
        hashMap.put(Biomes.DESERT_LAKES, DESERT);
        hashMap.put(Biomes.ERODED_BADLANDS, DESERT);
        hashMap.put(Biomes.MODIFIED_BADLANDS_PLATEAU, DESERT);
        hashMap.put(Biomes.MODIFIED_WOODED_BADLANDS_PLATEAU, DESERT);
        hashMap.put(Biomes.WOODED_BADLANDS_PLATEAU, DESERT);
        hashMap.put(Biomes.BAMBOO_JUNGLE, JUNGLE);
        hashMap.put(Biomes.BAMBOO_JUNGLE_HILLS, JUNGLE);
        hashMap.put(Biomes.JUNGLE, JUNGLE);
        hashMap.put(Biomes.JUNGLE_EDGE, JUNGLE);
        hashMap.put(Biomes.JUNGLE_HILLS, JUNGLE);
        hashMap.put(Biomes.MODIFIED_JUNGLE, JUNGLE);
        hashMap.put(Biomes.MODIFIED_JUNGLE_EDGE, JUNGLE);
        hashMap.put(Biomes.SAVANNA_PLATEAU, SAVANNA);
        hashMap.put(Biomes.SAVANNA, SAVANNA);
        hashMap.put(Biomes.SHATTERED_SAVANNA, SAVANNA);
        hashMap.put(Biomes.SHATTERED_SAVANNA_PLATEAU, SAVANNA);
        hashMap.put(Biomes.DEEP_FROZEN_OCEAN, SNOW);
        hashMap.put(Biomes.FROZEN_OCEAN, SNOW);
        hashMap.put(Biomes.FROZEN_RIVER, SNOW);
        hashMap.put(Biomes.ICE_SPIKES, SNOW);
        hashMap.put(Biomes.SNOWY_BEACH, SNOW);
        hashMap.put(Biomes.SNOWY_MOUNTAINS, SNOW);
        hashMap.put(Biomes.SNOWY_TAIGA, SNOW);
        hashMap.put(Biomes.SNOWY_TAIGA_HILLS, SNOW);
        hashMap.put(Biomes.SNOWY_TAIGA_MOUNTAINS, SNOW);
        hashMap.put(Biomes.SNOWY_TUNDRA, SNOW);
        hashMap.put(Biomes.SWAMP, SWAMP);
        hashMap.put(Biomes.SWAMP_HILLS, SWAMP);
        hashMap.put(Biomes.GIANT_SPRUCE_TAIGA, TAIGA);
        hashMap.put(Biomes.GIANT_SPRUCE_TAIGA_HILLS, TAIGA);
        hashMap.put(Biomes.GIANT_TREE_TAIGA, TAIGA);
        hashMap.put(Biomes.GIANT_TREE_TAIGA_HILLS, TAIGA);
        hashMap.put(Biomes.GRAVELLY_MOUNTAINS, TAIGA);
        hashMap.put(Biomes.MODIFIED_GRAVELLY_MOUNTAINS, TAIGA);
        hashMap.put(Biomes.MOUNTAIN_EDGE, TAIGA);
        hashMap.put(Biomes.MOUNTAINS, TAIGA);
        hashMap.put(Biomes.TAIGA, TAIGA);
        hashMap.put(Biomes.TAIGA_HILLS, TAIGA);
        hashMap.put(Biomes.TAIGA_MOUNTAINS, TAIGA);
        hashMap.put(Biomes.WOODED_MOUNTAINS, TAIGA);
    });

    public static VillagerType register(final String string) {
        return Registry.register(Registry.VILLAGER_TYPE, new ResourceLocation(string), new VillagerType(){

            public String toString() {
                return string;
            }
        });
    }

    public static VillagerType byBiome(Biome biome) {
        return BY_BIOME.getOrDefault(biome, PLAINS);
    }
}

