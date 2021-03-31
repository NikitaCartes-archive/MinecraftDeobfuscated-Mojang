package net.minecraft.network.protocol.status;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundPingRequestPacket implements Packet<ServerStatusPacketListener> {
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

	public void handle(ServerStatusPacketListener serverStatusPacketListener) {
		serverStatusPacketListener.handlePingRequest(this);
	}

	public long getTime() {
		return this.time;
	}
}
