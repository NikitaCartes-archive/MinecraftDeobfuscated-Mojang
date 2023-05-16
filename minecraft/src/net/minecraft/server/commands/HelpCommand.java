package net.minecraft.server.commands;

import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class HelpCommand {
	private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.help.failed"));

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("help")
				.executes(commandContext -> {
					Map<CommandNode<CommandSourceStack>, String> map = commandDispatcher.getSmartUsage(commandDispatcher.getRoot(), commandContext.getSource());

					for (String string : map.values()) {
						commandContext.getSource().sendSuccess(() -> Component.literal("/" + string), false);
					}

					return map.size();
				})
				.then(
					Commands.argument("command", StringArgumentType.greedyString())
						.executes(
							commandContext -> {
								ParseResults<CommandSourceStack> parseResults = commandDispatcher.parse(
									StringArgumentType.getString(commandContext, "command"), commandContext.getSource()
								);
								if (parseResults.getContext().getNodes().isEmpty()) {
									throw ERROR_FAILED.create();
								} else {
									Map<CommandNode<CommandSourceStack>, String> map = commandDispatcher.getSmartUsage(
										Iterables.<ParsedCommandNode<CommandSourceStack>>getLast(parseResults.getContext().getNodes()).getNode(), commandContext.getSource()
									);

									for (String string : map.values()) {
										commandContext.getSource().sendSuccess(() -> Component.literal("/" + parseResults.getReader().getString() + " " + string), false);
									}

									return map.size();
								}
							}
						)
				)
		);
	}
}
