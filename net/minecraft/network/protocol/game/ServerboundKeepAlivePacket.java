/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public class ServerboundKeepAlivePacket
implements Packet<ServerGamePacketListener> {
    private final long id;

    public ServerboundKeepAlivePacket(long l) {
        this.id = l;
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleKeepAlive(this);
    }

    public ServerboundKeepAlivePacket(FriendlyByteBuf friendlyByteBuf) {
        this.id = friendlyByteBuf.readLong();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeLong(this.id);
    }

    public long getId() {
        return this.id;
    }
}

