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

public class ClientboundTakeItemEntityPacket
implements Packet<ClientGamePacketListener> {
    private int itemId;
    private int playerId;
    private int amount;

    public ClientboundTakeItemEntityPacket() {
    }

    public ClientboundTakeItemEntityPacket(int i, int j, int k) {
        this.itemId = i;
        this.playerId = j;
        this.amount = k;
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.itemId = friendlyByteBuf.readVarInt();
        this.playerId = friendlyByteBuf.readVarInt();
        this.amount = friendlyByteBuf.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeVarInt(this.itemId);
        friendlyByteBuf.writeVarInt(this.playerId);
        friendlyByteBuf.writeVarInt(this.amount);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleTakeItemEntity(this);
    }

    @Environment(value=EnvType.CLIENT)
    public int getItemId() {
        return this.itemId;
    }

    @Environment(value=EnvType.CLIENT)
    public int getPlayerId() {
        return this.playerId;
    }

    @Environment(value=EnvType.CLIENT)
    public int getAmount() {
        return this.amount;
    }
}

