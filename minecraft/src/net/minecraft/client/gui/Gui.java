package net.minecraft.client.gui;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;

@Environment(EnvType.CLIENT)
public class Gui {
	private static final ResourceLocation VIGNETTE_LOCATION = new ResourceLocation("textures/misc/vignette.png");
	private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
	private static final ResourceLocation PUMPKIN_BLUR_LOCATION = new ResourceLocation("textures/misc/pumpkinblur.png");
	private static final ResourceLocation SPYGLASS_SCOPE_LOCATION = new ResourceLocation("textures/misc/spyglass_scope.png");
	private static final ResourceLocation POWDER_SNOW_OUTLINE_LOCATION = new ResourceLocation("textures/misc/powder_snow_outline.png");
	private static final ResourceLocation GUI_ICONS_LOCATION = new ResourceLocation("textures/gui/icons.png");
	private static final Component DEMO_EXPIRED_TEXT = Component.translatable("demo.demoExpired");
	private static final Component SAVING_TEXT = Component.translatable("menu.savingLevel");
	private static final int COLOR_WHITE = 16777215;
	private static final float MIN_CROSSHAIR_ATTACK_SPEED = 5.0F;
	private static final int NUM_HEARTS_PER_ROW = 10;
	private static final int LINE_HEIGHT = 10;
	private static final String SPACER = ": ";
	private static final float PORTAL_OVERLAY_ALPHA_MIN = 0.2F;
	private static final int HEART_SIZE = 9;
	private static final int HEART_SEPARATION = 8;
	private static final float AUTOSAVE_FADE_SPEED_FACTOR = 0.2F;
	private final RandomSource random = RandomSource.create();
	private final Minecraft minecraft;
	private final ItemRenderer itemRenderer;
	private final ChatComponent chat;
	private int tickCount;
	@Nullable
	private Component overlayMessageString;
	private int overlayMessageTime;
	private boolean animateOverlayMessageColor;
	private boolean chatDisabledByPlayerShown;
	public float vignetteBrightness = 1.0F;
	private int toolHighlightTimer;
	private ItemStack lastToolHighlight = ItemStack.EMPTY;
	private final DebugScreenOverlay debugScreen;
	private final SubtitleOverlay subtitleOverlay;
	private final SpectatorGui spectatorGui;
	private final PlayerTabOverlay tabList;
	private final BossHealthOverlay bossOverlay;
	private int titleTime;
	@Nullable
	private Component title;
	@Nullable
	private Component subtitle;
	private int titleFadeInTime;
	private int titleStayTime;
	private int titleFadeOutTime;
	private int lastHealth;
	private int displayHealth;
	private long lastHealthTime;
	private long healthBlinkTime;
	private int screenWidth;
	private int screenHeight;
	private float autosaveIndicatorValue;
	private float lastAutosaveIndicatorValue;
	private float scopeScale;

	public Gui(Minecraft minecraft, ItemRenderer itemRenderer) {
		this.minecraft = minecraft;
		this.itemRenderer = itemRenderer;
		this.debugScreen = new DebugScreenOverlay(minecraft);
		this.spectatorGui = new SpectatorGui(minecraft);
		this.chat = new ChatComponent(minecraft);
		this.tabList = new PlayerTabOverlay(minecraft, this);
		this.bossOverlay = new BossHealthOverlay(minecraft);
		this.subtitleOverlay = new SubtitleOverlay(minecraft);
		this.resetTitleTimes();
	}

	public void resetTitleTimes() {
		this.titleFadeInTime = 10;
		this.titleStayTime = 70;
		this.titleFadeOutTime = 20;
	}

	public void render(GuiGraphics guiGraphics, float f) {
		Window window = this.minecraft.getWindow();
		this.screenWidth = window.getGuiScaledWidth();
		this.screenHeight = window.getGuiScaledHeight();
		Font font = this.getFont();
		RenderSystem.enableBlend();
		if (Minecraft.useFancyGraphics()) {
			this.renderVignette(guiGraphics, this.minecraft.getCameraEntity());
		} else {
			RenderSystem.enableDepthTest();
		}

		float g = this.minecraft.getDeltaFrameTime();
		this.scopeScale = Mth.lerp(0.5F * g, this.scopeScale, 1.125F);
		if (this.minecraft.options.getCameraType().isFirstPerson()) {
			if (this.minecraft.player.isScoping()) {
				this.renderSpyglassOverlay(guiGraphics, this.scopeScale);
			} else {
				this.scopeScale = 0.5F;
				ItemStack itemStack = this.minecraft.player.getInventory().getArmor(3);
				if (itemStack.is(Blocks.CARVED_PUMPKIN.asItem())) {
					this.renderTextureOverlay(guiGraphics, PUMPKIN_BLUR_LOCATION, 1.0F);
				}
			}
		}

		if (this.minecraft.player.getTicksFrozen() > 0) {
			this.renderTextureOverlay(guiGraphics, POWDER_SNOW_OUTLINE_LOCATION, this.minecraft.player.getPercentFrozen());
		}

		float h = Mth.lerp(f, this.minecraft.player.oPortalTime, this.minecraft.player.portalTime);
		if (h > 0.0F && !this.minecraft.player.hasEffect(MobEffects.CONFUSION)) {
			this.renderPortalOverlay(guiGraphics, h);
		}

		if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
			this.spectatorGui.renderHotbar(guiGraphics);
		} else if (!this.minecraft.options.hideGui) {
			this.renderHotbar(f, guiGraphics);
		}

