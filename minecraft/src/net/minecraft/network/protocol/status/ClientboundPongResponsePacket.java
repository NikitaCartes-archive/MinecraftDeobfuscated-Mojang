package net.minecraft.network.protocol.status;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundPongResponsePacket implements Packet<ClientStatusPacketListener> {
	private long time;

	public ClientboundPongResponsePacket() {
	}

	public ClientboundPongResponsePacket(long l) {
		this.time = l;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.time = friendlyByteBuf.readLong();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeLong(this.time);
	}

	public void handle(ClientStatusPacketListener clientStatusPacketListener) {
		clientStatusPacketListener.handlePongResponse(this);
	}
}
