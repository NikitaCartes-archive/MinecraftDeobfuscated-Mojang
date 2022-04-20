package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;

public class SpectateCommand {
	private static final SimpleCommandExceptionType ERROR_SELF = new SimpleCommandExceptionType(Component.translatable("commands.spectate.self"));
	private static final DynamicCommandExceptionType ERROR_NOT_SPECTATOR = new DynamicCommandExceptionType(
		object -> Component.translatable("commands.spectate.not_spectator", object)
	);

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("spectate")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.executes(commandContext -> spectate(commandContext.getSource(), null, commandContext.getSource().getPlayerOrException()))
				.then(
					Commands.argument("target", EntityArgument.entity())
						.executes(
							commandContext -> spectate(
									commandContext.getSource(), EntityArgument.getEntity(commandContext, "target"), commandContext.getSource().getPlayerOrException()
								)
						)
						.then(
							Commands.argument("player", EntityArgument.player())
								.executes(
									commandContext -> spectate(
											commandContext.getSource(), EntityArgument.getEntity(commandContext, "target"), EntityArgument.getPlayer(commandContext, "player")
										)
								)
						)
				)
		);
	}

	private static int spectate(CommandSourceStack commandSourceStack, @Nullable Entity entity, ServerPlayer serverPlayer) throws CommandSyntaxException {
		if (serverPlayer == entity) {
			throw ERROR_SELF.create();
		} else if (serverPlayer.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
			throw ERROR_NOT_SPECTATOR.create(serverPlayer.getDisplayName());
		} else {
			serverPlayer.setCamera(entity);
			if (entity != null) {
				commandSourceStack.sendSuccess(Component.translatable("commands.spectate.success.started", entity.getDisplayName()), false);
			} else {
				commandSourceStack.sendSuccess(Component.translatable("commands.spectate.success.stopped"), false);
			}

			return 1;
		}
	}
}
