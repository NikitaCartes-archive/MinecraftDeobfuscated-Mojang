package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class ImageButton extends Button {
	private final ResourceLocation resourceLocation;
	private final int xTexStart;
	private final int yTexStart;
	private final int yDiffTex;
	private final int textureWidth;
	private final int textureHeight;

	public ImageButton(int i, int j, int k, int l, int m, int n, int o, ResourceLocation resourceLocation, Button.OnPress onPress) {
		this(i, j, k, l, m, n, o, resourceLocation, 256, 256, onPress);
	}

	public ImageButton(int i, int j, int k, int l, int m, int n, int o, ResourceLocation resourceLocation, int p, int q, Button.OnPress onPress) {
		this(i, j, k, l, m, n, o, resourceLocation, p, q, onPress, "");
	}

	public ImageButton(int i, int j, int k, int l, int m, int n, int o, ResourceLocation resourceLocation, int p, int q, Button.OnPress onPress, String string) {
		super(i, j, k, l, string, onPress);
		this.textureWidth = p;
		this.textureHeight = q;
		this.xTexStart = m;
		this.yTexStart = n;
		this.yDiffTex = o;
		this.resourceLocation = resourceLocation;
	}

	public void setPosition(int i, int j) {
		this.x = i;
		this.y = j;
	}

	@Override
	public void renderButton(int i, int j, float f) {
		Minecraft minecraft = Minecraft.getInstance();
		minecraft.getTextureManager().bind(this.resourceLocation);
		RenderSystem.disableDepthTest();
		int k = this.yTexStart;
		if (this.isHovered()) {
			k += this.yDiffTex;
		}

		blit(this.x, this.y, (float)this.xTexStart, (float)k, this.width, this.height, this.textureWidth, this.textureHeight);
		RenderSystem.enableDepthTest();
	}
}
