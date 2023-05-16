package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Queues;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.BanDetails;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.minecraft.UserApiService.UserFlag;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.ServicesKeyType;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.GlDebug;
import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.blaze3d.platform.IconSet;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.TimerQuery;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.gui.RealmsDataFetcher;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.CrashReportDetail;
import net.minecraft.FileUtil;
import net.minecraft.Optionull;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.BanNoticeScreen;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.OutOfMemoryScreen;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.client.main.GameConfig;
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
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
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
import net.minecraft.client.resources.DownloadedPackSource;
import net.minecraft.client.resources.FoliageColorReloadListener;
import net.minecraft.client.resources.GrassColorReloadListener;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.client.resources.PaintingTextureManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.searchtree.FullTextSearchTree;
import net.minecraft.client.searchtree.IdSearchTree;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.client.searchtree.SearchTree;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.KeybindResolver;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
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
import net.minecraft.tags.TagKey;
import net.minecraft.util.FileZipper;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.MemoryReserve;
import net.minecraft.util.ModCheck;
import net.minecraft.util.Mth;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.Unit;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.profiling.ContinuousProfiler;
import net.minecraft.util.profiling.EmptyProfileResults;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.ResultField;
import net.minecraft.util.profiling.SingleTickProfiler;
import net.minecraft.util.profiling.metrics.profiling.ActiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.InactiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.MetricsRecorder;
import net.minecraft.util.profiling.metrics.storage.MetricsPersister;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.commons.io.FileUtils;
import org.joml.Matrix4f;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class Minecraft extends ReentrantBlockableEventLoop<Runnable> implements WindowEventHandler {
	static Minecraft instance;
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final boolean ON_OSX = Util.getPlatform() == Util.OS.OSX;
	private static final int MAX_TICKS_PER_UPDATE = 10;
	public static final ResourceLocation DEFAULT_FONT = new ResourceLocation("default");
	public static final ResourceLocation UNIFORM_FONT = new ResourceLocation("uniform");
	public static final ResourceLocation ALT_FONT = new ResourceLocation("alt");
	private static final ResourceLocation REGIONAL_COMPLIANCIES = new ResourceLocation("regional_compliancies.json");
	private static final CompletableFuture<Unit> RESOURCE_RELOAD_INITIAL_TASK = CompletableFuture.completedFuture(Unit.INSTANCE);
	private static final Component SOCIAL_INTERACTIONS_NOT_AVAILABLE = Component.translatable("multiplayer.socialInteractions.not_available");
	public static final String UPDATE_DRIVERS_ADVICE = "Please make sure you have up-to-date drivers (see aka.ms/mcdriver for instructions).";
	private final Path resourcePackDirectory;
	private final PropertyMap profileProperties;
	private final TextureManager textureManager;
	private final DataFixer fixerUpper;
	private final VirtualScreen virtualScreen;
	private final Window window;
	private final Timer timer = new Timer(20.0F, 0L);
	private final RenderBuffers renderBuffers;
	public final LevelRenderer levelRenderer;
	private final EntityRenderDispatcher entityRenderDispatcher;
	private final ItemRenderer itemRenderer;
	public final ParticleEngine particleEngine;
	private final SearchRegistry searchRegistry = new SearchRegistry();
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
	public final FrameTimer frameTimer = new FrameTimer();
	private final boolean is64bit;
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
	private final SoundManager soundManager;
	private final MusicManager musicManager;
	private final FontManager fontManager;
	private final SplashManager splashManager;
	private final GpuWarnlistManager gpuWarnlistManager;
	private final PeriodicNotificationManager regionalCompliancies = new PeriodicNotificationManager(REGIONAL_COMPLIANCIES, Minecraft::countryEqualsISO3);
	private final YggdrasilAuthenticationService authenticationService;
	private final MinecraftSessionService minecraftSessionService;
	private final UserApiService userApiService;
	private final SkinManager skinManager;
	private final ModelManager modelManager;
	private final BlockRenderDispatcher blockRenderer;
	private final PaintingTextureManager paintingTextures;
	private final MobEffectTextureManager mobEffectTextures;
	private final ToastComponent toast;
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
	private float pausePartialTick;
	private long lastNanoTime = Util.getNanos();
	private long lastTime;
	private int frames;
	public boolean noRender;
	@Nullable
	public Screen screen;
	@Nullable
	private Overlay overlay;
	private boolean connectedToRealms;
	private Thread gameThread;
	private volatile boolean running;
	@Nullable
	private Supplier<CrashReport> delayedCrash;
	private static int fps;
	public String fpsString = "";
	private long frameTimeNs;
	public boolean wireframe;
	public boolean chunkPath;
	public boolean chunkVisibility;
	public boolean smartCull = true;
	private boolean windowActive;
	private final Queue<Runnable> progressTasks = Queues.<Runnable>newConcurrentLinkedQueue();
	@Nullable
	private CompletableFuture<Void> pendingReload;
	@Nullable
	private TutorialToast socialInteractionsToast;
	private ProfilerFiller profiler = InactiveProfiler.INSTANCE;
	private int fpsPieRenderTicks;
	private final ContinuousProfiler fpsPieProfiler = new ContinuousProfiler(Util.timeSource, () -> this.fpsPieRenderTicks);
	@Nullable
	private ProfileResults fpsPieResults;
	private MetricsRecorder metricsRecorder = InactiveMetricsRecorder.INSTANCE;
	private final ResourceLoadStateTracker reloadStateTracker = new ResourceLoadStateTracker();
	private long savedCpuDuration;
	private double gpuUtilization;
	@Nullable
	private TimerQuery.FrameProfile currentFrameProfile;
	private final Realms32BitWarningStatus realms32BitWarningStatus;
	private final GameNarrator narrator;
	private final ChatListener chatListener;
	private ReportingContext reportingContext;
	private String debugPath = "root";

	public Minecraft(GameConfig gameConfig) {
		super("Client");
		instance = this;
		this.gameDirectory = gameConfig.location.gameDirectory;
		File file = gameConfig.location.assetDirectory;
		this.resourcePackDirectory = gameConfig.location.resourcePackDirectory.toPath();
		this.launchedVersion = gameConfig.game.launchVersion;
		this.versionType = gameConfig.game.versionType;
		this.profileProperties = gameConfig.user.profileProperties;
		ClientPackSource clientPackSource = new ClientPackSource(gameConfig.location.getExternalAssetSource());
		this.downloadedPackSource = new DownloadedPackSource(new File(this.gameDirectory, "server-resource-packs"));
		RepositorySource repositorySource = new FolderRepositorySource(this.resourcePackDirectory, PackType.CLIENT_RESOURCES, PackSource.DEFAULT);
		this.resourcePackRepository = new PackRepository(clientPackSource, this.downloadedPackSource, repositorySource);
		this.vanillaPackResources = clientPackSource.getVanillaPack();
		this.proxy = gameConfig.user.proxy;
		this.authenticationService = new YggdrasilAuthenticationService(this.proxy);
		this.minecraftSessionService = this.authenticationService.createMinecraftSessionService();
		this.userApiService = this.createUserApiService(this.authenticationService, gameConfig);
		this.user = gameConfig.user.user;
		LOGGER.info("Setting user: {}", this.user.getName());
		LOGGER.debug("(Session ID is {})", this.user.getSessionId());
		this.demo = gameConfig.game.demo;
		this.allowsMultiplayer = !gameConfig.game.disableMultiplayer;
		this.allowsChat = !gameConfig.game.disableChat;
		this.is64bit = checkIs64Bit();
		this.singleplayerServer = null;
		KeybindResolver.setKeyResolver(KeyMapping::createNameSupplier);
		this.fixerUpper = DataFixers.getDataFixer();
		this.toast = new ToastComponent(this);
		this.gameThread = Thread.currentThread();
		this.options = new Options(this, this.gameDirectory);
		RenderSystem.setShaderGlintAlpha(this.options.glintStrength().get());
		this.running = true;
		this.tutorial = new Tutorial(this, this.options);
		this.hotbarManager = new HotbarManager(this.gameDirectory, this.fixerUpper);
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
		GameLoadTimesEvent.INSTANCE.endStep(TelemetryProperty.LOAD_TIME_PRE_WINDOW_MS);

		try {
			this.window.setIcon(this.vanillaPackResources, SharedConstants.getCurrentVersion().isStable() ? IconSet.RELEASE : IconSet.SNAPSHOT);
		} catch (IOException var10) {
			LOGGER.error("Couldn't set icon", (Throwable)var10);
		}

		this.window.setFramerateLimit(this.options.framerateLimit().get());
		this.mouseHandler = new MouseHandler(this);
		this.mouseHandler.setup(this.window.getWindow());
		this.keyboardHandler = new KeyboardHandler(this);
		this.keyboardHandler.setup(this.window.getWindow());
		RenderSystem.initRenderer(this.options.glDebugVerbosity, false);
		this.mainRenderTarget = new MainTarget(this.window.getWidth(), this.window.getHeight());
		this.mainRenderTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
		this.mainRenderTarget.clear(ON_OSX);
		this.resourceManager = new ReloadableResourceManager(PackType.CLIENT_RESOURCES);
		this.resourcePackRepository.reload();
		this.options.loadSelectedResourcePacks(this.resourcePackRepository);
		this.languageManager = new LanguageManager(this.options.languageCode);
		this.resourceManager.registerReloadListener(this.languageManager);
		this.textureManager = new TextureManager(this.resourceManager);
		this.resourceManager.registerReloadListener(this.textureManager);
		this.skinManager = new SkinManager(this.textureManager, new File(file, "skins"), this.minecraftSessionService);
		this.levelSource = new LevelStorageSource(this.gameDirectory.toPath().resolve("saves"), this.gameDirectory.toPath().resolve("backups"), this.fixerUpper);
		this.soundManager = new SoundManager(this.options);
		this.resourceManager.registerReloadListener(this.soundManager);
		this.splashManager = new SplashManager(this.user);
		this.resourceManager.registerReloadListener(this.splashManager);
		this.musicManager = new MusicManager(this);
		this.fontManager = new FontManager(this.textureManager);
		this.font = this.fontManager.createFont();
		this.fontFilterFishy = this.fontManager.createFontFilterFishy();
		this.resourceManager.registerReloadListener(this.fontManager);
		this.selectMainFont(this.isEnforceUnicode());
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
		this.blockEntityRenderDispatcher = new BlockEntityRenderDispatcher(
			this.font, this.entityModels, this::getBlockRenderer, this::getItemRenderer, this::getEntityRenderDispatcher
		);
		this.resourceManager.registerReloadListener(this.blockEntityRenderDispatcher);
		BlockEntityWithoutLevelRenderer blockEntityWithoutLevelRenderer = new BlockEntityWithoutLevelRenderer(this.blockEntityRenderDispatcher, this.entityModels);
		this.resourceManager.registerReloadListener(blockEntityWithoutLevelRenderer);
		this.itemRenderer = new ItemRenderer(this, this.textureManager, this.modelManager, this.itemColors, blockEntityWithoutLevelRenderer);
		this.resourceManager.registerReloadListener(this.itemRenderer);
		this.renderBuffers = new RenderBuffers();
		this.playerSocialManager = new PlayerSocialManager(this, this.userApiService);
		this.blockRenderer = new BlockRenderDispatcher(this.modelManager.getBlockModelShaper(), blockEntityWithoutLevelRenderer, this.blockColors);
		this.resourceManager.registerReloadListener(this.blockRenderer);
		this.entityRenderDispatcher = new EntityRenderDispatcher(
			this, this.textureManager, this.itemRenderer, this.blockRenderer, this.font, this.options, this.entityModels
		);
		this.resourceManager.registerReloadListener(this.entityRenderDispatcher);
		this.gameRenderer = new GameRenderer(this, this.entityRenderDispatcher.getItemInHandRenderer(), this.resourceManager, this.renderBuffers);
		this.resourceManager.registerReloadListener(this.gameRenderer.createReloadListener());
		this.levelRenderer = new LevelRenderer(this, this.entityRenderDispatcher, this.blockEntityRenderDispatcher, this.renderBuffers);
		this.resourceManager.registerReloadListener(this.levelRenderer);
		this.createSearchTrees();
		this.resourceManager.registerReloadListener(this.searchRegistry);
		this.particleEngine = new ParticleEngine(this.level, this.textureManager);
		this.resourceManager.registerReloadListener(this.particleEngine);
		this.paintingTextures = new PaintingTextureManager(this.textureManager);
		this.resourceManager.registerReloadListener(this.paintingTextures);
		this.mobEffectTextures = new MobEffectTextureManager(this.textureManager);
		this.resourceManager.registerReloadListener(this.mobEffectTextures);
		this.gpuWarnlistManager = new GpuWarnlistManager();
		this.resourceManager.registerReloadListener(this.gpuWarnlistManager);
		this.resourceManager.registerReloadListener(this.regionalCompliancies);
		this.gui = new Gui(this, this.itemRenderer);
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
		this.profileKeyPairManager = ProfileKeyPairManager.create(this.userApiService, this.user, this.gameDirectory.toPath());
		this.realms32BitWarningStatus = new Realms32BitWarningStatus(this);
		this.narrator = new GameNarrator(this);
		this.narrator.checkStatus(this.options.narrator().get() != NarratorStatus.OFF);
		this.chatListener = new ChatListener(this);
		this.chatListener.setMessageDelay(this.options.chatDelay().get());
		this.reportingContext = ReportingContext.create(ReportEnvironment.local(), this.userApiService);
		LoadingOverlay.registerTextures(this);
		List<PackResources> list = this.resourcePackRepository.openAllSelected();
		this.reloadStateTracker.startReload(ResourceLoadStateTracker.ReloadReason.INITIAL, list);
		ReloadInstance reloadInstance = this.resourceManager.createReload(Util.backgroundExecutor(), this, RESOURCE_RELOAD_INITIAL_TASK, list);
		GameLoadTimesEvent.INSTANCE.beginStep(TelemetryProperty.LOAD_TIME_LOADING_OVERLAY_MS);
		this.setOverlay(new LoadingOverlay(this, reloadInstance, optional -> Util.ifElse(optional, this::rollbackResourcePacks, () -> {
				if (SharedConstants.IS_RUNNING_IN_IDE) {
					this.selfTest();
				}

				this.reloadStateTracker.finishReload();
				this.onGameLoadFinished();
			}), false));
		this.quickPlayLog = QuickPlayLog.of(gameConfig.quickPlay.path());
		if (this.shouldShowBanNotice()) {
			this.setScreen(BanNoticeScreen.create(bl -> {
				if (bl) {
					Util.getPlatform().openUri("https://aka.ms/mcjavamoderation");
				}

				this.setInitialScreen(realmsClient, reloadInstance, gameConfig.quickPlay);
			}, this.multiplayerBan()));
		} else {
			this.setInitialScreen(realmsClient, reloadInstance, gameConfig.quickPlay);
		}
	}

	private void onGameLoadFinished() {
		GameLoadTimesEvent.INSTANCE.endStep(TelemetryProperty.LOAD_TIME_LOADING_OVERLAY_MS);
		GameLoadTimesEvent.INSTANCE.endStep(TelemetryProperty.LOAD_TIME_TOTAL_TIME_MS);
		GameLoadTimesEvent.INSTANCE.send(this.telemetryManager.getOutsideSessionSender());
	}

	private void setInitialScreen(RealmsClient realmsClient, ReloadInstance reloadInstance, GameConfig.QuickPlayData quickPlayData) {
		if (quickPlayData.isEnabled()) {
			QuickPlay.connect(this, quickPlayData, reloadInstance, realmsClient);
		} else if (this.options.onboardAccessibility) {
			this.setScreen(new AccessibilityOnboardingScreen(this.options));
		} else {
			this.setScreen(new TitleScreen(true));
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
			if (this.singleplayerServer != null && !this.singleplayerServer.isPublished()) {
				stringBuilder.append(I18n.get("title.singleplayer"));
			} else if (this.isConnectedToRealms()) {
				stringBuilder.append(I18n.get("title.multiplayer.realms"));
			} else if (this.singleplayerServer == null && (this.getCurrentServer() == null || !this.getCurrentServer().isLan())) {
				stringBuilder.append(I18n.get("title.multiplayer.other"));
			} else {
				stringBuilder.append(I18n.get("title.multiplayer.lan"));
			}
		}

		return stringBuilder.toString();
	}

	private UserApiService createUserApiService(YggdrasilAuthenticationService yggdrasilAuthenticationService, GameConfig gameConfig) {
		try {
			return yggdrasilAuthenticationService.createUserApiService(gameConfig.user.user.getAccessToken());
		} catch (AuthenticationException var4) {
			LOGGER.error("Failed to verify authentication", (Throwable)var4);
			return UserApiService.OFFLINE;
		}
	}

	public static ModCheck checkModStatus() {
		return ModCheck.identify("vanilla", ClientBrandRetriever::getClientModName, "Client", Minecraft.class);
	}

	private void rollbackResourcePacks(Throwable throwable) {
		if (this.resourcePackRepository.getSelectedIds().size() > 1) {
			this.clearResourcePacksOnError(throwable, null);
		} else {
			Util.throwAsRuntime(throwable);
		}
	}

	public void clearResourcePacksOnError(Throwable throwable, @Nullable Component component) {
		LOGGER.info("Caught error loading resourcepacks, removing all selected resourcepacks", throwable);
		this.reloadStateTracker.startRecovery(throwable);
		this.resourcePackRepository.setSelected(Collections.emptyList());
		this.options.resourcePacks.clear();
		this.options.incompatibleResourcePacks.clear();
		this.options.save();
		this.reloadResourcePacks(true).thenRun(() -> this.addResourcePackLoadFailToast(component));
	}

	private void abortResourcePackRecovery() {
		this.setOverlay(null);
		if (this.level != null) {
			this.level.disconnect();
			this.clearLevel();
		}

		this.setScreen(new TitleScreen());
		this.addResourcePackLoadFailToast(null);
	}

	private void addResourcePackLoadFailToast(@Nullable Component component) {
		ToastComponent toastComponent = this.getToasts();
		SystemToast.addOrUpdate(toastComponent, SystemToast.SystemToastIds.PACK_LOAD_FAILURE, Component.translatable("resourcePack.load_fail"), component);
	}

	public void run() {
		this.gameThread = Thread.currentThread();
		if (Runtime.getRuntime().availableProcessors() > 4) {
			this.gameThread.setPriority(10);
		}

		try {
			boolean bl = false;

			while (this.running) {
				if (this.delayedCrash != null) {
					crash((CrashReport)this.delayedCrash.get());
					return;
				}

				try {
					SingleTickProfiler singleTickProfiler = SingleTickProfiler.createTickProfiler("Renderer");
					boolean bl2 = this.shouldRenderFpsPie();
					this.profiler = this.constructProfiler(bl2, singleTickProfiler);
					this.profiler.startTick();
					this.metricsRecorder.startTick();
					this.runTick(!bl);
					this.metricsRecorder.endTick();
					this.profiler.endTick();
					this.finishProfilers(bl2, singleTickProfiler);
				} catch (OutOfMemoryError var4) {
					if (bl) {
						throw var4;
					}

					this.emergencySave();
					this.setScreen(new OutOfMemoryScreen());
					System.gc();
					LOGGER.error(LogUtils.FATAL_MARKER, "Out of memory", (Throwable)var4);
					bl = true;
				}
			}
		} catch (ReportedException var5) {
			this.fillReport(var5.getReport());
			this.emergencySave();
			LOGGER.error(LogUtils.FATAL_MARKER, "Reported exception thrown!", (Throwable)var5);
			crash(var5.getReport());
		} catch (Throwable var6) {
			CrashReport crashReport = this.fillReport(new CrashReport("Unexpected error", var6));
			LOGGER.error(LogUtils.FATAL_MARKER, "Unreported exception thrown!", var6);
			this.emergencySave();
			crash(crashReport);
		}
	}

	void selectMainFont(boolean bl) {
		this.fontManager.setRenames(bl ? ImmutableMap.of(DEFAULT_FONT, UNIFORM_FONT) : ImmutableMap.of());
	}

	private void createSearchTrees() {
		this.searchRegistry
			.register(
				SearchRegistry.CREATIVE_NAMES,
				list -> new FullTextSearchTree(
						itemStack -> itemStack.getTooltipLines(null, TooltipFlag.Default.NORMAL.asCreative())
								.stream()
								.map(component -> ChatFormatting.stripFormatting(component.getString()).trim())
								.filter(string -> !string.isEmpty()),
						itemStack -> Stream.of(BuiltInRegistries.ITEM.getKey(itemStack.getItem())),
						list
					)
			);
		this.searchRegistry.register(SearchRegistry.CREATIVE_TAGS, list -> new IdSearchTree(itemStack -> itemStack.getTags().map(TagKey::location), list));
		this.searchRegistry
			.register(
				SearchRegistry.RECIPE_COLLECTIONS,
				list -> new FullTextSearchTree(
						recipeCollection -> recipeCollection.getRecipes()
								.stream()
								.flatMap(recipe -> recipe.getResultItem(recipeCollection.registryAccess()).getTooltipLines(null, TooltipFlag.Default.NORMAL).stream())
								.map(component -> ChatFormatting.stripFormatting(component.getString()).trim())
								.filter(string -> !string.isEmpty()),
						recipeCollection -> recipeCollection.getRecipes()
								.stream()
								.map(recipe -> BuiltInRegistries.ITEM.getKey(recipe.getResultItem(recipeCollection.registryAccess()).getItem())),
						list
					)
			);
		CreativeModeTabs.searchTab().setSearchTreeBuilder(list -> {
			this.populateSearchTree(SearchRegistry.CREATIVE_NAMES, list);
			this.populateSearchTree(SearchRegistry.CREATIVE_TAGS, list);
		});
	}

	private void onFullscreenError(int i, long l) {
		this.options.enableVsync().set(false);
		this.options.save();
	}

	private static boolean checkIs64Bit() {
		String[] strings = new String[]{"sun.arch.data.model", "com.ibm.vm.bitmode", "os.arch"};

		for (String string : strings) {
			String string2 = System.getProperty(string);
			if (string2 != null && string2.contains("64")) {
				return true;
			}
		}

		return false;
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

	public static void crash(CrashReport crashReport) {
		File file = new File(getInstance().gameDirectory, "crash-reports");
		File file2 = new File(file, "crash-" + Util.getFilenameFormattedDateTime() + "-client.txt");
		Bootstrap.realStdoutPrintln(crashReport.getFriendlyReport());
		if (crashReport.getSaveFile() != null) {
			Bootstrap.realStdoutPrintln("#@!@# Game crashed! Crash report saved to: #@!@# " + crashReport.getSaveFile());
			System.exit(-1);
		} else if (crashReport.saveToFile(file2)) {
			Bootstrap.realStdoutPrintln("#@!@# Game crashed! Crash report saved to: #@!@# " + file2.getAbsolutePath());
			System.exit(-1);
		} else {
			Bootstrap.realStdoutPrintln("#@?@# Game crashed! Crash report could not be saved. #@?@#");
			System.exit(-2);
		}
	}

	public boolean isEnforceUnicode() {
		return this.options.forceUnicodeFont().get();
	}

	public CompletableFuture<Void> reloadResourcePacks() {
		return this.reloadResourcePacks(false);
	}

	private CompletableFuture<Void> reloadResourcePacks(boolean bl) {
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
						this.resourceManager.createReload(Util.backgroundExecutor(), this, RESOURCE_RELOAD_INITIAL_TASK, list),
						optional -> Util.ifElse(optional, throwable -> {
								if (bl) {
									this.abortResourcePackRecovery();
								} else {
									this.rollbackResourcePacks(throwable);
								}
							}, () -> {
								this.levelRenderer.allChanged();
								this.reloadStateTracker.finishReload();
								completableFuture.complete(null);
							}),
						true
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

		for (Item item : BuiltInRegistries.ITEM) {
			ItemStack itemStack = item.getDefaultInstance();
			String string = itemStack.getDescriptionId();
			String string2 = Component.translatable(string).getString();
			if (string2.toLowerCase(Locale.ROOT).equals(item.getDescriptionId())) {
				LOGGER.debug("Missing translation for: {} {} {}", itemStack, string, item);
			}
		}

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
						Util.getPlatform().openUri("https://aka.ms/JavaAccountSettings");
					}

					this.setScreen(null);
				}, Minecraft.ChatStatus.INFO_DISABLED_BY_PROFILE, "https://aka.ms/JavaAccountSettings", true));
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
		}

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

				this.clearLevel();
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
			this.levelRenderer.close();
			this.soundManager.destroy();
			this.particleEngine.close();
			this.mobEffectTextures.close();
			this.paintingTextures.close();
			this.textureManager.close();
			this.resourceManager.close();
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
		long l = Util.getNanos();
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

		if (bl) {
			int i = this.timer.advanceTime(Util.getMillis());
			this.profiler.push("scheduledExecutables");
			this.runAllTasks();
			this.profiler.pop();
			this.profiler.push("tick");

			for (int j = 0; j < Math.min(10, i); j++) {
				this.profiler.incrementCounter("clientTick");
				this.tick();
			}

			this.profiler.pop();
		}

		this.mouseHandler.turnPlayer();
		this.window.setErrorSection("Render");
		this.profiler.push("sound");
		this.soundManager.updateSource(this.gameRenderer.getMainCamera());
		this.profiler.pop();
		this.profiler.push("render");
		long m = Util.getNanos();
		boolean bl2;
		if (!this.options.renderDebug && !this.metricsRecorder.isRecording()) {
			bl2 = false;
			this.gpuUtilization = 0.0;
		} else {
			bl2 = this.currentFrameProfile == null || this.currentFrameProfile.isDone();
			if (bl2) {
				TimerQuery.getInstance().ifPresent(TimerQuery::beginProfile);
			}
		}

		RenderSystem.clear(16640, ON_OSX);
		this.mainRenderTarget.bindWrite(true);
		FogRenderer.setupNoFog();
		this.profiler.push("display");
		RenderSystem.enableCull();
		this.profiler.pop();
		if (!this.noRender) {
			this.profiler.popPush("gameRenderer");
			this.gameRenderer.render(this.pause ? this.pausePartialTick : this.timer.partialTick, l, bl);
			this.profiler.pop();
		}

		if (this.fpsPieResults != null) {
			this.profiler.push("fpsPie");
			GuiGraphics guiGraphics = new GuiGraphics(this, this.renderBuffers.bufferSource());
			this.renderFpsMeter(guiGraphics, this.fpsPieResults);
			guiGraphics.flush();
			this.profiler.pop();
		}

		this.profiler.push("blit");
		this.mainRenderTarget.unbindWrite();
		this.mainRenderTarget.blitToScreen(this.window.getWidth(), this.window.getHeight());
		this.frameTimeNs = Util.getNanos() - m;
		if (bl2) {
			TimerQuery.getInstance().ifPresent(timerQuery -> this.currentFrameProfile = timerQuery.endProfile());
		}

		this.profiler.popPush("updateDisplay");
		this.window.updateDisplay();
		int k = this.getFramerateLimit();
		if (k < 260) {
			RenderSystem.limitDisplayFPS(k);
		}

		this.profiler.popPush("yield");
		Thread.yield();
		this.profiler.pop();
		this.window.setErrorSection("Post render");
		this.frames++;
		boolean bl3 = this.hasSingleplayerServer()
			&& (this.screen != null && this.screen.isPauseScreen() || this.overlay != null && this.overlay.isPauseScreen())
			&& !this.singleplayerServer.isPublished();
		if (this.pause != bl3) {
			if (this.pause) {
				this.pausePartialTick = this.timer.partialTick;
			} else {
				this.timer.partialTick = this.pausePartialTick;
			}

			this.pause = bl3;
		}

		long n = Util.getNanos();
		long o = n - this.lastNanoTime;
		if (bl2) {
			this.savedCpuDuration = o;
		}

		this.frameTimer.logFrameDuration(o);
		this.lastNanoTime = n;
		this.profiler.push("fpsUpdate");
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
				this.options.enableVsync().get() ? " vsync" : "",
				this.options.graphicsMode().get(),
				this.options.cloudStatus().get() == CloudStatus.OFF ? "" : (this.options.cloudStatus().get() == CloudStatus.FAST ? " fast-clouds" : " fancy-clouds"),
				this.options.biomeBlendRadius().get(),
				string
			);
			this.lastTime += 1000L;
			this.frames = 0;
		}

		this.profiler.pop();
	}

	private boolean shouldRenderFpsPie() {
		return this.options.renderDebug && this.options.renderDebugCharts && !this.options.hideGui;
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
			profilerFiller = ProfilerFiller.tee(profilerFiller, this.metricsRecorder.getProfiler());
		}

		return SingleTickProfiler.decorateFiller(profilerFiller, singleTickProfiler);
	}

	private void finishProfilers(boolean bl, @Nullable SingleTickProfiler singleTickProfiler) {
		if (singleTickProfiler != null) {
			singleTickProfiler.endTick();
		}

		if (bl) {
			this.fpsPieResults = this.fpsPieProfiler.getResults();
		} else {
			this.fpsPieResults = null;
		}

		this.profiler = this.fpsPieProfiler.getFiller();
	}

	@Override
	public void resizeDisplay() {
		int i = this.window.calculateScale(this.options.guiScale().get(), this.isEnforceUnicode());
		this.window.setGuiScale((double)i);
		if (this.screen != null) {
			this.screen.resize(this, this.window.getGuiScaledWidth(), this.window.getGuiScaledHeight());
		}

		RenderTarget renderTarget = this.getMainRenderTarget();
		renderTarget.resize(this.window.getWidth(), this.window.getHeight(), ON_OSX);
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

	private int getFramerateLimit() {
		return this.level != null || this.screen == null && this.overlay == null ? this.window.getFramerateLimit() : 60;
	}

	public void emergencySave() {
		try {
			MemoryReserve.release();
			this.levelRenderer.clear();
		} catch (Throwable var3) {
		}

		try {
			System.gc();
			if (this.isLocalServer && this.singleplayerServer != null) {
				this.singleplayerServer.halt(true);
			}

			this.clearLevel(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));
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

	public void debugFpsMeterKeyPress(int i) {
		if (this.fpsPieResults != null) {
			List<ResultField> list = this.fpsPieResults.getTimes(this.debugPath);
			if (!list.isEmpty()) {
				ResultField resultField = (ResultField)list.remove(0);
				if (i == 0) {
					if (!resultField.name.isEmpty()) {
						int j = this.debugPath.lastIndexOf(30);
						if (j >= 0) {
							this.debugPath = this.debugPath.substring(0, j);
						}
					}
				} else {
					i--;
					if (i < list.size() && !"unspecified".equals(((ResultField)list.get(i)).name)) {
						if (!this.debugPath.isEmpty()) {
							this.debugPath = this.debugPath + "\u001e";
						}

						this.debugPath = this.debugPath + ((ResultField)list.get(i)).name;
					}
				}
			}
		}
	}

	private void renderFpsMeter(GuiGraphics guiGraphics, ProfileResults profileResults) {
		List<ResultField> list = profileResults.getTimes(this.debugPath);
		ResultField resultField = (ResultField)list.remove(0);
		RenderSystem.clear(256, ON_OSX);
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		Matrix4f matrix4f = new Matrix4f().setOrtho(0.0F, (float)this.window.getWidth(), (float)this.window.getHeight(), 0.0F, 1000.0F, 3000.0F);
		RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.ORTHOGRAPHIC_Z);
		PoseStack poseStack = RenderSystem.getModelViewStack();
		poseStack.pushPose();
		poseStack.setIdentity();
		poseStack.translate(0.0F, 0.0F, -2000.0F);
		RenderSystem.applyModelViewMatrix();
		RenderSystem.lineWidth(1.0F);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		int i = 160;
		int j = this.window.getWidth() - 160 - 10;
		int k = this.window.getHeight() - 320;
		RenderSystem.enableBlend();
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		bufferBuilder.vertex((double)((float)j - 176.0F), (double)((float)k - 96.0F - 16.0F), 0.0).color(200, 0, 0, 0).endVertex();
		bufferBuilder.vertex((double)((float)j - 176.0F), (double)(k + 320), 0.0).color(200, 0, 0, 0).endVertex();
		bufferBuilder.vertex((double)((float)j + 176.0F), (double)(k + 320), 0.0).color(200, 0, 0, 0).endVertex();
		bufferBuilder.vertex((double)((float)j + 176.0F), (double)((float)k - 96.0F - 16.0F), 0.0).color(200, 0, 0, 0).endVertex();
		tesselator.end();
		RenderSystem.disableBlend();
		double d = 0.0;

		for (ResultField resultField2 : list) {
			int l = Mth.floor(resultField2.percentage / 4.0) + 1;
			bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
			int m = resultField2.getColor();
			int n = m >> 16 & 0xFF;
			int o = m >> 8 & 0xFF;
			int p = m & 0xFF;
			bufferBuilder.vertex((double)j, (double)k, 0.0).color(n, o, p, 255).endVertex();

			for (int q = l; q >= 0; q--) {
				float f = (float)((d + resultField2.percentage * (double)q / (double)l) * (float) (Math.PI * 2) / 100.0);
				float g = Mth.sin(f) * 160.0F;
				float h = Mth.cos(f) * 160.0F * 0.5F;
				bufferBuilder.vertex((double)((float)j + g), (double)((float)k - h), 0.0).color(n, o, p, 255).endVertex();
			}

			tesselator.end();
			bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

			for (int q = l; q >= 0; q--) {
				float f = (float)((d + resultField2.percentage * (double)q / (double)l) * (float) (Math.PI * 2) / 100.0);
				float g = Mth.sin(f) * 160.0F;
				float h = Mth.cos(f) * 160.0F * 0.5F;
				if (!(h > 0.0F)) {
					bufferBuilder.vertex((double)((float)j + g), (double)((float)k - h), 0.0).color(n >> 1, o >> 1, p >> 1, 255).endVertex();
					bufferBuilder.vertex((double)((float)j + g), (double)((float)k - h + 10.0F), 0.0).color(n >> 1, o >> 1, p >> 1, 255).endVertex();
				}
			}

			tesselator.end();
			d += resultField2.percentage;
		}

		DecimalFormat decimalFormat = new DecimalFormat("##0.00");
		decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
		String string = ProfileResults.demanglePath(resultField.name);
		String string2 = "";
		if (!"unspecified".equals(string)) {
			string2 = string2 + "[0] ";
		}

		if (string.isEmpty()) {
			string2 = string2 + "ROOT ";
		} else {
			string2 = string2 + string + " ";
		}

		int m = 16777215;
		guiGraphics.drawString(this.font, string2, j - 160, k - 80 - 16, 16777215);
		string2 = decimalFormat.format(resultField.globalPercentage) + "%";
		guiGraphics.drawString(this.font, string2, j + 160 - this.font.width(string2), k - 80 - 16, 16777215);

		for (int r = 0; r < list.size(); r++) {
			ResultField resultField3 = (ResultField)list.get(r);
			StringBuilder stringBuilder = new StringBuilder();
			if ("unspecified".equals(resultField3.name)) {
				stringBuilder.append("[?] ");
			} else {
				stringBuilder.append("[").append(r + 1).append("] ");
			}

			String string3 = stringBuilder.append(resultField3.name).toString();
			guiGraphics.drawString(this.font, string3, j - 160, k + 80 + r * 8 + 20, resultField3.getColor());
			string3 = decimalFormat.format(resultField3.percentage) + "%";
			guiGraphics.drawString(this.font, string3, j + 160 - 50 - this.font.width(string3), k + 80 + r * 8 + 20, resultField3.getColor());
			string3 = decimalFormat.format(resultField3.globalPercentage) + "%";
			guiGraphics.drawString(this.font, string3, j + 160 - this.font.width(string3), k + 80 + r * 8 + 20, resultField3.getColor());
		}

		poseStack.popPose();
		RenderSystem.applyModelViewMatrix();
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

								if (interactionResult.consumesAction()) {
									if (interactionResult.shouldSwing()) {
										this.player.swing(interactionHand);
									}

									return;
								}
								break;
							case BLOCK:
								BlockHitResult blockHitResult = (BlockHitResult)this.hitResult;
								int i = itemStack.getCount();
								InteractionResult interactionResult2 = this.gameMode.useItemOn(this.player, interactionHand, blockHitResult);
								if (interactionResult2.consumesAction()) {
									if (interactionResult2.shouldSwing()) {
										this.player.swing(interactionHand);
										if (!itemStack.isEmpty() && (itemStack.getCount() != i || this.gameMode.hasInfiniteItems())) {
											this.gameRenderer.itemInHandRenderer.itemUsed(interactionHand);
										}
									}

									return;
								}

								if (interactionResult2 == InteractionResult.FAIL) {
									return;
								}
						}
					}

					if (!itemStack.isEmpty()) {
						InteractionResult interactionResult3 = this.gameMode.useItem(this.player, interactionHand);
						if (interactionResult3.consumesAction()) {
							if (interactionResult3.shouldSwing()) {
								this.player.swing(interactionHand);
							}

							this.gameRenderer.itemInHandRenderer.itemUsed(interactionHand);
							return;
						}
					}
				}
			}
		}
	}

	public MusicManager getMusicManager() {
		return this.musicManager;
	}

	public void tick() {
		if (this.rightClickDelay > 0) {
			this.rightClickDelay--;
		}

		this.profiler.push("gui");
		this.chatListener.tick();
		this.gui.tick(this.pause);
		this.profiler.pop();
		this.gameRenderer.pick(1.0F);
		this.tutorial.onLookAt(this.level, this.hitResult);
		this.profiler.push("gameMode");
		if (!this.pause && this.level != null) {
			this.gameMode.tick();
		}

		this.profiler.popPush("textures");
		if (this.level != null) {
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
			Screen.wrapScreenError(() -> this.screen.tick(), "Ticking screen", this.screen.getClass().getCanonicalName());
		}

		if (!this.options.renderDebug) {
			this.gui.clearCache();
		}

		if (this.overlay == null && this.screen == null) {
			this.profiler.popPush("Keybindings");
			this.handleKeybinds();
			if (this.missTime > 0) {
				this.missTime--;
			}
		}

		if (this.level != null) {
			this.profiler.popPush("gameRenderer");
			if (!this.pause) {
				this.gameRenderer.tick();
			}

			this.profiler.popPush("levelRenderer");
			if (!this.pause) {
				this.levelRenderer.tick();
			}

			this.profiler.popPush("level");
			if (!this.pause) {
				this.level.tickEntities();
			}
		} else if (this.gameRenderer.currentEffect() != null) {
			this.gameRenderer.shutdownEffect();
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
					this.socialInteractionsToast = new TutorialToast(TutorialToast.Icons.SOCIAL_INTERACTIONS, component, component2, true);
					this.tutorial.addTimedToast(this.socialInteractionsToast, 160);
					this.options.joinedFirstServer = true;
					this.options.save();
				}

				this.tutorial.tick();

				try {
					this.level.tick(() -> true);
				} catch (Throwable var4) {
					CrashReport crashReport = CrashReport.forThrowable(var4, "Exception in world tick");
					if (this.level == null) {
						CrashReportCategory crashReportCategory = crashReport.addCategory("Affected level");
						crashReportCategory.setDetail("Problem", "Level is null!");
					} else {
						this.level.fillReportDetails(crashReport);
					}

					throw new ReportedException(crashReport);
				}
			}

			this.profiler.popPush("animateTick");
			if (!this.pause && this.level != null) {
				this.level.animateTick(this.player.getBlockX(), this.player.getBlockY(), this.player.getBlockZ());
			}

			this.profiler.popPush("particles");
			if (!this.pause) {
				this.particleEngine.tick();
			}
		} else if (this.pendingConnection != null) {
			this.profiler.popPush("pendingConnection");
			this.pendingConnection.tick();
		}

		this.profiler.popPush("keyboard");
		this.keyboardHandler.tick();
		this.profiler.pop();
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
					this.tutorial.removeTimedToast(this.socialInteractionsToast);
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

	public void doWorldLoad(
		String string, LevelStorageSource.LevelStorageAccess levelStorageAccess, PackRepository packRepository, WorldStem worldStem, boolean bl
	) {
		this.clearLevel();
		this.progressListener.set(null);
		Instant instant = Instant.now();

		try {
			levelStorageAccess.saveDataTag(worldStem.registries().compositeAccess(), worldStem.worldData());
			Services services = Services.create(this.authenticationService, this.gameDirectory);
			services.profileCache().setExecutor(this);
			SkullBlockEntity.setup(services, this);
			GameProfileCache.setUsesAuthentication(false);
			this.singleplayerServer = MinecraftServer.spin(thread -> new IntegratedServer(thread, this, levelStorageAccess, packRepository, worldStem, services, i -> {
					StoringChunkProgressListener storingChunkProgressListener = new StoringChunkProgressListener(i + 0);
					this.progressListener.set(storingChunkProgressListener);
					return ProcessorChunkProgressListener.createStarted(storingChunkProgressListener, this.progressTasks::add);
				}));
			this.isLocalServer = true;
			this.updateReportEnvironment(ReportEnvironment.local());
			this.quickPlayLog.setWorldData(QuickPlayLog.Type.SINGLEPLAYER, string, worldStem.worldData().getLevelName());
		} catch (Throwable var12) {
			CrashReport crashReport = CrashReport.forThrowable(var12, "Starting integrated server");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Starting integrated server");
			crashReportCategory.setDetail("Level ID", string);
			crashReportCategory.setDetail("Level Name", (CrashReportDetail<String>)(() -> worldStem.worldData().getLevelName()));
			throw new ReportedException(crashReport);
		}

		while (this.progressListener.get() == null) {
			Thread.yield();
		}

		LevelLoadingScreen levelLoadingScreen = new LevelLoadingScreen((StoringChunkProgressListener)this.progressListener.get());
		this.setScreen(levelLoadingScreen);
		this.profiler.push("waitForServer");

		while (!this.singleplayerServer.isReady()) {
			levelLoadingScreen.tick();
			this.runTick(false);

			try {
				Thread.sleep(16L);
			} catch (InterruptedException var11) {
			}

			if (this.delayedCrash != null) {
				crash((CrashReport)this.delayedCrash.get());
				return;
			}
		}

		this.profiler.pop();
		Duration duration = Duration.between(instant, Instant.now());
		SocketAddress socketAddress = this.singleplayerServer.getConnection().startMemoryChannel();
		Connection connection = Connection.connectToLocalServer(socketAddress);
		connection.setListener(new ClientHandshakePacketListenerImpl(connection, this, null, null, bl, duration, component -> {
		}));
		connection.send(new ClientIntentionPacket(socketAddress.toString(), 0, ConnectionProtocol.LOGIN));
		connection.send(new ServerboundHelloPacket(this.getUser().getName(), Optional.ofNullable(this.getUser().getProfileId())));
		this.pendingConnection = connection;
	}

	public void setLevel(ClientLevel clientLevel) {
		ProgressScreen progressScreen = new ProgressScreen(true);
		progressScreen.progressStartNoAbort(Component.translatable("connect.joining"));
		this.updateScreenAndTick(progressScreen);
		this.level = clientLevel;
		this.updateLevelInEngines(clientLevel);
		if (!this.isLocalServer) {
			Services services = Services.create(this.authenticationService, this.gameDirectory);
			services.profileCache().setExecutor(this);
			SkullBlockEntity.setup(services, this);
			GameProfileCache.setUsesAuthentication(false);
		}
	}

	public void clearLevel() {
		this.clearLevel(new ProgressScreen(true));
	}

	public void clearLevel(Screen screen) {
		ClientPacketListener clientPacketListener = this.getConnection();
		if (clientPacketListener != null) {
			this.dropAllTasks();
			clientPacketListener.close();
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
		this.updateScreenAndTick(screen);
		if (this.level != null) {
			if (integratedServer != null) {
				this.profiler.push("waitForServer");

				while (!integratedServer.isShutdown()) {
					this.runTick(false);
				}

				this.profiler.pop();
			}

			this.downloadedPackSource.clearServerPack();
			this.gui.onDisconnected();
			this.isLocalServer = false;
		}

		this.level = null;
		this.updateLevelInEngines(null);
		this.player = null;
		SkullBlockEntity.clear();
	}

	private void updateScreenAndTick(Screen screen) {
		this.profiler.push("forcedTick");
		this.soundManager.stop();
		this.cameraEntity = null;
		this.pendingConnection = null;
		this.setScreen(screen);
		this.runTick(false);
		this.profiler.pop();
	}

	public void forceSetScreen(Screen screen) {
		this.profiler.push("forcedTick");
		this.setScreen(screen);
		this.runTick(false);
		this.profiler.pop();
	}

	private void updateLevelInEngines(@Nullable ClientLevel clientLevel) {
		this.levelRenderer.setLevel(clientLevel);
		this.particleEngine.setLevel(clientLevel);
		this.blockEntityRenderDispatcher.setLevel(clientLevel);
		this.updateTitle();
	}

	public boolean telemetryOptInExtra() {
		return this.extraTelemetryAvailable() && this.options.telemetryOptInExtra().get();
	}

	public boolean extraTelemetryAvailable() {
		return this.allowsTelemetry() && this.userApiService.properties().flag(UserFlag.OPTIONAL_TELEMETRY_AVAILABLE);
	}

	public boolean allowsTelemetry() {
		return this.userApiService.properties().flag(UserFlag.TELEMETRY_ENABLED);
	}

	public boolean allowsMultiplayer() {
		return this.allowsMultiplayer && this.userApiService.properties().flag(UserFlag.SERVERS_ALLOWED) && this.multiplayerBan() == null;
	}

	public boolean allowsRealms() {
		return this.userApiService.properties().flag(UserFlag.REALMS_ALLOWED) && this.multiplayerBan() == null;
	}

	public boolean shouldShowBanNotice() {
		return this.multiplayerBan() != null;
	}

	@Nullable
	public BanDetails multiplayerBan() {
		return (BanDetails)this.userApiService.properties().bannedScopes().get("MULTIPLAYER");
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
			return !this.userApiService.properties().flag(UserFlag.CHAT_ALLOWED) ? Minecraft.ChatStatus.DISABLED_BY_PROFILE : Minecraft.ChatStatus.ENABLED;
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
					this.addCustomNbtData(itemStack, blockEntity);
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

	private void addCustomNbtData(ItemStack itemStack, BlockEntity blockEntity) {
		CompoundTag compoundTag = blockEntity.saveWithFullMetadata();
		BlockItem.setBlockEntityData(itemStack, blockEntity.getType(), compoundTag);
		if (itemStack.getItem() instanceof PlayerHeadItem && compoundTag.contains("SkullOwner")) {
			CompoundTag compoundTag2 = compoundTag.getCompound("SkullOwner");
			CompoundTag compoundTag3 = itemStack.getOrCreateTag();
			compoundTag3.put("SkullOwner", compoundTag2);
			CompoundTag compoundTag4 = compoundTag3.getCompound("BlockEntityTag");
			compoundTag4.remove("SkullOwner");
			compoundTag4.remove("x");
			compoundTag4.remove("y");
			compoundTag4.remove("z");
		} else {
			CompoundTag compoundTag2 = new CompoundTag();
			ListTag listTag = new ListTag();
			listTag.add(StringTag.valueOf("\"(+NBT)\""));
			compoundTag2.put("Lore", listTag);
			itemStack.addTagElement("display", compoundTag2);
		}
	}

	public CrashReport fillReport(CrashReport crashReport) {
		SystemReport systemReport = crashReport.getSystemReport();
		fillSystemReport(systemReport, this, this.languageManager, this.launchedVersion, this.options);
		if (this.level != null) {
			this.level.fillReportDetails(crashReport);
		}

		if (this.singleplayerServer != null) {
			this.singleplayerServer.fillSystemReport(systemReport);
		}

		this.reloadStateTracker.fillCrashReport(crashReport);
		return crashReport;
	}

	public static void fillReport(
		@Nullable Minecraft minecraft, @Nullable LanguageManager languageManager, String string, @Nullable Options options, CrashReport crashReport
	) {
		SystemReport systemReport = crashReport.getSystemReport();
		fillSystemReport(systemReport, minecraft, languageManager, string, options);
	}

	private static SystemReport fillSystemReport(
		SystemReport systemReport, @Nullable Minecraft minecraft, @Nullable LanguageManager languageManager, String string, Options options
	) {
		systemReport.setDetail("Launched Version", (Supplier<String>)(() -> string));
		systemReport.setDetail("Backend library", RenderSystem::getBackendDescription);
		systemReport.setDetail("Backend API", RenderSystem::getApiDescription);
		systemReport.setDetail(
			"Window size", (Supplier<String>)(() -> minecraft != null ? minecraft.window.getWidth() + "x" + minecraft.window.getHeight() : "<not initialized>")
		);
		systemReport.setDetail("GL Caps", RenderSystem::getCapsString);
		systemReport.setDetail(
			"GL debug messages", (Supplier<String>)(() -> GlDebug.isDebugEnabled() ? String.join("\n", GlDebug.getLastOpenGlDebugMessages()) : "<disabled>")
		);
		systemReport.setDetail("Using VBOs", (Supplier<String>)(() -> "Yes"));
		systemReport.setDetail("Is Modded", (Supplier<String>)(() -> checkModStatus().fullDescription()));
		systemReport.setDetail("Type", "Client (map_client.txt)");
		if (options != null) {
			if (instance != null) {
				String string2 = instance.getGpuWarnlistManager().getAllWarnings();
				if (string2 != null) {
					systemReport.setDetail("GPU Warnings", string2);
				}
			}

			systemReport.setDetail("Graphics mode", options.graphicsMode().get().toString());
			systemReport.setDetail("Resource Packs", (Supplier<String>)(() -> {
				StringBuilder stringBuilder = new StringBuilder();

				for (String stringx : options.resourcePacks) {
					if (stringBuilder.length() > 0) {
						stringBuilder.append(", ");
					}

					stringBuilder.append(stringx);
					if (options.incompatibleResourcePacks.contains(stringx)) {
						stringBuilder.append(" (incompatible)");
					}
				}

				return stringBuilder.toString();
			}));
		}

		if (languageManager != null) {
			systemReport.setDetail("Current Language", (Supplier<String>)(() -> languageManager.getSelected()));
		}

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

	public User getUser() {
		return this.user;
	}

	public PropertyMap getProfileProperties() {
		if (this.profileProperties.isEmpty()) {
			GameProfile gameProfile = this.getMinecraftSessionService().fillProfileProperties(this.user.getGameProfile(), false);
			this.profileProperties.putAll(gameProfile.getProperties());
		}

		return this.profileProperties;
	}

	public Proxy getProxy() {
		return this.proxy;
	}

	public TextureManager getTextureManager() {
		return this.textureManager;
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

	public boolean is64Bit() {
		return this.is64bit;
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
	protected Runnable wrapRunnable(Runnable runnable) {
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

	public <T> SearchTree<T> getSearchTree(SearchRegistry.Key<T> key) {
		return this.searchRegistry.getTree(key);
	}

	public <T> void populateSearchTree(SearchRegistry.Key<T> key, List<T> list) {
		this.searchRegistry.populate(key, list);
	}

	public FrameTimer getFrameTimer() {
		return this.frameTimer;
	}

	public boolean isConnectedToRealms() {
		return this.connectedToRealms;
	}

	public void setConnectedToRealms(boolean bl) {
		this.connectedToRealms = bl;
	}

	public DataFixer getFixerUpper() {
		return this.fixerUpper;
	}

	public float getFrameTime() {
		return this.timer.partialTick;
	}

	public float getDeltaFrameTime() {
		return this.timer.tickDelta;
	}

	public BlockColors getBlockColors() {
		return this.blockColors;
	}

	public boolean showOnlyReducedInfo() {
		return this.player != null && this.player.isReducedDebugInfo() || this.options.reducedDebugInfo().get();
	}

	public ToastComponent getToasts() {
		return this.toast;
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

	@Override
	public void setWindowActive(boolean bl) {
		this.windowActive = bl;
	}

	public Component grabPanoramixScreenshot(File file, int i, int j) {
		int k = this.window.getWidth();
		int l = this.window.getHeight();
		RenderTarget renderTarget = new TextureTarget(i, j, true, ON_OSX);
		float f = this.player.getXRot();
		float g = this.player.getYRot();
		float h = this.player.xRotO;
		float m = this.player.yRotO;
		this.gameRenderer.setRenderBlockOutline(false);

		MutableComponent var12;
		try {
			this.gameRenderer.setPanoramicMode(true);
			this.levelRenderer.graphicsChanged();
			this.window.setWidth(i);
			this.window.setHeight(j);

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
				this.gameRenderer.renderLevel(1.0F, 0L, new PoseStack());

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
			renderTarget.destroyBuffers();
			this.gameRenderer.setPanoramicMode(false);
			this.levelRenderer.graphicsChanged();
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

	public ProfilerFiller getProfiler() {
		return this.profiler;
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

	public boolean renderOnThread() {
		return false;
	}

	public Window getWindow() {
		return this.window;
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

	public boolean isTextFilteringEnabled() {
		return this.userApiService.properties().flag(UserFlag.PROFANITY_FILTER_ENABLED);
	}

	public void prepareForMultiplayer() {
		this.playerSocialManager.startOnlineMode();
		this.getProfileKeyPairManager().prepareKeyPair();
	}

	public Realms32BitWarningStatus getRealms32BitWarningStatus() {
		return this.realms32BitWarningStatus;
	}

	@Nullable
	public SignatureValidator getProfileKeySignatureValidator() {
		return SignatureValidator.from(this.authenticationService.getServicesKeySet(), ServicesKeyType.PROFILE_KEY);
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

		ChatStatus(Component component) {
			this.message = component;
		}

		public Component getMessage() {
			return this.message;
		}

		public abstract boolean isChatAllowed(boolean bl);
	}
}
