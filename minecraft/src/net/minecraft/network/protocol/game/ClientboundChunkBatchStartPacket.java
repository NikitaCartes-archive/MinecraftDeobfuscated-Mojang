package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundChunkBatchStartPacket implements Packet<ClientGamePacketListener> {
	public static final ClientboundChunkBatchStartPacket INSTANCE = new ClientboundChunkBatchStartPacket();
	public static final StreamCodec<ByteBuf, ClientboundChunkBatchStartPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	private ClientboundChunkBatchStartPacket() {
	}

	@Override
	public PacketType<ClientboundChunkBatchStartPacket> type() {
		return GamePacketTypes.CLIENTBOUND_CHUNK_BATCH_START;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleChunkBatchStart(this);
	}
}
