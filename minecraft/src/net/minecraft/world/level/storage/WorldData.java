package net.minecraft.world.level.storage;

import com.mojang.serialization.Lifecycle;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Difficulty;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;

public interface WorldData {
	int ANVIL_VERSION_ID = 19133;
	int MCREGION_VERSION_ID = 19132;

	WorldDataConfiguration getDataConfiguration();

	void setDataConfiguration(WorldDataConfiguration worldDataConfiguration);

	boolean wasModded();

	Set<String> getKnownServerBrands();

	Set<String> getRemovedFeatureFlags();

	void setModdedInfo(String string, boolean bl);

	default void fillCrashReportCategory(CrashReportCategory crashReportCategory) {
		crashReportCategory.setDetail("Known server brands", (CrashReportDetail<String>)(() -> String.join(", ", this.getKnownServerBrands())));
		crashReportCategory.setDetail("Removed feature flags", (CrashReportDetail<String>)(() -> String.join(", ", this.getRemovedFeatureFlags())));
		crashReportCategory.setDetail("Level was modded", (CrashReportDetail<String>)(() -> Boolean.toString(this.wasModded())));
		crashReportCategory.setDetail("Level storage version", (CrashReportDetail<String>)(() -> {
			int i = this.getVersion();
			return String.format(Locale.ROOT, "0x%05X - %s", i, this.getStorageVersionName(i));
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

	@Nullable
	CompoundTag getLoadedPlayerTag();

	CompoundTag endDragonFightData();

	void setEndDragonFightData(CompoundTag compoundTag);

	WorldOptions worldGenOptions();

	boolean isFlatWorld();

	boolean isDebugWorld();

	Lifecycle worldGenSettingsLifecycle();

	default FeatureFlagSet enabledFeatures() {
		return this.getDataConfiguration().enabledFeatures();
	}
}
