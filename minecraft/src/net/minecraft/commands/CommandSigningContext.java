package net.minecraft.commands;

import java.time.Instant;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.util.Crypt;

public interface CommandSigningContext {
	CommandSigningContext NONE = string -> MessageSignature.unsigned(Util.NIL_UUID);

	MessageSignature getArgumentSignature(String string);

	default boolean signedArgumentPreview(String string) {
		return false;
	}

	public static record SignedArguments(UUID sender, Instant timeStamp, ArgumentSignatures argumentSignatures, boolean signedPreview)
		implements CommandSigningContext {
		@Override
		public MessageSignature getArgumentSignature(String string) {
			Crypt.SaltSignaturePair saltSignaturePair = this.argumentSignatures.get(string);
			return saltSignaturePair != null ? new MessageSignature(this.sender, this.timeStamp, saltSignaturePair) : MessageSignature.unsigned(this.sender);
		}

		@Override
		public boolean signedArgumentPreview(String string) {
			return this.signedPreview;
		}
	}
}
