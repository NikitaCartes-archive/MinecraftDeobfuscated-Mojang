package net.minecraft.client.gui.screens.achievement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface StatsUpdateListener {
	String[] LOADING_SYMBOLS = new String[]{"oooooo", "Oooooo", "oOoooo", "ooOooo", "oooOoo", "ooooOo", "oooooO"};

	void onStatsUpdated();
}
