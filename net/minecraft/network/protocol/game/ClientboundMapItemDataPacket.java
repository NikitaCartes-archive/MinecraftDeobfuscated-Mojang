/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

public class ClientboundMapItemDataPacket
implements Packet<ClientGamePacketListener> {
    private final int mapId;
    private final byte scale;
    private final boolean locked;
    @Nullable
    private final List<MapDecoration> decorations;
    @Nullable
    private final MapItemSavedData.MapPatch colorPatch;

    public ClientboundMapItemDataPacket(int i, byte b, boolean bl, @Nullable Collection<MapDecoration> collection, @Nullable MapItemSavedData.MapPatch mapPatch) {
        this.mapId = i;
        this.scale = b;
        this.locked = bl;
        this.decorations = collection != null ? Lists.newArrayList(collection) : null;
        this.colorPatch = mapPatch;
    }

    public ClientboundMapItemDataPacket(FriendlyByteBuf friendlyByteBuf) {
        this.mapId = friendlyByteBuf.readVarInt();
        this.scale = friendlyByteBuf.readByte();
        this.locked = friendlyByteBuf.readBoolean();
        this.decorations = (List)friendlyByteBuf.readNullable(friendlyByteBuf2 -> friendlyByteBuf2.readList(friendlyByteBuf -> {
            MapDecoration.Type type = friendlyByteBuf.readEnum(MapDecoration.Type.class);
            byte b = friendlyByteBuf.readByte();
            byte c = friendlyByteBuf.readByte();
            byte d = (byte)(friendlyByteBuf.readByte() & 0xF);
            Component component = (Component)friendlyByteBuf.readNullable(FriendlyByteBuf::readComponent);
            return new MapDecoration(type, b, c, d, component);
        }));
        short i = friendlyByteBuf.readUnsignedByte();
        if (i > 0) {
            short j = friendlyByteBuf.readUnsignedByte();
            short k = friendlyByteBuf.readUnsignedByte();
            short l = friendlyByteBuf.readUnsignedByte();
            byte[] bs = friendlyByteBuf.readByteArray();
            this.colorPatch = new MapItemSavedData.MapPatch(k, l, i, j, bs);
        } else {
            this.colorPatch = null;
        }
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.mapId);
        friendlyByteBuf.writeByte(this.scale);
        friendlyByteBuf.writeBoolean(this.locked);
        friendlyByteBuf.writeNullable(this.decorations, (friendlyByteBuf2, list) -> friendlyByteBuf2.writeCollection(list, (friendlyByteBuf, mapDecoration) -> {
            friendlyByteBuf.writeEnum(mapDecoration.getType());
            friendlyByteBuf.writeByte(mapDecoration.getX());
            friendlyByteBuf.writeByte(mapDecoration.getY());
            friendlyByteBuf.writeByte(mapDecoration.getRot() & 0xF);
            friendlyByteBuf.writeNullable(mapDecoration.getName(), FriendlyByteBuf::writeComponent);
        }));
        if (this.colorPatch != null) {
            friendlyByteBuf.writeByte(this.colorPatch.width);
            friendlyByteBuf.writeByte(this.colorPatch.height);
            friendlyByteBuf.writeByte(this.colorPatch.startX);
            friendlyByteBuf.writeByte(this.colorPatch.startY);
            friendlyByteBuf.writeByteArray(this.colorPatch.mapColors);
        } else {
            friendlyByteBuf.writeByte(0);
        }
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleMapItemData(this);
    }

    public int getMapId() {
        return this.mapId;
    }

    public void applyToMap(MapItemSavedData mapItemSavedData) {
        if (this.decorations != null) {
            mapItemSavedData.addClientSideDecorations(this.decorations);
        }
        if (this.colorPatch != null) {
            this.colorPatch.applyToMap(mapItemSavedData);
        }
    }

    public byte getScale() {
        return this.scale;
    }

    public boolean isLocked() {
        return this.locked;
    }
}

