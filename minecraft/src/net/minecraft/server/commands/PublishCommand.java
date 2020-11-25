package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.HttpUtil;

public class PublishCommand {
	private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.publish.failed"));
	private static final DynamicCommandExceptionType ERROR_ALREADY_PUBLISHED = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("commands.publish.alreadyPublished", object)
	);

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("publish")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(4))
				.executes(commandContext -> publish(commandContext.getSource(), HttpUtil.getAvailablePort()))
				.then(
					Commands.argument("port", IntegerArgumentType.integer(0, 65535))
						.executes(commandContext -> publish(commandContext.getSource(), IntegerArgumentType.getInteger(commandContext, "port")))
				)
		);
	}

	private static int publish(CommandSourceStack commandSourceStack, int i) throws CommandSyntaxException {
		if (commandSourceStack.getServer().isPublished()) {
			throw ERROR_ALREADY_PUBLISHED.create(commandSourceStack.getServer().getPort());
		} else if (!commandSourceStack.getServer().publishServer(null, false, i)) {
			throw ERROR_FAILED.create();
		} else {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.publish.success", i), true);
			return i;
		}
	}
}
