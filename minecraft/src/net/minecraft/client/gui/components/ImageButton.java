package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class ImageButton extends Button {
	protected final ResourceLocation resourceLocation;
	protected final int xTexStart;
	protected final int yTexStart;
	protected final int yDiffTex;
	protected final int textureWidth;
	protected final int textureHeight;

	public ImageButton(int i, int j, int k, int l, int m, int n, ResourceLocation resourceLocation, Button.OnPress onPress) {
		this(i, j, k, l, m, n, l, resourceLocation, 256, 256, onPress);
	}

	public ImageButton(int i, int j, int k, int l, int m, int n, int o, ResourceLocation resourceLocation, Button.OnPress onPress) {
		this(i, j, k, l, m, n, o, resourceLocation, 256, 256, onPress);
	}

	public ImageButton(int i, int j, int k, int l, int m, int n, int o, ResourceLocation resourceLocation, int p, int q, Button.OnPress onPress) {
		this(i, j, k, l, m, n, o, resourceLocation, p, q, onPress, CommonComponents.EMPTY);
	}

	public ImageButton(
		int i, int j, int k, int l, int m, int n, int o, ResourceLocation resourceLocation, int p, int q, Button.OnPress onPress, Component component
	) {
		super(i, j, k, l, component, onPress, DEFAULT_NARRATION);
		this.textureWidth = p;
		this.textureHeight = q;
		this.xTexStart = m;
		this.yTexStart = n;
		this.yDiffTex = o;
		this.resourceLocation = resourceLocation;
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
		this.renderTexture(
			guiGraphics,
			this.resourceLocation,
			this.getX(),
			this.getY(),
			this.xTexStart,
			this.yTexStart,
			this.yDiffTex,
			this.width,
			this.height,
			this.textureWidth,
			this.textureHeight
		);
	}
}
