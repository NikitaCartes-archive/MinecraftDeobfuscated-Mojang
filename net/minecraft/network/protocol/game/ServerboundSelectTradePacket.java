/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public class ServerboundSelectTradePacket
implements Packet<ServerGamePacketListener> {
    private final int item;

    public ServerboundSelectTradePacket(int i) {
        this.item = i;
    }

    public ServerboundSelectTradePacket(FriendlyByteBuf friendlyByteBuf) {
        this.item = friendlyByteBuf.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.item);
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleSelectTrade(this);
    }

    public int getItem() {
        return this.item;
    }
}

