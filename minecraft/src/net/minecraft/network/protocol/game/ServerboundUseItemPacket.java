package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.world.InteractionHand;

public class ServerboundUseItemPacket implements Packet<ServerGamePacketListener> {
	public static final StreamCodec<FriendlyByteBuf, ServerboundUseItemPacket> STREAM_CODEC = Packet.codec(
		ServerboundUseItemPacket::write, ServerboundUseItemPacket::new
	);
	private final InteractionHand hand;
	private final int sequence;

	public ServerboundUseItemPacket(InteractionHand interactionHand, int i) {
		this.hand = interactionHand;
		this.sequence = i;
	}

	private ServerboundUseItemPacket(FriendlyByteBuf friendlyByteBuf) {
		this.hand = friendlyByteBuf.readEnum(InteractionHand.class);
		this.sequence = friendlyByteBuf.readVarInt();
	}

	private void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeEnum(this.hand);
		friendlyByteBuf.writeVarInt(this.sequence);
	}

	@Override
	public PacketType<ServerboundUseItemPacket> type() {
		return GamePacketTypes.SERVERBOUND_USE_ITEM;
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleUseItem(this);
	}

	public InteractionHand getHand() {
		return this.hand;
	}

	public int getSequence() {
		return this.sequence;
	}
}
