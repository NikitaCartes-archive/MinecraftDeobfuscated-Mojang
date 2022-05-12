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
											commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), MessageArgument.getSignedMessage(commandContext, "message")
										)
								)
						)
				)
		);
		commandDispatcher.register(Commands.literal("tell").redirect(literalCommandNode));
		commandDispatcher.register(Commands.literal("w").redirect(literalCommandNode));
	}

	private static int sendMessage(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, PlayerChatMessage playerChatMessage) {
		PlayerChatMessage playerChatMessage2 = commandSourceStack.getServer().getChatDecorator().decorate(commandSourceStack.getPlayer(), playerChatMessage);

		for (ServerPlayer serverPlayer : collection) {
			commandSourceStack.sendSuccess(
				Component.translatable("commands.message.display.outgoing", serverPlayer.getDisplayName(), playerChatMessage2.serverContent())
					.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
				false
			);
			serverPlayer.sendChatMessage(playerChatMessage2, commandSourceStack.asChatSender(), ChatType.MSG_COMMAND);
		}

		return collection.size();
	}
}
