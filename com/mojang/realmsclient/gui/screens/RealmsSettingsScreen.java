/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.RealmsLongConfirmationScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;

@Environment(value=EnvType.CLIENT)
public class RealmsSettingsScreen
extends RealmsScreen {
    private final RealmsConfigureWorldScreen configureWorldScreen;
    private final RealmsServer serverData;
    private Button doneButton;
    private EditBox descEdit;
    private EditBox nameEdit;
    private RealmsLabel titleLabel;

    public RealmsSettingsScreen(RealmsConfigureWorldScreen realmsConfigureWorldScreen, RealmsServer realmsServer) {
        this.configureWorldScreen = realmsConfigureWorldScreen;
        this.serverData = realmsServer;
    }

    @Override
    public void tick() {
        this.nameEdit.tick();
        this.descEdit.tick();
        this.doneButton.active = !this.nameEdit.getValue().trim().isEmpty();
    }

    @Override
    public void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        int i = this.width / 2 - 106;
        this.doneButton = this.addButton(new Button(i - 2, RealmsSettingsScreen.row(12), 106, 20, I18n.get("mco.configure.world.buttons.done", new Object[0]), button -> this.save()));
        this.addButton(new Button(this.width / 2 + 2, RealmsSettingsScreen.row(12), 106, 20, I18n.get("gui.cancel", new Object[0]), button -> this.minecraft.setScreen(this.configureWorldScreen)));
        String string = this.serverData.state == RealmsServer.State.OPEN ? "mco.configure.world.buttons.close" : "mco.configure.world.buttons.open";
        Button button2 = new Button(this.width / 2 - 53, RealmsSettingsScreen.row(0), 106, 20, I18n.get(string, new Object[0]), button -> {
            if (this.serverData.state == RealmsServer.State.OPEN) {
                String string = I18n.get("mco.configure.world.close.question.line1", new Object[0]);
                String string2 = I18n.get("mco.configure.world.close.question.line2", new Object[0]);
                this.minecraft.setScreen(new RealmsLongConfirmationScreen(bl -> {
                    if (bl) {
                        this.configureWorldScreen.closeTheWorld(this);
                    } else {
                        this.minecraft.setScreen(this);
                    }
                }, RealmsLongConfirmationScreen.Type.Info, string, string2, true));
            } else {
                this.configureWorldScreen.openTheWorld(false, this);
            }
        });
        this.addButton(button2);
        this.nameEdit = new EditBox(this.minecraft.font, i, RealmsSettingsScreen.row(4), 212, 20, null, I18n.get("mco.configure.world.name", new Object[0]));
        this.nameEdit.setMaxLength(32);
        this.nameEdit.setValue(this.serverData.getName());
        this.addWidget(this.nameEdit);
        this.magicalSpecialHackyFocus(this.nameEdit);
        this.descEdit = new EditBox(this.minecraft.font, i, RealmsSettingsScreen.row(8), 212, 20, null, I18n.get("mco.configure.world.description", new Object[0]));
        this.descEdit.setMaxLength(32);
        this.descEdit.setValue(this.serverData.getDescription());
        this.addWidget(this.descEdit);
        this.titleLabel = this.addWidget(new RealmsLabel(I18n.get("mco.configure.world.settings.title", new Object[0]), this.width / 2, 17, 0xFFFFFF));
        this.narrateLabels();
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == 256) {
            this.minecraft.setScreen(this.configureWorldScreen);
            return true;
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackground();
        this.titleLabel.render(this);
        this.font.draw(I18n.get("mco.configure.world.name", new Object[0]), this.width / 2 - 106, RealmsSettingsScreen.row(3), 0xA0A0A0);
        this.font.draw(I18n.get("mco.configure.world.description", new Object[0]), this.width / 2 - 106, RealmsSettingsScreen.row(7), 0xA0A0A0);
        this.nameEdit.render(i, j, f);
        this.descEdit.render(i, j, f);
        super.render(i, j, f);
    }

    public void save() {
        this.configureWorldScreen.saveSettings(this.nameEdit.getValue(), this.descEdit.getValue());
    }
}

