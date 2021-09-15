/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.BitSet;
import java.util.List;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jetbrains.annotations.Nullable;

public class ClientboundLightUpdatePacketData {
    private final BitSet skyYMask;
    private final BitSet blockYMask;
    private final BitSet emptySkyYMask;
    private final BitSet emptyBlockYMask;
    private final List<byte[]> skyUpdates;
    private final List<byte[]> blockUpdates;
    private final boolean trustEdges;

    public ClientboundLightUpdatePacketData(ChunkPos chunkPos, LevelLightEngine levelLightEngine, @Nullable BitSet bitSet, @Nullable BitSet bitSet2, boolean bl) {
        this.trustEdges = bl;
        this.skyYMask = new BitSet();
        this.blockYMask = new BitSet();
        this.emptySkyYMask = new BitSet();
        this.emptyBlockYMask = new BitSet();
        this.skyUpdates = Lists.newArrayList();
        this.blockUpdates = Lists.newArrayList();
        for (int i = 0; i < levelLightEngine.getLightSectionCount(); ++i) {
            if (bitSet == null || bitSet.get(i)) {
                this.prepareSectionData(chunkPos, levelLightEngine, LightLayer.SKY, i, this.skyYMask, this.emptySkyYMask, this.skyUpdates);
            }
            if (bitSet2 != null && !bitSet2.get(i)) continue;
            this.prepareSectionData(chunkPos, levelLightEngine, LightLayer.BLOCK, i, this.blockYMask, this.emptyBlockYMask, this.blockUpdates);
        }
    }

    public ClientboundLightUpdatePacketData(FriendlyByteBuf friendlyByteBuf2, int i, int j) {
        this.trustEdges = friendlyByteBuf2.readBoolean();
        this.skyYMask = friendlyByteBuf2.readBitSet();
        this.blockYMask = friendlyByteBuf2.readBitSet();
        this.emptySkyYMask = friendlyByteBuf2.readBitSet();
        this.emptyBlockYMask = friendlyByteBuf2.readBitSet();
        this.skyUpdates = friendlyByteBuf2.readList(friendlyByteBuf -> friendlyByteBuf.readByteArray(2048));
        this.blockUpdates = friendlyByteBuf2.readList(friendlyByteBuf -> friendlyByteBuf.readByteArray(2048));
    }

    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBoolean(this.trustEdges);
        friendlyByteBuf.writeBitSet(this.skyYMask);
        friendlyByteBuf.writeBitSet(this.blockYMask);
        friendlyByteBuf.writeBitSet(this.emptySkyYMask);
        friendlyByteBuf.writeBitSet(this.emptyBlockYMask);
        friendlyByteBuf.writeCollection(this.skyUpdates, FriendlyByteBuf::writeByteArray);
        friendlyByteBuf.writeCollection(this.blockUpdates, FriendlyByteBuf::writeByteArray);
    }

    private void prepareSectionData(ChunkPos chunkPos, LevelLightEngine levelLightEngine, LightLayer lightLayer, int i, BitSet bitSet, BitSet bitSet2, List<byte[]> list) {
        DataLayer dataLayer = levelLightEngine.getLayerListener(lightLayer).getDataLayerData(SectionPos.of(chunkPos, levelLightEngine.getMinLightSection() + i));
        if (dataLayer != null) {
            if (dataLayer.isEmpty()) {
                bitSet2.set(i);
            } else {
                bitSet.set(i);
                list.add((byte[])dataLayer.getData().clone());
            }
        }
    }

    public BitSet getSkyYMask() {
        return this.skyYMask;
    }

    public BitSet getEmptySkyYMask() {
        return this.emptySkyYMask;
    }

    public List<byte[]> getSkyUpdates() {
        return this.skyUpdates;
    }

    public BitSet getBlockYMask() {
        return this.blockYMask;
    }

    public BitSet getEmptyBlockYMask() {
        return this.emptyBlockYMask;
    }

    public List<byte[]> getBlockUpdates() {
        return this.blockUpdates;
    }

    public boolean getTrustEdges() {
        return this.trustEdges;
    }
}

