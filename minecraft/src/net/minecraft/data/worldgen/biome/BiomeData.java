package net.minecraft.data.worldgen.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public abstract class BiomeData {
	public static void bootstrap(BootstapContext<Biome> bootstapContext) {
		HolderGetter<PlacedFeature> holderGetter = bootstapContext.lookup(Registries.PLACED_FEATURE);
		HolderGetter<ConfiguredWorldCarver<?>> holderGetter2 = bootstapContext.lookup(Registries.CONFIGURED_CARVER);
		bootstapContext.register(Biomes.THE_VOID, OverworldBiomes.theVoid(holderGetter, holderGetter2));
		bootstapContext.register(Biomes.PLAINS, OverworldBiomes.plains(holderGetter, holderGetter2, false, false, false));
		bootstapContext.register(Biomes.SUNFLOWER_PLAINS, OverworldBiomes.plains(holderGetter, holderGetter2, true, false, false));
		bootstapContext.register(Biomes.SNOWY_PLAINS, OverworldBiomes.plains(holderGetter, holderGetter2, false, true, false));
		bootstapContext.register(Biomes.ICE_SPIKES, OverworldBiomes.plains(holderGetter, holderGetter2, false, true, true));
		bootstapContext.register(Biomes.DESERT, OverworldBiomes.desert(holderGetter, holderGetter2));
		bootstapContext.register(Biomes.SWAMP, OverworldBiomes.swamp(holderGetter, holderGetter2));
		bootstapContext.register(Biomes.MANGROVE_SWAMP, OverworldBiomes.mangroveSwamp(holderGetter, holderGetter2));
		bootstapContext.register(Biomes.FOREST, OverworldBiomes.forest(holderGetter, holderGetter2, false, false, false));
		bootstapContext.register(Biomes.FLOWER_FOREST, OverworldBiomes.forest(holderGetter, holderGetter2, false, false, true));
		bootstapContext.register(Biomes.BIRCH_FOREST, OverworldBiomes.forest(holderGetter, holderGetter2, true, false, false));
		bootstapContext.register(Biomes.DARK_FOREST, OverworldBiomes.darkForest(holderGetter, holderGetter2));
		bootstapContext.register(Biomes.OLD_GROWTH_BIRCH_FOREST, OverworldBiomes.forest(holderGetter, holderGetter2, true, true, false));
		bootstapContext.register(Biomes.OLD_GROWTH_PINE_TAIGA, OverworldBiomes.oldGrowthTaiga(holderGetter, holderGetter2, false));
		bootstapContext.register(Biomes.OLD_GROWTH_SPRUCE_TAIGA, OverworldBiomes.oldGrowthTaiga(holderGetter, holderGetter2, true));
		bootstapContext.register(Biomes.TAIGA, OverworldBiomes.taiga(holderGetter, holderGetter2, false));
		bootstapContext.register(Biomes.SNOWY_TAIGA, OverworldBiomes.taiga(holderGetter, holderGetter2, true));
		bootstapContext.register(Biomes.SAVANNA, OverworldBiomes.savanna(holderGetter, holderGetter2, false, false));
		bootstapContext.register(Biomes.SAVANNA_PLATEAU, OverworldBiomes.savanna(holderGetter, holderGetter2, false, true));
		bootstapContext.register(Biomes.WINDSWEPT_HILLS, OverworldBiomes.windsweptHills(holderGetter, holderGetter2, false));
		bootstapContext.register(Biomes.WINDSWEPT_GRAVELLY_HILLS, OverworldBiomes.windsweptHills(holderGetter, holderGetter2, false));
		bootstapContext.register(Biomes.WINDSWEPT_FOREST, OverworldBiomes.windsweptHills(holderGetter, holderGetter2, true));
		bootstapContext.register(Biomes.WINDSWEPT_SAVANNA, OverworldBiomes.savanna(holderGetter, holderGetter2, true, false));
		bootstapContext.register(Biomes.JUNGLE, OverworldBiomes.jungle(holderGetter, holderGetter2));
		bootstapContext.register(Biomes.SPARSE_JUNGLE, OverworldBiomes.sparseJungle(holderGetter, holderGetter2));
		bootstapContext.register(Biomes.BAMBOO_JUNGLE, OverworldBiomes.bambooJungle(holderGetter, holderGetter2));
		bootstapContext.register(Biomes.BADLANDS, OverworldBiomes.badlands(holderGetter, holderGetter2, false));
		bootstapContext.register(Biomes.ERODED_BADLANDS, OverworldBiomes.badlands(holderGetter, holderGetter2, false));
		bootstapContext.register(Biomes.WOODED_BADLANDS, OverworldBiomes.badlands(holderGetter, holderGetter2, true));
		bootstapContext.register(Biomes.MEADOW, OverworldBiomes.meadowOrCherryGrove(holderGetter, holderGetter2, false));
		bootstapContext.register(Biomes.GROVE, OverworldBiomes.grove(holderGetter, holderGetter2));
		bootstapContext.register(Biomes.SNOWY_SLOPES, OverworldBiomes.snowySlopes(holderGetter, holderGetter2));
		bootstapContext.register(Biomes.FROZEN_PEAKS, OverworldBiomes.frozenPeaks(holderGetter, holderGetter2));
		bootstapContext.register(Biomes.JAGGED_PEAKS, OverworldBiomes.jaggedPeaks(holderGetter, holderGetter2));
		bootstapContext.register(Biomes.STONY_PEAKS, OverworldBiomes.stonyPeaks(holderGetter, holderGetter2));
		bootstapContext.register(Biomes.RIVER, OverworldBiomes.river(holderGetter, holderGetter2, false));
		bootstapContext.register(Biomes.FROZEN_RIVER, OverworldBiomes.river(holderGetter, holderGetter2, true));
		bootstapContext.register(Biomes.BEACH, OverworldBiomes.beach(holderGetter, holderGetter2, false, false));
		bootstapContext.register(Biomes.SNOWY_BEACH, OverworldBiomes.beach(holderGetter, holderGetter2, true, false));
		bootstapContext.register(Biomes.STONY_SHORE, OverworldBiomes.beach(holderGetter, holderGetter2, false, true));
		bootstapContext.register(Biomes.WARM_OCEAN, OverworldBiomes.warmOcean(holderGetter, holderGetter2));
		bootstapContext.register(Biomes.LUKEWARM_OCEAN, OverworldBiomes.lukeWarmOcean(holderGetter, holderGetter2, false));
		bootstapContext.register(Biomes.DEEP_LUKEWARM_OCEAN, OverworldBiomes.lukeWarmOcean(holderGetter, holderGetter2, true));
		bootstapContext.register(Biomes.OCEAN, OverworldBiomes.ocean(holderGetter, holderGetter2, false));
		bootstapContext.register(Biomes.DEEP_OCEAN, OverworldBiomes.ocean(holderGetter, holderGetter2, true));
		bootstapContext.register(Biomes.COLD_OCEAN, OverworldBiomes.coldOcean(holderGetter, holderGetter2, false));
		bootstapContext.register(Biomes.DEEP_COLD_OCEAN, OverworldBiomes.coldOcean(holderGetter, holderGetter2, true));
		bootstapContext.register(Biomes.FROZEN_OCEAN, OverworldBiomes.frozenOcean(holderGetter, holderGetter2, false));
		bootstapContext.register(Biomes.DEEP_FROZEN_OCEAN, OverworldBiomes.frozenOcean(holderGetter, holderGetter2, true));
		bootstapContext.register(Biomes.MUSHROOM_FIELDS, OverworldBiomes.mushroomFields(holderGetter, holderGetter2));
		bootstapContext.register(Biomes.DRIPSTONE_CAVES, OverworldBiomes.dripstoneCaves(holderGetter, holderGetter2));
		bootstapContext.register(Biomes.LUSH_CAVES, OverworldBiomes.lushCaves(holderGetter, holderGetter2));
		bootstapContext.register(Biomes.DEEP_DARK, OverworldBiomes.deepDark(holderGetter, holderGetter2));
		bootstapContext.register(Biomes.NETHER_WASTES, NetherBiomes.netherWastes(holderGetter, holderGetter2));
		bootstapContext.register(Biomes.WARPED_FOREST, NetherBiomes.warpedForest(holderGetter, holderGetter2));
		bootstapContext.register(Biomes.CRIMSON_FOREST, NetherBiomes.crimsonForest(holderGetter, holderGetter2));
		bootstapContext.register(Biomes.SOUL_SAND_VALLEY, NetherBiomes.soulSandValley(holderGetter, holderGetter2));
		bootstapContext.register(Biomes.BASALT_DELTAS, NetherBiomes.basaltDeltas(holderGetter, holderGetter2));
		bootstapContext.register(Biomes.THE_END, EndBiomes.theEnd(holderGetter, holderGetter2));
		bootstapContext.register(Biomes.END_HIGHLANDS, EndBiomes.endHighlands(holderGetter, holderGetter2));
		bootstapContext.register(Biomes.END_MIDLANDS, EndBiomes.endMidlands(holderGetter, holderGetter2));
		bootstapContext.register(Biomes.SMALL_END_ISLANDS, EndBiomes.smallEndIslands(holderGetter, holderGetter2));
		bootstapContext.register(Biomes.END_BARRENS, EndBiomes.endBarrens(holderGetter, holderGetter2));
	}

	public static void nextUpdate(BootstapContext<Biome> bootstapContext) {
		HolderGetter<PlacedFeature> holderGetter = bootstapContext.lookup(Registries.PLACED_FEATURE);
		HolderGetter<ConfiguredWorldCarver<?>> holderGetter2 = bootstapContext.lookup(Registries.CONFIGURED_CARVER);
		bootstapContext.register(Biomes.CHERRY_GROVE, OverworldBiomes.meadowOrCherryGrove(holderGetter, holderGetter2, true));
	}
}
