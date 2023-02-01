/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.google.common.util.concurrent.Runnables;
import com.mojang.authlib.minecraft.BanDetails;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen;
import java.io.IOException;
import java.lang.invoke.LambdaMetafactory;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.LanguageSelectScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class TitleScreen
extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DEMO_LEVEL_ID = "Demo_World";
    public static final Component COPYRIGHT_TEXT = Component.literal("Copyright Mojang AB. Do not distribute!");
    public static final CubeMap CUBE_MAP = new CubeMap(new ResourceLocation("textures/gui/title/background/panorama"));
    private static final ResourceLocation PANORAMA_OVERLAY = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
    @Nullable
    private String splash;
    private Button resetDemoButton;
    @Nullable
    private RealmsNotificationsScreen realmsNotificationsScreen;
    private final PanoramaRenderer panorama = new PanoramaRenderer(CUBE_MAP);
    private final boolean fading;
    private long fadeInStart;
    @Nullable
    private WarningLabel warningLabel;
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
        this.logoRenderer = Objects.requireNonNullElseGet(logoRenderer, () -> new LogoRenderer(false));
    }

    private boolean realmsNotificationsEnabled() {
        return this.minecraft.options.realmsNotifications().get() != false && this.realmsNotificationsScreen != null;
    }

    @Override
    public void tick() {
        if (this.realmsNotificationsEnabled()) {
            this.realmsNotificationsScreen.tick();
        }
        this.minecraft.getRealms32BitWarningStatus().showRealms32BitWarningIfNeeded(this);
    }

    public static CompletableFuture<Void> preloadResources(TextureManager textureManager, Executor executor) {
        return CompletableFuture.allOf(textureManager.preload(LogoRenderer.MINECRAFT_LOGO, executor), textureManager.preload(LogoRenderer.MINECRAFT_EDITION, executor), textureManager.preload(PANORAMA_OVERLAY, executor), CUBE_MAP.preload(textureManager, executor));
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
        this.addRenderableWidget(new ImageButton(this.width / 2 - 124, l + 72 + 12, 20, 20, 0, 106, 20, Button.WIDGETS_LOCATION, 256, 256, button -> this.minecraft.setScreen(new LanguageSelectScreen((Screen)this, this.minecraft.options, this.minecraft.getLanguageManager())), Component.translatable("narrator.button.language")));
        this.addRenderableWidget(Button.builder(Component.translatable("menu.options"), button -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options))).bounds(this.width / 2 - 100, l + 72 + 12, 98, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("menu.quit"), button -> this.minecraft.stop()).bounds(this.width / 2 + 2, l + 72 + 12, 98, 20).build());
        this.addRenderableWidget(new ImageButton(this.width / 2 + 104, l + 72 + 12, 20, 20, 0, 0, 20, Button.ACCESSIBILITY_TEXTURE, 32, 64, button -> this.minecraft.setScreen(new AccessibilityOptionsScreen(this, this.minecraft.options)), Component.translatable("narrator.button.accessibility")));
        this.addRenderableWidget(new PlainTextButton(j, this.height - 10, i, 10, COPYRIGHT_TEXT, button -> this.minecraft.setScreen(new WinScreen(false, this.logoRenderer, Runnables.doNothing())), this.font));
        this.minecraft.setConnectedToRealms(false);
        if (this.minecraft.options.realmsNotifications().get().booleanValue() && this.realmsNotificationsScreen == null) {
            this.realmsNotificationsScreen = new RealmsNotificationsScreen();
        }
        if (this.realmsNotificationsEnabled()) {
            this.realmsNotificationsScreen.init(this.minecraft, this.width, this.height);
        }
        if (!this.minecraft.is64Bit()) {
            this.warningLabel = new WarningLabel(this.font, MultiLineLabel.create(this.font, (FormattedText)Component.translatable("title.32bit.deprecation"), 350, 2), this.width / 2, l - 24);
        }
    }

    private void createNormalMenuOptions(int i, int j) {
        this.addRenderableWidget(Button.builder(Component.translatable("menu.singleplayer"), button -> this.minecraft.setScreen(new SelectWorldScreen(this))).bounds(this.width / 2 - 100, i, 200, 20).build());
        Component component = this.getMultiplayerDisabledReason();
        boolean bl = component == null;
        Tooltip tooltip = component != null ? Tooltip.create(component) : null;
        this.addRenderableWidget(Button.builder((Component)Component.translatable((String)"menu.multiplayer"), (Button.OnPress)(Button.OnPress)LambdaMetafactory.metafactory(null, null, null, (Lnet/minecraft/client/gui/components/Button;)V, method_19860(net.minecraft.client.gui.components.Button ), (Lnet/minecraft/client/gui/components/Button;)V)((TitleScreen)this)).bounds((int)(this.width / 2 - 100), (int)(i + j * 1), (int)200, (int)20).tooltip((Tooltip)tooltip).build()).active = bl;
        this.addRenderableWidget(Button.builder((Component)Component.translatable((String)"menu.online"), (Button.OnPress)(Button.OnPress)LambdaMetafactory.metafactory(null, null, null, (Lnet/minecraft/client/gui/components/Button;)V, method_19859(net.minecraft.client.gui.components.Button ), (Lnet/minecraft/client/gui/components/Button;)V)((TitleScreen)this)).bounds((int)(this.width / 2 - 100), (int)(i + j * 2), (int)200, (int)20).tooltip((Tooltip)tooltip).build()).active = bl;
    }

    @Nullable
    private Component getMultiplayerDisabledReason() {
        if (this.minecraft.allowsMultiplayer()) {
            return null;
        }
        BanDetails banDetails = this.minecraft.multiplayerBan();
        if (banDetails != null) {
            if (banDetails.expires() != null) {
                return Component.translatable("title.multiplayer.disabled.banned.temporary");
            }
            return Component.translatable("title.multiplayer.disabled.banned.permanent");
        }
        return Component.translatable("title.multiplayer.disabled");
    }

    private void createDemoMenuOptions(int i, int j) {
        boolean bl = this.checkDemoWorldPresence();
        this.addRenderableWidget(Button.builder(Component.translatable("menu.playdemo"), button -> {
            if (bl) {
                this.minecraft.createWorldOpenFlows().loadLevel(this, DEMO_LEVEL_ID);
            } else {
                this.minecraft.createWorldOpenFlows().createFreshLevel(DEMO_LEVEL_ID, MinecraftServer.DEMO_SETTINGS, WorldOptions.DEMO_OPTIONS, WorldPresets::createNormalWorldDimensions);
            }
        }).bounds(this.width / 2 - 100, i, 200, 20).build());
        this.resetDemoButton = this.addRenderableWidget(Button.builder(Component.translatable("menu.resetdemo"), button -> {
            LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();
            try (LevelStorageSource.LevelStorageAccess levelStorageAccess = levelStorageSource.createAccess(DEMO_LEVEL_ID);){
                LevelSummary levelSummary = levelStorageAccess.getSummary();
                if (levelSummary != null) {
                    this.minecraft.setScreen(new ConfirmScreen(this::confirmDemo, Component.translatable("selectWorld.deleteQuestion"), Component.translatable("selectWorld.deleteWarning", levelSummary.getLevelName()), Component.translatable("selectWorld.deleteButton"), CommonComponents.GUI_CANCEL));
                }
            } catch (IOException iOException) {
                SystemToast.onWorldAccessFailure(this.minecraft, DEMO_LEVEL_ID);
                LOGGER.warn("Failed to access demo world", iOException);
            }
        }).bounds(this.width / 2 - 100, i + j * 1, 200, 20).build());
        this.resetDemoButton.active = bl;
    }

    private boolean checkDemoWorldPresence() {
        boolean bl;
        block8: {
            LevelStorageSource.LevelStorageAccess levelStorageAccess = this.minecraft.getLevelSource().createAccess(DEMO_LEVEL_ID);
            try {
                boolean bl2 = bl = levelStorageAccess.getSummary() != null;
                if (levelStorageAccess == null) break block8;
            } catch (Throwable throwable) {
                try {
                    if (levelStorageAccess != null) {
                        try {
                            levelStorageAccess.close();
                        } catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                } catch (IOException iOException) {
                    SystemToast.onWorldAccessFailure(this.minecraft, DEMO_LEVEL_ID);
                    LOGGER.warn("Failed to read demo world data", iOException);
                    return false;
                }
            }
            levelStorageAccess.close();
        }
        return bl;
    }

    private void realmsButtonClicked() {
        this.minecraft.setScreen(new RealmsMainScreen(this));
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        if (this.fadeInStart == 0L && this.fading) {
            this.fadeInStart = Util.getMillis();
        }
        float g = this.fading ? (float)(Util.getMillis() - this.fadeInStart) / 1000.0f : 1.0f;
        this.panorama.render(f, Mth.clamp(g, 0.0f, 1.0f));
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, PANORAMA_OVERLAY);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, this.fading ? (float)Mth.ceil(Mth.clamp(g, 0.0f, 1.0f)) : 1.0f);
        TitleScreen.blit(poseStack, 0, 0, this.width, this.height, 0.0f, 0.0f, 16, 128, 16, 128);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        float h = this.fading ? Mth.clamp(g - 1.0f, 0.0f, 1.0f) : 1.0f;
        this.logoRenderer.renderLogo(poseStack, this.width, h);
        int k = Mth.ceil(h * 255.0f) << 24;
        if ((k & 0xFC000000) == 0) {
            return;
        }
        if (this.warningLabel != null) {
            this.warningLabel.render(poseStack, k);
        }
        if (this.splash != null) {
            poseStack.pushPose();
            poseStack.translate(this.width / 2 + 90, 70.0f, 0.0f);
            poseStack.mulPose(Axis.ZP.rotationDegrees(-20.0f));
            float l = 1.8f - Mth.abs(Mth.sin((float)(Util.getMillis() % 1000L) / 1000.0f * ((float)Math.PI * 2)) * 0.1f);
            l = l * 100.0f / (float)(this.font.width(this.splash) + 32);
            poseStack.scale(l, l, l);
            TitleScreen.drawCenteredString(poseStack, this.font, this.splash, 0, -8, 0xFFFF00 | k);
            poseStack.popPose();
        }
        String string = "Minecraft " + SharedConstants.getCurrentVersion().getName();
        string = this.minecraft.isDemo() ? string + " Demo" : string + (String)("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType());
        if (Minecraft.checkModStatus().shouldReportAsModified()) {
            string = string + I18n.get("menu.modded", new Object[0]);
        }
        TitleScreen.drawString(poseStack, this.font, string, 2, this.height - 10, 0xFFFFFF | k);
        for (GuiEventListener guiEventListener : this.children()) {
            if (!(guiEventListener instanceof AbstractWidget)) continue;
            ((AbstractWidget)guiEventListener).setAlpha(h);
        }
        super.render(poseStack, i, j, f);
        if (this.realmsNotificationsEnabled() && h >= 1.0f) {
            RenderSystem.enableDepthTest();
            this.realmsNotificationsScreen.render(poseStack, i, j, f);
        }
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if (super.mouseClicked(d, e, i)) {
            return true;
        }
        return this.realmsNotificationsEnabled() && this.realmsNotificationsScreen.mouseClicked(d, e, i);
    }

    @Override
    public void removed() {
        if (this.realmsNotificationsScreen != null) {
            this.realmsNotificationsScreen.removed();
        }
    }

    private void confirmDemo(boolean bl) {
        if (bl) {
            try (LevelStorageSource.LevelStorageAccess levelStorageAccess = this.minecraft.getLevelSource().createAccess(DEMO_LEVEL_ID);){
                levelStorageAccess.deleteLevel();
            } catch (IOException iOException) {
                SystemToast.onWorldDeleteFailure(this.minecraft, DEMO_LEVEL_ID);
                LOGGER.warn("Failed to delete demo world", iOException);
            }
        }
        this.minecraft.setScreen(this);
    }

    private /* synthetic */ void method_19859(Button button) {
        this.realmsButtonClicked();
    }

    private /* synthetic */ void method_19860(Button button) {
        Screen screen = this.minecraft.options.skipMultiplayerWarning ? new JoinMultiplayerScreen(this) : new SafetyScreen(this);
        this.minecraft.setScreen(screen);
    }

    @Environment(value=EnvType.CLIENT)
    record WarningLabel(Font font, MultiLineLabel label, int x, int y) {
        public void render(PoseStack poseStack, int i) {
            this.label.renderBackgroundCentered(poseStack, this.x, this.y, this.font.lineHeight, 2, 0x200000 | Math.min(i, 0x55000000));
            this.label.renderCentered(poseStack, this.x, this.y, this.font.lineHeight, 0xFFFFFF | i);
        }
    }
}

