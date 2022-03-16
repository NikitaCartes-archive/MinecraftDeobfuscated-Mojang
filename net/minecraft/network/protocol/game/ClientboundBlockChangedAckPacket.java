/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public record ClientboundBlockChangedAckPacket(int sequence) implements Packet<ClientGamePacketListener>
{
    public ClientboundBlockChangedAckPacket(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.sequence);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleBlockChangedAck(this);
    }
}

