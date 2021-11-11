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
		hashMap.put(Biomes.DESERT, DESERT);
		hashMap.put(Biomes.ERODED_BADLANDS, DESERT);
		hashMap.put(Biomes.WOODED_BADLANDS, DESERT);
		hashMap.put(Biomes.BAMBOO_JUNGLE, JUNGLE);
		hashMap.put(Biomes.JUNGLE, JUNGLE);
		hashMap.put(Biomes.SPARSE_JUNGLE, JUNGLE);
		hashMap.put(Biomes.SAVANNA_PLATEAU, SAVANNA);
		hashMap.put(Biomes.SAVANNA, SAVANNA);
		hashMap.put(Biomes.WINDSWEPT_SAVANNA, SAVANNA);
		hashMap.put(Biomes.DEEP_FROZEN_OCEAN, SNOW);
		hashMap.put(Biomes.FROZEN_OCEAN, SNOW);
		hashMap.put(Biomes.FROZEN_RIVER, SNOW);
		hashMap.put(Biomes.ICE_SPIKES, SNOW);
		hashMap.put(Biomes.SNOWY_BEACH, SNOW);
		hashMap.put(Biomes.SNOWY_TAIGA, SNOW);
		hashMap.put(Biomes.SNOWY_PLAINS, SNOW);
		hashMap.put(Biomes.GROVE, SNOW);
		hashMap.put(Biomes.SNOWY_SLOPES, SNOW);
		hashMap.put(Biomes.FROZEN_PEAKS, SNOW);
		hashMap.put(Biomes.JAGGED_PEAKS, SNOW);
		hashMap.put(Biomes.SWAMP, SWAMP);
		hashMap.put(Biomes.OLD_GROWTH_SPRUCE_TAIGA, TAIGA);
		hashMap.put(Biomes.OLD_GROWTH_PINE_TAIGA, TAIGA);
		hashMap.put(Biomes.WINDSWEPT_GRAVELLY_HILLS, TAIGA);
		hashMap.put(Biomes.WINDSWEPT_HILLS, TAIGA);
		hashMap.put(Biomes.TAIGA, TAIGA);
		hashMap.put(Biomes.WINDSWEPT_FOREST, TAIGA);
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
		return (VillagerType)optional.flatMap(resourceKey -> Optional.ofNullable((VillagerType)BY_BIOME.get(resourceKey))).orElse(PLAINS);
	}
}
