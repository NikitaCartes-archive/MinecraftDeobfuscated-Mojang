package net.minecraft.network.protocol.ping;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundPongResponsePacket(long time) implements Packet<ClientPongPacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundPongResponsePacket> STREAM_CODEC = Packet.codec(
		ClientboundPongResponsePacket::write, ClientboundPongResponsePacket::new
	);

	private ClientboundPongResponsePacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readLong());
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeLong(this.time);
	}

	@Override
	public PacketType<ClientboundPongResponsePacket> type() {
		return PingPacketTypes.CLIENTBOUND_PONG_RESPONSE;
	}

	public void handle(ClientPongPacketListener clientPongPacketListener) {
		clientPongPacketListener.handlePongResponse(this);
	}
}
