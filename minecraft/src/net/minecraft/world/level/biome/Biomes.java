package net.minecraft.world.level.biome;

import java.util.Collections;
import javax.annotation.Nullable;
import net.minecraft.core.IdMapper;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.biome.BadlandsBiome;
import net.minecraft.data.worldgen.biome.BadlandsPlateauBiome;
import net.minecraft.data.worldgen.biome.BambooJungleBiome;
import net.minecraft.data.worldgen.biome.BambooJungleHillsBiome;
import net.minecraft.data.worldgen.biome.BasaltDeltasBiome;
import net.minecraft.data.worldgen.biome.BeachBiome;
import net.minecraft.data.worldgen.biome.BirchForestBiome;
import net.minecraft.data.worldgen.biome.BirchForestHillsBiome;
import net.minecraft.data.worldgen.biome.ColdOceanBiome;
import net.minecraft.data.worldgen.biome.CrimsonForestBiome;
import net.minecraft.data.worldgen.biome.DarkForestBiome;
import net.minecraft.data.worldgen.biome.DarkForestHillsBiome;
import net.minecraft.data.worldgen.biome.DeepColdOceanBiome;
import net.minecraft.data.worldgen.biome.DeepFrozenOceanBiome;
import net.minecraft.data.worldgen.biome.DeepLukeWarmOceanBiome;
import net.minecraft.data.worldgen.biome.DeepOceanBiome;
import net.minecraft.data.worldgen.biome.DeepWarmOceanBiome;
import net.minecraft.data.worldgen.biome.DesertBiome;
import net.minecraft.data.worldgen.biome.DesertHillsBiome;
import net.minecraft.data.worldgen.biome.DesertLakesBiome;
import net.minecraft.data.worldgen.biome.EndBarrensBiome;
import net.minecraft.data.worldgen.biome.EndHighlandsBiome;
import net.minecraft.data.worldgen.biome.EndMidlandsBiome;
import net.minecraft.data.worldgen.biome.ErodedBadlandsBiome;
import net.minecraft.data.worldgen.biome.ForestBiome;
import net.minecraft.data.worldgen.biome.ForestFlowerBiome;
import net.minecraft.data.worldgen.biome.FrozenOceanBiome;
import net.minecraft.data.worldgen.biome.FrozenRiverBiome;
import net.minecraft.data.worldgen.biome.GiantSpruceTaigaBiome;
import net.minecraft.data.worldgen.biome.GiantSpruceTaigaHillsMutatedBiome;
import net.minecraft.data.worldgen.biome.GiantTreeTaigaBiome;
import net.minecraft.data.worldgen.biome.GiantTreeTaigaHillsBiome;
import net.minecraft.data.worldgen.biome.GravellyMountainsBiome;
import net.minecraft.data.worldgen.biome.IceSpikesBiome;
import net.minecraft.data.worldgen.biome.JungleBiome;
import net.minecraft.data.worldgen.biome.JungleEdgeBiome;
import net.minecraft.data.worldgen.biome.JungleHillsBiome;
import net.minecraft.data.worldgen.biome.LukeWarmOceanBiome;
import net.minecraft.data.worldgen.biome.ModifiedBadlandsPlateauBiome;
import net.minecraft.data.worldgen.biome.ModifiedGravellyMountainsBiome;
import net.minecraft.data.worldgen.biome.ModifiedJungleBiome;
import net.minecraft.data.worldgen.biome.ModifiedJungleEdgeBiome;
import net.minecraft.data.worldgen.biome.ModifiedWoodedBadlandsPlateauBiome;
import net.minecraft.data.worldgen.biome.MountainBiome;
import net.minecraft.data.worldgen.biome.MountainEdgeBiome;
import net.minecraft.data.worldgen.biome.MushroomFieldsBiome;
import net.minecraft.data.worldgen.biome.MushroomFieldsShoreBiome;
import net.minecraft.data.worldgen.biome.NetherWastesBiome;
import net.minecraft.data.worldgen.biome.OceanBiome;
import net.minecraft.data.worldgen.biome.PlainsBiome;
import net.minecraft.data.worldgen.biome.RiverBiome;
import net.minecraft.data.worldgen.biome.SavannaBiome;
import net.minecraft.data.worldgen.biome.SavannaPlateauBiome;
import net.minecraft.data.worldgen.biome.ShatteredSavannaBiome;
import net.minecraft.data.worldgen.biome.ShatteredSavannaPlateauBiome;
import net.minecraft.data.worldgen.biome.SmallEndIslandsBiome;
import net.minecraft.data.worldgen.biome.SnowyBeachBiome;
import net.minecraft.data.worldgen.biome.SnowyMountainsBiome;
import net.minecraft.data.worldgen.biome.SnowyTaigaBiome;
import net.minecraft.data.worldgen.biome.SnowyTaigaHillsBiome;
import net.minecraft.data.worldgen.biome.SnowyTaigaMountainsBiome;
import net.minecraft.data.worldgen.biome.SnowyTundraBiome;
import net.minecraft.data.worldgen.biome.SoulSandValleyBiome;
import net.minecraft.data.worldgen.biome.StoneShoreBiome;
import net.minecraft.data.worldgen.biome.SunflowerPlainsBiome;
import net.minecraft.data.worldgen.biome.SwampBiome;
import net.minecraft.data.worldgen.biome.SwampHillsBiome;
import net.minecraft.data.worldgen.biome.TaigaBiome;
import net.minecraft.data.worldgen.biome.TaigaHillsBiome;
import net.minecraft.data.worldgen.biome.TaigaMountainsBiome;
import net.minecraft.data.worldgen.biome.TallBirchForestBiome;
import net.minecraft.data.worldgen.biome.TallBirchHillsBiome;
import net.minecraft.data.worldgen.biome.TheEndBiome;
import net.minecraft.data.worldgen.biome.TheVoidBiome;
import net.minecraft.data.worldgen.biome.WarmOceanBiome;
import net.minecraft.data.worldgen.biome.WarpedForestBiome;
import net.minecraft.data.worldgen.biome.WoodedBadlandsBiome;
import net.minecraft.data.worldgen.biome.WoodedHillsBiome;
import net.minecraft.data.worldgen.biome.WoodedMountainBiome;
import net.minecraft.resources.ResourceLocation;

