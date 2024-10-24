package net.minecraft.network.protocol.game;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.item.ItemStack;

public record ServerboundSetCreativeModeSlotPacket(short slotNum, ItemStack itemStack) implements Packet<ServerGamePacketListener> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSetCreativeModeSlotPacket> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.SHORT,
		ServerboundSetCreativeModeSlotPacket::slotNum,
		ItemStack.validatedStreamCodec(ItemStack.OPTIONAL_STREAM_CODEC),
		ServerboundSetCreativeModeSlotPacket::itemStack,
		ServerboundSetCreativeModeSlotPacket::new
	);

	public ServerboundSetCreativeModeSlotPacket(int i, ItemStack itemStack) {
		this((short)i, itemStack);
	}

	@Override
	public PacketType<ServerboundSetCreativeModeSlotPacket> type() {
		return GamePacketTypes.SERVERBOUND_SET_CREATIVE_MODE_SLOT;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleSetCreativeModeSlot(this);
	}
}
