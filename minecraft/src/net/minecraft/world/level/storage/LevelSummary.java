package net.minecraft.world.level.storage;

import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import org.apache.commons.lang3.StringUtils;

public class LevelSummary implements Comparable<LevelSummary> {
	private final LevelSettings settings;
	private final LevelVersion levelVersion;
	private final String levelId;
	private final boolean requiresManualConversion;
	private final boolean locked;
	private final boolean experimental;
	private final Path icon;
	@Nullable
	private Component info;

	public LevelSummary(LevelSettings levelSettings, LevelVersion levelVersion, String string, boolean bl, boolean bl2, boolean bl3, Path path) {
		this.settings = levelSettings;
		this.levelVersion = levelVersion;
		this.levelId = string;
		this.locked = bl2;
		this.experimental = bl3;
		this.icon = path;
		this.requiresManualConversion = bl;
	}

	public String getLevelId() {
		return this.levelId;
	}

	public String getLevelName() {
		return StringUtils.isEmpty(this.settings.levelName()) ? this.levelId : this.settings.levelName();
	}

	public Path getIcon() {
		return this.icon;
	}

	public boolean requiresManualConversion() {
		return this.requiresManualConversion;
	}

	public boolean isExperimental() {
		return this.experimental;
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
		return StringUtil.isNullOrEmpty(this.levelVersion.minecraftVersionName())
			? Component.translatable("selectWorld.versionUnknown")
			: Component.literal(this.levelVersion.minecraftVersionName());
	}

	public LevelVersion levelVersion() {
		return this.levelVersion;
	}

	public boolean markVersionInList() {
		return this.askToOpenWorld() || !SharedConstants.getCurrentVersion().isStable() && !this.levelVersion.snapshot() || this.backupStatus().shouldBackup();
	}

	public boolean askToOpenWorld() {
		return this.levelVersion.minecraftVersion().getVersion() > SharedConstants.getCurrentVersion().getDataVersion().getVersion();
	}

	public LevelSummary.BackupStatus backupStatus() {
		WorldVersion worldVersion = SharedConstants.getCurrentVersion();
		int i = worldVersion.getDataVersion().getVersion();
		int j = this.levelVersion.minecraftVersion().getVersion();
		if (!worldVersion.isStable() && j < i) {
			return LevelSummary.BackupStatus.UPGRADE_TO_SNAPSHOT;
		} else {
			return j > i ? LevelSummary.BackupStatus.DOWNGRADE : LevelSummary.BackupStatus.NONE;
		}
	}

	public boolean isLocked() {
		return this.locked;
	}

	public boolean isDisabled() {
		return !this.isLocked() && !this.requiresManualConversion() ? !this.isCompatible() : true;
	}

	public boolean isCompatible() {
		return SharedConstants.getCurrentVersion().getDataVersion().isCompatible(this.levelVersion.minecraftVersion());
	}

	public Component getInfo() {
		if (this.info == null) {
			this.info = this.createInfo();
		}

		return this.info;
	}

	private Component createInfo() {
		if (this.isLocked()) {
			return Component.translatable("selectWorld.locked").withStyle(ChatFormatting.RED);
		} else if (this.requiresManualConversion()) {
			return Component.translatable("selectWorld.conversion").withStyle(ChatFormatting.RED);
		} else if (!this.isCompatible()) {
			return Component.translatable("selectWorld.incompatible_series").withStyle(ChatFormatting.RED);
		} else {
			MutableComponent mutableComponent = this.isHardcore()
				? Component.empty().append(Component.translatable("gameMode.hardcore").withStyle(ChatFormatting.DARK_RED))
				: Component.translatable("gameMode." + this.getGameMode().getName());
			if (this.hasCheats()) {
				mutableComponent.append(", ").append(Component.translatable("selectWorld.cheats"));
			}

			if (this.isExperimental()) {
				mutableComponent.append(", ").append(Component.translatable("selectWorld.experimental").withStyle(ChatFormatting.YELLOW));
			}

			MutableComponent mutableComponent2 = this.getWorldVersionName();
			MutableComponent mutableComponent3 = Component.literal(", ").append(Component.translatable("selectWorld.version")).append(" ");
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
