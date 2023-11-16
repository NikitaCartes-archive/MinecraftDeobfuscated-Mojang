package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundResetScorePacket(String owner, @Nullable String objectiveName) implements Packet<ClientGamePacketListener> {
	public ClientboundResetScorePacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readUtf(), friendlyByteBuf.readNullable(FriendlyByteBuf::readUtf));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.owner);
		friendlyByteBuf.writeNullable(this.objectiveName, FriendlyByteBuf::writeUtf);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleResetScore(this);
	}
}
