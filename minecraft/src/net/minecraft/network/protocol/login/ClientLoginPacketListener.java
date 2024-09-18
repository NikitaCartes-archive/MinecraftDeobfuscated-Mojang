package net.minecraft.network.protocol.login;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.cookie.ClientCookiePacketListener;

public interface ClientLoginPacketListener extends ClientCookiePacketListener {
	@Override
	default ConnectionProtocol protocol() {
		return ConnectionProtocol.LOGIN;
	}

	void handleHello(ClientboundHelloPacket clientboundHelloPacket);

	void handleLoginFinished(ClientboundLoginFinishedPacket clientboundLoginFinishedPacket);

	void handleDisconnect(ClientboundLoginDisconnectPacket clientboundLoginDisconnectPacket);

	void handleCompression(ClientboundLoginCompressionPacket clientboundLoginCompressionPacket);

	void handleCustomQuery(ClientboundCustomQueryPacket clientboundCustomQueryPacket);
}
