package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public abstract class AbstractButton extends AbstractWidget {
	protected static final int TEXT_MARGIN = 2;
	private static final WidgetSprites SPRITES = new WidgetSprites(
		ResourceLocation.withDefaultNamespace("widget/button"),
		ResourceLocation.withDefaultNamespace("widget/button_disabled"),
		ResourceLocation.withDefaultNamespace("widget/button_highlighted")
	);

	public AbstractButton(int i, int j, int k, int l, Component component) {
		super(i, j, k, l, component);
	}

	public abstract void onPress();

	@Override
	protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
		Minecraft minecraft = Minecraft.getInstance();
		guiGraphics.blitSprite(
			RenderType::guiTextured,
			SPRITES.get(this.active, this.isHoveredOrFocused()),
			this.getX(),
			this.getY(),
			this.getWidth(),
			this.getHeight(),
			ARGB.white(this.alpha)
		);
		int k = this.active ? 16777215 : 10526880;
		this.renderString(guiGraphics, minecraft.font, k | Mth.ceil(this.alpha * 255.0F) << 24);
	}

	public void renderString(GuiGraphics guiGraphics, Font font, int i) {
		this.renderScrollingString(guiGraphics, font, 2, i);
	}

	@Override
	public void onClick(double d, double e) {
		this.onPress();
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (!this.active || !this.visible) {
			return false;
		} else if (CommonInputs.selected(i)) {
			this.playDownSound(Minecraft.getInstance().getSoundManager());
			this.onPress();
			return true;
		} else {
			return false;
		}
	}
}
