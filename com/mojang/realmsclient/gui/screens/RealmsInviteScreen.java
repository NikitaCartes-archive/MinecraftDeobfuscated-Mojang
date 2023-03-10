/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsPlayerScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsInviteScreen
extends RealmsScreen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component NAME_LABEL = Component.translatable("mco.configure.world.invite.profile.name");
    private static final Component NO_SUCH_PLAYER_ERROR_TEXT = Component.translatable("mco.configure.world.players.error");
    private EditBox profileName;
    private final RealmsServer serverData;
    private final RealmsConfigureWorldScreen configureScreen;
    private final Screen lastScreen;
    @Nullable
    private Component errorMsg;

    public RealmsInviteScreen(RealmsConfigureWorldScreen realmsConfigureWorldScreen, Screen screen, RealmsServer realmsServer) {
        super(GameNarrator.NO_TITLE);
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
        this.profileName = new EditBox(this.minecraft.font, this.width / 2 - 100, RealmsInviteScreen.row(2), 200, 20, null, Component.translatable("mco.configure.world.invite.profile.name"));
        this.addWidget(this.profileName);
        this.setInitialFocus(this.profileName);
        this.addRenderableWidget(Button.builder(Component.translatable("mco.configure.world.buttons.invite"), button -> this.onInvite()).bounds(this.width / 2 - 100, RealmsInviteScreen.row(10), 200, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.lastScreen)).bounds(this.width / 2 - 100, RealmsInviteScreen.row(12), 200, 20).build());
    }

    private void onInvite() {
        RealmsClient realmsClient = RealmsClient.create();
        if (this.profileName.getValue() == null || this.profileName.getValue().isEmpty()) {
            this.showError(NO_SUCH_PLAYER_ERROR_TEXT);
            return;
        }
        try {
            RealmsServer realmsServer = realmsClient.invite(this.serverData.id, this.profileName.getValue().trim());
            if (realmsServer != null) {
                this.serverData.players = realmsServer.players;
                this.minecraft.setScreen(new RealmsPlayerScreen(this.configureScreen, this.serverData));
            } else {
                this.showError(NO_SUCH_PLAYER_ERROR_TEXT);
            }
        } catch (Exception exception) {
            LOGGER.error("Couldn't invite user");
            this.showError(NO_SUCH_PLAYER_ERROR_TEXT);
        }
    }

    private void showError(Component component) {
        this.errorMsg = component;
        this.minecraft.getNarrator().sayNow(component);
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
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        this.font.draw(poseStack, NAME_LABEL, (float)(this.width / 2 - 100), (float)RealmsInviteScreen.row(1), 0xA0A0A0);
        if (this.errorMsg != null) {
            RealmsInviteScreen.drawCenteredString(poseStack, this.font, this.errorMsg, this.width / 2, RealmsInviteScreen.row(5), 0xFF0000);
        }
        this.profileName.render(poseStack, i, j, f);
        super.render(poseStack, i, j, f);
    }
}

