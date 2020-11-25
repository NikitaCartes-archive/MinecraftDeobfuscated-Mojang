package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.dto.Backup;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;

@Environment(EnvType.CLIENT)
public class RealmsBackupInfoScreen extends RealmsScreen {
	private static final Component TEXT_UNKNOWN = new TextComponent("UNKNOWN");
	private final Screen lastScreen;
	private final Backup backup;
	private RealmsBackupInfoScreen.BackupInfoList backupInfoList;

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
		this.addButton(
			new Button(this.width / 2 - 100, this.height / 4 + 120 + 24, 200, 20, CommonComponents.GUI_BACK, button -> this.minecraft.setScreen(this.lastScreen))
		);
		this.backupInfoList = new RealmsBackupInfoScreen.BackupInfoList(this.minecraft);
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
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		drawCenteredString(poseStack, this.font, "Changes from last backup", this.width / 2, 10, 16777215);
		this.backupInfoList.render(poseStack, i, j, f);
		super.render(poseStack, i, j, f);
	}

	private Component checkForSpecificMetadata(String string, String string2) {
		String string3 = string.toLowerCase(Locale.ROOT);
		if (string3.contains("game") && string3.contains("mode")) {
			return this.gameModeMetadata(string2);
		} else {
			return (Component)(string3.contains("game") && string3.contains("difficulty") ? this.gameDifficultyMetadata(string2) : new TextComponent(string2));
		}
	}

	private Component gameDifficultyMetadata(String string) {
		try {
			return ((Difficulty)RealmsSlotOptionsScreen.DIFFICULTIES.get(Integer.parseInt(string))).getDisplayName();
		} catch (Exception var3) {
			return TEXT_UNKNOWN;
		}
	}

	private Component gameModeMetadata(String string) {
		try {
			return ((GameType)RealmsSlotOptionsScreen.GAME_MODES.get(Integer.parseInt(string))).getShortDisplayName();
		} catch (Exception var3) {
			return TEXT_UNKNOWN;
		}
	}

	@Environment(EnvType.CLIENT)
	class BackupInfoList extends ObjectSelectionList<RealmsBackupInfoScreen.BackupInfoListEntry> {
		public BackupInfoList(Minecraft minecraft) {
			super(minecraft, RealmsBackupInfoScreen.this.width, RealmsBackupInfoScreen.this.height, 32, RealmsBackupInfoScreen.this.height - 64, 36);
			this.setRenderSelection(false);
			if (RealmsBackupInfoScreen.this.backup.changeList != null) {
				RealmsBackupInfoScreen.this.backup
					.changeList
					.forEach((string, string2) -> this.addEntry(RealmsBackupInfoScreen.this.new BackupInfoListEntry(string, string2)));
			}
		}
	}

	@Environment(EnvType.CLIENT)
	class BackupInfoListEntry extends ObjectSelectionList.Entry<RealmsBackupInfoScreen.BackupInfoListEntry> {
		private final String key;
		private final String value;

		public BackupInfoListEntry(String string, String string2) {
			this.key = string;
			this.value = string2;
		}

		@Override
		public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			Font font = RealmsBackupInfoScreen.this.minecraft.font;
			GuiComponent.drawString(poseStack, font, this.key, k, j, 10526880);
			GuiComponent.drawString(poseStack, font, RealmsBackupInfoScreen.this.checkForSpecificMetadata(this.key, this.value), k, j + 12, 16777215);
		}
	}
}
