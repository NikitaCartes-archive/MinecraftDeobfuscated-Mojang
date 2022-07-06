package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatSender;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;

public class MsgCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		LiteralCommandNode<CommandSourceStack> literalCommandNode = commandDispatcher.register(
			Commands.literal("msg")
				.then(
					Commands.argument("targets", EntityArgument.players())
						.then(
							Commands.argument("message", MessageArgument.message())
								.executes(
									commandContext -> sendMessage(
											commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), MessageArgument.getChatMessage(commandContext, "message")
										)
								)
						)
				)
		);
		commandDispatcher.register(Commands.literal("tell").redirect(literalCommandNode));
		commandDispatcher.register(Commands.literal("w").redirect(literalCommandNode));
	}

	private static int sendMessage(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, MessageArgument.ChatMessage chatMessage) {
		if (collection.isEmpty()) {
			return 0;
		} else {
			ChatSender chatSender = commandSourceStack.asChatSender();
			chatMessage.resolve(commandSourceStack).thenAcceptAsync(filteredText -> {
				for (ServerPlayer serverPlayer : collection) {
					ChatSender chatSender2 = chatSender.withTargetName(serverPlayer.getDisplayName());
					commandSourceStack.sendChatMessage(chatSender2, (PlayerChatMessage)filteredText.raw(), ChatType.MSG_COMMAND_OUTGOING);
					PlayerChatMessage playerChatMessage = (PlayerChatMessage)filteredText.filter(commandSourceStack, serverPlayer);
					if (playerChatMessage != null) {
						serverPlayer.sendChatMessage(playerChatMessage, chatSender, ChatType.MSG_COMMAND_INCOMING);
					}
				}
			}, commandSourceStack.getServer());
			return collection.size();
		}
	}
}
