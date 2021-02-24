/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundContainerAckPacket
implements Packet<ClientGamePacketListener> {
    private final int containerId;
    private final short uid;
    private final boolean accepted;

    public ClientboundContainerAckPacket(int i, short s, boolean bl) {
        this.containerId = i;
        this.uid = s;
        this.accepted = bl;
    }

    public ClientboundContainerAckPacket(FriendlyByteBuf friendlyByteBuf) {
        this.containerId = friendlyByteBuf.readUnsignedByte();
        this.uid = friendlyByteBuf.readShort();
        this.accepted = friendlyByteBuf.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeByte(this.containerId);
        friendlyByteBuf.writeShort(this.uid);
        friendlyByteBuf.writeBoolean(this.accepted);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleContainerAck(this);
    }

    @Environment(value=EnvType.CLIENT)
    public int getContainerId() {
        return this.containerId;
    }

    @Environment(value=EnvType.CLIENT)
    public short getUid() {
        return this.uid;
    }

    @Environment(value=EnvType.CLIENT)
    public boolean isAccepted() {
        return this.accepted;
    }
}

