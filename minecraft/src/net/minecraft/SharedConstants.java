package net.minecraft;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;
import java.time.Duration;
import javax.annotation.Nullable;
import net.minecraft.commands.BrigadierExceptions;

public class SharedConstants {
	@Deprecated
	public static final boolean SNAPSHOT = true;
	@Deprecated
	public static final int WORLD_VERSION = 2826;
	@Deprecated
	public static final String SERIES = "ccpreview";
	@Deprecated
	public static final String VERSION_STRING = "1.18-experimental-2";
	@Deprecated
	public static final String RELEASE_TARGET = "1.18";
	@Deprecated
	public static final int RELEASE_NETWORK_PROTOCOL_VERSION = 756;
	@Deprecated
	public static final int SNAPSHOT_NETWORK_PROTOCOL_VERSION = 42;
	public static final int SNBT_NAG_VERSION = 2678;
	private static final int SNAPSHOT_PROTOCOL_BIT = 30;
	@Deprecated
	public static final int RESOURCE_PACK_FORMAT = 7;
	@Deprecated
	public static final int DATA_PACK_FORMAT = 7;
	public static final String DATA_VERSION_TAG = "DataVersion";
	public static final boolean CNC_PART_2 = true;
	public static final boolean CNC_PART_2_ITEMS_AND_BLOCKS = true;
	public static final boolean NEW_WORLD_GENERATION = true;
	public static final boolean EXTENDED_WORLD_HEIGHT = true;
	public static final boolean AQUIFER_ENABLED_CARVERS = true;
	public static final boolean USE_NEW_RENDERSYSTEM = false;
	public static final boolean MULTITHREADED_RENDERING = false;
	public static final boolean FIX_TNT_DUPE = false;
	public static final boolean FIX_SAND_DUPE = false;
	public static final boolean ENABLE_SNOOPER = false;
	public static final boolean USE_DEBUG_FEATURES = false;
	public static final boolean ALLOW_INCOMPATIBLE_WORLD_HEIGHT = false;
	public static final boolean DEBUG_HOTKEYS = false;
	public static final boolean DEBUG_UI_NARRATION = false;
	public static final boolean DEBUG_RENDER = false;
	public static final boolean DEBUG_PATHFINDING = false;
	public static final boolean DEBUG_WATER = false;
	public static final boolean DEBUG_HEIGHTMAP = false;
	public static final boolean DEBUG_COLLISION = false;
	public static final boolean DEBUG_SHAPES = false;
	public static final boolean DEBUG_NEIGHBORSUPDATE = false;
	public static final boolean DEBUG_STRUCTURES = false;
	public static final boolean DEBUG_LIGHT = false;
	public static final boolean DEBUG_WORLDGENATTEMPT = false;
	public static final boolean DEBUG_SOLID_FACE = false;
	public static final boolean DEBUG_CHUNKS = false;
	public static final boolean DEBUG_GAME_EVENT_LISTENERS = false;
	public static final boolean DEBUG_DUMP_TEXTURE_ATLAS = false;
	public static final boolean DEBUG_DUMP_INTERPOLATED_TEXTURE_FRAMES = false;
	public static final boolean DEBUG_STRUCTURE_EDIT_MODE = false;
	public static final boolean DEBUG_SAVE_STRUCTURES_AS_SNBT = false;
	public static final boolean DEBUG_SYNCHRONOUS_GL_LOGS = false;
	public static final boolean DEBUG_VERBOSE_SERVER_EVENTS = false;
	public static final boolean DEBUG_NAMED_RUNNABLES = false;
	public static final boolean DEBUG_GOAL_SELECTOR = false;
	public static final boolean DEBUG_VILLAGE_SECTIONS = false;
	public static final boolean DEBUG_BRAIN = false;
	public static final boolean DEBUG_BEES = false;
	public static final boolean DEBUG_RAIDS = false;
	public static final boolean DEBUG_BLOCK_BREAK = false;
	public static final boolean DEBUG_RESOURCE_LOAD_TIMES = false;
	public static final boolean DEBUG_MONITOR_TICK_TIMES = false;
	public static final boolean DEBUG_KEEP_JIGSAW_BLOCKS_DURING_STRUCTURE_GEN = false;
	public static final boolean DEBUG_DONT_SAVE_WORLD = false;
	public static final boolean DEBUG_LARGE_DRIPSTONE = false;
	public static final boolean DEBUG_PACKET_SERIALIZATION = false;
	public static final boolean DEBUG_CARVERS = false;
	public static final boolean DEBUG_ORE_VEINS = false;
	public static final boolean DEBUG_SMALL_SPAWN = false;
	public static final boolean DEBUG_DISABLE_LIQUID_SPREADING = false;
	public static final boolean DEBUG_AQUIFERS = false;
	public static final boolean JFR_PROFILING_ENABLE_WORLD_CREATION = false;
	public static boolean DEBUG_GENERATE_SQUARE_TERRAIN_WITHOUT_NOISE = false;
	public static final boolean DEBUG_ONLY_GENERATE_HALF_THE_WORLD = false;
	public static final boolean DEBUG_DISABLE_FLUID_GENERATION = false;
	public static final boolean DEBUG_DISABLE_AQUIFERS = false;
	public static final boolean DEBUG_DISABLE_NOISE_CAVES = false;
	public static final boolean DEBUG_DISABLE_SURFACE = false;
	public static final boolean DEBUG_DISABLE_CARVERS = false;
	public static final boolean DEBUG_DISABLE_STRUCTURES = false;
	public static final boolean DEBUG_DISABLE_FEATURES = false;
	public static final boolean DEBUG_DISABLE_ORE_VEINS = false;
	public static final boolean DEBUG_DISABLE_NOODLE_CAVES = false;
	public static final int DEFAULT_MINECRAFT_PORT = 25565;
	public static final boolean INGAME_DEBUG_OUTPUT = false;
	public static final boolean DEBUG_SUBTITLES = false;
	public static final int FAKE_MS_LATENCY = 0;
	public static final int FAKE_MS_JITTER = 0;
	public static final Level NETTY_LEAK_DETECTION = Level.DISABLED;
	public static final boolean COMMAND_STACK_TRACES = false;
	public static final boolean DEBUG_WORLD_RECREATE = false;
	public static final boolean DEBUG_SHOW_SERVER_DEBUG_VALUES = false;
	public static final boolean DEBUG_STORE_CHUNK_STACKTRACES = false;
	public static final float RAIN_THRESHOLD = 0.15F;
	public static final long MAXIMUM_TICK_TIME_NANOS = Duration.ofMillis(300L).toNanos();
	public static boolean CHECK_DATA_FIXER_SCHEMA = true;
	public static boolean IS_RUNNING_IN_IDE;
	public static final int WORLD_RESOLUTION = 16;
	public static final int MAX_CHAT_LENGTH = 256;
	public static final int MAX_COMMAND_LENGTH = 32500;
	public static final char[] ILLEGAL_FILE_CHARACTERS = new char[]{'/', '\n', '\r', '\t', '\u0000', '\f', '`', '?', '*', '\\', '<', '>', '|', '"', ':'};
	public static final int TICKS_PER_SECOND = 20;
	public static final int TICKS_PER_MINUTE = 1200;
	public static final int TICKS_PER_GAME_DAY = 24000;
	public static final float AVERAGE_GAME_TICKS_PER_RANDOM_TICK_PER_BLOCK = 1365.3334F;
	public static final float AVERAGE_RANDOM_TICKS_PER_BLOCK_PER_MINUTE = 0.87890625F;
	public static final float AVERAGE_RANDOM_TICKS_PER_BLOCK_PER_GAME_DAY = 17.578125F;
	@Nullable
	private static WorldVersion CURRENT_VERSION;

