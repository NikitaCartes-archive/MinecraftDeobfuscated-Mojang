package net.minecraft.server.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;

public class KillCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("kill")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.executes(commandContext -> kill(commandContext.getSource(), ImmutableList.of(commandContext.getSource().getEntityOrException())))
				.then(
					Commands.argument("targets", EntityArgument.entities())
						.executes(commandContext -> kill(commandContext.getSource(), EntityArgument.getEntities(commandContext, "targets")))
				)
		);
	}

	private static int kill(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection) {
		for (Entity entity : collection) {
			entity.kill();
		}

		if (collection.size() == 1) {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.kill.success.single", ((Entity)collection.iterator().next()).getDisplayName()), true);
		} else {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.kill.success.multiple", collection.size()), true);
		}

		return collection.size();
	}
}
