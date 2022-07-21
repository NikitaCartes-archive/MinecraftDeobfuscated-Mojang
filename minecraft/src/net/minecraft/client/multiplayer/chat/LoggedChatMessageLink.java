package net.minecraft.client.multiplayer.chat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageHeader;

@Environment(EnvType.CLIENT)
public interface LoggedChatMessageLink extends LoggedChatEvent {
	static LoggedChatMessageLink.Header header(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs) {
		return new LoggedChatMessageLink.Header(signedMessageHeader, messageSignature, bs);
	}

	SignedMessageHeader header();

	MessageSignature headerSignature();

	byte[] bodyDigest();

	@Environment(EnvType.CLIENT)
	public static record Header(SignedMessageHeader header, MessageSignature headerSignature, byte[] bodyDigest) implements LoggedChatMessageLink {
	}
}
