package net.minecraft.client.gui.screens;

import com.google.common.util.concurrent.Runnables;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class TitleScreen extends Screen {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String DEMO_LEVEL_ID = "Demo_World";
	public static final String COPYRIGHT_TEXT = "Copyright Mojang AB. Do not distribute!";
	public static final CubeMap CUBE_MAP = new CubeMap(new ResourceLocation("textures/gui/title/background/panorama"));
	private static final ResourceLocation PANORAMA_OVERLAY = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
	private static final ResourceLocation ACCESSIBILITY_TEXTURE = new ResourceLocation("textures/gui/accessibility.png");
	private final boolean minceraftEasterEgg;
	@Nullable
	private String splash;
	private Button resetDemoButton;
	private static final ResourceLocation MINECRAFT_LOGO = new ResourceLocation("textures/gui/title/minecraft.png");
	private static final ResourceLocation MINECRAFT_EDITION = new ResourceLocation("textures/gui/title/edition.png");
	private Screen realmsNotificationsScreen;
	private int copyrightWidth;
	private int copyrightX;
	private final PanoramaRenderer panorama = new PanoramaRenderer(CUBE_MAP);
	private final boolean fading;
	private long fadeInStart;

	public TitleScreen() {
		this(false);
	}

	public TitleScreen(boolean bl) {
		super(new TranslatableComponent("narrator.screen.title"));
		this.fading = bl;
		this.minceraftEasterEgg = (double)new Random().nextFloat() < 1.0E-4;
	}

	private boolean realmsNotificationsEnabled() {
		return this.minecraft.options.realmsNotifications && this.realmsNotificationsScreen != null;
	}

	@Override
	public void tick() {
		if (this.realmsNotificationsEnabled()) {
			this.realmsNotificationsScreen.tick();
		}
	}

	public static CompletableFuture<Void> preloadResources(TextureManager textureManager, Executor executor) {
		return CompletableFuture.allOf(
			textureManager.preload(MINECRAFT_LOGO, executor),
			textureManager.preload(MINECRAFT_EDITION, executor),
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

		this.copyrightWidth = this.font.width("Copyright Mojang AB. Do not distribute!");
		this.copyrightX = this.width - this.copyrightWidth - 2;
		int i = 24;
		int j = this.height / 4 + 48;
		if (this.minecraft.isDemo()) {
			this.createDemoMenuOptions(j, 24);
		} else {
			this.createNormalMenuOptions(j, 24);
		}

		this.addRenderableWidget(
			new ImageButton(
				this.width / 2 - 124,
				j + 72 + 12,
				20,
				20,
				0,
				106,
				20,
				Button.WIDGETS_LOCATION,
				256,
				256,
				button -> this.minecraft.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager())),
				new TranslatableComponent("narrator.button.language")
			)
		);
		this.addRenderableWidget(
			new Button(
				this.width / 2 - 100,
				j + 72 + 12,
				98,
				20,
				new TranslatableComponent("menu.options"),
				button -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options))
			)
		);
		this.addRenderableWidget(new Button(this.width / 2 + 2, j + 72 + 12, 98, 20, new TranslatableComponent("menu.quit"), button -> this.minecraft.stop()));
		this.addRenderableWidget(
			new ImageButton(
				this.width / 2 + 104,
				j + 72 + 12,
				20,
				20,
				0,
				0,
				20,
				ACCESSIBILITY_TEXTURE,
				32,
				64,
				button -> this.minecraft.setScreen(new AccessibilityOptionsScreen(this, this.minecraft.options)),
				new TranslatableComponent("narrator.button.accessibility")
			)
		);
		this.minecraft.setConnectedToRealms(false);
		if (this.minecraft.options.realmsNotifications && this.realmsNotificationsScreen == null) {
			this.realmsNotificationsScreen = new RealmsNotificationsScreen();
		}

		if (this.realmsNotificationsEnabled()) {
			this.realmsNotificationsScreen.init(this.minecraft, this.width, this.height);
		}
	}

	private void createNormalMenuOptions(int i, int j) {
		this.addRenderableWidget(
			new Button(this.width / 2 - 100, i, 200, 20, new TranslatableComponent("menu.singleplayer"), button -> this.minecraft.setScreen(new SelectWorldScreen(this)))
		);
		boolean bl = this.minecraft.allowsMultiplayer();
		Button.OnTooltip onTooltip = bl ? Button.NO_TOOLTIP : new Button.OnTooltip() {
			private final Component text = new TranslatableComponent("title.multiplayer.disabled");

			@Override
			public void onTooltip(Button button, PoseStack poseStack, int i, int j) {
				if (!button.active) {
					TitleScreen.this.renderTooltip(poseStack, TitleScreen.this.minecraft.font.split(this.text, Math.max(TitleScreen.this.width / 2 - 43, 170)), i, j);
				}
			}

			@Override
			public void narrateTooltip(Consumer<Component> consumer) {
				consumer.accept(this.text);
			}
		};
		this.addRenderableWidget(new Button(this.width / 2 - 100, i + j * 1, 200, 20, new TranslatableComponent("menu.multiplayer"), button -> {
			Screen screen = (Screen)(this.minecraft.options.skipMultiplayerWarning ? new JoinMultiplayerScreen(this) : new SafetyScreen(this));
			this.minecraft.setScreen(screen);
		}, onTooltip)).active = bl;
		this.addRenderableWidget(
				new Button(this.width / 2 - 100, i + j * 2, 200, 20, new TranslatableComponent("menu.online"), button -> this.realmsButtonClicked(), onTooltip)
			)
			.active = bl;
	}

	private void createDemoMenuOptions(int i, int j) {
		boolean bl = this.checkDemoWorldPresence();
		this.addRenderableWidget(new Button(this.width / 2 - 100, i, 200, 20, new TranslatableComponent("menu.playdemo"), button -> {
			if (bl) {
				this.minecraft.loadLevel("Demo_World");
			} else {
				RegistryAccess.RegistryHolder registryHolder = RegistryAccess.builtin();
				this.minecraft.createLevel("Demo_World", MinecraftServer.DEMO_SETTINGS, registryHolder, WorldGenSettings.demoSettings(registryHolder));
			}
		}));
		this.resetDemoButton = this.addRenderableWidget(
			new Button(
				this.width / 2 - 100,
				i + j * 1,
				200,
				20,
				new TranslatableComponent("menu.resetdemo"),
				button -> {
					LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();

					try (LevelStorageSource.LevelStorageAccess levelStorageAccess = levelStorageSource.createAccess("Demo_World")) {
						LevelSummary levelSummary = levelStorageAccess.getSummary();
						if (levelSummary != null) {
							this.minecraft
								.setScreen(
									new ConfirmScreen(
										this::confirmDemo,
										new TranslatableComponent("selectWorld.deleteQuestion"),
										new TranslatableComponent("selectWorld.deleteWarning", levelSummary.getLevelName()),
										new TranslatableComponent("selectWorld.deleteButton"),
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
	public void render(PoseStack poseStack, int i, int j, float f) {
		if (this.fadeInStart == 0L && this.fading) {
			this.fadeInStart = Util.getMillis();
		}

		float g = this.fading ? (float)(Util.getMillis() - this.fadeInStart) / 1000.0F : 1.0F;
		this.panorama.render(f, Mth.clamp(g, 0.0F, 1.0F));
		int k = 274;
		int l = this.width / 2 - 137;
		int m = 30;
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, PANORAMA_OVERLAY);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.fading ? (float)Mth.ceil(Mth.clamp(g, 0.0F, 1.0F)) : 1.0F);
		blit(poseStack, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
		float h = this.fading ? Mth.clamp(g - 1.0F, 0.0F, 1.0F) : 1.0F;
		int n = Mth.ceil(h * 255.0F) << 24;
		if ((n & -67108864) != 0) {
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, MINECRAFT_LOGO);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, h);
			if (this.minceraftEasterEgg) {
				this.blitOutlineBlack(l, 30, (integer, integer2) -> {
					this.blit(poseStack, integer + 0, integer2, 0, 0, 99, 44);
					this.blit(poseStack, integer + 99, integer2, 129, 0, 27, 44);
					this.blit(poseStack, integer + 99 + 26, integer2, 126, 0, 3, 44);
					this.blit(poseStack, integer + 99 + 26 + 3, integer2, 99, 0, 26, 44);
					this.blit(poseStack, integer + 155, integer2, 0, 45, 155, 44);
				});
			} else {
				this.blitOutlineBlack(l, 30, (integer, integer2) -> {
					this.blit(poseStack, integer + 0, integer2, 0, 0, 155, 44);
					this.blit(poseStack, integer + 155, integer2, 0, 45, 155, 44);
				});
			}

			RenderSystem.setShaderTexture(0, MINECRAFT_EDITION);
			blit(poseStack, l + 88, 67, 0.0F, 0.0F, 98, 14, 128, 16);
			if (this.splash != null) {
				poseStack.pushPose();
				poseStack.translate((double)(this.width / 2 + 90), 70.0, 0.0);
				poseStack.mulPose(Vector3f.ZP.rotationDegrees(-20.0F));
				float o = 1.8F - Mth.abs(Mth.sin((float)(Util.getMillis() % 1000L) / 1000.0F * (float) (Math.PI * 2)) * 0.1F);
				o = o * 100.0F / (float)(this.font.width(this.splash) + 32);
				poseStack.scale(o, o, o);
				drawCenteredString(poseStack, this.font, this.splash, 0, -8, 16776960 | n);
				poseStack.popPose();
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

			drawString(poseStack, this.font, string, 2, this.height - 10, 16777215 | n);
			drawString(poseStack, this.font, "Copyright Mojang AB. Do not distribute!", this.copyrightX, this.height - 10, 16777215 | n);
			if (i > this.copyrightX && i < this.copyrightX + this.copyrightWidth && j > this.height - 10 && j < this.height) {
				fill(poseStack, this.copyrightX, this.height - 1, this.copyrightX + this.copyrightWidth, this.height, 16777215 | n);
			}

			for (GuiEventListener guiEventListener : this.children()) {
				if (guiEventListener instanceof AbstractWidget) {
					((AbstractWidget)guiEventListener).setAlpha(h);
				}
			}

			super.render(poseStack, i, j, f);
			if (this.realmsNotificationsEnabled() && h >= 1.0F) {
				this.realmsNotificationsScreen.render(poseStack, i, j, f);
			}
		}
	}

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		if (super.mouseClicked(d, e, i)) {
			return true;
		} else if (this.realmsNotificationsEnabled() && this.realmsNotificationsScreen.mouseClicked(d, e, i)) {
			return true;
		} else {
			if (d > (double)this.copyrightX && d < (double)(this.copyrightX + this.copyrightWidth) && e > (double)(this.height - 10) && e < (double)this.height) {
				this.minecraft.setScreen(new WinScreen(false, Runnables.doNothing()));
			}

			return false;
		}
	}

	@Override
	public void removed() {
		if (this.realmsNotificationsScreen != null) {
			this.realmsNotificationsScreen.removed();
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
