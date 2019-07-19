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
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class TriggerCommand {
	private static final SimpleCommandExceptionType ERROR_NOT_PRIMED = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.trigger.failed.unprimed")
	);
	private static final SimpleCommandExceptionType ERROR_INVALID_OBJECTIVE = new SimpleCommandExceptionType(
		new TranslatableComponent("commands.trigger.failed.invalid")
	);

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("trigger")
				.then(
					Commands.argument("objective", ObjectiveArgument.objective())
						.suggests((commandContext, suggestionsBuilder) -> suggestObjectives(commandContext.getSource(), suggestionsBuilder))
						.executes(
							commandContext -> simpleTrigger(
									commandContext.getSource(), getScore(commandContext.getSource().getPlayerOrException(), ObjectiveArgument.getObjective(commandContext, "objective"))
								)
						)
						.then(
							Commands.literal("add")
								.then(
									Commands.argument("value", IntegerArgumentType.integer())
										.executes(
											commandContext -> addValue(
													commandContext.getSource(),
													getScore(commandContext.getSource().getPlayerOrException(), ObjectiveArgument.getObjective(commandContext, "objective")),
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
													getScore(commandContext.getSource().getPlayerOrException(), ObjectiveArgument.getObjective(commandContext, "objective")),
													IntegerArgumentType.getInteger(commandContext, "value")
												)
										)
								)
						)
				)
		);
	}

	public static CompletableFuture<Suggestions> suggestObjectives(CommandSourceStack commandSourceStack, SuggestionsBuilder suggestionsBuilder) {
		Entity entity = commandSourceStack.getEntity();
		List<String> list = Lists.<String>newArrayList();
		if (entity != null) {
			Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
			String string = entity.getScoreboardName();

			for (Objective objective : scoreboard.getObjectives()) {
				if (objective.getCriteria() == ObjectiveCriteria.TRIGGER && scoreboard.hasPlayerScore(string, objective)) {
					Score score = scoreboard.getOrCreatePlayerScore(string, objective);
					if (!score.isLocked()) {
						list.add(objective.getName());
					}
				}
			}
		}

		return SharedSuggestionProvider.suggest(list, suggestionsBuilder);
	}

	private static int addValue(CommandSourceStack commandSourceStack, Score score, int i) {
		score.add(i);
		commandSourceStack.sendSuccess(new TranslatableComponent("commands.trigger.add.success", score.getObjective().getFormattedDisplayName(), i), true);
		return score.getScore();
	}

	private static int setValue(CommandSourceStack commandSourceStack, Score score, int i) {
		score.setScore(i);
		commandSourceStack.sendSuccess(new TranslatableComponent("commands.trigger.set.success", score.getObjective().getFormattedDisplayName(), i), true);
		return i;
	}

	private static int simpleTrigger(CommandSourceStack commandSourceStack, Score score) {
		score.add(1);
		commandSourceStack.sendSuccess(new TranslatableComponent("commands.trigger.simple.success", score.getObjective().getFormattedDisplayName()), true);
		return score.getScore();
	}

	private static Score getScore(ServerPlayer serverPlayer, Objective objective) throws CommandSyntaxException {
		if (objective.getCriteria() != ObjectiveCriteria.TRIGGER) {
			throw ERROR_INVALID_OBJECTIVE.create();
		} else {
			Scoreboard scoreboard = serverPlayer.getScoreboard();
			String string = serverPlayer.getScoreboardName();
			if (!scoreboard.hasPlayerScore(string, objective)) {
				throw ERROR_NOT_PRIMED.create();
			} else {
				Score score = scoreboard.getOrCreatePlayerScore(string, objective);
				if (score.isLocked()) {
					throw ERROR_NOT_PRIMED.create();
				} else {
					score.setLocked(true);
					return score;
				}
			}
		}
	}
}
