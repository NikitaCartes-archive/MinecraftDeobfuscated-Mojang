package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundRemoveEntitiesPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundRemoveEntitiesPacket> STREAM_CODEC = Packet.codec(
		ClientboundRemoveEntitiesPacket::write, ClientboundRemoveEntitiesPacket::new
	);
	private final IntList entityIds;

	public ClientboundRemoveEntitiesPacket(IntList intList) {
		this.entityIds = new IntArrayList(intList);
	}

	public ClientboundRemoveEntitiesPacket(int... is) {
		this.entityIds = new IntArrayList(is);
	}

	private ClientboundRemoveEntitiesPacket(FriendlyByteBuf friendlyByteBuf) {
		this.entityIds = friendlyByteBuf.readIntIdList();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeIntIdList(this.entityIds);
	}

	@Override
	public PacketType<ClientboundRemoveEntitiesPacket> type() {
		return GamePacketTypes.CLIENTBOUND_REMOVE_ENTITIES;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleRemoveEntities(this);
	}

	public IntList getEntityIds() {
		return this.entityIds;
	}
}
