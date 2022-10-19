/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.util.SignatureValidator;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface SignedMessageValidator {
    public static final SignedMessageValidator ACCEPT_UNSIGNED = playerChatMessage -> !playerChatMessage.hasSignature();
    public static final SignedMessageValidator REJECT_ALL = playerChatMessage -> false;

    public boolean updateAndValidate(PlayerChatMessage var1);

    public static class KeyBased
    implements SignedMessageValidator {
        private final SignatureValidator validator;
        @Nullable
        private PlayerChatMessage lastMessage;
        private boolean isChainValid = true;

        public KeyBased(SignatureValidator signatureValidator) {
            this.validator = signatureValidator;
        }

        private boolean validateChain(PlayerChatMessage playerChatMessage) {
            if (playerChatMessage.equals(this.lastMessage)) {
                return true;
            }
            return this.lastMessage == null || playerChatMessage.link().isDescendantOf(this.lastMessage.link());
        }

        @Override
        public boolean updateAndValidate(PlayerChatMessage playerChatMessage) {
            boolean bl = this.isChainValid = this.isChainValid && playerChatMessage.verify(this.validator) && this.validateChain(playerChatMessage);
            if (!this.isChainValid) {
                return false;
            }
            this.lastMessage = playerChatMessage;
            return true;
        }
    }
}

