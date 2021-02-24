/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.status;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.status.ServerStatusPacketListener;

public class ServerboundPingRequestPacket
implements Packet<ServerStatusPacketListener> {
    private final long time;

    @Environment(value=EnvType.CLIENT)
    public ServerboundPingRequestPacket(long l) {
        this.time = l;
    }

    public ServerboundPingRequestPacket(FriendlyByteBuf friendlyByteBuf) {
        this.time = friendlyByteBuf.readLong();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeLong(this.time);
    }

    @Override
    public void handle(ServerStatusPacketListener serverStatusPacketListener) {
        serverStatusPacketListener.handlePingRequest(this);
    }

    public long getTime() {
        return this.time;
    }
}

