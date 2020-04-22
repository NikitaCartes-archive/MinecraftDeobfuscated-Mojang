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
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
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

@Environment(EnvType.CLIENT)
public class Gui extends GuiComponent {
	private static final ResourceLocation VIGNETTE_LOCATION = new ResourceLocation("textures/misc/vignette.png");
	private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
	private static final ResourceLocation PUMPKIN_BLUR_LOCATION = new ResourceLocation("textures/misc/pumpkinblur.png");
	private final Random random = new Random();
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
	private final Map<ChatType, List<ChatListener>> chatListeners = Maps.<ChatType, List<ChatListener>>newHashMap();

	public Gui(Minecraft minecraft) {
		this.minecraft = minecraft;
		this.itemRenderer = minecraft.getItemRenderer();
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
			RenderSystem.defaultBlendFunc();
		}

		ItemStack itemStack = this.minecraft.player.inventory.getArmor(3);
		if (this.minecraft.options.thirdPersonView == 0 && itemStack.getItem() == Blocks.CARVED_PUMPKIN.asItem()) {
			this.renderPumpkin();
		}

		if (!this.minecraft.player.hasEffect(MobEffects.CONFUSION)) {
			float g = Mth.lerp(f, this.minecraft.player.oPortalTime, this.minecraft.player.portalTime);
			if (g > 0.0F) {
				this.renderPortalOverlay(g);
			}
		}

