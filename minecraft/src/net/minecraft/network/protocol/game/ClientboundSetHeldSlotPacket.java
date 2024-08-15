package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;

public class ClientboundSetHeldSlotPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundSetHeldSlotPacket> STREAM_CODEC = Packet.codec(
		ClientboundSetHeldSlotPacket::write, ClientboundSetHeldSlotPacket::new
	);
	private final int slot;

	public ClientboundSetHeldSlotPacket(int i) {
		this.slot = i;
	}

	private ClientboundSetHeldSlotPacket(FriendlyByteBuf friendlyByteBuf) {
		this.slot = friendlyByteBuf.readByte();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeByte(this.slot);
	}

	@Override
	public PacketType<ClientboundSetHeldSlotPacket> type() {
		return GamePacketTypes.CLIENTBOUND_SET_HELD_SLOT;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleSetHeldSlot(this);
	}

	public int getSlot() {
		return this.slot;
	}
}
