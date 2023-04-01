package net.minecraft.world.entity.transform;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

public class TransformCommand {
	private static final SimpleCommandExceptionType ERROR_NOT_LIVING = new SimpleCommandExceptionType(Component.literal("Target is not a living entity"));
	private static final SimpleCommandExceptionType ERROR_MULTIPLE_SKINS = new SimpleCommandExceptionType(
		Component.literal("Expected only one player for target skin")
	);

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
		commandDispatcher.register(
			Commands.literal("transform")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.literal("into")
						.then(
							Commands.argument("entity", ResourceArgument.resource(commandBuildContext, Registries.ENTITY_TYPE))
								.executes(
									commandContext -> transformEntity(commandContext.getSource(), ResourceArgument.getResource(commandContext, "entity", Registries.ENTITY_TYPE), null)
								)
								.then(
									Commands.argument("nbt", CompoundTagArgument.compoundTag())
										.executes(
											commandContext -> transformEntity(
													commandContext.getSource(),
													ResourceArgument.getResource(commandContext, "entity", Registries.ENTITY_TYPE),
													CompoundTagArgument.getCompoundTag(commandContext, "nbt")
												)
										)
								)
						)
						.then(
							Commands.literal("player")
								.then(
									Commands.argument("player", GameProfileArgument.gameProfile())
										.executes(commandContext -> transformPlayerSkin(commandContext.getSource(), GameProfileArgument.getGameProfiles(commandContext, "player")))
								)
						)
				)
				.then(
					Commands.literal("scale")
						.then(
							Commands.argument("scale", FloatArgumentType.floatArg(0.1F, 16.0F))
								.executes(commandContext -> transformScale(commandContext.getSource(), FloatArgumentType.getFloat(commandContext, "scale")))
						)
				)
				.then(Commands.literal("clear").executes(commandContext -> clearTransform(commandContext.getSource())))
		);
	}

	private static int transformEntity(CommandSourceStack commandSourceStack, Holder.Reference<EntityType<?>> reference, @Nullable CompoundTag compoundTag) throws CommandSyntaxException {
		if (commandSourceStack.getEntityOrException() instanceof LivingEntity livingEntity) {
			livingEntity.updateTransform(entityTransformType -> entityTransformType.withEntity(reference.value(), Optional.ofNullable(compoundTag)));
			commandSourceStack.sendSuccess(Component.literal("Transformed into ").append(reference.value().getDescription()), false);
			return 1;
		} else {
			throw ERROR_NOT_LIVING.create();
		}
	}

	private static int transformPlayerSkin(CommandSourceStack commandSourceStack, Collection<GameProfile> collection) throws CommandSyntaxException {
		if (collection.size() != 1) {
			throw ERROR_MULTIPLE_SKINS.create();
		} else {
			ServerPlayer serverPlayer = commandSourceStack.getPlayerOrException();
			SkullBlockEntity.updateGameprofile((GameProfile)collection.iterator().next(), gameProfile -> {
				serverPlayer.updateTransform(entityTransformType -> entityTransformType.withPlayerSkin(Optional.of((GameProfile)collection.iterator().next())));
				commandSourceStack.sendSuccess(Component.literal("Applied skin of " + ((GameProfile)collection.iterator().next()).getName()), false);
			});
			return 1;
		}
	}

	private static int transformScale(CommandSourceStack commandSourceStack, float f) throws CommandSyntaxException {
		if (commandSourceStack.getEntityOrException() instanceof LivingEntity livingEntity) {
			livingEntity.updateTransform(entityTransformType -> entityTransformType.withScale(f));
			commandSourceStack.sendSuccess(Component.literal("Transformed scale by " + String.format("%.2f", f) + "x"), false);
			return 1;
		} else {
			throw ERROR_NOT_LIVING.create();
		}
	}

	private static int clearTransform(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
		if (commandSourceStack.getEntityOrException() instanceof LivingEntity livingEntity) {
			livingEntity.setTransform(EntityTransformType.IDENTITY);
			commandSourceStack.sendSuccess(Component.literal("Cleared transform"), false);
			return 1;
		} else {
			throw ERROR_NOT_LIVING.create();
		}
	}
}
