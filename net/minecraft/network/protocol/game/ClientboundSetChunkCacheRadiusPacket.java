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

public class ClientboundSetChunkCacheRadiusPacket
implements Packet<ClientGamePacketListener> {
    private int radius;

    public ClientboundSetChunkCacheRadiusPacket() {
    }

    public ClientboundSetChunkCacheRadiusPacket(int i) {
        this.radius = i;
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.radius = friendlyByteBuf.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeVarInt(this.radius);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetChunkCacheRadius(this);
    }

    @Environment(value=EnvType.CLIENT)
    public int getRadius() {
        return this.radius;
    }
}

