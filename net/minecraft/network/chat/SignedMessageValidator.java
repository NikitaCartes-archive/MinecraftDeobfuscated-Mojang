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
        public void validateHeader(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs) {
        }

        @Override
        public boolean validateMessage(PlayerChatMessage playerChatMessage) {
            return false;
        }
    };

    public static SignedMessageValidator create(@Nullable ProfilePublicKey profilePublicKey) {
        return profilePublicKey != null ? new KeyBased(profilePublicKey.createSignatureValidator()) : ALWAYS_REJECT;
    }

    public void validateHeader(SignedMessageHeader var1, MessageSignature var2, byte[] var3);

    public boolean validateMessage(PlayerChatMessage var1);

    public static class KeyBased
    implements SignedMessageValidator {
        private final SignatureValidator validator;
        @Nullable
        private MessageSignature lastSignature;
        boolean isChainConsistent = true;

        public KeyBased(SignatureValidator signatureValidator) {
            this.validator = signatureValidator;
        }

        private boolean validateChain(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature) {
            return this.lastSignature == null || this.lastSignature.equals(signedMessageHeader.previousSignature()) || this.lastSignature.equals(messageSignature);
        }

        private boolean validateContents(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs) {
            return this.validateChain(signedMessageHeader, messageSignature) && messageSignature.verify(this.validator, signedMessageHeader, bs);
        }

        @Override
        public void validateHeader(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs) {
            this.isChainConsistent = this.isChainConsistent && this.validateContents(signedMessageHeader, messageSignature, bs);
            this.lastSignature = messageSignature;
        }

        @Override
        public boolean validateMessage(PlayerChatMessage playerChatMessage) {
            if (this.isChainConsistent && this.validateContents(playerChatMessage.signedHeader(), playerChatMessage.headerSignature(), playerChatMessage.signedBody().hash().asBytes())) {
                this.lastSignature = playerChatMessage.headerSignature();
                return true;
            }
            this.isChainConsistent = true;
            this.lastSignature = null;
            return false;
        }
    }
}

