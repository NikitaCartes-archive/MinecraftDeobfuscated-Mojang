package net.minecraft.world.level.storage;

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
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.ChunkGeneratorProvider;
import net.minecraft.world.level.timers.TimerQueue;

public class DerivedLevelData implements ServerLevelData {
	private final DimensionType dimensionType;
	private final WorldData worldData;
	private final ServerLevelData wrapped;

	public DerivedLevelData(DimensionType dimensionType, WorldData worldData, ServerLevelData serverLevelData) {
		this.dimensionType = dimensionType;
		this.worldData = worldData;
		this.wrapped = serverLevelData;
	}

	@Override
	public long getSeed() {
		return this.worldData.getSeed();
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
	public String getLevelName() {
		return this.worldData.getLevelName();
	}

	@Override
	public int getClearWeatherTime() {
		return this.wrapped.getClearWeatherTime();
	}

	@Override
	public void setClearWeatherTime(int i) {
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
		return this.worldData.getGameType();
	}

	@Override
	public void setXSpawn(int i) {
	}

	@Override
	public void setYSpawn(int i) {
	}

	@Override
	public void setZSpawn(int i) {
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
	public boolean shouldGenerateMapFeatures() {
		return this.wrapped.shouldGenerateMapFeatures();
	}

	@Override
	public void setGameType(GameType gameType) {
	}

	@Override
	public boolean isHardcore() {
		return this.worldData.isHardcore();
	}

	@Override
	public LevelType getGeneratorType() {
		return this.wrapped.getGeneratorType();
	}

	@Override
	public ChunkGeneratorProvider getGeneratorProvider() {
		return this.wrapped.getGeneratorProvider();
	}

	@Override
	public boolean getAllowCommands() {
		return this.worldData.getAllowCommands();
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
		return this.worldData.getGameRules();
	}

	@Override
	public WorldBorder.Settings getWorldBorder() {
		return this.wrapped.getWorldBorder();
	}

	@Override
	public void setWorldBorder(WorldBorder.Settings settings) {
	}

	@Override
	public Difficulty getDifficulty() {
		return this.worldData.getDifficulty();
	}

	@Override
	public boolean isDifficultyLocked() {
		return this.worldData.isDifficultyLocked();
	}

	@Override
	public TimerQueue<MinecraftServer> getScheduledEvents() {
		return this.wrapped.getScheduledEvents();
	}

	@Override
	public void setDimensionData(CompoundTag compoundTag) {
		this.worldData.setDimensionData(this.dimensionType, compoundTag);
	}

	@Override
	public CompoundTag getDimensionData() {
		return this.worldData.getDimensionData(this.dimensionType);
	}

	@Override
	public int getWanderingTraderSpawnDelay() {
		return 0;
	}

	@Override
	public void setWanderingTraderSpawnDelay(int i) {
	}

	@Override
	public int getWanderingTraderSpawnChance() {
		return 0;
	}

	@Override
	public void setWanderingTraderSpawnChance(int i) {
	}

	@Override
	public void setWanderingTraderId(UUID uUID) {
	}

	@Override
	public void fillCrashReportCategory(CrashReportCategory crashReportCategory) {
		crashReportCategory.setDetail("Derived", true);
		this.wrapped.fillCrashReportCategory(crashReportCategory);
	}
}
