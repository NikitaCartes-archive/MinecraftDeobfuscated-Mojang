package net.minecraft.network.protocol.status;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundStatusResponsePacket(ServerStatus status) implements Packet<ClientStatusPacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundStatusResponsePacket> STREAM_CODEC = Packet.codec(
		ClientboundStatusResponsePacket::write, ClientboundStatusResponsePacket::new
	);

	private ClientboundStatusResponsePacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readJsonWithCodec(ServerStatus.CODEC));
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeJsonWithCodec(ServerStatus.CODEC, this.status);
	}

	@Override
	public PacketType<ClientboundStatusResponsePacket> type() {
		return StatusPacketTypes.CLIENTBOUND_STATUS_RESPONSE;
	}

	public void handle(ClientStatusPacketListener clientStatusPacketListener) {
		clientStatusPacketListener.handleStatusResponse(this);
	}
}
