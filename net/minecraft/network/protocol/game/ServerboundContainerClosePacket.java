/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public class ServerboundContainerClosePacket
implements Packet<ServerGamePacketListener> {
    private final int containerId;

    public ServerboundContainerClosePacket(int i) {
        this.containerId = i;
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleContainerClose(this);
    }

    public ServerboundContainerClosePacket(FriendlyByteBuf friendlyByteBuf) {
        this.containerId = friendlyByteBuf.readByte();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeByte(this.containerId);
    }

    public int getContainerId() {
        return this.containerId;
    }
}

