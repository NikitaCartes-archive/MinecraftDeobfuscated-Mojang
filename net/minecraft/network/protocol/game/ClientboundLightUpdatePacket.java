/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.BitSet;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jetbrains.annotations.Nullable;

public class ClientboundLightUpdatePacket
implements Packet<ClientGamePacketListener> {
    private int x;
    private int z;
    private BitSet skyYMask = new BitSet();
    private BitSet blockYMask = new BitSet();
    private BitSet emptySkyYMask = new BitSet();
    private BitSet emptyBlockYMask = new BitSet();
    private final List<byte[]> skyUpdates = Lists.newArrayList();
    private final List<byte[]> blockUpdates = Lists.newArrayList();
    private boolean trustEdges;

    public ClientboundLightUpdatePacket() {
    }

    public ClientboundLightUpdatePacket(ChunkPos chunkPos, LevelLightEngine levelLightEngine, @Nullable BitSet bitSet, @Nullable BitSet bitSet2, boolean bl) {
        this.x = chunkPos.x;
        this.z = chunkPos.z;
        this.trustEdges = bl;
        for (int i = 0; i < levelLightEngine.getLightSectionCount(); ++i) {
            if (bitSet == null || bitSet.get(i)) {
                ClientboundLightUpdatePacket.prepareSectionData(chunkPos, levelLightEngine, LightLayer.SKY, i, this.skyYMask, this.emptySkyYMask, this.skyUpdates);
            }
            if (bitSet2 != null && !bitSet2.get(i)) continue;
            ClientboundLightUpdatePacket.prepareSectionData(chunkPos, levelLightEngine, LightLayer.BLOCK, i, this.blockYMask, this.emptyBlockYMask, this.blockUpdates);
        }
    }

    private static void prepareSectionData(ChunkPos chunkPos, LevelLightEngine levelLightEngine, LightLayer lightLayer, int i, BitSet bitSet, BitSet bitSet2, List<byte[]> list) {
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

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        int j;
        this.x = friendlyByteBuf.readVarInt();
        this.z = friendlyByteBuf.readVarInt();
        this.trustEdges = friendlyByteBuf.readBoolean();
        this.skyYMask = friendlyByteBuf.readBitSet();
        this.blockYMask = friendlyByteBuf.readBitSet();
        this.emptySkyYMask = friendlyByteBuf.readBitSet();
        this.emptyBlockYMask = friendlyByteBuf.readBitSet();
        int i = friendlyByteBuf.readVarInt();
        for (j = 0; j < i; ++j) {
            this.skyUpdates.add(friendlyByteBuf.readByteArray(2048));
        }
        j = friendlyByteBuf.readVarInt();
        for (int k = 0; k < j; ++k) {
            this.blockUpdates.add(friendlyByteBuf.readByteArray(2048));
        }
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeVarInt(this.x);
        friendlyByteBuf.writeVarInt(this.z);
        friendlyByteBuf.writeBoolean(this.trustEdges);
        friendlyByteBuf.writeBitSet(this.skyYMask);
        friendlyByteBuf.writeBitSet(this.blockYMask);
        friendlyByteBuf.writeBitSet(this.emptySkyYMask);
        friendlyByteBuf.writeBitSet(this.emptyBlockYMask);
        friendlyByteBuf.writeVarInt(this.skyUpdates.size());
        for (byte[] bs : this.skyUpdates) {
            friendlyByteBuf.writeByteArray(bs);
        }
        friendlyByteBuf.writeVarInt(this.blockUpdates.size());
        for (byte[] bs : this.blockUpdates) {
            friendlyByteBuf.writeByteArray(bs);
        }
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleLightUpdatePacked(this);
    }

    @Environment(value=EnvType.CLIENT)
    public int getX() {
        return this.x;
    }

    @Environment(value=EnvType.CLIENT)
    public int getZ() {
        return this.z;
    }

    @Environment(value=EnvType.CLIENT)
    public BitSet getSkyYMask() {
        return this.skyYMask;
    }

    @Environment(value=EnvType.CLIENT)
    public BitSet getEmptySkyYMask() {
        return this.emptySkyYMask;
    }

    @Environment(value=EnvType.CLIENT)
    public List<byte[]> getSkyUpdates() {
        return this.skyUpdates;
    }

    @Environment(value=EnvType.CLIENT)
    public BitSet getBlockYMask() {
        return this.blockYMask;
    }

    @Environment(value=EnvType.CLIENT)
    public BitSet getEmptyBlockYMask() {
        return this.emptyBlockYMask;
    }

    @Environment(value=EnvType.CLIENT)
    public List<byte[]> getBlockUpdates() {
        return this.blockUpdates;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean getTrustEdges() {
        return this.trustEdges;
    }
}

