package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundRemoveEntityPacket implements Packet<ClientGamePacketListener> {
	private final int entityId;

	public ClientboundRemoveEntityPacket(int i) {
		this.entityId = i;
	}

	public ClientboundRemoveEntityPacket(FriendlyByteBuf friendlyByteBuf) {
		this.entityId = friendlyByteBuf.readVarInt();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.entityId);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleRemoveEntity(this);
	}

	public int getEntityId() {
		return this.entityId;
	}
}
