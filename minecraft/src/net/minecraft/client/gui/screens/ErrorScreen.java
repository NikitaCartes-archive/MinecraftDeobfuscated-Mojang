package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class ErrorScreen extends Screen {
	private final Component message;

	public ErrorScreen(Component component, Component component2) {
		super(component);
		this.message = component2;
	}

	@Override
	protected void init() {
		super.init();
		this.addRenderableWidget(
			Button.builder(CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(null)).bounds(this.width / 2 - 100, 140, 200, 20).build()
		);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		guiGraphics.fillGradient(0, 0, this.width, this.height, -12574688, -11530224);
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 90, 16777215);
		guiGraphics.drawCenteredString(this.font, this.message, this.width / 2, 110, 16777215);
		super.render(guiGraphics, i, j, f);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}
}
