package net.minecraft.world.level;

import com.mojang.serialization.Dynamic;
import net.minecraft.world.Difficulty;

public final class LevelSettings {
	private final String levelName;
	private final GameType gameType;
	private final boolean hardcore;
	private final Difficulty difficulty;
	private final boolean allowCommands;
	private final GameRules gameRules;
	private final DataPackConfig dataPackConfig;

	public LevelSettings(String string, GameType gameType, boolean bl, Difficulty difficulty, boolean bl2, GameRules gameRules, DataPackConfig dataPackConfig) {
		this.levelName = string;
		this.gameType = gameType;
		this.hardcore = bl;
		this.difficulty = difficulty;
		this.allowCommands = bl2;
		this.gameRules = gameRules;
		this.dataPackConfig = dataPackConfig;
	}

	public static LevelSettings parse(Dynamic<?> dynamic, DataPackConfig dataPackConfig) {
		GameType gameType = GameType.byId(dynamic.get("GameType").asInt(0));
		return new LevelSettings(
			dynamic.get("LevelName").asString(""),
			gameType,
			dynamic.get("hardcore").asBoolean(false),
			(Difficulty)dynamic.get("Difficulty").asNumber().map(number -> Difficulty.byId(number.byteValue())).result().orElse(Difficulty.NORMAL),
			dynamic.get("allowCommands").asBoolean(gameType == GameType.CREATIVE),
			new GameRules(dynamic.get("GameRules")),
			dataPackConfig
		);
	}

	public String levelName() {
		return this.levelName;
	}

	public GameType gameType() {
		return this.gameType;
	}

	public boolean hardcore() {
		return this.hardcore;
	}

	public Difficulty difficulty() {
		return this.difficulty;
	}

	public boolean allowCommands() {
		return this.allowCommands;
	}

	public GameRules gameRules() {
		return this.gameRules;
	}

	public DataPackConfig getDataPackConfig() {
		return this.dataPackConfig;
	}

	public LevelSettings withGameType(GameType gameType) {
		return new LevelSettings(this.levelName, gameType, this.hardcore, this.difficulty, this.allowCommands, this.gameRules, this.dataPackConfig);
	}

	public LevelSettings withDifficulty(Difficulty difficulty) {
		return new LevelSettings(this.levelName, this.gameType, this.hardcore, difficulty, this.allowCommands, this.gameRules, this.dataPackConfig);
	}

	public LevelSettings withDataPackConfig(DataPackConfig dataPackConfig) {
		return new LevelSettings(this.levelName, this.gameType, this.hardcore, this.difficulty, this.allowCommands, this.gameRules, dataPackConfig);
	}

	public LevelSettings copy() {
		return new LevelSettings(this.levelName, this.gameType, this.hardcore, this.difficulty, this.allowCommands, this.gameRules.copy(), this.dataPackConfig);
	}
}
