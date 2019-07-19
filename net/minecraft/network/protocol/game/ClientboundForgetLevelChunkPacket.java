/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundForgetLevelChunkPacket
implements Packet<ClientGamePacketListener> {
    private int x;
    private int z;

    public ClientboundForgetLevelChunkPacket() {
    }

    public ClientboundForgetLevelChunkPacket(int i, int j) {
        this.x = i;
        this.z = j;
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.x = friendlyByteBuf.readInt();
        this.z = friendlyByteBuf.readInt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeInt(this.x);
        friendlyByteBuf.writeInt(this.z);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleForgetLevelChunk(this);
    }

    @Environment(value=EnvType.CLIENT)
    public int getX() {
        return this.x;
    }

    @Environment(value=EnvType.CLIENT)
    public int getZ() {
        return this.z;
    }
}

