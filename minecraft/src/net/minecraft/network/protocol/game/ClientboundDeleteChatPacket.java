package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.protocol.Packet;

public record ClientboundDeleteChatPacket(MessageSignature.Packed messageSignature) implements Packet<ClientGamePacketListener> {
	public ClientboundDeleteChatPacket(FriendlyByteBuf friendlyByteBuf) {
		this(MessageSignature.Packed.read(friendlyByteBuf));
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		MessageSignature.Packed.write(friendlyByteBuf, this.messageSignature);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handleDeleteChat(this);
	}
}
