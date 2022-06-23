package net.minecraft.commands.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public interface PreviewedArgument<T> extends ArgumentType<T> {
	@Nullable
	static CompletableFuture<Component> resolvePreviewed(
		ArgumentCommandNode<?, ?> argumentCommandNode, CommandContextBuilder<CommandSourceStack> commandContextBuilder
	) throws CommandSyntaxException {
		return argumentCommandNode.getType() instanceof PreviewedArgument<?> previewedArgument
			? previewedArgument.resolvePreview(commandContextBuilder, argumentCommandNode.getName())
			: null;
	}

	static boolean isPreviewed(CommandNode<?> commandNode) {
		if (commandNode instanceof ArgumentCommandNode<?, ?> argumentCommandNode && argumentCommandNode.getType() instanceof PreviewedArgument) {
			return true;
		}

		return false;
	}

	@Nullable
	default CompletableFuture<Component> resolvePreview(CommandContextBuilder<CommandSourceStack> commandContextBuilder, String string) throws CommandSyntaxException {
		ParsedArgument<CommandSourceStack, ?> parsedArgument = (ParsedArgument<CommandSourceStack, ?>)commandContextBuilder.getArguments().get(string);
		return parsedArgument != null && this.getValueType().isInstance(parsedArgument.getResult())
			? this.resolvePreview(commandContextBuilder.getSource(), (T)this.getValueType().cast(parsedArgument.getResult()))
			: null;
	}

	CompletableFuture<Component> resolvePreview(CommandSourceStack commandSourceStack, T object) throws CommandSyntaxException;

	Class<T> getValueType();
}
