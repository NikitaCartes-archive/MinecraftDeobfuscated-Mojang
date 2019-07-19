package net.minecraft.network.protocol.login;

import net.minecraft.network.PacketListener;

public interface ClientLoginPacketListener extends PacketListener {
	void handleHello(ClientboundHelloPacket clientboundHelloPacket);

	void handleGameProfile(ClientboundGameProfilePacket clientboundGameProfilePacket);

	void handleDisconnect(ClientboundLoginDisconnectPacket clientboundLoginDisconnectPacket);

	void handleCompression(ClientboundLoginCompressionPacket clientboundLoginCompressionPacket);

	void handleCustomQuery(ClientboundCustomQueryPacket clientboundCustomQueryPacket);
}
