package net.minecraft.network.protocol.status;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerPingPacketListener;

public class ServerboundPingRequestPacket implements Packet<ServerPingPacketListener> {
	private final long time;

	public ServerboundPingRequestPacket(long l) {
		this.time = l;
	}

	public ServerboundPingRequestPacket(FriendlyByteBuf friendlyByteBuf) {
		this.time = friendlyByteBuf.readLong();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeLong(this.time);
	}

	public void handle(ServerPingPacketListener serverPingPacketListener) {
		serverPingPacketListener.handlePingRequest(this);
	}

	public long getTime() {
		return this.time;
	}
}
