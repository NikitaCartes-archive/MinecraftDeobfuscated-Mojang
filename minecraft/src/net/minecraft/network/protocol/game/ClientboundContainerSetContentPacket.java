package net.minecraft.network.protocol.game;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.ItemStack;

public class ClientboundContainerSetContentPacket implements Packet<ClientGamePacketListener> {
	private final int containerId;
	private final List<ItemStack> items;

	public ClientboundContainerSetContentPacket(int i, NonNullList<ItemStack> nonNullList) {
		this.containerId = i;
		this.items = NonNullList.<ItemStack>withSize(nonNullList.size(), ItemStack.EMPTY);

		for (int j = 0; j < this.items.size(); j++) {
			this.items.set(j, nonNullList.get(j).copy());
		}
	}

	public ClientboundContainerSetContentPacket(FriendlyByteBuf friendlyByteBuf) {
		this.containerId = friendlyByteBuf.readUnsignedByte();
		int i = friendlyByteBuf.readShort();
		this.items = NonNullList.<ItemStack>withSize(i, ItemStack.EMPTY);

		for (int j = 0; j < i; j++) {
			this.items.set(j, friendlyByteBuf.readItem());
		}
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeByte(this.containerId);
		friendlyByteBuf.writeShort(this.items.size());

		for (ItemStack itemStack : this.items) {
			friendlyByteBuf.writeItem(itemStack);
		}
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
}
