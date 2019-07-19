package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class ExperienceCommand {
	private static final SimpleCommandExceptionType ERROR_SET_POINTS_INVALID = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.experience.set.points.invalid")
	);

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		LiteralCommandNode<CommandSourceStack> literalCommandNode = commandDispatcher.register(
			Commands.literal("experience")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.literal("add")
						.then(
							Commands.argument("targets", EntityArgument.players())
								.then(
									Commands.argument("amount", IntegerArgumentType.integer())
										.executes(
											commandContext -> addExperience(
													commandContext.getSource(),
													EntityArgument.getPlayers(commandContext, "targets"),
													IntegerArgumentType.getInteger(commandContext, "amount"),
													ExperienceCommand.Type.POINTS
												)
										)
										.then(
											Commands.literal("points")
												.executes(
													commandContext -> addExperience(
															commandContext.getSource(),
															EntityArgument.getPlayers(commandContext, "targets"),
															IntegerArgumentType.getInteger(commandContext, "amount"),
															ExperienceCommand.Type.POINTS
														)
												)
										)
										.then(
											Commands.literal("levels")
												.executes(
													commandContext -> addExperience(
															commandContext.getSource(),
															EntityArgument.getPlayers(commandContext, "targets"),
															IntegerArgumentType.getInteger(commandContext, "amount"),
															ExperienceCommand.Type.LEVELS
														)
												)
										)
								)
						)
				)
				.then(
					Commands.literal("set")
						.then(
							Commands.argument("targets", EntityArgument.players())
								.then(
									Commands.argument("amount", IntegerArgumentType.integer(0))
										.executes(
											commandContext -> setExperience(
													commandContext.getSource(),
													EntityArgument.getPlayers(commandContext, "targets"),
													IntegerArgumentType.getInteger(commandContext, "amount"),
													ExperienceCommand.Type.POINTS
												)
										)
										.then(
											Commands.literal("points")
												.executes(
													commandContext -> setExperience(
															commandContext.getSource(),
															EntityArgument.getPlayers(commandContext, "targets"),
															IntegerArgumentType.getInteger(commandContext, "amount"),
															ExperienceCommand.Type.POINTS
														)
												)
										)
										.then(
											Commands.literal("levels")
												.executes(
													commandContext -> setExperience(
															commandContext.getSource(),
															EntityArgument.getPlayers(commandContext, "targets"),
															IntegerArgumentType.getInteger(commandContext, "amount"),
															ExperienceCommand.Type.LEVELS
														)
												)
										)
								)
						)
				)
				.then(
					Commands.literal("query")
						.then(
							Commands.argument("targets", EntityArgument.player())
								.then(
									Commands.literal("points")
										.executes(
											commandContext -> queryExperience(commandContext.getSource(), EntityArgument.getPlayer(commandContext, "targets"), ExperienceCommand.Type.POINTS)
										)
								)
								.then(
									Commands.literal("levels")
										.executes(
											commandContext -> queryExperience(commandContext.getSource(), EntityArgument.getPlayer(commandContext, "targets"), ExperienceCommand.Type.LEVELS)
										)
								)
						)
				)
		);
		commandDispatcher.register(Commands.literal("xp").requires(commandSourceStack -> commandSourceStack.hasPermission(2)).redirect(literalCommandNode));
	}

	private static int queryExperience(CommandSourceStack commandSourceStack, ServerPlayer serverPlayer, ExperienceCommand.Type type) {
		int i = type.query.applyAsInt(serverPlayer);
		commandSourceStack.sendSuccess(new TranslatableComponent("commands.experience.query." + type.name, serverPlayer.getDisplayName(), i), false);
		return i;
	}

	private static int addExperience(CommandSourceStack commandSourceStack, Collection<? extends ServerPlayer> collection, int i, ExperienceCommand.Type type) {
		for (ServerPlayer serverPlayer : collection) {
			type.add.accept(serverPlayer, i);
		}

		if (collection.size() == 1) {
			commandSourceStack.sendSuccess(
				new TranslatableComponent("commands.experience.add." + type.name + ".success.single", i, ((ServerPlayer)collection.iterator().next()).getDisplayName()),
				true
			);
		} else {
			commandSourceStack.sendSuccess(new TranslatableComponent("commands.experience.add." + type.name + ".success.multiple", i, collection.size()), true);
		}

		return collection.size();
	}

	private static int setExperience(CommandSourceStack commandSourceStack, Collection<? extends ServerPlayer> collection, int i, ExperienceCommand.Type type) throws CommandSyntaxException {
		int j = 0;

		for (ServerPlayer serverPlayer : collection) {
			if (type.set.test(serverPlayer, i)) {
				j++;
			}
		}

		if (j == 0) {
			throw ERROR_SET_POINTS_INVALID.create();
		} else {
			if (collection.size() == 1) {
				commandSourceStack.sendSuccess(
					new TranslatableComponent("commands.experience.set." + type.name + ".success.single", i, ((ServerPlayer)collection.iterator().next()).getDisplayName()),
					true
				);
			} else {
				commandSourceStack.sendSuccess(new TranslatableComponent("commands.experience.set." + type.name + ".success.multiple", i, collection.size()), true);
			}

			return collection.size();
		}
	}

	static enum Type {
		POINTS("points", Player::giveExperiencePoints, (serverPlayer, integer) -> {
			if (integer >= serverPlayer.getXpNeededForNextLevel()) {
				return false;
			} else {
				serverPlayer.setExperiencePoints(integer);
				return true;
			}
		}, serverPlayer -> Mth.floor(serverPlayer.experienceProgress * (float)serverPlayer.getXpNeededForNextLevel())),
		LEVELS("levels", ServerPlayer::giveExperienceLevels, (serverPlayer, integer) -> {
			serverPlayer.setExperienceLevels(integer);
			return true;
		}, serverPlayer -> serverPlayer.experienceLevel);

		public final BiConsumer<ServerPlayer, Integer> add;
		public final BiPredicate<ServerPlayer, Integer> set;
		public final String name;
		private final ToIntFunction<ServerPlayer> query;

		private Type(
			String string2, BiConsumer<ServerPlayer, Integer> biConsumer, BiPredicate<ServerPlayer, Integer> biPredicate, ToIntFunction<ServerPlayer> toIntFunction
		) {
			this.add = biConsumer;
			this.name = string2;
			this.set = biPredicate;
			this.query = toIntFunction;
		}
	}
}
