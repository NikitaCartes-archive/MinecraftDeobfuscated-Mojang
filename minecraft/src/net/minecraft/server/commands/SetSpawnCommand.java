package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.AngleArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class SetSpawnCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("spawnpoint")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.executes(
					commandContext -> setSpawn(
							commandContext.getSource(),
							Collections.singleton(commandContext.getSource().getPlayerOrException()),
							new BlockPos(commandContext.getSource().getPosition()),
							0.0F
						)
				)
				.then(
					Commands.argument("targets", EntityArgument.players())
						.executes(
							commandContext -> setSpawn(
									commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), new BlockPos(commandContext.getSource().getPosition()), 0.0F
								)
						)
						.then(
							Commands.argument("pos", BlockPosArgument.blockPos())
								.executes(
									commandContext -> setSpawn(
											commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), BlockPosArgument.getSpawnablePos(commandContext, "pos"), 0.0F
										)
								)
								.then(
									Commands.argument("angle", AngleArgument.angle())
										.executes(
											commandContext -> setSpawn(
													commandContext.getSource(),
													EntityArgument.getPlayers(commandContext, "targets"),
													BlockPosArgument.getSpawnablePos(commandContext, "pos"),
													AngleArgument.getAngle(commandContext, "angle")
												)
										)
								)
						)
				)
		);
	}

	private static int setSpawn(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, BlockPos blockPos, float f) {
		ResourceKey<Level> resourceKey = commandSourceStack.getLevel().dimension();

		for (ServerPlayer serverPlayer : collection) {
			serverPlayer.setRespawnPosition(resourceKey, blockPos, f, true, false);
		}

		String string = resourceKey.location().toString();
		if (collection.size() == 1) {
			commandSourceStack.sendSuccess(
				new TranslatableComponent(
					"commands.spawnpoint.success.single",
					blockPos.getX(),
					blockPos.getY(),
					blockPos.getZ(),
					f,
					string,
					((ServerPlayer)collection.iterator().next()).getDisplayName()
				),
				true
			);
		} else {
			commandSourceStack.sendSuccess(
				new TranslatableComponent("commands.spawnpoint.success.multiple", blockPos.getX(), blockPos.getY(), blockPos.getZ(), f, string, collection.size()), true
			);
		}

		return collection.size();
	}
}
