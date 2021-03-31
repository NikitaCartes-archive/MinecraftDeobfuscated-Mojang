/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundSetDefaultSpawnPositionPacket
implements Packet<ClientGamePacketListener> {
    private final BlockPos pos;
    private final float angle;

    public ClientboundSetDefaultSpawnPositionPacket(BlockPos blockPos, float f) {
        this.pos = blockPos;
        this.angle = f;
    }

    public ClientboundSetDefaultSpawnPositionPacket(FriendlyByteBuf friendlyByteBuf) {
        this.pos = friendlyByteBuf.readBlockPos();
        this.angle = friendlyByteBuf.readFloat();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBlockPos(this.pos);
        friendlyByteBuf.writeFloat(this.angle);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetSpawn(this);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public float getAngle() {
        return this.angle;
    }
}

