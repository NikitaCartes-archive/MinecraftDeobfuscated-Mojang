/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage;

import com.google.common.hash.Hashing;
import net.minecraft.CrashReportCategory;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.levelgen.ChunkGeneratorProvider;

public interface LevelData {
    public long getSeed();

    public static long obfuscateSeed(long l) {
        return Hashing.sha256().hashLong(l).asLong();
    }

    public int getXSpawn();

    public int getYSpawn();

    public int getZSpawn();

    public long getGameTime();

    public long getDayTime();

    public boolean isThundering();

    public boolean isRaining();

    public void setRaining(boolean var1);

    public boolean isHardcore();

    public LevelType getGeneratorType();

    public ChunkGeneratorProvider getGeneratorProvider();

    public GameRules getGameRules();

    public Difficulty getDifficulty();

    public boolean isDifficultyLocked();

    default public void fillCrashReportCategory(CrashReportCategory crashReportCategory) {
        crashReportCategory.setDetail("Level seed", () -> String.valueOf(this.getSeed()));
        crashReportCategory.setDetail("Level generator options", () -> this.getGeneratorProvider().getSettings().toString());
        crashReportCategory.setDetail("Level spawn location", () -> CrashReportCategory.formatLocation(this.getXSpawn(), this.getYSpawn(), this.getZSpawn()));
        crashReportCategory.setDetail("Level time", () -> String.format("%d game time, %d day time", this.getGameTime(), this.getDayTime()));
    }
}

