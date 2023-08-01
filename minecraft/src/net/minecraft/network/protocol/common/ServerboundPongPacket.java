package net.minecraft.network.protocol.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundPongPacket implements Packet<ServerCommonPacketListener> {
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

	public void handle(ServerCommonPacketListener serverCommonPacketListener) {
		serverCommonPacketListener.handlePong(this);
	}

	public int getId() {
		return this.id;
	}
}
