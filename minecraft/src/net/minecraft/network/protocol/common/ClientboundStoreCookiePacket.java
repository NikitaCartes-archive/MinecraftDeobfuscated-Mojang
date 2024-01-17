package net.minecraft.network.protocol.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceLocation;

public record ClientboundStoreCookiePacket(ResourceLocation key, byte[] payload) implements Packet<ClientCommonPacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundStoreCookiePacket> STREAM_CODEC = Packet.codec(
		ClientboundStoreCookiePacket::write, ClientboundStoreCookiePacket::new
	);
	public static final int MAX_PAYLOAD_SIZE = 5120;

	private ClientboundStoreCookiePacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readResourceLocation(), friendlyByteBuf.readByteArray(5120));
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeResourceLocation(this.key);
		friendlyByteBuf.writeByteArray(this.payload);
	}

	@Override
	public PacketType<ClientboundStoreCookiePacket> type() {
		return CommonPacketTypes.CLIENTBOUND_STORE_COOKIE;
	}

	public void handle(ClientCommonPacketListener clientCommonPacketListener) {
		clientCommonPacketListener.handleStoreCookie(this);
	}
}
