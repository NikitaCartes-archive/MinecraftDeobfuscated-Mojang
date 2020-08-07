package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;

public class EmoteCommands {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("me")
				.then(
					Commands.argument("action", StringArgumentType.greedyString())
						.executes(
							commandContext -> {
								TranslatableComponent translatableComponent = new TranslatableComponent(
									"chat.type.emote", commandContext.getSource().getDisplayName(), StringArgumentType.getString(commandContext, "action")
								);
								Entity entity = commandContext.getSource().getEntity();
								if (entity != null) {
									commandContext.getSource().getServer().getPlayerList().broadcastMessage(translatableComponent, ChatType.CHAT, entity.getUUID());
								} else {
									commandContext.getSource().getServer().getPlayerList().broadcastMessage(translatableComponent, ChatType.SYSTEM, Util.NIL_UUID);
								}

								return 1;
							}
						)
				)
		);
	}
}
