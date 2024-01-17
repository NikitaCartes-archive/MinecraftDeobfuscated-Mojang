package net.minecraft.network.protocol.ping;

import net.minecraft.network.PacketListener;

public interface ServerPingPacketListener extends PacketListener {
	void handlePingRequest(ServerboundPingRequestPacket serverboundPingRequestPacket);
}
