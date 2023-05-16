package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameModeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public class DefaultGameModeCommands {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("defaultgamemode")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.argument("gamemode", GameModeArgument.gameMode())
						.executes(commandContext -> setMode(commandContext.getSource(), GameModeArgument.getGameMode(commandContext, "gamemode")))
				)
		);
	}

	private static int setMode(CommandSourceStack commandSourceStack, GameType gameType) {
		int i = 0;
		MinecraftServer minecraftServer = commandSourceStack.getServer();
		minecraftServer.setDefaultGameType(gameType);
		GameType gameType2 = minecraftServer.getForcedGameType();
		if (gameType2 != null) {
			for (ServerPlayer serverPlayer : minecraftServer.getPlayerList().getPlayers()) {
				if (serverPlayer.setGameMode(gameType2)) {
					i++;
				}
			}
		}

		commandSourceStack.sendSuccess(() -> Component.translatable("commands.defaultgamemode.success", gameType.getLongDisplayName()), true);
		return i;
	}
}
