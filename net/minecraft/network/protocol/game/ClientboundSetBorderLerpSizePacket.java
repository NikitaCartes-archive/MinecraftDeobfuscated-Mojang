/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderLerpSizePacket
implements Packet<ClientGamePacketListener> {
    private final double oldSize;
    private final double newSize;
    private final long lerpTime;

    public ClientboundSetBorderLerpSizePacket(WorldBorder worldBorder) {
        this.oldSize = worldBorder.getSize();
        this.newSize = worldBorder.getLerpTarget();
        this.lerpTime = worldBorder.getLerpRemainingTime();
    }

    public ClientboundSetBorderLerpSizePacket(FriendlyByteBuf friendlyByteBuf) {
        this.oldSize = friendlyByteBuf.readDouble();
        this.newSize = friendlyByteBuf.readDouble();
        this.lerpTime = friendlyByteBuf.readVarLong();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeDouble(this.oldSize);
        friendlyByteBuf.writeDouble(this.newSize);
        friendlyByteBuf.writeVarLong(this.lerpTime);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetBorderLerpSize(this);
    }

    @Environment(value=EnvType.CLIENT)
    public double getOldSize() {
        return this.oldSize;
    }

    @Environment(value=EnvType.CLIENT)
    public double getNewSize() {
        return this.newSize;
    }

    @Environment(value=EnvType.CLIENT)
    public long getLerpTime() {
        return this.lerpTime;
    }
}

