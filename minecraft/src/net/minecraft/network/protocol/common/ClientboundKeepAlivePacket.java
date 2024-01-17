package net.minecraft.network.protocol.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundKeepAlivePacket implements Packet<ClientCommonPacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundKeepAlivePacket> STREAM_CODEC = Packet.codec(
		ClientboundKeepAlivePacket::write, ClientboundKeepAlivePacket::new
	);
	private final long id;

	public ClientboundKeepAlivePacket(long l) {
		this.id = l;
	}

	private ClientboundKeepAlivePacket(FriendlyByteBuf friendlyByteBuf) {
		this.id = friendlyByteBuf.readLong();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeLong(this.id);
	}

	@Override
	public PacketType<ClientboundKeepAlivePacket> type() {
		return CommonPacketTypes.CLIENTBOUND_KEEP_ALIVE;
	}

	public void handle(ClientCommonPacketListener clientCommonPacketListener) {
		clientCommonPacketListener.handleKeepAlive(this);
	}

	public long getId() {
		return this.id;
	}
}
