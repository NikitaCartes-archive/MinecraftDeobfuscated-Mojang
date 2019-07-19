/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundSetSpawnPositionPacket
implements Packet<ClientGamePacketListener> {
    private BlockPos pos;

    public ClientboundSetSpawnPositionPacket() {
    }

    public ClientboundSetSpawnPositionPacket(BlockPos blockPos) {
        this.pos = blockPos;
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.pos = friendlyByteBuf.readBlockPos();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeBlockPos(this.pos);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetSpawn(this);
    }

    @Environment(value=EnvType.CLIENT)
    public BlockPos getPos() {
        return this.pos;
    }
}

