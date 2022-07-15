package net.minecraft.network.chat;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.network.FilteredText;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;

public record PlayerChatMessage(
	SignedMessageHeader signedHeader, MessageSignature headerSignature, SignedMessageBody signedBody, Optional<Component> unsignedContent
) {
	public static final Duration MESSAGE_EXPIRES_AFTER_SERVER = Duration.ofMinutes(5L);
	public static final Duration MESSAGE_EXPIRES_AFTER_CLIENT = MESSAGE_EXPIRES_AFTER_SERVER.plus(Duration.ofMinutes(2L));

	public PlayerChatMessage(FriendlyByteBuf friendlyByteBuf) {
		this(
			new SignedMessageHeader(friendlyByteBuf),
			new MessageSignature(friendlyByteBuf),
			new SignedMessageBody(friendlyByteBuf),
			friendlyByteBuf.readOptional(FriendlyByteBuf::readComponent)
		);
	}

	public static PlayerChatMessage unsigned(MessageSigner messageSigner, Component component) {
		SignedMessageBody signedMessageBody = new SignedMessageBody(
			new ChatMessageContent(component), messageSigner.timeStamp(), messageSigner.salt(), LastSeenMessages.EMPTY
		);
		SignedMessageHeader signedMessageHeader = new SignedMessageHeader(null, messageSigner.profileId());
		return new PlayerChatMessage(signedMessageHeader, MessageSignature.EMPTY, signedMessageBody, Optional.empty());
	}

	public void write(FriendlyByteBuf friendlyByteBuf) {
		this.signedHeader.write(friendlyByteBuf);
		this.headerSignature.write(friendlyByteBuf);
		this.signedBody.write(friendlyByteBuf);
		friendlyByteBuf.writeOptional(this.unsignedContent, FriendlyByteBuf::writeComponent);
	}

	public FilteredText<PlayerChatMessage> withFilteredText(@Nullable Component component) {
		if (component == null) {
			return FilteredText.fullyFiltered(this);
		} else {
			return this.signedContent().decorated().equals(component) ? FilteredText.passThrough(this) : new FilteredText<>(this, unsigned(this.signer(), component));
		}
	}

	public PlayerChatMessage withUnsignedContent(Component component) {
		Optional<Component> optional = !this.signedContent().decorated().equals(component) ? Optional.of(component) : Optional.empty();
		return new PlayerChatMessage(this.signedHeader, this.headerSignature, this.signedBody, optional);
	}

	public PlayerChatMessage removeUnsignedContent() {
		return this.unsignedContent.isPresent() ? new PlayerChatMessage(this.signedHeader, this.headerSignature, this.signedBody, Optional.empty()) : this;
	}

	public boolean verify(SignatureValidator signatureValidator) {
		return this.headerSignature.verify(signatureValidator, this.signedHeader, this.signedBody);
	}

	public boolean verify(ProfilePublicKey profilePublicKey) {
		SignatureValidator signatureValidator = profilePublicKey.createSignatureValidator();
		return this.verify(signatureValidator);
	}

	public boolean verify(ChatSender chatSender) {
		ProfilePublicKey profilePublicKey = chatSender.profilePublicKey();
		return profilePublicKey != null && this.verify(profilePublicKey);
	}

	public ChatMessageContent signedContent() {
		return this.signedBody.content();
	}

	public Component serverContent() {
		return (Component)this.unsignedContent().orElse(this.signedContent().decorated());
	}

	public Instant timeStamp() {
		return this.signedBody.timeStamp();
	}

	public long salt() {
		return this.signedBody.salt();
	}

	public boolean hasExpiredServer(Instant instant) {
		return instant.isAfter(this.timeStamp().plus(MESSAGE_EXPIRES_AFTER_SERVER));
	}

	public boolean hasExpiredClient(Instant instant) {
		return instant.isAfter(this.timeStamp().plus(MESSAGE_EXPIRES_AFTER_CLIENT));
	}

	public MessageSigner signer() {
		return new MessageSigner(this.signedHeader.sender(), this.timeStamp(), this.salt());
	}

	public LastSeenMessages.Entry toLastSeenEntry() {
		return new LastSeenMessages.Entry(this.signer().profileId(), this.headerSignature);
	}
}
