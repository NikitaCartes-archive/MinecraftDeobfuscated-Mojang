package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class PlaceFeatureCommand {
	private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.placefeature.failed"));

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("placefeature")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.argument("feature", ResourceKeyArgument.key(Registry.CONFIGURED_FEATURE_REGISTRY))
						.executes(
							commandContext -> placeFeature(
									commandContext.getSource(),
									ResourceKeyArgument.getConfiguredFeature(commandContext, "feature"),
									new BlockPos(commandContext.getSource().getPosition())
								)
						)
						.then(
							Commands.argument("pos", BlockPosArgument.blockPos())
								.executes(
									commandContext -> placeFeature(
											commandContext.getSource(),
											ResourceKeyArgument.getConfiguredFeature(commandContext, "feature"),
											BlockPosArgument.getLoadedBlockPos(commandContext, "pos")
										)
								)
						)
				)
		);
	}

	public static int placeFeature(CommandSourceStack commandSourceStack, Holder<ConfiguredFeature<?, ?>> holder, BlockPos blockPos) throws CommandSyntaxException {
		ServerLevel serverLevel = commandSourceStack.getLevel();
		ConfiguredFeature<?, ?> configuredFeature = holder.value();
		if (!configuredFeature.place(serverLevel, serverLevel.getChunkSource().getGenerator(), serverLevel.getRandom(), blockPos)) {
			throw ERROR_FAILED.create();
		} else {
			String string = (String)holder.unwrapKey().map(resourceKey -> resourceKey.location().toString()).orElse("[unregistered]");
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.placefeature.success", string, blockPos.getX(), blockPos.getY(), blockPos.getZ()), true);
			return 1;
		}
	}
}
