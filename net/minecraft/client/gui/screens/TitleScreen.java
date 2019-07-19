/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.google.common.util.concurrent.Runnables;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.LanguageSelectScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsBridge;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class TitleScreen
extends Screen {
    public static final CubeMap CUBE_MAP = new CubeMap(new ResourceLocation("textures/gui/title/background/panorama"));
    private static final ResourceLocation PANORAMA_OVERLAY = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
    private static final ResourceLocation ACCESSIBILITY_TEXTURE = new ResourceLocation("textures/gui/accessibility.png");
    private final boolean minceraftEasterEgg;
    @Nullable
    private String splash;
    private Button resetDemoButton;
    @Nullable
    private WarningMessageWidget warningMessage;
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
        super(new TranslatableComponent("narrator.screen.title", new Object[0]));
        this.fading = bl;
        boolean bl2 = this.minceraftEasterEgg = (double)new Random().nextFloat() < 1.0E-4;
        if (!GLX.supportsOpenGL2() && !GLX.isNextGen()) {
            this.warningMessage = new WarningMessageWidget(new TranslatableComponent("title.oldgl.eol.line1", new Object[0]).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD), new TranslatableComponent("title.oldgl.eol.line2", new Object[0]).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD), "https://help.mojang.com/customer/portal/articles/325948?ref=game");
        }
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
        return CompletableFuture.allOf(textureManager.preload(MINECRAFT_LOGO, executor), textureManager.preload(MINECRAFT_EDITION, executor), textureManager.preload(PANORAMA_OVERLAY, executor), CUBE_MAP.preload(textureManager, executor));
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
        this.addButton(new ImageButton(this.width / 2 - 124, j + 72 + 12, 20, 20, 0, 106, 20, Button.WIDGETS_LOCATION, 256, 256, button -> this.minecraft.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager())), I18n.get("narrator.button.language", new Object[0])));
        this.addButton(new Button(this.width / 2 - 100, j + 72 + 12, 98, 20, I18n.get("menu.options", new Object[0]), button -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options))));
        this.addButton(new Button(this.width / 2 + 2, j + 72 + 12, 98, 20, I18n.get("menu.quit", new Object[0]), button -> this.minecraft.stop()));
        this.addButton(new ImageButton(this.width / 2 + 104, j + 72 + 12, 20, 20, 0, 0, 20, ACCESSIBILITY_TEXTURE, 32, 64, button -> this.minecraft.setScreen(new AccessibilityOptionsScreen(this, this.minecraft.options)), I18n.get("narrator.button.accessibility", new Object[0])));
        if (this.warningMessage != null) {
            this.warningMessage.updatePosition(j);
        }
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
        this.addButton(new Button(this.width / 2 - 100, i, 200, 20, I18n.get("menu.singleplayer", new Object[0]), button -> this.minecraft.setScreen(new SelectWorldScreen(this))));
        this.addButton(new Button(this.width / 2 - 100, i + j * 1, 200, 20, I18n.get("menu.multiplayer", new Object[0]), button -> this.minecraft.setScreen(new JoinMultiplayerScreen(this))));
        this.addButton(new Button(this.width / 2 - 100, i + j * 2, 200, 20, I18n.get("menu.online", new Object[0]), button -> this.realmsButtonClicked()));
    }

    private void createDemoMenuOptions(int i, int j) {
        this.addButton(new Button(this.width / 2 - 100, i, 200, 20, I18n.get("menu.playdemo", new Object[0]), button -> this.minecraft.selectLevel("Demo_World", "Demo_World", MinecraftServer.DEMO_SETTINGS)));
        this.resetDemoButton = this.addButton(new Button(this.width / 2 - 100, i + j * 1, 200, 20, I18n.get("menu.resetdemo", new Object[0]), button -> {
            LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();
            LevelData levelData = levelStorageSource.getDataTagFor("Demo_World");
            if (levelData != null) {
                this.minecraft.setScreen(new ConfirmScreen(this::confirmDemo, new TranslatableComponent("selectWorld.deleteQuestion", new Object[0]), new TranslatableComponent("selectWorld.deleteWarning", levelData.getLevelName()), I18n.get("selectWorld.deleteButton", new Object[0]), I18n.get("gui.cancel", new Object[0])));
            }
        }));
        LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();
        LevelData levelData = levelStorageSource.getDataTagFor("Demo_World");
        if (levelData == null) {
            this.resetDemoButton.active = false;
        }
    }

    private void realmsButtonClicked() {
        RealmsBridge realmsBridge = new RealmsBridge();
        realmsBridge.switchToRealms(this);
    }

    @Override
    public void render(int i, int j, float f) {
        if (this.fadeInStart == 0L && this.fading) {
            this.fadeInStart = Util.getMillis();
        }
        float g = this.fading ? (float)(Util.getMillis() - this.fadeInStart) / 1000.0f : 1.0f;
        TitleScreen.fill(0, 0, this.width, this.height, -1);
        this.panorama.render(f, Mth.clamp(g, 0.0f, 1.0f));
        int k = 274;
        int l = this.width / 2 - 137;
        int m = 30;
        this.minecraft.getTextureManager().bind(PANORAMA_OVERLAY);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, this.fading ? (float)Mth.ceil(Mth.clamp(g, 0.0f, 1.0f)) : 1.0f);
        TitleScreen.blit(0, 0, this.width, this.height, 0.0f, 0.0f, 16, 128, 16, 128);
        float h = this.fading ? Mth.clamp(g - 1.0f, 0.0f, 1.0f) : 1.0f;
        int n = Mth.ceil(h * 255.0f) << 24;
        if ((n & 0xFC000000) == 0) {
            return;
        }
        this.minecraft.getTextureManager().bind(MINECRAFT_LOGO);
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, h);
        if (this.minceraftEasterEgg) {
            this.blit(l + 0, 30, 0, 0, 99, 44);
            this.blit(l + 99, 30, 129, 0, 27, 44);
            this.blit(l + 99 + 26, 30, 126, 0, 3, 44);
            this.blit(l + 99 + 26 + 3, 30, 99, 0, 26, 44);
            this.blit(l + 155, 30, 0, 45, 155, 44);
        } else {
            this.blit(l + 0, 30, 0, 0, 155, 44);
            this.blit(l + 155, 30, 0, 45, 155, 44);
        }
        this.minecraft.getTextureManager().bind(MINECRAFT_EDITION);
        TitleScreen.blit(l + 88, 67, 0.0f, 0.0f, 98, 14, 128, 16);
        if (this.splash != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translatef(this.width / 2 + 90, 70.0f, 0.0f);
            GlStateManager.rotatef(-20.0f, 0.0f, 0.0f, 1.0f);
            float o = 1.8f - Mth.abs(Mth.sin((float)(Util.getMillis() % 1000L) / 1000.0f * ((float)Math.PI * 2)) * 0.1f);
            o = o * 100.0f / (float)(this.font.width(this.splash) + 32);
            GlStateManager.scalef(o, o, o);
            this.drawCenteredString(this.font, this.splash, 0, -8, 0xFFFF00 | n);
            GlStateManager.popMatrix();
        }
        String string = "Minecraft " + SharedConstants.getCurrentVersion().getName();
        string = this.minecraft.isDemo() ? string + " Demo" : string + ("release".equalsIgnoreCase(this.minecraft.getVersionType()) ? "" : "/" + this.minecraft.getVersionType());
        this.drawString(this.font, string, 2, this.height - 10, 0xFFFFFF | n);
        this.drawString(this.font, "Copyright Mojang AB. Do not distribute!", this.copyrightX, this.height - 10, 0xFFFFFF | n);
        if (i > this.copyrightX && i < this.copyrightX + this.copyrightWidth && j > this.height - 10 && j < this.height) {
            TitleScreen.fill(this.copyrightX, this.height - 1, this.copyrightX + this.copyrightWidth, this.height, 0xFFFFFF | n);
        }
        if (this.warningMessage != null) {
            this.warningMessage.render(n);
        }
        for (AbstractWidget abstractWidget : this.buttons) {
            abstractWidget.setAlpha(h);
        }
        super.render(i, j, f);
        if (this.realmsNotificationsEnabled() && h >= 1.0f) {
            this.realmsNotificationsScreen.render(i, j, f);
        }
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if (super.mouseClicked(d, e, i)) {
            return true;
        }
        if (this.warningMessage != null && this.warningMessage.mouseClicked(d, e)) {
            return true;
        }
        if (this.realmsNotificationsEnabled() && this.realmsNotificationsScreen.mouseClicked(d, e, i)) {
            return true;
        }
        if (d > (double)this.copyrightX && d < (double)(this.copyrightX + this.copyrightWidth) && e > (double)(this.height - 10) && e < (double)this.height) {
            this.minecraft.setScreen(new WinScreen(false, Runnables.doNothing()));
        }
        return false;
    }

    @Override
    public void removed() {
        if (this.realmsNotificationsScreen != null) {
            this.realmsNotificationsScreen.removed();
        }
    }

    private void confirmDemo(boolean bl) {
        if (bl) {
            LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();
            levelStorageSource.deleteLevel("Demo_World");
        }
        this.minecraft.setScreen(this);
    }

    @Environment(value=EnvType.CLIENT)
    class WarningMessageWidget {
        private int warningClickWidth;
        private int warningx0;
        private int warningy0;
        private int warningx1;
        private int warningy1;
        private final Component warningMessageTop;
        private final Component warningMessageBottom;
        private final String warningMessageUrl;

        public WarningMessageWidget(Component component, Component component2, String string) {
            this.warningMessageTop = component;
            this.warningMessageBottom = component2;
            this.warningMessageUrl = string;
        }

        public void updatePosition(int i) {
            int j = TitleScreen.this.font.width(this.warningMessageTop.getString());
            this.warningClickWidth = TitleScreen.this.font.width(this.warningMessageBottom.getString());
            int k = Math.max(j, this.warningClickWidth);
            this.warningx0 = (TitleScreen.this.width - k) / 2;
            this.warningy0 = i - 24;
            this.warningx1 = this.warningx0 + k;
            this.warningy1 = this.warningy0 + 24;
        }

        public void render(int i) {
            GuiComponent.fill(this.warningx0 - 2, this.warningy0 - 2, this.warningx1 + 2, this.warningy1 - 1, 0x55200000);
            TitleScreen.this.drawString(TitleScreen.this.font, this.warningMessageTop.getColoredString(), this.warningx0, this.warningy0, 0xFFFFFF | i);
            TitleScreen.this.drawString(TitleScreen.this.font, this.warningMessageBottom.getColoredString(), (TitleScreen.this.width - this.warningClickWidth) / 2, this.warningy0 + 12, 0xFFFFFF | i);
        }

        public boolean mouseClicked(double d, double e) {
            if (!StringUtil.isNullOrEmpty(this.warningMessageUrl) && d >= (double)this.warningx0 && d <= (double)this.warningx1 && e >= (double)this.warningy0 && e <= (double)this.warningy1) {
                TitleScreen.this.minecraft.setScreen(new ConfirmLinkScreen(bl -> {
                    if (bl) {
                        Util.getPlatform().openUri(this.warningMessageUrl);
                    }
                    TitleScreen.this.minecraft.setScreen(TitleScreen.this);
                }, this.warningMessageUrl, true));
                return true;
            }
            return false;
        }
    }
}

