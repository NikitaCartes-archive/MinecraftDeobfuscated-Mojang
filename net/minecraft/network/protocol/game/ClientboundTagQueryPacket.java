/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import org.jetbrains.annotations.Nullable;

public class ClientboundTagQueryPacket
implements Packet<ClientGamePacketListener> {
    private int transactionId;
    @Nullable
    private CompoundTag tag;

    public ClientboundTagQueryPacket() {
    }

    public ClientboundTagQueryPacket(int i, @Nullable CompoundTag compoundTag) {
        this.transactionId = i;
        this.tag = compoundTag;
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.transactionId = friendlyByteBuf.readVarInt();
        this.tag = friendlyByteBuf.readNbt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeVarInt(this.transactionId);
        friendlyByteBuf.writeNbt(this.tag);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleTagQueryPacket(this);
    }

    @Environment(value=EnvType.CLIENT)
    public int getTransactionId() {
        return this.transactionId;
    }

    @Nullable
    @Environment(value=EnvType.CLIENT)
    public CompoundTag getTag() {
        return this.tag;
    }

    @Override
    public boolean isSkippable() {
        return true;
    }
}

