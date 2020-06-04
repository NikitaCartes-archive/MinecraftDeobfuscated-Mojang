package net.minecraft.server;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.longs.LongIterator;
import java.awt.image.BufferedImage;
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
import java.nio.file.Path;
import java.security.KeyPair;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportDetail;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
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
import net.minecraft.util.profiling.ContinuousProfiler;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.SingleTickProfiler;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Snooper;
import net.minecraft.world.SnooperPopulator;
import net.minecraft.world.entity.ai.village.VillageSiege;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.entity.npc.WanderingTraderSpawner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.saveddata.SaveDataDirtyRunnable;
import net.minecraft.world.level.storage.CommandStorage;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PlayerDataStorage;
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

public abstract class MinecraftServer extends ReentrantBlockableEventLoop<TickTask> implements SnooperPopulator, CommandSource, AutoCloseable {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final File USERID_CACHE_FILE = new File("usercache.json");
	public static final LevelSettings DEMO_SETTINGS = new LevelSettings(
		"Demo World", GameType.SURVIVAL, false, Difficulty.NORMAL, false, new GameRules(), DataPackConfig.DEFAULT
	);
	protected final LevelStorageSource.LevelStorageAccess storageSource;
	protected final PlayerDataStorage playerDataStorage;
	private final Snooper snooper = new Snooper("server", this, Util.getMillis());
	private final List<Runnable> tickables = Lists.<Runnable>newArrayList();
	private ContinuousProfiler continousProfiler = new ContinuousProfiler(Util.timeSource, this::getTickCount);
	private ProfilerFiller profiler = InactiveProfiler.INSTANCE;
	private final ServerConnectionListener connection;
	private final ChunkProgressListenerFactory progressListenerFactory;
	private final ServerStatus status = new ServerStatus();
	private final Random random = new Random();
	private final DataFixer fixerUpper;
	private String localIp;
	private int port = -1;
	protected final RegistryAccess.RegistryHolder registryHolder;
	private final Map<ResourceKey<Level>, ServerLevel> levels = Maps.<ResourceKey<Level>, ServerLevel>newLinkedHashMap();
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
	private final Thread serverThread;
	private long nextTickTime = Util.getMillis();
	private long delayedTasksMaxNextTickTime;
	private boolean mayHaveDelayedTasks;
	@Environment(EnvType.CLIENT)
	private boolean hasWorldScreenshot;
	private final PackRepository<Pack> packRepository;
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
	private ServerResources resources;
	private final StructureManager structureManager;
	protected final WorldData worldData;

	public static <S extends MinecraftServer> S spin(Function<Thread, S> function) {
		AtomicReference<S> atomicReference = new AtomicReference();
		Thread thread = new Thread(() -> ((MinecraftServer)atomicReference.get()).runServer(), "Server thread");
		thread.setUncaughtExceptionHandler((threadx, throwable) -> LOGGER.error(throwable));
		S minecraftServer = (S)function.apply(thread);
		atomicReference.set(minecraftServer);
		thread.start();
		return minecraftServer;
	}

	public MinecraftServer(
		Thread thread,
		RegistryAccess.RegistryHolder registryHolder,
		LevelStorageSource.LevelStorageAccess levelStorageAccess,
		WorldData worldData,
		PackRepository<Pack> packRepository,
		Proxy proxy,
		DataFixer dataFixer,
		ServerResources serverResources,
		MinecraftSessionService minecraftSessionService,
		GameProfileRepository gameProfileRepository,
		GameProfileCache gameProfileCache,
		ChunkProgressListenerFactory chunkProgressListenerFactory
	) {
		super("Server");
		this.registryHolder = registryHolder;
		this.worldData = worldData;
		this.proxy = proxy;
		this.packRepository = packRepository;
		this.resources = serverResources;
		this.sessionService = minecraftSessionService;
		this.profileRepository = gameProfileRepository;
		this.profileCache = gameProfileCache;
		this.connection = new ServerConnectionListener(this);
		this.progressListenerFactory = chunkProgressListenerFactory;
		this.storageSource = levelStorageAccess;
		this.playerDataStorage = levelStorageAccess.createPlayerStorage();
		this.fixerUpper = dataFixer;
		this.functionManager = new ServerFunctionManager(this, serverResources.getFunctionLibrary());
		this.structureManager = new StructureManager(serverResources.getResourceManager(), levelStorageAccess, dataFixer);
		this.serverThread = thread;
		this.executor = Util.backgroundExecutor();
	}

