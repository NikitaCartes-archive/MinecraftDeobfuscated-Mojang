/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.status;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.status.ServerStatusPacketListener;

public class ServerboundStatusRequestPacket
implements Packet<ServerStatusPacketListener> {
    public ServerboundStatusRequestPacket() {
    }

    public ServerboundStatusRequestPacket(FriendlyByteBuf friendlyByteBuf) {
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
    }

    @Override
    public void handle(ServerStatusPacketListener serverStatusPacketListener) {
        serverStatusPacketListener.handleStatusRequest(this);
    }
}

