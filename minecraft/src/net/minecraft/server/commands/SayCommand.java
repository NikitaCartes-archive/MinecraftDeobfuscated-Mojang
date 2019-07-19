package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class SayCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("say")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.argument("message", MessageArgument.message())
						.executes(
							commandContext -> {
								Component component = MessageArgument.getMessage(commandContext, "message");
								commandContext.getSource()
									.getServer()
									.getPlayerList()
									.broadcastMessage(new TranslatableComponent("chat.type.announcement", commandContext.getSource().getDisplayName(), component));
								return 1;
							}
						)
				)
		);
	}
}
