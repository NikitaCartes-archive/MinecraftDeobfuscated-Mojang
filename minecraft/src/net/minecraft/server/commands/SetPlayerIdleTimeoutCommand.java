package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;

public class SetPlayerIdleTimeoutCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("setidletimeout")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(3))
				.then(
					Commands.argument("minutes", IntegerArgumentType.integer(0))
						.executes(commandContext -> setIdleTimeout(commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "minutes")))
				)
		);
	}

	private static int setIdleTimeout(CommandSourceStack commandSourceStack, int i) {
		commandSourceStack.getServer().setPlayerIdleTimeout(i);
		commandSourceStack.sendSuccess(new TranslatableComponent("commands.setidletimeout.success", i), true);
		return i;
	}
}
