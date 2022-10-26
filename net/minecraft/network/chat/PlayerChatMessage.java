/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import com.google.common.primitives.Ints;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.security.SignatureException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageLink;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.SignatureUpdater;
import net.minecraft.util.SignatureValidator;
import org.jetbrains.annotations.Nullable;

public record PlayerChatMessage(SignedMessageLink link, @Nullable MessageSignature signature, SignedMessageBody signedBody, @Nullable Component unsignedContent, FilterMask filterMask) {
    public static final MapCodec<PlayerChatMessage> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(((MapCodec)SignedMessageLink.CODEC.fieldOf("link")).forGetter(PlayerChatMessage::link), MessageSignature.CODEC.optionalFieldOf("signature").forGetter(playerChatMessage -> Optional.ofNullable(playerChatMessage.signature)), SignedMessageBody.MAP_CODEC.forGetter(PlayerChatMessage::signedBody), ExtraCodecs.COMPONENT.optionalFieldOf("unsigned_content").forGetter(playerChatMessage -> Optional.ofNullable(playerChatMessage.unsignedContent)), FilterMask.CODEC.optionalFieldOf("filter_mask", FilterMask.PASS_THROUGH).forGetter(PlayerChatMessage::filterMask)).apply((Applicative<PlayerChatMessage, ?>)instance, (signedMessageLink, optional, signedMessageBody, optional2, filterMask) -> new PlayerChatMessage((SignedMessageLink)signedMessageLink, optional.orElse(null), (SignedMessageBody)signedMessageBody, optional2.orElse(null), (FilterMask)filterMask)));
    private static final UUID SYSTEM_SENDER = Util.NIL_UUID;
    public static final Duration MESSAGE_EXPIRES_AFTER_SERVER = Duration.ofMinutes(5L);
    public static final Duration MESSAGE_EXPIRES_AFTER_CLIENT = MESSAGE_EXPIRES_AFTER_SERVER.plus(Duration.ofMinutes(2L));

    public static PlayerChatMessage system(String string) {
        return PlayerChatMessage.unsigned(SYSTEM_SENDER, string);
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
        if (this.unsignedContent != null) {
            return new PlayerChatMessage(this.link, this.signature, this.signedBody, null, this.filterMask);
        }
        return this;
    }

    public PlayerChatMessage filter(FilterMask filterMask) {
        if (this.filterMask.equals(filterMask)) {
            return this;
        }
        return new PlayerChatMessage(this.link, this.signature, this.signedBody, this.unsignedContent, filterMask);
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
        return this.signature != null && this.signature.verify(signatureValidator, output -> PlayerChatMessage.updateSignature(output, this.link, this.signedBody));
    }

    public String signedContent() {
        return this.signedBody.content();
    }

    public Component decoratedContent() {
        return Objects.requireNonNullElseGet(this.unsignedContent, () -> Component.literal(this.signedContent()));
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

    @Nullable
    public MessageSignature signature() {
        return this.signature;
    }

    @Nullable
    public Component unsignedContent() {
        return this.unsignedContent;
    }
}

