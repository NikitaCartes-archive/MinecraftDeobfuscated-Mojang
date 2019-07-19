/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.gui.screens.RealmsResetWorldScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsEditBox;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;

@Environment(value=EnvType.CLIENT)
public class RealmsResetNormalWorldScreen
extends RealmsScreen {
    private final RealmsResetWorldScreen lastScreen;
    private RealmsLabel titleLabel;
    private RealmsEditBox seedEdit;
    private Boolean generateStructures = true;
    private Integer levelTypeIndex = 0;
    String[] levelTypes;
    private final int BUTTON_CANCEL_ID = 0;
    private final int BUTTON_RESET_ID = 1;
    private final int SEED_EDIT_BOX = 4;
    private RealmsButton resetButton;
    private RealmsButton levelTypeButton;
    private RealmsButton generateStructuresButton;
    private String buttonTitle = RealmsResetNormalWorldScreen.getLocalizedString("mco.backup.button.reset");

    public RealmsResetNormalWorldScreen(RealmsResetWorldScreen realmsResetWorldScreen) {
        this.lastScreen = realmsResetWorldScreen;
    }

    public RealmsResetNormalWorldScreen(RealmsResetWorldScreen realmsResetWorldScreen, String string) {
        this(realmsResetWorldScreen);
        this.buttonTitle = string;
    }

    @Override
    public void tick() {
        this.seedEdit.tick();
        super.tick();
    }

    @Override
    public void init() {
        this.levelTypes = new String[]{RealmsResetNormalWorldScreen.getLocalizedString("generator.default"), RealmsResetNormalWorldScreen.getLocalizedString("generator.flat"), RealmsResetNormalWorldScreen.getLocalizedString("generator.largeBiomes"), RealmsResetNormalWorldScreen.getLocalizedString("generator.amplified")};
        this.setKeyboardHandlerSendRepeatsToGui(true);
        this.buttonsAdd(new RealmsButton(0, this.width() / 2 + 8, RealmsConstants.row(12), 97, 20, RealmsResetNormalWorldScreen.getLocalizedString("gui.back")){

            @Override
            public void onPress() {
                Realms.setScreen(RealmsResetNormalWorldScreen.this.lastScreen);
            }
        });
        this.resetButton = new RealmsButton(1, this.width() / 2 - 102, RealmsConstants.row(12), 97, 20, this.buttonTitle){

            @Override
            public void onPress() {
                RealmsResetNormalWorldScreen.this.onReset();
            }
        };
        this.buttonsAdd(this.resetButton);
        this.seedEdit = this.newEditBox(4, this.width() / 2 - 100, RealmsConstants.row(2), 200, 20, RealmsResetNormalWorldScreen.getLocalizedString("mco.reset.world.seed"));
        this.seedEdit.setMaxLength(32);
        this.seedEdit.setValue("");
        this.addWidget(this.seedEdit);
        this.focusOn(this.seedEdit);
        this.levelTypeButton = new RealmsButton(2, this.width() / 2 - 102, RealmsConstants.row(4), 205, 20, this.levelTypeTitle()){

            @Override
            public void onPress() {
                RealmsResetNormalWorldScreen.this.levelTypeIndex = (RealmsResetNormalWorldScreen.this.levelTypeIndex + 1) % RealmsResetNormalWorldScreen.this.levelTypes.length;
                this.setMessage(RealmsResetNormalWorldScreen.this.levelTypeTitle());
            }
        };
        this.buttonsAdd(this.levelTypeButton);
        this.generateStructuresButton = new RealmsButton(3, this.width() / 2 - 102, RealmsConstants.row(6) - 2, 205, 20, this.generateStructuresTitle()){

            @Override
            public void onPress() {
                RealmsResetNormalWorldScreen.this.generateStructures = RealmsResetNormalWorldScreen.this.generateStructures == false;
                this.setMessage(RealmsResetNormalWorldScreen.this.generateStructuresTitle());
            }
        };
        this.buttonsAdd(this.generateStructuresButton);
        this.titleLabel = new RealmsLabel(RealmsResetNormalWorldScreen.getLocalizedString("mco.reset.world.generate"), this.width() / 2, 17, 0xFFFFFF);
        this.addWidget(this.titleLabel);
        this.narrateLabels();
    }

    @Override
    public void removed() {
        this.setKeyboardHandlerSendRepeatsToGui(false);
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == 256) {
            Realms.setScreen(this.lastScreen);
            return true;
        }
        return super.keyPressed(i, j, k);
    }

    private void onReset() {
        this.lastScreen.resetWorld(new RealmsResetWorldScreen.ResetWorldInfo(this.seedEdit.getValue(), this.levelTypeIndex, this.generateStructures));
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackground();
        this.titleLabel.render(this);
        this.drawString(RealmsResetNormalWorldScreen.getLocalizedString("mco.reset.world.seed"), this.width() / 2 - 100, RealmsConstants.row(1), 0xA0A0A0);
        this.seedEdit.render(i, j, f);
        super.render(i, j, f);
    }

    private String levelTypeTitle() {
        String string = RealmsResetNormalWorldScreen.getLocalizedString("selectWorld.mapType");
        return string + " " + this.levelTypes[this.levelTypeIndex];
    }

    private String generateStructuresTitle() {
        return RealmsResetNormalWorldScreen.getLocalizedString("selectWorld.mapFeatures") + " " + RealmsResetNormalWorldScreen.getLocalizedString(this.generateStructures != false ? "mco.configure.world.on" : "mco.configure.world.off");
    }
}

