package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface TabOrderedElement {
	default int getTabOrderGroup() {
		return 0;
	}
}
