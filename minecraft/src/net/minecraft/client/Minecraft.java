package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.BanDetails;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.minecraft.UserApiService.UserFlag;
import com.mojang.authlib.minecraft.UserApiService.UserProperties;
import com.mojang.authlib.yggdrasil.ProfileActionType;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.authlib.yggdrasil.ServicesKeyType;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.blaze3d.TracyFrameCapture;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.ClientShutdownWatchdog;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.FramerateLimitTracker;
import com.mojang.blaze3d.platform.GlDebug;
import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.blaze3d.platform.IconSet;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.TimerQuery;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.datafixers.DataFixer;
import com.mojang.jtracy.DiscontinuousFrame;
import com.mojang.jtracy.TracyClient;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.management.ManagementFactory;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.FileUtil;
import net.minecraft.Optionull;
import net.minecraft.ReportType;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiSpriteManager;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.components.debugchart.ProfilerPieChart;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.font.providers.FreeTypeUtil;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.BanNoticeScreens;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.OutOfMemoryScreen;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.main.SilentInitException;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.client.multiplayer.chat.report.ReportEnvironment;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.profiling.ClientMetricsSamplersProvider;
import net.minecraft.client.quickplay.QuickPlay;
import net.minecraft.client.quickplay.QuickPlayLog;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.client.renderer.VirtualScreen;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.client.resources.FoliageColorReloadListener;
import net.minecraft.client.resources.GrassColorReloadListener;
import net.minecraft.client.resources.MapDecorationTextureManager;
import net.minecraft.client.resources.MapTextureManager;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.client.resources.PaintingTextureManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.EquipmentModelSet;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.server.DownloadedPackSource;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.telemetry.ClientTelemetryManager;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraft.client.telemetry.events.GameLoadTimesEvent;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.KeybindResolver;
import net.minecraft.network.protocol.game.ServerboundClientTickEndPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.ProcessorChunkProgressListener;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.CommonLinks;
import net.minecraft.util.FileZipper;
import net.minecraft.util.MemoryReserve;
import net.minecraft.util.ModCheck;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.Unit;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.profiling.ContinuousProfiler;
import net.minecraft.util.profiling.EmptyProfileResults;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.SingleTickProfiler;
import net.minecraft.util.profiling.Zone;
import net.minecraft.util.profiling.metrics.profiling.ActiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.InactiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.MetricsRecorder;
import net.minecraft.util.profiling.metrics.storage.MetricsPersister;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.commons.io.FileUtils;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class Minecraft extends ReentrantBlockableEventLoop<Runnable> implements WindowEventHandler {
	static Minecraft instance;
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final boolean ON_OSX = Util.getPlatform() == Util.OS.OSX;
	private static final int MAX_TICKS_PER_UPDATE = 10;
	public static final ResourceLocation DEFAULT_FONT = ResourceLocation.withDefaultNamespace("default");
	public static final ResourceLocation UNIFORM_FONT = ResourceLocation.withDefaultNamespace("uniform");
	public static final ResourceLocation ALT_FONT = ResourceLocation.withDefaultNamespace("alt");
	private static final ResourceLocation REGIONAL_COMPLIANCIES = ResourceLocation.withDefaultNamespace("regional_compliancies.json");
	private static final CompletableFuture<Unit> RESOURCE_RELOAD_INITIAL_TASK = CompletableFuture.completedFuture(Unit.INSTANCE);
	private static final Component SOCIAL_INTERACTIONS_NOT_AVAILABLE = Component.translatable("multiplayer.socialInteractions.not_available");
	public static final String UPDATE_DRIVERS_ADVICE = "Please make sure you have up-to-date drivers (see aka.ms/mcdriver for instructions).";
	private final long canary = Double.doubleToLongBits(Math.PI);
	private final Path resourcePackDirectory;
	private final CompletableFuture<ProfileResult> profileFuture;
	private final TextureManager textureManager;
	private final ShaderManager shaderManager;
	private final DataFixer fixerUpper;
	private final VirtualScreen virtualScreen;
	private final Window window;
	private final DeltaTracker.Timer deltaTracker = new DeltaTracker.Timer(20.0F, 0L, this::getTickTargetMillis);
	private final RenderBuffers renderBuffers;
	public final LevelRenderer levelRenderer;
	private final EntityRenderDispatcher entityRenderDispatcher;
	private final ItemRenderer itemRenderer;
	private final MapRenderer mapRenderer;
	public final ParticleEngine particleEngine;
	private final User user;
	public final Font font;
	public final Font fontFilterFishy;
	public final GameRenderer gameRenderer;
	public final DebugRenderer debugRenderer;
	private final AtomicReference<StoringChunkProgressListener> progressListener = new AtomicReference();
	public final Gui gui;
	public final Options options;
	private final HotbarManager hotbarManager;
	public final MouseHandler mouseHandler;
	public final KeyboardHandler keyboardHandler;
	private InputType lastInputType = InputType.NONE;
	public final File gameDirectory;
	private final String launchedVersion;
	private final String versionType;
	private final Proxy proxy;
	private final LevelStorageSource levelSource;
	private final boolean demo;
	private final boolean allowsMultiplayer;
	private final boolean allowsChat;
	private final ReloadableResourceManager resourceManager;
	private final VanillaPackResources vanillaPackResources;
	private final DownloadedPackSource downloadedPackSource;
	private final PackRepository resourcePackRepository;
	private final LanguageManager languageManager;
	private final BlockColors blockColors;
	private final ItemColors itemColors;
	private final RenderTarget mainRenderTarget;
	@Nullable
	private final TracyFrameCapture tracyFrameCapture;
	private final SoundManager soundManager;
	private final MusicManager musicManager;
	private final FontManager fontManager;
	private final SplashManager splashManager;
	private final GpuWarnlistManager gpuWarnlistManager;
	private final PeriodicNotificationManager regionalCompliancies = new PeriodicNotificationManager(REGIONAL_COMPLIANCIES, Minecraft::countryEqualsISO3);
	private final YggdrasilAuthenticationService authenticationService;
	private final MinecraftSessionService minecraftSessionService;
	private final UserApiService userApiService;
	private final CompletableFuture<UserProperties> userPropertiesFuture;
	private final SkinManager skinManager;
	private final ModelManager modelManager;
	private final BlockRenderDispatcher blockRenderer;
	private final EquipmentModelSet equipmentModels;
	private final PaintingTextureManager paintingTextures;
	private final MobEffectTextureManager mobEffectTextures;
	private final MapTextureManager mapTextureManager;
	private final MapDecorationTextureManager mapDecorationTextures;
	private final GuiSpriteManager guiSprites;
	private final ToastManager toastManager;
	private final Tutorial tutorial;
	private final PlayerSocialManager playerSocialManager;
	private final EntityModelSet entityModels;
	private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
	private final ClientTelemetryManager telemetryManager;
	private final ProfileKeyPairManager profileKeyPairManager;
	private final RealmsDataFetcher realmsDataFetcher;
	private final QuickPlayLog quickPlayLog;
	@Nullable
	public MultiPlayerGameMode gameMode;
	@Nullable
	public ClientLevel level;
	@Nullable
	public LocalPlayer player;
	@Nullable
	private IntegratedServer singleplayerServer;
	@Nullable
	private Connection pendingConnection;
	private boolean isLocalServer;
	@Nullable
	public Entity cameraEntity;
	@Nullable
	public Entity crosshairPickEntity;
	@Nullable
	public HitResult hitResult;
	private int rightClickDelay;
	protected int missTime;
	private volatile boolean pause;
	private long lastNanoTime = Util.getNanos();
	private long lastTime;
	private int frames;
	public boolean noRender;
	@Nullable
	public Screen screen;
	@Nullable
	private Overlay overlay;
	private boolean clientLevelTeardownInProgress;
	Thread gameThread;
	private volatile boolean running;
	@Nullable
	private Supplier<CrashReport> delayedCrash;
	private static int fps;
	public String fpsString = "";
	private long frameTimeNs;
	private final FramerateLimitTracker framerateLimitTracker;
	public boolean wireframe;
	public boolean sectionPath;
	public boolean sectionVisibility;
	public boolean smartCull = true;
	private boolean windowActive;
	private final Queue<Runnable> progressTasks = Queues.<Runnable>newConcurrentLinkedQueue();
	@Nullable
	private CompletableFuture<Void> pendingReload;
	@Nullable
	private TutorialToast socialInteractionsToast;
	private int fpsPieRenderTicks;
	private final ContinuousProfiler fpsPieProfiler = new ContinuousProfiler(Util.timeSource, () -> this.fpsPieRenderTicks);
	private MetricsRecorder metricsRecorder = InactiveMetricsRecorder.INSTANCE;
	private final ResourceLoadStateTracker reloadStateTracker = new ResourceLoadStateTracker();
	private long savedCpuDuration;
	private double gpuUtilization;
	@Nullable
	private TimerQuery.FrameProfile currentFrameProfile;
	private final GameNarrator narrator;
	private final ChatListener chatListener;
	private ReportingContext reportingContext;
	private final CommandHistory commandHistory;
	private final DirectoryValidator directoryValidator;
	private boolean gameLoadFinished;
	private final long clientStartTimeMs;
	private long clientTickCount;

	public Minecraft(GameConfig gameConfig) {
		super("Client");
		instance = this;
		this.clientStartTimeMs = System.currentTimeMillis();
		this.gameDirectory = gameConfig.location.gameDirectory;
		File file = gameConfig.location.assetDirectory;
		this.resourcePackDirectory = gameConfig.location.resourcePackDirectory.toPath();
		this.launchedVersion = gameConfig.game.launchVersion;
		this.versionType = gameConfig.game.versionType;
		Path path = this.gameDirectory.toPath();
		this.directoryValidator = LevelStorageSource.parseValidator(path.resolve("allowed_symlinks.txt"));
		ClientPackSource clientPackSource = new ClientPackSource(gameConfig.location.getExternalAssetSource(), this.directoryValidator);
		this.downloadedPackSource = new DownloadedPackSource(this, path.resolve("downloads"), gameConfig.user);
		RepositorySource repositorySource = new FolderRepositorySource(
			this.resourcePackDirectory, PackType.CLIENT_RESOURCES, PackSource.DEFAULT, this.directoryValidator
		);
		this.resourcePackRepository = new PackRepository(clientPackSource, this.downloadedPackSource.createRepositorySource(), repositorySource);
		this.vanillaPackResources = clientPackSource.getVanillaPack();
		this.proxy = gameConfig.user.proxy;
		this.authenticationService = new YggdrasilAuthenticationService(this.proxy);
		this.minecraftSessionService = this.authenticationService.createMinecraftSessionService();
		this.user = gameConfig.user.user;
		this.profileFuture = CompletableFuture.supplyAsync(() -> this.minecraftSessionService.fetchProfile(this.user.getProfileId(), true), Util.nonCriticalIoPool());
		this.userApiService = this.createUserApiService(this.authenticationService, gameConfig);
		this.userPropertiesFuture = CompletableFuture.supplyAsync(() -> {
			try {
				return this.userApiService.fetchProperties();
			} catch (AuthenticationException var2x) {
				LOGGER.error("Failed to fetch user properties", (Throwable)var2x);
				return UserApiService.OFFLINE_PROPERTIES;
			}
		}, Util.nonCriticalIoPool());
		LOGGER.info("Setting user: {}", this.user.getName());
		LOGGER.debug("(Session ID is {})", this.user.getSessionId());
		this.demo = gameConfig.game.demo;
		this.allowsMultiplayer = !gameConfig.game.disableMultiplayer;
		this.allowsChat = !gameConfig.game.disableChat;
		this.singleplayerServer = null;
		KeybindResolver.setKeyResolver(KeyMapping::createNameSupplier);
		this.fixerUpper = DataFixers.getDataFixer();
		this.toastManager = new ToastManager(this);
		this.gameThread = Thread.currentThread();
		this.options = new Options(this, this.gameDirectory);
		RenderSystem.setShaderGlintAlpha(this.options.glintStrength().get());
		this.running = true;
		this.tutorial = new Tutorial(this, this.options);
		this.hotbarManager = new HotbarManager(path, this.fixerUpper);
		LOGGER.info("Backend library: {}", RenderSystem.getBackendDescription());
		DisplayData displayData;
		if (this.options.overrideHeight > 0 && this.options.overrideWidth > 0) {
			displayData = new DisplayData(
				this.options.overrideWidth,
				this.options.overrideHeight,
				gameConfig.display.fullscreenWidth,
				gameConfig.display.fullscreenHeight,
				gameConfig.display.isFullscreen
			);
		} else {
			displayData = gameConfig.display;
		}

		Util.timeSource = RenderSystem.initBackendSystem();
		this.virtualScreen = new VirtualScreen(this);
		this.window = this.virtualScreen.newWindow(displayData, this.options.fullscreenVideoModeString, this.createTitle());
		this.setWindowActive(true);
		this.window.setWindowCloseCallback(new Runnable() {
			private boolean threadStarted;

			public void run() {
				if (!this.threadStarted) {
					this.threadStarted = true;
					ClientShutdownWatchdog.startShutdownWatchdog(gameConfig.location.gameDirectory, Minecraft.this.gameThread.threadId());
				}
			}
		});
		GameLoadTimesEvent.INSTANCE.endStep(TelemetryProperty.LOAD_TIME_PRE_WINDOW_MS);

		try {
			this.window.setIcon(this.vanillaPackResources, SharedConstants.getCurrentVersion().isStable() ? IconSet.RELEASE : IconSet.SNAPSHOT);
		} catch (IOException var13) {
			LOGGER.error("Couldn't set icon", (Throwable)var13);
		}

		this.mouseHandler = new MouseHandler(this);
		this.mouseHandler.setup(this.window.getWindow());
		this.keyboardHandler = new KeyboardHandler(this);
		this.keyboardHandler.setup(this.window.getWindow());
		RenderSystem.initRenderer(this.options.glDebugVerbosity, false);
		this.mainRenderTarget = new MainTarget(this.window.getWidth(), this.window.getHeight());
		this.mainRenderTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
		this.mainRenderTarget.clear();
		this.resourceManager = new ReloadableResourceManager(PackType.CLIENT_RESOURCES);
		this.resourcePackRepository.reload();
		this.options.loadSelectedResourcePacks(this.resourcePackRepository);
		this.languageManager = new LanguageManager(this.options.languageCode, clientLanguage -> {
			if (this.player != null) {
				this.player.connection.updateSearchTrees();
			}
		});
		this.resourceManager.registerReloadListener(this.languageManager);
		this.textureManager = new TextureManager(this.resourceManager);
		this.resourceManager.registerReloadListener(this.textureManager);
		this.shaderManager = new ShaderManager(this.textureManager, this::triggerResourcePackRecovery);
		this.resourceManager.registerReloadListener(this.shaderManager);
		this.skinManager = new SkinManager(this.textureManager, file.toPath().resolve("skins"), this.minecraftSessionService, this);
		this.levelSource = new LevelStorageSource(path.resolve("saves"), path.resolve("backups"), this.directoryValidator, this.fixerUpper);
		this.commandHistory = new CommandHistory(path);
		this.soundManager = new SoundManager(this.options);
		this.resourceManager.registerReloadListener(this.soundManager);
		this.splashManager = new SplashManager(this.user);
		this.resourceManager.registerReloadListener(this.splashManager);
		this.musicManager = new MusicManager(this);
		this.fontManager = new FontManager(this.textureManager);
		this.font = this.fontManager.createFont();
		this.fontFilterFishy = this.fontManager.createFontFilterFishy();
		this.resourceManager.registerReloadListener(this.fontManager);
		this.updateFontOptions();
		this.resourceManager.registerReloadListener(new GrassColorReloadListener());
		this.resourceManager.registerReloadListener(new FoliageColorReloadListener());
		this.window.setErrorSection("Startup");
		RenderSystem.setupDefaultState(0, 0, this.window.getWidth(), this.window.getHeight());
		this.window.setErrorSection("Post startup");
		this.blockColors = BlockColors.createDefault();
		this.itemColors = ItemColors.createDefault(this.blockColors);
		this.modelManager = new ModelManager(this.textureManager, this.blockColors, this.options.mipmapLevels().get());
		this.resourceManager.registerReloadListener(this.modelManager);
		this.entityModels = new EntityModelSet();
		this.resourceManager.registerReloadListener(this.entityModels);
		this.equipmentModels = new EquipmentModelSet();
		this.resourceManager.registerReloadListener(this.equipmentModels);
		this.blockEntityRenderDispatcher = new BlockEntityRenderDispatcher(
			this.font, this.entityModels, this::getBlockRenderer, this::getItemRenderer, this::getEntityRenderDispatcher
		);
		this.resourceManager.registerReloadListener(this.blockEntityRenderDispatcher);
		BlockEntityWithoutLevelRenderer blockEntityWithoutLevelRenderer = new BlockEntityWithoutLevelRenderer(this.blockEntityRenderDispatcher, this.entityModels);
		this.resourceManager.registerReloadListener(blockEntityWithoutLevelRenderer);
		this.itemRenderer = new ItemRenderer(this.modelManager, this.itemColors, blockEntityWithoutLevelRenderer);
		this.resourceManager.registerReloadListener(this.itemRenderer);
		this.mapTextureManager = new MapTextureManager(this.textureManager);
		this.mapDecorationTextures = new MapDecorationTextureManager(this.textureManager);
		this.resourceManager.registerReloadListener(this.mapDecorationTextures);
		this.mapRenderer = new MapRenderer(this.mapDecorationTextures, this.mapTextureManager);

		try {
			int i = Runtime.getRuntime().availableProcessors();
			Tesselator.init();
			this.renderBuffers = new RenderBuffers(i);
		} catch (OutOfMemoryError var12) {
			TinyFileDialogs.tinyfd_messageBox(
				"Minecraft",
				"Oh no! The game was unable to allocate memory off-heap while trying to start. You may try to free some memory by closing other applications on your computer, check that your system meets the minimum requirements, and try again. If the problem persists, please visit: "
					+ CommonLinks.GENERAL_HELP,
				"ok",
				"error",
				true
			);
			throw new SilentInitException("Unable to allocate render buffers", var12);
		}

		this.playerSocialManager = new PlayerSocialManager(this, this.userApiService);
		this.blockRenderer = new BlockRenderDispatcher(this.modelManager.getBlockModelShaper(), blockEntityWithoutLevelRenderer, this.blockColors);
		this.resourceManager.registerReloadListener(this.blockRenderer);
		this.entityRenderDispatcher = new EntityRenderDispatcher(
			this, this.textureManager, this.itemRenderer, this.mapRenderer, this.blockRenderer, this.font, this.options, this.entityModels, this.equipmentModels
		);
		this.resourceManager.registerReloadListener(this.entityRenderDispatcher);
		this.particleEngine = new ParticleEngine(this.level, this.textureManager);
		this.resourceManager.registerReloadListener(this.particleEngine);
		this.paintingTextures = new PaintingTextureManager(this.textureManager);
		this.resourceManager.registerReloadListener(this.paintingTextures);
		this.mobEffectTextures = new MobEffectTextureManager(this.textureManager);
		this.resourceManager.registerReloadListener(this.mobEffectTextures);
		this.guiSprites = new GuiSpriteManager(this.textureManager);
		this.resourceManager.registerReloadListener(this.guiSprites);
		this.gameRenderer = new GameRenderer(this, this.entityRenderDispatcher.getItemInHandRenderer(), this.resourceManager, this.renderBuffers);
		this.levelRenderer = new LevelRenderer(this, this.entityRenderDispatcher, this.blockEntityRenderDispatcher, this.renderBuffers);
		this.resourceManager.registerReloadListener(this.levelRenderer);
		this.resourceManager.registerReloadListener(this.levelRenderer.getCloudRenderer());
		this.gpuWarnlistManager = new GpuWarnlistManager();
		this.resourceManager.registerReloadListener(this.gpuWarnlistManager);
		this.resourceManager.registerReloadListener(this.regionalCompliancies);
		this.gui = new Gui(this);
		this.debugRenderer = new DebugRenderer(this);
		RealmsClient realmsClient = RealmsClient.create(this);
		this.realmsDataFetcher = new RealmsDataFetcher(realmsClient);
		RenderSystem.setErrorCallback(this::onFullscreenError);
		if (this.mainRenderTarget.width != this.window.getWidth() || this.mainRenderTarget.height != this.window.getHeight()) {
			StringBuilder stringBuilder = new StringBuilder(
				"Recovering from unsupported resolution ("
					+ this.window.getWidth()
					+ "x"
					+ this.window.getHeight()
					+ ").\nPlease make sure you have up-to-date drivers (see aka.ms/mcdriver for instructions)."
			);
			if (GlDebug.isDebugEnabled()) {
				stringBuilder.append("\n\nReported GL debug messages:\n").append(String.join("\n", GlDebug.getLastOpenGlDebugMessages()));
			}

			this.window.setWindowed(this.mainRenderTarget.width, this.mainRenderTarget.height);
			TinyFileDialogs.tinyfd_messageBox("Minecraft", stringBuilder.toString(), "ok", "error", false);
		} else if (this.options.fullscreen().get() && !this.window.isFullscreen()) {
			this.window.toggleFullScreen();
			this.options.fullscreen().set(this.window.isFullscreen());
		}

		this.window.updateVsync(this.options.enableVsync().get());
		this.window.updateRawMouseInput(this.options.rawMouseInput().get());
		this.window.setDefaultErrorCallback();
		this.resizeDisplay();
		this.gameRenderer.preloadUiShader(this.vanillaPackResources.asProvider());
		this.telemetryManager = new ClientTelemetryManager(this, this.userApiService, this.user);
		this.profileKeyPairManager = ProfileKeyPairManager.create(this.userApiService, this.user, path);
		this.narrator = new GameNarrator(this);
		this.narrator.checkStatus(this.options.narrator().get() != NarratorStatus.OFF);
		this.chatListener = new ChatListener(this);
		this.chatListener.setMessageDelay(this.options.chatDelay().get());
		this.reportingContext = ReportingContext.create(ReportEnvironment.local(), this.userApiService);
		LoadingOverlay.registerTextures(this);
		this.setScreen(new GenericMessageScreen(Component.translatable("gui.loadingMinecraft")));
		List<PackResources> list = this.resourcePackRepository.openAllSelected();
		this.reloadStateTracker.startReload(ResourceLoadStateTracker.ReloadReason.INITIAL, list);
		ReloadInstance reloadInstance = this.resourceManager
			.createReload(Util.backgroundExecutor().forName("resourceLoad"), this, RESOURCE_RELOAD_INITIAL_TASK, list);
		GameLoadTimesEvent.INSTANCE.beginStep(TelemetryProperty.LOAD_TIME_LOADING_OVERLAY_MS);
		Minecraft.GameLoadCookie gameLoadCookie = new Minecraft.GameLoadCookie(realmsClient, gameConfig.quickPlay);
		this.setOverlay(
			new LoadingOverlay(this, reloadInstance, optional -> Util.ifElse(optional, throwable -> this.rollbackResourcePacks(throwable, gameLoadCookie), () -> {
					if (SharedConstants.IS_RUNNING_IN_IDE) {
						this.selfTest();
					}

					this.reloadStateTracker.finishReload();
					this.onResourceLoadFinished(gameLoadCookie);
				}), false)
		);
		this.quickPlayLog = QuickPlayLog.of(gameConfig.quickPlay.path());
		this.framerateLimitTracker = new FramerateLimitTracker(this.options, this);
		if (TracyClient.isAvailable() && gameConfig.game.captureTracyImages) {
			this.tracyFrameCapture = new TracyFrameCapture();
		} else {
			this.tracyFrameCapture = null;
		}
	}

	private void onResourceLoadFinished(@Nullable Minecraft.GameLoadCookie gameLoadCookie) {
		if (!this.gameLoadFinished) {
			this.gameLoadFinished = true;
			this.onGameLoadFinished(gameLoadCookie);
		}
	}

	private void onGameLoadFinished(@Nullable Minecraft.GameLoadCookie gameLoadCookie) {
		Runnable runnable = this.buildInitialScreens(gameLoadCookie);
		GameLoadTimesEvent.INSTANCE.endStep(TelemetryProperty.LOAD_TIME_LOADING_OVERLAY_MS);
		GameLoadTimesEvent.INSTANCE.endStep(TelemetryProperty.LOAD_TIME_TOTAL_TIME_MS);
		GameLoadTimesEvent.INSTANCE.send(this.telemetryManager.getOutsideSessionSender());
		runnable.run();
	}

	public boolean isGameLoadFinished() {
		return this.gameLoadFinished;
	}

	private Runnable buildInitialScreens(@Nullable Minecraft.GameLoadCookie gameLoadCookie) {
		List<Function<Runnable, Screen>> list = new ArrayList();
		this.addInitialScreens(list);
		Runnable runnable = () -> {
			if (gameLoadCookie != null && gameLoadCookie.quickPlayData().isEnabled()) {
				QuickPlay.connect(this, gameLoadCookie.quickPlayData(), gameLoadCookie.realmsClient());
			} else {
				this.setScreen(new TitleScreen(true));
			}
		};

		for (Function<Runnable, Screen> function : Lists.reverse(list)) {
			Screen screen = (Screen)function.apply(runnable);
			runnable = () -> this.setScreen(screen);
		}

		return runnable;
	}

	private void addInitialScreens(List<Function<Runnable, Screen>> list) {
		if (this.options.onboardAccessibility) {
			list.add((Function)runnable -> new AccessibilityOnboardingScreen(this.options, runnable));
		}

		BanDetails banDetails = this.multiplayerBan();
		if (banDetails != null) {
			list.add((Function)runnable -> BanNoticeScreens.create(bl -> {
					if (bl) {
						Util.getPlatform().openUri(CommonLinks.SUSPENSION_HELP);
					}

					runnable.run();
				}, banDetails));
		}

		ProfileResult profileResult = (ProfileResult)this.profileFuture.join();
		if (profileResult != null) {
			GameProfile gameProfile = profileResult.profile();
			Set<ProfileActionType> set = profileResult.actions();
			if (set.contains(ProfileActionType.FORCED_NAME_CHANGE)) {
				list.add((Function)runnable -> BanNoticeScreens.createNameBan(gameProfile.getName(), runnable));
			}

			if (set.contains(ProfileActionType.USING_BANNED_SKIN)) {
				list.add(BanNoticeScreens::createSkinBan);
			}
		}
	}

	private static boolean countryEqualsISO3(Object object) {
		try {
			return Locale.getDefault().getISO3Country().equals(object);
		} catch (MissingResourceException var2) {
			return false;
		}
	}

	public void updateTitle() {
		this.window.setTitle(this.createTitle());
	}

	private String createTitle() {
		StringBuilder stringBuilder = new StringBuilder("Minecraft");
		if (checkModStatus().shouldReportAsModified()) {
			stringBuilder.append("*");
		}

		stringBuilder.append(" ");
		stringBuilder.append(SharedConstants.getCurrentVersion().getName());
		ClientPacketListener clientPacketListener = this.getConnection();
		if (clientPacketListener != null && clientPacketListener.getConnection().isConnected()) {
			stringBuilder.append(" - ");
			ServerData serverData = this.getCurrentServer();
			if (this.singleplayerServer != null && !this.singleplayerServer.isPublished()) {
				stringBuilder.append(I18n.get("title.singleplayer"));
			} else if (serverData != null && serverData.isRealm()) {
				stringBuilder.append(I18n.get("title.multiplayer.realms"));
			} else if (this.singleplayerServer == null && (serverData == null || !serverData.isLan())) {
				stringBuilder.append(I18n.get("title.multiplayer.other"));
			} else {
				stringBuilder.append(I18n.get("title.multiplayer.lan"));
			}
		}

		return stringBuilder.toString();
	}

	private UserApiService createUserApiService(YggdrasilAuthenticationService yggdrasilAuthenticationService, GameConfig gameConfig) {
		return gameConfig.user.user.getType() != User.Type.MSA
			? UserApiService.OFFLINE
			: yggdrasilAuthenticationService.createUserApiService(gameConfig.user.user.getAccessToken());
	}

	public static ModCheck checkModStatus() {
		return ModCheck.identify("vanilla", ClientBrandRetriever::getClientModName, "Client", Minecraft.class);
	}

	private void rollbackResourcePacks(Throwable throwable, @Nullable Minecraft.GameLoadCookie gameLoadCookie) {
		if (this.resourcePackRepository.getSelectedIds().size() > 1) {
			this.clearResourcePacksOnError(throwable, null, gameLoadCookie);
		} else {
			Util.throwAsRuntime(throwable);
		}
	}

	public void clearResourcePacksOnError(Throwable throwable, @Nullable Component component, @Nullable Minecraft.GameLoadCookie gameLoadCookie) {
		LOGGER.info("Caught error loading resourcepacks, removing all selected resourcepacks", throwable);
		this.reloadStateTracker.startRecovery(throwable);
		this.downloadedPackSource.onRecovery();
		this.resourcePackRepository.setSelected(Collections.emptyList());
		this.options.resourcePacks.clear();
		this.options.incompatibleResourcePacks.clear();
		this.options.save();
		this.reloadResourcePacks(true, gameLoadCookie).thenRun(() -> this.addResourcePackLoadFailToast(component));
	}

	private void abortResourcePackRecovery() {
		this.setOverlay(null);
		if (this.level != null) {
			this.level.disconnect();
			this.disconnect();
		}

		this.setScreen(new TitleScreen());
		this.addResourcePackLoadFailToast(null);
	}

	private void addResourcePackLoadFailToast(@Nullable Component component) {
		ToastManager toastManager = this.getToastManager();
		SystemToast.addOrUpdate(toastManager, SystemToast.SystemToastId.PACK_LOAD_FAILURE, Component.translatable("resourcePack.load_fail"), component);
	}

	public void triggerResourcePackRecovery(Exception exception) {
		if (!this.resourcePackRepository.isAbleToClearAnyPack()) {
			if (this.resourcePackRepository.getSelectedIds().size() <= 1) {
				LOGGER.error(LogUtils.FATAL_MARKER, exception.getMessage(), (Throwable)exception);
				this.emergencySaveAndCrash(new CrashReport(exception.getMessage(), exception));
			} else {
				this.schedule(this::abortResourcePackRecovery);
			}
		} else {
			this.clearResourcePacksOnError(exception, Component.translatable("resourcePack.runtime_failure"), null);
		}
	}

	public void run() {
		this.gameThread = Thread.currentThread();
		if (Runtime.getRuntime().availableProcessors() > 4) {
			this.gameThread.setPriority(10);
		}

		DiscontinuousFrame discontinuousFrame = TracyClient.createDiscontinuousFrame("Client Tick");

		try {
			boolean bl = false;

			while (this.running) {
				this.handleDelayedCrash();

				try {
					SingleTickProfiler singleTickProfiler = SingleTickProfiler.createTickProfiler("Renderer");
					boolean bl2 = this.getDebugOverlay().showProfilerChart();

					try (Profiler.Scope scope = Profiler.use(this.constructProfiler(bl2, singleTickProfiler))) {
						this.metricsRecorder.startTick();
						discontinuousFrame.start();
						this.runTick(!bl);
						discontinuousFrame.end();
						this.metricsRecorder.endTick();
					}

					this.finishProfilers(bl2, singleTickProfiler);
				} catch (OutOfMemoryError var10) {
					if (bl) {
						throw var10;
					}

					this.emergencySave();
					this.setScreen(new OutOfMemoryScreen());
					System.gc();
					LOGGER.error(LogUtils.FATAL_MARKER, "Out of memory", (Throwable)var10);
					bl = true;
				}
			}
		} catch (ReportedException var11) {
			LOGGER.error(LogUtils.FATAL_MARKER, "Reported exception thrown!", (Throwable)var11);
			this.emergencySaveAndCrash(var11.getReport());
		} catch (Throwable var12) {
			LOGGER.error(LogUtils.FATAL_MARKER, "Unreported exception thrown!", var12);
			this.emergencySaveAndCrash(new CrashReport("Unexpected error", var12));
		}
	}

	void updateFontOptions() {
		this.fontManager.updateOptions(this.options);
	}

	private void onFullscreenError(int i, long l) {
		this.options.enableVsync().set(false);
		this.options.save();
	}

	public RenderTarget getMainRenderTarget() {
		return this.mainRenderTarget;
	}

	public String getLaunchedVersion() {
		return this.launchedVersion;
	}

	public String getVersionType() {
		return this.versionType;
	}

	public void delayCrash(CrashReport crashReport) {
		this.delayedCrash = () -> this.fillReport(crashReport);
	}

	public void delayCrashRaw(CrashReport crashReport) {
		this.delayedCrash = () -> crashReport;
	}

	private void handleDelayedCrash() {
		if (this.delayedCrash != null) {
			crash(this, this.gameDirectory, (CrashReport)this.delayedCrash.get());
		}
	}

	public void emergencySaveAndCrash(CrashReport crashReport) {
		MemoryReserve.release();
		CrashReport crashReport2 = this.fillReport(crashReport);
		this.emergencySave();
		crash(this, this.gameDirectory, crashReport2);
	}

	public static int saveReport(File file, CrashReport crashReport) {
		Path path = file.toPath().resolve("crash-reports");
		Path path2 = path.resolve("crash-" + Util.getFilenameFormattedDateTime() + "-client.txt");
		Bootstrap.realStdoutPrintln(crashReport.getFriendlyReport(ReportType.CRASH));
		if (crashReport.getSaveFile() != null) {
			Bootstrap.realStdoutPrintln("#@!@# Game crashed! Crash report saved to: #@!@# " + crashReport.getSaveFile().toAbsolutePath());
			return -1;
		} else if (crashReport.saveToFile(path2, ReportType.CRASH)) {
			Bootstrap.realStdoutPrintln("#@!@# Game crashed! Crash report saved to: #@!@# " + path2.toAbsolutePath());
			return -1;
		} else {
			Bootstrap.realStdoutPrintln("#@?@# Game crashed! Crash report could not be saved. #@?@#");
			return -2;
		}
	}

	public static void crash(@Nullable Minecraft minecraft, File file, CrashReport crashReport) {
		int i = saveReport(file, crashReport);
		if (minecraft != null) {
			minecraft.soundManager.emergencyShutdown();
		}

		System.exit(i);
	}

	public boolean isEnforceUnicode() {
		return this.options.forceUnicodeFont().get();
	}

	public CompletableFuture<Void> reloadResourcePacks() {
		return this.reloadResourcePacks(false, null);
	}

	private CompletableFuture<Void> reloadResourcePacks(boolean bl, @Nullable Minecraft.GameLoadCookie gameLoadCookie) {
		if (this.pendingReload != null) {
			return this.pendingReload;
		} else {
			CompletableFuture<Void> completableFuture = new CompletableFuture();
			if (!bl && this.overlay instanceof LoadingOverlay) {
				this.pendingReload = completableFuture;
				return completableFuture;
			} else {
				this.resourcePackRepository.reload();
				List<PackResources> list = this.resourcePackRepository.openAllSelected();
				if (!bl) {
					this.reloadStateTracker.startReload(ResourceLoadStateTracker.ReloadReason.MANUAL, list);
				}

				this.setOverlay(
					new LoadingOverlay(
						this,
						this.resourceManager.createReload(Util.backgroundExecutor().forName("resourceLoad"), this, RESOURCE_RELOAD_INITIAL_TASK, list),
						optional -> Util.ifElse(optional, throwable -> {
								if (bl) {
									this.downloadedPackSource.onRecoveryFailure();
									this.abortResourcePackRecovery();
								} else {
									this.rollbackResourcePacks(throwable, gameLoadCookie);
								}
							}, () -> {
								this.levelRenderer.allChanged();
								this.reloadStateTracker.finishReload();
								this.downloadedPackSource.onReloadSuccess();
								completableFuture.complete(null);
								this.onResourceLoadFinished(gameLoadCookie);
							}),
						!bl
					)
				);
				return completableFuture;
			}
		}
	}

	private void selfTest() {
		boolean bl = false;
		BlockModelShaper blockModelShaper = this.getBlockRenderer().getBlockModelShaper();
		BakedModel bakedModel = blockModelShaper.getModelManager().getMissingModel();

		for (Block block : BuiltInRegistries.BLOCK) {
			for (BlockState blockState : block.getStateDefinition().getPossibleStates()) {
				if (blockState.getRenderShape() == RenderShape.MODEL) {
					BakedModel bakedModel2 = blockModelShaper.getBlockModel(blockState);
					if (bakedModel2 == bakedModel) {
						LOGGER.debug("Missing model for: {}", blockState);
						bl = true;
					}
				}
			}
		}

		TextureAtlasSprite textureAtlasSprite = bakedModel.getParticleIcon();

		for (Block block2 : BuiltInRegistries.BLOCK) {
			for (BlockState blockState2 : block2.getStateDefinition().getPossibleStates()) {
				TextureAtlasSprite textureAtlasSprite2 = blockModelShaper.getParticleIcon(blockState2);
				if (!blockState2.isAir() && textureAtlasSprite2 == textureAtlasSprite) {
					LOGGER.debug("Missing particle icon for: {}", blockState2);
				}
			}
		}

		BuiltInRegistries.ITEM.listElements().forEach(reference -> {
			Item item = (Item)reference.value();
			String string = item.getDescriptionId();
			String string2 = Component.translatable(string).getString();
			if (string2.toLowerCase(Locale.ROOT).equals(item.getDescriptionId())) {
				LOGGER.debug("Missing translation for: {} {} {}", reference.key().location(), string, item);
			}
		});
		bl |= MenuScreens.selfTest();
		bl |= EntityRenderers.validateRegistrations();
		if (bl) {
			throw new IllegalStateException("Your game data is foobar, fix the errors above!");
		}
	}

	public LevelStorageSource getLevelSource() {
		return this.levelSource;
	}

	private void openChatScreen(String string) {
		Minecraft.ChatStatus chatStatus = this.getChatStatus();
		if (!chatStatus.isChatAllowed(this.isLocalServer())) {
			if (this.gui.isShowingChatDisabledByPlayer()) {
				this.gui.setChatDisabledByPlayerShown(false);
				this.setScreen(new ConfirmLinkScreen(bl -> {
					if (bl) {
						Util.getPlatform().openUri(CommonLinks.ACCOUNT_SETTINGS);
					}

					this.setScreen(null);
				}, Minecraft.ChatStatus.INFO_DISABLED_BY_PROFILE, CommonLinks.ACCOUNT_SETTINGS, true));
			} else {
				Component component = chatStatus.getMessage();
				this.gui.setOverlayMessage(component, false);
				this.narrator.sayNow(component);
				this.gui.setChatDisabledByPlayerShown(chatStatus == Minecraft.ChatStatus.DISABLED_BY_PROFILE);
			}
		} else {
			this.setScreen(new ChatScreen(string));
		}
	}

	public void setScreen(@Nullable Screen screen) {
		if (SharedConstants.IS_RUNNING_IN_IDE && Thread.currentThread() != this.gameThread) {
			LOGGER.error("setScreen called from non-game thread");
		}

		if (this.screen != null) {
			this.screen.removed();
		} else {
			this.setLastInputType(InputType.NONE);
		}

		if (screen == null && this.clientLevelTeardownInProgress) {
			throw new IllegalStateException("Trying to return to in-game GUI during disconnection");
		} else {
			if (screen == null && this.level == null) {
				screen = new TitleScreen();
			} else if (screen == null && this.player.isDeadOrDying()) {
				if (this.player.shouldShowDeathScreen()) {
					screen = new DeathScreen(null, this.level.getLevelData().isHardcore());
				} else {
					this.player.respawn();
				}
			}

			this.screen = screen;
			if (this.screen != null) {
				this.screen.added();
			}

			BufferUploader.reset();
			if (screen != null) {
				this.mouseHandler.releaseMouse();
				KeyMapping.releaseAll();
				screen.init(this, this.window.getGuiScaledWidth(), this.window.getGuiScaledHeight());
				this.noRender = false;
			} else {
				this.soundManager.resume();
				this.mouseHandler.grabMouse();
			}

			this.updateTitle();
		}
	}

	public void setOverlay(@Nullable Overlay overlay) {
		this.overlay = overlay;
	}

	public void destroy() {
		try {
			LOGGER.info("Stopping!");

			try {
				this.narrator.destroy();
			} catch (Throwable var7) {
			}

			try {
				if (this.level != null) {
					this.level.disconnect();
				}

				this.disconnect();
			} catch (Throwable var6) {
			}

			if (this.screen != null) {
				this.screen.removed();
			}

			this.close();
		} finally {
			Util.timeSource = System::nanoTime;
			if (this.delayedCrash == null) {
				System.exit(0);
			}
		}
	}

	@Override
	public void close() {
		if (this.currentFrameProfile != null) {
			this.currentFrameProfile.cancel();
		}

		try {
			this.telemetryManager.close();
			this.regionalCompliancies.close();
			this.modelManager.close();
			this.fontManager.close();
			this.gameRenderer.close();
			this.shaderManager.close();
			this.levelRenderer.close();
			this.soundManager.destroy();
			this.particleEngine.close();
			this.mobEffectTextures.close();
			this.paintingTextures.close();
			this.mapDecorationTextures.close();
			this.guiSprites.close();
			this.mapTextureManager.close();
			this.textureManager.close();
			this.resourceManager.close();
			if (this.tracyFrameCapture != null) {
				this.tracyFrameCapture.close();
			}

			FreeTypeUtil.destroy();
			Util.shutdownExecutors();
		} catch (Throwable var5) {
			LOGGER.error("Shutdown failure!", var5);
			throw var5;
		} finally {
			this.virtualScreen.close();
			this.window.close();
		}
	}

	private void runTick(boolean bl) {
		this.window.setErrorSection("Pre render");
		if (this.window.shouldClose()) {
			this.stop();
		}

		if (this.pendingReload != null && !(this.overlay instanceof LoadingOverlay)) {
			CompletableFuture<Void> completableFuture = this.pendingReload;
			this.pendingReload = null;
			this.reloadResourcePacks().thenRun(() -> completableFuture.complete(null));
		}

		Runnable runnable;
		while ((runnable = (Runnable)this.progressTasks.poll()) != null) {
			runnable.run();
		}

		int i = this.deltaTracker.advanceTime(Util.getMillis(), bl);
		ProfilerFiller profilerFiller = Profiler.get();
		if (bl) {
			profilerFiller.push("scheduledExecutables");
			this.runAllTasks();
			profilerFiller.pop();
			profilerFiller.push("tick");

			for (int j = 0; j < Math.min(10, i); j++) {
				profilerFiller.incrementCounter("clientTick");
				this.tick();
			}

			profilerFiller.pop();
		}

		this.window.setErrorSection("Render");
		profilerFiller.push("sound");
		this.soundManager.updateSource(this.gameRenderer.getMainCamera());
		profilerFiller.popPush("toasts");
		this.toastManager.update();
		profilerFiller.popPush("render");
		long l = Util.getNanos();
		boolean bl2;
		if (!this.getDebugOverlay().showDebugScreen() && !this.metricsRecorder.isRecording()) {
			bl2 = false;
			this.gpuUtilization = 0.0;
		} else {
			bl2 = this.currentFrameProfile == null || this.currentFrameProfile.isDone();
			if (bl2) {
				TimerQuery.getInstance().ifPresent(TimerQuery::beginProfile);
			}
		}

		RenderSystem.clear(16640);
		this.mainRenderTarget.bindWrite(true);
		RenderSystem.setShaderFog(FogParameters.NO_FOG);
		profilerFiller.push("display");
		RenderSystem.enableCull();
		profilerFiller.popPush("mouse");
		this.mouseHandler.handleAccumulatedMovement();
		profilerFiller.pop();
		if (!this.noRender) {
			profilerFiller.popPush("gameRenderer");
			this.gameRenderer.render(this.deltaTracker, bl);
			profilerFiller.pop();
		}

		profilerFiller.push("blit");
		this.mainRenderTarget.unbindWrite();
		this.mainRenderTarget.blitToScreen(this.window.getWidth(), this.window.getHeight());
		this.frameTimeNs = Util.getNanos() - l;
		if (bl2) {
			TimerQuery.getInstance().ifPresent(timerQuery -> this.currentFrameProfile = timerQuery.endProfile());
		}

		profilerFiller.popPush("updateDisplay");
		if (this.tracyFrameCapture != null) {
			this.tracyFrameCapture.upload();
			this.tracyFrameCapture.capture(this.mainRenderTarget);
		}

		this.window.updateDisplay(this.tracyFrameCapture);
		int k = this.framerateLimitTracker.getFramerateLimit();
		if (k < 260) {
			RenderSystem.limitDisplayFPS(k);
		}

		profilerFiller.popPush("yield");
		Thread.yield();
		profilerFiller.pop();
		this.window.setErrorSection("Post render");
		this.frames++;
		this.pause = this.hasSingleplayerServer()
			&& (this.screen != null && this.screen.isPauseScreen() || this.overlay != null && this.overlay.isPauseScreen())
			&& !this.singleplayerServer.isPublished();
		this.deltaTracker.updatePauseState(this.pause);
		this.deltaTracker.updateFrozenState(!this.isLevelRunningNormally());
		long m = Util.getNanos();
		long n = m - this.lastNanoTime;
		if (bl2) {
			this.savedCpuDuration = n;
		}

		this.getDebugOverlay().logFrameDuration(n);
		this.lastNanoTime = m;
		profilerFiller.push("fpsUpdate");
		if (this.currentFrameProfile != null && this.currentFrameProfile.isDone()) {
			this.gpuUtilization = (double)this.currentFrameProfile.get() * 100.0 / (double)this.savedCpuDuration;
		}

		while (Util.getMillis() >= this.lastTime + 1000L) {
			String string;
			if (this.gpuUtilization > 0.0) {
				string = " GPU: " + (this.gpuUtilization > 100.0 ? ChatFormatting.RED + "100%" : Math.round(this.gpuUtilization) + "%");
			} else {
				string = "";
			}

			fps = this.frames;
			this.fpsString = String.format(
				Locale.ROOT,
				"%d fps T: %s%s%s%s B: %d%s",
				fps,
				k == 260 ? "inf" : k,
				this.options.enableVsync().get() ? " vsync " : " ",
				this.options.graphicsMode().get(),
				this.options.cloudStatus().get() == CloudStatus.OFF ? "" : (this.options.cloudStatus().get() == CloudStatus.FAST ? " fast-clouds" : " fancy-clouds"),
				this.options.biomeBlendRadius().get(),
				string
			);
			this.lastTime += 1000L;
			this.frames = 0;
		}

		profilerFiller.pop();
	}

	private ProfilerFiller constructProfiler(boolean bl, @Nullable SingleTickProfiler singleTickProfiler) {
		if (!bl) {
			this.fpsPieProfiler.disable();
			if (!this.metricsRecorder.isRecording() && singleTickProfiler == null) {
				return InactiveProfiler.INSTANCE;
			}
		}

		ProfilerFiller profilerFiller;
		if (bl) {
			if (!this.fpsPieProfiler.isEnabled()) {
				this.fpsPieRenderTicks = 0;
				this.fpsPieProfiler.enable();
			}

			this.fpsPieRenderTicks++;
			profilerFiller = this.fpsPieProfiler.getFiller();
		} else {
			profilerFiller = InactiveProfiler.INSTANCE;
		}

		if (this.metricsRecorder.isRecording()) {
			profilerFiller = ProfilerFiller.combine(profilerFiller, this.metricsRecorder.getProfiler());
		}

		return SingleTickProfiler.decorateFiller(profilerFiller, singleTickProfiler);
	}

	private void finishProfilers(boolean bl, @Nullable SingleTickProfiler singleTickProfiler) {
		if (singleTickProfiler != null) {
			singleTickProfiler.endTick();
		}

		ProfilerPieChart profilerPieChart = this.getDebugOverlay().getProfilerPieChart();
		if (bl) {
			profilerPieChart.setPieChartResults(this.fpsPieProfiler.getResults());
		} else {
			profilerPieChart.setPieChartResults(null);
		}
	}

	@Override
	public void resizeDisplay() {
		int i = this.window.calculateScale(this.options.guiScale().get(), this.isEnforceUnicode());
		this.window.setGuiScale((double)i);
		if (this.screen != null) {
			this.screen.resize(this, this.window.getGuiScaledWidth(), this.window.getGuiScaledHeight());
		}

		RenderTarget renderTarget = this.getMainRenderTarget();
		renderTarget.resize(this.window.getWidth(), this.window.getHeight());
		this.gameRenderer.resize(this.window.getWidth(), this.window.getHeight());
		this.mouseHandler.setIgnoreFirstMove();
	}

	@Override
	public void cursorEntered() {
		this.mouseHandler.cursorEntered();
	}

	public int getFps() {
		return fps;
	}

	public long getFrameTimeNs() {
		return this.frameTimeNs;
	}

	private void emergencySave() {
		MemoryReserve.release();

		try {
			if (this.isLocalServer && this.singleplayerServer != null) {
				this.singleplayerServer.halt(true);
			}

			this.disconnect(new GenericMessageScreen(Component.translatable("menu.savingLevel")));
		} catch (Throwable var2) {
		}

		System.gc();
	}

	public boolean debugClientMetricsStart(Consumer<Component> consumer) {
		if (this.metricsRecorder.isRecording()) {
			this.debugClientMetricsStop();
			return false;
		} else {
			Consumer<ProfileResults> consumer2 = profileResults -> {
				if (profileResults != EmptyProfileResults.EMPTY) {
					int i = profileResults.getTickDuration();
					double d = (double)profileResults.getNanoDuration() / (double)TimeUtil.NANOSECONDS_PER_SECOND;
					this.execute(
						() -> consumer.accept(
								Component.translatable("commands.debug.stopped", String.format(Locale.ROOT, "%.2f", d), i, String.format(Locale.ROOT, "%.2f", (double)i / d))
							)
					);
				}
			};
			Consumer<Path> consumer3 = path -> {
				Component component = Component.literal(path.toString())
					.withStyle(ChatFormatting.UNDERLINE)
					.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, path.toFile().getParent())));
				this.execute(() -> consumer.accept(Component.translatable("debug.profiling.stop", component)));
			};
			SystemReport systemReport = fillSystemReport(new SystemReport(), this, this.languageManager, this.launchedVersion, this.options);
			Consumer<List<Path>> consumer4 = list -> {
				Path path = this.archiveProfilingReport(systemReport, list);
				consumer3.accept(path);
			};
			Consumer<Path> consumer5;
			if (this.singleplayerServer == null) {
				consumer5 = path -> consumer4.accept(ImmutableList.of(path));
			} else {
				this.singleplayerServer.fillSystemReport(systemReport);
				CompletableFuture<Path> completableFuture = new CompletableFuture();
				CompletableFuture<Path> completableFuture2 = new CompletableFuture();
				CompletableFuture.allOf(completableFuture, completableFuture2)
					.thenRunAsync(() -> consumer4.accept(ImmutableList.of((Path)completableFuture.join(), (Path)completableFuture2.join())), Util.ioPool());
				this.singleplayerServer.startRecordingMetrics(profileResults -> {
				}, completableFuture2::complete);
				consumer5 = completableFuture::complete;
			}

			this.metricsRecorder = ActiveMetricsRecorder.createStarted(
				new ClientMetricsSamplersProvider(Util.timeSource, this.levelRenderer), Util.timeSource, Util.ioPool(), new MetricsPersister("client"), profileResults -> {
					this.metricsRecorder = InactiveMetricsRecorder.INSTANCE;
					consumer2.accept(profileResults);
				}, consumer5
			);
			return true;
		}
	}

	private void debugClientMetricsStop() {
		this.metricsRecorder.end();
		if (this.singleplayerServer != null) {
			this.singleplayerServer.finishRecordingMetrics();
		}
	}

	private void debugClientMetricsCancel() {
		this.metricsRecorder.cancel();
		if (this.singleplayerServer != null) {
			this.singleplayerServer.cancelRecordingMetrics();
		}
	}

	private Path archiveProfilingReport(SystemReport systemReport, List<Path> list) {
		String string;
		if (this.isLocalServer()) {
			string = this.getSingleplayerServer().getWorldData().getLevelName();
		} else {
			ServerData serverData = this.getCurrentServer();
			string = serverData != null ? serverData.name : "unknown";
		}

		Path path;
		try {
			String string2 = String.format(Locale.ROOT, "%s-%s-%s", Util.getFilenameFormattedDateTime(), string, SharedConstants.getCurrentVersion().getId());
			String string3 = FileUtil.findAvailableName(MetricsPersister.PROFILING_RESULTS_DIR, string2, ".zip");
			path = MetricsPersister.PROFILING_RESULTS_DIR.resolve(string3);
		} catch (IOException var21) {
			throw new UncheckedIOException(var21);
		}

		try {
			FileZipper fileZipper = new FileZipper(path);

			try {
				fileZipper.add(Paths.get("system.txt"), systemReport.toLineSeparatedString());
				fileZipper.add(Paths.get("client").resolve(this.options.getFile().getName()), this.options.dumpOptionsForReport());
				list.forEach(fileZipper::add);
			} catch (Throwable var20) {
				try {
					fileZipper.close();
				} catch (Throwable var19) {
					var20.addSuppressed(var19);
				}

				throw var20;
			}

			fileZipper.close();
		} finally {
			for (Path path3 : list) {
				try {
					FileUtils.forceDelete(path3.toFile());
				} catch (IOException var18) {
					LOGGER.warn("Failed to delete temporary profiling result {}", path3, var18);
				}
			}
		}

		return path;
	}

	public void stop() {
		this.running = false;
	}

	public boolean isRunning() {
		return this.running;
	}

	public void pauseGame(boolean bl) {
		if (this.screen == null) {
			boolean bl2 = this.hasSingleplayerServer() && !this.singleplayerServer.isPublished();
			if (bl2) {
				this.setScreen(new PauseScreen(!bl));
				this.soundManager.pause();
			} else {
				this.setScreen(new PauseScreen(true));
			}
		}
	}

	private void continueAttack(boolean bl) {
		if (!bl) {
			this.missTime = 0;
		}

		if (this.missTime <= 0 && !this.player.isUsingItem()) {
			if (bl && this.hitResult != null && this.hitResult.getType() == HitResult.Type.BLOCK) {
				BlockHitResult blockHitResult = (BlockHitResult)this.hitResult;
				BlockPos blockPos = blockHitResult.getBlockPos();
				if (!this.level.getBlockState(blockPos).isAir()) {
					Direction direction = blockHitResult.getDirection();
					if (this.gameMode.continueDestroyBlock(blockPos, direction)) {
						this.particleEngine.crack(blockPos, direction);
						this.player.swing(InteractionHand.MAIN_HAND);
					}
				}
			} else {
				this.gameMode.stopDestroyBlock();
			}
		}
	}

	private boolean startAttack() {
		if (this.missTime > 0) {
			return false;
		} else if (this.hitResult == null) {
			LOGGER.error("Null returned as 'hitResult', this shouldn't happen!");
			if (this.gameMode.hasMissTime()) {
				this.missTime = 10;
			}

			return false;
		} else if (this.player.isHandsBusy()) {
			return false;
		} else {
			ItemStack itemStack = this.player.getItemInHand(InteractionHand.MAIN_HAND);
			if (!itemStack.isItemEnabled(this.level.enabledFeatures())) {
				return false;
			} else {
				boolean bl = false;
				switch (this.hitResult.getType()) {
					case ENTITY:
						this.gameMode.attack(this.player, ((EntityHitResult)this.hitResult).getEntity());
						break;
					case BLOCK:
						BlockHitResult blockHitResult = (BlockHitResult)this.hitResult;
						BlockPos blockPos = blockHitResult.getBlockPos();
						if (!this.level.getBlockState(blockPos).isAir()) {
							this.gameMode.startDestroyBlock(blockPos, blockHitResult.getDirection());
							if (this.level.getBlockState(blockPos).isAir()) {
								bl = true;
							}
							break;
						}
					case MISS:
						if (this.gameMode.hasMissTime()) {
							this.missTime = 10;
						}

						this.player.resetAttackStrengthTicker();
				}

				this.player.swing(InteractionHand.MAIN_HAND);
				return bl;
			}
		}
	}

	private void startUseItem() {
		if (!this.gameMode.isDestroying()) {
			this.rightClickDelay = 4;
			if (!this.player.isHandsBusy()) {
				if (this.hitResult == null) {
					LOGGER.warn("Null returned as 'hitResult', this shouldn't happen!");
				}

				for (InteractionHand interactionHand : InteractionHand.values()) {
					ItemStack itemStack = this.player.getItemInHand(interactionHand);
					if (!itemStack.isItemEnabled(this.level.enabledFeatures())) {
						return;
					}

					if (this.hitResult != null) {
						switch (this.hitResult.getType()) {
							case ENTITY:
								EntityHitResult entityHitResult = (EntityHitResult)this.hitResult;
								Entity entity = entityHitResult.getEntity();
								if (!this.level.getWorldBorder().isWithinBounds(entity.blockPosition())) {
									return;
								}

								InteractionResult interactionResult = this.gameMode.interactAt(this.player, entity, entityHitResult, interactionHand);
								if (!interactionResult.consumesAction()) {
									interactionResult = this.gameMode.interact(this.player, entity, interactionHand);
								}

								if (interactionResult instanceof InteractionResult.Success success) {
									if (success.swingSource() == InteractionResult.SwingSource.CLIENT) {
										this.player.swing(interactionHand);
									}

									return;
								}
								break;
							case BLOCK:
								BlockHitResult blockHitResult = (BlockHitResult)this.hitResult;
								int i = itemStack.getCount();
								InteractionResult interactionResult2 = this.gameMode.useItemOn(this.player, interactionHand, blockHitResult);
								if (interactionResult2 instanceof InteractionResult.Success success2) {
									if (success2.swingSource() == InteractionResult.SwingSource.CLIENT) {
										this.player.swing(interactionHand);
										if (!itemStack.isEmpty() && (itemStack.getCount() != i || this.gameMode.hasInfiniteItems())) {
											this.gameRenderer.itemInHandRenderer.itemUsed(interactionHand);
										}
									}

									return;
								}

								if (interactionResult2 instanceof InteractionResult.Fail) {
									return;
								}
						}
					}

					if (!itemStack.isEmpty() && this.gameMode.useItem(this.player, interactionHand) instanceof InteractionResult.Success success3) {
						if (success3.swingSource() == InteractionResult.SwingSource.CLIENT) {
							this.player.swing(interactionHand);
						}

						this.gameRenderer.itemInHandRenderer.itemUsed(interactionHand);
						return;
					}
				}
			}
		}
	}

	public MusicManager getMusicManager() {
		return this.musicManager;
	}

	public void tick() {
		this.clientTickCount++;
		if (this.level != null && !this.pause) {
			this.level.tickRateManager().tick();
		}

		if (this.rightClickDelay > 0) {
			this.rightClickDelay--;
		}

		ProfilerFiller profilerFiller = Profiler.get();
		profilerFiller.push("gui");
		this.chatListener.tick();
		this.gui.tick(this.pause);
		profilerFiller.pop();
		this.gameRenderer.pick(1.0F);
		this.tutorial.onLookAt(this.level, this.hitResult);
		profilerFiller.push("gameMode");
		if (!this.pause && this.level != null) {
			this.gameMode.tick();
		}

		profilerFiller.popPush("textures");
		if (this.isLevelRunningNormally()) {
			this.textureManager.tick();
		}

		if (this.screen != null || this.player == null) {
			if (this.screen instanceof InBedChatScreen inBedChatScreen && !this.player.isSleeping()) {
				inBedChatScreen.onPlayerWokeUp();
			}
		} else if (this.player.isDeadOrDying() && !(this.screen instanceof DeathScreen)) {
			this.setScreen(null);
		} else if (this.player.isSleeping() && this.level != null) {
			this.setScreen(new InBedChatScreen());
		}

		if (this.screen != null) {
			this.missTime = 10000;
		}

		if (this.screen != null) {
			try {
				this.screen.tick();
			} catch (Throwable var5) {
				CrashReport crashReport = CrashReport.forThrowable(var5, "Ticking screen");
				this.screen.fillCrashDetails(crashReport);
				throw new ReportedException(crashReport);
			}
		}

		if (!this.getDebugOverlay().showDebugScreen()) {
			this.gui.clearCache();
		}

		if (this.overlay == null && this.screen == null) {
			profilerFiller.popPush("Keybindings");
			this.handleKeybinds();
			if (this.missTime > 0) {
				this.missTime--;
			}
		}

		if (this.level != null) {
			profilerFiller.popPush("gameRenderer");
			if (!this.pause) {
				this.gameRenderer.tick();
			}

			profilerFiller.popPush("levelRenderer");
			if (!this.pause) {
				this.levelRenderer.tick();
			}

			profilerFiller.popPush("level");
			if (!this.pause) {
				this.level.tickEntities();
			}
		} else if (this.gameRenderer.currentPostEffect() != null) {
			this.gameRenderer.clearPostEffect();
		}

		if (!this.pause) {
			this.musicManager.tick();
		}

		this.soundManager.tick(this.pause);
		if (this.level != null) {
			if (!this.pause) {
				if (!this.options.joinedFirstServer && this.isMultiplayerServer()) {
					Component component = Component.translatable("tutorial.socialInteractions.title");
					Component component2 = Component.translatable("tutorial.socialInteractions.description", Tutorial.key("socialInteractions"));
					this.socialInteractionsToast = new TutorialToast(TutorialToast.Icons.SOCIAL_INTERACTIONS, component, component2, true, 8000);
					this.toastManager.addToast(this.socialInteractionsToast);
					this.options.joinedFirstServer = true;
					this.options.save();
				}

				this.tutorial.tick();

				try {
					this.level.tick(() -> true);
				} catch (Throwable var6) {
					CrashReport crashReport = CrashReport.forThrowable(var6, "Exception in world tick");
					if (this.level == null) {
						CrashReportCategory crashReportCategory = crashReport.addCategory("Affected level");
						crashReportCategory.setDetail("Problem", "Level is null!");
					} else {
						this.level.fillReportDetails(crashReport);
					}

					throw new ReportedException(crashReport);
				}
			}

			profilerFiller.popPush("animateTick");
			if (!this.pause && this.isLevelRunningNormally()) {
				this.level.animateTick(this.player.getBlockX(), this.player.getBlockY(), this.player.getBlockZ());
			}

			profilerFiller.popPush("particles");
			if (!this.pause && this.isLevelRunningNormally()) {
				this.particleEngine.tick();
			}

			ClientPacketListener clientPacketListener = this.getConnection();
			if (clientPacketListener != null && !this.pause) {
				clientPacketListener.send(ServerboundClientTickEndPacket.INSTANCE);
			}
		} else if (this.pendingConnection != null) {
			profilerFiller.popPush("pendingConnection");
			this.pendingConnection.tick();
		}

		profilerFiller.popPush("keyboard");
		this.keyboardHandler.tick();
		profilerFiller.pop();
	}

	private boolean isLevelRunningNormally() {
		return this.level == null || this.level.tickRateManager().runsNormally();
	}

	private boolean isMultiplayerServer() {
		return !this.isLocalServer || this.singleplayerServer != null && this.singleplayerServer.isPublished();
	}

	private void handleKeybinds() {
		while (this.options.keyTogglePerspective.consumeClick()) {
			CameraType cameraType = this.options.getCameraType();
			this.options.setCameraType(this.options.getCameraType().cycle());
			if (cameraType.isFirstPerson() != this.options.getCameraType().isFirstPerson()) {
				this.gameRenderer.checkEntityPostEffect(this.options.getCameraType().isFirstPerson() ? this.getCameraEntity() : null);
			}

			this.levelRenderer.needsUpdate();
		}

		while (this.options.keySmoothCamera.consumeClick()) {
			this.options.smoothCamera = !this.options.smoothCamera;
		}

		for (int i = 0; i < 9; i++) {
			boolean bl = this.options.keySaveHotbarActivator.isDown();
			boolean bl2 = this.options.keyLoadHotbarActivator.isDown();
			if (this.options.keyHotbarSlots[i].consumeClick()) {
				if (this.player.isSpectator()) {
					this.gui.getSpectatorGui().onHotbarSelected(i);
				} else if (!this.player.isCreative() || this.screen != null || !bl2 && !bl) {
					this.player.getInventory().selected = i;
				} else {
					CreativeModeInventoryScreen.handleHotbarLoadOrSave(this, i, bl2, bl);
				}
			}
		}

		while (this.options.keySocialInteractions.consumeClick()) {
			if (!this.isMultiplayerServer()) {
				this.player.displayClientMessage(SOCIAL_INTERACTIONS_NOT_AVAILABLE, true);
				this.narrator.sayNow(SOCIAL_INTERACTIONS_NOT_AVAILABLE);
			} else {
				if (this.socialInteractionsToast != null) {
					this.socialInteractionsToast.hide();
					this.socialInteractionsToast = null;
				}

				this.setScreen(new SocialInteractionsScreen());
			}
		}

		while (this.options.keyInventory.consumeClick()) {
			if (this.gameMode.isServerControlledInventory()) {
				this.player.sendOpenInventory();
			} else {
				this.tutorial.onOpenInventory();
				this.setScreen(new InventoryScreen(this.player));
			}
		}

		while (this.options.keyAdvancements.consumeClick()) {
			this.setScreen(new AdvancementsScreen(this.player.connection.getAdvancements()));
		}

		while (this.options.keySwapOffhand.consumeClick()) {
			if (!this.player.isSpectator()) {
				this.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
			}
		}

		while (this.options.keyDrop.consumeClick()) {
			if (!this.player.isSpectator() && this.player.drop(Screen.hasControlDown())) {
				this.player.swing(InteractionHand.MAIN_HAND);
			}
		}

		while (this.options.keyChat.consumeClick()) {
			this.openChatScreen("");
		}

		if (this.screen == null && this.overlay == null && this.options.keyCommand.consumeClick()) {
			this.openChatScreen("/");
		}

		boolean bl3 = false;
		if (this.player.isUsingItem()) {
			if (!this.options.keyUse.isDown()) {
				this.gameMode.releaseUsingItem(this.player);
			}

			while (this.options.keyAttack.consumeClick()) {
			}

			while (this.options.keyUse.consumeClick()) {
			}

			while (this.options.keyPickItem.consumeClick()) {
			}
		} else {
			while (this.options.keyAttack.consumeClick()) {
				bl3 |= this.startAttack();
			}

			while (this.options.keyUse.consumeClick()) {
				this.startUseItem();
			}

			while (this.options.keyPickItem.consumeClick()) {
				this.pickBlock();
			}
		}

		if (this.options.keyUse.isDown() && this.rightClickDelay == 0 && !this.player.isUsingItem()) {
			this.startUseItem();
		}

		this.continueAttack(this.screen == null && !bl3 && this.options.keyAttack.isDown() && this.mouseHandler.isMouseGrabbed());
	}

	public ClientTelemetryManager getTelemetryManager() {
		return this.telemetryManager;
	}

	public double getGpuUtilization() {
		return this.gpuUtilization;
	}

	public ProfileKeyPairManager getProfileKeyPairManager() {
		return this.profileKeyPairManager;
	}

	public WorldOpenFlows createWorldOpenFlows() {
		return new WorldOpenFlows(this, this.levelSource);
	}

	public void doWorldLoad(LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, boolean bl) {
		this.disconnect();
		this.progressListener.set(null);
		Instant instant = Instant.now();

		try {
			levelStorageAccess.saveDataTag(worldStem.registries().compositeAccess(), worldStem.worldData());
			Services services = Services.create(this.authenticationService, this.gameDirectory);
			services.profileCache().setExecutor(this);
			SkullBlockEntity.setup(services, this);
			GameProfileCache.setUsesAuthentication(false);
			this.singleplayerServer = MinecraftServer.spin(thread -> new IntegratedServer(thread, this, levelStorageAccess, packRepository, worldStem, services, i -> {
					StoringChunkProgressListener storingChunkProgressListener = StoringChunkProgressListener.createFromGameruleRadius(i + 0);
					this.progressListener.set(storingChunkProgressListener);
					return ProcessorChunkProgressListener.createStarted(storingChunkProgressListener, this.progressTasks::add);
				}));
			this.isLocalServer = true;
			this.updateReportEnvironment(ReportEnvironment.local());
			this.quickPlayLog.setWorldData(QuickPlayLog.Type.SINGLEPLAYER, levelStorageAccess.getLevelId(), worldStem.worldData().getLevelName());
		} catch (Throwable var12) {
			CrashReport crashReport = CrashReport.forThrowable(var12, "Starting integrated server");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Starting integrated server");
			crashReportCategory.setDetail("Level ID", levelStorageAccess.getLevelId());
			crashReportCategory.setDetail("Level Name", (CrashReportDetail<String>)(() -> worldStem.worldData().getLevelName()));
			throw new ReportedException(crashReport);
		}

		while (this.progressListener.get() == null) {
			Thread.yield();
		}

		LevelLoadingScreen levelLoadingScreen = new LevelLoadingScreen((StoringChunkProgressListener)this.progressListener.get());
		ProfilerFiller profilerFiller = Profiler.get();
		this.setScreen(levelLoadingScreen);
		profilerFiller.push("waitForServer");

		for (; !this.singleplayerServer.isReady() || this.overlay != null; this.handleDelayedCrash()) {
			levelLoadingScreen.tick();
			this.runTick(false);

			try {
				Thread.sleep(16L);
			} catch (InterruptedException var11) {
			}
		}

		profilerFiller.pop();
		Duration duration = Duration.between(instant, Instant.now());
		SocketAddress socketAddress = this.singleplayerServer.getConnection().startMemoryChannel();
		Connection connection = Connection.connectToLocalServer(socketAddress);
		connection.initiateServerboundPlayConnection(
			socketAddress.toString(), 0, new ClientHandshakePacketListenerImpl(connection, this, null, null, bl, duration, component -> {
			}, null)
		);
		connection.send(new ServerboundHelloPacket(this.getUser().getName(), this.getUser().getProfileId()));
		this.pendingConnection = connection;
	}

	public void setLevel(ClientLevel clientLevel, ReceivingLevelScreen.Reason reason) {
		this.updateScreenAndTick(new ReceivingLevelScreen(() -> false, reason));
		this.level = clientLevel;
		this.updateLevelInEngines(clientLevel);
		if (!this.isLocalServer) {
			Services services = Services.create(this.authenticationService, this.gameDirectory);
			services.profileCache().setExecutor(this);
			SkullBlockEntity.setup(services, this);
			GameProfileCache.setUsesAuthentication(false);
		}
	}

	public void disconnect() {
		this.disconnect(new ProgressScreen(true), false);
	}

	public void disconnect(Screen screen) {
		this.disconnect(screen, false);
	}

	public void disconnect(Screen screen, boolean bl) {
		ClientPacketListener clientPacketListener = this.getConnection();
		if (clientPacketListener != null) {
			this.dropAllTasks();
			clientPacketListener.close();
			if (!bl) {
				this.clearDownloadedResourcePacks();
			}
		}

		this.playerSocialManager.stopOnlineMode();
		if (this.metricsRecorder.isRecording()) {
			this.debugClientMetricsCancel();
		}

		IntegratedServer integratedServer = this.singleplayerServer;
		this.singleplayerServer = null;
		this.gameRenderer.resetData();
		this.gameMode = null;
		this.narrator.clear();
		this.clientLevelTeardownInProgress = true;

		try {
			this.updateScreenAndTick(screen);
			if (this.level != null) {
				if (integratedServer != null) {
					ProfilerFiller profilerFiller = Profiler.get();
					profilerFiller.push("waitForServer");

					while (!integratedServer.isShutdown()) {
						this.runTick(false);
					}

					profilerFiller.pop();
				}

				this.gui.onDisconnected();
				this.isLocalServer = false;
			}

			this.level = null;
			this.updateLevelInEngines(null);
			this.player = null;
		} finally {
			this.clientLevelTeardownInProgress = false;
		}

		SkullBlockEntity.clear();
	}

	public void clearDownloadedResourcePacks() {
		this.downloadedPackSource.cleanupAfterDisconnect();
		this.runAllTasks();
	}

	public void clearClientLevel(Screen screen) {
		ClientPacketListener clientPacketListener = this.getConnection();
		if (clientPacketListener != null) {
			clientPacketListener.clearLevel();
		}

		if (this.metricsRecorder.isRecording()) {
			this.debugClientMetricsCancel();
		}

		this.gameRenderer.resetData();
		this.gameMode = null;
		this.narrator.clear();
		this.clientLevelTeardownInProgress = true;

		try {
			this.updateScreenAndTick(screen);
			this.gui.onDisconnected();
			this.level = null;
			this.updateLevelInEngines(null);
			this.player = null;
		} finally {
			this.clientLevelTeardownInProgress = false;
		}

		SkullBlockEntity.clear();
	}

	private void updateScreenAndTick(Screen screen) {
		ProfilerFiller profilerFiller = Profiler.get();
		profilerFiller.push("forcedTick");
		this.soundManager.stop();
		this.cameraEntity = null;
		this.pendingConnection = null;
		this.setScreen(screen);
		this.runTick(false);
		profilerFiller.pop();
	}

	public void forceSetScreen(Screen screen) {
		try (Zone zone = Profiler.get().zone("forcedTick")) {
			this.setScreen(screen);
			this.runTick(false);
		}
	}

	private void updateLevelInEngines(@Nullable ClientLevel clientLevel) {
		this.levelRenderer.setLevel(clientLevel);
		this.particleEngine.setLevel(clientLevel);
		this.blockEntityRenderDispatcher.setLevel(clientLevel);
		this.updateTitle();
	}

	private UserProperties userProperties() {
		return (UserProperties)this.userPropertiesFuture.join();
	}

	public boolean telemetryOptInExtra() {
		return this.extraTelemetryAvailable() && this.options.telemetryOptInExtra().get();
	}

	public boolean extraTelemetryAvailable() {
		return this.allowsTelemetry() && this.userProperties().flag(UserFlag.OPTIONAL_TELEMETRY_AVAILABLE);
	}

	public boolean allowsTelemetry() {
		return SharedConstants.IS_RUNNING_IN_IDE ? false : this.userProperties().flag(UserFlag.TELEMETRY_ENABLED);
	}

	public boolean allowsMultiplayer() {
		return this.allowsMultiplayer && this.userProperties().flag(UserFlag.SERVERS_ALLOWED) && this.multiplayerBan() == null && !this.isNameBanned();
	}

	public boolean allowsRealms() {
		return this.userProperties().flag(UserFlag.REALMS_ALLOWED) && this.multiplayerBan() == null;
	}

	@Nullable
	public BanDetails multiplayerBan() {
		return (BanDetails)this.userProperties().bannedScopes().get("MULTIPLAYER");
	}

	public boolean isNameBanned() {
		ProfileResult profileResult = (ProfileResult)this.profileFuture.getNow(null);
		return profileResult != null && profileResult.actions().contains(ProfileActionType.FORCED_NAME_CHANGE);
	}

	public boolean isBlocked(UUID uUID) {
		return this.getChatStatus().isChatAllowed(false)
			? this.playerSocialManager.shouldHideMessageFrom(uUID)
			: (this.player == null || !uUID.equals(this.player.getUUID())) && !uUID.equals(Util.NIL_UUID);
	}

	public Minecraft.ChatStatus getChatStatus() {
		if (this.options.chatVisibility().get() == ChatVisiblity.HIDDEN) {
			return Minecraft.ChatStatus.DISABLED_BY_OPTIONS;
		} else if (!this.allowsChat) {
			return Minecraft.ChatStatus.DISABLED_BY_LAUNCHER;
		} else {
			return !this.userProperties().flag(UserFlag.CHAT_ALLOWED) ? Minecraft.ChatStatus.DISABLED_BY_PROFILE : Minecraft.ChatStatus.ENABLED;
		}
	}

	public final boolean isDemo() {
		return this.demo;
	}

	@Nullable
	public ClientPacketListener getConnection() {
		return this.player == null ? null : this.player.connection;
	}

	public static boolean renderNames() {
		return !instance.options.hideGui;
	}

	public static boolean useFancyGraphics() {
		return instance.options.graphicsMode().get().getId() >= GraphicsStatus.FANCY.getId();
	}

	public static boolean useShaderTransparency() {
		return !instance.gameRenderer.isPanoramicMode() && instance.options.graphicsMode().get().getId() >= GraphicsStatus.FABULOUS.getId();
	}

	public static boolean useAmbientOcclusion() {
		return instance.options.ambientOcclusion().get();
	}

	private void pickBlock() {
		if (this.hitResult != null && this.hitResult.getType() != HitResult.Type.MISS) {
			boolean bl = this.player.getAbilities().instabuild;
			BlockEntity blockEntity = null;
			HitResult.Type type = this.hitResult.getType();
			ItemStack itemStack;
			if (type == HitResult.Type.BLOCK) {
				BlockPos blockPos = ((BlockHitResult)this.hitResult).getBlockPos();
				BlockState blockState = this.level.getBlockState(blockPos);
				if (blockState.isAir()) {
					return;
				}

				Block block = blockState.getBlock();
				itemStack = block.getCloneItemStack(this.level, blockPos, blockState);
				if (itemStack.isEmpty()) {
					return;
				}

				if (bl && Screen.hasControlDown() && blockState.hasBlockEntity()) {
					blockEntity = this.level.getBlockEntity(blockPos);
				}
			} else {
				if (type != HitResult.Type.ENTITY || !bl) {
					return;
				}

				Entity entity = ((EntityHitResult)this.hitResult).getEntity();
				itemStack = entity.getPickResult();
				if (itemStack == null) {
					return;
				}
			}

			if (itemStack.isEmpty()) {
				String string = "";
				if (type == HitResult.Type.BLOCK) {
					string = BuiltInRegistries.BLOCK.getKey(this.level.getBlockState(((BlockHitResult)this.hitResult).getBlockPos()).getBlock()).toString();
				} else if (type == HitResult.Type.ENTITY) {
					string = BuiltInRegistries.ENTITY_TYPE.getKey(((EntityHitResult)this.hitResult).getEntity().getType()).toString();
				}

				LOGGER.warn("Picking on: [{}] {} gave null item", type, string);
			} else {
				Inventory inventory = this.player.getInventory();
				if (blockEntity != null) {
					this.addCustomNbtData(itemStack, blockEntity, this.level.registryAccess());
				}

				int i = inventory.findSlotMatchingItem(itemStack);
				if (bl) {
					inventory.setPickedItem(itemStack);
					this.gameMode.handleCreativeModeItemAdd(this.player.getItemInHand(InteractionHand.MAIN_HAND), 36 + inventory.selected);
				} else if (i != -1) {
					if (Inventory.isHotbarSlot(i)) {
						inventory.selected = i;
					} else {
						this.gameMode.handlePickItem(i);
					}
				}
			}
		}
	}

	private void addCustomNbtData(ItemStack itemStack, BlockEntity blockEntity, RegistryAccess registryAccess) {
		CompoundTag compoundTag = blockEntity.saveCustomAndMetadata(registryAccess);
		blockEntity.removeComponentsFromTag(compoundTag);
		BlockItem.setBlockEntityData(itemStack, blockEntity.getType(), compoundTag);
		itemStack.applyComponents(blockEntity.collectComponents());
	}

	public CrashReport fillReport(CrashReport crashReport) {
		SystemReport systemReport = crashReport.getSystemReport();

		try {
			fillSystemReport(systemReport, this, this.languageManager, this.launchedVersion, this.options);
			this.fillUptime(crashReport.addCategory("Uptime"));
			if (this.level != null) {
				this.level.fillReportDetails(crashReport);
			}

			if (this.singleplayerServer != null) {
				this.singleplayerServer.fillSystemReport(systemReport);
			}

			this.reloadStateTracker.fillCrashReport(crashReport);
		} catch (Throwable var4) {
			LOGGER.error("Failed to collect details", var4);
		}

		return crashReport;
	}

	public static void fillReport(
		@Nullable Minecraft minecraft, @Nullable LanguageManager languageManager, String string, @Nullable Options options, CrashReport crashReport
	) {
		SystemReport systemReport = crashReport.getSystemReport();
		fillSystemReport(systemReport, minecraft, languageManager, string, options);
	}

	private static String formatSeconds(double d) {
		return String.format(Locale.ROOT, "%.3fs", d);
	}

	private void fillUptime(CrashReportCategory crashReportCategory) {
		crashReportCategory.setDetail(
			"JVM uptime", (CrashReportDetail<String>)(() -> formatSeconds((double)ManagementFactory.getRuntimeMXBean().getUptime() / 1000.0))
		);
		crashReportCategory.setDetail(
			"Wall uptime", (CrashReportDetail<String>)(() -> formatSeconds((double)(System.currentTimeMillis() - this.clientStartTimeMs) / 1000.0))
		);
		crashReportCategory.setDetail("High-res time", (CrashReportDetail<String>)(() -> formatSeconds((double)Util.getMillis() / 1000.0)));
		crashReportCategory.setDetail(
			"Client ticks", (CrashReportDetail<String>)(() -> String.format(Locale.ROOT, "%d ticks / %.3fs", this.clientTickCount, (double)this.clientTickCount / 20.0))
		);
	}

	private static SystemReport fillSystemReport(
		SystemReport systemReport, @Nullable Minecraft minecraft, @Nullable LanguageManager languageManager, String string, @Nullable Options options
	) {
		systemReport.setDetail("Launched Version", (Supplier<String>)(() -> string));
		String string2 = getLauncherBrand();
		if (string2 != null) {
			systemReport.setDetail("Launcher name", string2);
		}

		systemReport.setDetail("Backend library", RenderSystem::getBackendDescription);
		systemReport.setDetail("Backend API", RenderSystem::getApiDescription);
		systemReport.setDetail(
			"Window size", (Supplier<String>)(() -> minecraft != null ? minecraft.window.getWidth() + "x" + minecraft.window.getHeight() : "<not initialized>")
		);
		systemReport.setDetail("GFLW Platform", Window::getPlatform);
		systemReport.setDetail("GL Caps", RenderSystem::getCapsString);
		systemReport.setDetail(
			"GL debug messages", (Supplier<String>)(() -> GlDebug.isDebugEnabled() ? String.join("\n", GlDebug.getLastOpenGlDebugMessages()) : "<disabled>")
		);
		systemReport.setDetail("Is Modded", (Supplier<String>)(() -> checkModStatus().fullDescription()));
		systemReport.setDetail("Universe", (Supplier<String>)(() -> minecraft != null ? Long.toHexString(minecraft.canary) : "404"));
		systemReport.setDetail("Type", "Client (map_client.txt)");
		if (options != null) {
			if (minecraft != null) {
				String string3 = minecraft.getGpuWarnlistManager().getAllWarnings();
				if (string3 != null) {
					systemReport.setDetail("GPU Warnings", string3);
				}
			}

			systemReport.setDetail("Graphics mode", options.graphicsMode().get().toString());
			systemReport.setDetail("Render Distance", options.getEffectiveRenderDistance() + "/" + options.renderDistance().get() + " chunks");
		}

		if (minecraft != null) {
			systemReport.setDetail("Resource Packs", (Supplier<String>)(() -> PackRepository.displayPackList(minecraft.getResourcePackRepository().getSelectedPacks())));
		}

		if (languageManager != null) {
			systemReport.setDetail("Current Language", (Supplier<String>)(() -> languageManager.getSelected()));
		}

		systemReport.setDetail("Locale", String.valueOf(Locale.getDefault()));
		systemReport.setDetail("System encoding", (Supplier<String>)(() -> System.getProperty("sun.jnu.encoding", "<not set>")));
		systemReport.setDetail("File encoding", (Supplier<String>)(() -> System.getProperty("file.encoding", "<not set>")));
		systemReport.setDetail("CPU", GlUtil::getCpuInfo);
		return systemReport;
	}

	public static Minecraft getInstance() {
		return instance;
	}

	public CompletableFuture<Void> delayTextureReload() {
		return this.submit(this::reloadResourcePacks).thenCompose(completableFuture -> completableFuture);
	}

	public void updateReportEnvironment(ReportEnvironment reportEnvironment) {
		if (!this.reportingContext.matches(reportEnvironment)) {
			this.reportingContext = ReportingContext.create(reportEnvironment, this.userApiService);
		}
	}

	@Nullable
	public ServerData getCurrentServer() {
		return Optionull.map(this.getConnection(), ClientPacketListener::getServerData);
	}

	public boolean isLocalServer() {
		return this.isLocalServer;
	}

	public boolean hasSingleplayerServer() {
		return this.isLocalServer && this.singleplayerServer != null;
	}

	@Nullable
	public IntegratedServer getSingleplayerServer() {
		return this.singleplayerServer;
	}

	public boolean isSingleplayer() {
		IntegratedServer integratedServer = this.getSingleplayerServer();
		return integratedServer != null && !integratedServer.isPublished();
	}

	public boolean isLocalPlayer(UUID uUID) {
		return uUID.equals(this.getUser().getProfileId());
	}

	public User getUser() {
		return this.user;
	}

	public GameProfile getGameProfile() {
		ProfileResult profileResult = (ProfileResult)this.profileFuture.join();
		return profileResult != null ? profileResult.profile() : new GameProfile(this.user.getProfileId(), this.user.getName());
	}

	public Proxy getProxy() {
		return this.proxy;
	}

	public TextureManager getTextureManager() {
		return this.textureManager;
	}

	public ShaderManager getShaderManager() {
		return this.shaderManager;
	}

	public ResourceManager getResourceManager() {
		return this.resourceManager;
	}

	public PackRepository getResourcePackRepository() {
		return this.resourcePackRepository;
	}

	public VanillaPackResources getVanillaPackResources() {
		return this.vanillaPackResources;
	}

	public DownloadedPackSource getDownloadedPackSource() {
		return this.downloadedPackSource;
	}

	public Path getResourcePackDirectory() {
		return this.resourcePackDirectory;
	}

	public LanguageManager getLanguageManager() {
		return this.languageManager;
	}

	public Function<ResourceLocation, TextureAtlasSprite> getTextureAtlas(ResourceLocation resourceLocation) {
		return this.modelManager.getAtlas(resourceLocation)::getSprite;
	}

	public boolean isPaused() {
		return this.pause;
	}

	public GpuWarnlistManager getGpuWarnlistManager() {
		return this.gpuWarnlistManager;
	}

	public SoundManager getSoundManager() {
		return this.soundManager;
	}

	public Music getSituationalMusic() {
		Music music = Optionull.map(this.screen, Screen::getBackgroundMusic);
		if (music != null) {
			return music;
		} else if (this.player != null) {
			if (this.player.level().dimension() == Level.END) {
				return this.gui.getBossOverlay().shouldPlayMusic() ? Musics.END_BOSS : Musics.END;
			} else {
				Holder<Biome> holder = this.player.level().getBiome(this.player.blockPosition());
				if (!this.musicManager.isPlayingMusic(Musics.UNDER_WATER) && (!this.player.isUnderWater() || !holder.is(BiomeTags.PLAYS_UNDERWATER_MUSIC))) {
					return this.player.level().dimension() != Level.NETHER && this.player.getAbilities().instabuild && this.player.getAbilities().mayfly
						? Musics.CREATIVE
						: (Music)holder.value().getBackgroundMusic().orElse(Musics.GAME);
				} else {
					return Musics.UNDER_WATER;
				}
			}
		} else {
			return Musics.MENU;
		}
	}

	public MinecraftSessionService getMinecraftSessionService() {
		return this.minecraftSessionService;
	}

	public SkinManager getSkinManager() {
		return this.skinManager;
	}

	@Nullable
	public Entity getCameraEntity() {
		return this.cameraEntity;
	}

	public void setCameraEntity(Entity entity) {
		this.cameraEntity = entity;
		this.gameRenderer.checkEntityPostEffect(entity);
	}

	public boolean shouldEntityAppearGlowing(Entity entity) {
		return entity.isCurrentlyGlowing()
			|| this.player != null && this.player.isSpectator() && this.options.keySpectatorOutlines.isDown() && entity.getType() == EntityType.PLAYER;
	}

	@Override
	protected Thread getRunningThread() {
		return this.gameThread;
	}

	@Override
	public Runnable wrapRunnable(Runnable runnable) {
		return runnable;
	}

	@Override
	protected boolean shouldRun(Runnable runnable) {
		return true;
	}

	public BlockRenderDispatcher getBlockRenderer() {
		return this.blockRenderer;
	}

	public EntityRenderDispatcher getEntityRenderDispatcher() {
		return this.entityRenderDispatcher;
	}

	public BlockEntityRenderDispatcher getBlockEntityRenderDispatcher() {
		return this.blockEntityRenderDispatcher;
	}

	public ItemRenderer getItemRenderer() {
		return this.itemRenderer;
	}

	public MapRenderer getMapRenderer() {
		return this.mapRenderer;
	}

	public DataFixer getFixerUpper() {
		return this.fixerUpper;
	}

	public DeltaTracker getDeltaTracker() {
		return this.deltaTracker;
	}

	public BlockColors getBlockColors() {
		return this.blockColors;
	}

	public boolean showOnlyReducedInfo() {
		return this.player != null && this.player.isReducedDebugInfo() || this.options.reducedDebugInfo().get();
	}

	public ToastManager getToastManager() {
		return this.toastManager;
	}

	public Tutorial getTutorial() {
		return this.tutorial;
	}

	public boolean isWindowActive() {
		return this.windowActive;
	}

	public HotbarManager getHotbarManager() {
		return this.hotbarManager;
	}

	public ModelManager getModelManager() {
		return this.modelManager;
	}

	public PaintingTextureManager getPaintingTextures() {
		return this.paintingTextures;
	}

	public MobEffectTextureManager getMobEffectTextures() {
		return this.mobEffectTextures;
	}

	public MapTextureManager getMapTextureManager() {
		return this.mapTextureManager;
	}

	public MapDecorationTextureManager getMapDecorationTextures() {
		return this.mapDecorationTextures;
	}

	public GuiSpriteManager getGuiSprites() {
		return this.guiSprites;
	}

	@Override
	public void setWindowActive(boolean bl) {
		this.windowActive = bl;
	}

	public Component grabPanoramixScreenshot(File file, int i, int j) {
		int k = this.window.getWidth();
		int l = this.window.getHeight();
		RenderTarget renderTarget = this.getMainRenderTarget();
		float f = this.player.getXRot();
		float g = this.player.getYRot();
		float h = this.player.xRotO;
		float m = this.player.yRotO;
		this.gameRenderer.setRenderBlockOutline(false);

		MutableComponent var12;
		try {
			this.gameRenderer.setPanoramicMode(true);
			this.window.setWidth(i);
			this.window.setHeight(j);
			renderTarget.resize(i, j);

			for (int n = 0; n < 6; n++) {
				switch (n) {
					case 0:
						this.player.setYRot(g);
						this.player.setXRot(0.0F);
						break;
					case 1:
						this.player.setYRot((g + 90.0F) % 360.0F);
						this.player.setXRot(0.0F);
						break;
					case 2:
						this.player.setYRot((g + 180.0F) % 360.0F);
						this.player.setXRot(0.0F);
						break;
					case 3:
						this.player.setYRot((g - 90.0F) % 360.0F);
						this.player.setXRot(0.0F);
						break;
					case 4:
						this.player.setYRot(g);
						this.player.setXRot(-90.0F);
						break;
					case 5:
					default:
						this.player.setYRot(g);
						this.player.setXRot(90.0F);
				}

				this.player.yRotO = this.player.getYRot();
				this.player.xRotO = this.player.getXRot();
				renderTarget.bindWrite(true);
				this.gameRenderer.renderLevel(DeltaTracker.ONE);

				try {
					Thread.sleep(10L);
				} catch (InterruptedException var17) {
				}

				Screenshot.grab(file, "panorama_" + n + ".png", renderTarget, component -> {
				});
			}

			Component component = Component.literal(file.getName())
				.withStyle(ChatFormatting.UNDERLINE)
				.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath())));
			return Component.translatable("screenshot.success", component);
		} catch (Exception var18) {
			LOGGER.error("Couldn't save image", (Throwable)var18);
			var12 = Component.translatable("screenshot.failure", var18.getMessage());
		} finally {
			this.player.setXRot(f);
			this.player.setYRot(g);
			this.player.xRotO = h;
			this.player.yRotO = m;
			this.gameRenderer.setRenderBlockOutline(true);
			this.window.setWidth(k);
			this.window.setHeight(l);
			renderTarget.resize(k, l);
			this.gameRenderer.setPanoramicMode(false);
			this.getMainRenderTarget().bindWrite(true);
		}

		return var12;
	}

	private Component grabHugeScreenshot(File file, int i, int j, int k, int l) {
		try {
			ByteBuffer byteBuffer = GlUtil.allocateMemory(i * j * 3);
			Screenshot screenshot = new Screenshot(file, k, l, j);
			float f = (float)k / (float)i;
			float g = (float)l / (float)j;
			float h = f > g ? f : g;

			for (int m = (l - 1) / j * j; m >= 0; m -= j) {
				for (int n = 0; n < k; n += i) {
					RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
					float o = (float)(k - i) / 2.0F * 2.0F - (float)(n * 2);
					float p = (float)(l - j) / 2.0F * 2.0F - (float)(m * 2);
					o /= (float)i;
					p /= (float)j;
					this.gameRenderer.renderZoomed(h, o, p);
					byteBuffer.clear();
					RenderSystem.pixelStore(3333, 1);
					RenderSystem.pixelStore(3317, 1);
					RenderSystem.readPixels(0, 0, i, j, 32992, 5121, byteBuffer);
					screenshot.addRegion(byteBuffer, n, m, i, j);
				}

				screenshot.saveRow();
			}

			File file2 = screenshot.close();
			GlUtil.freeMemory(byteBuffer);
			Component component = Component.literal(file2.getName())
				.withStyle(ChatFormatting.UNDERLINE)
				.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file2.getAbsolutePath())));
			return Component.translatable("screenshot.success", component);
		} catch (Exception var15) {
			LOGGER.warn("Couldn't save screenshot", (Throwable)var15);
			return Component.translatable("screenshot.failure", var15.getMessage());
		}
	}

	@Nullable
	public StoringChunkProgressListener getProgressListener() {
		return (StoringChunkProgressListener)this.progressListener.get();
	}

	public SplashManager getSplashManager() {
		return this.splashManager;
	}

	@Nullable
	public Overlay getOverlay() {
		return this.overlay;
	}

	public PlayerSocialManager getPlayerSocialManager() {
		return this.playerSocialManager;
	}

	public Window getWindow() {
		return this.window;
	}

	public FramerateLimitTracker getFramerateLimitTracker() {
		return this.framerateLimitTracker;
	}

	public DebugScreenOverlay getDebugOverlay() {
		return this.gui.getDebugOverlay();
	}

	public RenderBuffers renderBuffers() {
		return this.renderBuffers;
	}

	public void updateMaxMipLevel(int i) {
		this.modelManager.updateMaxMipLevel(i);
	}

	public EntityModelSet getEntityModels() {
		return this.entityModels;
	}

	public EquipmentModelSet getEquipmentModels() {
		return this.equipmentModels;
	}

	public boolean isTextFilteringEnabled() {
		return this.userProperties().flag(UserFlag.PROFANITY_FILTER_ENABLED);
	}

	public void prepareForMultiplayer() {
		this.playerSocialManager.startOnlineMode();
		this.getProfileKeyPairManager().prepareKeyPair();
	}

	@Nullable
	public SignatureValidator getProfileKeySignatureValidator() {
		return SignatureValidator.from(this.authenticationService.getServicesKeySet(), ServicesKeyType.PROFILE_KEY);
	}

	public boolean canValidateProfileKeys() {
		return !this.authenticationService.getServicesKeySet().keys(ServicesKeyType.PROFILE_KEY).isEmpty();
	}

	public InputType getLastInputType() {
		return this.lastInputType;
	}

	public void setLastInputType(InputType inputType) {
		this.lastInputType = inputType;
	}

	public GameNarrator getNarrator() {
		return this.narrator;
	}

	public ChatListener getChatListener() {
		return this.chatListener;
	}

	public ReportingContext getReportingContext() {
		return this.reportingContext;
	}

	public RealmsDataFetcher realmsDataFetcher() {
		return this.realmsDataFetcher;
	}

	public QuickPlayLog quickPlayLog() {
		return this.quickPlayLog;
	}

	public CommandHistory commandHistory() {
		return this.commandHistory;
	}

	public DirectoryValidator directoryValidator() {
		return this.directoryValidator;
	}

	private float getTickTargetMillis(float f) {
		if (this.level != null) {
			TickRateManager tickRateManager = this.level.tickRateManager();
			if (tickRateManager.runsNormally()) {
				return Math.max(f, tickRateManager.millisecondsPerTick());
			}
		}

		return f;
	}

	@Nullable
	public static String getLauncherBrand() {
		return System.getProperty("minecraft.launcher.brand");
	}

	@Environment(EnvType.CLIENT)
	public static enum ChatStatus {
		ENABLED(CommonComponents.EMPTY) {
			@Override
			public boolean isChatAllowed(boolean bl) {
				return true;
			}
		},
		DISABLED_BY_OPTIONS(Component.translatable("chat.disabled.options").withStyle(ChatFormatting.RED)) {
			@Override
			public boolean isChatAllowed(boolean bl) {
				return false;
			}
		},
		DISABLED_BY_LAUNCHER(Component.translatable("chat.disabled.launcher").withStyle(ChatFormatting.RED)) {
			@Override
			public boolean isChatAllowed(boolean bl) {
				return bl;
			}
		},
		DISABLED_BY_PROFILE(
			Component.translatable("chat.disabled.profile", Component.keybind(Minecraft.instance.options.keyChat.getName())).withStyle(ChatFormatting.RED)
		) {
			@Override
			public boolean isChatAllowed(boolean bl) {
				return bl;
			}
		};

		static final Component INFO_DISABLED_BY_PROFILE = Component.translatable("chat.disabled.profile.moreInfo");
		private final Component message;

		ChatStatus(final Component component) {
			this.message = component;
		}

		public Component getMessage() {
			return this.message;
		}

		public abstract boolean isChatAllowed(boolean bl);
	}

	@Environment(EnvType.CLIENT)
	static record GameLoadCookie(RealmsClient realmsClient, GameConfig.QuickPlayData quickPlayData) {
	}
}
