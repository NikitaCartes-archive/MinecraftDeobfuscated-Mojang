package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class KickCommand {
	private static final SimpleCommandExceptionType ERROR_KICKING_OWNER = new SimpleCommandExceptionType(Component.translatable("commands.kick.owner.failed"));

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("kick")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(3))
				.then(
					Commands.argument("targets", EntityArgument.players())
						.executes(
							commandContext -> kickPlayers(
									commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), Component.translatable("multiplayer.disconnect.kicked")
								)
						)
						.then(
							Commands.argument("reason", MessageArgument.message())
								.executes(
									commandContext -> kickPlayers(
											commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), MessageArgument.getMessage(commandContext, "reason")
										)
								)
						)
				)
		);
	}

	private static int kickPlayers(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, Component component) throws CommandSyntaxException {
		int i = 0;

		for (ServerPlayer serverPlayer : collection) {
			if (!commandSourceStack.getServer().isSingleplayerOwner(serverPlayer.getGameProfile())) {
				serverPlayer.connection.disconnect(component);
				commandSourceStack.sendSuccess(() -> Component.translatable("commands.kick.success", serverPlayer.getDisplayName(), component), true);
				i++;
			}
		}

		if (i == 0) {
			throw ERROR_KICKING_OWNER.create();
		} else {
			return i;
		}
	}
}
