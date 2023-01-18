package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;

@Environment(EnvType.CLIENT)
public class RealmsClientOutdatedScreen extends RealmsScreen {
	private static final Component INCOMPATIBLE_TITLE = Component.translatable("mco.client.incompatible.title");
	private static final Component[] INCOMPATIBLE_MESSAGES_SNAPSHOT = new Component[]{
		Component.translatable("mco.client.incompatible.msg.line1"),
		Component.translatable("mco.client.incompatible.msg.line2"),
		Component.translatable("mco.client.incompatible.msg.line3")
	};
	private static final Component[] INCOMPATIBLE_MESSAGES = new Component[]{
		Component.translatable("mco.client.incompatible.msg.line1"), Component.translatable("mco.client.incompatible.msg.line2")
	};
	private final Screen lastScreen;

	public RealmsClientOutdatedScreen(Screen screen) {
		super(INCOMPATIBLE_TITLE);
		this.lastScreen = screen;
	}

	@Override
	public void init() {
		this.addRenderableWidget(
			Button.builder(CommonComponents.GUI_BACK, button -> this.minecraft.setScreen(this.lastScreen)).bounds(this.width / 2 - 100, row(12), 200, 20).build()
		);
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, row(3), 16711680);
		Component[] components = this.getMessages();

		for (int k = 0; k < components.length; k++) {
			drawCenteredString(poseStack, this.font, components[k], this.width / 2, row(5) + k * 12, 16777215);
		}

		super.render(poseStack, i, j, f);
	}

	private Component[] getMessages() {
		return SharedConstants.getCurrentVersion().isStable() ? INCOMPATIBLE_MESSAGES : INCOMPATIBLE_MESSAGES_SNAPSHOT;
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i != 257 && i != 335 && i != 256) {
			return super.keyPressed(i, j, k);
		} else {
			this.minecraft.setScreen(this.lastScreen);
			return true;
		}
	}
}
