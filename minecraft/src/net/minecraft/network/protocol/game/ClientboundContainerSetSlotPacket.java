package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.ItemStack;

public class ClientboundContainerSetSlotPacket implements Packet<ClientGamePacketListener> {
	public static final int CARRIED_ITEM = -1;
	public static final int PLAYER_INVENTORY = -2;
	private final int containerId;
	private final int stateId;
	private final int slot;
	private final ItemStack itemStack;

	public ClientboundContainerSetSlotPacket(int i, int j, int k, ItemStack itemStack) {
		this.containerId = i;
		this.stateId = j;
		this.slot = k;
		this.itemStack = itemStack.copy();
	}

	public ClientboundContainerSetSlotPacket(FriendlyByteBuf friendlyByteBuf) {
		this.containerId = friendlyByteBuf.readByte();
		this.stateId = friendlyByteBuf.readVarInt();
		this.slot = friendlyByteBuf.readShort();
		this.itemStack = friendlyByteBuf.readItem();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeByte(this.containerId);
		friendlyByteBuf.writeVarInt(this.stateId);
		friendlyByteBuf.writeShort(this.slot);
		friendlyByteBuf.writeItem(this.itemStack);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleContainerSetSlot(this);
	}

	public int getContainerId() {
		return this.containerId;
	}

	public int getSlot() {
		return this.slot;
	}

	public ItemStack getItem() {
		return this.itemStack;
	}

	public int getStateId() {
		return this.stateId;
	}
}
