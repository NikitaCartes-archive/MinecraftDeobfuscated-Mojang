package net.minecraft.server;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
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
import java.nio.file.Paths;
import java.security.KeyPair;
import java.text.SimpleDateFormat;
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
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportDetail;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.bossevents.CustomBossEvents;
import net.minecraft.server.dedicated.DedicatedServer;
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
import net.minecraft.tags.TagManager;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.Mth;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.Unit;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.profiling.GameProfiler;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelConflictException;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SaveDataDirtyRunnable;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.ScoreboardSaveData;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class MinecraftServer extends ReentrantBlockableEventLoop<TickTask> implements SnooperPopulator, CommandSource, AutoCloseable, Runnable {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final File USERID_CACHE_FILE = new File("usercache.json");
	private static final CompletableFuture<Unit> DATA_RELOAD_INITIAL_TASK = CompletableFuture.completedFuture(Unit.INSTANCE);
	public static final LevelSettings DEMO_SETTINGS = new LevelSettings((long)"North Carolina".hashCode(), GameType.SURVIVAL, true, false, LevelType.NORMAL)
		.enableStartingBonusItems();
	private final LevelStorageSource storageSource;
	private final Snooper snooper = new Snooper("server", this, Util.getMillis());
	private final File universe;
	private final List<Runnable> tickables = Lists.<Runnable>newArrayList();
	private final GameProfiler profiler = new GameProfiler(this::getTickCount);
	private final ServerConnectionListener connection;
	protected final ChunkProgressListenerFactory progressListenerFactory;
	private final ServerStatus status = new ServerStatus();
	private final Random random = new Random();
	private final DataFixer fixerUpper;
	private String localIp;
	private int port = -1;
	private final Map<DimensionType, ServerLevel> levels = Maps.<DimensionType, ServerLevel>newIdentityHashMap();
	private PlayerList playerList;
	private volatile boolean running = true;
	private boolean stopped;
	private int tickCount;
	protected final Proxy proxy;
	private boolean onlineMode;
	private boolean preventProxyConnections;
	private boolean animals;
	private boolean npcs;
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
	private final String levelIdName;
	@Nullable
	@Environment(EnvType.CLIENT)
	private String levelName;
	private boolean isDemo;
	private boolean levelHasStartingBonusChest;
	private String resourcePack = "";
	private String resourcePackHash = "";
	private volatile boolean isReady;
	private long lastOverloadWarning;
	@Nullable
	private Component startupState;
	private boolean delayProfilerStart;
	private boolean forceGameType;
	@Nullable
	private final YggdrasilAuthenticationService authenticationService;
	private final MinecraftSessionService sessionService;
	private final GameProfileRepository profileRepository;
	private final GameProfileCache profileCache;
	private long lastServerStatus;
	protected final Thread serverThread = Util.make(
		new Thread(this, "Server thread"), thread -> thread.setUncaughtExceptionHandler((threadx, throwable) -> LOGGER.error(throwable))
	);
	private long nextTickTime = Util.getMillis();
	private long delayedTasksMaxNextTickTime;
	private boolean mayHaveDelayedTasks;
	@Environment(EnvType.CLIENT)
	private boolean hasWorldScreenshot;
	private final ReloadableResourceManager resources = new SimpleReloadableResourceManager(PackType.SERVER_DATA, this.serverThread);
	private final PackRepository<UnopenedPack> packRepository = new PackRepository<>(UnopenedPack::new);
	@Nullable
	private FolderRepositorySource folderPackSource;
	private final Commands commands;
	private final RecipeManager recipes = new RecipeManager();
	private final TagManager tags = new TagManager();
	private final ServerScoreboard scoreboard = new ServerScoreboard(this);
	private final CustomBossEvents customBossEvents = new CustomBossEvents(this);
	private final LootTables lootTables = new LootTables();
	private final ServerAdvancementManager advancements = new ServerAdvancementManager();
	private final ServerFunctionManager functions = new ServerFunctionManager(this);
	private final FrameTimer frameTimer = new FrameTimer();
	private boolean enforceWhitelist;
	private boolean forceUpgrade;
	private boolean eraseCache;
	private float averageTickTime;
	private final Executor executor;
	@Nullable
	private String serverId;

	public MinecraftServer(
		File file,
		Proxy proxy,
		DataFixer dataFixer,
		Commands commands,
		YggdrasilAuthenticationService yggdrasilAuthenticationService,
		MinecraftSessionService minecraftSessionService,
		GameProfileRepository gameProfileRepository,
		GameProfileCache gameProfileCache,
		ChunkProgressListenerFactory chunkProgressListenerFactory,
		String string
	) {
		super("Server");
		this.proxy = proxy;
		this.commands = commands;
		this.authenticationService = yggdrasilAuthenticationService;
		this.sessionService = minecraftSessionService;
		this.profileRepository = gameProfileRepository;
		this.profileCache = gameProfileCache;
		this.universe = file;
		this.connection = new ServerConnectionListener(this);
		this.progressListenerFactory = chunkProgressListenerFactory;
		this.storageSource = new LevelStorageSource(file.toPath(), file.toPath().resolve("../backups"), dataFixer);
		this.fixerUpper = dataFixer;
		this.resources.registerReloadListener(this.tags);
		this.resources.registerReloadListener(this.recipes);
		this.resources.registerReloadListener(this.lootTables);
		this.resources.registerReloadListener(this.functions);
		this.resources.registerReloadListener(this.advancements);
		this.executor = Util.backgroundExecutor();
		this.levelIdName = string;
	}

	private void readScoreboard(DimensionDataStorage dimensionDataStorage) {
		ScoreboardSaveData scoreboardSaveData = dimensionDataStorage.computeIfAbsent(ScoreboardSaveData::new, "scoreboard");
		scoreboardSaveData.setScoreboard(this.getScoreboard());
		this.getScoreboard().addDirtyListener(new SaveDataDirtyRunnable(scoreboardSaveData));
	}

	protected abstract boolean initServer() throws IOException;

	protected void ensureLevelConversion(String string) {
		if (this.getStorageSource().requiresConversion(string)) {
			LOGGER.info("Converting map!");
			this.setServerStartupState(new TranslatableComponent("menu.convertingLevel"));
			this.getStorageSource().convertLevel(string, new ProgressListener() {
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

		if (this.forceUpgrade) {
			LOGGER.info("Forcing world upgrade!");
			LevelData levelData = this.getStorageSource().getDataTagFor(this.getLevelIdName());
			if (levelData != null) {
				WorldUpgrader worldUpgrader = new WorldUpgrader(this.getLevelIdName(), this.getStorageSource(), levelData, this.eraseCache);
				Component component = null;

				while (!worldUpgrader.isFinished()) {
					Component component2 = worldUpgrader.getStatus();
					if (component != component2) {
						component = component2;
						LOGGER.info(worldUpgrader.getStatus().getString());
					}

					int i = worldUpgrader.getTotalChunks();
					if (i > 0) {
						int j = worldUpgrader.getConverted() + worldUpgrader.getSkipped();
						LOGGER.info("{}% completed ({} / {} chunks)...", Mth.floor((float)j / (float)i * 100.0F), j, i);
					}

					if (this.isStopped()) {
						worldUpgrader.cancel();
					} else {
						try {
							Thread.sleep(1000L);
						} catch (InterruptedException var8) {
						}
					}
				}
			}
		}
	}

	protected synchronized void setServerStartupState(Component component) {
		this.startupState = component;
	}

	protected void loadLevel(String string, String string2, long l, LevelType levelType, JsonElement jsonElement) {
		this.ensureLevelConversion(string);
		this.setServerStartupState(new TranslatableComponent("menu.loadingLevel"));
		LevelStorage levelStorage = this.getStorageSource().selectLevel(string, this);
		this.detectBundledResources(this.getLevelIdName(), levelStorage);
		LevelData levelData = levelStorage.prepareLevel();
		LevelSettings levelSettings;
		if (levelData == null) {
			if (this.isDemo()) {
				levelSettings = DEMO_SETTINGS;
			} else {
				levelSettings = new LevelSettings(l, this.getDefaultGameType(), this.canGenerateStructures(), this.isHardcore(), levelType);
				levelSettings.setLevelTypeOptions(jsonElement);
				if (this.levelHasStartingBonusChest) {
					levelSettings.enableStartingBonusItems();
				}
			}

			levelData = new LevelData(levelSettings, string2);
		} else {
			levelData.setLevelName(string2);
			levelSettings = new LevelSettings(levelData);
		}

		this.loadDataPacks(levelStorage.getFolder(), levelData);
		ChunkProgressListener chunkProgressListener = this.progressListenerFactory.create(11);
		this.createLevels(levelStorage, levelData, levelSettings, chunkProgressListener);
		this.setDifficulty(this.getDefaultDifficulty(), true);
		this.prepareLevels(chunkProgressListener);
	}

	protected void createLevels(LevelStorage levelStorage, LevelData levelData, LevelSettings levelSettings, ChunkProgressListener chunkProgressListener) {
		if (this.isDemo()) {
			levelData.setLevelSettings(DEMO_SETTINGS);
		}

		ServerLevel serverLevel = new ServerLevel(this, this.executor, levelStorage, levelData, DimensionType.OVERWORLD, this.profiler, chunkProgressListener);
		this.levels.put(DimensionType.OVERWORLD, serverLevel);
		this.readScoreboard(serverLevel.getDataStorage());
		serverLevel.getWorldBorder().readBorderData(levelData);
		ServerLevel serverLevel2 = this.getLevel(DimensionType.OVERWORLD);
		if (!levelData.isInitialized()) {
			try {
				serverLevel2.setInitialSpawn(levelSettings);
				if (levelData.getGeneratorType() == LevelType.DEBUG_ALL_BLOCK_STATES) {
					this.setupDebugLevel(levelData);
				}

				levelData.setInitialized(true);
			} catch (Throwable var11) {
				CrashReport crashReport = CrashReport.forThrowable(var11, "Exception initializing level");

				try {
					serverLevel2.fillReportDetails(crashReport);
				} catch (Throwable var10) {
				}

				throw new ReportedException(crashReport);
			}

			levelData.setInitialized(true);
		}

		this.getPlayerList().setLevel(serverLevel2);
		if (levelData.getCustomBossEvents() != null) {
			this.getCustomBossEvents().load(levelData.getCustomBossEvents());
		}

		for (DimensionType dimensionType : DimensionType.getAllTypes()) {
			if (dimensionType != DimensionType.OVERWORLD) {
				this.levels
					.put(dimensionType, new DerivedServerLevel(serverLevel2, this, this.executor, levelStorage, dimensionType, this.profiler, chunkProgressListener));
			}
		}
	}

	private void setupDebugLevel(LevelData levelData) {
		levelData.setGenerateMapFeatures(false);
		levelData.setAllowCommands(true);
		levelData.setRaining(false);
		levelData.setThundering(false);
		levelData.setClearWeatherTime(1000000000);
		levelData.setDayTime(6000L);
		levelData.setGameType(GameType.SPECTATOR);
		levelData.setHardcore(false);
		levelData.setDifficulty(Difficulty.PEACEFUL);
		levelData.setDifficultyLocked(true);
		levelData.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, this);
	}

	protected void loadDataPacks(File file, LevelData levelData) {
		this.packRepository.addSource(new ServerPacksSource());
		this.folderPackSource = new FolderRepositorySource(new File(file, "datapacks"));
		this.packRepository.addSource(this.folderPackSource);
		this.packRepository.reload();
		List<UnopenedPack> list = Lists.<UnopenedPack>newArrayList();

		for (String string : levelData.getEnabledDataPacks()) {
			UnopenedPack unopenedPack = this.packRepository.getPack(string);
			if (unopenedPack != null) {
				list.add(unopenedPack);
			} else {
				LOGGER.warn("Missing data pack {}", string);
			}
		}

		this.packRepository.setSelected(list);
		this.updateSelectedPacks(levelData);
	}

	protected void prepareLevels(ChunkProgressListener chunkProgressListener) {
		this.setServerStartupState(new TranslatableComponent("menu.generatingTerrain"));
		ServerLevel serverLevel = this.getLevel(DimensionType.OVERWORLD);
		LOGGER.info("Preparing start region for dimension " + DimensionType.getName(serverLevel.dimension.getType()));
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
			if (forcedChunksSavedData != null) {
				ServerLevel serverLevel2 = this.getLevel(dimensionType);
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
	}

	protected void detectBundledResources(String string, LevelStorage levelStorage) {
		File file = new File(levelStorage.getFolder(), "resources.zip");
		if (file.isFile()) {
			try {
				this.setResourcePack("level://" + URLEncoder.encode(string, StandardCharsets.UTF_8.toString()) + "/" + "resources.zip", "");
			} catch (UnsupportedEncodingException var5) {
				LOGGER.warn("Something went wrong url encoding {}", string);
			}
		}
	}

	public abstract boolean canGenerateStructures();

	public abstract GameType getDefaultGameType();

	public abstract Difficulty getDefaultDifficulty();

	public abstract boolean isHardcore();

	public abstract int getOperatorUserPermissionLevel();

	public abstract int getFunctionCompilationLevel();

	public abstract boolean shouldRconBroadcast();

	public boolean saveAllChunks(boolean bl, boolean bl2, boolean bl3) {
		boolean bl4 = false;

		for (ServerLevel serverLevel : this.getAllLevels()) {
			if (!bl) {
				LOGGER.info("Saving chunks for level '{}'/{}", serverLevel.getLevelData().getLevelName(), DimensionType.getName(serverLevel.dimension.getType()));
			}

			try {
				serverLevel.save(null, bl2, serverLevel.noSave && !bl3);
			} catch (LevelConflictException var8) {
				LOGGER.warn(var8.getMessage());
			}

			bl4 = true;
		}

		ServerLevel serverLevel2 = this.getLevel(DimensionType.OVERWORLD);
		LevelData levelData = serverLevel2.getLevelData();
		serverLevel2.getWorldBorder().saveWorldBorderData(levelData);
		levelData.setCustomBossEvents(this.getCustomBossEvents().save());
		serverLevel2.getLevelStorage().saveLevelData(levelData, this.getPlayerList().getSingleplayerData());
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
				} catch (IOException var4) {
					LOGGER.error("Exception closing the level", (Throwable)var4);
				}
			}
		}

		if (this.snooper.isStarted()) {
			this.snooper.interrupt();
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
						LOGGER.warn("Can't keep up! Is the server overloaded? Running {}ms or {} ticks behind", l, m);
						this.nextTickTime += m * 50L;
						this.lastOverloadWarning = this.nextTickTime;
					}

					this.nextTickTime += 50L;
					if (this.delayProfilerStart) {
						this.delayProfilerStart = false;
						this.profiler.continuous().enable();
					}

					this.profiler.startTick();
					this.profiler.push("tick");
					this.tickServer(this::haveTime);
					this.profiler.popPush("nextTickWait");
					this.mayHaveDelayedTasks = true;
					this.delayedTasksMaxNextTickTime = Math.max(Util.getMillis() + 50L, this.nextTickTime);
					this.waitUntilNextTick();
					this.profiler.pop();
					this.profiler.endTick();
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

	public void updateStatusIcon(ServerStatus serverStatus) {
		File file = this.getFile("server-icon.png");
		if (!file.exists()) {
			file = this.getStorageSource().getFile(this.getLevelIdName(), "icon.png");
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
		return this.getStorageSource().getFile(this.getLevelIdName(), "icon.png");
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
			if (serverLevel.dimension.getType() == DimensionType.OVERWORLD || this.isNetherEnabled()) {
				this.profiler
					.push((Supplier<String>)(() -> serverLevel.getLevelData().getLevelName() + " " + Registry.DIMENSION_TYPE.getKey(serverLevel.dimension.getType())));
				if (this.tickCount % 20 == 0) {
					this.profiler.push("timeSync");
					this.playerList
						.broadcastAll(
							new ClientboundSetTimePacket(serverLevel.getGameTime(), serverLevel.getDayTime(), serverLevel.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)),
							serverLevel.dimension.getType()
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

	public static void main(String[] strings) {
		OptionParser optionParser = new OptionParser();
		OptionSpec<Void> optionSpec = optionParser.accepts("nogui");
		OptionSpec<Void> optionSpec2 = optionParser.accepts("initSettings", "Initializes 'server.properties' and 'eula.txt', then quits");
		OptionSpec<Void> optionSpec3 = optionParser.accepts("demo");
		OptionSpec<Void> optionSpec4 = optionParser.accepts("bonusChest");
		OptionSpec<Void> optionSpec5 = optionParser.accepts("forceUpgrade");
		OptionSpec<Void> optionSpec6 = optionParser.accepts("eraseCache");
		OptionSpec<Void> optionSpec7 = optionParser.accepts("help").forHelp();
		OptionSpec<String> optionSpec8 = optionParser.accepts("singleplayer").withRequiredArg();
		OptionSpec<String> optionSpec9 = optionParser.accepts("universe").withRequiredArg().defaultsTo(".");
		OptionSpec<String> optionSpec10 = optionParser.accepts("world").withRequiredArg();
		OptionSpec<Integer> optionSpec11 = optionParser.accepts("port").withRequiredArg().<Integer>ofType(Integer.class).defaultsTo(-1);
		OptionSpec<String> optionSpec12 = optionParser.accepts("serverId").withRequiredArg();
		OptionSpec<String> optionSpec13 = optionParser.nonOptions();

		try {
			OptionSet optionSet = optionParser.parse(strings);
			if (optionSet.has(optionSpec7)) {
				optionParser.printHelpOn(System.err);
				return;
			}

			Path path = Paths.get("server.properties");
			DedicatedServerSettings dedicatedServerSettings = new DedicatedServerSettings(path);
			dedicatedServerSettings.forceSave();
			Path path2 = Paths.get("eula.txt");
			Eula eula = new Eula(path2);
			if (optionSet.has(optionSpec2)) {
				LOGGER.info("Initialized '" + path.toAbsolutePath().toString() + "' and '" + path2.toAbsolutePath().toString() + "'");
				return;
			}

			if (!eula.hasAgreedToEULA()) {
				LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
				return;
			}

			Bootstrap.bootStrap();
			Bootstrap.validate();
			String string = optionSet.valueOf(optionSpec9);
			YggdrasilAuthenticationService yggdrasilAuthenticationService = new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString());
			MinecraftSessionService minecraftSessionService = yggdrasilAuthenticationService.createMinecraftSessionService();
			GameProfileRepository gameProfileRepository = yggdrasilAuthenticationService.createProfileRepository();
			GameProfileCache gameProfileCache = new GameProfileCache(gameProfileRepository, new File(string, USERID_CACHE_FILE.getName()));
			String string2 = (String)Optional.ofNullable(optionSet.valueOf(optionSpec10)).orElse(dedicatedServerSettings.getProperties().levelName);
			final DedicatedServer dedicatedServer = new DedicatedServer(
				new File(string),
				dedicatedServerSettings,
				DataFixers.getDataFixer(),
				yggdrasilAuthenticationService,
				minecraftSessionService,
				gameProfileRepository,
				gameProfileCache,
				LoggerChunkProgressListener::new,
				string2
			);
			dedicatedServer.setSingleplayerName(optionSet.valueOf(optionSpec8));
			dedicatedServer.setPort(optionSet.valueOf(optionSpec11));
			dedicatedServer.setDemo(optionSet.has(optionSpec3));
			dedicatedServer.setBonusChest(optionSet.has(optionSpec4));
			dedicatedServer.forceUpgrade(optionSet.has(optionSpec5));
			dedicatedServer.eraseCache(optionSet.has(optionSpec6));
			dedicatedServer.setId(optionSet.valueOf(optionSpec12));
			boolean bl = !optionSet.has(optionSpec) && !optionSet.valuesOf(optionSpec13).contains("nogui");
			if (bl && !GraphicsEnvironment.isHeadless()) {
				dedicatedServer.showGui();
			}

			dedicatedServer.forkAndRun();
			Thread thread = new Thread("Server Shutdown Thread") {
				public void run() {
					dedicatedServer.halt(true);
				}
			};
			thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
			Runtime.getRuntime().addShutdownHook(thread);
		} catch (Exception var29) {
			LOGGER.fatal("Failed to start the minecraft server", (Throwable)var29);
		}
	}

	protected void setId(String string) {
		this.serverId = string;
	}

	protected void forceUpgrade(boolean bl) {
		this.forceUpgrade = bl;
	}

	protected void eraseCache(boolean bl) {
		this.eraseCache = bl;
	}

	public void forkAndRun() {
		this.serverThread.start();
	}

	@Environment(EnvType.CLIENT)
	public boolean isShutdown() {
		return !this.serverThread.isAlive();
	}

	public File getFile(String string) {
		return new File(this.getServerDirectory(), string);
	}

	public void info(String string) {
		LOGGER.info(string);
	}

	public void warn(String string) {
		LOGGER.warn(string);
	}

	public ServerLevel getLevel(DimensionType dimensionType) {
		return (ServerLevel)this.levels.get(dimensionType);
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

	public boolean isDebugging() {
		return false;
	}

	public void error(String string) {
		LOGGER.error(string);
	}

	public void debug(String string) {
		if (this.isDebugging()) {
			LOGGER.info(string);
		}
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

			for (UnopenedPack unopenedPack : this.packRepository.getSelected()) {
				if (stringBuilder.length() > 0) {
					stringBuilder.append(", ");
				}

				stringBuilder.append(unopenedPack.getId());
				if (!unopenedPack.getCompatibility().isCompatible()) {
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

	public boolean isInitialized() {
		return this.universe != null;
	}

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

	public String getLevelIdName() {
		return this.levelIdName;
	}

	@Environment(EnvType.CLIENT)
	public void setLevelName(String string) {
		this.levelName = string;
	}

	@Environment(EnvType.CLIENT)
	public String getLevelName() {
		return this.levelName;
	}

	public void setKeyPair(KeyPair keyPair) {
		this.keyPair = keyPair;
	}

	public void setDifficulty(Difficulty difficulty, boolean bl) {
		for (ServerLevel serverLevel : this.getAllLevels()) {
			LevelData levelData = serverLevel.getLevelData();
			if (bl || !levelData.isDifficultyLocked()) {
				if (levelData.isHardcore()) {
					levelData.setDifficulty(Difficulty.HARD);
					serverLevel.setSpawnSettings(true, true);
				} else if (this.isSingleplayer()) {
					levelData.setDifficulty(difficulty);
					serverLevel.setSpawnSettings(serverLevel.getDifficulty() != Difficulty.PEACEFUL, true);
				} else {
					levelData.setDifficulty(difficulty);
					serverLevel.setSpawnSettings(this.getSpawnMonsters(), this.animals);
				}
			}
		}

		this.getPlayerList().getPlayers().forEach(this::sendDifficultyUpdate);
	}

	public void setDifficultyLocked(boolean bl) {
		for (ServerLevel serverLevel : this.getAllLevels()) {
			LevelData levelData = serverLevel.getLevelData();
			levelData.setDifficultyLocked(bl);
		}

		this.getPlayerList().getPlayers().forEach(this::sendDifficultyUpdate);
	}

	private void sendDifficultyUpdate(ServerPlayer serverPlayer) {
		LevelData levelData = serverPlayer.getLevel().getLevelData();
		serverPlayer.connection.send(new ClientboundChangeDifficultyPacket(levelData.getDifficulty(), levelData.isDifficultyLocked()));
	}

	protected boolean getSpawnMonsters() {
		return true;
	}

	public boolean isDemo() {
		return this.isDemo;
	}

	public void setDemo(boolean bl) {
		this.isDemo = bl;
	}

	public void setBonusChest(boolean bl) {
		this.levelHasStartingBonusChest = bl;
	}

	public LevelStorageSource getStorageSource() {
		return this.storageSource;
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
			snooper.setDynamicData("players_seen", this.getLevel(DimensionType.OVERWORLD).getLevelStorage().getSeenPlayers().length);
		}

		snooper.setDynamicData("uses_auth", this.onlineMode);
		snooper.setDynamicData("gui_state", this.hasGui() ? "enabled" : "disabled");
		snooper.setDynamicData("run_time", (Util.getMillis() - snooper.getStartupTime()) / 60L * 1000L);
		snooper.setDynamicData("avg_tick_ms", (int)(Mth.average(this.tickTimes) * 1.0E-6));
		int i = 0;

		for (ServerLevel serverLevel : this.getAllLevels()) {
			if (serverLevel != null) {
				LevelData levelData = serverLevel.getLevelData();
				snooper.setDynamicData("world[" + i + "][dimension]", serverLevel.dimension.getType());
				snooper.setDynamicData("world[" + i + "][mode]", levelData.getGameType());
				snooper.setDynamicData("world[" + i + "][difficulty]", serverLevel.getDifficulty());
				snooper.setDynamicData("world[" + i + "][hardcore]", levelData.isHardcore());
				snooper.setDynamicData("world[" + i + "][generator_name]", levelData.getGeneratorType().getName());
				snooper.setDynamicData("world[" + i + "][generator_version]", levelData.getGeneratorType().getVersion());
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

	public boolean isAnimals() {
		return this.animals;
	}

	public void setAnimals(boolean bl) {
		this.animals = bl;
	}

	public boolean isNpcsEnabled() {
		return this.npcs;
	}

	public abstract boolean isEpollEnabled();

	public void setNpcsEnabled(boolean bl) {
		this.npcs = bl;
	}

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

	public void setDefaultGameMode(GameType gameType) {
		for (ServerLevel serverLevel : this.getAllLevels()) {
			serverLevel.getLevelData().setGameType(gameType);
		}
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

	public void delayStartProfiler() {
		this.delayProfilerStart = true;
	}

	@Environment(EnvType.CLIENT)
	public Snooper getSnooper() {
		return this.snooper;
	}

	public int getSpawnProtectionRadius() {
		return 16;
	}

	public boolean isUnderSpawnProtection(Level level, BlockPos blockPos, Player player) {
		return false;
	}

	public void setForceGameType(boolean bl) {
		this.forceGameType = bl;
	}

	public boolean getForceGameType() {
		return this.forceGameType;
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
		return this.advancements;
	}

	public ServerFunctionManager getFunctions() {
		return this.functions;
	}

	public void reloadResources() {
		if (!this.isSameThread()) {
			this.execute(this::reloadResources);
		} else {
			this.getPlayerList().saveAll();
			this.packRepository.reload();
			this.updateSelectedPacks(this.getLevel(DimensionType.OVERWORLD).getLevelData());
			this.getPlayerList().reloadResources();
		}
	}

	private void updateSelectedPacks(LevelData levelData) {
		List<UnopenedPack> list = Lists.<UnopenedPack>newArrayList(this.packRepository.getSelected());

		for (UnopenedPack unopenedPack : this.packRepository.getAvailable()) {
			if (!levelData.getDisabledDataPacks().contains(unopenedPack.getId()) && !list.contains(unopenedPack)) {
				LOGGER.info("Found new data pack {}, loading it automatically", unopenedPack.getId());
				unopenedPack.getDefaultPosition().insert(list, unopenedPack, unopenedPackx -> unopenedPackx, false);
			}
		}

		this.packRepository.setSelected(list);
		List<Pack> list2 = Lists.<Pack>newArrayList();
		this.packRepository.getSelected().forEach(unopenedPackx -> list2.add(unopenedPackx.open()));
		CompletableFuture<Unit> completableFuture = this.resources.reload(this.executor, this, list2, DATA_RELOAD_INITIAL_TASK);
		this.managedBlock(completableFuture::isDone);

		try {
			completableFuture.get();
		} catch (Exception var6) {
			LOGGER.error("Failed to reload data packs", (Throwable)var6);
		}

		levelData.getEnabledDataPacks().clear();
		levelData.getDisabledDataPacks().clear();
		this.packRepository.getSelected().forEach(unopenedPackx -> levelData.getEnabledDataPacks().add(unopenedPackx.getId()));
		this.packRepository.getAvailable().forEach(unopenedPackx -> {
			if (!this.packRepository.getSelected().contains(unopenedPackx)) {
				levelData.getDisabledDataPacks().add(unopenedPackx.getId());
			}
		});
	}

	public void kickUnlistedPlayers(CommandSourceStack commandSourceStack) {
		if (this.isEnforceWhitelist()) {
			PlayerList playerList = commandSourceStack.getServer().getPlayerList();
			UserWhiteList userWhiteList = playerList.getWhiteList();
			if (userWhiteList.isEnabled()) {
				for (ServerPlayer serverPlayer : Lists.newArrayList(playerList.getPlayers())) {
					if (!userWhiteList.isWhiteListed(serverPlayer.getGameProfile())) {
						serverPlayer.connection.disconnect(new TranslatableComponent("multiplayer.disconnect.not_whitelisted"));
					}
				}
			}
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
		return new CommandSourceStack(
			this,
			this.getLevel(DimensionType.OVERWORLD) == null ? Vec3.ZERO : new Vec3(this.getLevel(DimensionType.OVERWORLD).getSharedSpawnPos()),
			Vec2.ZERO,
			this.getLevel(DimensionType.OVERWORLD),
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
		return this.recipes;
	}

	public TagManager getTags() {
		return this.tags;
	}

	public ServerScoreboard getScoreboard() {
		return this.scoreboard;
	}

	public LootTables getLootTables() {
		return this.lootTables;
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

	public GameProfiler getProfiler() {
		return this.profiler;
	}

	public Executor getBackgroundTaskExecutor() {
		return this.executor;
	}

	public abstract boolean isSingleplayerOwner(GameProfile gameProfile);

	public void saveDebugReport(Path path) throws IOException {
		Path path2 = path.resolve("levels");

		for (Entry<DimensionType, ServerLevel> entry : this.levels.entrySet()) {
			ResourceLocation resourceLocation = DimensionType.getName((DimensionType)entry.getKey());
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
}