public abstract class Biomes {
	public static final IdMapper<Biome> MUTATED_BIOMES = new IdMapper<>();
	public static final Biome OCEAN = register(0, "ocean", new OceanBiome());
	public static final Biome DEFAULT = OCEAN;
	public static final Biome PLAINS = register(1, "plains", new PlainsBiome());
	public static final Biome DESERT = register(2, "desert", new DesertBiome());
	public static final Biome MOUNTAINS = register(3, "mountains", new MountainBiome());
	public static final Biome FOREST = register(4, "forest", new ForestBiome());
	public static final Biome TAIGA = register(5, "taiga", new TaigaBiome());
	public static final Biome SWAMP = register(6, "swamp", new SwampBiome());
	public static final Biome RIVER = register(7, "river", new RiverBiome());
	public static final Biome NETHER_WASTES = register(8, "nether_wastes", new NetherWastesBiome());
	public static final Biome THE_END = register(9, "the_end", new TheEndBiome());
	public static final Biome FROZEN_OCEAN = register(10, "frozen_ocean", new FrozenOceanBiome());
	public static final Biome FROZEN_RIVER = register(11, "frozen_river", new FrozenRiverBiome());
	public static final Biome SNOWY_TUNDRA = register(12, "snowy_tundra", new SnowyTundraBiome());
	public static final Biome SNOWY_MOUNTAINS = register(13, "snowy_mountains", new SnowyMountainsBiome());
	public static final Biome MUSHROOM_FIELDS = register(14, "mushroom_fields", new MushroomFieldsBiome());
	public static final Biome MUSHROOM_FIELD_SHORE = register(15, "mushroom_field_shore", new MushroomFieldsShoreBiome());
	public static final Biome BEACH = register(16, "beach", new BeachBiome());
	public static final Biome DESERT_HILLS = register(17, "desert_hills", new DesertHillsBiome());
	public static final Biome WOODED_HILLS = register(18, "wooded_hills", new WoodedHillsBiome());
	public static final Biome TAIGA_HILLS = register(19, "taiga_hills", new TaigaHillsBiome());
	public static final Biome MOUNTAIN_EDGE = register(20, "mountain_edge", new MountainEdgeBiome());
	public static final Biome JUNGLE = register(21, "jungle", new JungleBiome());
	public static final Biome JUNGLE_HILLS = register(22, "jungle_hills", new JungleHillsBiome());
	public static final Biome JUNGLE_EDGE = register(23, "jungle_edge", new JungleEdgeBiome());
	public static final Biome DEEP_OCEAN = register(24, "deep_ocean", new DeepOceanBiome());
	public static final Biome STONE_SHORE = register(25, "stone_shore", new StoneShoreBiome());
	public static final Biome SNOWY_BEACH = register(26, "snowy_beach", new SnowyBeachBiome());
	public static final Biome BIRCH_FOREST = register(27, "birch_forest", new BirchForestBiome());
	public static final Biome BIRCH_FOREST_HILLS = register(28, "birch_forest_hills", new BirchForestHillsBiome());
	public static final Biome DARK_FOREST = register(29, "dark_forest", new DarkForestBiome());
	public static final Biome SNOWY_TAIGA = register(30, "snowy_taiga", new SnowyTaigaBiome());
	public static final Biome SNOWY_TAIGA_HILLS = register(31, "snowy_taiga_hills", new SnowyTaigaHillsBiome());
	public static final Biome GIANT_TREE_TAIGA = register(32, "giant_tree_taiga", new GiantTreeTaigaBiome());
	public static final Biome GIANT_TREE_TAIGA_HILLS = register(33, "giant_tree_taiga_hills", new GiantTreeTaigaHillsBiome());
	public static final Biome WOODED_MOUNTAINS = register(34, "wooded_mountains", new WoodedMountainBiome());
	public static final Biome SAVANNA = register(35, "savanna", new SavannaBiome());
	public static final Biome SAVANNA_PLATEAU = register(36, "savanna_plateau", new SavannaPlateauBiome());
	public static final Biome BADLANDS = register(37, "badlands", new BadlandsBiome());
	public static final Biome WOODED_BADLANDS_PLATEAU = register(38, "wooded_badlands_plateau", new WoodedBadlandsBiome());
	public static final Biome BADLANDS_PLATEAU = register(39, "badlands_plateau", new BadlandsPlateauBiome());
	public static final Biome SMALL_END_ISLANDS = register(40, "small_end_islands", new SmallEndIslandsBiome());
	public static final Biome END_MIDLANDS = register(41, "end_midlands", new EndMidlandsBiome());
	public static final Biome END_HIGHLANDS = register(42, "end_highlands", new EndHighlandsBiome());
	public static final Biome END_BARRENS = register(43, "end_barrens", new EndBarrensBiome());
	public static final Biome WARM_OCEAN = register(44, "warm_ocean", new WarmOceanBiome());
	public static final Biome LUKEWARM_OCEAN = register(45, "lukewarm_ocean", new LukeWarmOceanBiome());
	public static final Biome COLD_OCEAN = register(46, "cold_ocean", new ColdOceanBiome());
	public static final Biome DEEP_WARM_OCEAN = register(47, "deep_warm_ocean", new DeepWarmOceanBiome());
	public static final Biome DEEP_LUKEWARM_OCEAN = register(48, "deep_lukewarm_ocean", new DeepLukeWarmOceanBiome());
	public static final Biome DEEP_COLD_OCEAN = register(49, "deep_cold_ocean", new DeepColdOceanBiome());
	public static final Biome DEEP_FROZEN_OCEAN = register(50, "deep_frozen_ocean", new DeepFrozenOceanBiome());
	public static final Biome THE_VOID = register(127, "the_void", new TheVoidBiome());
	public static final Biome SUNFLOWER_PLAINS = register(129, "sunflower_plains", new SunflowerPlainsBiome());
	public static final Biome DESERT_LAKES = register(130, "desert_lakes", new DesertLakesBiome());
	public static final Biome GRAVELLY_MOUNTAINS = register(131, "gravelly_mountains", new GravellyMountainsBiome());
	public static final Biome FLOWER_FOREST = register(132, "flower_forest", new ForestFlowerBiome());
	public static final Biome TAIGA_MOUNTAINS = register(133, "taiga_mountains", new TaigaMountainsBiome());
	public static final Biome SWAMP_HILLS = register(134, "swamp_hills", new SwampHillsBiome());
	public static final Biome ICE_SPIKES = register(140, "ice_spikes", new IceSpikesBiome());
	public static final Biome MODIFIED_JUNGLE = register(149, "modified_jungle", new ModifiedJungleBiome());
	public static final Biome MODIFIED_JUNGLE_EDGE = register(151, "modified_jungle_edge", new ModifiedJungleEdgeBiome());
	public static final Biome TALL_BIRCH_FOREST = register(155, "tall_birch_forest", new TallBirchForestBiome());
	public static final Biome TALL_BIRCH_HILLS = register(156, "tall_birch_hills", new TallBirchHillsBiome());
	public static final Biome DARK_FOREST_HILLS = register(157, "dark_forest_hills", new DarkForestHillsBiome());
	public static final Biome SNOWY_TAIGA_MOUNTAINS = register(158, "snowy_taiga_mountains", new SnowyTaigaMountainsBiome());
	public static final Biome GIANT_SPRUCE_TAIGA = register(160, "giant_spruce_taiga", new GiantSpruceTaigaBiome());
	public static final Biome GIANT_SPRUCE_TAIGA_HILLS = register(161, "giant_spruce_taiga_hills", new GiantSpruceTaigaHillsMutatedBiome());
	public static final Biome MODIFIED_GRAVELLY_MOUNTAINS = register(162, "modified_gravelly_mountains", new ModifiedGravellyMountainsBiome());
	public static final Biome SHATTERED_SAVANNA = register(163, "shattered_savanna", new ShatteredSavannaBiome());
	public static final Biome SHATTERED_SAVANNA_PLATEAU = register(164, "shattered_savanna_plateau", new ShatteredSavannaPlateauBiome());
	public static final Biome ERODED_BADLANDS = register(165, "eroded_badlands", new ErodedBadlandsBiome());
	public static final Biome MODIFIED_WOODED_BADLANDS_PLATEAU = register(166, "modified_wooded_badlands_plateau", new ModifiedWoodedBadlandsPlateauBiome());
	public static final Biome MODIFIED_BADLANDS_PLATEAU = register(167, "modified_badlands_plateau", new ModifiedBadlandsPlateauBiome());
	public static final Biome BAMBOO_JUNGLE = register(168, "bamboo_jungle", new BambooJungleBiome());
	public static final Biome BAMBOO_JUNGLE_HILLS = register(169, "bamboo_jungle_hills", new BambooJungleHillsBiome());
	public static final Biome SOUL_SAND_VALLEY = register(170, "soul_sand_valley", new SoulSandValleyBiome());
	public static final Biome CRIMSON_FOREST = register(171, "crimson_forest", new CrimsonForestBiome());
	public static final Biome WARPED_FOREST = register(172, "warped_forest", new WarpedForestBiome());
	public static final Biome BASALT_DELTAS = register(173, "basalt_deltas", new BasaltDeltasBiome());

