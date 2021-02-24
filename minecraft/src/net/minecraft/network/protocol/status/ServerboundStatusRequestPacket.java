package net.minecraft.network.protocol.status;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundStatusRequestPacket implements Packet<ServerStatusPacketListener> {
	public ServerboundStatusRequestPacket() {
	}

	public ServerboundStatusRequestPacket(FriendlyByteBuf friendlyByteBuf) {
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
	}

	public void handle(ServerStatusPacketListener serverStatusPacketListener) {
		serverStatusPacketListener.handleStatusRequest(this);
	}
}
