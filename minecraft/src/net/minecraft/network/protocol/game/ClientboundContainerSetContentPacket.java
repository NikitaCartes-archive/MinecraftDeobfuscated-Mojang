package net.minecraft.network.protocol.game;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.ItemStack;

public class ClientboundContainerSetContentPacket implements Packet<ClientGamePacketListener> {
	private final int containerId;
	private final int stateId;
	private final List<ItemStack> items;
	private final ItemStack carriedItem;

	public ClientboundContainerSetContentPacket(int i, int j, NonNullList<ItemStack> nonNullList, ItemStack itemStack) {
		this.containerId = i;
		this.stateId = j;
		this.items = NonNullList.<ItemStack>withSize(nonNullList.size(), ItemStack.EMPTY);

		for (int k = 0; k < nonNullList.size(); k++) {
			this.items.set(k, nonNullList.get(k).copy());
		}

		this.carriedItem = itemStack.copy();
	}

	public ClientboundContainerSetContentPacket(FriendlyByteBuf friendlyByteBuf) {
		this.containerId = friendlyByteBuf.readUnsignedByte();
		this.stateId = friendlyByteBuf.readVarInt();
		this.items = friendlyByteBuf.readCollection(NonNullList::createWithCapacity, FriendlyByteBuf::readItem);
		this.carriedItem = friendlyByteBuf.readItem();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeByte(this.containerId);
		friendlyByteBuf.writeVarInt(this.stateId);
		friendlyByteBuf.writeCollection(this.items, FriendlyByteBuf::writeItem);
		friendlyByteBuf.writeItem(this.carriedItem);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleContainerContent(this);
	}

	public int getContainerId() {
		return this.containerId;
	}

	public List<ItemStack> getItems() {
		return this.items;
	}

	public ItemStack getCarriedItem() {
		return this.carriedItem;
	}

	public int getStateId() {
		return this.stateId;
	}
}
