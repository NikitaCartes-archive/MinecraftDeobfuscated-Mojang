/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.RealmsMainScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CenteredStringWidget;
import net.minecraft.client.gui.components.FrameWidget;
import net.minecraft.client.gui.components.GridWidget;
import net.minecraft.client.gui.components.LayoutSettings;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@Environment(value=EnvType.CLIENT)
public class PauseScreen
extends Screen {
    private static final String URL_FEEDBACK_SNAPSHOT = "https://aka.ms/snapshotfeedback?ref=game";
    private static final String URL_FEEDBACK_RELEASE = "https://aka.ms/javafeedback?ref=game";
    private static final String URL_BUGS = "https://aka.ms/snapshotbugs?ref=game";
    private final boolean showPauseMenu;
    private Button disconnectButton;

    public PauseScreen(boolean bl) {
        super(bl ? Component.translatable("menu.game") : Component.translatable("menu.paused"));
        this.showPauseMenu = bl;
    }

    @Override
    protected void init() {
        if (this.showPauseMenu) {
            this.createPauseMenu();
        } else {
            FrameWidget frameWidget = this.addRenderableWidget(FrameWidget.withMinDimensions(this.width, this.height));
            frameWidget.defaultChildLayoutSetting().alignHorizontallyCenter().alignVerticallyTop().paddingTop(10);
            frameWidget.addChild(new CenteredStringWidget(this.title, this.font));
            frameWidget.pack();
        }
    }

    private void createPauseMenu() {
        int i = 204;
        int j = 98;
        GridWidget gridWidget = new GridWidget();
        gridWidget.defaultCellSetting().padding(0, 2).alignHorizontallyCenter();
        LayoutSettings layoutSettings = gridWidget.newCellSettings().alignHorizontallyLeft();
        LayoutSettings layoutSettings2 = gridWidget.newCellSettings().alignHorizontallyRight();
        int k = 0;
        gridWidget.addChild(new CenteredStringWidget(this.title, this.minecraft.font), k, 0, 1, 2, gridWidget.newCellSettings().paddingBottom(5));
        gridWidget.addChild(Button.builder(Component.translatable("menu.returnToGame"), button -> {
            this.minecraft.setScreen(null);
            this.minecraft.mouseHandler.grabMouse();
        }).width(204).build(), ++k, 0, 1, 2);
        gridWidget.addChild(Button.builder(Component.translatable("gui.advancements"), button -> this.minecraft.setScreen(new AdvancementsScreen(this.minecraft.player.connection.getAdvancements()))).width(98).build(), ++k, 0, layoutSettings);
        gridWidget.addChild(Button.builder(Component.translatable("gui.stats"), button -> this.minecraft.setScreen(new StatsScreen(this, this.minecraft.player.getStats()))).width(98).build(), k, 1, layoutSettings2);
        String string = SharedConstants.getCurrentVersion().isStable() ? URL_FEEDBACK_RELEASE : URL_FEEDBACK_SNAPSHOT;
        gridWidget.addChild(Button.builder(Component.translatable("menu.sendFeedback"), button -> this.minecraft.setScreen(new ConfirmLinkScreen(bl -> {
            if (bl) {
                Util.getPlatform().openUri(string);
            }
            this.minecraft.setScreen(this);
        }, string, true))).width(98).build(), ++k, 0, layoutSettings);
        Button button2 = gridWidget.addChild(Button.builder(Component.translatable("menu.reportBugs"), button -> this.minecraft.setScreen(new ConfirmLinkScreen(bl -> {
            if (bl) {
                Util.getPlatform().openUri(URL_BUGS);
            }
            this.minecraft.setScreen(this);
        }, URL_BUGS, true))).width(98).build(), k, 1, layoutSettings2);
        button2.active = !SharedConstants.getCurrentVersion().getDataVersion().isSideSeries();
        gridWidget.addChild(Button.builder(Component.translatable("menu.options"), button -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options))).width(98).build(), ++k, 0, layoutSettings);
        if (this.minecraft.hasSingleplayerServer() && !this.minecraft.getSingleplayerServer().isPublished()) {
            gridWidget.addChild(Button.builder(Component.translatable("menu.shareToLan"), button -> this.minecraft.setScreen(new ShareToLanScreen(this))).width(98).build(), k, 1, layoutSettings2);
        } else {
            gridWidget.addChild(Button.builder(Component.translatable("menu.playerReporting"), button -> this.minecraft.setScreen(new SocialInteractionsScreen())).width(98).build(), k, 1, layoutSettings2);
        }
        MutableComponent component = this.minecraft.isLocalServer() ? Component.translatable("menu.returnToMenu") : Component.translatable("menu.disconnect");
        this.disconnectButton = gridWidget.addChild(Button.builder(component, button -> {
            if (this.minecraft.getReportingContext().draftReportHandled(this.minecraft, this, true)) {
                boolean bl = this.minecraft.isLocalServer();
                boolean bl2 = this.minecraft.isConnectedToRealms();
                button.active = false;
                this.minecraft.level.disconnect();
                if (bl) {
                    this.minecraft.clearLevel(new GenericDirtMessageScreen(Component.translatable("menu.savingLevel")));
                } else {
                    this.minecraft.clearLevel();
                }
                TitleScreen titleScreen = new TitleScreen();
                if (bl) {
                    this.minecraft.setScreen(titleScreen);
                } else if (bl2) {
                    this.minecraft.setScreen(new RealmsMainScreen(titleScreen));
                } else {
                    this.minecraft.setScreen(new JoinMultiplayerScreen(titleScreen));
                }
            }
        }).width(204).build(), ++k, 0, 1, 2);
        gridWidget.pack();
        FrameWidget.centerInRectangle(gridWidget, 0, 0, this.width, this.height);
        this.addRenderableWidget(gridWidget);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        if (this.showPauseMenu) {
            this.renderBackground(poseStack);
        }
        super.render(poseStack, i, j, f);
        if (this.showPauseMenu && this.minecraft != null && this.minecraft.getReportingContext().hasDraftReport()) {
            RenderSystem.setShaderTexture(0, AbstractWidget.WIDGETS_LOCATION);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            this.blit(poseStack, this.disconnectButton.getX() + this.disconnectButton.getWidth() - 17, this.disconnectButton.getY() + 3, 182, 24, 15, 15);
        }
    }
}

