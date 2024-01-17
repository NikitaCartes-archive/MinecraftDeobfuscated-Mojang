package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ClientboundRespawnPacket(CommonPlayerSpawnInfo commonPlayerSpawnInfo, byte dataToKeep) implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundRespawnPacket> STREAM_CODEC = Packet.codec(
		ClientboundRespawnPacket::write, ClientboundRespawnPacket::new
	);
	public static final byte KEEP_ATTRIBUTES = 1;
	public static final byte KEEP_ENTITY_DATA = 2;
	public static final byte KEEP_ALL_DATA = 3;

	private ClientboundRespawnPacket(FriendlyByteBuf friendlyByteBuf) {
		this(new CommonPlayerSpawnInfo(friendlyByteBuf), friendlyByteBuf.readByte());
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		this.commonPlayerSpawnInfo.write(friendlyByteBuf);
		friendlyByteBuf.writeByte(this.dataToKeep);
	}

	@Override
	public PacketType<ClientboundRespawnPacket> type() {
		return GamePacketTypes.CLIENTBOUND_RESPAWN;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleRespawn(this);
	}

	public boolean shouldKeep(byte b) {
		return (this.dataToKeep & b) != 0;
	}
}
