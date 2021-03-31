package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundHorseScreenOpenPacket implements Packet<ClientGamePacketListener> {
	private final int containerId;
	private final int size;
	private final int entityId;

	public ClientboundHorseScreenOpenPacket(int i, int j, int k) {
		this.containerId = i;
		this.size = j;
		this.entityId = k;
	}

	public ClientboundHorseScreenOpenPacket(FriendlyByteBuf friendlyByteBuf) {
		this.containerId = friendlyByteBuf.readUnsignedByte();
		this.size = friendlyByteBuf.readVarInt();
		this.entityId = friendlyByteBuf.readInt();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeByte(this.containerId);
		friendlyByteBuf.writeVarInt(this.size);
		friendlyByteBuf.writeInt(this.entityId);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleHorseScreenOpen(this);
	}

	public int getContainerId() {
		return this.containerId;
	}

	public int getSize() {
		return this.size;
	}

	public int getEntityId() {
		return this.entityId;
	}
}
