/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.SignedMessageHeader;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.jetbrains.annotations.Nullable;

public interface SignedMessageValidator {
    public static SignedMessageValidator create(@Nullable ProfilePublicKey profilePublicKey, boolean bl) {
        if (profilePublicKey != null) {
            return new KeyBased(profilePublicKey.createSignatureValidator());
        }
        return new Unsigned(bl);
    }

    public State validateHeader(SignedMessageHeader var1, MessageSignature var2, byte[] var3);

    public State validateMessage(PlayerChatMessage var1);

    public static class KeyBased
    implements SignedMessageValidator {
        private final SignatureValidator validator;
        @Nullable
        private MessageSignature lastSignature;
        private boolean isChainConsistent = true;

        public KeyBased(SignatureValidator signatureValidator) {
            this.validator = signatureValidator;
        }

        private boolean validateChain(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, boolean bl) {
            if (messageSignature.isEmpty()) {
                return false;
            }
            if (bl && messageSignature.equals(this.lastSignature)) {
                return true;
            }
            return this.lastSignature == null || this.lastSignature.equals(signedMessageHeader.previousSignature());
        }

        private boolean validateContents(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs, boolean bl) {
            return this.validateChain(signedMessageHeader, messageSignature, bl) && messageSignature.verify(this.validator, signedMessageHeader, bs);
        }

        private State updateAndValidate(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs, boolean bl) {
            boolean bl2 = this.isChainConsistent = this.isChainConsistent && this.validateContents(signedMessageHeader, messageSignature, bs, bl);
            if (!this.isChainConsistent) {
                return State.BROKEN_CHAIN;
            }
            this.lastSignature = messageSignature;
            return State.SECURE;
        }

        @Override
        public State validateHeader(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs) {
            return this.updateAndValidate(signedMessageHeader, messageSignature, bs, false);
        }

        @Override
        public State validateMessage(PlayerChatMessage playerChatMessage) {
            byte[] bs = playerChatMessage.signedBody().hash().asBytes();
            return this.updateAndValidate(playerChatMessage.signedHeader(), playerChatMessage.headerSignature(), bs, true);
        }
    }

    public static class Unsigned
    implements SignedMessageValidator {
        private final boolean enforcesSecureChat;

        public Unsigned(boolean bl) {
            this.enforcesSecureChat = bl;
        }

        private State validate(MessageSignature messageSignature) {
            if (!messageSignature.isEmpty()) {
                return State.BROKEN_CHAIN;
            }
            return this.enforcesSecureChat ? State.BROKEN_CHAIN : State.NOT_SECURE;
        }

        @Override
        public State validateHeader(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs) {
            return this.validate(messageSignature);
        }

        @Override
        public State validateMessage(PlayerChatMessage playerChatMessage) {
            return this.validate(playerChatMessage.headerSignature());
        }
    }

    public static enum State {
        SECURE,
        NOT_SECURE,
        BROKEN_CHAIN;

    }
}

