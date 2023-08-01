package net.minecraft.server.network;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;

public interface ServerPlayerConnection {
	ServerPlayer getPlayer();

	void send(Packet<?> packet);

	void sendNoFlush(Packet<?> packet);

	void flush();
}
