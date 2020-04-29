package net.minecraft.world.level.storage;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.Dynamic;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReportCategory;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.ChunkGeneratorProvider;
import net.minecraft.world.level.timers.TimerCallbacks;
import net.minecraft.world.level.timers.TimerQueue;

public class PrimaryLevelData implements ServerLevelData, WorldData {
	private final String minecraftVersionName;
	private final int minecraftVersion;
	private final boolean snapshot;
	private final long seed;
	private final ChunkGeneratorProvider generatorProvider;
	@Nullable
	private String legacyCustomOptions;
	private int xSpawn;
	private int ySpawn;
	private int zSpawn;
	private long gameTime;
	private long dayTime;
	private long lastPlayed;
	private long sizeOnDisk;
	@Nullable
	private final DataFixer fixerUpper;
	private final int playerDataVersion;
	private boolean upgradedPlayerTag;
	private CompoundTag loadedPlayerTag;
	private final String levelName;
	private final int version;
	private int clearWeatherTime;
	private boolean raining;
	private int rainTime;
	private boolean thundering;
	private int thunderTime;
	private GameType gameType;
	private final boolean generateMapFeatures;
	private final boolean hardcore;
	private final boolean allowCommands;
	private final boolean generateBonusChest;
	private boolean initialized;
	private Difficulty difficulty = Difficulty.NORMAL;
	private boolean difficultyLocked;
	private WorldBorder.Settings worldBorder = WorldBorder.DEFAULT_SETTINGS;
	private final Set<String> disabledDataPacks = Sets.<String>newHashSet();
	private final Set<String> enabledDataPacks = Sets.<String>newLinkedHashSet();
	private final Map<DimensionType, CompoundTag> dimensionData = Maps.<DimensionType, CompoundTag>newIdentityHashMap();
	@Nullable
	private CompoundTag customBossEvents;
	private int wanderingTraderSpawnDelay;
	private int wanderingTraderSpawnChance;
	private UUID wanderingTraderId;
	private Set<String> knownServerBrands = Sets.<String>newLinkedHashSet();
	private boolean wasModded;
	private final GameRules gameRules = new GameRules();
	private final TimerQueue<MinecraftServer> scheduledEvents = new TimerQueue<>(TimerCallbacks.SERVER_CALLBACKS);

