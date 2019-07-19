package net.minecraft.client.gui.spectator;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface SpectatorMenuListener {
	void onSpectatorMenuClosed(SpectatorMenu spectatorMenu);
}
