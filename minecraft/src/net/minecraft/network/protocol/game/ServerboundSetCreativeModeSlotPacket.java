package net.minecraft.network.protocol.game;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.item.ItemStack;

public class ServerboundSetCreativeModeSlotPacket implements Packet<ServerGamePacketListener> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSetCreativeModeSlotPacket> STREAM_CODEC = Packet.codec(
		ServerboundSetCreativeModeSlotPacket::write, ServerboundSetCreativeModeSlotPacket::new
	);
	private final int slotNum;
	private final ItemStack itemStack;

	public ServerboundSetCreativeModeSlotPacket(int i, ItemStack itemStack) {
		this.slotNum = i;
		this.itemStack = itemStack.copy();
	}

	private ServerboundSetCreativeModeSlotPacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		this.slotNum = registryFriendlyByteBuf.readShort();
		this.itemStack = ItemStack.OPTIONAL_STREAM_CODEC.decode(registryFriendlyByteBuf);
	}

	private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
		registryFriendlyByteBuf.writeShort(this.slotNum);
		ItemStack.OPTIONAL_STREAM_CODEC.encode(registryFriendlyByteBuf, this.itemStack);
	}

	@Override
	public PacketType<ServerboundSetCreativeModeSlotPacket> type() {
		return GamePacketTypes.SERVERBOUND_SET_CREATIVE_MODE_SLOT;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleSetCreativeModeSlot(this);
	}

	public int getSlotNum() {
		return this.slotNum;
	}

	public ItemStack getItem() {
		return this.itemStack;
	}
}
