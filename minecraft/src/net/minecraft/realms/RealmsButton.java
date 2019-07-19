package net.minecraft.realms;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public abstract class RealmsButton extends AbstractRealmsButton<RealmsButtonProxy> {
	protected static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
	private final int id;
	private final RealmsButtonProxy proxy;

	public RealmsButton(int i, int j, int k, String string) {
		this(i, j, k, 200, 20, string);
	}

	public RealmsButton(int i, int j, int k, int l, int m, String string) {
		this.id = i;
		this.proxy = new RealmsButtonProxy(this, j, k, string, l, m, button -> this.onPress());
	}

	public RealmsButtonProxy getProxy() {
		return this.proxy;
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

	public int x() {
		return this.proxy.x;
	}

	public void renderBg(int i, int j) {
	}

	public int getYImage(boolean bl) {
		return this.proxy.getSuperYImage(bl);
	}

	public abstract void onPress();

	public void onRelease(double d, double e) {
	}

	public void renderButton(int i, int j, float f) {
		this.getProxy().superRenderButton(i, j, f);
	}

	public void drawCenteredString(String string, int i, int j, int k) {
		this.getProxy().drawCenteredString(Minecraft.getInstance().font, string, i, j, k);
	}
}
