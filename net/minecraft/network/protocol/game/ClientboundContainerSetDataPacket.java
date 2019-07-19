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

public class ClientboundContainerSetDataPacket
implements Packet<ClientGamePacketListener> {
    private int containerId;
    private int id;
    private int value;

    public ClientboundContainerSetDataPacket() {
    }

    public ClientboundContainerSetDataPacket(int i, int j, int k) {
        this.containerId = i;
        this.id = j;
        this.value = k;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleContainerSetData(this);
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.containerId = friendlyByteBuf.readUnsignedByte();
        this.id = friendlyByteBuf.readShort();
        this.value = friendlyByteBuf.readShort();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeByte(this.containerId);
        friendlyByteBuf.writeShort(this.id);
        friendlyByteBuf.writeShort(this.value);
    }

    @Environment(value=EnvType.CLIENT)
    public int getContainerId() {
        return this.containerId;
    }

    @Environment(value=EnvType.CLIENT)
    public int getId() {
        return this.id;
    }

    @Environment(value=EnvType.CLIENT)
    public int getValue() {
        return this.value;
    }
}