	public static boolean isAllowedChatCharacter(char c) {
		return c != 167 && c >= ' ' && c != 127;
	}

	public static String filterText(String string) {
		StringBuilder stringBuilder = new StringBuilder();

		for (char c : string.toCharArray()) {
			if (isAllowedChatCharacter(c)) {
				stringBuilder.append(c);
			}
		}

		return stringBuilder.toString();
	}

	public static void setVersion(WorldVersion worldVersion) {
		if (CURRENT_VERSION == null) {
			CURRENT_VERSION = worldVersion;
		} else if (worldVersion != CURRENT_VERSION) {
			throw new IllegalStateException("Cannot override the current game version!");
		}
	}

	public static void tryDetectVersion() {
		if (CURRENT_VERSION == null) {
			CURRENT_VERSION = DetectedVersion.tryDetectVersion();
		}
	}

	public static WorldVersion getCurrentVersion() {
		if (CURRENT_VERSION == null) {
			throw new IllegalStateException("Game version not set");
		} else {
			return CURRENT_VERSION;
		}
	}

	public static int getProtocolVersion() {
		return 1073741866;
	}

	public static boolean debugVoidTerrain(int i, int j) {
		return !DEBUG_GENERATE_SQUARE_TERRAIN_WITHOUT_NOISE ? false : i > 8192 || i < 0 || j > 1024 || j < 0;
	}

	static {
		ResourceLeakDetector.setLevel(NETTY_LEAK_DETECTION);
		CommandSyntaxException.ENABLE_COMMAND_STACK_TRACES = false;
		CommandSyntaxException.BUILT_IN_EXCEPTIONS = new BrigadierExceptions();
	}
}
