package net.minecraft.network.chat;

import com.mojang.logging.LogUtils;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.util.SignatureValidator;
import org.slf4j.Logger;

@FunctionalInterface
public interface SignedMessageValidator {
	Logger LOGGER = LogUtils.getLogger();
	SignedMessageValidator ACCEPT_UNSIGNED = playerChatMessage -> {
		if (playerChatMessage.hasSignature()) {
			LOGGER.error("Received chat message with signature from {}, but they have no chat session initialized", playerChatMessage.sender());
			return false;
		} else {
			return true;
		}
	};
	SignedMessageValidator REJECT_ALL = playerChatMessage -> {
		LOGGER.error("Received chat message from {}, but they have no chat session initialized and secure chat is enforced", playerChatMessage.sender());
		return false;
	};

	boolean updateAndValidate(PlayerChatMessage playerChatMessage);

	public static class KeyBased implements SignedMessageValidator {
		private final SignatureValidator validator;
		private final BooleanSupplier expired;
		@Nullable
		private PlayerChatMessage lastMessage;
		private boolean isChainValid = true;

		public KeyBased(SignatureValidator signatureValidator, BooleanSupplier booleanSupplier) {
			this.validator = signatureValidator;
			this.expired = booleanSupplier;
		}

		private boolean validateChain(PlayerChatMessage playerChatMessage) {
			if (playerChatMessage.equals(this.lastMessage)) {
				return true;
			} else if (this.lastMessage != null && !playerChatMessage.link().isDescendantOf(this.lastMessage.link())) {
				LOGGER.error(
					"Received out-of-order chat message from {}: expected index > {} for session {}, but was {} for session {}",
					playerChatMessage.sender(),
					this.lastMessage.link().index(),
					this.lastMessage.link().sessionId(),
					playerChatMessage.link().index(),
					playerChatMessage.link().sessionId()
				);
				return false;
			} else {
				return true;
			}
		}

		private boolean validate(PlayerChatMessage playerChatMessage) {
			if (this.expired.getAsBoolean()) {
				LOGGER.error("Received message from player with expired profile public key: {}", playerChatMessage);
				return false;
			} else if (!playerChatMessage.verify(this.validator)) {
				LOGGER.error("Received message with invalid signature from {}", playerChatMessage.sender());
				return false;
			} else {
				return this.validateChain(playerChatMessage);
			}
		}

		@Override
		public boolean updateAndValidate(PlayerChatMessage playerChatMessage) {
			this.isChainValid = this.isChainValid && this.validate(playerChatMessage);
			if (!this.isChainValid) {
				return false;
			} else {
				this.lastMessage = playerChatMessage;
				return true;
			}
		}
	}
}
