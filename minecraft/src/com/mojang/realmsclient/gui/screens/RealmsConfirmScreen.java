package com.mojang.realmsclient.gui.screens;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.realms.RealmsScreen;

@Environment(EnvType.CLIENT)
public class RealmsConfirmScreen extends RealmsScreen {
	protected BooleanConsumer callback;
	protected String title1;
	private final String title2;
	protected String yesButton = I18n.get("gui.yes");
	protected String noButton = I18n.get("gui.no");
	private int delayTicker;

	public RealmsConfirmScreen(BooleanConsumer booleanConsumer, String string, String string2) {
		this.callback = booleanConsumer;
		this.title1 = string;
		this.title2 = string2;
	}

	@Override
	public void init() {
		this.addButton(new Button(this.width / 2 - 105, row(9), 100, 20, this.yesButton, button -> this.callback.accept(true)));
		this.addButton(new Button(this.width / 2 + 5, row(9), 100, 20, this.noButton, button -> this.callback.accept(false)));
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		this.drawCenteredString(this.font, this.title1, this.width / 2, row(3), 16777215);
		this.drawCenteredString(this.font, this.title2, this.width / 2, row(5), 16777215);
		super.render(i, j, f);
	}

	@Override
	public void tick() {
		super.tick();
		if (--this.delayTicker == 0) {
			for (AbstractWidget abstractWidget : this.buttons) {
				abstractWidget.active = true;
			}
		}
	}
}
