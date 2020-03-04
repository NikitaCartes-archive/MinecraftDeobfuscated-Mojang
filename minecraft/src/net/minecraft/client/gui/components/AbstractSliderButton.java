package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public abstract class AbstractSliderButton extends AbstractWidget {
	protected double value;

	public AbstractSliderButton(int i, int j, int k, int l, String string, double d) {
		super(i, j, k, l, string);
		this.value = d;
	}

	@Override
	protected int getYImage(boolean bl) {
		return 0;
	}

	@Override
	protected String getNarrationMessage() {
		return I18n.get("gui.narrate.slider", this.getMessage());
	}

	@Override
	protected void renderBg(Minecraft minecraft, int i, int j) {
		minecraft.getTextureManager().bind(WIDGETS_LOCATION);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		int k = (this.isHovered() ? 2 : 1) * 20;
		this.blit(this.x + (int)(this.value * (double)(this.width - 8)), this.y, 0, 46 + k, 4, 20);
		this.blit(this.x + (int)(this.value * (double)(this.width - 8)) + 4, this.y, 196, 46 + k, 4, 20);
	}

	@Override
	public void onClick(double d, double e) {
		this.setValueFromMouse(d);
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		boolean bl = i == 263;
		if (bl || i == 262) {
			float f = bl ? -1.0F : 1.0F;
			this.setValue(this.value + (double)(f / (float)(this.width - 8)));
		}

		return false;
	}

	private void setValueFromMouse(double d) {
		this.setValue((d - (double)(this.x + 4)) / (double)(this.width - 8));
	}

	private void setValue(double d) {
		double e = this.value;
		this.value = Mth.clamp(d, 0.0, 1.0);
		if (e != this.value) {
			this.applyValue();
		}

		this.updateMessage();
	}

	@Override
	protected void onDrag(double d, double e, double f, double g) {
		this.setValueFromMouse(d);
		super.onDrag(d, e, f, g);
	}

	@Override
	public void playDownSound(SoundManager soundManager) {
	}

	@Override
	public void onRelease(double d, double e) {
		super.playDownSound(Minecraft.getInstance().getSoundManager());
	}

	protected abstract void updateMessage();

	protected abstract void applyValue();
}
