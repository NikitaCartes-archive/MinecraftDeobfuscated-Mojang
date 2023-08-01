package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL.TypeReference;

public class References {
	public static final TypeReference LEVEL = () -> "level";
	public static final TypeReference PLAYER = () -> "player";
	public static final TypeReference CHUNK = () -> "chunk";
	public static final TypeReference HOTBAR = () -> "hotbar";
	public static final TypeReference OPTIONS = () -> "options";
	public static final TypeReference STRUCTURE = () -> "structure";
	public static final TypeReference STATS = () -> "stats";
	public static final TypeReference SAVED_DATA_COMMAND_STORAGE = () -> "saved_data/command_storage";
	public static final TypeReference SAVED_DATA_FORCED_CHUNKS = () -> "saved_data/chunks";
	public static final TypeReference SAVED_DATA_MAP_DATA = () -> "saved_data/map_data";
	public static final TypeReference SAVED_DATA_MAP_INDEX = () -> "saved_data/idcounts";
	public static final TypeReference SAVED_DATA_RAIDS = () -> "saved_data/raids";
	public static final TypeReference SAVED_DATA_RANDOM_SEQUENCES = () -> "saved_data/random_sequences";
	public static final TypeReference SAVED_DATA_STRUCTURE_FEATURE_INDICES = () -> "saved_data/structure_feature_indices";
	public static final TypeReference SAVED_DATA_SCOREBOARD = () -> "saved_data/scoreboard";
	public static final TypeReference ADVANCEMENTS = () -> "advancements";
	public static final TypeReference POI_CHUNK = () -> "poi_chunk";
	public static final TypeReference ENTITY_CHUNK = () -> "entity_chunk";
	public static final TypeReference BLOCK_ENTITY = () -> "block_entity";
	public static final TypeReference ITEM_STACK = () -> "item_stack";
	public static final TypeReference BLOCK_STATE = () -> "block_state";
	public static final TypeReference ENTITY_NAME = () -> "entity_name";
	public static final TypeReference ENTITY_TREE = () -> "entity_tree";
	public static final TypeReference ENTITY = () -> "entity";
	public static final TypeReference BLOCK_NAME = () -> "block_name";
	public static final TypeReference ITEM_NAME = () -> "item_name";
	public static final TypeReference GAME_EVENT_NAME = () -> "game_event_name";
	public static final TypeReference UNTAGGED_SPAWNER = () -> "untagged_spawner";
	public static final TypeReference STRUCTURE_FEATURE = () -> "structure_feature";
	public static final TypeReference OBJECTIVE = () -> "objective";
	public static final TypeReference TEAM = () -> "team";
	public static final TypeReference RECIPE = () -> "recipe";
	public static final TypeReference BIOME = () -> "biome";
	public static final TypeReference MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST = () -> "multi_noise_biome_source_parameter_list";
	public static final TypeReference WORLD_GEN_SETTINGS = () -> "world_gen_settings";
}
