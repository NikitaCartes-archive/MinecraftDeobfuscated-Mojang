/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class Gui
extends GuiComponent {
    private static final ResourceLocation VIGNETTE_LOCATION = new ResourceLocation("textures/misc/vignette.png");
    private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    private static final ResourceLocation PUMPKIN_BLUR_LOCATION = new ResourceLocation("textures/misc/pumpkinblur.png");
    private static final ResourceLocation SPYGLASS_SCOPE_LOCATION = new ResourceLocation("textures/misc/spyglass_scope.png");
    private static final ResourceLocation POWDER_SNOW_OUTLINE_LOCATION = new ResourceLocation("textures/misc/powder_snow_outline.png");
    private static final Component DEMO_EXPIRED_TEXT = Component.translatable("demo.demoExpired");
    private static final Component SAVING_TEXT = Component.translatable("menu.savingLevel");
    private static final int COLOR_WHITE = 0xFFFFFF;
    private static final float MIN_CROSSHAIR_ATTACK_SPEED = 5.0f;
    private static final int NUM_HEARTS_PER_ROW = 10;
    private static final int LINE_HEIGHT = 10;
    private static final String SPACER = ": ";
    private static final float PORTAL_OVERLAY_ALPHA_MIN = 0.2f;
    private static final int HEART_SIZE = 9;
    private static final int HEART_SEPARATION = 8;
    private static final float AUTOSAVE_FADE_SPEED_FACTOR = 0.2f;
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
    public float vignetteBrightness = 1.0f;
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

    public void render(PoseStack poseStack, float f) {
        int l;
        float h;
        Window window = this.minecraft.getWindow();
        this.screenWidth = window.getGuiScaledWidth();
        this.screenHeight = window.getGuiScaledHeight();
        Font font = this.getFont();
        RenderSystem.enableBlend();
        if (Minecraft.useFancyGraphics()) {
            this.renderVignette(poseStack, this.minecraft.getCameraEntity());
        } else {
            RenderSystem.enableDepthTest();
        }
        float g = this.minecraft.getDeltaFrameTime();
        this.scopeScale = Mth.lerp(0.5f * g, this.scopeScale, 1.125f);
        if (this.minecraft.options.getCameraType().isFirstPerson()) {
            if (this.minecraft.player.isScoping()) {
                this.renderSpyglassOverlay(poseStack, this.scopeScale);
            } else {
                this.scopeScale = 0.5f;
                ItemStack itemStack = this.minecraft.player.getInventory().getArmor(3);
                if (itemStack.is(Blocks.CARVED_PUMPKIN.asItem())) {
                    this.renderTextureOverlay(poseStack, PUMPKIN_BLUR_LOCATION, 1.0f);
                }
            }
        }
        if (this.minecraft.player.getTicksFrozen() > 0) {
            this.renderTextureOverlay(poseStack, POWDER_SNOW_OUTLINE_LOCATION, this.minecraft.player.getPercentFrozen());
        }
        if ((h = Mth.lerp(f, this.minecraft.player.oPortalTime, this.minecraft.player.portalTime)) > 0.0f && !this.minecraft.player.hasEffect(MobEffects.CONFUSION)) {
            this.renderPortalOverlay(poseStack, h);
        }
        if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
            this.spectatorGui.renderHotbar(poseStack);
        } else if (!this.minecraft.options.hideGui) {
            this.renderHotbar(f, poseStack);
        }
        if (!this.minecraft.options.hideGui) {
            RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
            RenderSystem.enableBlend();
            this.renderCrosshair(poseStack);
            this.minecraft.getProfiler().push("bossHealth");
            this.bossOverlay.render(poseStack);
            this.minecraft.getProfiler().pop();
            RenderSystem.setShaderTexture(0, GUI_ICONS_LOCATION);
            if (this.minecraft.gameMode.canHurtPlayer()) {
                this.renderPlayerHealth(poseStack);
            }
            this.renderVehicleHealth(poseStack);
            RenderSystem.disableBlend();
            int i = this.screenWidth / 2 - 91;
            PlayerRideableJumping playerRideableJumping = this.minecraft.player.jumpableVehicle();
            if (playerRideableJumping != null) {
                this.renderJumpMeter(playerRideableJumping, poseStack, i);
            } else if (this.minecraft.gameMode.hasExperience()) {
                this.renderExperienceBar(poseStack, i);
            }
            if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
                this.renderSelectedItemName(poseStack);
            } else if (this.minecraft.player.isSpectator()) {
                this.spectatorGui.renderTooltip(poseStack);
            }
        }
        if (this.minecraft.player.getSleepTimer() > 0) {
            this.minecraft.getProfiler().push("sleep");
            RenderSystem.disableDepthTest();
            float j = this.minecraft.player.getSleepTimer();
            float k = j / 100.0f;
            if (k > 1.0f) {
                k = 1.0f - (j - 100.0f) / 10.0f;
            }
            l = (int)(220.0f * k) << 24 | 0x101020;
            Gui.fill(poseStack, 0, 0, this.screenWidth, this.screenHeight, l);
            RenderSystem.enableDepthTest();
            this.minecraft.getProfiler().pop();
        }
        if (this.minecraft.isDemo()) {
            this.renderDemoOverlay(poseStack);
        }
        this.renderEffects(poseStack);
        if (this.minecraft.options.renderDebug) {
            this.debugScreen.render(poseStack);
        }
        if (!this.minecraft.options.hideGui) {
            Objective objective2;
            int o;
            int n;
            if (this.overlayMessageString != null && this.overlayMessageTime > 0) {
                this.minecraft.getProfiler().push("overlayMessage");
                float j = (float)this.overlayMessageTime - f;
                int m = (int)(j * 255.0f / 20.0f);
                if (m > 255) {
                    m = 255;
                }
                if (m > 8) {
                    poseStack.pushPose();
                    poseStack.translate(this.screenWidth / 2, this.screenHeight - 68, 0.0f);
                    l = 0xFFFFFF;
                    if (this.animateOverlayMessageColor) {
                        l = Mth.hsvToRgb(j / 50.0f, 0.7f, 0.6f) & 0xFFFFFF;
                    }
                    n = m << 24 & 0xFF000000;
                    o = font.width(this.overlayMessageString);
                    this.drawBackdrop(poseStack, font, -4, o, 0xFFFFFF | n);
                    font.drawShadow(poseStack, this.overlayMessageString, (float)(-o / 2), -4.0f, l | n);
                    poseStack.popPose();
                }
                this.minecraft.getProfiler().pop();
            }
            if (this.title != null && this.titleTime > 0) {
                this.minecraft.getProfiler().push("titleAndSubtitle");
                float j = (float)this.titleTime - f;
                int m = 255;
                if (this.titleTime > this.titleFadeOutTime + this.titleStayTime) {
                    float p = (float)(this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime) - j;
                    m = (int)(p * 255.0f / (float)this.titleFadeInTime);
                }
                if (this.titleTime <= this.titleFadeOutTime) {
                    m = (int)(j * 255.0f / (float)this.titleFadeOutTime);
                }
                if ((m = Mth.clamp(m, 0, 255)) > 8) {
                    poseStack.pushPose();
                    poseStack.translate(this.screenWidth / 2, this.screenHeight / 2, 0.0f);
                    RenderSystem.enableBlend();
                    poseStack.pushPose();
                    poseStack.scale(4.0f, 4.0f, 4.0f);
                    int l2 = m << 24 & 0xFF000000;
                    n = font.width(this.title);
                    this.drawBackdrop(poseStack, font, -10, n, 0xFFFFFF | l2);
                    font.drawShadow(poseStack, this.title, (float)(-n / 2), -10.0f, 0xFFFFFF | l2);
                    poseStack.popPose();
                    if (this.subtitle != null) {
                        poseStack.pushPose();
                        poseStack.scale(2.0f, 2.0f, 2.0f);
                        o = font.width(this.subtitle);
                        this.drawBackdrop(poseStack, font, 5, o, 0xFFFFFF | l2);
                        font.drawShadow(poseStack, this.subtitle, (float)(-o / 2), 5.0f, 0xFFFFFF | l2);
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
            if (playerTeam != null && (n = playerTeam.getColor().getId()) >= 0) {
                objective = scoreboard.getDisplayObjective(3 + n);
            }
            Objective objective3 = objective2 = objective != null ? objective : scoreboard.getDisplayObjective(1);
            if (objective2 != null) {
                this.displayScoreboardSidebar(poseStack, objective2);
            }
            RenderSystem.enableBlend();
            o = Mth.floor(this.minecraft.mouseHandler.xpos() * (double)window.getGuiScaledWidth() / (double)window.getScreenWidth());
            int q = Mth.floor(this.minecraft.mouseHandler.ypos() * (double)window.getGuiScaledHeight() / (double)window.getScreenHeight());
            this.minecraft.getProfiler().push("chat");
            this.chat.render(poseStack, this.tickCount, o, q);
            this.minecraft.getProfiler().pop();
            objective2 = scoreboard.getDisplayObjective(0);
            if (this.minecraft.options.keyPlayerList.isDown() && (!this.minecraft.isLocalServer() || this.minecraft.player.connection.getListedOnlinePlayers().size() > 1 || objective2 != null)) {
                this.tabList.setVisible(true);
                this.tabList.render(poseStack, this.screenWidth, scoreboard, objective2);
            } else {
                this.tabList.setVisible(false);
            }
            this.renderSavingIndicator(poseStack);
        }
    }

    private void drawBackdrop(PoseStack poseStack, Font font, int i, int j, int k) {
        int l = this.minecraft.options.getBackgroundColor(0.0f);
        if (l != 0) {
            int m = -j / 2;
            Gui.fill(poseStack, m - 2, i - 2, m + j + 2, i + font.lineHeight + 2, FastColor.ARGB32.multiply(l, k));
        }
    }

    private void renderCrosshair(PoseStack poseStack) {
        Options options = this.minecraft.options;
        if (!options.getCameraType().isFirstPerson()) {
            return;
        }
        if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR && !this.canRenderCrosshairForSpectator(this.minecraft.hitResult)) {
            return;
        }
        if (options.renderDebug && !options.hideGui && !this.minecraft.player.isReducedDebugInfo() && !options.reducedDebugInfo().get().booleanValue()) {
            Camera camera = this.minecraft.gameRenderer.getMainCamera();
            PoseStack poseStack2 = RenderSystem.getModelViewStack();
            poseStack2.pushPose();
            poseStack2.mulPoseMatrix(poseStack.last().pose());
            poseStack2.translate(this.screenWidth / 2, this.screenHeight / 2, 0.0f);
            poseStack2.mulPose(Axis.XN.rotationDegrees(camera.getXRot()));
            poseStack2.mulPose(Axis.YP.rotationDegrees(camera.getYRot()));
            poseStack2.scale(-1.0f, -1.0f, -1.0f);
            RenderSystem.applyModelViewMatrix();
            RenderSystem.renderCrosshair(10);
            poseStack2.popPose();
            RenderSystem.applyModelViewMatrix();
        } else {
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            int i = 15;
            Gui.blit(poseStack, (this.screenWidth - 15) / 2, (this.screenHeight - 15) / 2, 0, 0, 15, 15);
            if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.CROSSHAIR) {
                float f = this.minecraft.player.getAttackStrengthScale(0.0f);
                boolean bl = false;
                if (this.minecraft.crosshairPickEntity != null && this.minecraft.crosshairPickEntity instanceof LivingEntity && f >= 1.0f) {
                    bl = this.minecraft.player.getCurrentItemAttackStrengthDelay() > 5.0f;
                    bl &= this.minecraft.crosshairPickEntity.isAlive();
                }
                int j = this.screenHeight / 2 - 7 + 16;
                int k = this.screenWidth / 2 - 8;
                if (bl) {
                    Gui.blit(poseStack, k, j, 68, 94, 16, 16);
                } else if (f < 1.0f) {
                    int l = (int)(f * 17.0f);
                    Gui.blit(poseStack, k, j, 36, 94, 16, 4);
                    Gui.blit(poseStack, k, j, 52, 94, l, 4);
                }
            }
            RenderSystem.defaultBlendFunc();
        }
    }

    private boolean canRenderCrosshairForSpectator(HitResult hitResult) {
        if (hitResult == null) {
            return false;
        }
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            return ((EntityHitResult)hitResult).getEntity() instanceof MenuProvider;
        }
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            ClientLevel level = this.minecraft.level;
            BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
            return level.getBlockState(blockPos).getMenuProvider(level, blockPos) != null;
        }
        return false;
    }

    protected void renderEffects(PoseStack poseStack) {
        EffectRenderingInventoryScreen effectRenderingInventoryScreen;
        Screen screen;
        Collection<MobEffectInstance> collection = this.minecraft.player.getActiveEffects();
        if (collection.isEmpty() || (screen = this.minecraft.screen) instanceof EffectRenderingInventoryScreen && (effectRenderingInventoryScreen = (EffectRenderingInventoryScreen)screen).canSeeEffects()) {
            return;
        }
        RenderSystem.enableBlend();
        int i = 0;
        int j = 0;
        MobEffectTextureManager mobEffectTextureManager = this.minecraft.getMobEffectTextures();
        ArrayList<Runnable> list = Lists.newArrayListWithExpectedSize(collection.size());
        RenderSystem.setShaderTexture(0, AbstractContainerScreen.INVENTORY_LOCATION);
        for (MobEffectInstance mobEffectInstance : Ordering.natural().reverse().sortedCopy(collection)) {
            int n;
            MobEffect mobEffect = mobEffectInstance.getEffect();
            if (!mobEffectInstance.showIcon()) continue;
            int k = this.screenWidth;
            int l = 1;
            if (this.minecraft.isDemo()) {
                l += 15;
            }
            if (mobEffect.isBeneficial()) {
                k -= 25 * ++i;
            } else {
                k -= 25 * ++j;
                l += 26;
            }
            float f = 1.0f;
            if (mobEffectInstance.isAmbient()) {
                Gui.blit(poseStack, k, l, 165, 166, 24, 24);
            } else {
                Gui.blit(poseStack, k, l, 141, 166, 24, 24);
                if (mobEffectInstance.endsWithin(200)) {
                    int m = mobEffectInstance.getDuration();
                    n = 10 - m / 20;
                    f = Mth.clamp((float)m / 10.0f / 5.0f * 0.5f, 0.0f, 0.5f) + Mth.cos((float)m * (float)Math.PI / 5.0f) * Mth.clamp((float)n / 10.0f * 0.25f, 0.0f, 0.25f);
                }
            }
            TextureAtlasSprite textureAtlasSprite = mobEffectTextureManager.get(mobEffect);
            n = k;
            int o = l;
            float g = f;
            list.add(() -> {
                RenderSystem.setShaderTexture(0, textureAtlasSprite.atlasLocation());
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, g);
                Gui.blit(poseStack, n + 3, o + 3, 0, 18, 18, textureAtlasSprite);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            });
        }
        list.forEach(Runnable::run);
    }

    private void renderHotbar(float f, PoseStack poseStack) {
        float g;
        int o;
        int n;
        int m;
        Player player = this.getCameraPlayer();
        if (player == null) {
            return;
        }
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        ItemStack itemStack = player.getOffhandItem();
        HumanoidArm humanoidArm = player.getMainArm().getOpposite();
        int i = this.screenWidth / 2;
        int j = 182;
        int k = 91;
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.0f, -90.0f);
        Gui.blit(poseStack, i - 91, this.screenHeight - 22, 0, 0, 182, 22);
        Gui.blit(poseStack, i - 91 - 1 + player.getInventory().selected * 20, this.screenHeight - 22 - 1, 0, 22, 24, 22);
        if (!itemStack.isEmpty()) {
            if (humanoidArm == HumanoidArm.LEFT) {
                Gui.blit(poseStack, i - 91 - 29, this.screenHeight - 23, 24, 22, 29, 24);
            } else {
                Gui.blit(poseStack, i + 91, this.screenHeight - 23, 53, 22, 29, 24);
            }
        }
        poseStack.popPose();
        int l = 1;
        for (m = 0; m < 9; ++m) {
            n = i - 90 + m * 20 + 2;
            o = this.screenHeight - 16 - 3;
            this.renderSlot(poseStack, n, o, f, player, player.getInventory().items.get(m), l++);
        }
        if (!itemStack.isEmpty()) {
            m = this.screenHeight - 16 - 3;
            if (humanoidArm == HumanoidArm.LEFT) {
                this.renderSlot(poseStack, i - 91 - 26, m, f, player, itemStack, l++);
            } else {
                this.renderSlot(poseStack, i + 91 + 10, m, f, player, itemStack, l++);
            }
        }
        RenderSystem.enableBlend();
        if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR && (g = this.minecraft.player.getAttackStrengthScale(0.0f)) < 1.0f) {
            n = this.screenHeight - 20;
            o = i + 91 + 6;
            if (humanoidArm == HumanoidArm.RIGHT) {
                o = i - 91 - 22;
            }
            RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
            int p = (int)(g * 19.0f);
            Gui.blit(poseStack, o, n, 0, 94, 18, 18);
            Gui.blit(poseStack, o, n + 18 - p, 18, 112 - p, 18, p);
        }
        RenderSystem.disableBlend();
    }

    public void renderJumpMeter(PlayerRideableJumping playerRideableJumping, PoseStack poseStack, int i) {
        this.minecraft.getProfiler().push("jumpBar");
        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
        float f = this.minecraft.player.getJumpRidingScale();
        int j = 182;
        int k = (int)(f * 183.0f);
        int l = this.screenHeight - 32 + 3;
        Gui.blit(poseStack, i, l, 0, 84, 182, 5);
        if (playerRideableJumping.getJumpCooldown() > 0) {
            Gui.blit(poseStack, i, l, 0, 74, 182, 5);
        } else if (k > 0) {
            Gui.blit(poseStack, i, l, 0, 89, k, 5);
        }
        this.minecraft.getProfiler().pop();
    }

    public void renderExperienceBar(PoseStack poseStack, int i) {
        int m;
        int l;
        this.minecraft.getProfiler().push("expBar");
        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
        int j = this.minecraft.player.getXpNeededForNextLevel();
        if (j > 0) {
            int k = 182;
            l = (int)(this.minecraft.player.experienceProgress * 183.0f);
            m = this.screenHeight - 32 + 3;
            Gui.blit(poseStack, i, m, 0, 64, 182, 5);
            if (l > 0) {
                Gui.blit(poseStack, i, m, 0, 69, l, 5);
            }
        }
        this.minecraft.getProfiler().pop();
        if (this.minecraft.player.experienceLevel > 0) {
            this.minecraft.getProfiler().push("expLevel");
            String string = "" + this.minecraft.player.experienceLevel;
            l = (this.screenWidth - this.getFont().width(string)) / 2;
            m = this.screenHeight - 31 - 4;
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
            int l;
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
            if ((l = (int)((float)this.toolHighlightTimer * 256.0f / 10.0f)) > 255) {
                l = 255;
            }
            if (l > 0) {
                Gui.fill(poseStack, j - 2, k - 2, j + i + 2, k + this.getFont().lineHeight + 2, this.minecraft.options.getBackgroundColor(0));
                this.getFont().drawShadow(poseStack, mutableComponent, (float)j, (float)k, 0xFFFFFF + (l << 24));
            }
        }
        this.minecraft.getProfiler().pop();
    }

    public void renderDemoOverlay(PoseStack poseStack) {
        this.minecraft.getProfiler().push("demo");
        Component component = this.minecraft.level.getGameTime() >= 120500L ? DEMO_EXPIRED_TEXT : Component.translatable("demo.remainingTime", StringUtil.formatTickDuration((int)(120500L - this.minecraft.level.getGameTime())));
        int i = this.getFont().width(component);
        this.getFont().drawShadow(poseStack, component, (float)(this.screenWidth - i - 10), 5.0f, 0xFFFFFF);
        this.minecraft.getProfiler().pop();
    }

    private void displayScoreboardSidebar(PoseStack poseStack, Objective objective) {
        int i;
        Scoreboard scoreboard = objective.getScoreboard();
        Collection<Score> collection = scoreboard.getPlayerScores(objective);
        List list = collection.stream().filter(score -> score.getOwner() != null && !score.getOwner().startsWith("#")).collect(Collectors.toList());
        collection = list.size() > 15 ? Lists.newArrayList(Iterables.skip(list, collection.size() - 15)) : list;
        ArrayList<Pair<Score, MutableComponent>> list2 = Lists.newArrayListWithCapacity(collection.size());
        Component component = objective.getDisplayName();
        int j = i = this.getFont().width(component);
        int k = this.getFont().width(SPACER);
        for (Score score2 : collection) {
            PlayerTeam playerTeam = scoreboard.getPlayersTeam(score2.getOwner());
            MutableComponent component2 = PlayerTeam.formatNameForTeam(playerTeam, Component.literal(score2.getOwner()));
            list2.add(Pair.of(score2, component2));
            j = Math.max(j, this.getFont().width(component2) + k + this.getFont().width(Integer.toString(score2.getScore())));
        }
        int l = collection.size() * this.getFont().lineHeight;
        int m = this.screenHeight / 2 + l / 3;
        int n = 3;
        int o = this.screenWidth - j - 3;
        int p = 0;
        int q = this.minecraft.options.getBackgroundColor(0.3f);
        int r = this.minecraft.options.getBackgroundColor(0.4f);
        for (Pair pair : list2) {
            Score score2 = (Score)pair.getFirst();
            Component component3 = (Component)pair.getSecond();
            String string = "" + ChatFormatting.RED + score2.getScore();
            int s = o;
            int t = m - ++p * this.getFont().lineHeight;
            int u = this.screenWidth - 3 + 2;
            Gui.fill(poseStack, s - 2, t, u, t + this.getFont().lineHeight, q);
            this.getFont().draw(poseStack, component3, (float)s, (float)t, -1);
            this.getFont().draw(poseStack, string, (float)(u - this.getFont().width(string)), (float)t, -1);
            if (p != collection.size()) continue;
            Gui.fill(poseStack, s - 2, t - this.getFont().lineHeight - 1, u, t - 1, r);
            Gui.fill(poseStack, s - 2, t - 1, u, t, q);
            this.getFont().draw(poseStack, component, (float)(s + j / 2 - i / 2), (float)(t - this.getFont().lineHeight), -1);
        }
    }

    private Player getCameraPlayer() {
        if (!(this.minecraft.getCameraEntity() instanceof Player)) {
            return null;
        }
        return (Player)this.minecraft.getCameraEntity();
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
        if (livingEntity == null || !livingEntity.showVehicleHealth()) {
            return 0;
        }
        float f = livingEntity.getMaxHealth();
        int i = (int)(f + 0.5f) / 2;
        if (i > 30) {
            i = 30;
        }
        return i;
    }

    private int getVisibleVehicleHeartRows(int i) {
        return (int)Math.ceil((double)i / 10.0);
    }

    private void renderPlayerHealth(PoseStack poseStack) {
        int ac;
        int ab;
        int aa;
        int z;
        int y;
        int x;
        Player player = this.getCameraPlayer();
        if (player == null) {
            return;
        }
        int i = Mth.ceil(player.getHealth());
        boolean bl = this.healthBlinkTime > (long)this.tickCount && (this.healthBlinkTime - (long)this.tickCount) / 3L % 2L == 1L;
        long l = Util.getMillis();
        if (i < this.lastHealth && player.invulnerableTime > 0) {
            this.lastHealthTime = l;
            this.healthBlinkTime = this.tickCount + 20;
        } else if (i > this.lastHealth && player.invulnerableTime > 0) {
            this.lastHealthTime = l;
            this.healthBlinkTime = this.tickCount + 10;
        }
        if (l - this.lastHealthTime > 1000L) {
            this.lastHealth = i;
            this.displayHealth = i;
            this.lastHealthTime = l;
        }
        this.lastHealth = i;
        int j = this.displayHealth;
        this.random.setSeed(this.tickCount * 312871);
        FoodData foodData = player.getFoodData();
        int k = foodData.getFoodLevel();
        int m = this.screenWidth / 2 - 91;
        int n = this.screenWidth / 2 + 91;
        int o = this.screenHeight - 39;
        float f = Math.max((float)player.getAttributeValue(Attributes.MAX_HEALTH), (float)Math.max(j, i));
        int p = Mth.ceil(player.getAbsorptionAmount());
        int q = Mth.ceil((f + (float)p) / 2.0f / 10.0f);
        int r = Math.max(10 - (q - 2), 3);
        int s = o - (q - 1) * r - 10;
        int t = o - 10;
        int u = player.getArmorValue();
        int v = -1;
        if (player.hasEffect(MobEffects.REGENERATION)) {
            v = this.tickCount % Mth.ceil(f + 5.0f);
        }
        this.minecraft.getProfiler().push("armor");
        for (int w = 0; w < 10; ++w) {
            if (u <= 0) continue;
            x = m + w * 8;
            if (w * 2 + 1 < u) {
                Gui.blit(poseStack, x, s, 34, 9, 9, 9);
            }
            if (w * 2 + 1 == u) {
                Gui.blit(poseStack, x, s, 25, 9, 9, 9);
            }
            if (w * 2 + 1 <= u) continue;
            Gui.blit(poseStack, x, s, 16, 9, 9, 9);
        }
        this.minecraft.getProfiler().popPush("health");
        this.renderHearts(poseStack, player, m, o, r, v, f, i, j, p, bl);
        LivingEntity livingEntity = this.getPlayerVehicleWithHealth();
        x = this.getVehicleMaxHearts(livingEntity);
        if (x == 0) {
            this.minecraft.getProfiler().popPush("food");
            for (y = 0; y < 10; ++y) {
                z = o;
                aa = 16;
                ab = 0;
                if (player.hasEffect(MobEffects.HUNGER)) {
                    aa += 36;
                    ab = 13;
                }
                if (player.getFoodData().getSaturationLevel() <= 0.0f && this.tickCount % (k * 3 + 1) == 0) {
                    z += this.random.nextInt(3) - 1;
                }
                ac = n - y * 8 - 9;
                Gui.blit(poseStack, ac, z, 16 + ab * 9, 27, 9, 9);
                if (y * 2 + 1 < k) {
                    Gui.blit(poseStack, ac, z, aa + 36, 27, 9, 9);
                }
                if (y * 2 + 1 != k) continue;
                Gui.blit(poseStack, ac, z, aa + 45, 27, 9, 9);
            }
            t -= 10;
        }
        this.minecraft.getProfiler().popPush("air");
        y = player.getMaxAirSupply();
        z = Math.min(player.getAirSupply(), y);
        if (player.isEyeInFluid(FluidTags.WATER) || z < y) {
            aa = this.getVisibleVehicleHeartRows(x) - 1;
            t -= aa * 10;
            ab = Mth.ceil((double)(z - 2) * 10.0 / (double)y);
            ac = Mth.ceil((double)z * 10.0 / (double)y) - ab;
            for (int ad = 0; ad < ab + ac; ++ad) {
                if (ad < ab) {
                    Gui.blit(poseStack, n - ad * 8 - 9, t, 16, 18, 9, 9);
                    continue;
                }
                Gui.blit(poseStack, n - ad * 8 - 9, t, 25, 18, 9, 9);
            }
        }
        this.minecraft.getProfiler().pop();
    }

    private void renderHearts(PoseStack poseStack, Player player, int i, int j, int k, int l, float f, int m, int n, int o, boolean bl) {
        HeartType heartType = HeartType.forPlayer(player);
        int p = 9 * (player.level.getLevelData().isHardcore() ? 5 : 0);
        int q = Mth.ceil((double)f / 2.0);
        int r = Mth.ceil((double)o / 2.0);
        int s = q * 2;
        for (int t = q + r - 1; t >= 0; --t) {
            boolean bl4;
            int z;
            boolean bl2;
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
            this.renderHeart(poseStack, HeartType.CONTAINER, w, x, p, bl, false);
            int y = t * 2;
            boolean bl3 = bl2 = t >= q;
            if (bl2 && (z = y - s) < o) {
                boolean bl32 = z + 1 == o;
                this.renderHeart(poseStack, heartType == HeartType.WITHERED ? heartType : HeartType.ABSORBING, w, x, p, false, bl32);
            }
            if (bl && y < n) {
                bl4 = y + 1 == n;
                this.renderHeart(poseStack, heartType, w, x, p, true, bl4);
            }
            if (y >= m) continue;
            bl4 = y + 1 == m;
            this.renderHeart(poseStack, heartType, w, x, p, false, bl4);
        }
    }

    private void renderHeart(PoseStack poseStack, HeartType heartType, int i, int j, int k, boolean bl, boolean bl2) {
        Gui.blit(poseStack, i, j, heartType.getX(bl2, bl), k, 9, 9);
    }

    private void renderVehicleHealth(PoseStack poseStack) {
        LivingEntity livingEntity = this.getPlayerVehicleWithHealth();
        if (livingEntity == null) {
            return;
        }
        int i = this.getVehicleMaxHearts(livingEntity);
        if (i == 0) {
            return;
        }
        int j = (int)Math.ceil(livingEntity.getHealth());
        this.minecraft.getProfiler().popPush("mountHealth");
        int k = this.screenHeight - 39;
        int l = this.screenWidth / 2 + 91;
        int m = k;
        int n = 0;
        boolean bl = false;
        while (i > 0) {
            int o = Math.min(i, 10);
            i -= o;
            for (int p = 0; p < o; ++p) {
                int q = 52;
                int r = 0;
                int s = l - p * 8 - 9;
                Gui.blit(poseStack, s, m, 52 + r * 9, 9, 9, 9);
                if (p * 2 + 1 + n < j) {
                    Gui.blit(poseStack, s, m, 88, 9, 9, 9);
                }
                if (p * 2 + 1 + n != j) continue;
                Gui.blit(poseStack, s, m, 97, 9, 9, 9);
            }
            m -= 10;
            n += 20;
        }
    }

    private void renderTextureOverlay(PoseStack poseStack, ResourceLocation resourceLocation, float f) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, f);
        RenderSystem.setShaderTexture(0, resourceLocation);
        Gui.blit(poseStack, 0, 0, -90, 0.0f, 0.0f, this.screenWidth, this.screenHeight, this.screenWidth, this.screenHeight);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderSpyglassOverlay(PoseStack poseStack, float f) {
        float g;
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        float h = g = (float)Math.min(this.screenWidth, this.screenHeight);
        float i = Math.min((float)this.screenWidth / g, (float)this.screenHeight / h) * f;
        int j = Mth.floor(g * i);
        int k = Mth.floor(h * i);
        int l = (this.screenWidth - j) / 2;
        int m = (this.screenHeight - k) / 2;
        int n = l + j;
        int o = m + k;
        RenderSystem.setShaderTexture(0, SPYGLASS_SCOPE_LOCATION);
        Gui.blit(poseStack, l, m, -90, 0.0f, 0.0f, j, k, j, k);
        Gui.fill(poseStack, 0, o, this.screenWidth, this.screenHeight, -90, -16777216);
        Gui.fill(poseStack, 0, 0, this.screenWidth, m, -90, -16777216);
        Gui.fill(poseStack, 0, m, l, o, -90, -16777216);
        Gui.fill(poseStack, n, m, this.screenWidth, o, -90, -16777216);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private void updateVignetteBrightness(Entity entity) {
        if (entity == null) {
            return;
        }
        BlockPos blockPos = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
        float f = LightTexture.getBrightness(entity.level.dimensionType(), entity.level.getMaxLocalRawBrightness(blockPos));
        float g = Mth.clamp(1.0f - f, 0.0f, 1.0f);
        this.vignetteBrightness += (g - this.vignetteBrightness) * 0.01f;
    }

    private void renderVignette(PoseStack poseStack, Entity entity) {
        WorldBorder worldBorder = this.minecraft.level.getWorldBorder();
        float f = (float)worldBorder.getDistanceToBorder(entity);
        double d = Math.min(worldBorder.getLerpSpeed() * (double)worldBorder.getWarningTime() * 1000.0, Math.abs(worldBorder.getLerpTarget() - worldBorder.getSize()));
        double e = Math.max((double)worldBorder.getWarningBlocks(), d);
        f = (double)f < e ? 1.0f - (float)((double)f / e) : 0.0f;
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        if (f > 0.0f) {
            f = Mth.clamp(f, 0.0f, 1.0f);
            RenderSystem.setShaderColor(0.0f, f, f, 1.0f);
        } else {
            float g = this.vignetteBrightness;
            g = Mth.clamp(g, 0.0f, 1.0f);
            RenderSystem.setShaderColor(g, g, g, 1.0f);
        }
        RenderSystem.setShaderTexture(0, VIGNETTE_LOCATION);
        Gui.blit(poseStack, 0, 0, -90, 0.0f, 0.0f, this.screenWidth, this.screenHeight, this.screenWidth, this.screenHeight);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.defaultBlendFunc();
    }

    private void renderPortalOverlay(PoseStack poseStack, float f) {
        if (f < 1.0f) {
            f *= f;
            f *= f;
            f = f * 0.8f + 0.2f;
        }
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, f);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite textureAtlasSprite = this.minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.NETHER_PORTAL.defaultBlockState());
        Gui.blit(poseStack, 0, 0, -90, this.screenWidth, this.screenHeight, textureAtlasSprite);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderSlot(PoseStack poseStack, int i, int j, float f, Player player, ItemStack itemStack, int k) {
        if (itemStack.isEmpty()) {
            return;
        }
        float g = (float)itemStack.getPopTime() - f;
        if (g > 0.0f) {
            float h = 1.0f + g / 5.0f;
            poseStack.pushPose();
            poseStack.translate(i + 8, j + 12, 0.0f);
            poseStack.scale(1.0f / h, (h + 1.0f) / 2.0f, 1.0f);
            poseStack.translate(-(i + 8), -(j + 12), 0.0f);
        }
        this.itemRenderer.renderAndDecorateItem(poseStack, player, itemStack, i, j, k);
        if (g > 0.0f) {
            poseStack.popPose();
        }
        this.itemRenderer.renderGuiItemDecorations(poseStack, this.minecraft.font, itemStack, i, j);
    }

    public void tick(boolean bl) {
        this.tickAutosaveIndicator();
        if (!bl) {
            this.tick();
        }
    }

    private void tick() {
        if (this.overlayMessageTime > 0) {
            --this.overlayMessageTime;
        }
        if (this.titleTime > 0) {
            --this.titleTime;
            if (this.titleTime <= 0) {
                this.title = null;
                this.subtitle = null;
            }
        }
        ++this.tickCount;
        Entity entity = this.minecraft.getCameraEntity();
        if (entity != null) {
            this.updateVignetteBrightness(entity);
        }
        if (this.minecraft.player != null) {
            ItemStack itemStack = this.minecraft.player.getInventory().getSelected();
            if (itemStack.isEmpty()) {
                this.toolHighlightTimer = 0;
            } else if (this.lastToolHighlight.isEmpty() || !itemStack.is(this.lastToolHighlight.getItem()) || !itemStack.getHoverName().equals(this.lastToolHighlight.getHoverName())) {
                this.toolHighlightTimer = (int)(40.0 * this.minecraft.options.notificationDisplayTime().get());
            } else if (this.toolHighlightTimer > 0) {
                --this.toolHighlightTimer;
            }
            this.lastToolHighlight = itemStack;
        }
        this.chat.tick();
    }

    private void tickAutosaveIndicator() {
        IntegratedServer minecraftServer = this.minecraft.getSingleplayerServer();
        boolean bl = minecraftServer != null && minecraftServer.isCurrentlySaving();
        this.lastAutosaveIndicatorValue = this.autosaveIndicatorValue;
        this.autosaveIndicatorValue = Mth.lerp(0.2f, this.autosaveIndicatorValue, bl ? 1.0f : 0.0f);
    }

    public void setNowPlaying(Component component) {
        MutableComponent component2 = Component.translatable("record.nowPlaying", component);
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

    private void renderSavingIndicator(PoseStack poseStack) {
        int i;
        if (this.minecraft.options.showAutosaveIndicator().get().booleanValue() && (this.autosaveIndicatorValue > 0.0f || this.lastAutosaveIndicatorValue > 0.0f) && (i = Mth.floor(255.0f * Mth.clamp(Mth.lerp(this.minecraft.getFrameTime(), this.lastAutosaveIndicatorValue, this.autosaveIndicatorValue), 0.0f, 1.0f))) > 8) {
            Font font = this.getFont();
            int j = font.width(SAVING_TEXT);
            int k = 0xFFFFFF | i << 24 & 0xFF000000;
            font.drawShadow(poseStack, SAVING_TEXT, (float)(this.screenWidth - j - 10), (float)(this.screenHeight - 15), k);
        }
    }

    @Environment(value=EnvType.CLIENT)
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

        static HeartType forPlayer(Player player) {
            HeartType heartType = player.hasEffect(MobEffects.POISON) ? POISIONED : (player.hasEffect(MobEffects.WITHER) ? WITHERED : (player.isFullyFrozen() ? FROZEN : NORMAL));
            return heartType;
        }
    }
}

