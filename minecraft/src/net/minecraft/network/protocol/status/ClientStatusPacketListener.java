package net.minecraft.network.protocol.status;

import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.ConnectionProtocol;

public interface ClientStatusPacketListener extends ClientboundPacketListener {
	@Override
	default ConnectionProtocol protocol() {
		return ConnectionProtocol.STATUS;
	}

	void handleStatusResponse(ClientboundStatusResponsePacket clientboundStatusResponsePacket);

	void handlePongResponse(ClientboundPongResponsePacket clientboundPongResponsePacket);
}
