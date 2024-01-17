package net.minecraft.network.protocol.ping;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundPingRequestPacket implements Packet<ServerPingPacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ServerboundPingRequestPacket> STREAM_CODEC = Packet.codec(
		ServerboundPingRequestPacket::write, ServerboundPingRequestPacket::new
	);
	private final long time;

	public ServerboundPingRequestPacket(long l) {
		this.time = l;
	}

	private ServerboundPingRequestPacket(FriendlyByteBuf friendlyByteBuf) {
		this.time = friendlyByteBuf.readLong();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeLong(this.time);
	}

	@Override
	public PacketType<ServerboundPingRequestPacket> type() {
		return PingPacketTypes.SERVERBOUND_PING_REQUEST;
	}

	public void handle(ServerPingPacketListener serverPingPacketListener) {
		serverPingPacketListener.handlePingRequest(this);
	}

	public long getTime() {
		return this.time;
	}
}
