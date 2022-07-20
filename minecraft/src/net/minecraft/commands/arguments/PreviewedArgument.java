package net.minecraft.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public interface PreviewedArgument<T> extends ArgumentType<T> {
	@Nullable
	default CompletableFuture<Component> resolvePreview(CommandSourceStack commandSourceStack, ParsedArgument<CommandSourceStack, ?> parsedArgument) throws CommandSyntaxException {
		return this.getValueType().isInstance(parsedArgument.getResult())
			? this.resolvePreview(commandSourceStack, (T)this.getValueType().cast(parsedArgument.getResult()))
			: null;
	}

	CompletableFuture<Component> resolvePreview(CommandSourceStack commandSourceStack, T object) throws CommandSyntaxException;

	Class<T> getValueType();
}
