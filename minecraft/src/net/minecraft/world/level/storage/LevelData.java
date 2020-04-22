package net.minecraft.world.level.storage;

import com.google.common.hash.Hashing;
import java.util.UUID;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
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
	long getSeed();

	static long obfuscateSeed(long l) {
		return Hashing.sha256().hashLong(l).asLong();
	}

	int getXSpawn();

	void setXSpawn(int i);

	int getYSpawn();

	void setYSpawn(int i);

	int getZSpawn();

	void setZSpawn(int i);

	default void setSpawn(BlockPos blockPos) {
		this.setXSpawn(blockPos.getX());
		this.setYSpawn(blockPos.getY());
		this.setZSpawn(blockPos.getZ());
	}

	long getGameTime();

	void setGameTime(long l);

	long getDayTime();

	void setDayTime(long l);

	String getLevelName();

	int getClearWeatherTime();

	void setClearWeatherTime(int i);

	boolean isThundering();

	void setThundering(boolean bl);

	int getThunderTime();

	void setThunderTime(int i);

	boolean isRaining();

	void setRaining(boolean bl);

	int getRainTime();

	void setRainTime(int i);

	GameType getGameType();

	boolean shouldGenerateMapFeatures();

	void setGameType(GameType gameType);

	boolean isHardcore();

	LevelType getGeneratorType();

	ChunkGeneratorProvider getGeneratorProvider();

	boolean getAllowCommands();

	boolean isInitialized();

	void setInitialized(boolean bl);

	GameRules getGameRules();

	WorldBorder.Settings getWorldBorder();

	void setWorldBorder(WorldBorder.Settings settings);

	Difficulty getDifficulty();

	boolean isDifficultyLocked();

	TimerQueue<MinecraftServer> getScheduledEvents();

	default void fillCrashReportCategory(CrashReportCategory crashReportCategory) {
		crashReportCategory.setDetail("Level name", this::getLevelName);
		crashReportCategory.setDetail("Level seed", (CrashReportDetail<String>)(() -> String.valueOf(this.getSeed())));
		crashReportCategory.setDetail(
			"Level generator",
			(CrashReportDetail<String>)(() -> {
				LevelType levelType = this.getGeneratorProvider().getType();
				return String.format(
					"ID %02d - %s, ver %d. Features enabled: %b", levelType.getId(), levelType.getName(), levelType.getVersion(), this.shouldGenerateMapFeatures()
				);
			})
		);
		crashReportCategory.setDetail("Level generator options", (CrashReportDetail<String>)(() -> this.getGeneratorProvider().getSettings().toString()));
		crashReportCategory.setDetail(
			"Level spawn location", (CrashReportDetail<String>)(() -> CrashReportCategory.formatLocation(this.getXSpawn(), this.getYSpawn(), this.getZSpawn()))
		);
		crashReportCategory.setDetail(
			"Level time", (CrashReportDetail<String>)(() -> String.format("%d game time, %d day time", this.getGameTime(), this.getDayTime()))
		);
		crashReportCategory.setDetail(
			"Level weather",
			(CrashReportDetail<String>)(() -> String.format(
					"Rain time: %d (now: %b), thunder time: %d (now: %b)", this.getRainTime(), this.isRaining(), this.getThunderTime(), this.isThundering()
				))
		);
		crashReportCategory.setDetail(
			"Level game mode",
			(CrashReportDetail<String>)(() -> String.format(
					"Game mode: %s (ID %d). Hardcore: %b. Cheats: %b", this.getGameType().getName(), this.getGameType().getId(), this.isHardcore(), this.getAllowCommands()
				))
		);
	}

	CompoundTag getDimensionData();

	void setDimensionData(CompoundTag compoundTag);

	int getWanderingTraderSpawnDelay();

	void setWanderingTraderSpawnDelay(int i);

	int getWanderingTraderSpawnChance();

	void setWanderingTraderSpawnChance(int i);

	void setWanderingTraderId(UUID uUID);
}
