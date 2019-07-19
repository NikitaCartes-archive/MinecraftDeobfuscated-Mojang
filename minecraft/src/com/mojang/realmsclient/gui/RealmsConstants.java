package com.mojang.realmsclient.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RealmsConstants {
	public static int row(int i) {
		return 40 + i * 13;
	}
}
