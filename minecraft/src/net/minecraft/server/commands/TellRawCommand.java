package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerPlayer;

public class TellRawCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("tellraw")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.argument("targets", EntityArgument.players())
						.then(
							Commands.argument("message", ComponentArgument.textComponent())
								.executes(
									commandContext -> {
										int i = 0;

										for (ServerPlayer serverPlayer : EntityArgument.getPlayers(commandContext, "targets")) {
											serverPlayer.sendMessage(
												ComponentUtils.updateForEntity(commandContext.getSource(), ComponentArgument.getComponent(commandContext, "message"), serverPlayer, 0),
												Util.NIL_UUID
											);
											i++;
										}

										return i;
									}
								)
						)
				)
		);
	}
}
