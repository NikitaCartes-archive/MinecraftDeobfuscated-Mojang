package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ServerboundContainerSlotStateChangedPacket(int slotId, int containerId, boolean newState) implements Packet<ServerGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ServerboundContainerSlotStateChangedPacket> STREAM_CODEC = Packet.codec(
		ServerboundContainerSlotStateChangedPacket::write, ServerboundContainerSlotStateChangedPacket::new
	);

	private ServerboundContainerSlotStateChangedPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readVarInt(), friendlyByteBuf.readContainerId(), friendlyByteBuf.readBoolean());
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.slotId);
		friendlyByteBuf.writeContainerId(this.containerId);
		friendlyByteBuf.writeBoolean(this.newState);
	}

	@Override
	public PacketType<ServerboundContainerSlotStateChangedPacket> type() {
		return GamePacketTypes.SERVERBOUND_CONTAINER_SLOT_STATE_CHANGED;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleContainerSlotStateChanged(this);
	}
}
