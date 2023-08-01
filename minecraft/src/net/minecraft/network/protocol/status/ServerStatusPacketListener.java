package net.minecraft.network.protocol.status;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.game.ServerPacketListener;

public interface ServerStatusPacketListener extends ServerPacketListener {
	@Override
	default ConnectionProtocol protocol() {
		return ConnectionProtocol.STATUS;
	}

	void handlePingRequest(ServerboundPingRequestPacket serverboundPingRequestPacket);

	void handleStatusRequest(ServerboundStatusRequestPacket serverboundStatusRequestPacket);
}
