package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionHand;

public class ClientboundOpenBookPacket implements Packet<ClientGamePacketListener> {
	private InteractionHand hand;

	public ClientboundOpenBookPacket() {
	}

	public ClientboundOpenBookPacket(InteractionHand interactionHand) {
		this.hand = interactionHand;
	}

	@Override
	public void read(FriendlyByteBuf friendlyByteBuf) throws IOException {
		this.hand = friendlyByteBuf.readEnum(InteractionHand.class);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) throws IOException {
		friendlyByteBuf.writeEnum(this.hand);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleOpenBook(this);
	}

	@Environment(EnvType.CLIENT)
	public InteractionHand getHand() {
		return this.hand;
	}
}
