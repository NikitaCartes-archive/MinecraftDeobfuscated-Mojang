package net.minecraft.network.protocol.status;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundPingRequestPacket implements Packet<ServerStatusPacketListener> {
	private long time;

	public ServerboundPingRequestPacket() {
	}

	@Environment(EnvType.CLIENT)
	public ServerboundPingRequestPacket(long l) {
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

	public void handle(ServerStatusPacketListener serverStatusPacketListener) {
		serverStatusPacketListener.handlePingRequest(this);
	}

	public long getTime() {
		return this.time;
	}
}
