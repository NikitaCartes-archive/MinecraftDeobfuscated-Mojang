package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.status.ServerboundPingRequestPacket;

public interface ServerPingPacketListener extends PacketListener {
	void handlePingRequest(ServerboundPingRequestPacket serverboundPingRequestPacket);
}
