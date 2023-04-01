package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.voting.votes.OptionId;

public record ServerboundVoteCastPacket(int transactionId, OptionId optionId) implements Packet<ServerGamePacketListener> {
	public ServerboundVoteCastPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readVarInt(), friendlyByteBuf.read(OptionId.READER));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.transactionId);
		friendlyByteBuf.write(OptionId.WRITER, this.optionId);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleVoteCast(this);
	}
}
