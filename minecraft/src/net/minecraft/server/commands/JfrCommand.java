package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.nio.file.Path;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.profiling.jfr.JfrRecording;

public class JfrCommand {
	private static final SimpleCommandExceptionType START_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.jfr.start.failed"));
	private static final DynamicCommandExceptionType DUMP_FAILED = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("commands.jfr.dump.failed", object)
	);

	private JfrCommand() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("jfr")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(4))
				.then(Commands.literal("start").executes(commandContext -> startJfr(commandContext.getSource())))
				.then(Commands.literal("stop").executes(commandContext -> stopJfr(commandContext.getSource())))
		);
	}

	private static int startJfr(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
		JfrRecording.Environment environment = JfrRecording.Environment.from(commandSourceStack.getServer());
		if (!JfrRecording.start(environment)) {
			throw START_FAILED.create();
		} else {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.jfr.started"), false);
			return 1;
		}
	}

	private static int stopJfr(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
		try {
			Path path = JfrRecording.stop();
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.jfr.stopped", path), false);
			return 1;
		} catch (Throwable var2) {
			throw DUMP_FAILED.create(var2.getMessage());
		}
	}
}
