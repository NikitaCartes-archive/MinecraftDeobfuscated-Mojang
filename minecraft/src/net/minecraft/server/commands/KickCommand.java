package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

public class KickCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("kick")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(3))
				.then(
					Commands.argument("targets", EntityArgument.players())
						.executes(
							commandContext -> kickPlayers(
									commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), new TranslatableComponent("multiplayer.disconnect.kicked")
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

	private static int kickPlayers(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, Component component) {
		for (ServerPlayer serverPlayer : collection) {
			serverPlayer.connection.disconnect(component);
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.kick.success", serverPlayer.getDisplayName(), component), true);
		}

		return collection.size();
	}
}
