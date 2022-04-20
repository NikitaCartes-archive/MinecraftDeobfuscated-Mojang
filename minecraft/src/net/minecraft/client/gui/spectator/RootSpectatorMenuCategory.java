package net.minecraft.client.gui.spectator;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.spectator.categories.TeleportToPlayerMenuCategory;
import net.minecraft.client.gui.spectator.categories.TeleportToTeamMenuCategory;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class RootSpectatorMenuCategory implements SpectatorMenuCategory {
	private static final Component PROMPT_TEXT = Component.translatable("spectatorMenu.root.prompt");
	private final List<SpectatorMenuItem> items = Lists.<SpectatorMenuItem>newArrayList();

	public RootSpectatorMenuCategory() {
		this.items.add(new TeleportToPlayerMenuCategory());
		this.items.add(new TeleportToTeamMenuCategory());
	}

	@Override
	public List<SpectatorMenuItem> getItems() {
		return this.items;
	}

	@Override
	public Component getPrompt() {
		return PROMPT_TEXT;
	}
}
