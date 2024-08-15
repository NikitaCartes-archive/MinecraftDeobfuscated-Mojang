package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ARGB;
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
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.joml.Matrix4fStack;

@Environment(EnvType.CLIENT)
public class Gui {
	private static final ResourceLocation CROSSHAIR_SPRITE = ResourceLocation.withDefaultNamespace("hud/crosshair");
	private static final ResourceLocation CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE = ResourceLocation.withDefaultNamespace("hud/crosshair_attack_indicator_full");
	private static final ResourceLocation CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace(
		"hud/crosshair_attack_indicator_background"
	);
	private static final ResourceLocation CROSSHAIR_ATTACK_INDICATOR_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace(
		"hud/crosshair_attack_indicator_progress"
	);
	private static final ResourceLocation EFFECT_BACKGROUND_AMBIENT_SPRITE = ResourceLocation.withDefaultNamespace("hud/effect_background_ambient");
	private static final ResourceLocation EFFECT_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("hud/effect_background");
	private static final ResourceLocation HOTBAR_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar");
	private static final ResourceLocation HOTBAR_SELECTION_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar_selection");
	private static final ResourceLocation HOTBAR_OFFHAND_LEFT_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar_offhand_left");
	private static final ResourceLocation HOTBAR_OFFHAND_RIGHT_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar_offhand_right");
	private static final ResourceLocation HOTBAR_ATTACK_INDICATOR_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace(
		"hud/hotbar_attack_indicator_background"
	);
	private static final ResourceLocation HOTBAR_ATTACK_INDICATOR_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar_attack_indicator_progress");
	private static final ResourceLocation JUMP_BAR_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("hud/jump_bar_background");
	private static final ResourceLocation JUMP_BAR_COOLDOWN_SPRITE = ResourceLocation.withDefaultNamespace("hud/jump_bar_cooldown");
	private static final ResourceLocation JUMP_BAR_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("hud/jump_bar_progress");
	private static final ResourceLocation EXPERIENCE_BAR_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("hud/experience_bar_background");
	private static final ResourceLocation EXPERIENCE_BAR_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("hud/experience_bar_progress");
	private static final ResourceLocation ARMOR_EMPTY_SPRITE = ResourceLocation.withDefaultNamespace("hud/armor_empty");
	private static final ResourceLocation ARMOR_HALF_SPRITE = ResourceLocation.withDefaultNamespace("hud/armor_half");
	private static final ResourceLocation ARMOR_FULL_SPRITE = ResourceLocation.withDefaultNamespace("hud/armor_full");
	private static final ResourceLocation FOOD_EMPTY_HUNGER_SPRITE = ResourceLocation.withDefaultNamespace("hud/food_empty_hunger");
	private static final ResourceLocation FOOD_HALF_HUNGER_SPRITE = ResourceLocation.withDefaultNamespace("hud/food_half_hunger");
	private static final ResourceLocation FOOD_FULL_HUNGER_SPRITE = ResourceLocation.withDefaultNamespace("hud/food_full_hunger");
	private static final ResourceLocation FOOD_EMPTY_SPRITE = ResourceLocation.withDefaultNamespace("hud/food_empty");
	private static final ResourceLocation FOOD_HALF_SPRITE = ResourceLocation.withDefaultNamespace("hud/food_half");
	private static final ResourceLocation FOOD_FULL_SPRITE = ResourceLocation.withDefaultNamespace("hud/food_full");
	private static final ResourceLocation AIR_SPRITE = ResourceLocation.withDefaultNamespace("hud/air");
	private static final ResourceLocation AIR_BURSTING_SPRITE = ResourceLocation.withDefaultNamespace("hud/air_bursting");
	private static final ResourceLocation HEART_VEHICLE_CONTAINER_SPRITE = ResourceLocation.withDefaultNamespace("hud/heart/vehicle_container");
	private static final ResourceLocation HEART_VEHICLE_FULL_SPRITE = ResourceLocation.withDefaultNamespace("hud/heart/vehicle_full");
	private static final ResourceLocation HEART_VEHICLE_HALF_SPRITE = ResourceLocation.withDefaultNamespace("hud/heart/vehicle_half");
	private static final ResourceLocation VIGNETTE_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/vignette.png");
	public static final ResourceLocation NAUSEA_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/nausea.png");
	private static final ResourceLocation PUMPKIN_BLUR_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/pumpkinblur.png");
	private static final ResourceLocation SPYGLASS_SCOPE_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/spyglass_scope.png");
	private static final ResourceLocation POWDER_SNOW_OUTLINE_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/powder_snow_outline.png");
	private static final Comparator<PlayerScoreEntry> SCORE_DISPLAY_ORDER = Comparator.comparing(PlayerScoreEntry::value)
		.reversed()
		.thenComparing(PlayerScoreEntry::owner, String.CASE_INSENSITIVE_ORDER);
	private static final Component DEMO_EXPIRED_TEXT = Component.translatable("demo.demoExpired");
	private static final Component SAVING_TEXT = Component.translatable("menu.savingLevel");
	private static final float MIN_CROSSHAIR_ATTACK_SPEED = 5.0F;
	private static final int NUM_HEARTS_PER_ROW = 10;
	private static final int LINE_HEIGHT = 10;
	private static final String SPACER = ": ";
	private static final float PORTAL_OVERLAY_ALPHA_MIN = 0.2F;
	private static final int HEART_SIZE = 9;
	private static final int HEART_SEPARATION = 8;
	private static final float AUTOSAVE_FADE_SPEED_FACTOR = 0.2F;
	private static final int SAVING_INDICATOR_WIDTH_PADDING_RIGHT = 5;
	private static final int SAVING_INDICATOR_HEIGHT_PADDING_BOTTOM = 5;
	private final RandomSource random = RandomSource.create();
	private final Minecraft minecraft;
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
	private final DebugScreenOverlay debugOverlay;
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
	private float autosaveIndicatorValue;
	private float lastAutosaveIndicatorValue;
	private final LayeredDraw layers = new LayeredDraw();
	private float scopeScale;

