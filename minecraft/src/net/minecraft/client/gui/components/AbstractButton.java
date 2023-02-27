package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public abstract class AbstractButton extends AbstractWidget {
	protected static final int TEXTURE_Y_OFFSET = 46;
	protected static final int TEXTURE_WIDTH = 200;
	protected static final int TEXTURE_HEIGHT = 20;
	protected static final int TEXTURE_BORDER_X = 20;
	protected static final int TEXTURE_BORDER_Y = 4;
	protected static final int TEXT_MARGIN = 2;

	public AbstractButton(int i, int j, int k, int l, Component component) {
		super(i, j, k, l, component);
	}

	public abstract void onPress();

	@Override
	public void renderWidget(PoseStack poseStack, int i, int j, float f) {
		Minecraft minecraft = Minecraft.getInstance();
		RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		blitNineSliced(poseStack, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, this.getTextureY());
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		int k = this.active ? 16777215 : 10526880;
		this.renderString(poseStack, minecraft.font, k | Mth.ceil(this.alpha * 255.0F) << 24);
	}

	public void renderString(PoseStack poseStack, Font font, int i) {
		this.renderScrollingString(poseStack, font, 2, i);
	}

	private int getTextureY() {
		int i = 1;
		if (!this.active) {
			i = 0;
		} else if (this.isHoveredOrFocused()) {
			i = 2;
		}

		return 46 + i * 20;
	}

	@Override
	public void onClick(double d, double e) {
		this.onPress();
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (!this.active || !this.visible) {
			return false;
		} else if (i != 257 && i != 32 && i != 335) {
			return false;
		} else {
			this.playDownSound(Minecraft.getInstance().getSoundManager());
			this.onPress();
			return true;
		}
	}
}
