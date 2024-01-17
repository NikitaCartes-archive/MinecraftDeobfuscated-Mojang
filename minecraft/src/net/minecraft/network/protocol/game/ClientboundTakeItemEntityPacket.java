package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundTakeItemEntityPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundTakeItemEntityPacket> STREAM_CODEC = Packet.codec(
		ClientboundTakeItemEntityPacket::write, ClientboundTakeItemEntityPacket::new
	);
	private final int itemId;
	private final int playerId;
	private final int amount;

	public ClientboundTakeItemEntityPacket(int i, int j, int k) {
		this.itemId = i;
		this.playerId = j;
		this.amount = k;
	}

	private ClientboundTakeItemEntityPacket(FriendlyByteBuf friendlyByteBuf) {
		this.itemId = friendlyByteBuf.readVarInt();
		this.playerId = friendlyByteBuf.readVarInt();
		this.amount = friendlyByteBuf.readVarInt();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.itemId);
		friendlyByteBuf.writeVarInt(this.playerId);
		friendlyByteBuf.writeVarInt(this.amount);
	}

	@Override
	public PacketType<ClientboundTakeItemEntityPacket> type() {
		return GamePacketTypes.CLIENTBOUND_TAKE_ITEM_ENTITY;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleTakeItemEntity(this);
	}

	public int getItemId() {
		return this.itemId;
	}

	public int getPlayerId() {
		return this.playerId;
	}

	public int getAmount() {
		return this.amount;
	}
}
