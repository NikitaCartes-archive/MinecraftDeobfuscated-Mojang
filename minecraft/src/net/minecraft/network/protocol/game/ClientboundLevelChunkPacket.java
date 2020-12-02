package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;

public class ClientboundLevelChunkPacket implements Packet<ClientGamePacketListener> {
	private int x;
	private int z;
	private int availableSections;
	private CompoundTag heightmaps;
	@Nullable
	private int[] biomes;
	private byte[] buffer;
	private List<CompoundTag> blockEntitiesTags;

	public ClientboundLevelChunkPacket() {
	}

	public ClientboundLevelChunkPacket(LevelChunk levelChunk) {
		ChunkPos chunkPos = levelChunk.getPos();
		this.x = chunkPos.x;
		this.z = chunkPos.z;
		this.heightmaps = new CompoundTag();

		for (Entry<Heightmap.Types, Heightmap> entry : levelChunk.getHeightmaps()) {
			if (((Heightmap.Types)entry.getKey()).sendToClient()) {
				this.heightmaps.put(((Heightmap.Types)entry.getKey()).getSerializationKey(), new LongArrayTag(((Heightmap)entry.getValue()).getRawData()));
			}
		}

		this.biomes = levelChunk.getBiomes().writeBiomes();
		this.buffer = new byte[this.calculateChunkSize(levelChunk)];
		this.availableSections = this.extractChunkData(new FriendlyByteBuf(this.getWriteBuffer()), levelChunk);
		this.blockEntitiesTags = Lists.<CompoundTag>newArrayList();

		for (Entry<BlockPos, BlockEntity> entryx : levelChunk.getBlockEntities().entrySet()) {
			BlockEntity blockEntity = (BlockEntity)entryx.getValue();
			CompoundTag compoundTag = blockEntity.getUpdateTag();
			this.blockEntitiesTags.add(compoundTag);
		}
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.x = friendlyByteBuf.readInt();
		this.z = friendlyByteBuf.readInt();
		this.availableSections = friendlyByteBuf.readVarInt();
		this.heightmaps = friendlyByteBuf.readNbt();
		this.biomes = friendlyByteBuf.readVarIntArray(ChunkBiomeContainer.MAX_SIZE);
		int i = friendlyByteBuf.readVarInt();
		if (i > 2097152) {
			throw new RuntimeException("Chunk Packet trying to allocate too much memory on read.");
		} else {
			this.buffer = new byte[i];
			friendlyByteBuf.readBytes(this.buffer);
			int j = friendlyByteBuf.readVarInt();
			this.blockEntitiesTags = Lists.<CompoundTag>newArrayList();

			for (int k = 0; k < j; k++) {
				this.blockEntitiesTags.add(friendlyByteBuf.readNbt());
			}
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeInt(this.x);
		friendlyByteBuf.writeInt(this.z);
		friendlyByteBuf.writeVarInt(this.availableSections);
		friendlyByteBuf.writeNbt(this.heightmaps);
		if (this.biomes != null) {
			friendlyByteBuf.writeVarIntArray(this.biomes);
		}

		friendlyByteBuf.writeVarInt(this.buffer.length);
		friendlyByteBuf.writeBytes(this.buffer);
		friendlyByteBuf.writeVarInt(this.blockEntitiesTags.size());

		for (CompoundTag compoundTag : this.blockEntitiesTags) {
			friendlyByteBuf.writeNbt(compoundTag);
		}
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleLevelChunk(this);
	}

	@Environment(EnvType.CLIENT)
	public FriendlyByteBuf getReadBuffer() {
		return new FriendlyByteBuf(Unpooled.wrappedBuffer(this.buffer));
	}

	private ByteBuf getWriteBuffer() {
		ByteBuf byteBuf = Unpooled.wrappedBuffer(this.buffer);
		byteBuf.writerIndex(0);
		return byteBuf;
	}

	public int extractChunkData(FriendlyByteBuf friendlyByteBuf, LevelChunk levelChunk) {
		int i = 0;
		LevelChunkSection[] levelChunkSections = levelChunk.getSections();
		int j = 0;

		for (int k = levelChunkSections.length; j < k; j++) {
			LevelChunkSection levelChunkSection = levelChunkSections[j];
			if (levelChunkSection != LevelChunk.EMPTY_SECTION && !levelChunkSection.isEmpty()) {
				i |= 1 << j;
				levelChunkSection.write(friendlyByteBuf);
			}
		}

		return i;
	}

	protected int calculateChunkSize(LevelChunk levelChunk) {
		int i = 0;
		LevelChunkSection[] levelChunkSections = levelChunk.getSections();
		int j = 0;

		for (int k = levelChunkSections.length; j < k; j++) {
			LevelChunkSection levelChunkSection = levelChunkSections[j];
			if (levelChunkSection != LevelChunk.EMPTY_SECTION && !levelChunkSection.isEmpty()) {
				i += levelChunkSection.getSerializedSize();
			}
		}

		return i;
	}

	@Environment(EnvType.CLIENT)
	public int getX() {
		return this.x;
	}

	@Environment(EnvType.CLIENT)
	public int getZ() {
		return this.z;
	}

	@Environment(EnvType.CLIENT)
	public int getAvailableSections() {
		return this.availableSections;
	}

	@Environment(EnvType.CLIENT)
	public CompoundTag getHeightmaps() {
		return this.heightmaps;
	}

	@Environment(EnvType.CLIENT)
	public List<CompoundTag> getBlockEntitiesTags() {
		return this.blockEntitiesTags;
	}

	@Nullable
	@Environment(EnvType.CLIENT)
	public int[] getBiomes() {
		return this.biomes;
	}
}
