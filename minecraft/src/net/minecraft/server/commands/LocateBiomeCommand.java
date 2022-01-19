package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.biome.Biome;

public class LocateBiomeCommand {
	private static final DynamicCommandExceptionType ERROR_BIOME_NOT_FOUND = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("commands.locatebiome.notFound", object)
	);
	private static final int MAX_SEARCH_RADIUS = 6400;
	private static final int SEARCH_STEP = 8;

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("locatebiome")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.argument("biome", ResourceLocationArgument.id())
						.suggests(SuggestionProviders.AVAILABLE_BIOMES)
						.executes(commandContext -> locateBiome(commandContext.getSource(), ResourceLocationArgument.getBiome(commandContext, "biome")))
				)
		);
	}

	private static int locateBiome(CommandSourceStack commandSourceStack, ResourceLocationArgument.LocatedResource<Biome> locatedResource) throws CommandSyntaxException {
		Biome biome = locatedResource.resource();
		BlockPos blockPos = new BlockPos(commandSourceStack.getPosition());
		BlockPos blockPos2 = commandSourceStack.getLevel().findNearestBiome(biome, blockPos, 6400, 8);
		String string = locatedResource.id().toString();
		if (blockPos2 == null) {
			throw ERROR_BIOME_NOT_FOUND.create(string);
		} else {
			return LocateCommand.showLocateResult(commandSourceStack, string, blockPos, blockPos2, "commands.locatebiome.success");
		}
	}
}
