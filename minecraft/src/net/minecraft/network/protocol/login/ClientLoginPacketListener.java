package net.minecraft.network.protocol.login;

import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.ConnectionProtocol;

public interface ClientLoginPacketListener extends ClientboundPacketListener {
	@Override
	default ConnectionProtocol protocol() {
		return ConnectionProtocol.LOGIN;
	}

	void handleHello(ClientboundHelloPacket clientboundHelloPacket);

	void handleGameProfile(ClientboundGameProfilePacket clientboundGameProfilePacket);

	void handleDisconnect(ClientboundLoginDisconnectPacket clientboundLoginDisconnectPacket);

	void handleCompression(ClientboundLoginCompressionPacket clientboundLoginCompressionPacket);

	void handleCustomQuery(ClientboundCustomQueryPacket clientboundCustomQueryPacket);
}
