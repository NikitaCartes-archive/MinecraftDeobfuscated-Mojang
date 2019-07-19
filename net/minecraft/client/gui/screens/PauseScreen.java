/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.screens;

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
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsBridge;

@Environment(value=EnvType.CLIENT)
public class PauseScreen
extends Screen {
    private final boolean showPauseMenu;

    public PauseScreen(boolean bl) {
        super(bl ? new TranslatableComponent("menu.game", new Object[0]) : new TranslatableComponent("menu.paused", new Object[0]));
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
        this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 24 + -16, 204, 20, I18n.get("menu.returnToGame", new Object[0]), button -> {
            this.minecraft.setScreen(null);
            this.minecraft.mouseHandler.grabMouse();
        }));
        this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 48 + -16, 98, 20, I18n.get("gui.advancements", new Object[0]), button -> this.minecraft.setScreen(new AdvancementsScreen(this.minecraft.player.connection.getAdvancements()))));
        this.addButton(new Button(this.width / 2 + 4, this.height / 4 + 48 + -16, 98, 20, I18n.get("gui.stats", new Object[0]), button -> this.minecraft.setScreen(new StatsScreen(this, this.minecraft.player.getStats()))));
        String string = SharedConstants.getCurrentVersion().isStable() ? "https://aka.ms/javafeedback?ref=game" : "https://aka.ms/snapshotfeedback?ref=game";
        this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 72 + -16, 98, 20, I18n.get("menu.sendFeedback", new Object[0]), button -> this.minecraft.setScreen(new ConfirmLinkScreen(bl -> {
            if (bl) {
                Util.getPlatform().openUri(string);
            }
            this.minecraft.setScreen(this);
        }, string, true))));
        this.addButton(new Button(this.width / 2 + 4, this.height / 4 + 72 + -16, 98, 20, I18n.get("menu.reportBugs", new Object[0]), button -> this.minecraft.setScreen(new ConfirmLinkScreen(bl -> {
            if (bl) {
                Util.getPlatform().openUri("https://aka.ms/snapshotbugs?ref=game");
            }
            this.minecraft.setScreen(this);
        }, "https://aka.ms/snapshotbugs?ref=game", true))));
        this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 96 + -16, 98, 20, I18n.get("menu.options", new Object[0]), button -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options))));
        Button button2 = this.addButton(new Button(this.width / 2 + 4, this.height / 4 + 96 + -16, 98, 20, I18n.get("menu.shareToLan", new Object[0]), button -> this.minecraft.setScreen(new ShareToLanScreen(this))));
        button2.active = this.minecraft.hasSingleplayerServer() && !this.minecraft.getSingleplayerServer().isPublished();
        Button button22 = this.addButton(new Button(this.width / 2 - 102, this.height / 4 + 120 + -16, 204, 20, I18n.get("menu.returnToMenu", new Object[0]), button -> {
            boolean bl = this.minecraft.isLocalServer();
            boolean bl2 = this.minecraft.isConnectedToRealms();
            button.active = false;
            this.minecraft.level.disconnect();
            if (bl) {
                this.minecraft.clearLevel(new GenericDirtMessageScreen(new TranslatableComponent("menu.savingLevel", new Object[0])));
            } else {
                this.minecraft.clearLevel();
            }
            if (bl) {
                this.minecraft.setScreen(new TitleScreen());
            } else if (bl2) {
                RealmsBridge realmsBridge = new RealmsBridge();
                realmsBridge.switchToRealms(new TitleScreen());
            } else {
                this.minecraft.setScreen(new JoinMultiplayerScreen(new TitleScreen()));
            }
        }));
        if (!this.minecraft.isLocalServer()) {
            button22.setMessage(I18n.get("menu.disconnect", new Object[0]));
        }
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void render(int i, int j, float f) {
        if (this.showPauseMenu) {
            this.renderBackground();
            this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 40, 0xFFFFFF);
        } else {
            this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 10, 0xFFFFFF);
        }
        super.render(i, j, f);
    }
}

