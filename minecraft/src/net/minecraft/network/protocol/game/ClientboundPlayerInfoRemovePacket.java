package net.minecraft.network.protocol.game;

import java.util.List;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundPlayerInfoRemovePacket(List<UUID> profileIds) implements Packet<ClientGamePacketListener> {
	public ClientboundPlayerInfoRemovePacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readList(FriendlyByteBuf::readUUID));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeCollection(this.profileIds, FriendlyByteBuf::writeUUID);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handlePlayerInfoRemove(this);
	}
}
