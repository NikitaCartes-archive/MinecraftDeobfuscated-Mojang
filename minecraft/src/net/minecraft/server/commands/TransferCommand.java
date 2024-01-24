package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundTransferPacket;
import net.minecraft.server.level.ServerPlayer;

public class TransferCommand {
	private static final SimpleCommandExceptionType ERROR_NO_PLAYERS = new SimpleCommandExceptionType(Component.translatable("commands.transfer.error.no_players"));

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("transfer")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(3))
				.then(
					Commands.argument("hostname", StringArgumentType.string())
						.executes(
							commandContext -> transfer(
									commandContext.getSource(),
									StringArgumentType.getString(commandContext, "hostname"),
									25565,
									List.of(commandContext.getSource().getPlayerOrException())
								)
						)
						.then(
							Commands.argument("port", IntegerArgumentType.integer(1, 65535))
								.executes(
									commandContext -> transfer(
											commandContext.getSource(),
											StringArgumentType.getString(commandContext, "hostname"),
											IntegerArgumentType.getInteger(commandContext, "port"),
											List.of(commandContext.getSource().getPlayerOrException())
										)
								)
								.then(
									Commands.argument("players", EntityArgument.players())
										.executes(
											commandContext -> transfer(
													commandContext.getSource(),
													StringArgumentType.getString(commandContext, "hostname"),
													IntegerArgumentType.getInteger(commandContext, "port"),
													EntityArgument.getPlayers(commandContext, "players")
												)
										)
								)
						)
				)
		);
	}

	private static int transfer(CommandSourceStack commandSourceStack, String string, int i, Collection<ServerPlayer> collection) throws CommandSyntaxException {
		if (collection.isEmpty()) {
			throw ERROR_NO_PLAYERS.create();
		} else {
			for (ServerPlayer serverPlayer : collection) {
				serverPlayer.connection.send(new ClientboundTransferPacket(string, i));
			}

			if (collection.size() == 1) {
				commandSourceStack.sendSuccess(
					() -> Component.translatable("commands.transfer.success.single", ((ServerPlayer)collection.iterator().next()).getDisplayName(), string, i), true
				);
			} else {
				commandSourceStack.sendSuccess(() -> Component.translatable("commands.transfer.success.multiple", collection.size(), string, i), true);
			}

			return collection.size();
		}
	}
}
