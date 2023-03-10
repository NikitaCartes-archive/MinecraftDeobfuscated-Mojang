/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedWriter;
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
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
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
import net.minecraft.core.HolderLookup;
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
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.Services;
import net.minecraft.server.TickTask;
import net.minecraft.server.WorldStem;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.DemoMode;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.network.TextFilter;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.ModCheck;
import net.minecraft.util.Mth;
import net.minecraft.util.NativeModuleLister;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.Unit;
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
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
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
import net.minecraft.world.level.storage.loot.ItemModifierManager;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.PredicateManager;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public abstract class MinecraftServer
extends ReentrantBlockableEventLoop<TickTask>
implements CommandSource,
AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String VANILLA_BRAND = "vanilla";
    private static final float AVERAGE_TICK_TIME_SMOOTHING = 0.8f;
    private static final int TICK_STATS_SPAN = 100;
    public static final int MS_PER_TICK = 50;
    private static final int OVERLOADED_THRESHOLD = 2000;
    private static final int OVERLOADED_WARNING_INTERVAL = 15000;
    private static final long STATUS_EXPIRE_TIME_NS = 5000000000L;
    private static final int MAX_STATUS_PLAYER_SAMPLE = 12;
    public static final int START_CHUNK_RADIUS = 11;
    private static final int START_TICKING_CHUNK_COUNT = 441;
    private static final int AUTOSAVE_INTERVAL = 6000;
    private static final int MAX_TICK_LATENCY = 3;
    public static final int ABSOLUTE_MAX_WORLD_SIZE = 29999984;
    public static final LevelSettings DEMO_SETTINGS = new LevelSettings("Demo World", GameType.SURVIVAL, false, Difficulty.NORMAL, false, new GameRules(), WorldDataConfiguration.DEFAULT);
    private static final long DELAYED_TASKS_TICK_EXTENSION = 50L;
    public static final GameProfile ANONYMOUS_PLAYER_PROFILE = new GameProfile(Util.NIL_UUID, "Anonymous Player");
    protected final LevelStorageSource.LevelStorageAccess storageSource;
    protected final PlayerDataStorage playerDataStorage;
    private final List<Runnable> tickables = Lists.newArrayList();
    private MetricsRecorder metricsRecorder = InactiveMetricsRecorder.INSTANCE;
    private ProfilerFiller profiler = this.metricsRecorder.getProfiler();
    private Consumer<ProfileResults> onMetricsRecordingStopped = profileResults -> this.stopRecordingMetrics();
    private Consumer<Path> onMetricsRecordingFinished = path -> {};
    private boolean willStartRecordingMetrics;
    @Nullable
    private TimeProfiler debugCommandProfiler;
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
    private final Map<ResourceKey<Level>, ServerLevel> levels = Maps.newLinkedHashMap();
    private PlayerList playerList;
    private volatile boolean running = true;
    private boolean stopped;
    private int tickCount;
    protected final Proxy proxy;
    private boolean onlineMode;
    private boolean preventProxyConnections;
    private boolean pvp;
    private boolean allowFlight;
    @Nullable
    private String motd;
    private int playerIdleTimeout;
    public final long[] tickTimes = new long[100];
    @Nullable
    private KeyPair keyPair;
    @Nullable
    private GameProfile singleplayerProfile;
    private boolean isDemo;
    private volatile boolean isReady;
    private long lastOverloadWarning;
    protected final Services services;
    private long lastServerStatus;
    private final Thread serverThread;
    private long nextTickTime = Util.getMillis();
    private long delayedTasksMaxNextTickTime;
    private boolean mayHaveDelayedTasks;
    private final PackRepository packRepository;
    private final ServerScoreboard scoreboard = new ServerScoreboard(this);
    @Nullable
    private CommandStorage commandStorage;
    private final CustomBossEvents customBossEvents = new CustomBossEvents();
    private final ServerFunctionManager functionManager;
    private final FrameTimer frameTimer = new FrameTimer();
    private boolean enforceWhitelist;
    private float averageTickTime;
    private final Executor executor;
    @Nullable
    private String serverId;
    private ReloadableResources resources;
    private final StructureTemplateManager structureTemplateManager;
    protected final WorldData worldData;
    private volatile boolean isSaving;

    public static <S extends MinecraftServer> S spin(Function<Thread, S> function) {
        AtomicReference<MinecraftServer> atomicReference = new AtomicReference<MinecraftServer>();
        Thread thread2 = new Thread(() -> ((MinecraftServer)atomicReference.get()).runServer(), "Server thread");
        thread2.setUncaughtExceptionHandler((thread, throwable) -> LOGGER.error("Uncaught exception in server thread", throwable));
        if (Runtime.getRuntime().availableProcessors() > 4) {
            thread2.setPriority(8);
        }
        MinecraftServer minecraftServer = (MinecraftServer)function.apply(thread2);
        atomicReference.set(minecraftServer);
        thread2.start();
        return (S)minecraftServer;
    }

    public MinecraftServer(Thread thread, LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, Proxy proxy, DataFixer dataFixer, Services services, ChunkProgressListenerFactory chunkProgressListenerFactory) {
        super("Server");
        this.registries = worldStem.registries();
        this.worldData = worldStem.worldData();
        if (!this.registries.compositeAccess().registryOrThrow(Registries.LEVEL_STEM).containsKey(LevelStem.OVERWORLD)) {
            throw new IllegalStateException("Missing Overworld dimension data");
        }
        this.proxy = proxy;
        this.packRepository = packRepository;
        this.resources = new ReloadableResources(worldStem.resourceManager(), worldStem.dataPackResources());
        this.services = services;
        if (services.profileCache() != null) {
            services.profileCache().setExecutor(this);
        }
        this.connection = new ServerConnectionListener(this);
        this.progressListenerFactory = chunkProgressListenerFactory;
        this.storageSource = levelStorageAccess;
        this.playerDataStorage = levelStorageAccess.createPlayerStorage();
        this.fixerUpper = dataFixer;
        this.functionManager = new ServerFunctionManager(this, this.resources.managers.getFunctionLibrary());
        HolderLookup<Block> holderGetter = this.registries.compositeAccess().registryOrThrow(Registries.BLOCK).asLookup().filterFeatures(this.worldData.enabledFeatures());
        this.structureTemplateManager = new StructureTemplateManager(worldStem.resourceManager(), levelStorageAccess, dataFixer, holderGetter);
        this.serverThread = thread;
        this.executor = Util.backgroundExecutor();
    }

    private void readScoreboard(DimensionDataStorage dimensionDataStorage) {
        dimensionDataStorage.computeIfAbsent(this.getScoreboard()::createData, this.getScoreboard()::createData, "scoreboard");
    }

    protected abstract boolean initServer() throws IOException;

    protected void loadLevel() {
        if (!JvmProfiler.INSTANCE.isRunning()) {
            // empty if block
        }
        boolean bl = false;
        ProfiledDuration profiledDuration = JvmProfiler.INSTANCE.onWorldLoadedStarted();
        this.worldData.setModdedInfo(this.getServerModName(), this.getModdedStatus().shouldReportAsModified());
        ChunkProgressListener chunkProgressListener = this.progressListenerFactory.create(11);
        this.createLevels(chunkProgressListener);
        this.forceDifficulty();
        this.prepareLevels(chunkProgressListener);
        if (profiledDuration != null) {
            profiledDuration.finish();
        }
        if (bl) {
            try {
                JvmProfiler.INSTANCE.stop();
            } catch (Throwable throwable) {
                LOGGER.warn("Failed to stop JFR profiling", throwable);
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
        ImmutableList<CustomSpawner> list = ImmutableList.of(new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(serverLevelData));
        LevelStem levelStem = registry.get(LevelStem.OVERWORLD);
        ServerLevel serverLevel = new ServerLevel(this, this.executor, this.storageSource, serverLevelData, Level.OVERWORLD, levelStem, chunkProgressListener, bl, m, list, true);
        this.levels.put(Level.OVERWORLD, serverLevel);
        DimensionDataStorage dimensionDataStorage = serverLevel.getDataStorage();
        this.readScoreboard(dimensionDataStorage);
        this.commandStorage = new CommandStorage(dimensionDataStorage);
        WorldBorder worldBorder = serverLevel.getWorldBorder();
        if (!serverLevelData.isInitialized()) {
            try {
                MinecraftServer.setInitialSpawn(serverLevel, serverLevelData, worldOptions.generateBonusChest(), bl);
                serverLevelData.setInitialized(true);
                if (bl) {
                    this.setupDebugLevel(this.worldData);
                }
            } catch (Throwable throwable) {
                CrashReport crashReport = CrashReport.forThrowable(throwable, "Exception initializing level");
                try {
                    serverLevel.fillReportDetails(crashReport);
                } catch (Throwable throwable2) {
                    // empty catch block
                }
                throw new ReportedException(crashReport);
            }
            serverLevelData.setInitialized(true);
        }
        this.getPlayerList().addWorldborderListener(serverLevel);
        if (this.worldData.getCustomBossEvents() != null) {
            this.getCustomBossEvents().load(this.worldData.getCustomBossEvents());
        }
        for (Map.Entry<ResourceKey<LevelStem>, LevelStem> entry : registry.entrySet()) {
            ResourceKey<LevelStem> resourceKey = entry.getKey();
            if (resourceKey == LevelStem.OVERWORLD) continue;
            ResourceKey<Level> resourceKey2 = ResourceKey.create(Registries.DIMENSION, resourceKey.location());
            DerivedLevelData derivedLevelData = new DerivedLevelData(this.worldData, serverLevelData);
            ServerLevel serverLevel2 = new ServerLevel(this, this.executor, this.storageSource, derivedLevelData, resourceKey2, entry.getValue(), chunkProgressListener, bl, m, ImmutableList.of(), false);
            worldBorder.addListener(new BorderChangeListener.DelegateBorderChangeListener(serverLevel2.getWorldBorder()));
            this.levels.put(resourceKey2, serverLevel2);
        }
        worldBorder.applySettings(serverLevelData.getWorldBorder());
    }

    private static void setInitialSpawn(ServerLevel serverLevel, ServerLevelData serverLevelData, boolean bl, boolean bl2) {
        if (bl2) {
            serverLevelData.setSpawn(BlockPos.ZERO.above(80), 0.0f);
            return;
        }
        ServerChunkCache serverChunkCache = serverLevel.getChunkSource();
        ChunkPos chunkPos = new ChunkPos(serverChunkCache.randomState().sampler().findSpawnPosition());
        int i = serverChunkCache.getGenerator().getSpawnHeight(serverLevel);
        if (i < serverLevel.getMinBuildHeight()) {
            BlockPos blockPos = chunkPos.getWorldPosition();
            i = serverLevel.getHeight(Heightmap.Types.WORLD_SURFACE, blockPos.getX() + 8, blockPos.getZ() + 8);
        }
        serverLevelData.setSpawn(chunkPos.getWorldPosition().offset(8, i, 8), 0.0f);
        int j = 0;
        int k = 0;
        int l = 0;
        int m = -1;
        int n = 5;
        for (int o = 0; o < Mth.square(11); ++o) {
            BlockPos blockPos2;
            if (j >= -5 && j <= 5 && k >= -5 && k <= 5 && (blockPos2 = PlayerRespawnLogic.getSpawnPosInChunk(serverLevel, new ChunkPos(chunkPos.x + j, chunkPos.z + k))) != null) {
                serverLevelData.setSpawn(blockPos2, 0.0f);
                break;
            }
            if (j == k || j < 0 && j == -k || j > 0 && j == 1 - k) {
                int p = l;
                l = -m;
                m = p;
            }
            j += l;
            k += m;
        }
        if (bl) {
            serverLevel.registryAccess().registry(Registries.CONFIGURED_FEATURE).flatMap(registry -> registry.getHolder(MiscOverworldFeatures.BONUS_CHEST)).ifPresent(reference -> ((ConfiguredFeature)reference.value()).place(serverLevel, serverChunkCache.getGenerator(), serverLevel.random, new BlockPos(serverLevelData.getXSpawn(), serverLevelData.getYSpawn(), serverLevelData.getZSpawn())));
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
        LOGGER.info("Preparing start region for dimension {}", (Object)serverLevel.dimension().location());
        BlockPos blockPos = serverLevel.getSharedSpawnPos();
        chunkProgressListener.updateSpawnPos(new ChunkPos(blockPos));
        ServerChunkCache serverChunkCache = serverLevel.getChunkSource();
        serverChunkCache.getLightEngine().setTaskPerBatch(500);
        this.nextTickTime = Util.getMillis();
        serverChunkCache.addRegionTicket(TicketType.START, new ChunkPos(blockPos), 11, Unit.INSTANCE);
        while (serverChunkCache.getTickingGenerated() != 441) {
            this.nextTickTime = Util.getMillis() + 10L;
            this.waitUntilNextTick();
        }
        this.nextTickTime = Util.getMillis() + 10L;
        this.waitUntilNextTick();
        for (ServerLevel serverLevel2 : this.levels.values()) {
            ForcedChunksSavedData forcedChunksSavedData = serverLevel2.getDataStorage().get(ForcedChunksSavedData::load, "chunks");
            if (forcedChunksSavedData == null) continue;
            LongIterator longIterator = forcedChunksSavedData.getChunks().iterator();
            while (longIterator.hasNext()) {
                long l = longIterator.nextLong();
                ChunkPos chunkPos = new ChunkPos(l);
                serverLevel2.getChunkSource().updateChunkForced(chunkPos, true);
            }
        }
        this.nextTickTime = Util.getMillis() + 10L;
        this.waitUntilNextTick();
        chunkProgressListener.stop();
        serverChunkCache.getLightEngine().setTaskPerBatch(5);
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
                LOGGER.info("Saving chunks for level '{}'/{}", (Object)serverLevel, (Object)serverLevel.dimension().location());
            }
            serverLevel.save(null, bl2, serverLevel.noSave && !bl3);
            bl4 = true;
        }
        ServerLevel serverLevel2 = this.overworld();
        ServerLevelData serverLevelData = this.worldData.overworldData();
        serverLevelData.setWorldBorder(serverLevel2.getWorldBorder().createSettings());
        this.worldData.setCustomBossEvents(this.getCustomBossEvents().save());
        this.storageSource.saveDataTag(this.registryAccess(), this.worldData, this.getPlayerList().getSingleplayerData());
        if (bl2) {
            for (ServerLevel serverLevel3 : this.getAllLevels()) {
                LOGGER.info("ThreadedAnvilChunkStorage ({}): All chunks are saved", (Object)serverLevel3.getChunkSource().chunkMap.getStorageName());
            }
            LOGGER.info("ThreadedAnvilChunkStorage: All dimensions are saved");
        }
        return bl4;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean saveEverything(boolean bl, boolean bl2, boolean bl3) {
        try {
            this.isSaving = true;
            this.getPlayerList().saveAll();
            boolean bl4 = this.saveAllChunks(bl, bl2, bl3);
            return bl4;
        } finally {
            this.isSaving = false;
        }
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
        if (this.getConnection() != null) {
            this.getConnection().stop();
        }
        this.isSaving = true;
        if (this.playerList != null) {
            LOGGER.info("Saving players");
            this.playerList.saveAll();
            this.playerList.removeAll();
        }
        LOGGER.info("Saving worlds");
        for (ServerLevel serverLevel2 : this.getAllLevels()) {
            if (serverLevel2 == null) continue;
            serverLevel2.noSave = false;
        }
        while (this.levels.values().stream().anyMatch(serverLevel -> serverLevel.getChunkSource().chunkMap.hasWork())) {
            this.nextTickTime = Util.getMillis() + 1L;
            for (ServerLevel serverLevel2 : this.getAllLevels()) {
                serverLevel2.getChunkSource().removeTicketsOnClosing();
                serverLevel2.getChunkSource().tick(() -> true, false);
            }
            this.waitUntilNextTick();
        }
        this.saveAllChunks(false, true, false);
        for (ServerLevel serverLevel2 : this.getAllLevels()) {
            if (serverLevel2 == null) continue;
            try {
                serverLevel2.close();
            } catch (IOException iOException) {
                LOGGER.error("Exception closing the level", iOException);
            }
        }
        this.isSaving = false;
        this.resources.close();
        try {
            this.storageSource.close();
        } catch (IOException iOException2) {
            LOGGER.error("Failed to unlock level {}", (Object)this.storageSource.getLevelId(), (Object)iOException2);
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
            } catch (InterruptedException interruptedException) {
                LOGGER.error("Error while shutting down", interruptedException);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected void runServer() {
        block25: {
            try {
                if (this.initServer()) {
                    this.nextTickTime = Util.getMillis();
                    this.statusIcon = this.loadStatusIcon().orElse(null);
                    this.status = this.buildServerStatus();
                    while (this.running) {
                        long l = Util.getMillis() - this.nextTickTime;
                        if (l > 2000L && this.nextTickTime - this.lastOverloadWarning >= 15000L) {
                            long m = l / 50L;
                            LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", (Object)l, (Object)m);
                            this.nextTickTime += m * 50L;
                            this.lastOverloadWarning = this.nextTickTime;
                        }
                        if (this.debugCommandProfilerDelayStart) {
                            this.debugCommandProfilerDelayStart = false;
                            this.debugCommandProfiler = new TimeProfiler(Util.getNanos(), this.tickCount);
                        }
                        this.nextTickTime += 50L;
                        this.startMetricsRecordingTick();
                        this.profiler.push("tick");
                        this.tickServer(this::haveTime);
                        this.profiler.popPush("nextTickWait");
                        this.mayHaveDelayedTasks = true;
                        this.delayedTasksMaxNextTickTime = Math.max(Util.getMillis() + 50L, this.nextTickTime);
                        this.waitUntilNextTick();
                        this.profiler.pop();
                        this.endMetricsRecordingTick();
                        this.isReady = true;
                        JvmProfiler.INSTANCE.onServerTick(this.averageTickTime);
                    }
                    break block25;
                }
                throw new IllegalStateException("Failed to initialize server");
            } catch (Throwable throwable) {
                LOGGER.error("Encountered an unexpected exception", throwable);
                CrashReport crashReport = MinecraftServer.constructOrExtractCrashReport(throwable);
                this.fillSystemReport(crashReport.getSystemReport());
                File file = new File(new File(this.getServerDirectory(), "crash-reports"), "crash-" + Util.getFilenameFormattedDateTime() + "-server.txt");
                if (crashReport.saveToFile(file)) {
                    LOGGER.error("This crash report has been saved to: {}", (Object)file.getAbsolutePath());
                } else {
                    LOGGER.error("We were unable to save this crash report to disk.");
                }
                this.onServerCrash(crashReport);
            } finally {
                try {
                    this.stopped = true;
                    this.stopServer();
                } catch (Throwable throwable) {
                    LOGGER.error("Exception stopping the server", throwable);
                } finally {
                    if (this.services.profileCache() != null) {
                        this.services.profileCache().clearExecutor();
                    }
                    this.onServerExit();
                }
            }
        }
    }

    private static CrashReport constructOrExtractCrashReport(Throwable throwable) {
        CrashReport crashReport;
        ReportedException reportedException = null;
        for (Throwable throwable2 = throwable; throwable2 != null; throwable2 = throwable2.getCause()) {
            ReportedException reportedException2;
            if (!(throwable2 instanceof ReportedException)) continue;
            reportedException = reportedException2 = (ReportedException)throwable2;
        }
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
        return this.runningTask() || Util.getMillis() < (this.mayHaveDelayedTasks ? this.delayedTasksMaxNextTickTime : this.nextTickTime);
    }

    protected void waitUntilNextTick() {
        this.runAllTasks();
        this.managedBlock(() -> !this.haveTime());
    }

    @Override
    protected TickTask wrapRunnable(Runnable runnable) {
        return new TickTask(this.tickCount, runnable);
    }

    @Override
    protected boolean shouldRun(TickTask tickTask) {
        return tickTask.getTick() + 3 < this.tickCount || this.haveTime();
    }

    @Override
    public boolean pollTask() {
        boolean bl;
        this.mayHaveDelayedTasks = bl = this.pollTaskInternal();
        return bl;
    }

    private boolean pollTaskInternal() {
        if (super.pollTask()) {
            return true;
        }
        if (this.haveTime()) {
            for (ServerLevel serverLevel : this.getAllLevels()) {
                if (!serverLevel.getChunkSource().pollTask()) continue;
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doRunTask(TickTask tickTask) {
        this.getProfiler().incrementCounter("runTask");
        super.doRunTask(tickTask);
    }

    private Optional<ServerStatus.Favicon> loadStatusIcon() {
        Optional<Path> optional = Optional.of(this.getFile("server-icon.png").toPath()).filter(path -> Files.isRegularFile(path, new LinkOption[0])).or(() -> this.storageSource.getIconFile().filter(path -> Files.isRegularFile(path, new LinkOption[0])));
        return optional.flatMap(path -> {
            try {
                BufferedImage bufferedImage = ImageIO.read(path.toFile());
                Preconditions.checkState(bufferedImage.getWidth() == 64, "Must be 64 pixels wide");
                Preconditions.checkState(bufferedImage.getHeight() == 64, "Must be 64 pixels high");
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ImageIO.write((RenderedImage)bufferedImage, "PNG", byteArrayOutputStream);
                return Optional.of(new ServerStatus.Favicon(byteArrayOutputStream.toByteArray()));
            } catch (Exception exception) {
                LOGGER.error("Couldn't load server icon", exception);
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

    public void tickServer(BooleanSupplier booleanSupplier) {
        long l = Util.getNanos();
        ++this.tickCount;
        this.tickChildren(booleanSupplier);
        if (l - this.lastServerStatus >= 5000000000L) {
            this.lastServerStatus = l;
            this.status = this.buildServerStatus();
        }
        if (this.tickCount % 6000 == 0) {
            LOGGER.debug("Autosave started");
            this.profiler.push("save");
            this.saveEverything(true, false, false);
            this.profiler.pop();
            LOGGER.debug("Autosave finished");
        }
        this.profiler.push("tallying");
        long l2 = Util.getNanos() - l;
        this.tickTimes[this.tickCount % 100] = l2;
        long m = l2;
        this.averageTickTime = this.averageTickTime * 0.8f + (float)m / 1000000.0f * 0.19999999f;
        long n = Util.getNanos();
        this.frameTimer.logFrameDuration(n - l);
        this.profiler.pop();
    }

    private ServerStatus buildServerStatus() {
        ServerStatus.Players players = this.buildPlayerStatus();
        return new ServerStatus(Component.nullToEmpty(this.motd), Optional.of(players), Optional.of(ServerStatus.Version.current()), Optional.ofNullable(this.statusIcon), this.enforceSecureProfile());
    }

    private ServerStatus.Players buildPlayerStatus() {
        List<ServerPlayer> list = this.playerList.getPlayers();
        int i = this.getMaxPlayers();
        if (this.hidesOnlinePlayers()) {
            return new ServerStatus.Players(i, list.size(), List.of());
        }
        int j = Math.min(list.size(), 12);
        ObjectArrayList<GameProfile> objectArrayList = new ObjectArrayList<GameProfile>(j);
        int k = Mth.nextInt(this.random, 0, list.size() - j);
        for (int l = 0; l < j; ++l) {
            ServerPlayer serverPlayer = list.get(k + l);
            objectArrayList.add(serverPlayer.allowsListing() ? serverPlayer.getGameProfile() : ANONYMOUS_PLAYER_PROFILE);
        }
        Util.shuffle(objectArrayList, this.random);
        return new ServerStatus.Players(i, list.size(), objectArrayList);
    }

    public void tickChildren(BooleanSupplier booleanSupplier) {
        this.profiler.push("commandFunctions");
        this.getFunctions().tick();
        this.profiler.popPush("levels");
        for (ServerLevel serverLevel : this.getAllLevels()) {
            this.profiler.push(() -> serverLevel + " " + serverLevel.dimension().location());
            if (this.tickCount % 20 == 0) {
                this.profiler.push("timeSync");
                this.synchronizeTime(serverLevel);
                this.profiler.pop();
            }
            this.profiler.push("tick");
            try {
                serverLevel.tick(booleanSupplier);
            } catch (Throwable throwable) {
                CrashReport crashReport = CrashReport.forThrowable(throwable, "Exception ticking world");
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
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            GameTestTicker.SINGLETON.tick();
        }
        this.profiler.popPush("server gui refresh");
        for (int i = 0; i < this.tickables.size(); ++i) {
            this.tickables.get(i).run();
        }
        this.profiler.pop();
    }

    private void synchronizeTime(ServerLevel serverLevel) {
        this.playerList.broadcastAll(new ClientboundSetTimePacket(serverLevel.getGameTime(), serverLevel.getDayTime(), serverLevel.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)), serverLevel.dimension());
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
        return this.levels.get(Level.OVERWORLD);
    }

    @Nullable
    public ServerLevel getLevel(ResourceKey<Level> resourceKey) {
        return this.levels.get(resourceKey);
    }

    public Set<ResourceKey<Level>> levelKeys() {
        return this.levels.keySet();
    }

    public Iterable<ServerLevel> getAllLevels() {
        return this.levels.values();
    }

    public String getServerVersion() {
        return SharedConstants.getCurrentVersion().getName();
    }

    public int getPlayerCount() {
        return this.playerList.getPlayerCount();
    }

    public int getMaxPlayers() {
        return this.playerList.getMaxPlayers();
    }

    public String[] getPlayerNames() {
        return this.playerList.getPlayerNamesArray();
    }

    @DontObfuscate
    public String getServerModName() {
        return VANILLA_BRAND;
    }

    public SystemReport fillSystemReport(SystemReport systemReport) {
        systemReport.setDetail("Server Running", () -> Boolean.toString(this.running));
        if (this.playerList != null) {
            systemReport.setDetail("Player Count", () -> this.playerList.getPlayerCount() + " / " + this.playerList.getMaxPlayers() + "; " + this.playerList.getPlayers());
        }
        systemReport.setDetail("Data Packs", () -> this.packRepository.getSelectedPacks().stream().map(pack -> pack.getId() + (pack.getCompatibility().isCompatible() ? "" : " (incompatible)")).collect(Collectors.joining(", ")));
        systemReport.setDetail("Enabled Feature Flags", () -> FeatureFlags.REGISTRY.toNames(this.worldData.enabledFeatures()).stream().map(ResourceLocation::toString).collect(Collectors.joining(", ")));
        systemReport.setDetail("World Generation", () -> this.worldData.worldGenSettingsLifecycle().toString());
        if (this.serverId != null) {
            systemReport.setDetail("Server Id", () -> this.serverId);
        }
        return this.fillServerSystemReport(systemReport);
    }

    public abstract SystemReport fillServerSystemReport(SystemReport var1);

    public ModCheck getModdedStatus() {
        return ModCheck.identify(VANILLA_BRAND, this::getServerModName, "Server", MinecraftServer.class);
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
        } catch (CryptException cryptException) {
            throw new IllegalStateException("Failed to generate key pair", cryptException);
        }
    }

    public void setDifficulty(Difficulty difficulty, boolean bl) {
        if (!bl && this.worldData.isDifficultyLocked()) {
            return;
        }
        this.worldData.setDifficulty(this.worldData.isHardcore() ? Difficulty.HARD : difficulty);
        this.updateMobSpawningFlags();
        this.getPlayerList().getPlayers().forEach(this::sendDifficultyUpdate);
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
        LevelData levelData = serverPlayer.getLevel().getLevelData();
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

    public Optional<ServerResourcePackInfo> getServerResourcePack() {
        return Optional.empty();
    }

    public boolean isResourcePackRequired() {
        return this.getServerResourcePack().filter(ServerResourcePackInfo::isRequired).isPresent();
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

    @Nullable
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

    public SignatureValidator getServiceSignatureValidator() {
        return this.services.serviceSignatureValidator();
    }

    public GameProfileRepository getProfileRepository() {
        return this.services.profileRepository();
    }

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
        }
        super.executeIfPossible(runnable);
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
        return this.nextTickTime;
    }

    public DataFixer getFixerUpper() {
        return this.fixerUpper;
    }

    public int getSpawnRadius(@Nullable ServerLevel serverLevel) {
        if (serverLevel != null) {
            return serverLevel.getGameRules().getInt(GameRules.RULE_SPAWN_RADIUS);
        }
        return 10;
    }

    public ServerAdvancementManager getAdvancements() {
        return this.resources.managers.getAdvancements();
    }

    public ServerFunctionManager getFunctions() {
        return this.functionManager;
    }

    public CompletableFuture<Void> reloadResources(Collection<String> collection) {
        RegistryAccess.Frozen frozen = this.registries.getAccessForLoading(RegistryLayer.RELOADABLE);
        CompletionStage completableFuture = ((CompletableFuture)CompletableFuture.supplyAsync(() -> collection.stream().map(this.packRepository::getPack).filter(Objects::nonNull).map(Pack::open).collect(ImmutableList.toImmutableList()), this).thenCompose(immutableList -> {
            MultiPackResourceManager closeableResourceManager = new MultiPackResourceManager(PackType.SERVER_DATA, (List<PackResources>)immutableList);
            return ((CompletableFuture)ReloadableServerResources.loadResources(closeableResourceManager, frozen, this.worldData.enabledFeatures(), this.isDedicatedServer() ? Commands.CommandSelection.DEDICATED : Commands.CommandSelection.INTEGRATED, this.getFunctionCompilationLevel(), this.executor, this).whenComplete((reloadableServerResources, throwable) -> {
                if (throwable != null) {
                    closeableResourceManager.close();
                }
            })).thenApply(reloadableServerResources -> new ReloadableResources(closeableResourceManager, (ReloadableServerResources)reloadableServerResources));
        })).thenAcceptAsync(reloadableResources -> {
            this.resources.close();
            this.resources = reloadableResources;
            this.packRepository.setSelected(collection);
            WorldDataConfiguration worldDataConfiguration = new WorldDataConfiguration(MinecraftServer.getSelectedPacks(this.packRepository), this.worldData.enabledFeatures());
            this.worldData.setDataConfiguration(worldDataConfiguration);
            this.resources.managers.updateRegistryTags(this.registryAccess());
            this.getPlayerList().saveAll();
            this.getPlayerList().reloadResources();
            this.functionManager.replaceLibrary(this.resources.managers.getFunctionLibrary());
            this.structureTemplateManager.onResourceManagerReload(this.resources.resourceManager);
        }, (Executor)this);
        if (this.isSameThread()) {
            this.managedBlock(((CompletableFuture)completableFuture)::isDone);
        }
        return completableFuture;
    }

    public static WorldDataConfiguration configurePackRepository(PackRepository packRepository, DataPackConfig dataPackConfig, boolean bl, FeatureFlagSet featureFlagSet) {
        packRepository.reload();
        if (bl) {
            packRepository.setSelected(Collections.singleton(VANILLA_BRAND));
            return WorldDataConfiguration.DEFAULT;
        }
        LinkedHashSet<String> set = Sets.newLinkedHashSet();
        for (String string : dataPackConfig.getEnabled()) {
            if (packRepository.isAvailable(string)) {
                set.add(string);
                continue;
            }
            LOGGER.warn("Missing data pack {}", (Object)string);
        }
        for (Pack pack : packRepository.getAvailablePacks()) {
            String string2 = pack.getId();
            if (dataPackConfig.getDisabled().contains(string2)) continue;
            FeatureFlagSet featureFlagSet2 = pack.getRequestedFeatures();
            boolean bl2 = set.contains(string2);
            if (!bl2 && pack.getPackSource().shouldAddAutomatically()) {
                if (featureFlagSet2.isSubsetOf(featureFlagSet)) {
                    LOGGER.info("Found new data pack {}, loading it automatically", (Object)string2);
                    set.add(string2);
                } else {
                    LOGGER.info("Found new data pack {}, but can't load it due to missing features {}", (Object)string2, (Object)FeatureFlags.printMissingFlags(featureFlagSet, featureFlagSet2));
                }
            }
            if (!bl2 || featureFlagSet2.isSubsetOf(featureFlagSet)) continue;
            LOGGER.warn("Pack {} requires features {} that are not enabled for this world, disabling pack.", (Object)string2, (Object)FeatureFlags.printMissingFlags(featureFlagSet, featureFlagSet2));
            set.remove(string2);
        }
        if (set.isEmpty()) {
            LOGGER.info("No datapacks selected, forcing vanilla");
            set.add(VANILLA_BRAND);
        }
        packRepository.setSelected(set);
        DataPackConfig dataPackConfig2 = MinecraftServer.getSelectedPacks(packRepository);
        FeatureFlagSet featureFlagSet3 = packRepository.getRequestedFeatureFlags();
        return new WorldDataConfiguration(dataPackConfig2, featureFlagSet3);
    }

    private static DataPackConfig getSelectedPacks(PackRepository packRepository) {
        Collection<String> collection = packRepository.getSelectedIds();
        ImmutableList<String> list = ImmutableList.copyOf(collection);
        List list2 = packRepository.getAvailableIds().stream().filter(string -> !collection.contains(string)).collect(ImmutableList.toImmutableList());
        return new DataPackConfig(list, list2);
    }

    public void kickUnlistedPlayers(CommandSourceStack commandSourceStack) {
        if (!this.isEnforceWhitelist()) {
            return;
        }
        PlayerList playerList = commandSourceStack.getServer().getPlayerList();
        UserWhiteList userWhiteList = playerList.getWhiteList();
        ArrayList<ServerPlayer> list = Lists.newArrayList(playerList.getPlayers());
        for (ServerPlayer serverPlayer : list) {
            if (userWhiteList.isWhiteListed(serverPlayer.getGameProfile())) continue;
            serverPlayer.connection.disconnect(Component.translatable("multiplayer.disconnect.not_whitelisted"));
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
        return new CommandSourceStack(this, serverLevel == null ? Vec3.ZERO : Vec3.atLowerCornerOf(serverLevel.getSharedSpawnPos()), Vec2.ZERO, serverLevel, 4, "Server", Component.literal("Server"), this, null);
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
        }
        return this.commandStorage;
    }

    public LootTables getLootTables() {
        return this.resources.managers.getLootTables();
    }

    public PredicateManager getPredicateManager() {
        return this.resources.managers.getPredicateManager();
    }

    public ItemModifierManager getItemModifierManager() {
        return this.resources.managers.getItemModifierManager();
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

    public float getAverageTickTime() {
        return this.averageTickTime;
    }

    public int getProfilePermissions(GameProfile gameProfile) {
        if (this.getPlayerList().isOp(gameProfile)) {
            ServerOpListEntry serverOpListEntry = (ServerOpListEntry)this.getPlayerList().getOps().get(gameProfile);
            if (serverOpListEntry != null) {
                return serverOpListEntry.getLevel();
            }
            if (this.isSingleplayerOwner(gameProfile)) {
                return 4;
            }
            if (this.isSingleplayer()) {
                return this.getPlayerList().isAllowCheatsForAllPlayers() ? 4 : 0;
            }
            return this.getOperatorUserPermissionLevel();
        }
        return 0;
    }

    public FrameTimer getFrameTimer() {
        return this.frameTimer;
    }

    public ProfilerFiller getProfiler() {
        return this.profiler;
    }

    public abstract boolean isSingleplayerOwner(GameProfile var1);

    public void dumpServerProperties(Path path) throws IOException {
    }

    private void saveDebugReport(Path path) {
        Path path2 = path.resolve("levels");
        try {
            for (Map.Entry<ResourceKey<Level>, ServerLevel> entry : this.levels.entrySet()) {
                ResourceLocation resourceLocation = entry.getKey().location();
                Path path3 = path2.resolve(resourceLocation.getNamespace()).resolve(resourceLocation.getPath());
                Files.createDirectories(path3, new FileAttribute[0]);
                entry.getValue().saveDebugReport(path3);
            }
            this.dumpGameRules(path.resolve("gamerules.txt"));
            this.dumpClasspath(path.resolve("classpath.txt"));
            this.dumpMiscStats(path.resolve("stats.txt"));
            this.dumpThreads(path.resolve("threads.txt"));
            this.dumpServerProperties(path.resolve("server.properties.txt"));
            this.dumpNativeModules(path.resolve("modules.txt"));
        } catch (IOException iOException) {
            LOGGER.warn("Failed to save debug report", iOException);
        }
    }

    private void dumpMiscStats(Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, new OpenOption[0]);){
            writer.write(String.format(Locale.ROOT, "pending_tasks: %d\n", this.getPendingTasksCount()));
            writer.write(String.format(Locale.ROOT, "average_tick_time: %f\n", Float.valueOf(this.getAverageTickTime())));
            writer.write(String.format(Locale.ROOT, "tick_times: %s\n", Arrays.toString(this.tickTimes)));
            writer.write(String.format(Locale.ROOT, "queue: %s\n", Util.backgroundExecutor()));
        }
    }

    private void dumpGameRules(Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, new OpenOption[0]);){
            final ArrayList<String> list = Lists.newArrayList();
            final GameRules gameRules = this.getGameRules();
            GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor(){

                @Override
                public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                    list.add(String.format(Locale.ROOT, "%s=%s\n", key.getId(), gameRules.getRule(key)));
                }
            });
            for (String string : list) {
                writer.write(string);
            }
        }
    }

    private void dumpClasspath(Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, new OpenOption[0]);){
            String string = System.getProperty("java.class.path");
            String string2 = System.getProperty("path.separator");
            for (String string3 : Splitter.on(string2).split(string)) {
                writer.write(string3);
                writer.write("\n");
            }
        }
    }

    private void dumpThreads(Path path) throws IOException {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(true, true);
        Arrays.sort(threadInfos, Comparator.comparing(ThreadInfo::getThreadName));
        try (BufferedWriter writer = Files.newBufferedWriter(path, new OpenOption[0]);){
            for (ThreadInfo threadInfo : threadInfos) {
                writer.write(threadInfo.toString());
                ((Writer)writer).write(10);
            }
        }
    }

    private void dumpNativeModules(Path path) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(path, new OpenOption[0]);
        try {
            ArrayList<NativeModuleLister.NativeModuleInfo> list;
            try {
                list = Lists.newArrayList(NativeModuleLister.listModules());
            } catch (Throwable throwable) {
                LOGGER.warn("Failed to list native modules", throwable);
                if (writer != null) {
                    ((Writer)writer).close();
                }
                return;
            }
            list.sort(Comparator.comparing(nativeModuleInfo -> nativeModuleInfo.name));
            for (NativeModuleLister.NativeModuleInfo nativeModuleInfo2 : list) {
                writer.write(nativeModuleInfo2.toString());
                ((Writer)writer).write(10);
            }
        } finally {
            if (writer != null) {
                try {
                    ((Writer)writer).close();
                } catch (Throwable throwable) {
                    Throwable throwable2;
                    throwable2.addSuppressed(throwable);
                }
            }
        }
    }

    private void startMetricsRecordingTick() {
        if (this.willStartRecordingMetrics) {
            this.metricsRecorder = ActiveMetricsRecorder.createStarted(new ServerMetricsSamplersProvider(Util.timeSource, this.isDedicatedServer()), Util.timeSource, Util.ioPool(), new MetricsPersister("server"), this.onMetricsRecordingStopped, path -> {
                this.executeBlocking(() -> this.saveDebugReport(path.resolve("server")));
                this.onMetricsRecordingFinished.accept((Path)path);
            });
            this.willStartRecordingMetrics = false;
        }
        this.profiler = SingleTickProfiler.decorateFiller(this.metricsRecorder.getProfiler(), SingleTickProfiler.createTickProfiler("Server"));
        this.metricsRecorder.startTick();
        this.profiler.startTick();
    }

    private void endMetricsRecordingTick() {
        this.profiler.endTick();
        this.metricsRecorder.endTick();
    }

    public boolean isRecordingMetrics() {
        return this.metricsRecorder.isRecording();
    }

    public void startRecordingMetrics(Consumer<ProfileResults> consumer, Consumer<Path> consumer2) {
        this.onMetricsRecordingStopped = profileResults -> {
            this.stopRecordingMetrics();
            consumer.accept((ProfileResults)profileResults);
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

    public TextFilter createTextFilterForPlayer(ServerPlayer serverPlayer) {
        return TextFilter.DUMMY;
    }

    public ServerPlayerGameMode createGameModeForPlayer(ServerPlayer serverPlayer) {
        return this.isDemo() ? new DemoMode(serverPlayer) : new ServerPlayerGameMode(serverPlayer);
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
        }
        ProfileResults profileResults = this.debugCommandProfiler.stop(Util.getNanos(), this.tickCount);
        this.debugCommandProfiler = null;
        return profileResults;
    }

    public int getMaxChainedNeighborUpdates() {
        return 1000000;
    }

    public void logChatMessage(Component component, ChatType.Bound bound, @Nullable String string) {
        String string2 = bound.decorate(component).getString();
        if (string != null) {
            LOGGER.info("[{}] {}", (Object)string, (Object)string2);
        } else {
            LOGGER.info("{}", (Object)string2);
        }
    }

    public ChatDecorator getChatDecorator() {
        return ChatDecorator.PLAIN;
    }

    @Override
    public /* synthetic */ void doRunTask(Runnable runnable) {
        this.doRunTask((TickTask)runnable);
    }

    @Override
    public /* synthetic */ boolean shouldRun(Runnable runnable) {
        return this.shouldRun((TickTask)runnable);
    }

    @Override
    public /* synthetic */ Runnable wrapRunnable(Runnable runnable) {
        return this.wrapRunnable(runnable);
    }

    record ReloadableResources(CloseableResourceManager resourceManager, ReloadableServerResources managers) implements AutoCloseable
    {
        @Override
        public void close() {
            this.resourceManager.close();
        }
    }

    static class TimeProfiler {
        final long startNanos;
        final int startTick;

        TimeProfiler(long l, int i) {
            this.startNanos = l;
            this.startTick = i;
        }

        ProfileResults stop(final long l, final int i) {
            return new ProfileResults(){

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
                    return startNanos;
                }

                @Override
                public int getStartTimeTicks() {
                    return startTick;
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

    public record ServerResourcePackInfo(String url, String hash, boolean isRequired, @Nullable Component prompt) {
        @Nullable
        public Component prompt() {
            return this.prompt;
        }
    }
}

