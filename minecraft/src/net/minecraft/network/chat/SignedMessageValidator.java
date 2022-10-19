package net.minecraft.network.chat;

import javax.annotation.Nullable;
import net.minecraft.util.SignatureValidator;

@FunctionalInterface
public interface SignedMessageValidator {
	SignedMessageValidator ACCEPT_UNSIGNED = playerChatMessage -> !playerChatMessage.hasSignature();
	SignedMessageValidator REJECT_ALL = playerChatMessage -> false;

	boolean updateAndValidate(PlayerChatMessage playerChatMessage);

	public static class KeyBased implements SignedMessageValidator {
		private final SignatureValidator validator;
		@Nullable
		private PlayerChatMessage lastMessage;
		private boolean isChainValid = true;

		public KeyBased(SignatureValidator signatureValidator) {
			this.validator = signatureValidator;
		}

		private boolean validateChain(PlayerChatMessage playerChatMessage) {
			return playerChatMessage.equals(this.lastMessage) ? true : this.lastMessage == null || playerChatMessage.link().isDescendantOf(this.lastMessage.link());
		}

		@Override
		public boolean updateAndValidate(PlayerChatMessage playerChatMessage) {
			this.isChainValid = this.isChainValid && playerChatMessage.verify(this.validator) && this.validateChain(playerChatMessage);
			if (!this.isChainValid) {
				return false;
			} else {
				this.lastMessage = playerChatMessage;
				return true;
			}
		}
	}
}
