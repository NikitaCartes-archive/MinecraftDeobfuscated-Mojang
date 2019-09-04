/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.dto.Backup;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.RealmsSimpleScrolledSelectionList;
import net.minecraft.realms.Tezzelator;

@Environment(value=EnvType.CLIENT)
public class RealmsBackupInfoScreen
extends RealmsScreen {
    private final RealmsScreen lastScreen;
    private final int BUTTON_BACK_ID = 0;
    private final Backup backup;
    private final List<String> keys = Lists.newArrayList();
    private BackupInfoList backupInfoList;
    String[] difficulties = new String[]{RealmsBackupInfoScreen.getLocalizedString("options.difficulty.peaceful"), RealmsBackupInfoScreen.getLocalizedString("options.difficulty.easy"), RealmsBackupInfoScreen.getLocalizedString("options.difficulty.normal"), RealmsBackupInfoScreen.getLocalizedString("options.difficulty.hard")};
    String[] gameModes = new String[]{RealmsBackupInfoScreen.getLocalizedString("selectWorld.gameMode.survival"), RealmsBackupInfoScreen.getLocalizedString("selectWorld.gameMode.creative"), RealmsBackupInfoScreen.getLocalizedString("selectWorld.gameMode.adventure")};

    public RealmsBackupInfoScreen(RealmsScreen realmsScreen, Backup backup) {
        this.lastScreen = realmsScreen;
        this.backup = backup;
        if (backup.changeList != null) {
            for (Map.Entry<String, String> entry : backup.changeList.entrySet()) {
                this.keys.add(entry.getKey());
            }
        }
    }

    @Override
    public void tick() {
    }

    @Override
    public void init() {
        this.setKeyboardHandlerSendRepeatsToGui(true);
        this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 100, this.height() / 4 + 120 + 24, RealmsBackupInfoScreen.getLocalizedString("gui.back")){

            @Override
            public void onPress() {
                Realms.setScreen(RealmsBackupInfoScreen.this.lastScreen);
            }
        });
        this.backupInfoList = new BackupInfoList();
        this.addWidget(this.backupInfoList);
        this.focusOn(this.backupInfoList);
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

    @Override
    public void render(int i, int j, float f) {
        this.renderBackground();
        this.drawCenteredString("Changes from last backup", this.width() / 2, 10, 0xFFFFFF);
        this.backupInfoList.render(i, j, f);
        super.render(i, j, f);
    }

    private String checkForSpecificMetadata(String string, String string2) {
        String string3 = string.toLowerCase(Locale.ROOT);
        if (string3.contains("game") && string3.contains("mode")) {
            return this.gameModeMetadata(string2);
        }
        if (string3.contains("game") && string3.contains("difficulty")) {
            return this.gameDifficultyMetadata(string2);
        }
        return string2;
    }

    private String gameDifficultyMetadata(String string) {
        try {
            return this.difficulties[Integer.parseInt(string)];
        } catch (Exception exception) {
            return "UNKNOWN";
        }
    }

    private String gameModeMetadata(String string) {
        try {
            return this.gameModes[Integer.parseInt(string)];
        } catch (Exception exception) {
            return "UNKNOWN";
        }
    }

    @Environment(value=EnvType.CLIENT)
    class BackupInfoList
    extends RealmsSimpleScrolledSelectionList {
        public BackupInfoList() {
            super(RealmsBackupInfoScreen.this.width(), RealmsBackupInfoScreen.this.height(), 32, RealmsBackupInfoScreen.this.height() - 64, 36);
        }

        @Override
        public int getItemCount() {
            return ((RealmsBackupInfoScreen)RealmsBackupInfoScreen.this).backup.changeList.size();
        }

        @Override
        public boolean isSelectedItem(int i) {
            return false;
        }

        @Override
        public int getMaxPosition() {
            return this.getItemCount() * 36;
        }

        @Override
        public void renderBackground() {
        }

        @Override
        public void renderItem(int i, int j, int k, int l, Tezzelator tezzelator, int m, int n) {
            String string = (String)RealmsBackupInfoScreen.this.keys.get(i);
            RealmsBackupInfoScreen.this.drawString(string, this.width() / 2 - 40, k, 0xA0A0A0);
            String string2 = ((RealmsBackupInfoScreen)RealmsBackupInfoScreen.this).backup.changeList.get(string);
            RealmsBackupInfoScreen.this.drawString(RealmsBackupInfoScreen.this.checkForSpecificMetadata(string, string2), this.width() / 2 - 40, k + 12, 0xFFFFFF);
        }
    }
}

