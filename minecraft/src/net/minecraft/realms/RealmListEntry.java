package net.minecraft.realms;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.ObjectSelectionList;

@Environment(EnvType.CLIENT)
public abstract class RealmListEntry extends ObjectSelectionList.Entry<RealmListEntry> {
	@Override
	public abstract void render(int i, int j, int k, int l, int m, int n, int o, boolean bl, float f);

	@Override
	public boolean mouseClicked(double d, double e, int i) {
		return false;
	}
}
