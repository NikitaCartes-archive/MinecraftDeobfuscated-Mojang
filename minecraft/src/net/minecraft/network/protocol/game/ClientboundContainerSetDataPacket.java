package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundContainerSetDataPacket implements Packet<ClientGamePacketListener> {
	private final int containerId;
	private final int id;
	private final int value;

	public ClientboundContainerSetDataPacket(int i, int j, int k) {
		this.containerId = i;
		this.id = j;
		this.value = k;
	}

	public ClientboundContainerSetDataPacket(FriendlyByteBuf friendlyByteBuf) {
		this.containerId = friendlyByteBuf.readUnsignedByte();
		this.id = friendlyByteBuf.readShort();
		this.value = friendlyByteBuf.readShort();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeByte(this.containerId);
		friendlyByteBuf.writeShort(this.id);
		friendlyByteBuf.writeShort(this.value);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleContainerSetData(this);
	}

	@Environment(EnvType.CLIENT)
	public int getContainerId() {
		return this.containerId;
	}

	@Environment(EnvType.CLIENT)
	public int getId() {
		return this.id;
	}

	@Environment(EnvType.CLIENT)
	public int getValue() {
		return this.value;
	}
}
