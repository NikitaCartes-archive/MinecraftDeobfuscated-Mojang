package net.minecraft.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.players.PlayerList;

public class DeOpCommands {
	private static final SimpleCommandExceptionType ERROR_NOT_OP = new SimpleCommandExceptionType(new TranslatableComponent("commands.deop.failed"));

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("deop")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(3))
				.then(
					Commands.argument("targets", GameProfileArgument.gameProfile())
						.suggests(
							(commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(
									commandContext.getSource().getServer().getPlayerList().getOpNames(), suggestionsBuilder
								)
						)
						.executes(commandContext -> deopPlayers(commandContext.getSource(), GameProfileArgument.getGameProfiles(commandContext, "targets")))
				)
		);
	}

	private static int deopPlayers(CommandSourceStack commandSourceStack, Collection<GameProfile> collection) throws CommandSyntaxException {
		PlayerList playerList = commandSourceStack.getServer().getPlayerList();
		int i = 0;

		for (GameProfile gameProfile : collection) {
			if (playerList.isOp(gameProfile)) {
				playerList.deop(gameProfile);
				i++;
				commandSourceStack.sendSuccess(new TranslatableComponent("commands.deop.success", ((GameProfile)collection.iterator().next()).getName()), true);
			}
		}

		if (i == 0) {
			throw ERROR_NOT_OP.create();
		} else {
			commandSourceStack.getServer().kickUnlistedPlayers(commandSourceStack);
			return i;
		}
	}
}
