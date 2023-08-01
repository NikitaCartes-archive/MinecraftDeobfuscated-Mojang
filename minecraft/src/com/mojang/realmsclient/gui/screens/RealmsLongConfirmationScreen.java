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
public class RealmsLongConfirmationScreen extends RealmsScreen {
	static final Component WARNING = Component.translatable("mco.warning");
	static final Component INFO = Component.translatable("mco.info");
	private final RealmsLongConfirmationScreen.Type type;
	private final Component line2;
	private final Component line3;
	protected final BooleanConsumer callback;
	private final boolean yesNoQuestion;

	public RealmsLongConfirmationScreen(
		BooleanConsumer booleanConsumer, RealmsLongConfirmationScreen.Type type, Component component, Component component2, boolean bl
	) {
		super(GameNarrator.NO_TITLE);
		this.callback = booleanConsumer;
		this.type = type;
		this.line2 = component;
		this.line3 = component2;
		this.yesNoQuestion = bl;
	}

	@Override
	public void init() {
		if (this.yesNoQuestion) {
			this.addRenderableWidget(
				Button.builder(CommonComponents.GUI_YES, button -> this.callback.accept(true)).bounds(this.width / 2 - 105, row(8), 100, 20).build()
			);
			this.addRenderableWidget(Button.builder(CommonComponents.GUI_NO, button -> this.callback.accept(false)).bounds(this.width / 2 + 5, row(8), 100, 20).build());
		} else {
			this.addRenderableWidget(Button.builder(CommonComponents.GUI_OK, button -> this.callback.accept(true)).bounds(this.width / 2 - 50, row(8), 100, 20).build());
		}
	}

	@Override
	public Component getNarrationMessage() {
		return CommonComponents.joinLines(this.type.text, this.line2, this.line3);
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
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		guiGraphics.drawCenteredString(this.font, this.type.text, this.width / 2, row(2), this.type.colorCode);
		guiGraphics.drawCenteredString(this.font, this.line2, this.width / 2, row(4), -1);
		guiGraphics.drawCenteredString(this.font, this.line3, this.width / 2, row(6), -1);
	}

	@Environment(EnvType.CLIENT)
	public static enum Type {
		WARNING(RealmsLongConfirmationScreen.WARNING, -65536),
		INFO(RealmsLongConfirmationScreen.INFO, 8226750);

		public final int colorCode;
		public final Component text;

		private Type(Component component, int j) {
			this.text = component;
			this.colorCode = j;
		}
	}
}
