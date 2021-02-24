/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundKeepAlivePacket
implements Packet<ClientGamePacketListener> {
    private final long id;

    public ClientboundKeepAlivePacket(long l) {
        this.id = l;
    }

    public ClientboundKeepAlivePacket(FriendlyByteBuf friendlyByteBuf) {
        this.id = friendlyByteBuf.readLong();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeLong(this.id);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleKeepAlive(this);
    }

    @Environment(value=EnvType.CLIENT)
    public long getId() {
        return this.id;
    }
}

