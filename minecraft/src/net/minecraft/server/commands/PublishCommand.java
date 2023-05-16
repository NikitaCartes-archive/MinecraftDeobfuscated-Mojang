package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameModeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;

public class PublishCommand {
	private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.publish.failed"));
	private static final DynamicCommandExceptionType ERROR_ALREADY_PUBLISHED = new DynamicCommandExceptionType(
		object -> Component.translatable("commands.publish.alreadyPublished", object)
	);

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("publish")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(4))
				.executes(commandContext -> publish(commandContext.getSource(), HttpUtil.getAvailablePort(), false, null))
				.then(
					Commands.argument("allowCommands", BoolArgumentType.bool())
						.executes(
							commandContext -> publish(commandContext.getSource(), HttpUtil.getAvailablePort(), BoolArgumentType.getBool(commandContext, "allowCommands"), null)
						)
						.then(
							Commands.argument("gamemode", GameModeArgument.gameMode())
								.executes(
									commandContext -> publish(
											commandContext.getSource(),
											HttpUtil.getAvailablePort(),
											BoolArgumentType.getBool(commandContext, "allowCommands"),
											GameModeArgument.getGameMode(commandContext, "gamemode")
										)
								)
								.then(
									Commands.argument("port", IntegerArgumentType.integer(0, 65535))
										.executes(
											commandContext -> publish(
													commandContext.getSource(),
													IntegerArgumentType.getInteger(commandContext, "port"),
													BoolArgumentType.getBool(commandContext, "allowCommands"),
													GameModeArgument.getGameMode(commandContext, "gamemode")
												)
										)
								)
						)
				)
		);
	}

	private static int publish(CommandSourceStack commandSourceStack, int i, boolean bl, @Nullable GameType gameType) throws CommandSyntaxException {
		if (commandSourceStack.getServer().isPublished()) {
			throw ERROR_ALREADY_PUBLISHED.create(commandSourceStack.getServer().getPort());
		} else if (!commandSourceStack.getServer().publishServer(gameType, bl, i)) {
			throw ERROR_FAILED.create();
		} else {
			commandSourceStack.sendSuccess(() -> getSuccessMessage(i), true);
			return i;
		}
	}

	public static MutableComponent getSuccessMessage(int i) {
		Component component = ComponentUtils.copyOnClickText(String.valueOf(i));
		return Component.translatable("commands.publish.started", component);
	}
}
