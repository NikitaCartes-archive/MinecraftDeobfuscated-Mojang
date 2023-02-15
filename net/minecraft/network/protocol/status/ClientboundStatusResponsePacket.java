/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.status;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.status.ClientStatusPacketListener;
import net.minecraft.network.protocol.status.ServerStatus;

public record ClientboundStatusResponsePacket(ServerStatus status) implements Packet<ClientStatusPacketListener>
{
    public ClientboundStatusResponsePacket(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readJsonWithCodec(ServerStatus.CODEC));
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeJsonWithCodec(ServerStatus.CODEC, this.status);
    }

    @Override
    public void handle(ClientStatusPacketListener clientStatusPacketListener) {
        clientStatusPacketListener.handleStatusResponse(this);
    }
}

