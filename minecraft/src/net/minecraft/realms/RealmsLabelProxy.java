package net.minecraft.realms;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.events.GuiEventListener;

@Environment(EnvType.CLIENT)
public class RealmsLabelProxy implements GuiEventListener {
	private final RealmsLabel label;

	public RealmsLabelProxy(RealmsLabel realmsLabel) {
		this.label = realmsLabel;
	}

	public RealmsLabel getLabel() {
		return this.label;
	}
}
