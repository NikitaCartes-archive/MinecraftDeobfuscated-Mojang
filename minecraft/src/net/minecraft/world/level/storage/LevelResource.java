package net.minecraft.world.level.storage;

public class LevelResource {
	public static final LevelResource PLAYER_ADVANCEMENTS_DIR = new LevelResource("advancements");
	public static final LevelResource PLAYER_STATS_DIR = new LevelResource("stats");
	public static final LevelResource PLAYER_DATA_DIR = new LevelResource("playerdata");
	public static final LevelResource PLAYER_OLD_DATA_DIR = new LevelResource("players");
	public static final LevelResource LEVEL_DATA_FILE = new LevelResource("level.dat");
	public static final LevelResource OLD_LEVEL_DATA_FILE = new LevelResource("level.dat_old");
	public static final LevelResource ICON_FILE = new LevelResource("icon.png");
	public static final LevelResource LOCK_FILE = new LevelResource("session.lock");
	public static final LevelResource GENERATED_DIR = new LevelResource("generated");
	public static final LevelResource DATAPACK_DIR = new LevelResource("datapacks");
	public static final LevelResource MAP_RESOURCE_FILE = new LevelResource("resources.zip");
	public static final LevelResource ROOT = new LevelResource(".");
	public static final LevelResource VOTES = new LevelResource("votes.json");
	public static final LevelResource OLD_VOTES = new LevelResource("votes.json_old");
	private final String id;

	private LevelResource(String string) {
		this.id = string;
	}

	public String getId() {
		return this.id;
	}

	public String toString() {
		return "/" + this.id;
	}
}
