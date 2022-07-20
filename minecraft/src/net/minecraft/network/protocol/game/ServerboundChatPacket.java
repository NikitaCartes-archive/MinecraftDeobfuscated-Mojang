package net.minecraft.network.protocol.game;

import java.time.Instant;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSigner;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;

public record ServerboundChatPacket(
	String message, Instant timeStamp, long salt, MessageSignature signature, boolean signedPreview, LastSeenMessages.Update lastSeenMessages
) implements Packet<ServerGamePacketListener> {
	public ServerboundChatPacket(FriendlyByteBuf friendlyByteBuf) {
		this(
			friendlyByteBuf.readUtf(256),
			friendlyByteBuf.readInstant(),
			friendlyByteBuf.readLong(),
			new MessageSignature(friendlyByteBuf),
			friendlyByteBuf.readBoolean(),
			new LastSeenMessages.Update(friendlyByteBuf)
		);
	}

	@Override
	public void write(FriendlyByteBuf friendlyByteBuf) {
		friendlyByteBuf.writeUtf(this.message, 256);
		friendlyByteBuf.writeInstant(this.timeStamp);
		friendlyByteBuf.writeLong(this.salt);
		this.signature.write(friendlyByteBuf);
		friendlyByteBuf.writeBoolean(this.signedPreview);
		this.lastSeenMessages.write(friendlyByteBuf);
	}

	public void handle(ServerGamePacketListener serverGamePacketListener) {
		serverGamePacketListener.handleChat(this);
	}

	public MessageSigner getSigner(ServerPlayer serverPlayer) {
		return new MessageSigner(serverPlayer.getUUID(), this.timeStamp, this.salt);
	}
}
