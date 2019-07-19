package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundPaddleBoatPacket implements Packet<ServerGamePacketListener> {
	private boolean left;
	private boolean right;

	public ServerboundPaddleBoatPacket() {
	}

	public ServerboundPaddleBoatPacket(boolean bl, boolean bl2) {
		this.left = bl;
		this.right = bl2;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.left = friendlyByteBuf.readBoolean();
		this.right = friendlyByteBuf.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
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
