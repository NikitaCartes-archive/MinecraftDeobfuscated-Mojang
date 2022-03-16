/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.RealmsMainScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(value=EnvType.CLIENT)
public class PauseScreen
extends Screen {
    private static final String URL_FEEDBACK_SNAPSHOT = "https://aka.ms/snapshotfeedback?ref=game";
    private static final String URL_FEEDBACK_RELEASE = "https://aka.ms/javafeedback?ref=game";
    private static final String URL_BUGS = "https://aka.ms/snapshotbugs?ref=game";
    private final boolean showPauseMenu;

    public PauseScreen(boolean bl) {
        super(bl ? new TranslatableComponent("menu.game") : new TranslatableComponent("menu.paused"));
        this.showPauseMenu = bl;
    }

    @Override
    protected void init() {
        if (this.showPauseMenu) {
            this.createPauseMenu();
        }
    }

    private void createPauseMenu() {
        int i = -16;
        int j = 98;
        this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 24 + -16, 204, 20, new TranslatableComponent("menu.returnToGame"), button -> {
            this.minecraft.setScreen(null);
            this.minecraft.mouseHandler.grabMouse();
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 48 + -16, 98, 20, new TranslatableComponent("gui.advancements"), button -> this.minecraft.setScreen(new AdvancementsScreen(this.minecraft.player.connection.getAdvancements()))));
        this.addRenderableWidget(new Button(this.width / 2 + 4, this.height / 4 + 48 + -16, 98, 20, new TranslatableComponent("gui.stats"), button -> this.minecraft.setScreen(new StatsScreen(this, this.minecraft.player.getStats()))));
        String string = SharedConstants.getCurrentVersion().isStable() ? URL_FEEDBACK_RELEASE : URL_FEEDBACK_SNAPSHOT;
        this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 72 + -16, 98, 20, new TranslatableComponent("menu.sendFeedback"), button -> this.minecraft.setScreen(new ConfirmLinkScreen(bl -> {
            if (bl) {
                Util.getPlatform().openUri(string);
            }
            this.minecraft.setScreen(this);
        }, string, true))));
        Button button2 = this.addRenderableWidget(new Button(this.width / 2 + 4, this.height / 4 + 72 + -16, 98, 20, new TranslatableComponent("menu.reportBugs"), button -> this.minecraft.setScreen(new ConfirmLinkScreen(bl -> {
            if (bl) {
                Util.getPlatform().openUri(URL_BUGS);
            }
            this.minecraft.setScreen(this);
        }, URL_BUGS, true))));
        button2.active = !SharedConstants.getCurrentVersion().getDataVersion().isSideSeries();
        this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 96 + -16, 98, 20, new TranslatableComponent("menu.options"), button -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options))));
        Button button22 = this.addRenderableWidget(new Button(this.width / 2 + 4, this.height / 4 + 96 + -16, 98, 20, new TranslatableComponent("menu.shareToLan"), button -> this.minecraft.setScreen(new ShareToLanScreen(this))));
        button22.active = this.minecraft.hasSingleplayerServer() && !this.minecraft.getSingleplayerServer().isPublished();
        TranslatableComponent component = this.minecraft.isLocalServer() ? new TranslatableComponent("menu.returnToMenu") : new TranslatableComponent("menu.disconnect");
        this.addRenderableWidget(new Button(this.width / 2 - 102, this.height / 4 + 120 + -16, 204, 20, component, button -> {
            boolean bl = this.minecraft.isLocalServer();
            boolean bl2 = this.minecraft.isConnectedToRealms();
            button.active = false;
            this.minecraft.level.disconnect();
            if (bl) {
                this.minecraft.clearLevel(new GenericDirtMessageScreen(new TranslatableComponent("menu.savingLevel")));
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
        }));
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void render(PoseStack poseStack, int i, int j, float f) {
        if (this.showPauseMenu) {
            this.renderBackground(poseStack);
            PauseScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 40, 0xFFFFFF);
        } else {
            PauseScreen.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 10, 0xFFFFFF);
        }
        super.render(poseStack, i, j, f);
    }
}

