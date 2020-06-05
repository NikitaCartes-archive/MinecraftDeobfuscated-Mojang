package net.minecraft.client;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Queues;
import com.google.gson.JsonElement;
import com.mojang.authlib.AuthenticationService;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
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
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DatapackLoadFailureScreen;
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
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.VirtualScreen;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.client.resources.FoliageColorReloadListener;
import net.minecraft.client.resources.GrassColorReloadListener;
import net.minecraft.client.resources.LegacyPackResourcesAdapter;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.client.resources.PackResourcesAdapterV4;
import net.minecraft.client.resources.PaintingTextureManager;
import net.minecraft.client.resources.ResourcePack;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.searchtree.MutableSearchTree;
import net.minecraft.client.searchtree.ReloadableIdSearchTree;
import net.minecraft.client.searchtree.ReloadableSearchTree;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.KeybindComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerResources;
import net.minecraft.server.level.progress.ProcessorChunkProgressListener;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.profiling.ContinuousProfiler;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.ResultField;
import net.minecraft.util.profiling.SingleTickProfiler;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Snooper;
import net.minecraft.world.SnooperPopulator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class Minecraft extends ReentrantBlockableEventLoop<Runnable> implements SnooperPopulator, WindowEventHandler {
	private static Minecraft instance;
	private static final Logger LOGGER = LogManager.getLogger();
	public static final boolean ON_OSX = Util.getPlatform() == Util.OS.OSX;
	public static final ResourceLocation DEFAULT_FONT = new ResourceLocation("default");
	public static final ResourceLocation UNIFORM_FONT = new ResourceLocation("uniform");
	public static final ResourceLocation ALT_FONT = new ResourceLocation("alt");
	private static final CompletableFuture<Unit> RESOURCE_RELOAD_INITIAL_TASK = CompletableFuture.completedFuture(Unit.INSTANCE);
	private final File resourcePackDirectory;
	private final PropertyMap profileProperties;
	private final TextureManager textureManager;
	private final DataFixer fixerUpper;
	private final VirtualScreen virtualScreen;
	private final Window window;
	private final Timer timer = new Timer(20.0F, 0L);
	private final Snooper snooper = new Snooper("client", this, Util.getMillis());
	private final RenderBuffers renderBuffers;
	public final LevelRenderer levelRenderer;
	private final EntityRenderDispatcher entityRenderDispatcher;
	private final ItemRenderer itemRenderer;
	private final ItemInHandRenderer itemInHandRenderer;
	public final ParticleEngine particleEngine;
	private final SearchRegistry searchRegistry = new SearchRegistry();
	private final User user;
	public final Font font;
	public final GameRenderer gameRenderer;
	public final DebugRenderer debugRenderer;
	private final AtomicReference<StoringChunkProgressListener> progressListener = new AtomicReference();
	public final Gui gui;
	public final Options options;
	private final HotbarManager hotbarManager;
	public final MouseHandler mouseHandler;
	public final KeyboardHandler keyboardHandler;
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
	private final ClientPackSource clientPackSource;
	private final PackRepository<ResourcePack> resourcePackRepository;
	private final LanguageManager languageManager;
	private final BlockColors blockColors;
	private final ItemColors itemColors;
	private final RenderTarget mainRenderTarget;
	private final SoundManager soundManager;
	private final MusicManager musicManager;
	private final FontManager fontManager;
	private final SplashManager splashManager;
	private final MinecraftSessionService minecraftSessionService;
	private final SkinManager skinManager;
	private final ModelManager modelManager;
	private final BlockRenderDispatcher blockRenderer;
	private final PaintingTextureManager paintingTextures;
	private final MobEffectTextureManager mobEffectTextures;
	private final ToastComponent toast;
	private final Game game = new Game(this);
	private final Tutorial tutorial;
	public static byte[] reserve = new byte[10485760];
	@Nullable
	public MultiPlayerGameMode gameMode;
	@Nullable
	public ClientLevel level;
	@Nullable
	public LocalPlayer player;
	@Nullable
	private IntegratedServer singleplayerServer;
	@Nullable
	private ServerData currentServer;
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
	private boolean pause;
	private float pausePartialTick;
	private long lastNanoTime = Util.getNanos();
	private long lastTime;
	private int frames;
	public boolean noRender;
	@Nullable
	public Screen screen;
	@Nullable
	public Overlay overlay;
	private boolean connectedToRealms;
	private Thread gameThread;
	private volatile boolean running = true;
	@Nullable
	private CrashReport delayedCrash;
	private static int fps;
	public String fpsString = "";
	public boolean chunkPath;
	public boolean chunkVisibility;
	public boolean smartCull = true;
	private boolean windowActive;
	private final Queue<Runnable> progressTasks = Queues.<Runnable>newConcurrentLinkedQueue();
	@Nullable
	private CompletableFuture<Void> pendingReload;
	private ProfilerFiller profiler = InactiveProfiler.INSTANCE;
	private int fpsPieRenderTicks;
	private final ContinuousProfiler fpsPieProfiler = new ContinuousProfiler(Util.timeSource, () -> this.fpsPieRenderTicks);
	@Nullable
	private ProfileResults fpsPieResults;
	private String debugPath = "root";

	public Minecraft(GameConfig gameConfig) {
		super("Client");
		instance = this;
		this.gameDirectory = gameConfig.location.gameDirectory;
		File file = gameConfig.location.assetDirectory;
		this.resourcePackDirectory = gameConfig.location.resourcePackDirectory;
		this.launchedVersion = gameConfig.game.launchVersion;
		this.versionType = gameConfig.game.versionType;
		this.profileProperties = gameConfig.user.profileProperties;
		this.clientPackSource = new ClientPackSource(new File(this.gameDirectory, "server-resource-packs"), gameConfig.location.getAssetIndex());
		this.resourcePackRepository = new PackRepository<>(
			Minecraft::createClientPackAdapter, this.clientPackSource, new FolderRepositorySource(this.resourcePackDirectory, PackSource.DEFAULT)
		);
		this.proxy = gameConfig.user.proxy;
		this.minecraftSessionService = new YggdrasilAuthenticationService(this.proxy, UUID.randomUUID().toString()).createMinecraftSessionService();
		this.user = gameConfig.user.user;
		LOGGER.info("Setting user: {}", this.user.getName());
		LOGGER.debug("(Session ID is {})", this.user.getSessionId());
		this.demo = gameConfig.game.demo;
		this.allowsMultiplayer = !gameConfig.game.disableMultiplayer;
		this.allowsChat = !gameConfig.game.disableChat;
		this.is64bit = checkIs64Bit();
		this.singleplayerServer = null;
		String string;
		int i;
		if (this.allowsMultiplayer && gameConfig.server.hostname != null) {
			string = gameConfig.server.hostname;
			i = gameConfig.server.port;
		} else {
			string = null;
			i = 0;
		}

		KeybindComponent.setKeyResolver(KeyMapping::createNameSupplier);
		this.fixerUpper = DataFixers.getDataFixer();
		this.toast = new ToastComponent(this);
		this.tutorial = new Tutorial(this);
		this.gameThread = Thread.currentThread();
		this.options = new Options(this, this.gameDirectory);
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

		try {
			InputStream inputStream = this.getClientPackSource().getVanillaPack().getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("icons/icon_16x16.png"));
			InputStream inputStream2 = this.getClientPackSource().getVanillaPack().getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("icons/icon_32x32.png"));
			this.window.setIcon(inputStream, inputStream2);
		} catch (IOException var8) {
			LOGGER.error("Couldn't set icon", (Throwable)var8);
		}

		this.window.setFramerateLimit(this.options.framerateLimit);
		this.mouseHandler = new MouseHandler(this);
		this.mouseHandler.setup(this.window.getWindow());
		this.keyboardHandler = new KeyboardHandler(this);
		this.keyboardHandler.setup(this.window.getWindow());
		RenderSystem.initRenderer(this.options.glDebugVerbosity, false);
		this.mainRenderTarget = new RenderTarget(this.window.getWidth(), this.window.getHeight(), true, ON_OSX);
		this.mainRenderTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
		this.resourceManager = new SimpleReloadableResourceManager(PackType.CLIENT_RESOURCES);
		this.resourcePackRepository.reload();
		this.options.loadSelectedResourcePacks(this.resourcePackRepository);
		this.languageManager = new LanguageManager(this.options.languageCode);
		this.resourceManager.registerReloadListener(this.languageManager);
		this.textureManager = new TextureManager(this.resourceManager);
		this.resourceManager.registerReloadListener(this.textureManager);
		this.skinManager = new SkinManager(this.textureManager, new File(file, "skins"), this.minecraftSessionService);
		this.levelSource = new LevelStorageSource(this.gameDirectory.toPath().resolve("saves"), this.gameDirectory.toPath().resolve("backups"), this.fixerUpper);
		this.soundManager = new SoundManager(this.resourceManager, this.options);
		this.resourceManager.registerReloadListener(this.soundManager);
		this.splashManager = new SplashManager(this.user);
		this.resourceManager.registerReloadListener(this.splashManager);
		this.musicManager = new MusicManager(this);
		this.fontManager = new FontManager(this.textureManager);
		this.font = this.fontManager.createFont();
		this.resourceManager.registerReloadListener(this.fontManager.getReloadListener());
		this.selectMainFont(this.isEnforceUnicode());
		this.resourceManager.registerReloadListener(new GrassColorReloadListener());
		this.resourceManager.registerReloadListener(new FoliageColorReloadListener());
		this.window.setErrorSection("Startup");
		RenderSystem.setupDefaultState(0, 0, this.window.getWidth(), this.window.getHeight());
		this.window.setErrorSection("Post startup");
		this.blockColors = BlockColors.createDefault();
		this.itemColors = ItemColors.createDefault(this.blockColors);
		this.modelManager = new ModelManager(this.textureManager, this.blockColors, this.options.mipmapLevels);
		this.resourceManager.registerReloadListener(this.modelManager);
		this.itemRenderer = new ItemRenderer(this.textureManager, this.modelManager, this.itemColors);
		this.entityRenderDispatcher = new EntityRenderDispatcher(this.textureManager, this.itemRenderer, this.resourceManager, this.font, this.options);
		this.itemInHandRenderer = new ItemInHandRenderer(this);
		this.resourceManager.registerReloadListener(this.itemRenderer);
		this.renderBuffers = new RenderBuffers();
		this.gameRenderer = new GameRenderer(this, this.resourceManager, this.renderBuffers);
		this.resourceManager.registerReloadListener(this.gameRenderer);
		this.blockRenderer = new BlockRenderDispatcher(this.modelManager.getBlockModelShaper(), this.blockColors);
		this.resourceManager.registerReloadListener(this.blockRenderer);
		this.levelRenderer = new LevelRenderer(this, this.renderBuffers);
		this.resourceManager.registerReloadListener(this.levelRenderer);
		this.createSearchTrees();
		this.resourceManager.registerReloadListener(this.searchRegistry);
		this.particleEngine = new ParticleEngine(this.level, this.textureManager);
		this.resourceManager.registerReloadListener(this.particleEngine);
		this.paintingTextures = new PaintingTextureManager(this.textureManager);
		this.resourceManager.registerReloadListener(this.paintingTextures);
		this.mobEffectTextures = new MobEffectTextureManager(this.textureManager);
		this.resourceManager.registerReloadListener(this.mobEffectTextures);
		this.gui = new Gui(this);
		this.debugRenderer = new DebugRenderer(this);
		RenderSystem.setErrorCallback(this::onFullscreenError);
		if (this.options.fullscreen && !this.window.isFullscreen()) {
			this.window.toggleFullScreen();
			this.options.fullscreen = this.window.isFullscreen();
		}

		this.window.updateVsync(this.options.enableVsync);
		this.window.updateRawMouseInput(this.options.rawMouseInput);
		this.window.setDefaultErrorCallback();
		this.resizeDisplay();
		if (string != null) {
			this.setScreen(new ConnectScreen(new TitleScreen(), this, string, i));
		} else {
			this.setScreen(new TitleScreen(true));
		}

		LoadingOverlay.registerTextures(this);
		List<PackResources> list = this.resourcePackRepository.openAllSelected();
		this.setOverlay(
			new LoadingOverlay(
				this,
				this.resourceManager.createFullReload(Util.backgroundExecutor(), this, RESOURCE_RELOAD_INITIAL_TASK, list),
				optional -> Util.ifElse(optional, this::rollbackResourcePacks, () -> {
						if (SharedConstants.IS_RUNNING_IN_IDE) {
							this.selfTest();
						}
					}),
				false
			)
		);
	}

	public void updateTitle() {
		this.window.setTitle(this.createTitle());
	}

	private String createTitle() {
		StringBuilder stringBuilder = new StringBuilder("Minecraft");
		if (this.isProbablyModded()) {
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
			} else if (this.singleplayerServer == null && (this.currentServer == null || !this.currentServer.isLan())) {
				stringBuilder.append(I18n.get("title.multiplayer.other"));
			} else {
				stringBuilder.append(I18n.get("title.multiplayer.lan"));
			}
		}

		return stringBuilder.toString();
	}

	public boolean isProbablyModded() {
		return !"vanilla".equals(ClientBrandRetriever.getClientModName()) || Minecraft.class.getSigners() == null;
	}

	private void rollbackResourcePacks(Throwable throwable) {
		if (this.resourcePackRepository.getSelectedIds().size() > 1) {
			Component component;
			if (throwable instanceof SimpleReloadableResourceManager.ResourcePackLoadingFailure) {
				component = new TextComponent(((SimpleReloadableResourceManager.ResourcePackLoadingFailure)throwable).getPack().getName());
			} else {
				component = null;
			}

			LOGGER.info("Caught error loading resourcepacks, removing all selected resourcepacks", throwable);
			this.resourcePackRepository.setSelected(Collections.emptyList());
			this.options.resourcePacks.clear();
			this.options.incompatibleResourcePacks.clear();
			this.options.save();
			this.reloadResourcePacks().thenRun(() -> {
				ToastComponent toastComponent = this.getToasts();
				SystemToast.addOrUpdate(toastComponent, SystemToast.SystemToastIds.PACK_LOAD_FAILURE, new TranslatableComponent("resourcePack.load_fail"), component);
			});
		} else {
			Util.throwAsRuntime(throwable);
		}
	}

	public void run() {
		this.gameThread = Thread.currentThread();

		try {
			boolean bl = false;

			while (this.running) {
				if (this.delayedCrash != null) {
					crash(this.delayedCrash);
					return;
				}

				try {
					SingleTickProfiler singleTickProfiler = SingleTickProfiler.createTickProfiler("Renderer");
					boolean bl2 = this.shouldRenderFpsPie();
					this.startProfilers(bl2, singleTickProfiler);
					this.profiler.startTick();
					this.runTick(!bl);
					this.profiler.endTick();
					this.finishProfilers(bl2, singleTickProfiler);
				} catch (OutOfMemoryError var4) {
					if (bl) {
						throw var4;
					}

					this.emergencySave();
					this.setScreen(new OutOfMemoryScreen());
					System.gc();
					LOGGER.fatal("Out of memory", (Throwable)var4);
					bl = true;
				}
			}
		} catch (ReportedException var5) {
			this.fillReport(var5.getReport());
			this.emergencySave();
			LOGGER.fatal("Reported exception thrown!", (Throwable)var5);
			crash(var5.getReport());
		} catch (Throwable var6) {
			CrashReport crashReport = this.fillReport(new CrashReport("Unexpected error", var6));
			LOGGER.fatal("Unreported exception thrown!", var6);
			this.emergencySave();
			crash(crashReport);
		}
	}

	void selectMainFont(boolean bl) {
		this.fontManager.setRenames(bl ? ImmutableMap.of(DEFAULT_FONT, UNIFORM_FONT) : ImmutableMap.of());
	}

	private void createSearchTrees() {
		ReloadableSearchTree<ItemStack> reloadableSearchTree = new ReloadableSearchTree<>(
			itemStack -> itemStack.getTooltipLines(null, TooltipFlag.Default.NORMAL)
					.stream()
					.map(component -> ChatFormatting.stripFormatting(component.getString()).trim())
					.filter(string -> !string.isEmpty()),
			itemStack -> Stream.of(Registry.ITEM.getKey(itemStack.getItem()))
		);
		ReloadableIdSearchTree<ItemStack> reloadableIdSearchTree = new ReloadableIdSearchTree<>(
			itemStack -> ItemTags.getAllTags().getMatchingTags(itemStack.getItem()).stream()
		);
		NonNullList<ItemStack> nonNullList = NonNullList.create();

		for (Item item : Registry.ITEM) {
			item.fillItemCategory(CreativeModeTab.TAB_SEARCH, nonNullList);
		}

		nonNullList.forEach(itemStack -> {
			reloadableSearchTree.add(itemStack);
			reloadableIdSearchTree.add(itemStack);
		});
		ReloadableSearchTree<RecipeCollection> reloadableSearchTree2 = new ReloadableSearchTree<>(
			recipeCollection -> recipeCollection.getRecipes()
					.stream()
					.flatMap(recipe -> recipe.getResultItem().getTooltipLines(null, TooltipFlag.Default.NORMAL).stream())
					.map(component -> ChatFormatting.stripFormatting(component.getString()).trim())
					.filter(string -> !string.isEmpty()),
			recipeCollection -> recipeCollection.getRecipes().stream().map(recipe -> Registry.ITEM.getKey(recipe.getResultItem().getItem()))
		);
		this.searchRegistry.register(SearchRegistry.CREATIVE_NAMES, reloadableSearchTree);
		this.searchRegistry.register(SearchRegistry.CREATIVE_TAGS, reloadableIdSearchTree);
		this.searchRegistry.register(SearchRegistry.RECIPE_COLLECTIONS, reloadableSearchTree2);
	}

	private void onFullscreenError(int i, long l) {
		this.options.enableVsync = false;
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
		this.delayedCrash = crashReport;
	}

	public static void crash(CrashReport crashReport) {
		File file = new File(getInstance().gameDirectory, "crash-reports");
		File file2 = new File(file, "crash-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + "-client.txt");
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
		return this.options.forceUnicodeFont;
	}

	public CompletableFuture<Void> reloadResourcePacks() {
		if (this.pendingReload != null) {
			return this.pendingReload;
		} else {
			CompletableFuture<Void> completableFuture = new CompletableFuture();
			if (this.overlay instanceof LoadingOverlay) {
				this.pendingReload = completableFuture;
				return completableFuture;
			} else {
				this.resourcePackRepository.reload();
				List<PackResources> list = this.resourcePackRepository.openAllSelected();
				this.setOverlay(
					new LoadingOverlay(
						this,
						this.resourceManager.createFullReload(Util.backgroundExecutor(), this, RESOURCE_RELOAD_INITIAL_TASK, list),
						optional -> Util.ifElse(optional, this::rollbackResourcePacks, () -> {
								this.levelRenderer.allChanged();
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

		for (Block block : Registry.BLOCK) {
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

		for (Block block2 : Registry.BLOCK) {
			for (BlockState blockState2 : block2.getStateDefinition().getPossibleStates()) {
				TextureAtlasSprite textureAtlasSprite2 = blockModelShaper.getParticleIcon(blockState2);
				if (!blockState2.isAir() && textureAtlasSprite2 == textureAtlasSprite) {
					LOGGER.debug("Missing particle icon for: {}", blockState2);
					bl = true;
				}
			}
		}

		NonNullList<ItemStack> nonNullList = NonNullList.create();

		for (Item item : Registry.ITEM) {
			nonNullList.clear();
			item.fillItemCategory(CreativeModeTab.TAB_SEARCH, nonNullList);

			for (ItemStack itemStack : nonNullList) {
				String string = itemStack.getDescriptionId();
				String string2 = new TranslatableComponent(string).getString();
				if (string2.toLowerCase(Locale.ROOT).equals(item.getDescriptionId())) {
					LOGGER.debug("Missing translation for: {} {} {}", itemStack, string, itemStack.getItem());
				}
			}
		}

		bl |= MenuScreens.selfTest();
		if (bl) {
			throw new IllegalStateException("Your game data is foobar, fix the errors above!");
		}
	}

	public LevelStorageSource getLevelSource() {
		return this.levelSource;
	}

	private void openChatScreen(String string) {
		if (this.isLocalServer() || this.allowsChat()) {
			this.setScreen(new ChatScreen(string));
		} else if (this.player != null) {
			this.player.sendMessage(new TranslatableComponent("chat.cannotSend").withStyle(ChatFormatting.RED), Util.NIL_UUID);
		}
	}

	public void setScreen(@Nullable Screen screen) {
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

		if (screen instanceof TitleScreen || screen instanceof JoinMultiplayerScreen) {
			this.options.renderDebug = false;
			this.gui.getChat().clearMessages(true);
		}

		this.screen = screen;
		if (screen != null) {
			this.mouseHandler.releaseMouse();
			KeyMapping.releaseAll();
			screen.init(this, this.window.getGuiScaledWidth(), this.window.getGuiScaledHeight());
			this.noRender = false;
			NarratorChatListener.INSTANCE.sayNow(screen.getNarrationMessage());
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
				NarratorChatListener.INSTANCE.destroy();
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
		try {
			this.modelManager.close();
			this.fontManager.close();
			this.gameRenderer.close();
			this.levelRenderer.close();
			this.soundManager.destroy();
			this.resourcePackRepository.close();
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
		RenderSystem.pushMatrix();
		RenderSystem.clear(16640, ON_OSX);
		this.mainRenderTarget.bindWrite(true);
		FogRenderer.setupNoFog();
		this.profiler.push("display");
		RenderSystem.enableTexture();
		RenderSystem.enableCull();
		this.profiler.pop();
		if (!this.noRender) {
			this.profiler.popPush("gameRenderer");
			this.gameRenderer.render(this.pause ? this.pausePartialTick : this.timer.partialTick, l, bl);
			this.profiler.popPush("toasts");
			this.toast.render(new PoseStack());
			this.profiler.pop();
		}

		if (this.fpsPieResults != null) {
			this.profiler.push("fpsPie");
			this.renderFpsMeter(new PoseStack(), this.fpsPieResults);
			this.profiler.pop();
		}

		this.profiler.push("blit");
		this.mainRenderTarget.unbindWrite();
		RenderSystem.popMatrix();
		RenderSystem.pushMatrix();
		this.mainRenderTarget.blitToScreen(this.window.getWidth(), this.window.getHeight());
		RenderSystem.popMatrix();
		this.profiler.popPush("updateDisplay");
		this.window.updateDisplay();
		int i = this.getFramerateLimit();
		if ((double)i < Option.FRAMERATE_LIMIT.getMaxValue()) {
			RenderSystem.limitDisplayFPS(i);
		}

		this.profiler.popPush("yield");
		Thread.yield();
		this.profiler.pop();
		this.window.setErrorSection("Post render");
		this.frames++;
		boolean bl2 = this.hasSingleplayerServer()
			&& (this.screen != null && this.screen.isPauseScreen() || this.overlay != null && this.overlay.isPauseScreen())
			&& !this.singleplayerServer.isPublished();
		if (this.pause != bl2) {
			if (this.pause) {
				this.pausePartialTick = this.timer.partialTick;
			} else {
				this.timer.partialTick = this.pausePartialTick;
			}

			this.pause = bl2;
		}

		long m = Util.getNanos();
		this.frameTimer.logFrameDuration(m - this.lastNanoTime);
		this.lastNanoTime = m;
		this.profiler.push("fpsUpdate");

		while (Util.getMillis() >= this.lastTime + 1000L) {
			fps = this.frames;
			this.fpsString = String.format(
				"%d fps T: %s%s%s%s B: %d",
				fps,
				(double)this.options.framerateLimit == Option.FRAMERATE_LIMIT.getMaxValue() ? "inf" : this.options.framerateLimit,
				this.options.enableVsync ? " vsync" : "",
				this.options.graphicsMode.toString(),
				this.options.renderClouds == CloudStatus.OFF ? "" : (this.options.renderClouds == CloudStatus.FAST ? " fast-clouds" : " fancy-clouds"),
				this.options.biomeBlendRadius
			);
			this.lastTime += 1000L;
			this.frames = 0;
			this.snooper.prepare();
			if (!this.snooper.isStarted()) {
				this.snooper.start();
			}
		}

		this.profiler.pop();
	}

	private boolean shouldRenderFpsPie() {
		return this.options.renderDebug && this.options.renderDebugCharts && !this.options.hideGui;
	}

	private void startProfilers(boolean bl, @Nullable SingleTickProfiler singleTickProfiler) {
		if (bl) {
			if (!this.fpsPieProfiler.isEnabled()) {
				this.fpsPieRenderTicks = 0;
				this.fpsPieProfiler.enable();
			}

			this.fpsPieRenderTicks++;
		} else {
			this.fpsPieProfiler.disable();
		}

		this.profiler = SingleTickProfiler.decorateFiller(this.fpsPieProfiler.getFiller(), singleTickProfiler);
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
		int i = this.window.calculateScale(this.options.guiScale, this.isEnforceUnicode());
		this.window.setGuiScale((double)i);
		if (this.screen != null) {
			this.screen.resize(this, this.window.getGuiScaledWidth(), this.window.getGuiScaledHeight());
		}

		RenderTarget renderTarget = this.getMainRenderTarget();
		renderTarget.resize(this.window.getWidth(), this.window.getHeight(), ON_OSX);
		this.gameRenderer.resize(this.window.getWidth(), this.window.getHeight());
		this.mouseHandler.setIgnoreFirstMove();
	}

	private int getFramerateLimit() {
		return this.level != null || this.screen == null && this.overlay == null ? this.window.getFramerateLimit() : 60;
	}

	public void emergencySave() {
		try {
			reserve = new byte[0];
			this.levelRenderer.clear();
		} catch (Throwable var3) {
		}

		try {
			System.gc();
			if (this.isLocalServer && this.singleplayerServer != null) {
				this.singleplayerServer.halt(true);
			}

			this.clearLevel(new GenericDirtMessageScreen(new TranslatableComponent("menu.savingLevel")));
		} catch (Throwable var2) {
		}

		System.gc();
	}

	void debugFpsMeterKeyPress(int i) {
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
							this.debugPath = this.debugPath + '\u001e';
						}

						this.debugPath = this.debugPath + ((ResultField)list.get(i)).name;
					}
				}
			}
		}
	}

	private void renderFpsMeter(PoseStack poseStack, ProfileResults profileResults) {
		List<ResultField> list = profileResults.getTimes(this.debugPath);
		ResultField resultField = (ResultField)list.remove(0);
		RenderSystem.clear(256, ON_OSX);
		RenderSystem.matrixMode(5889);
		RenderSystem.loadIdentity();
		RenderSystem.ortho(0.0, (double)this.window.getWidth(), (double)this.window.getHeight(), 0.0, 1000.0, 3000.0);
		RenderSystem.matrixMode(5888);
		RenderSystem.loadIdentity();
		RenderSystem.translatef(0.0F, 0.0F, -2000.0F);
		RenderSystem.lineWidth(1.0F);
		RenderSystem.disableTexture();
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		int i = 160;
		int j = this.window.getWidth() - 160 - 10;
		int k = this.window.getHeight() - 320;
		RenderSystem.enableBlend();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
		bufferBuilder.vertex((double)((float)j - 176.0F), (double)((float)k - 96.0F - 16.0F), 0.0).color(200, 0, 0, 0).endVertex();
		bufferBuilder.vertex((double)((float)j - 176.0F), (double)(k + 320), 0.0).color(200, 0, 0, 0).endVertex();
		bufferBuilder.vertex((double)((float)j + 176.0F), (double)(k + 320), 0.0).color(200, 0, 0, 0).endVertex();
		bufferBuilder.vertex((double)((float)j + 176.0F), (double)((float)k - 96.0F - 16.0F), 0.0).color(200, 0, 0, 0).endVertex();
		tesselator.end();
		RenderSystem.disableBlend();
		double d = 0.0;

		for (ResultField resultField2 : list) {
			int l = Mth.floor(resultField2.percentage / 4.0) + 1;
			bufferBuilder.begin(6, DefaultVertexFormat.POSITION_COLOR);
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
			bufferBuilder.begin(5, DefaultVertexFormat.POSITION_COLOR);

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
		RenderSystem.enableTexture();
		String string = ProfileResults.demanglePath(resultField.name);
		String string2 = "";
		if (!"unspecified".equals(string)) {
			string2 = string2 + "[0] ";
		}

		if (string.isEmpty()) {
			string2 = string2 + "ROOT ";
		} else {
			string2 = string2 + string + ' ';
		}

		int m = 16777215;
		this.font.drawShadow(poseStack, string2, (float)(j - 160), (float)(k - 80 - 16), 16777215);
		string2 = decimalFormat.format(resultField.globalPercentage) + "%";
		this.font.drawShadow(poseStack, string2, (float)(j + 160 - this.font.width(string2)), (float)(k - 80 - 16), 16777215);

		for (int r = 0; r < list.size(); r++) {
			ResultField resultField3 = (ResultField)list.get(r);
			StringBuilder stringBuilder = new StringBuilder();
			if ("unspecified".equals(resultField3.name)) {
				stringBuilder.append("[?] ");
			} else {
				stringBuilder.append("[").append(r + 1).append("] ");
			}

			String string3 = stringBuilder.append(resultField3.name).toString();
			this.font.drawShadow(poseStack, string3, (float)(j - 160), (float)(k + 80 + r * 8 + 20), resultField3.getColor());
			string3 = decimalFormat.format(resultField3.percentage) + "%";
			this.font.drawShadow(poseStack, string3, (float)(j + 160 - 50 - this.font.width(string3)), (float)(k + 80 + r * 8 + 20), resultField3.getColor());
			string3 = decimalFormat.format(resultField3.globalPercentage) + "%";
			this.font.drawShadow(poseStack, string3, (float)(j + 160 - this.font.width(string3)), (float)(k + 80 + r * 8 + 20), resultField3.getColor());
		}
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

	private void startAttack() {
		if (this.missTime <= 0) {
			if (this.hitResult == null) {
				LOGGER.error("Null returned as 'hitResult', this shouldn't happen!");
				if (this.gameMode.hasMissTime()) {
					this.missTime = 10;
				}
			} else if (!this.player.isHandsBusy()) {
				switch (this.hitResult.getType()) {
					case ENTITY:
						this.gameMode.attack(this.player, ((EntityHitResult)this.hitResult).getEntity());
						break;
					case BLOCK:
						BlockHitResult blockHitResult = (BlockHitResult)this.hitResult;
						BlockPos blockPos = blockHitResult.getBlockPos();
						if (!this.level.getBlockState(blockPos).isAir()) {
							this.gameMode.startDestroyBlock(blockPos, blockHitResult.getDirection());
							break;
						}
					case MISS:
						if (this.gameMode.hasMissTime()) {
							this.missTime = 10;
						}

						this.player.resetAttackStrengthTicker();
				}

				this.player.swing(InteractionHand.MAIN_HAND);
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
					if (this.hitResult != null) {
						switch (this.hitResult.getType()) {
							case ENTITY:
								EntityHitResult entityHitResult = (EntityHitResult)this.hitResult;
								Entity entity = entityHitResult.getEntity();
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
								InteractionResult interactionResult2 = this.gameMode.useItemOn(this.player, this.level, interactionHand, blockHitResult);
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
						InteractionResult interactionResult3 = this.gameMode.useItem(this.player, this.level, interactionHand);
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
		if (!this.pause) {
			this.gui.tick();
		}

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

		if (this.screen == null && this.player != null) {
			if (this.player.isDeadOrDying() && !(this.screen instanceof DeathScreen)) {
				this.setScreen(null);
			} else if (this.player.isSleeping() && this.level != null) {
				this.setScreen(new InBedChatScreen());
			}
		} else if (this.screen != null && this.screen instanceof InBedChatScreen && !this.player.isSleeping()) {
			this.setScreen(null);
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

		if (this.overlay == null && (this.screen == null || this.screen.passEvents)) {
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
				if (this.level.getSkyFlashTime() > 0) {
					this.level.setSkyFlashTime(this.level.getSkyFlashTime() - 1);
				}

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
				this.level.animateTick(Mth.floor(this.player.getX()), Mth.floor(this.player.getY()), Mth.floor(this.player.getZ()));
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

	private void handleKeybinds() {
		while (this.options.keyTogglePerspective.consumeClick()) {
			this.options.thirdPersonView++;
			if (this.options.thirdPersonView > 2) {
				this.options.thirdPersonView = 0;
			}

			if (this.options.thirdPersonView == 0) {
				this.gameRenderer.checkEntityPostEffect(this.getCameraEntity());
			} else if (this.options.thirdPersonView == 1) {
				this.gameRenderer.checkEntityPostEffect(null);
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
					this.player.inventory.selected = i;
				} else {
					CreativeModeInventoryScreen.handleHotbarLoadOrSave(this, i, bl2, bl);
				}
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

		while (this.options.keySwapHands.consumeClick()) {
			if (!this.player.isSpectator()) {
				this.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_HELD_ITEMS, BlockPos.ZERO, Direction.DOWN));
			}
		}

		while (this.options.keyDrop.consumeClick()) {
			if (!this.player.isSpectator() && this.player.drop(Screen.hasControlDown())) {
				this.player.swing(InteractionHand.MAIN_HAND);
			}
		}

		boolean bl3 = this.options.chatVisibility != ChatVisiblity.HIDDEN;
		if (bl3) {
			while (this.options.keyChat.consumeClick()) {
				this.openChatScreen("");
			}

			if (this.screen == null && this.overlay == null && this.options.keyCommand.consumeClick()) {
				this.openChatScreen("/");
			}
		}

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
				this.startAttack();
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

		this.continueAttack(this.screen == null && this.options.keyAttack.isDown() && this.mouseHandler.isMouseGrabbed());
	}

	public static DataPackConfig loadDataPacks(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
		MinecraftServer.convertFromRegionFormatIfNeeded(levelStorageAccess);
		DataPackConfig dataPackConfig = levelStorageAccess.getDataPacks();
		if (dataPackConfig == null) {
			throw new IllegalStateException("Failed to load data pack config");
		} else {
			return dataPackConfig;
		}
	}

	public static WorldData loadWorldData(
		LevelStorageSource.LevelStorageAccess levelStorageAccess,
		RegistryAccess.RegistryHolder registryHolder,
		ResourceManager resourceManager,
		DataPackConfig dataPackConfig
	) {
		RegistryReadOps<Tag> registryReadOps = RegistryReadOps.create(NbtOps.INSTANCE, resourceManager, registryHolder);
		WorldData worldData = levelStorageAccess.getDataTag(registryReadOps, dataPackConfig);
		if (worldData == null) {
			throw new IllegalStateException("Failed to load world");
		} else {
			return worldData;
		}
	}

	public void loadLevel(String string) {
		this.doLoadLevel(string, RegistryAccess.builtin(), Minecraft::loadDataPacks, Minecraft::loadWorldData, false, Minecraft.ExperimentalDialogType.BACKUP);
	}

	public void createLevel(String string, LevelSettings levelSettings, RegistryAccess.RegistryHolder registryHolder, WorldGenSettings worldGenSettings) {
		this.doLoadLevel(
			string,
			registryHolder,
			levelStorageAccess -> levelSettings.getDataPackConfig(),
			(levelStorageAccess, registryHolder2, resourceManager, dataPackConfig) -> {
				RegistryReadOps<JsonElement> registryReadOps = RegistryReadOps.create(JsonOps.INSTANCE, resourceManager, registryHolder);
				DataResult<MappedRegistry<LevelStem>> dataResult = registryReadOps.decodeElements(
					worldGenSettings.dimensions(), Registry.LEVEL_STEM_REGISTRY, LevelStem.CODEC
				);
				MappedRegistry<LevelStem> mappedRegistry = (MappedRegistry<LevelStem>)dataResult.resultOrPartial(LOGGER::error).orElse(worldGenSettings.dimensions());
				return new PrimaryLevelData(levelSettings, worldGenSettings.withDimensions(mappedRegistry), dataResult.lifecycle());
			},
			false,
			Minecraft.ExperimentalDialogType.CREATE
		);
	}

	private void doLoadLevel(
		String string,
		RegistryAccess.RegistryHolder registryHolder,
		Function<LevelStorageSource.LevelStorageAccess, DataPackConfig> function,
		Function4<LevelStorageSource.LevelStorageAccess, RegistryAccess.RegistryHolder, ResourceManager, DataPackConfig, WorldData> function4,
		boolean bl,
		Minecraft.ExperimentalDialogType experimentalDialogType
	) {
		LevelStorageSource.LevelStorageAccess levelStorageAccess;
		try {
			levelStorageAccess = this.levelSource.createAccess(string);
		} catch (IOException var21) {
			LOGGER.warn("Failed to read level {} data", string, var21);
			SystemToast.onWorldAccessFailure(this, string);
			this.setScreen(null);
			return;
		}

		Minecraft.ServerStem serverStem;
		try {
			serverStem = this.makeServerStem(registryHolder, function, function4, bl, levelStorageAccess);
		} catch (Exception var20) {
			LOGGER.warn("Failed to load datapacks, can't proceed with server load", (Throwable)var20);
			this.setScreen(new DatapackLoadFailureScreen(() -> this.doLoadLevel(string, registryHolder, function, function4, true, experimentalDialogType)));

			try {
				levelStorageAccess.close();
			} catch (IOException var16) {
				LOGGER.warn("Failed to unlock access to level {}", string, var16);
			}

			return;
		}

		WorldData worldData = serverStem.worldData();
		boolean bl2 = worldData.worldGenSettings().isOldCustomizedWorld();
		boolean bl3 = worldData.worldGenSettingsLifecycle() != Lifecycle.stable();
		if (experimentalDialogType == Minecraft.ExperimentalDialogType.NONE || !bl2 && !bl3) {
			this.clearLevel();
			this.progressListener.set(null);

			try {
				levelStorageAccess.saveDataTag(registryHolder, worldData);
				serverStem.serverResources().updateGlobals();
				YggdrasilAuthenticationService yggdrasilAuthenticationService = new YggdrasilAuthenticationService(this.proxy, UUID.randomUUID().toString());
				MinecraftSessionService minecraftSessionService = yggdrasilAuthenticationService.createMinecraftSessionService();
				GameProfileRepository gameProfileRepository = yggdrasilAuthenticationService.createProfileRepository();
				GameProfileCache gameProfileCache = new GameProfileCache(gameProfileRepository, new File(this.gameDirectory, MinecraftServer.USERID_CACHE_FILE.getName()));
				SkullBlockEntity.setProfileCache(gameProfileCache);
				SkullBlockEntity.setSessionService(minecraftSessionService);
				GameProfileCache.setUsesAuthentication(false);
				this.singleplayerServer = MinecraftServer.spin(
					thread -> new IntegratedServer(
							thread,
							this,
							registryHolder,
							levelStorageAccess,
							serverStem.packRepository(),
							serverStem.serverResources(),
							worldData,
							minecraftSessionService,
							gameProfileRepository,
							gameProfileCache,
							i -> {
								StoringChunkProgressListener storingChunkProgressListener = new StoringChunkProgressListener(i + 0);
								storingChunkProgressListener.start();
								this.progressListener.set(storingChunkProgressListener);
								return new ProcessorChunkProgressListener(storingChunkProgressListener, this.progressTasks::add);
							}
						)
				);
				this.isLocalServer = true;
			} catch (Throwable var19) {
				CrashReport crashReport = CrashReport.forThrowable(var19, "Starting integrated server");
				CrashReportCategory crashReportCategory = crashReport.addCategory("Starting integrated server");
				crashReportCategory.setDetail("Level ID", string);
				crashReportCategory.setDetail("Level Name", worldData.getLevelName());
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
				} catch (InterruptedException var18) {
				}

				if (this.delayedCrash != null) {
					crash(this.delayedCrash);
					return;
				}
			}

			this.profiler.pop();
			SocketAddress socketAddress = this.singleplayerServer.getConnection().startMemoryChannel();
			Connection connection = Connection.connectToLocalServer(socketAddress);
			connection.setListener(new ClientHandshakePacketListenerImpl(connection, this, null, component -> {
			}));
			connection.send(new ClientIntentionPacket(socketAddress.toString(), 0, ConnectionProtocol.LOGIN));
			connection.send(new ServerboundHelloPacket(this.getUser().getGameProfile()));
			this.pendingConnection = connection;
		} else {
			this.displayExperimentalConfirmationDialog(
				experimentalDialogType, string, bl2, () -> this.doLoadLevel(string, registryHolder, function, function4, bl, Minecraft.ExperimentalDialogType.NONE)
			);
			serverStem.close();

			try {
				levelStorageAccess.close();
			} catch (IOException var17) {
				LOGGER.warn("Failed to unlock access to level {}", string, var17);
			}
		}
	}

	private void displayExperimentalConfirmationDialog(Minecraft.ExperimentalDialogType experimentalDialogType, String string, boolean bl, Runnable runnable) {
		if (experimentalDialogType == Minecraft.ExperimentalDialogType.BACKUP) {
			Component component;
			Component component2;
			if (bl) {
				component = new TranslatableComponent("selectWorld.backupQuestion.customized");
				component2 = new TranslatableComponent("selectWorld.backupWarning.customized");
			} else {
				component = new TranslatableComponent("selectWorld.backupQuestion.experimental");
				component2 = new TranslatableComponent("selectWorld.backupWarning.experimental");
			}

			this.setScreen(new BackupConfirmScreen(null, (blx, bl2) -> {
				if (blx) {
					EditWorldScreen.makeBackupAndShowToast(this.levelSource, string);
				}

				runnable.run();
			}, component, component2, false));
		} else {
			this.setScreen(
				new ConfirmScreen(
					blx -> {
						if (blx) {
							runnable.run();
						} else {
							this.setScreen(null);
						}
					},
					new TranslatableComponent("selectWorld.backupQuestion.experimental"),
					new TranslatableComponent("selectWorld.backupWarning.experimental"),
					CommonComponents.GUI_PROCEED,
					CommonComponents.GUI_CANCEL
				)
			);
		}
	}

	public Minecraft.ServerStem makeServerStem(
		RegistryAccess.RegistryHolder registryHolder,
		Function<LevelStorageSource.LevelStorageAccess, DataPackConfig> function,
		Function4<LevelStorageSource.LevelStorageAccess, RegistryAccess.RegistryHolder, ResourceManager, DataPackConfig, WorldData> function4,
		boolean bl,
		LevelStorageSource.LevelStorageAccess levelStorageAccess
	) throws InterruptedException, ExecutionException {
		DataPackConfig dataPackConfig = (DataPackConfig)function.apply(levelStorageAccess);
		PackRepository<Pack> packRepository = new PackRepository<>(
			Pack::new, new ServerPacksSource(), new FolderRepositorySource(levelStorageAccess.getLevelPath(LevelResource.DATAPACK_DIR).toFile(), PackSource.WORLD)
		);

		try {
			DataPackConfig dataPackConfig2 = MinecraftServer.configurePackRepository(packRepository, dataPackConfig, bl);
			CompletableFuture<ServerResources> completableFuture = ServerResources.loadResources(
				packRepository.openAllSelected(), Commands.CommandSelection.INTEGRATED, 2, Util.backgroundExecutor(), this
			);
			this.managedBlock(completableFuture::isDone);
			ServerResources serverResources = (ServerResources)completableFuture.get();
			WorldData worldData = function4.apply(levelStorageAccess, registryHolder, serverResources.getResourceManager(), dataPackConfig2);
			return new Minecraft.ServerStem(packRepository, serverResources, worldData);
		} catch (ExecutionException | InterruptedException var12) {
			packRepository.close();
			throw var12;
		}
	}

	public void setLevel(ClientLevel clientLevel) {
		ProgressScreen progressScreen = new ProgressScreen();
		progressScreen.progressStartNoAbort(new TranslatableComponent("connect.joining"));
		this.updateScreenAndTick(progressScreen);
		this.level = clientLevel;
		this.updateLevelInEngines(clientLevel);
		if (!this.isLocalServer) {
			AuthenticationService authenticationService = new YggdrasilAuthenticationService(this.proxy, UUID.randomUUID().toString());
			MinecraftSessionService minecraftSessionService = authenticationService.createMinecraftSessionService();
			GameProfileRepository gameProfileRepository = authenticationService.createProfileRepository();
			GameProfileCache gameProfileCache = new GameProfileCache(gameProfileRepository, new File(this.gameDirectory, MinecraftServer.USERID_CACHE_FILE.getName()));
			SkullBlockEntity.setProfileCache(gameProfileCache);
			SkullBlockEntity.setSessionService(minecraftSessionService);
			GameProfileCache.setUsesAuthentication(false);
		}
	}

	public void clearLevel() {
		this.clearLevel(new ProgressScreen());
	}

	public void clearLevel(Screen screen) {
		ClientPacketListener clientPacketListener = this.getConnection();
		if (clientPacketListener != null) {
			this.dropAllTasks();
			clientPacketListener.cleanup();
		}

		IntegratedServer integratedServer = this.singleplayerServer;
		this.singleplayerServer = null;
		this.gameRenderer.resetData();
		this.gameMode = null;
		NarratorChatListener.INSTANCE.clear();
		this.updateScreenAndTick(screen);
		if (this.level != null) {
			if (integratedServer != null) {
				this.profiler.push("waitForServer");

				while (!integratedServer.isShutdown()) {
					this.runTick(false);
				}

				this.profiler.pop();
			}

			this.clientPackSource.clearServerPack();
			this.gui.onDisconnected();
			this.currentServer = null;
			this.isLocalServer = false;
			this.game.onLeaveGameSession();
		}

		this.level = null;
		this.updateLevelInEngines(null);
		this.player = null;
	}

	private void updateScreenAndTick(Screen screen) {
		this.profiler.push("forcedTick");
		this.musicManager.stopPlaying();
		this.soundManager.stop();
		this.cameraEntity = null;
		this.pendingConnection = null;
		this.setScreen(screen);
		this.runTick(false);
		this.profiler.pop();
	}

	private void updateLevelInEngines(@Nullable ClientLevel clientLevel) {
		this.levelRenderer.setLevel(clientLevel);
		this.particleEngine.setLevel(clientLevel);
		BlockEntityRenderDispatcher.instance.setLevel(clientLevel);
		this.updateTitle();
	}

	public boolean allowsMultiplayer() {
		return this.allowsMultiplayer;
	}

	public boolean isBlocked(UUID uUID) {
		return this.allowsChat() ? false : (this.player == null || !uUID.equals(this.player.getUUID())) && !uUID.equals(Util.NIL_UUID);
	}

	public boolean allowsChat() {
		return this.allowsChat;
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
		return instance.options.graphicsMode.getId() >= GraphicsStatus.FANCY.getId();
	}

	public static boolean useShaderTransparency() {
		return instance.options.graphicsMode.getId() >= GraphicsStatus.FABULOUS.getId();
	}

	public static boolean useAmbientOcclusion() {
		return instance.options.ambientOcclusion != AmbientOcclusionStatus.OFF;
	}

	private void pickBlock() {
		if (this.hitResult != null && this.hitResult.getType() != HitResult.Type.MISS) {
			boolean bl = this.player.abilities.instabuild;
			BlockEntity blockEntity = null;
			HitResult.Type type = this.hitResult.getType();
			ItemStack itemStack;
			if (type == HitResult.Type.BLOCK) {
				BlockPos blockPos = ((BlockHitResult)this.hitResult).getBlockPos();
				BlockState blockState = this.level.getBlockState(blockPos);
				Block block = blockState.getBlock();
				if (blockState.isAir()) {
					return;
				}

				itemStack = block.getCloneItemStack(this.level, blockPos, blockState);
				if (itemStack.isEmpty()) {
					return;
				}

				if (bl && Screen.hasControlDown() && block.isEntityBlock()) {
					blockEntity = this.level.getBlockEntity(blockPos);
				}
			} else {
				if (type != HitResult.Type.ENTITY || !bl) {
					return;
				}

				Entity entity = ((EntityHitResult)this.hitResult).getEntity();
				if (entity instanceof Painting) {
					itemStack = new ItemStack(Items.PAINTING);
				} else if (entity instanceof LeashFenceKnotEntity) {
					itemStack = new ItemStack(Items.LEAD);
				} else if (entity instanceof ItemFrame) {
					ItemFrame itemFrame = (ItemFrame)entity;
					ItemStack itemStack2 = itemFrame.getItem();
					if (itemStack2.isEmpty()) {
						itemStack = new ItemStack(Items.ITEM_FRAME);
					} else {
						itemStack = itemStack2.copy();
					}
				} else if (entity instanceof AbstractMinecart) {
					AbstractMinecart abstractMinecart = (AbstractMinecart)entity;
					Item item;
					switch (abstractMinecart.getMinecartType()) {
						case FURNACE:
							item = Items.FURNACE_MINECART;
							break;
						case CHEST:
							item = Items.CHEST_MINECART;
							break;
						case TNT:
							item = Items.TNT_MINECART;
							break;
						case HOPPER:
							item = Items.HOPPER_MINECART;
							break;
						case COMMAND_BLOCK:
							item = Items.COMMAND_BLOCK_MINECART;
							break;
						default:
							item = Items.MINECART;
					}

					itemStack = new ItemStack(item);
				} else if (entity instanceof Boat) {
					itemStack = new ItemStack(((Boat)entity).getDropItem());
				} else if (entity instanceof ArmorStand) {
					itemStack = new ItemStack(Items.ARMOR_STAND);
				} else if (entity instanceof EndCrystal) {
					itemStack = new ItemStack(Items.END_CRYSTAL);
				} else {
					SpawnEggItem spawnEggItem = SpawnEggItem.byId(entity.getType());
					if (spawnEggItem == null) {
						return;
					}

					itemStack = new ItemStack(spawnEggItem);
				}
			}

			if (itemStack.isEmpty()) {
				String string = "";
				if (type == HitResult.Type.BLOCK) {
					string = Registry.BLOCK.getKey(this.level.getBlockState(((BlockHitResult)this.hitResult).getBlockPos()).getBlock()).toString();
				} else if (type == HitResult.Type.ENTITY) {
					string = Registry.ENTITY_TYPE.getKey(((EntityHitResult)this.hitResult).getEntity().getType()).toString();
				}

				LOGGER.warn("Picking on: [{}] {} gave null item", type, string);
			} else {
				Inventory inventory = this.player.inventory;
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

	private ItemStack addCustomNbtData(ItemStack itemStack, BlockEntity blockEntity) {
		CompoundTag compoundTag = blockEntity.save(new CompoundTag());
		if (itemStack.getItem() instanceof PlayerHeadItem && compoundTag.contains("SkullOwner")) {
			CompoundTag compoundTag2 = compoundTag.getCompound("SkullOwner");
			itemStack.getOrCreateTag().put("SkullOwner", compoundTag2);
			return itemStack;
		} else {
			itemStack.addTagElement("BlockEntityTag", compoundTag);
			CompoundTag compoundTag2 = new CompoundTag();
			ListTag listTag = new ListTag();
			listTag.add(StringTag.valueOf("\"(+NBT)\""));
			compoundTag2.put("Lore", listTag);
			itemStack.addTagElement("display", compoundTag2);
			return itemStack;
		}
	}

	public CrashReport fillReport(CrashReport crashReport) {
		fillReport(this.languageManager, this.launchedVersion, this.options, crashReport);
		if (this.level != null) {
			this.level.fillReportDetails(crashReport);
		}

		return crashReport;
	}

	public static void fillReport(@Nullable LanguageManager languageManager, String string, @Nullable Options options, CrashReport crashReport) {
		CrashReportCategory crashReportCategory = crashReport.getSystemDetails();
		crashReportCategory.setDetail("Launched Version", (CrashReportDetail<String>)(() -> string));
		crashReportCategory.setDetail("Backend library", RenderSystem::getBackendDescription);
		crashReportCategory.setDetail("Backend API", RenderSystem::getApiDescription);
		crashReportCategory.setDetail("GL Caps", RenderSystem::getCapsString);
		crashReportCategory.setDetail("Using VBOs", (CrashReportDetail<String>)(() -> "Yes"));
		crashReportCategory.setDetail(
			"Is Modded",
			(CrashReportDetail<String>)(() -> {
				String stringx = ClientBrandRetriever.getClientModName();
				if (!"vanilla".equals(stringx)) {
					return "Definitely; Client brand changed to '" + stringx + "'";
				} else {
					return Minecraft.class.getSigners() == null
						? "Very likely; Jar signature invalidated"
						: "Probably not. Jar signature remains and client brand is untouched.";
				}
			})
		);
		crashReportCategory.setDetail("Type", "Client (map_client.txt)");
		if (options != null) {
			crashReportCategory.setDetail("Resource Packs", (CrashReportDetail<String>)(() -> {
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
			crashReportCategory.setDetail("Current Language", (CrashReportDetail<String>)(() -> languageManager.getSelected().toString()));
		}

		crashReportCategory.setDetail("CPU", GlUtil::getCpuInfo);
	}

	public static Minecraft getInstance() {
		return instance;
	}

	public CompletableFuture<Void> delayTextureReload() {
		return this.submit(this::reloadResourcePacks).thenCompose(completableFuture -> completableFuture);
	}

	@Override
	public void populateSnooper(Snooper snooper) {
		snooper.setDynamicData("fps", fps);
		snooper.setDynamicData("vsync_enabled", this.options.enableVsync);
		snooper.setDynamicData("display_frequency", this.window.getRefreshRate());
		snooper.setDynamicData("display_type", this.window.isFullscreen() ? "fullscreen" : "windowed");
		snooper.setDynamicData("run_time", (Util.getMillis() - snooper.getStartupTime()) / 60L * 1000L);
		snooper.setDynamicData("current_action", this.getCurrentSnooperAction());
		snooper.setDynamicData("language", this.options.languageCode == null ? "en_us" : this.options.languageCode);
		String string = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? "little" : "big";
		snooper.setDynamicData("endianness", string);
		snooper.setDynamicData("subtitles", this.options.showSubtitles);
		snooper.setDynamicData("touch", this.options.touchscreen ? "touch" : "mouse");
		int i = 0;

		for (ResourcePack resourcePack : this.resourcePackRepository.getSelectedPacks()) {
			if (!resourcePack.isRequired() && !resourcePack.isFixedPosition()) {
				snooper.setDynamicData("resource_pack[" + i++ + "]", resourcePack.getId());
			}
		}

		snooper.setDynamicData("resource_packs", i);
		if (this.singleplayerServer != null) {
			snooper.setDynamicData("snooper_partner", this.singleplayerServer.getSnooper().getToken());
		}
	}

	private String getCurrentSnooperAction() {
		if (this.singleplayerServer != null) {
			return this.singleplayerServer.isPublished() ? "hosting_lan" : "singleplayer";
		} else if (this.currentServer != null) {
			return this.currentServer.isLan() ? "playing_lan" : "multiplayer";
		} else {
			return "out_of_game";
		}
	}

	public void setCurrentServer(@Nullable ServerData serverData) {
		this.currentServer = serverData;
	}

	@Nullable
	public ServerData getCurrentServer() {
		return this.currentServer;
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

	public Snooper getSnooper() {
		return this.snooper;
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

	public PackRepository<ResourcePack> getResourcePackRepository() {
		return this.resourcePackRepository;
	}

	public ClientPackSource getClientPackSource() {
		return this.clientPackSource;
	}

	public File getResourcePackDirectory() {
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

	public SoundManager getSoundManager() {
		return this.soundManager;
	}

	public Music getSituationalMusic() {
		if (this.screen instanceof WinScreen) {
			return Musics.CREDITS;
		} else if (this.player != null) {
			if (this.player.level.dimensionType().isEnd()) {
				return this.gui.getBossOverlay().shouldPlayMusic() ? Musics.END_BOSS : Musics.END;
			} else {
				Biome.BiomeCategory biomeCategory = this.player.level.getBiome(this.player.blockPosition()).getBiomeCategory();
				if (!this.musicManager.isPlayingMusic(Musics.UNDER_WATER)
					&& (!this.player.isUnderWater() || biomeCategory != Biome.BiomeCategory.OCEAN && biomeCategory != Biome.BiomeCategory.RIVER)) {
					return this.player.abilities.instabuild && this.player.abilities.mayfly
						? Musics.CREATIVE
						: (Music)this.level.getBiomeManager().getNoiseBiomeAtPosition(this.player.blockPosition()).getBackgroundMusic().orElse(Musics.GAME);
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
		return entity.isGlowing()
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

	public ItemRenderer getItemRenderer() {
		return this.itemRenderer;
	}

	public ItemInHandRenderer getItemInHandRenderer() {
		return this.itemInHandRenderer;
	}

	public <T> MutableSearchTree<T> getSearchTree(SearchRegistry.Key<T> key) {
		return this.searchRegistry.getTree(key);
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
		return this.player != null && this.player.isReducedDebugInfo() || this.options.reducedDebugInfo;
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

	public ProfilerFiller getProfiler() {
		return this.profiler;
	}

	public Game getGame() {
		return this.game;
	}

	public SplashManager getSplashManager() {
		return this.splashManager;
	}

	@Nullable
	public Overlay getOverlay() {
		return this.overlay;
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

	private static ResourcePack createClientPackAdapter(
		String string,
		boolean bl,
		Supplier<PackResources> supplier,
		PackResources packResources,
		PackMetadataSection packMetadataSection,
		Pack.Position position,
		PackSource packSource
	) {
		int i = packMetadataSection.getPackFormat();
		Supplier<PackResources> supplier2 = supplier;
		if (i <= 3) {
			supplier2 = adaptV3(supplier);
		}

		if (i <= 4) {
			supplier2 = adaptV4(supplier2);
		}

		return new ResourcePack(string, bl, supplier2, packResources, packMetadataSection, position, packSource);
	}

	private static Supplier<PackResources> adaptV3(Supplier<PackResources> supplier) {
		return () -> new LegacyPackResourcesAdapter((PackResources)supplier.get(), LegacyPackResourcesAdapter.V3);
	}

	private static Supplier<PackResources> adaptV4(Supplier<PackResources> supplier) {
		return () -> new PackResourcesAdapterV4((PackResources)supplier.get());
	}

	public void updateMaxMipLevel(int i) {
		this.modelManager.updateMaxMipLevel(i);
	}

	@Environment(EnvType.CLIENT)
	static enum ExperimentalDialogType {
		NONE,
		CREATE,
		BACKUP;
	}

	@Environment(EnvType.CLIENT)
	public static final class ServerStem implements AutoCloseable {
		private final PackRepository<Pack> packRepository;
		private final ServerResources serverResources;
		private final WorldData worldData;

		private ServerStem(PackRepository<Pack> packRepository, ServerResources serverResources, WorldData worldData) {
			this.packRepository = packRepository;
			this.serverResources = serverResources;
			this.worldData = worldData;
		}

		public PackRepository<Pack> packRepository() {
			return this.packRepository;
		}

		public ServerResources serverResources() {
			return this.serverResources;
		}

		public WorldData worldData() {
			return this.worldData;
		}

		public void close() {
			this.packRepository.close();
			this.serverResources.close();
		}
	}
}
