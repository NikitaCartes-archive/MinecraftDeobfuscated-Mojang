package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundSetBeaconPacket implements Packet<ServerGamePacketListener> {
	private final int primary;
	private final int secondary;

	public ServerboundSetBeaconPacket(int i, int j) {
		this.primary = i;
		this.secondary = j;
	}

	public ServerboundSetBeaconPacket(FriendlyByteBuf friendlyByteBuf) {
		this.primary = friendlyByteBuf.readVarInt();
		this.secondary = friendlyByteBuf.readVarInt();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.primary);
		friendlyByteBuf.writeVarInt(this.secondary);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleSetBeaconPacket(this);
	}

	public int getPrimary() {
		return this.primary;
	}

	public int getSecondary() {
		return this.secondary;
	}
}
