package net.minecraft.network.chat;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;

public record PlayerChatMessage(
	SignedMessageHeader signedHeader, MessageSignature headerSignature, SignedMessageBody signedBody, Optional<Component> unsignedContent, FilterMask filterMask
) {
	public static final Duration MESSAGE_EXPIRES_AFTER_SERVER = Duration.ofMinutes(5L);
	public static final Duration MESSAGE_EXPIRES_AFTER_CLIENT = MESSAGE_EXPIRES_AFTER_SERVER.plus(Duration.ofMinutes(2L));

	public PlayerChatMessage(FriendlyByteBuf friendlyByteBuf) {
		this(
			new SignedMessageHeader(friendlyByteBuf),
			new MessageSignature(friendlyByteBuf),
			new SignedMessageBody(friendlyByteBuf),
			friendlyByteBuf.readOptional(FriendlyByteBuf::readComponent),
			FilterMask.read(friendlyByteBuf)
		);
	}

	public static PlayerChatMessage system(ChatMessageContent chatMessageContent) {
		return unsigned(MessageSigner.system(), chatMessageContent);
	}

	public static PlayerChatMessage unsigned(MessageSigner messageSigner, ChatMessageContent chatMessageContent) {
		SignedMessageBody signedMessageBody = new SignedMessageBody(chatMessageContent, messageSigner.timeStamp(), messageSigner.salt(), LastSeenMessages.EMPTY);
		SignedMessageHeader signedMessageHeader = new SignedMessageHeader(null, messageSigner.profileId());
		return new PlayerChatMessage(signedMessageHeader, MessageSignature.EMPTY, signedMessageBody, Optional.empty(), FilterMask.PASS_THROUGH);
	}

	public void write(FriendlyByteBuf friendlyByteBuf) {
		this.signedHeader.write(friendlyByteBuf);
		this.headerSignature.write(friendlyByteBuf);
		this.signedBody.write(friendlyByteBuf);
		friendlyByteBuf.writeOptional(this.unsignedContent, FriendlyByteBuf::writeComponent);
		FilterMask.write(friendlyByteBuf, this.filterMask);
	}

	public PlayerChatMessage withUnsignedContent(Component component) {
		Optional<Component> optional = !this.signedContent().decorated().equals(component) ? Optional.of(component) : Optional.empty();
		return new PlayerChatMessage(this.signedHeader, this.headerSignature, this.signedBody, optional, this.filterMask);
	}

	public PlayerChatMessage removeUnsignedContent() {
		return this.unsignedContent.isPresent()
			? new PlayerChatMessage(this.signedHeader, this.headerSignature, this.signedBody, Optional.empty(), this.filterMask)
			: this;
	}

	public PlayerChatMessage filter(FilterMask filterMask) {
		return this.filterMask.equals(filterMask)
			? this
			: new PlayerChatMessage(this.signedHeader, this.headerSignature, this.signedBody, this.unsignedContent, filterMask);
	}

	public PlayerChatMessage filter(boolean bl) {
		return this.filter(bl ? this.filterMask : FilterMask.PASS_THROUGH);
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

	@Nullable
	public LastSeenMessages.Entry toLastSeenEntry() {
		MessageSigner messageSigner = this.signer();
		return !this.headerSignature.isEmpty() && !messageSigner.isSystem() ? new LastSeenMessages.Entry(messageSigner.profileId(), this.headerSignature) : null;
	}

	public boolean hasSignatureFrom(UUID uUID) {
		return !this.headerSignature.isEmpty() && this.signedHeader.sender().equals(uUID);
	}

	public boolean isFullyFiltered() {
		return this.filterMask.isFullyFiltered();
	}
}
