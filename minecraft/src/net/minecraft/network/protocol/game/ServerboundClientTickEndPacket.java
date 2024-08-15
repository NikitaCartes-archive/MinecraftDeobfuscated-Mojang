package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ServerboundClientTickEndPacket() implements Packet<ServerGamePacketListener> {
	public static final ServerboundClientTickEndPacket INSTANCE = new ServerboundClientTickEndPacket();
	public static final StreamCodec<ByteBuf, ServerboundClientTickEndPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public PacketType<ServerboundClientTickEndPacket> type() {
		return GamePacketTypes.SERVERBOUND_CLIENT_TICK_END;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleClientTickEnd(this);
	}
}
