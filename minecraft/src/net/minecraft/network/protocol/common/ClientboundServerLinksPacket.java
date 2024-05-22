package net.minecraft.network.protocol.common;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.server.ServerLinks;

public record ClientboundServerLinksPacket(ServerLinks links) implements Packet<ClientCommonPacketListener> {
	public static final StreamCodec<ByteBuf, ClientboundServerLinksPacket> STREAM_CODEC = StreamCodec.composite(
		ServerLinks.STREAM_CODEC, ClientboundServerLinksPacket::links, ClientboundServerLinksPacket::new
	);

	@Override
	public PacketType<ClientboundServerLinksPacket> type() {
		return CommonPacketTypes.CLIENTBOUND_SERVER_LINKS;
	}

	public void handle(ClientCommonPacketListener clientCommonPacketListener) {
		clientCommonPacketListener.handleServerLinks(this);
	}
}
