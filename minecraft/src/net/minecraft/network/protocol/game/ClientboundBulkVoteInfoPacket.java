package net.minecraft.network.protocol.game;

import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.voting.votes.ClientVote;
import net.minecraft.voting.votes.OptionId;
import net.minecraft.voting.votes.OptionVotes;

public record ClientboundBulkVoteInfoPacket(boolean clear, Map<UUID, ClientVote> votes, Map<OptionId, OptionVotes> voters)
	implements Packet<ClientGamePacketListener> {
	public ClientboundBulkVoteInfoPacket(FriendlyByteBuf friendlyByteBuf) {
		this(
			friendlyByteBuf.readBoolean(),
			friendlyByteBuf.readMap(FriendlyByteBuf::readUUID, friendlyByteBufx -> friendlyByteBufx.readWithCodec(NbtOps.INSTANCE, ClientVote.CODEC)),
			friendlyByteBuf.readMap(OptionId.READER, OptionVotes.READER)
		);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeBoolean(this.clear);
		friendlyByteBuf.writeMap(
			this.votes, FriendlyByteBuf::writeUUID, (friendlyByteBufx, clientVote) -> friendlyByteBufx.writeWithCodec(NbtOps.INSTANCE, ClientVote.CODEC, clientVote)
		);
		friendlyByteBuf.writeMap(this.voters, OptionId.WRITER, OptionVotes.WRITER);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleBulkVoteInfoPacket(this);
	}
}
