package net.minecraft.client.gui.navigation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class CommonInputs {
	public static boolean selected(int i) {
		return i == 257 || i == 32 || i == 335;
	}
}
