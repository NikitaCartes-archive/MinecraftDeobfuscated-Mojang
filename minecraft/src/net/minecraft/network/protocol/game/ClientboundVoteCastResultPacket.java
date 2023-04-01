package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;

public record ClientboundVoteCastResultPacket(int transactionId, Optional<Component> rejectReason) implements Packet<ClientGamePacketListener> {
	public ClientboundVoteCastResultPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readVarInt(), friendlyByteBuf.readOptional(FriendlyByteBuf::readComponent));
	}

	public static ClientboundVoteCastResultPacket success(int i) {
		return new ClientboundVoteCastResultPacket(i, Optional.empty());
	}

	public static ClientboundVoteCastResultPacket failure(int i, Component component) {
		return new ClientboundVoteCastResultPacket(i, Optional.of(component));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.transactionId);
		friendlyByteBuf.writeOptional(this.rejectReason, FriendlyByteBuf::writeComponent);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleVoteCastResult(this);
	}
}
