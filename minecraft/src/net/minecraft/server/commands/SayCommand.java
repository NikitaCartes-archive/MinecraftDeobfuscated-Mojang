package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.SignedMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

public class SayCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("say")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.argument("message", MessageArgument.message())
						.executes(
							commandContext -> {
								SignedMessage signedMessage = MessageArgument.getSignedMessage(commandContext, "message");
								CommandSourceStack commandSourceStack = commandContext.getSource();
								PlayerList playerList = commandSourceStack.getServer().getPlayerList();
								if (commandSourceStack.isPlayer()) {
									ServerPlayer serverPlayer = commandSourceStack.getPlayerOrException();
									serverPlayer.getTextFilter()
										.processStreamMessage(signedMessage.content().getString())
										.thenAcceptAsync(
											filteredText -> playerList.broadcastChatMessage(signedMessage, filteredText, serverPlayer, ChatType.SAY_COMMAND), commandSourceStack.getServer()
										);
								} else {
									playerList.broadcastChatMessage(signedMessage, commandSourceStack.asChatSender(), ChatType.SAY_COMMAND);
								}

								return 1;
							}
						)
				)
		);
	}
}
