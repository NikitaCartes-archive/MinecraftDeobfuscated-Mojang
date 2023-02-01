package net.minecraft.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class StringWidget extends AbstractWidget {
	private int color = 16777215;
	private final Font font;
	private float alignX = 0.5F;

	public StringWidget(Component component, Font font) {
		this(0, 0, font.width(component.getVisualOrderText()), 9, component, font);
	}

	public StringWidget(int i, int j, Component component, Font font) {
		this(0, 0, i, j, component, font);
	}

	public StringWidget(int i, int j, int k, int l, Component component, Font font) {
		super(i, j, k, l, component);
		this.font = font;
		this.active = false;
	}

	public StringWidget color(int i) {
		this.color = i;
		return this;
	}

	private StringWidget horizontalAlignment(float f) {
		this.alignX = f;
		return this;
	}

	public StringWidget alignLeft() {
		return this.horizontalAlignment(0.0F);
	}

	public StringWidget alignCenter() {
		return this.horizontalAlignment(0.5F);
	}

	public StringWidget alignRight() {
		return this.horizontalAlignment(1.0F);
	}

	@Override
	public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
	}

	@Override
	public void renderWidget(PoseStack poseStack, int i, int j, float f) {
		Component component = this.getMessage();
		int k = this.getX() + Math.round(this.alignX * (float)(this.getWidth() - this.font.width(component)));
		int l = this.getY() + (this.getHeight() - 9) / 2;
		drawString(poseStack, this.font, component, k, l, this.color);
	}
}
