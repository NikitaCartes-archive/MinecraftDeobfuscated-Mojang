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
import net.minecraft.server.network.TextFilter;
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
									if (entity instanceof ServerPlayer) {
										TextFilter textFilter = ((ServerPlayer)entity).getTextFilter();
										if (textFilter != null) {
											textFilter.processStreamMessage(string)
												.thenAcceptAsync(
													optional -> optional.ifPresent(
															stringx -> minecraftServer.getPlayerList().broadcastMessage(createMessage(commandContext, stringx), ChatType.CHAT, entity.getUUID())
														),
													minecraftServer
												);
											return 1;
										}
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
