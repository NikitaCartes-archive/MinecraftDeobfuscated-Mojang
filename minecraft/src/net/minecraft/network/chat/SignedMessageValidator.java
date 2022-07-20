package net.minecraft.network.chat;

import javax.annotation.Nullable;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;

public interface SignedMessageValidator {
	SignedMessageValidator ALWAYS_REJECT = new SignedMessageValidator() {
		@Override
		public SignedMessageValidator.State validateHeader(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs) {
			return SignedMessageValidator.State.NOT_SECURE;
		}

		@Override
		public SignedMessageValidator.State validateMessage(PlayerChatMessage playerChatMessage) {
			return SignedMessageValidator.State.NOT_SECURE;
		}
	};

	static SignedMessageValidator create(@Nullable ProfilePublicKey profilePublicKey) {
		return (SignedMessageValidator)(profilePublicKey != null ? new SignedMessageValidator.KeyBased(profilePublicKey.createSignatureValidator()) : ALWAYS_REJECT);
	}

	SignedMessageValidator.State validateHeader(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs);

	SignedMessageValidator.State validateMessage(PlayerChatMessage playerChatMessage);

	public static class KeyBased implements SignedMessageValidator {
		private final SignatureValidator validator;
		@Nullable
		private MessageSignature lastSignature;
		private boolean isChainConsistent = true;

		public KeyBased(SignatureValidator signatureValidator) {
			this.validator = signatureValidator;
		}

		private boolean validateChain(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature) {
			return messageSignature.isEmpty()
				? false
				: this.lastSignature == null || this.lastSignature.equals(signedMessageHeader.previousSignature()) || this.lastSignature.equals(messageSignature);
		}

		private boolean validateContents(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs) {
			return messageSignature.verify(this.validator, signedMessageHeader, bs);
		}

		private SignedMessageValidator.State updateAndValidate(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs) {
			this.isChainConsistent = this.isChainConsistent && this.validateChain(signedMessageHeader, messageSignature);
			if (!this.isChainConsistent) {
				return SignedMessageValidator.State.BROKEN_CHAIN;
			} else if (!this.validateContents(signedMessageHeader, messageSignature, bs)) {
				this.lastSignature = null;
				return SignedMessageValidator.State.NOT_SECURE;
			} else {
				this.lastSignature = messageSignature;
				return SignedMessageValidator.State.SECURE;
			}
		}

		@Override
		public SignedMessageValidator.State validateHeader(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs) {
			return this.updateAndValidate(signedMessageHeader, messageSignature, bs);
		}

		@Override
		public SignedMessageValidator.State validateMessage(PlayerChatMessage playerChatMessage) {
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
