/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

public class ClientboundRemoveEntitiesPacket
implements Packet<ClientGamePacketListener> {
    private final IntList entityIds;

    public ClientboundRemoveEntitiesPacket(IntList intList) {
        this.entityIds = new IntArrayList(intList);
    }

    public ClientboundRemoveEntitiesPacket(int ... is) {
        this.entityIds = new IntArrayList(is);
    }

    public ClientboundRemoveEntitiesPacket(FriendlyByteBuf friendlyByteBuf) {
        this.entityIds = friendlyByteBuf.readIntIdList();
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeIntIdList(this.entityIds);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleRemoveEntities(this);
    }

    public IntList getEntityIds() {
        return this.entityIds;
    }
}

