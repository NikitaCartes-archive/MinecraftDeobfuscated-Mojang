package com.mojang.realmsclient.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
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
		this.addButton(new Button(this.width / 2 - 100, row(12), 200, 20, I18n.get("gui.back"), button -> this.minecraft.setScreen(this.lastScreen)));
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		String string = I18n.get(this.outdated ? "mco.client.outdated.title" : "mco.client.incompatible.title");
		this.drawCenteredString(this.font, string, this.width / 2, row(3), 16711680);
		int k = this.outdated ? 2 : 3;

		for (int l = 0; l < k; l++) {
			String string2 = (this.outdated ? "mco.client.outdated.msg.line" : "mco.client.incompatible.msg.line") + (l + 1);
			String string3 = I18n.get(string2);
			this.drawCenteredString(this.font, string3, this.width / 2, row(5) + l * 12, 16777215);
		}

		super.render(i, j, f);
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
