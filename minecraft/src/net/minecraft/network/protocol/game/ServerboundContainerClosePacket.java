package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundContainerClosePacket implements Packet<ServerGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ServerboundContainerClosePacket> STREAM_CODEC = Packet.codec(
		ServerboundContainerClosePacket::write, ServerboundContainerClosePacket::new
	);
	private final int containerId;

	public ServerboundContainerClosePacket(int i) {
		this.containerId = i;
	}

	private ServerboundContainerClosePacket(FriendlyByteBuf friendlyByteBuf) {
		this.containerId = friendlyByteBuf.readContainerId();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeContainerId(this.containerId);
	}

	@Override
	public PacketType<ServerboundContainerClosePacket> type() {
		return GamePacketTypes.SERVERBOUND_CONTAINER_CLOSE;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleContainerClose(this);
	}

	public int getContainerId() {
		return this.containerId;
	}
}
