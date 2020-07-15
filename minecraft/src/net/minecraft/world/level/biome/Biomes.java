package net.minecraft.world.level.biome;

import java.util.Collections;
import javax.annotation.Nullable;
import net.minecraft.core.IdMapper;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.SurfaceBuilders;
import net.minecraft.data.worldgen.biome.VanillaBiomes;
import net.minecraft.resources.ResourceLocation;

public abstract class Biomes {
	public static final IdMapper<Biome> MUTATED_BIOMES = new IdMapper<>();
	public static final Biome OCEAN = register(0, "ocean", VanillaBiomes.oceanBiome(false));
	public static final Biome DEFAULT = OCEAN;
	public static final Biome PLAINS = register(1, "plains", VanillaBiomes.plainsBiome(null, false));
	public static final Biome DESERT = register(2, "desert", VanillaBiomes.desertBiome(null, 0.125F, 0.05F, true, true, true));
	public static final Biome MOUNTAINS = register(3, "mountains", VanillaBiomes.mountainBiome(1.0F, 0.5F, SurfaceBuilders.MOUNTAIN, false, null));
	public static final Biome FOREST = register(4, "forest", VanillaBiomes.forestBiome(0.1F, 0.2F));
	public static final Biome TAIGA = register(5, "taiga", VanillaBiomes.taigaBiome(null, 0.2F, 0.2F, false, false, true, false));
	public static final Biome SWAMP = register(6, "swamp", VanillaBiomes.swampBiome(null, -0.2F, 0.1F, false));
	public static final Biome RIVER = register(7, "river", VanillaBiomes.riverBiome(-0.5F, 0.0F, 0.5F, 4159204, false));
	public static final Biome NETHER_WASTES = register(8, "nether_wastes", VanillaBiomes.netherWastesBiome());
	public static final Biome THE_END = register(9, "the_end", VanillaBiomes.theEndBiome());
	public static final Biome FROZEN_OCEAN = register(10, "frozen_ocean", VanillaBiomes.frozenOceanBiome(false));
	public static final Biome FROZEN_RIVER = register(11, "frozen_river", VanillaBiomes.riverBiome(-0.5F, 0.0F, 0.0F, 3750089, true));
	public static final Biome SNOWY_TUNDRA = register(12, "snowy_tundra", VanillaBiomes.tundraBiome(null, 0.125F, 0.05F, false, false));
	public static final Biome SNOWY_MOUNTAINS = register(13, "snowy_mountains", VanillaBiomes.tundraBiome(null, 0.45F, 0.3F, false, true));
	public static final Biome MUSHROOM_FIELDS = register(14, "mushroom_fields", VanillaBiomes.mushroomFieldsBiome(0.2F, 0.3F));
	public static final Biome MUSHROOM_FIELD_SHORE = register(15, "mushroom_field_shore", VanillaBiomes.mushroomFieldsBiome(0.0F, 0.025F));
	public static final Biome BEACH = register(16, "beach", VanillaBiomes.beachBiome(0.0F, 0.025F, 0.8F, 0.4F, 4159204, false, false));
	public static final Biome DESERT_HILLS = register(17, "desert_hills", VanillaBiomes.desertBiome(null, 0.45F, 0.3F, false, true, false));
	public static final Biome WOODED_HILLS = register(18, "wooded_hills", VanillaBiomes.forestBiome(0.45F, 0.3F));
	public static final Biome TAIGA_HILLS = register(19, "taiga_hills", VanillaBiomes.taigaBiome(null, 0.45F, 0.3F, false, false, false, false));
	public static final Biome MOUNTAIN_EDGE = register(20, "mountain_edge", VanillaBiomes.mountainBiome(0.8F, 0.3F, SurfaceBuilders.GRASS, true, null));
	public static final Biome JUNGLE = register(21, "jungle", VanillaBiomes.jungleBiome());
	public static final Biome JUNGLE_HILLS = register(22, "jungle_hills", VanillaBiomes.jungleHillsBiome());
	public static final Biome JUNGLE_EDGE = register(23, "jungle_edge", VanillaBiomes.jungleEdgeBiome());
	public static final Biome DEEP_OCEAN = register(24, "deep_ocean", VanillaBiomes.oceanBiome(true));
	public static final Biome STONE_SHORE = register(25, "stone_shore", VanillaBiomes.beachBiome(0.1F, 0.8F, 0.2F, 0.3F, 4159204, false, true));
	public static final Biome SNOWY_BEACH = register(26, "snowy_beach", VanillaBiomes.beachBiome(0.0F, 0.025F, 0.05F, 0.3F, 4020182, true, false));
	public static final Biome BIRCH_FOREST = register(27, "birch_forest", VanillaBiomes.birchForestBiome(0.1F, 0.2F, null, false));
	public static final Biome BIRCH_FOREST_HILLS = register(28, "birch_forest_hills", VanillaBiomes.birchForestBiome(0.45F, 0.3F, null, false));
	public static final Biome DARK_FOREST = register(29, "dark_forest", VanillaBiomes.darkForestBiome(null, 0.1F, 0.2F, false));
	public static final Biome SNOWY_TAIGA = register(30, "snowy_taiga", VanillaBiomes.taigaBiome(null, 0.2F, 0.2F, true, false, false, true));
	public static final Biome SNOWY_TAIGA_HILLS = register(31, "snowy_taiga_hills", VanillaBiomes.taigaBiome(null, 0.45F, 0.3F, true, false, false, false));
	public static final Biome GIANT_TREE_TAIGA = register(32, "giant_tree_taiga", VanillaBiomes.giantTreeTaiga(0.2F, 0.2F, 0.3F, false, null));
	public static final Biome GIANT_TREE_TAIGA_HILLS = register(33, "giant_tree_taiga_hills", VanillaBiomes.giantTreeTaiga(0.45F, 0.3F, 0.3F, false, null));
	public static final Biome WOODED_MOUNTAINS = register(34, "wooded_mountains", VanillaBiomes.mountainBiome(1.0F, 0.5F, SurfaceBuilders.GRASS, true, null));
	public static final Biome SAVANNA = register(35, "savanna", VanillaBiomes.savannaBiome(null, 0.125F, 0.05F, 1.2F, false, false));
	public static final Biome SAVANNA_PLATEAU = register(36, "savanna_plateau", VanillaBiomes.savanaPlateauBiome());
	public static final Biome BADLANDS = register(37, "badlands", VanillaBiomes.badlandsBiome(null, 0.1F, 0.2F, false));
	public static final Biome WOODED_BADLANDS_PLATEAU = register(38, "wooded_badlands_plateau", VanillaBiomes.woodedBadlandsPlateauBiome(null, 1.5F, 0.025F));
	public static final Biome BADLANDS_PLATEAU = register(39, "badlands_plateau", VanillaBiomes.badlandsBiome(null, 1.5F, 0.025F, true));
	public static final Biome SMALL_END_ISLANDS = register(40, "small_end_islands", VanillaBiomes.smallEndIslandsBiome());
	public static final Biome END_MIDLANDS = register(41, "end_midlands", VanillaBiomes.endMidlandsBiome());
	public static final Biome END_HIGHLANDS = register(42, "end_highlands", VanillaBiomes.endHighlandsBiome());
	public static final Biome END_BARRENS = register(43, "end_barrens", VanillaBiomes.endBarrensBiome());
	public static final Biome WARM_OCEAN = register(44, "warm_ocean", VanillaBiomes.warmOceanBiome());
	public static final Biome LUKEWARM_OCEAN = register(45, "lukewarm_ocean", VanillaBiomes.lukeWarmOceanBiome(false));
	public static final Biome COLD_OCEAN = register(46, "cold_ocean", VanillaBiomes.coldOceanBiome(false));
	public static final Biome DEEP_WARM_OCEAN = register(47, "deep_warm_ocean", VanillaBiomes.deepWarmOceanBiome());
	public static final Biome DEEP_LUKEWARM_OCEAN = register(48, "deep_lukewarm_ocean", VanillaBiomes.lukeWarmOceanBiome(true));
	public static final Biome DEEP_COLD_OCEAN = register(49, "deep_cold_ocean", VanillaBiomes.coldOceanBiome(true));
	public static final Biome DEEP_FROZEN_OCEAN = register(50, "deep_frozen_ocean", VanillaBiomes.frozenOceanBiome(true));
	public static final Biome THE_VOID = register(127, "the_void", VanillaBiomes.theVoidBiome());
	public static final Biome SUNFLOWER_PLAINS = register(129, "sunflower_plains", VanillaBiomes.plainsBiome("plains", true));
	public static final Biome DESERT_LAKES = register(130, "desert_lakes", VanillaBiomes.desertBiome("desert", 0.225F, 0.25F, false, false, false));
	public static final Biome GRAVELLY_MOUNTAINS = register(
		131, "gravelly_mountains", VanillaBiomes.mountainBiome(1.0F, 0.5F, SurfaceBuilders.GRAVELLY_MOUNTAIN, false, "mountains")
	);
	public static final Biome FLOWER_FOREST = register(132, "flower_forest", VanillaBiomes.flowerForestBiome());
	public static final Biome TAIGA_MOUNTAINS = register(133, "taiga_mountains", VanillaBiomes.taigaBiome("taiga", 0.3F, 0.4F, false, true, false, false));
	public static final Biome SWAMP_HILLS = register(134, "swamp_hills", VanillaBiomes.swampBiome("swamp", -0.1F, 0.3F, true));
	public static final Biome ICE_SPIKES = register(140, "ice_spikes", VanillaBiomes.tundraBiome("snowy_tundra", 0.425F, 0.45000002F, true, false));
	public static final Biome MODIFIED_JUNGLE = register(149, "modified_jungle", VanillaBiomes.modifiedJungleBiome());
	public static final Biome MODIFIED_JUNGLE_EDGE = register(151, "modified_jungle_edge", VanillaBiomes.modifiedJungleEdgeBiome());
	public static final Biome TALL_BIRCH_FOREST = register(155, "tall_birch_forest", VanillaBiomes.birchForestBiome(0.2F, 0.4F, "birch_forest", true));
	public static final Biome TALL_BIRCH_HILLS = register(156, "tall_birch_hills", VanillaBiomes.birchForestBiome(0.55F, 0.5F, "birch_forest_hills", true));
	public static final Biome DARK_FOREST_HILLS = register(157, "dark_forest_hills", VanillaBiomes.darkForestBiome("dark_forest", 0.2F, 0.4F, true));
	public static final Biome SNOWY_TAIGA_MOUNTAINS = register(
		158, "snowy_taiga_mountains", VanillaBiomes.taigaBiome("snowy_taiga", 0.3F, 0.4F, true, true, false, false)
	);
	public static final Biome GIANT_SPRUCE_TAIGA = register(160, "giant_spruce_taiga", VanillaBiomes.giantTreeTaiga(0.2F, 0.2F, 0.25F, true, "giant_tree_taiga"));
	public static final Biome GIANT_SPRUCE_TAIGA_HILLS = register(
		161, "giant_spruce_taiga_hills", VanillaBiomes.giantTreeTaiga(0.2F, 0.2F, 0.25F, true, "giant_tree_taiga_hills")
	);
	public static final Biome MODIFIED_GRAVELLY_MOUNTAINS = register(
		162, "modified_gravelly_mountains", VanillaBiomes.mountainBiome(1.0F, 0.5F, SurfaceBuilders.GRAVELLY_MOUNTAIN, false, "wooded_mountains")
	);
	public static final Biome SHATTERED_SAVANNA = register(163, "shattered_savanna", VanillaBiomes.savannaBiome("savanna", 0.3625F, 1.225F, 1.1F, true, true));
	public static final Biome SHATTERED_SAVANNA_PLATEAU = register(
		164, "shattered_savanna_plateau", VanillaBiomes.savannaBiome("savanna_plateau", 1.05F, 1.2125001F, 1.0F, true, true)
	);
	public static final Biome ERODED_BADLANDS = register(165, "eroded_badlands", VanillaBiomes.erodedBadlandsBiome());
	public static final Biome MODIFIED_WOODED_BADLANDS_PLATEAU = register(
		166, "modified_wooded_badlands_plateau", VanillaBiomes.woodedBadlandsPlateauBiome("wooded_badlands_plateau", 0.45F, 0.3F)
	);
	public static final Biome MODIFIED_BADLANDS_PLATEAU = register(
		167, "modified_badlands_plateau", VanillaBiomes.badlandsBiome("badlands_plateau", 0.45F, 0.3F, true)
	);
	public static final Biome BAMBOO_JUNGLE = register(168, "bamboo_jungle", VanillaBiomes.bambooJungleBiome());
	public static final Biome BAMBOO_JUNGLE_HILLS = register(169, "bamboo_jungle_hills", VanillaBiomes.bambooJungleHillsBiome());
	public static final Biome SOUL_SAND_VALLEY = register(170, "soul_sand_valley", VanillaBiomes.soulSandValleyBiome());
	public static final Biome CRIMSON_FOREST = register(171, "crimson_forest", VanillaBiomes.crimsonForestBiome());
	public static final Biome WARPED_FOREST = register(172, "warped_forest", VanillaBiomes.warpedForestBiome());
	public static final Biome BASALT_DELTAS = register(173, "basalt_deltas", VanillaBiomes.basaltDeltasBiome());

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
