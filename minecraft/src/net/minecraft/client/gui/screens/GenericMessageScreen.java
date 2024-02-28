package net.minecraft.client.gui.screens;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.FocusableTextWidget;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class GenericMessageScreen extends Screen {
	@Nullable
	private FocusableTextWidget textWidget;

	public GenericMessageScreen(Component component) {
		super(component);
	}

	@Override
	protected void init() {
		this.textWidget = this.addRenderableWidget(new FocusableTextWidget(this.width, this.title, this.font, 12));
		this.repositionElements();
	}

	@Override
	protected void repositionElements() {
		if (this.textWidget != null) {
			this.textWidget.containWithin(this.width);
			this.textWidget.setPosition(this.width / 2 - this.textWidget.getWidth() / 2, this.height / 2 - 9 / 2);
		}
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
	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
		this.renderPanorama(guiGraphics, f);
		this.renderBlurredBackground(f);
		this.renderMenuBackground(guiGraphics);
	}
}