	public PrimaryLevelData(CompoundTag compoundTag, DataFixer dataFixer, int i, @Nullable CompoundTag compoundTag2) {
		this.fixerUpper = dataFixer;
		ListTag listTag = compoundTag.getList("ServerBrands", 8);

		for (int j = 0; j < listTag.size(); j++) {
			this.knownServerBrands.add(listTag.getString(j));
		}

		this.wasModded = compoundTag.getBoolean("WasModded");
		if (compoundTag.contains("Version", 10)) {
			CompoundTag compoundTag3 = compoundTag.getCompound("Version");
			this.minecraftVersionName = compoundTag3.getString("Name");
			this.minecraftVersion = compoundTag3.getInt("Id");
			this.snapshot = compoundTag3.getBoolean("Snapshot");
		} else {
			this.minecraftVersionName = SharedConstants.getCurrentVersion().getName();
			this.minecraftVersion = SharedConstants.getCurrentVersion().getWorldVersion();
			this.snapshot = !SharedConstants.getCurrentVersion().isStable();
		}

		this.seed = compoundTag.getLong("RandomSeed");
		if (compoundTag.contains("generatorName", 8)) {
			String string = compoundTag.getString("generatorName");
			LevelType levelType = LevelType.getLevelType(string);
			if (levelType == null) {
				levelType = LevelType.NORMAL;
			} else if (levelType == LevelType.CUSTOMIZED) {
				this.legacyCustomOptions = compoundTag.getString("generatorOptions");
			} else if (levelType.hasReplacement()) {
				int k = 0;
				if (compoundTag.contains("generatorVersion", 99)) {
					k = compoundTag.getInt("generatorVersion");
				}

				levelType = levelType.getReplacementForVersion(k);
			}

			CompoundTag compoundTag4 = compoundTag.getCompound("generatorOptions");
			Dynamic<?> dynamic = new Dynamic<>(NbtOps.INSTANCE, compoundTag4);
			Dynamic<?> dynamic2 = datafixGeneratorOptions(levelType, dynamic, i, dataFixer);
			this.generatorProvider = levelType.createProvider(dynamic2);
		} else {
			this.generatorProvider = LevelType.NORMAL.getDefaultProvider();
		}

		this.gameType = GameType.byId(compoundTag.getInt("GameType"));
		if (compoundTag.contains("legacy_custom_options", 8)) {
			this.legacyCustomOptions = compoundTag.getString("legacy_custom_options");
		}

		if (compoundTag.contains("MapFeatures", 99)) {
			this.generateMapFeatures = compoundTag.getBoolean("MapFeatures");
		} else {
			this.generateMapFeatures = true;
		}

		this.xSpawn = compoundTag.getInt("SpawnX");
		this.ySpawn = compoundTag.getInt("SpawnY");
		this.zSpawn = compoundTag.getInt("SpawnZ");
		this.gameTime = compoundTag.getLong("Time");
		if (compoundTag.contains("DayTime", 99)) {
			this.dayTime = compoundTag.getLong("DayTime");
		} else {
			this.dayTime = this.gameTime;
		}

		this.lastPlayed = compoundTag.getLong("LastPlayed");
		this.sizeOnDisk = compoundTag.getLong("SizeOnDisk");
		this.levelName = compoundTag.getString("LevelName");
		this.version = compoundTag.getInt("version");
		this.clearWeatherTime = compoundTag.getInt("clearWeatherTime");
		this.rainTime = compoundTag.getInt("rainTime");
		this.raining = compoundTag.getBoolean("raining");
		this.thunderTime = compoundTag.getInt("thunderTime");
		this.thundering = compoundTag.getBoolean("thundering");
		this.hardcore = compoundTag.getBoolean("hardcore");
		if (compoundTag.contains("initialized", 99)) {
			this.initialized = compoundTag.getBoolean("initialized");
		} else {
			this.initialized = true;
		}

		if (compoundTag.contains("allowCommands", 99)) {
			this.allowCommands = compoundTag.getBoolean("allowCommands");
		} else {
			this.allowCommands = this.gameType == GameType.CREATIVE;
		}

		this.generateBonusChest = compoundTag.getBoolean("BonusChest");
		this.playerDataVersion = i;
		if (compoundTag2 != null) {
			this.loadedPlayerTag = compoundTag2;
		}

		if (compoundTag.contains("GameRules", 10)) {
			this.gameRules.loadFromTag(compoundTag.getCompound("GameRules"));
		}

		if (compoundTag.contains("Difficulty", 99)) {
			this.difficulty = Difficulty.byId(compoundTag.getByte("Difficulty"));
		}

		if (compoundTag.contains("DifficultyLocked", 1)) {
			this.difficultyLocked = compoundTag.getBoolean("DifficultyLocked");
		}

		this.worldBorder = WorldBorder.Settings.read(compoundTag, WorldBorder.DEFAULT_SETTINGS);
		if (compoundTag.contains("DimensionData", 10)) {
			CompoundTag compoundTag3 = compoundTag.getCompound("DimensionData");

			for (String string2 : compoundTag3.getAllKeys()) {
				this.dimensionData.put(DimensionType.getById(Integer.parseInt(string2)), compoundTag3.getCompound(string2));
			}
		}

		if (compoundTag.contains("DataPacks", 10)) {
			CompoundTag compoundTag3 = compoundTag.getCompound("DataPacks");
			ListTag listTag2 = compoundTag3.getList("Disabled", 8);

			for (int k = 0; k < listTag2.size(); k++) {
				this.disabledDataPacks.add(listTag2.getString(k));
			}

			ListTag listTag3 = compoundTag3.getList("Enabled", 8);

			for (int l = 0; l < listTag3.size(); l++) {
				this.enabledDataPacks.add(listTag3.getString(l));
			}
		}

		if (compoundTag.contains("CustomBossEvents", 10)) {
			this.customBossEvents = compoundTag.getCompound("CustomBossEvents");
		}

		if (compoundTag.contains("ScheduledEvents", 9)) {
			this.scheduledEvents.load(compoundTag.getList("ScheduledEvents", 10));
		}

		if (compoundTag.contains("WanderingTraderSpawnDelay", 99)) {
			this.wanderingTraderSpawnDelay = compoundTag.getInt("WanderingTraderSpawnDelay");
		}

		if (compoundTag.contains("WanderingTraderSpawnChance", 99)) {
			this.wanderingTraderSpawnChance = compoundTag.getInt("WanderingTraderSpawnChance");
		}

		if (compoundTag.hasUUID("WanderingTraderId")) {
			this.wanderingTraderId = compoundTag.getUUID("WanderingTraderId");
		}
	}

