package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundPaddleBoatPacket implements Packet<ServerGamePacketListener> {
	private final boolean left;
	private final boolean right;

	public ServerboundPaddleBoatPacket(boolean bl, boolean bl2) {
		this.left = bl;
		this.right = bl2;
	}

	public ServerboundPaddleBoatPacket(FriendlyByteBuf friendlyByteBuf) {
		this.left = friendlyByteBuf.readBoolean();
		this.right = friendlyByteBuf.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeBoolean(this.left);
		friendlyByteBuf.writeBoolean(this.right);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handlePaddleBoat(this);
	}

	public boolean getLeft() {
		return this.left;
	}

	public boolean getRight() {
		return this.right;
	}
}
