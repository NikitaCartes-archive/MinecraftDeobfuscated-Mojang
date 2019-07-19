package net.minecraft.realms;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;

@Environment(EnvType.CLIENT)
public class RealmsSliderButtonProxy extends AbstractSliderButton implements RealmsAbstractButtonProxy<RealmsSliderButton> {
	private final RealmsSliderButton button;

	public RealmsSliderButtonProxy(RealmsSliderButton realmsSliderButton, int i, int j, int k, int l, double d) {
		super(i, j, k, l, d);
		this.button = realmsSliderButton;
	}

	@Override
	public boolean active() {
		return this.active;
	}

	@Override
	public void active(boolean bl) {
		this.active = bl;
	}

	@Override
	public boolean isVisible() {
		return this.visible;
	}

	@Override
	public void setVisible(boolean bl) {
		this.visible = bl;
	}

	@Override
	public void setMessage(String string) {
		super.setMessage(string);
	}

	@Override
	public int getWidth() {
		return super.getWidth();
	}

	public int y() {
		return this.y;
	}

	@Override
	public void onClick(double d, double e) {
		this.button.onClick(d, e);
	}

	@Override
	public void onRelease(double d, double e) {
		this.button.onRelease(d, e);
	}

	@Override
	public void updateMessage() {
		this.button.updateMessage();
	}

	@Override
	public void applyValue() {
		this.button.applyValue();
	}

	public double getValue() {
		return this.value;
	}

	@Override
	public void setValue(double d) {
		this.value = d;
	}

	@Override
	public void renderBg(Minecraft minecraft, int i, int j) {
		super.renderBg(minecraft, i, j);
	}

	public RealmsSliderButton getButton() {
		return this.button;
	}

	@Override
	public int getYImage(boolean bl) {
		return this.button.getYImage(bl);
	}

	public int getSuperYImage(boolean bl) {
		return super.getYImage(bl);
	}

	public int getHeight() {
		return this.height;
	}
}
