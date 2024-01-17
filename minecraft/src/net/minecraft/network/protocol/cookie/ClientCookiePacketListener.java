package net.minecraft.network.protocol.cookie;

import net.minecraft.network.ClientboundPacketListener;

public interface ClientCookiePacketListener extends ClientboundPacketListener {
	void handleRequestCookie(ClientboundCookieRequestPacket clientboundCookieRequestPacket);
}
