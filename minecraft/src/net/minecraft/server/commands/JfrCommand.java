package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.nio.file.Path;
import java.nio.file.Paths;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.util.profiling.jfr.JvmProfiler;

public class JfrCommand {
	private static final SimpleCommandExceptionType START_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.jfr.start.failed"));
	private static final DynamicCommandExceptionType DUMP_FAILED = new DynamicCommandExceptionType(
		object -> Component.translatable("commands.jfr.dump.failed", object)
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
			commandSourceStack.sendSuccess(Component.translatable("commands.jfr.started"), false);
			return 1;
		}
	}

	private static int stopJfr(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
		try {
			Path path = Paths.get(".").relativize(JvmProfiler.INSTANCE.stop().normalize());
			Path path2 = commandSourceStack.getServer().isPublished() && !SharedConstants.IS_RUNNING_IN_IDE ? path : path.toAbsolutePath();
			Component component = Component.literal(path.toString())
				.withStyle(ChatFormatting.UNDERLINE)
				.withStyle(
					style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, path2.toString()))
							.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.copy.click")))
				);
			commandSourceStack.sendSuccess(Component.translatable("commands.jfr.stopped", component), false);
			return 1;
		} catch (Throwable var4) {
			throw DUMP_FAILED.create(var4.getMessage());
		}
	}
}
