package net.minecraft.network.protocol.status;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundStatusResponsePacket(ServerStatus status) implements Packet<ClientStatusPacketListener> {
	public ClientboundStatusResponsePacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readJsonWithCodec(ServerStatus.CODEC));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeJsonWithCodec(ServerStatus.CODEC, this.status);
	}

	public void handle(ClientStatusPacketListener clientStatusPacketListener) {
		clientStatusPacketListener.handleStatusResponse(this);
	}
}
