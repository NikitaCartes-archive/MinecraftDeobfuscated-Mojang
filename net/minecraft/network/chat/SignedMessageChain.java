/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSigner;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageHeader;
import net.minecraft.server.network.FilteredText;
import net.minecraft.util.Signer;
import org.jetbrains.annotations.Nullable;

public class SignedMessageChain {
    @Nullable
    private MessageSignature previousSignature;

    private Link pack(Signer signer, MessageSigner messageSigner, Component component) {
        MessageSignature messageSignature;
        this.previousSignature = messageSignature = SignedMessageChain.pack(signer, messageSigner, this.previousSignature, component);
        return new Link(messageSignature);
    }

    private static MessageSignature pack(Signer signer, MessageSigner messageSigner, @Nullable MessageSignature messageSignature, Component component) {
        SignedMessageHeader signedMessageHeader = new SignedMessageHeader(messageSignature, messageSigner.profileId());
        SignedMessageBody signedMessageBody = new SignedMessageBody(component, messageSigner.timeStamp(), messageSigner.salt(), List.of());
        byte[] bs = signedMessageBody.hash().asBytes();
        return new MessageSignature(signer.sign(output -> signedMessageHeader.updateSignature(output, bs)));
    }

    private PlayerChatMessage unpack(Link link, MessageSigner messageSigner, Component component) {
        PlayerChatMessage playerChatMessage = SignedMessageChain.unpack(link, this.previousSignature, messageSigner, component);
        this.previousSignature = link.signature;
        return playerChatMessage;
    }

    private static PlayerChatMessage unpack(Link link, @Nullable MessageSignature messageSignature, MessageSigner messageSigner, Component component) {
        SignedMessageHeader signedMessageHeader = new SignedMessageHeader(messageSignature, messageSigner.profileId());
        SignedMessageBody signedMessageBody = new SignedMessageBody(component, messageSigner.timeStamp(), messageSigner.salt(), List.of());
        return new PlayerChatMessage(signedMessageHeader, link.signature, signedMessageBody, Optional.empty());
    }

    public Decoder decoder() {
        return this::unpack;
    }

    public Encoder encoder() {
        return this::pack;
    }

    public record Link(MessageSignature signature) {
    }

    @FunctionalInterface
    public static interface Decoder {
        public static final Decoder UNSIGNED = (link, messageSigner, component) -> PlayerChatMessage.unsigned(messageSigner, component);

        public PlayerChatMessage unpack(Link var1, MessageSigner var2, Component var3);

        default public FilteredText<PlayerChatMessage> unpack(Link link, MessageSigner messageSigner, FilteredText<Component> filteredText) {
            return this.unpack(link, messageSigner, filteredText.raw()).withFilteredText(filteredText.filtered());
        }
    }

    @FunctionalInterface
    public static interface Encoder {
        public Link pack(Signer var1, MessageSigner var2, Component var3);
    }
}

