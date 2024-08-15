package net.minecraft.network.protocol.common;

import net.minecraft.network.protocol.cookie.ServerCookiePacketListener;

public interface ServerCommonPacketListener extends ServerCookiePacketListener {
	void handleKeepAlive(ServerboundKeepAlivePacket serverboundKeepAlivePacket);

	void handlePong(ServerboundPongPacket serverboundPongPacket);

	void handleCustomPayload(ServerboundCustomPayloadPacket serverboundCustomPayloadPacket);

	void handleResourcePackResponse(ServerboundResourcePackPacket serverboundResourcePackPacket);

	void handleClientInformation(ServerboundClientInformationPacket serverboundClientInformationPacket);
}