	private static <T> Dynamic<T> datafixGeneratorOptions(LevelType levelType, Dynamic<T> dynamic, int i, DataFixer dataFixer) {
		int j = Math.max(i, 2501);
		Dynamic<T> dynamic2 = dynamic.merge(dynamic.createString("levelType"), dynamic.createString(levelType.getSerialization()));
		return dataFixer.update(References.CHUNK_GENERATOR_SETTINGS, dynamic2, j, SharedConstants.getCurrentVersion().getWorldVersion()).remove("levelType");
	}

	public PrimaryLevelData(LevelSettings levelSettings) {
		this.fixerUpper = null;
		this.playerDataVersion = SharedConstants.getCurrentVersion().getWorldVersion();
		this.seed = levelSettings.getSeed();
		this.gameType = levelSettings.getGameType();
		this.difficulty = levelSettings.getDifficulty();
		this.generateMapFeatures = levelSettings.shouldGenerateMapFeatures();
		this.hardcore = levelSettings.isHardcore();
		this.generatorProvider = levelSettings.getGeneratorProvider();
		this.allowCommands = levelSettings.getAllowCommands();
		this.generateBonusChest = levelSettings.hasStartingBonusItems();
		this.levelName = levelSettings.getLevelName();
		this.version = 19133;
		this.initialized = false;
		this.minecraftVersionName = SharedConstants.getCurrentVersion().getName();
		this.minecraftVersion = SharedConstants.getCurrentVersion().getWorldVersion();
		this.snapshot = !SharedConstants.getCurrentVersion().isStable();
		this.gameRules.assignFrom(levelSettings.getGameRules(), null);
	}

	@Override
	public CompoundTag createTag(@Nullable CompoundTag compoundTag) {
		this.updatePlayerTag();
		if (compoundTag == null) {
			compoundTag = this.loadedPlayerTag;
		}

		CompoundTag compoundTag2 = new CompoundTag();
		this.setTagData(compoundTag2, compoundTag);
		return compoundTag2;
	}