	public Gui(Minecraft minecraft) {
		this.minecraft = minecraft;
		this.debugOverlay = new DebugScreenOverlay(minecraft);
		this.spectatorGui = new SpectatorGui(minecraft);
		this.chat = new ChatComponent(minecraft);
		this.tabList = new PlayerTabOverlay(minecraft, this);
		this.bossOverlay = new BossHealthOverlay(minecraft);
		this.subtitleOverlay = new SubtitleOverlay(minecraft);
		this.resetTitleTimes();
		LayeredDraw layeredDraw = new LayeredDraw()
			.add(this::renderCameraOverlays)
			.add(this::renderCrosshair)
			.add(this::renderHotbarAndDecorations)
			.add(this::renderExperienceLevel)
			.add(this::renderEffects)
			.add((guiGraphics, deltaTracker) -> this.bossOverlay.render(guiGraphics));
		LayeredDraw layeredDraw2 = new LayeredDraw()
			.add(this::renderDemoOverlay)
			.add((guiGraphics, deltaTracker) -> {
				if (this.debugOverlay.showDebugScreen()) {
					this.debugOverlay.render(guiGraphics);
				}
			})
			.add(this::renderScoreboardSidebar)
			.add(this::renderOverlayMessage)
			.add(this::renderTitle)
			.add(this::renderChat)
			.add(this::renderTabList)
			.add((guiGraphics, deltaTracker) -> this.subtitleOverlay.render(guiGraphics));
		this.layers.add(layeredDraw, () -> !minecraft.options.hideGui).add(this::renderSleepOverlay).add(layeredDraw2, () -> !minecraft.options.hideGui);
	}

	public void resetTitleTimes() {
		this.titleFadeInTime = 10;
		this.titleStayTime = 70;
		this.titleFadeOutTime = 20;
	}

