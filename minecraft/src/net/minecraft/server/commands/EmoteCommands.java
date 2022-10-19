package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.server.players.PlayerList;

public class EmoteCommands {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(Commands.literal("me").then(Commands.argument("action", MessageArgument.message()).executes(commandContext -> {
			MessageArgument.resolveChatMessage(commandContext, "action", playerChatMessage -> {
				CommandSourceStack commandSourceStack = commandContext.getSource();
				PlayerList playerList = commandSourceStack.getServer().getPlayerList();
				playerList.broadcastChatMessage(playerChatMessage, commandSourceStack, ChatType.bind(ChatType.EMOTE_COMMAND, commandSourceStack));
			});
			return 1;
		})));
	}
}
