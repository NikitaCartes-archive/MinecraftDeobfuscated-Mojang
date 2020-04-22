package net.minecraft.world.level.storage;

import java.io.File;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.dimension.DimensionType;

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
	@Nullable
	private Component info;

	public LevelSummary(WorldData worldData, String string, String string2, long l, boolean bl, boolean bl2, File file) {
		this.levelId = string;
		this.levelName = string2;
		this.locked = bl2;
		this.icon = file;
		this.lastPlayed = worldData.getLastPlayed();
		this.sizeOnDisk = l;
		this.gameMode = worldData.getGameType();
		this.requiresConversion = bl;
		this.hardcore = worldData.isHardcore();
		this.hasCheats = worldData.getAllowCommands();
		this.worldVersionName = worldData.getMinecraftVersionName();
		this.worldVersion = worldData.getMinecraftVersion();
		this.snapshot = worldData.isSnapshot();
		this.generatorType = worldData.getLevelData(DimensionType.OVERWORLD).getGeneratorType();
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

	public MutableComponent getWorldVersionName() {
		return (MutableComponent)(StringUtil.isNullOrEmpty(this.worldVersionName)
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

	public Component getInfo() {
		if (this.info == null) {
			this.info = this.createInfo();
		}

		return this.info;
	}

	private Component createInfo() {
		if (this.isLocked()) {
			return new TranslatableComponent("selectWorld.locked").withStyle(ChatFormatting.RED);
		} else if (this.isRequiresConversion()) {
			return new TranslatableComponent("selectWorld.conversion");
		} else {
			MutableComponent mutableComponent = (MutableComponent)(this.isHardcore()
				? new TextComponent("").append(new TranslatableComponent("gameMode.hardcore").withStyle(ChatFormatting.DARK_RED))
				: new TranslatableComponent("gameMode." + this.getGameMode().getName()));
			if (this.hasCheats()) {
				mutableComponent.append(", ").append(new TranslatableComponent("selectWorld.cheats"));
			}

			MutableComponent mutableComponent2 = this.getWorldVersionName();
			MutableComponent mutableComponent3 = new TextComponent(", ").append(new TranslatableComponent("selectWorld.version")).append(" ");
			if (this.markVersionInList()) {
				mutableComponent3.append(mutableComponent2.withStyle(this.askToOpenWorld() ? ChatFormatting.RED : ChatFormatting.ITALIC));
			} else {
				mutableComponent3.append(mutableComponent2);
			}

			mutableComponent.append(mutableComponent3);
			return mutableComponent;
		}
	}
}
