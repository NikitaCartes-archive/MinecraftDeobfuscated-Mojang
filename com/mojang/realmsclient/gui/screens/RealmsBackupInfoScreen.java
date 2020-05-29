/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.dto.Backup;
import com.mojang.realmsclient.gui.screens.RealmsSlotOptionsScreen;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.realms.RealmsScreen;

@Environment(value=EnvType.CLIENT)
public class RealmsBackupInfoScreen
extends RealmsScreen {
    private final Screen lastScreen;
    private final Backup backup;
    private BackupInfoList backupInfoList;

    public RealmsBackupInfoScreen(Screen screen, Backup backup) {
        this.lastScreen = screen;
        this.backup = backup;
    }

    @Override
    public void tick() {
    }

    @Override
    public void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 120 + 24, 200, 20, CommonComponents.GUI_BACK, button -> this.minecraft.setScreen(this.lastScreen)));
        this.backupInfoList = new BackupInfoList(this.minecraft);
        this.addWidget(this.backupInfoList);
        this.magicalSpecialHackyFocus(this.backupInfoList);
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
    public void render(PoseStack poseStack, int i, int j, float f) {
        this.renderBackground(poseStack);
        this.drawCenteredString(poseStack, this.font, "Changes from last backup", this.width / 2, 10, 0xFFFFFF);
        this.backupInfoList.render(poseStack, i, j, f);
        super.render(poseStack, i, j, f);
    }

    private Component checkForSpecificMetadata(String string, String string2) {
        String string3 = string.toLowerCase(Locale.ROOT);
        if (string3.contains("game") && string3.contains("mode")) {
            return this.gameModeMetadata(string2);
        }
        if (string3.contains("game") && string3.contains("difficulty")) {
            return this.gameDifficultyMetadata(string2);
        }
        return new TextComponent(string2);
    }

    private Component gameDifficultyMetadata(String string) {
        try {
            return RealmsSlotOptionsScreen.DIFFICULTIES[Integer.parseInt(string)];
        } catch (Exception exception) {
            return new TextComponent("UNKNOWN");
        }
    }

    private Component gameModeMetadata(String string) {
        try {
            return RealmsSlotOptionsScreen.GAME_MODES[Integer.parseInt(string)];
        } catch (Exception exception) {
            return new TextComponent("UNKNOWN");
        }
    }

    @Environment(value=EnvType.CLIENT)
    class BackupInfoList
    extends ObjectSelectionList<BackupInfoListEntry> {
        public BackupInfoList(Minecraft minecraft) {
            super(minecraft, RealmsBackupInfoScreen.this.width, RealmsBackupInfoScreen.this.height, 32, RealmsBackupInfoScreen.this.height - 64, 36);
            this.setRenderSelection(false);
            if (((RealmsBackupInfoScreen)RealmsBackupInfoScreen.this).backup.changeList != null) {
                ((RealmsBackupInfoScreen)RealmsBackupInfoScreen.this).backup.changeList.forEach((string, string2) -> this.addEntry(new BackupInfoListEntry((String)string, (String)string2)));
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    class BackupInfoListEntry
    extends ObjectSelectionList.Entry<BackupInfoListEntry> {
        private final String key;
        private final String value;

        public BackupInfoListEntry(String string, String string2) {
            this.key = string;
            this.value = string2;
        }

        @Override
        public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            Font font = ((RealmsBackupInfoScreen)RealmsBackupInfoScreen.this).minecraft.font;
            RealmsBackupInfoScreen.this.drawString(poseStack, font, this.key, k, j, 0xA0A0A0);
            RealmsBackupInfoScreen.this.drawString(poseStack, font, RealmsBackupInfoScreen.this.checkForSpecificMetadata(this.key, this.value), k, j + 12, 0xFFFFFF);
        }
    }
}

