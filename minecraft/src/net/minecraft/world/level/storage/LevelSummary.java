package net.minecraft.world.level.storage;

import java.io.File;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelType;

@Environment(EnvType.CLIENT)
public class LevelSummary implements Comparable<LevelSummary> {
	private final String levelId;
	private final String levelName;
	private final long lastPlayed;
	private final long sizeOnDisk;
	private final boolean requiresConversion;
	private final GameType gameMode;
	private final boolean hardcore;
	private final boolean hasCheats;
	private final String worldVersionName;
	private final int worldVersion;
	private final boolean snapshot;
	private final LevelType generatorType;
	private final boolean locked;
	private final File icon;

	public LevelSummary(LevelData levelData, String string, String string2, long l, boolean bl, boolean bl2, File file) {
		this.levelId = string;
		this.levelName = string2;
		this.locked = bl2;
		this.icon = file;
		this.lastPlayed = levelData.getLastPlayed();
		this.sizeOnDisk = l;
		this.gameMode = levelData.getGameType();
		this.requiresConversion = bl;
		this.hardcore = levelData.isHardcore();
		this.hasCheats = levelData.getAllowCommands();
		this.worldVersionName = levelData.getMinecraftVersionName();
		this.worldVersion = levelData.getMinecraftVersion();
		this.snapshot = levelData.isSnapshot();
		this.generatorType = levelData.getGeneratorType();
	}

	public String getLevelId() {
		return this.levelId;
	}

	public String getLevelName() {
		return this.levelName;
	}

	public File getIcon() {
		return this.icon;
	}

	public boolean isRequiresConversion() {
		return this.requiresConversion;
	}

	public long getLastPlayed() {
		return this.lastPlayed;
	}

	public int compareTo(LevelSummary levelSummary) {
		if (this.lastPlayed < levelSummary.lastPlayed) {
			return 1;
		} else {
			return this.lastPlayed > levelSummary.lastPlayed ? -1 : this.levelId.compareTo(levelSummary.levelId);
		}
	}

	public GameType getGameMode() {
		return this.gameMode;
	}

	public boolean isHardcore() {
		return this.hardcore;
	}

	public boolean hasCheats() {
		return this.hasCheats;
	}

	public Component getWorldVersionName() {
		return (Component)(StringUtil.isNullOrEmpty(this.worldVersionName)
			? new TranslatableComponent("selectWorld.versionUnknown")
			: new TextComponent(this.worldVersionName));
	}

	public boolean markVersionInList() {
		return this.askToOpenWorld() || !SharedConstants.getCurrentVersion().isStable() && !this.snapshot || this.shouldBackup() || this.isOldCustomizedWorld();
	}

	public boolean askToOpenWorld() {
		return this.worldVersion > SharedConstants.getCurrentVersion().getWorldVersion();
	}

	public boolean isOldCustomizedWorld() {
		return this.generatorType == LevelType.CUSTOMIZED && this.worldVersion < 1466;
	}

	public boolean shouldBackup() {
		return this.worldVersion < SharedConstants.getCurrentVersion().getWorldVersion();
	}

	public boolean isLocked() {
		return this.locked;
	}
}