		if (!this.minecraft.options.hideGui) {
			RenderSystem.enableBlend();
			this.renderCrosshair(guiGraphics);
			this.minecraft.getProfiler().push("bossHealth");
			this.bossOverlay.render(guiGraphics);
			this.minecraft.getProfiler().pop();
			if (this.minecraft.gameMode.canHurtPlayer()) {
				this.renderPlayerHealth(guiGraphics);
			}

			this.renderVehicleHealth(guiGraphics);
			RenderSystem.disableBlend();
			int i = this.screenWidth / 2 - 91;
			PlayerRideableJumping playerRideableJumping = this.minecraft.player.jumpableVehicle();
			if (playerRideableJumping != null) {
				this.renderJumpMeter(playerRideableJumping, guiGraphics, i);
			} else if (this.minecraft.gameMode.hasExperience()) {
				this.renderExperienceBar(guiGraphics, i);
			}

			if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
				this.renderSelectedItemName(guiGraphics);
			} else if (this.minecraft.player.isSpectator()) {
				this.spectatorGui.renderTooltip(guiGraphics);
			}
		}

		if (this.minecraft.player.getSleepTimer() > 0) {
			this.minecraft.getProfiler().push("sleep");
			RenderSystem.disableDepthTest();
			float j = (float)this.minecraft.player.getSleepTimer();
			float k = j / 100.0F;
			if (k > 1.0F) {
				k = 1.0F - (j - 100.0F) / 10.0F;
			}

			int l = (int)(220.0F * k) << 24 | 1052704;
			guiGraphics.fill(0, 0, this.screenWidth, this.screenHeight, l);
			RenderSystem.enableDepthTest();
			this.minecraft.getProfiler().pop();
		}

		if (this.minecraft.isDemo()) {
			this.renderDemoOverlay(guiGraphics);
		}

		this.renderEffects(guiGraphics);
		if (this.minecraft.options.renderDebug) {
			this.debugScreen.render(guiGraphics);
		}

		if (!this.minecraft.options.hideGui) {
			if (this.overlayMessageString != null && this.overlayMessageTime > 0) {
				this.minecraft.getProfiler().push("overlayMessage");
				float j = (float)this.overlayMessageTime - f;
				int m = (int)(j * 255.0F / 20.0F);
				if (m > 255) {
					m = 255;
				}

				if (m > 8) {
					guiGraphics.pose().pushPose();
					guiGraphics.pose().translate((float)(this.screenWidth / 2), (float)(this.screenHeight - 68), 0.0F);
					int l = 16777215;
					if (this.animateOverlayMessageColor) {
						l = Mth.hsvToRgb(j / 50.0F, 0.7F, 0.6F) & 16777215;
					}

					int n = m << 24 & 0xFF000000;
					int o = font.width(this.overlayMessageString);
					this.drawBackdrop(guiGraphics, font, -4, o, 16777215 | n);
					guiGraphics.drawString(font, this.overlayMessageString, -o / 2, -4, l | n);
					guiGraphics.pose().popPose();
				}

				this.minecraft.getProfiler().pop();
			}

			if (this.title != null && this.titleTime > 0) {
				this.minecraft.getProfiler().push("titleAndSubtitle");
				float jx = (float)this.titleTime - f;
				int mx = 255;
				if (this.titleTime > this.titleFadeOutTime + this.titleStayTime) {
					float p = (float)(this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime) - jx;
					mx = (int)(p * 255.0F / (float)this.titleFadeInTime);
				}

				if (this.titleTime <= this.titleFadeOutTime) {
					mx = (int)(jx * 255.0F / (float)this.titleFadeOutTime);
				}

				mx = Mth.clamp(mx, 0, 255);
				if (mx > 8) {
					guiGraphics.pose().pushPose();
					guiGraphics.pose().translate((float)(this.screenWidth / 2), (float)(this.screenHeight / 2), 0.0F);
					RenderSystem.enableBlend();
					guiGraphics.pose().pushPose();
					guiGraphics.pose().scale(4.0F, 4.0F, 4.0F);
					int l = mx << 24 & 0xFF000000;
					int n = font.width(this.title);
					this.drawBackdrop(guiGraphics, font, -10, n, 16777215 | l);
					guiGraphics.drawString(font, this.title, -n / 2, -10, 16777215 | l);
					guiGraphics.pose().popPose();
					if (this.subtitle != null) {
						guiGraphics.pose().pushPose();
						guiGraphics.pose().scale(2.0F, 2.0F, 2.0F);
						int o = font.width(this.subtitle);
						this.drawBackdrop(guiGraphics, font, 5, o, 16777215 | l);
						guiGraphics.drawString(font, this.subtitle, -o / 2, 5, 16777215 | l);
						guiGraphics.pose().popPose();
					}

					RenderSystem.disableBlend();
					guiGraphics.pose().popPose();
				}

				this.minecraft.getProfiler().pop();
			}

			this.subtitleOverlay.render(guiGraphics);
			Scoreboard scoreboard = this.minecraft.level.getScoreboard();
			Objective objective = null;
			PlayerTeam playerTeam = scoreboard.getPlayersTeam(this.minecraft.player.getScoreboardName());
			if (playerTeam != null) {
				int n = playerTeam.getColor().getId();
				if (n >= 0) {
					objective = scoreboard.getDisplayObjective(3 + n);
				}
			}

			Objective objective2 = objective != null ? objective : scoreboard.getDisplayObjective(1);
			if (objective2 != null) {
				this.displayScoreboardSidebar(guiGraphics, objective2);
			}

			RenderSystem.enableBlend();
			int o = Mth.floor(this.minecraft.mouseHandler.xpos() * (double)window.getGuiScaledWidth() / (double)window.getScreenWidth());
			int q = Mth.floor(this.minecraft.mouseHandler.ypos() * (double)window.getGuiScaledHeight() / (double)window.getScreenHeight());
			this.minecraft.getProfiler().push("chat");
			this.chat.render(guiGraphics, this.tickCount, o, q);
			this.minecraft.getProfiler().pop();
			objective2 = scoreboard.getDisplayObjective(0);
			if (!this.minecraft.options.keyPlayerList.isDown()
				|| this.minecraft.isLocalServer() && this.minecraft.player.connection.getListedOnlinePlayers().size() <= 1 && objective2 == null) {
				this.tabList.setVisible(false);
			} else {
				this.tabList.setVisible(true);
				this.tabList.render(guiGraphics, this.screenWidth, scoreboard, objective2);
			}

			this.renderSavingIndicator(guiGraphics);
		}
	}

	private void drawBackdrop(GuiGraphics guiGraphics, Font font, int i, int j, int k) {
		int l = this.minecraft.options.getBackgroundColor(0.0F);
		if (l != 0) {
			int m = -j / 2;
			guiGraphics.fill(m - 2, i - 2, m + j + 2, i + 9 + 2, FastColor.ARGB32.multiply(l, k));
		}
	}

	private void renderCrosshair(GuiGraphics guiGraphics) {
		Options options = this.minecraft.options;
		if (options.getCameraType().isFirstPerson()) {
			if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR || this.canRenderCrosshairForSpectator(this.minecraft.hitResult)) {
				if (options.renderDebug && !options.hideGui && !this.minecraft.player.isReducedDebugInfo() && !options.reducedDebugInfo().get()) {
					Camera camera = this.minecraft.gameRenderer.getMainCamera();
					PoseStack poseStack = RenderSystem.getModelViewStack();
					poseStack.pushPose();
					poseStack.mulPoseMatrix(guiGraphics.pose().last().pose());
					poseStack.translate((float)(this.screenWidth / 2), (float)(this.screenHeight / 2), 0.0F);
					poseStack.mulPose(Axis.XN.rotationDegrees(camera.getXRot()));
					poseStack.mulPose(Axis.YP.rotationDegrees(camera.getYRot()));
					poseStack.scale(-1.0F, -1.0F, -1.0F);
					RenderSystem.applyModelViewMatrix();
					RenderSystem.renderCrosshair(10);
					poseStack.popPose();
					RenderSystem.applyModelViewMatrix();
				} else {
					RenderSystem.blendFuncSeparate(
						GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
						GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
						GlStateManager.SourceFactor.ONE,
						GlStateManager.DestFactor.ZERO
					);
					int i = 15;
					guiGraphics.blit(GUI_ICONS_LOCATION, (this.screenWidth - 15) / 2, (this.screenHeight - 15) / 2, 0, 0, 15, 15);
					if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.CROSSHAIR) {
						float f = this.minecraft.player.getAttackStrengthScale(0.0F);
						boolean bl = false;
						if (this.minecraft.crosshairPickEntity != null && this.minecraft.crosshairPickEntity instanceof LivingEntity && f >= 1.0F) {
							bl = this.minecraft.player.getCurrentItemAttackStrengthDelay() > 5.0F;
							bl &= this.minecraft.crosshairPickEntity.isAlive();
						}

						int j = this.screenHeight / 2 - 7 + 16;
						int k = this.screenWidth / 2 - 8;
						if (bl) {
							guiGraphics.blit(GUI_ICONS_LOCATION, k, j, 68, 94, 16, 16);
						} else if (f < 1.0F) {
							int l = (int)(f * 17.0F);
							guiGraphics.blit(GUI_ICONS_LOCATION, k, j, 36, 94, 16, 4);
							guiGraphics.blit(GUI_ICONS_LOCATION, k, j, 52, 94, l, 4);
						}
					}

					RenderSystem.defaultBlendFunc();
				}
			}
		}
	}

	private boolean canRenderCrosshairForSpectator(HitResult hitResult) {
		if (hitResult == null) {
			return false;
		} else if (hitResult.getType() == HitResult.Type.ENTITY) {
			return ((EntityHitResult)hitResult).getEntity() instanceof MenuProvider;
		} else if (hitResult.getType() == HitResult.Type.BLOCK) {
			BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
			Level level = this.minecraft.level;
			return level.getBlockState(blockPos).getMenuProvider(level, blockPos) != null;
		} else {
			return false;
		}
	}

	protected void renderEffects(GuiGraphics guiGraphics) {
		Collection<MobEffectInstance> collection = this.minecraft.player.getActiveEffects();
		if (!collection.isEmpty()) {
			if (this.minecraft.screen instanceof EffectRenderingInventoryScreen effectRenderingInventoryScreen && effectRenderingInventoryScreen.canSeeEffects()) {
				return;
			}

			RenderSystem.enableBlend();
			int i = 0;
			int j = 0;
			MobEffectTextureManager mobEffectTextureManager = this.minecraft.getMobEffectTextures();
			List<Runnable> list = Lists.<Runnable>newArrayListWithExpectedSize(collection.size());

			for (MobEffectInstance mobEffectInstance : Ordering.natural().reverse().sortedCopy(collection)) {
				MobEffect mobEffect = mobEffectInstance.getEffect();
				if (mobEffectInstance.showIcon()) {
					int k = this.screenWidth;
					int l = 1;
					if (this.minecraft.isDemo()) {
						l += 15;
					}

					if (mobEffect.isBeneficial()) {
						i++;
						k -= 25 * i;
					} else {
						j++;
						k -= 25 * j;
						l += 26;
					}

					float f = 1.0F;
					if (mobEffectInstance.isAmbient()) {
						guiGraphics.blit(AbstractContainerScreen.INVENTORY_LOCATION, k, l, 165, 166, 24, 24);
					} else {
						guiGraphics.blit(AbstractContainerScreen.INVENTORY_LOCATION, k, l, 141, 166, 24, 24);
						if (mobEffectInstance.endsWithin(200)) {
							int m = mobEffectInstance.getDuration();
							int n = 10 - m / 20;
							f = Mth.clamp((float)m / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F)
								+ Mth.cos((float)m * (float) Math.PI / 5.0F) * Mth.clamp((float)n / 10.0F * 0.25F, 0.0F, 0.25F);
						}
					}

					TextureAtlasSprite textureAtlasSprite = mobEffectTextureManager.get(mobEffect);
					int n = k;
					int o = l;
					float g = f;
					list.add((Runnable)() -> {
						guiGraphics.setColor(1.0F, 1.0F, 1.0F, g);
						guiGraphics.blit(n + 3, o + 3, 0, 18, 18, textureAtlasSprite);
						guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
					});
				}
			}

			list.forEach(Runnable::run);
		}
	}

	private void renderHotbar(float f, GuiGraphics guiGraphics) {
		Player player = this.getCameraPlayer();
		if (player != null) {
			ItemStack itemStack = player.getOffhandItem();
			HumanoidArm humanoidArm = player.getMainArm().getOpposite();
			int i = this.screenWidth / 2;
			int j = 182;
			int k = 91;
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(0.0F, 0.0F, -90.0F);
			guiGraphics.blit(WIDGETS_LOCATION, i - 91, this.screenHeight - 22, 0, 0, 182, 22);
			guiGraphics.blit(WIDGETS_LOCATION, i - 91 - 1 + player.getInventory().selected * 20, this.screenHeight - 22 - 1, 0, 22, 24, 22);
			if (!itemStack.isEmpty()) {
				if (humanoidArm == HumanoidArm.LEFT) {
					guiGraphics.blit(WIDGETS_LOCATION, i - 91 - 29, this.screenHeight - 23, 24, 22, 29, 24);
				} else {
					guiGraphics.blit(WIDGETS_LOCATION, i + 91, this.screenHeight - 23, 53, 22, 29, 24);
				}
			}

			guiGraphics.pose().popPose();
			int l = 1;

			for (int m = 0; m < 9; m++) {
				int n = i - 90 + m * 20 + 2;
				int o = this.screenHeight - 16 - 3;
				this.renderSlot(guiGraphics, n, o, f, player, player.getInventory().items.get(m), l++);
			}

			if (!itemStack.isEmpty()) {
				int m = this.screenHeight - 16 - 3;
				if (humanoidArm == HumanoidArm.LEFT) {
					this.renderSlot(guiGraphics, i - 91 - 26, m, f, player, itemStack, l++);
				} else {
					this.renderSlot(guiGraphics, i + 91 + 10, m, f, player, itemStack, l++);
				}
			}

			RenderSystem.enableBlend();
			if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR) {
				float g = this.minecraft.player.getAttackStrengthScale(0.0F);
				if (g < 1.0F) {
					int n = this.screenHeight - 20;
					int o = i + 91 + 6;
					if (humanoidArm == HumanoidArm.RIGHT) {
						o = i - 91 - 22;
					}

					int p = (int)(g * 19.0F);
					guiGraphics.blit(GUI_ICONS_LOCATION, o, n, 0, 94, 18, 18);
					guiGraphics.blit(GUI_ICONS_LOCATION, o, n + 18 - p, 18, 112 - p, 18, p);
				}
			}

			RenderSystem.disableBlend();
		}
	}

	public void renderJumpMeter(PlayerRideableJumping playerRideableJumping, GuiGraphics guiGraphics, int i) {
		this.minecraft.getProfiler().push("jumpBar");
		float f = this.minecraft.player.getJumpRidingScale();
		int j = 182;
		int k = (int)(f * 183.0F);
		int l = this.screenHeight - 32 + 3;
		guiGraphics.blit(GUI_ICONS_LOCATION, i, l, 0, 84, 182, 5);
		if (playerRideableJumping.getJumpCooldown() > 0) {
			guiGraphics.blit(GUI_ICONS_LOCATION, i, l, 0, 74, 182, 5);
		} else if (k > 0) {
			guiGraphics.blit(GUI_ICONS_LOCATION, i, l, 0, 89, k, 5);
		}

		this.minecraft.getProfiler().pop();
	}

	public void renderExperienceBar(GuiGraphics guiGraphics, int i) {
		this.minecraft.getProfiler().push("expBar");
		int j = this.minecraft.player.getXpNeededForNextLevel();
		if (j > 0) {
			int k = 182;
			int l = (int)(this.minecraft.player.experienceProgress * 183.0F);
			int m = this.screenHeight - 32 + 3;
			guiGraphics.blit(GUI_ICONS_LOCATION, i, m, 0, 64, 182, 5);
			if (l > 0) {
				guiGraphics.blit(GUI_ICONS_LOCATION, i, m, 0, 69, l, 5);
			}
		}

		this.minecraft.getProfiler().pop();
		if (this.minecraft.player.experienceLevel > 0) {
			this.minecraft.getProfiler().push("expLevel");
			String string = this.minecraft.player.experienceLevel + "";
			int l = (this.screenWidth - this.getFont().width(string)) / 2;
			int m = this.screenHeight - 31 - 4;
			guiGraphics.drawString(this.getFont(), string, l + 1, m, 0, false);
			guiGraphics.drawString(this.getFont(), string, l - 1, m, 0, false);
			guiGraphics.drawString(this.getFont(), string, l, m + 1, 0, false);
			guiGraphics.drawString(this.getFont(), string, l, m - 1, 0, false);
			guiGraphics.drawString(this.getFont(), string, l, m, 8453920, false);
			this.minecraft.getProfiler().pop();
		}
	}

	public void renderSelectedItemName(GuiGraphics guiGraphics) {
		this.minecraft.getProfiler().push("selectedItemName");
		if (this.toolHighlightTimer > 0 && !this.lastToolHighlight.isEmpty()) {
			MutableComponent mutableComponent = Component.empty().append(this.lastToolHighlight.getHoverName()).withStyle(this.lastToolHighlight.getRarity().color);
			if (this.lastToolHighlight.hasCustomHoverName()) {
				mutableComponent.withStyle(ChatFormatting.ITALIC);
			}

			int i = this.getFont().width(mutableComponent);
			int j = (this.screenWidth - i) / 2;
			int k = this.screenHeight - 59;
			if (!this.minecraft.gameMode.canHurtPlayer()) {
				k += 14;
			}

			int l = (int)((float)this.toolHighlightTimer * 256.0F / 10.0F);
			if (l > 255) {
				l = 255;
			}

			if (l > 0) {
				guiGraphics.fill(j - 2, k - 2, j + i + 2, k + 9 + 2, this.minecraft.options.getBackgroundColor(0));
				guiGraphics.drawString(this.getFont(), mutableComponent, j, k, 16777215 + (l << 24));
			}
		}

		this.minecraft.getProfiler().pop();
	}

	public void renderDemoOverlay(GuiGraphics guiGraphics) {
		this.minecraft.getProfiler().push("demo");
		Component component;
		if (this.minecraft.level.getGameTime() >= 120500L) {
			component = DEMO_EXPIRED_TEXT;
		} else {
			component = Component.translatable("demo.remainingTime", StringUtil.formatTickDuration((int)(120500L - this.minecraft.level.getGameTime())));
		}

		int i = this.getFont().width(component);
		guiGraphics.drawString(this.getFont(), component, this.screenWidth - i - 10, 5, 16777215);
		this.minecraft.getProfiler().pop();
	}

	private void displayScoreboardSidebar(GuiGraphics guiGraphics, Objective objective) {
		Scoreboard scoreboard = objective.getScoreboard();
		Collection<Score> collection = scoreboard.getPlayerScores(objective);
		List<Score> list = (List<Score>)collection.stream()
			.filter(score -> score.getOwner() != null && !score.getOwner().startsWith("#"))
			.collect(Collectors.toList());
		if (list.size() > 15) {
			collection = Lists.<Score>newArrayList(Iterables.skip(list, collection.size() - 15));
		} else {
			collection = list;
		}

		List<Pair<Score, Component>> list2 = Lists.<Pair<Score, Component>>newArrayListWithCapacity(collection.size());
		Component component = objective.getDisplayName();
		int i = this.getFont().width(component);
		int j = i;
		int k = this.getFont().width(": ");

		for (Score score : collection) {
			PlayerTeam playerTeam = scoreboard.getPlayersTeam(score.getOwner());
			Component component2 = PlayerTeam.formatNameForTeam(playerTeam, Component.literal(score.getOwner()));
			list2.add(Pair.of(score, component2));
			j = Math.max(j, this.getFont().width(component2) + k + this.getFont().width(Integer.toString(score.getScore())));
		}

		int l = collection.size() * 9;
		int m = this.screenHeight / 2 + l / 3;
		int n = 3;
		int o = this.screenWidth - j - 3;
		int p = 0;
		int q = this.minecraft.options.getBackgroundColor(0.3F);
		int r = this.minecraft.options.getBackgroundColor(0.4F);

		for (Pair<Score, Component> pair : list2) {
			p++;
			Score score2 = pair.getFirst();
			Component component3 = pair.getSecond();
			String string = "" + ChatFormatting.RED + score2.getScore();
			int t = m - p * 9;
			int u = this.screenWidth - 3 + 2;
			guiGraphics.fill(o - 2, t, u, t + 9, q);
			guiGraphics.drawString(this.getFont(), component3, o, t, -1, false);
			guiGraphics.drawString(this.getFont(), string, u - this.getFont().width(string), t, -1, false);
			if (p == collection.size()) {
				guiGraphics.fill(o - 2, t - 9 - 1, u, t - 1, r);
				guiGraphics.fill(o - 2, t - 1, u, t, q);
				guiGraphics.drawString(this.getFont(), component, o + j / 2 - i / 2, t - 9, -1, false);
			}
		}
	}

	private Player getCameraPlayer() {
		return !(this.minecraft.getCameraEntity() instanceof Player) ? null : (Player)this.minecraft.getCameraEntity();
	}

	private LivingEntity getPlayerVehicleWithHealth() {
		Player player = this.getCameraPlayer();
		if (player != null) {
			Entity entity = player.getVehicle();
			if (entity == null) {
				return null;
			}

			if (entity instanceof LivingEntity) {
				return (LivingEntity)entity;
			}
		}

		return null;
	}

	private int getVehicleMaxHearts(LivingEntity livingEntity) {
		if (livingEntity != null && livingEntity.showVehicleHealth()) {
			float f = livingEntity.getMaxHealth();
			int i = (int)(f + 0.5F) / 2;
			if (i > 30) {
				i = 30;
			}

			return i;
		} else {
			return 0;
		}
	}

	private int getVisibleVehicleHeartRows(int i) {
		return (int)Math.ceil((double)i / 10.0);
	}

	private void renderPlayerHealth(GuiGraphics guiGraphics) {
		Player player = this.getCameraPlayer();
		if (player != null) {
			int i = Mth.ceil(player.getHealth());
			boolean bl = this.healthBlinkTime > (long)this.tickCount && (this.healthBlinkTime - (long)this.tickCount) / 3L % 2L == 1L;
			long l = Util.getMillis();
			if (i < this.lastHealth && player.invulnerableTime > 0) {
				this.lastHealthTime = l;
				this.healthBlinkTime = (long)(this.tickCount + 20);
			} else if (i > this.lastHealth && player.invulnerableTime > 0) {
				this.lastHealthTime = l;
				this.healthBlinkTime = (long)(this.tickCount + 10);
			}

			if (l - this.lastHealthTime > 1000L) {
				this.lastHealth = i;
				this.displayHealth = i;
				this.lastHealthTime = l;
			}

			this.lastHealth = i;
			int j = this.displayHealth;
			this.random.setSeed((long)(this.tickCount * 312871));
			FoodData foodData = player.getFoodData();
			int k = foodData.getFoodLevel();
			int m = this.screenWidth / 2 - 91;
			int n = this.screenWidth / 2 + 91;
			int o = this.screenHeight - 39;
			float f = Math.max((float)player.getAttributeValue(Attributes.MAX_HEALTH), (float)Math.max(j, i));
			int p = Mth.ceil(player.getAbsorptionAmount());
			int q = Mth.ceil((f + (float)p) / 2.0F / 10.0F);
			int r = Math.max(10 - (q - 2), 3);
			int s = o - (q - 1) * r - 10;
			int t = o - 10;
			int u = player.getArmorValue();
			int v = -1;
			if (player.hasEffect(MobEffects.REGENERATION)) {
				v = this.tickCount % Mth.ceil(f + 5.0F);
			}

			this.minecraft.getProfiler().push("armor");

			for (int w = 0; w < 10; w++) {
				if (u > 0) {
					int x = m + w * 8;
					if (w * 2 + 1 < u) {
						guiGraphics.blit(GUI_ICONS_LOCATION, x, s, 34, 9, 9, 9);
					}

					if (w * 2 + 1 == u) {
						guiGraphics.blit(GUI_ICONS_LOCATION, x, s, 25, 9, 9, 9);
					}

					if (w * 2 + 1 > u) {
						guiGraphics.blit(GUI_ICONS_LOCATION, x, s, 16, 9, 9, 9);
					}
				}
			}

			this.minecraft.getProfiler().popPush("health");
			this.renderHearts(guiGraphics, player, m, o, r, v, f, i, j, p, bl);
			LivingEntity livingEntity = this.getPlayerVehicleWithHealth();
			int xx = this.getVehicleMaxHearts(livingEntity);
			if (xx == 0) {
				this.minecraft.getProfiler().popPush("food");

				for (int y = 0; y < 10; y++) {
					int z = o;
					int aa = 16;
					int ab = 0;
					if (player.hasEffect(MobEffects.HUNGER)) {
						aa += 36;
						ab = 13;
					}

					if (player.getFoodData().getSaturationLevel() <= 0.0F && this.tickCount % (k * 3 + 1) == 0) {
						z = o + (this.random.nextInt(3) - 1);
					}

					int ac = n - y * 8 - 9;
					guiGraphics.blit(GUI_ICONS_LOCATION, ac, z, 16 + ab * 9, 27, 9, 9);
					if (y * 2 + 1 < k) {
						guiGraphics.blit(GUI_ICONS_LOCATION, ac, z, aa + 36, 27, 9, 9);
					}

					if (y * 2 + 1 == k) {
						guiGraphics.blit(GUI_ICONS_LOCATION, ac, z, aa + 45, 27, 9, 9);
					}
				}

				t -= 10;
			}

			this.minecraft.getProfiler().popPush("air");
			int y = player.getMaxAirSupply();
			int zx = Math.min(player.getAirSupply(), y);
			if (player.isEyeInFluid(FluidTags.WATER) || zx < y) {
				int aax = this.getVisibleVehicleHeartRows(xx) - 1;
				t -= aax * 10;
				int abx = Mth.ceil((double)(zx - 2) * 10.0 / (double)y);
				int acx = Mth.ceil((double)zx * 10.0 / (double)y) - abx;

				for (int ad = 0; ad < abx + acx; ad++) {
					if (ad < abx) {
						guiGraphics.blit(GUI_ICONS_LOCATION, n - ad * 8 - 9, t, 16, 18, 9, 9);
					} else {
						guiGraphics.blit(GUI_ICONS_LOCATION, n - ad * 8 - 9, t, 25, 18, 9, 9);
					}
				}
			}

			this.minecraft.getProfiler().pop();
		}
	}

	private void renderHearts(GuiGraphics guiGraphics, Player player, int i, int j, int k, int l, float f, int m, int n, int o, boolean bl) {
		Gui.HeartType heartType = Gui.HeartType.forPlayer(player);
		int p = 9 * (player.level().getLevelData().isHardcore() ? 5 : 0);
		int q = Mth.ceil((double)f / 2.0);
		int r = Mth.ceil((double)o / 2.0);
		int s = q * 2;

		for (int t = q + r - 1; t >= 0; t--) {
			int u = t / 10;
			int v = t % 10;
			int w = i + v * 8;
			int x = j - u * k;
			if (m + o <= 4) {
				x += this.random.nextInt(2);
			}

			if (t < q && t == l) {
				x -= 2;
			}

			this.renderHeart(guiGraphics, Gui.HeartType.CONTAINER, w, x, p, bl, false);
			int y = t * 2;
			boolean bl2 = t >= q;
			if (bl2) {
				int z = y - s;
				if (z < o) {
					boolean bl3 = z + 1 == o;
					this.renderHeart(guiGraphics, heartType == Gui.HeartType.WITHERED ? heartType : Gui.HeartType.ABSORBING, w, x, p, false, bl3);
				}
			}

			if (bl && y < n) {
				boolean bl4 = y + 1 == n;
				this.renderHeart(guiGraphics, heartType, w, x, p, true, bl4);
			}

			if (y < m) {
				boolean bl4 = y + 1 == m;
				this.renderHeart(guiGraphics, heartType, w, x, p, false, bl4);
			}
		}
	}

	private void renderHeart(GuiGraphics guiGraphics, Gui.HeartType heartType, int i, int j, int k, boolean bl, boolean bl2) {
		guiGraphics.blit(GUI_ICONS_LOCATION, i, j, heartType.getX(bl2, bl), k, 9, 9);
	}

	private void renderVehicleHealth(GuiGraphics guiGraphics) {
		LivingEntity livingEntity = this.getPlayerVehicleWithHealth();
		if (livingEntity != null) {
			int i = this.getVehicleMaxHearts(livingEntity);
			if (i != 0) {
				int j = (int)Math.ceil((double)livingEntity.getHealth());
				this.minecraft.getProfiler().popPush("mountHealth");
				int k = this.screenHeight - 39;
				int l = this.screenWidth / 2 + 91;
				int m = k;
				int n = 0;

				for (boolean bl = false; i > 0; n += 20) {
					int o = Math.min(i, 10);
					i -= o;

					for (int p = 0; p < o; p++) {
						int q = 52;
						int r = 0;
						int s = l - p * 8 - 9;
						guiGraphics.blit(GUI_ICONS_LOCATION, s, m, 52 + r * 9, 9, 9, 9);
						if (p * 2 + 1 + n < j) {
							guiGraphics.blit(GUI_ICONS_LOCATION, s, m, 88, 9, 9, 9);
						}

						if (p * 2 + 1 + n == j) {
							guiGraphics.blit(GUI_ICONS_LOCATION, s, m, 97, 9, 9, 9);
						}
					}

					m -= 10;
				}
			}
		}
	}

	private void renderTextureOverlay(GuiGraphics guiGraphics, ResourceLocation resourceLocation, float f) {
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, f);
		guiGraphics.blit(resourceLocation, 0, 0, -90, 0.0F, 0.0F, this.screenWidth, this.screenHeight, this.screenWidth, this.screenHeight);
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
	}

	private void renderSpyglassOverlay(GuiGraphics guiGraphics, float f) {
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		float g = (float)Math.min(this.screenWidth, this.screenHeight);
		float i = Math.min((float)this.screenWidth / g, (float)this.screenHeight / g) * f;
		int j = Mth.floor(g * i);
		int k = Mth.floor(g * i);
		int l = (this.screenWidth - j) / 2;
		int m = (this.screenHeight - k) / 2;
		int n = l + j;
		int o = m + k;
		guiGraphics.blit(SPYGLASS_SCOPE_LOCATION, l, m, -90, 0.0F, 0.0F, j, k, j, k);
		guiGraphics.fill(0, o, this.screenWidth, this.screenHeight, -90, -16777216);
		guiGraphics.fill(0, 0, this.screenWidth, m, -90, -16777216);
		guiGraphics.fill(0, m, l, o, -90, -16777216);
		guiGraphics.fill(n, m, this.screenWidth, o, -90, -16777216);
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
	}

	private void updateVignetteBrightness(Entity entity) {
		if (entity != null) {
			BlockPos blockPos = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
			float f = LightTexture.getBrightness(entity.level().dimensionType(), entity.level().getMaxLocalRawBrightness(blockPos));
			float g = Mth.clamp(1.0F - f, 0.0F, 1.0F);
			this.vignetteBrightness = this.vignetteBrightness + (g - this.vignetteBrightness) * 0.01F;
		}
	}

	private void renderVignette(GuiGraphics guiGraphics, Entity entity) {
		WorldBorder worldBorder = this.minecraft.level.getWorldBorder();
		float f = (float)worldBorder.getDistanceToBorder(entity);
		double d = Math.min(worldBorder.getLerpSpeed() * (double)worldBorder.getWarningTime() * 1000.0, Math.abs(worldBorder.getLerpTarget() - worldBorder.getSize()));
		double e = Math.max((double)worldBorder.getWarningBlocks(), d);
		if ((double)f < e) {
			f = 1.0F - (float)((double)f / e);
		} else {
			f = 0.0F;
		}

		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.blendFuncSeparate(
			GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
		);
		if (f > 0.0F) {
			f = Mth.clamp(f, 0.0F, 1.0F);
			guiGraphics.setColor(0.0F, f, f, 1.0F);
		} else {
			float g = this.vignetteBrightness;
			g = Mth.clamp(g, 0.0F, 1.0F);
			guiGraphics.setColor(g, g, g, 1.0F);
		}

		guiGraphics.blit(VIGNETTE_LOCATION, 0, 0, -90, 0.0F, 0.0F, this.screenWidth, this.screenHeight, this.screenWidth, this.screenHeight);
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.defaultBlendFunc();
	}

	private void renderPortalOverlay(GuiGraphics guiGraphics, float f) {
		if (f < 1.0F) {
			f *= f;
			f *= f;
			f = f * 0.8F + 0.2F;
		}

		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, f);
		TextureAtlasSprite textureAtlasSprite = this.minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.NETHER_PORTAL.defaultBlockState());
		guiGraphics.blit(0, 0, -90, this.screenWidth, this.screenHeight, textureAtlasSprite);
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
	}

	private void renderSlot(GuiGraphics guiGraphics, int i, int j, float f, Player player, ItemStack itemStack, int k) {
		if (!itemStack.isEmpty()) {
			float g = (float)itemStack.getPopTime() - f;
			if (g > 0.0F) {
				float h = 1.0F + g / 5.0F;
				guiGraphics.pose().pushPose();
				guiGraphics.pose().translate((float)(i + 8), (float)(j + 12), 0.0F);
				guiGraphics.pose().scale(1.0F / h, (h + 1.0F) / 2.0F, 1.0F);
				guiGraphics.pose().translate((float)(-(i + 8)), (float)(-(j + 12)), 0.0F);
			}

			guiGraphics.renderItem(player, itemStack, i, j, k);
			if (g > 0.0F) {
				guiGraphics.pose().popPose();
			}

			guiGraphics.renderItemDecorations(this.minecraft.font, itemStack, i, j);
		}
	}

	public void tick(boolean bl) {
		this.tickAutosaveIndicator();
		if (!bl) {
			this.tick();
		}
	}

	private void tick() {
		if (this.overlayMessageTime > 0) {
			this.overlayMessageTime--;
		}

		if (this.titleTime > 0) {
			this.titleTime--;
			if (this.titleTime <= 0) {
				this.title = null;
				this.subtitle = null;
			}
		}

		this.tickCount++;
		Entity entity = this.minecraft.getCameraEntity();
		if (entity != null) {
			this.updateVignetteBrightness(entity);
		}

		if (this.minecraft.player != null) {
			ItemStack itemStack = this.minecraft.player.getInventory().getSelected();
			if (itemStack.isEmpty()) {
				this.toolHighlightTimer = 0;
			} else if (this.lastToolHighlight.isEmpty()
				|| !itemStack.is(this.lastToolHighlight.getItem())
				|| !itemStack.getHoverName().equals(this.lastToolHighlight.getHoverName())) {
				this.toolHighlightTimer = (int)(40.0 * this.minecraft.options.notificationDisplayTime().get());
			} else if (this.toolHighlightTimer > 0) {
				this.toolHighlightTimer--;
			}

			this.lastToolHighlight = itemStack;
		}

		this.chat.tick();
	}

	private void tickAutosaveIndicator() {
		MinecraftServer minecraftServer = this.minecraft.getSingleplayerServer();
		boolean bl = minecraftServer != null && minecraftServer.isCurrentlySaving();
		this.lastAutosaveIndicatorValue = this.autosaveIndicatorValue;
		this.autosaveIndicatorValue = Mth.lerp(0.2F, this.autosaveIndicatorValue, bl ? 1.0F : 0.0F);
	}

	public void setNowPlaying(Component component) {
		Component component2 = Component.translatable("record.nowPlaying", component);
		this.setOverlayMessage(component2, true);
		this.minecraft.getNarrator().sayNow(component2);
	}

	public void setOverlayMessage(Component component, boolean bl) {
		this.setChatDisabledByPlayerShown(false);
		this.overlayMessageString = component;
		this.overlayMessageTime = 60;
		this.animateOverlayMessageColor = bl;
	}

	public void setChatDisabledByPlayerShown(boolean bl) {
		this.chatDisabledByPlayerShown = bl;
	}

	public boolean isShowingChatDisabledByPlayer() {
		return this.chatDisabledByPlayerShown && this.overlayMessageTime > 0;
	}

	public void setTimes(int i, int j, int k) {
		if (i >= 0) {
			this.titleFadeInTime = i;
		}

		if (j >= 0) {
			this.titleStayTime = j;
		}

		if (k >= 0) {
			this.titleFadeOutTime = k;
		}

		if (this.titleTime > 0) {
			this.titleTime = this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime;
		}
	}

	public void setSubtitle(Component component) {
		this.subtitle = component;
	}

	public void setTitle(Component component) {
		this.title = component;
		this.titleTime = this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime;
	}

	public void clear() {
		this.title = null;
		this.subtitle = null;
		this.titleTime = 0;
	}

	public ChatComponent getChat() {
		return this.chat;
	}

	public int getGuiTicks() {
		return this.tickCount;
	}

	public Font getFont() {
		return this.minecraft.font;
	}

	public SpectatorGui getSpectatorGui() {
		return this.spectatorGui;
	}

	public PlayerTabOverlay getTabList() {
		return this.tabList;
	}

	public void onDisconnected() {
		this.tabList.reset();
		this.bossOverlay.reset();
		this.minecraft.getToasts().clear();
		this.minecraft.options.renderDebug = false;
		this.chat.clearMessages(true);
	}

	public BossHealthOverlay getBossOverlay() {
		return this.bossOverlay;
	}

	public void clearCache() {
		this.debugScreen.clearChunkCache();
	}

	private void renderSavingIndicator(GuiGraphics guiGraphics) {
		if (this.minecraft.options.showAutosaveIndicator().get() && (this.autosaveIndicatorValue > 0.0F || this.lastAutosaveIndicatorValue > 0.0F)) {
			int i = Mth.floor(255.0F * Mth.clamp(Mth.lerp(this.minecraft.getFrameTime(), this.lastAutosaveIndicatorValue, this.autosaveIndicatorValue), 0.0F, 1.0F));
			if (i > 8) {
				Font font = this.getFont();
				int j = font.width(SAVING_TEXT);
				int k = 16777215 | i << 24 & 0xFF000000;
				guiGraphics.drawString(font, SAVING_TEXT, this.screenWidth - j - 10, this.screenHeight - 15, k);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	static enum HeartType {
		CONTAINER(0, false),
		NORMAL(2, true),
		POISIONED(4, true),
		WITHERED(6, true),
		ABSORBING(8, false),
		FROZEN(9, false);

		private final int index;
		private final boolean canBlink;

		private HeartType(int j, boolean bl) {
			this.index = j;
			this.canBlink = bl;
		}

		public int getX(boolean bl, boolean bl2) {
			int i;
			if (this == CONTAINER) {
				i = bl2 ? 1 : 0;
			} else {
				int j = bl ? 1 : 0;
				int k = this.canBlink && bl2 ? 2 : 0;
				i = j + k;
			}

			return 16 + (this.index * 2 + i) * 9;
		}

		static Gui.HeartType forPlayer(Player player) {
			Gui.HeartType heartType;
			if (player.hasEffect(MobEffects.POISON)) {
				heartType = POISIONED;
			} else if (player.hasEffect(MobEffects.WITHER)) {
				heartType = WITHERED;
			} else if (player.isFullyFrozen()) {
				heartType = FROZEN;
			} else {
				heartType = NORMAL;
			}

			return heartType;
		}
	}
}
