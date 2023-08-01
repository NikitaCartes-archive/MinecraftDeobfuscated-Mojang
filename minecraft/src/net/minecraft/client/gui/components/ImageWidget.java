package net.minecraft.client.gui.components;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public abstract class ImageWidget extends AbstractWidget {
	ImageWidget(int i, int j, int k, int l) {
		super(i, j, k, l, CommonComponents.EMPTY);
	}

	public static ImageWidget texture(int i, int j, ResourceLocation resourceLocation, int k, int l) {
		return new ImageWidget.Texture(0, 0, i, j, resourceLocation, k, l);
	}

	public static ImageWidget sprite(int i, int j, ResourceLocation resourceLocation) {
		return new ImageWidget.Sprite(0, 0, i, j, resourceLocation);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
	}

	@Override
	public void playDownSound(SoundManager soundManager) {
	}

	@Override
	public boolean isActive() {
		return false;
	}

	@Nullable
	@Override
	public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
		return null;
	}

	@Environment(EnvType.CLIENT)
	static class Sprite extends ImageWidget {
		private final ResourceLocation sprite;

		public Sprite(int i, int j, int k, int l, ResourceLocation resourceLocation) {
			super(i, j, k, l);
			this.sprite = resourceLocation;
		}

		@Override
		public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
			guiGraphics.blitSprite(this.sprite, this.getX(), this.getY(), this.getWidth(), this.getHeight());
		}
	}

	@Environment(EnvType.CLIENT)
	static class Texture extends ImageWidget {
		private final ResourceLocation texture;
		private final int textureWidth;
		private final int textureHeight;

		public Texture(int i, int j, int k, int l, ResourceLocation resourceLocation, int m, int n) {
			super(i, j, k, l);
			this.texture = resourceLocation;
			this.textureWidth = m;
			this.textureHeight = n;
		}

		@Override
		protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
			guiGraphics.blit(
				this.texture,
				this.getX(),
				this.getY(),
				this.getWidth(),
				this.getHeight(),
				0.0F,
				0.0F,
				this.getWidth(),
				this.getHeight(),
				this.textureWidth,
				this.textureHeight
			);
		}
	}
}
