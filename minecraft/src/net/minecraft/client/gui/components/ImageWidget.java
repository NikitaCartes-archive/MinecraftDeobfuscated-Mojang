package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class ImageWidget extends AbstractWidget {
	private final ResourceLocation imageLocation;

	public ImageWidget(int i, int j, ResourceLocation resourceLocation) {
		this(0, 0, i, j, resourceLocation);
	}

	public ImageWidget(int i, int j, int k, int l, ResourceLocation resourceLocation) {
		super(i, j, k, l, Component.empty());
		this.imageLocation = resourceLocation;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
		int k = this.getWidth();
		int l = this.getHeight();
		guiGraphics.blit(this.imageLocation, this.getX(), this.getY(), 0.0F, 0.0F, k, l, k, l);
	}
}
