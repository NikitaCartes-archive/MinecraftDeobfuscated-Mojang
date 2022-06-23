package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
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
			chatMessage.resolve(commandSourceStack)
				.thenAcceptAsync(
					filteredText -> {
						Component component = ((PlayerChatMessage)filteredText.raw()).serverContent();

						for (ServerPlayer serverPlayer : collection) {
							commandSourceStack.sendSuccess(
								Component.translatable("commands.message.display.outgoing", serverPlayer.getDisplayName(), component)
									.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
								false
							);
							PlayerChatMessage playerChatMessage = (PlayerChatMessage)filteredText.filter(commandSourceStack, serverPlayer);
							if (playerChatMessage != null) {
								serverPlayer.sendChatMessage(playerChatMessage, commandSourceStack.asChatSender(), ChatType.MSG_COMMAND);
							}
						}
					},
					commandSourceStack.getServer()
				);
			return collection.size();
		}
	}
}
