package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.InteractionHand;

public class ClientboundOpenBookPacket implements Packet<ClientGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ClientboundOpenBookPacket> STREAM_CODEC = Packet.codec(
		ClientboundOpenBookPacket::write, ClientboundOpenBookPacket::new
	);
	private final InteractionHand hand;

	public ClientboundOpenBookPacket(InteractionHand interactionHand) {
		this.hand = interactionHand;
	}

	private ClientboundOpenBookPacket(FriendlyByteBuf friendlyByteBuf) {
		this.hand = friendlyByteBuf.readEnum(InteractionHand.class);
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeEnum(this.hand);
	}

	@Override
	public PacketType<ClientboundOpenBookPacket> type() {
		return GamePacketTypes.CLIENTBOUND_OPEN_BOOK;
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleOpenBook(this);
	}

	public InteractionHand getHand() {
		return this.hand;
	}
}
