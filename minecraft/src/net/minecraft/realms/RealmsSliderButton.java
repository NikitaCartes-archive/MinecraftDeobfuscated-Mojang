package net.minecraft.realms;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public abstract class RealmsSliderButton extends AbstractRealmsButton<RealmsSliderButtonProxy> {
	protected static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
	private final int id;
	private final RealmsSliderButtonProxy proxy;
	private final double minValue;
	private final double maxValue;

	public RealmsSliderButton(int i, int j, int k, int l, int m, double d, double e) {
		this.id = i;
		this.minValue = d;
		this.maxValue = e;
		this.proxy = new RealmsSliderButtonProxy(this, j, k, l, 20, this.toPct((double)m));
		this.getProxy().setMessage(this.getMessage());
	}

	public String getMessage() {
		return "";
	}

	public double toPct(double d) {
		return Mth.clamp((this.clamp(d) - this.minValue) / (this.maxValue - this.minValue), 0.0, 1.0);
	}

	public double toValue(double d) {
		return this.clamp(Mth.lerp(Mth.clamp(d, 0.0, 1.0), this.minValue, this.maxValue));
	}

	public double clamp(double d) {
		return Mth.clamp(d, this.minValue, this.maxValue);
	}

	public int getYImage(boolean bl) {
		return 0;
	}

	public void onClick(double d, double e) {
	}

	public void onRelease(double d, double e) {
	}

	public RealmsSliderButtonProxy getProxy() {
		return this.proxy;
	}

	public double getValue() {
		return this.proxy.getValue();
	}

	public void setValue(double d) {
		this.proxy.setValue(d);
	}

	public int id() {
		return this.id;
	}

	public void setMessage(String string) {
		this.proxy.setMessage(string);
	}

	public int getWidth() {
		return this.proxy.getWidth();
	}

	public int getHeight() {
		return this.proxy.getHeight();
	}

	public int y() {
		return this.proxy.y();
	}

	public abstract void applyValue();

	public void updateMessage() {
		this.proxy.setMessage(this.getMessage());
	}
}
