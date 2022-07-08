package net.minecraft.commands;

import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSigner;
import net.minecraft.network.chat.SignedMessageChain;

public interface CommandSigningContext {
	static CommandSigningContext anonymous() {
		final MessageSigner messageSigner = MessageSigner.system();
		return new CommandSigningContext() {
			@Override
			public MessageSignature getArgumentSignature(String string) {
				return MessageSignature.EMPTY;
			}

			@Override
			public MessageSigner argumentSigner() {
				return messageSigner;
			}

			@Override
			public boolean signedArgumentPreview(String string) {
				return false;
			}

			@Override
			public SignedMessageChain.Decoder decoder() {
				return SignedMessageChain.Decoder.UNSIGNED;
			}
		};
	}

	MessageSignature getArgumentSignature(String string);

	MessageSigner argumentSigner();

	SignedMessageChain.Decoder decoder();

	boolean signedArgumentPreview(String string);

	public static record SignedArguments(
		SignedMessageChain.Decoder decoder, MessageSigner argumentSigner, ArgumentSignatures argumentSignatures, boolean signedPreview
	) implements CommandSigningContext {
		@Override
		public MessageSignature getArgumentSignature(String string) {
			return this.argumentSignatures.get(string);
		}

		@Override
		public boolean signedArgumentPreview(String string) {
			return this.signedPreview;
		}
	}
}
