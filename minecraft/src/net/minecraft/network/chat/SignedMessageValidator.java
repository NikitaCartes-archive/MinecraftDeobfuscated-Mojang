package net.minecraft.network.chat;

import javax.annotation.Nullable;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;

public interface SignedMessageValidator {
	SignedMessageValidator ALWAYS_REJECT = new SignedMessageValidator() {
		@Override
		public void validateHeader(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs) {
		}

		@Override
		public boolean validateMessage(PlayerChatMessage playerChatMessage) {
			return false;
		}
	};

	static SignedMessageValidator create(@Nullable ProfilePublicKey profilePublicKey) {
		return (SignedMessageValidator)(profilePublicKey != null ? new SignedMessageValidator.KeyBased(profilePublicKey.createSignatureValidator()) : ALWAYS_REJECT);
	}

	void validateHeader(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs);

	boolean validateMessage(PlayerChatMessage playerChatMessage);

	public static class KeyBased implements SignedMessageValidator {
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
			if (this.isChainConsistent
				&& this.validateContents(playerChatMessage.signedHeader(), playerChatMessage.headerSignature(), playerChatMessage.signedBody().hash().asBytes())) {
				this.lastSignature = playerChatMessage.headerSignature();
				return true;
			} else {
				this.isChainConsistent = true;
				this.lastSignature = null;
				return false;
			}
		}
	}
}
