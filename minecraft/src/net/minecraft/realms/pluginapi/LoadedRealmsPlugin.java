package net.minecraft.realms.pluginapi;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.realms.RealmsScreen;

@Environment(EnvType.CLIENT)
public interface LoadedRealmsPlugin {
	RealmsScreen getMainScreen(RealmsScreen realmsScreen);

	RealmsScreen getNotificationsScreen(RealmsScreen realmsScreen);
}