	public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		this.layers.render(guiGraphics, deltaTracker);
	}

	private void renderCameraOverlays(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		if (Minecraft.useFancyGraphics()) {
			this.renderVignette(guiGraphics, this.minecraft.getCameraEntity());
		}

		float f = deltaTracker.getGameTimeDeltaTicks();
		this.scopeScale = Mth.lerp(0.5F * f, this.scopeScale, 1.125F);
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

		float g = Mth.lerp(
			deltaTracker.getGameTimeDeltaPartialTick(false), this.minecraft.player.oSpinningEffectIntensity, this.minecraft.player.spinningEffectIntensity
		);
		if (g > 0.0F) {
			if (!this.minecraft.player.hasEffect(MobEffects.CONFUSION)) {
				this.renderPortalOverlay(guiGraphics, g);
			} else {
				float h = this.minecraft.options.screenEffectScale().get().floatValue();
				if (h < 1.0F) {
					float i = g * (1.0F - h);
					this.renderConfusionOverlay(guiGraphics, i);
				}
			}
		}
	}

	private void renderSleepOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		if (this.minecraft.player.getSleepTimer() > 0) {
			this.minecraft.getProfiler().push("sleep");
			float f = (float)this.minecraft.player.getSleepTimer();
			float g = f / 100.0F;
			if (g > 1.0F) {
				g = 1.0F - (f - 100.0F) / 10.0F;
			}

			int i = (int)(220.0F * g) << 24 | 1052704;
			guiGraphics.fill(RenderType.guiOverlay(), 0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), i);
			this.minecraft.getProfiler().pop();
		}
	}

	private void renderOverlayMessage(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		Font font = this.getFont();
		if (this.overlayMessageString != null && this.overlayMessageTime > 0) {
			this.minecraft.getProfiler().push("overlayMessage");
			float f = (float)this.overlayMessageTime - deltaTracker.getGameTimeDeltaPartialTick(false);
			int i = (int)(f * 255.0F / 20.0F);
			if (i > 255) {
				i = 255;
			}

			if (i > 8) {
				guiGraphics.pose().pushPose();
				guiGraphics.pose().translate((float)(guiGraphics.guiWidth() / 2), (float)(guiGraphics.guiHeight() - 68), 0.0F);
				int j;
				if (this.animateOverlayMessageColor) {
					j = Mth.hsvToArgb(f / 50.0F, 0.7F, 0.6F, i);
				} else {
					j = ARGB.color(i, -1);
				}

				int k = font.width(this.overlayMessageString);
				guiGraphics.drawStringWithBackdrop(font, this.overlayMessageString, -k / 2, -4, k, j);
				guiGraphics.pose().popPose();
			}

			this.minecraft.getProfiler().pop();
		}
	}

	private void renderTitle(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		if (this.title != null && this.titleTime > 0) {
			Font font = this.getFont();
			this.minecraft.getProfiler().push("titleAndSubtitle");
			float f = (float)this.titleTime - deltaTracker.getGameTimeDeltaPartialTick(false);
			int i = 255;
			if (this.titleTime > this.titleFadeOutTime + this.titleStayTime) {
				float g = (float)(this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime) - f;
				i = (int)(g * 255.0F / (float)this.titleFadeInTime);
			}

			if (this.titleTime <= this.titleFadeOutTime) {
				i = (int)(f * 255.0F / (float)this.titleFadeOutTime);
			}

			i = Mth.clamp(i, 0, 255);
			if (i > 8) {
				guiGraphics.pose().pushPose();
				guiGraphics.pose().translate((float)(guiGraphics.guiWidth() / 2), (float)(guiGraphics.guiHeight() / 2), 0.0F);
				guiGraphics.pose().pushPose();
				guiGraphics.pose().scale(4.0F, 4.0F, 4.0F);
				int j = font.width(this.title);
				int k = ARGB.color(i, -1);
				guiGraphics.drawStringWithBackdrop(font, this.title, -j / 2, -10, j, k);
				guiGraphics.pose().popPose();
				if (this.subtitle != null) {
					guiGraphics.pose().pushPose();
					guiGraphics.pose().scale(2.0F, 2.0F, 2.0F);
					int l = font.width(this.subtitle);
					guiGraphics.drawStringWithBackdrop(font, this.subtitle, -l / 2, 5, l, k);
					guiGraphics.pose().popPose();
				}

				guiGraphics.pose().popPose();
			}

			this.minecraft.getProfiler().pop();
		}
	}

	private void renderChat(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		if (!this.chat.isChatFocused()) {
			Window window = this.minecraft.getWindow();
			int i = Mth.floor(this.minecraft.mouseHandler.xpos() * (double)window.getGuiScaledWidth() / (double)window.getScreenWidth());
			int j = Mth.floor(this.minecraft.mouseHandler.ypos() * (double)window.getGuiScaledHeight() / (double)window.getScreenHeight());
			this.chat.render(guiGraphics, this.tickCount, i, j, false);
		}
	}

	private void renderScoreboardSidebar(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		Scoreboard scoreboard = this.minecraft.level.getScoreboard();
		Objective objective = null;
		PlayerTeam playerTeam = scoreboard.getPlayersTeam(this.minecraft.player.getScoreboardName());
		if (playerTeam != null) {
			DisplaySlot displaySlot = DisplaySlot.teamColorToSlot(playerTeam.getColor());
			if (displaySlot != null) {
				objective = scoreboard.getDisplayObjective(displaySlot);
			}
		}

		Objective objective2 = objective != null ? objective : scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
		if (objective2 != null) {
			this.displayScoreboardSidebar(guiGraphics, objective2);
		}
	}

	private void renderTabList(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		Scoreboard scoreboard = this.minecraft.level.getScoreboard();
		Objective objective = scoreboard.getDisplayObjective(DisplaySlot.LIST);
		if (!this.minecraft.options.keyPlayerList.isDown()
			|| this.minecraft.isLocalServer() && this.minecraft.player.connection.getListedOnlinePlayers().size() <= 1 && objective == null) {
			this.tabList.setVisible(false);
		} else {
			this.tabList.setVisible(true);
			this.tabList.render(guiGraphics, guiGraphics.guiWidth(), scoreboard, objective);
		}
	}

	private void renderCrosshair(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		Options options = this.minecraft.options;
		if (options.getCameraType().isFirstPerson()) {
			if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR || this.canRenderCrosshairForSpectator(this.minecraft.hitResult)) {
				if (this.debugOverlay.showDebugScreen() && !this.minecraft.player.isReducedDebugInfo() && !options.reducedDebugInfo().get()) {
					Camera camera = this.minecraft.gameRenderer.getMainCamera();
					Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
					matrix4fStack.pushMatrix();
					matrix4fStack.mul(guiGraphics.pose().last().pose());
					matrix4fStack.translate((float)(guiGraphics.guiWidth() / 2), (float)(guiGraphics.guiHeight() / 2), 0.0F);
					matrix4fStack.rotateX(-camera.getXRot() * (float) (Math.PI / 180.0));
					matrix4fStack.rotateY(camera.getYRot() * (float) (Math.PI / 180.0));
					matrix4fStack.scale(-1.0F, -1.0F, -1.0F);
					RenderSystem.renderCrosshair(10);
					matrix4fStack.popMatrix();
				} else {
					int i = 15;
					guiGraphics.blitSprite(RenderType::crosshair, CROSSHAIR_SPRITE, (guiGraphics.guiWidth() - 15) / 2, (guiGraphics.guiHeight() - 15) / 2, 15, 15);
					if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.CROSSHAIR) {
						float f = this.minecraft.player.getAttackStrengthScale(0.0F);
						boolean bl = false;
						if (this.minecraft.crosshairPickEntity != null && this.minecraft.crosshairPickEntity instanceof LivingEntity && f >= 1.0F) {
							bl = this.minecraft.player.getCurrentItemAttackStrengthDelay() > 5.0F;
							bl &= this.minecraft.crosshairPickEntity.isAlive();
						}

						int j = guiGraphics.guiHeight() / 2 - 7 + 16;
						int k = guiGraphics.guiWidth() / 2 - 8;
						if (bl) {
							guiGraphics.blitSprite(RenderType::crosshair, CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE, k, j, 16, 16);
						} else if (f < 1.0F) {
							int l = (int)(f * 17.0F);
							guiGraphics.blitSprite(RenderType::crosshair, CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_SPRITE, k, j, 16, 4);
							guiGraphics.blitSprite(RenderType::crosshair, CROSSHAIR_ATTACK_INDICATOR_PROGRESS_SPRITE, 16, 4, 0, 0, k, j, l, 4);
						}
					}
				}
			}
		}
	}

	private boolean canRenderCrosshairForSpectator(@Nullable HitResult hitResult) {
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

	private void renderEffects(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		Collection<MobEffectInstance> collection = this.minecraft.player.getActiveEffects();
		if (!collection.isEmpty()) {
			if (this.minecraft.screen instanceof EffectRenderingInventoryScreen effectRenderingInventoryScreen && effectRenderingInventoryScreen.canSeeEffects()) {
				return;
			}

			int i = 0;
			int j = 0;
			MobEffectTextureManager mobEffectTextureManager = this.minecraft.getMobEffectTextures();
			List<Runnable> list = Lists.<Runnable>newArrayListWithExpectedSize(collection.size());

			for (MobEffectInstance mobEffectInstance : Ordering.natural().reverse().sortedCopy(collection)) {
				Holder<MobEffect> holder = mobEffectInstance.getEffect();
				if (mobEffectInstance.showIcon()) {
					int k = guiGraphics.guiWidth();
					int l = 1;
					if (this.minecraft.isDemo()) {
						l += 15;
					}

					if (holder.value().isBeneficial()) {
						i++;
						k -= 25 * i;
					} else {
						j++;
						k -= 25 * j;
						l += 26;
					}

					float f = 1.0F;
					if (mobEffectInstance.isAmbient()) {
						guiGraphics.blitSprite(RenderType::guiTextured, EFFECT_BACKGROUND_AMBIENT_SPRITE, k, l, 24, 24);
					} else {
						guiGraphics.blitSprite(RenderType::guiTextured, EFFECT_BACKGROUND_SPRITE, k, l, 24, 24);
						if (mobEffectInstance.endsWithin(200)) {
							int m = mobEffectInstance.getDuration();
							int n = 10 - m / 20;
							f = Mth.clamp((float)m / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F)
								+ Mth.cos((float)m * (float) Math.PI / 5.0F) * Mth.clamp((float)n / 10.0F * 0.25F, 0.0F, 0.25F);
							f = Mth.clamp(f, 0.0F, 1.0F);
						}
					}

					TextureAtlasSprite textureAtlasSprite = mobEffectTextureManager.get(holder);
					int n = k;
					int o = l;
					float g = f;
					list.add((Runnable)() -> {
						int kx = ARGB.white(g);
						guiGraphics.blitSprite(RenderType::guiTextured, textureAtlasSprite, n + 3, o + 3, 18, 18, kx);
					});
				}
			}

			list.forEach(Runnable::run);
		}
	}

	private void renderHotbarAndDecorations(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
			this.spectatorGui.renderHotbar(guiGraphics);
		} else {
			this.renderItemHotbar(guiGraphics, deltaTracker);
		}

		int i = guiGraphics.guiWidth() / 2 - 91;
		PlayerRideableJumping playerRideableJumping = this.minecraft.player.jumpableVehicle();
		if (playerRideableJumping != null) {
			this.renderJumpMeter(playerRideableJumping, guiGraphics, i);
		} else if (this.isExperienceBarVisible()) {
			this.renderExperienceBar(guiGraphics, i);
		}

		if (this.minecraft.gameMode.canHurtPlayer()) {
			this.renderPlayerHealth(guiGraphics);
		}

		this.renderVehicleHealth(guiGraphics);
		if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
			this.renderSelectedItemName(guiGraphics);
		} else if (this.minecraft.player.isSpectator()) {
			this.spectatorGui.renderTooltip(guiGraphics);
		}
	}

	private void renderItemHotbar(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		Player player = this.getCameraPlayer();
		if (player != null) {
			ItemStack itemStack = player.getOffhandItem();
			HumanoidArm humanoidArm = player.getMainArm().getOpposite();
			int i = guiGraphics.guiWidth() / 2;
			int j = 182;
			int k = 91;
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(0.0F, 0.0F, -90.0F);
			guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_SPRITE, i - 91, guiGraphics.guiHeight() - 22, 182, 22);
			guiGraphics.blitSprite(
				RenderType::guiTextured, HOTBAR_SELECTION_SPRITE, i - 91 - 1 + player.getInventory().selected * 20, guiGraphics.guiHeight() - 22 - 1, 24, 23
			);
			if (!itemStack.isEmpty()) {
				if (humanoidArm == HumanoidArm.LEFT) {
					guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_OFFHAND_LEFT_SPRITE, i - 91 - 29, guiGraphics.guiHeight() - 23, 29, 24);
				} else {
					guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_OFFHAND_RIGHT_SPRITE, i + 91, guiGraphics.guiHeight() - 23, 29, 24);
				}
			}

			guiGraphics.pose().popPose();
			int l = 1;

			for (int m = 0; m < 9; m++) {
				int n = i - 90 + m * 20 + 2;
				int o = guiGraphics.guiHeight() - 16 - 3;
				this.renderSlot(guiGraphics, n, o, deltaTracker, player, player.getInventory().items.get(m), l++);
			}

			if (!itemStack.isEmpty()) {
				int m = guiGraphics.guiHeight() - 16 - 3;
				if (humanoidArm == HumanoidArm.LEFT) {
					this.renderSlot(guiGraphics, i - 91 - 26, m, deltaTracker, player, itemStack, l++);
				} else {
					this.renderSlot(guiGraphics, i + 91 + 10, m, deltaTracker, player, itemStack, l++);
				}
			}

			if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR) {
				float f = this.minecraft.player.getAttackStrengthScale(0.0F);
				if (f < 1.0F) {
					int n = guiGraphics.guiHeight() - 20;
					int o = i + 91 + 6;
					if (humanoidArm == HumanoidArm.RIGHT) {
						o = i - 91 - 22;
					}

					int p = (int)(f * 19.0F);
					guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_ATTACK_INDICATOR_BACKGROUND_SPRITE, o, n, 18, 18);
					guiGraphics.blitSprite(RenderType::guiTextured, HOTBAR_ATTACK_INDICATOR_PROGRESS_SPRITE, 18, 18, 0, 18 - p, o, n + 18 - p, 18, p);
				}
			}
		}
	}

	private void renderJumpMeter(PlayerRideableJumping playerRideableJumping, GuiGraphics guiGraphics, int i) {
		this.minecraft.getProfiler().push("jumpBar");
		float f = this.minecraft.player.getJumpRidingScale();
		int j = 182;
		int k = (int)(f * 183.0F);
		int l = guiGraphics.guiHeight() - 32 + 3;
		guiGraphics.blitSprite(RenderType::guiTextured, JUMP_BAR_BACKGROUND_SPRITE, i, l, 182, 5);
		if (playerRideableJumping.getJumpCooldown() > 0) {
			guiGraphics.blitSprite(RenderType::guiTextured, JUMP_BAR_COOLDOWN_SPRITE, i, l, 182, 5);
		} else if (k > 0) {
			guiGraphics.blitSprite(RenderType::guiTextured, JUMP_BAR_PROGRESS_SPRITE, 182, 5, 0, 0, i, l, k, 5);
		}

		this.minecraft.getProfiler().pop();
	}

	private void renderExperienceBar(GuiGraphics guiGraphics, int i) {
		this.minecraft.getProfiler().push("expBar");
		int j = this.minecraft.player.getXpNeededForNextLevel();
		if (j > 0) {
			int k = 182;
			int l = (int)(this.minecraft.player.experienceProgress * 183.0F);
			int m = guiGraphics.guiHeight() - 32 + 3;
			guiGraphics.blitSprite(RenderType::guiTextured, EXPERIENCE_BAR_BACKGROUND_SPRITE, i, m, 182, 5);
			if (l > 0) {
				guiGraphics.blitSprite(RenderType::guiTextured, EXPERIENCE_BAR_PROGRESS_SPRITE, 182, 5, 0, 0, i, m, l, 5);
			}
		}

		this.minecraft.getProfiler().pop();
	}

	private void renderExperienceLevel(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		int i = this.minecraft.player.experienceLevel;
		if (this.isExperienceBarVisible() && i > 0) {
			this.minecraft.getProfiler().push("expLevel");
			String string = i + "";
			int j = (guiGraphics.guiWidth() - this.getFont().width(string)) / 2;
			int k = guiGraphics.guiHeight() - 31 - 4;
			guiGraphics.drawString(this.getFont(), string, j + 1, k, 0, false);
			guiGraphics.drawString(this.getFont(), string, j - 1, k, 0, false);
			guiGraphics.drawString(this.getFont(), string, j, k + 1, 0, false);
			guiGraphics.drawString(this.getFont(), string, j, k - 1, 0, false);
			guiGraphics.drawString(this.getFont(), string, j, k, 8453920, false);
			this.minecraft.getProfiler().pop();
		}
	}

	private boolean isExperienceBarVisible() {
		return this.minecraft.player.jumpableVehicle() == null && this.minecraft.gameMode.hasExperience();
	}

	private void renderSelectedItemName(GuiGraphics guiGraphics) {
		this.minecraft.getProfiler().push("selectedItemName");
		if (this.toolHighlightTimer > 0 && !this.lastToolHighlight.isEmpty()) {
			MutableComponent mutableComponent = Component.empty().append(this.lastToolHighlight.getHoverName()).withStyle(this.lastToolHighlight.getRarity().color());
			if (this.lastToolHighlight.has(DataComponents.CUSTOM_NAME)) {
				mutableComponent.withStyle(ChatFormatting.ITALIC);
			}

			int i = this.getFont().width(mutableComponent);
			int j = (guiGraphics.guiWidth() - i) / 2;
			int k = guiGraphics.guiHeight() - 59;
			if (!this.minecraft.gameMode.canHurtPlayer()) {
				k += 14;
			}

			int l = (int)((float)this.toolHighlightTimer * 256.0F / 10.0F);
			if (l > 255) {
				l = 255;
			}

			if (l > 0) {
				guiGraphics.drawStringWithBackdrop(this.getFont(), mutableComponent, j, k, i, ARGB.color(l, -1));
			}
		}

		this.minecraft.getProfiler().pop();
	}

	private void renderDemoOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		if (this.minecraft.isDemo()) {
			this.minecraft.getProfiler().push("demo");
			Component component;
			if (this.minecraft.level.getGameTime() >= 120500L) {
				component = DEMO_EXPIRED_TEXT;
			} else {
				component = Component.translatable(
					"demo.remainingTime",
					StringUtil.formatTickDuration((int)(120500L - this.minecraft.level.getGameTime()), this.minecraft.level.tickRateManager().tickrate())
				);
			}

			int i = this.getFont().width(component);
			int j = guiGraphics.guiWidth() - i - 10;
			int k = 5;
			guiGraphics.drawStringWithBackdrop(this.getFont(), component, j, 5, i, -1);
			this.minecraft.getProfiler().pop();
		}
	}

	private void displayScoreboardSidebar(GuiGraphics guiGraphics, Objective objective) {
		Scoreboard scoreboard = objective.getScoreboard();
		NumberFormat numberFormat = objective.numberFormatOrDefault(StyledFormat.SIDEBAR_DEFAULT);

		@Environment(EnvType.CLIENT)
		record DisplayEntry(Component name, Component score, int scoreWidth) {
		}

		DisplayEntry[] lvs = (DisplayEntry[])scoreboard.listPlayerScores(objective)
			.stream()
			.filter(playerScoreEntry -> !playerScoreEntry.isHidden())
			.sorted(SCORE_DISPLAY_ORDER)
			.limit(15L)
			.map(playerScoreEntry -> {
				PlayerTeam playerTeam = scoreboard.getPlayersTeam(playerScoreEntry.owner());
				Component componentx = playerScoreEntry.ownerName();
				Component component2 = PlayerTeam.formatNameForTeam(playerTeam, componentx);
				Component component3 = playerScoreEntry.formatValue(numberFormat);
				int ix = this.getFont().width(component3);
				return new DisplayEntry(component2, component3, ix);
			})
			.toArray(DisplayEntry[]::new);
		Component component = objective.getDisplayName();
		int i = this.getFont().width(component);
		int j = i;
		int k = this.getFont().width(": ");

		for (DisplayEntry lv : lvs) {
			j = Math.max(j, this.getFont().width(lv.name) + (lv.scoreWidth > 0 ? k + lv.scoreWidth : 0));
		}

		int m = lvs.length;
		int n = m * 9;
		int o = guiGraphics.guiHeight() / 2 + n / 3;
		int p = 3;
		int q = guiGraphics.guiWidth() - j - 3;
		int r = guiGraphics.guiWidth() - 3 + 2;
		int s = this.minecraft.options.getBackgroundColor(0.3F);
		int t = this.minecraft.options.getBackgroundColor(0.4F);
		int u = o - m * 9;
		guiGraphics.fill(q - 2, u - 9 - 1, r, u - 1, t);
		guiGraphics.fill(q - 2, u - 1, r, o, s);
		guiGraphics.drawString(this.getFont(), component, q + j / 2 - i / 2, u - 9, -1, false);

		for (int v = 0; v < m; v++) {
			DisplayEntry lv2 = lvs[v];
			int w = o - (m - v) * 9;
			guiGraphics.drawString(this.getFont(), lv2.name, q, w, -1, false);
			guiGraphics.drawString(this.getFont(), lv2.score, r - lv2.scoreWidth, w, -1, false);
		}
	}

	@Nullable
	private Player getCameraPlayer() {
		return this.minecraft.getCameraEntity() instanceof Player player ? player : null;
	}

	@Nullable
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

	private int getVehicleMaxHearts(@Nullable LivingEntity livingEntity) {
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
				this.displayHealth = i;
				this.lastHealthTime = l;
			}

			this.lastHealth = i;
			int j = this.displayHealth;
			this.random.setSeed((long)(this.tickCount * 312871));
			int k = guiGraphics.guiWidth() / 2 - 91;
			int m = guiGraphics.guiWidth() / 2 + 91;
			int n = guiGraphics.guiHeight() - 39;
			float f = Math.max((float)player.getAttributeValue(Attributes.MAX_HEALTH), (float)Math.max(j, i));
			int o = Mth.ceil(player.getAbsorptionAmount());
			int p = Mth.ceil((f + (float)o) / 2.0F / 10.0F);
			int q = Math.max(10 - (p - 2), 3);
			int r = n - 10;
			int s = -1;
			if (player.hasEffect(MobEffects.REGENERATION)) {
				s = this.tickCount % Mth.ceil(f + 5.0F);
			}

			this.minecraft.getProfiler().push("armor");
			renderArmor(guiGraphics, player, n, p, q, k);
			this.minecraft.getProfiler().popPush("health");
			this.renderHearts(guiGraphics, player, k, n, q, s, f, i, j, o, bl);
			LivingEntity livingEntity = this.getPlayerVehicleWithHealth();
			int t = this.getVehicleMaxHearts(livingEntity);
			if (t == 0) {
				this.minecraft.getProfiler().popPush("food");
				this.renderFood(guiGraphics, player, n, m);
				r -= 10;
			}

			this.minecraft.getProfiler().popPush("air");
			int u = player.getMaxAirSupply();
			int v = Math.min(player.getAirSupply(), u);
			if (player.isEyeInFluid(FluidTags.WATER) || v < u) {
				int w = this.getVisibleVehicleHeartRows(t) - 1;
				r -= w * 10;
				int x = Mth.ceil((double)(v - 2) * 10.0 / (double)u);
				int y = Mth.ceil((double)v * 10.0 / (double)u) - x;

				for (int z = 0; z < x + y; z++) {
					if (z < x) {
						guiGraphics.blitSprite(RenderType::guiTextured, AIR_SPRITE, m - z * 8 - 9, r, 9, 9);
					} else {
						guiGraphics.blitSprite(RenderType::guiTextured, AIR_BURSTING_SPRITE, m - z * 8 - 9, r, 9, 9);
					}
				}
			}

			this.minecraft.getProfiler().pop();
		}
	}

	private static void renderArmor(GuiGraphics guiGraphics, Player player, int i, int j, int k, int l) {
		int m = player.getArmorValue();
		if (m > 0) {
			int n = i - (j - 1) * k - 10;

			for (int o = 0; o < 10; o++) {
				int p = l + o * 8;
				if (o * 2 + 1 < m) {
					guiGraphics.blitSprite(RenderType::guiTextured, ARMOR_FULL_SPRITE, p, n, 9, 9);
				}

				if (o * 2 + 1 == m) {
					guiGraphics.blitSprite(RenderType::guiTextured, ARMOR_HALF_SPRITE, p, n, 9, 9);
				}

				if (o * 2 + 1 > m) {
					guiGraphics.blitSprite(RenderType::guiTextured, ARMOR_EMPTY_SPRITE, p, n, 9, 9);
				}
			}
		}
	}

	private void renderHearts(GuiGraphics guiGraphics, Player player, int i, int j, int k, int l, float f, int m, int n, int o, boolean bl) {
		Gui.HeartType heartType = Gui.HeartType.forPlayer(player);
		boolean bl2 = player.level().getLevelData().isHardcore();
		int p = Mth.ceil((double)f / 2.0);
		int q = Mth.ceil((double)o / 2.0);
		int r = p * 2;

		for (int s = p + q - 1; s >= 0; s--) {
			int t = s / 10;
			int u = s % 10;
			int v = i + u * 8;
			int w = j - t * k;
			if (m + o <= 4) {
				w += this.random.nextInt(2);
			}

			if (s < p && s == l) {
				w -= 2;
			}

			this.renderHeart(guiGraphics, Gui.HeartType.CONTAINER, v, w, bl2, bl, false);
			int x = s * 2;
			boolean bl3 = s >= p;
			if (bl3) {
				int y = x - r;
				if (y < o) {
					boolean bl4 = y + 1 == o;
					this.renderHeart(guiGraphics, heartType == Gui.HeartType.WITHERED ? heartType : Gui.HeartType.ABSORBING, v, w, bl2, false, bl4);
				}
			}

			if (bl && x < n) {
				boolean bl5 = x + 1 == n;
				this.renderHeart(guiGraphics, heartType, v, w, bl2, true, bl5);
			}

			if (x < m) {
				boolean bl5 = x + 1 == m;
				this.renderHeart(guiGraphics, heartType, v, w, bl2, false, bl5);
			}
		}
	}

	private void renderHeart(GuiGraphics guiGraphics, Gui.HeartType heartType, int i, int j, boolean bl, boolean bl2, boolean bl3) {
		guiGraphics.blitSprite(RenderType::guiTextured, heartType.getSprite(bl, bl3, bl2), i, j, 9, 9);
	}

	private void renderFood(GuiGraphics guiGraphics, Player player, int i, int j) {
		FoodData foodData = player.getFoodData();
		int k = foodData.getFoodLevel();

		for (int l = 0; l < 10; l++) {
			int m = i;
			ResourceLocation resourceLocation;
			ResourceLocation resourceLocation2;
			ResourceLocation resourceLocation3;
			if (player.hasEffect(MobEffects.HUNGER)) {
				resourceLocation = FOOD_EMPTY_HUNGER_SPRITE;
				resourceLocation2 = FOOD_HALF_HUNGER_SPRITE;
				resourceLocation3 = FOOD_FULL_HUNGER_SPRITE;
			} else {
				resourceLocation = FOOD_EMPTY_SPRITE;
				resourceLocation2 = FOOD_HALF_SPRITE;
				resourceLocation3 = FOOD_FULL_SPRITE;
			}

			if (player.getFoodData().getSaturationLevel() <= 0.0F && this.tickCount % (k * 3 + 1) == 0) {
				m = i + (this.random.nextInt(3) - 1);
			}

			int n = j - l * 8 - 9;
			guiGraphics.blitSprite(RenderType::guiTextured, resourceLocation, n, m, 9, 9);
			if (l * 2 + 1 < k) {
				guiGraphics.blitSprite(RenderType::guiTextured, resourceLocation3, n, m, 9, 9);
			}

			if (l * 2 + 1 == k) {
				guiGraphics.blitSprite(RenderType::guiTextured, resourceLocation2, n, m, 9, 9);
			}
		}
	}

	private void renderVehicleHealth(GuiGraphics guiGraphics) {
		LivingEntity livingEntity = this.getPlayerVehicleWithHealth();
		if (livingEntity != null) {
			int i = this.getVehicleMaxHearts(livingEntity);
			if (i != 0) {
				int j = (int)Math.ceil((double)livingEntity.getHealth());
				this.minecraft.getProfiler().popPush("mountHealth");
				int k = guiGraphics.guiHeight() - 39;
				int l = guiGraphics.guiWidth() / 2 + 91;
				int m = k;

				for (int n = 0; i > 0; n += 20) {
					int o = Math.min(i, 10);
					i -= o;

					for (int p = 0; p < o; p++) {
						int q = l - p * 8 - 9;
						guiGraphics.blitSprite(RenderType::guiTextured, HEART_VEHICLE_CONTAINER_SPRITE, q, m, 9, 9);
						if (p * 2 + 1 + n < j) {
							guiGraphics.blitSprite(RenderType::guiTextured, HEART_VEHICLE_FULL_SPRITE, q, m, 9, 9);
						}

						if (p * 2 + 1 + n == j) {
							guiGraphics.blitSprite(RenderType::guiTextured, HEART_VEHICLE_HALF_SPRITE, q, m, 9, 9);
						}
					}

					m -= 10;
				}
			}
		}
	}

	private void renderTextureOverlay(GuiGraphics guiGraphics, ResourceLocation resourceLocation, float f) {
		int i = ARGB.white(f);
		guiGraphics.blit(
			RenderType::guiTexturedOverlay,
			resourceLocation,
			0,
			0,
			0.0F,
			0.0F,
			guiGraphics.guiWidth(),
			guiGraphics.guiHeight(),
			guiGraphics.guiWidth(),
			guiGraphics.guiHeight(),
			i
		);
	}

	private void renderSpyglassOverlay(GuiGraphics guiGraphics, float f) {
		float g = (float)Math.min(guiGraphics.guiWidth(), guiGraphics.guiHeight());
		float i = Math.min((float)guiGraphics.guiWidth() / g, (float)guiGraphics.guiHeight() / g) * f;
		int j = Mth.floor(g * i);
		int k = Mth.floor(g * i);
		int l = (guiGraphics.guiWidth() - j) / 2;
		int m = (guiGraphics.guiHeight() - k) / 2;
		int n = l + j;
		int o = m + k;
		guiGraphics.blit(RenderType::guiTextured, SPYGLASS_SCOPE_LOCATION, l, m, 0.0F, 0.0F, j, k, j, k);
		guiGraphics.fill(RenderType.guiOverlay(), 0, o, guiGraphics.guiWidth(), guiGraphics.guiHeight(), -90, -16777216);
		guiGraphics.fill(RenderType.guiOverlay(), 0, 0, guiGraphics.guiWidth(), m, -90, -16777216);
		guiGraphics.fill(RenderType.guiOverlay(), 0, m, l, o, -90, -16777216);
		guiGraphics.fill(RenderType.guiOverlay(), n, m, guiGraphics.guiWidth(), o, -90, -16777216);
	}

	private void updateVignetteBrightness(Entity entity) {
		BlockPos blockPos = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
		float f = LightTexture.getBrightness(entity.level().dimensionType(), entity.level().getMaxLocalRawBrightness(blockPos));
		float g = Mth.clamp(1.0F - f, 0.0F, 1.0F);
		this.vignetteBrightness = this.vignetteBrightness + (g - this.vignetteBrightness) * 0.01F;
	}

	private void renderVignette(GuiGraphics guiGraphics, @Nullable Entity entity) {
		WorldBorder worldBorder = this.minecraft.level.getWorldBorder();
		float f = 0.0F;
		if (entity != null) {
			float g = (float)worldBorder.getDistanceToBorder(entity);
			double d = Math.min(
				worldBorder.getLerpSpeed() * (double)worldBorder.getWarningTime() * 1000.0, Math.abs(worldBorder.getLerpTarget() - worldBorder.getSize())
			);
			double e = Math.max((double)worldBorder.getWarningBlocks(), d);
			if ((double)g < e) {
				f = 1.0F - (float)((double)g / e);
			}
		}

		int i;
		if (f > 0.0F) {
			f = Mth.clamp(f, 0.0F, 1.0F);
			i = ARGB.colorFromFloat(1.0F, 0.0F, f, f);
		} else {
			float h = this.vignetteBrightness;
			h = Mth.clamp(h, 0.0F, 1.0F);
			i = ARGB.colorFromFloat(1.0F, h, h, h);
		}

		guiGraphics.blit(
			RenderType::vignette,
			VIGNETTE_LOCATION,
			0,
			0,
			0.0F,
			0.0F,
			guiGraphics.guiWidth(),
			guiGraphics.guiHeight(),
			guiGraphics.guiWidth(),
			guiGraphics.guiHeight(),
			i
		);
	}

	private void renderPortalOverlay(GuiGraphics guiGraphics, float f) {
		if (f < 1.0F) {
			f *= f;
			f *= f;
			f = f * 0.8F + 0.2F;
		}

		int i = ARGB.white(f);
		TextureAtlasSprite textureAtlasSprite = this.minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.NETHER_PORTAL.defaultBlockState());
		guiGraphics.blitSprite(RenderType::guiTexturedOverlay, textureAtlasSprite, 0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), i);
	}

	private void renderConfusionOverlay(GuiGraphics guiGraphics, float f) {
		int i = guiGraphics.guiWidth();
		int j = guiGraphics.guiHeight();
		guiGraphics.pose().pushPose();
		float g = Mth.lerp(f, 2.0F, 1.0F);
		guiGraphics.pose().translate((float)i / 2.0F, (float)j / 2.0F, 0.0F);
		guiGraphics.pose().scale(g, g, g);
		guiGraphics.pose().translate((float)(-i) / 2.0F, (float)(-j) / 2.0F, 0.0F);
		float h = 0.2F * f;
		float k = 0.4F * f;
		float l = 0.2F * f;
		guiGraphics.blit(resourceLocation -> RenderType.guiNauseaOverlay(), NAUSEA_LOCATION, 0, 0, 0.0F, 0.0F, i, j, i, j, ARGB.colorFromFloat(1.0F, h, k, l));
		guiGraphics.pose().popPose();
	}

	private void renderSlot(GuiGraphics guiGraphics, int i, int j, DeltaTracker deltaTracker, Player player, ItemStack itemStack, int k) {
		if (!itemStack.isEmpty()) {
			float f = (float)itemStack.getPopTime() - deltaTracker.getGameTimeDeltaPartialTick(false);
			if (f > 0.0F) {
				float g = 1.0F + f / 5.0F;
				guiGraphics.pose().pushPose();
				guiGraphics.pose().translate((float)(i + 8), (float)(j + 12), 0.0F);
				guiGraphics.pose().scale(1.0F / g, (g + 1.0F) / 2.0F, 1.0F);
				guiGraphics.pose().translate((float)(-(i + 8)), (float)(-(j + 12)), 0.0F);
			}

			guiGraphics.renderItem(player, itemStack, i, j, k);
			if (f > 0.0F) {
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
		this.minecraft.getToastManager().clear();
		this.debugOverlay.reset();
		this.chat.clearMessages(true);
	}

	public BossHealthOverlay getBossOverlay() {
		return this.bossOverlay;
	}

	public DebugScreenOverlay getDebugOverlay() {
		return this.debugOverlay;
	}

	public void clearCache() {
		this.debugOverlay.clearChunkCache();
	}

	public void renderSavingIndicator(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		if (this.minecraft.options.showAutosaveIndicator().get() && (this.autosaveIndicatorValue > 0.0F || this.lastAutosaveIndicatorValue > 0.0F)) {
			int i = Mth.floor(
				255.0F * Mth.clamp(Mth.lerp(deltaTracker.getRealtimeDeltaTicks(), this.lastAutosaveIndicatorValue, this.autosaveIndicatorValue), 0.0F, 1.0F)
			);
			if (i > 8) {
				Font font = this.getFont();
				int j = font.width(SAVING_TEXT);
				int k = ARGB.color(i, -1);
				int l = guiGraphics.guiWidth() - j - 5;
				int m = guiGraphics.guiHeight() - 9 - 5;
				guiGraphics.drawStringWithBackdrop(font, SAVING_TEXT, l, m, j, k);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	static enum HeartType {
		CONTAINER(
			ResourceLocation.withDefaultNamespace("hud/heart/container"),
			ResourceLocation.withDefaultNamespace("hud/heart/container_blinking"),
			ResourceLocation.withDefaultNamespace("hud/heart/container"),
			ResourceLocation.withDefaultNamespace("hud/heart/container_blinking"),
			ResourceLocation.withDefaultNamespace("hud/heart/container_hardcore"),
			ResourceLocation.withDefaultNamespace("hud/heart/container_hardcore_blinking"),
			ResourceLocation.withDefaultNamespace("hud/heart/container_hardcore"),
			ResourceLocation.withDefaultNamespace("hud/heart/container_hardcore_blinking")
		),
		NORMAL(
			ResourceLocation.withDefaultNamespace("hud/heart/full"),
			ResourceLocation.withDefaultNamespace("hud/heart/full_blinking"),
			ResourceLocation.withDefaultNamespace("hud/heart/half"),
			ResourceLocation.withDefaultNamespace("hud/heart/half_blinking"),
			ResourceLocation.withDefaultNamespace("hud/heart/hardcore_full"),
			ResourceLocation.withDefaultNamespace("hud/heart/hardcore_full_blinking"),
			ResourceLocation.withDefaultNamespace("hud/heart/hardcore_half"),
			ResourceLocation.withDefaultNamespace("hud/heart/hardcore_half_blinking")
		),
		POISIONED(
			ResourceLocation.withDefaultNamespace("hud/heart/poisoned_full"),
			ResourceLocation.withDefaultNamespace("hud/heart/poisoned_full_blinking"),
			ResourceLocation.withDefaultNamespace("hud/heart/poisoned_half"),
			ResourceLocation.withDefaultNamespace("hud/heart/poisoned_half_blinking"),
			ResourceLocation.withDefaultNamespace("hud/heart/poisoned_hardcore_full"),
			ResourceLocation.withDefaultNamespace("hud/heart/poisoned_hardcore_full_blinking"),
			ResourceLocation.withDefaultNamespace("hud/heart/poisoned_hardcore_half"),
			ResourceLocation.withDefaultNamespace("hud/heart/poisoned_hardcore_half_blinking")
		),
		WITHERED(
			ResourceLocation.withDefaultNamespace("hud/heart/withered_full"),
			ResourceLocation.withDefaultNamespace("hud/heart/withered_full_blinking"),
			ResourceLocation.withDefaultNamespace("hud/heart/withered_half"),
			ResourceLocation.withDefaultNamespace("hud/heart/withered_half_blinking"),
			ResourceLocation.withDefaultNamespace("hud/heart/withered_hardcore_full"),
			ResourceLocation.withDefaultNamespace("hud/heart/withered_hardcore_full_blinking"),
			ResourceLocation.withDefaultNamespace("hud/heart/withered_hardcore_half"),
			ResourceLocation.withDefaultNamespace("hud/heart/withered_hardcore_half_blinking")
		),
		ABSORBING(
			ResourceLocation.withDefaultNamespace("hud/heart/absorbing_full"),
			ResourceLocation.withDefaultNamespace("hud/heart/absorbing_full_blinking"),
			ResourceLocation.withDefaultNamespace("hud/heart/absorbing_half"),
			ResourceLocation.withDefaultNamespace("hud/heart/absorbing_half_blinking"),
			ResourceLocation.withDefaultNamespace("hud/heart/absorbing_hardcore_full"),
			ResourceLocation.withDefaultNamespace("hud/heart/absorbing_hardcore_full_blinking"),
			ResourceLocation.withDefaultNamespace("hud/heart/absorbing_hardcore_half"),
			ResourceLocation.withDefaultNamespace("hud/heart/absorbing_hardcore_half_blinking")
		),
		FROZEN(
			ResourceLocation.withDefaultNamespace("hud/heart/frozen_full"),
			ResourceLocation.withDefaultNamespace("hud/heart/frozen_full_blinking"),
			ResourceLocation.withDefaultNamespace("hud/heart/frozen_half"),
			ResourceLocation.withDefaultNamespace("hud/heart/frozen_half_blinking"),
			ResourceLocation.withDefaultNamespace("hud/heart/frozen_hardcore_full"),
			ResourceLocation.withDefaultNamespace("hud/heart/frozen_hardcore_full_blinking"),
			ResourceLocation.withDefaultNamespace("hud/heart/frozen_hardcore_half"),
			ResourceLocation.withDefaultNamespace("hud/heart/frozen_hardcore_half_blinking")
		);

		private final ResourceLocation full;
		private final ResourceLocation fullBlinking;
		private final ResourceLocation half;
		private final ResourceLocation halfBlinking;
		private final ResourceLocation hardcoreFull;
		private final ResourceLocation hardcoreFullBlinking;
		private final ResourceLocation hardcoreHalf;
		private final ResourceLocation hardcoreHalfBlinking;

		private HeartType(
			final ResourceLocation resourceLocation,
			final ResourceLocation resourceLocation2,
			final ResourceLocation resourceLocation3,
			final ResourceLocation resourceLocation4,
			final ResourceLocation resourceLocation5,
			final ResourceLocation resourceLocation6,
			final ResourceLocation resourceLocation7,
			final ResourceLocation resourceLocation8
		) {
			this.full = resourceLocation;
			this.fullBlinking = resourceLocation2;
			this.half = resourceLocation3;
			this.halfBlinking = resourceLocation4;
			this.hardcoreFull = resourceLocation5;
			this.hardcoreFullBlinking = resourceLocation6;
			this.hardcoreHalf = resourceLocation7;
			this.hardcoreHalfBlinking = resourceLocation8;
		}

		public ResourceLocation getSprite(boolean bl, boolean bl2, boolean bl3) {
			if (!bl) {
				if (bl2) {
					return bl3 ? this.halfBlinking : this.half;
				} else {
					return bl3 ? this.fullBlinking : this.full;
				}
			} else if (bl2) {
				return bl3 ? this.hardcoreHalfBlinking : this.hardcoreHalf;
			} else {
				return bl3 ? this.hardcoreFullBlinking : this.hardcoreFull;
			}
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
