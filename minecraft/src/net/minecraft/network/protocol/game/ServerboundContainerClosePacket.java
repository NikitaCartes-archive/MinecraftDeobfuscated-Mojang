package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundContainerClosePacket implements Packet<ServerGamePacketListener> {
	private final int containerId;

	@Environment(EnvType.CLIENT)
	public ServerboundContainerClosePacket(int i) {
		this.containerId = i;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleContainerClose(this);
	}

	public ServerboundContainerClosePacket(FriendlyByteBuf friendlyByteBuf) {
		this.containerId = friendlyByteBuf.readByte();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeByte(this.containerId);
	}
}
