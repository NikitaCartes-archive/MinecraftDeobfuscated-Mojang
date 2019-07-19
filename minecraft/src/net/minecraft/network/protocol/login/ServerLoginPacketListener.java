package net.minecraft.network.protocol.login;

import net.minecraft.network.PacketListener;

public interface ServerLoginPacketListener extends PacketListener {
	void handleHello(ServerboundHelloPacket serverboundHelloPacket);

	void handleKey(ServerboundKeyPacket serverboundKeyPacket);

	void handleCustomQueryPacket(ServerboundCustomQueryPacket serverboundCustomQueryPacket);
}
