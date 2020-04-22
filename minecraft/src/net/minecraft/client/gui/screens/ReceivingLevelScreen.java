package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.resources.language.I18n;

@Environment(EnvType.CLIENT)
public class ReceivingLevelScreen extends Screen {
	public ReceivingLevelScreen() {
		super(NarratorChatListener.NO_TITLE);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderDirtBackground(0);
		this.drawCenteredString(poseStack, this.font, I18n.get("multiplayer.downloadingTerrain"), this.width / 2, this.height / 2 - 50, 16777215);
		super.render(poseStack, i, j, f);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
