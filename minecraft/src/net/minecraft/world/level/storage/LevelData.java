package net.minecraft.world.level.storage;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
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
import net.minecraft.CrashReportDetail;
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
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.ChunkGeneratorProvider;
import net.minecraft.world.level.timers.TimerCallbacks;
import net.minecraft.world.level.timers.TimerQueue;

public class LevelData {
	private String minecraftVersionName;
	private int minecraftVersion;
	private boolean snapshot;
	public static final Difficulty DEFAULT_DIFFICULTY = Difficulty.NORMAL;
	private long seed;
	private ChunkGeneratorProvider generatorProvider = LevelType.NORMAL.getDefaultProvider();
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
	private String levelName;
	private int version;
	private int clearWeatherTime;
	private boolean raining;
	private int rainTime;
	private boolean thundering;
	private int thunderTime;
	private GameType gameType;
	private boolean generateMapFeatures;
	private boolean hardcore;
	private boolean allowCommands;
	private boolean initialized;
	private Difficulty difficulty;
	private boolean difficultyLocked;
	private double borderX;
	private double borderZ;
	private double borderSize = 6.0E7;
	private long borderSizeLerpTime;
	private double borderSizeLerpTarget;
	private double borderSafeZone = 5.0;
	private double borderDamagePerBlock = 0.2;
	private int borderWarningBlocks = 5;
	private int borderWarningTime = 15;
	private final Set<String> disabledDataPacks = Sets.<String>newHashSet();
	private final Set<String> enabledDataPacks = Sets.<String>newLinkedHashSet();
	private final Map<DimensionType, CompoundTag> dimensionData = Maps.<DimensionType, CompoundTag>newIdentityHashMap();
	private CompoundTag customBossEvents;
	private int wanderingTraderSpawnDelay;
	private int wanderingTraderSpawnChance;
	private UUID wanderingTraderId;
	private Set<String> knownServerBrands = Sets.<String>newLinkedHashSet();
	private boolean wasModded;
	private final GameRules gameRules = new GameRules();
	private final TimerQueue<MinecraftServer> scheduledEvents = new TimerQueue<>(TimerCallbacks.SERVER_CALLBACKS);

	protected LevelData() {
		this.fixerUpper = null;
		this.playerDataVersion = SharedConstants.getCurrentVersion().getWorldVersion();
	}

	public LevelData(CompoundTag compoundTag, DataFixer dataFixer, int i, @Nullable CompoundTag compoundTag2) {
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

		if (compoundTag.contains("BorderCenterX", 99)) {
			this.borderX = compoundTag.getDouble("BorderCenterX");
		}

		if (compoundTag.contains("BorderCenterZ", 99)) {
			this.borderZ = compoundTag.getDouble("BorderCenterZ");
		}

		if (compoundTag.contains("BorderSize", 99)) {
			this.borderSize = compoundTag.getDouble("BorderSize");
		}

		if (compoundTag.contains("BorderSizeLerpTime", 99)) {
			this.borderSizeLerpTime = compoundTag.getLong("BorderSizeLerpTime");
		}

		if (compoundTag.contains("BorderSizeLerpTarget", 99)) {
			this.borderSizeLerpTarget = compoundTag.getDouble("BorderSizeLerpTarget");
		}

		if (compoundTag.contains("BorderSafeZone", 99)) {
			this.borderSafeZone = compoundTag.getDouble("BorderSafeZone");
		}

		if (compoundTag.contains("BorderDamagePerBlock", 99)) {
			this.borderDamagePerBlock = compoundTag.getDouble("BorderDamagePerBlock");
		}

		if (compoundTag.contains("BorderWarningBlocks", 99)) {
			this.borderWarningBlocks = compoundTag.getInt("BorderWarningBlocks");
		}

		if (compoundTag.contains("BorderWarningTime", 99)) {
			this.borderWarningTime = compoundTag.getInt("BorderWarningTime");
		}

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

		if (compoundTag.contains("WanderingTraderId", 8)) {
			this.wanderingTraderId = UUID.fromString(compoundTag.getString("WanderingTraderId"));
		}
	}

