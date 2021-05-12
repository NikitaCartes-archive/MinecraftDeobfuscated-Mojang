package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;

public class DebugPathCommand {
	private static final SimpleCommandExceptionType ERROR_NOT_MOB = new SimpleCommandExceptionType(new TextComponent("Source is not a mob"));
	private static final SimpleCommandExceptionType ERROR_NO_PATH = new SimpleCommandExceptionType(new TextComponent("Path not found"));
	private static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType(new TextComponent("Target not reached"));

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("debugpath")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.argument("to", BlockPosArgument.blockPos())
						.executes(commandContext -> fillBlocks(commandContext.getSource(), BlockPosArgument.getLoadedBlockPos(commandContext, "to")))
				)
		);
	}

	private static int fillBlocks(CommandSourceStack commandSourceStack, BlockPos blockPos) throws CommandSyntaxException {
		if (!(commandSourceStack.getEntity() instanceof Mob mob)) {
			throw ERROR_NOT_MOB.create();
		} else {
			PathNavigation pathNavigation = new GroundPathNavigation(mob, commandSourceStack.getLevel());
			Path path = pathNavigation.createPath(blockPos, 0);
			DebugPackets.sendPathFindingPacket(commandSourceStack.getLevel(), mob, path, pathNavigation.getMaxDistanceToWaypoint());
			if (path == null) {
				throw ERROR_NO_PATH.create();
			} else if (!path.canReach()) {
				throw ERROR_NOT_COMPLETE.create();
			} else {
				commandSourceStack.sendSuccess(new TextComponent("Made path"), true);
				return 1;
			}
		}
	}
}
