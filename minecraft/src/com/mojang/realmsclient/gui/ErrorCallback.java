package com.mojang.realmsclient.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public interface ErrorCallback {
	void error(Component component);

	default void error(String string) {
		this.error(Component.literal(string));
	}
}
