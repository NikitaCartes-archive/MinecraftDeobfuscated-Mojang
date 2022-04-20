package net.minecraft.client.gui;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Vector3f;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import net.minecraft.client.gui.chat.ChatListener;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.chat.OverlayChatListener;
import net.minecraft.client.gui.chat.StandardChatListener;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringDecomposer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
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
import org.apache.commons.lang3.StringUtils;

@Environment(EnvType.CLIENT)
public class Gui extends GuiComponent {
	private static final ResourceLocation VIGNETTE_LOCATION = new ResourceLocation("textures/misc/vignette.png");
	private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
	private static final ResourceLocation PUMPKIN_BLUR_LOCATION = new ResourceLocation("textures/misc/pumpkinblur.png");
	private static final ResourceLocation SPYGLASS_SCOPE_LOCATION = new ResourceLocation("textures/misc/spyglass_scope.png");
	private static final ResourceLocation POWDER_SNOW_OUTLINE_LOCATION = new ResourceLocation("textures/misc/powder_snow_outline.png");
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
	private final Map<ChatType, List<ChatListener>> chatListeners = Maps.<ChatType, List<ChatListener>>newHashMap();
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

		for (ChatType chatType : ChatType.values()) {
			this.chatListeners.put(chatType, Lists.newArrayList());
		}

