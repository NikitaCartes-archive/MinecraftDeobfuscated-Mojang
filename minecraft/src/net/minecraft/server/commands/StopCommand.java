package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;

public class StopCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(Commands.literal("stop").requires(commandSourceStack -> commandSourceStack.hasPermission(4)).executes(commandContext -> {
			commandContext.getSource().sendSuccess(new TranslatableComponent("commands.stop.stopping"), true);
			commandContext.getSource().getServer().halt(false);
			return 1;
		}));
	}
}
