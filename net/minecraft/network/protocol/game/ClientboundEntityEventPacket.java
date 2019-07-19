/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ClientboundEntityEventPacket
implements Packet<ClientGamePacketListener> {
    private int entityId;
    private byte eventId;

    public ClientboundEntityEventPacket() {
    }

    public ClientboundEntityEventPacket(Entity entity, byte b) {
        this.entityId = entity.getId();
        this.eventId = b;
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.entityId = friendlyByteBuf.readInt();
        this.eventId = friendlyByteBuf.readByte();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeInt(this.entityId);
        friendlyByteBuf.writeByte(this.eventId);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleEntityEvent(this);
    }

    @Environment(value=EnvType.CLIENT)
    public Entity getEntity(Level level) {
        return level.getEntity(this.entityId);
    }

    @Environment(value=EnvType.CLIENT)
    public byte getEventId() {
        return this.eventId;
    }
}

