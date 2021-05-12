package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
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
											commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), MessageArgument.getMessage(commandContext, "message")
										)
								)
						)
				)
		);
		commandDispatcher.register(Commands.literal("tell").redirect(literalCommandNode));
		commandDispatcher.register(Commands.literal("w").redirect(literalCommandNode));
	}

	private static int sendMessage(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, Component component) {
		UUID uUID = commandSourceStack.getEntity() == null ? Util.NIL_UUID : commandSourceStack.getEntity().getUUID();
		Consumer<Component> consumer;
		if (commandSourceStack.getEntity() instanceof ServerPlayer serverPlayer) {
			consumer = component2 -> serverPlayer.sendMessage(
					new TranslatableComponent("commands.message.display.outgoing", component2, component)
						.withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}),
					serverPlayer.getUUID()
				);
		} else {
			consumer = component2 -> commandSourceStack.sendSuccess(
					new TranslatableComponent("commands.message.display.outgoing", component2, component)
						.withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}),
					false
				);
		}

		for (ServerPlayer serverPlayer2 : collection) {
			consumer.accept(serverPlayer2.getDisplayName());
			serverPlayer2.sendMessage(
				new TranslatableComponent("commands.message.display.incoming", commandSourceStack.getDisplayName(), component)
					.withStyle(new ChatFormatting[]{ChatFormatting.GRAY, ChatFormatting.ITALIC}),
				uUID
			);
		}

		return collection.size();
	}
}
