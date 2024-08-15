package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public record ServerboundSelectBundleItemPacket(int slotId, int selectedItemIndex) implements Packet<ServerGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ServerboundSelectBundleItemPacket> STREAM_CODEC = Packet.codec(
		ServerboundSelectBundleItemPacket::write, ServerboundSelectBundleItemPacket::new
	);

	private ServerboundSelectBundleItemPacket(FriendlyByteBuf friendlyByteBuf) {
		this(friendlyByteBuf.readVarInt(), friendlyByteBuf.readVarInt());
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeVarInt(this.slotId);
		friendlyByteBuf.writeVarInt(this.selectedItemIndex);
	}

	@Override
	public PacketType<ServerboundSelectBundleItemPacket> type() {
		return GamePacketTypes.SERVERBOUND_BUNDLE_ITEM_SELECTED;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleBundleItemSelectedPacket(this);
	}
}
