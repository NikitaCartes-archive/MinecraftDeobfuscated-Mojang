/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.level.border.WorldBorder;

public class ClientboundSetBorderWarningDistancePacket
implements Packet<ClientGamePacketListener> {
    private final int warningBlocks;

    public ClientboundSetBorderWarningDistancePacket(WorldBorder worldBorder) {
        this.warningBlocks = worldBorder.getWarningBlocks();
    }

    public ClientboundSetBorderWarningDistancePacket(FriendlyByteBuf friendlyByteBuf) {
        this.warningBlocks = friendlyByteBuf.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.warningBlocks);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetBorderWarningDistance(this);
    }

    public int getWarningBlocks() {
        return this.warningBlocks;
    }
}

