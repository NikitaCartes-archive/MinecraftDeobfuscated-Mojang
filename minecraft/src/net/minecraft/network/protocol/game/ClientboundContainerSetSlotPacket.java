package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.ItemStack;

public class ClientboundContainerSetSlotPacket implements Packet<ClientGamePacketListener> {
	private int containerId;
	private int slot;
	private ItemStack itemStack = ItemStack.EMPTY;

	public ClientboundContainerSetSlotPacket() {
	}

	public ClientboundContainerSetSlotPacket(int i, int j, ItemStack itemStack) {
		this.containerId = i;
		this.slot = j;
		this.itemStack = itemStack.copy();
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleContainerSetSlot(this);
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.containerId = friendlyByteBuf.readByte();
		this.slot = friendlyByteBuf.readShort();
		this.itemStack = friendlyByteBuf.readItem();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeByte(this.containerId);
		friendlyByteBuf.writeShort(this.slot);
		friendlyByteBuf.writeItem(this.itemStack);
	}

	@Environment(EnvType.CLIENT)
	public int getContainerId() {
		return this.containerId;
	}

	@Environment(EnvType.CLIENT)
	public int getSlot() {
		return this.slot;
	}

	@Environment(EnvType.CLIENT)
	public ItemStack getItem() {
		return this.itemStack;
	}
}
