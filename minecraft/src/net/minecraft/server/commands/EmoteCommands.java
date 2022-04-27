package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class EmoteCommands {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("me")
				.then(
					Commands.argument("action", StringArgumentType.greedyString())
						.executes(
							commandContext -> {
								String string = StringArgumentType.getString(commandContext, "action");
								Entity entity = commandContext.getSource().getEntity();
								MinecraftServer minecraftServer = commandContext.getSource().getServer();
								if (entity instanceof ServerPlayer serverPlayer) {
									serverPlayer.getTextFilter()
										.processStreamMessage(string)
										.thenAcceptAsync(
											filteredText -> {
												String stringx = filteredText.getFiltered();
												Component component = stringx.isEmpty() ? null : createMessage(commandContext, stringx);
												Component component2 = createMessage(commandContext, filteredText.getRaw());
												minecraftServer.getPlayerList()
													.broadcastSystemMessage(component2, serverPlayer2 -> serverPlayer.shouldFilterMessageTo(serverPlayer2) ? component : component2, ChatType.SYSTEM);
											},
											minecraftServer
										);
									return 1;
								} else {
									minecraftServer.getPlayerList().broadcastSystemMessage(createMessage(commandContext, string), ChatType.SYSTEM);
									return 1;
								}
							}
						)
				)
		);
	}

	private static Component createMessage(CommandContext<CommandSourceStack> commandContext, String string) {
		return Component.translatable("chat.type.emote", commandContext.getSource().getDisplayName(), string);
	}
}
