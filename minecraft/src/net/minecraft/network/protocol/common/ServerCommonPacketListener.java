package net.minecraft.network.protocol.common;

import net.minecraft.network.protocol.game.ServerPacketListener;

public interface ServerCommonPacketListener extends ServerPacketListener {
	void handleKeepAlive(ServerboundKeepAlivePacket serverboundKeepAlivePacket);

	void handlePong(ServerboundPongPacket serverboundPongPacket);

	void handleCustomPayload(ServerboundCustomPayloadPacket serverboundCustomPayloadPacket);

	void handleResourcePackResponse(ServerboundResourcePackPacket serverboundResourcePackPacket);
}
