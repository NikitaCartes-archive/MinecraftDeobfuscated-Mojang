package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class OptionsSubScreen extends Screen {
	protected final Screen lastScreen;
	protected final Options options;

	public OptionsSubScreen(Screen screen, Options options, Component component) {
		super(component);
		this.lastScreen = screen;
		this.options = options;
	}

	@Override
	public void removed() {
		this.minecraft.options.save();
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.lastScreen);
	}

	protected void basicListRender(GuiGraphics guiGraphics, OptionsList optionsList, int i, int j, float f) {
		this.renderBackground(guiGraphics);
		optionsList.render(guiGraphics, i, j, f);
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215);
		super.render(guiGraphics, i, j, f);
	}
}
