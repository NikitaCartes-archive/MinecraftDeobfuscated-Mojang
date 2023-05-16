package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.OptionalInt;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerFunctionManager;
import org.apache.commons.lang3.mutable.MutableObject;

public class FunctionCommand {
	public static final SuggestionProvider<CommandSourceStack> SUGGEST_FUNCTION = (commandContext, suggestionsBuilder) -> {
		ServerFunctionManager serverFunctionManager = commandContext.getSource().getServer().getFunctions();
		SharedSuggestionProvider.suggestResource(serverFunctionManager.getTagNames(), suggestionsBuilder, "#");
		return SharedSuggestionProvider.suggestResource(serverFunctionManager.getFunctionNames(), suggestionsBuilder);
	};

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("function")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.argument("name", FunctionArgument.functions())
						.suggests(SUGGEST_FUNCTION)
						.executes(commandContext -> runFunction(commandContext.getSource(), FunctionArgument.getFunctions(commandContext, "name")))
				)
		);
	}

	private static int runFunction(CommandSourceStack commandSourceStack, Collection<CommandFunction> collection) {
		int i = 0;
		boolean bl = false;

		for (CommandFunction commandFunction : collection) {
			MutableObject<OptionalInt> mutableObject = new MutableObject<>(OptionalInt.empty());
			int j = commandSourceStack.getServer()
				.getFunctions()
				.execute(
					commandFunction,
					commandSourceStack.withSuppressedOutput().withMaximumPermission(2).withReturnValueConsumer(ix -> mutableObject.setValue(OptionalInt.of(ix)))
				);
			OptionalInt optionalInt = mutableObject.getValue();
			i += optionalInt.orElse(j);
			bl |= optionalInt.isPresent();
		}

		int k = i;
		if (collection.size() == 1) {
			if (bl) {
				commandSourceStack.sendSuccess(
					() -> Component.translatable("commands.function.success.single.result", k, ((CommandFunction)collection.iterator().next()).getId()), true
				);
			} else {
				commandSourceStack.sendSuccess(
					() -> Component.translatable("commands.function.success.single", k, ((CommandFunction)collection.iterator().next()).getId()), true
				);
			}
		} else if (bl) {
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.function.success.multiple.result", collection.size()), true);
		} else {
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.function.success.multiple", k, collection.size()), true);
		}

		return i;
	}
}
