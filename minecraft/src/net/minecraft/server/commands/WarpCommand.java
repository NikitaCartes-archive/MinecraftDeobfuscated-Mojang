package net.minecraft.server.commands;

import com.google.common.collect.ImmutableSet;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Comparator;
import java.util.stream.IntStream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimHash;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;

public class WarpCommand {
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("warp")
				.then(
					Commands.argument("target", StringArgumentType.greedyString())
						.executes(commandContext -> wrap(commandContext.getSource(), StringArgumentType.getString(commandContext, "target")))
				)
		);
	}

	private static int wrap(CommandSourceStack commandSourceStack, String string) throws CommandSyntaxException {
		DimensionType dimensionType = Registry.DIMENSION_TYPE.byId(DimHash.getHash(string));
		ServerLevel serverLevel = commandSourceStack.getServer().getLevel(dimensionType);
		LevelChunk levelChunk = serverLevel.getChunk(0, 0);
		BlockPos blockPos = (BlockPos)IntStream.range(0, 15).boxed().flatMap(integer -> IntStream.range(0, 15).mapToObj(i -> {
				int j = levelChunk.getHeight(Heightmap.Types.MOTION_BLOCKING, integer, i);
				return new BlockPos(integer, j, i);
			})).filter(blockPosx -> blockPosx.getY() > 0).max(Comparator.comparing(Vec3i::getY)).orElse(new BlockPos(0, 256, 0));
		TeleportCommand.performTeleport(
			commandSourceStack,
			commandSourceStack.getEntityOrException(),
			serverLevel,
			(double)blockPos.getX(),
			(double)(blockPos.getY() + 1),
			(double)blockPos.getZ(),
			ImmutableSet.of(),
			0.0F,
			0.0F,
			null
		);
		return 0;
	}
}
