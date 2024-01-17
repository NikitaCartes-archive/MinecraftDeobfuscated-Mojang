package net.minecraft.network.protocol.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundPongPacket implements Packet<ServerCommonPacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ServerboundPongPacket> STREAM_CODEC = Packet.codec(ServerboundPongPacket::write, ServerboundPongPacket::new);
	private final int id;

	public ServerboundPongPacket(int i) {
		this.id = i;
	}

	private ServerboundPongPacket(FriendlyByteBuf friendlyByteBuf) {
		this.id = friendlyByteBuf.readInt();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeInt(this.id);
	}

	@Override
	public PacketType<ServerboundPongPacket> type() {
		return CommonPacketTypes.SERVERBOUND_PONG;
	}

	public void handle(ServerCommonPacketListener serverCommonPacketListener) {
		serverCommonPacketListener.handlePong(this);
	}

	public int getId() {
		return this.id;
	}
}
