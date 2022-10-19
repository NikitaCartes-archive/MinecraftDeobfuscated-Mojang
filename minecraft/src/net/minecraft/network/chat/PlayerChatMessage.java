package net.minecraft.network.chat;

import com.google.common.primitives.Ints;
import java.security.SignatureException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.SignatureUpdater;
import net.minecraft.util.SignatureValidator;

public record PlayerChatMessage(
	SignedMessageLink link, @Nullable MessageSignature signature, SignedMessageBody signedBody, @Nullable Component unsignedContent, FilterMask filterMask
) {
	private static final UUID SYSTEM_SENDER = Util.NIL_UUID;
	public static final Duration MESSAGE_EXPIRES_AFTER_SERVER = Duration.ofMinutes(5L);
	public static final Duration MESSAGE_EXPIRES_AFTER_CLIENT = MESSAGE_EXPIRES_AFTER_SERVER.plus(Duration.ofMinutes(2L));

	public static PlayerChatMessage system(String string) {
		return unsigned(SYSTEM_SENDER, string);
	}

	public static PlayerChatMessage unsigned(UUID uUID, String string) {
		SignedMessageBody signedMessageBody = SignedMessageBody.unsigned(string);
		SignedMessageLink signedMessageLink = SignedMessageLink.unsigned(uUID);
		return new PlayerChatMessage(signedMessageLink, null, signedMessageBody, null, FilterMask.PASS_THROUGH);
	}

	public PlayerChatMessage withUnsignedContent(Component component) {
		Component component2 = !component.equals(Component.literal(this.signedContent())) ? component : null;
		return new PlayerChatMessage(this.link, this.signature, this.signedBody, component2, this.filterMask);
	}

	public PlayerChatMessage removeUnsignedContent() {
		return this.unsignedContent != null ? new PlayerChatMessage(this.link, this.signature, this.signedBody, null, this.filterMask) : this;
	}

	public PlayerChatMessage filter(FilterMask filterMask) {
		return this.filterMask.equals(filterMask) ? this : new PlayerChatMessage(this.link, this.signature, this.signedBody, this.unsignedContent, filterMask);
	}

	public PlayerChatMessage filter(boolean bl) {
		return this.filter(bl ? this.filterMask : FilterMask.PASS_THROUGH);
	}

	public static void updateSignature(SignatureUpdater.Output output, SignedMessageLink signedMessageLink, SignedMessageBody signedMessageBody) throws SignatureException {
		output.update(Ints.toByteArray(1));
		signedMessageLink.updateSignature(output);
		signedMessageBody.updateSignature(output);
	}

	public boolean verify(SignatureValidator signatureValidator) {
		return this.signature != null && this.signature.verify(signatureValidator, output -> updateSignature(output, this.link, this.signedBody));
	}

	public String signedContent() {
		return this.signedBody.content();
	}

	public Component decoratedContent() {
		return (Component)Objects.requireNonNullElseGet(this.unsignedContent, () -> Component.literal(this.signedContent()));
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

	public UUID sender() {
		return this.link.sender();
	}

	public boolean isSystem() {
		return this.sender().equals(SYSTEM_SENDER);
	}

	public boolean hasSignature() {
		return this.signature != null;
	}

	public boolean hasSignatureFrom(UUID uUID) {
		return this.hasSignature() && this.link.sender().equals(uUID);
	}

	public boolean isFullyFiltered() {
		return this.filterMask.isFullyFiltered();
	}
}
