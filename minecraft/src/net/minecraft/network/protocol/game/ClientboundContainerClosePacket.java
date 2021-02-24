package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundContainerClosePacket implements Packet<ClientGamePacketListener> {
	private final int containerId;

	public ClientboundContainerClosePacket(int i) {
		this.containerId = i;
	}

	public ClientboundContainerClosePacket(FriendlyByteBuf friendlyByteBuf) {
		this.containerId = friendlyByteBuf.readUnsignedByte();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeByte(this.containerId);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleContainerClose(this);
	}
}
