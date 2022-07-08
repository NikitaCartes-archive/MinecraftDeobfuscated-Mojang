package net.minecraft.network.chat;

import javax.annotation.Nullable;
import net.minecraft.util.SignatureValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;

public interface SignedMessageValidator {
	SignedMessageValidator ALWAYS_ACCEPT = new SignedMessageValidator() {
		@Override
		public void validateHeader(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs) {
		}

		@Override
		public boolean validateMessage(PlayerChatMessage playerChatMessage) {
			return true;
		}
	};

	static SignedMessageValidator create(@Nullable ProfilePublicKey profilePublicKey) {
		return (SignedMessageValidator)(profilePublicKey != null ? new SignedMessageValidator.KeyBased(profilePublicKey.createSignatureValidator()) : ALWAYS_ACCEPT);
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

		private boolean updateAndValidateChain(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature) {
			boolean bl = this.lastSignature == null || this.lastSignature.equals(signedMessageHeader.previousSignature()) || this.lastSignature.equals(messageSignature);
			this.lastSignature = messageSignature;
			return bl;
		}

		@Override
		public void validateHeader(SignedMessageHeader signedMessageHeader, MessageSignature messageSignature, byte[] bs) {
			boolean bl = messageSignature.verify(this.validator, signedMessageHeader, bs);
			boolean bl2 = this.updateAndValidateChain(signedMessageHeader, messageSignature);
			this.isChainConsistent = this.isChainConsistent && bl && bl2;
		}

		@Override
		public boolean validateMessage(PlayerChatMessage playerChatMessage) {
			byte[] bs = playerChatMessage.signedBody().hash().asBytes();
			boolean bl = playerChatMessage.headerSignature().verify(this.validator, playerChatMessage.signedHeader(), bs);
			boolean bl2 = this.updateAndValidateChain(playerChatMessage.signedHeader(), playerChatMessage.headerSignature());
			boolean bl3 = this.isChainConsistent && bl && bl2;
			this.isChainConsistent = bl;
			return bl3;
		}
	}
}
