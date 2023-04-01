package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.voting.votes.ClientVote;

public record ClientboundVoteStartPacket(UUID id, ClientVote voteData) implements Packet<ClientGamePacketListener> {
	public ClientboundVoteStartPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readUUID(), friendlyByteBuf.readWithCodec(NbtOps.INSTANCE, ClientVote.CODEC));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUUID(this.id);
		friendlyByteBuf.writeWithCodec(NbtOps.INSTANCE, ClientVote.CODEC, this.voteData);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleVoteStart(this);
	}
}
