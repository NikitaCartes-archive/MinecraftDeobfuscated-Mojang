package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public class LocateBiomeCommand {
	public static final DynamicCommandExceptionType ERROR_INVALID_BIOME = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("commands.locatebiome.invalid", object)
	);
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
						.executes(commandContext -> locateBiome(commandContext.getSource(), commandContext.getArgument("biome", ResourceLocation.class)))
				)
		);
	}

	private static int locateBiome(CommandSourceStack commandSourceStack, ResourceLocation resourceLocation) throws CommandSyntaxException {
		Biome biome = (Biome)commandSourceStack.getServer()
			.registryAccess()
			.registryOrThrow(Registry.BIOME_REGISTRY)
			.getOptional(resourceLocation)
			.orElseThrow(() -> ERROR_INVALID_BIOME.create(resourceLocation));
		BlockPos blockPos = new BlockPos(commandSourceStack.getPosition());
		BlockPos blockPos2 = commandSourceStack.getLevel().findNearestBiome(biome, blockPos, 6400, 8);
		String string = resourceLocation.toString();
		if (blockPos2 == null) {
			throw ERROR_BIOME_NOT_FOUND.create(string);
		} else {
			return LocateCommand.showLocateResult(commandSourceStack, string, blockPos, blockPos2, "commands.locatebiome.success");
		}
	}
}
