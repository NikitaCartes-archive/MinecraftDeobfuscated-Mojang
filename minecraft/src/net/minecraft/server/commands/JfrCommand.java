package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.io.File;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.util.profiling.jfr.JvmProfiler;

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
		Environment environment = Environment.from(commandSourceStack.getServer());
		if (!JvmProfiler.INSTANCE.start(environment)) {
			throw START_FAILED.create();
		} else {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.jfr.started"), false);
			return 1;
		}
	}

	private static int stopJfr(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
		try {
			File file = JvmProfiler.INSTANCE.stop().toFile();
			Component component = new TextComponent(file.getName())
				.withStyle(ChatFormatting.UNDERLINE)
				.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, file.getAbsolutePath())));
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.jfr.stopped", component), false);
			return 1;
		} catch (Throwable var3) {
			throw DUMP_FAILED.create(var3.getMessage());
		}
	}
}
