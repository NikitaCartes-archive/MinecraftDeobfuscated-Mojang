package net.minecraft.network.chat;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.server.network.FilteredText;
import net.minecraft.util.Signer;

public class SignedMessageChain {
	@Nullable
	private MessageSignature previousSignature;

	private SignedMessageChain.Link pack(Signer signer, MessageSigner messageSigner, Component component) {
		MessageSignature messageSignature = pack(signer, messageSigner, this.previousSignature, component);
		this.previousSignature = messageSignature;
		return new SignedMessageChain.Link(messageSignature);
	}

	private static MessageSignature pack(Signer signer, MessageSigner messageSigner, @Nullable MessageSignature messageSignature, Component component) {
		SignedMessageHeader signedMessageHeader = new SignedMessageHeader(messageSignature, messageSigner.profileId());
		SignedMessageBody signedMessageBody = new SignedMessageBody(component, messageSigner.timeStamp(), messageSigner.salt(), List.of());
		byte[] bs = signedMessageBody.hash().asBytes();
		return new MessageSignature(signer.sign(output -> signedMessageHeader.updateSignature(output, bs)));
	}

	private PlayerChatMessage unpack(SignedMessageChain.Link link, MessageSigner messageSigner, Component component) {
		PlayerChatMessage playerChatMessage = unpack(link, this.previousSignature, messageSigner, component);
		this.previousSignature = link.signature;
		return playerChatMessage;
	}

	private static PlayerChatMessage unpack(
		SignedMessageChain.Link link, @Nullable MessageSignature messageSignature, MessageSigner messageSigner, Component component
	) {
		SignedMessageHeader signedMessageHeader = new SignedMessageHeader(messageSignature, messageSigner.profileId());
		SignedMessageBody signedMessageBody = new SignedMessageBody(component, messageSigner.timeStamp(), messageSigner.salt(), List.of());
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
		SignedMessageChain.Decoder UNSIGNED = (link, messageSigner, component) -> PlayerChatMessage.unsigned(messageSigner, component);

		PlayerChatMessage unpack(SignedMessageChain.Link link, MessageSigner messageSigner, Component component);

		default FilteredText<PlayerChatMessage> unpack(SignedMessageChain.Link link, MessageSigner messageSigner, FilteredText<Component> filteredText) {
			return this.unpack(link, messageSigner, filteredText.raw()).withFilteredText(filteredText.filtered());
		}
	}

	@FunctionalInterface
	public interface Encoder {
		SignedMessageChain.Link pack(Signer signer, MessageSigner messageSigner, Component component);
	}

	public static record Link(MessageSignature signature) {
	}
}
