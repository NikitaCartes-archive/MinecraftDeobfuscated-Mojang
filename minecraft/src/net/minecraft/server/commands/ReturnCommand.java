package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ReturnCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("return")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.argument("value", IntegerArgumentType.integer())
						.executes(commandContext -> setReturn(commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "value")))
				)
				.then(
					Commands.literal("run")
						.redirect(commandDispatcher.getRoot(), commandContext -> commandContext.getSource().withCallback(ReturnCommand::writeResultToReturnValue))
				)
		);
	}

	private static int setReturn(CommandSourceStack commandSourceStack, int i) {
		commandSourceStack.getReturnValueConsumer().accept(i);
		return i;
	}

	private static int writeResultToReturnValue(CommandContext<CommandSourceStack> commandContext, boolean bl, int i) {
		int j = bl ? i : 0;
		setReturn(commandContext.getSource(), j);
		return j;
	}
}
