package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.gui.RealmsConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;

@Environment(EnvType.CLIENT)
public class RealmsClientOutdatedScreen extends RealmsScreen {
	private final RealmsScreen lastScreen;
	private final boolean outdated;

	public RealmsClientOutdatedScreen(RealmsScreen realmsScreen, boolean bl) {
		this.lastScreen = realmsScreen;
		this.outdated = bl;
	}

	@Override
	public void init() {
		this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 100, RealmsConstants.row(12), getLocalizedString("gui.back")) {
			@Override
			public void onPress() {
				Realms.setScreen(RealmsClientOutdatedScreen.this.lastScreen);
			}
		});
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		String string = getLocalizedString(this.outdated ? "mco.client.outdated.title" : "mco.client.incompatible.title");
		this.drawCenteredString(string, this.width() / 2, RealmsConstants.row(3), 16711680);
		int k = this.outdated ? 2 : 3;

		for (int l = 0; l < k; l++) {
			String string2 = getLocalizedString((this.outdated ? "mco.client.outdated.msg.line" : "mco.client.incompatible.msg.line") + (l + 1));
			this.drawCenteredString(string2, this.width() / 2, RealmsConstants.row(5) + l * 12, 16777215);
		}

		super.render(i, j, f);
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i != 257 && i != 335 && i != 256) {
			return super.keyPressed(i, j, k);
		} else {
			Realms.setScreen(this.lastScreen);
			return true;
		}
	}
}
