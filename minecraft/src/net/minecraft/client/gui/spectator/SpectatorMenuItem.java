package net.minecraft.client.gui.spectator;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public interface SpectatorMenuItem {
	void selectItem(SpectatorMenu spectatorMenu);

	Component getName();

	void renderIcon(float f, int i);

	boolean isEnabled();
}
