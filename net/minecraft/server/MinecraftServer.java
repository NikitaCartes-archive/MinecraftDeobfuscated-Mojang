/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.longs.LongIterator;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.Proxy;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import javax.imageio.ImageIO;
import joptsimple.AbstractOptionSpec;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.Eula;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.TickTask;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.DedicatedServerSettings;
import net.minecraft.server.level.DerivedServerLevel;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.packs.Pack;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.repository.UnopenedPack;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.server.players.UserWhiteList;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagManager;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.Mth;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.Unit;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.profiling.ContinuousProfiler;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.SingleTickProfiler;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.util.worldupdate.WorldUpgrader;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Snooper;
import net.minecraft.world.SnooperPopulator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.saveddata.SaveDataDirtyRunnable;
import net.minecraft.world.level.storage.CommandStorage;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.PredicateManager;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.ScoreboardSaveData;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public abstract class MinecraftServer
extends ReentrantBlockableEventLoop<TickTask>
implements SnooperPopulator,
CommandSource,
AutoCloseable,
Runnable {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final File USERID_CACHE_FILE = new File("usercache.json");
    private static final CompletableFuture<Unit> DATA_RELOAD_INITIAL_TASK = CompletableFuture.completedFuture(Unit.INSTANCE);
    public static final LevelSettings DEMO_SETTINGS = new LevelSettings("Demo World", GameType.SURVIVAL, false, Difficulty.NORMAL, false, new GameRules(), WorldGenSettings.DEMO_SETTINGS);
    protected final LevelStorageSource.LevelStorageAccess storageSource;
    protected final PlayerDataStorage playerDataStorage;
    private final Snooper snooper = new Snooper("server", this, Util.getMillis());
    private final List<Runnable> tickables = Lists.newArrayList();
    private ContinuousProfiler continousProfiler = new ContinuousProfiler(Util.timeSource, this::getTickCount);
    private ProfilerFiller profiler = InactiveProfiler.INSTANCE;
    private final ServerConnectionListener connection;
    protected final ChunkProgressListenerFactory progressListenerFactory;
    private final ServerStatus status = new ServerStatus();
    private final Random random = new Random();
    private final DataFixer fixerUpper;
    private String localIp;
    private int port = -1;
    private final Map<DimensionType, ServerLevel> levels = Maps.newIdentityHashMap();
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
    private int maxBuildHeight;
    private int playerIdleTimeout;
    public final long[] tickTimes = new long[100];
    @Nullable
    private KeyPair keyPair;
    @Nullable
    private String singleplayerName;
    private boolean isDemo;
    private String resourcePack = "";
    private String resourcePackHash = "";
    private volatile boolean isReady;
    private long lastOverloadWarning;
    private boolean delayProfilerStart;
    private boolean forceGameType;
    private final MinecraftSessionService sessionService;
    private final GameProfileRepository profileRepository;
    private final GameProfileCache profileCache;
    private long lastServerStatus;
    protected final Thread serverThread = Util.make(new Thread((Runnable)this, "Server thread"), thread2 -> thread2.setUncaughtExceptionHandler((thread, throwable) -> LOGGER.error(throwable)));
    private long nextTickTime = Util.getMillis();
    private long delayedTasksMaxNextTickTime;
    private boolean mayHaveDelayedTasks;
    @Environment(value=EnvType.CLIENT)
    private boolean hasWorldScreenshot;
    private final ReloadableResourceManager resources = new SimpleReloadableResourceManager(PackType.SERVER_DATA, this.serverThread);
    private final PackRepository<UnopenedPack> packRepository = new PackRepository<UnopenedPack>(UnopenedPack::new);
    @Nullable
    private FolderRepositorySource folderPackSource;
    private final Commands commands;
    private final RecipeManager recipes = new RecipeManager();
    private final TagManager tags = new TagManager();
    private final ServerScoreboard scoreboard = new ServerScoreboard(this);
    @Nullable
    private CommandStorage commandStorage;
    private final CustomBossEvents customBossEvents = new CustomBossEvents(this);
    private final PredicateManager predicateManager = new PredicateManager();
    private final LootTables lootTables = new LootTables(this.predicateManager);
    private final ServerAdvancementManager advancements = new ServerAdvancementManager(this.predicateManager);
    private final ServerFunctionManager functions = new ServerFunctionManager(this);
    private final FrameTimer frameTimer = new FrameTimer();
    private boolean enforceWhitelist;
    private float averageTickTime;
    private final Executor executor;
    @Nullable
    private String serverId;
    private final StructureManager structureManager;
    protected final WorldData worldData;

    public MinecraftServer(LevelStorageSource.LevelStorageAccess levelStorageAccess, WorldData worldData, Proxy proxy, DataFixer dataFixer, Commands commands, MinecraftSessionService minecraftSessionService, GameProfileRepository gameProfileRepository, GameProfileCache gameProfileCache, ChunkProgressListenerFactory chunkProgressListenerFactory) {
        super("Server");
        this.worldData = worldData;
        this.proxy = proxy;
        this.commands = commands;
        this.sessionService = minecraftSessionService;
        this.profileRepository = gameProfileRepository;
        this.profileCache = gameProfileCache;
        this.connection = new ServerConnectionListener(this);
        this.progressListenerFactory = chunkProgressListenerFactory;
        this.storageSource = levelStorageAccess;
        this.playerDataStorage = levelStorageAccess.createPlayerStorage();
        this.fixerUpper = dataFixer;
        this.resources.registerReloadListener(this.tags);
        this.resources.registerReloadListener(this.predicateManager);
        this.resources.registerReloadListener(this.recipes);
        this.resources.registerReloadListener(this.lootTables);
        this.resources.registerReloadListener(this.functions);
        this.resources.registerReloadListener(this.advancements);
        this.executor = Util.backgroundExecutor();
        this.structureManager = new StructureManager(this, levelStorageAccess, dataFixer);
    }

    private void readScoreboard(DimensionDataStorage dimensionDataStorage) {
        ScoreboardSaveData scoreboardSaveData = dimensionDataStorage.computeIfAbsent(ScoreboardSaveData::new, "scoreboard");
        scoreboardSaveData.setScoreboard(this.getScoreboard());
        this.getScoreboard().addDirtyListener(new SaveDataDirtyRunnable(scoreboardSaveData));
    }

    protected abstract boolean initServer() throws IOException;

    public static void ensureLevelConversion(LevelStorageSource.LevelStorageAccess levelStorageAccess, DataFixer dataFixer, boolean bl, boolean bl2, BooleanSupplier booleanSupplier) {
        if (levelStorageAccess.requiresConversion()) {
            LOGGER.info("Converting map!");
            levelStorageAccess.convertLevel(new ProgressListener(){
                private long timeStamp = Util.getMillis();

                @Override
                public void progressStartNoAbort(Component component) {
                }

                @Override
                @Environment(value=EnvType.CLIENT)
                public void progressStart(Component component) {
                }

                @Override
                public void progressStagePercentage(int i) {
                    if (Util.getMillis() - this.timeStamp >= 1000L) {
                        this.timeStamp = Util.getMillis();
                        LOGGER.info("Converting... {}%", (Object)i);
                    }
                }

                @Override
                @Environment(value=EnvType.CLIENT)
                public void stop() {
                }

                @Override
                public void progressStage(Component component) {
                }
            });
        }
        if (bl) {
            LOGGER.info("Forcing world upgrade!");
            WorldData worldData = levelStorageAccess.getDataTag();
            if (worldData != null) {
                WorldUpgrader worldUpgrader = new WorldUpgrader(levelStorageAccess, dataFixer, worldData, bl2);
                Component component = null;
                while (!worldUpgrader.isFinished()) {
                    int i;
                    Component component2 = worldUpgrader.getStatus();
                    if (component != component2) {
                        component = component2;
                        LOGGER.info(worldUpgrader.getStatus().getString());
                    }
                    if ((i = worldUpgrader.getTotalChunks()) > 0) {
                        int j = worldUpgrader.getConverted() + worldUpgrader.getSkipped();
                        LOGGER.info("{}% completed ({} / {} chunks)...", (Object)Mth.floor((float)j / (float)i * 100.0f), (Object)j, (Object)i);
                    }
                    if (!booleanSupplier.getAsBoolean()) {
                        worldUpgrader.cancel();
                        continue;
                    }
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException interruptedException) {}
                }
            }
        }
    }

    protected void loadLevel() {
        this.detectBundledResources();
        this.worldData.setModdedInfo(this.getServerModName(), this.getModdedStatus().isPresent());
        this.loadDataPacks();
        ChunkProgressListener chunkProgressListener = this.progressListenerFactory.create(11);
        this.createLevels(chunkProgressListener);
        this.forceDifficulty();
        this.prepareLevels(chunkProgressListener);
    }

    protected void forceDifficulty() {
    }

    protected void createLevels(ChunkProgressListener chunkProgressListener) {
        ServerLevelData serverLevelData = this.worldData.overworldData();
        WorldGenSettings worldGenSettings = this.worldData.worldGenSettings();
        boolean bl = worldGenSettings.isDebug();
        long l = worldGenSettings.seed();
        long m = BiomeManager.obfuscateSeed(l);
        ServerLevel serverLevel = new ServerLevel(this, this.executor, this.storageSource, serverLevelData, DimensionType.OVERWORLD, chunkProgressListener, worldGenSettings.overworld(), bl, m);
        this.levels.put(DimensionType.OVERWORLD, serverLevel);
        DimensionDataStorage dimensionDataStorage = serverLevel.getDataStorage();
        this.readScoreboard(dimensionDataStorage);
        this.commandStorage = new CommandStorage(dimensionDataStorage);
        serverLevel.getWorldBorder().applySettings(serverLevelData.getWorldBorder());
        ServerLevel serverLevel2 = this.getLevel(DimensionType.OVERWORLD);
        if (!serverLevelData.isInitialized()) {
            try {
                MinecraftServer.setInitialSpawn(serverLevel2, serverLevel2.getDimension(), serverLevelData, worldGenSettings.generateBonusChest(), bl);
                serverLevelData.setInitialized(true);
                if (bl) {
                    this.setupDebugLevel(this.worldData);
                }
            } catch (Throwable throwable) {
                CrashReport crashReport = CrashReport.forThrowable(throwable, "Exception initializing level");
                try {
                    serverLevel2.fillReportDetails(crashReport);
                } catch (Throwable throwable2) {
                    // empty catch block
                }
                throw new ReportedException(crashReport);
            }
            serverLevelData.setInitialized(true);
        }
        this.getPlayerList().setLevel(serverLevel2);
        if (this.worldData.getCustomBossEvents() != null) {
            this.getCustomBossEvents().load(this.worldData.getCustomBossEvents());
        }
        for (Map.Entry<DimensionType, ChunkGenerator> entry : worldGenSettings.generators().entrySet()) {
            DimensionType dimensionType = entry.getKey();
            if (dimensionType == DimensionType.OVERWORLD) continue;
            this.levels.put(dimensionType, new DerivedServerLevel(serverLevel2, this.worldData.overworldData(), this, this.executor, this.storageSource, dimensionType, chunkProgressListener, entry.getValue(), bl, m));
        }
    }

    private static void setInitialSpawn(ServerLevel serverLevel, Dimension dimension, ServerLevelData serverLevelData, boolean bl, boolean bl2) {
        ChunkPos chunkPos;
        ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
        if (!dimension.mayRespawn()) {
            serverLevelData.setSpawn(BlockPos.ZERO.above(chunkGenerator.getSpawnHeight()));
            return;
        }
        if (bl2) {
            serverLevelData.setSpawn(BlockPos.ZERO.above());
            return;
        }
        BiomeSource biomeSource = chunkGenerator.getBiomeSource();
        List<Biome> list = biomeSource.getPlayerSpawnBiomes();
        Random random = new Random(serverLevel.getSeed());
        BlockPos blockPos = biomeSource.findBiomeHorizontal(0, serverLevel.getSeaLevel(), 0, 256, list, random);
        ChunkPos chunkPos2 = chunkPos = blockPos == null ? new ChunkPos(0, 0) : new ChunkPos(blockPos);
        if (blockPos == null) {
            LOGGER.warn("Unable to find spawn biome");
        }
        boolean bl3 = false;
        for (Block block : BlockTags.VALID_SPAWN.getValues()) {
            if (!biomeSource.getSurfaceBlocks().contains(block.defaultBlockState())) continue;
            bl3 = true;
            break;
        }
        serverLevelData.setSpawn(chunkPos.getWorldPosition().offset(8, chunkGenerator.getSpawnHeight(), 8));
        int i = 0;
        int j = 0;
        int k = 0;
        int l = -1;
        int m = 32;
        for (int n = 0; n < 1024; ++n) {
            BlockPos blockPos2;
            if (i > -16 && i <= 16 && j > -16 && j <= 16 && (blockPos2 = dimension.getSpawnPosInChunk(serverLevel.getSeed(), new ChunkPos(chunkPos.x + i, chunkPos.z + j), bl3)) != null) {
                serverLevelData.setSpawn(blockPos2);
                break;
            }
            if (i == j || i < 0 && i == -j || i > 0 && i == 1 - j) {
                int o = k;
                k = -l;
                l = o;
            }
            i += k;
            j += l;
        }
        if (bl) {
            ConfiguredFeature<NoneFeatureConfiguration, ?> configuredFeature = Feature.BONUS_CHEST.configured(FeatureConfiguration.NONE);
            configuredFeature.place(serverLevel, serverLevel.structureFeatureManager(), chunkGenerator, serverLevel.random, new BlockPos(serverLevelData.getXSpawn(), serverLevelData.getYSpawn(), serverLevelData.getZSpawn()));
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

    private void loadDataPacks() {
        this.packRepository.addSource(new ServerPacksSource());
        this.folderPackSource = new FolderRepositorySource(this.storageSource.getLevelPath(LevelResource.DATAPACK_DIR).toFile());
        this.packRepository.addSource(this.folderPackSource);
        this.packRepository.reload();
        ArrayList<UnopenedPack> list = Lists.newArrayList();
        for (String string : this.worldData.getEnabledDataPacks()) {
            UnopenedPack unopenedPack = this.packRepository.getPack(string);
            if (unopenedPack != null) {
                list.add(unopenedPack);
                continue;
            }
            LOGGER.warn("Missing data pack {}", (Object)string);
        }
        this.packRepository.setSelected(list);
        this.updateSelectedPacks();
        this.refreshRegistries();
    }

    private void prepareLevels(ChunkProgressListener chunkProgressListener) {
        ServerLevel serverLevel = this.getLevel(DimensionType.OVERWORLD);
        LOGGER.info("Preparing start region for dimension " + DimensionType.getName(serverLevel.dimensionType()));
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
        for (DimensionType dimensionType : DimensionType.getAllTypes()) {
            ForcedChunksSavedData forcedChunksSavedData = this.getLevel(dimensionType).getDataStorage().get(ForcedChunksSavedData::new, "chunks");
            if (forcedChunksSavedData == null) continue;
            ServerLevel serverLevel2 = this.getLevel(dimensionType);
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

    protected void detectBundledResources() {
        File file = this.storageSource.getLevelPath(LevelResource.MAP_RESOURCE_FILE).toFile();
        if (file.isFile()) {
            String string = this.storageSource.getLevelId();
            try {
                this.setResourcePack("level://" + URLEncoder.encode(string, StandardCharsets.UTF_8.toString()) + "/" + "resources.zip", "");
            } catch (UnsupportedEncodingException unsupportedEncodingException) {
                LOGGER.warn("Something went wrong url encoding {}", (Object)string);
            }
        }
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
                LOGGER.info("Saving chunks for level '{}'/{}", (Object)serverLevel, (Object)DimensionType.getName(serverLevel.dimensionType()));
            }
            serverLevel.save(null, bl2, serverLevel.noSave && !bl3);
            bl4 = true;
        }
        ServerLevel serverLevel2 = this.getLevel(DimensionType.OVERWORLD);
        ServerLevelData serverLevelData = this.worldData.overworldData();
        serverLevelData.setWorldBorder(serverLevel2.getWorldBorder().createSettings());
        this.worldData.setCustomBossEvents(this.getCustomBossEvents().save());
        this.storageSource.saveDataTag(this.worldData, this.getPlayerList().getSingleplayerData());
        return bl4;
    }

    @Override
    public void close() {
        this.stopServer();
    }

    protected void stopServer() {
        LOGGER.info("Stopping server");
        if (this.getConnection() != null) {
            this.getConnection().stop();
        }
        if (this.playerList != null) {
            LOGGER.info("Saving players");
            this.playerList.saveAll();
            this.playerList.removeAll();
        }
        LOGGER.info("Saving worlds");
        for (ServerLevel serverLevel : this.getAllLevels()) {
            if (serverLevel == null) continue;
            serverLevel.noSave = false;
        }
        this.saveAllChunks(false, true, false);
        for (ServerLevel serverLevel : this.getAllLevels()) {
            if (serverLevel == null) continue;
            try {
                serverLevel.close();
            } catch (IOException iOException) {
                LOGGER.error("Exception closing the level", (Throwable)iOException);
            }
        }
        if (this.snooper.isStarted()) {
            this.snooper.interrupt();
        }
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
                LOGGER.error("Error while shutting down", (Throwable)interruptedException);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        try {
            if (this.initServer()) {
                this.nextTickTime = Util.getMillis();
                this.status.setDescription(new TextComponent(this.motd));
                this.status.setVersion(new ServerStatus.Version(SharedConstants.getCurrentVersion().getName(), SharedConstants.getCurrentVersion().getProtocolVersion()));
                this.updateStatusIcon(this.status);
                while (this.running) {
                    long l = Util.getMillis() - this.nextTickTime;
                    if (l > 2000L && this.nextTickTime - this.lastOverloadWarning >= 15000L) {
                        long m = l / 50L;
                        LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", (Object)l, (Object)m);
                        this.nextTickTime += m * 50L;
                        this.lastOverloadWarning = this.nextTickTime;
                    }
                    this.nextTickTime += 50L;
                    SingleTickProfiler singleTickProfiler = SingleTickProfiler.createTickProfiler("Server");
                    this.startProfilerTick(singleTickProfiler);
                    this.profiler.startTick();
                    this.profiler.push("tick");
                    this.tickServer(this::haveTime);
                    this.profiler.popPush("nextTickWait");
                    this.mayHaveDelayedTasks = true;
                    this.delayedTasksMaxNextTickTime = Math.max(Util.getMillis() + 50L, this.nextTickTime);
                    this.waitUntilNextTick();
                    this.profiler.pop();
                    this.profiler.endTick();
                    this.endProfilerTick(singleTickProfiler);
                    this.isReady = true;
                }
            } else {
                this.onServerCrash(null);
            }
        } catch (Throwable throwable) {
            LOGGER.error("Encountered an unexpected exception", throwable);
            CrashReport crashReport = throwable instanceof ReportedException ? this.fillReport(((ReportedException)throwable).getReport()) : this.fillReport(new CrashReport("Exception in server tick loop", throwable));
            File file = new File(new File(this.getServerDirectory(), "crash-reports"), "crash-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + "-server.txt");
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
                this.onServerExit();
            }
        }
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

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void updateStatusIcon(ServerStatus serverStatus) {
        File file = this.getFile("server-icon.png");
        if (!file.exists()) {
            file = this.storageSource.getIconFile();
        }
        if (file.isFile()) {
            ByteBuf byteBuf = Unpooled.buffer();
            try {
                BufferedImage bufferedImage = ImageIO.read(file);
                Validate.validState(bufferedImage.getWidth() == 64, "Must be 64 pixels wide", new Object[0]);
                Validate.validState(bufferedImage.getHeight() == 64, "Must be 64 pixels high", new Object[0]);
                ImageIO.write((RenderedImage)bufferedImage, "PNG", new ByteBufOutputStream(byteBuf));
                ByteBuffer byteBuffer = Base64.getEncoder().encode(byteBuf.nioBuffer());
                serverStatus.setFavicon("data:image/png;base64," + StandardCharsets.UTF_8.decode(byteBuffer));
            } catch (Exception exception) {
                LOGGER.error("Couldn't load server icon", (Throwable)exception);
            } finally {
                byteBuf.release();
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public boolean hasWorldScreenshot() {
        this.hasWorldScreenshot = this.hasWorldScreenshot || this.getWorldScreenshotFile().isFile();
        return this.hasWorldScreenshot;
    }

    @Environment(value=EnvType.CLIENT)
    public File getWorldScreenshotFile() {
        return this.storageSource.getIconFile();
    }

    public File getServerDirectory() {
        return new File(".");
    }

    protected void onServerCrash(CrashReport crashReport) {
    }

    protected void onServerExit() {
    }

    protected void tickServer(BooleanSupplier booleanSupplier) {
        long l = Util.getNanos();
        ++this.tickCount;
        this.tickChildren(booleanSupplier);
        if (l - this.lastServerStatus >= 5000000000L) {
            this.lastServerStatus = l;
            this.status.setPlayers(new ServerStatus.Players(this.getMaxPlayers(), this.getPlayerCount()));
            GameProfile[] gameProfiles = new GameProfile[Math.min(this.getPlayerCount(), 12)];
            int i = Mth.nextInt(this.random, 0, this.getPlayerCount() - gameProfiles.length);
            for (int j = 0; j < gameProfiles.length; ++j) {
                gameProfiles[j] = this.playerList.getPlayers().get(i + j).getGameProfile();
            }
            Collections.shuffle(Arrays.asList(gameProfiles));
            this.status.getPlayers().setSample(gameProfiles);
        }
        if (this.tickCount % 6000 == 0) {
            LOGGER.debug("Autosave started");
            this.profiler.push("save");
            this.playerList.saveAll();
            this.saveAllChunks(true, false, false);
            this.profiler.pop();
            LOGGER.debug("Autosave finished");
        }
        this.profiler.push("snooper");
        if (!this.snooper.isStarted() && this.tickCount > 100) {
            this.snooper.start();
        }
        if (this.tickCount % 6000 == 0) {
            this.snooper.prepare();
        }
        this.profiler.pop();
        this.profiler.push("tallying");
        long l2 = Util.getNanos() - l;
        this.tickTimes[this.tickCount % 100] = l2;
        long m = l2;
        this.averageTickTime = this.averageTickTime * 0.8f + (float)m / 1000000.0f * 0.19999999f;
        long n = Util.getNanos();
        this.frameTimer.logFrameDuration(n - l);
        this.profiler.pop();
    }

    protected void tickChildren(BooleanSupplier booleanSupplier) {
        this.profiler.push("commandFunctions");
        this.getFunctions().tick();
        this.profiler.popPush("levels");
        for (ServerLevel serverLevel : this.getAllLevels()) {
            if (serverLevel.dimensionType() != DimensionType.OVERWORLD && !this.isNetherEnabled()) continue;
            this.profiler.push(() -> serverLevel + " " + Registry.DIMENSION_TYPE.getKey(serverLevel.dimensionType()));
            if (this.tickCount % 20 == 0) {
                this.profiler.push("timeSync");
                this.playerList.broadcastAll(new ClientboundSetTimePacket(serverLevel.getGameTime(), serverLevel.getDayTime(), serverLevel.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)), serverLevel.dimensionType());
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
            GameTestTicker.singleton.tick();
        }
        this.profiler.popPush("server gui refresh");
        for (int i = 0; i < this.tickables.size(); ++i) {
            this.tickables.get(i).run();
        }
        this.profiler.pop();
    }

    public boolean isNetherEnabled() {
        return true;
    }

    public void addTickable(Runnable runnable) {
        this.tickables.add(runnable);
    }

    public static void main(String[] strings) {
        OptionParser optionParser = new OptionParser();
        OptionSpecBuilder optionSpec = optionParser.accepts("nogui");
        OptionSpecBuilder optionSpec2 = optionParser.accepts("initSettings", "Initializes 'server.properties' and 'eula.txt', then quits");
        OptionSpecBuilder optionSpec3 = optionParser.accepts("demo");
        OptionSpecBuilder optionSpec4 = optionParser.accepts("bonusChest");
        OptionSpecBuilder optionSpec5 = optionParser.accepts("forceUpgrade");
        OptionSpecBuilder optionSpec6 = optionParser.accepts("eraseCache");
        AbstractOptionSpec optionSpec7 = optionParser.accepts("help").forHelp();
        ArgumentAcceptingOptionSpec<String> optionSpec8 = optionParser.accepts("singleplayer").withRequiredArg();
        ArgumentAcceptingOptionSpec<String> optionSpec9 = optionParser.accepts("universe").withRequiredArg().defaultsTo(".", (String[])new String[0]);
        ArgumentAcceptingOptionSpec<String> optionSpec10 = optionParser.accepts("world").withRequiredArg();
        ArgumentAcceptingOptionSpec<Integer> optionSpec11 = optionParser.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo(-1, (Integer[])new Integer[0]);
        ArgumentAcceptingOptionSpec<String> optionSpec12 = optionParser.accepts("serverId").withRequiredArg();
        NonOptionArgumentSpec<String> optionSpec13 = optionParser.nonOptions();
        try {
            boolean bl;
            OptionSet optionSet = optionParser.parse(strings);
            if (optionSet.has(optionSpec7)) {
                optionParser.printHelpOn(System.err);
                return;
            }
            Path path = Paths.get("server.properties", new String[0]);
            DedicatedServerSettings dedicatedServerSettings = new DedicatedServerSettings(path);
            dedicatedServerSettings.forceSave();
            Path path2 = Paths.get("eula.txt", new String[0]);
            Eula eula = new Eula(path2);
            if (optionSet.has(optionSpec2)) {
                LOGGER.info("Initialized '" + path.toAbsolutePath().toString() + "' and '" + path2.toAbsolutePath().toString() + "'");
                return;
            }
            if (!eula.hasAgreedToEULA()) {
                LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
                return;
            }
            CrashReport.preload();
            Bootstrap.bootStrap();
            Bootstrap.validate();
            File file = new File(optionSet.valueOf(optionSpec9));
            YggdrasilAuthenticationService yggdrasilAuthenticationService = new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString());
            MinecraftSessionService minecraftSessionService = yggdrasilAuthenticationService.createMinecraftSessionService();
            GameProfileRepository gameProfileRepository = yggdrasilAuthenticationService.createProfileRepository();
            GameProfileCache gameProfileCache = new GameProfileCache(gameProfileRepository, new File(file, USERID_CACHE_FILE.getName()));
            String string = Optional.ofNullable(optionSet.valueOf(optionSpec10)).orElse(dedicatedServerSettings.getProperties().levelName);
            LevelStorageSource levelStorageSource = LevelStorageSource.createDefault(file.toPath());
            LevelStorageSource.LevelStorageAccess levelStorageAccess = levelStorageSource.createAccess(string);
            MinecraftServer.ensureLevelConversion(levelStorageAccess, DataFixers.getDataFixer(), optionSet.has(optionSpec5), optionSet.has(optionSpec6), () -> true);
            WorldData worldData = levelStorageAccess.getDataTag();
            if (worldData == null) {
                LevelSettings levelSettings;
                if (optionSet.has(optionSpec3)) {
                    levelSettings = DEMO_SETTINGS;
                } else {
                    DedicatedServerProperties dedicatedServerProperties = dedicatedServerSettings.getProperties();
                    levelSettings = new LevelSettings(dedicatedServerProperties.levelName, dedicatedServerProperties.gamemode, dedicatedServerProperties.hardcore, dedicatedServerProperties.difficulty, false, new GameRules(), optionSet.has(optionSpec4) ? dedicatedServerProperties.worldGenSettings : dedicatedServerProperties.worldGenSettings.withBonusChest());
                }
                worldData = new PrimaryLevelData(levelSettings);
            }
            final DedicatedServer dedicatedServer = new DedicatedServer(levelStorageAccess, worldData, dedicatedServerSettings, DataFixers.getDataFixer(), minecraftSessionService, gameProfileRepository, gameProfileCache, LoggerChunkProgressListener::new);
            dedicatedServer.setSingleplayerName(optionSet.valueOf(optionSpec8));
            dedicatedServer.setPort(optionSet.valueOf(optionSpec11));
            dedicatedServer.setDemo(optionSet.has(optionSpec3));
            dedicatedServer.setId(optionSet.valueOf(optionSpec12));
            boolean bl2 = bl = !optionSet.has(optionSpec) && !optionSet.valuesOf(optionSpec13).contains("nogui");
            if (bl && !GraphicsEnvironment.isHeadless()) {
                dedicatedServer.showGui();
            }
            dedicatedServer.forkAndRun();
            Thread thread = new Thread("Server Shutdown Thread"){

                @Override
                public void run() {
                    dedicatedServer.halt(true);
                }
            };
            thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
            Runtime.getRuntime().addShutdownHook(thread);
        } catch (Exception exception) {
            LOGGER.fatal("Failed to start the minecraft server", (Throwable)exception);
        }
    }

    protected void setId(String string) {
        this.serverId = string;
    }

    public void forkAndRun() {
        this.serverThread.start();
    }

    @Environment(value=EnvType.CLIENT)
    public boolean isShutdown() {
        return !this.serverThread.isAlive();
    }

    public File getFile(String string) {
        return new File(this.getServerDirectory(), string);
    }

    public ServerLevel getLevel(DimensionType dimensionType) {
        return this.levels.get(dimensionType);
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

    public String getServerModName() {
        return "vanilla";
    }

    public CrashReport fillReport(CrashReport crashReport) {
        if (this.playerList != null) {
            crashReport.getSystemDetails().setDetail("Player Count", () -> this.playerList.getPlayerCount() + " / " + this.playerList.getMaxPlayers() + "; " + this.playerList.getPlayers());
        }
        crashReport.getSystemDetails().setDetail("Data Packs", () -> {
            StringBuilder stringBuilder = new StringBuilder();
            for (UnopenedPack unopenedPack : this.packRepository.getSelected()) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(", ");
                }
                stringBuilder.append(unopenedPack.getId());
                if (unopenedPack.getCompatibility().isCompatible()) continue;
                stringBuilder.append(" (incompatible)");
            }
            return stringBuilder.toString();
        });
        if (this.serverId != null) {
            crashReport.getSystemDetails().setDetail("Server Id", () -> this.serverId);
        }
        return crashReport;
    }

    public abstract Optional<String> getModdedStatus();

    @Override
    public void sendMessage(Component component) {
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

    public String getSingleplayerName() {
        return this.singleplayerName;
    }

    public void setSingleplayerName(String string) {
        this.singleplayerName = string;
    }

    public boolean isSingleplayer() {
        return this.singleplayerName != null;
    }

    public void setKeyPair(KeyPair keyPair) {
        this.keyPair = keyPair;
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

    protected boolean isSpawningMonsters() {
        return this.worldData.getDifficulty() != Difficulty.PEACEFUL;
    }

    public boolean isDemo() {
        return this.isDemo;
    }

    public void setDemo(boolean bl) {
        this.isDemo = bl;
    }

    public String getResourcePack() {
        return this.resourcePack;
    }

    public String getResourcePackHash() {
        return this.resourcePackHash;
    }

    public void setResourcePack(String string, String string2) {
        this.resourcePack = string;
        this.resourcePackHash = string2;
    }

    @Override
    public void populateSnooper(Snooper snooper) {
        snooper.setDynamicData("whitelist_enabled", false);
        snooper.setDynamicData("whitelist_count", 0);
        if (this.playerList != null) {
            snooper.setDynamicData("players_current", this.getPlayerCount());
            snooper.setDynamicData("players_max", this.getMaxPlayers());
            snooper.setDynamicData("players_seen", this.playerDataStorage.getSeenPlayers().length);
        }
        snooper.setDynamicData("uses_auth", this.onlineMode);
        snooper.setDynamicData("gui_state", this.hasGui() ? "enabled" : "disabled");
        snooper.setDynamicData("run_time", (Util.getMillis() - snooper.getStartupTime()) / 60L * 1000L);
        snooper.setDynamicData("avg_tick_ms", (int)(Mth.average(this.tickTimes) * 1.0E-6));
        int i = 0;
        for (ServerLevel serverLevel : this.getAllLevels()) {
            if (serverLevel == null) continue;
            snooper.setDynamicData("world[" + i + "][dimension]", serverLevel.dimensionType());
            snooper.setDynamicData("world[" + i + "][mode]", (Object)this.worldData.getGameType());
            snooper.setDynamicData("world[" + i + "][difficulty]", (Object)serverLevel.getDifficulty());
            snooper.setDynamicData("world[" + i + "][hardcore]", this.worldData.isHardcore());
            snooper.setDynamicData("world[" + i + "][height]", this.maxBuildHeight);
            snooper.setDynamicData("world[" + i + "][chunks_loaded]", serverLevel.getChunkSource().getLoadedChunksCount());
            ++i;
        }
        snooper.setDynamicData("worlds", i);
    }

    public abstract boolean isDedicatedServer();

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

    public int getMaxBuildHeight() {
        return this.maxBuildHeight;
    }

    public void setMaxBuildHeight(int i) {
        this.maxBuildHeight = i;
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

    @Environment(value=EnvType.CLIENT)
    public boolean isReady() {
        return this.isReady;
    }

    public boolean hasGui() {
        return false;
    }

    public abstract boolean publishServer(GameType var1, boolean var2, int var3);

    public int getTickCount() {
        return this.tickCount;
    }

    @Environment(value=EnvType.CLIENT)
    public Snooper getSnooper() {
        return this.snooper;
    }

    public int getSpawnProtectionRadius() {
        return 16;
    }

    public boolean isUnderSpawnProtection(ServerLevel serverLevel, BlockPos blockPos, Player player) {
        return false;
    }

    public void setForceGameType(boolean bl) {
        this.forceGameType = bl;
    }

    public boolean getForceGameType() {
        return this.forceGameType;
    }

    public boolean repliesToStatus() {
        return true;
    }

    public int getPlayerIdleTimeout() {
        return this.playerIdleTimeout;
    }

    public void setPlayerIdleTimeout(int i) {
        this.playerIdleTimeout = i;
    }

    public MinecraftSessionService getSessionService() {
        return this.sessionService;
    }

    public GameProfileRepository getProfileRepository() {
        return this.profileRepository;
    }

    public GameProfileCache getProfileCache() {
        return this.profileCache;
    }

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
    public Thread getRunningThread() {
        return this.serverThread;
    }

    public int getCompressionThreshold() {
        return 256;
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
        return this.advancements;
    }

    public ServerFunctionManager getFunctions() {
        return this.functions;
    }

    public void reloadResources() {
        if (!this.isSameThread()) {
            this.execute(this::reloadResources);
            return;
        }
        this.getPlayerList().saveAll();
        this.packRepository.reload();
        this.updateSelectedPacks();
        this.getPlayerList().reloadResources();
        this.refreshRegistries();
    }

    private void updateSelectedPacks() {
        ArrayList<UnopenedPack> list = Lists.newArrayList(this.packRepository.getSelected());
        for (UnopenedPack unopenedPack2 : this.packRepository.getAvailable()) {
            if (this.worldData.getDisabledDataPacks().contains(unopenedPack2.getId()) || list.contains(unopenedPack2)) continue;
            LOGGER.info("Found new data pack {}, loading it automatically", (Object)unopenedPack2.getId());
            unopenedPack2.getDefaultPosition().insert(list, unopenedPack2, unopenedPack -> unopenedPack, false);
        }
        this.packRepository.setSelected(list);
        ArrayList<Pack> list2 = Lists.newArrayList();
        this.packRepository.getSelected().forEach(unopenedPack -> list2.add(unopenedPack.open()));
        CompletableFuture<Unit> completableFuture = this.resources.reload(this.executor, this, list2, DATA_RELOAD_INITIAL_TASK);
        this.managedBlock(completableFuture::isDone);
        try {
            completableFuture.get();
        } catch (Exception exception) {
            LOGGER.error("Failed to reload data packs", (Throwable)exception);
        }
        this.worldData.getEnabledDataPacks().clear();
        this.worldData.getDisabledDataPacks().clear();
        this.packRepository.getSelected().forEach(unopenedPack -> this.worldData.getEnabledDataPacks().add(unopenedPack.getId()));
        this.packRepository.getAvailable().forEach(unopenedPack -> {
            if (!this.packRepository.getSelected().contains(unopenedPack)) {
                this.worldData.getDisabledDataPacks().add(unopenedPack.getId());
            }
        });
    }

    public void kickUnlistedPlayers(CommandSourceStack commandSourceStack) {
        if (!this.isEnforceWhitelist()) {
            return;
        }
        PlayerList playerList = commandSourceStack.getServer().getPlayerList();
        UserWhiteList userWhiteList = playerList.getWhiteList();
        if (!userWhiteList.isEnabled()) {
            return;
        }
        ArrayList<ServerPlayer> list = Lists.newArrayList(playerList.getPlayers());
        for (ServerPlayer serverPlayer : list) {
            if (userWhiteList.isWhiteListed(serverPlayer.getGameProfile())) continue;
            serverPlayer.connection.disconnect(new TranslatableComponent("multiplayer.disconnect.not_whitelisted"));
        }
    }

    public ReloadableResourceManager getResources() {
        return this.resources;
    }

    public PackRepository<UnopenedPack> getPackRepository() {
        return this.packRepository;
    }

    public Commands getCommands() {
        return this.commands;
    }

    public CommandSourceStack createCommandSourceStack() {
        return new CommandSourceStack(this, this.getLevel(DimensionType.OVERWORLD) == null ? Vec3.ZERO : Vec3.atLowerCornerOf(this.getLevel(DimensionType.OVERWORLD).getSharedSpawnPos()), Vec2.ZERO, this.getLevel(DimensionType.OVERWORLD), 4, "Server", new TextComponent("Server"), this, null);
    }

    @Override
    public boolean acceptsSuccess() {
        return true;
    }

    @Override
    public boolean acceptsFailure() {
        return true;
    }

    public RecipeManager getRecipeManager() {
        return this.recipes;
    }

    public TagManager getTags() {
        return this.tags;
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
        return this.lootTables;
    }

    public PredicateManager getPredicateManager() {
        return this.predicateManager;
    }

    public GameRules getGameRules() {
        return this.getLevel(DimensionType.OVERWORLD).getGameRules();
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

    @Environment(value=EnvType.CLIENT)
    public FrameTimer getFrameTimer() {
        return this.frameTimer;
    }

    public ProfilerFiller getProfiler() {
        return this.profiler;
    }

    public Executor getBackgroundTaskExecutor() {
        return this.executor;
    }

    public abstract boolean isSingleplayerOwner(GameProfile var1);

    public void saveDebugReport(Path path) throws IOException {
        Path path2 = path.resolve("levels");
        for (Map.Entry<DimensionType, ServerLevel> entry : this.levels.entrySet()) {
            ResourceLocation resourceLocation = DimensionType.getName(entry.getKey());
            Path path3 = path2.resolve(resourceLocation.getNamespace()).resolve(resourceLocation.getPath());
            Files.createDirectories(path3, new FileAttribute[0]);
            entry.getValue().saveDebugReport(path3);
        }
        this.dumpGameRules(path.resolve("gamerules.txt"));
        this.dumpClasspath(path.resolve("classpath.txt"));
        this.dumpCrashCategory(path.resolve("example_crash.txt"));
        this.dumpMiscStats(path.resolve("stats.txt"));
        this.dumpThreads(path.resolve("threads.txt"));
    }

    private void dumpMiscStats(Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, new OpenOption[0]);){
            writer.write(String.format("pending_tasks: %d\n", this.getPendingTasksCount()));
            writer.write(String.format("average_tick_time: %f\n", Float.valueOf(this.getAverageTickTime())));
            writer.write(String.format("tick_times: %s\n", Arrays.toString(this.tickTimes)));
            writer.write(String.format("queue: %s\n", Util.backgroundExecutor()));
        }
    }

    private void dumpCrashCategory(Path path) throws IOException {
        CrashReport crashReport = new CrashReport("Server dump", new Exception("dummy"));
        this.fillReport(crashReport);
        try (BufferedWriter writer = Files.newBufferedWriter(path, new OpenOption[0]);){
            writer.write(crashReport.getFriendlyReport());
        }
    }

    private void dumpGameRules(Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, new OpenOption[0]);){
            final ArrayList<String> list = Lists.newArrayList();
            final GameRules gameRules = this.getGameRules();
            GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor(){

                @Override
                public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                    list.add(String.format("%s=%s\n", key.getId(), ((GameRules.Value)gameRules.getRule(key)).toString()));
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

    private void refreshRegistries() {
        Blocks.rebuildCache();
    }

    private void startProfilerTick(@Nullable SingleTickProfiler singleTickProfiler) {
        if (this.delayProfilerStart) {
            this.delayProfilerStart = false;
            this.continousProfiler.enable();
        }
        this.profiler = SingleTickProfiler.decorateFiller(this.continousProfiler.getFiller(), singleTickProfiler);
    }

    private void endProfilerTick(@Nullable SingleTickProfiler singleTickProfiler) {
        if (singleTickProfiler != null) {
            singleTickProfiler.endTick();
        }
        this.profiler = this.continousProfiler.getFiller();
    }

    public boolean isProfiling() {
        return this.continousProfiler.isEnabled();
    }

    public void startProfiling() {
        this.delayProfilerStart = true;
    }

    public ProfileResults finishProfiling() {
        ProfileResults profileResults = this.continousProfiler.getResults();
        this.continousProfiler.disable();
        return profileResults;
    }

    public Path getWorldPath(LevelResource levelResource) {
        return this.storageSource.getLevelPath(levelResource);
    }

    public boolean forceSynchronousWrites() {
        return true;
    }

    public StructureManager getStructureManager() {
        return this.structureManager;
    }

    public WorldData getWorldData() {
        return this.worldData;
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
}

