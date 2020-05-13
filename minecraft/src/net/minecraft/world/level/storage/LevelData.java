package net.minecraft.world.level.storage;

import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;

public interface LevelData {
	int getXSpawn();

	int getYSpawn();

	int getZSpawn();

	long getGameTime();

	long getDayTime();

	boolean isThundering();

	boolean isRaining();

	void setRaining(boolean bl);

	boolean isHardcore();

	GameRules getGameRules();

	Difficulty getDifficulty();

	boolean isDifficultyLocked();

	default void fillCrashReportCategory(CrashReportCategory crashReportCategory) {
		crashReportCategory.setDetail(
			"Level spawn location", (CrashReportDetail<String>)(() -> CrashReportCategory.formatLocation(this.getXSpawn(), this.getYSpawn(), this.getZSpawn()))
		);
		crashReportCategory.setDetail(
			"Level time", (CrashReportDetail<String>)(() -> String.format("%d game time, %d day time", this.getGameTime(), this.getDayTime()))
		);
	}
}
