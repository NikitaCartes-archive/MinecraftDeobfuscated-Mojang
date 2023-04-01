package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundVoteFinishPacket(UUID id) implements Packet<ClientGamePacketListener> {
	public ClientboundVoteFinishPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readUUID());
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUUID(this.id);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleVoteFinish(this);
	}
}
