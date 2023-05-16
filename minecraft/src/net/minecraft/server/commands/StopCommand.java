package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class StopCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(Commands.literal("stop").requires(commandSourceStack -> commandSourceStack.hasPermission(4)).executes(commandContext -> {
			commandContext.getSource().sendSuccess(() -> Component.translatable("commands.stop.stopping"), true);
			commandContext.getSource().getServer().halt(false);
			return 1;
		}));
	}
}
