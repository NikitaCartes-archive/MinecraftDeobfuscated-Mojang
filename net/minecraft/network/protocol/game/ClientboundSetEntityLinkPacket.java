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
import org.jetbrains.annotations.Nullable;

public class ClientboundSetEntityLinkPacket
implements Packet<ClientGamePacketListener> {
    private int sourceId;
    private int destId;

    public ClientboundSetEntityLinkPacket() {
    }

    public ClientboundSetEntityLinkPacket(Entity entity, @Nullable Entity entity2) {
        this.sourceId = entity.getId();
        this.destId = entity2 != null ? entity2.getId() : 0;
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.sourceId = friendlyByteBuf.readInt();
        this.destId = friendlyByteBuf.readInt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeInt(this.sourceId);
        friendlyByteBuf.writeInt(this.destId);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleEntityLinkPacket(this);
    }

    @Environment(value=EnvType.CLIENT)
    public int getSourceId() {
        return this.sourceId;
    }

    @Environment(value=EnvType.CLIENT)
    public int getDestId() {
        return this.destId;
    }
}

