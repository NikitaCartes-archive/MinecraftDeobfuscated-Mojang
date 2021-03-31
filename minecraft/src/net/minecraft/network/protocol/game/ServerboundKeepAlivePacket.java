package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundKeepAlivePacket implements Packet<ServerGamePacketListener> {
	private final long id;

	public ServerboundKeepAlivePacket(long l) {
		this.id = l;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleKeepAlive(this);
	}

	public ServerboundKeepAlivePacket(FriendlyByteBuf friendlyByteBuf) {
		this.id = friendlyByteBuf.readLong();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeLong(this.id);
	}

	public long getId() {
		return this.id;
	}
}
