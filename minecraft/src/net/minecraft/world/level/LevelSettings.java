package net.minecraft.world.level;

import net.minecraft.world.Difficulty;
import net.minecraft.world.level.levelgen.WorldGenSettings;

public final class LevelSettings {
	private final String levelName;
	private final GameType gameType;
	private final boolean hardcore;
	private final Difficulty difficulty;
	private final boolean allowCommands;
	private final GameRules gameRules;
	private final WorldGenSettings worldGenSettings;

	public LevelSettings(String string, GameType gameType, boolean bl, Difficulty difficulty, boolean bl2, GameRules gameRules, WorldGenSettings worldGenSettings) {
		this.levelName = string;
		this.gameType = gameType;
		this.hardcore = bl;
		this.difficulty = difficulty;
		this.allowCommands = bl2;
		this.gameRules = gameRules;
		this.worldGenSettings = worldGenSettings;
	}

	public WorldGenSettings worldGenSettings() {
		return this.worldGenSettings;
	}

	public GameType getGameType() {
		return this.gameType;
	}

	public boolean isHardcore() {
		return this.hardcore;
	}

	public boolean getAllowCommands() {
		return this.allowCommands;
	}

	public String getLevelName() {
		return this.levelName;
	}

	public Difficulty getDifficulty() {
		return this.difficulty;
	}

	public GameRules getGameRules() {
		return this.gameRules;
	}
}
