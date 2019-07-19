package net.minecraft.world.level;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.storage.LevelData;

public final class LevelSettings {
	private final long seed;
	private final GameType gameType;
	private final boolean generateMapFeatures;
	private final boolean hardcore;
	private final LevelType levelType;
	private boolean allowCommands;
	private boolean startingBonusItems;
	private JsonElement levelTypeOptions = new JsonObject();

	public LevelSettings(long l, GameType gameType, boolean bl, boolean bl2, LevelType levelType) {
		this.seed = l;
		this.gameType = gameType;
		this.generateMapFeatures = bl;
		this.hardcore = bl2;
		this.levelType = levelType;
	}

	public LevelSettings(LevelData levelData) {
		this(levelData.getSeed(), levelData.getGameType(), levelData.isGenerateMapFeatures(), levelData.isHardcore(), levelData.getGeneratorType());
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

	public LevelSettings setLevelTypeOptions(JsonElement jsonElement) {
		this.levelTypeOptions = jsonElement;
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

	public LevelType getLevelType() {
		return this.levelType;
	}

	public boolean getAllowCommands() {
		return this.allowCommands;
	}

	public JsonElement getLevelTypeOptions() {
		return this.levelTypeOptions;
	}
}
