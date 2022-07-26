package net.minecraft.network.chat;

import javax.annotation.Nullable;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;

public interface SignedMessageValidator {
	static SignedMessageValidator create(@Nullable ProfilePublicKey profilePublicKey, boolean bl) {
		return (SignedMessageValidator)(profilePublicKey != null
			? new SignedMessageValidator.KeyBased(profilePublicKey.createSignatureValidator())
			: new SignedMessageValidator.Unsigned(bl));
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

		private boolean validateChain(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, boolean bl) {
			if (messageSignature.isEmpty()) {
				return false;
			} else {
				return bl && messageSignature.equals(this.lastSignature)
					? true
					: this.lastSignature == null || this.lastSignature.equals(signedMessageHeader.previousSignature());
			}
		}

		private boolean validateContents(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs, boolean bl) {
			return this.validateChain(signedMessageHeader, messageSignature, bl) && messageSignature.verify(this.validator, signedMessageHeader, bs);
		}

		private SignedMessageValidator.State updateAndValidate(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs, boolean bl) {
			this.isChainConsistent = this.isChainConsistent && this.validateContents(signedMessageHeader, messageSignature, bs, bl);
			if (!this.isChainConsistent) {
				return SignedMessageValidator.State.BROKEN_CHAIN;
			} else {
				this.lastSignature = messageSignature;
				return SignedMessageValidator.State.SECURE;
			}
		}

		@Override
		public SignedMessageValidator.State validateHeader(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs) {
			return this.updateAndValidate(signedMessageHeader, messageSignature, bs, false);
		}

		@Override
		public SignedMessageValidator.State validateMessage(PlayerChatMessage playerChatMessage) {
			byte[] bs = playerChatMessage.signedBody().hash().asBytes();
			return this.updateAndValidate(playerChatMessage.signedHeader(), playerChatMessage.headerSignature(), bs, true);
		}
	}

	public static enum State {
		SECURE,
		NOT_SECURE,
		BROKEN_CHAIN;
	}

	public static class Unsigned implements SignedMessageValidator {
		private final boolean enforcesSecureChat;

		public Unsigned(boolean bl) {
			this.enforcesSecureChat = bl;
		}

		private SignedMessageValidator.State validate(MessageSignature messageSignature) {
			if (!messageSignature.isEmpty()) {
				return SignedMessageValidator.State.BROKEN_CHAIN;
			} else {
				return this.enforcesSecureChat ? SignedMessageValidator.State.BROKEN_CHAIN : SignedMessageValidator.State.NOT_SECURE;
			}
		}

		@Override
		public SignedMessageValidator.State validateHeader(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs) {
			return this.validate(messageSignature);
		}

		@Override
		public SignedMessageValidator.State validateMessage(PlayerChatMessage playerChatMessage) {
			return this.validate(playerChatMessage.headerSignature());
		}
	}
}
