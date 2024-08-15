package net.minecraft.client.gui.screens.recipebook;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface SlotSelectTime {
	int currentIndex();
}
