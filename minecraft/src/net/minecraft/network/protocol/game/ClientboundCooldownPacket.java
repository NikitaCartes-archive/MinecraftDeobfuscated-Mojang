package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.Item;

public class ClientboundCooldownPacket implements Packet<ClientGamePacketListener> {
	private final Item item;
	private final int duration;

	public ClientboundCooldownPacket(Item item, int i) {
		this.item = item;
		this.duration = i;
	}

	public ClientboundCooldownPacket(FriendlyByteBuf friendlyByteBuf) {
		this.item = Item.byId(friendlyByteBuf.readVarInt());
		this.duration = friendlyByteBuf.readVarInt();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(Item.getId(this.item));
		friendlyByteBuf.writeVarInt(this.duration);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleItemCooldown(this);
	}

	public Item getItem() {
		return this.item;
	}

	public int getDuration() {
		return this.duration;
	}
}
