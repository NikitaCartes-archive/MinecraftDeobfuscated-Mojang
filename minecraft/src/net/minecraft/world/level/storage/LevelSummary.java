package net.minecraft.world.level.storage;

import com.mojang.bridge.game.GameVersion;
import java.io.File;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import org.apache.commons.lang3.StringUtils;

public class LevelSummary implements Comparable<LevelSummary> {
	private final LevelSettings settings;
	private final LevelVersion levelVersion;
	private final String levelId;
	private final boolean requiresConversion;
	private final boolean locked;
	private final File icon;
	@Nullable
	private Component info;

	public LevelSummary(LevelSettings levelSettings, LevelVersion levelVersion, String string, boolean bl, boolean bl2, File file) {
		this.settings = levelSettings;
		this.levelVersion = levelVersion;
		this.levelId = string;
		this.locked = bl2;
		this.icon = file;
		this.requiresConversion = bl;
	}

	public String getLevelId() {
		return this.levelId;
	}

	public String getLevelName() {
		return StringUtils.isEmpty(this.settings.levelName()) ? this.levelId : this.settings.levelName();
	}

	public File getIcon() {
		return this.icon;
	}

	public boolean isRequiresConversion() {
		return this.requiresConversion;
	}

	public long getLastPlayed() {
		return this.levelVersion.lastPlayed();
	}

	public int compareTo(LevelSummary levelSummary) {
		if (this.levelVersion.lastPlayed() < levelSummary.levelVersion.lastPlayed()) {
			return 1;
		} else {
			return this.levelVersion.lastPlayed() > levelSummary.levelVersion.lastPlayed() ? -1 : this.levelId.compareTo(levelSummary.levelId);
		}
	}

	public LevelSettings getSettings() {
		return this.settings;
	}

	public GameType getGameMode() {
		return this.settings.gameType();
	}

	public boolean isHardcore() {
		return this.settings.hardcore();
	}

	public boolean hasCheats() {
		return this.settings.allowCommands();
	}

	public MutableComponent getWorldVersionName() {
		return (MutableComponent)(StringUtil.isNullOrEmpty(this.levelVersion.minecraftVersionName())
			? new TranslatableComponent("selectWorld.versionUnknown")
			: new TextComponent(this.levelVersion.minecraftVersionName()));
	}

	public LevelVersion levelVersion() {
		return this.levelVersion;
	}

	public boolean markVersionInList() {
		return this.askToOpenWorld() || !SharedConstants.getCurrentVersion().isStable() && !this.levelVersion.snapshot() || this.backupStatus().shouldBackup();
	}

	public boolean askToOpenWorld() {
		return this.levelVersion.minecraftVersion() > SharedConstants.getCurrentVersion().getWorldVersion();
	}

	public LevelSummary.BackupStatus backupStatus() {
		GameVersion gameVersion = SharedConstants.getCurrentVersion();
		int i = gameVersion.getWorldVersion();
		int j = this.levelVersion.minecraftVersion();
		if (!gameVersion.isStable() && j < i) {
			return LevelSummary.BackupStatus.UPGRADE_TO_SNAPSHOT;
		} else {
			return j > i ? LevelSummary.BackupStatus.DOWNGRADE : LevelSummary.BackupStatus.NONE;
		}
	}

	public boolean isLocked() {
		return this.locked;
	}

	public boolean isIncompatibleWorldHeight() {
		return this.levelVersion.minecraftVersion() <= 2692;
	}

	public boolean isDisabled() {
		return this.isLocked() || this.isIncompatibleWorldHeight();
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
		} else if (this.isIncompatibleWorldHeight()) {
			return new TranslatableComponent("selectWorld.pre_worldheight").withStyle(ChatFormatting.RED);
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

	public static enum BackupStatus {
		NONE(false, false, ""),
		DOWNGRADE(true, true, "downgrade"),
		UPGRADE_TO_SNAPSHOT(true, false, "snapshot");

		private final boolean shouldBackup;
		private final boolean severe;
		private final String translationKey;

		private BackupStatus(boolean bl, boolean bl2, String string2) {
			this.shouldBackup = bl;
			this.severe = bl2;
			this.translationKey = string2;
		}

		public boolean shouldBackup() {
			return this.shouldBackup;
		}

		public boolean isSevere() {
			return this.severe;
		}

		public String getTranslationKey() {
			return this.translationKey;
		}
	}
}
