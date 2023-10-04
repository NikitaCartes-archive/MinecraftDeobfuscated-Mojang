package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import java.util.List;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;

public class ListPlayersCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("list")
				.executes(commandContext -> listPlayers(commandContext.getSource()))
				.then(Commands.literal("uuids").executes(commandContext -> listPlayersWithUuids(commandContext.getSource())))
		);
	}

	private static int listPlayers(CommandSourceStack commandSourceStack) {
		return format(commandSourceStack, Player::getDisplayName);
	}

	private static int listPlayersWithUuids(CommandSourceStack commandSourceStack) {
		return format(
			commandSourceStack,
			serverPlayer -> Component.translatable("commands.list.nameAndId", serverPlayer.getName(), Component.translationArg(serverPlayer.getGameProfile().getId()))
		);
	}

	private static int format(CommandSourceStack commandSourceStack, Function<ServerPlayer, Component> function) {
		PlayerList playerList = commandSourceStack.getServer().getPlayerList();
		List<ServerPlayer> list = playerList.getPlayers();
		Component component = ComponentUtils.formatList(list, function);
		commandSourceStack.sendSuccess(() -> Component.translatable("commands.list.players", list.size(), playerList.getMaxPlayers(), component), false);
		return list.size();
	}
}
