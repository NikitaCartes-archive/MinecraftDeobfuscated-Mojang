package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundSetBeaconPacket implements Packet<ServerGamePacketListener> {
	private int primary;
	private int secondary;

	public ServerboundSetBeaconPacket() {
	}

	@Environment(EnvType.CLIENT)
	public ServerboundSetBeaconPacket(int i, int j) {
		this.primary = i;
		this.secondary = j;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.primary = friendlyByteBuf.readVarInt();
		this.secondary = friendlyByteBuf.readVarInt();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
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
