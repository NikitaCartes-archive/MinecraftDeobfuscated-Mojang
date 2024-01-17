package net.minecraft.network.protocol.status;

import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.ping.ClientPongPacketListener;

public interface ClientStatusPacketListener extends ClientPongPacketListener, ClientboundPacketListener {
	@Override
	default ConnectionProtocol protocol() {
		return ConnectionProtocol.STATUS;
	}

	void handleStatusResponse(ClientboundStatusResponsePacket clientboundStatusResponsePacket);
}
