package net.minecraft.network;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;

public interface PacketListener {
	PacketFlow flow();

	ConnectionProtocol protocol();

	void onDisconnect(Component component);

	boolean isAcceptingMessages();

	default boolean shouldHandleMessage(Packet<?> packet) {
		return this.isAcceptingMessages();
	}

	default boolean shouldPropagateHandlingExceptions() {
		return true;
	}
}
