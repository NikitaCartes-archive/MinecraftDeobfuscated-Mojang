package net.minecraft.network.protocol.ping;

import net.minecraft.network.PacketListener;

public interface ClientPongPacketListener extends PacketListener {
	void handlePongResponse(ClientboundPongResponsePacket clientboundPongResponsePacket);
}
