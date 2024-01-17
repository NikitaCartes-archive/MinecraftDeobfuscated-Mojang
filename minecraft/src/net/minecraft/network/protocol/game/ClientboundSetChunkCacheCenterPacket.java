package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundSetChunkCacheCenterPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundSetChunkCacheCenterPacket> STREAM_CODEC = Packet.codec(
		ClientboundSetChunkCacheCenterPacket::write, ClientboundSetChunkCacheCenterPacket::new
	);
	private final int x;
	private final int z;

	public ClientboundSetChunkCacheCenterPacket(int i, int j) {
		this.x = i;
		this.z = j;
	}

	private ClientboundSetChunkCacheCenterPacket(FriendlyByteBuf friendlyByteBuf) {
		this.x = friendlyByteBuf.readVarInt();
		this.z = friendlyByteBuf.readVarInt();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.x);
		friendlyByteBuf.writeVarInt(this.z);
	}

	@Override
	public PacketType<ClientboundSetChunkCacheCenterPacket> type() {
		return GamePacketTypes.CLIENTBOUND_SET_CHUNK_CACHE_CENTER;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetChunkCacheCenter(this);
	}

	public int getX() {
		return this.x;
	}

	public int getZ() {
		return this.z;
	}
}
