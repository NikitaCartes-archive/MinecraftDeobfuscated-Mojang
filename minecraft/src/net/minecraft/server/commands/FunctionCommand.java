package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.item.FunctionArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.ServerFunctionManager;

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

		for (CommandFunction commandFunction : collection) {
			i += commandSourceStack.getServer().getFunctions().execute(commandFunction, commandSourceStack.withSuppressedOutput().withMaximumPermission(2));
		}

		if (collection.size() == 1) {
			commandSourceStack.sendSuccess(
				new TranslatableComponent("commands.function.success.single", i, ((CommandFunction)collection.iterator().next()).getId()), true
			);
		} else {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.function.success.multiple", i, collection.size()), true);
		}

		return i;
	}
}
