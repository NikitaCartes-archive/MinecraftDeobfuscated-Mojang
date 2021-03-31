/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ClientboundEntityEventPacket
implements Packet<ClientGamePacketListener> {
    private final int entityId;
    private final byte eventId;

    public ClientboundEntityEventPacket(Entity entity, byte b) {
        this.entityId = entity.getId();
        this.eventId = b;
    }

    public ClientboundEntityEventPacket(FriendlyByteBuf friendlyByteBuf) {
        this.entityId = friendlyByteBuf.readInt();
        this.eventId = friendlyByteBuf.readByte();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(this.entityId);
        friendlyByteBuf.writeByte(this.eventId);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleEntityEvent(this);
    }

    @Nullable
    public Entity getEntity(Level level) {
        return level.getEntity(this.entityId);
    }

    public byte getEventId() {
        return this.eventId;
    }
}

