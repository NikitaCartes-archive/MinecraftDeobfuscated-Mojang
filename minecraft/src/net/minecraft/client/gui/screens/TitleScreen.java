package net.minecraft.client.gui.screens;

import com.google.common.util.concurrent.Runnables;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsBridge;
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
	public static final CubeMap CUBE_MAP = new CubeMap(new ResourceLocation("textures/gui/title/background/panorama"));
	private static final ResourceLocation PANORAMA_OVERLAY = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
	private static final ResourceLocation ACCESSIBILITY_TEXTURE = new ResourceLocation("textures/gui/accessibility.png");
	private final boolean minceraftEasterEgg;
	@Nullable
	private String splash;
	private Button resetDemoButton;
	private static final ResourceLocation MINECRAFT_LOGO = new ResourceLocation("textures/gui/title/minecraft.png");
	private static final ResourceLocation MINECRAFT_EDITION = new ResourceLocation("textures/gui/title/edition.png");
	private boolean realmsNotificationsInitialized;
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

		this.addButton(
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
		this.addButton(
			new Button(
				this.width / 2 - 100,
				j + 72 + 12,
				98,
				20,
				new TranslatableComponent("menu.options"),
				button -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options))
			)
		);
		this.addButton(new Button(this.width / 2 + 2, j + 72 + 12, 98, 20, new TranslatableComponent("menu.quit"), button -> this.minecraft.stop()));
		this.addButton(
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
		if (this.minecraft.options.realmsNotifications && !this.realmsNotificationsInitialized) {
			RealmsBridge realmsBridge = new RealmsBridge();
			this.realmsNotificationsScreen = realmsBridge.getNotificationScreen(this);
			this.realmsNotificationsInitialized = true;
		}

		if (this.realmsNotificationsEnabled()) {
			this.realmsNotificationsScreen.init(this.minecraft, this.width, this.height);
		}
	}

	private void createNormalMenuOptions(int i, int j) {
		this.addButton(
			new Button(this.width / 2 - 100, i, 200, 20, new TranslatableComponent("menu.singleplayer"), button -> this.minecraft.setScreen(new SelectWorldScreen(this)))
		);
		boolean bl = this.minecraft.allowsMultiplayer();
		Button.OnTooltip onTooltip = bl
			? Button.NO_TOOLTIP
			: (button, poseStack, ix, jx) -> {
				if (!button.active) {
					this.renderTooltip(
						poseStack, this.minecraft.font.split(new TranslatableComponent("title.multiplayer.disabled"), Math.max(this.width / 2 - 43, 170)), ix, jx
					);
				}
			};
		this.addButton(new Button(this.width / 2 - 100, i + j * 1, 200, 20, new TranslatableComponent("menu.multiplayer"), button -> {
			Screen screen = (Screen)(this.minecraft.options.skipMultiplayerWarning ? new JoinMultiplayerScreen(this) : new SafetyScreen(this));
			this.minecraft.setScreen(screen);
		}, onTooltip)).active = bl;
		this.addButton(
				new Button(this.width / 2 - 100, i + j * 2, 200, 20, new TranslatableComponent("menu.online"), button -> this.realmsButtonClicked(), onTooltip)
			)
			.active = bl;
	}

	private void createDemoMenuOptions(int i, int j) {
		boolean bl = this.checkDemoWorldPresence();
		this.addButton(new Button(this.width / 2 - 100, i, 200, 20, new TranslatableComponent("menu.playdemo"), button -> {
			if (bl) {
				this.minecraft.loadLevel("Demo_World");
			} else {
				RegistryAccess.RegistryHolder registryHolder = RegistryAccess.builtin();
				this.minecraft.createLevel("Demo_World", MinecraftServer.DEMO_SETTINGS, registryHolder, WorldGenSettings.demoSettings(registryHolder));
			}
		}));
		this.resetDemoButton = this.addButton(
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
					} catch (IOException var16) {
						SystemToast.onWorldAccessFailure(this.minecraft, "Demo_World");
						LOGGER.warn("Failed to access demo world", (Throwable)var16);
					}
				}
			)
		);
		this.resetDemoButton.active = bl;
	}

	private boolean checkDemoWorldPresence() {
		try (LevelStorageSource.LevelStorageAccess levelStorageAccess = this.minecraft.getLevelSource().createAccess("Demo_World")) {
			return levelStorageAccess.getSummary() != null;
		} catch (IOException var15) {
			SystemToast.onWorldAccessFailure(this.minecraft, "Demo_World");
			LOGGER.warn("Failed to read demo world data", (Throwable)var15);
			return false;
		}
	}

	private void realmsButtonClicked() {
		RealmsBridge realmsBridge = new RealmsBridge();
		realmsBridge.switchToRealms(this);
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		if (this.fadeInStart == 0L && this.fading) {
			this.fadeInStart = Util.getMillis();
		}

		float g = this.fading ? (float)(Util.getMillis() - this.fadeInStart) / 1000.0F : 1.0F;
		fill(poseStack, 0, 0, this.width, this.height, -1);
		this.panorama.render(f, Mth.clamp(g, 0.0F, 1.0F));
		int k = 274;
		int l = this.width / 2 - 137;
		int m = 30;
		this.minecraft.getTextureManager().bind(PANORAMA_OVERLAY);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.fading ? (float)Mth.ceil(Mth.clamp(g, 0.0F, 1.0F)) : 1.0F);
		blit(poseStack, 0, 0, this.width, this.height, 0.0F, 0.0F, 16, 128, 16, 128);
		float h = this.fading ? Mth.clamp(g - 1.0F, 0.0F, 1.0F) : 1.0F;
		int n = Mth.ceil(h * 255.0F) << 24;
		if ((n & -67108864) != 0) {
			this.minecraft.getTextureManager().bind(MINECRAFT_LOGO);
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, h);
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

			this.minecraft.getTextureManager().bind(MINECRAFT_EDITION);
			blit(poseStack, l + 88, 67, 0.0F, 0.0F, 98, 14, 128, 16);
			if (this.splash != null) {
				RenderSystem.pushMatrix();
				RenderSystem.translatef((float)(this.width / 2 + 90), 70.0F, 0.0F);
				RenderSystem.rotatef(-20.0F, 0.0F, 0.0F, 1.0F);
				float o = 1.8F - Mth.abs(Mth.sin((float)(Util.getMillis() % 1000L) / 1000.0F * (float) (Math.PI * 2)) * 0.1F);
				o = o * 100.0F / (float)(this.font.width(this.splash) + 32);
				RenderSystem.scalef(o, o, o);
				drawCenteredString(poseStack, this.font, this.splash, 0, -8, 16776960 | n);
				RenderSystem.popMatrix();
			}

			String string = "Minecraft " + SharedConstants.getCurrentVersion().getName();
			if (this.minecraft.isDemo()) {
				string = string + " Demo";
			} else {
				string = string + ("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType());
			}

			if (this.minecraft.isProbablyModded()) {
				string = string + I18n.get("menu.modded");
			}

			drawString(poseStack, this.font, string, 2, this.height - 10, 16777215 | n);
			drawString(poseStack, this.font, "Copyright Mojang AB. Do not distribute!", this.copyrightX, this.height - 10, 16777215 | n);
			if (i > this.copyrightX && i < this.copyrightX + this.copyrightWidth && j > this.height - 10 && j < this.height) {
				fill(poseStack, this.copyrightX, this.height - 1, this.copyrightX + this.copyrightWidth, this.height, 16777215 | n);
			}

			for (AbstractWidget abstractWidget : this.buttons) {
				abstractWidget.setAlpha(h);
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
			} catch (IOException var15) {
				SystemToast.onWorldDeleteFailure(this.minecraft, "Demo_World");
				LOGGER.warn("Failed to delete demo world", (Throwable)var15);
			}
		}

		this.minecraft.setScreen(this);
	}
}
