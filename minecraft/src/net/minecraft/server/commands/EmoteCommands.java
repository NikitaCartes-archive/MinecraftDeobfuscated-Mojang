package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;

public class EmoteCommands {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("me")
				.then(
					Commands.argument("action", StringArgumentType.greedyString())
						.executes(
							commandContext -> {
								commandContext.getSource()
									.getServer()
									.getPlayerList()
									.broadcastMessage(
										new TranslatableComponent("chat.type.emote", commandContext.getSource().getDisplayName(), StringArgumentType.getString(commandContext, "action"))
									);
								return 1;
							}
						)
				)
		);
	}
}
