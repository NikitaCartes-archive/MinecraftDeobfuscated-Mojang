package net.minecraft.network.protocol.common;

import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.protocol.cookie.ClientCookiePacketListener;

public interface ClientCommonPacketListener extends ClientCookiePacketListener, ClientboundPacketListener {
	void handleKeepAlive(ClientboundKeepAlivePacket clientboundKeepAlivePacket);

	void handlePing(ClientboundPingPacket clientboundPingPacket);

	void handleCustomPayload(ClientboundCustomPayloadPacket clientboundCustomPayloadPacket);

	void handleDisconnect(ClientboundDisconnectPacket clientboundDisconnectPacket);

	void handleResourcePackPush(ClientboundResourcePackPushPacket clientboundResourcePackPushPacket);

	void handleResourcePackPop(ClientboundResourcePackPopPacket clientboundResourcePackPopPacket);

	void handleUpdateTags(ClientboundUpdateTagsPacket clientboundUpdateTagsPacket);

	void handleStoreCookie(ClientboundStoreCookiePacket clientboundStoreCookiePacket);

	void handleTransfer(ClientboundTransferPacket clientboundTransferPacket);
}
