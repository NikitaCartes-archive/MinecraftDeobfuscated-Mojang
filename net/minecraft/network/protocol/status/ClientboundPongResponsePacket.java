/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.status;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.status.ClientStatusPacketListener;

public class ClientboundPongResponsePacket
implements Packet<ClientStatusPacketListener> {
    private final long time;

    public ClientboundPongResponsePacket(long l) {
        this.time = l;
    }

    public ClientboundPongResponsePacket(FriendlyByteBuf friendlyByteBuf) {
        this.time = friendlyByteBuf.readLong();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeLong(this.time);
    }

    @Override
    public void handle(ClientStatusPacketListener clientStatusPacketListener) {
        clientStatusPacketListener.handlePongResponse(this);
    }

    public long getTime() {
        return this.time;
    }
}

