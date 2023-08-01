package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundRespawnPacket(CommonPlayerSpawnInfo commonPlayerSpawnInfo, byte dataToKeep) implements Packet<ClientGamePacketListener> {
	public static final byte KEEP_ATTRIBUTES = 1;
	public static final byte KEEP_ENTITY_DATA = 2;
	public static final byte KEEP_ALL_DATA = 3;

	public ClientboundRespawnPacket(FriendlyByteBuf friendlyByteBuf) {
		this(new CommonPlayerSpawnInfo(friendlyByteBuf), friendlyByteBuf.readByte());
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		this.commonPlayerSpawnInfo.write(friendlyByteBuf);
		friendlyByteBuf.writeByte(this.dataToKeep);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleRespawn(this);
	}

	public boolean shouldKeep(byte b) {
		return (this.dataToKeep & b) != 0;
	}
}
