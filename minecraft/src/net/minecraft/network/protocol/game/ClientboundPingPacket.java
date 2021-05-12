package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundPingPacket implements Packet<ClientGamePacketListener> {
	private final int id;

	public ClientboundPingPacket(int i) {
		this.id = i;
	}

	public ClientboundPingPacket(FriendlyByteBuf friendlyByteBuf) {
		this.id = friendlyByteBuf.readInt();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeInt(this.id);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handlePing(this);
	}

	public int getId() {
		return this.id;
	}
}
