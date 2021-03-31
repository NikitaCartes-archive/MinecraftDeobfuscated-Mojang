/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ClientboundRotateHeadPacket
implements Packet<ClientGamePacketListener> {
    private final int entityId;
    private final byte yHeadRot;

    public ClientboundRotateHeadPacket(Entity entity, byte b) {
        this.entityId = entity.getId();
        this.yHeadRot = b;
    }

    public ClientboundRotateHeadPacket(FriendlyByteBuf friendlyByteBuf) {
        this.entityId = friendlyByteBuf.readVarInt();
        this.yHeadRot = friendlyByteBuf.readByte();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.entityId);
        friendlyByteBuf.writeByte(this.yHeadRot);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleRotateMob(this);
    }

    public Entity getEntity(Level level) {
        return level.getEntity(this.entityId);
    }

    public byte getYHeadRot() {
        return this.yHeadRot;
    }
}

