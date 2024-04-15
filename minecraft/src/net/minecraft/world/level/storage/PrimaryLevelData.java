package net.minecraft.world.level.storage;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReportCategory;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.timers.TimerCallbacks;
import net.minecraft.world.level.timers.TimerQueue;
import org.slf4j.Logger;

public class PrimaryLevelData implements ServerLevelData, WorldData {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final String LEVEL_NAME = "LevelName";
	protected static final String PLAYER = "Player";
	protected static final String WORLD_GEN_SETTINGS = "WorldGenSettings";
	private LevelSettings settings;
	private final WorldOptions worldOptions;
	private final PrimaryLevelData.SpecialWorldProperty specialWorldProperty;
	private final Lifecycle worldGenSettingsLifecycle;
	private BlockPos spawnPos;
	private float spawnAngle;
	private long gameTime;
	private long dayTime;
	@Nullable
	private final CompoundTag loadedPlayerTag;
	private final int version;
	private int clearWeatherTime;
	private boolean raining;
	private int rainTime;
	private boolean thundering;
	private int thunderTime;
	private boolean initialized;
	private boolean difficultyLocked;
	private WorldBorder.Settings worldBorder;
	private EndDragonFight.Data endDragonFightData;
	@Nullable
	private CompoundTag customBossEvents;
	private int wanderingTraderSpawnDelay;
	private int wanderingTraderSpawnChance;
	@Nullable
	private UUID wanderingTraderId;
	private final Set<String> knownServerBrands;
	private boolean wasModded;
	private final Set<String> removedFeatureFlags;
	private final TimerQueue<MinecraftServer> scheduledEvents;

	private PrimaryLevelData(
		@Nullable CompoundTag compoundTag,
		boolean bl,
		BlockPos blockPos,
		float f,
		long l,
		long m,
		int i,
		int j,
		int k,
		boolean bl2,
		int n,
		boolean bl3,
		boolean bl4,
		boolean bl5,
		WorldBorder.Settings settings,
		int o,
		int p,
		@Nullable UUID uUID,
		Set<String> set,
		Set<String> set2,
		TimerQueue<MinecraftServer> timerQueue,
		@Nullable CompoundTag compoundTag2,
		EndDragonFight.Data data,
		LevelSettings levelSettings,
		WorldOptions worldOptions,
		PrimaryLevelData.SpecialWorldProperty specialWorldProperty,
		Lifecycle lifecycle
	) {
		this.wasModded = bl;
		this.spawnPos = blockPos;
		this.spawnAngle = f;
		this.gameTime = l;
		this.dayTime = m;
		this.version = i;
		this.clearWeatherTime = j;
		this.rainTime = k;
		this.raining = bl2;
		this.thunderTime = n;
		this.thundering = bl3;
		this.initialized = bl4;
		this.difficultyLocked = bl5;
		this.worldBorder = settings;
		this.wanderingTraderSpawnDelay = o;
		this.wanderingTraderSpawnChance = p;
		this.wanderingTraderId = uUID;
		this.knownServerBrands = set;
		this.removedFeatureFlags = set2;
		this.loadedPlayerTag = compoundTag;
		this.scheduledEvents = timerQueue;
		this.customBossEvents = compoundTag2;
		this.endDragonFightData = data;
		this.settings = levelSettings;
		this.worldOptions = worldOptions;
		this.specialWorldProperty = specialWorldProperty;
		this.worldGenSettingsLifecycle = lifecycle;
	}

	public PrimaryLevelData(
		LevelSettings levelSettings, WorldOptions worldOptions, PrimaryLevelData.SpecialWorldProperty specialWorldProperty, Lifecycle lifecycle
	) {
		this(
			null,
			false,
			BlockPos.ZERO,
			0.0F,
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
			Sets.<String>newLinkedHashSet(),
			new HashSet(),
			new TimerQueue<>(TimerCallbacks.SERVER_CALLBACKS),
			null,
			EndDragonFight.Data.DEFAULT,
			levelSettings.copy(),
			worldOptions,
			specialWorldProperty,
			lifecycle
		);
	}

	public static <T> PrimaryLevelData parse(
		Dynamic<T> dynamic, LevelSettings levelSettings, PrimaryLevelData.SpecialWorldProperty specialWorldProperty, WorldOptions worldOptions, Lifecycle lifecycle
	) {
		long l = dynamic.get("Time").asLong(0L);
		return new PrimaryLevelData(
			(CompoundTag)dynamic.get("Player").flatMap(CompoundTag.CODEC::parse).result().orElse(null),
			dynamic.get("WasModded").asBoolean(false),
			new BlockPos(dynamic.get("SpawnX").asInt(0), dynamic.get("SpawnY").asInt(0), dynamic.get("SpawnZ").asInt(0)),
			dynamic.get("SpawnAngle").asFloat(0.0F),
			l,
			dynamic.get("DayTime").asLong(l),
			LevelVersion.parse(dynamic).levelDataVersion(),
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
			(UUID)dynamic.get("WanderingTraderId").read(UUIDUtil.CODEC).result().orElse(null),
			(Set<String>)dynamic.get("ServerBrands")
				.asStream()
				.flatMap(dynamicx -> dynamicx.asString().result().stream())
				.collect(Collectors.toCollection(Sets::newLinkedHashSet)),
			(Set<String>)dynamic.get("removed_features").asStream().flatMap(dynamicx -> dynamicx.asString().result().stream()).collect(Collectors.toSet()),
			new TimerQueue<>(TimerCallbacks.SERVER_CALLBACKS, dynamic.get("ScheduledEvents").asStream()),
			(CompoundTag)dynamic.get("CustomBossEvents").orElseEmptyMap().getValue(),
			(EndDragonFight.Data)dynamic.get("DragonFight").read(EndDragonFight.Data.CODEC).resultOrPartial(LOGGER::error).orElse(EndDragonFight.Data.DEFAULT),
			levelSettings,
			worldOptions,
			specialWorldProperty,
			lifecycle
		);
	}

