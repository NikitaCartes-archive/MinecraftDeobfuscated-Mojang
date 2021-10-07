package net.minecraft.data.worldgen.biome;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.SurfaceBuilders;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public abstract class Biomes {
	public static final Biome PLAINS = register(net.minecraft.world.level.biome.Biomes.PLAINS, VanillaBiomes.plainsBiome(false));
	public static final Biome THE_VOID = register(net.minecraft.world.level.biome.Biomes.THE_VOID, VanillaBiomes.theVoidBiome());

	private static Biome register(ResourceKey<Biome> resourceKey, Biome biome) {
		return BuiltinRegistries.registerMapping(BuiltinRegistries.BIOME, resourceKey, biome);
	}

	static {
		register(net.minecraft.world.level.biome.Biomes.OCEAN, VanillaBiomes.oceanBiome(false));
		register(net.minecraft.world.level.biome.Biomes.DESERT, VanillaBiomes.desertBiome(true));
		register(net.minecraft.world.level.biome.Biomes.WINDSWEPT_HILLS, VanillaBiomes.mountainBiome(SurfaceBuilders.MOUNTAIN, false));
		register(net.minecraft.world.level.biome.Biomes.FOREST, VanillaBiomes.forestBiome());
		register(net.minecraft.world.level.biome.Biomes.TAIGA, VanillaBiomes.taigaBiome(false, false));
		register(net.minecraft.world.level.biome.Biomes.SWAMP, VanillaBiomes.swampBiome(false));
		register(net.minecraft.world.level.biome.Biomes.RIVER, VanillaBiomes.riverBiome(0.5F, 4159204, false));
		register(net.minecraft.world.level.biome.Biomes.NETHER_WASTES, VanillaBiomes.netherWastesBiome());
		register(net.minecraft.world.level.biome.Biomes.THE_END, VanillaBiomes.theEndBiome());
		register(net.minecraft.world.level.biome.Biomes.FROZEN_OCEAN, VanillaBiomes.frozenOceanBiome(false));
		register(net.minecraft.world.level.biome.Biomes.FROZEN_RIVER, VanillaBiomes.riverBiome(0.0F, 3750089, true));
		register(net.minecraft.world.level.biome.Biomes.SNOWY_PLAINS, VanillaBiomes.tundraBiome(false));
		register(net.minecraft.world.level.biome.Biomes.MUSHROOM_FIELDS, VanillaBiomes.mushroomFieldsBiome());
		register(net.minecraft.world.level.biome.Biomes.BEACH, VanillaBiomes.beachBiome(0.8F, 0.4F, 4159204, false, false));
		register(net.minecraft.world.level.biome.Biomes.JUNGLE, VanillaBiomes.jungleBiome());
		register(net.minecraft.world.level.biome.Biomes.SPARSE_JUNGLE, VanillaBiomes.jungleEdgeBiome());
		register(net.minecraft.world.level.biome.Biomes.DEEP_OCEAN, VanillaBiomes.oceanBiome(true));
		register(net.minecraft.world.level.biome.Biomes.STONY_SHORE, VanillaBiomes.beachBiome(0.2F, 0.3F, 4159204, false, true));
		register(net.minecraft.world.level.biome.Biomes.SNOWY_BEACH, VanillaBiomes.beachBiome(0.05F, 0.3F, 4020182, true, false));
		register(net.minecraft.world.level.biome.Biomes.BIRCH_FOREST, VanillaBiomes.birchForestBiome(false));
		register(net.minecraft.world.level.biome.Biomes.DARK_FOREST, VanillaBiomes.darkForestBiome(false));
		register(net.minecraft.world.level.biome.Biomes.SNOWY_TAIGA, VanillaBiomes.taigaBiome(true, false));
		register(net.minecraft.world.level.biome.Biomes.OLD_GROWTH_PINE_TAIGA, VanillaBiomes.giantTreeTaiga(0.3F, false));
		register(net.minecraft.world.level.biome.Biomes.WINDSWEPT_FOREST, VanillaBiomes.mountainBiome(SurfaceBuilders.GRASS, true));
		register(net.minecraft.world.level.biome.Biomes.SAVANNA, VanillaBiomes.savannaBiome(1.2F, false));
		register(net.minecraft.world.level.biome.Biomes.SAVANNA_PLATEAU, VanillaBiomes.savanaPlateauBiome());
		register(net.minecraft.world.level.biome.Biomes.WOODED_BADLANDS, VanillaBiomes.woodedBadlandsPlateauBiome());
		register(net.minecraft.world.level.biome.Biomes.BADLANDS, VanillaBiomes.badlandsBiome());
		register(net.minecraft.world.level.biome.Biomes.SMALL_END_ISLANDS, VanillaBiomes.smallEndIslandsBiome());
		register(net.minecraft.world.level.biome.Biomes.END_MIDLANDS, VanillaBiomes.endMidlandsBiome());
		register(net.minecraft.world.level.biome.Biomes.END_HIGHLANDS, VanillaBiomes.endHighlandsBiome());
		register(net.minecraft.world.level.biome.Biomes.END_BARRENS, VanillaBiomes.endBarrensBiome());
		register(net.minecraft.world.level.biome.Biomes.WARM_OCEAN, VanillaBiomes.warmOceanBiome());
		register(net.minecraft.world.level.biome.Biomes.LUKEWARM_OCEAN, VanillaBiomes.lukeWarmOceanBiome(false));
		register(net.minecraft.world.level.biome.Biomes.COLD_OCEAN, VanillaBiomes.coldOceanBiome(false));
		register(net.minecraft.world.level.biome.Biomes.DEEP_WARM_OCEAN, VanillaBiomes.deepWarmOceanBiome());
		register(net.minecraft.world.level.biome.Biomes.DEEP_LUKEWARM_OCEAN, VanillaBiomes.lukeWarmOceanBiome(true));
		register(net.minecraft.world.level.biome.Biomes.DEEP_COLD_OCEAN, VanillaBiomes.coldOceanBiome(true));
		register(net.minecraft.world.level.biome.Biomes.DEEP_FROZEN_OCEAN, VanillaBiomes.frozenOceanBiome(true));
		register(net.minecraft.world.level.biome.Biomes.SUNFLOWER_PLAINS, VanillaBiomes.plainsBiome(true));
		register(net.minecraft.world.level.biome.Biomes.WINDSWEPT_GRAVELLY_HILLS, VanillaBiomes.mountainBiome(SurfaceBuilders.GRAVELLY_MOUNTAIN, false));
		register(net.minecraft.world.level.biome.Biomes.FLOWER_FOREST, VanillaBiomes.flowerForestBiome());
		register(net.minecraft.world.level.biome.Biomes.ICE_SPIKES, VanillaBiomes.tundraBiome(true));
		register(net.minecraft.world.level.biome.Biomes.OLD_GROWTH_BIRCH_FOREST, VanillaBiomes.birchForestBiome(true));
		register(net.minecraft.world.level.biome.Biomes.OLD_GROWTH_SPRUCE_TAIGA, VanillaBiomes.giantTreeTaiga(0.25F, true));
		register(net.minecraft.world.level.biome.Biomes.WINDSWEPT_SAVANNA, VanillaBiomes.savannaBiome(1.1F, true));
		register(net.minecraft.world.level.biome.Biomes.ERODED_BADLANDS, VanillaBiomes.erodedBadlandsBiome());
		register(net.minecraft.world.level.biome.Biomes.BAMBOO_JUNGLE, VanillaBiomes.bambooJungleBiome());
		register(net.minecraft.world.level.biome.Biomes.SOUL_SAND_VALLEY, VanillaBiomes.soulSandValleyBiome());
		register(net.minecraft.world.level.biome.Biomes.CRIMSON_FOREST, VanillaBiomes.crimsonForestBiome());
		register(net.minecraft.world.level.biome.Biomes.WARPED_FOREST, VanillaBiomes.warpedForestBiome());
		register(net.minecraft.world.level.biome.Biomes.BASALT_DELTAS, VanillaBiomes.basaltDeltasBiome());
		register(net.minecraft.world.level.biome.Biomes.DRIPSTONE_CAVES, VanillaBiomes.dripstoneCaves());
		register(net.minecraft.world.level.biome.Biomes.LUSH_CAVES, VanillaBiomes.lushCaves());
		register(net.minecraft.world.level.biome.Biomes.MEADOW, VanillaBiomes.meadowBiome());
		register(net.minecraft.world.level.biome.Biomes.GROVE, VanillaBiomes.groveBiome());
		register(net.minecraft.world.level.biome.Biomes.SNOWY_SLOPES, VanillaBiomes.snowySlopesBiome());
		register(net.minecraft.world.level.biome.Biomes.FROZEN_PEAKS, VanillaBiomes.snowcappedPeaksBiome());
		register(net.minecraft.world.level.biome.Biomes.JAGGED_PEAKS, VanillaBiomes.loftyPeaksBiome());
		register(net.minecraft.world.level.biome.Biomes.STONY_PEAKS, VanillaBiomes.stonyPeaksBiome());
	}
}
