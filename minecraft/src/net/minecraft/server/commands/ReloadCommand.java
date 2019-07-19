package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;

public class ReloadCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(Commands.literal("reload").requires(commandSourceStack -> commandSourceStack.hasPermission(2)).executes(commandContext -> {
			commandContext.getSource().sendSuccess(new TranslatableComponent("commands.reload.success"), true);
			commandContext.getSource().getServer().reloadResources();
			return 0;
		}));
	}
}
