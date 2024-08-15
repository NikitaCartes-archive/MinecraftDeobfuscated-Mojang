package net.minecraft.network.protocol.game;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.item.ItemStack;

public record ClientboundSetCursorItemPacket(ItemStack contents) implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSetCursorItemPacket> STREAM_CODEC = StreamCodec.composite(
		ItemStack.OPTIONAL_STREAM_CODEC, ClientboundSetCursorItemPacket::contents, ClientboundSetCursorItemPacket::new
	);

	@Override
	public PacketType<ClientboundSetCursorItemPacket> type() {
		return GamePacketTypes.CLIENTBOUND_SET_CURSOR_ITEM;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetCursorItem(this);
	}
}
