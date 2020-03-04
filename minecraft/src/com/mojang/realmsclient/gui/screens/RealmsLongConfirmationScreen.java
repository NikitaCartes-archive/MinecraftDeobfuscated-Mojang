package com.mojang.realmsclient.gui.screens;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsScreen;

@Environment(EnvType.CLIENT)
public class RealmsLongConfirmationScreen extends RealmsScreen {
	private final RealmsLongConfirmationScreen.Type type;
	private final String line2;
	private final String line3;
	protected final BooleanConsumer callback;
	protected final String yesButton;
	protected final String noButton;
	private final String okButton;
	private final boolean yesNoQuestion;

	public RealmsLongConfirmationScreen(BooleanConsumer booleanConsumer, RealmsLongConfirmationScreen.Type type, String string, String string2, boolean bl) {
		this.callback = booleanConsumer;
		this.type = type;
		this.line2 = string;
		this.line3 = string2;
		this.yesNoQuestion = bl;
		this.yesButton = I18n.get("gui.yes");
		this.noButton = I18n.get("gui.no");
		this.okButton = I18n.get("mco.gui.ok");
	}

	@Override
	public void init() {
		NarrationHelper.now(this.type.text, this.line2, this.line3);
		if (this.yesNoQuestion) {
			this.addButton(new Button(this.width / 2 - 105, row(8), 100, 20, this.yesButton, button -> this.callback.accept(true)));
			this.addButton(new Button(this.width / 2 + 5, row(8), 100, 20, this.noButton, button -> this.callback.accept(false)));
		} else {
			this.addButton(new Button(this.width / 2 - 50, row(8), 100, 20, this.okButton, button -> this.callback.accept(true)));
		}
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 256) {
			this.callback.accept(false);
			return true;
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		this.drawCenteredString(this.font, this.type.text, this.width / 2, row(2), this.type.colorCode);
		this.drawCenteredString(this.font, this.line2, this.width / 2, row(4), 16777215);
		this.drawCenteredString(this.font, this.line3, this.width / 2, row(6), 16777215);
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