	private void readScoreboard(DimensionDataStorage dimensionDataStorage) {
		ScoreboardSaveData scoreboardSaveData = dimensionDataStorage.computeIfAbsent(ScoreboardSaveData::new, "scoreboard");
		scoreboardSaveData.setScoreboard(this.getScoreboard());
		this.getScoreboard().addDirtyListener(new SaveDataDirtyRunnable(scoreboardSaveData));
	}

	protected abstract boolean initServer() throws IOException;

	public static void convertFromRegionFormatIfNeeded(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
		if (levelStorageAccess.requiresConversion()) {
			LOGGER.info("Converting map!");
			levelStorageAccess.convertLevel(new ProgressListener() {
				private long timeStamp = Util.getMillis();

				@Override
				public void progressStartNoAbort(Component component) {
				}

				@Environment(EnvType.CLIENT)
				@Override
				public void progressStart(Component component) {
				}

				@Override
				public void progressStagePercentage(int i) {
					if (Util.getMillis() - this.timeStamp >= 1000L) {
						this.timeStamp = Util.getMillis();
						MinecraftServer.LOGGER.info("Converting... {}%", i);
					}
				}

				@Environment(EnvType.CLIENT)
				@Override
				public void stop() {
				}

				@Override
				public void progressStage(Component component) {
				}
			});
		}
	}

	protected void loadLevel() {
		this.detectBundledResources();
		this.worldData.setModdedInfo(this.getServerModName(), this.getModdedStatus().isPresent());
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
		List<CustomSpawner> list = ImmutableList.of(
			new PhantomSpawner(), new PatrolSpawner(), new CatSpawner(), new VillageSiege(), new WanderingTraderSpawner(serverLevelData)
		);
		MappedRegistry<LevelStem> mappedRegistry = worldGenSettings.dimensions();
		LevelStem levelStem = mappedRegistry.get(LevelStem.OVERWORLD);
		ChunkGenerator chunkGenerator;
		DimensionType dimensionType;
		if (levelStem == null) {
			dimensionType = DimensionType.defaultOverworld();
			chunkGenerator = WorldGenSettings.makeDefaultOverworld(new Random().nextLong());
		} else {
			dimensionType = levelStem.type();
			chunkGenerator = levelStem.generator();
		}

		ServerLevel serverLevel = new ServerLevel(
			this,
			this.executor,
			this.storageSource,
			serverLevelData,
			Level.OVERWORLD,
			DimensionType.OVERWORLD_LOCATION,
			dimensionType,
			chunkProgressListener,
			chunkGenerator,
			bl,
			m,
			list,
			true
		);
		this.levels.put(Level.OVERWORLD, serverLevel);
		DimensionDataStorage dimensionDataStorage = serverLevel.getDataStorage();
		this.readScoreboard(dimensionDataStorage);
		this.commandStorage = new CommandStorage(dimensionDataStorage);
		WorldBorder worldBorder = serverLevel.getWorldBorder();
		worldBorder.applySettings(serverLevelData.getWorldBorder());
		if (!serverLevelData.isInitialized()) {
			try {
				setInitialSpawn(serverLevel, serverLevelData, worldGenSettings.generateBonusChest(), bl, true);
				serverLevelData.setInitialized(true);
				if (bl) {
					this.setupDebugLevel(this.worldData);
				}
			} catch (Throwable var27) {
				CrashReport crashReport = CrashReport.forThrowable(var27, "Exception initializing level");

				try {
					serverLevel.fillReportDetails(crashReport);
				} catch (Throwable var26) {
				}

				throw new ReportedException(crashReport);
			}

			serverLevelData.setInitialized(true);
		}

		this.getPlayerList().setLevel(serverLevel);
		if (this.worldData.getCustomBossEvents() != null) {
			this.getCustomBossEvents().load(this.worldData.getCustomBossEvents());
		}

		for (Entry<ResourceKey<LevelStem>, LevelStem> entry : mappedRegistry.entrySet()) {
			ResourceKey<LevelStem> resourceKey = (ResourceKey<LevelStem>)entry.getKey();
			if (resourceKey != LevelStem.OVERWORLD) {
				ResourceKey<Level> resourceKey2 = ResourceKey.create(Registry.DIMENSION_REGISTRY, resourceKey.location());
				DimensionType dimensionType2 = ((LevelStem)entry.getValue()).type();
				ResourceKey<DimensionType> resourceKey3 = (ResourceKey<DimensionType>)this.registryHolder
					.dimensionTypes()
					.getResourceKey(dimensionType2)
					.orElseThrow(() -> new IllegalStateException("Unregistered dimension type: " + dimensionType2));
				ChunkGenerator chunkGenerator2 = ((LevelStem)entry.getValue()).generator();
				DerivedLevelData derivedLevelData = new DerivedLevelData(this.worldData, serverLevelData);
				ServerLevel serverLevel2 = new ServerLevel(
					this,
					this.executor,
					this.storageSource,
					derivedLevelData,
					resourceKey2,
					resourceKey3,
					dimensionType2,
					chunkProgressListener,
					chunkGenerator2,
					bl,
					m,
					ImmutableList.of(),
					false
				);
				worldBorder.addListener(new BorderChangeListener.DelegateBorderChangeListener(serverLevel2.getWorldBorder()));
				this.levels.put(resourceKey2, serverLevel2);
			}
		}
	}

