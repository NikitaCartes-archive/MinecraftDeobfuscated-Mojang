package net.minecraft.world.level;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.levelgen.ChunkGeneratorProvider;
import net.minecraft.world.level.storage.LevelData;

public final class LevelSettings {
	private final long seed;
	private final GameType gameType;
	private final boolean generateMapFeatures;
	private final boolean hardcore;
	private final ChunkGeneratorProvider generatorProvider;
	private boolean allowCommands;
	private boolean startingBonusItems;

	public LevelSettings(long l, GameType gameType, boolean bl, boolean bl2, ChunkGeneratorProvider chunkGeneratorProvider) {
		this.seed = l;
		this.gameType = gameType;
		this.generateMapFeatures = bl;
		this.hardcore = bl2;
		this.generatorProvider = chunkGeneratorProvider;
	}

	public LevelSettings(LevelData levelData) {
		this(levelData.getSeed(), levelData.getGameType(), levelData.isGenerateMapFeatures(), levelData.isHardcore(), levelData.getGeneratorProvider());
	}

	public LevelSettings enableStartingBonusItems() {
		this.startingBonusItems = true;
		return this;
	}

	@Environment(EnvType.CLIENT)
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

	public boolean isGenerateMapFeatures() {
		return this.generateMapFeatures;
	}

	public ChunkGeneratorProvider getGeneratorProvider() {
		return this.generatorProvider;
	}

	public boolean getAllowCommands() {
		return this.allowCommands;
	}
}
