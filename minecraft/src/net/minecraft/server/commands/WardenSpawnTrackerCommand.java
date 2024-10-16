package net.minecraft.server.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.warden.WardenSpawnTracker;
import net.minecraft.world.entity.player.Player;

public class WardenSpawnTrackerCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("warden_spawn_tracker")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.literal("clear")
						.executes(commandContext -> resetTracker(commandContext.getSource(), ImmutableList.of(commandContext.getSource().getPlayerOrException())))
				)
				.then(
					Commands.literal("set")
						.then(
							Commands.argument("warning_level", IntegerArgumentType.integer(0, 4))
								.executes(
									commandContext -> setWarningLevel(
											commandContext.getSource(),
											ImmutableList.of(commandContext.getSource().getPlayerOrException()),
											IntegerArgumentType.getInteger(commandContext, "warning_level")
										)
								)
						)
				)
		);
	}

	private static int setWarningLevel(CommandSourceStack commandSourceStack, Collection<? extends Player> collection, int i) {
		for (Player player : collection) {
			player.getWardenSpawnTracker().ifPresent(wardenSpawnTracker -> wardenSpawnTracker.setWarningLevel(i));
		}

		if (collection.size() == 1) {
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.warden_spawn_tracker.set.success.single", ((Player)collection.iterator().next()).getDisplayName()), true
			);
		} else {
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.warden_spawn_tracker.set.success.multiple", collection.size()), true);
		}

		return collection.size();
	}

	private static int resetTracker(CommandSourceStack commandSourceStack, Collection<? extends Player> collection) {
		for (Player player : collection) {
			player.getWardenSpawnTracker().ifPresent(WardenSpawnTracker::reset);
		}

		if (collection.size() == 1) {
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.warden_spawn_tracker.clear.success.single", ((Player)collection.iterator().next()).getDisplayName()), true
			);
		} else {
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.warden_spawn_tracker.clear.success.multiple", collection.size()), true);
		}

		return collection.size();
	}
}