		if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
			this.spectatorGui.renderHotbar(poseStack, f);
		} else if (!this.minecraft.options.hideGui) {
			this.renderHotbar(f, poseStack);
		}

		if (!this.minecraft.options.hideGui) {
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.minecraft.getTextureManager().bind(GUI_ICONS_LOCATION);
			RenderSystem.enableBlend();
			RenderSystem.enableAlphaTest();
			this.renderCrosshair(poseStack);
			RenderSystem.defaultBlendFunc();
			this.minecraft.getProfiler().push("bossHealth");
			this.bossOverlay.render(poseStack);
			this.minecraft.getProfiler().pop();
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.minecraft.getTextureManager().bind(GUI_ICONS_LOCATION);
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
			RenderSystem.disableAlphaTest();
			float g = (float)this.minecraft.player.getSleepTimer();
			float h = g / 100.0F;
			if (h > 1.0F) {
				h = 1.0F - (g - 100.0F) / 10.0F;
			}

			int j = (int)(220.0F * h) << 24 | 1052704;
			fill(poseStack, 0, 0, this.screenWidth, this.screenHeight, j);
			RenderSystem.enableAlphaTest();
			RenderSystem.enableDepthTest();
			this.minecraft.getProfiler().pop();
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
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
				float g = (float)this.overlayMessageTime - f;
				int k = (int)(g * 255.0F / 20.0F);
				if (k > 255) {
					k = 255;
				}

				if (k > 8) {
					RenderSystem.pushMatrix();
					RenderSystem.translatef((float)(this.screenWidth / 2), (float)(this.screenHeight - 68), 0.0F);
					RenderSystem.enableBlend();
					RenderSystem.defaultBlendFunc();
					int j = 16777215;
					if (this.animateOverlayMessageColor) {
						j = Mth.hsvToRgb(g / 50.0F, 0.7F, 0.6F) & 16777215;
					}

					int l = k << 24 & 0xFF000000;
					int m = font.width(this.overlayMessageString);
					this.drawBackdrop(poseStack, font, -4, m);
					font.draw(poseStack, this.overlayMessageString, (float)(-m / 2), -4.0F, j | l);
					RenderSystem.disableBlend();
					RenderSystem.popMatrix();
				}

				this.minecraft.getProfiler().pop();
			}

			if (this.title != null && this.titleTime > 0) {
				this.minecraft.getProfiler().push("titleAndSubtitle");
				float gx = (float)this.titleTime - f;
				int kx = 255;
				if (this.titleTime > this.titleFadeOutTime + this.titleStayTime) {
					float n = (float)(this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime) - gx;
					kx = (int)(n * 255.0F / (float)this.titleFadeInTime);
				}

				if (this.titleTime <= this.titleFadeOutTime) {
					kx = (int)(gx * 255.0F / (float)this.titleFadeOutTime);
				}

				kx = Mth.clamp(kx, 0, 255);
				if (kx > 8) {
					RenderSystem.pushMatrix();
					RenderSystem.translatef((float)(this.screenWidth / 2), (float)(this.screenHeight / 2), 0.0F);
					RenderSystem.enableBlend();
					RenderSystem.defaultBlendFunc();
					RenderSystem.pushMatrix();
					RenderSystem.scalef(4.0F, 4.0F, 4.0F);
					int j = kx << 24 & 0xFF000000;
					int l = font.width(this.title);
					this.drawBackdrop(poseStack, font, -10, l);
					font.drawShadow(poseStack, this.title, (float)(-l / 2), -10.0F, 16777215 | j);
					RenderSystem.popMatrix();
					if (this.subtitle != null) {
						RenderSystem.pushMatrix();
						RenderSystem.scalef(2.0F, 2.0F, 2.0F);
						int m = font.width(this.subtitle);
						this.drawBackdrop(poseStack, font, 5, m);
						font.drawShadow(poseStack, this.subtitle, (float)(-m / 2), 5.0F, 16777215 | j);
						RenderSystem.popMatrix();
					}

					RenderSystem.disableBlend();
					RenderSystem.popMatrix();
				}

				this.minecraft.getProfiler().pop();
			}

			this.subtitleOverlay.render(poseStack);
			Scoreboard scoreboard = this.minecraft.level.getScoreboard();
			Objective objective = null;
			PlayerTeam playerTeam = scoreboard.getPlayersTeam(this.minecraft.player.getScoreboardName());
			if (playerTeam != null) {
				int l = playerTeam.getColor().getId();
				if (l >= 0) {
					objective = scoreboard.getDisplayObjective(3 + l);
				}
			}

			Objective objective2 = objective != null ? objective : scoreboard.getDisplayObjective(1);
			if (objective2 != null) {
				this.displayScoreboardSidebar(poseStack, objective2);
			}

			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.disableAlphaTest();
			RenderSystem.pushMatrix();
			RenderSystem.translatef(0.0F, (float)(this.screenHeight - 48), 0.0F);
			this.minecraft.getProfiler().push("chat");
			this.chat.render(poseStack, this.tickCount);
			this.minecraft.getProfiler().pop();
			RenderSystem.popMatrix();
			objective2 = scoreboard.getDisplayObjective(0);
			if (!this.minecraft.options.keyPlayerList.isDown()
				|| this.minecraft.isLocalServer() && this.minecraft.player.connection.getOnlinePlayers().size() <= 1 && objective2 == null) {
				this.tabList.setVisible(false);
			} else {
				this.tabList.setVisible(true);
				this.tabList.render(poseStack, this.screenWidth, scoreboard, objective2);
			}
		}

		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableAlphaTest();
	}

	private void drawBackdrop(PoseStack poseStack, Font font, int i, int j) {
		int k = this.minecraft.options.getBackgroundColor(0.0F);
		if (k != 0) {
			int l = -j / 2;
			fill(poseStack, l - 2, i - 2, l + j + 2, i + 9 + 2, k);
		}
	}

	private void renderCrosshair(PoseStack poseStack) {
		Options options = this.minecraft.options;
		if (options.thirdPersonView == 0) {
			if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR || this.canRenderCrosshairForSpectator(this.minecraft.hitResult)) {
				if (options.renderDebug && !options.hideGui && !this.minecraft.player.isReducedDebugInfo() && !options.reducedDebugInfo) {
					RenderSystem.pushMatrix();
					RenderSystem.translatef((float)(this.screenWidth / 2), (float)(this.screenHeight / 2), (float)this.getBlitOffset());
					Camera camera = this.minecraft.gameRenderer.getMainCamera();
					RenderSystem.rotatef(camera.getXRot(), -1.0F, 0.0F, 0.0F);
					RenderSystem.rotatef(camera.getYRot(), 0.0F, 1.0F, 0.0F);
					RenderSystem.scalef(-1.0F, -1.0F, -1.0F);
					RenderSystem.renderCrosshair(10);
					RenderSystem.popMatrix();
				} else {
					RenderSystem.blendFuncSeparate(
						GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
						GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
						GlStateManager.SourceFactor.ONE,
						GlStateManager.DestFactor.ZERO
					);
					int i = 15;
					this.blit(poseStack, (this.screenWidth - 15) / 2, (this.screenHeight - 15) / 2, 0, 0, 15, 15);
					if (this.minecraft.options.attackIndicator == AttackIndicatorStatus.CROSSHAIR) {
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
			RenderSystem.enableBlend();
			int i = 0;
			int j = 0;
			MobEffectTextureManager mobEffectTextureManager = this.minecraft.getMobEffectTextures();
			List<Runnable> list = Lists.<Runnable>newArrayListWithExpectedSize(collection.size());
			this.minecraft.getTextureManager().bind(AbstractContainerScreen.INVENTORY_LOCATION);

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

					RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
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
						this.minecraft.getTextureManager().bind(textureAtlasSprite.atlas().location());
						RenderSystem.color4f(1.0F, 1.0F, 1.0F, g);
						blit(poseStack, n + 3, o + 3, this.getBlitOffset(), 18, 18, textureAtlasSprite);
					});
				}
			}

			list.forEach(Runnable::run);
		}
	}

	protected void renderHotbar(float f, PoseStack poseStack) {
		Player player = this.getCameraPlayer();
		if (player != null) {
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.minecraft.getTextureManager().bind(WIDGETS_LOCATION);
			ItemStack itemStack = player.getOffhandItem();
			HumanoidArm humanoidArm = player.getMainArm().getOpposite();
			int i = this.screenWidth / 2;
			int j = this.getBlitOffset();
			int k = 182;
			int l = 91;
			this.setBlitOffset(-90);
			this.blit(poseStack, i - 91, this.screenHeight - 22, 0, 0, 182, 22);
			this.blit(poseStack, i - 91 - 1 + player.inventory.selected * 20, this.screenHeight - 22 - 1, 0, 22, 24, 22);
			if (!itemStack.isEmpty()) {
				if (humanoidArm == HumanoidArm.LEFT) {
					this.blit(poseStack, i - 91 - 29, this.screenHeight - 23, 24, 22, 29, 24);
				} else {
					this.blit(poseStack, i + 91, this.screenHeight - 23, 53, 22, 29, 24);
				}
			}

			this.setBlitOffset(j);
			RenderSystem.enableRescaleNormal();
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();

			for (int m = 0; m < 9; m++) {
				int n = i - 90 + m * 20 + 2;
				int o = this.screenHeight - 16 - 3;
				this.renderSlot(n, o, f, player, player.inventory.items.get(m));
			}

			if (!itemStack.isEmpty()) {
				int m = this.screenHeight - 16 - 3;
				if (humanoidArm == HumanoidArm.LEFT) {
					this.renderSlot(i - 91 - 26, m, f, player, itemStack);
				} else {
					this.renderSlot(i + 91 + 10, m, f, player, itemStack);
				}
			}

			if (this.minecraft.options.attackIndicator == AttackIndicatorStatus.HOTBAR) {
				float g = this.minecraft.player.getAttackStrengthScale(0.0F);
				if (g < 1.0F) {
					int n = this.screenHeight - 20;
					int o = i + 91 + 6;
					if (humanoidArm == HumanoidArm.RIGHT) {
						o = i - 91 - 22;
					}

					this.minecraft.getTextureManager().bind(GuiComponent.GUI_ICONS_LOCATION);
					int p = (int)(g * 19.0F);
					RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
					this.blit(poseStack, o, n, 0, 94, 18, 18);
					this.blit(poseStack, o, n + 18 - p, 18, 112 - p, 18, p);
				}
			}

			RenderSystem.disableRescaleNormal();
			RenderSystem.disableBlend();
		}
	}

	public void renderJumpMeter(PoseStack poseStack, int i) {
		this.minecraft.getProfiler().push("jumpBar");
		this.minecraft.getTextureManager().bind(GuiComponent.GUI_ICONS_LOCATION);
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
		this.minecraft.getTextureManager().bind(GuiComponent.GUI_ICONS_LOCATION);
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
			String string = "" + this.minecraft.player.experienceLevel;
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
			MutableComponent mutableComponent = new TextComponent("").append(this.lastToolHighlight.getHoverName()).withStyle(this.lastToolHighlight.getRarity().color);
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
				RenderSystem.pushMatrix();
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				fill(poseStack, j - 2, k - 2, j + i + 2, k + 9 + 2, this.minecraft.options.getBackgroundColor(0));
				this.getFont().drawShadow(poseStack, mutableComponent, (float)j, (float)k, 16777215 + (l << 24));
				RenderSystem.disableBlend();
				RenderSystem.popMatrix();
			}
		}

		this.minecraft.getProfiler().pop();
	}

	public void renderDemoOverlay(PoseStack poseStack) {
		this.minecraft.getProfiler().push("demo");
		String string;
		if (this.minecraft.level.getGameTime() >= 120500L) {
			string = I18n.get("demo.demoExpired");
		} else {
			string = I18n.get("demo.remainingTime", StringUtil.formatTickDuration((int)(120500L - this.minecraft.level.getGameTime())));
		}

		int i = this.getFont().width(string);
		this.getFont().drawShadow(poseStack, string, (float)(this.screenWidth - i - 10), 5.0F, 16777215);
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
			Component component2 = PlayerTeam.formatNameForTeam(playerTeam, new TextComponent(score.getOwner()));
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
			String string = ChatFormatting.RED + "" + score2.getScore();
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
			float f = (float)player.getAttributeValue(Attributes.MAX_HEALTH);
			int p = Mth.ceil(player.getAbsorptionAmount());
			int q = Mth.ceil((f + (float)p) / 2.0F / 10.0F);
			int r = Math.max(10 - (q - 2), 3);
			int s = o - (q - 1) * r - 10;
			int t = o - 10;
			int u = p;
			int v = player.getArmorValue();
			int w = -1;
			if (player.hasEffect(MobEffects.REGENERATION)) {
				w = this.tickCount % Mth.ceil(f + 5.0F);
			}

			this.minecraft.getProfiler().push("armor");

			for (int x = 0; x < 10; x++) {
				if (v > 0) {
					int y = m + x * 8;
					if (x * 2 + 1 < v) {
						this.blit(poseStack, y, s, 34, 9, 9, 9);
					}

					if (x * 2 + 1 == v) {
						this.blit(poseStack, y, s, 25, 9, 9, 9);
					}

					if (x * 2 + 1 > v) {
						this.blit(poseStack, y, s, 16, 9, 9, 9);
					}
				}
			}

			this.minecraft.getProfiler().popPush("health");

			for (int xx = Mth.ceil((f + (float)p) / 2.0F) - 1; xx >= 0; xx--) {
				int yx = 16;
				if (player.hasEffect(MobEffects.POISON)) {
					yx += 36;
				} else if (player.hasEffect(MobEffects.WITHER)) {
					yx += 72;
				}

				int z = 0;
				if (bl) {
					z = 1;
				}

				int aa = Mth.ceil((float)(xx + 1) / 10.0F) - 1;
				int ab = m + xx % 10 * 8;
				int ac = o - aa * r;
				if (i <= 4) {
					ac += this.random.nextInt(2);
				}

				if (u <= 0 && xx == w) {
					ac -= 2;
				}

				int ad = 0;
				if (player.level.getLevelData().isHardcore()) {
					ad = 5;
				}

				this.blit(poseStack, ab, ac, 16 + z * 9, 9 * ad, 9, 9);
				if (bl) {
					if (xx * 2 + 1 < j) {
						this.blit(poseStack, ab, ac, yx + 54, 9 * ad, 9, 9);
					}

					if (xx * 2 + 1 == j) {
						this.blit(poseStack, ab, ac, yx + 63, 9 * ad, 9, 9);
					}
				}

				if (u > 0) {
					if (u == p && p % 2 == 1) {
						this.blit(poseStack, ab, ac, yx + 153, 9 * ad, 9, 9);
						u--;
					} else {
						this.blit(poseStack, ab, ac, yx + 144, 9 * ad, 9, 9);
						u -= 2;
					}
				} else {
					if (xx * 2 + 1 < i) {
						this.blit(poseStack, ab, ac, yx + 36, 9 * ad, 9, 9);
					}

					if (xx * 2 + 1 == i) {
						this.blit(poseStack, ab, ac, yx + 45, 9 * ad, 9, 9);
					}
				}
			}

			LivingEntity livingEntity = this.getPlayerVehicleWithHealth();
			int yxx = this.getVehicleMaxHearts(livingEntity);
			if (yxx == 0) {
				this.minecraft.getProfiler().popPush("food");

				for (int zx = 0; zx < 10; zx++) {
					int aax = o;
					int abx = 16;
					int acx = 0;
					if (player.hasEffect(MobEffects.HUNGER)) {
						abx += 36;
						acx = 13;
					}

					if (player.getFoodData().getSaturationLevel() <= 0.0F && this.tickCount % (k * 3 + 1) == 0) {
						aax = o + (this.random.nextInt(3) - 1);
					}

					int adx = n - zx * 8 - 9;
					this.blit(poseStack, adx, aax, 16 + acx * 9, 27, 9, 9);
					if (zx * 2 + 1 < k) {
						this.blit(poseStack, adx, aax, abx + 36, 27, 9, 9);
					}

					if (zx * 2 + 1 == k) {
						this.blit(poseStack, adx, aax, abx + 45, 27, 9, 9);
					}
				}

				t -= 10;
			}

			this.minecraft.getProfiler().popPush("air");
			int zx = player.getAirSupply();
			int aaxx = player.getMaxAirSupply();
			if (player.isUnderLiquid(FluidTags.WATER) || zx < aaxx) {
				int abxx = this.getVisibleVehicleHeartRows(yxx) - 1;
				t -= abxx * 10;
				int acxx = Mth.ceil((double)(zx - 2) * 10.0 / (double)aaxx);
				int adxx = Mth.ceil((double)zx * 10.0 / (double)aaxx) - acxx;

				for (int ae = 0; ae < acxx + adxx; ae++) {
					if (ae < acxx) {
						this.blit(poseStack, n - ae * 8 - 9, t, 16, 18, 9, 9);
					} else {
						this.blit(poseStack, n - ae * 8 - 9, t, 25, 18, 9, 9);
					}
				}
			}

			this.minecraft.getProfiler().pop();
		}
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

	private void renderPumpkin() {
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.defaultBlendFunc();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableAlphaTest();
		this.minecraft.getTextureManager().bind(PUMPKIN_BLUR_LOCATION);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(0.0, (double)this.screenHeight, -90.0).uv(0.0F, 1.0F).endVertex();
		bufferBuilder.vertex((double)this.screenWidth, (double)this.screenHeight, -90.0).uv(1.0F, 1.0F).endVertex();
		bufferBuilder.vertex((double)this.screenWidth, 0.0, -90.0).uv(1.0F, 0.0F).endVertex();
		bufferBuilder.vertex(0.0, 0.0, -90.0).uv(0.0F, 0.0F).endVertex();
		tesselator.end();
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.enableAlphaTest();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
	}

	private void updateVignetteBrightness(Entity entity) {
		if (entity != null) {
			float f = Mth.clamp(1.0F - entity.getBrightness(), 0.0F, 1.0F);
			this.vignetteBrightness = (float)((double)this.vignetteBrightness + (double)(f - this.vignetteBrightness) * 0.01);
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
			RenderSystem.color4f(0.0F, f, f, 1.0F);
		} else {
			RenderSystem.color4f(this.vignetteBrightness, this.vignetteBrightness, this.vignetteBrightness, 1.0F);
		}

		this.minecraft.getTextureManager().bind(VIGNETTE_LOCATION);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(0.0, (double)this.screenHeight, -90.0).uv(0.0F, 1.0F).endVertex();
		bufferBuilder.vertex((double)this.screenWidth, (double)this.screenHeight, -90.0).uv(1.0F, 1.0F).endVertex();
		bufferBuilder.vertex((double)this.screenWidth, 0.0, -90.0).uv(1.0F, 0.0F).endVertex();
		bufferBuilder.vertex(0.0, 0.0, -90.0).uv(0.0F, 0.0F).endVertex();
		tesselator.end();
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.defaultBlendFunc();
	}

	private void renderPortalOverlay(float f) {
		if (f < 1.0F) {
			f *= f;
			f *= f;
			f = f * 0.8F + 0.2F;
		}

		RenderSystem.disableAlphaTest();
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.defaultBlendFunc();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, f);
		this.minecraft.getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
		TextureAtlasSprite textureAtlasSprite = this.minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.NETHER_PORTAL.defaultBlockState());
		float g = textureAtlasSprite.getU0();
		float h = textureAtlasSprite.getV0();
		float i = textureAtlasSprite.getU1();
		float j = textureAtlasSprite.getV1();
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
		bufferBuilder.vertex(0.0, (double)this.screenHeight, -90.0).uv(g, j).endVertex();
		bufferBuilder.vertex((double)this.screenWidth, (double)this.screenHeight, -90.0).uv(i, j).endVertex();
		bufferBuilder.vertex((double)this.screenWidth, 0.0, -90.0).uv(i, h).endVertex();
		bufferBuilder.vertex(0.0, 0.0, -90.0).uv(g, h).endVertex();
		tesselator.end();
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.enableAlphaTest();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
	}

	private void renderSlot(int i, int j, float f, Player player, ItemStack itemStack) {
		if (!itemStack.isEmpty()) {
			float g = (float)itemStack.getPopTime() - f;
			if (g > 0.0F) {
				RenderSystem.pushMatrix();
				float h = 1.0F + g / 5.0F;
				RenderSystem.translatef((float)(i + 8), (float)(j + 12), 0.0F);
				RenderSystem.scalef(1.0F / h, (h + 1.0F) / 2.0F, 1.0F);
				RenderSystem.translatef((float)(-(i + 8)), (float)(-(j + 12)), 0.0F);
			}

			this.itemRenderer.renderAndDecorateItem(player, itemStack, i, j);
			if (g > 0.0F) {
				RenderSystem.popMatrix();
			}

			this.itemRenderer.renderGuiItemDecorations(this.minecraft.font, itemStack, i, j);
		}
	}

	public void tick() {
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
			ItemStack itemStack = this.minecraft.player.inventory.getSelected();
			if (itemStack.isEmpty()) {
				this.toolHighlightTimer = 0;
			} else if (this.lastToolHighlight.isEmpty()
				|| itemStack.getItem() != this.lastToolHighlight.getItem()
				|| !itemStack.getHoverName().equals(this.lastToolHighlight.getHoverName())) {
				this.toolHighlightTimer = 40;
			} else if (this.toolHighlightTimer > 0) {
				this.toolHighlightTimer--;
			}

			this.lastToolHighlight = itemStack;
		}
	}

	public void setNowPlaying(Component component) {
		this.setOverlayMessage(new TranslatableComponent("record.nowPlaying", component), true);
	}

	public void setOverlayMessage(Component component, boolean bl) {
		this.overlayMessageString = component;
		this.overlayMessageTime = 60;
		this.animateOverlayMessageColor = bl;
	}

	public void setTitles(@Nullable Component component, @Nullable Component component2, int i, int j, int k) {
		if (component == null && component2 == null && i < 0 && j < 0 && k < 0) {
			this.title = null;
			this.subtitle = null;
			this.titleTime = 0;
		} else if (component != null) {
			this.title = component;
			this.titleTime = this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime;
		} else if (component2 != null) {
			this.subtitle = component2;
		} else {
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
	}

	public void handleChat(ChatType chatType, Component component) {
		for (ChatListener chatListener : (List)this.chatListeners.get(chatType)) {
			chatListener.handle(chatType, component);
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
	}

	public BossHealthOverlay getBossOverlay() {
		return this.bossOverlay;
	}

	public void clearCache() {
		this.debugScreen.clearChunkCache();
	}
}
