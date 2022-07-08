package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageHeader;
import net.minecraft.network.protocol.Packet;

public record ClientboundPlayerChatHeaderPacket(SignedMessageHeader header, MessageSignature headerSignature, byte[] bodyDigest)
	implements Packet<ClientGamePacketListener> {
	public ClientboundPlayerChatHeaderPacket(FriendlyByteBuf friendlyByteBuf) {
		this(new SignedMessageHeader(friendlyByteBuf), new MessageSignature(friendlyByteBuf), friendlyByteBuf.readByteArray());
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		this.header.write(friendlyByteBuf);
		this.headerSignature.write(friendlyByteBuf);
		friendlyByteBuf.writeByteArray(this.bodyDigest);
	}

	public void handle(ClientGamePacketListener clientGamePacketListener) {
		clientGamePacketListener.handlePlayerChatHeader(this);
	}
}
