package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;

public class ClientboundLevelChunkPacketData {
	private static final int TWO_MEGABYTES = 2097152;
	private final CompoundTag heightmaps;
	private final byte[] buffer;
	private final List<ClientboundLevelChunkPacketData.BlockEntityInfo> blockEntitiesData;

	public ClientboundLevelChunkPacketData(LevelChunk levelChunk) {
		this.heightmaps = new CompoundTag();

		for (Entry<Heightmap.Types, Heightmap> entry : levelChunk.getHeightmaps()) {
			if (((Heightmap.Types)entry.getKey()).sendToClient()) {
				this.heightmaps.put(((Heightmap.Types)entry.getKey()).getSerializationKey(), new LongArrayTag(((Heightmap)entry.getValue()).getRawData()));
			}
		}

		this.buffer = new byte[calculateChunkSize(levelChunk)];
		extractChunkData(new FriendlyByteBuf(this.getWriteBuffer()), levelChunk);
		this.blockEntitiesData = Lists.<ClientboundLevelChunkPacketData.BlockEntityInfo>newArrayList();

		for (Entry<BlockPos, BlockEntity> entryx : levelChunk.getBlockEntities().entrySet()) {
			this.blockEntitiesData.add(ClientboundLevelChunkPacketData.BlockEntityInfo.create((BlockEntity)entryx.getValue()));
		}
	}

	public ClientboundLevelChunkPacketData(FriendlyByteBuf friendlyByteBuf, int i, int j) {
		this.heightmaps = friendlyByteBuf.readNbt();
		if (this.heightmaps == null) {
			throw new RuntimeException("Can't read heightmap in packet for [" + i + ", " + j + "]");
		} else {
			int k = friendlyByteBuf.readVarInt();
			if (k > 2097152) {
				throw new RuntimeException("Chunk Packet trying to allocate too much memory on read.");
			} else {
				this.buffer = new byte[k];
				friendlyByteBuf.readBytes(this.buffer);
				this.blockEntitiesData = friendlyByteBuf.readList(ClientboundLevelChunkPacketData.BlockEntityInfo::new);
			}
		}
	}

	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeNbt(this.heightmaps);
		friendlyByteBuf.writeVarInt(this.buffer.length);
		friendlyByteBuf.writeBytes(this.buffer);
		friendlyByteBuf.writeCollection(this.blockEntitiesData, (friendlyByteBufx, blockEntityInfo) -> blockEntityInfo.write(friendlyByteBufx));
	}

	private static int calculateChunkSize(LevelChunk levelChunk) {
		int i = 0;

		for (LevelChunkSection levelChunkSection : levelChunk.getSections()) {
			i += levelChunkSection.getSerializedSize();
		}

		return i;
	}

	private ByteBuf getWriteBuffer() {
		ByteBuf byteBuf = Unpooled.wrappedBuffer(this.buffer);
		byteBuf.writerIndex(0);
		return byteBuf;
	}

	public static void extractChunkData(FriendlyByteBuf friendlyByteBuf, LevelChunk levelChunk) {
		for (LevelChunkSection levelChunkSection : levelChunk.getSections()) {
			levelChunkSection.write(friendlyByteBuf);
		}
	}

	public Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> getBlockEntitiesTagsConsumer(int i, int j) {
		return blockEntityTagOutput -> this.getBlockEntitiesTags(blockEntityTagOutput, i, j);
	}

	private void getBlockEntitiesTags(ClientboundLevelChunkPacketData.BlockEntityTagOutput blockEntityTagOutput, int i, int j) {
		int k = 16 * i;
		int l = 16 * j;
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (ClientboundLevelChunkPacketData.BlockEntityInfo blockEntityInfo : this.blockEntitiesData) {
			int m = k + SectionPos.sectionRelative(blockEntityInfo.packedXZ >> 4);
			int n = l + SectionPos.sectionRelative(blockEntityInfo.packedXZ);
			mutableBlockPos.set(m, blockEntityInfo.y, n);
			blockEntityTagOutput.accept(mutableBlockPos, blockEntityInfo.type, blockEntityInfo.tag);
		}
	}

	public FriendlyByteBuf getReadBuffer() {
		return new FriendlyByteBuf(Unpooled.wrappedBuffer(this.buffer));
	}

	public CompoundTag getHeightmaps() {
		return this.heightmaps;
	}

	static class BlockEntityInfo {
		final int packedXZ;
		final int y;
		final BlockEntityType<?> type;
		@Nullable
		final CompoundTag tag;

		private BlockEntityInfo(int i, int j, BlockEntityType<?> blockEntityType, @Nullable CompoundTag compoundTag) {
			this.packedXZ = i;
			this.y = j;
			this.type = blockEntityType;
			this.tag = compoundTag;
		}

		private BlockEntityInfo(FriendlyByteBuf friendlyByteBuf) {
			this.packedXZ = friendlyByteBuf.readByte();
			this.y = friendlyByteBuf.readShort();
			this.type = friendlyByteBuf.readById(BuiltInRegistries.BLOCK_ENTITY_TYPE);
			this.tag = friendlyByteBuf.readNbt();
		}

		void write(FriendlyByteBuf friendlyByteBuf) {
			friendlyByteBuf.writeByte(this.packedXZ);
			friendlyByteBuf.writeShort(this.y);
			friendlyByteBuf.writeId(BuiltInRegistries.BLOCK_ENTITY_TYPE, this.type);
			friendlyByteBuf.writeNbt(this.tag);
		}

		static ClientboundLevelChunkPacketData.BlockEntityInfo create(BlockEntity blockEntity) {
			CompoundTag compoundTag = blockEntity.getUpdateTag();
			BlockPos blockPos = blockEntity.getBlockPos();
			int i = SectionPos.sectionRelative(blockPos.getX()) << 4 | SectionPos.sectionRelative(blockPos.getZ());
			return new ClientboundLevelChunkPacketData.BlockEntityInfo(i, blockPos.getY(), blockEntity.getType(), compoundTag.isEmpty() ? null : compoundTag);
		}
	}

	@FunctionalInterface
	public interface BlockEntityTagOutput {
		void accept(BlockPos blockPos, BlockEntityType<?> blockEntityType, @Nullable CompoundTag compoundTag);
	}
}
