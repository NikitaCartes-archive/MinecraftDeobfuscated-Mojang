/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.network.chat.ChatMessageContent;
import net.minecraft.network.chat.LastSeenMessages;
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

    private Link pack(Signer signer, MessageSigner messageSigner, ChatMessageContent chatMessageContent, LastSeenMessages lastSeenMessages) {
        MessageSignature messageSignature;
        this.previousSignature = messageSignature = SignedMessageChain.pack(signer, messageSigner, this.previousSignature, chatMessageContent, lastSeenMessages);
        return new Link(messageSignature);
    }

    private static MessageSignature pack(Signer signer, MessageSigner messageSigner, @Nullable MessageSignature messageSignature, ChatMessageContent chatMessageContent, LastSeenMessages lastSeenMessages) {
        SignedMessageHeader signedMessageHeader = new SignedMessageHeader(messageSignature, messageSigner.profileId());
        SignedMessageBody signedMessageBody = new SignedMessageBody(chatMessageContent, messageSigner.timeStamp(), messageSigner.salt(), lastSeenMessages);
        byte[] bs = signedMessageBody.hash().asBytes();
        return new MessageSignature(signer.sign(output -> signedMessageHeader.updateSignature(output, bs)));
    }

    private PlayerChatMessage unpack(Link link, MessageSigner messageSigner, ChatMessageContent chatMessageContent, LastSeenMessages lastSeenMessages) {
        PlayerChatMessage playerChatMessage = SignedMessageChain.unpack(link, this.previousSignature, messageSigner, chatMessageContent, lastSeenMessages);
        this.previousSignature = link.signature;
        return playerChatMessage;
    }

    private static PlayerChatMessage unpack(Link link, @Nullable MessageSignature messageSignature, MessageSigner messageSigner, ChatMessageContent chatMessageContent, LastSeenMessages lastSeenMessages) {
        SignedMessageHeader signedMessageHeader = new SignedMessageHeader(messageSignature, messageSigner.profileId());
        SignedMessageBody signedMessageBody = new SignedMessageBody(chatMessageContent, messageSigner.timeStamp(), messageSigner.salt(), lastSeenMessages);
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
        public static final Decoder UNSIGNED = (link, messageSigner, chatMessageContent, lastSeenMessages) -> PlayerChatMessage.unsigned(messageSigner, chatMessageContent.decorated());

        public PlayerChatMessage unpack(Link var1, MessageSigner var2, ChatMessageContent var3, LastSeenMessages var4);

        default public FilteredText<PlayerChatMessage> unpack(Link link, MessageSigner messageSigner, FilteredText<ChatMessageContent> filteredText, LastSeenMessages lastSeenMessages) {
            return this.unpack(link, messageSigner, filteredText.raw(), lastSeenMessages).withFilteredText(Util.mapNullable(filteredText.filtered(), ChatMessageContent::decorated));
        }
    }

    @FunctionalInterface
    public static interface Encoder {
        public Link pack(Signer var1, MessageSigner var2, ChatMessageContent var3, LastSeenMessages var4);
    }
}

