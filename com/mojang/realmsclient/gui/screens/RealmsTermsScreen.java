/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.util.task.GetServerDetailsTask;
import java.util.concurrent.locks.ReentrantLock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.realms.RealmsScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsTermsScreen
extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Screen lastScreen;
    private final RealmsMainScreen mainScreen;
    private final RealmsServer realmsServer;
    private boolean onLink;
    private final String realmsToSUrl = "https://minecraft.net/realms/terms";

    public RealmsTermsScreen(Screen screen, RealmsMainScreen realmsMainScreen, RealmsServer realmsServer) {
        this.lastScreen = screen;
        this.mainScreen = realmsMainScreen;
        this.realmsServer = realmsServer;
    }

    @Override
    public void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        int i = this.width / 4 - 2;
        this.addButton(new Button(this.width / 4, RealmsTermsScreen.row(12), i, 20, I18n.get("mco.terms.buttons.agree", new Object[0]), button -> this.agreedToTos()));
        this.addButton(new Button(this.width / 2 + 4, RealmsTermsScreen.row(12), i, 20, I18n.get("mco.terms.buttons.disagree", new Object[0]), button -> this.minecraft.setScreen(this.lastScreen)));
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == 256) {
            this.minecraft.setScreen(this.lastScreen);
            return true;
        }
        return super.keyPressed(i, j, k);
    }

    private void agreedToTos() {
        RealmsClient realmsClient = RealmsClient.create();
        try {
            realmsClient.agreeToTos();
            this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen, new GetServerDetailsTask(this.mainScreen, this.lastScreen, this.realmsServer, new ReentrantLock())));
        } catch (RealmsServiceException realmsServiceException) {
            LOGGER.error("Couldn't agree to TOS");
        }
    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if (this.onLink) {
            this.minecraft.keyboardHandler.setClipboard("https://minecraft.net/realms/terms");
            Util.getPlatform().openUri("https://minecraft.net/realms/terms");
            return true;
        }
        return super.mouseClicked(d, e, i);
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackground();
        this.drawCenteredString(this.font, I18n.get("mco.terms.title", new Object[0]), this.width / 2, 17, 0xFFFFFF);
        this.font.draw(I18n.get("mco.terms.sentence.1", new Object[0]), this.width / 2 - 120, RealmsTermsScreen.row(5), 0xFFFFFF);
        int k = this.font.width(I18n.get("mco.terms.sentence.1", new Object[0]));
        int l = this.width / 2 - 121 + k;
        int m = RealmsTermsScreen.row(5);
        int n = l + this.font.width("mco.terms.sentence.2") + 1;
        int o = m + 1 + this.font.lineHeight;
        if (l <= i && i <= n && m <= j && j <= o) {
            this.onLink = true;
            this.font.draw(" " + I18n.get("mco.terms.sentence.2", new Object[0]), this.width / 2 - 120 + k, RealmsTermsScreen.row(5), 7107012);
        } else {
            this.onLink = false;
            this.font.draw(" " + I18n.get("mco.terms.sentence.2", new Object[0]), this.width / 2 - 120 + k, RealmsTermsScreen.row(5), 0x3366BB);
        }
        super.render(i, j, f);
    }
}