	private void setTagData(CompoundTag compoundTag, CompoundTag compoundTag2) {
		ListTag listTag = new ListTag();
		this.knownServerBrands.stream().map(StringTag::valueOf).forEach(listTag::add);
		compoundTag.put("ServerBrands", listTag);
		compoundTag.putBoolean("WasModded", this.wasModded);
		CompoundTag compoundTag3 = new CompoundTag();
		compoundTag3.putString("Name", SharedConstants.getCurrentVersion().getName());
		compoundTag3.putInt("Id", SharedConstants.getCurrentVersion().getWorldVersion());
		compoundTag3.putBoolean("Snapshot", !SharedConstants.getCurrentVersion().isStable());
		compoundTag.put("Version", compoundTag3);
		compoundTag.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
		compoundTag.putLong("RandomSeed", this.seed);
		compoundTag.putString("generatorName", this.generatorProvider.getType().getSerialization());
		compoundTag.putInt("generatorVersion", this.generatorProvider.getType().getVersion());
		CompoundTag compoundTag4 = (CompoundTag)this.generatorProvider.getSettings().convert(NbtOps.INSTANCE).getValue();
		if (!compoundTag4.isEmpty()) {
			compoundTag.put("generatorOptions", compoundTag4);
		}

		if (this.legacyCustomOptions != null) {
			compoundTag.putString("legacy_custom_options", this.legacyCustomOptions);
		}

		compoundTag.putInt("GameType", this.gameType.getId());
		compoundTag.putBoolean("MapFeatures", this.generateMapFeatures);
		compoundTag.putInt("SpawnX", this.xSpawn);
		compoundTag.putInt("SpawnY", this.ySpawn);
		compoundTag.putInt("SpawnZ", this.zSpawn);
		compoundTag.putLong("Time", this.gameTime);
		compoundTag.putLong("DayTime", this.dayTime);
		compoundTag.putLong("SizeOnDisk", this.sizeOnDisk);
		compoundTag.putLong("LastPlayed", Util.getEpochMillis());
		compoundTag.putString("LevelName", this.levelName);
		compoundTag.putInt("version", 19133);
		compoundTag.putInt("clearWeatherTime", this.clearWeatherTime);
		compoundTag.putInt("rainTime", this.rainTime);
		compoundTag.putBoolean("raining", this.raining);
		compoundTag.putInt("thunderTime", this.thunderTime);
		compoundTag.putBoolean("thundering", this.thundering);
		compoundTag.putBoolean("hardcore", this.hardcore);
		compoundTag.putBoolean("allowCommands", this.allowCommands);
		compoundTag.putBoolean("BonusChest", this.generateBonusChest);
		compoundTag.putBoolean("initialized", this.initialized);
		this.worldBorder.write(compoundTag);
		compoundTag.putByte("Difficulty", (byte)this.difficulty.getId());
		compoundTag.putBoolean("DifficultyLocked", this.difficultyLocked);
		compoundTag.put("GameRules", this.gameRules.createTag());
		CompoundTag compoundTag5 = new CompoundTag();

		for (Entry<DimensionType, CompoundTag> entry : this.dimensionData.entrySet()) {
			compoundTag5.put(String.valueOf(((DimensionType)entry.getKey()).getId()), (Tag)entry.getValue());
		}

		compoundTag.put("DimensionData", compoundTag5);
		if (compoundTag2 != null) {
			compoundTag.put("Player", compoundTag2);
		}

		CompoundTag compoundTag6 = new CompoundTag();
		ListTag listTag2 = new ListTag();

		for (String string : this.enabledDataPacks) {
			listTag2.add(StringTag.valueOf(string));
		}

		compoundTag6.put("Enabled", listTag2);
		ListTag listTag3 = new ListTag();

		for (String string2 : this.disabledDataPacks) {
			listTag3.add(StringTag.valueOf(string2));
		}

		compoundTag6.put("Disabled", listTag3);
		compoundTag.put("DataPacks", compoundTag6);
		if (this.customBossEvents != null) {
			compoundTag.put("CustomBossEvents", this.customBossEvents);
		}

		compoundTag.put("ScheduledEvents", this.scheduledEvents.store());
		compoundTag.putInt("WanderingTraderSpawnDelay", this.wanderingTraderSpawnDelay);
		compoundTag.putInt("WanderingTraderSpawnChance", this.wanderingTraderSpawnChance);
		if (this.wanderingTraderId != null) {
			compoundTag.putUUID("WanderingTraderId", this.wanderingTraderId);
		}
	}

