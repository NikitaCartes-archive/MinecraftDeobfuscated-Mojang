/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsPlayerScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsInviteScreen
extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private EditBox profileName;
    private final RealmsServer serverData;
    private final RealmsConfigureWorldScreen configureScreen;
    private final Screen lastScreen;
    private String errorMsg;
    private boolean showError;

    public RealmsInviteScreen(RealmsConfigureWorldScreen realmsConfigureWorldScreen, Screen screen, RealmsServer realmsServer) {
        this.configureScreen = realmsConfigureWorldScreen;
        this.lastScreen = screen;
        this.serverData = realmsServer;
    }

    @Override
    public void tick() {
        this.profileName.tick();
    }

    @Override
    public void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.profileName = new EditBox(this.minecraft.font, this.width / 2 - 100, RealmsInviteScreen.row(2), 200, 20, null, I18n.get("mco.configure.world.invite.profile.name", new Object[0]));
        this.addWidget(this.profileName);
        this.setInitialFocus(this.profileName);
        this.addButton(new Button(this.width / 2 - 100, RealmsInviteScreen.row(10), 200, 20, I18n.get("mco.configure.world.buttons.invite", new Object[0]), button -> this.onInvite()));
        this.addButton(new Button(this.width / 2 - 100, RealmsInviteScreen.row(12), 200, 20, I18n.get("gui.cancel", new Object[0]), button -> this.minecraft.setScreen(this.lastScreen)));
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    private void onInvite() {
        RealmsClient realmsClient = RealmsClient.create();
        if (this.profileName.getValue() == null || this.profileName.getValue().isEmpty()) {
            this.showError(I18n.get("mco.configure.world.players.error", new Object[0]));
            return;
        }
        try {
            RealmsServer realmsServer = realmsClient.invite(this.serverData.id, this.profileName.getValue().trim());
            if (realmsServer != null) {
                this.serverData.players = realmsServer.players;
                this.minecraft.setScreen(new RealmsPlayerScreen(this.configureScreen, this.serverData));
            } else {
                this.showError(I18n.get("mco.configure.world.players.error", new Object[0]));
            }
        } catch (Exception exception) {
            LOGGER.error("Couldn't invite user");
            this.showError(I18n.get("mco.configure.world.players.error", new Object[0]));
        }
    }

    private void showError(String string) {
        this.showError = true;
        this.errorMsg = string;
        NarrationHelper.now(string);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == 256) {
            this.minecraft.setScreen(this.lastScreen);
            return true;
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackground();
        this.font.draw(I18n.get("mco.configure.world.invite.profile.name", new Object[0]), this.width / 2 - 100, RealmsInviteScreen.row(1), 0xA0A0A0);
        if (this.showError) {
            this.drawCenteredString(this.font, this.errorMsg, this.width / 2, RealmsInviteScreen.row(5), 0xFF0000);
        }
        this.profileName.render(i, j, f);
        super.render(i, j, f);
    }
}

