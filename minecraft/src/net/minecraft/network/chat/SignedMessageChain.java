package net.minecraft.network.chat;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.util.Signer;

public class SignedMessageChain {
	@Nullable
	private MessageSignature previousSignature;

	private SignedMessageChain.Link pack(Signer signer, MessageSigner messageSigner, ChatMessageContent chatMessageContent, LastSeenMessages lastSeenMessages) {
		MessageSignature messageSignature = pack(signer, messageSigner, this.previousSignature, chatMessageContent, lastSeenMessages);
		this.previousSignature = messageSignature;
		return new SignedMessageChain.Link(messageSignature);
	}

	private static MessageSignature pack(
		Signer signer,
		MessageSigner messageSigner,
		@Nullable MessageSignature messageSignature,
		ChatMessageContent chatMessageContent,
		LastSeenMessages lastSeenMessages
	) {
		SignedMessageHeader signedMessageHeader = new SignedMessageHeader(messageSignature, messageSigner.profileId());
		SignedMessageBody signedMessageBody = new SignedMessageBody(chatMessageContent, messageSigner.timeStamp(), messageSigner.salt(), lastSeenMessages);
		byte[] bs = signedMessageBody.hash().asBytes();
		return new MessageSignature(signer.sign(output -> signedMessageHeader.updateSignature(output, bs)));
	}

	private PlayerChatMessage unpack(
		SignedMessageChain.Link link, MessageSigner messageSigner, ChatMessageContent chatMessageContent, LastSeenMessages lastSeenMessages
	) {
		PlayerChatMessage playerChatMessage = unpack(link, this.previousSignature, messageSigner, chatMessageContent, lastSeenMessages);
		this.previousSignature = link.signature;
		return playerChatMessage;
	}

	private static PlayerChatMessage unpack(
		SignedMessageChain.Link link,
		@Nullable MessageSignature messageSignature,
		MessageSigner messageSigner,
		ChatMessageContent chatMessageContent,
		LastSeenMessages lastSeenMessages
	) {
		SignedMessageHeader signedMessageHeader = new SignedMessageHeader(messageSignature, messageSigner.profileId());
		SignedMessageBody signedMessageBody = new SignedMessageBody(chatMessageContent, messageSigner.timeStamp(), messageSigner.salt(), lastSeenMessages);
		return new PlayerChatMessage(signedMessageHeader, link.signature, signedMessageBody, Optional.empty());
	}

	public SignedMessageChain.Decoder decoder() {
		return this::unpack;
	}

	public SignedMessageChain.Encoder encoder() {
		return this::pack;
	}

	@FunctionalInterface
	public interface Decoder {
		PlayerChatMessage unpack(SignedMessageChain.Link link, MessageSigner messageSigner, ChatMessageContent chatMessageContent, LastSeenMessages lastSeenMessages);
	}

	@FunctionalInterface
	public interface Encoder {
		SignedMessageChain.Link pack(Signer signer, MessageSigner messageSigner, ChatMessageContent chatMessageContent, LastSeenMessages lastSeenMessages);
	}

	public static record Link(MessageSignature signature) {
	}
}
