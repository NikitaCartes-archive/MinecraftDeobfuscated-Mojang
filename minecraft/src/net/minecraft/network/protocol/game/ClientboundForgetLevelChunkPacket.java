package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundForgetLevelChunkPacket implements Packet<ClientGamePacketListener> {
	private final int x;
	private final int z;

	public ClientboundForgetLevelChunkPacket(int i, int j) {
		this.x = i;
		this.z = j;
	}

	public ClientboundForgetLevelChunkPacket(FriendlyByteBuf friendlyByteBuf) {
		this.x = friendlyByteBuf.readInt();
		this.z = friendlyByteBuf.readInt();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeInt(this.x);
		friendlyByteBuf.writeInt(this.z);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleForgetLevelChunk(this);
	}

	@Environment(EnvType.CLIENT)
	public int getX() {
		return this.x;
	}

	@Environment(EnvType.CLIENT)
	public int getZ() {
		return this.z;
	}
}
