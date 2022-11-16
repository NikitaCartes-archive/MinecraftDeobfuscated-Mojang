package net.minecraft.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class CenteredStringWidget extends AbstractWidget {
	private int color = 16777215;
	private final Font font;

	public CenteredStringWidget(Component component, Font font) {
		this(0, 0, font.width(component.getVisualOrderText()), 9, component, font);
	}

	public CenteredStringWidget(int i, int j, Component component, Font font) {
		this(0, 0, i, j, component, font);
	}

	public CenteredStringWidget(int i, int j, int k, int l, Component component, Font font) {
		super(i, j, k, l, component);
		this.font = font;
		this.active = false;
	}

	public CenteredStringWidget color(int i) {
		this.color = i;
		return this;
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
	}

	@Override
	public void renderButton(PoseStack poseStack, int i, int j, float f) {
		drawCenteredString(poseStack, this.font, this.getMessage(), this.getX() + this.getWidth() / 2, this.getY() + (this.getHeight() - 9) / 2, this.color);
	}
}
