package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

public class EmoteCommands {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(Commands.literal("me").then(Commands.argument("action", MessageArgument.message()).executes(commandContext -> {
			PlayerChatMessage playerChatMessage = MessageArgument.getSignedMessage(commandContext, "action");
			CommandSourceStack commandSourceStack = commandContext.getSource();
			if (commandSourceStack.isPlayer()) {
				ServerPlayer serverPlayer = commandSourceStack.getPlayerOrException();
				serverPlayer.getTextFilter().processStreamMessage(playerChatMessage.signedContent().getString()).thenAcceptAsync(filteredText -> {
					PlayerList playerList = commandSourceStack.getServer().getPlayerList();
					PlayerChatMessage playerChatMessage2 = commandSourceStack.getServer().getChatDecorator().decorate(serverPlayer, playerChatMessage);
					playerList.broadcastChatMessage(playerChatMessage2, filteredText, serverPlayer, ChatType.EMOTE_COMMAND);
				}, commandSourceStack.getServer());
			} else {
				commandSourceStack.getServer().getPlayerList().broadcastChatMessage(playerChatMessage, commandSourceStack.asChatSender(), ChatType.EMOTE_COMMAND);
			}

			return 1;
		})));
	}
}
