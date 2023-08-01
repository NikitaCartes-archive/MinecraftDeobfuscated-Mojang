package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class ImageButton extends Button {
	protected final WidgetSprites sprites;

	public ImageButton(int i, int j, int k, int l, WidgetSprites widgetSprites, Button.OnPress onPress) {
		this(i, j, k, l, widgetSprites, onPress, CommonComponents.EMPTY);
	}

	public ImageButton(int i, int j, int k, int l, WidgetSprites widgetSprites, Button.OnPress onPress, Component component) {
		super(i, j, k, l, component, onPress, DEFAULT_NARRATION);
		this.sprites = widgetSprites;
	}

	public ImageButton(int i, int j, WidgetSprites widgetSprites, Button.OnPress onPress, Component component) {
		this(0, 0, i, j, widgetSprites, onPress, component);
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
		ResourceLocation resourceLocation = this.sprites.get(this.isActive(), this.isHoveredOrFocused());
		guiGraphics.blitSprite(resourceLocation, this.getX(), this.getY(), this.width, this.height);
	}
}