	@Override
	public long getSeed() {
		return this.seed;
	}

	@Override
	public int getXSpawn() {
		return this.xSpawn;
	}

	@Override
	public int getYSpawn() {
		return this.ySpawn;
	}

	@Override
	public int getZSpawn() {
		return this.zSpawn;
	}

	@Override
	public long getGameTime() {
		return this.gameTime;
	}

	@Override
	public long getDayTime() {
		return this.dayTime;
	}

	private void updatePlayerTag() {
		if (!this.upgradedPlayerTag && this.loadedPlayerTag != null) {
			if (this.playerDataVersion < SharedConstants.getCurrentVersion().getWorldVersion()) {
				if (this.fixerUpper == null) {
					throw (NullPointerException)Util.pauseInIde(new NullPointerException("Fixer Upper not set inside LevelData, and the player tag is not upgraded."));
				}

				this.loadedPlayerTag = NbtUtils.update(this.fixerUpper, DataFixTypes.PLAYER, this.loadedPlayerTag, this.playerDataVersion);
			}

			this.upgradedPlayerTag = true;
		}
	}

	@Override
	public CompoundTag getLoadedPlayerTag() {
		this.updatePlayerTag();
		return this.loadedPlayerTag;
	}

	@Override
	public void setXSpawn(int i) {
		this.xSpawn = i;
	}

	@Override
	public void setYSpawn(int i) {
		this.ySpawn = i;
	}

	@Override
	public void setZSpawn(int i) {
		this.zSpawn = i;
	}

	@Override
	public void setGameTime(long l) {
		this.gameTime = l;
	}

	@Override
	public void setDayTime(long l) {
		this.dayTime = l;
	}

	@Override
	public void setSpawn(BlockPos blockPos) {
		this.xSpawn = blockPos.getX();
		this.ySpawn = blockPos.getY();
		this.zSpawn = blockPos.getZ();
	}

	@Override
	public String getLevelName() {
		return this.levelName;
	}

