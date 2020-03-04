package com.mojang.realmsclient.gui.screens;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsScreen;

@Environment(EnvType.CLIENT)
public class RealmsParentalConsentScreen extends RealmsScreen {
	private final Screen nextScreen;

	public RealmsParentalConsentScreen(Screen screen) {
		this.nextScreen = screen;
	}

	@Override
	public void init() {
		NarrationHelper.now(I18n.get("mco.account.privacyinfo"));
		String string = I18n.get("mco.account.update");
		String string2 = I18n.get("gui.back");
		int i = Math.max(this.font.width(string), this.font.width(string2)) + 30;
		String string3 = I18n.get("mco.account.privacy.info");
		int j = (int)((double)this.font.width(string3) * 1.2);
		this.addButton(new Button(this.width / 2 - j / 2, row(11), j, 20, string3, button -> Util.getPlatform().openUri("https://minecraft.net/privacy/gdpr/")));
		this.addButton(new Button(this.width / 2 - (i + 5), row(13), i, 20, string, button -> Util.getPlatform().openUri("https://minecraft.net/update-account")));
		this.addButton(new Button(this.width / 2 + 5, row(13), i, 20, string2, button -> this.minecraft.setScreen(this.nextScreen)));
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		List<String> list = this.minecraft.font.split(I18n.get("mco.account.privacyinfo"), (int)Math.round((double)this.width * 0.9));
		int k = 15;

		for (String string : list) {
			this.drawCenteredString(this.font, string, this.width / 2, k, 16777215);
			k += 15;
		}

		super.render(i, j, f);
	}
}
