package net.minecraft.world.level;

import net.minecraft.world.Difficulty;
import net.minecraft.world.level.levelgen.ChunkGeneratorProvider;

public final class LevelSettings {
	private final String levelName;
	private final long seed;
	private final GameType gameType;
	private final boolean generateMapFeatures;
	private final boolean hardcore;
	private final ChunkGeneratorProvider generatorProvider;
	private final Difficulty difficulty;
	private boolean allowCommands;
	private boolean startingBonusItems;
	private final GameRules gameRules;

	public LevelSettings(String string, long l, GameType gameType, boolean bl, boolean bl2, Difficulty difficulty, ChunkGeneratorProvider chunkGeneratorProvider) {
		this(string, l, gameType, bl, bl2, difficulty, chunkGeneratorProvider, new GameRules());
	}

	public LevelSettings(
		String string, long l, GameType gameType, boolean bl, boolean bl2, Difficulty difficulty, ChunkGeneratorProvider chunkGeneratorProvider, GameRules gameRules
	) {
		this.levelName = string;
		this.seed = l;
		this.gameType = gameType;
		this.generateMapFeatures = bl;
		this.hardcore = bl2;
		this.generatorProvider = chunkGeneratorProvider;
		this.difficulty = difficulty;
		this.gameRules = gameRules;
	}

	public LevelSettings enableStartingBonusItems() {
		this.startingBonusItems = true;
		return this;
	}

	public LevelSettings enableSinglePlayerCommands() {
		this.allowCommands = true;
		return this;
	}

	public boolean hasStartingBonusItems() {
		return this.startingBonusItems;
	}

	public long getSeed() {
		return this.seed;
	}

	public GameType getGameType() {
		return this.gameType;
	}

	public boolean isHardcore() {
		return this.hardcore;
	}

	public boolean shouldGenerateMapFeatures() {
		return this.generateMapFeatures;
	}

	public ChunkGeneratorProvider getGeneratorProvider() {
		return this.generatorProvider;
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
