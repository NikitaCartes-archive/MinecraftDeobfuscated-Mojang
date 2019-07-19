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

public class OpCommand {
	private static final SimpleCommandExceptionType ERROR_ALREADY_OP = new SimpleCommandExceptionType(new TranslatableComponent("commands.op.failed"));

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("op")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(3))
				.then(
					Commands.argument("targets", GameProfileArgument.gameProfile())
						.suggests(
							(commandContext, suggestionsBuilder) -> {
								PlayerList playerList = commandContext.getSource().getServer().getPlayerList();
								return SharedSuggestionProvider.suggest(
									playerList.getPlayers()
										.stream()
										.filter(serverPlayer -> !playerList.isOp(serverPlayer.getGameProfile()))
										.map(serverPlayer -> serverPlayer.getGameProfile().getName()),
									suggestionsBuilder
								);
							}
						)
						.executes(commandContext -> opPlayers(commandContext.getSource(), GameProfileArgument.getGameProfiles(commandContext, "targets")))
				)
		);
	}

	private static int opPlayers(CommandSourceStack commandSourceStack, Collection<GameProfile> collection) throws CommandSyntaxException {
		PlayerList playerList = commandSourceStack.getServer().getPlayerList();
		int i = 0;

		for (GameProfile gameProfile : collection) {
			if (!playerList.isOp(gameProfile)) {
				playerList.op(gameProfile);
				i++;
				commandSourceStack.sendSuccess(new TranslatableComponent("commands.op.success", ((GameProfile)collection.iterator().next()).getName()), true);
			}
		}

		if (i == 0) {
			throw ERROR_ALREADY_OP.create();
		} else {
			return i;
		}
	}
}
