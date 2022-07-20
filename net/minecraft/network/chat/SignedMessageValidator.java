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
    public static final SignedMessageValidator ALWAYS_REJECT = new SignedMessageValidator(){

        @Override
        public State validateHeader(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs) {
            return State.NOT_SECURE;
        }

        @Override
        public State validateMessage(PlayerChatMessage playerChatMessage) {
            return State.NOT_SECURE;
        }
    };

    public static SignedMessageValidator create(@Nullable ProfilePublicKey profilePublicKey) {
        return profilePublicKey != null ? new KeyBased(profilePublicKey.createSignatureValidator()) : ALWAYS_REJECT;
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

        private boolean validateChain(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature) {
            if (messageSignature.isEmpty()) {
                return false;
            }
            return this.lastSignature == null || this.lastSignature.equals(signedMessageHeader.previousSignature()) || this.lastSignature.equals(messageSignature);
        }

        private boolean validateContents(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs) {
            return messageSignature.verify(this.validator, signedMessageHeader, bs);
        }

        private State updateAndValidate(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs) {
            boolean bl = this.isChainConsistent = this.isChainConsistent && this.validateChain(signedMessageHeader, messageSignature);
            if (!this.isChainConsistent) {
                return State.BROKEN_CHAIN;
            }
            if (!this.validateContents(signedMessageHeader, messageSignature, bs)) {
                this.lastSignature = null;
                return State.NOT_SECURE;
            }
            this.lastSignature = messageSignature;
            return State.SECURE;
        }

        @Override
        public State validateHeader(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs) {
            return this.updateAndValidate(signedMessageHeader, messageSignature, bs);
        }

        @Override
        public State validateMessage(PlayerChatMessage playerChatMessage) {
            byte[] bs = playerChatMessage.signedBody().hash().asBytes();
            return this.updateAndValidate(playerChatMessage.signedHeader(), playerChatMessage.headerSignature(), bs);
        }
    }

    public static enum State {
        SECURE,
        NOT_SECURE,
        BROKEN_CHAIN;

    }
}

