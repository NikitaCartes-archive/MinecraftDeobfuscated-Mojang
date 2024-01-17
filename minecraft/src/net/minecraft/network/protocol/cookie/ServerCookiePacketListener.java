package net.minecraft.network.protocol.cookie;

import net.minecraft.network.protocol.game.ServerPacketListener;

public interface ServerCookiePacketListener extends ServerPacketListener {
	void handleCookieResponse(ServerboundCookieResponsePacket serverboundCookieResponsePacket);
}
