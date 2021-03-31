package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundContainerButtonClickPacket implements Packet<ServerGamePacketListener> {
	private final int containerId;
	private final int buttonId;

	public ServerboundContainerButtonClickPacket(int i, int j) {
		this.containerId = i;
		this.buttonId = j;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleContainerButtonClick(this);
	}

	public ServerboundContainerButtonClickPacket(FriendlyByteBuf friendlyByteBuf) {
		this.containerId = friendlyByteBuf.readByte();
		this.buttonId = friendlyByteBuf.readByte();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeByte(this.containerId);
		friendlyByteBuf.writeByte(this.buttonId);
	}

	public int getContainerId() {
		return this.containerId;
	}

	public int getButtonId() {
		return this.buttonId;
	}
}
