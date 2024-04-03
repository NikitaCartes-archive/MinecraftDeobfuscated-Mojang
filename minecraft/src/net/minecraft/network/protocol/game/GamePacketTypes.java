package net.minecraft.network.protocol.game;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceLocation;

public class GamePacketTypes {
	public static final PacketType<ClientboundBundlePacket> CLIENTBOUND_BUNDLE = createClientbound("bundle");
	public static final PacketType<ClientboundBundleDelimiterPacket> CLIENTBOUND_BUNDLE_DELIMITER = createClientbound("bundle_delimiter");
	public static final PacketType<ClientboundAddEntityPacket> CLIENTBOUND_ADD_ENTITY = createClientbound("add_entity");
	public static final PacketType<ClientboundAddExperienceOrbPacket> CLIENTBOUND_ADD_EXPERIENCE_ORB = createClientbound("add_experience_orb");
	public static final PacketType<ClientboundAnimatePacket> CLIENTBOUND_ANIMATE = createClientbound("animate");
	public static final PacketType<ClientboundAwardStatsPacket> CLIENTBOUND_AWARD_STATS = createClientbound("award_stats");
	public static final PacketType<ClientboundBlockChangedAckPacket> CLIENTBOUND_BLOCK_CHANGED_ACK = createClientbound("block_changed_ack");
	public static final PacketType<ClientboundBlockDestructionPacket> CLIENTBOUND_BLOCK_DESTRUCTION = createClientbound("block_destruction");
	public static final PacketType<ClientboundBlockEntityDataPacket> CLIENTBOUND_BLOCK_ENTITY_DATA = createClientbound("block_entity_data");
	public static final PacketType<ClientboundBlockEventPacket> CLIENTBOUND_BLOCK_EVENT = createClientbound("block_event");
	public static final PacketType<ClientboundBlockUpdatePacket> CLIENTBOUND_BLOCK_UPDATE = createClientbound("block_update");
	public static final PacketType<ClientboundBossEventPacket> CLIENTBOUND_BOSS_EVENT = createClientbound("boss_event");
	public static final PacketType<ClientboundChangeDifficultyPacket> CLIENTBOUND_CHANGE_DIFFICULTY = createClientbound("change_difficulty");
	public static final PacketType<ClientboundChunkBatchFinishedPacket> CLIENTBOUND_CHUNK_BATCH_FINISHED = createClientbound("chunk_batch_finished");
	public static final PacketType<ClientboundChunkBatchStartPacket> CLIENTBOUND_CHUNK_BATCH_START = createClientbound("chunk_batch_start");
	public static final PacketType<ClientboundChunksBiomesPacket> CLIENTBOUND_CHUNKS_BIOMES = createClientbound("chunks_biomes");
	public static final PacketType<ClientboundClearTitlesPacket> CLIENTBOUND_CLEAR_TITLES = createClientbound("clear_titles");
	public static final PacketType<ClientboundCommandSuggestionsPacket> CLIENTBOUND_COMMAND_SUGGESTIONS = createClientbound("command_suggestions");
	public static final PacketType<ClientboundCommandsPacket> CLIENTBOUND_COMMANDS = createClientbound("commands");
	public static final PacketType<ClientboundContainerClosePacket> CLIENTBOUND_CONTAINER_CLOSE = createClientbound("container_close");
	public static final PacketType<ClientboundContainerSetContentPacket> CLIENTBOUND_CONTAINER_SET_CONTENT = createClientbound("container_set_content");
	public static final PacketType<ClientboundContainerSetDataPacket> CLIENTBOUND_CONTAINER_SET_DATA = createClientbound("container_set_data");
	public static final PacketType<ClientboundContainerSetSlotPacket> CLIENTBOUND_CONTAINER_SET_SLOT = createClientbound("container_set_slot");
	public static final PacketType<ClientboundCooldownPacket> CLIENTBOUND_COOLDOWN = createClientbound("cooldown");
	public static final PacketType<ClientboundCustomChatCompletionsPacket> CLIENTBOUND_CUSTOM_CHAT_COMPLETIONS = createClientbound("custom_chat_completions");
	public static final PacketType<ClientboundDamageEventPacket> CLIENTBOUND_DAMAGE_EVENT = createClientbound("damage_event");
	public static final PacketType<ClientboundDebugSamplePacket> CLIENTBOUND_DEBUG_SAMPLE = createClientbound("debug_sample");
	public static final PacketType<ClientboundDeleteChatPacket> CLIENTBOUND_DELETE_CHAT = createClientbound("delete_chat");
	public static final PacketType<ClientboundDisguisedChatPacket> CLIENTBOUND_DISGUISED_CHAT = createClientbound("disguised_chat");
	public static final PacketType<ClientboundEntityEventPacket> CLIENTBOUND_ENTITY_EVENT = createClientbound("entity_event");
	public static final PacketType<ClientboundExplodePacket> CLIENTBOUND_EXPLODE = createClientbound("explode");
	public static final PacketType<ClientboundForgetLevelChunkPacket> CLIENTBOUND_FORGET_LEVEL_CHUNK = createClientbound("forget_level_chunk");
	public static final PacketType<ClientboundGameEventPacket> CLIENTBOUND_GAME_EVENT = createClientbound("game_event");
	public static final PacketType<ClientboundHorseScreenOpenPacket> CLIENTBOUND_HORSE_SCREEN_OPEN = createClientbound("horse_screen_open");
	public static final PacketType<ClientboundHurtAnimationPacket> CLIENTBOUND_HURT_ANIMATION = createClientbound("hurt_animation");
	public static final PacketType<ClientboundInitializeBorderPacket> CLIENTBOUND_INITIALIZE_BORDER = createClientbound("initialize_border");
	public static final PacketType<ClientboundLevelChunkWithLightPacket> CLIENTBOUND_LEVEL_CHUNK_WITH_LIGHT = createClientbound("level_chunk_with_light");
	public static final PacketType<ClientboundLevelEventPacket> CLIENTBOUND_LEVEL_EVENT = createClientbound("level_event");
	public static final PacketType<ClientboundLevelParticlesPacket> CLIENTBOUND_LEVEL_PARTICLES = createClientbound("level_particles");
	public static final PacketType<ClientboundLightUpdatePacket> CLIENTBOUND_LIGHT_UPDATE = createClientbound("light_update");
	public static final PacketType<ClientboundLoginPacket> CLIENTBOUND_LOGIN = createClientbound("login");
	public static final PacketType<ClientboundMapItemDataPacket> CLIENTBOUND_MAP_ITEM_DATA = createClientbound("map_item_data");
	public static final PacketType<ClientboundMerchantOffersPacket> CLIENTBOUND_MERCHANT_OFFERS = createClientbound("merchant_offers");
	public static final PacketType<ClientboundMoveEntityPacket.Pos> CLIENTBOUND_MOVE_ENTITY_POS = createClientbound("move_entity_pos");
	public static final PacketType<ClientboundMoveEntityPacket.PosRot> CLIENTBOUND_MOVE_ENTITY_POS_ROT = createClientbound("move_entity_pos_rot");
	public static final PacketType<ClientboundMoveEntityPacket.Rot> CLIENTBOUND_MOVE_ENTITY_ROT = createClientbound("move_entity_rot");
	public static final PacketType<ClientboundMoveVehiclePacket> CLIENTBOUND_MOVE_VEHICLE = createClientbound("move_vehicle");
	public static final PacketType<ClientboundOpenBookPacket> CLIENTBOUND_OPEN_BOOK = createClientbound("open_book");
	public static final PacketType<ClientboundOpenScreenPacket> CLIENTBOUND_OPEN_SCREEN = createClientbound("open_screen");
	public static final PacketType<ClientboundOpenSignEditorPacket> CLIENTBOUND_OPEN_SIGN_EDITOR = createClientbound("open_sign_editor");
	public static final PacketType<ClientboundPlaceGhostRecipePacket> CLIENTBOUND_PLACE_GHOST_RECIPE = createClientbound("place_ghost_recipe");
	public static final PacketType<ClientboundPlayerAbilitiesPacket> CLIENTBOUND_PLAYER_ABILITIES = createClientbound("player_abilities");
	public static final PacketType<ClientboundPlayerChatPacket> CLIENTBOUND_PLAYER_CHAT = createClientbound("player_chat");
	public static final PacketType<ClientboundPlayerCombatEndPacket> CLIENTBOUND_PLAYER_COMBAT_END = createClientbound("player_combat_end");
	public static final PacketType<ClientboundPlayerCombatEnterPacket> CLIENTBOUND_PLAYER_COMBAT_ENTER = createClientbound("player_combat_enter");
	public static final PacketType<ClientboundPlayerCombatKillPacket> CLIENTBOUND_PLAYER_COMBAT_KILL = createClientbound("player_combat_kill");
	public static final PacketType<ClientboundPlayerInfoRemovePacket> CLIENTBOUND_PLAYER_INFO_REMOVE = createClientbound("player_info_remove");
	public static final PacketType<ClientboundPlayerInfoUpdatePacket> CLIENTBOUND_PLAYER_INFO_UPDATE = createClientbound("player_info_update");
	public static final PacketType<ClientboundPlayerLookAtPacket> CLIENTBOUND_PLAYER_LOOK_AT = createClientbound("player_look_at");
	public static final PacketType<ClientboundPlayerPositionPacket> CLIENTBOUND_PLAYER_POSITION = createClientbound("player_position");
	public static final PacketType<ClientboundRecipePacket> CLIENTBOUND_RECIPE = createClientbound("recipe");
	public static final PacketType<ClientboundRemoveEntitiesPacket> CLIENTBOUND_REMOVE_ENTITIES = createClientbound("remove_entities");
	public static final PacketType<ClientboundRemoveMobEffectPacket> CLIENTBOUND_REMOVE_MOB_EFFECT = createClientbound("remove_mob_effect");
	public static final PacketType<ClientboundRespawnPacket> CLIENTBOUND_RESPAWN = createClientbound("respawn");
	public static final PacketType<ClientboundRotateHeadPacket> CLIENTBOUND_ROTATE_HEAD = createClientbound("rotate_head");
	public static final PacketType<ClientboundSectionBlocksUpdatePacket> CLIENTBOUND_SECTION_BLOCKS_UPDATE = createClientbound("section_blocks_update");
	public static final PacketType<ClientboundSelectAdvancementsTabPacket> CLIENTBOUND_SELECT_ADVANCEMENTS_TAB = createClientbound("select_advancements_tab");
	public static final PacketType<ClientboundServerDataPacket> CLIENTBOUND_SERVER_DATA = createClientbound("server_data");
	public static final PacketType<ClientboundSetActionBarTextPacket> CLIENTBOUND_SET_ACTION_BAR_TEXT = createClientbound("set_action_bar_text");
	public static final PacketType<ClientboundSetBorderCenterPacket> CLIENTBOUND_SET_BORDER_CENTER = createClientbound("set_border_center");
	public static final PacketType<ClientboundSetBorderLerpSizePacket> CLIENTBOUND_SET_BORDER_LERP_SIZE = createClientbound("set_border_lerp_size");
	public static final PacketType<ClientboundSetBorderSizePacket> CLIENTBOUND_SET_BORDER_SIZE = createClientbound("set_border_size");
	public static final PacketType<ClientboundSetBorderWarningDelayPacket> CLIENTBOUND_SET_BORDER_WARNING_DELAY = createClientbound("set_border_warning_delay");
	public static final PacketType<ClientboundSetBorderWarningDistancePacket> CLIENTBOUND_SET_BORDER_WARNING_DISTANCE = createClientbound(
		"set_border_warning_distance"
	);
	public static final PacketType<ClientboundSetCameraPacket> CLIENTBOUND_SET_CAMERA = createClientbound("set_camera");
	public static final PacketType<ClientboundSetCarriedItemPacket> CLIENTBOUND_SET_CARRIED_ITEM = createClientbound("set_carried_item");
	public static final PacketType<ClientboundSetChunkCacheCenterPacket> CLIENTBOUND_SET_CHUNK_CACHE_CENTER = createClientbound("set_chunk_cache_center");
	public static final PacketType<ClientboundSetChunkCacheRadiusPacket> CLIENTBOUND_SET_CHUNK_CACHE_RADIUS = createClientbound("set_chunk_cache_radius");
	public static final PacketType<ClientboundSetDefaultSpawnPositionPacket> CLIENTBOUND_SET_DEFAULT_SPAWN_POSITION = createClientbound(
		"set_default_spawn_position"
	);
	public static final PacketType<ClientboundSetDisplayObjectivePacket> CLIENTBOUND_SET_DISPLAY_OBJECTIVE = createClientbound("set_display_objective");
	public static final PacketType<ClientboundSetEntityDataPacket> CLIENTBOUND_SET_ENTITY_DATA = createClientbound("set_entity_data");
	public static final PacketType<ClientboundSetEntityLinkPacket> CLIENTBOUND_SET_ENTITY_LINK = createClientbound("set_entity_link");
	public static final PacketType<ClientboundSetEntityMotionPacket> CLIENTBOUND_SET_ENTITY_MOTION = createClientbound("set_entity_motion");
	public static final PacketType<ClientboundSetEquipmentPacket> CLIENTBOUND_SET_EQUIPMENT = createClientbound("set_equipment");
	public static final PacketType<ClientboundSetExperiencePacket> CLIENTBOUND_SET_EXPERIENCE = createClientbound("set_experience");
	public static final PacketType<ClientboundSetHealthPacket> CLIENTBOUND_SET_HEALTH = createClientbound("set_health");
	public static final PacketType<ClientboundSetObjectivePacket> CLIENTBOUND_SET_OBJECTIVE = createClientbound("set_objective");
	public static final PacketType<ClientboundSetPassengersPacket> CLIENTBOUND_SET_PASSENGERS = createClientbound("set_passengers");
	public static final PacketType<ClientboundSetPlayerTeamPacket> CLIENTBOUND_SET_PLAYER_TEAM = createClientbound("set_player_team");
	public static final PacketType<ClientboundSetScorePacket> CLIENTBOUND_SET_SCORE = createClientbound("set_score");
	public static final PacketType<ClientboundSetSimulationDistancePacket> CLIENTBOUND_SET_SIMULATION_DISTANCE = createClientbound("set_simulation_distance");
	public static final PacketType<ClientboundSetSubtitleTextPacket> CLIENTBOUND_SET_SUBTITLE_TEXT = createClientbound("set_subtitle_text");
	public static final PacketType<ClientboundSetTimePacket> CLIENTBOUND_SET_TIME = createClientbound("set_time");
	public static final PacketType<ClientboundSetTitleTextPacket> CLIENTBOUND_SET_TITLE_TEXT = createClientbound("set_title_text");
	public static final PacketType<ClientboundSetTitlesAnimationPacket> CLIENTBOUND_SET_TITLES_ANIMATION = createClientbound("set_titles_animation");
	public static final PacketType<ClientboundSoundEntityPacket> CLIENTBOUND_SOUND_ENTITY = createClientbound("sound_entity");
	public static final PacketType<ClientboundSoundPacket> CLIENTBOUND_SOUND = createClientbound("sound");
	public static final PacketType<ClientboundStartConfigurationPacket> CLIENTBOUND_START_CONFIGURATION = createClientbound("start_configuration");
	public static final PacketType<ClientboundStopSoundPacket> CLIENTBOUND_STOP_SOUND = createClientbound("stop_sound");
	public static final PacketType<ClientboundSystemChatPacket> CLIENTBOUND_SYSTEM_CHAT = createClientbound("system_chat");
	public static final PacketType<ClientboundTabListPacket> CLIENTBOUND_TAB_LIST = createClientbound("tab_list");
	public static final PacketType<ClientboundTagQueryPacket> CLIENTBOUND_TAG_QUERY = createClientbound("tag_query");
	public static final PacketType<ClientboundTakeItemEntityPacket> CLIENTBOUND_TAKE_ITEM_ENTITY = createClientbound("take_item_entity");
	public static final PacketType<ClientboundTeleportEntityPacket> CLIENTBOUND_TELEPORT_ENTITY = createClientbound("teleport_entity");
	public static final PacketType<ClientboundUpdateAdvancementsPacket> CLIENTBOUND_UPDATE_ADVANCEMENTS = createClientbound("update_advancements");
	public static final PacketType<ClientboundUpdateAttributesPacket> CLIENTBOUND_UPDATE_ATTRIBUTES = createClientbound("update_attributes");
	public static final PacketType<ClientboundUpdateMobEffectPacket> CLIENTBOUND_UPDATE_MOB_EFFECT = createClientbound("update_mob_effect");
	public static final PacketType<ClientboundUpdateRecipesPacket> CLIENTBOUND_UPDATE_RECIPES = createClientbound("update_recipes");
	public static final PacketType<ClientboundProjectilePowerPacket> CLIENTBOUND_PROJECTILE_POWER = createClientbound("projectile_power");
	public static final PacketType<ServerboundAcceptTeleportationPacket> SERVERBOUND_ACCEPT_TELEPORTATION = createServerbound("accept_teleportation");
	public static final PacketType<ServerboundBlockEntityTagQueryPacket> SERVERBOUND_BLOCK_ENTITY_TAG_QUERY = createServerbound("block_entity_tag_query");
	public static final PacketType<ServerboundChangeDifficultyPacket> SERVERBOUND_CHANGE_DIFFICULTY = createServerbound("change_difficulty");
	public static final PacketType<ServerboundChatAckPacket> SERVERBOUND_CHAT_ACK = createServerbound("chat_ack");
	public static final PacketType<ServerboundChatCommandPacket> SERVERBOUND_CHAT_COMMAND = createServerbound("chat_command");
	public static final PacketType<ServerboundChatCommandSignedPacket> SERVERBOUND_CHAT_COMMAND_SIGNED = createServerbound("chat_command_signed");
	public static final PacketType<ServerboundChatPacket> SERVERBOUND_CHAT = createServerbound("chat");
	public static final PacketType<ServerboundChatSessionUpdatePacket> SERVERBOUND_CHAT_SESSION_UPDATE = createServerbound("chat_session_update");
	public static final PacketType<ServerboundChunkBatchReceivedPacket> SERVERBOUND_CHUNK_BATCH_RECEIVED = createServerbound("chunk_batch_received");
	public static final PacketType<ServerboundClientCommandPacket> SERVERBOUND_CLIENT_COMMAND = createServerbound("client_command");
	public static final PacketType<ServerboundCommandSuggestionPacket> SERVERBOUND_COMMAND_SUGGESTION = createServerbound("command_suggestion");
	public static final PacketType<ServerboundConfigurationAcknowledgedPacket> SERVERBOUND_CONFIGURATION_ACKNOWLEDGED = createServerbound(
		"configuration_acknowledged"
	);
	public static final PacketType<ServerboundContainerButtonClickPacket> SERVERBOUND_CONTAINER_BUTTON_CLICK = createServerbound("container_button_click");
	public static final PacketType<ServerboundContainerClickPacket> SERVERBOUND_CONTAINER_CLICK = createServerbound("container_click");
	public static final PacketType<ServerboundContainerClosePacket> SERVERBOUND_CONTAINER_CLOSE = createServerbound("container_close");
	public static final PacketType<ServerboundContainerSlotStateChangedPacket> SERVERBOUND_CONTAINER_SLOT_STATE_CHANGED = createServerbound(
		"container_slot_state_changed"
	);
	public static final PacketType<ServerboundDebugSampleSubscriptionPacket> SERVERBOUND_DEBUG_SAMPLE_SUBSCRIPTION = createServerbound("debug_sample_subscription");
	public static final PacketType<ServerboundEditBookPacket> SERVERBOUND_EDIT_BOOK = createServerbound("edit_book");
	public static final PacketType<ServerboundEntityTagQueryPacket> SERVERBOUND_ENTITY_TAG_QUERY = createServerbound("entity_tag_query");
	public static final PacketType<ServerboundInteractPacket> SERVERBOUND_INTERACT = createServerbound("interact");
	public static final PacketType<ServerboundJigsawGeneratePacket> SERVERBOUND_JIGSAW_GENERATE = createServerbound("jigsaw_generate");
	public static final PacketType<ServerboundLockDifficultyPacket> SERVERBOUND_LOCK_DIFFICULTY = createServerbound("lock_difficulty");
	public static final PacketType<ServerboundMovePlayerPacket.Pos> SERVERBOUND_MOVE_PLAYER_POS = createServerbound("move_player_pos");
	public static final PacketType<ServerboundMovePlayerPacket.PosRot> SERVERBOUND_MOVE_PLAYER_POS_ROT = createServerbound("move_player_pos_rot");
	public static final PacketType<ServerboundMovePlayerPacket.Rot> SERVERBOUND_MOVE_PLAYER_ROT = createServerbound("move_player_rot");
	public static final PacketType<ServerboundMovePlayerPacket.StatusOnly> SERVERBOUND_MOVE_PLAYER_STATUS_ONLY = createServerbound("move_player_status_only");
	public static final PacketType<ServerboundMoveVehiclePacket> SERVERBOUND_MOVE_VEHICLE = createServerbound("move_vehicle");
	public static final PacketType<ServerboundPaddleBoatPacket> SERVERBOUND_PADDLE_BOAT = createServerbound("paddle_boat");
	public static final PacketType<ServerboundPickItemPacket> SERVERBOUND_PICK_ITEM = createServerbound("pick_item");
	public static final PacketType<ServerboundPlaceRecipePacket> SERVERBOUND_PLACE_RECIPE = createServerbound("place_recipe");
	public static final PacketType<ServerboundPlayerAbilitiesPacket> SERVERBOUND_PLAYER_ABILITIES = createServerbound("player_abilities");
	public static final PacketType<ServerboundPlayerActionPacket> SERVERBOUND_PLAYER_ACTION = createServerbound("player_action");
	public static final PacketType<ServerboundPlayerCommandPacket> SERVERBOUND_PLAYER_COMMAND = createServerbound("player_command");
	public static final PacketType<ServerboundPlayerInputPacket> SERVERBOUND_PLAYER_INPUT = createServerbound("player_input");
	public static final PacketType<ServerboundRecipeBookChangeSettingsPacket> SERVERBOUND_RECIPE_BOOK_CHANGE_SETTINGS = createServerbound(
		"recipe_book_change_settings"
	);
	public static final PacketType<ServerboundRecipeBookSeenRecipePacket> SERVERBOUND_RECIPE_BOOK_SEEN_RECIPE = createServerbound("recipe_book_seen_recipe");
	public static final PacketType<ServerboundRenameItemPacket> SERVERBOUND_RENAME_ITEM = createServerbound("rename_item");
	public static final PacketType<ServerboundSeenAdvancementsPacket> SERVERBOUND_SEEN_ADVANCEMENTS = createServerbound("seen_advancements");
	public static final PacketType<ServerboundSelectTradePacket> SERVERBOUND_SELECT_TRADE = createServerbound("select_trade");
	public static final PacketType<ServerboundSetBeaconPacket> SERVERBOUND_SET_BEACON = createServerbound("set_beacon");
	public static final PacketType<ServerboundSetCarriedItemPacket> SERVERBOUND_SET_CARRIED_ITEM = createServerbound("set_carried_item");
	public static final PacketType<ServerboundSetCommandBlockPacket> SERVERBOUND_SET_COMMAND_BLOCK = createServerbound("set_command_block");
	public static final PacketType<ServerboundSetCommandMinecartPacket> SERVERBOUND_SET_COMMAND_MINECART = createServerbound("set_command_minecart");
	public static final PacketType<ServerboundSetCreativeModeSlotPacket> SERVERBOUND_SET_CREATIVE_MODE_SLOT = createServerbound("set_creative_mode_slot");
	public static final PacketType<ServerboundSetJigsawBlockPacket> SERVERBOUND_SET_JIGSAW_BLOCK = createServerbound("set_jigsaw_block");
	public static final PacketType<ServerboundSetStructureBlockPacket> SERVERBOUND_SET_STRUCTURE_BLOCK = createServerbound("set_structure_block");
	public static final PacketType<ServerboundSignUpdatePacket> SERVERBOUND_SIGN_UPDATE = createServerbound("sign_update");
	public static final PacketType<ServerboundSwingPacket> SERVERBOUND_SWING = createServerbound("swing");
	public static final PacketType<ServerboundTeleportToEntityPacket> SERVERBOUND_TELEPORT_TO_ENTITY = createServerbound("teleport_to_entity");
	public static final PacketType<ServerboundUseItemOnPacket> SERVERBOUND_USE_ITEM_ON = createServerbound("use_item_on");
	public static final PacketType<ServerboundUseItemPacket> SERVERBOUND_USE_ITEM = createServerbound("use_item");
	public static final PacketType<ClientboundResetScorePacket> CLIENTBOUND_RESET_SCORE = createClientbound("reset_score");
	public static final PacketType<ClientboundTickingStatePacket> CLIENTBOUND_TICKING_STATE = createClientbound("ticking_state");
	public static final PacketType<ClientboundTickingStepPacket> CLIENTBOUND_TICKING_STEP = createClientbound("ticking_step");

	private static <T extends Packet<ClientGamePacketListener>> PacketType<T> createClientbound(String string) {
		return new PacketType<>(PacketFlow.CLIENTBOUND, new ResourceLocation(string));
	}

	private static <T extends Packet<ServerGamePacketListener>> PacketType<T> createServerbound(String string) {
		return new PacketType<>(PacketFlow.SERVERBOUND, new ResourceLocation(string));
	}
}
