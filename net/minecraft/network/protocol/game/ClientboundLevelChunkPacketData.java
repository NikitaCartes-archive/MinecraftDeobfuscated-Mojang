/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;

public class ClientboundLevelChunkPacketData {
    private static final int TWO_MEGABYTES = 0x200000;
    private final CompoundTag heightmaps;
    private final byte[] buffer;
    private final List<BlockEntityInfo> blockEntitiesData;

    public ClientboundLevelChunkPacketData(LevelChunk levelChunk) {
        this.heightmaps = new CompoundTag();
        for (Map.Entry<Heightmap.Types, Heightmap> entry : levelChunk.getHeightmaps()) {
            if (!entry.getKey().sendToClient()) continue;
            this.heightmaps.put(entry.getKey().getSerializationKey(), new LongArrayTag(entry.getValue().getRawData()));
        }
        this.buffer = new byte[ClientboundLevelChunkPacketData.calculateChunkSize(levelChunk)];
        ClientboundLevelChunkPacketData.extractChunkData(new FriendlyByteBuf(this.getWriteBuffer()), levelChunk);
        this.blockEntitiesData = Lists.newArrayList();
        for (Map.Entry<Object, Object> entry : levelChunk.getBlockEntities().entrySet()) {
            this.blockEntitiesData.add(BlockEntityInfo.create((BlockEntity)entry.getValue()));
        }
    }

    public ClientboundLevelChunkPacketData(FriendlyByteBuf friendlyByteBuf, int i, int j) {
        this.heightmaps = friendlyByteBuf.readNbt();
        if (this.heightmaps == null) {
            throw new RuntimeException("Can't read heightmap in packet for [" + i + ", " + j + "]");
        }
        int k = friendlyByteBuf.readVarInt();
        if (k > 0x200000) {
            throw new RuntimeException("Chunk Packet trying to allocate too much memory on read.");
        }
        this.buffer = new byte[k];
        friendlyByteBuf.readBytes(this.buffer);
        this.blockEntitiesData = friendlyByteBuf.readList(BlockEntityInfo::new);
    }

    public void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeNbt(this.heightmaps);
        friendlyByteBuf2.writeVarInt(this.buffer.length);
        friendlyByteBuf2.writeBytes(this.buffer);
        friendlyByteBuf2.writeCollection(this.blockEntitiesData, (friendlyByteBuf, blockEntityInfo) -> blockEntityInfo.write((FriendlyByteBuf)friendlyByteBuf));
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

    public Consumer<BlockEntityTagOutput> getBlockEntitiesTagsConsumer(int i, int j) {
        return blockEntityTagOutput -> this.getBlockEntitiesTags((BlockEntityTagOutput)blockEntityTagOutput, i, j);
    }

    private void getBlockEntitiesTags(BlockEntityTagOutput blockEntityTagOutput, int i, int j) {
        int k = 16 * i;
        int l = 16 * j;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (BlockEntityInfo blockEntityInfo : this.blockEntitiesData) {
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
            int i = friendlyByteBuf.readVarInt();
            this.type = (BlockEntityType)Registry.BLOCK_ENTITY_TYPE.byId(i);
            this.tag = friendlyByteBuf.readNbt();
        }

        void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeByte(this.packedXZ);
            friendlyByteBuf.writeShort(this.y);
            friendlyByteBuf.writeVarInt(Registry.BLOCK_ENTITY_TYPE.getId(this.type));
            friendlyByteBuf.writeNbt(this.tag);
        }

        static BlockEntityInfo create(BlockEntity blockEntity) {
            CompoundTag compoundTag = blockEntity.getUpdateTag();
            BlockPos blockPos = blockEntity.getBlockPos();
            int i = SectionPos.sectionRelative(blockPos.getX()) << 4 | SectionPos.sectionRelative(blockPos.getZ());
            return new BlockEntityInfo(i, blockPos.getY(), blockEntity.getType(), compoundTag.isEmpty() ? null : compoundTag);
        }
    }

    @FunctionalInterface
    public static interface BlockEntityTagOutput {
        public void accept(BlockPos var1, BlockEntityType<?> var2, @Nullable CompoundTag var3);
    }
}

