package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;

public class ClientboundSetEntityLinkPacket implements Packet<ClientGamePacketListener> {
	private final int sourceId;
	private final int destId;

	public ClientboundSetEntityLinkPacket(Entity entity, @Nullable Entity entity2) {
		this.sourceId = entity.getId();
		this.destId = entity2 != null ? entity2.getId() : 0;
	}

	public ClientboundSetEntityLinkPacket(FriendlyByteBuf friendlyByteBuf) {
		this.sourceId = friendlyByteBuf.readInt();
		this.destId = friendlyByteBuf.readInt();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeInt(this.sourceId);
		friendlyByteBuf.writeInt(this.destId);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleEntityLinkPacket(this);
	}

	public int getSourceId() {
		return this.sourceId;
	}

	public int getDestId() {
		return this.destId;
	}
}
