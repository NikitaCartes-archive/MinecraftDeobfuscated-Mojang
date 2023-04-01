package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.voting.votes.OptionId;
import net.minecraft.voting.votes.OptionVotes;

public record ClientboundVoteProgressInfoPacket(OptionId id, OptionVotes voters) implements Packet<ClientGamePacketListener> {
	public ClientboundVoteProgressInfoPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.read(OptionId.READER), friendlyByteBuf.read(OptionVotes.READER));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.write(OptionId.WRITER, this.id);
		friendlyByteBuf.write(OptionVotes.WRITER, this.voters);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleVoteOptionInfo(this);
	}
}
