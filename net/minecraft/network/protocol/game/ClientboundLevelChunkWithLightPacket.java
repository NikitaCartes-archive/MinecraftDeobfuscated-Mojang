/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.util.BitSet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jetbrains.annotations.Nullable;

public class ClientboundLevelChunkWithLightPacket
implements Packet<ClientGamePacketListener> {
    private final int x;
    private final int z;
    private final ClientboundLevelChunkPacketData chunkData;
    private final ClientboundLightUpdatePacketData lightData;

    public ClientboundLevelChunkWithLightPacket(LevelChunk levelChunk, LevelLightEngine levelLightEngine, @Nullable BitSet bitSet, @Nullable BitSet bitSet2, boolean bl) {
        ChunkPos chunkPos = levelChunk.getPos();
        this.x = chunkPos.x;
        this.z = chunkPos.z;
        this.chunkData = new ClientboundLevelChunkPacketData(levelChunk);
        this.lightData = new ClientboundLightUpdatePacketData(chunkPos, levelLightEngine, bitSet, bitSet2, bl);
    }

    public ClientboundLevelChunkWithLightPacket(FriendlyByteBuf friendlyByteBuf) {
        this.x = friendlyByteBuf.readInt();
        this.z = friendlyByteBuf.readInt();
        this.chunkData = new ClientboundLevelChunkPacketData(friendlyByteBuf, this.x, this.z);
        this.lightData = new ClientboundLightUpdatePacketData(friendlyByteBuf, this.x, this.z);
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(this.x);
        friendlyByteBuf.writeInt(this.z);
        this.chunkData.write(friendlyByteBuf);
        this.lightData.write(friendlyByteBuf);
    }

    @Override
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

