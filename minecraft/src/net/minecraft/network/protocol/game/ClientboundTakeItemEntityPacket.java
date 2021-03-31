package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundTakeItemEntityPacket implements Packet<ClientGamePacketListener> {
	private final int itemId;
	private final int playerId;
	private final int amount;

	public ClientboundTakeItemEntityPacket(int i, int j, int k) {
		this.itemId = i;
		this.playerId = j;
		this.amount = k;
	}

	public ClientboundTakeItemEntityPacket(FriendlyByteBuf friendlyByteBuf) {
		this.itemId = friendlyByteBuf.readVarInt();
		this.playerId = friendlyByteBuf.readVarInt();
		this.amount = friendlyByteBuf.readVarInt();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.itemId);
		friendlyByteBuf.writeVarInt(this.playerId);
		friendlyByteBuf.writeVarInt(this.amount);
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
