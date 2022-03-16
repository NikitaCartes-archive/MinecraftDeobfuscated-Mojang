package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;

public class ServerboundUseItemPacket implements Packet<ServerGamePacketListener> {
	private final InteractionHand hand;
	private final int sequence;

	public ServerboundUseItemPacket(InteractionHand interactionHand, int i) {
		this.hand = interactionHand;
		this.sequence = i;
	}

	public ServerboundUseItemPacket(FriendlyByteBuf friendlyByteBuf) {
		this.hand = friendlyByteBuf.readEnum(InteractionHand.class);
		this.sequence = friendlyByteBuf.readVarInt();
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeEnum(this.hand);
		friendlyByteBuf.writeVarInt(this.sequence);
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
