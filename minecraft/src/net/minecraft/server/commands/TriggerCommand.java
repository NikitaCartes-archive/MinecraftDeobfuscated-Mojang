package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class TriggerCommand {
	private static final SimpleCommandExceptionType ERROR_NOT_PRIMED = new SimpleCommandExceptionType(Component.translatable("commands.trigger.failed.unprimed"));
	private static final SimpleCommandExceptionType ERROR_INVALID_OBJECTIVE = new SimpleCommandExceptionType(
		Component.translatable("commands.trigger.failed.invalid")
	);

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("trigger")
				.then(
					Commands.argument("objective", ObjectiveArgument.objective())
						.suggests((commandContext, suggestionsBuilder) -> suggestObjectives(commandContext.getSource(), suggestionsBuilder))
						.executes(
							commandContext -> simpleTrigger(
									commandContext.getSource(), commandContext.getSource().getPlayerOrException(), ObjectiveArgument.getObjective(commandContext, "objective")
								)
						)
						.then(
							Commands.literal("add")
								.then(
									Commands.argument("value", IntegerArgumentType.integer())
										.executes(
											commandContext -> addValue(
													commandContext.getSource(),
													commandContext.getSource().getPlayerOrException(),
													ObjectiveArgument.getObjective(commandContext, "objective"),
													IntegerArgumentType.getInteger(commandContext, "value")
												)
										)
								)
						)
						.then(
							Commands.literal("set")
								.then(
									Commands.argument("value", IntegerArgumentType.integer())
										.executes(
											commandContext -> setValue(
													commandContext.getSource(),
													commandContext.getSource().getPlayerOrException(),
													ObjectiveArgument.getObjective(commandContext, "objective"),
													IntegerArgumentType.getInteger(commandContext, "value")
												)
										)
								)
						)
				)
		);
	}

	public static CompletableFuture<Suggestions> suggestObjectives(CommandSourceStack commandSourceStack, SuggestionsBuilder suggestionsBuilder) {
		ScoreHolder scoreHolder = commandSourceStack.getEntity();
		List<String> list = Lists.<String>newArrayList();
		if (scoreHolder != null) {
			Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();

			for (Objective objective : scoreboard.getObjectives()) {
				if (objective.getCriteria() == ObjectiveCriteria.TRIGGER) {
					ReadOnlyScoreInfo readOnlyScoreInfo = scoreboard.getPlayerScoreInfo(scoreHolder, objective);
					if (readOnlyScoreInfo != null && !readOnlyScoreInfo.isLocked()) {
						list.add(objective.getName());
					}
				}
			}
		}

		return SharedSuggestionProvider.suggest(list, suggestionsBuilder);
	}

	private static int addValue(CommandSourceStack commandSourceStack, ServerPlayer serverPlayer, Objective objective, int i) throws CommandSyntaxException {
		ScoreAccess scoreAccess = getScore(commandSourceStack.getServer().getScoreboard(), serverPlayer, objective);
		int j = scoreAccess.add(i);
		commandSourceStack.sendSuccess(() -> Component.translatable("commands.trigger.add.success", objective.getFormattedDisplayName(), i), true);
		return j;
	}

	private static int setValue(CommandSourceStack commandSourceStack, ServerPlayer serverPlayer, Objective objective, int i) throws CommandSyntaxException {
		ScoreAccess scoreAccess = getScore(commandSourceStack.getServer().getScoreboard(), serverPlayer, objective);
		scoreAccess.set(i);
		commandSourceStack.sendSuccess(() -> Component.translatable("commands.trigger.set.success", objective.getFormattedDisplayName(), i), true);
		return i;
	}

	private static int simpleTrigger(CommandSourceStack commandSourceStack, ServerPlayer serverPlayer, Objective objective) throws CommandSyntaxException {
		ScoreAccess scoreAccess = getScore(commandSourceStack.getServer().getScoreboard(), serverPlayer, objective);
		int i = scoreAccess.add(1);
		commandSourceStack.sendSuccess(() -> Component.translatable("commands.trigger.simple.success", objective.getFormattedDisplayName()), true);
		return i;
	}

	private static ScoreAccess getScore(Scoreboard scoreboard, ScoreHolder scoreHolder, Objective objective) throws CommandSyntaxException {
		if (objective.getCriteria() != ObjectiveCriteria.TRIGGER) {
			throw ERROR_INVALID_OBJECTIVE.create();
		} else {
			ReadOnlyScoreInfo readOnlyScoreInfo = scoreboard.getPlayerScoreInfo(scoreHolder, objective);
			if (readOnlyScoreInfo != null && !readOnlyScoreInfo.isLocked()) {
				ScoreAccess scoreAccess = scoreboard.getOrCreatePlayerScore(scoreHolder, objective);
				scoreAccess.lock();
				return scoreAccess;
			} else {
				throw ERROR_NOT_PRIMED.create();
			}
		}
	}
}
