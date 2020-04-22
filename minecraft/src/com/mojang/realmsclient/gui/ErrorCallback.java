package com.mojang.realmsclient.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

@Environment(EnvType.CLIENT)
public interface ErrorCallback {
	void error(Component component);

	default void error(String string) {
		this.error(new TextComponent(string));
	}
}
