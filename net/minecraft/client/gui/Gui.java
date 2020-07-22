/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
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
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.FastColor;
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
    private static final Component DEMO_EXPIRED_TEXT = new TranslatableComponent("demo.demoExpired");
    private final Random random = new Random();
    private final Minecraft minecraft;
    private final ItemRenderer itemRenderer;
    private final ChatComponent chat;
    private int tickCount;
    @Nullable
    private Component overlayMessageString;
    private int overlayMessageTime;
    private boolean animateOverlayMessageColor;
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
    private final Map<ChatType, List<ChatListener>> chatListeners = Maps.newHashMap();

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
        NarratorChatListener chatListener = NarratorChatListener.INSTANCE;
        this.chatListeners.get((Object)ChatType.CHAT).add(new StandardChatListener(minecraft));
        this.chatListeners.get((Object)ChatType.CHAT).add(chatListener);
        this.chatListeners.get((Object)ChatType.SYSTEM).add(new StandardChatListener(minecraft));
        this.chatListeners.get((Object)ChatType.SYSTEM).add(chatListener);
        this.chatListeners.get((Object)ChatType.GAME_INFO).add(new OverlayChatListener(minecraft));
        this.resetTitleTimes();
    }

    public void resetTitleTimes() {
        this.titleFadeInTime = 10;
        this.titleStayTime = 70;
        this.titleFadeOutTime = 20;
    }

    public void render(PoseStack poseStack, float f) {
        int j;
        float g;
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
        if (!this.minecraft.player.hasEffect(MobEffects.CONFUSION) && (g = Mth.lerp(f, this.minecraft.player.oPortalTime, this.minecraft.player.portalTime)) > 0.0f) {
            this.renderPortalOverlay(g);
        }
        if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
            this.spectatorGui.renderHotbar(poseStack, f);
        } else if (!this.minecraft.options.hideGui) {
            this.renderHotbar(f, poseStack);
        }
        if (!this.minecraft.options.hideGui) {
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            this.minecraft.getTextureManager().bind(GUI_ICONS_LOCATION);
            RenderSystem.enableBlend();
            RenderSystem.enableAlphaTest();
            this.renderCrosshair(poseStack);
            RenderSystem.defaultBlendFunc();
            this.minecraft.getProfiler().push("bossHealth");
            this.bossOverlay.render(poseStack);
            this.minecraft.getProfiler().pop();
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
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
            g = this.minecraft.player.getSleepTimer();
            float h = g / 100.0f;
            if (h > 1.0f) {
                h = 1.0f - (g - 100.0f) / 10.0f;
            }
            j = (int)(220.0f * h) << 24 | 0x101020;
            Gui.fill(poseStack, 0, 0, this.screenWidth, this.screenHeight, j);
            RenderSystem.enableAlphaTest();
            RenderSystem.enableDepthTest();
            this.minecraft.getProfiler().pop();
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
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
            int m;
            int l;
            if (this.overlayMessageString != null && this.overlayMessageTime > 0) {
                this.minecraft.getProfiler().push("overlayMessage");
                g = (float)this.overlayMessageTime - f;
                int k = (int)(g * 255.0f / 20.0f);
                if (k > 255) {
                    k = 255;
                }
                if (k > 8) {
                    RenderSystem.pushMatrix();
                    RenderSystem.translatef(this.screenWidth / 2, this.screenHeight - 68, 0.0f);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    j = 0xFFFFFF;
                    if (this.animateOverlayMessageColor) {
                        j = Mth.hsvToRgb(g / 50.0f, 0.7f, 0.6f) & 0xFFFFFF;
                    }
                    l = k << 24 & 0xFF000000;
                    m = font.width(this.overlayMessageString);
                    this.drawBackdrop(poseStack, font, -4, m, 0xFFFFFF | l);
                    font.draw(poseStack, this.overlayMessageString, (float)(-m / 2), -4.0f, j | l);
                    RenderSystem.disableBlend();
                    RenderSystem.popMatrix();
                }
                this.minecraft.getProfiler().pop();
            }
            if (this.title != null && this.titleTime > 0) {
                this.minecraft.getProfiler().push("titleAndSubtitle");
                g = (float)this.titleTime - f;
                int k = 255;
                if (this.titleTime > this.titleFadeOutTime + this.titleStayTime) {
                    float n = (float)(this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime) - g;
                    k = (int)(n * 255.0f / (float)this.titleFadeInTime);
                }
                if (this.titleTime <= this.titleFadeOutTime) {
                    k = (int)(g * 255.0f / (float)this.titleFadeOutTime);
                }
                if ((k = Mth.clamp(k, 0, 255)) > 8) {
                    RenderSystem.pushMatrix();
                    RenderSystem.translatef(this.screenWidth / 2, this.screenHeight / 2, 0.0f);
                    RenderSystem.enableBlend();
                    RenderSystem.defaultBlendFunc();
                    RenderSystem.pushMatrix();
                    RenderSystem.scalef(4.0f, 4.0f, 4.0f);
                    int j2 = k << 24 & 0xFF000000;
                    l = font.width(this.title);
                    this.drawBackdrop(poseStack, font, -10, l, 0xFFFFFF | j2);
                    font.drawShadow(poseStack, this.title, (float)(-l / 2), -10.0f, 0xFFFFFF | j2);
                    RenderSystem.popMatrix();
                    if (this.subtitle != null) {
                        RenderSystem.pushMatrix();
                        RenderSystem.scalef(2.0f, 2.0f, 2.0f);
                        m = font.width(this.subtitle);
                        this.drawBackdrop(poseStack, font, 5, m, 0xFFFFFF | j2);
                        font.drawShadow(poseStack, this.subtitle, (float)(-m / 2), 5.0f, 0xFFFFFF | j2);
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
            if (playerTeam != null && (l = playerTeam.getColor().getId()) >= 0) {
                objective = scoreboard.getDisplayObjective(3 + l);
            }
            Objective objective3 = objective2 = objective != null ? objective : scoreboard.getDisplayObjective(1);
            if (objective2 != null) {
                this.displayScoreboardSidebar(poseStack, objective2);
            }
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableAlphaTest();
            RenderSystem.pushMatrix();
            RenderSystem.translatef(0.0f, this.screenHeight - 48, 0.0f);
            this.minecraft.getProfiler().push("chat");
            this.chat.render(poseStack, this.tickCount);
            this.minecraft.getProfiler().pop();
            RenderSystem.popMatrix();
            objective2 = scoreboard.getDisplayObjective(0);
            if (this.minecraft.options.keyPlayerList.isDown() && (!this.minecraft.isLocalServer() || this.minecraft.player.connection.getOnlinePlayers().size() > 1 || objective2 != null)) {
                this.tabList.setVisible(true);
                this.tabList.render(poseStack, this.screenWidth, scoreboard, objective2);
            } else {
                this.tabList.setVisible(false);
            }
        }
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableAlphaTest();
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
        if (options.thirdPersonView != 0) {
            return;
        }
        if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR && !this.canRenderCrosshairForSpectator(this.minecraft.hitResult)) {
            return;
        }
        if (options.renderDebug && !options.hideGui && !this.minecraft.player.isReducedDebugInfo() && !options.reducedDebugInfo) {
            RenderSystem.pushMatrix();
            RenderSystem.translatef(this.screenWidth / 2, this.screenHeight / 2, this.getBlitOffset());
            Camera camera = this.minecraft.gameRenderer.getMainCamera();
            RenderSystem.rotatef(camera.getXRot(), -1.0f, 0.0f, 0.0f);
            RenderSystem.rotatef(camera.getYRot(), 0.0f, 1.0f, 0.0f);
            RenderSystem.scalef(-1.0f, -1.0f, -1.0f);
            RenderSystem.renderCrosshair(10);
            RenderSystem.popMatrix();
        } else {
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            int i = 15;
            this.blit(poseStack, (this.screenWidth - 15) / 2, (this.screenHeight - 15) / 2, 0, 0, 15, 15);
            if (this.minecraft.options.attackIndicator == AttackIndicatorStatus.CROSSHAIR) {
                float f = this.minecraft.player.getAttackStrengthScale(0.0f);
                boolean bl = false;
                if (this.minecraft.crosshairPickEntity != null && this.minecraft.crosshairPickEntity instanceof LivingEntity && f >= 1.0f) {
                    bl = this.minecraft.player.getCurrentItemAttackStrengthDelay() > 5.0f;
                    bl &= this.minecraft.crosshairPickEntity.isAlive();
                }
                int j = this.screenHeight / 2 - 7 + 16;
                int k = this.screenWidth / 2 - 8;
                if (bl) {
                    this.blit(poseStack, k, j, 68, 94, 16, 16);
                } else if (f < 1.0f) {
                    int l = (int)(f * 17.0f);
                    this.blit(poseStack, k, j, 36, 94, 16, 4);
                    this.blit(poseStack, k, j, 52, 94, l, 4);
                }
            }
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
        Collection<MobEffectInstance> collection = this.minecraft.player.getActiveEffects();
        if (collection.isEmpty()) {
            return;
        }
        RenderSystem.enableBlend();
        int i = 0;
        int j = 0;
        MobEffectTextureManager mobEffectTextureManager = this.minecraft.getMobEffectTextures();
        ArrayList<Runnable> list = Lists.newArrayListWithExpectedSize(collection.size());
        this.minecraft.getTextureManager().bind(AbstractContainerScreen.INVENTORY_LOCATION);
        for (MobEffectInstance mobEffectInstance : Ordering.natural().reverse().sortedCopy(collection)) {
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
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            float f = 1.0f;
            if (mobEffectInstance.isAmbient()) {
                this.blit(poseStack, k, l, 165, 166, 24, 24);
            } else {
                this.blit(poseStack, k, l, 141, 166, 24, 24);
                if (mobEffectInstance.getDuration() <= 200) {
                    int m = 10 - mobEffectInstance.getDuration() / 20;
                    f = Mth.clamp((float)mobEffectInstance.getDuration() / 10.0f / 5.0f * 0.5f, 0.0f, 0.5f) + Mth.cos((float)mobEffectInstance.getDuration() * (float)Math.PI / 5.0f) * Mth.clamp((float)m / 10.0f * 0.25f, 0.0f, 0.25f);
                }
            }
            TextureAtlasSprite textureAtlasSprite = mobEffectTextureManager.get(mobEffect);
            int n = k;
            int o = l;
            float g = f;
            list.add(() -> {
                this.minecraft.getTextureManager().bind(textureAtlasSprite.atlas().location());
                RenderSystem.color4f(1.0f, 1.0f, 1.0f, g);
                Gui.blit(poseStack, n + 3, o + 3, this.getBlitOffset(), 18, 18, textureAtlasSprite);
            });
        }
        list.forEach(Runnable::run);
    }

    protected void renderHotbar(float f, PoseStack poseStack) {
        float g;
        int o;
        int n;
        int m;
        Player player = this.getCameraPlayer();
        if (player == null) {
            return;
        }
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
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
        for (m = 0; m < 9; ++m) {
            n = i - 90 + m * 20 + 2;
            o = this.screenHeight - 16 - 3;
            this.renderSlot(n, o, f, player, player.inventory.items.get(m));
        }
        if (!itemStack.isEmpty()) {
            m = this.screenHeight - 16 - 3;
            if (humanoidArm == HumanoidArm.LEFT) {
                this.renderSlot(i - 91 - 26, m, f, player, itemStack);
            } else {
                this.renderSlot(i + 91 + 10, m, f, player, itemStack);
            }
        }
        if (this.minecraft.options.attackIndicator == AttackIndicatorStatus.HOTBAR && (g = this.minecraft.player.getAttackStrengthScale(0.0f)) < 1.0f) {
            n = this.screenHeight - 20;
            o = i + 91 + 6;
            if (humanoidArm == HumanoidArm.RIGHT) {
                o = i - 91 - 22;
            }
            this.minecraft.getTextureManager().bind(GuiComponent.GUI_ICONS_LOCATION);
            int p = (int)(g * 19.0f);
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            this.blit(poseStack, o, n, 0, 94, 18, 18);
            this.blit(poseStack, o, n + 18 - p, 18, 112 - p, 18, p);
        }
        RenderSystem.disableRescaleNormal();
        RenderSystem.disableBlend();
    }

    public void renderJumpMeter(PoseStack poseStack, int i) {
        this.minecraft.getProfiler().push("jumpBar");
        this.minecraft.getTextureManager().bind(GuiComponent.GUI_ICONS_LOCATION);
        float f = this.minecraft.player.getJumpRidingScale();
        int j = 182;
        int k = (int)(f * 183.0f);
        int l = this.screenHeight - 32 + 3;
        this.blit(poseStack, i, l, 0, 84, 182, 5);
        if (k > 0) {
            this.blit(poseStack, i, l, 0, 89, k, 5);
        }
        this.minecraft.getProfiler().pop();
    }

    public void renderExperienceBar(PoseStack poseStack, int i) {
        int m;
        int l;
        this.minecraft.getProfiler().push("expBar");
        this.minecraft.getTextureManager().bind(GuiComponent.GUI_ICONS_LOCATION);
        int j = this.minecraft.player.getXpNeededForNextLevel();
        if (j > 0) {
            int k = 182;
            l = (int)(this.minecraft.player.experienceProgress * 183.0f);
            m = this.screenHeight - 32 + 3;
            this.blit(poseStack, i, m, 0, 64, 182, 5);
            if (l > 0) {
                this.blit(poseStack, i, m, 0, 69, l, 5);
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
            if ((l = (int)((float)this.toolHighlightTimer * 256.0f / 10.0f)) > 255) {
                l = 255;
            }
            if (l > 0) {
                RenderSystem.pushMatrix();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                Gui.fill(poseStack, j - 2, k - 2, j + i + 2, k + this.getFont().lineHeight + 2, this.minecraft.options.getBackgroundColor(0));
                this.getFont().drawShadow(poseStack, mutableComponent, (float)j, (float)k, 0xFFFFFF + (l << 24));
                RenderSystem.disableBlend();
                RenderSystem.popMatrix();
            }
        }
        this.minecraft.getProfiler().pop();
    }

    public void renderDemoOverlay(PoseStack poseStack) {
        this.minecraft.getProfiler().push("demo");
        Component component = this.minecraft.level.getGameTime() >= 120500L ? DEMO_EXPIRED_TEXT : new TranslatableComponent("demo.remainingTime", StringUtil.formatTickDuration((int)(120500L - this.minecraft.level.getGameTime())));
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
        int k = this.getFont().width(": ");
        for (Score score2 : collection) {
            PlayerTeam playerTeam = scoreboard.getPlayersTeam(score2.getOwner());
            MutableComponent component2 = PlayerTeam.formatNameForTeam(playerTeam, new TextComponent(score2.getOwner()));
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
            String string = (Object)((Object)ChatFormatting.RED) + "" + score2.getScore();
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
        int ad;
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
        float f = (float)player.getAttributeValue(Attributes.MAX_HEALTH);
        int p = Mth.ceil(player.getAbsorptionAmount());
        int q = Mth.ceil((f + (float)p) / 2.0f / 10.0f);
        int r = Math.max(10 - (q - 2), 3);
        int s = o - (q - 1) * r - 10;
        int t = o - 10;
        int u = p;
        int v = player.getArmorValue();
        int w = -1;
        if (player.hasEffect(MobEffects.REGENERATION)) {
            w = this.tickCount % Mth.ceil(f + 5.0f);
        }
        this.minecraft.getProfiler().push("armor");
        for (x = 0; x < 10; ++x) {
            if (v <= 0) continue;
            y = m + x * 8;
            if (x * 2 + 1 < v) {
                this.blit(poseStack, y, s, 34, 9, 9, 9);
            }
            if (x * 2 + 1 == v) {
                this.blit(poseStack, y, s, 25, 9, 9, 9);
            }
            if (x * 2 + 1 <= v) continue;
            this.blit(poseStack, y, s, 16, 9, 9, 9);
        }
        this.minecraft.getProfiler().popPush("health");
        for (x = Mth.ceil((f + (float)p) / 2.0f) - 1; x >= 0; --x) {
            y = 16;
            if (player.hasEffect(MobEffects.POISON)) {
                y += 36;
            } else if (player.hasEffect(MobEffects.WITHER)) {
                y += 72;
            }
            z = 0;
            if (bl) {
                z = 1;
            }
            aa = Mth.ceil((float)(x + 1) / 10.0f) - 1;
            ab = m + x % 10 * 8;
            ac = o - aa * r;
            if (i <= 4) {
                ac += this.random.nextInt(2);
            }
            if (u <= 0 && x == w) {
                ac -= 2;
            }
            ad = 0;
            if (player.level.getLevelData().isHardcore()) {
                ad = 5;
            }
            this.blit(poseStack, ab, ac, 16 + z * 9, 9 * ad, 9, 9);
            if (bl) {
                if (x * 2 + 1 < j) {
                    this.blit(poseStack, ab, ac, y + 54, 9 * ad, 9, 9);
                }
                if (x * 2 + 1 == j) {
                    this.blit(poseStack, ab, ac, y + 63, 9 * ad, 9, 9);
                }
            }
            if (u > 0) {
                if (u == p && p % 2 == 1) {
                    this.blit(poseStack, ab, ac, y + 153, 9 * ad, 9, 9);
                    --u;
                    continue;
                }
                this.blit(poseStack, ab, ac, y + 144, 9 * ad, 9, 9);
                u -= 2;
                continue;
            }
            if (x * 2 + 1 < i) {
                this.blit(poseStack, ab, ac, y + 36, 9 * ad, 9, 9);
            }
            if (x * 2 + 1 != i) continue;
            this.blit(poseStack, ab, ac, y + 45, 9 * ad, 9, 9);
        }
        LivingEntity livingEntity = this.getPlayerVehicleWithHealth();
        y = this.getVehicleMaxHearts(livingEntity);
        if (y == 0) {
            this.minecraft.getProfiler().popPush("food");
            for (z = 0; z < 10; ++z) {
                aa = o;
                ab = 16;
                ac = 0;
                if (player.hasEffect(MobEffects.HUNGER)) {
                    ab += 36;
                    ac = 13;
                }
                if (player.getFoodData().getSaturationLevel() <= 0.0f && this.tickCount % (k * 3 + 1) == 0) {
                    aa += this.random.nextInt(3) - 1;
                }
                ad = n - z * 8 - 9;
                this.blit(poseStack, ad, aa, 16 + ac * 9, 27, 9, 9);
                if (z * 2 + 1 < k) {
                    this.blit(poseStack, ad, aa, ab + 36, 27, 9, 9);
                }
                if (z * 2 + 1 != k) continue;
                this.blit(poseStack, ad, aa, ab + 45, 27, 9, 9);
            }
            t -= 10;
        }
        this.minecraft.getProfiler().popPush("air");
        z = player.getAirSupply();
        aa = player.getMaxAirSupply();
        if (player.isEyeInFluid(FluidTags.WATER) || z < aa) {
            ab = this.getVisibleVehicleHeartRows(y) - 1;
            t -= ab * 10;
            ac = Mth.ceil((double)(z - 2) * 10.0 / (double)aa);
            ad = Mth.ceil((double)z * 10.0 / (double)aa) - ac;
            for (int ae = 0; ae < ac + ad; ++ae) {
                if (ae < ac) {
                    this.blit(poseStack, n - ae * 8 - 9, t, 16, 18, 9, 9);
                    continue;
                }
                this.blit(poseStack, n - ae * 8 - 9, t, 25, 18, 9, 9);
            }
        }
        this.minecraft.getProfiler().pop();
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
                this.blit(poseStack, s, m, 52 + r * 9, 9, 9, 9);
                if (p * 2 + 1 + n < j) {
                    this.blit(poseStack, s, m, 88, 9, 9, 9);
                }
                if (p * 2 + 1 + n != j) continue;
                this.blit(poseStack, s, m, 97, 9, 9, 9);
            }
            m -= 10;
            n += 20;
        }
    }

    private void renderPumpkin() {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableAlphaTest();
        this.minecraft.getTextureManager().bind(PUMPKIN_BLUR_LOCATION);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(0.0, this.screenHeight, -90.0).uv(0.0f, 1.0f).endVertex();
        bufferBuilder.vertex(this.screenWidth, this.screenHeight, -90.0).uv(1.0f, 1.0f).endVertex();
        bufferBuilder.vertex(this.screenWidth, 0.0, -90.0).uv(1.0f, 0.0f).endVertex();
        bufferBuilder.vertex(0.0, 0.0, -90.0).uv(0.0f, 0.0f).endVertex();
        tesselator.end();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableAlphaTest();
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void updateVignetteBrightness(Entity entity) {
        if (entity == null) {
            return;
        }
        float f = Mth.clamp(1.0f - entity.getBrightness(), 0.0f, 1.0f);
        this.vignetteBrightness = (float)((double)this.vignetteBrightness + (double)(f - this.vignetteBrightness) * 0.01);
    }

    private void renderVignette(Entity entity) {
        WorldBorder worldBorder = this.minecraft.level.getWorldBorder();
        float f = (float)worldBorder.getDistanceToBorder(entity);
        double d = Math.min(worldBorder.getLerpSpeed() * (double)worldBorder.getWarningTime() * 1000.0, Math.abs(worldBorder.getLerpTarget() - worldBorder.getSize()));
        double e = Math.max((double)worldBorder.getWarningBlocks(), d);
        f = (double)f < e ? 1.0f - (float)((double)f / e) : 0.0f;
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        if (f > 0.0f) {
            RenderSystem.color4f(0.0f, f, f, 1.0f);
        } else {
            RenderSystem.color4f(this.vignetteBrightness, this.vignetteBrightness, this.vignetteBrightness, 1.0f);
        }
        this.minecraft.getTextureManager().bind(VIGNETTE_LOCATION);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(0.0, this.screenHeight, -90.0).uv(0.0f, 1.0f).endVertex();
        bufferBuilder.vertex(this.screenWidth, this.screenHeight, -90.0).uv(1.0f, 1.0f).endVertex();
        bufferBuilder.vertex(this.screenWidth, 0.0, -90.0).uv(1.0f, 0.0f).endVertex();
        bufferBuilder.vertex(0.0, 0.0, -90.0).uv(0.0f, 0.0f).endVertex();
        tesselator.end();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.defaultBlendFunc();
    }

    private void renderPortalOverlay(float f) {
        if (f < 1.0f) {
            f *= f;
            f *= f;
            f = f * 0.8f + 0.2f;
        }
        RenderSystem.disableAlphaTest();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, f);
        this.minecraft.getTextureManager().bind(TextureAtlas.LOCATION_BLOCKS);
        TextureAtlasSprite textureAtlasSprite = this.minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.NETHER_PORTAL.defaultBlockState());
        float g = textureAtlasSprite.getU0();
        float h = textureAtlasSprite.getV0();
        float i = textureAtlasSprite.getU1();
        float j = textureAtlasSprite.getV1();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(0.0, this.screenHeight, -90.0).uv(g, j).endVertex();
        bufferBuilder.vertex(this.screenWidth, this.screenHeight, -90.0).uv(i, j).endVertex();
        bufferBuilder.vertex(this.screenWidth, 0.0, -90.0).uv(i, h).endVertex();
        bufferBuilder.vertex(0.0, 0.0, -90.0).uv(g, h).endVertex();
        tesselator.end();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableAlphaTest();
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private void renderSlot(int i, int j, float f, Player player, ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return;
        }
        float g = (float)itemStack.getPopTime() - f;
        if (g > 0.0f) {
            RenderSystem.pushMatrix();
            float h = 1.0f + g / 5.0f;
            RenderSystem.translatef(i + 8, j + 12, 0.0f);
            RenderSystem.scalef(1.0f / h, (h + 1.0f) / 2.0f, 1.0f);
            RenderSystem.translatef(-(i + 8), -(j + 12), 0.0f);
        }
        this.itemRenderer.renderAndDecorateItem(player, itemStack, i, j);
        if (g > 0.0f) {
            RenderSystem.popMatrix();
        }
        this.itemRenderer.renderGuiItemDecorations(this.minecraft.font, itemStack, i, j);
    }

    public void tick() {
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
            ItemStack itemStack = this.minecraft.player.inventory.getSelected();
            if (itemStack.isEmpty()) {
                this.toolHighlightTimer = 0;
            } else if (this.lastToolHighlight.isEmpty() || itemStack.getItem() != this.lastToolHighlight.getItem() || !itemStack.getHoverName().equals(this.lastToolHighlight.getHoverName())) {
                this.toolHighlightTimer = 40;
            } else if (this.toolHighlightTimer > 0) {
                --this.toolHighlightTimer;
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
            return;
        }
        if (component != null) {
            this.title = component;
            this.titleTime = this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime;
            return;
        }
        if (component2 != null) {
            this.subtitle = component2;
            return;
        }
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

    public void handleChat(ChatType chatType, Component component, UUID uUID) {
        for (ChatListener chatListener : this.chatListeners.get((Object)chatType)) {
            chatListener.handle(chatType, component, uUID);
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

