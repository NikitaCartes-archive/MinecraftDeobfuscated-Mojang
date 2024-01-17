package net.minecraft.network.protocol.common;

import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;

public record ServerboundCustomPayloadPacket(CustomPacketPayload payload) implements Packet<ServerCommonPacketListener> {
	private static final int MAX_PAYLOAD_SIZE = 32767;
	public static final StreamCodec<FriendlyByteBuf, ServerboundCustomPayloadPacket> STREAM_CODEC = CustomPacketPayload.<FriendlyByteBuf>codec(
			resourceLocation -> DiscardedPayload.codec(resourceLocation, 32767),
			List.of(new CustomPacketPayload.TypeAndCodec<>(BrandPayload.TYPE, BrandPayload.STREAM_CODEC))
		)
		.map(ServerboundCustomPayloadPacket::new, ServerboundCustomPayloadPacket::payload);

	@Override
	public PacketType<ServerboundCustomPayloadPacket> type() {
		return CommonPacketTypes.SERVERBOUND_CUSTOM_PAYLOAD;
	}

	public void handle(ServerCommonPacketListener serverCommonPacketListener) {
		serverCommonPacketListener.handleCustomPayload(this);
	}
}
