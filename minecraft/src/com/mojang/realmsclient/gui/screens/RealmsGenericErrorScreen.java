package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.exception.RealmsServiceException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;

@Environment(EnvType.CLIENT)
public class RealmsGenericErrorScreen extends RealmsScreen {
	private final RealmsScreen nextScreen;
	private String line1;
	private String line2;

	public RealmsGenericErrorScreen(RealmsServiceException realmsServiceException, RealmsScreen realmsScreen) {
		this.nextScreen = realmsScreen;
		this.errorMessage(realmsServiceException);
	}

	public RealmsGenericErrorScreen(String string, RealmsScreen realmsScreen) {
		this.nextScreen = realmsScreen;
		this.errorMessage(string);
	}

	public RealmsGenericErrorScreen(String string, String string2, RealmsScreen realmsScreen) {
		this.nextScreen = realmsScreen;
		this.errorMessage(string, string2);
	}

	private void errorMessage(RealmsServiceException realmsServiceException) {
		if (realmsServiceException.errorCode == -1) {
			this.line1 = "An error occurred (" + realmsServiceException.httpResultCode + "):";
			this.line2 = realmsServiceException.httpResponseContent;
		} else {
			this.line1 = "Realms (" + realmsServiceException.errorCode + "):";
			String string = "mco.errorMessage." + realmsServiceException.errorCode;
			String string2 = getLocalizedString(string);
			this.line2 = string2.equals(string) ? realmsServiceException.errorMsg : string2;
		}
	}

	private void errorMessage(String string) {
		this.line1 = "An error occurred: ";
		this.line2 = string;
	}

	private void errorMessage(String string, String string2) {
		this.line1 = string;
		this.line2 = string2;
	}

	@Override
	public void init() {
		Realms.narrateNow(this.line1 + ": " + this.line2);
		this.buttonsAdd(new RealmsButton(10, this.width() / 2 - 100, this.height() - 52, 200, 20, "Ok") {
			@Override
			public void onPress() {
				Realms.setScreen(RealmsGenericErrorScreen.this.nextScreen);
			}
		});
	}

	@Override
	public void tick() {
		super.tick();
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		this.drawCenteredString(this.line1, this.width() / 2, 80, 16777215);
		this.drawCenteredString(this.line2, this.width() / 2, 100, 16711680);
		super.render(i, j, f);
	}
}
