package net.minecraft.network.protocol.common;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundResourcePackPopPacket(Optional<UUID> id) implements Packet<ClientCommonPacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundResourcePackPopPacket> STREAM_CODEC = Packet.codec(
		ClientboundResourcePackPopPacket::write, ClientboundResourcePackPopPacket::new
	);

	private ClientboundResourcePackPopPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readOptional(UUIDUtil.STREAM_CODEC));
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeOptional(this.id, UUIDUtil.STREAM_CODEC);
	}

	@Override
	public PacketType<ClientboundResourcePackPopPacket> type() {
		return CommonPacketTypes.CLIENTBOUND_RESOURCE_PACK_POP;
	}

	public void handle(ClientCommonPacketListener clientCommonPacketListener) {
		clientCommonPacketListener.handleResourcePackPop(this);
	}
}
