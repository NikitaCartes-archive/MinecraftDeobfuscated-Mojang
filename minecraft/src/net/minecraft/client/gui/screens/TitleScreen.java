package net.minecraft.client.gui.screens;

import com.mojang.authlib.minecraft.BanDetails;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommonButtons;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.options.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class TitleScreen extends Screen {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Component TITLE = Component.translatable("narrator.screen.title");
	private static final Component COPYRIGHT_TEXT = Component.translatable("title.credits");
	private static final String DEMO_LEVEL_ID = "Demo_World";
	private static final float FADE_IN_TIME = 2000.0F;
	@Nullable
	private SplashRenderer splash;
	private Button resetDemoButton;
	@Nullable
	private RealmsNotificationsScreen realmsNotificationsScreen;
	private float panoramaFade = 1.0F;
	private boolean fading;
	private long fadeInStart;
	private final LogoRenderer logoRenderer;

	public TitleScreen() {
		this(false);
	}

	public TitleScreen(boolean bl) {
		this(bl, null);
	}

	public TitleScreen(boolean bl, @Nullable LogoRenderer logoRenderer) {
		super(TITLE);
		this.fading = bl;
		this.logoRenderer = (LogoRenderer)Objects.requireNonNullElseGet(logoRenderer, () -> new LogoRenderer(false));
	}

	private boolean realmsNotificationsEnabled() {
		return this.realmsNotificationsScreen != null;
	}

	@Override
	public void tick() {
		if (this.realmsNotificationsEnabled()) {
			this.realmsNotificationsScreen.tick();
		}
	}

	public static CompletableFuture<Void> preloadResources(TextureManager textureManager, Executor executor) {
		return CompletableFuture.allOf(
			textureManager.preload(LogoRenderer.MINECRAFT_LOGO, executor),
			textureManager.preload(LogoRenderer.MINECRAFT_EDITION, executor),
			textureManager.preload(PanoramaRenderer.PANORAMA_OVERLAY, executor),
			CUBE_MAP.preload(textureManager, executor)
		);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	protected void init() {
		if (this.splash == null) {
			this.splash = this.minecraft.getSplashManager().getSplash();
		}

		int i = this.font.width(COPYRIGHT_TEXT);
		int j = this.width - i - 2;
		int k = 24;
		int l = this.height / 4 + 48;
		if (this.minecraft.isDemo()) {
			l = this.createDemoMenuOptions(l, 24);
		} else {
			l = this.createNormalMenuOptions(l, 24);
		}

		l = this.createTestWorldButton(l, 24);
		SpriteIconButton spriteIconButton = this.addRenderableWidget(
			CommonButtons.language(
				20, button -> this.minecraft.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager())), true
			)
		);
		int var10001 = this.width / 2 - 124;
		l += 36;
		spriteIconButton.setPosition(var10001, l);
		this.addRenderableWidget(
			Button.builder(Component.translatable("menu.options"), button -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options)))
				.bounds(this.width / 2 - 100, l, 98, 20)
				.build()
		);
		this.addRenderableWidget(Button.builder(Component.translatable("menu.quit"), button -> this.minecraft.stop()).bounds(this.width / 2 + 2, l, 98, 20).build());
		SpriteIconButton spriteIconButton2 = this.addRenderableWidget(
			CommonButtons.accessibility(20, button -> this.minecraft.setScreen(new AccessibilityOptionsScreen(this, this.minecraft.options)), true)
		);
		spriteIconButton2.setPosition(this.width / 2 + 104, l);
		this.addRenderableWidget(
			new PlainTextButton(j, this.height - 10, i, 10, COPYRIGHT_TEXT, button -> this.minecraft.setScreen(new CreditsAndAttributionScreen(this)), this.font)
		);
		if (this.realmsNotificationsScreen == null) {
			this.realmsNotificationsScreen = new RealmsNotificationsScreen();
		}

		if (this.realmsNotificationsEnabled()) {
			this.realmsNotificationsScreen.init(this.minecraft, this.width, this.height);
		}
	}

	private int createTestWorldButton(int i, int j) {
		if (SharedConstants.IS_RUNNING_IN_IDE) {
			this.addRenderableWidget(
				Button.builder(Component.literal("Create Test World"), button -> CreateWorldScreen.testWorld(this.minecraft, this))
					.bounds(this.width / 2 - 100, i += j, 200, 20)
					.build()
			);
		}

		return i;
	}

	private int createNormalMenuOptions(int i, int j) {
		this.addRenderableWidget(
			Button.builder(Component.translatable("menu.singleplayer"), button -> this.minecraft.setScreen(new SelectWorldScreen(this)))
				.bounds(this.width / 2 - 100, i, 200, 20)
				.build()
		);
		Component component = this.getMultiplayerDisabledReason();
		boolean bl = component == null;
		Tooltip tooltip = component != null ? Tooltip.create(component) : null;
		int var6;
		this.addRenderableWidget(Button.builder(Component.translatable("menu.multiplayer"), button -> {
			Screen screen = (Screen)(this.minecraft.options.skipMultiplayerWarning ? new JoinMultiplayerScreen(this) : new SafetyScreen(this));
			this.minecraft.setScreen(screen);
		}).bounds(this.width / 2 - 100, var6 = i + j, 200, 20).tooltip(tooltip).build()).active = bl;
		this.addRenderableWidget(
				Button.builder(Component.translatable("menu.online"), button -> this.minecraft.setScreen(new RealmsMainScreen(this)))
					.bounds(this.width / 2 - 100, i = var6 + j, 200, 20)
					.tooltip(tooltip)
					.build()
			)
			.active = bl;
		return i;
	}

	@Nullable
	private Component getMultiplayerDisabledReason() {
		if (this.minecraft.allowsMultiplayer()) {
			return null;
		} else if (this.minecraft.isNameBanned()) {
			return Component.translatable("title.multiplayer.disabled.banned.name");
		} else {
			BanDetails banDetails = this.minecraft.multiplayerBan();
			if (banDetails != null) {
				return banDetails.expires() != null
					? Component.translatable("title.multiplayer.disabled.banned.temporary")
					: Component.translatable("title.multiplayer.disabled.banned.permanent");
			} else {
				return Component.translatable("title.multiplayer.disabled");
			}
		}
	}

	private int createDemoMenuOptions(int i, int j) {
		boolean bl = this.checkDemoWorldPresence();
		this.addRenderableWidget(
			Button.builder(
					Component.translatable("menu.playdemo"),
					button -> {
						if (bl) {
							this.minecraft.createWorldOpenFlows().openWorld("Demo_World", () -> this.minecraft.setScreen(this));
						} else {
							this.minecraft
								.createWorldOpenFlows()
								.createFreshLevel("Demo_World", MinecraftServer.DEMO_SETTINGS, WorldOptions.DEMO_OPTIONS, WorldPresets::createNormalWorldDimensions, this);
						}
					}
				)
				.bounds(this.width / 2 - 100, i, 200, 20)
				.build()
		);
		int var4;
		this.resetDemoButton = this.addRenderableWidget(
			Button.builder(
					Component.translatable("menu.resetdemo"),
					button -> {
						LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();

						try (LevelStorageSource.LevelStorageAccess levelStorageAccess = levelStorageSource.createAccess("Demo_World")) {
							if (levelStorageAccess.hasWorldData()) {
								this.minecraft
									.setScreen(
										new ConfirmScreen(
											this::confirmDemo,
											Component.translatable("selectWorld.deleteQuestion"),
											Component.translatable("selectWorld.deleteWarning", MinecraftServer.DEMO_SETTINGS.levelName()),
											Component.translatable("selectWorld.deleteButton"),
											CommonComponents.GUI_CANCEL
										)
									);
							}
						} catch (IOException var8) {
							SystemToast.onWorldAccessFailure(this.minecraft, "Demo_World");
							LOGGER.warn("Failed to access demo world", (Throwable)var8);
						}
					}
				)
				.bounds(this.width / 2 - 100, var4 = i + j, 200, 20)
				.build()
		);
		this.resetDemoButton.active = bl;
		return var4;
	}

	private boolean checkDemoWorldPresence() {
		try {
			boolean var2;
			try (LevelStorageSource.LevelStorageAccess levelStorageAccess = this.minecraft.getLevelSource().createAccess("Demo_World")) {
				var2 = levelStorageAccess.hasWorldData();
			}

			return var2;
		} catch (IOException var6) {
			SystemToast.onWorldAccessFailure(this.minecraft, "Demo_World");
			LOGGER.warn("Failed to read demo world data", (Throwable)var6);
			return false;
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		if (this.fadeInStart == 0L && this.fading) {
			this.fadeInStart = Util.getMillis();
		}

		float g = 1.0F;
		if (this.fading) {
			float h = (float)(Util.getMillis() - this.fadeInStart) / 2000.0F;
			if (h > 1.0F) {
				this.fading = false;
				this.panoramaFade = 1.0F;
			} else {
				h = Mth.clamp(h, 0.0F, 1.0F);
				g = Mth.clampedMap(h, 0.5F, 1.0F, 0.0F, 1.0F);
				this.panoramaFade = Mth.clampedMap(h, 0.0F, 0.5F, 0.0F, 1.0F);
			}

			this.fadeWidgets(g);
		}

		this.renderPanorama(guiGraphics, f);
		int k = Mth.ceil(g * 255.0F) << 24;
		if ((k & -67108864) != 0) {
			super.render(guiGraphics, i, j, f);
			this.logoRenderer.renderLogo(guiGraphics, this.width, g);
			if (this.splash != null && !this.minecraft.options.hideSplashTexts().get()) {
				this.splash.render(guiGraphics, this.width, this.font, k);
			}

			String string = "Minecraft " + SharedConstants.getCurrentVersion().getName();
			if (this.minecraft.isDemo()) {
				string = string + " Demo";
			} else {
				string = string + ("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType());
			}

			if (Minecraft.checkModStatus().shouldReportAsModified()) {
				string = string + I18n.get("menu.modded");
			}

			guiGraphics.drawString(this.font, string, 2, this.height - 10, 16777215 | k);
			if (this.realmsNotificationsEnabled() && g >= 1.0F) {
				this.realmsNotificationsScreen.render(guiGraphics, i, j, f);
			}
		}
	}

	private void fadeWidgets(float f) {
		for (GuiEventListener guiEventListener : this.children()) {
			if (guiEventListener instanceof AbstractWidget abstractWidget) {
				abstractWidget.setAlpha(f);
			}
		}
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
	}

	@Override
	protected void renderPanorama(GuiGraphics guiGraphics, float f) {
		PANORAMA.render(guiGraphics, this.width, this.height, this.panoramaFade, f);
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		return super.mouseClicked(d, e, i) ? true : this.realmsNotificationsEnabled() && this.realmsNotificationsScreen.mouseClicked(d, e, i);
	}

	@Override
	public void removed() {
		if (this.realmsNotificationsScreen != null) {
			this.realmsNotificationsScreen.removed();
		}
	}

	@Override
	public void added() {
		super.added();
		if (this.realmsNotificationsScreen != null) {
			this.realmsNotificationsScreen.added();
		}
	}

	private void confirmDemo(boolean bl) {
		if (bl) {
			try (LevelStorageSource.LevelStorageAccess levelStorageAccess = this.minecraft.getLevelSource().createAccess("Demo_World")) {
				levelStorageAccess.deleteLevel();
			} catch (IOException var7) {
				SystemToast.onWorldDeleteFailure(this.minecraft, "Demo_World");
				LOGGER.warn("Failed to delete demo world", (Throwable)var7);
			}
		}

		this.minecraft.setScreen(this);
	}
}
