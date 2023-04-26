package net.minecraft.network.protocol.game;

import java.util.BitSet;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class ClientboundLightUpdatePacket implements Packet<ClientGamePacketListener> {
	private final int x;
	private final int z;
	private final ClientboundLightUpdatePacketData lightData;

	public ClientboundLightUpdatePacket(ChunkPos chunkPos, LevelLightEngine levelLightEngine, @Nullable BitSet bitSet, @Nullable BitSet bitSet2) {
		this.x = chunkPos.x;
		this.z = chunkPos.z;
		this.lightData = new ClientboundLightUpdatePacketData(chunkPos, levelLightEngine, bitSet, bitSet2);
	}

	public ClientboundLightUpdatePacket(FriendlyByteBuf friendlyByteBuf) {
		this.x = friendlyByteBuf.readVarInt();
		this.z = friendlyByteBuf.readVarInt();
		this.lightData = new ClientboundLightUpdatePacketData(friendlyByteBuf, this.x, this.z);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.x);
		friendlyByteBuf.writeVarInt(this.z);
		this.lightData.write(friendlyByteBuf);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleLightUpdatePacket(this);
	}

	public int getX() {
		return this.x;
	}

	public int getZ() {
		return this.z;
	}

	public ClientboundLightUpdatePacketData getLightData() {
		return this.lightData;
	}
}