		ChatListener chatListener = NarratorChatListener.INSTANCE;
		((List)this.chatListeners.get(ChatType.CHAT)).add(new StandardChatListener(minecraft));
		((List)this.chatListeners.get(ChatType.CHAT)).add(chatListener);
		((List)this.chatListeners.get(ChatType.SYSTEM)).add(new StandardChatListener(minecraft));
		((List)this.chatListeners.get(ChatType.SYSTEM)).add(chatListener);
		((List)this.chatListeners.get(ChatType.GAME_INFO)).add(new OverlayChatListener(minecraft));
		this.resetTitleTimes();
	}

	public void resetTitleTimes() {
		this.titleFadeInTime = 10;
		this.titleStayTime = 70;
		this.titleFadeOutTime = 20;
	}

	public void render(PoseStack poseStack, float f) {
		this.screenWidth = this.minecraft.getWindow().getGuiScaledWidth();
		this.screenHeight = this.minecraft.getWindow().getGuiScaledHeight();
		Font font = this.getFont();
		RenderSystem.enableBlend();
		if (Minecraft.useFancyGraphics()) {
			this.renderVignette(this.minecraft.getCameraEntity());
		} else {
			RenderSystem.enableDepthTest();
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.defaultBlendFunc();
		}

		float g = this.minecraft.getDeltaFrameTime();
		this.scopeScale = Mth.lerp(0.5F * g, this.scopeScale, 1.125F);
		if (this.minecraft.options.getCameraType().isFirstPerson()) {
			if (this.minecraft.player.isScoping()) {
				this.renderSpyglassOverlay(this.scopeScale);
			} else {
				this.scopeScale = 0.5F;
				ItemStack itemStack = this.minecraft.player.getInventory().getArmor(3);
				if (itemStack.is(Blocks.CARVED_PUMPKIN.asItem())) {
					this.renderTextureOverlay(PUMPKIN_BLUR_LOCATION, 1.0F);
				}
			}
		}

		if (this.minecraft.player.getTicksFrozen() > 0) {
			this.renderTextureOverlay(POWDER_SNOW_OUTLINE_LOCATION, this.minecraft.player.getPercentFrozen());
		}

		float h = Mth.lerp(f, this.minecraft.player.oPortalTime, this.minecraft.player.portalTime);
		if (h > 0.0F && !this.minecraft.player.hasEffect(MobEffects.CONFUSION)) {
			this.renderPortalOverlay(h);
		}

		if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
			this.spectatorGui.renderHotbar(poseStack);
		} else if (!this.minecraft.options.hideGui) {
			this.renderHotbar(f, poseStack);
		}

		if (!this.minecraft.options.hideGui) {
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
			RenderSystem.enableBlend();
			this.renderCrosshair(poseStack);
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.defaultBlendFunc();
			this.minecraft.getProfiler().push("bossHealth");
			this.bossOverlay.render(poseStack);
			this.minecraft.getProfiler().pop();
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
			if (this.minecraft.gameMode.canHurtPlayer()) {
				this.renderPlayerHealth(poseStack);
			}

			this.renderVehicleHealth(poseStack);
			RenderSystem.disableBlend();
			int i = this.screenWidth / 2 - 91;
			if (this.minecraft.player.isRidingJumpable()) {
				this.renderJumpMeter(poseStack, i);
			} else if (this.minecraft.gameMode.hasExperience()) {
				this.renderExperienceBar(poseStack, i);
			}

			if (this.minecraft.options.heldItemTooltips && this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
				this.renderSelectedItemName(poseStack);
			} else if (this.minecraft.player.isSpectator()) {
				this.spectatorGui.renderTooltip(poseStack);
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
			fill(poseStack, 0, 0, this.screenWidth, this.screenHeight, l);
			RenderSystem.enableDepthTest();
			this.minecraft.getProfiler().pop();
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		}

		if (this.minecraft.isDemo()) {
			this.renderDemoOverlay(poseStack);
		}

		this.renderEffects(poseStack);
		if (this.minecraft.options.renderDebug) {
			this.debugScreen.render(poseStack);
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
					poseStack.pushPose();
					poseStack.translate((double)(this.screenWidth / 2), (double)(this.screenHeight - 68), 0.0);
					RenderSystem.enableBlend();
					RenderSystem.defaultBlendFunc();
					int l = 16777215;
					if (this.animateOverlayMessageColor) {
						l = Mth.hsvToRgb(j / 50.0F, 0.7F, 0.6F) & 16777215;
					}

					int n = m << 24 & 0xFF000000;
					int o = font.width(this.overlayMessageString);
					this.drawBackdrop(poseStack, font, -4, o, 16777215 | n);
					font.draw(poseStack, this.overlayMessageString, (float)(-o / 2), -4.0F, l | n);
					RenderSystem.disableBlend();
					poseStack.popPose();
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
					poseStack.pushPose();
					poseStack.translate((double)(this.screenWidth / 2), (double)(this.screenHeight / 2), 0.0);
					RenderSystem.enableBlend();
					RenderSystem.defaultBlendFunc();
					poseStack.pushPose();
					poseStack.scale(4.0F, 4.0F, 4.0F);
					int l = mx << 24 & 0xFF000000;
					int n = font.width(this.title);
					this.drawBackdrop(poseStack, font, -10, n, 16777215 | l);
					font.drawShadow(poseStack, this.title, (float)(-n / 2), -10.0F, 16777215 | l);
					poseStack.popPose();
					if (this.subtitle != null) {
						poseStack.pushPose();
						poseStack.scale(2.0F, 2.0F, 2.0F);
						int o = font.width(this.subtitle);
						this.drawBackdrop(poseStack, font, 5, o, 16777215 | l);
						font.drawShadow(poseStack, this.subtitle, (float)(-o / 2), 5.0F, 16777215 | l);
						poseStack.popPose();
					}

					RenderSystem.disableBlend();
					poseStack.popPose();
				}

				this.minecraft.getProfiler().pop();
			}

			this.subtitleOverlay.render(poseStack);
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
				this.displayScoreboardSidebar(poseStack, objective2);
			}

			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			poseStack.pushPose();
			poseStack.translate(0.0, (double)(this.screenHeight - 48), 0.0);
			this.minecraft.getProfiler().push("chat");
			this.chat.render(poseStack, this.tickCount);
			this.minecraft.getProfiler().pop();
			poseStack.popPose();
			objective2 = scoreboard.getDisplayObjective(0);
			if (!this.minecraft.options.keyPlayerList.isDown()
				|| this.minecraft.isLocalServer() && this.minecraft.player.connection.getOnlinePlayers().size() <= 1 && objective2 == null) {
				this.tabList.setVisible(false);
			} else {
				this.tabList.setVisible(true);
				this.tabList.render(poseStack, this.screenWidth, scoreboard, objective2);
			}

			this.renderSavingIndicator(poseStack);
		}

		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}

	private void drawBackdrop(PoseStack poseStack, Font font, int i, int j, int k) {
		int l = this.minecraft.options.getBackgroundColor(0.0F);
		if (l != 0) {
			int m = -j / 2;
			fill(poseStack, m - 2, i - 2, m + j + 2, i + 9 + 2, FastColor.ARGB32.multiply(l, k));
		}
	}

	private void renderCrosshair(PoseStack poseStack) {
		Options options = this.minecraft.options;
		if (options.getCameraType().isFirstPerson()) {
			if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR || this.canRenderCrosshairForSpectator(this.minecraft.hitResult)) {
				if (options.renderDebug && !options.hideGui && !this.minecraft.player.isReducedDebugInfo() && !options.reducedDebugInfo().get()) {
					Camera camera = this.minecraft.gameRenderer.getMainCamera();
					PoseStack poseStack2 = RenderSystem.getModelViewStack();
					poseStack2.pushPose();
					poseStack2.translate((double)(this.screenWidth / 2), (double)(this.screenHeight / 2), (double)this.getBlitOffset());
					poseStack2.mulPose(Vector3f.XN.rotationDegrees(camera.getXRot()));
					poseStack2.mulPose(Vector3f.YP.rotationDegrees(camera.getYRot()));
					poseStack2.scale(-1.0F, -1.0F, -1.0F);
					RenderSystem.applyModelViewMatrix();
					RenderSystem.renderCrosshair(10);
					poseStack2.popPose();
					RenderSystem.applyModelViewMatrix();
				} else {
					RenderSystem.blendFuncSeparate(
						GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
						GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
						GlStateManager.SourceFactor.ONE,
						GlStateManager.DestFactor.ZERO
					);
					int i = 15;
					this.blit(poseStack, (this.screenWidth - 15) / 2, (this.screenHeight - 15) / 2, 0, 0, 15, 15);
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
							this.blit(poseStack, k, j, 68, 94, 16, 16);
						} else if (f < 1.0F) {
							int l = (int)(f * 17.0F);
							this.blit(poseStack, k, j, 36, 94, 16, 4);
							this.blit(poseStack, k, j, 52, 94, l, 4);
						}
					}
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

	protected void renderEffects(PoseStack poseStack) {
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
			RenderSystem.setShaderTexture(0, AbstractContainerScreen.INVENTORY_LOCATION);

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

					RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
					float f = 1.0F;
					if (mobEffectInstance.isAmbient()) {
						this.blit(poseStack, k, l, 165, 166, 24, 24);
					} else {
						this.blit(poseStack, k, l, 141, 166, 24, 24);
						if (mobEffectInstance.getDuration() <= 200) {
							int m = 10 - mobEffectInstance.getDuration() / 20;
							f = Mth.clamp((float)mobEffectInstance.getDuration() / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F)
								+ Mth.cos((float)mobEffectInstance.getDuration() * (float) Math.PI / 5.0F) * Mth.clamp((float)m / 10.0F * 0.25F, 0.0F, 0.25F);
						}
					}

					TextureAtlasSprite textureAtlasSprite = mobEffectTextureManager.get(mobEffect);
					int n = k;
					int o = l;
					float g = f;
					list.add((Runnable)() -> {
						RenderSystem.setShaderTexture(0, textureAtlasSprite.atlas().location());
						RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, g);
						blit(poseStack, n + 3, o + 3, this.getBlitOffset(), 18, 18, textureAtlasSprite);
					});
				}
			}

			list.forEach(Runnable::run);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		}
	}

	private void renderHotbar(float f, PoseStack poseStack) {
		Player player = this.getCameraPlayer();
		if (player != null) {
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
			ItemStack itemStack = player.getOffhandItem();
			HumanoidArm humanoidArm = player.getMainArm().getOpposite();
			int i = this.screenWidth / 2;
			int j = this.getBlitOffset();
			int k = 182;
			int l = 91;
			this.setBlitOffset(-90);
			this.blit(poseStack, i - 91, this.screenHeight - 22, 0, 0, 182, 22);
			this.blit(poseStack, i - 91 - 1 + player.getInventory().selected * 20, this.screenHeight - 22 - 1, 0, 22, 24, 22);
			if (!itemStack.isEmpty()) {
				if (humanoidArm == HumanoidArm.LEFT) {
					this.blit(poseStack, i - 91 - 29, this.screenHeight - 23, 24, 22, 29, 24);
				} else {
					this.blit(poseStack, i + 91, this.screenHeight - 23, 53, 22, 29, 24);
				}
			}

			this.setBlitOffset(j);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			int m = 1;

			for (int n = 0; n < 9; n++) {
				int o = i - 90 + n * 20 + 2;
				int p = this.screenHeight - 16 - 3;
				this.renderSlot(o, p, f, player, player.getInventory().items.get(n), m++);
			}

			if (!itemStack.isEmpty()) {
				int n = this.screenHeight - 16 - 3;
				if (humanoidArm == HumanoidArm.LEFT) {
					this.renderSlot(i - 91 - 26, n, f, player, itemStack, m++);
				} else {
					this.renderSlot(i + 91 + 10, n, f, player, itemStack, m++);
				}
			}

			if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR) {
				float g = this.minecraft.player.getAttackStrengthScale(0.0F);
				if (g < 1.0F) {
					int o = this.screenHeight - 20;
					int p = i + 91 + 6;
					if (humanoidArm == HumanoidArm.RIGHT) {
						p = i - 91 - 22;
					}

					RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
					int q = (int)(g * 19.0F);
					RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
					this.blit(poseStack, p, o, 0, 94, 18, 18);
					this.blit(poseStack, p, o + 18 - q, 18, 112 - q, 18, q);
				}
			}

			RenderSystem.disableBlend();
		}
	}

	public void renderJumpMeter(PoseStack poseStack, int i) {
		this.minecraft.getProfiler().push("jumpBar");
		RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
		float f = this.minecraft.player.getJumpRidingScale();
		int j = 182;
		int k = (int)(f * 183.0F);
		int l = this.screenHeight - 32 + 3;
		this.blit(poseStack, i, l, 0, 84, 182, 5);
		if (k > 0) {
			this.blit(poseStack, i, l, 0, 89, k, 5);
		}

		this.minecraft.getProfiler().pop();
	}

	public void renderExperienceBar(PoseStack poseStack, int i) {
		this.minecraft.getProfiler().push("expBar");
		RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
		int j = this.minecraft.player.getXpNeededForNextLevel();
		if (j > 0) {
			int k = 182;
			int l = (int)(this.minecraft.player.experienceProgress * 183.0F);
			int m = this.screenHeight - 32 + 3;
			this.blit(poseStack, i, m, 0, 64, 182, 5);
			if (l > 0) {
				this.blit(poseStack, i, m, 0, 69, l, 5);
			}
		}

		this.minecraft.getProfiler().pop();
		if (this.minecraft.player.experienceLevel > 0) {
			this.minecraft.getProfiler().push("expLevel");
			String string = this.minecraft.player.experienceLevel + "";
			int l = (this.screenWidth - this.getFont().width(string)) / 2;
			int m = this.screenHeight - 31 - 4;
			this.getFont().draw(poseStack, string, (float)(l + 1), (float)m, 0);
			this.getFont().draw(poseStack, string, (float)(l - 1), (float)m, 0);
			this.getFont().draw(poseStack, string, (float)l, (float)(m + 1), 0);
			this.getFont().draw(poseStack, string, (float)l, (float)(m - 1), 0);
			this.getFont().draw(poseStack, string, (float)l, (float)m, 8453920);
			this.minecraft.getProfiler().pop();
		}
	}

	public void renderSelectedItemName(PoseStack poseStack) {
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
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				fill(poseStack, j - 2, k - 2, j + i + 2, k + 9 + 2, this.minecraft.options.getBackgroundColor(0));
				this.getFont().drawShadow(poseStack, mutableComponent, (float)j, (float)k, 16777215 + (l << 24));
				RenderSystem.disableBlend();
			}
		}

		this.minecraft.getProfiler().pop();
	}

	public void renderDemoOverlay(PoseStack poseStack) {
		this.minecraft.getProfiler().push("demo");
		Component component;
		if (this.minecraft.level.getGameTime() >= 120500L) {
			component = DEMO_EXPIRED_TEXT;
		} else {
			component = Component.translatable("demo.remainingTime", StringUtil.formatTickDuration((int)(120500L - this.minecraft.level.getGameTime())));
		}

		int i = this.getFont().width(component);
		this.getFont().drawShadow(poseStack, component, (float)(this.screenWidth - i - 10), 5.0F, 16777215);
		this.minecraft.getProfiler().pop();
	}

	private void displayScoreboardSidebar(PoseStack poseStack, Objective objective) {
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
			fill(poseStack, o - 2, t, u, t + 9, q);
			this.getFont().draw(poseStack, component3, (float)o, (float)t, -1);
			this.getFont().draw(poseStack, string, (float)(u - this.getFont().width(string)), (float)t, -1);
			if (p == collection.size()) {
				fill(poseStack, o - 2, t - 9 - 1, u, t - 1, r);
				fill(poseStack, o - 2, t - 1, u, t, q);
				this.getFont().draw(poseStack, component, (float)(o + j / 2 - i / 2), (float)(t - 9), -1);
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

	private void renderPlayerHealth(PoseStack poseStack) {
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
						this.blit(poseStack, x, s, 34, 9, 9, 9);
					}

					if (w * 2 + 1 == u) {
						this.blit(poseStack, x, s, 25, 9, 9, 9);
					}

					if (w * 2 + 1 > u) {
						this.blit(poseStack, x, s, 16, 9, 9, 9);
					}
				}
			}

			this.minecraft.getProfiler().popPush("health");
			this.renderHearts(poseStack, player, m, o, r, v, f, i, j, p, bl);
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
					this.blit(poseStack, ac, z, 16 + ab * 9, 27, 9, 9);
					if (y * 2 + 1 < k) {
						this.blit(poseStack, ac, z, aa + 36, 27, 9, 9);
					}

					if (y * 2 + 1 == k) {
						this.blit(poseStack, ac, z, aa + 45, 27, 9, 9);
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
						this.blit(poseStack, n - ad * 8 - 9, t, 16, 18, 9, 9);
					} else {
						this.blit(poseStack, n - ad * 8 - 9, t, 25, 18, 9, 9);
					}
				}
			}

			this.minecraft.getProfiler().pop();
		}
	}

	private void renderHearts(PoseStack poseStack, Player player, int i, int j, int k, int l, float f, int m, int n, int o, boolean bl) {
		Gui.HeartType heartType = Gui.HeartType.forPlayer(player);
		int p = 9 * (player.level.getLevelData().isHardcore() ? 5 : 0);
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

			this.renderHeart(poseStack, Gui.HeartType.CONTAINER, w, x, p, bl, false);
			int y = t * 2;
			boolean bl2 = t >= q;
			if (bl2) {
				int z = y - s;
				if (z < o) {
					boolean bl3 = z + 1 == o;
					this.renderHeart(poseStack, heartType == Gui.HeartType.WITHERED ? heartType : Gui.HeartType.ABSORBING, w, x, p, false, bl3);
				}
			}

			if (bl && y < n) {
				boolean bl4 = y + 1 == n;
				this.renderHeart(poseStack, heartType, w, x, p, true, bl4);
			}

			if (y < m) {
				boolean bl4 = y + 1 == m;
				this.renderHeart(poseStack, heartType, w, x, p, false, bl4);
			}
		}
	}

	private void renderHeart(PoseStack poseStack, Gui.HeartType heartType, int i, int j, int k, boolean bl, boolean bl2) {
		this.blit(poseStack, i, j, heartType.getX(bl2, bl), k, 9, 9);
	}

	private void renderVehicleHealth(PoseStack poseStack) {
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
						this.blit(poseStack, s, m, 52 + r * 9, 9, 9, 9);
						if (p * 2 + 1 + n < j) {
							this.blit(poseStack, s, m, 88, 9, 9, 9);
						}

						if (p * 2 + 1 + n == j) {
							this.blit(poseStack, s, m, 97, 9, 9, 9);
						}
					}

					m -= 10;
				}
			}
		}
	}

	private void renderTextureOverlay(ResourceLocation resourceLocation, float f) {
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, f);
		RenderSystem.setShaderTexture(0, resourceLocation);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(0.0, (double)this.screenHeight, -90.0).uv(0.0F, 1.0F).endVertex();
		bufferBuilder.vertex((double)this.screenWidth, (double)this.screenHeight, -90.0).uv(1.0F, 1.0F).endVertex();
		bufferBuilder.vertex((double)this.screenWidth, 0.0, -90.0).uv(1.0F, 0.0F).endVertex();
		bufferBuilder.vertex(0.0, 0.0, -90.0).uv(0.0F, 0.0F).endVertex();
		tesselator.end();
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}

	private void renderSpyglassOverlay(float f) {
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, SPYGLASS_SCOPE_LOCATION);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		float g = (float)Math.min(this.screenWidth, this.screenHeight);
		float i = Math.min((float)this.screenWidth / g, (float)this.screenHeight / g) * f;
		float j = g * i;
		float k = g * i;
		float l = ((float)this.screenWidth - j) / 2.0F;
		float m = ((float)this.screenHeight - k) / 2.0F;
		float n = l + j;
		float o = m + k;
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex((double)l, (double)o, -90.0).uv(0.0F, 1.0F).endVertex();
		bufferBuilder.vertex((double)n, (double)o, -90.0).uv(1.0F, 1.0F).endVertex();
		bufferBuilder.vertex((double)n, (double)m, -90.0).uv(1.0F, 0.0F).endVertex();
		bufferBuilder.vertex((double)l, (double)m, -90.0).uv(0.0F, 0.0F).endVertex();
		tesselator.end();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.disableTexture();
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		bufferBuilder.vertex(0.0, (double)this.screenHeight, -90.0).color(0, 0, 0, 255).endVertex();
		bufferBuilder.vertex((double)this.screenWidth, (double)this.screenHeight, -90.0).color(0, 0, 0, 255).endVertex();
		bufferBuilder.vertex((double)this.screenWidth, (double)o, -90.0).color(0, 0, 0, 255).endVertex();
		bufferBuilder.vertex(0.0, (double)o, -90.0).color(0, 0, 0, 255).endVertex();
		bufferBuilder.vertex(0.0, (double)m, -90.0).color(0, 0, 0, 255).endVertex();
		bufferBuilder.vertex((double)this.screenWidth, (double)m, -90.0).color(0, 0, 0, 255).endVertex();
		bufferBuilder.vertex((double)this.screenWidth, 0.0, -90.0).color(0, 0, 0, 255).endVertex();
		bufferBuilder.vertex(0.0, 0.0, -90.0).color(0, 0, 0, 255).endVertex();
		bufferBuilder.vertex(0.0, (double)o, -90.0).color(0, 0, 0, 255).endVertex();
		bufferBuilder.vertex((double)l, (double)o, -90.0).color(0, 0, 0, 255).endVertex();
		bufferBuilder.vertex((double)l, (double)m, -90.0).color(0, 0, 0, 255).endVertex();
		bufferBuilder.vertex(0.0, (double)m, -90.0).color(0, 0, 0, 255).endVertex();
		bufferBuilder.vertex((double)n, (double)o, -90.0).color(0, 0, 0, 255).endVertex();
		bufferBuilder.vertex((double)this.screenWidth, (double)o, -90.0).color(0, 0, 0, 255).endVertex();
		bufferBuilder.vertex((double)this.screenWidth, (double)m, -90.0).color(0, 0, 0, 255).endVertex();
		bufferBuilder.vertex((double)n, (double)m, -90.0).color(0, 0, 0, 255).endVertex();
		tesselator.end();
		RenderSystem.enableTexture();
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}

	private void updateVignetteBrightness(Entity entity) {
		if (entity != null) {
			BlockPos blockPos = new BlockPos(entity.getX(), entity.getEyeY(), entity.getZ());
			float f = LightTexture.getBrightness(entity.level.dimensionType(), entity.level.getMaxLocalRawBrightness(blockPos));
			float g = Mth.clamp(1.0F - f, 0.0F, 1.0F);
			this.vignetteBrightness = this.vignetteBrightness + (g - this.vignetteBrightness) * 0.01F;
		}
	}

	private void renderVignette(Entity entity) {
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
			RenderSystem.setShaderColor(0.0F, f, f, 1.0F);
		} else {
			float g = this.vignetteBrightness;
			g = Mth.clamp(g, 0.0F, 1.0F);
			RenderSystem.setShaderColor(g, g, g, 1.0F);
		}

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, VIGNETTE_LOCATION);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(0.0, (double)this.screenHeight, -90.0).uv(0.0F, 1.0F).endVertex();
		bufferBuilder.vertex((double)this.screenWidth, (double)this.screenHeight, -90.0).uv(1.0F, 1.0F).endVertex();
		bufferBuilder.vertex((double)this.screenWidth, 0.0, -90.0).uv(1.0F, 0.0F).endVertex();
		bufferBuilder.vertex(0.0, 0.0, -90.0).uv(0.0F, 0.0F).endVertex();
		tesselator.end();
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.defaultBlendFunc();
	}

	private void renderPortalOverlay(float f) {
		if (f < 1.0F) {
			f *= f;
			f *= f;
			f = f * 0.8F + 0.2F;
		}

		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, f);
		RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		TextureAtlasSprite textureAtlasSprite = this.minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.NETHER_PORTAL.defaultBlockState());
		float g = textureAtlasSprite.getU0();
		float h = textureAtlasSprite.getV0();
		float i = textureAtlasSprite.getU1();
		float j = textureAtlasSprite.getV1();
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(0.0, (double)this.screenHeight, -90.0).uv(g, j).endVertex();
		bufferBuilder.vertex((double)this.screenWidth, (double)this.screenHeight, -90.0).uv(i, j).endVertex();
		bufferBuilder.vertex((double)this.screenWidth, 0.0, -90.0).uv(i, h).endVertex();
		bufferBuilder.vertex(0.0, 0.0, -90.0).uv(g, h).endVertex();
		tesselator.end();
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}

	private void renderSlot(int i, int j, float f, Player player, ItemStack itemStack, int k) {
		if (!itemStack.isEmpty()) {
			PoseStack poseStack = RenderSystem.getModelViewStack();
			float g = (float)itemStack.getPopTime() - f;
			if (g > 0.0F) {
				float h = 1.0F + g / 5.0F;
				poseStack.pushPose();
				poseStack.translate((double)(i + 8), (double)(j + 12), 0.0);
				poseStack.scale(1.0F / h, (h + 1.0F) / 2.0F, 1.0F);
				poseStack.translate((double)(-(i + 8)), (double)(-(j + 12)), 0.0);
				RenderSystem.applyModelViewMatrix();
			}

			this.itemRenderer.renderAndDecorateItem(player, itemStack, i, j, k);
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			if (g > 0.0F) {
				poseStack.popPose();
				RenderSystem.applyModelViewMatrix();
			}

			this.itemRenderer.renderGuiItemDecorations(this.minecraft.font, itemStack, i, j);
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
				this.toolHighlightTimer = 40;
			} else if (this.toolHighlightTimer > 0) {
				this.toolHighlightTimer--;
			}

			this.lastToolHighlight = itemStack;
		}
	}

	private void tickAutosaveIndicator() {
		MinecraftServer minecraftServer = this.minecraft.getSingleplayerServer();
		boolean bl = minecraftServer != null && minecraftServer.isCurrentlySaving();
		this.lastAutosaveIndicatorValue = this.autosaveIndicatorValue;
		this.autosaveIndicatorValue = Mth.lerp(0.2F, this.autosaveIndicatorValue, bl ? 1.0F : 0.0F);
	}

	public void setNowPlaying(Component component) {
		this.setOverlayMessage(Component.translatable("record.nowPlaying", component), true);
	}

	public void setOverlayMessage(Component component, boolean bl) {
		this.overlayMessageString = component;
		this.overlayMessageTime = 60;
		this.animateOverlayMessageColor = bl;
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

	public UUID guessChatUUID(Component component) {
		String string = StringDecomposer.getPlainText(component);
		String string2 = StringUtils.substringBetween(string, "<", ">");
		return string2 == null ? Util.NIL_UUID : this.minecraft.getPlayerSocialManager().getDiscoveredUUID(string2);
	}

	public void handleChat(ChatType chatType, Component component, UUID uUID) {
		if (!this.minecraft.isBlocked(uUID)) {
			if (!this.minecraft.options.hideMatchedNames().get() || !this.minecraft.isBlocked(this.guessChatUUID(component))) {
				for (ChatListener chatListener : (List)this.chatListeners.get(chatType)) {
					chatListener.handle(chatType, component, uUID);
				}
			}
		}
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

	private void renderSavingIndicator(PoseStack poseStack) {
		if (this.minecraft.options.showAutosaveIndicator().get() && (this.autosaveIndicatorValue > 0.0F || this.lastAutosaveIndicatorValue > 0.0F)) {
			int i = Mth.floor(255.0F * Mth.clamp(Mth.lerp(this.minecraft.getFrameTime(), this.lastAutosaveIndicatorValue, this.autosaveIndicatorValue), 0.0F, 1.0F));
			if (i > 8) {
				Font font = this.getFont();
				int j = font.width(SAVING_TEXT);
				int k = 16777215 | i << 24 & 0xFF000000;
				font.drawShadow(poseStack, SAVING_TEXT, (float)(this.screenWidth - j - 10), (float)(this.screenHeight - 15), k);
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
