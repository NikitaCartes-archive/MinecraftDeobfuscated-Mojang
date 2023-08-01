package net.minecraft.network.protocol.common;

import net.minecraft.network.ClientboundPacketListener;

public interface ClientCommonPacketListener extends ClientboundPacketListener {
	void handleKeepAlive(ClientboundKeepAlivePacket clientboundKeepAlivePacket);

	void handlePing(ClientboundPingPacket clientboundPingPacket);

	void handleCustomPayload(ClientboundCustomPayloadPacket clientboundCustomPayloadPacket);

	void handleDisconnect(ClientboundDisconnectPacket clientboundDisconnectPacket);

	void handleResourcePack(ClientboundResourcePackPacket clientboundResourcePackPacket);

	void handleUpdateTags(ClientboundUpdateTagsPacket clientboundUpdateTagsPacket);
}
