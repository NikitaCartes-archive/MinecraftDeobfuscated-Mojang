package net.minecraft.world.level.storage;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReportCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.ChunkGeneratorProvider;
import net.minecraft.world.level.timers.TimerQueue;

public class DerivedLevelData extends LevelData {
	private final LevelData wrapped;

	public DerivedLevelData(LevelData levelData) {
		this.wrapped = levelData;
	}

	@Override
	public CompoundTag createTag(@Nullable CompoundTag compoundTag) {
		return this.wrapped.createTag(compoundTag);
	}

	@Override
	public long getSeed() {
		return this.wrapped.getSeed();
	}

	@Override
	public int getXSpawn() {
		return this.wrapped.getXSpawn();
	}

	@Override
	public int getYSpawn() {
		return this.wrapped.getYSpawn();
	}

	@Override
	public int getZSpawn() {
		return this.wrapped.getZSpawn();
	}

	@Override
	public long getGameTime() {
		return this.wrapped.getGameTime();
	}

	@Override
	public long getDayTime() {
		return this.wrapped.getDayTime();
	}

	@Override
	public CompoundTag getLoadedPlayerTag() {
		return this.wrapped.getLoadedPlayerTag();
	}

	@Override
	public String getLevelName() {
		return this.wrapped.getLevelName();
	}

	@Override
	public int getVersion() {
		return this.wrapped.getVersion();
	}

	@Environment(EnvType.CLIENT)
	@Override
	public long getLastPlayed() {
		return this.wrapped.getLastPlayed();
	}

	@Override
	public boolean isThundering() {
		return this.wrapped.isThundering();
	}

	@Override
	public int getThunderTime() {
		return this.wrapped.getThunderTime();
	}

	@Override
	public boolean isRaining() {
		return this.wrapped.isRaining();
	}

	@Override
	public int getRainTime() {
		return this.wrapped.getRainTime();
	}

	@Override
	public GameType getGameType() {
		return this.wrapped.getGameType();
	}

	@Override
	public void setGameTime(long l) {
	}

	@Override
	public void setDayTime(long l) {
	}

	@Override
	public void setSpawn(BlockPos blockPos) {
	}

	@Override
	public void setLevelName(String string) {
	}

	@Override
	public void setVersion(int i) {
	}

	@Override
	public void setThundering(boolean bl) {
	}

	@Override
	public void setThunderTime(int i) {
	}

	@Override
	public void setRaining(boolean bl) {
	}

	@Override
	public void setRainTime(int i) {
	}

	@Override
	public boolean isGenerateMapFeatures() {
		return this.wrapped.isGenerateMapFeatures();
	}

	@Override
	public boolean isHardcore() {
		return this.wrapped.isHardcore();
	}

	@Override
	public LevelType getGeneratorType() {
		return this.wrapped.getGeneratorType();
	}

	@Override
	public void setGeneratorProvider(ChunkGeneratorProvider chunkGeneratorProvider) {
	}

	@Override
	public boolean getAllowCommands() {
		return this.wrapped.getAllowCommands();
	}

	@Override
	public void setAllowCommands(boolean bl) {
	}

	@Override
	public boolean isInitialized() {
		return this.wrapped.isInitialized();
	}

	@Override
	public void setInitialized(boolean bl) {
	}

	@Override
	public GameRules getGameRules() {
		return this.wrapped.getGameRules();
	}

	@Override
	public Difficulty getDifficulty() {
		return this.wrapped.getDifficulty();
	}

	@Override
	public void setDifficulty(Difficulty difficulty) {
	}

	@Override
	public boolean isDifficultyLocked() {
		return this.wrapped.isDifficultyLocked();
	}

	@Override
	public void setDifficultyLocked(boolean bl) {
	}

	@Override
	public TimerQueue<MinecraftServer> getScheduledEvents() {
		return this.wrapped.getScheduledEvents();
	}

	@Override
	public void setDimensionData(DimensionType dimensionType, CompoundTag compoundTag) {
		this.wrapped.setDimensionData(dimensionType, compoundTag);
	}

	@Override
	public CompoundTag getDimensionData(DimensionType dimensionType) {
		return this.wrapped.getDimensionData(dimensionType);
	}

	@Override
	public void fillCrashReportCategory(CrashReportCategory crashReportCategory) {
		crashReportCategory.setDetail("Derived", true);
		this.wrapped.fillCrashReportCategory(crashReportCategory);
	}
}
