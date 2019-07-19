/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongRunningMcoTaskScreen;
import com.mojang.realmsclient.util.RealmsTasks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsEditBox;
import net.minecraft.realms.RealmsScreen;

@Environment(value=EnvType.CLIENT)
public class RealmsCreateTrialScreen
extends RealmsScreen {
    private final RealmsMainScreen lastScreen;
    private RealmsEditBox nameBox;
    private RealmsEditBox descriptionBox;
    private boolean initialized;
    private RealmsButton createButton;

    public RealmsCreateTrialScreen(RealmsMainScreen realmsMainScreen) {
        this.lastScreen = realmsMainScreen;
    }

    @Override
    public void tick() {
        if (this.nameBox != null) {
            this.nameBox.tick();
            this.createButton.active(this.valid());
        }
        if (this.descriptionBox != null) {
            this.descriptionBox.tick();
        }
    }

    @Override
    public void init() {
        this.setKeyboardHandlerSendRepeatsToGui(true);
        if (!this.initialized) {
            this.initialized = true;
            this.nameBox = this.newEditBox(3, this.width() / 2 - 100, 65, 200, 20, RealmsCreateTrialScreen.getLocalizedString("mco.configure.world.name"));
            this.focusOn(this.nameBox);
            this.descriptionBox = this.newEditBox(4, this.width() / 2 - 100, 115, 200, 20, RealmsCreateTrialScreen.getLocalizedString("mco.configure.world.description"));
        }
        this.createButton = new RealmsButton(0, this.width() / 2 - 100, this.height() / 4 + 120 + 17, 97, 20, RealmsCreateTrialScreen.getLocalizedString("mco.create.world")){

            @Override
            public void onPress() {
                RealmsCreateTrialScreen.this.createWorld();
            }
        };
        this.buttonsAdd(this.createButton);
        this.buttonsAdd(new RealmsButton(1, this.width() / 2 + 5, this.height() / 4 + 120 + 17, 95, 20, RealmsCreateTrialScreen.getLocalizedString("gui.cancel")){

            @Override
            public void onPress() {
                Realms.setScreen(RealmsCreateTrialScreen.this.lastScreen);
            }
        });
        this.createButton.active(this.valid());
        this.addWidget(this.nameBox);
        this.addWidget(this.descriptionBox);
    }

    @Override
    public void removed() {
        this.setKeyboardHandlerSendRepeatsToGui(false);
    }

    @Override
    public boolean charTyped(char c, int i) {
        this.createButton.active(this.valid());
        return false;
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        switch (i) {
            case 256: {
                Realms.setScreen(this.lastScreen);
                return true;
            }
        }
        this.createButton.active(this.valid());
        return false;
    }

    private void createWorld() {
        if (this.valid()) {
            RealmsTasks.TrialCreationTask trialCreationTask = new RealmsTasks.TrialCreationTask(this.nameBox.getValue(), this.descriptionBox.getValue(), this.lastScreen);
            RealmsLongRunningMcoTaskScreen realmsLongRunningMcoTaskScreen = new RealmsLongRunningMcoTaskScreen(this.lastScreen, trialCreationTask);
            realmsLongRunningMcoTaskScreen.start();
            Realms.setScreen(realmsLongRunningMcoTaskScreen);
        }
    }

    private boolean valid() {
        return this.nameBox != null && this.nameBox.getValue() != null && !this.nameBox.getValue().trim().isEmpty();
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackground();
        this.drawCenteredString(RealmsCreateTrialScreen.getLocalizedString("mco.trial.title"), this.width() / 2, 11, 0xFFFFFF);
        this.drawString(RealmsCreateTrialScreen.getLocalizedString("mco.configure.world.name"), this.width() / 2 - 100, 52, 0xA0A0A0);
        this.drawString(RealmsCreateTrialScreen.getLocalizedString("mco.configure.world.description"), this.width() / 2 - 100, 102, 0xA0A0A0);
        if (this.nameBox != null) {
            this.nameBox.render(i, j, f);
        }
        if (this.descriptionBox != null) {
            this.descriptionBox.render(i, j, f);
        }
        super.render(i, j, f);
    }
}

