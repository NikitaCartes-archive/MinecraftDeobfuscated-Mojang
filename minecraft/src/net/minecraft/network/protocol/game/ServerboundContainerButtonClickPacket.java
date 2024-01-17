package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ServerboundContainerButtonClickPacket implements Packet<ServerGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ServerboundContainerButtonClickPacket> STREAM_CODEC = Packet.codec(
		ServerboundContainerButtonClickPacket::write, ServerboundContainerButtonClickPacket::new
	);
	private final int containerId;
	private final int buttonId;

	public ServerboundContainerButtonClickPacket(int i, int j) {
		this.containerId = i;
		this.buttonId = j;
	}

	private ServerboundContainerButtonClickPacket(FriendlyByteBuf friendlyByteBuf) {
		this.containerId = friendlyByteBuf.readByte();
		this.buttonId = friendlyByteBuf.readByte();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeByte(this.containerId);
		friendlyByteBuf.writeByte(this.buttonId);
	}

	@Override
	public PacketType<ServerboundContainerButtonClickPacket> type() {
		return GamePacketTypes.SERVERBOUND_CONTAINER_BUTTON_CLICK;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleContainerButtonClick(this);
	}

	public int getContainerId() {
		return this.containerId;
	}

	public int getButtonId() {
		return this.buttonId;
	}
}
