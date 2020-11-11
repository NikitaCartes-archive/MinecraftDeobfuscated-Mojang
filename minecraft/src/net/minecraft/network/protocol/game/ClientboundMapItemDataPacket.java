package net.minecraft.network.protocol.game;

import java.io.IOException;
import java.util.Collection;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class ClientboundMapItemDataPacket implements Packet<ClientGamePacketListener> {
	private int mapId;
	private byte scale;
	private boolean locked;
	@Nullable
	private MapDecoration[] decorations;
	@Nullable
	private MapItemSavedData.MapPatch colorPatch;

	public ClientboundMapItemDataPacket() {
	}

	public ClientboundMapItemDataPacket(int i, byte b, boolean bl, @Nullable Collection<MapDecoration> collection, @Nullable MapItemSavedData.MapPatch mapPatch) {
		this.mapId = i;
		this.scale = b;
		this.locked = bl;
		this.decorations = collection != null ? (MapDecoration[])collection.toArray(new MapDecoration[0]) : null;
		this.colorPatch = mapPatch;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.mapId = friendlyByteBuf.readVarInt();
		this.scale = friendlyByteBuf.readByte();
		this.locked = friendlyByteBuf.readBoolean();
		if (friendlyByteBuf.readBoolean()) {
			this.decorations = new MapDecoration[friendlyByteBuf.readVarInt()];

			for (int i = 0; i < this.decorations.length; i++) {
				MapDecoration.Type type = friendlyByteBuf.readEnum(MapDecoration.Type.class);
				this.decorations[i] = new MapDecoration(
					type,
					friendlyByteBuf.readByte(),
					friendlyByteBuf.readByte(),
					(byte)(friendlyByteBuf.readByte() & 15),
					friendlyByteBuf.readBoolean() ? friendlyByteBuf.readComponent() : null
				);
			}
		}

		int i = friendlyByteBuf.readUnsignedByte();
		if (i > 0) {
			int j = friendlyByteBuf.readUnsignedByte();
			int k = friendlyByteBuf.readUnsignedByte();
			int l = friendlyByteBuf.readUnsignedByte();
			byte[] bs = friendlyByteBuf.readByteArray();
			this.colorPatch = new MapItemSavedData.MapPatch(k, l, i, j, bs);
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeVarInt(this.mapId);
		friendlyByteBuf.writeByte(this.scale);
		friendlyByteBuf.writeBoolean(this.locked);
		if (this.decorations != null) {
			friendlyByteBuf.writeBoolean(true);
			friendlyByteBuf.writeVarInt(this.decorations.length);

			for (MapDecoration mapDecoration : this.decorations) {
				friendlyByteBuf.writeEnum(mapDecoration.getType());
				friendlyByteBuf.writeByte(mapDecoration.getX());
				friendlyByteBuf.writeByte(mapDecoration.getY());
				friendlyByteBuf.writeByte(mapDecoration.getRot() & 15);
				if (mapDecoration.getName() != null) {
					friendlyByteBuf.writeBoolean(true);
					friendlyByteBuf.writeComponent(mapDecoration.getName());
				} else {
					friendlyByteBuf.writeBoolean(false);
				}
			}
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
