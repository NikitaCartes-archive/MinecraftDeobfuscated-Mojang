package net.minecraft.client.gui.screens.advancements;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum AdvancementWidgetType {
	OBTAINED(0),
	UNOBTAINED(1);

	private final int y;

	private AdvancementWidgetType(int j) {
		this.y = j;
	}

	public int getIndex() {
		return this.y;
	}
}
