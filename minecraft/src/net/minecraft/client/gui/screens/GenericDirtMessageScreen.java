package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class GenericDirtMessageScreen extends Screen {
	public GenericDirtMessageScreen(Component component) {
		super(component);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	protected boolean shouldNarrateNavigation() {
		return false;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 70, 16777215);
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
		this.renderDirtBackground(guiGraphics);
	}
}
