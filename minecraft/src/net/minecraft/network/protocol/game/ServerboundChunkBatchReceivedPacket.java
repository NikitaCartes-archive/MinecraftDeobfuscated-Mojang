package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ServerboundChunkBatchReceivedPacket(float desiredBatchSize) implements Packet<ServerGamePacketListener> {
	public ServerboundChunkBatchReceivedPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readFloat());
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeFloat(this.desiredBatchSize);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleChunkBatchReceived(this);
	}
}
