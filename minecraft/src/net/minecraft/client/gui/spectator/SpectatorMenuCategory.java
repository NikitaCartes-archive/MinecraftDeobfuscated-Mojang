package net.minecraft.client.gui.spectator;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public interface SpectatorMenuCategory {
	List<SpectatorMenuItem> getItems();

	Component getPrompt();
}
