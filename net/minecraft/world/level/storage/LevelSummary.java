/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage;

import java.nio.file.Path;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.LevelVersion;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public class LevelSummary
implements Comparable<LevelSummary> {
    private final LevelSettings settings;
    private final LevelVersion levelVersion;
    private final String levelId;
    private final boolean requiresManualConversion;
    private final boolean locked;
    private final Path icon;
    @Nullable
    private Component info;

    public LevelSummary(LevelSettings levelSettings, LevelVersion levelVersion, String string, boolean bl, boolean bl2, Path path) {
        this.settings = levelSettings;
        this.levelVersion = levelVersion;
        this.levelId = string;
        this.locked = bl2;
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

    public long getLastPlayed() {
        return this.levelVersion.lastPlayed();
    }

    @Override
    public int compareTo(LevelSummary levelSummary) {
        if (this.levelVersion.lastPlayed() < levelSummary.levelVersion.lastPlayed()) {
            return 1;
        }
        if (this.levelVersion.lastPlayed() > levelSummary.levelVersion.lastPlayed()) {
            return -1;
        }
        return this.levelId.compareTo(levelSummary.levelId);
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
        if (StringUtil.isNullOrEmpty(this.levelVersion.minecraftVersionName())) {
            return Component.translatable("selectWorld.versionUnknown");
        }
        return Component.literal(this.levelVersion.minecraftVersionName());
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

    public BackupStatus backupStatus() {
        WorldVersion worldVersion = SharedConstants.getCurrentVersion();
        int i = worldVersion.getDataVersion().getVersion();
        int j = this.levelVersion.minecraftVersion().getVersion();
        if (!worldVersion.isStable() && j < i) {
            return BackupStatus.UPGRADE_TO_SNAPSHOT;
        }
        if (j > i) {
            return BackupStatus.DOWNGRADE;
        }
        return BackupStatus.NONE;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public boolean isDisabled() {
        if (this.isLocked() || this.requiresManualConversion()) {
            return true;
        }
        return !this.isCompatible();
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
        MutableComponent mutableComponent;
        if (this.isLocked()) {
            return Component.translatable("selectWorld.locked").withStyle(ChatFormatting.RED);
        }
        if (this.requiresManualConversion()) {
            return Component.translatable("selectWorld.conversion").withStyle(ChatFormatting.RED);
        }
        if (!this.isCompatible()) {
            return Component.translatable("selectWorld.incompatible_series").withStyle(ChatFormatting.RED);
        }
        MutableComponent mutableComponent2 = mutableComponent = this.isHardcore() ? Component.empty().append(Component.translatable("gameMode.hardcore").withStyle(ChatFormatting.DARK_RED)) : Component.translatable("gameMode." + this.getGameMode().getName());
        if (this.hasCheats()) {
            mutableComponent.append(", ").append(Component.translatable("selectWorld.cheats"));
        }
        MutableComponent mutableComponent22 = this.getWorldVersionName();
        MutableComponent mutableComponent3 = Component.literal(", ").append(Component.translatable("selectWorld.version")).append(" ");
        if (this.markVersionInList()) {
            mutableComponent3.append(mutableComponent22.withStyle(this.askToOpenWorld() ? ChatFormatting.RED : ChatFormatting.ITALIC));
        } else {
            mutableComponent3.append(mutableComponent22);
        }
        mutableComponent.append(mutableComponent3);
        return mutableComponent;
    }

    @Override
    public /* synthetic */ int compareTo(Object object) {
        return this.compareTo((LevelSummary)object);
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

