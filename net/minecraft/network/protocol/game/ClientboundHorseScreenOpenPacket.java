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

public class ClientboundHorseScreenOpenPacket
implements Packet<ClientGamePacketListener> {
    private int containerId;
    private int size;
    private int entityId;

    public ClientboundHorseScreenOpenPacket() {
    }

    public ClientboundHorseScreenOpenPacket(int i, int j, int k) {
        this.containerId = i;
        this.size = j;
        this.entityId = k;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleHorseScreenOpen(this);
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
        this.containerId = friendlyByteBuf.readUnsignedByte();
        this.size = friendlyByteBuf.readVarInt();
        this.entityId = friendlyByteBuf.readInt();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
        friendlyByteBuf.writeByte(this.containerId);
        friendlyByteBuf.writeVarInt(this.size);
        friendlyByteBuf.writeInt(this.entityId);
    }

    @Environment(value=EnvType.CLIENT)
    public int getContainerId() {
        return this.containerId;
    }

    @Environment(value=EnvType.CLIENT)
    public int getSize() {
        return this.size;
    }

    @Environment(value=EnvType.CLIENT)
    public int getEntityId() {
        return this.entityId;
    }
}

