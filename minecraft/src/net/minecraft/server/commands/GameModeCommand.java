package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameModeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;

public class GameModeCommand {
	public static final int PERMISSION_LEVEL = 2;

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("gamemode")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.argument("gamemode", GameModeArgument.gameMode())
						.executes(
							commandContext -> setMode(
									commandContext, Collections.singleton(commandContext.getSource().getPlayerOrException()), GameModeArgument.getGameMode(commandContext, "gamemode")
								)
						)
						.then(
							Commands.argument("target", EntityArgument.players())
								.executes(
									commandContext -> setMode(
											commandContext, EntityArgument.getPlayers(commandContext, "target"), GameModeArgument.getGameMode(commandContext, "gamemode")
										)
								)
						)
				)
		);
	}

	private static void logGamemodeChange(CommandSourceStack commandSourceStack, ServerPlayer serverPlayer, GameType gameType) {
		Component component = Component.translatable("gameMode." + gameType.getName());
		if (commandSourceStack.getEntity() == serverPlayer) {
			commandSourceStack.sendSuccess(Component.translatable("commands.gamemode.success.self", component), true);
		} else {
			if (commandSourceStack.getLevel().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
				serverPlayer.sendSystemMessage(Component.translatable("gameMode.changed", component));
			}

			commandSourceStack.sendSuccess(Component.translatable("commands.gamemode.success.other", serverPlayer.getDisplayName(), component), true);
		}
	}

	private static int setMode(CommandContext<CommandSourceStack> commandContext, Collection<ServerPlayer> collection, GameType gameType) {
		int i = 0;

		for (ServerPlayer serverPlayer : collection) {
			if (serverPlayer.setGameMode(gameType)) {
				logGamemodeChange(commandContext.getSource(), serverPlayer, gameType);
				i++;
			}
		}

		return i;
	}
}
