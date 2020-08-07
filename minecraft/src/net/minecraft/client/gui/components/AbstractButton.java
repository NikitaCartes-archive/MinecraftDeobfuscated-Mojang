package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public abstract class AbstractButton extends AbstractWidget {
	public AbstractButton(int i, int j, int k, int l, Component component) {
		super(i, j, k, l, component);
	}

	public abstract void onPress();

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
