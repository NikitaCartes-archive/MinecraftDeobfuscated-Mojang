/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.spectator.categories;

import com.google.common.base.MoreObjects;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuCategory;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;

@Environment(value=EnvType.CLIENT)
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
        if (i < 0 || i >= this.items.size()) {
            return SpectatorMenu.EMPTY_SLOT;
        }
        return MoreObjects.firstNonNull(this.items.get(i), SpectatorMenu.EMPTY_SLOT);
    }

    public int getSelectedSlot() {
        return this.selection;
    }
}

