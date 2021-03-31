/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundClearTitlesPacket
implements Packet<ClientGamePacketListener> {
    private final boolean resetTimes;

    public ClientboundClearTitlesPacket(boolean bl) {
        this.resetTimes = bl;
    }

    public ClientboundClearTitlesPacket(FriendlyByteBuf friendlyByteBuf) {
        this.resetTimes = friendlyByteBuf.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBoolean(this.resetTimes);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleTitlesClear(this);
    }

    public boolean shouldResetTimes() {
        return this.resetTimes;
    }
}

