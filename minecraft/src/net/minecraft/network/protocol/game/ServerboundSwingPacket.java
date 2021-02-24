package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;

public class ServerboundSwingPacket implements Packet<ServerGamePacketListener> {
	private final InteractionHand hand;

	public ServerboundSwingPacket(InteractionHand interactionHand) {
		this.hand = interactionHand;
	}

	public ServerboundSwingPacket(FriendlyByteBuf friendlyByteBuf) {
		this.hand = friendlyByteBuf.readEnum(InteractionHand.class);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeEnum(this.hand);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleAnimate(this);
	}

	public InteractionHand getHand() {
		return this.hand;
	}
}
