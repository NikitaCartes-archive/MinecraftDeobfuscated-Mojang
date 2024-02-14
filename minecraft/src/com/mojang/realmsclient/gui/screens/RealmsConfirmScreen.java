package com.mojang.realmsclient.gui.screens;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;

@Environment(EnvType.CLIENT)
public class RealmsConfirmScreen extends RealmsScreen {
	protected BooleanConsumer callback;
	private final Component title1;
	private final Component title2;

	public RealmsConfirmScreen(BooleanConsumer booleanConsumer, Component component, Component component2) {
		super(GameNarrator.NO_TITLE);
		this.callback = booleanConsumer;
		this.title1 = component;
		this.title2 = component2;
	}

	@Override
	public void init() {
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_YES, button -> this.callback.accept(true)).bounds(this.width / 2 - 105, row(9), 100, 20).build());
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_NO, button -> this.callback.accept(false)).bounds(this.width / 2 + 5, row(9), 100, 20).build());
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		guiGraphics.drawCenteredString(this.font, this.title1, this.width / 2, row(3), -1);
		guiGraphics.drawCenteredString(this.font, this.title2, this.width / 2, row(5), -1);
	}
}
