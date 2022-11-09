package net.minecraft.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public class BiomeTags {
	public static final TagKey<Biome> IS_DEEP_OCEAN = create("is_deep_ocean");
	public static final TagKey<Biome> IS_OCEAN = create("is_ocean");
	public static final TagKey<Biome> IS_BEACH = create("is_beach");
	public static final TagKey<Biome> IS_RIVER = create("is_river");
	public static final TagKey<Biome> IS_MOUNTAIN = create("is_mountain");
	public static final TagKey<Biome> IS_BADLANDS = create("is_badlands");
	public static final TagKey<Biome> IS_HILL = create("is_hill");
	public static final TagKey<Biome> IS_TAIGA = create("is_taiga");
	public static final TagKey<Biome> IS_JUNGLE = create("is_jungle");
	public static final TagKey<Biome> IS_FOREST = create("is_forest");
	public static final TagKey<Biome> IS_SAVANNA = create("is_savanna");
	public static final TagKey<Biome> IS_OVERWORLD = create("is_overworld");
	public static final TagKey<Biome> IS_NETHER = create("is_nether");
	public static final TagKey<Biome> IS_END = create("is_end");
	public static final TagKey<Biome> STRONGHOLD_BIASED_TO = create("stronghold_biased_to");
	public static final TagKey<Biome> HAS_BURIED_TREASURE = create("has_structure/buried_treasure");
	public static final TagKey<Biome> HAS_DESERT_PYRAMID = create("has_structure/desert_pyramid");
	public static final TagKey<Biome> HAS_IGLOO = create("has_structure/igloo");
	public static final TagKey<Biome> HAS_JUNGLE_TEMPLE = create("has_structure/jungle_temple");
	public static final TagKey<Biome> HAS_MINESHAFT = create("has_structure/mineshaft");
	public static final TagKey<Biome> HAS_MINESHAFT_MESA = create("has_structure/mineshaft_mesa");
	public static final TagKey<Biome> HAS_OCEAN_MONUMENT = create("has_structure/ocean_monument");
	public static final TagKey<Biome> HAS_OCEAN_RUIN_COLD = create("has_structure/ocean_ruin_cold");
	public static final TagKey<Biome> HAS_OCEAN_RUIN_WARM = create("has_structure/ocean_ruin_warm");
	public static final TagKey<Biome> HAS_PILLAGER_OUTPOST = create("has_structure/pillager_outpost");
	public static final TagKey<Biome> HAS_RUINED_PORTAL_DESERT = create("has_structure/ruined_portal_desert");
	public static final TagKey<Biome> HAS_RUINED_PORTAL_JUNGLE = create("has_structure/ruined_portal_jungle");
	public static final TagKey<Biome> HAS_RUINED_PORTAL_OCEAN = create("has_structure/ruined_portal_ocean");
	public static final TagKey<Biome> HAS_RUINED_PORTAL_SWAMP = create("has_structure/ruined_portal_swamp");
	public static final TagKey<Biome> HAS_RUINED_PORTAL_MOUNTAIN = create("has_structure/ruined_portal_mountain");
	public static final TagKey<Biome> HAS_RUINED_PORTAL_STANDARD = create("has_structure/ruined_portal_standard");
	public static final TagKey<Biome> HAS_SHIPWRECK_BEACHED = create("has_structure/shipwreck_beached");
	public static final TagKey<Biome> HAS_SHIPWRECK = create("has_structure/shipwreck");
	public static final TagKey<Biome> HAS_STRONGHOLD = create("has_structure/stronghold");
	public static final TagKey<Biome> HAS_SWAMP_HUT = create("has_structure/swamp_hut");
	public static final TagKey<Biome> HAS_VILLAGE_DESERT = create("has_structure/village_desert");
	public static final TagKey<Biome> HAS_VILLAGE_PLAINS = create("has_structure/village_plains");
	public static final TagKey<Biome> HAS_VILLAGE_SAVANNA = create("has_structure/village_savanna");
	public static final TagKey<Biome> HAS_VILLAGE_SNOWY = create("has_structure/village_snowy");
	public static final TagKey<Biome> HAS_VILLAGE_TAIGA = create("has_structure/village_taiga");
	public static final TagKey<Biome> HAS_WOODLAND_MANSION = create("has_structure/woodland_mansion");
	public static final TagKey<Biome> HAS_NETHER_FORTRESS = create("has_structure/nether_fortress");
	public static final TagKey<Biome> HAS_NETHER_FOSSIL = create("has_structure/nether_fossil");
	public static final TagKey<Biome> HAS_BASTION_REMNANT = create("has_structure/bastion_remnant");
	public static final TagKey<Biome> HAS_ANCIENT_CITY = create("has_structure/ancient_city");
	public static final TagKey<Biome> HAS_RUINED_PORTAL_NETHER = create("has_structure/ruined_portal_nether");
	public static final TagKey<Biome> HAS_END_CITY = create("has_structure/end_city");
	public static final TagKey<Biome> REQUIRED_OCEAN_MONUMENT_SURROUNDING = create("required_ocean_monument_surrounding");
	public static final TagKey<Biome> MINESHAFT_BLOCKING = create("mineshaft_blocking");
	public static final TagKey<Biome> PLAYS_UNDERWATER_MUSIC = create("plays_underwater_music");
	public static final TagKey<Biome> HAS_CLOSER_WATER_FOG = create("has_closer_water_fog");
	public static final TagKey<Biome> WATER_ON_MAP_OUTLINES = create("water_on_map_outlines");
	public static final TagKey<Biome> PRODUCES_CORALS_FROM_BONEMEAL = create("produces_corals_from_bonemeal");
	public static final TagKey<Biome> WITHOUT_ZOMBIE_SIEGES = create("without_zombie_sieges");
	public static final TagKey<Biome> WITHOUT_PATROL_SPAWNS = create("without_patrol_spawns");
	public static final TagKey<Biome> WITHOUT_WANDERING_TRADER_SPAWNS = create("without_wandering_trader_spawns");
	public static final TagKey<Biome> SPAWNS_COLD_VARIANT_FROGS = create("spawns_cold_variant_frogs");
	public static final TagKey<Biome> SPAWNS_WARM_VARIANT_FROGS = create("spawns_warm_variant_frogs");
	public static final TagKey<Biome> ONLY_ALLOWS_SNOW_AND_GOLD_RABBITS = create("only_allows_snow_and_gold_rabbits");
	public static final TagKey<Biome> REDUCED_WATER_AMBIENT_SPAWNS = create("reduce_water_ambient_spawns");
	public static final TagKey<Biome> ALLOWS_TROPICAL_FISH_SPAWNS_AT_ANY_HEIGHT = create("allows_tropical_fish_spawns_at_any_height");
	public static final TagKey<Biome> POLAR_BEARS_SPAWN_ON_ALTERNATE_BLOCKS = create("polar_bears_spawn_on_alternate_blocks");
	public static final TagKey<Biome> MORE_FREQUENT_DROWNED_SPAWNS = create("more_frequent_drowned_spawns");
	public static final TagKey<Biome> ALLOWS_SURFACE_SLIME_SPAWNS = create("allows_surface_slime_spawns");

	private BiomeTags() {
	}

	private static TagKey<Biome> create(String string) {
		return TagKey.create(Registries.BIOME, new ResourceLocation(string));
	}
}
