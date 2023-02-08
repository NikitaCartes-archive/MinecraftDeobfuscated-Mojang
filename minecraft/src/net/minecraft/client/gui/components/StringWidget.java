package net.minecraft.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class StringWidget extends AbstractStringWidget {
	private float alignX = 0.5F;

	public StringWidget(Component component, Font font) {
		this(0, 0, font.width(component.getVisualOrderText()), 9, component, font);
	}

	public StringWidget(int i, int j, Component component, Font font) {
		this(0, 0, i, j, component, font);
	}

	public StringWidget(int i, int j, int k, int l, Component component, Font font) {
		super(i, j, k, l, component, font);
		this.active = false;
	}

	public StringWidget setColor(int i) {
		super.setColor(i);
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
	public void renderWidget(PoseStack poseStack, int i, int j, float f) {
		Component component = this.getMessage();
		Font font = this.getFont();
		int k = this.getX() + Math.round(this.alignX * (float)(this.getWidth() - font.width(component)));
		int l = this.getY() + (this.getHeight() - 9) / 2;
		drawString(poseStack, font, component, k, l, this.getColor());
	}
}
