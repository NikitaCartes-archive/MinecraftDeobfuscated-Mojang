/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage;

import net.minecraft.CrashReportCategory;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;

public interface LevelData {
    public int getXSpawn();

    public int getYSpawn();

    public int getZSpawn();

    public long getGameTime();

    public long getDayTime();

    public boolean isThundering();

    public boolean isRaining();

    public void setRaining(boolean var1);

    public boolean isHardcore();

    public GameRules getGameRules();

    public Difficulty getDifficulty();

    public boolean isDifficultyLocked();

    default public void fillCrashReportCategory(CrashReportCategory crashReportCategory) {
        crashReportCategory.setDetail("Level spawn location", () -> CrashReportCategory.formatLocation(this.getXSpawn(), this.getYSpawn(), this.getZSpawn()));
        crashReportCategory.setDetail("Level time", () -> String.format("%d game time, %d day time", this.getGameTime(), this.getDayTime()));
    }
}

