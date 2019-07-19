package net.minecraft.network.protocol.game;

import java.io.IOException;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.SynchedEntityData;

public class ClientboundSetEntityDataPacket implements Packet<ClientGamePacketListener> {
	private int id;
	private List<SynchedEntityData.DataItem<?>> packedItems;

	public ClientboundSetEntityDataPacket() {
	}

	public ClientboundSetEntityDataPacket(int i, SynchedEntityData synchedEntityData, boolean bl) {
		this.id = i;
		if (bl) {
			this.packedItems = synchedEntityData.getAll();
			synchedEntityData.clearDirty();
		} else {
			this.packedItems = synchedEntityData.packDirty();
		}
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.id = friendlyByteBuf.readVarInt();
		this.packedItems = SynchedEntityData.unpack(friendlyByteBuf);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeVarInt(this.id);
		SynchedEntityData.pack(this.packedItems, friendlyByteBuf);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetEntityData(this);
	}

	@Environment(EnvType.CLIENT)
	public List<SynchedEntityData.DataItem<?>> getUnpackedData() {
		return this.packedItems;
	}

	@Environment(EnvType.CLIENT)
	public int getId() {
		return this.id;
	}
}
