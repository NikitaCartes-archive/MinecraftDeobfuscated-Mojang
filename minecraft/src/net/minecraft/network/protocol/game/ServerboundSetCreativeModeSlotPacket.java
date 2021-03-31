package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.ItemStack;

public class ServerboundSetCreativeModeSlotPacket implements Packet<ServerGamePacketListener> {
	private final int slotNum;
	private final ItemStack itemStack;

	public ServerboundSetCreativeModeSlotPacket(int i, ItemStack itemStack) {
		this.slotNum = i;
		this.itemStack = itemStack.copy();
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleSetCreativeModeSlot(this);
	}

	public ServerboundSetCreativeModeSlotPacket(FriendlyByteBuf friendlyByteBuf) {
		this.slotNum = friendlyByteBuf.readShort();
		this.itemStack = friendlyByteBuf.readItem();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeShort(this.slotNum);
		friendlyByteBuf.writeItem(this.itemStack);
	}

	public int getSlotNum() {
		return this.slotNum;
	}

	public ItemStack getItem() {
		return this.itemStack;
	}
}
