/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.gui.screens.RealmsResetWorldScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;

@Environment(value=EnvType.CLIENT)
public class RealmsResetNormalWorldScreen
extends RealmsScreen {
    private final RealmsResetWorldScreen lastScreen;
    private RealmsLabel titleLabel;
    private EditBox seedEdit;
    private Boolean generateStructures = true;
    private Integer levelTypeIndex = 0;
    private String[] levelTypes;
    private String buttonTitle;

    public RealmsResetNormalWorldScreen(RealmsResetWorldScreen realmsResetWorldScreen, String string) {
        this.lastScreen = realmsResetWorldScreen;
        this.buttonTitle = string;
    }

    @Override
    public void tick() {
        this.seedEdit.tick();
        super.tick();
    }

    @Override
    public void init() {
        this.levelTypes = new String[]{I18n.get("generator.default", new Object[0]), I18n.get("generator.flat", new Object[0]), I18n.get("generator.largeBiomes", new Object[0]), I18n.get("generator.amplified", new Object[0])};
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.titleLabel = new RealmsLabel(I18n.get("mco.reset.world.generate", new Object[0]), this.width / 2, 17, 0xFFFFFF);
        this.addWidget(this.titleLabel);
        this.seedEdit = new EditBox(this.minecraft.font, this.width / 2 - 100, RealmsResetNormalWorldScreen.row(2), 200, 20, null, I18n.get("mco.reset.world.seed", new Object[0]));
        this.seedEdit.setMaxLength(32);
        this.addWidget(this.seedEdit);
        this.setInitialFocus(this.seedEdit);
        this.addButton(new Button(this.width / 2 - 102, RealmsResetNormalWorldScreen.row(4), 205, 20, this.levelTypeTitle(), button -> {
            this.levelTypeIndex = (this.levelTypeIndex + 1) % this.levelTypes.length;
            button.setMessage(this.levelTypeTitle());
        }));
        this.addButton(new Button(this.width / 2 - 102, RealmsResetNormalWorldScreen.row(6) - 2, 205, 20, this.generateStructuresTitle(), button -> {
            this.generateStructures = this.generateStructures == false;
            button.setMessage(this.generateStructuresTitle());
        }));
        this.addButton(new Button(this.width / 2 - 102, RealmsResetNormalWorldScreen.row(12), 97, 20, this.buttonTitle, button -> this.lastScreen.resetWorld(new RealmsResetWorldScreen.ResetWorldInfo(this.seedEdit.getValue(), this.levelTypeIndex, this.generateStructures))));
        this.addButton(new Button(this.width / 2 + 8, RealmsResetNormalWorldScreen.row(12), 97, 20, I18n.get("gui.back", new Object[0]), button -> this.minecraft.setScreen(this.lastScreen)));
        this.narrateLabels();
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

    @Override
    public void render(int i, int j, float f) {
        this.renderBackground();
        this.titleLabel.render(this);
        this.font.draw(I18n.get("mco.reset.world.seed", new Object[0]), this.width / 2 - 100, RealmsResetNormalWorldScreen.row(1), 0xA0A0A0);
        this.seedEdit.render(i, j, f);
        super.render(i, j, f);
    }

    private String levelTypeTitle() {
        String string = I18n.get("selectWorld.mapType", new Object[0]);
        return string + " " + this.levelTypes[this.levelTypeIndex];
    }

    private String generateStructuresTitle() {
        String string = this.generateStructures != false ? "mco.configure.world.on" : "mco.configure.world.off";
        return I18n.get("selectWorld.mapFeatures", new Object[0]) + " " + I18n.get(string, new Object[0]);
    }
}

