package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import java.io.IOException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.profiling.jfr.JfrRecording;
import net.minecraft.util.profiling.jfr.parse.JfrStatsParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JfrCommand {
	private static final Logger LOGGER = LogManager.getLogger();

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("jfr")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(4))
				.then(Commands.literal("start").executes(commandContext -> startJfr(commandContext.getSource())))
				.then(Commands.literal("stop").executes(commandContext -> stopJfr(commandContext.getSource())))
		);
	}

	private static int startJfr(CommandSourceStack commandSourceStack) {
		JfrRecording.Environment environment = JfrRecording.Environment.from(commandSourceStack.getServer());
		if (JfrRecording.start(environment)) {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.jfr.started"), false);
			return 0;
		} else {
			commandSourceStack.sendFailure(new TranslatableComponent("commands.jfr.start.failed"));
			return 1;
		}
	}

	private static int stopJfr(CommandSourceStack commandSourceStack) {
		return JfrRecording.stop().<Integer>map(path -> {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.jfr.stopped", path.toString()), false);

			try {
				String string = JfrStatsParser.parse(path).asText();
				LOGGER.info(string);
			} catch (IOException var3) {
				LOGGER.warn("Failed to collect stats", (Throwable)var3);
			}

			return 0;
		}, illegalStateException -> {
			commandSourceStack.sendFailure(new TranslatableComponent("commands.jfr.dump.failed", illegalStateException.getMessage()));
			return 1;
		});
	}
}