	private static Biome register(int i, String string, Biome biome) {
		BuiltinRegistries.registerMapping(BuiltinRegistries.BIOME, i, string, biome);
		if (biome.isMutated()) {
			MUTATED_BIOMES.addMapping(biome, BuiltinRegistries.BIOME.getId(BuiltinRegistries.BIOME.get(new ResourceLocation(biome.parent))));
		}

		return biome;
	}

	@Nullable
	public static Biome getMutatedVariant(Biome biome) {
		return MUTATED_BIOMES.byId(BuiltinRegistries.BIOME.getId(biome));
	}

	static {
		Collections.addAll(
			Biome.EXPLORABLE_BIOMES,
			new Biome[]{
				OCEAN,
				PLAINS,
				DESERT,
				MOUNTAINS,
				FOREST,
				TAIGA,
				SWAMP,
				RIVER,
				FROZEN_RIVER,
				SNOWY_TUNDRA,
				SNOWY_MOUNTAINS,
				MUSHROOM_FIELDS,
				MUSHROOM_FIELD_SHORE,
				BEACH,
				DESERT_HILLS,
				WOODED_HILLS,
				TAIGA_HILLS,
				JUNGLE,
				JUNGLE_HILLS,
				JUNGLE_EDGE,
				DEEP_OCEAN,
				STONE_SHORE,
				SNOWY_BEACH,
				BIRCH_FOREST,
				BIRCH_FOREST_HILLS,
				DARK_FOREST,
				SNOWY_TAIGA,
				SNOWY_TAIGA_HILLS,
				GIANT_TREE_TAIGA,
				GIANT_TREE_TAIGA_HILLS,
				WOODED_MOUNTAINS,
				SAVANNA,
				SAVANNA_PLATEAU,
				BADLANDS,
				WOODED_BADLANDS_PLATEAU,
				BADLANDS_PLATEAU
			}
		);
	}
}
