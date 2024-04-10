package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL.TypeReference;

public class References {
	public static final TypeReference LEVEL = reference("level");
	public static final TypeReference PLAYER = reference("player");
	public static final TypeReference CHUNK = reference("chunk");
	public static final TypeReference HOTBAR = reference("hotbar");
	public static final TypeReference OPTIONS = reference("options");
	public static final TypeReference STRUCTURE = reference("structure");
	public static final TypeReference STATS = reference("stats");
	public static final TypeReference SAVED_DATA_COMMAND_STORAGE = reference("saved_data/command_storage");
	public static final TypeReference SAVED_DATA_FORCED_CHUNKS = reference("saved_data/chunks");
	public static final TypeReference SAVED_DATA_MAP_DATA = reference("saved_data/map_data");
	public static final TypeReference SAVED_DATA_MAP_INDEX = reference("saved_data/idcounts");
	public static final TypeReference SAVED_DATA_RAIDS = reference("saved_data/raids");
	public static final TypeReference SAVED_DATA_RANDOM_SEQUENCES = reference("saved_data/random_sequences");
	public static final TypeReference SAVED_DATA_STRUCTURE_FEATURE_INDICES = reference("saved_data/structure_feature_indices");
	public static final TypeReference SAVED_DATA_SCOREBOARD = reference("saved_data/scoreboard");
	public static final TypeReference ADVANCEMENTS = reference("advancements");
	public static final TypeReference POI_CHUNK = reference("poi_chunk");
	public static final TypeReference ENTITY_CHUNK = reference("entity_chunk");
	public static final TypeReference BLOCK_ENTITY = reference("block_entity");
	public static final TypeReference ITEM_STACK = reference("item_stack");
	public static final TypeReference BLOCK_STATE = reference("block_state");
	public static final TypeReference FLAT_BLOCK_STATE = reference("flat_block_state");
	public static final TypeReference DATA_COMPONENTS = reference("data_components");
	public static final TypeReference VILLAGER_TRADE = reference("villager_trade");
	public static final TypeReference PARTICLE = reference("particle");
	public static final TypeReference ENTITY_NAME = reference("entity_name");
	public static final TypeReference ENTITY_TREE = reference("entity_tree");
	public static final TypeReference ENTITY = reference("entity");
	public static final TypeReference BLOCK_NAME = reference("block_name");
	public static final TypeReference ITEM_NAME = reference("item_name");
	public static final TypeReference GAME_EVENT_NAME = reference("game_event_name");
	public static final TypeReference UNTAGGED_SPAWNER = reference("untagged_spawner");
	public static final TypeReference STRUCTURE_FEATURE = reference("structure_feature");
	public static final TypeReference OBJECTIVE = reference("objective");
	public static final TypeReference TEAM = reference("team");
	public static final TypeReference RECIPE = reference("recipe");
	public static final TypeReference BIOME = reference("biome");
	public static final TypeReference MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST = reference("multi_noise_biome_source_parameter_list");
	public static final TypeReference WORLD_GEN_SETTINGS = reference("world_gen_settings");

	public static TypeReference reference(String string) {
		return new TypeReference() {
			@Override
			public String typeName() {
				return string;
			}

			public String toString() {
				return "@" + string;
			}
		};
	}
}