	private static <T> Dynamic<T> datafixGeneratorOptions(LevelType levelType, Dynamic<T> dynamic, int i, DataFixer dataFixer) {
		int j = Math.max(i, 2501);
		Dynamic<T> dynamic2 = dynamic.merge(dynamic.createString("levelType"), dynamic.createString(levelType.getSerialization()));
		return dataFixer.update(References.CHUNK_GENERATOR_SETTINGS, dynamic2, j, SharedConstants.getCurrentVersion().getWorldVersion()).remove("levelType");
	}

	public LevelData(LevelSettings levelSettings, String string) {
		this.fixerUpper = null;
		this.playerDataVersion = SharedConstants.getCurrentVersion().getWorldVersion();
		this.setLevelSettings(levelSettings);
		this.levelName = string;
		this.difficulty = DEFAULT_DIFFICULTY;
		this.initialized = false;
	}

	public void setLevelSettings(LevelSettings levelSettings) {
		this.seed = levelSettings.getSeed();
		this.gameType = levelSettings.getGameType();
		this.generateMapFeatures = levelSettings.isGenerateMapFeatures();
		this.hardcore = levelSettings.isHardcore();
		this.generatorProvider = levelSettings.getGeneratorProvider();
		this.allowCommands = levelSettings.getAllowCommands();
	}

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
		compoundTag.putInt("version", this.version);
		compoundTag.putInt("clearWeatherTime", this.clearWeatherTime);
		compoundTag.putInt("rainTime", this.rainTime);
		compoundTag.putBoolean("raining", this.raining);
		compoundTag.putInt("thunderTime", this.thunderTime);
		compoundTag.putBoolean("thundering", this.thundering);
		compoundTag.putBoolean("hardcore", this.hardcore);
		compoundTag.putBoolean("allowCommands", this.allowCommands);
		compoundTag.putBoolean("initialized", this.initialized);
		compoundTag.putDouble("BorderCenterX", this.borderX);
		compoundTag.putDouble("BorderCenterZ", this.borderZ);
		compoundTag.putDouble("BorderSize", this.borderSize);
		compoundTag.putLong("BorderSizeLerpTime", this.borderSizeLerpTime);
		compoundTag.putDouble("BorderSafeZone", this.borderSafeZone);
		compoundTag.putDouble("BorderDamagePerBlock", this.borderDamagePerBlock);
		compoundTag.putDouble("BorderSizeLerpTarget", this.borderSizeLerpTarget);
		compoundTag.putDouble("BorderWarningBlocks", (double)this.borderWarningBlocks);
		compoundTag.putDouble("BorderWarningTime", (double)this.borderWarningTime);
		if (this.difficulty != null) {
			compoundTag.putByte("Difficulty", (byte)this.difficulty.getId());
		}

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
			compoundTag.putString("WanderingTraderId", this.wanderingTraderId.toString());
		}
	}

	public long getSeed() {
		return this.seed;
	}

	public static long obfuscateSeed(long l) {
		return Hashing.sha256().hashLong(l).asLong();
	}

	public int getXSpawn() {
		return this.xSpawn;
	}

	public int getYSpawn() {
		return this.ySpawn;
	}

	public int getZSpawn() {
		return this.zSpawn;
	}

	public long getGameTime() {
		return this.gameTime;
	}

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

	public CompoundTag getLoadedPlayerTag() {
		this.updatePlayerTag();
		return this.loadedPlayerTag;
	}

	@Environment(EnvType.CLIENT)
	public void setXSpawn(int i) {
		this.xSpawn = i;
	}

	@Environment(EnvType.CLIENT)
	public void setYSpawn(int i) {
		this.ySpawn = i;
	}

	@Environment(EnvType.CLIENT)
	public void setZSpawn(int i) {
		this.zSpawn = i;
	}

	public void setGameTime(long l) {
		this.gameTime = l;
	}

	public void setDayTime(long l) {
		this.dayTime = l;
	}

	public void setSpawn(BlockPos blockPos) {
		this.xSpawn = blockPos.getX();
		this.ySpawn = blockPos.getY();
		this.zSpawn = blockPos.getZ();
	}

	public String getLevelName() {
		return this.levelName;
	}

	public void setLevelName(String string) {
		this.levelName = string;
	}

	public int getVersion() {
		return this.version;
	}

	public void setVersion(int i) {
		this.version = i;
	}

	@Environment(EnvType.CLIENT)
	public long getLastPlayed() {
		return this.lastPlayed;
	}

	public int getClearWeatherTime() {
		return this.clearWeatherTime;
	}

	public void setClearWeatherTime(int i) {
		this.clearWeatherTime = i;
	}

	public boolean isThundering() {
		return this.thundering;
	}

	public void setThundering(boolean bl) {
		this.thundering = bl;
	}

	public int getThunderTime() {
		return this.thunderTime;
	}

	public void setThunderTime(int i) {
		this.thunderTime = i;
	}

	public boolean isRaining() {
		return this.raining;
	}

	public void setRaining(boolean bl) {
		this.raining = bl;
	}

	public int getRainTime() {
		return this.rainTime;
	}

	public void setRainTime(int i) {
		this.rainTime = i;
	}

	public GameType getGameType() {
		return this.gameType;
	}

	public boolean isGenerateMapFeatures() {
		return this.generateMapFeatures;
	}

	public void setGenerateMapFeatures(boolean bl) {
		this.generateMapFeatures = bl;
	}

	public void setGameType(GameType gameType) {
		this.gameType = gameType;
	}

	public boolean isHardcore() {
		return this.hardcore;
	}

	public void setHardcore(boolean bl) {
		this.hardcore = bl;
	}

	public LevelType getGeneratorType() {
		return this.generatorProvider.getType();
	}

	public ChunkGeneratorProvider getGeneratorProvider() {
		return this.generatorProvider;
	}

	public void setGeneratorProvider(ChunkGeneratorProvider chunkGeneratorProvider) {
		this.generatorProvider = chunkGeneratorProvider;
	}

	public boolean getAllowCommands() {
		return this.allowCommands;
	}

	public void setAllowCommands(boolean bl) {
		this.allowCommands = bl;
	}

	public boolean isInitialized() {
		return this.initialized;
	}

	public void setInitialized(boolean bl) {
		this.initialized = bl;
	}

	public GameRules getGameRules() {
		return this.gameRules;
	}

	public double getBorderX() {
		return this.borderX;
	}

	public double getBorderZ() {
		return this.borderZ;
	}

	public double getBorderSize() {
		return this.borderSize;
	}

	public void setBorderSize(double d) {
		this.borderSize = d;
	}

	public long getBorderSizeLerpTime() {
		return this.borderSizeLerpTime;
	}

	public void setBorderSizeLerpTime(long l) {
		this.borderSizeLerpTime = l;
	}

	public double getBorderSizeLerpTarget() {
		return this.borderSizeLerpTarget;
	}

	public void setBorderSizeLerpTarget(double d) {
		this.borderSizeLerpTarget = d;
	}

	public void setBorderZ(double d) {
		this.borderZ = d;
	}

	public void setBorderX(double d) {
		this.borderX = d;
	}

	public double getBorderSafeZone() {
		return this.borderSafeZone;
	}

	public void setBorderSafeZone(double d) {
		this.borderSafeZone = d;
	}

	public double getBorderDamagePerBlock() {
		return this.borderDamagePerBlock;
	}

	public void setBorderDamagePerBlock(double d) {
		this.borderDamagePerBlock = d;
	}

	public int getBorderWarningBlocks() {
		return this.borderWarningBlocks;
	}

	public int getBorderWarningTime() {
		return this.borderWarningTime;
	}

	public void setBorderWarningBlocks(int i) {
		this.borderWarningBlocks = i;
	}

	public void setBorderWarningTime(int i) {
		this.borderWarningTime = i;
	}

	public Difficulty getDifficulty() {
		return this.difficulty;
	}

	public void setDifficulty(Difficulty difficulty) {
		this.difficulty = difficulty;
	}

	public boolean isDifficultyLocked() {
		return this.difficultyLocked;
	}

	public void setDifficultyLocked(boolean bl) {
		this.difficultyLocked = bl;
	}

	public TimerQueue<MinecraftServer> getScheduledEvents() {
		return this.scheduledEvents;
	}

	public void fillCrashReportCategory(CrashReportCategory crashReportCategory) {
		crashReportCategory.setDetail("Level name", (CrashReportDetail<String>)(() -> this.levelName));
		crashReportCategory.setDetail("Level seed", (CrashReportDetail<String>)(() -> String.valueOf(this.seed)));
		crashReportCategory.setDetail(
			"Level generator",
			(CrashReportDetail<String>)(() -> String.format(
					"ID %02d - %s, ver %d. Features enabled: %b",
					this.generatorProvider.getType().getId(),
					this.generatorProvider.getType().getName(),
					this.generatorProvider.getType().getVersion(),
					this.generateMapFeatures
				))
		);
		crashReportCategory.setDetail("Level generator options", (CrashReportDetail<String>)(() -> this.generatorProvider.getSettings().toString()));
		crashReportCategory.setDetail(
			"Level spawn location", (CrashReportDetail<String>)(() -> CrashReportCategory.formatLocation(this.xSpawn, this.ySpawn, this.zSpawn))
		);
		crashReportCategory.setDetail("Level time", (CrashReportDetail<String>)(() -> String.format("%d game time, %d day time", this.gameTime, this.dayTime)));
		crashReportCategory.setDetail("Known server brands", (CrashReportDetail<String>)(() -> String.join(", ", this.knownServerBrands)));
		crashReportCategory.setDetail("Level was modded", (CrashReportDetail<String>)(() -> Boolean.toString(this.wasModded)));
		crashReportCategory.setDetail("Level storage version", (CrashReportDetail<String>)(() -> {
			String string = "Unknown?";

			try {
				switch (this.version) {
					case 19132:
						string = "McRegion";
						break;
					case 19133:
						string = "Anvil";
				}
			} catch (Throwable var3) {
			}

			return String.format("0x%05X - %s", this.version, string);
		}));
		crashReportCategory.setDetail(
			"Level weather",
			(CrashReportDetail<String>)(() -> String.format(
					"Rain time: %d (now: %b), thunder time: %d (now: %b)", this.rainTime, this.raining, this.thunderTime, this.thundering
				))
		);
		crashReportCategory.setDetail(
			"Level game mode",
			(CrashReportDetail<String>)(() -> String.format(
					"Game mode: %s (ID %d). Hardcore: %b. Cheats: %b", this.gameType.getName(), this.gameType.getId(), this.hardcore, this.allowCommands
				))
		);
	}

	public CompoundTag getDimensionData(DimensionType dimensionType) {
		CompoundTag compoundTag = (CompoundTag)this.dimensionData.get(dimensionType);
		return compoundTag == null ? new CompoundTag() : compoundTag;
	}

	public void setDimensionData(DimensionType dimensionType, CompoundTag compoundTag) {
		this.dimensionData.put(dimensionType, compoundTag);
	}

	@Environment(EnvType.CLIENT)
	public int getMinecraftVersion() {
		return this.minecraftVersion;
	}

	@Environment(EnvType.CLIENT)
	public boolean isSnapshot() {
		return this.snapshot;
	}

	@Environment(EnvType.CLIENT)
	public String getMinecraftVersionName() {
		return this.minecraftVersionName;
	}

	public Set<String> getDisabledDataPacks() {
		return this.disabledDataPacks;
	}

	public Set<String> getEnabledDataPacks() {
		return this.enabledDataPacks;
	}

	@Nullable
	public CompoundTag getCustomBossEvents() {
		return this.customBossEvents;
	}

	public void setCustomBossEvents(@Nullable CompoundTag compoundTag) {
		this.customBossEvents = compoundTag;
	}

	public int getWanderingTraderSpawnDelay() {
		return this.wanderingTraderSpawnDelay;
	}

	public void setWanderingTraderSpawnDelay(int i) {
		this.wanderingTraderSpawnDelay = i;
	}

	public int getWanderingTraderSpawnChance() {
		return this.wanderingTraderSpawnChance;
	}

	public void setWanderingTraderSpawnChance(int i) {
		this.wanderingTraderSpawnChance = i;
	}

	public void setWanderingTraderId(UUID uUID) {
		this.wanderingTraderId = uUID;
	}

	public void setModdedInfo(String string, boolean bl) {
		this.knownServerBrands.add(string);
		this.wasModded |= bl;
	}
}
