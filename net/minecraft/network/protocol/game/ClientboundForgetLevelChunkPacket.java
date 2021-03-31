/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundForgetLevelChunkPacket
implements Packet<ClientGamePacketListener> {
    private final int x;
    private final int z;

    public ClientboundForgetLevelChunkPacket(int i, int j) {
        this.x = i;
        this.z = j;
    }

    public ClientboundForgetLevelChunkPacket(FriendlyByteBuf friendlyByteBuf) {
        this.x = friendlyByteBuf.readInt();
        this.z = friendlyByteBuf.readInt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(this.x);
        friendlyByteBuf.writeInt(this.z);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleForgetLevelChunk(this);
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }
}

