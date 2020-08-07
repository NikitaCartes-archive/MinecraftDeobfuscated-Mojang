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
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.players.UserBanList;

public class PardonCommand {
	private static final SimpleCommandExceptionType ERROR_NOT_BANNED = new SimpleCommandExceptionType(new TranslatableComponent("commands.pardon.failed"));

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("pardon")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(3))
				.then(
					Commands.argument("targets", GameProfileArgument.gameProfile())
						.suggests(
							(commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(
									commandContext.getSource().getServer().getPlayerList().getBans().getUserList(), suggestionsBuilder
								)
						)
						.executes(commandContext -> pardonPlayers(commandContext.getSource(), GameProfileArgument.getGameProfiles(commandContext, "targets")))
				)
		);
	}

	private static int pardonPlayers(CommandSourceStack commandSourceStack, Collection<GameProfile> collection) throws CommandSyntaxException {
		UserBanList userBanList = commandSourceStack.getServer().getPlayerList().getBans();
		int i = 0;

		for (GameProfile gameProfile : collection) {
			if (userBanList.isBanned(gameProfile)) {
				userBanList.remove(gameProfile);
				i++;
				commandSourceStack.sendSuccess(new TranslatableComponent("commands.pardon.success", ComponentUtils.getDisplayName(gameProfile)), true);
			}
		}

		if (i == 0) {
			throw ERROR_NOT_BANNED.create();
		} else {
			return i;
		}
	}
}