	@Override
	public CompoundTag createTag(RegistryAccess registryAccess, @Nullable CompoundTag compoundTag) {
		if (compoundTag == null) {
			compoundTag = this.loadedPlayerTag;
		}

		CompoundTag compoundTag2 = new CompoundTag();
		this.setTagData(registryAccess, compoundTag2, compoundTag);
		return compoundTag2;
	}

	private void setTagData(RegistryAccess registryAccess, CompoundTag compoundTag, @Nullable CompoundTag compoundTag2) {
		compoundTag.put("ServerBrands", stringCollectionToTag(this.knownServerBrands));
		compoundTag.putBoolean("WasModded", this.wasModded);
		if (!this.removedFeatureFlags.isEmpty()) {
			compoundTag.put("removed_features", stringCollectionToTag(this.removedFeatureFlags));
		}

		CompoundTag compoundTag3 = new CompoundTag();
		compoundTag3.putString("Name", SharedConstants.getCurrentVersion().getName());
		compoundTag3.putInt("Id", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
		compoundTag3.putBoolean("Snapshot", !SharedConstants.getCurrentVersion().isStable());
		compoundTag3.putString("Series", SharedConstants.getCurrentVersion().getDataVersion().getSeries());
		compoundTag.put("Version", compoundTag3);
		NbtUtils.addCurrentDataVersion(compoundTag);
		DynamicOps<Tag> dynamicOps = registryAccess.createSerializationContext(NbtOps.INSTANCE);
		WorldGenSettings.encode(dynamicOps, this.worldOptions, registryAccess)
			.resultOrPartial(Util.prefix("WorldGenSettings: ", LOGGER::error))
			.ifPresent(tag -> compoundTag.put("WorldGenSettings", tag));
		compoundTag.putInt("GameType", this.settings.gameType().getId());
		compoundTag.putInt("SpawnX", this.spawnPos.getX());
		compoundTag.putInt("SpawnY", this.spawnPos.getY());
		compoundTag.putInt("SpawnZ", this.spawnPos.getZ());
		compoundTag.putFloat("SpawnAngle", this.spawnAngle);
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
		compoundTag.put("DragonFight", EndDragonFight.Data.CODEC.encodeStart(NbtOps.INSTANCE, this.endDragonFightData).getOrThrow());
		if (compoundTag2 != null) {
			compoundTag.put("Player", compoundTag2);
		}

		WorldDataConfiguration.CODEC
			.encodeStart(NbtOps.INSTANCE, this.settings.getDataConfiguration())
			.ifSuccess(tag -> compoundTag.merge((CompoundTag)tag))
			.ifError(error -> LOGGER.warn("Failed to encode configuration {}", error.message()));
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

	private static ListTag stringCollectionToTag(Set<String> set) {
		ListTag listTag = new ListTag();
		set.stream().map(StringTag::valueOf).forEach(listTag::add);
		return listTag;
	}

	@Override
	public BlockPos getSpawnPos() {
		return this.spawnPos;
	}

	@Override
	public float getSpawnAngle() {
		return this.spawnAngle;
	}

	@Override
	public long getGameTime() {
		return this.gameTime;
	}

	@Override
	public long getDayTime() {
		return this.dayTime;
	}

	@Nullable
	@Override
	public CompoundTag getLoadedPlayerTag() {
		return this.loadedPlayerTag;
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
	public void setSpawn(BlockPos blockPos, float f) {
		this.spawnPos = blockPos.immutable();
		this.spawnAngle = f;
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
	public boolean isAllowCommands() {
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
	public void fillCrashReportCategory(CrashReportCategory crashReportCategory, LevelHeightAccessor levelHeightAccessor) {
		ServerLevelData.super.fillCrashReportCategory(crashReportCategory, levelHeightAccessor);
		WorldData.super.fillCrashReportCategory(crashReportCategory);
	}

	@Override
	public WorldOptions worldGenOptions() {
		return this.worldOptions;
	}

	@Override
	public boolean isFlatWorld() {
		return this.specialWorldProperty == PrimaryLevelData.SpecialWorldProperty.FLAT;
	}

	@Override
	public boolean isDebugWorld() {
		return this.specialWorldProperty == PrimaryLevelData.SpecialWorldProperty.DEBUG;
	}

	@Override
	public Lifecycle worldGenSettingsLifecycle() {
		return this.worldGenSettingsLifecycle;
	}

	@Override
	public EndDragonFight.Data endDragonFightData() {
		return this.endDragonFightData;
	}

	@Override
	public void setEndDragonFightData(EndDragonFight.Data data) {
		this.endDragonFightData = data;
	}

	@Override
	public WorldDataConfiguration getDataConfiguration() {
		return this.settings.getDataConfiguration();
	}

	@Override
	public void setDataConfiguration(WorldDataConfiguration worldDataConfiguration) {
		this.settings = this.settings.withDataConfiguration(worldDataConfiguration);
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

	@Nullable
	@Override
	public UUID getWanderingTraderId() {
		return this.wanderingTraderId;
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
	public Set<String> getRemovedFeatureFlags() {
		return Set.copyOf(this.removedFeatureFlags);
	}

	@Override
	public ServerLevelData overworldData() {
		return this;
	}

	@Override
	public LevelSettings getLevelSettings() {
		return this.settings.copy();
	}

	@Deprecated
	public static enum SpecialWorldProperty {
		NONE,
		FLAT,
		DEBUG;
	}
}
