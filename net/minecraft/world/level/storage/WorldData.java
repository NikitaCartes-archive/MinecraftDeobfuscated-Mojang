/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage;

import com.mojang.serialization.Lifecycle;
import java.util.Set;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.ServerLevelData;
import org.jetbrains.annotations.Nullable;

public interface WorldData {
    public static final int ANVIL_VERSION_ID = 19133;
    public static final int MCREGION_VERSION_ID = 19132;

    public DataPackConfig getDataPackConfig();

    public void setDataPackConfig(DataPackConfig var1);

    public boolean wasModded();

    public Set<String> getKnownServerBrands();

    public void setModdedInfo(String var1, boolean var2);

    default public void fillCrashReportCategory(CrashReportCategory crashReportCategory) {
        crashReportCategory.setDetail("Known server brands", () -> String.join((CharSequence)", ", this.getKnownServerBrands()));
        crashReportCategory.setDetail("Level was modded", () -> Boolean.toString(this.wasModded()));
        crashReportCategory.setDetail("Level storage version", () -> {
            int i = this.getVersion();
            return String.format("0x%05X - %s", i, this.getStorageVersionName(i));
        });
    }

    default public String getStorageVersionName(int i) {
        switch (i) {
            case 19133: {
                return "Anvil";
            }
            case 19132: {
                return "McRegion";
            }
        }
        return "Unknown?";
    }

    @Nullable
    public CompoundTag getCustomBossEvents();

    public void setCustomBossEvents(@Nullable CompoundTag var1);

    public ServerLevelData overworldData();

    public LevelSettings getLevelSettings();

    public CompoundTag createTag(RegistryAccess var1, @Nullable CompoundTag var2);

    public boolean isHardcore();

    public int getVersion();

    public String getLevelName();

    public GameType getGameType();

    public void setGameType(GameType var1);

    public boolean getAllowCommands();

    public Difficulty getDifficulty();

    public void setDifficulty(Difficulty var1);

    public boolean isDifficultyLocked();

    public void setDifficultyLocked(boolean var1);

    public GameRules getGameRules();

    public CompoundTag getLoadedPlayerTag();

    public CompoundTag endDragonFightData();

    public void setEndDragonFightData(CompoundTag var1);

    public WorldGenSettings worldGenSettings();

    public Lifecycle worldGenSettingsLifecycle();
}

