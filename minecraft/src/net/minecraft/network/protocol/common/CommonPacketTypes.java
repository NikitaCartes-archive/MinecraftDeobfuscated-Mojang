package net.minecraft.network.protocol.common;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceLocation;

public class CommonPacketTypes {
	public static final PacketType<ClientboundCustomPayloadPacket> CLIENTBOUND_CUSTOM_PAYLOAD = createClientbound("custom_payload");
	public static final PacketType<ClientboundDisconnectPacket> CLIENTBOUND_DISCONNECT = createClientbound("disconnect");
	public static final PacketType<ClientboundKeepAlivePacket> CLIENTBOUND_KEEP_ALIVE = createClientbound("keep_alive");
	public static final PacketType<ClientboundPingPacket> CLIENTBOUND_PING = createClientbound("ping");
	public static final PacketType<ClientboundResourcePackPopPacket> CLIENTBOUND_RESOURCE_PACK_POP = createClientbound("resource_pack_pop");
	public static final PacketType<ClientboundResourcePackPushPacket> CLIENTBOUND_RESOURCE_PACK_PUSH = createClientbound("resource_pack_push");
	public static final PacketType<ClientboundStoreCookiePacket> CLIENTBOUND_STORE_COOKIE = createClientbound("store_cookie");
	public static final PacketType<ClientboundTransferPacket> CLIENTBOUND_TRANSFER = createClientbound("transfer");
	public static final PacketType<ClientboundUpdateTagsPacket> CLIENTBOUND_UPDATE_TAGS = createClientbound("update_tags");
	public static final PacketType<ServerboundClientInformationPacket> SERVERBOUND_CLIENT_INFORMATION = createServerbound("client_information");
	public static final PacketType<ServerboundCustomPayloadPacket> SERVERBOUND_CUSTOM_PAYLOAD = createServerbound("custom_payload");
	public static final PacketType<ServerboundKeepAlivePacket> SERVERBOUND_KEEP_ALIVE = createServerbound("keep_alive");
	public static final PacketType<ServerboundPongPacket> SERVERBOUND_PONG = createServerbound("pong");
	public static final PacketType<ServerboundResourcePackPacket> SERVERBOUND_RESOURCE_PACK = createServerbound("resource_pack");

	private static <T extends Packet<ClientCommonPacketListener>> PacketType<T> createClientbound(String string) {
		return new PacketType<>(PacketFlow.CLIENTBOUND, new ResourceLocation(string));
	}

	private static <T extends Packet<ServerCommonPacketListener>> PacketType<T> createServerbound(String string) {
		return new PacketType<>(PacketFlow.SERVERBOUND, new ResourceLocation(string));
	}
}
