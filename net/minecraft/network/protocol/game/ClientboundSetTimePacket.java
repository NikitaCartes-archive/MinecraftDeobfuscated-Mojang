/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundSetTimePacket
implements Packet<ClientGamePacketListener> {
    private final long gameTime;
    private final long dayTime;

    public ClientboundSetTimePacket(long l, long m, boolean bl) {
        this.gameTime = l;
        long n = m;
        if (!bl && (n = -n) == 0L) {
            n = -1L;
        }
        this.dayTime = n;
    }

    public ClientboundSetTimePacket(FriendlyByteBuf friendlyByteBuf) {
        this.gameTime = friendlyByteBuf.readLong();
        this.dayTime = friendlyByteBuf.readLong();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeLong(this.gameTime);
        friendlyByteBuf.writeLong(this.dayTime);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetTime(this);
    }

    @Environment(value=EnvType.CLIENT)
    public long getGameTime() {
        return this.gameTime;
    }

    @Environment(value=EnvType.CLIENT)
    public long getDayTime() {
        return this.dayTime;
    }
}

