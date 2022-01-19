package net.minecraft.network;

import net.minecraft.network.chat.Component;

public interface PacketListener {
	void onDisconnect(Component component);

	Connection getConnection();

	default boolean shouldPropagateHandlingExceptions() {
		return true;
	}
}
