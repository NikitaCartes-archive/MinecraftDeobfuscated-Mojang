package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsScreen;

@Environment(EnvType.CLIENT)
public class RealmsClientOutdatedScreen extends RealmsScreen {
	private final Screen lastScreen;
	private final boolean outdated;

	public RealmsClientOutdatedScreen(Screen screen, boolean bl) {
		this.lastScreen = screen;
		this.outdated = bl;
	}

	@Override
	public void init() {
		this.addButton(new Button(this.width / 2 - 100, row(12), 200, 20, CommonComponents.GUI_BACK, button -> this.minecraft.setScreen(this.lastScreen)));
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		Component component = new TranslatableComponent(this.outdated ? "mco.client.outdated.title" : "mco.client.incompatible.title");
		this.drawCenteredString(poseStack, this.font, component, this.width / 2, row(3), 16711680);
		int k = this.outdated ? 2 : 3;

		for (int l = 0; l < k; l++) {
			String string = (this.outdated ? "mco.client.outdated.msg.line" : "mco.client.incompatible.msg.line") + (l + 1);
			this.drawCenteredString(poseStack, this.font, new TranslatableComponent(string), this.width / 2, row(5) + l * 12, 16777215);
		}

		super.render(poseStack, i, j, f);
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
