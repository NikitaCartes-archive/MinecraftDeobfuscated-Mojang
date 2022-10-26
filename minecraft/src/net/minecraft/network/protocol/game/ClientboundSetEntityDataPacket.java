package net.minecraft.network.protocol.game;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.SynchedEntityData;

public record ClientboundSetEntityDataPacket(int id, List<SynchedEntityData.DataValue<?>> packedItems) implements Packet<ClientGamePacketListener> {
	public static final int EOF_MARKER = 255;

	public ClientboundSetEntityDataPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readVarInt(), unpack(friendlyByteBuf));
	}

	private static void pack(List<SynchedEntityData.DataValue<?>> list, FriendlyByteBuf friendlyByteBuf) {
		for (SynchedEntityData.DataValue<?> dataValue : list) {
			dataValue.write(friendlyByteBuf);
		}

		friendlyByteBuf.writeByte(255);
	}

	private static List<SynchedEntityData.DataValue<?>> unpack(FriendlyByteBuf friendlyByteBuf) {
		List<SynchedEntityData.DataValue<?>> list = new ArrayList();

		int i;
		while ((i = friendlyByteBuf.readUnsignedByte()) != 255) {
			list.add(SynchedEntityData.DataValue.read(friendlyByteBuf, i));
		}

		return list;
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.id);
		pack(this.packedItems, friendlyByteBuf);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetEntityData(this);
	}
}
