package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundContainerButtonClickPacket implements Packet<ServerGamePacketListener> {
	private int containerId;
	private int buttonId;

	public ServerboundContainerButtonClickPacket() {
	}

	@Environment(EnvType.CLIENT)
	public ServerboundContainerButtonClickPacket(int i, int j) {
		this.containerId = i;
		this.buttonId = j;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleContainerButtonClick(this);
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.containerId = friendlyByteBuf.readByte();
		this.buttonId = friendlyByteBuf.readByte();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
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
