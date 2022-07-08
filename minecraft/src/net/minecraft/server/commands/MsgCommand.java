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
import net.minecraft.network.chat.OutgoingPlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;

public class MsgCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		LiteralCommandNode<CommandSourceStack> literalCommandNode = commandDispatcher.register(
			Commands.literal("msg")
				.then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("message", MessageArgument.message()).executes(commandContext -> {
					MessageArgument.ChatMessage chatMessage = MessageArgument.getChatMessage(commandContext, "message");

					try {
						return sendMessage(commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), chatMessage);
					} catch (Exception var3) {
						chatMessage.consume(commandContext.getSource());
						throw var3;
					}
				})))
		);
		commandDispatcher.register(Commands.literal("tell").redirect(literalCommandNode));
		commandDispatcher.register(Commands.literal("w").redirect(literalCommandNode));
	}

	private static int sendMessage(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, MessageArgument.ChatMessage chatMessage) {
		ChatSender chatSender = commandSourceStack.asChatSender();
		ChatType.Bound bound = ChatType.bind(ChatType.MSG_COMMAND_INCOMING, commandSourceStack);
		chatMessage.resolve(commandSourceStack).thenAcceptAsync(filteredText -> {
			FilteredText<OutgoingPlayerChatMessage> filteredText2 = OutgoingPlayerChatMessage.createFromFiltered(filteredText, chatSender);

			for (ServerPlayer serverPlayer : collection) {
				ChatType.Bound bound2 = ChatType.bind(ChatType.MSG_COMMAND_OUTGOING, commandSourceStack).withTargetName(serverPlayer.getDisplayName());
				commandSourceStack.sendChatMessage(filteredText2.raw(), bound2);
				OutgoingPlayerChatMessage outgoingPlayerChatMessage = filteredText2.filter(commandSourceStack, serverPlayer);
				if (outgoingPlayerChatMessage != null) {
					serverPlayer.sendChatMessage(outgoingPlayerChatMessage, bound);
				}
			}

			filteredText2.raw().sendHeadersToRemainingPlayers(commandSourceStack.getServer().getPlayerList());
		}, commandSourceStack.getServer());
		return collection.size();
	}
}
