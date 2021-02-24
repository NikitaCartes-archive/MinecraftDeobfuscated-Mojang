package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetChunkCacheCenterPacket implements Packet<ClientGamePacketListener> {
	private final int x;
	private final int z;

	public ClientboundSetChunkCacheCenterPacket(int i, int j) {
		this.x = i;
		this.z = j;
	}

	public ClientboundSetChunkCacheCenterPacket(FriendlyByteBuf friendlyByteBuf) {
		this.x = friendlyByteBuf.readVarInt();
		this.z = friendlyByteBuf.readVarInt();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.x);
		friendlyByteBuf.writeVarInt(this.z);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetChunkCacheCenter(this);
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
