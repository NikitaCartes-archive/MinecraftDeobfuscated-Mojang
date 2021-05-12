package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundPongPacket implements Packet<ServerGamePacketListener> {
	private final int id;

	public ServerboundPongPacket(int i) {
		this.id = i;
	}

	public ServerboundPongPacket(FriendlyByteBuf friendlyByteBuf) {
		this.id = friendlyByteBuf.readInt();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeInt(this.id);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handlePong(this);
	}

	public int getId() {
		return this.id;
	}
}
