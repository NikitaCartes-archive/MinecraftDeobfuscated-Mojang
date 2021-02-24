package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
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
		if (friendlyByteBuf.readBoolean()) {
			this.decorations = friendlyByteBuf.readList(
				friendlyByteBufx -> {
					MapDecoration.Type type = friendlyByteBufx.readEnum(MapDecoration.Type.class);
					return new MapDecoration(
						type,
						friendlyByteBufx.readByte(),
						friendlyByteBufx.readByte(),
						(byte)(friendlyByteBufx.readByte() & 15),
						friendlyByteBufx.readBoolean() ? friendlyByteBufx.readComponent() : null
					);
				}
			);
		} else {
			this.decorations = null;
		}

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
		if (this.decorations != null) {
			friendlyByteBuf.writeBoolean(true);
			friendlyByteBuf.writeCollection(this.decorations, (friendlyByteBufx, mapDecoration) -> {
				friendlyByteBufx.writeEnum(mapDecoration.getType());
				friendlyByteBufx.writeByte(mapDecoration.getX());
				friendlyByteBufx.writeByte(mapDecoration.getY());
				friendlyByteBufx.writeByte(mapDecoration.getRot() & 15);
				if (mapDecoration.getName() != null) {
					friendlyByteBufx.writeBoolean(true);
					friendlyByteBufx.writeComponent(mapDecoration.getName());
				} else {
					friendlyByteBufx.writeBoolean(false);
				}
			});
		} else {
			friendlyByteBuf.writeBoolean(false);
		}

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

	@Environment(EnvType.CLIENT)
	public int getMapId() {
		return this.mapId;
	}

	@Environment(EnvType.CLIENT)
	public void applyToMap(MapItemSavedData mapItemSavedData) {
		if (this.decorations != null) {
			mapItemSavedData.addClientSideDecorations(this.decorations);
		}

		if (this.colorPatch != null) {
			this.colorPatch.applyToMap(mapItemSavedData);
		}
	}

	@Environment(EnvType.CLIENT)
	public byte getScale() {
		return this.scale;
	}

	@Environment(EnvType.CLIENT)
	public boolean isLocked() {
		return this.locked;
	}
}
