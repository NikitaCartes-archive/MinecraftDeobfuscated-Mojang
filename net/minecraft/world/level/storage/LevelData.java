/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage;

import com.google.common.hash.Hashing;
import java.util.UUID;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.ChunkGeneratorProvider;
import net.minecraft.world.level.timers.TimerQueue;

public interface LevelData {
    public long getSeed();

    public static long obfuscateSeed(long l) {
        return Hashing.sha256().hashLong(l).asLong();
    }

    public int getXSpawn();

    public void setXSpawn(int var1);

    public int getYSpawn();

    public void setYSpawn(int var1);

    public int getZSpawn();

    public void setZSpawn(int var1);

    default public void setSpawn(BlockPos blockPos) {
        this.setXSpawn(blockPos.getX());
        this.setYSpawn(blockPos.getY());
        this.setZSpawn(blockPos.getZ());
    }

    public long getGameTime();

    public void setGameTime(long var1);

    public long getDayTime();

    public void setDayTime(long var1);

    public String getLevelName();

    public int getClearWeatherTime();

    public void setClearWeatherTime(int var1);

    public boolean isThundering();

    public void setThundering(boolean var1);

    public int getThunderTime();

    public void setThunderTime(int var1);

    public boolean isRaining();

    public void setRaining(boolean var1);

    public int getRainTime();

    public void setRainTime(int var1);

    public GameType getGameType();

    public boolean shouldGenerateMapFeatures();

    public void setGameType(GameType var1);

    public boolean isHardcore();

    public LevelType getGeneratorType();

    public ChunkGeneratorProvider getGeneratorProvider();

    public boolean getAllowCommands();

    public boolean isInitialized();

    public void setInitialized(boolean var1);

    public GameRules getGameRules();

    public WorldBorder.Settings getWorldBorder();

    public void setWorldBorder(WorldBorder.Settings var1);

    public Difficulty getDifficulty();

    public boolean isDifficultyLocked();

    public TimerQueue<MinecraftServer> getScheduledEvents();

    default public void fillCrashReportCategory(CrashReportCategory crashReportCategory) {
        crashReportCategory.setDetail("Level name", this::getLevelName);
        crashReportCategory.setDetail("Level seed", () -> String.valueOf(this.getSeed()));
        crashReportCategory.setDetail("Level generator", () -> {
            LevelType levelType = this.getGeneratorProvider().getType();
            return String.format("ID %02d - %s, ver %d. Features enabled: %b", levelType.getId(), levelType.getName(), levelType.getVersion(), this.shouldGenerateMapFeatures());
        });
        crashReportCategory.setDetail("Level generator options", () -> this.getGeneratorProvider().getSettings().toString());
        crashReportCategory.setDetail("Level spawn location", () -> CrashReportCategory.formatLocation(this.getXSpawn(), this.getYSpawn(), this.getZSpawn()));
        crashReportCategory.setDetail("Level time", () -> String.format("%d game time, %d day time", this.getGameTime(), this.getDayTime()));
        crashReportCategory.setDetail("Level weather", () -> String.format("Rain time: %d (now: %b), thunder time: %d (now: %b)", this.getRainTime(), this.isRaining(), this.getThunderTime(), this.isThundering()));
        crashReportCategory.setDetail("Level game mode", () -> String.format("Game mode: %s (ID %d). Hardcore: %b. Cheats: %b", this.getGameType().getName(), this.getGameType().getId(), this.isHardcore(), this.getAllowCommands()));
    }

    public CompoundTag getDimensionData();

    public void setDimensionData(CompoundTag var1);

    public int getWanderingTraderSpawnDelay();

    public void setWanderingTraderSpawnDelay(int var1);

    public int getWanderingTraderSpawnChance();

    public void setWanderingTraderSpawnChance(int var1);

    public void setWanderingTraderId(UUID var1);
}

