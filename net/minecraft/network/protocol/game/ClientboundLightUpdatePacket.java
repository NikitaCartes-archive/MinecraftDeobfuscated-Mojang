/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.util.BitSet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jetbrains.annotations.Nullable;

public class ClientboundLightUpdatePacket
implements Packet<ClientGamePacketListener> {
    private final int x;
    private final int z;
    private final ClientboundLightUpdatePacketData lightData;

    public ClientboundLightUpdatePacket(ChunkPos chunkPos, LevelLightEngine levelLightEngine, @Nullable BitSet bitSet, @Nullable BitSet bitSet2, boolean bl) {
        this.x = chunkPos.x;
        this.z = chunkPos.z;
        this.lightData = new ClientboundLightUpdatePacketData(chunkPos, levelLightEngine, bitSet, bitSet2, bl);
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

    @Override
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

