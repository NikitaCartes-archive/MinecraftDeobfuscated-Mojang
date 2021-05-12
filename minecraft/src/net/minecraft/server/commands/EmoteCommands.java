package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
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
								if (entity != null) {
									if (entity instanceof ServerPlayer serverPlayer) {
										serverPlayer.getTextFilter()
											.processStreamMessage(string)
											.thenAcceptAsync(
												filteredText -> {
													String stringx = filteredText.getFiltered();
													Component component = stringx.isEmpty() ? null : createMessage(commandContext, stringx);
													Component component2 = createMessage(commandContext, filteredText.getRaw());
													minecraftServer.getPlayerList()
														.broadcastMessage(
															component2, serverPlayer2 -> serverPlayer.shouldFilterMessageTo(serverPlayer2) ? component : component2, ChatType.CHAT, entity.getUUID()
														);
												},
												minecraftServer
											);
										return 1;
									}

									minecraftServer.getPlayerList().broadcastMessage(createMessage(commandContext, string), ChatType.CHAT, entity.getUUID());
								} else {
									minecraftServer.getPlayerList().broadcastMessage(createMessage(commandContext, string), ChatType.SYSTEM, Util.NIL_UUID);
								}

								return 1;
							}
						)
				)
		);
	}

	private static Component createMessage(CommandContext<CommandSourceStack> commandContext, String string) {
		return new TranslatableComponent("chat.type.emote", commandContext.getSource().getDisplayName(), string);
	}
}
