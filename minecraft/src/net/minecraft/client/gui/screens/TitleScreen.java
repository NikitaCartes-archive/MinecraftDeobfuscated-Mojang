package net.minecraft.client.gui.screens;

import com.mojang.authlib.minecraft.BanDetails;
import com.mojang.blaze3d.systems.RenderSystem;
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
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommonButtons;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class TitleScreen extends Screen {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String DEMO_LEVEL_ID = "Demo_World";
	public static final Component COPYRIGHT_TEXT = Component.literal("Copyright Mojang AB. Do not distribute!");
	public static final CubeMap CUBE_MAP = new CubeMap(new ResourceLocation("textures/gui/title/background/panorama"));
	private static final ResourceLocation PANORAMA_OVERLAY = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
	@Nullable
	private SplashRenderer splash;
	private Button resetDemoButton;
	@Nullable
	private RealmsNotificationsScreen realmsNotificationsScreen;
	private final PanoramaRenderer panorama = new PanoramaRenderer(CUBE_MAP);
	private final boolean fading;
	private long fadeInStart;
	@Nullable
	private TitleScreen.WarningLabel warningLabel;
	private final LogoRenderer logoRenderer;

	public TitleScreen() {
		this(false);
	}

	public TitleScreen(boolean bl) {
		this(bl, null);
	}

	public TitleScreen(boolean bl, @Nullable LogoRenderer logoRenderer) {
		super(Component.translatable("narrator.screen.title"));
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

		this.minecraft.getRealms32BitWarningStatus().showRealms32BitWarningIfNeeded(this);
	}

	public static CompletableFuture<Void> preloadResources(TextureManager textureManager, Executor executor) {
		return CompletableFuture.allOf(
			textureManager.preload(LogoRenderer.MINECRAFT_LOGO, executor),
			textureManager.preload(LogoRenderer.MINECRAFT_EDITION, executor),
			textureManager.preload(PANORAMA_OVERLAY, executor),
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
			this.createDemoMenuOptions(l, 24);
		} else {
			this.createNormalMenuOptions(l, 24);
		}

		SpriteIconButton spriteIconButton = this.addRenderableWidget(
			CommonButtons.language(
				20, button -> this.minecraft.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager())), true
			)
		);
		spriteIconButton.setPosition(this.width / 2 - 124, l + 72 + 12);
		this.addRenderableWidget(
			Button.builder(Component.translatable("menu.options"), button -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options)))
				.bounds(this.width / 2 - 100, l + 72 + 12, 98, 20)
				.build()
		);
		this.addRenderableWidget(
			Button.builder(Component.translatable("menu.quit"), button -> this.minecraft.stop()).bounds(this.width / 2 + 2, l + 72 + 12, 98, 20).build()
		);
		SpriteIconButton spriteIconButton2 = this.addRenderableWidget(
			CommonButtons.accessibility(20, button -> this.minecraft.setScreen(new AccessibilityOptionsScreen(this, this.minecraft.options)), true)
		);
		spriteIconButton2.setPosition(this.width / 2 + 104, l + 72 + 12);
		this.addRenderableWidget(
			new PlainTextButton(j, this.height - 10, i, 10, COPYRIGHT_TEXT, button -> this.minecraft.setScreen(new CreditsAndAttributionScreen(this)), this.font)
		);
		if (this.realmsNotificationsScreen == null) {
			this.realmsNotificationsScreen = new RealmsNotificationsScreen();
		}

		if (this.realmsNotificationsEnabled()) {
			this.realmsNotificationsScreen.init(this.minecraft, this.width, this.height);
		}

		if (!this.minecraft.is64Bit()) {
			this.warningLabel = new TitleScreen.WarningLabel(
				this.font, MultiLineLabel.create(this.font, Component.translatable("title.32bit.deprecation"), 350, 2), this.width / 2, l - 24
			);
		}
	}

	private void createNormalMenuOptions(int i, int j) {
		this.addRenderableWidget(
			Button.builder(Component.translatable("menu.singleplayer"), button -> this.minecraft.setScreen(new SelectWorldScreen(this)))
				.bounds(this.width / 2 - 100, i, 200, 20)
				.build()
		);
		Component component = this.getMultiplayerDisabledReason();
		boolean bl = component == null;
		Tooltip tooltip = component != null ? Tooltip.create(component) : null;
		this.addRenderableWidget(Button.builder(Component.translatable("menu.multiplayer"), button -> {
			Screen screen = (Screen)(this.minecraft.options.skipMultiplayerWarning ? new JoinMultiplayerScreen(this) : new SafetyScreen(this));
			this.minecraft.setScreen(screen);
		}).bounds(this.width / 2 - 100, i + j * 1, 200, 20).tooltip(tooltip).build()).active = bl;
		this.addRenderableWidget(
				Button.builder(Component.translatable("menu.online"), button -> this.realmsButtonClicked())
					.bounds(this.width / 2 - 100, i + j * 2, 200, 20)
					.tooltip(tooltip)
					.build()
			)
			.active = bl;
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

	private void createDemoMenuOptions(int i, int j) {
		boolean bl = this.checkDemoWorldPresence();
		this.addRenderableWidget(
			Button.builder(
					Component.translatable("menu.playdemo"),
					button -> {
						if (bl) {
							this.minecraft.createWorldOpenFlows().loadLevel(this, "Demo_World");
						} else {
							this.minecraft
								.createWorldOpenFlows()
								.createFreshLevel("Demo_World", MinecraftServer.DEMO_SETTINGS, WorldOptions.DEMO_OPTIONS, WorldPresets::createNormalWorldDimensions);
						}
					}
				)
				.bounds(this.width / 2 - 100, i, 200, 20)
				.build()
		);
		this.resetDemoButton = this.addRenderableWidget(
			Button.builder(
					Component.translatable("menu.resetdemo"),
					button -> {
						LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();

						try (LevelStorageSource.LevelStorageAccess levelStorageAccess = levelStorageSource.createAccess("Demo_World")) {
							LevelSummary levelSummary = levelStorageAccess.getSummary();
							if (levelSummary != null) {
								this.minecraft
									.setScreen(
										new ConfirmScreen(
											this::confirmDemo,
											Component.translatable("selectWorld.deleteQuestion"),
											Component.translatable("selectWorld.deleteWarning", levelSummary.getLevelName()),
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
				.bounds(this.width / 2 - 100, i + j * 1, 200, 20)
				.build()
		);
		this.resetDemoButton.active = bl;
	}

	private boolean checkDemoWorldPresence() {
		try {
			boolean var2;
			try (LevelStorageSource.LevelStorageAccess levelStorageAccess = this.minecraft.getLevelSource().createAccess("Demo_World")) {
				var2 = levelStorageAccess.getSummary() != null;
			}

			return var2;
		} catch (IOException var6) {
			SystemToast.onWorldAccessFailure(this.minecraft, "Demo_World");
			LOGGER.warn("Failed to read demo world data", (Throwable)var6);
			return false;
		}
	}

	private void realmsButtonClicked() {
		this.minecraft.setScreen(new RealmsMainScreen(this));
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		if (this.fadeInStart == 0L && this.fading) {
			this.fadeInStart = Util.getMillis();
		}

		float g = this.fading ? (float)(Util.getMillis() - this.fadeInStart) / 1000.0F : 1.0F;
		this.panorama.render(f, Mth.clamp(g, 0.0F, 1.0F));
		RenderSystem.enableBlend();
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.fading ? (float)Mth.ceil(Mth.clamp(g, 0.0F, 1.0F)) : 1.0F);
		guiGraphics.blit(PANORAMA_OVERLAY, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
		float h = this.fading ? Mth.clamp(g - 1.0F, 0.0F, 1.0F) : 1.0F;
		this.logoRenderer.renderLogo(guiGraphics, this.width, h);
		int k = Mth.ceil(h * 255.0F) << 24;
		if ((k & -67108864) != 0) {
			if (this.warningLabel != null) {
				this.warningLabel.render(guiGraphics, k);
			}

			if (this.splash != null) {
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

			for (GuiEventListener guiEventListener : this.children()) {
				if (guiEventListener instanceof AbstractWidget) {
					((AbstractWidget)guiEventListener).setAlpha(h);
				}
			}

			super.render(guiGraphics, i, j, f);
			if (this.realmsNotificationsEnabled() && h >= 1.0F) {
				RenderSystem.enableDepthTest();
				this.realmsNotificationsScreen.render(guiGraphics, i, j, f);
			}
		}
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
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

	@Environment(EnvType.CLIENT)
	static record WarningLabel(Font font, MultiLineLabel label, int x, int y) {
		public void render(GuiGraphics guiGraphics, int i) {
			this.label.renderBackgroundCentered(guiGraphics, this.x, this.y, 9, 2, 2097152 | Math.min(i, 1426063360));
			this.label.renderCentered(guiGraphics, this.x, this.y, 9, 16777215 | i);
		}
	}
}
