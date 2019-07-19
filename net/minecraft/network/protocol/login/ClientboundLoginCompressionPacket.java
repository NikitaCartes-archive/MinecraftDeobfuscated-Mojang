/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.login;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.login.ClientLoginPacketListener;

public class ClientboundLoginCompressionPacket
implements Packet<ClientLoginPacketListener> {
    private int compressionThreshold;

    public ClientboundLoginCompressionPacket() {
    }

    public ClientboundLoginCompressionPacket(int i) {
        this.compressionThreshold = i;
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.compressionThreshold = friendlyByteBuf.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeVarInt(this.compressionThreshold);
    }

    @Override
    public void handle(ClientLoginPacketListener clientLoginPacketListener) {
        clientLoginPacketListener.handleCompression(this);
    }

    @Environment(value=EnvType.CLIENT)
    public int getCompressionThreshold() {
        return this.compressionThreshold;
    }
}

