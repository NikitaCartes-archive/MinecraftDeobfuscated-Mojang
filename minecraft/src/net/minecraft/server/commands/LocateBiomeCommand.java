package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Pair;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceOrTagLocationArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.biome.Biome;

public class LocateBiomeCommand {
	private static final DynamicCommandExceptionType ERROR_BIOME_NOT_FOUND = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("commands.locatebiome.notFound", object)
	);
	private static final int MAX_SEARCH_RADIUS = 6400;
	private static final int SAMPLE_RESOLUTION_HORIZONTAL = 32;
	private static final int SAMPLE_RESOLUTION_VERTICAL = 64;

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("locatebiome")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.argument("biome", ResourceOrTagLocationArgument.resourceOrTag(Registry.BIOME_REGISTRY))
						.executes(commandContext -> locateBiome(commandContext.getSource(), ResourceOrTagLocationArgument.getBiome(commandContext, "biome")))
				)
		);
	}

	private static int locateBiome(CommandSourceStack commandSourceStack, ResourceOrTagLocationArgument.Result<Biome> result) throws CommandSyntaxException {
		BlockPos blockPos = new BlockPos(commandSourceStack.getPosition());
		Pair<BlockPos, Holder<Biome>> pair = commandSourceStack.getLevel().findClosestBiome3d(result, blockPos, 6400, 32, 64);
		if (pair == null) {
			throw ERROR_BIOME_NOT_FOUND.create(result.asPrintable());
		} else {
			return LocateCommand.showLocateResult(commandSourceStack, result, blockPos, pair, "commands.locatebiome.success", true);
		}
	}
}
