package net.minecraft.network.protocol.game;

import java.io.IOException;
import java.util.Collection;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

public class ClientboundMapItemDataPacket implements Packet<ClientGamePacketListener> {
	private int mapId;
	private byte scale;
	private boolean trackingPosition;
	private boolean locked;
	private MapDecoration[] decorations;
	private int startX;
	private int startY;
	private int width;
	private int height;
	private byte[] mapColors;

	public ClientboundMapItemDataPacket() {
	}

	public ClientboundMapItemDataPacket(int i, byte b, boolean bl, boolean bl2, Collection<MapDecoration> collection, byte[] bs, int j, int k, int l, int m) {
		this.mapId = i;
		this.scale = b;
		this.trackingPosition = bl;
		this.locked = bl2;
		this.decorations = (MapDecoration[])collection.toArray(new MapDecoration[collection.size()]);
		this.startX = j;
		this.startY = k;
		this.width = l;
		this.height = m;
		this.mapColors = new byte[l * m];

		for (int n = 0; n < l; n++) {
			for (int o = 0; o < m; o++) {
				this.mapColors[n + o * l] = bs[j + n + (k + o) * 128];
			}
		}
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.mapId = friendlyByteBuf.readVarInt();
		this.scale = friendlyByteBuf.readByte();
		this.trackingPosition = friendlyByteBuf.readBoolean();
		this.locked = friendlyByteBuf.readBoolean();
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

		this.width = friendlyByteBuf.readUnsignedByte();
		if (this.width > 0) {
			this.height = friendlyByteBuf.readUnsignedByte();
			this.startX = friendlyByteBuf.readUnsignedByte();
			this.startY = friendlyByteBuf.readUnsignedByte();
			this.mapColors = friendlyByteBuf.readByteArray();
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeVarInt(this.mapId);
		friendlyByteBuf.writeByte(this.scale);
		friendlyByteBuf.writeBoolean(this.trackingPosition);
		friendlyByteBuf.writeBoolean(this.locked);
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

		friendlyByteBuf.writeByte(this.width);
		if (this.width > 0) {
			friendlyByteBuf.writeByte(this.height);
			friendlyByteBuf.writeByte(this.startX);
			friendlyByteBuf.writeByte(this.startY);
			friendlyByteBuf.writeByteArray(this.mapColors);
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
		mapItemSavedData.scale = this.scale;
		mapItemSavedData.trackingPosition = this.trackingPosition;
		mapItemSavedData.locked = this.locked;
		mapItemSavedData.decorations.clear();

		for (int i = 0; i < this.decorations.length; i++) {
			MapDecoration mapDecoration = this.decorations[i];
			mapItemSavedData.decorations.put("icon-" + i, mapDecoration);
		}

		for (int i = 0; i < this.width; i++) {
			for (int j = 0; j < this.height; j++) {
				mapItemSavedData.colors[this.startX + i + (this.startY + j) * 128] = this.mapColors[i + j * this.width];
			}
		}
	}
}
