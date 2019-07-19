package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.gui.RealmsConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsConfirmResultListener;
import net.minecraft.realms.RealmsScreen;

@Environment(EnvType.CLIENT)
public class RealmsLongConfirmationScreen extends RealmsScreen {
	private final RealmsLongConfirmationScreen.Type type;
	private final String line2;
	private final String line3;
	protected final RealmsConfirmResultListener listener;
	protected final String yesButton;
	protected final String noButton;
	private final String okButton;
	protected final int id;
	private final boolean yesNoQuestion;

	public RealmsLongConfirmationScreen(
		RealmsConfirmResultListener realmsConfirmResultListener, RealmsLongConfirmationScreen.Type type, String string, String string2, boolean bl, int i
	) {
		this.listener = realmsConfirmResultListener;
		this.id = i;
		this.type = type;
		this.line2 = string;
		this.line3 = string2;
		this.yesNoQuestion = bl;
		this.yesButton = getLocalizedString("gui.yes");
		this.noButton = getLocalizedString("gui.no");
		this.okButton = getLocalizedString("mco.gui.ok");
	}

	@Override
	public void init() {
		Realms.narrateNow(this.type.text, this.line2, this.line3);
		if (this.yesNoQuestion) {
			this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 105, RealmsConstants.row(8), 100, 20, this.yesButton) {
				@Override
				public void onPress() {
					RealmsLongConfirmationScreen.this.listener.confirmResult(true, RealmsLongConfirmationScreen.this.id);
				}
			});
			this.buttonsAdd(new RealmsButton(1, this.width() / 2 + 5, RealmsConstants.row(8), 100, 20, this.noButton) {
				@Override
				public void onPress() {
					RealmsLongConfirmationScreen.this.listener.confirmResult(false, RealmsLongConfirmationScreen.this.id);
				}
			});
		} else {
			this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 50, RealmsConstants.row(8), 100, 20, this.okButton) {
				@Override
				public void onPress() {
					RealmsLongConfirmationScreen.this.listener.confirmResult(true, RealmsLongConfirmationScreen.this.id);
				}
			});
		}
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 256) {
			this.listener.confirmResult(false, this.id);
			return true;
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		this.drawCenteredString(this.type.text, this.width() / 2, RealmsConstants.row(2), this.type.colorCode);
		this.drawCenteredString(this.line2, this.width() / 2, RealmsConstants.row(4), 16777215);
		this.drawCenteredString(this.line3, this.width() / 2, RealmsConstants.row(6), 16777215);
		super.render(i, j, f);
	}

	@Environment(EnvType.CLIENT)
	public static enum Type {
		Warning("Warning!", 16711680),
		Info("Info!", 8226750);

		public final int colorCode;
		public final String text;

		private Type(String string2, int j) {
			this.text = string2;
			this.colorCode = j;
		}
	}
}
