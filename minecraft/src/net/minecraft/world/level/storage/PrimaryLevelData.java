package net.minecraft.world.level.storage;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReportCategory;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SerializableUUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.timers.TimerCallbacks;
import net.minecraft.world.level.timers.TimerQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PrimaryLevelData implements ServerLevelData, WorldData {
	private static final Logger LOGGER = LogManager.getLogger();
	private LevelSettings settings;
	private int xSpawn;
	private int ySpawn;
	private int zSpawn;
	private long gameTime;
	private long dayTime;
	@Nullable
	private final DataFixer fixerUpper;
	private final int playerDataVersion;
	private boolean upgradedPlayerTag;
	@Nullable
	private CompoundTag loadedPlayerTag;
	private final int version;
	private int clearWeatherTime;
	private boolean raining;
	private int rainTime;
	private boolean thundering;
	private int thunderTime;
	private boolean initialized;
	private boolean difficultyLocked;
	private WorldBorder.Settings worldBorder;
	private final Set<String> disabledDataPacks;
	private final Set<String> enabledDataPacks;
	private CompoundTag endDragonFightData;
	@Nullable
	private CompoundTag customBossEvents;
	private int wanderingTraderSpawnDelay;
	private int wanderingTraderSpawnChance;
	@Nullable
	private UUID wanderingTraderId;
	private final Set<String> knownServerBrands;
	private boolean wasModded;
	private final TimerQueue<MinecraftServer> scheduledEvents;

	private PrimaryLevelData(
		@Nullable DataFixer dataFixer,
		int i,
		@Nullable CompoundTag compoundTag,
		boolean bl,
		int j,
		int k,
		int l,
		long m,
		long n,
		int o,
		int p,
		int q,
		boolean bl2,
		int r,
		boolean bl3,
		boolean bl4,
		boolean bl5,
		WorldBorder.Settings settings,
		int s,
		int t,
		@Nullable UUID uUID,
		LinkedHashSet<String> linkedHashSet,
		LinkedHashSet<String> linkedHashSet2,
		Set<String> set,
		TimerQueue<MinecraftServer> timerQueue,
		@Nullable CompoundTag compoundTag2,
		CompoundTag compoundTag3,
		LevelSettings levelSettings
	) {
		this.fixerUpper = dataFixer;
		this.wasModded = bl;
		this.settings = levelSettings;
		this.xSpawn = j;
		this.ySpawn = k;
		this.zSpawn = l;
		this.gameTime = m;
		this.dayTime = n;
		this.version = o;
		this.clearWeatherTime = p;
		this.rainTime = q;
		this.raining = bl2;
		this.thunderTime = r;
		this.thundering = bl3;
		this.initialized = bl4;
		this.difficultyLocked = bl5;
		this.worldBorder = settings;
		this.wanderingTraderSpawnDelay = s;
		this.wanderingTraderSpawnChance = t;
		this.wanderingTraderId = uUID;
		this.knownServerBrands = linkedHashSet;
		this.loadedPlayerTag = compoundTag;
		this.playerDataVersion = i;
		this.scheduledEvents = timerQueue;
		this.enabledDataPacks = linkedHashSet2;
		this.disabledDataPacks = set;
		this.customBossEvents = compoundTag2;
		this.endDragonFightData = compoundTag3;
	}

	public PrimaryLevelData(LevelSettings levelSettings) {
		this(
			null,
			SharedConstants.getCurrentVersion().getWorldVersion(),
			null,
			false,
			0,
			0,
			0,
			0L,
			0L,
			19133,
			0,
			0,
			false,
			0,
			false,
			false,
			false,
			WorldBorder.DEFAULT_SETTINGS,
			0,
			0,
			null,
			Sets.newLinkedHashSet(),
			Sets.newLinkedHashSet(),
			Sets.<String>newHashSet(),
			new TimerQueue<>(TimerCallbacks.SERVER_CALLBACKS),
			null,
			new CompoundTag(),
			levelSettings.copy()
		);
	}

	public static PrimaryLevelData parse(
		Dynamic<Tag> dynamic, DataFixer dataFixer, int i, @Nullable CompoundTag compoundTag, LevelSettings levelSettings, LevelVersion levelVersion
	) {
		long l = dynamic.get("Time").asLong(0L);
		OptionalDynamic<?> optionalDynamic = dynamic.get("DataPacks");
		CompoundTag compoundTag2 = (CompoundTag)dynamic.get("DragonFight")
			.result()
			.map(Dynamic::getValue)
			.orElseGet(() -> dynamic.get("DimensionData").get("1").get("DragonFight").orElseEmptyMap().getValue());
		return new PrimaryLevelData(
			dataFixer,
			i,
			compoundTag,
			dynamic.get("WasModded").asBoolean(false),
			dynamic.get("SpawnX").asInt(0),
			dynamic.get("SpawnY").asInt(0),
			dynamic.get("SpawnZ").asInt(0),
			l,
			dynamic.get("DayTime").asLong(l),
			levelVersion.levelDataVersion(),
			dynamic.get("clearWeatherTime").asInt(0),
			dynamic.get("rainTime").asInt(0),
			dynamic.get("raining").asBoolean(false),
			dynamic.get("thunderTime").asInt(0),
			dynamic.get("thundering").asBoolean(false),
			dynamic.get("initialized").asBoolean(true),
			dynamic.get("DifficultyLocked").asBoolean(false),
			WorldBorder.Settings.read(dynamic, WorldBorder.DEFAULT_SETTINGS),
			dynamic.get("WanderingTraderSpawnDelay").asInt(0),
			dynamic.get("WanderingTraderSpawnChance").asInt(0),
			(UUID)dynamic.get("WanderingTraderId").read(SerializableUUID.CODEC).result().map(SerializableUUID::value).orElse(null),
			(LinkedHashSet<String>)dynamic.get("ServerBrands")
				.asStream()
				.flatMap(dynamicx -> Util.toStream(dynamicx.asString().result()))
				.collect(Collectors.toCollection(Sets::newLinkedHashSet)),
			(LinkedHashSet<String>)optionalDynamic.get("Enabled")
				.asStream()
				.flatMap(dynamicx -> Util.toStream(dynamicx.asString().result()))
				.collect(Collectors.toCollection(Sets::newLinkedHashSet)),
			(Set<String>)optionalDynamic.get("Disabled").asStream().flatMap(dynamicx -> Util.toStream(dynamicx.asString().result())).collect(Collectors.toSet()),
			new TimerQueue<>(TimerCallbacks.SERVER_CALLBACKS, dynamic.get("ScheduledEvents").asStream()),
			(CompoundTag)dynamic.get("CustomBossEvents").orElseEmptyMap().getValue(),
			compoundTag2,
			levelSettings
		);
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
		WorldGenSettings.CODEC
			.encodeStart(NbtOps.INSTANCE, this.settings.worldGenSettings())
			.resultOrPartial(Util.prefix("WorldGenSettings: ", LOGGER::error))
			.ifPresent(tag -> compoundTag.put("WorldGenSettings", tag));
		compoundTag.putInt("GameType", this.settings.gameType().getId());
		compoundTag.putInt("SpawnX", this.xSpawn);
		compoundTag.putInt("SpawnY", this.ySpawn);
		compoundTag.putInt("SpawnZ", this.zSpawn);
		compoundTag.putLong("Time", this.gameTime);
		compoundTag.putLong("DayTime", this.dayTime);
		compoundTag.putLong("LastPlayed", Util.getEpochMillis());
		compoundTag.putString("LevelName", this.settings.levelName());
		compoundTag.putInt("version", 19133);
		compoundTag.putInt("clearWeatherTime", this.clearWeatherTime);
		compoundTag.putInt("rainTime", this.rainTime);
		compoundTag.putBoolean("raining", this.raining);
		compoundTag.putInt("thunderTime", this.thunderTime);
		compoundTag.putBoolean("thundering", this.thundering);
		compoundTag.putBoolean("hardcore", this.settings.hardcore());
		compoundTag.putBoolean("allowCommands", this.settings.allowCommands());
		compoundTag.putBoolean("initialized", this.initialized);
		this.worldBorder.write(compoundTag);
		compoundTag.putByte("Difficulty", (byte)this.settings.difficulty().getId());
		compoundTag.putBoolean("DifficultyLocked", this.difficultyLocked);
		compoundTag.put("GameRules", this.settings.gameRules().createTag());
		compoundTag.put("DragonFight", this.endDragonFightData);
		if (compoundTag2 != null) {
			compoundTag.put("Player", compoundTag2);
		}

		CompoundTag compoundTag4 = new CompoundTag();
		ListTag listTag2 = new ListTag();

		for (String string : this.enabledDataPacks) {
			listTag2.add(StringTag.valueOf(string));
		}

		compoundTag4.put("Enabled", listTag2);
		ListTag listTag3 = new ListTag();

		for (String string2 : this.disabledDataPacks) {
			listTag3.add(StringTag.valueOf(string2));
		}

		compoundTag4.put("Disabled", listTag3);
		compoundTag.put("DataPacks", compoundTag4);
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
		return this.settings.levelName();
	}

	@Override
	public int getVersion() {
		return this.version;
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
		return this.settings.gameType();
	}

	@Override
	public void setGameType(GameType gameType) {
		this.settings = this.settings.withGameType(gameType);
	}

	@Override
	public boolean isHardcore() {
		return this.settings.hardcore();
	}

	@Override
	public boolean getAllowCommands() {
		return this.settings.allowCommands();
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
		return this.settings.gameRules();
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
		return this.settings.difficulty();
	}

	@Override
	public void setDifficulty(Difficulty difficulty) {
		this.settings = this.settings.withDifficulty(difficulty);
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
	public WorldGenSettings worldGenSettings() {
		return this.settings.worldGenSettings();
	}

	@Override
	public CompoundTag endDragonFightData() {
		return this.endDragonFightData;
	}

	@Override
	public void setEndDragonFightData(CompoundTag compoundTag) {
		this.endDragonFightData = compoundTag;
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

	@Environment(EnvType.CLIENT)
	@Override
	public LevelSettings getLevelSettings() {
		return this.settings.copy();
	}
}
