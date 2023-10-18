package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ServerboundContainerSlotStateChangedPacket(int slotId, int containerId, boolean newState) implements Packet<ServerGamePacketListener> {
	public ServerboundContainerSlotStateChangedPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readVarInt(), friendlyByteBuf.readVarInt(), friendlyByteBuf.readBoolean());
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.slotId);
		friendlyByteBuf.writeVarInt(this.containerId);
		friendlyByteBuf.writeBoolean(this.newState);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleContainerSlotStateChanged(this);
	}
}
