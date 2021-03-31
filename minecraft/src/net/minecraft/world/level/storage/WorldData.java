package net.minecraft.world.level.storage;

import com.mojang.serialization.Lifecycle;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;

public interface WorldData {
	int ANVIL_VERSION_ID = 19133;
	int MCREGION_VERSION_ID = 19132;

	DataPackConfig getDataPackConfig();

	void setDataPackConfig(DataPackConfig dataPackConfig);

	boolean wasModded();

	Set<String> getKnownServerBrands();

	void setModdedInfo(String string, boolean bl);

	default void fillCrashReportCategory(CrashReportCategory crashReportCategory) {
		crashReportCategory.setDetail("Known server brands", (CrashReportDetail<String>)(() -> String.join(", ", this.getKnownServerBrands())));
		crashReportCategory.setDetail("Level was modded", (CrashReportDetail<String>)(() -> Boolean.toString(this.wasModded())));
		crashReportCategory.setDetail("Level storage version", (CrashReportDetail<String>)(() -> {
			int i = this.getVersion();
			return String.format("0x%05X - %s", i, this.getStorageVersionName(i));
		}));
	}

	default String getStorageVersionName(int i) {
		switch (i) {
			case 19132:
				return "McRegion";
			case 19133:
				return "Anvil";
			default:
				return "Unknown?";
		}
	}

	@Nullable
	CompoundTag getCustomBossEvents();

	void setCustomBossEvents(@Nullable CompoundTag compoundTag);

	ServerLevelData overworldData();

	LevelSettings getLevelSettings();

	CompoundTag createTag(RegistryAccess registryAccess, @Nullable CompoundTag compoundTag);

	boolean isHardcore();

	int getVersion();

	String getLevelName();

	GameType getGameType();

	void setGameType(GameType gameType);

	boolean getAllowCommands();

	Difficulty getDifficulty();

	void setDifficulty(Difficulty difficulty);

	boolean isDifficultyLocked();

	void setDifficultyLocked(boolean bl);

	GameRules getGameRules();

	CompoundTag getLoadedPlayerTag();

	CompoundTag endDragonFightData();

	void setEndDragonFightData(CompoundTag compoundTag);

	WorldGenSettings worldGenSettings();

	Lifecycle worldGenSettingsLifecycle();
}
