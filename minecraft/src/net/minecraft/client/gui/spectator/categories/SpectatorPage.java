package net.minecraft.client.gui.spectator.categories;

import com.google.common.base.MoreObjects;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuCategory;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;

@Environment(EnvType.CLIENT)
public class SpectatorPage {
	private final SpectatorMenuCategory category;
	private final List<SpectatorMenuItem> items;
	private final int selection;

	public SpectatorPage(SpectatorMenuCategory spectatorMenuCategory, List<SpectatorMenuItem> list, int i) {
		this.category = spectatorMenuCategory;
		this.items = list;
		this.selection = i;
	}

	public SpectatorMenuItem getItem(int i) {
		return i >= 0 && i < this.items.size() ? MoreObjects.firstNonNull((SpectatorMenuItem)this.items.get(i), SpectatorMenu.EMPTY_SLOT) : SpectatorMenu.EMPTY_SLOT;
	}

	public int getSelectedSlot() {
		return this.selection;
	}
}
