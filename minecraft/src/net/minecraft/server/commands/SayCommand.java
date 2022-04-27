package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class SayCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("say")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(Commands.argument("message", MessageArgument.message()).executes(commandContext -> {
					Component component = MessageArgument.getMessage(commandContext, "message");
					Component component2 = Component.translatable("chat.type.announcement", commandContext.getSource().getDisplayName(), component);
					Entity entity = commandContext.getSource().getEntity();
					if (entity != null) {
						commandContext.getSource().getServer().getPlayerList().broadcastUnsignedMessage(component2, ChatType.SYSTEM, entity.getUUID());
					} else {
						commandContext.getSource().getServer().getPlayerList().broadcastSystemMessage(component2, serverPlayer -> component2, ChatType.SYSTEM);
					}

					return 1;
				}))
		);
	}
}
