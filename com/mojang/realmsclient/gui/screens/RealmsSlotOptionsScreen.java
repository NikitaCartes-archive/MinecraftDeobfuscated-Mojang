/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.gui.RealmsConstants;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsEditBox;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.RealmsSliderButton;

@Environment(value=EnvType.CLIENT)
public class RealmsSlotOptionsScreen
extends RealmsScreen {
    private RealmsEditBox nameEdit;
    protected final RealmsConfigureWorldScreen parent;
    private int column1_x;
    private int column_width;
    private int column2_x;
    private final RealmsWorldOptions options;
    private final RealmsServer.WorldType worldType;
    private final int activeSlot;
    private int difficultyIndex;
    private int gameModeIndex;
    private Boolean pvp;
    private Boolean spawnNPCs;
    private Boolean spawnAnimals;
    private Boolean spawnMonsters;
    private Integer spawnProtection;
    private Boolean commandBlocks;
    private Boolean forceGameMode;
    private RealmsButton pvpButton;
    private RealmsButton spawnAnimalsButton;
    private RealmsButton spawnMonstersButton;
    private RealmsButton spawnNPCsButton;
    private RealmsSliderButton spawnProtectionButton;
    private RealmsButton commandBlocksButton;
    private RealmsButton forceGameModeButton;
    String[] difficulties;
    String[] gameModes;
    String[][] gameModeHints;
    private RealmsLabel titleLabel;
    private RealmsLabel warningLabel;

    public RealmsSlotOptionsScreen(RealmsConfigureWorldScreen realmsConfigureWorldScreen, RealmsWorldOptions realmsWorldOptions, RealmsServer.WorldType worldType, int i) {
        this.parent = realmsConfigureWorldScreen;
        this.options = realmsWorldOptions;
        this.worldType = worldType;
        this.activeSlot = i;
    }

    @Override
    public void removed() {
        this.setKeyboardHandlerSendRepeatsToGui(false);
    }

    @Override
    public void tick() {
        this.nameEdit.tick();
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        switch (i) {
            case 256: {
                Realms.setScreen(this.parent);
                return true;
            }
        }
        return super.keyPressed(i, j, k);
    }

    @Override
    public void init() {
        this.column_width = 170;
        this.column1_x = this.width() / 2 - this.column_width * 2 / 2;
        this.column2_x = this.width() / 2 + 10;
        this.createDifficultyAndGameMode();
        this.difficultyIndex = this.options.difficulty;
        this.gameModeIndex = this.options.gameMode;
        if (this.worldType.equals((Object)RealmsServer.WorldType.NORMAL)) {
            this.pvp = this.options.pvp;
            this.spawnProtection = this.options.spawnProtection;
            this.forceGameMode = this.options.forceGameMode;
            this.spawnAnimals = this.options.spawnAnimals;
            this.spawnMonsters = this.options.spawnMonsters;
            this.spawnNPCs = this.options.spawnNPCs;
            this.commandBlocks = this.options.commandBlocks;
        } else {
            String string = this.worldType.equals((Object)RealmsServer.WorldType.ADVENTUREMAP) ? RealmsSlotOptionsScreen.getLocalizedString("mco.configure.world.edit.subscreen.adventuremap") : (this.worldType.equals((Object)RealmsServer.WorldType.INSPIRATION) ? RealmsSlotOptionsScreen.getLocalizedString("mco.configure.world.edit.subscreen.inspiration") : RealmsSlotOptionsScreen.getLocalizedString("mco.configure.world.edit.subscreen.experience"));
            this.warningLabel = new RealmsLabel(string, this.width() / 2, 26, 0xFF0000);
            this.pvp = true;
            this.spawnProtection = 0;
            this.forceGameMode = false;
            this.spawnAnimals = true;
            this.spawnMonsters = true;
            this.spawnNPCs = true;
            this.commandBlocks = true;
        }
        this.nameEdit = this.newEditBox(11, this.column1_x + 2, RealmsConstants.row(1), this.column_width - 4, 20, RealmsSlotOptionsScreen.getLocalizedString("mco.configure.world.edit.slot.name"));
        this.nameEdit.setMaxLength(10);
        this.nameEdit.setValue(this.options.getSlotName(this.activeSlot));
        this.focusOn(this.nameEdit);
        this.pvpButton = new RealmsButton(4, this.column2_x, RealmsConstants.row(1), this.column_width, 20, this.pvpTitle()){

            @Override
            public void onPress() {
                RealmsSlotOptionsScreen.this.pvp = RealmsSlotOptionsScreen.this.pvp == false;
                this.setMessage(RealmsSlotOptionsScreen.this.pvpTitle());
            }
        };
        this.buttonsAdd(this.pvpButton);
        this.buttonsAdd(new RealmsButton(3, this.column1_x, RealmsConstants.row(3), this.column_width, 20, this.gameModeTitle()){

            @Override
            public void onPress() {
                RealmsSlotOptionsScreen.this.gameModeIndex = (RealmsSlotOptionsScreen.this.gameModeIndex + 1) % RealmsSlotOptionsScreen.this.gameModes.length;
                this.setMessage(RealmsSlotOptionsScreen.this.gameModeTitle());
            }
        });
        this.spawnAnimalsButton = new RealmsButton(5, this.column2_x, RealmsConstants.row(3), this.column_width, 20, this.spawnAnimalsTitle()){

            @Override
            public void onPress() {
                RealmsSlotOptionsScreen.this.spawnAnimals = RealmsSlotOptionsScreen.this.spawnAnimals == false;
                this.setMessage(RealmsSlotOptionsScreen.this.spawnAnimalsTitle());
            }
        };
        this.buttonsAdd(this.spawnAnimalsButton);
        this.buttonsAdd(new RealmsButton(2, this.column1_x, RealmsConstants.row(5), this.column_width, 20, this.difficultyTitle()){

            @Override
            public void onPress() {
                RealmsSlotOptionsScreen.this.difficultyIndex = (RealmsSlotOptionsScreen.this.difficultyIndex + 1) % RealmsSlotOptionsScreen.this.difficulties.length;
                this.setMessage(RealmsSlotOptionsScreen.this.difficultyTitle());
                if (RealmsSlotOptionsScreen.this.worldType.equals((Object)RealmsServer.WorldType.NORMAL)) {
                    RealmsSlotOptionsScreen.this.spawnMonstersButton.active(RealmsSlotOptionsScreen.this.difficultyIndex != 0);
                    RealmsSlotOptionsScreen.this.spawnMonstersButton.setMessage(RealmsSlotOptionsScreen.this.spawnMonstersTitle());
                }
            }
        });
        this.spawnMonstersButton = new RealmsButton(6, this.column2_x, RealmsConstants.row(5), this.column_width, 20, this.spawnMonstersTitle()){

            @Override
            public void onPress() {
                RealmsSlotOptionsScreen.this.spawnMonsters = RealmsSlotOptionsScreen.this.spawnMonsters == false;
                this.setMessage(RealmsSlotOptionsScreen.this.spawnMonstersTitle());
            }
        };
        this.buttonsAdd(this.spawnMonstersButton);
        this.spawnProtectionButton = new SettingsSlider(8, this.column1_x, RealmsConstants.row(7), this.column_width, this.spawnProtection, 0.0f, 16.0f);
        this.buttonsAdd(this.spawnProtectionButton);
        this.spawnNPCsButton = new RealmsButton(7, this.column2_x, RealmsConstants.row(7), this.column_width, 20, this.spawnNPCsTitle()){

            @Override
            public void onPress() {
                RealmsSlotOptionsScreen.this.spawnNPCs = RealmsSlotOptionsScreen.this.spawnNPCs == false;
                this.setMessage(RealmsSlotOptionsScreen.this.spawnNPCsTitle());
            }
        };
        this.buttonsAdd(this.spawnNPCsButton);
        this.forceGameModeButton = new RealmsButton(10, this.column1_x, RealmsConstants.row(9), this.column_width, 20, this.forceGameModeTitle()){

            @Override
            public void onPress() {
                RealmsSlotOptionsScreen.this.forceGameMode = RealmsSlotOptionsScreen.this.forceGameMode == false;
                this.setMessage(RealmsSlotOptionsScreen.this.forceGameModeTitle());
            }
        };
        this.buttonsAdd(this.forceGameModeButton);
        this.commandBlocksButton = new RealmsButton(9, this.column2_x, RealmsConstants.row(9), this.column_width, 20, this.commandBlocksTitle()){

            @Override
            public void onPress() {
                RealmsSlotOptionsScreen.this.commandBlocks = RealmsSlotOptionsScreen.this.commandBlocks == false;
                this.setMessage(RealmsSlotOptionsScreen.this.commandBlocksTitle());
            }
        };
        this.buttonsAdd(this.commandBlocksButton);
        if (!this.worldType.equals((Object)RealmsServer.WorldType.NORMAL)) {
            this.pvpButton.active(false);
            this.spawnAnimalsButton.active(false);
            this.spawnNPCsButton.active(false);
            this.spawnMonstersButton.active(false);
            this.spawnProtectionButton.active(false);
            this.commandBlocksButton.active(false);
            this.spawnProtectionButton.active(false);
            this.forceGameModeButton.active(false);
        }
        if (this.difficultyIndex == 0) {
            this.spawnMonstersButton.active(false);
        }
        this.buttonsAdd(new RealmsButton(1, this.column1_x, RealmsConstants.row(13), this.column_width, 20, RealmsSlotOptionsScreen.getLocalizedString("mco.configure.world.buttons.done")){

            @Override
            public void onPress() {
                RealmsSlotOptionsScreen.this.saveSettings();
            }
        });
        this.buttonsAdd(new RealmsButton(0, this.column2_x, RealmsConstants.row(13), this.column_width, 20, RealmsSlotOptionsScreen.getLocalizedString("gui.cancel")){

            @Override
            public void onPress() {
                Realms.setScreen(RealmsSlotOptionsScreen.this.parent);
            }
        });
        this.addWidget(this.nameEdit);
        this.titleLabel = new RealmsLabel(RealmsSlotOptionsScreen.getLocalizedString("mco.configure.world.buttons.options"), this.width() / 2, 17, 0xFFFFFF);
        this.addWidget(this.titleLabel);
        if (this.warningLabel != null) {
            this.addWidget(this.warningLabel);
        }
        this.narrateLabels();
    }

    private void createDifficultyAndGameMode() {
        this.difficulties = new String[]{RealmsSlotOptionsScreen.getLocalizedString("options.difficulty.peaceful"), RealmsSlotOptionsScreen.getLocalizedString("options.difficulty.easy"), RealmsSlotOptionsScreen.getLocalizedString("options.difficulty.normal"), RealmsSlotOptionsScreen.getLocalizedString("options.difficulty.hard")};
        this.gameModes = new String[]{RealmsSlotOptionsScreen.getLocalizedString("selectWorld.gameMode.survival"), RealmsSlotOptionsScreen.getLocalizedString("selectWorld.gameMode.creative"), RealmsSlotOptionsScreen.getLocalizedString("selectWorld.gameMode.adventure")};
        this.gameModeHints = new String[][]{{RealmsSlotOptionsScreen.getLocalizedString("selectWorld.gameMode.survival.line1"), RealmsSlotOptionsScreen.getLocalizedString("selectWorld.gameMode.survival.line2")}, {RealmsSlotOptionsScreen.getLocalizedString("selectWorld.gameMode.creative.line1"), RealmsSlotOptionsScreen.getLocalizedString("selectWorld.gameMode.creative.line2")}, {RealmsSlotOptionsScreen.getLocalizedString("selectWorld.gameMode.adventure.line1"), RealmsSlotOptionsScreen.getLocalizedString("selectWorld.gameMode.adventure.line2")}};
    }

    private String difficultyTitle() {
        String string = RealmsSlotOptionsScreen.getLocalizedString("options.difficulty");
        return string + ": " + this.difficulties[this.difficultyIndex];
    }

    private String gameModeTitle() {
        String string = RealmsSlotOptionsScreen.getLocalizedString("selectWorld.gameMode");
        return string + ": " + this.gameModes[this.gameModeIndex];
    }

    private String pvpTitle() {
        return RealmsSlotOptionsScreen.getLocalizedString("mco.configure.world.pvp") + ": " + RealmsSlotOptionsScreen.getLocalizedString(this.pvp != false ? "mco.configure.world.on" : "mco.configure.world.off");
    }

    private String spawnAnimalsTitle() {
        return RealmsSlotOptionsScreen.getLocalizedString("mco.configure.world.spawnAnimals") + ": " + RealmsSlotOptionsScreen.getLocalizedString(this.spawnAnimals != false ? "mco.configure.world.on" : "mco.configure.world.off");
    }

    private String spawnMonstersTitle() {
        if (this.difficultyIndex == 0) {
            return RealmsSlotOptionsScreen.getLocalizedString("mco.configure.world.spawnMonsters") + ": " + RealmsSlotOptionsScreen.getLocalizedString("mco.configure.world.off");
        }
        return RealmsSlotOptionsScreen.getLocalizedString("mco.configure.world.spawnMonsters") + ": " + RealmsSlotOptionsScreen.getLocalizedString(this.spawnMonsters != false ? "mco.configure.world.on" : "mco.configure.world.off");
    }

    private String spawnNPCsTitle() {
        return RealmsSlotOptionsScreen.getLocalizedString("mco.configure.world.spawnNPCs") + ": " + RealmsSlotOptionsScreen.getLocalizedString(this.spawnNPCs != false ? "mco.configure.world.on" : "mco.configure.world.off");
    }

    private String commandBlocksTitle() {
        return RealmsSlotOptionsScreen.getLocalizedString("mco.configure.world.commandBlocks") + ": " + RealmsSlotOptionsScreen.getLocalizedString(this.commandBlocks != false ? "mco.configure.world.on" : "mco.configure.world.off");
    }

    private String forceGameModeTitle() {
        return RealmsSlotOptionsScreen.getLocalizedString("mco.configure.world.forceGameMode") + ": " + RealmsSlotOptionsScreen.getLocalizedString(this.forceGameMode != false ? "mco.configure.world.on" : "mco.configure.world.off");
    }

    @Override
    public void render(int i, int j, float f) {
        this.renderBackground();
        String string = RealmsSlotOptionsScreen.getLocalizedString("mco.configure.world.edit.slot.name");
        this.drawString(string, this.column1_x + this.column_width / 2 - this.fontWidth(string) / 2, RealmsConstants.row(0) - 5, 0xFFFFFF);
        this.titleLabel.render(this);
        if (this.warningLabel != null) {
            this.warningLabel.render(this);
        }
        this.nameEdit.render(i, j, f);
        super.render(i, j, f);
    }

    private String getSlotName() {
        if (this.nameEdit.getValue().equals(this.options.getDefaultSlotName(this.activeSlot))) {
            return "";
        }
        return this.nameEdit.getValue();
    }

    private void saveSettings() {
        if (this.worldType.equals((Object)RealmsServer.WorldType.ADVENTUREMAP) || this.worldType.equals((Object)RealmsServer.WorldType.EXPERIENCE) || this.worldType.equals((Object)RealmsServer.WorldType.INSPIRATION)) {
            this.parent.saveSlotSettings(new RealmsWorldOptions(this.options.pvp, this.options.spawnAnimals, this.options.spawnMonsters, this.options.spawnNPCs, this.options.spawnProtection, this.options.commandBlocks, this.difficultyIndex, this.gameModeIndex, this.options.forceGameMode, this.getSlotName()));
        } else {
            this.parent.saveSlotSettings(new RealmsWorldOptions(this.pvp, this.spawnAnimals, this.spawnMonsters, this.spawnNPCs, this.spawnProtection, this.commandBlocks, this.difficultyIndex, this.gameModeIndex, this.forceGameMode, this.getSlotName()));
        }
    }

    @Environment(value=EnvType.CLIENT)
    class SettingsSlider
    extends RealmsSliderButton {
        public SettingsSlider(int i, int j, int k, int l, int m, float f, float g) {
            super(i, j, k, l, m, f, g);
        }

        @Override
        public void applyValue() {
            if (!RealmsSlotOptionsScreen.this.spawnProtectionButton.active()) {
                return;
            }
            RealmsSlotOptionsScreen.this.spawnProtection = (int)this.toValue(this.getValue());
        }

        @Override
        public String getMessage() {
            return RealmsScreen.getLocalizedString("mco.configure.world.spawnProtection") + ": " + (RealmsSlotOptionsScreen.this.spawnProtection == 0 ? RealmsScreen.getLocalizedString("mco.configure.world.off") : RealmsSlotOptionsScreen.this.spawnProtection);
        }
    }
}

