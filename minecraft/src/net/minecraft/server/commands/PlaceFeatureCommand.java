package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class PlaceFeatureCommand {
	private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.placefeature.failed"));

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("placefeature")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.argument("feature", ResourceLocationArgument.id())
						.suggests(SuggestionProviders.AVAILABLE_FEATURES)
						.executes(
							commandContext -> placeFeature(
									commandContext.getSource(),
									ResourceLocationArgument.getConfiguredFeature(commandContext, "feature"),
									new BlockPos(commandContext.getSource().getPosition())
								)
						)
						.then(
							Commands.argument("pos", BlockPosArgument.blockPos())
								.executes(
									commandContext -> placeFeature(
											commandContext.getSource(),
											ResourceLocationArgument.getConfiguredFeature(commandContext, "feature"),
											BlockPosArgument.getLoadedBlockPos(commandContext, "pos")
										)
								)
						)
				)
		);
	}

	public static int placeFeature(
		CommandSourceStack commandSourceStack, ResourceLocationArgument.LocatedResource<ConfiguredFeature<?, ?>> locatedResource, BlockPos blockPos
	) throws CommandSyntaxException {
		ServerLevel serverLevel = commandSourceStack.getLevel();
		ConfiguredFeature<?, ?> configuredFeature = locatedResource.resource();
		if (!configuredFeature.place(serverLevel, serverLevel.getChunkSource().getGenerator(), serverLevel.getRandom(), blockPos)) {
			throw ERROR_FAILED.create();
		} else {
			ResourceLocation resourceLocation = locatedResource.id();
			commandSourceStack.sendSuccess(
				new TranslatableComponent("commands.placefeature.success", resourceLocation, blockPos.getX(), blockPos.getY(), blockPos.getZ()), true
			);
			return 1;
		}
	}
}