	private static void setInitialSpawn(ServerLevel serverLevel, ServerLevelData serverLevelData, boolean bl, boolean bl2, boolean bl3) {
		ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
		if (!bl3) {
			serverLevelData.setSpawn(BlockPos.ZERO.above(chunkGenerator.getSpawnHeight()));
		} else if (bl2) {
			serverLevelData.setSpawn(BlockPos.ZERO.above());
		} else {
			BiomeSource biomeSource = chunkGenerator.getBiomeSource();
			List<Biome> list = biomeSource.getPlayerSpawnBiomes();
			Random random = new Random(serverLevel.getSeed());
			BlockPos blockPos = biomeSource.findBiomeHorizontal(0, serverLevel.getSeaLevel(), 0, 256, list, random);
			ChunkPos chunkPos = blockPos == null ? new ChunkPos(0, 0) : new ChunkPos(blockPos);
			if (blockPos == null) {
				LOGGER.warn("Unable to find spawn biome");
			}

			boolean bl4 = false;

			for (Block block : BlockTags.VALID_SPAWN.getValues()) {
				if (biomeSource.getSurfaceBlocks().contains(block.defaultBlockState())) {
					bl4 = true;
					break;
				}
			}

			serverLevelData.setSpawn(chunkPos.getWorldPosition().offset(8, chunkGenerator.getSpawnHeight(), 8));
			int i = 0;
			int j = 0;
			int k = 0;
			int l = -1;
			int m = 32;

			for (int n = 0; n < 1024; n++) {
				if (i > -16 && i <= 16 && j > -16 && j <= 16) {
					BlockPos blockPos2 = PlayerRespawnLogic.getSpawnPosInChunk(serverLevel, new ChunkPos(chunkPos.x + i, chunkPos.z + j), bl4);
					if (blockPos2 != null) {
						serverLevelData.setSpawn(blockPos2);
						break;
					}
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
				ConfiguredFeature<?, ?> configuredFeature = Feature.BONUS_CHEST.configured(FeatureConfiguration.NONE);
				configuredFeature.place(
					serverLevel,
					serverLevel.structureFeatureManager(),
					chunkGenerator,
					serverLevel.random,
					new BlockPos(serverLevelData.getXSpawn(), serverLevelData.getYSpawn(), serverLevelData.getZSpawn())
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
		ServerLevel serverLevel = this.getLevel(Level.OVERWORLD);
		LOGGER.info("Preparing start region for dimension {}", serverLevel.dimension().location());
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
			ForcedChunksSavedData forcedChunksSavedData = serverLevel2.getDataStorage().get(ForcedChunksSavedData::new, "chunks");
			if (forcedChunksSavedData != null) {
				LongIterator longIterator = forcedChunksSavedData.getChunks().iterator();

				while (longIterator.hasNext()) {
					long l = longIterator.nextLong();
					ChunkPos chunkPos = new ChunkPos(l);
					serverLevel2.getChunkSource().updateChunkForced(chunkPos, true);
				}
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
			} catch (UnsupportedEncodingException var4) {
				LOGGER.warn("Something went wrong url encoding {}", string);
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
				LOGGER.info("Saving chunks for level '{}'/{}", serverLevel, serverLevel.dimension().location());
			}

			serverLevel.save(null, bl2, serverLevel.noSave && !bl3);
			bl4 = true;
		}

		ServerLevel serverLevel2 = this.getLevel(Level.OVERWORLD);
		ServerLevelData serverLevelData = this.worldData.overworldData();
		serverLevelData.setWorldBorder(serverLevel2.getWorldBorder().createSettings());
		this.worldData.setCustomBossEvents(this.getCustomBossEvents().save());
		this.storageSource.saveDataTag(this.registryHolder, this.worldData, this.getPlayerList().getSingleplayerData());
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
			if (serverLevel != null) {
				serverLevel.noSave = false;
			}
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

		if (this.snooper.isStarted()) {
			this.snooper.interrupt();
		}

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
			if (this.initServer()) {
				this.nextTickTime = Util.getMillis();
				this.status.setDescription(new TextComponent(this.motd));
				this.status.setVersion(new ServerStatus.Version(SharedConstants.getCurrentVersion().getName(), SharedConstants.getCurrentVersion().getProtocolVersion()));
				this.updateStatusIcon(this.status);

				while (this.running) {
					long l = Util.getMillis() - this.nextTickTime;
					if (l > 2000L && this.nextTickTime - this.lastOverloadWarning >= 15000L) {
						long m = l / 50L;
						LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", l, m);
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
		} catch (Throwable var44) {
			LOGGER.error("Encountered an unexpected exception", var44);
			CrashReport crashReport;
			if (var44 instanceof ReportedException) {
				crashReport = this.fillReport(((ReportedException)var44).getReport());
			} else {
				crashReport = this.fillReport(new CrashReport("Exception in server tick loop", var44));
			}

			File file = new File(
				new File(this.getServerDirectory(), "crash-reports"), "crash-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + "-server.txt"
			);
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
			} catch (Throwable var42) {
				LOGGER.error("Exception stopping the server", var42);
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
			if (this.haveTime()) {
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

	private void updateStatusIcon(ServerStatus serverStatus) {
		File file = this.getFile("server-icon.png");
		if (!file.exists()) {
			file = this.storageSource.getIconFile();
		}

		if (file.isFile()) {
			ByteBuf byteBuf = Unpooled.buffer();

			try {
				BufferedImage bufferedImage = ImageIO.read(file);
				Validate.validState(bufferedImage.getWidth() == 64, "Must be 64 pixels wide");
				Validate.validState(bufferedImage.getHeight() == 64, "Must be 64 pixels high");
				ImageIO.write(bufferedImage, "PNG", new ByteBufOutputStream(byteBuf));
				ByteBuffer byteBuffer = Base64.getEncoder().encode(byteBuf.nioBuffer());
				serverStatus.setFavicon("data:image/png;base64," + StandardCharsets.UTF_8.decode(byteBuffer));
			} catch (Exception var9) {
				LOGGER.error("Couldn't load server icon", (Throwable)var9);
			} finally {
				byteBuf.release();
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public boolean hasWorldScreenshot() {
		this.hasWorldScreenshot = this.hasWorldScreenshot || this.getWorldScreenshotFile().isFile();
		return this.hasWorldScreenshot;
	}

	@Environment(EnvType.CLIENT)
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
		this.tickCount++;
		this.tickChildren(booleanSupplier);
		if (l - this.lastServerStatus >= 5000000000L) {
			this.lastServerStatus = l;
			this.status.setPlayers(new ServerStatus.Players(this.getMaxPlayers(), this.getPlayerCount()));
			GameProfile[] gameProfiles = new GameProfile[Math.min(this.getPlayerCount(), 12)];
			int i = Mth.nextInt(this.random, 0, this.getPlayerCount() - gameProfiles.length);

			for (int j = 0; j < gameProfiles.length; j++) {
				gameProfiles[j] = ((ServerPlayer)this.playerList.getPlayers().get(i + j)).getGameProfile();
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
		long m = this.tickTimes[this.tickCount % 100] = Util.getNanos() - l;
		this.averageTickTime = this.averageTickTime * 0.8F + (float)m / 1000000.0F * 0.19999999F;
		long n = Util.getNanos();
		this.frameTimer.logFrameDuration(n - l);
		this.profiler.pop();
	}

	protected void tickChildren(BooleanSupplier booleanSupplier) {
		this.profiler.push("commandFunctions");
		this.getFunctions().tick();
		this.profiler.popPush("levels");

		for (ServerLevel serverLevel : this.getAllLevels()) {
			if (serverLevel.dimensionType().isOverworld() || this.isNetherEnabled()) {
				this.profiler.push((Supplier<String>)(() -> serverLevel + " " + serverLevel.dimension().location()));
				if (this.tickCount % 20 == 0) {
					this.profiler.push("timeSync");
					this.playerList
						.broadcastAll(
							new ClientboundSetTimePacket(serverLevel.getGameTime(), serverLevel.getDayTime(), serverLevel.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)),
							serverLevel.dimension()
						);
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
		}

		this.profiler.popPush("connection");
		this.getConnection().tick();
		this.profiler.popPush("players");
		this.playerList.tick();
		if (SharedConstants.IS_RUNNING_IN_IDE) {
			GameTestTicker.singleton.tick();
		}

		this.profiler.popPush("server gui refresh");

		for (int i = 0; i < this.tickables.size(); i++) {
			((Runnable)this.tickables.get(i)).run();
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

	@Environment(EnvType.CLIENT)
	public boolean isShutdown() {
		return !this.serverThread.isAlive();
	}

	public File getFile(String string) {
		return new File(this.getServerDirectory(), string);
	}

	public ServerLevel getLevel(ResourceKey<Level> resourceKey) {
		return (ServerLevel)this.levels.get(resourceKey);
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

	public String getServerModName() {
		return "vanilla";
	}

	public CrashReport fillReport(CrashReport crashReport) {
		if (this.playerList != null) {
			crashReport.getSystemDetails()
				.setDetail(
					"Player Count",
					(CrashReportDetail<String>)(() -> this.playerList.getPlayerCount() + " / " + this.playerList.getMaxPlayers() + "; " + this.playerList.getPlayers())
				);
		}

		crashReport.getSystemDetails().setDetail("Data Packs", (CrashReportDetail<String>)(() -> {
			StringBuilder stringBuilder = new StringBuilder();

			for (Pack pack : this.packRepository.getSelectedPacks()) {
				if (stringBuilder.length() > 0) {
					stringBuilder.append(", ");
				}

				stringBuilder.append(pack.getId());
				if (!pack.getCompatibility().isCompatible()) {
					stringBuilder.append(" (incompatible)");
				}
			}

			return stringBuilder.toString();
		}));
		if (this.serverId != null) {
			crashReport.getSystemDetails().setDetail("Server Id", (CrashReportDetail<String>)(() -> this.serverId));
		}

		return crashReport;
	}

	public abstract Optional<String> getModdedStatus();

	@Override
	public void sendMessage(Component component, UUID uUID) {
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
			if (serverLevel != null) {
				snooper.setDynamicData("world[" + i + "][dimension]", serverLevel.dimension().location());
				snooper.setDynamicData("world[" + i + "][mode]", this.worldData.getGameType());
				snooper.setDynamicData("world[" + i + "][difficulty]", serverLevel.getDifficulty());
				snooper.setDynamicData("world[" + i + "][hardcore]", this.worldData.isHardcore());
				snooper.setDynamicData("world[" + i + "][height]", this.maxBuildHeight);
				snooper.setDynamicData("world[" + i + "][chunks_loaded]", serverLevel.getChunkSource().getLoadedChunksCount());
				i++;
			}
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

	@Environment(EnvType.CLIENT)
	public boolean isReady() {
		return this.isReady;
	}

	public boolean hasGui() {
		return false;
	}

	public abstract boolean publishServer(GameType gameType, boolean bl, int i);

	public int getTickCount() {
		return this.tickCount;
	}

	@Environment(EnvType.CLIENT)
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
		return serverLevel != null ? serverLevel.getGameRules().getInt(GameRules.RULE_SPAWN_RADIUS) : 10;
	}

	public ServerAdvancementManager getAdvancements() {
		return this.resources.getAdvancements();
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
				immutableList -> ServerResources.loadResources(
						immutableList,
						this.isDedicatedServer() ? Commands.CommandSelection.DEDICATED : Commands.CommandSelection.INTEGRATED,
						this.getFunctionCompilationLevel(),
						this.executor,
						this
					)
			)
			.thenAcceptAsync(serverResources -> {
				this.resources.close();
				this.resources = serverResources;
				this.packRepository.setSelected(collection);
				this.worldData.setDataPackConfig(getSelectedPacks(this.packRepository));
				serverResources.updateGlobals();
				this.getPlayerList().saveAll();
				this.getPlayerList().reloadResources();
				this.functionManager.replaceLibrary(this.resources.getFunctionLibrary());
				this.structureManager.onResourceManagerReload(this.resources.getResourceManager());
			}, this);
		if (this.isSameThread()) {
			this.managedBlock(completableFuture::isDone);
		}

		return completableFuture;
	}

	public static DataPackConfig configurePackRepository(PackRepository<Pack> packRepository, DataPackConfig dataPackConfig, boolean bl) {
		packRepository.reload();
		if (bl) {
			packRepository.setSelected(Collections.singleton("vanilla"));
			return new DataPackConfig(ImmutableList.of("vanilla"), ImmutableList.of());
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
				if (!dataPackConfig.getDisabled().contains(string2) && !set.contains(string2)) {
					LOGGER.info("Found new data pack {}, loading it automatically", string2);
					set.add(string2);
				}
			}

			if (set.isEmpty()) {
				LOGGER.info("No datapacks selected, forcing vanilla");
				set.add("vanilla");
			}

			packRepository.setSelected(set);
			return getSelectedPacks(packRepository);
		}
	}

	private static DataPackConfig getSelectedPacks(PackRepository<?> packRepository) {
		Collection<String> collection = packRepository.getSelectedIds();
		List<String> list = ImmutableList.copyOf(collection);
		List<String> list2 = (List<String>)packRepository.getAvailableIds()
			.stream()
			.filter(string -> !collection.contains(string))
			.collect(ImmutableList.toImmutableList());
		return new DataPackConfig(list, list2);
	}

	public void kickUnlistedPlayers(CommandSourceStack commandSourceStack) {
		if (this.isEnforceWhitelist()) {
			PlayerList playerList = commandSourceStack.getServer().getPlayerList();
			UserWhiteList userWhiteList = playerList.getWhiteList();

			for (ServerPlayer serverPlayer : Lists.newArrayList(playerList.getPlayers())) {
				if (!userWhiteList.isWhiteListed(serverPlayer.getGameProfile())) {
					serverPlayer.connection.disconnect(new TranslatableComponent("multiplayer.disconnect.not_whitelisted"));
				}
			}
		}
	}

	public PackRepository<Pack> getPackRepository() {
		return this.packRepository;
	}

	public Commands getCommands() {
		return this.resources.getCommands();
	}

	public CommandSourceStack createCommandSourceStack() {
		ServerLevel serverLevel = this.getLevel(Level.OVERWORLD);
		return new CommandSourceStack(
			this,
			serverLevel == null ? Vec3.ZERO : Vec3.atLowerCornerOf(serverLevel.getSharedSpawnPos()),
			Vec2.ZERO,
			serverLevel,
			4,
			"Server",
			new TextComponent("Server"),
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

	public RecipeManager getRecipeManager() {
		return this.resources.getRecipeManager();
	}

	public TagManager getTags() {
		return this.resources.getTags();
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

	public LootTables getLootTables() {
		return this.resources.getLootTables();
	}

	public PredicateManager getPredicateManager() {
		return this.resources.getPredicateManager();
	}

	public GameRules getGameRules() {
		return this.getLevel(Level.OVERWORLD).getGameRules();
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
			ServerOpListEntry serverOpListEntry = this.getPlayerList().getOps().get(gameProfile);
			if (serverOpListEntry != null) {
				return serverOpListEntry.getLevel();
			} else if (this.isSingleplayerOwner(gameProfile)) {
				return 4;
			} else if (this.isSingleplayer()) {
				return this.getPlayerList().isAllowCheatsForAllPlayers() ? 4 : 0;
			} else {
				return this.getOperatorUserPermissionLevel();
			}
		} else {
			return 0;
		}
	}

	@Environment(EnvType.CLIENT)
	public FrameTimer getFrameTimer() {
		return this.frameTimer;
	}

	public ProfilerFiller getProfiler() {
		return this.profiler;
	}

	public abstract boolean isSingleplayerOwner(GameProfile gameProfile);

	public void saveDebugReport(Path path) throws IOException {
		Path path2 = path.resolve("levels");

		for (Entry<ResourceKey<Level>, ServerLevel> entry : this.levels.entrySet()) {
			ResourceLocation resourceLocation = ((ResourceKey)entry.getKey()).location();
			Path path3 = path2.resolve(resourceLocation.getNamespace()).resolve(resourceLocation.getPath());
			Files.createDirectories(path3);
			((ServerLevel)entry.getValue()).saveDebugReport(path3);
		}

		this.dumpGameRules(path.resolve("gamerules.txt"));
		this.dumpClasspath(path.resolve("classpath.txt"));
		this.dumpCrashCategory(path.resolve("example_crash.txt"));
		this.dumpMiscStats(path.resolve("stats.txt"));
		this.dumpThreads(path.resolve("threads.txt"));
	}

	private void dumpMiscStats(Path path) throws IOException {
		Writer writer = Files.newBufferedWriter(path);
		Throwable var3 = null;

		try {
			writer.write(String.format("pending_tasks: %d\n", this.getPendingTasksCount()));
			writer.write(String.format("average_tick_time: %f\n", this.getAverageTickTime()));
			writer.write(String.format("tick_times: %s\n", Arrays.toString(this.tickTimes)));
			writer.write(String.format("queue: %s\n", Util.backgroundExecutor()));
		} catch (Throwable var12) {
			var3 = var12;
			throw var12;
		} finally {
			if (writer != null) {
				if (var3 != null) {
					try {
						writer.close();
					} catch (Throwable var11) {
						var3.addSuppressed(var11);
					}
				} else {
					writer.close();
				}
			}
		}
	}

	private void dumpCrashCategory(Path path) throws IOException {
		CrashReport crashReport = new CrashReport("Server dump", new Exception("dummy"));
		this.fillReport(crashReport);
		Writer writer = Files.newBufferedWriter(path);
		Throwable var4 = null;

		try {
			writer.write(crashReport.getFriendlyReport());
		} catch (Throwable var13) {
			var4 = var13;
			throw var13;
		} finally {
			if (writer != null) {
				if (var4 != null) {
					try {
						writer.close();
					} catch (Throwable var12) {
						var4.addSuppressed(var12);
					}
				} else {
					writer.close();
				}
			}
		}
	}

	private void dumpGameRules(Path path) throws IOException {
		Writer writer = Files.newBufferedWriter(path);
		Throwable var3 = null;

		try {
			final List<String> list = Lists.<String>newArrayList();
			final GameRules gameRules = this.getGameRules();
			GameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
				@Override
				public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
					list.add(String.format("%s=%s\n", key.getId(), gameRules.<T>getRule(key).toString()));
				}
			});

			for (String string : list) {
				writer.write(string);
			}
		} catch (Throwable var15) {
			var3 = var15;
			throw var15;
		} finally {
			if (writer != null) {
				if (var3 != null) {
					try {
						writer.close();
					} catch (Throwable var14) {
						var3.addSuppressed(var14);
					}
				} else {
					writer.close();
				}
			}
		}
	}

	private void dumpClasspath(Path path) throws IOException {
		Writer writer = Files.newBufferedWriter(path);
		Throwable var3 = null;

		try {
			String string = System.getProperty("java.class.path");
			String string2 = System.getProperty("path.separator");

			for (String string3 : Splitter.on(string2).split(string)) {
				writer.write(string3);
				writer.write("\n");
			}
		} catch (Throwable var15) {
			var3 = var15;
			throw var15;
		} finally {
			if (writer != null) {
				if (var3 != null) {
					try {
						writer.close();
					} catch (Throwable var14) {
						var3.addSuppressed(var14);
					}
				} else {
					writer.close();
				}
			}
		}
	}

	private void dumpThreads(Path path) throws IOException {
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(true, true);
		Arrays.sort(threadInfos, Comparator.comparing(ThreadInfo::getThreadName));
		Writer writer = Files.newBufferedWriter(path);
		Throwable var5 = null;

		try {
			for (ThreadInfo threadInfo : threadInfos) {
				writer.write(threadInfo.toString());
				writer.write(10);
			}
		} catch (Throwable var17) {
			var5 = var17;
			throw var17;
		} finally {
			if (writer != null) {
				if (var5 != null) {
					try {
						writer.close();
					} catch (Throwable var16) {
						var5.addSuppressed(var16);
					}
				} else {
					writer.close();
				}
			}
		}
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
}
