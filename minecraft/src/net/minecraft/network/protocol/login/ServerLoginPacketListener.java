package net.minecraft.network.protocol.login;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.game.ServerPacketListener;

public interface ServerLoginPacketListener extends ServerPacketListener {
	@Override
	default ConnectionProtocol protocol() {
		return ConnectionProtocol.LOGIN;
	}

	void handleHello(ServerboundHelloPacket serverboundHelloPacket);

	void handleKey(ServerboundKeyPacket serverboundKeyPacket);

	void handleCustomQueryPacket(ServerboundCustomQueryAnswerPacket serverboundCustomQueryAnswerPacket);

	void handleLoginAcknowledgement(ServerboundLoginAcknowledgedPacket serverboundLoginAcknowledgedPacket);
}
