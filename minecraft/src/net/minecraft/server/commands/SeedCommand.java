package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;

public class SeedCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, boolean bl) {
		commandDispatcher.register(Commands.literal("seed").requires(commandSourceStack -> !bl || commandSourceStack.hasPermission(2)).executes(commandContext -> {
			long l = commandContext.getSource().getLevel().getSeed();
			Component component = ComponentUtils.copyOnClickText(String.valueOf(l));
			commandContext.getSource().sendSuccess(() -> Component.translatable("commands.seed.success", component), false);
			return (int)l;
		}));
	}
}
