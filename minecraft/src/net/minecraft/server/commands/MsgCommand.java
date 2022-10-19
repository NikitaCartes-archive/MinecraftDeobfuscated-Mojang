package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

public class MsgCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		LiteralCommandNode<CommandSourceStack> literalCommandNode = commandDispatcher.register(
			Commands.literal("msg")
				.then(
					Commands.argument("targets", EntityArgument.players())
						.then(
							Commands.argument("message", MessageArgument.message())
								.executes(
									commandContext -> {
										Collection<ServerPlayer> collection = EntityArgument.getPlayers(commandContext, "targets");
										if (!collection.isEmpty()) {
											MessageArgument.resolveChatMessage(
												commandContext, "message", playerChatMessage -> sendMessage(commandContext.getSource(), collection, playerChatMessage)
											);
										}

										return collection.size();
									}
								)
						)
				)
		);
		commandDispatcher.register(Commands.literal("tell").redirect(literalCommandNode));
		commandDispatcher.register(Commands.literal("w").redirect(literalCommandNode));
	}

	private static void sendMessage(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, PlayerChatMessage playerChatMessage) {
		ChatType.Bound bound = ChatType.bind(ChatType.MSG_COMMAND_INCOMING, commandSourceStack);
		OutgoingChatMessage outgoingChatMessage = OutgoingChatMessage.create(playerChatMessage);
		boolean bl = false;

		for (ServerPlayer serverPlayer : collection) {
			ChatType.Bound bound2 = ChatType.bind(ChatType.MSG_COMMAND_OUTGOING, commandSourceStack).withTargetName(serverPlayer.getDisplayName());
			commandSourceStack.sendChatMessage(outgoingChatMessage, false, bound2);
			boolean bl2 = commandSourceStack.shouldFilterMessageTo(serverPlayer);
			serverPlayer.sendChatMessage(outgoingChatMessage, bl2, bound);
			bl |= bl2 && playerChatMessage.isFullyFiltered();
		}

		if (bl) {
			commandSourceStack.sendSystemMessage(PlayerList.CHAT_FILTERED_FULL);
		}
	}
}
