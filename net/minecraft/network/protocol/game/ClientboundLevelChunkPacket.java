/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;

public class ClientboundLevelChunkPacket
implements Packet<ClientGamePacketListener> {
    public static final int TWO_MEGABYTES = 0x200000;
    private final int x;
    private final int z;
    private final BitSet availableSections;
    private final CompoundTag heightmaps;
    private final int[] biomes;
    private final byte[] buffer;
    private final List<CompoundTag> blockEntitiesTags;

    public ClientboundLevelChunkPacket(LevelChunk levelChunk) {
        ChunkPos chunkPos = levelChunk.getPos();
        this.x = chunkPos.x;
        this.z = chunkPos.z;
        this.heightmaps = new CompoundTag();
        for (Map.Entry<Heightmap.Types, Heightmap> entry : levelChunk.getHeightmaps()) {
            if (!entry.getKey().sendToClient()) continue;
            this.heightmaps.put(entry.getKey().getSerializationKey(), new LongArrayTag(entry.getValue().getRawData()));
        }
        this.biomes = levelChunk.getBiomes().writeBiomes();
        this.buffer = new byte[this.calculateChunkSize(levelChunk)];
        this.availableSections = this.extractChunkData(new FriendlyByteBuf(this.getWriteBuffer()), levelChunk);
        this.blockEntitiesTags = Lists.newArrayList();
        for (Map.Entry<Object, Object> entry : levelChunk.getBlockEntities().entrySet()) {
            BlockEntity blockEntity = (BlockEntity)entry.getValue();
            CompoundTag compoundTag = blockEntity.getUpdateTag();
            this.blockEntitiesTags.add(compoundTag);
        }
    }

    public ClientboundLevelChunkPacket(FriendlyByteBuf friendlyByteBuf) {
        this.x = friendlyByteBuf.readInt();
        this.z = friendlyByteBuf.readInt();
        this.availableSections = friendlyByteBuf.readBitSet();
        this.heightmaps = friendlyByteBuf.readNbt();
        if (this.heightmaps == null) {
            throw new RuntimeException("Can't read heightmap in packet for [" + this.x + ", " + this.z + "]");
        }
        this.biomes = friendlyByteBuf.readVarIntArray(ChunkBiomeContainer.MAX_SIZE);
        int i = friendlyByteBuf.readVarInt();
        if (i > 0x200000) {
            throw new RuntimeException("Chunk Packet trying to allocate too much memory on read.");
        }
        this.buffer = new byte[i];
        friendlyByteBuf.readBytes(this.buffer);
        this.blockEntitiesTags = friendlyByteBuf.readList(FriendlyByteBuf::readNbt);
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(this.x);
        friendlyByteBuf.writeInt(this.z);
        friendlyByteBuf.writeBitSet(this.availableSections);
        friendlyByteBuf.writeNbt(this.heightmaps);
        friendlyByteBuf.writeVarIntArray(this.biomes);
        friendlyByteBuf.writeVarInt(this.buffer.length);
        friendlyByteBuf.writeBytes(this.buffer);
        friendlyByteBuf.writeCollection(this.blockEntitiesTags, FriendlyByteBuf::writeNbt);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleLevelChunk(this);
    }

    public FriendlyByteBuf getReadBuffer() {
        return new FriendlyByteBuf(Unpooled.wrappedBuffer(this.buffer));
    }

    private ByteBuf getWriteBuffer() {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(this.buffer);
        byteBuf.writerIndex(0);
        return byteBuf;
    }

    public BitSet extractChunkData(FriendlyByteBuf friendlyByteBuf, LevelChunk levelChunk) {
        BitSet bitSet = new BitSet();
        LevelChunkSection[] levelChunkSections = levelChunk.getSections();
        int j = levelChunkSections.length;
        for (int i = 0; i < j; ++i) {
            LevelChunkSection levelChunkSection = levelChunkSections[i];
            if (levelChunkSection == LevelChunk.EMPTY_SECTION || levelChunkSection.isEmpty()) continue;
            bitSet.set(i);
            levelChunkSection.write(friendlyByteBuf);
        }
        return bitSet;
    }

    protected int calculateChunkSize(LevelChunk levelChunk) {
        int i = 0;
        for (LevelChunkSection levelChunkSection : levelChunk.getSections()) {
            if (levelChunkSection == LevelChunk.EMPTY_SECTION || levelChunkSection.isEmpty()) continue;
            i += levelChunkSection.getSerializedSize();
        }
        return i;
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    public BitSet getAvailableSections() {
        return this.availableSections;
    }

    public CompoundTag getHeightmaps() {
        return this.heightmaps;
    }

    public List<CompoundTag> getBlockEntitiesTags() {
        return this.blockEntitiesTags;
    }

    public int[] getBiomes() {
        return this.biomes;
    }
}

