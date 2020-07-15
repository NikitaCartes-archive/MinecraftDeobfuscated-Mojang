package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.AngleArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;

public class SetWorldSpawnCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("setworldspawn")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.executes(commandContext -> setSpawn(commandContext.getSource(), new BlockPos(commandContext.getSource().getPosition()), 0.0F))
				.then(
					Commands.argument("pos", BlockPosArgument.blockPos())
						.executes(commandContext -> setSpawn(commandContext.getSource(), BlockPosArgument.getOrLoadBlockPos(commandContext, "pos"), 0.0F))
						.then(
							Commands.argument("angle", AngleArgument.angle())
								.executes(
									commandContext -> setSpawn(
											commandContext.getSource(), BlockPosArgument.getOrLoadBlockPos(commandContext, "pos"), AngleArgument.getAngle(commandContext, "angle")
										)
								)
						)
				)
		);
	}

	private static int setSpawn(CommandSourceStack commandSourceStack, BlockPos blockPos, float f) {
		commandSourceStack.getLevel().setDefaultSpawnPos(blockPos, f);
		commandSourceStack.sendSuccess(new TranslatableComponent("commands.setworldspawn.success", blockPos.getX(), blockPos.getY(), blockPos.getZ(), f), true);
		return 1;
	}
}
