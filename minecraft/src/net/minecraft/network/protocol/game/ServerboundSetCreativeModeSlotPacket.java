package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.item.ItemStack;

public class ServerboundSetCreativeModeSlotPacket implements Packet<ServerGamePacketListener> {
	private int slotNum;
	private ItemStack itemStack = ItemStack.EMPTY;

	public ServerboundSetCreativeModeSlotPacket() {
	}

	@Environment(EnvType.CLIENT)
	public ServerboundSetCreativeModeSlotPacket(int i, ItemStack itemStack) {
		this.slotNum = i;
		this.itemStack = itemStack.copy();
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleSetCreativeModeSlot(this);
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.slotNum = friendlyByteBuf.readShort();
		this.itemStack = friendlyByteBuf.readItem();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
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
