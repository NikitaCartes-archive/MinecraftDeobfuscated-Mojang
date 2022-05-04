package net.minecraft.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.time.Instant;
import java.util.UUID;
import net.minecraft.commands.arguments.ArgumentSignatures;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessage;
import net.minecraft.util.Crypt;

public interface CommandSigningContext {
	CommandSigningContext NONE = (commandContext, string, component) -> new SignedMessage(component, MessageSignature.unsigned());

	SignedMessage signArgument(CommandContext<CommandSourceStack> commandContext, String string, Component component) throws CommandSyntaxException;

	public static record PlainArguments(UUID sender, Instant timeStamp, ArgumentSignatures argumentSignatures) implements CommandSigningContext {
		@Override
		public SignedMessage signArgument(CommandContext<CommandSourceStack> commandContext, String string, Component component) {
			Crypt.SaltSignaturePair saltSignaturePair = this.argumentSignatures.get(string);
			return saltSignaturePair != null
				? new SignedMessage(component, new MessageSignature(this.sender, this.timeStamp, saltSignaturePair))
				: new SignedMessage(component, MessageSignature.unsigned());
		}
	}
}
