package net.minecraft.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class TextAndImageButton extends Button {
	protected final ResourceLocation resourceLocation;
	protected final int xTexStart;
	protected final int yTexStart;
	protected final int yDiffTex;
	protected final int textureWidth;
	protected final int textureHeight;
	private final int xOffset;
	private final int yOffset;
	private final int usedTextureWidth;
	private final int usedTextureHeight;

	TextAndImageButton(
		Component component, int i, int j, int k, int l, int m, int n, int o, int p, int q, ResourceLocation resourceLocation, Button.OnPress onPress
	) {
		super(0, 0, 150, 20, component, onPress, DEFAULT_NARRATION);
		this.textureWidth = p;
		this.textureHeight = q;
		this.xTexStart = i;
		this.yTexStart = j;
		this.yDiffTex = m;
		this.resourceLocation = resourceLocation;
		this.xOffset = k;
		this.yOffset = l;
		this.usedTextureWidth = n;
		this.usedTextureHeight = o;
	}

	@Override
	public void renderWidget(PoseStack poseStack, int i, int j, float f) {
		super.renderWidget(poseStack, i, j, f);
		this.renderTexture(
			poseStack,
			this.resourceLocation,
			this.getXOffset(),
			this.getYOffset(),
			this.xTexStart,
			this.yTexStart,
			this.yDiffTex,
			this.usedTextureWidth,
			this.usedTextureHeight,
			this.textureWidth,
			this.textureHeight
		);
	}

	@Override
	public void renderString(PoseStack poseStack, Font font, int i) {
		int j = this.getX() + 2;
		int k = this.getX() + this.getWidth() - this.usedTextureWidth - 6;
		renderScrollingString(poseStack, font, this.getMessage(), j, this.getY(), k, this.getY() + this.getHeight(), i);
	}

	private int getXOffset() {
		return this.getX() + (this.width / 2 - this.usedTextureWidth / 2) + this.xOffset;
	}

	private int getYOffset() {
		return this.getY() + this.yOffset;
	}

	public static TextAndImageButton.Builder builder(Component component, ResourceLocation resourceLocation, Button.OnPress onPress) {
		return new TextAndImageButton.Builder(component, resourceLocation, onPress);
	}

	@Environment(EnvType.CLIENT)
	public static class Builder {
		private final Component message;
		private final ResourceLocation resourceLocation;
		private final Button.OnPress onPress;
		private int xTexStart;
		private int yTexStart;
		private int yDiffTex;
		private int usedTextureWidth;
		private int usedTextureHeight;
		private int textureWidth;
		private int textureHeight;
		private int xOffset;
		private int yOffset;

		public Builder(Component component, ResourceLocation resourceLocation, Button.OnPress onPress) {
			this.message = component;
			this.resourceLocation = resourceLocation;
			this.onPress = onPress;
		}

		public TextAndImageButton.Builder texStart(int i, int j) {
			this.xTexStart = i;
			this.yTexStart = j;
			return this;
		}

		public TextAndImageButton.Builder offset(int i, int j) {
			this.xOffset = i;
			this.yOffset = j;
			return this;
		}

		public TextAndImageButton.Builder yDiffTex(int i) {
			this.yDiffTex = i;
			return this;
		}

		public TextAndImageButton.Builder usedTextureSize(int i, int j) {
			this.usedTextureWidth = i;
			this.usedTextureHeight = j;
			return this;
		}

		public TextAndImageButton.Builder textureSize(int i, int j) {
			this.textureWidth = i;
			this.textureHeight = j;
			return this;
		}

		public TextAndImageButton build() {
			return new TextAndImageButton(
				this.message,
				this.xTexStart,
				this.yTexStart,
				this.xOffset,
				this.yOffset,
				this.yDiffTex,
				this.usedTextureWidth,
				this.usedTextureHeight,
				this.textureWidth,
				this.textureHeight,
				this.resourceLocation,
				this.onPress
			);
		}
	}
}
