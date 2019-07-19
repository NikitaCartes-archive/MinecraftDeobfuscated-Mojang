/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsPlayerScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsEditBox;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsInviteScreen
extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private RealmsEditBox profileName;
    private final RealmsServer serverData;
    private final RealmsConfigureWorldScreen configureScreen;
    private final RealmsScreen lastScreen;
    private final int BUTTON_INVITE_ID = 0;
    private final int BUTTON_CANCEL_ID = 1;
    private RealmsButton inviteButton;
    private final int PROFILENAME_EDIT_BOX = 2;
    private String errorMsg;
    private boolean showError;

    public RealmsInviteScreen(RealmsConfigureWorldScreen realmsConfigureWorldScreen, RealmsScreen realmsScreen, RealmsServer realmsServer) {
        this.configureScreen = realmsConfigureWorldScreen;
        this.lastScreen = realmsScreen;
        this.serverData = realmsServer;
    }

    @Override
    public void tick() {
        this.profileName.tick();
    }

    @Override
    public void init() {
        this.setKeyboardHandlerSendRepeatsToGui(true);
        this.inviteButton = new RealmsButton(0, this.width() / 2 - 100, RealmsConstants.row(10), RealmsInviteScreen.getLocalizedString("mco.configure.world.buttons.invite")){

            @Override
            public void onPress() {
                RealmsInviteScreen.this.onInvite();
            }
        };
        this.buttonsAdd(this.inviteButton);
        this.buttonsAdd(new RealmsButton(1, this.width() / 2 - 100, RealmsConstants.row(12), RealmsInviteScreen.getLocalizedString("gui.cancel")){

            @Override
            public void onPress() {
                Realms.setScreen(RealmsInviteScreen.this.lastScreen);
            }
        });
        this.profileName = this.newEditBox(2, this.width() / 2 - 100, RealmsConstants.row(2), 200, 20, RealmsInviteScreen.getLocalizedString("mco.configure.world.invite.profile.name"));
        this.focusOn(this.profileName);
        this.addWidget(this.profileName);
    }

    @Override
    public void removed() {
        this.setKeyboardHandlerSendRepeatsToGui(false);
    }

    private void onInvite() {
        RealmsClient realmsClient = RealmsClient.createRealmsClient();
        if (this.profileName.getValue() == null || this.profileName.getValue().isEmpty()) {
            this.showError(RealmsInviteScreen.getLocalizedString("mco.configure.world.players.error"));
            return;
        }
        try {
            RealmsServer realmsServer = realmsClient.invite(this.serverData.id, this.profileName.getValue().trim());
            if (realmsServer != null) {
                this.serverData.players = realmsServer.players;
                Realms.setScreen(new RealmsPlayerScreen(this.configureScreen, this.serverData));
            } else {
                this.showError(RealmsInviteScreen.getLocalizedString("mco.configure.world.players.error"));
            }
        } catch (Exception exception) {
            LOGGER.error("Couldn't invite user");
            this.showError(RealmsInviteScreen.getLocalizedString("mco.configure.world.players.error"));
        }
    }

    private void showError(String string) {
        this.showError = true;
        this.errorMsg = string;
        Realms.narrateNow(string);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == 256) {
            Realms.setScreen(this.lastScreen);
            return true;
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackground();
        this.drawString(RealmsInviteScreen.getLocalizedString("mco.configure.world.invite.profile.name"), this.width() / 2 - 100, RealmsConstants.row(1), 0xA0A0A0);
        if (this.showError) {
            this.drawCenteredString(this.errorMsg, this.width() / 2, RealmsConstants.row(5), 0xFF0000);
        }
        this.profileName.render(i, j, f);
        super.render(i, j, f);
    }
}

