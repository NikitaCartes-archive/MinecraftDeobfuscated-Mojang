package net.minecraft.world.level.storage;

import com.mojang.serialization.Lifecycle;
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
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.apache.commons.lang3.StringUtils;

@Environment(EnvType.CLIENT)
public class LevelSummary implements Comparable<LevelSummary> {
	private final LevelSettings settings;
	private final LevelVersion levelVersion;
	private final String levelId;
	private final boolean requiresConversion;
	private final boolean locked;
	private final File icon;
	private final Lifecycle lifecycle;
	@Nullable
	private Component info;

	public LevelSummary(LevelSettings levelSettings, LevelVersion levelVersion, String string, boolean bl, boolean bl2, File file, Lifecycle lifecycle) {
		this.settings = levelSettings;
		this.levelVersion = levelVersion;
		this.levelId = string;
		this.locked = bl2;
		this.icon = file;
		this.requiresConversion = bl;
		this.lifecycle = lifecycle;
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

	public boolean markVersionInList() {
		return this.askToOpenWorld()
			|| !SharedConstants.getCurrentVersion().isStable() && !this.levelVersion.snapshot()
			|| this.shouldBackup()
			|| this.isOldCustomizedWorld()
			|| this.experimental();
	}

	public boolean askToOpenWorld() {
		return this.levelVersion.minecraftVersion() > SharedConstants.getCurrentVersion().getWorldVersion();
	}

	public boolean isOldCustomizedWorld() {
		return this.settings.worldGenSettings().isOldCustomizedWorld() && this.levelVersion.minecraftVersion() < 1466;
	}

	protected WorldGenSettings worldGenSettings() {
		return this.settings.worldGenSettings();
	}

	public boolean experimental() {
		return this.lifecycle != Lifecycle.stable();
	}

	public boolean shouldBackup() {
		return this.levelVersion.minecraftVersion() < SharedConstants.getCurrentVersion().getWorldVersion();
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
