/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.SynchedEntityData;

public record ClientboundSetEntityDataPacket(int id, List<SynchedEntityData.DataValue<?>> packedItems) implements Packet<ClientGamePacketListener>
{
    public static final int EOF_MARKER = 255;

    public ClientboundSetEntityDataPacket(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readVarInt(), ClientboundSetEntityDataPacket.unpack(friendlyByteBuf));
    }

    private static void pack(List<SynchedEntityData.DataValue<?>> list, FriendlyByteBuf friendlyByteBuf) {
        for (SynchedEntityData.DataValue<?> dataValue : list) {
            dataValue.write(friendlyByteBuf);
        }
        friendlyByteBuf.writeByte(255);
    }

    private static List<SynchedEntityData.DataValue<?>> unpack(FriendlyByteBuf friendlyByteBuf) {
        short i;
        ArrayList list = new ArrayList();
        while ((i = friendlyByteBuf.readUnsignedByte()) != 255) {
            list.add(SynchedEntityData.DataValue.read(friendlyByteBuf, i));
        }
        return list;
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.id);
        ClientboundSetEntityDataPacket.pack(this.packedItems, friendlyByteBuf);
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetEntityData(this);
    }
}

