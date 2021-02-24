package net.minecraft.network.protocol.status;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundPongResponsePacket implements Packet<ClientStatusPacketListener> {
	private final long time;

	public ClientboundPongResponsePacket(long l) {
		this.time = l;
	}

	public ClientboundPongResponsePacket(FriendlyByteBuf friendlyByteBuf) {
		this.time = friendlyByteBuf.readLong();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeLong(this.time);
	}

	public void handle(ClientStatusPacketListener clientStatusPacketListener) {
		clientStatusPacketListener.handlePongResponse(this);
	}
}