	@Override
	public int getVersion() {
		return this.version;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public long getLastPlayed() {
		return this.lastPlayed;
	}

	@Override
	public int getClearWeatherTime() {
		return this.clearWeatherTime;
	}

	@Override
	public void setClearWeatherTime(int i) {
		this.clearWeatherTime = i;
	}

	@Override
	public boolean isThundering() {
		return this.thundering;
	}

	@Override
	public void setThundering(boolean bl) {
		this.thundering = bl;
	}

	@Override
	public int getThunderTime() {
		return this.thunderTime;
	}

	@Override
	public void setThunderTime(int i) {
		this.thunderTime = i;
	}

	@Override
	public boolean isRaining() {
		return this.raining;
	}

	@Override
	public void setRaining(boolean bl) {
		this.raining = bl;
	}

	@Override
	public int getRainTime() {
		return this.rainTime;
	}

	@Override
	public void setRainTime(int i) {
		this.rainTime = i;
	}

	@Override
	public GameType getGameType() {
		return this.gameType;
	}

	@Override
	public boolean shouldGenerateMapFeatures() {
		return this.generateMapFeatures;
	}

	@Override
	public void setGameType(GameType gameType) {
		this.gameType = gameType;
	}

	@Override
	public boolean isHardcore() {
		return this.hardcore;
	}

	@Override
	public LevelType getGeneratorType() {
		return this.generatorProvider.getType();
	}

	@Override
	public ChunkGeneratorProvider getGeneratorProvider() {
		return this.generatorProvider;
	}

	@Override
	public boolean getAllowCommands() {
		return this.allowCommands;
	}

	@Override
	public boolean isInitialized() {
		return this.initialized;
	}

	@Override
	public void setInitialized(boolean bl) {
		this.initialized = bl;
	}

	@Override
	public GameRules getGameRules() {
		return this.gameRules;
	}

	@Override
	public WorldBorder.Settings getWorldBorder() {
		return this.worldBorder;
	}

	@Override
	public void setWorldBorder(WorldBorder.Settings settings) {
		this.worldBorder = settings;
	}

	@Override
	public Difficulty getDifficulty() {
		return this.difficulty;
	}

	@Override
	public void setDifficulty(Difficulty difficulty) {
		this.difficulty = difficulty;
	}

	@Override
	public boolean isDifficultyLocked() {
		return this.difficultyLocked;
	}

	@Override
	public void setDifficultyLocked(boolean bl) {
		this.difficultyLocked = bl;
	}

	@Override
	public TimerQueue<MinecraftServer> getScheduledEvents() {
		return this.scheduledEvents;
	}

	@Override
	public void fillCrashReportCategory(CrashReportCategory crashReportCategory) {
		ServerLevelData.super.fillCrashReportCategory(crashReportCategory);
		WorldData.super.fillCrashReportCategory(crashReportCategory);
	}

	@Override
	public CompoundTag getDimensionData(DimensionType dimensionType) {
		CompoundTag compoundTag = (CompoundTag)this.dimensionData.get(dimensionType);
		return compoundTag == null ? new CompoundTag() : compoundTag;
	}

	@Override
	public void setDimensionData(DimensionType dimensionType, CompoundTag compoundTag) {
		this.dimensionData.put(dimensionType, compoundTag);
	}

	@Override
	public CompoundTag getDimensionData() {
		return this.getDimensionData(DimensionType.OVERWORLD);
	}

	@Override
	public void setDimensionData(CompoundTag compoundTag) {
		this.setDimensionData(DimensionType.OVERWORLD, compoundTag);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public int getMinecraftVersion() {
		return this.minecraftVersion;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean isSnapshot() {
		return this.snapshot;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public String getMinecraftVersionName() {
		return this.minecraftVersionName;
	}

	@Override
	public Set<String> getDisabledDataPacks() {
		return this.disabledDataPacks;
	}

	@Override
	public Set<String> getEnabledDataPacks() {
		return this.enabledDataPacks;
	}

	@Nullable
	@Override
	public CompoundTag getCustomBossEvents() {
		return this.customBossEvents;
	}

	@Override
	public void setCustomBossEvents(@Nullable CompoundTag compoundTag) {
		this.customBossEvents = compoundTag;
	}

	@Override
	public int getWanderingTraderSpawnDelay() {
		return this.wanderingTraderSpawnDelay;
	}

	@Override
	public void setWanderingTraderSpawnDelay(int i) {
		this.wanderingTraderSpawnDelay = i;
	}

	@Override
	public int getWanderingTraderSpawnChance() {
		return this.wanderingTraderSpawnChance;
	}

	@Override
	public void setWanderingTraderSpawnChance(int i) {
		this.wanderingTraderSpawnChance = i;
	}

	@Override
	public void setWanderingTraderId(UUID uUID) {
		this.wanderingTraderId = uUID;
	}

	@Override
	public void setModdedInfo(String string, boolean bl) {
		this.knownServerBrands.add(string);
		this.wasModded |= bl;
	}

	@Override
	public boolean wasModded() {
		return this.wasModded;
	}

	@Override
	public Set<String> getKnownServerBrands() {
		return ImmutableSet.copyOf(this.knownServerBrands);
	}

	@Override
	public ServerLevelData overworldData() {
		return this;
	}

	@Override
	public LevelSettings getLevelSettings() {
		LevelSettings levelSettings = new LevelSettings(
			this.levelName, this.seed, this.gameType, this.generateMapFeatures, this.hardcore, this.difficulty, this.generatorProvider, this.gameRules.copy()
		);
		if (this.generateBonusChest) {
			levelSettings = levelSettings.enableStartingBonusItems();
		}

		if (this.allowCommands) {
			levelSettings = levelSettings.enableSinglePlayerCommands();
		}

		return levelSettings;
	}
}
