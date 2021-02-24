package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundContainerAckPacket implements Packet<ServerGamePacketListener> {
	private final int containerId;
	private final short uid;
	private final boolean accepted;

	@Environment(EnvType.CLIENT)
	public ServerboundContainerAckPacket(int i, short s, boolean bl) {
		this.containerId = i;
		this.uid = s;
		this.accepted = bl;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleContainerAck(this);
	}

	public ServerboundContainerAckPacket(FriendlyByteBuf friendlyByteBuf) {
		this.containerId = friendlyByteBuf.readByte();
		this.uid = friendlyByteBuf.readShort();
		this.accepted = friendlyByteBuf.readByte() != 0;
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeByte(this.containerId);
		friendlyByteBuf.writeShort(this.uid);
		friendlyByteBuf.writeByte(this.accepted ? 1 : 0);
	}

	public int getContainerId() {
		return this.containerId;
	}

	public short getUid() {
		return this.uid;
	}
}
