package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.AngleArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class SetWorldSpawnCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("setworldspawn")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.executes(commandContext -> setSpawn(commandContext.getSource(), BlockPos.containing(commandContext.getSource().getPosition()), 0.0F))
				.then(
					Commands.argument("pos", BlockPosArgument.blockPos())
						.executes(commandContext -> setSpawn(commandContext.getSource(), BlockPosArgument.getSpawnablePos(commandContext, "pos"), 0.0F))
						.then(
							Commands.argument("angle", AngleArgument.angle())
								.executes(
									commandContext -> setSpawn(
											commandContext.getSource(), BlockPosArgument.getSpawnablePos(commandContext, "pos"), AngleArgument.getAngle(commandContext, "angle")
										)
								)
						)
				)
		);
	}

	private static int setSpawn(CommandSourceStack commandSourceStack, BlockPos blockPos, float f) {
		ServerLevel serverLevel = commandSourceStack.getLevel();
		if (serverLevel.dimension() != Level.OVERWORLD) {
			commandSourceStack.sendFailure(Component.translatable("commands.setworldspawn.failure.not_overworld"));
			return 0;
		} else {
			serverLevel.setDefaultSpawnPos(blockPos, f);
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.setworldspawn.success", blockPos.getX(), blockPos.getY(), blockPos.getZ(), f), true);
			return 1;
		}
	}
}
