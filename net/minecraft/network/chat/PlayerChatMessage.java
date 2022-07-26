/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ChatMessageContent;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSigner;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageHeader;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.jetbrains.annotations.Nullable;

public record PlayerChatMessage(SignedMessageHeader signedHeader, MessageSignature headerSignature, SignedMessageBody signedBody, Optional<Component> unsignedContent, FilterMask filterMask) {
    public static final Duration MESSAGE_EXPIRES_AFTER_SERVER = Duration.ofMinutes(5L);
    public static final Duration MESSAGE_EXPIRES_AFTER_CLIENT = MESSAGE_EXPIRES_AFTER_SERVER.plus(Duration.ofMinutes(2L));

    public PlayerChatMessage(FriendlyByteBuf friendlyByteBuf) {
        this(new SignedMessageHeader(friendlyByteBuf), new MessageSignature(friendlyByteBuf), new SignedMessageBody(friendlyByteBuf), friendlyByteBuf.readOptional(FriendlyByteBuf::readComponent), FilterMask.read(friendlyByteBuf));
    }

    public static PlayerChatMessage system(ChatMessageContent chatMessageContent) {
        return PlayerChatMessage.unsigned(MessageSigner.system(), chatMessageContent);
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
        if (this.unsignedContent.isPresent()) {
            return new PlayerChatMessage(this.signedHeader, this.headerSignature, this.signedBody, Optional.empty(), this.filterMask);
        }
        return this;
    }

    public PlayerChatMessage filter(FilterMask filterMask) {
        if (this.filterMask.equals(filterMask)) {
            return this;
        }
        return new PlayerChatMessage(this.signedHeader, this.headerSignature, this.signedBody, this.unsignedContent, filterMask);
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
        return this.unsignedContent().orElse(this.signedContent().decorated());
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
        if (!this.headerSignature.isEmpty() && !messageSigner.isSystem()) {
            return new LastSeenMessages.Entry(messageSigner.profileId(), this.headerSignature);
        }
        return null;
    }

    public boolean hasSignatureFrom(UUID uUID) {
        return !this.headerSignature.isEmpty() && this.signedHeader.sender().equals(uUID);
    }

    public boolean isFullyFiltered() {
        return this.filterMask.isFullyFiltered();
    }
}

