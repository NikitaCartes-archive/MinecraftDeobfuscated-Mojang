package net.minecraft.server;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.MiscOverworldFeatures;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.DemoMode;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.network.TextFilter;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.ModCheck;
import net.minecraft.util.Mth;
import net.minecraft.util.NativeModuleLister;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.debugchart.RemoteDebugSampleType;
import net.minecraft.util.debugchart.SampleLogger;
import net.minecraft.util.debugchart.TpsDebugDimensions;
import net.minecraft.util.profiling.EmptyProfileResults;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.ResultField;
import net.minecraft.util.profiling.SingleTickProfiler;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import net.minecraft.util.profiling.jfr.callback.ProfiledDuration;
import net.minecraft.util.profiling.metrics.profiling.ActiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.InactiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.MetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.ServerMetricsSamplersProvider;
import net.minecraft.util.profiling.metrics.storage.MetricsPersister;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.Difficulty;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.CommandStorage;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public abstract class MinecraftServer extends ReentrantBlockableEventLoop<TickTask> implements ServerInfo, CommandSource, AutoCloseable {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final String VANILLA_BRAND = "vanilla";
	private static final float AVERAGE_TICK_TIME_SMOOTHING = 0.8F;
	private static final int TICK_STATS_SPAN = 100;
	private static final long OVERLOADED_THRESHOLD_NANOS = 20L * TimeUtil.NANOSECONDS_PER_SECOND / 20L;
	private static final int OVERLOADED_TICKS_THRESHOLD = 20;
	private static final long OVERLOADED_WARNING_INTERVAL_NANOS = 10L * TimeUtil.NANOSECONDS_PER_SECOND;
	private static final int OVERLOADED_TICKS_WARNING_INTERVAL = 100;
	private static final long STATUS_EXPIRE_TIME_NANOS = 5L * TimeUtil.NANOSECONDS_PER_SECOND;
	private static final long PREPARE_LEVELS_DEFAULT_DELAY_NANOS = 10L * TimeUtil.NANOSECONDS_PER_MILLISECOND;
	private static final int MAX_STATUS_PLAYER_SAMPLE = 12;
	private static final int SPAWN_POSITION_SEARCH_RADIUS = 5;
	private static final int AUTOSAVE_INTERVAL = 6000;
	private static final int MIMINUM_AUTOSAVE_TICKS = 100;
	private static final int MAX_TICK_LATENCY = 3;
	public static final int ABSOLUTE_MAX_WORLD_SIZE = 29999984;
	public static final LevelSettings DEMO_SETTINGS = new LevelSettings(
		"Demo World", GameType.SURVIVAL, false, Difficulty.NORMAL, false, new GameRules(), WorldDataConfiguration.DEFAULT
	);
	public static final GameProfile ANONYMOUS_PLAYER_PROFILE = new GameProfile(Util.NIL_UUID, "Anonymous Player");
	protected final LevelStorageSource.LevelStorageAccess storageSource;
	protected final PlayerDataStorage playerDataStorage;
	private final List<Runnable> tickables = Lists.<Runnable>newArrayList();
	private MetricsRecorder metricsRecorder = InactiveMetricsRecorder.INSTANCE;
	private ProfilerFiller profiler = this.metricsRecorder.getProfiler();
	private Consumer<ProfileResults> onMetricsRecordingStopped = profileResults -> this.stopRecordingMetrics();
	private Consumer<Path> onMetricsRecordingFinished = path -> {
	};
	private boolean willStartRecordingMetrics;
	@Nullable
	private MinecraftServer.TimeProfiler debugCommandProfiler;
	private boolean debugCommandProfilerDelayStart;
	private final ServerConnectionListener connection;
	private final ChunkProgressListenerFactory progressListenerFactory;
	@Nullable
	private ServerStatus status;
	@Nullable
	private ServerStatus.Favicon statusIcon;
	private final RandomSource random = RandomSource.create();
	private final DataFixer fixerUpper;
	private String localIp;
	private int port = -1;
	private final LayeredRegistryAccess<RegistryLayer> registries;
	private final Map<ResourceKey<Level>, ServerLevel> levels = Maps.<ResourceKey<Level>, ServerLevel>newLinkedHashMap();
	private PlayerList playerList;
	private volatile boolean running = true;
	private boolean stopped;
	private int tickCount;
	private int ticksUntilAutosave = 6000;
	protected final Proxy proxy;
	private boolean onlineMode;
	private boolean preventProxyConnections;
	private boolean pvp;
	private boolean allowFlight;
	@Nullable
	private String motd;
	private int playerIdleTimeout;
	private final long[] tickTimesNanos = new long[100];
	private long aggregatedTickTimesNanos = 0L;
	@Nullable
	private KeyPair keyPair;
	@Nullable
	private GameProfile singleplayerProfile;
	private boolean isDemo;
	private volatile boolean isReady;
	private long lastOverloadWarningNanos;
	protected final Services services;
	private long lastServerStatus;
	private final Thread serverThread;
	private long lastTickNanos = Util.getNanos();
	private long taskExecutionStartNanos = Util.getNanos();
	private long idleTimeNanos;
	private long nextTickTimeNanos = Util.getNanos();
	private long delayedTasksMaxNextTickTimeNanos;
	private boolean mayHaveDelayedTasks;
	private final PackRepository packRepository;
	private final ServerScoreboard scoreboard = new ServerScoreboard(this);
	@Nullable
	private CommandStorage commandStorage;
	private final CustomBossEvents customBossEvents = new CustomBossEvents();
	private final ServerFunctionManager functionManager;
	private boolean enforceWhitelist;
	private float smoothedTickTimeMillis;
	private final Executor executor;
	@Nullable
	private String serverId;
	private MinecraftServer.ReloadableResources resources;
	private final StructureTemplateManager structureTemplateManager;
	private final ServerTickRateManager tickRateManager;
	protected final WorldData worldData;
	private final PotionBrewing potionBrewing;
	private volatile boolean isSaving;

	public static <S extends MinecraftServer> S spin(Function<Thread, S> function) {
		AtomicReference<S> atomicReference = new AtomicReference();
		Thread thread = new Thread(() -> ((MinecraftServer)atomicReference.get()).runServer(), "Server thread");
		thread.setUncaughtExceptionHandler((threadx, throwable) -> LOGGER.error("Uncaught exception in server thread", throwable));
		if (Runtime.getRuntime().availableProcessors() > 4) {
			thread.setPriority(8);
		}

		S minecraftServer = (S)function.apply(thread);
		atomicReference.set(minecraftServer);
		thread.start();
		return minecraftServer;
	}

	public MinecraftServer(
		Thread thread,
		LevelStorageSource.LevelStorageAccess levelStorageAccess,
		PackRepository packRepository,
		WorldStem worldStem,
		Proxy proxy,
		DataFixer dataFixer,
		Services services,
		ChunkProgressListenerFactory chunkProgressListenerFactory
	) {
		super("Server");
		this.registries = worldStem.registries();
		this.worldData = worldStem.worldData();
		if (!this.registries.compositeAccess().registryOrThrow(Registries.LEVEL_STEM).containsKey(LevelStem.OVERWORLD)) {
			throw new IllegalStateException("Missing Overworld dimension data");
		} else {
			this.proxy = proxy;
			this.packRepository = packRepository;
			this.resources = new MinecraftServer.ReloadableResources(worldStem.resourceManager(), worldStem.dataPackResources());
			this.services = services;
			if (services.profileCache() != null) {
				services.profileCache().setExecutor(this);
			}

			this.connection = new ServerConnectionListener(this);
			this.tickRateManager = new ServerTickRateManager(this);
			this.progressListenerFactory = chunkProgressListenerFactory;
			this.storageSource = levelStorageAccess;
			this.playerDataStorage = levelStorageAccess.createPlayerStorage();
			this.fixerUpper = dataFixer;
			this.functionManager = new ServerFunctionManager(this, this.resources.managers.getFunctionLibrary());
			HolderGetter<Block> holderGetter = this.registries
				.compositeAccess()
				.registryOrThrow(Registries.BLOCK)
				.asLookup()
				.filterFeatures(this.worldData.enabledFeatures());
			this.structureTemplateManager = new StructureTemplateManager(worldStem.resourceManager(), levelStorageAccess, dataFixer, holderGetter);
			this.serverThread = thread;
			this.executor = Util.backgroundExecutor();
			this.potionBrewing = PotionBrewing.bootstrap(this.worldData.enabledFeatures());
		}
	}

	private void readScoreboard(DimensionDataStorage dimensionDataStorage) {
		dimensionDataStorage.computeIfAbsent(this.getScoreboard().dataFactory(), "scoreboard");
	}

	protected abstract boolean initServer() throws IOException;

	protected void loadLevel() {
		if (!JvmProfiler.INSTANCE.isRunning()) {
		}

		boolean bl = false;
		ProfiledDuration profiledDuration = JvmProfiler.INSTANCE.onWorldLoadedStarted();
		this.worldData.setModdedInfo(this.getServerModName(), this.getModdedStatus().shouldReportAsModified());
		ChunkProgressListener chunkProgressListener = this.progressListenerFactory.create(this.worldData.getGameRules().getInt(GameRules.RULE_SPAWN_CHUNK_RADIUS));
		this.createLevels(chunkProgressListener);
		this.forceDifficulty();
		this.prepareLevels(chunkProgressListener);
		if (profiledDuration != null) {
			profiledDuration.finish();
		}

		if (bl) {
			try {
				JvmProfiler.INSTANCE.stop();
			} catch (Throwable var5) {
				LOGGER.warn("Failed to stop JFR profiling", var5);
			}
		}
	}

	protected void forceDifficulty() {
	}

	protected void createLevels(ChunkProgressListener chunkProgressListener) {
		ServerLevelData serverLevelData = this.worldData.overworldData();
		boolean bl = this.worldData.isDebugWorld();
		Registry<LevelStem> registry = this.registries.compositeAccess().registryOrThrow(Registries.LEVEL_STEM);
		WorldOptions worldOptions = this.worldData.worldGenOptions();
		long l = worldOptions.seed();
		long m = BiomeManager.obfuscateSeed(l);
		List<CustomSpawner> list = ImmutableList.of(
			new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(serverLevelData)
		);
		LevelStem levelStem = registry.get(LevelStem.OVERWORLD);
		ServerLevel serverLevel = new ServerLevel(
			this, this.executor, this.storageSource, serverLevelData, Level.OVERWORLD, levelStem, chunkProgressListener, bl, m, list, true, null
		);
		this.levels.put(Level.OVERWORLD, serverLevel);
		DimensionDataStorage dimensionDataStorage = serverLevel.getDataStorage();
		this.readScoreboard(dimensionDataStorage);
		this.commandStorage = new CommandStorage(dimensionDataStorage);
		WorldBorder worldBorder = serverLevel.getWorldBorder();
		if (!serverLevelData.isInitialized()) {
			try {
				setInitialSpawn(serverLevel, serverLevelData, worldOptions.generateBonusChest(), bl);
				serverLevelData.setInitialized(true);
				if (bl) {
					this.setupDebugLevel(this.worldData);
				}
			} catch (Throwable var23) {
				CrashReport crashReport = CrashReport.forThrowable(var23, "Exception initializing level");

				try {
					serverLevel.fillReportDetails(crashReport);
				} catch (Throwable var22) {
				}

				throw new ReportedException(crashReport);
			}

			serverLevelData.setInitialized(true);
		}

		this.getPlayerList().addWorldborderListener(serverLevel);
		if (this.worldData.getCustomBossEvents() != null) {
			this.getCustomBossEvents().load(this.worldData.getCustomBossEvents(), this.registryAccess());
		}

		RandomSequences randomSequences = serverLevel.getRandomSequences();

		for (Entry<ResourceKey<LevelStem>, LevelStem> entry : registry.entrySet()) {
			ResourceKey<LevelStem> resourceKey = (ResourceKey<LevelStem>)entry.getKey();
			if (resourceKey != LevelStem.OVERWORLD) {
				ResourceKey<Level> resourceKey2 = ResourceKey.create(Registries.DIMENSION, resourceKey.location());
				DerivedLevelData derivedLevelData = new DerivedLevelData(this.worldData, serverLevelData);
				ServerLevel serverLevel2 = new ServerLevel(
					this,
					this.executor,
					this.storageSource,
					derivedLevelData,
					resourceKey2,
					(LevelStem)entry.getValue(),
					chunkProgressListener,
					bl,
					m,
					ImmutableList.of(),
					false,
					randomSequences
				);
				worldBorder.addListener(new BorderChangeListener.DelegateBorderChangeListener(serverLevel2.getWorldBorder()));
				this.levels.put(resourceKey2, serverLevel2);
			}
		}

		worldBorder.applySettings(serverLevelData.getWorldBorder());
	}

	private static void setInitialSpawn(ServerLevel serverLevel, ServerLevelData serverLevelData, boolean bl, boolean bl2) {
		if (bl2) {
			serverLevelData.setSpawn(BlockPos.ZERO.above(80), 0.0F);
		} else {
			ServerChunkCache serverChunkCache = serverLevel.getChunkSource();
			ChunkPos chunkPos = new ChunkPos(serverChunkCache.randomState().sampler().findSpawnPosition());
			int i = serverChunkCache.getGenerator().getSpawnHeight(serverLevel);
			if (i < serverLevel.getMinBuildHeight()) {
				BlockPos blockPos = chunkPos.getWorldPosition();
				i = serverLevel.getHeight(Heightmap.Types.WORLD_SURFACE, blockPos.getX() + 8, blockPos.getZ() + 8);
			}

			serverLevelData.setSpawn(chunkPos.getWorldPosition().offset(8, i, 8), 0.0F);
			int j = 0;
			int k = 0;
			int l = 0;
			int m = -1;

			for (int n = 0; n < Mth.square(11); n++) {
				if (j >= -5 && j <= 5 && k >= -5 && k <= 5) {
					BlockPos blockPos2 = PlayerRespawnLogic.getSpawnPosInChunk(serverLevel, new ChunkPos(chunkPos.x + j, chunkPos.z + k));
					if (blockPos2 != null) {
						serverLevelData.setSpawn(blockPos2, 0.0F);
						break;
					}
				}

				if (j == k || j < 0 && j == -k || j > 0 && j == 1 - k) {
					int o = l;
					l = -m;
					m = o;
				}

				j += l;
				k += m;
			}

			if (bl) {
				serverLevel.registryAccess()
					.registry(Registries.CONFIGURED_FEATURE)
					.flatMap(registry -> registry.getHolder(MiscOverworldFeatures.BONUS_CHEST))
					.ifPresent(
						reference -> ((ConfiguredFeature)reference.value())
								.place(serverLevel, serverChunkCache.getGenerator(), serverLevel.random, serverLevelData.getSpawnPos())
					);
			}
		}
	}

	private void setupDebugLevel(WorldData worldData) {
		worldData.setDifficulty(Difficulty.PEACEFUL);
		worldData.setDifficultyLocked(true);
		ServerLevelData serverLevelData = worldData.overworldData();
		serverLevelData.setRaining(false);
		serverLevelData.setThundering(false);
		serverLevelData.setClearWeatherTime(1000000000);
		serverLevelData.setDayTime(6000L);
		serverLevelData.setGameType(GameType.SPECTATOR);
	}

	private void prepareLevels(ChunkProgressListener chunkProgressListener) {
		ServerLevel serverLevel = this.overworld();
		LOGGER.info("Preparing start region for dimension {}", serverLevel.dimension().location());
		BlockPos blockPos = serverLevel.getSharedSpawnPos();
		chunkProgressListener.updateSpawnPos(new ChunkPos(blockPos));
		ServerChunkCache serverChunkCache = serverLevel.getChunkSource();
		this.nextTickTimeNanos = Util.getNanos();
		serverLevel.setDefaultSpawnPos(blockPos, serverLevel.getSharedSpawnAngle());
		int i = this.getGameRules().getInt(GameRules.RULE_SPAWN_CHUNK_RADIUS);
		int j = i > 0 ? Mth.square(ChunkProgressListener.calculateDiameter(i)) : 0;

		while (serverChunkCache.getTickingGenerated() < j) {
			this.nextTickTimeNanos = Util.getNanos() + PREPARE_LEVELS_DEFAULT_DELAY_NANOS;
			this.waitUntilNextTick();
		}

		this.nextTickTimeNanos = Util.getNanos() + PREPARE_LEVELS_DEFAULT_DELAY_NANOS;
		this.waitUntilNextTick();

		for (ServerLevel serverLevel2 : this.levels.values()) {
			ForcedChunksSavedData forcedChunksSavedData = serverLevel2.getDataStorage().get(ForcedChunksSavedData.factory(), "chunks");
			if (forcedChunksSavedData != null) {
				LongIterator longIterator = forcedChunksSavedData.getChunks().iterator();

				while (longIterator.hasNext()) {
					long l = longIterator.nextLong();
					ChunkPos chunkPos = new ChunkPos(l);
					serverLevel2.getChunkSource().updateChunkForced(chunkPos, true);
				}
			}
		}

		this.nextTickTimeNanos = Util.getNanos() + PREPARE_LEVELS_DEFAULT_DELAY_NANOS;
		this.waitUntilNextTick();
		chunkProgressListener.stop();
		this.updateMobSpawningFlags();
	}

	public GameType getDefaultGameType() {
		return this.worldData.getGameType();
	}

	public boolean isHardcore() {
		return this.worldData.isHardcore();
	}

	public abstract int getOperatorUserPermissionLevel();

	public abstract int getFunctionCompilationLevel();

	public abstract boolean shouldRconBroadcast();

	public boolean saveAllChunks(boolean bl, boolean bl2, boolean bl3) {
		boolean bl4 = false;

		for (ServerLevel serverLevel : this.getAllLevels()) {
			if (!bl) {
				LOGGER.info("Saving chunks for level '{}'/{}", serverLevel, serverLevel.dimension().location());
			}

			serverLevel.save(null, bl2, serverLevel.noSave && !bl3);
			bl4 = true;
		}

		ServerLevel serverLevel2 = this.overworld();
		ServerLevelData serverLevelData = this.worldData.overworldData();
		serverLevelData.setWorldBorder(serverLevel2.getWorldBorder().createSettings());
		this.worldData.setCustomBossEvents(this.getCustomBossEvents().save(this.registryAccess()));
		this.storageSource.saveDataTag(this.registryAccess(), this.worldData, this.getPlayerList().getSingleplayerData());
		if (bl2) {
			for (ServerLevel serverLevel3 : this.getAllLevels()) {
				LOGGER.info("ThreadedAnvilChunkStorage ({}): All chunks are saved", serverLevel3.getChunkSource().chunkMap.getStorageName());
			}

			LOGGER.info("ThreadedAnvilChunkStorage: All dimensions are saved");
		}

		return bl4;
	}

	public boolean saveEverything(boolean bl, boolean bl2, boolean bl3) {
		boolean var4;
		try {
			this.isSaving = true;
			this.getPlayerList().saveAll();
			var4 = this.saveAllChunks(bl, bl2, bl3);
		} finally {
			this.isSaving = false;
		}

		return var4;
	}

	@Override
	public void close() {
		this.stopServer();
	}

	public void stopServer() {
		if (this.metricsRecorder.isRecording()) {
			this.cancelRecordingMetrics();
		}

		LOGGER.info("Stopping server");
		this.getConnection().stop();
		this.isSaving = true;
		if (this.playerList != null) {
			LOGGER.info("Saving players");
			this.playerList.saveAll();
			this.playerList.removeAll();
		}

		LOGGER.info("Saving worlds");

		for (ServerLevel serverLevel : this.getAllLevels()) {
			if (serverLevel != null) {
				serverLevel.noSave = false;
			}
		}

		while (this.levels.values().stream().anyMatch(serverLevelx -> serverLevelx.getChunkSource().chunkMap.hasWork())) {
			this.nextTickTimeNanos = Util.getNanos() + TimeUtil.NANOSECONDS_PER_MILLISECOND;

			for (ServerLevel serverLevelx : this.getAllLevels()) {
				serverLevelx.getChunkSource().removeTicketsOnClosing();
				serverLevelx.getChunkSource().tick(() -> true, false);
			}

			this.waitUntilNextTick();
		}

		this.saveAllChunks(false, true, false);

		for (ServerLevel serverLevelx : this.getAllLevels()) {
			if (serverLevelx != null) {
				try {
					serverLevelx.close();
				} catch (IOException var5) {
					LOGGER.error("Exception closing the level", (Throwable)var5);
				}
			}
		}

		this.isSaving = false;
		this.resources.close();

		try {
			this.storageSource.close();
		} catch (IOException var4) {
			LOGGER.error("Failed to unlock level {}", this.storageSource.getLevelId(), var4);
		}
	}

	public String getLocalIp() {
		return this.localIp;
	}

	public void setLocalIp(String string) {
		this.localIp = string;
	}

	public boolean isRunning() {
		return this.running;
	}

	public void halt(boolean bl) {
		this.running = false;
		if (bl) {
			try {
				this.serverThread.join();
			} catch (InterruptedException var3) {
				LOGGER.error("Error while shutting down", (Throwable)var3);
			}
		}
	}

	protected void runServer() {
		try {
			if (!this.initServer()) {
				throw new IllegalStateException("Failed to initialize server");
			}

			this.nextTickTimeNanos = Util.getNanos();
			this.statusIcon = (ServerStatus.Favicon)this.loadStatusIcon().orElse(null);
			this.status = this.buildServerStatus();

			while (this.running) {
				long l;
				if (!this.isPaused() && this.tickRateManager.isSprinting() && this.tickRateManager.checkShouldSprintThisTick()) {
					l = 0L;
					this.nextTickTimeNanos = Util.getNanos();
					this.lastOverloadWarningNanos = this.nextTickTimeNanos;
				} else {
					l = this.tickRateManager.nanosecondsPerTick();
					long m = Util.getNanos() - this.nextTickTimeNanos;
					if (m > OVERLOADED_THRESHOLD_NANOS + 20L * l && this.nextTickTimeNanos - this.lastOverloadWarningNanos >= OVERLOADED_WARNING_INTERVAL_NANOS + 100L * l) {
						long n = m / l;
						LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", m / TimeUtil.NANOSECONDS_PER_MILLISECOND, n);
						this.nextTickTimeNanos += n * l;
						this.lastOverloadWarningNanos = this.nextTickTimeNanos;
					}
				}

				boolean bl = l == 0L;
				if (this.debugCommandProfilerDelayStart) {
					this.debugCommandProfilerDelayStart = false;
					this.debugCommandProfiler = new MinecraftServer.TimeProfiler(Util.getNanos(), this.tickCount);
				}

				this.nextTickTimeNanos += l;
				this.startMetricsRecordingTick();
				this.profiler.push("tick");
				this.tickServer(bl ? () -> false : this::haveTime);
				this.profiler.popPush("nextTickWait");
				this.mayHaveDelayedTasks = true;
				this.delayedTasksMaxNextTickTimeNanos = Math.max(Util.getNanos() + l, this.nextTickTimeNanos);
				this.startMeasuringTaskExecutionTime();
				this.waitUntilNextTick();
				this.finishMeasuringTaskExecutionTime();
				if (bl) {
					this.tickRateManager.endTickWork();
				}

				this.profiler.pop();
				this.logFullTickTime();
				this.endMetricsRecordingTick();
				this.isReady = true;
				JvmProfiler.INSTANCE.onServerTick(this.smoothedTickTimeMillis);
			}
		} catch (Throwable var46) {
			LOGGER.error("Encountered an unexpected exception", var46);
			CrashReport crashReport = constructOrExtractCrashReport(var46);
			this.fillSystemReport(crashReport.getSystemReport());
			File file = new File(new File(this.getServerDirectory(), "crash-reports"), "crash-" + Util.getFilenameFormattedDateTime() + "-server.txt");
			if (crashReport.saveToFile(file)) {
				LOGGER.error("This crash report has been saved to: {}", file.getAbsolutePath());
			} else {
				LOGGER.error("We were unable to save this crash report to disk.");
			}

			this.onServerCrash(crashReport);
		} finally {
			try {
				this.stopped = true;
				this.stopServer();
			} catch (Throwable var44) {
				LOGGER.error("Exception stopping the server", var44);
			} finally {
				if (this.services.profileCache() != null) {
					this.services.profileCache().clearExecutor();
				}

				this.onServerExit();
			}
		}
	}

	private void logFullTickTime() {
		long l = Util.getNanos();
		if (this.isTickTimeLoggingEnabled()) {
			this.getTickTimeLogger().logSample(l - this.lastTickNanos);
		}

		this.lastTickNanos = l;
	}

	private void startMeasuringTaskExecutionTime() {
		if (this.isTickTimeLoggingEnabled()) {
			this.taskExecutionStartNanos = Util.getNanos();
			this.idleTimeNanos = 0L;
		}
	}

	private void finishMeasuringTaskExecutionTime() {
		if (this.isTickTimeLoggingEnabled()) {
			SampleLogger sampleLogger = this.getTickTimeLogger();
			sampleLogger.logPartialSample(Util.getNanos() - this.taskExecutionStartNanos - this.idleTimeNanos, TpsDebugDimensions.SCHEDULED_TASKS.ordinal());
			sampleLogger.logPartialSample(this.idleTimeNanos, TpsDebugDimensions.IDLE.ordinal());
		}
	}

	private static CrashReport constructOrExtractCrashReport(Throwable throwable) {
		ReportedException reportedException = null;

		for (Throwable throwable2 = throwable; throwable2 != null; throwable2 = throwable2.getCause()) {
			if (throwable2 instanceof ReportedException reportedException2) {
				reportedException = reportedException2;
			}
		}

		CrashReport crashReport;
		if (reportedException != null) {
			crashReport = reportedException.getReport();
			if (reportedException != throwable) {
				crashReport.addCategory("Wrapped in").setDetailError("Wrapping exception", throwable);
			}
		} else {
			crashReport = new CrashReport("Exception in server tick loop", throwable);
		}

		return crashReport;
	}

	private boolean haveTime() {
		return this.runningTask() || Util.getNanos() < (this.mayHaveDelayedTasks ? this.delayedTasksMaxNextTickTimeNanos : this.nextTickTimeNanos);
	}

	protected void waitUntilNextTick() {
		this.runAllTasks();
		this.managedBlock(() -> !this.haveTime());
	}

	@Override
	public void waitForTasks() {
		boolean bl = this.isTickTimeLoggingEnabled();
		long l = bl ? Util.getNanos() : 0L;
		super.waitForTasks();
		if (bl) {
			this.idleTimeNanos = this.idleTimeNanos + (Util.getNanos() - l);
		}
	}

	protected TickTask wrapRunnable(Runnable runnable) {
		return new TickTask(this.tickCount, runnable);
	}

	protected boolean shouldRun(TickTask tickTask) {
		return tickTask.getTick() + 3 < this.tickCount || this.haveTime();
	}

	@Override
	public boolean pollTask() {
		boolean bl = this.pollTaskInternal();
		this.mayHaveDelayedTasks = bl;
		return bl;
	}

	private boolean pollTaskInternal() {
		if (super.pollTask()) {
			return true;
		} else {
			if (this.tickRateManager.isSprinting() || this.haveTime()) {
				for (ServerLevel serverLevel : this.getAllLevels()) {
					if (serverLevel.getChunkSource().pollTask()) {
						return true;
					}
				}
			}

			return false;
		}
	}

	protected void doRunTask(TickTask tickTask) {
		this.getProfiler().incrementCounter("runTask");
		super.doRunTask(tickTask);
	}

	private Optional<ServerStatus.Favicon> loadStatusIcon() {
		Optional<Path> optional = Optional.of(this.getFile("server-icon.png").toPath())
			.filter(path -> Files.isRegularFile(path, new LinkOption[0]))
			.or(() -> this.storageSource.getIconFile().filter(path -> Files.isRegularFile(path, new LinkOption[0])));
		return optional.flatMap(path -> {
			try {
				BufferedImage bufferedImage = ImageIO.read(path.toFile());
				Preconditions.checkState(bufferedImage.getWidth() == 64, "Must be 64 pixels wide");
				Preconditions.checkState(bufferedImage.getHeight() == 64, "Must be 64 pixels high");
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				ImageIO.write(bufferedImage, "PNG", byteArrayOutputStream);
				return Optional.of(new ServerStatus.Favicon(byteArrayOutputStream.toByteArray()));
			} catch (Exception var3) {
				LOGGER.error("Couldn't load server icon", (Throwable)var3);
				return Optional.empty();
			}
		});
	}

	public Optional<Path> getWorldScreenshotFile() {
		return this.storageSource.getIconFile();
	}

	public File getServerDirectory() {
		return new File(".");
	}

	public void onServerCrash(CrashReport crashReport) {
	}

	public void onServerExit() {
	}

	public boolean isPaused() {
		return false;
	}

	public void tickServer(BooleanSupplier booleanSupplier) {
		long l = Util.getNanos();
		this.tickCount++;
		this.tickRateManager.tick();
		this.tickChildren(booleanSupplier);
		if (l - this.lastServerStatus >= STATUS_EXPIRE_TIME_NANOS) {
			this.lastServerStatus = l;
			this.status = this.buildServerStatus();
		}

		this.ticksUntilAutosave--;
		if (this.ticksUntilAutosave <= 0) {
			this.ticksUntilAutosave = this.computeNextAutosaveInterval();
			LOGGER.debug("Autosave started");
			this.profiler.push("save");
			this.saveEverything(true, false, false);
			this.profiler.pop();
			LOGGER.debug("Autosave finished");
		}

		this.profiler.push("tallying");
		long m = Util.getNanos() - l;
		int i = this.tickCount % 100;
		this.aggregatedTickTimesNanos = this.aggregatedTickTimesNanos - this.tickTimesNanos[i];
		this.aggregatedTickTimesNanos += m;
		this.tickTimesNanos[i] = m;
		this.smoothedTickTimeMillis = this.smoothedTickTimeMillis * 0.8F + (float)m / (float)TimeUtil.NANOSECONDS_PER_MILLISECOND * 0.19999999F;
		this.logTickMethodTime(l);
		this.profiler.pop();
	}

	private void logTickMethodTime(long l) {
		if (this.isTickTimeLoggingEnabled()) {
			this.getTickTimeLogger().logPartialSample(Util.getNanos() - l, TpsDebugDimensions.TICK_SERVER_METHOD.ordinal());
		}
	}

	private int computeNextAutosaveInterval() {
		float f;
		if (this.tickRateManager.isSprinting()) {
			long l = this.getAverageTickTimeNanos() + 1L;
			f = (float)TimeUtil.NANOSECONDS_PER_SECOND / (float)l;
		} else {
			f = this.tickRateManager.tickrate();
		}

		int i = 300;
		return Math.max(100, (int)(f * 300.0F));
	}

	public void onTickRateChanged() {
		int i = this.computeNextAutosaveInterval();
		if (i < this.ticksUntilAutosave) {
			this.ticksUntilAutosave = i;
		}
	}

	protected abstract SampleLogger getTickTimeLogger();

	public abstract boolean isTickTimeLoggingEnabled();

	private ServerStatus buildServerStatus() {
		ServerStatus.Players players = this.buildPlayerStatus();
		return new ServerStatus(
			Component.nullToEmpty(this.motd),
			Optional.of(players),
			Optional.of(ServerStatus.Version.current()),
			Optional.ofNullable(this.statusIcon),
			this.enforceSecureProfile()
		);
	}

	private ServerStatus.Players buildPlayerStatus() {
		List<ServerPlayer> list = this.playerList.getPlayers();
		int i = this.getMaxPlayers();
		if (this.hidesOnlinePlayers()) {
			return new ServerStatus.Players(i, list.size(), List.of());
		} else {
			int j = Math.min(list.size(), 12);
			ObjectArrayList<GameProfile> objectArrayList = new ObjectArrayList<>(j);
			int k = Mth.nextInt(this.random, 0, list.size() - j);

			for (int l = 0; l < j; l++) {
				ServerPlayer serverPlayer = (ServerPlayer)list.get(k + l);
				objectArrayList.add(serverPlayer.allowsListing() ? serverPlayer.getGameProfile() : ANONYMOUS_PLAYER_PROFILE);
			}

			Util.shuffle(objectArrayList, this.random);
			return new ServerStatus.Players(i, list.size(), objectArrayList);
		}
	}

	public void tickChildren(BooleanSupplier booleanSupplier) {
		this.getPlayerList().getPlayers().forEach(serverPlayerx -> serverPlayerx.connection.suspendFlushing());
		this.profiler.push("commandFunctions");
		this.getFunctions().tick();
		this.profiler.popPush("levels");

		for (ServerLevel serverLevel : this.getAllLevels()) {
			this.profiler.push((Supplier<String>)(() -> serverLevel + " " + serverLevel.dimension().location()));
			if (this.tickCount % 20 == 0) {
				this.profiler.push("timeSync");
				this.synchronizeTime(serverLevel);
				this.profiler.pop();
			}

			this.profiler.push("tick");

			try {
				serverLevel.tick(booleanSupplier);
			} catch (Throwable var6) {
				CrashReport crashReport = CrashReport.forThrowable(var6, "Exception ticking world");
				serverLevel.fillReportDetails(crashReport);
				throw new ReportedException(crashReport);
			}

			this.profiler.pop();
			this.profiler.pop();
		}

		this.profiler.popPush("connection");
		this.getConnection().tick();
		this.profiler.popPush("players");
		this.playerList.tick();
		if (SharedConstants.IS_RUNNING_IN_IDE && this.tickRateManager.runsNormally()) {
			GameTestTicker.SINGLETON.tick();
		}

		this.profiler.popPush("server gui refresh");

		for (int i = 0; i < this.tickables.size(); i++) {
			((Runnable)this.tickables.get(i)).run();
		}

		this.profiler.popPush("send chunks");

		for (ServerPlayer serverPlayer : this.playerList.getPlayers()) {
			serverPlayer.connection.chunkSender.sendNextChunks(serverPlayer);
			serverPlayer.connection.resumeFlushing();
		}

		this.profiler.pop();
	}

	private void synchronizeTime(ServerLevel serverLevel) {
		this.playerList
			.broadcastAll(
				new ClientboundSetTimePacket(serverLevel.getGameTime(), serverLevel.getDayTime(), serverLevel.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)),
				serverLevel.dimension()
			);
	}

	public void forceTimeSynchronization() {
		this.profiler.push("timeSync");

		for (ServerLevel serverLevel : this.getAllLevels()) {
			this.synchronizeTime(serverLevel);
		}

		this.profiler.pop();
	}

	public boolean isNetherEnabled() {
		return true;
	}

	public void addTickable(Runnable runnable) {
		this.tickables.add(runnable);
	}

	protected void setId(String string) {
		this.serverId = string;
	}

	public boolean isShutdown() {
		return !this.serverThread.isAlive();
	}

	public File getFile(String string) {
		return new File(this.getServerDirectory(), string);
	}

	public final ServerLevel overworld() {
		return (ServerLevel)this.levels.get(Level.OVERWORLD);
	}

	@Nullable
	public ServerLevel getLevel(ResourceKey<Level> resourceKey) {
		return (ServerLevel)this.levels.get(resourceKey);
	}

	public Set<ResourceKey<Level>> levelKeys() {
		return this.levels.keySet();
	}

	public Iterable<ServerLevel> getAllLevels() {
		return this.levels.values();
	}

	@Override
	public String getServerVersion() {
		return SharedConstants.getCurrentVersion().getName();
	}

	@Override
	public int getPlayerCount() {
		return this.playerList.getPlayerCount();
	}

	@Override
	public int getMaxPlayers() {
		return this.playerList.getMaxPlayers();
	}

	public String[] getPlayerNames() {
		return this.playerList.getPlayerNamesArray();
	}

	@DontObfuscate
	public String getServerModName() {
		return "vanilla";
	}

	public SystemReport fillSystemReport(SystemReport systemReport) {
		systemReport.setDetail("Server Running", (Supplier<String>)(() -> Boolean.toString(this.running)));
		if (this.playerList != null) {
			systemReport.setDetail(
				"Player Count", (Supplier<String>)(() -> this.playerList.getPlayerCount() + " / " + this.playerList.getMaxPlayers() + "; " + this.playerList.getPlayers())
			);
		}

		systemReport.setDetail("Active Data Packs", (Supplier<String>)(() -> PackRepository.displayPackList(this.packRepository.getSelectedPacks())));
		systemReport.setDetail("Available Data Packs", (Supplier<String>)(() -> PackRepository.displayPackList(this.packRepository.getAvailablePacks())));
		systemReport.setDetail(
			"Enabled Feature Flags",
			(Supplier<String>)(() -> (String)FeatureFlags.REGISTRY
					.toNames(this.worldData.enabledFeatures())
					.stream()
					.map(ResourceLocation::toString)
					.collect(Collectors.joining(", ")))
		);
		systemReport.setDetail("World Generation", (Supplier<String>)(() -> this.worldData.worldGenSettingsLifecycle().toString()));
		systemReport.setDetail("World Seed", (Supplier<String>)(() -> String.valueOf(this.worldData.worldGenOptions().seed())));
		if (this.serverId != null) {
			systemReport.setDetail("Server Id", (Supplier<String>)(() -> this.serverId));
		}

		return this.fillServerSystemReport(systemReport);
	}

	public abstract SystemReport fillServerSystemReport(SystemReport systemReport);

	public ModCheck getModdedStatus() {
		return ModCheck.identify("vanilla", this::getServerModName, "Server", MinecraftServer.class);
	}

	@Override
	public void sendSystemMessage(Component component) {
		LOGGER.info(component.getString());
	}

	public KeyPair getKeyPair() {
		return this.keyPair;
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int i) {
		this.port = i;
	}

	@Nullable
	public GameProfile getSingleplayerProfile() {
		return this.singleplayerProfile;
	}

	public void setSingleplayerProfile(@Nullable GameProfile gameProfile) {
		this.singleplayerProfile = gameProfile;
	}

	public boolean isSingleplayer() {
		return this.singleplayerProfile != null;
	}

	protected void initializeKeyPair() {
		LOGGER.info("Generating keypair");

		try {
			this.keyPair = Crypt.generateKeyPair();
		} catch (CryptException var2) {
			throw new IllegalStateException("Failed to generate key pair", var2);
		}
	}

	public void setDifficulty(Difficulty difficulty, boolean bl) {
		if (bl || !this.worldData.isDifficultyLocked()) {
			this.worldData.setDifficulty(this.worldData.isHardcore() ? Difficulty.HARD : difficulty);
			this.updateMobSpawningFlags();
			this.getPlayerList().getPlayers().forEach(this::sendDifficultyUpdate);
		}
	}

	public int getScaledTrackingDistance(int i) {
		return i;
	}

	private void updateMobSpawningFlags() {
		for (ServerLevel serverLevel : this.getAllLevels()) {
			serverLevel.setSpawnSettings(this.isSpawningMonsters(), this.isSpawningAnimals());
		}
	}

	public void setDifficultyLocked(boolean bl) {
		this.worldData.setDifficultyLocked(bl);
		this.getPlayerList().getPlayers().forEach(this::sendDifficultyUpdate);
	}

	private void sendDifficultyUpdate(ServerPlayer serverPlayer) {
		LevelData levelData = serverPlayer.level().getLevelData();
		serverPlayer.connection.send(new ClientboundChangeDifficultyPacket(levelData.getDifficulty(), levelData.isDifficultyLocked()));
	}

	public boolean isSpawningMonsters() {
		return this.worldData.getDifficulty() != Difficulty.PEACEFUL;
	}

	public boolean isDemo() {
		return this.isDemo;
	}

	public void setDemo(boolean bl) {
		this.isDemo = bl;
	}

	public Optional<MinecraftServer.ServerResourcePackInfo> getServerResourcePack() {
		return Optional.empty();
	}

	public boolean isResourcePackRequired() {
		return this.getServerResourcePack().filter(MinecraftServer.ServerResourcePackInfo::isRequired).isPresent();
	}

	public abstract boolean isDedicatedServer();

	public abstract int getRateLimitPacketsPerSecond();

	public boolean usesAuthentication() {
		return this.onlineMode;
	}

	public void setUsesAuthentication(boolean bl) {
		this.onlineMode = bl;
	}

	public boolean getPreventProxyConnections() {
		return this.preventProxyConnections;
	}

	public void setPreventProxyConnections(boolean bl) {
		this.preventProxyConnections = bl;
	}

	public boolean isSpawningAnimals() {
		return true;
	}

	public boolean areNpcsEnabled() {
		return true;
	}

	public abstract boolean isEpollEnabled();

	public boolean isPvpAllowed() {
		return this.pvp;
	}

	public void setPvpAllowed(boolean bl) {
		this.pvp = bl;
	}

	public boolean isFlightAllowed() {
		return this.allowFlight;
	}

	public void setFlightAllowed(boolean bl) {
		this.allowFlight = bl;
	}

	public abstract boolean isCommandBlockEnabled();

	@Override
	public String getMotd() {
		return this.motd;
	}

	public void setMotd(String string) {
		this.motd = string;
	}

	public boolean isStopped() {
		return this.stopped;
	}

	public PlayerList getPlayerList() {
		return this.playerList;
	}

	public void setPlayerList(PlayerList playerList) {
		this.playerList = playerList;
	}

	public abstract boolean isPublished();

	public void setDefaultGameType(GameType gameType) {
		this.worldData.setGameType(gameType);
	}

	public ServerConnectionListener getConnection() {
		return this.connection;
	}

	public boolean isReady() {
		return this.isReady;
	}

	public boolean hasGui() {
		return false;
	}

	public boolean publishServer(@Nullable GameType gameType, boolean bl, int i) {
		return false;
	}

	public int getTickCount() {
		return this.tickCount;
	}

	public int getSpawnProtectionRadius() {
		return 16;
	}

	public boolean isUnderSpawnProtection(ServerLevel serverLevel, BlockPos blockPos, Player player) {
		return false;
	}

	public boolean repliesToStatus() {
		return true;
	}

	public boolean hidesOnlinePlayers() {
		return false;
	}

	public Proxy getProxy() {
		return this.proxy;
	}

	public int getPlayerIdleTimeout() {
		return this.playerIdleTimeout;
	}

	public void setPlayerIdleTimeout(int i) {
		this.playerIdleTimeout = i;
	}

	public MinecraftSessionService getSessionService() {
		return this.services.sessionService();
	}

	@Nullable
	public SignatureValidator getProfileKeySignatureValidator() {
		return this.services.profileKeySignatureValidator();
	}

	public GameProfileRepository getProfileRepository() {
		return this.services.profileRepository();
	}

	@Nullable
	public GameProfileCache getProfileCache() {
		return this.services.profileCache();
	}

	@Nullable
	public ServerStatus getStatus() {
		return this.status;
	}

	public void invalidateStatus() {
		this.lastServerStatus = 0L;
	}

	public int getAbsoluteMaxWorldSize() {
		return 29999984;
	}

	@Override
	public boolean scheduleExecutables() {
		return super.scheduleExecutables() && !this.isStopped();
	}

	@Override
	public void executeIfPossible(Runnable runnable) {
		if (this.isStopped()) {
			throw new RejectedExecutionException("Server already shutting down");
		} else {
			super.executeIfPossible(runnable);
		}
	}

	@Override
	public Thread getRunningThread() {
		return this.serverThread;
	}

	public int getCompressionThreshold() {
		return 256;
	}

	public boolean enforceSecureProfile() {
		return false;
	}

	public long getNextTickTime() {
		return this.nextTickTimeNanos;
	}

	public DataFixer getFixerUpper() {
		return this.fixerUpper;
	}

	public int getSpawnRadius(@Nullable ServerLevel serverLevel) {
		return serverLevel != null ? serverLevel.getGameRules().getInt(GameRules.RULE_SPAWN_RADIUS) : 10;
	}

	public ServerAdvancementManager getAdvancements() {
		return this.resources.managers.getAdvancements();
	}

	public ServerFunctionManager getFunctions() {
		return this.functionManager;
	}

	public CompletableFuture<Void> reloadResources(Collection<String> collection) {
		CompletableFuture<Void> completableFuture = CompletableFuture.supplyAsync(
				() -> (ImmutableList)collection.stream()
						.map(this.packRepository::getPack)
						.filter(Objects::nonNull)
						.map(Pack::open)
						.collect(ImmutableList.toImmutableList()),
				this
			)
			.thenCompose(
				immutableList -> {
					CloseableResourceManager closeableResourceManager = new MultiPackResourceManager(PackType.SERVER_DATA, immutableList);
					return ReloadableServerResources.loadResources(
							closeableResourceManager,
							this.registries,
							this.worldData.enabledFeatures(),
							this.isDedicatedServer() ? Commands.CommandSelection.DEDICATED : Commands.CommandSelection.INTEGRATED,
							this.getFunctionCompilationLevel(),
							this.executor,
							this
						)
						.whenComplete((reloadableServerResources, throwable) -> {
							if (throwable != null) {
								closeableResourceManager.close();
							}
						})
						.thenApply(reloadableServerResources -> new MinecraftServer.ReloadableResources(closeableResourceManager, reloadableServerResources));
				}
			)
			.thenAcceptAsync(reloadableResources -> {
				this.resources.close();
				this.resources = reloadableResources;
				this.packRepository.setSelected(collection);
				WorldDataConfiguration worldDataConfiguration = new WorldDataConfiguration(getSelectedPacks(this.packRepository, true), this.worldData.enabledFeatures());
				this.worldData.setDataConfiguration(worldDataConfiguration);
				this.resources.managers.updateRegistryTags();
				this.getPlayerList().saveAll();
				this.getPlayerList().reloadResources();
				this.functionManager.replaceLibrary(this.resources.managers.getFunctionLibrary());
				this.structureTemplateManager.onResourceManagerReload(this.resources.resourceManager);
			}, this);
		if (this.isSameThread()) {
			this.managedBlock(completableFuture::isDone);
		}

		return completableFuture;
	}

	public static WorldDataConfiguration configurePackRepository(
		PackRepository packRepository, WorldDataConfiguration worldDataConfiguration, boolean bl, boolean bl2
	) {
		DataPackConfig dataPackConfig = worldDataConfiguration.dataPacks();
		FeatureFlagSet featureFlagSet = bl ? FeatureFlagSet.of() : worldDataConfiguration.enabledFeatures();
		FeatureFlagSet featureFlagSet2 = bl ? FeatureFlags.REGISTRY.allFlags() : worldDataConfiguration.enabledFeatures();
		packRepository.reload();
		if (bl2) {
			return configureRepositoryWithSelection(packRepository, List.of("vanilla"), featureFlagSet, false);
		} else {
			Set<String> set = Sets.<String>newLinkedHashSet();

			for (String string : dataPackConfig.getEnabled()) {
				if (packRepository.isAvailable(string)) {
					set.add(string);
				} else {
					LOGGER.warn("Missing data pack {}", string);
				}
			}

			for (Pack pack : packRepository.getAvailablePacks()) {
				String string2 = pack.getId();
				if (!dataPackConfig.getDisabled().contains(string2)) {
					FeatureFlagSet featureFlagSet3 = pack.getRequestedFeatures();
					boolean bl3 = set.contains(string2);
					if (!bl3 && pack.getPackSource().shouldAddAutomatically()) {
						if (featureFlagSet3.isSubsetOf(featureFlagSet2)) {
							LOGGER.info("Found new data pack {}, loading it automatically", string2);
							set.add(string2);
						} else {
							LOGGER.info(
								"Found new data pack {}, but can't load it due to missing features {}", string2, FeatureFlags.printMissingFlags(featureFlagSet2, featureFlagSet3)
							);
						}
					}

					if (bl3 && !featureFlagSet3.isSubsetOf(featureFlagSet2)) {
						LOGGER.warn(
							"Pack {} requires features {} that are not enabled for this world, disabling pack.",
							string2,
							FeatureFlags.printMissingFlags(featureFlagSet2, featureFlagSet3)
						);
						set.remove(string2);
					}
				}
			}

			if (set.isEmpty()) {
				LOGGER.info("No datapacks selected, forcing vanilla");
				set.add("vanilla");
			}

			return configureRepositoryWithSelection(packRepository, set, featureFlagSet, true);
		}
	}

	private static WorldDataConfiguration configureRepositoryWithSelection(
		PackRepository packRepository, Collection<String> collection, FeatureFlagSet featureFlagSet, boolean bl
	) {
		packRepository.setSelected(collection);
		enableForcedFeaturePacks(packRepository, featureFlagSet);
		DataPackConfig dataPackConfig = getSelectedPacks(packRepository, bl);
		FeatureFlagSet featureFlagSet2 = packRepository.getRequestedFeatureFlags().join(featureFlagSet);
		return new WorldDataConfiguration(dataPackConfig, featureFlagSet2);
	}

	private static void enableForcedFeaturePacks(PackRepository packRepository, FeatureFlagSet featureFlagSet) {
		FeatureFlagSet featureFlagSet2 = packRepository.getRequestedFeatureFlags();
		FeatureFlagSet featureFlagSet3 = featureFlagSet.subtract(featureFlagSet2);
		if (!featureFlagSet3.isEmpty()) {
			Set<String> set = new ObjectArraySet<>(packRepository.getSelectedIds());

			for (Pack pack : packRepository.getAvailablePacks()) {
				if (featureFlagSet3.isEmpty()) {
					break;
				}

				if (pack.getPackSource() == PackSource.FEATURE) {
					String string = pack.getId();
					FeatureFlagSet featureFlagSet4 = pack.getRequestedFeatures();
					if (!featureFlagSet4.isEmpty() && featureFlagSet4.intersects(featureFlagSet3) && featureFlagSet4.isSubsetOf(featureFlagSet)) {
						if (!set.add(string)) {
							throw new IllegalStateException("Tried to force '" + string + "', but it was already enabled");
						}

						LOGGER.info("Found feature pack ('{}') for requested feature, forcing to enabled", string);
						featureFlagSet3 = featureFlagSet3.subtract(featureFlagSet4);
					}
				}
			}

			packRepository.setSelected(set);
		}
	}

	private static DataPackConfig getSelectedPacks(PackRepository packRepository, boolean bl) {
		Collection<String> collection = packRepository.getSelectedIds();
		List<String> list = ImmutableList.copyOf(collection);
		List<String> list2 = bl ? packRepository.getAvailableIds().stream().filter(string -> !collection.contains(string)).toList() : List.of();
		return new DataPackConfig(list, list2);
	}

	public void kickUnlistedPlayers(CommandSourceStack commandSourceStack) {
		if (this.isEnforceWhitelist()) {
			PlayerList playerList = commandSourceStack.getServer().getPlayerList();
			UserWhiteList userWhiteList = playerList.getWhiteList();

			for (ServerPlayer serverPlayer : Lists.newArrayList(playerList.getPlayers())) {
				if (!userWhiteList.isWhiteListed(serverPlayer.getGameProfile())) {
					serverPlayer.connection.disconnect(Component.translatable("multiplayer.disconnect.not_whitelisted"));
				}
			}
		}
	}

	public PackRepository getPackRepository() {
		return this.packRepository;
	}

	public Commands getCommands() {
		return this.resources.managers.getCommands();
	}

	public CommandSourceStack createCommandSourceStack() {
		ServerLevel serverLevel = this.overworld();
		return new CommandSourceStack(
			this,
			serverLevel == null ? Vec3.ZERO : Vec3.atLowerCornerOf(serverLevel.getSharedSpawnPos()),
			Vec2.ZERO,
			serverLevel,
			4,
			"Server",
			Component.literal("Server"),
			this,
			null
		);
	}

	@Override
	public boolean acceptsSuccess() {
		return true;
	}

	@Override
	public boolean acceptsFailure() {
		return true;
	}

	@Override
	public abstract boolean shouldInformAdmins();

	public RecipeManager getRecipeManager() {
		return this.resources.managers.getRecipeManager();
	}

	public ServerScoreboard getScoreboard() {
		return this.scoreboard;
	}

	public CommandStorage getCommandStorage() {
		if (this.commandStorage == null) {
			throw new NullPointerException("Called before server init");
		} else {
			return this.commandStorage;
		}
	}

	public GameRules getGameRules() {
		return this.overworld().getGameRules();
	}

	public CustomBossEvents getCustomBossEvents() {
		return this.customBossEvents;
	}

	public boolean isEnforceWhitelist() {
		return this.enforceWhitelist;
	}

	public void setEnforceWhitelist(boolean bl) {
		this.enforceWhitelist = bl;
	}

	public float getCurrentSmoothedTickTime() {
		return this.smoothedTickTimeMillis;
	}

	public ServerTickRateManager tickRateManager() {
		return this.tickRateManager;
	}

	public long getAverageTickTimeNanos() {
		return this.aggregatedTickTimesNanos / (long)Math.min(100, Math.max(this.tickCount, 1));
	}

	public long[] getTickTimesNanos() {
		return this.tickTimesNanos;
	}

	public int getProfilePermissions(GameProfile gameProfile) {
		if (this.getPlayerList().isOp(gameProfile)) {
			ServerOpListEntry serverOpListEntry = this.getPlayerList().getOps().get(gameProfile);
			if (serverOpListEntry != null) {
				return serverOpListEntry.getLevel();
			} else if (this.isSingleplayerOwner(gameProfile)) {
				return 4;
			} else if (this.isSingleplayer()) {
				return this.getPlayerList().isAllowCommandsForAllPlayers() ? 4 : 0;
			} else {
				return this.getOperatorUserPermissionLevel();
			}
		} else {
			return 0;
		}
	}

	public ProfilerFiller getProfiler() {
		return this.profiler;
	}

	public abstract boolean isSingleplayerOwner(GameProfile gameProfile);

	public void dumpServerProperties(Path path) throws IOException {
	}

	private void saveDebugReport(Path path) {
		Path path2 = path.resolve("levels");

		try {
			for (Entry<ResourceKey<Level>, ServerLevel> entry : this.levels.entrySet()) {
				ResourceLocation resourceLocation = ((ResourceKey)entry.getKey()).location();
				Path path3 = path2.resolve(resourceLocation.getNamespace()).resolve(resourceLocation.getPath());
				Files.createDirectories(path3);
				((ServerLevel)entry.getValue()).saveDebugReport(path3);
			}

			this.dumpGameRules(path.resolve("gamerules.txt"));
			this.dumpClasspath(path.resolve("classpath.txt"));
			this.dumpMiscStats(path.resolve("stats.txt"));
			this.dumpThreads(path.resolve("threads.txt"));
			this.dumpServerProperties(path.resolve("server.properties.txt"));
			this.dumpNativeModules(path.resolve("modules.txt"));
		} catch (IOException var7) {
			LOGGER.warn("Failed to save debug report", (Throwable)var7);
		}
	}

	private void dumpMiscStats(Path path) throws IOException {
		Writer writer = Files.newBufferedWriter(path);

		try {
			writer.write(String.format(Locale.ROOT, "pending_tasks: %d\n", this.getPendingTasksCount()));
			writer.write(String.format(Locale.ROOT, "average_tick_time: %f\n", this.getCurrentSmoothedTickTime()));
			writer.write(String.format(Locale.ROOT, "tick_times: %s\n", Arrays.toString(this.tickTimesNanos)));
			writer.write(String.format(Locale.ROOT, "queue: %s\n", Util.backgroundExecutor()));
		} catch (Throwable var6) {
			if (writer != null) {
				try {
					writer.close();
				} catch (Throwable var5) {
					var6.addSuppressed(var5);
				}
			}

			throw var6;
		}

		if (writer != null) {
			writer.close();
		}
	}

	private void dumpGameRules(Path path) throws IOException {
		Writer writer = Files.newBufferedWriter(path);

		try {
			final List<String> list = Lists.<String>newArrayList();
			final GameRules gameRules = this.getGameRules();
			GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
				@Override
				public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
					list.add(String.format(Locale.ROOT, "%s=%s\n", key.getId(), gameRules.getRule(key)));
				}
			});

			for (String string : list) {
				writer.write(string);
			}
		} catch (Throwable var8) {
			if (writer != null) {
				try {
					writer.close();
				} catch (Throwable var7) {
					var8.addSuppressed(var7);
				}
			}

			throw var8;
		}

		if (writer != null) {
			writer.close();
		}
	}

	private void dumpClasspath(Path path) throws IOException {
		Writer writer = Files.newBufferedWriter(path);

		try {
			String string = System.getProperty("java.class.path");
			String string2 = System.getProperty("path.separator");

			for (String string3 : Splitter.on(string2).split(string)) {
				writer.write(string3);
				writer.write("\n");
			}
		} catch (Throwable var8) {
			if (writer != null) {
				try {
					writer.close();
				} catch (Throwable var7) {
					var8.addSuppressed(var7);
				}
			}

			throw var8;
		}

		if (writer != null) {
			writer.close();
		}
	}

	private void dumpThreads(Path path) throws IOException {
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(true, true);
		Arrays.sort(threadInfos, Comparator.comparing(ThreadInfo::getThreadName));
		Writer writer = Files.newBufferedWriter(path);

		try {
			for (ThreadInfo threadInfo : threadInfos) {
				writer.write(threadInfo.toString());
				writer.write(10);
			}
		} catch (Throwable var10) {
			if (writer != null) {
				try {
					writer.close();
				} catch (Throwable var9) {
					var10.addSuppressed(var9);
				}
			}

			throw var10;
		}

		if (writer != null) {
			writer.close();
		}
	}

	private void dumpNativeModules(Path path) throws IOException {
		Writer writer = Files.newBufferedWriter(path);

		label49: {
			try {
				label50: {
					List<NativeModuleLister.NativeModuleInfo> list;
					try {
						list = Lists.<NativeModuleLister.NativeModuleInfo>newArrayList(NativeModuleLister.listModules());
					} catch (Throwable var7) {
						LOGGER.warn("Failed to list native modules", var7);
						break label50;
					}

					list.sort(Comparator.comparing(nativeModuleInfox -> nativeModuleInfox.name));
					Iterator throwable = list.iterator();

					while (true) {
						if (!throwable.hasNext()) {
							break label49;
						}

						NativeModuleLister.NativeModuleInfo nativeModuleInfo = (NativeModuleLister.NativeModuleInfo)throwable.next();
						writer.write(nativeModuleInfo.toString());
						writer.write(10);
					}
				}
			} catch (Throwable var8) {
				if (writer != null) {
					try {
						writer.close();
					} catch (Throwable var6) {
						var8.addSuppressed(var6);
					}
				}

				throw var8;
			}

			if (writer != null) {
				writer.close();
			}

			return;
		}

		if (writer != null) {
			writer.close();
		}
	}

	private void startMetricsRecordingTick() {
		if (this.willStartRecordingMetrics) {
			this.metricsRecorder = ActiveMetricsRecorder.createStarted(
				new ServerMetricsSamplersProvider(Util.timeSource, this.isDedicatedServer()),
				Util.timeSource,
				Util.ioPool(),
				new MetricsPersister("server"),
				this.onMetricsRecordingStopped,
				path -> {
					this.executeBlocking(() -> this.saveDebugReport(path.resolve("server")));
					this.onMetricsRecordingFinished.accept(path);
				}
			);
			this.willStartRecordingMetrics = false;
		}

		this.profiler = SingleTickProfiler.decorateFiller(this.metricsRecorder.getProfiler(), SingleTickProfiler.createTickProfiler("Server"));
		this.metricsRecorder.startTick();
		this.profiler.startTick();
	}

	public void endMetricsRecordingTick() {
		this.profiler.endTick();
		this.metricsRecorder.endTick();
	}

	public boolean isRecordingMetrics() {
		return this.metricsRecorder.isRecording();
	}

	public void startRecordingMetrics(Consumer<ProfileResults> consumer, Consumer<Path> consumer2) {
		this.onMetricsRecordingStopped = profileResults -> {
			this.stopRecordingMetrics();
			consumer.accept(profileResults);
		};
		this.onMetricsRecordingFinished = consumer2;
		this.willStartRecordingMetrics = true;
	}

	public void stopRecordingMetrics() {
		this.metricsRecorder = InactiveMetricsRecorder.INSTANCE;
	}

	public void finishRecordingMetrics() {
		this.metricsRecorder.end();
	}

	public void cancelRecordingMetrics() {
		this.metricsRecorder.cancel();
		this.profiler = this.metricsRecorder.getProfiler();
	}

	public Path getWorldPath(LevelResource levelResource) {
		return this.storageSource.getLevelPath(levelResource);
	}

	public boolean forceSynchronousWrites() {
		return true;
	}

	public StructureTemplateManager getStructureManager() {
		return this.structureTemplateManager;
	}

	public WorldData getWorldData() {
		return this.worldData;
	}

	public RegistryAccess.Frozen registryAccess() {
		return this.registries.compositeAccess();
	}

	public LayeredRegistryAccess<RegistryLayer> registries() {
		return this.registries;
	}

	public ReloadableServerRegistries.Holder reloadableRegistries() {
		return this.resources.managers.fullRegistries();
	}

	public TextFilter createTextFilterForPlayer(ServerPlayer serverPlayer) {
		return TextFilter.DUMMY;
	}

	public ServerPlayerGameMode createGameModeForPlayer(ServerPlayer serverPlayer) {
		return (ServerPlayerGameMode)(this.isDemo() ? new DemoMode(serverPlayer) : new ServerPlayerGameMode(serverPlayer));
	}

	@Nullable
	public GameType getForcedGameType() {
		return null;
	}

	public ResourceManager getResourceManager() {
		return this.resources.resourceManager;
	}

	public boolean isCurrentlySaving() {
		return this.isSaving;
	}

	public boolean isTimeProfilerRunning() {
		return this.debugCommandProfilerDelayStart || this.debugCommandProfiler != null;
	}

	public void startTimeProfiler() {
		this.debugCommandProfilerDelayStart = true;
	}

	public ProfileResults stopTimeProfiler() {
		if (this.debugCommandProfiler == null) {
			return EmptyProfileResults.EMPTY;
		} else {
			ProfileResults profileResults = this.debugCommandProfiler.stop(Util.getNanos(), this.tickCount);
			this.debugCommandProfiler = null;
			return profileResults;
		}
	}

	public int getMaxChainedNeighborUpdates() {
		return 1000000;
	}

	public void logChatMessage(Component component, ChatType.Bound bound, @Nullable String string) {
		String string2 = bound.decorate(component).getString();
		if (string != null) {
			LOGGER.info("[{}] {}", string, string2);
		} else {
			LOGGER.info("{}", string2);
		}
	}

	public ChatDecorator getChatDecorator() {
		return ChatDecorator.PLAIN;
	}

	public boolean logIPs() {
		return true;
	}

	public void subscribeToDebugSample(ServerPlayer serverPlayer, RemoteDebugSampleType remoteDebugSampleType) {
	}

	public boolean acceptsTransfers() {
		return false;
	}

	public void reportChunkLoadFailure(ChunkPos chunkPos) {
	}

	public void reportChunkSaveFailure(ChunkPos chunkPos) {
	}

	public PotionBrewing potionBrewing() {
		return this.potionBrewing;
	}

	static record ReloadableResources(CloseableResourceManager resourceManager, ReloadableServerResources managers) implements AutoCloseable {

		public void close() {
			this.resourceManager.close();
		}
	}

	public static record ServerResourcePackInfo(UUID id, String url, String hash, boolean isRequired, @Nullable Component prompt) {
	}

	static class TimeProfiler {
		final long startNanos;
		final int startTick;

		TimeProfiler(long l, int i) {
			this.startNanos = l;
			this.startTick = i;
		}

		ProfileResults stop(long l, int i) {
			return new ProfileResults() {
				@Override
				public List<ResultField> getTimes(String string) {
					return Collections.emptyList();
				}

				@Override
				public boolean saveResults(Path path) {
					return false;
				}

				@Override
				public long getStartTimeNano() {
					return TimeProfiler.this.startNanos;
				}

				@Override
				public int getStartTimeTicks() {
					return TimeProfiler.this.startTick;
				}

				@Override
				public long getEndTimeNano() {
					return l;
				}

				@Override
				public int getEndTimeTicks() {
					return i;
				}

				@Override
				public String getProfilerResults() {
					return "";
				}
			};
		}
	}
}
