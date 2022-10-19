/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import com.mojang.logging.LogUtils;
import java.time.Instant;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageLink;
import net.minecraft.network.chat.ThrowingComponent;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.Signer;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SignedMessageChain {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    private SignedMessageLink nextLink;

    public SignedMessageChain(UUID uUID, UUID uUID2) {
        this.nextLink = SignedMessageLink.root(uUID, uUID2);
    }

    public Encoder encoder(Signer signer) {
        return signedMessageBody -> {
            SignedMessageLink signedMessageLink = this.advanceLink();
            if (signedMessageLink == null) {
                return null;
            }
            return new MessageSignature(signer.sign(output -> PlayerChatMessage.updateSignature(output, signedMessageLink, signedMessageBody)));
        };
    }

    public Decoder decoder(ProfilePublicKey profilePublicKey) {
        SignatureValidator signatureValidator = profilePublicKey.createSignatureValidator();
        return (messageSignature, signedMessageBody) -> {
            SignedMessageLink signedMessageLink = this.advanceLink();
            if (signedMessageLink == null) {
                throw new DecodeException((Component)Component.translatable("chat.disabled.chain_broken"), false);
            }
            if (profilePublicKey.data().hasExpired()) {
                throw new DecodeException((Component)Component.translatable("chat.disabled.expiredProfileKey"), false);
            }
            PlayerChatMessage playerChatMessage = new PlayerChatMessage(signedMessageLink, messageSignature, signedMessageBody, null, FilterMask.PASS_THROUGH);
            if (!playerChatMessage.verify(signatureValidator)) {
                throw new DecodeException((Component)Component.translatable("multiplayer.disconnect.unsigned_chat"), true);
            }
            if (playerChatMessage.hasExpiredServer(Instant.now())) {
                LOGGER.warn("Received expired chat: '{}'. Is the client/server system time unsynchronized?", (Object)signedMessageBody.content());
            }
            return playerChatMessage;
        };
    }

    @Nullable
    private SignedMessageLink advanceLink() {
        SignedMessageLink signedMessageLink = this.nextLink;
        if (signedMessageLink != null) {
            this.nextLink = signedMessageLink.advance();
        }
        return signedMessageLink;
    }

    @FunctionalInterface
    public static interface Encoder {
        public static final Encoder UNSIGNED = signedMessageBody -> null;

        @Nullable
        public MessageSignature pack(SignedMessageBody var1);
    }

    @FunctionalInterface
    public static interface Decoder {
        public static Decoder unsigned(UUID uUID) {
            return (messageSignature, signedMessageBody) -> PlayerChatMessage.unsigned(uUID, signedMessageBody.content());
        }

        public PlayerChatMessage unpack(@Nullable MessageSignature var1, SignedMessageBody var2) throws DecodeException;
    }

    public static class DecodeException
    extends ThrowingComponent {
        private final boolean shouldDisconnect;

        public DecodeException(Component component, boolean bl) {
            super(component);
            this.shouldDisconnect = bl;
        }

        public boolean shouldDisconnect() {
            return this.shouldDisconnect;
        }
    }
}

