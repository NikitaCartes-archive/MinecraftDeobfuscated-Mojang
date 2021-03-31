/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public class ServerboundAcceptTeleportationPacket
implements Packet<ServerGamePacketListener> {
    private final int id;

    public ServerboundAcceptTeleportationPacket(int i) {
        this.id = i;
    }

    public ServerboundAcceptTeleportationPacket(FriendlyByteBuf friendlyByteBuf) {
        this.id = friendlyByteBuf.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.id);
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleAcceptTeleportPacket(this);
    }

    public int getId() {
        return this.id;
    }
}

