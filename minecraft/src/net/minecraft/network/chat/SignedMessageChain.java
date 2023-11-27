package net.minecraft.network.chat;

import com.mojang.logging.LogUtils;
import java.time.Instant;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.util.SignatureValidator;
import net.minecraft.util.Signer;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.slf4j.Logger;

public class SignedMessageChain {
	private static final Logger LOGGER = LogUtils.getLogger();
	@Nullable
	private SignedMessageLink nextLink;
	private Instant lastTimeStamp = Instant.EPOCH;

	public SignedMessageChain(UUID uUID, UUID uUID2) {
		this.nextLink = SignedMessageLink.root(uUID, uUID2);
	}

	public SignedMessageChain.Encoder encoder(Signer signer) {
		return signedMessageBody -> {
			SignedMessageLink signedMessageLink = this.advanceLink();
			return signedMessageLink == null
				? null
				: new MessageSignature(signer.sign(output -> PlayerChatMessage.updateSignature(output, signedMessageLink, signedMessageBody)));
		};
	}

	public SignedMessageChain.Decoder decoder(ProfilePublicKey profilePublicKey) {
		SignatureValidator signatureValidator = profilePublicKey.createSignatureValidator();
		return (messageSignature, signedMessageBody) -> {
			SignedMessageLink signedMessageLink = this.advanceLink();
			if (signedMessageLink == null) {
				throw new SignedMessageChain.DecodeException(Component.translatable("chat.disabled.chain_broken"), false);
			} else if (profilePublicKey.data().hasExpired()) {
				throw new SignedMessageChain.DecodeException(Component.translatable("chat.disabled.expiredProfileKey"), false);
			} else if (signedMessageBody.timeStamp().isBefore(this.lastTimeStamp)) {
				throw new SignedMessageChain.DecodeException(Component.translatable("multiplayer.disconnect.out_of_order_chat"), true);
			} else {
				this.lastTimeStamp = signedMessageBody.timeStamp();
				PlayerChatMessage playerChatMessage = new PlayerChatMessage(signedMessageLink, messageSignature, signedMessageBody, null, FilterMask.PASS_THROUGH);
				if (!playerChatMessage.verify(signatureValidator)) {
					throw new SignedMessageChain.DecodeException(Component.translatable("multiplayer.disconnect.unsigned_chat"), true);
				} else {
					if (playerChatMessage.hasExpiredServer(Instant.now())) {
						LOGGER.warn("Received expired chat: '{}'. Is the client/server system time unsynchronized?", signedMessageBody.content());
					}

					return playerChatMessage;
				}
			}
		};
	}

	@Nullable
	private SignedMessageLink advanceLink() {
		SignedMessageLink signedMessageLink = this.nextLink;
		if (signedMessageLink != null) {
			this.nextLink = signedMessageLink.advance();
		}

		return signedMessageLink;
	}

	public static class DecodeException extends ThrowingComponent {
		private final boolean shouldDisconnect;

		public DecodeException(Component component, boolean bl) {
			super(component);
			this.shouldDisconnect = bl;
		}

		public boolean shouldDisconnect() {
			return this.shouldDisconnect;
		}
	}

	@FunctionalInterface
	public interface Decoder {
		static SignedMessageChain.Decoder unsigned(UUID uUID, BooleanSupplier booleanSupplier) {
			return (messageSignature, signedMessageBody) -> {
				if (booleanSupplier.getAsBoolean()) {
					throw new SignedMessageChain.DecodeException(Component.translatable("chat.disabled.missingProfileKey"), false);
				} else {
					return PlayerChatMessage.unsigned(uUID, signedMessageBody.content());
				}
			};
		}

		PlayerChatMessage unpack(@Nullable MessageSignature messageSignature, SignedMessageBody signedMessageBody) throws SignedMessageChain.DecodeException;
	}

	@FunctionalInterface
	public interface Encoder {
		SignedMessageChain.Encoder UNSIGNED = signedMessageBody -> null;

		@Nullable
		MessageSignature pack(SignedMessageBody signedMessageBody);
	}
}
