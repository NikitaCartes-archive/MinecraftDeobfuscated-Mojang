package net.minecraft.network.protocol.login;

import net.minecraft.network.protocol.game.ServerPacketListener;

public interface ServerLoginPacketListener extends ServerPacketListener {
	void handleHello(ServerboundHelloPacket serverboundHelloPacket);

	void handleKey(ServerboundKeyPacket serverboundKeyPacket);

	void handleCustomQueryPacket(ServerboundCustomQueryPacket serverboundCustomQueryPacket);
}
