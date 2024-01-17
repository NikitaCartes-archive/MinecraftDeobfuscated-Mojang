package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundSetCarriedItemPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundSetCarriedItemPacket> STREAM_CODEC = Packet.codec(
		ClientboundSetCarriedItemPacket::write, ClientboundSetCarriedItemPacket::new
	);
	private final int slot;

	public ClientboundSetCarriedItemPacket(int i) {
		this.slot = i;
	}

	private ClientboundSetCarriedItemPacket(FriendlyByteBuf friendlyByteBuf) {
		this.slot = friendlyByteBuf.readByte();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeByte(this.slot);
	}

	@Override
	public PacketType<ClientboundSetCarriedItemPacket> type() {
		return GamePacketTypes.CLIENTBOUND_SET_CARRIED_ITEM;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetCarriedItem(this);
	}

	public int getSlot() {
		return this.slot;
	}
}
