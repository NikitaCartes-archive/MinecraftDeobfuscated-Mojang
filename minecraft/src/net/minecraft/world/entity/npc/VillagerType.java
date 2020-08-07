package net.minecraft.world.entity.npc;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public final class VillagerType {
	public static final VillagerType DESERT = register("desert");
	public static final VillagerType JUNGLE = register("jungle");
	public static final VillagerType PLAINS = register("plains");
	public static final VillagerType SAVANNA = register("savanna");
	public static final VillagerType SNOW = register("snow");
	public static final VillagerType SWAMP = register("swamp");
	public static final VillagerType TAIGA = register("taiga");
	private final String name;
	private static final Map<ResourceKey<Biome>, VillagerType> BY_BIOME = Util.make(Maps.<ResourceKey<Biome>, VillagerType>newHashMap(), hashMap -> {
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

	private VillagerType(String string) {
		this.name = string;
	}

	public String toString() {
		return this.name;
	}

	private static VillagerType register(String string) {
		return Registry.register(Registry.VILLAGER_TYPE, new ResourceLocation(string), new VillagerType(string));
	}

	public static VillagerType byBiome(Optional<ResourceKey<Biome>> optional) {
		return (VillagerType)optional.flatMap(resourceKey -> Optional.ofNullable(BY_BIOME.get(resourceKey))).orElse(PLAINS);
	}
}
