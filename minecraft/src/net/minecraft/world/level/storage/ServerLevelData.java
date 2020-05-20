package net.minecraft.world.level.storage;

import java.util.UUID;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.timers.TimerQueue;

public interface ServerLevelData extends WritableLevelData {
	String getLevelName();

	void setThundering(boolean bl);

	int getRainTime();

	void setRainTime(int i);

	void setThunderTime(int i);

	int getThunderTime();

	@Override
	default void fillCrashReportCategory(CrashReportCategory crashReportCategory) {
		WritableLevelData.super.fillCrashReportCategory(crashReportCategory);
		crashReportCategory.setDetail("Level name", this::getLevelName);
		crashReportCategory.setDetail(
			"Level game mode",
			(CrashReportDetail<String>)(() -> String.format(
					"Game mode: %s (ID %d). Hardcore: %b. Cheats: %b", this.getGameType().getName(), this.getGameType().getId(), this.isHardcore(), this.getAllowCommands()
				))
		);
		crashReportCategory.setDetail(
			"Level weather",
			(CrashReportDetail<String>)(() -> String.format(
					"Rain time: %d (now: %b), thunder time: %d (now: %b)", this.getRainTime(), this.isRaining(), this.getThunderTime(), this.isThundering()
				))
		);
	}

	int getClearWeatherTime();

	void setClearWeatherTime(int i);

	int getWanderingTraderSpawnDelay();

	void setWanderingTraderSpawnDelay(int i);

	int getWanderingTraderSpawnChance();

	void setWanderingTraderSpawnChance(int i);

	void setWanderingTraderId(UUID uUID);

	GameType getGameType();

	void setWorldBorder(WorldBorder.Settings settings);

	WorldBorder.Settings getWorldBorder();

	boolean isInitialized();

	void setInitialized(boolean bl);

	boolean getAllowCommands();

	void setGameType(GameType gameType);

	TimerQueue<MinecraftServer> getScheduledEvents();

	void setGameTime(long l);

	void setDayTime(long l);
}
