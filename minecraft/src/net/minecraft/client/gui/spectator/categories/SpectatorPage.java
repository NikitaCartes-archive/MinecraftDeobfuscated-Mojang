package net.minecraft.client.gui.spectator.categories;

import com.google.common.base.MoreObjects;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;

@Environment(EnvType.CLIENT)
public class SpectatorPage {
	public static final int NO_SELECTION = -1;
	private final List<SpectatorMenuItem> items;
	private final int selection;

	public SpectatorPage(List<SpectatorMenuItem> list, int i) {
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
