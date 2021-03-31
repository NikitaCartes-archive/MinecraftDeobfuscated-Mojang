/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderWarningDelayPacket
implements Packet<ClientGamePacketListener> {
    private final int warningDelay;

    public ClientboundSetBorderWarningDelayPacket(WorldBorder worldBorder) {
        this.warningDelay = worldBorder.getWarningTime();
    }

    public ClientboundSetBorderWarningDelayPacket(FriendlyByteBuf friendlyByteBuf) {
        this.warningDelay = friendlyByteBuf.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.warningDelay);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetBorderWarningDelay(this);
    }

    public int getWarningDelay() {
        return this.warningDelay;
    }
}

