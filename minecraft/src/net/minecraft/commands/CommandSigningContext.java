package net.minecraft.commands;

import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.MessageSigner;
import net.minecraft.network.chat.SignedMessageChain;

public interface CommandSigningContext {
	static CommandSigningContext anonymous() {
		final MessageSigner messageSigner = MessageSigner.system();
		return new CommandSigningContext() {
			@Override
			public CommandSigningContext.SignedArgument getArgument(String string) {
				return CommandSigningContext.SignedArgument.UNSIGNED;
			}

			@Override
			public MessageSigner argumentSigner() {
				return messageSigner;
			}

			@Override
			public SignedMessageChain.Decoder decoder() {
				return SignedMessageChain.Decoder.UNSIGNED;
			}
		};
	}

	CommandSigningContext.SignedArgument getArgument(String string);

	MessageSigner argumentSigner();

	SignedMessageChain.Decoder decoder();

	public static record SignedArgument(MessageSignature signature, boolean signedPreview, LastSeenMessages lastSeenMessages) {
		public static final CommandSigningContext.SignedArgument UNSIGNED = new CommandSigningContext.SignedArgument(
			MessageSignature.EMPTY, false, LastSeenMessages.EMPTY
		);
	}

	public static record SignedArguments(
		SignedMessageChain.Decoder decoder,
		MessageSigner argumentSigner,
		ArgumentSignatures argumentSignatures,
		boolean signedPreview,
		LastSeenMessages lastSeenMessages
	) implements CommandSigningContext {
		@Override
		public CommandSigningContext.SignedArgument getArgument(String string) {
			return new CommandSigningContext.SignedArgument(this.argumentSignatures.get(string), this.signedPreview, this.lastSeenMessages);
		}
	}
}
