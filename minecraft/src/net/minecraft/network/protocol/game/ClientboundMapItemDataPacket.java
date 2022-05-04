package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class ClientboundMapItemDataPacket implements Packet<ClientGamePacketListener> {
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
		this.decorations = collection != null ? Lists.<MapDecoration>newArrayList(collection) : null;
		this.colorPatch = mapPatch;
	}

	public ClientboundMapItemDataPacket(FriendlyByteBuf friendlyByteBuf) {
		this.mapId = friendlyByteBuf.readVarInt();
		this.scale = friendlyByteBuf.readByte();
		this.locked = friendlyByteBuf.readBoolean();
		this.decorations = friendlyByteBuf.readNullable(friendlyByteBufx -> friendlyByteBufx.readList(friendlyByteBufxx -> {
				MapDecoration.Type type = friendlyByteBufxx.readEnum(MapDecoration.Type.class);
				byte b = friendlyByteBufxx.readByte();
				byte c = friendlyByteBufxx.readByte();
				byte d = (byte)(friendlyByteBufxx.readByte() & 15);
				Component component = friendlyByteBufxx.readNullable(FriendlyByteBuf::readComponent);
				return new MapDecoration(type, b, c, d, component);
			}));
		int i = friendlyByteBuf.readUnsignedByte();
		if (i > 0) {
			int j = friendlyByteBuf.readUnsignedByte();
			int k = friendlyByteBuf.readUnsignedByte();
			int l = friendlyByteBuf.readUnsignedByte();
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
		friendlyByteBuf.writeNullable(this.decorations, (friendlyByteBufx, list) -> friendlyByteBufx.writeCollection(list, (friendlyByteBufxx, mapDecoration) -> {
				friendlyByteBufxx.writeEnum(mapDecoration.getType());
				friendlyByteBufxx.writeByte(mapDecoration.getX());
				friendlyByteBufxx.writeByte(mapDecoration.getY());
				friendlyByteBufxx.writeByte(mapDecoration.getRot() & 15);
				friendlyByteBufxx.writeNullable(mapDecoration.getName(), FriendlyByteBuf::writeComponent);
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
