package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Options;

@Environment(EnvType.CLIENT)
public abstract class AbstractOptionSliderButton extends AbstractSliderButton {
	protected final Options options;

	protected AbstractOptionSliderButton(Options options, int i, int j, int k, int l, double d) {
		super(i, j, k, l, "", d);
		this.options = options;
	}
}
