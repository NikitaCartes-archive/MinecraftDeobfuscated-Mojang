package net.minecraft.network.protocol.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundTransferPacket(String host, int port) implements Packet<ClientCommonPacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundTransferPacket> STREAM_CODEC = Packet.codec(
		ClientboundTransferPacket::write, ClientboundTransferPacket::new
	);

	private ClientboundTransferPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readUtf(), friendlyByteBuf.readVarInt());
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.host);
		friendlyByteBuf.writeVarInt(this.port);
	}

	@Override
	public PacketType<ClientboundTransferPacket> type() {
		return CommonPacketTypes.CLIENTBOUND_TRANSFER;
	}

	public void handle(ClientCommonPacketListener clientCommonPacketListener) {
		clientCommonPacketListener.handleTransfer(this);
	}
}
