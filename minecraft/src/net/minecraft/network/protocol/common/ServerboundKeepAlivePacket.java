package net.minecraft.network.protocol.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundKeepAlivePacket implements Packet<ServerCommonPacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ServerboundKeepAlivePacket> STREAM_CODEC = Packet.codec(
		ServerboundKeepAlivePacket::write, ServerboundKeepAlivePacket::new
	);
	private final long id;

	public ServerboundKeepAlivePacket(long l) {
		this.id = l;
	}

	private ServerboundKeepAlivePacket(FriendlyByteBuf friendlyByteBuf) {
		this.id = friendlyByteBuf.readLong();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeLong(this.id);
	}

	@Override
	public PacketType<ServerboundKeepAlivePacket> type() {
		return CommonPacketTypes.SERVERBOUND_KEEP_ALIVE;
	}

	public void handle(ServerCommonPacketListener serverCommonPacketListener) {
		serverCommonPacketListener.handleKeepAlive(this);
	}

	public long getId() {
		return this.id;
	}
}
