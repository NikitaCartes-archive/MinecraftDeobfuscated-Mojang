package net.minecraft.network.protocol.game;

import java.util.BitSet;
import javax.annotation.Nullable;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class ClientboundLevelChunkWithLightPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundLevelChunkWithLightPacket> STREAM_CODEC = Packet.codec(
		ClientboundLevelChunkWithLightPacket::write, ClientboundLevelChunkWithLightPacket::new
	);
	private final int x;
	private final int z;
	private final ClientboundLevelChunkPacketData chunkData;
	private final ClientboundLightUpdatePacketData lightData;

	public ClientboundLevelChunkWithLightPacket(LevelChunk levelChunk, LevelLightEngine levelLightEngine, @Nullable BitSet bitSet, @Nullable BitSet bitSet2) {
		ChunkPos chunkPos = levelChunk.getPos();
		this.x = chunkPos.x;
		this.z = chunkPos.z;
		this.chunkData = new ClientboundLevelChunkPacketData(levelChunk);
		this.lightData = new ClientboundLightUpdatePacketData(chunkPos, levelLightEngine, bitSet, bitSet2);
	}

	private ClientboundLevelChunkWithLightPacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		this.x = registryFriendlyByteBuf.readInt();
		this.z = registryFriendlyByteBuf.readInt();
		this.chunkData = new ClientboundLevelChunkPacketData(registryFriendlyByteBuf, this.x, this.z);
		this.lightData = new ClientboundLightUpdatePacketData(registryFriendlyByteBuf, this.x, this.z);
	}

	private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		registryFriendlyByteBuf.writeInt(this.x);
		registryFriendlyByteBuf.writeInt(this.z);
		this.chunkData.write(registryFriendlyByteBuf);
		this.lightData.write(registryFriendlyByteBuf);
	}

	@Override
	public PacketType<ClientboundLevelChunkWithLightPacket> type() {
		return GamePacketTypes.CLIENTBOUND_LEVEL_CHUNK_WITH_LIGHT;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleLevelChunkWithLight(this);
	}

	public int getX() {
		return this.x;
	}

	public int getZ() {
		return this.z;
	}

	public ClientboundLevelChunkPacketData getChunkData() {
		return this.chunkData;
	}

	public ClientboundLightUpdatePacketData getLightData() {
		return this.lightData;
	}
}
