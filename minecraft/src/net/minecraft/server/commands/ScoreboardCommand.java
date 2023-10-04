package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.commands.arguments.ObjectiveCriteriaArgument;
import net.minecraft.commands.arguments.OperationArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.ScoreboardSlotArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class ScoreboardCommand {
	private static final SimpleCommandExceptionType ERROR_OBJECTIVE_ALREADY_EXISTS = new SimpleCommandExceptionType(
		Component.translatable("commands.scoreboard.objectives.add.duplicate")
	);
	private static final SimpleCommandExceptionType ERROR_DISPLAY_SLOT_ALREADY_EMPTY = new SimpleCommandExceptionType(
		Component.translatable("commands.scoreboard.objectives.display.alreadyEmpty")
	);
	private static final SimpleCommandExceptionType ERROR_DISPLAY_SLOT_ALREADY_SET = new SimpleCommandExceptionType(
		Component.translatable("commands.scoreboard.objectives.display.alreadySet")
	);
	private static final SimpleCommandExceptionType ERROR_TRIGGER_ALREADY_ENABLED = new SimpleCommandExceptionType(
		Component.translatable("commands.scoreboard.players.enable.failed")
	);
	private static final SimpleCommandExceptionType ERROR_NOT_TRIGGER = new SimpleCommandExceptionType(
		Component.translatable("commands.scoreboard.players.enable.invalid")
	);
	private static final Dynamic2CommandExceptionType ERROR_NO_VALUE = new Dynamic2CommandExceptionType(
		(object, object2) -> Component.translatableEscape("commands.scoreboard.players.get.null", object, object2)
	);

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("scoreboard")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.literal("objectives")
						.then(Commands.literal("list").executes(commandContext -> listObjectives(commandContext.getSource())))
						.then(
							Commands.literal("add")
								.then(
									Commands.argument("objective", StringArgumentType.word())
										.then(
											Commands.argument("criteria", ObjectiveCriteriaArgument.criteria())
												.executes(
													commandContext -> addObjective(
															commandContext.getSource(),
															StringArgumentType.getString(commandContext, "objective"),
															ObjectiveCriteriaArgument.getCriteria(commandContext, "criteria"),
															Component.literal(StringArgumentType.getString(commandContext, "objective"))
														)
												)
												.then(
													Commands.argument("displayName", ComponentArgument.textComponent())
														.executes(
															commandContext -> addObjective(
																	commandContext.getSource(),
																	StringArgumentType.getString(commandContext, "objective"),
																	ObjectiveCriteriaArgument.getCriteria(commandContext, "criteria"),
																	ComponentArgument.getComponent(commandContext, "displayName")
																)
														)
												)
										)
								)
						)
						.then(
							Commands.literal("modify")
								.then(
									Commands.argument("objective", ObjectiveArgument.objective())
										.then(
											Commands.literal("displayname")
												.then(
													Commands.argument("displayName", ComponentArgument.textComponent())
														.executes(
															commandContext -> setDisplayName(
																	commandContext.getSource(),
																	ObjectiveArgument.getObjective(commandContext, "objective"),
																	ComponentArgument.getComponent(commandContext, "displayName")
																)
														)
												)
										)
										.then(createRenderTypeModify())
								)
						)
						.then(
							Commands.literal("remove")
								.then(
									Commands.argument("objective", ObjectiveArgument.objective())
										.executes(commandContext -> removeObjective(commandContext.getSource(), ObjectiveArgument.getObjective(commandContext, "objective")))
								)
						)
						.then(
							Commands.literal("setdisplay")
								.then(
									Commands.argument("slot", ScoreboardSlotArgument.displaySlot())
										.executes(commandContext -> clearDisplaySlot(commandContext.getSource(), ScoreboardSlotArgument.getDisplaySlot(commandContext, "slot")))
										.then(
											Commands.argument("objective", ObjectiveArgument.objective())
												.executes(
													commandContext -> setDisplaySlot(
															commandContext.getSource(),
															ScoreboardSlotArgument.getDisplaySlot(commandContext, "slot"),
															ObjectiveArgument.getObjective(commandContext, "objective")
														)
												)
										)
								)
						)
				)
				.then(
					Commands.literal("players")
						.then(
							Commands.literal("list")
								.executes(commandContext -> listTrackedPlayers(commandContext.getSource()))
								.then(
									Commands.argument("target", ScoreHolderArgument.scoreHolder())
										.suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
										.executes(commandContext -> listTrackedPlayerScores(commandContext.getSource(), ScoreHolderArgument.getName(commandContext, "target")))
								)
						)
						.then(
							Commands.literal("set")
								.then(
									Commands.argument("targets", ScoreHolderArgument.scoreHolders())
										.suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
										.then(
											Commands.argument("objective", ObjectiveArgument.objective())
												.then(
													Commands.argument("score", IntegerArgumentType.integer())
														.executes(
															commandContext -> setScore(
																	commandContext.getSource(),
																	ScoreHolderArgument.getNamesWithDefaultWildcard(commandContext, "targets"),
																	ObjectiveArgument.getWritableObjective(commandContext, "objective"),
																	IntegerArgumentType.getInteger(commandContext, "score")
																)
														)
												)
										)
								)
						)
						.then(
							Commands.literal("get")
								.then(
									Commands.argument("target", ScoreHolderArgument.scoreHolder())
										.suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
										.then(
											Commands.argument("objective", ObjectiveArgument.objective())
												.executes(
													commandContext -> getScore(
															commandContext.getSource(), ScoreHolderArgument.getName(commandContext, "target"), ObjectiveArgument.getObjective(commandContext, "objective")
														)
												)
										)
								)
						)
						.then(
							Commands.literal("add")
								.then(
									Commands.argument("targets", ScoreHolderArgument.scoreHolders())
										.suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
										.then(
											Commands.argument("objective", ObjectiveArgument.objective())
												.then(
													Commands.argument("score", IntegerArgumentType.integer(0))
														.executes(
															commandContext -> addScore(
																	commandContext.getSource(),
																	ScoreHolderArgument.getNamesWithDefaultWildcard(commandContext, "targets"),
																	ObjectiveArgument.getWritableObjective(commandContext, "objective"),
																	IntegerArgumentType.getInteger(commandContext, "score")
																)
														)
												)
										)
								)
						)
						.then(
							Commands.literal("remove")
								.then(
									Commands.argument("targets", ScoreHolderArgument.scoreHolders())
										.suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
										.then(
											Commands.argument("objective", ObjectiveArgument.objective())
												.then(
													Commands.argument("score", IntegerArgumentType.integer(0))
														.executes(
															commandContext -> removeScore(
																	commandContext.getSource(),
																	ScoreHolderArgument.getNamesWithDefaultWildcard(commandContext, "targets"),
																	ObjectiveArgument.getWritableObjective(commandContext, "objective"),
																	IntegerArgumentType.getInteger(commandContext, "score")
																)
														)
												)
										)
								)
						)
						.then(
							Commands.literal("reset")
								.then(
									Commands.argument("targets", ScoreHolderArgument.scoreHolders())
										.suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
										.executes(commandContext -> resetScores(commandContext.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(commandContext, "targets")))
										.then(
											Commands.argument("objective", ObjectiveArgument.objective())
												.executes(
													commandContext -> resetScore(
															commandContext.getSource(),
															ScoreHolderArgument.getNamesWithDefaultWildcard(commandContext, "targets"),
															ObjectiveArgument.getObjective(commandContext, "objective")
														)
												)
										)
								)
						)
						.then(
							Commands.literal("enable")
								.then(
									Commands.argument("targets", ScoreHolderArgument.scoreHolders())
										.suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
										.then(
											Commands.argument("objective", ObjectiveArgument.objective())
												.suggests(
													(commandContext, suggestionsBuilder) -> suggestTriggers(
															commandContext.getSource(), ScoreHolderArgument.getNamesWithDefaultWildcard(commandContext, "targets"), suggestionsBuilder
														)
												)
												.executes(
													commandContext -> enableTrigger(
															commandContext.getSource(),
															ScoreHolderArgument.getNamesWithDefaultWildcard(commandContext, "targets"),
															ObjectiveArgument.getObjective(commandContext, "objective")
														)
												)
										)
								)
						)
						.then(
							Commands.literal("operation")
								.then(
									Commands.argument("targets", ScoreHolderArgument.scoreHolders())
										.suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
										.then(
											Commands.argument("targetObjective", ObjectiveArgument.objective())
												.then(
													Commands.argument("operation", OperationArgument.operation())
														.then(
															Commands.argument("source", ScoreHolderArgument.scoreHolders())
																.suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
																.then(
																	Commands.argument("sourceObjective", ObjectiveArgument.objective())
																		.executes(
																			commandContext -> performOperation(
																					commandContext.getSource(),
																					ScoreHolderArgument.getNamesWithDefaultWildcard(commandContext, "targets"),
																					ObjectiveArgument.getWritableObjective(commandContext, "targetObjective"),
																					OperationArgument.getOperation(commandContext, "operation"),
																					ScoreHolderArgument.getNamesWithDefaultWildcard(commandContext, "source"),
																					ObjectiveArgument.getObjective(commandContext, "sourceObjective")
																				)
																		)
																)
														)
												)
										)
								)
						)
				)
		);
	}

	private static LiteralArgumentBuilder<CommandSourceStack> createRenderTypeModify() {
		LiteralArgumentBuilder<CommandSourceStack> literalArgumentBuilder = Commands.literal("rendertype");

		for (ObjectiveCriteria.RenderType renderType : ObjectiveCriteria.RenderType.values()) {
			literalArgumentBuilder.then(
				Commands.literal(renderType.getId())
					.executes(commandContext -> setRenderType(commandContext.getSource(), ObjectiveArgument.getObjective(commandContext, "objective"), renderType))
			);
		}

		return literalArgumentBuilder;
	}

	private static CompletableFuture<Suggestions> suggestTriggers(
		CommandSourceStack commandSourceStack, Collection<String> collection, SuggestionsBuilder suggestionsBuilder
	) {
		List<String> list = Lists.<String>newArrayList();
		Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();

		for (Objective objective : scoreboard.getObjectives()) {
			if (objective.getCriteria() == ObjectiveCriteria.TRIGGER) {
				boolean bl = false;

				for (String string : collection) {
					if (!scoreboard.hasPlayerScore(string, objective) || scoreboard.getOrCreatePlayerScore(string, objective).isLocked()) {
						bl = true;
						break;
					}
				}

				if (bl) {
					list.add(objective.getName());
				}
			}
		}

		return SharedSuggestionProvider.suggest(list, suggestionsBuilder);
	}

	private static int getScore(CommandSourceStack commandSourceStack, String string, Objective objective) throws CommandSyntaxException {
		Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
		if (!scoreboard.hasPlayerScore(string, objective)) {
			throw ERROR_NO_VALUE.create(objective.getName(), string);
		} else {
			Score score = scoreboard.getOrCreatePlayerScore(string, objective);
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.scoreboard.players.get.success", string, score.getScore(), objective.getFormattedDisplayName()), false
			);
			return score.getScore();
		}
	}

	private static int performOperation(
		CommandSourceStack commandSourceStack,
		Collection<String> collection,
		Objective objective,
		OperationArgument.Operation operation,
		Collection<String> collection2,
		Objective objective2
	) throws CommandSyntaxException {
		Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
		int i = 0;

		for (String string : collection) {
			Score score = scoreboard.getOrCreatePlayerScore(string, objective);

			for (String string2 : collection2) {
				Score score2 = scoreboard.getOrCreatePlayerScore(string2, objective2);
				operation.apply(score, score2);
			}

			i += score.getScore();
		}

		if (collection.size() == 1) {
			int j = i;
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.scoreboard.players.operation.success.single", objective.getFormattedDisplayName(), collection.iterator().next(), j),
				true
			);
		} else {
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.scoreboard.players.operation.success.multiple", objective.getFormattedDisplayName(), collection.size()), true
			);
		}

		return i;
	}

	private static int enableTrigger(CommandSourceStack commandSourceStack, Collection<String> collection, Objective objective) throws CommandSyntaxException {
		if (objective.getCriteria() != ObjectiveCriteria.TRIGGER) {
			throw ERROR_NOT_TRIGGER.create();
		} else {
			Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
			int i = 0;

			for (String string : collection) {
				Score score = scoreboard.getOrCreatePlayerScore(string, objective);
				if (score.isLocked()) {
					score.setLocked(false);
					i++;
				}
			}

			if (i == 0) {
				throw ERROR_TRIGGER_ALREADY_ENABLED.create();
			} else {
				if (collection.size() == 1) {
					commandSourceStack.sendSuccess(
						() -> Component.translatable("commands.scoreboard.players.enable.success.single", objective.getFormattedDisplayName(), collection.iterator().next()),
						true
					);
				} else {
					commandSourceStack.sendSuccess(
						() -> Component.translatable("commands.scoreboard.players.enable.success.multiple", objective.getFormattedDisplayName(), collection.size()), true
					);
				}

				return i;
			}
		}
	}

	private static int resetScores(CommandSourceStack commandSourceStack, Collection<String> collection) {
		Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();

		for (String string : collection) {
			scoreboard.resetPlayerScore(string, null);
		}

		if (collection.size() == 1) {
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.reset.all.single", collection.iterator().next()), true);
		} else {
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.reset.all.multiple", collection.size()), true);
		}

		return collection.size();
	}

	private static int resetScore(CommandSourceStack commandSourceStack, Collection<String> collection, Objective objective) {
		Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();

		for (String string : collection) {
			scoreboard.resetPlayerScore(string, objective);
		}

		if (collection.size() == 1) {
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.scoreboard.players.reset.specific.single", objective.getFormattedDisplayName(), collection.iterator().next()), true
			);
		} else {
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.scoreboard.players.reset.specific.multiple", objective.getFormattedDisplayName(), collection.size()), true
			);
		}

		return collection.size();
	}

	private static int setScore(CommandSourceStack commandSourceStack, Collection<String> collection, Objective objective, int i) {
		Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();

		for (String string : collection) {
			Score score = scoreboard.getOrCreatePlayerScore(string, objective);
			score.setScore(i);
		}

		if (collection.size() == 1) {
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.scoreboard.players.set.success.single", objective.getFormattedDisplayName(), collection.iterator().next(), i), true
			);
		} else {
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.scoreboard.players.set.success.multiple", objective.getFormattedDisplayName(), collection.size(), i), true
			);
		}

		return i * collection.size();
	}

	private static int addScore(CommandSourceStack commandSourceStack, Collection<String> collection, Objective objective, int i) {
		Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
		int j = 0;

		for (String string : collection) {
			Score score = scoreboard.getOrCreatePlayerScore(string, objective);
			score.setScore(score.getScore() + i);
			j += score.getScore();
		}

		if (collection.size() == 1) {
			int k = j;
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.scoreboard.players.add.success.single", i, objective.getFormattedDisplayName(), collection.iterator().next(), k),
				true
			);
		} else {
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.scoreboard.players.add.success.multiple", i, objective.getFormattedDisplayName(), collection.size()), true
			);
		}

		return j;
	}

	private static int removeScore(CommandSourceStack commandSourceStack, Collection<String> collection, Objective objective, int i) {
		Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
		int j = 0;

		for (String string : collection) {
			Score score = scoreboard.getOrCreatePlayerScore(string, objective);
			score.setScore(score.getScore() - i);
			j += score.getScore();
		}

		if (collection.size() == 1) {
			int k = j;
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.scoreboard.players.remove.success.single", i, objective.getFormattedDisplayName(), collection.iterator().next(), k),
				true
			);
		} else {
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.scoreboard.players.remove.success.multiple", i, objective.getFormattedDisplayName(), collection.size()), true
			);
		}

		return j;
	}

	private static int listTrackedPlayers(CommandSourceStack commandSourceStack) {
		Collection<String> collection = commandSourceStack.getServer().getScoreboard().getTrackedPlayers();
		if (collection.isEmpty()) {
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.list.empty"), false);
		} else {
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.scoreboard.players.list.success", collection.size(), ComponentUtils.formatList(collection)), false
			);
		}

		return collection.size();
	}

	private static int listTrackedPlayerScores(CommandSourceStack commandSourceStack, String string) {
		Map<Objective, Score> map = commandSourceStack.getServer().getScoreboard().getPlayerScores(string);
		if (map.isEmpty()) {
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.list.entity.empty", string), false);
		} else {
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.list.entity.success", string, map.size()), false);

			for (Entry<Objective, Score> entry : map.entrySet()) {
				commandSourceStack.sendSuccess(
					() -> Component.translatable(
							"commands.scoreboard.players.list.entity.entry", ((Objective)entry.getKey()).getFormattedDisplayName(), ((Score)entry.getValue()).getScore()
						),
					false
				);
			}
		}

		return map.size();
	}

	private static int clearDisplaySlot(CommandSourceStack commandSourceStack, DisplaySlot displaySlot) throws CommandSyntaxException {
		Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
		if (scoreboard.getDisplayObjective(displaySlot) == null) {
			throw ERROR_DISPLAY_SLOT_ALREADY_EMPTY.create();
		} else {
			scoreboard.setDisplayObjective(displaySlot, null);
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.display.cleared", displaySlot.getSerializedName()), true);
			return 0;
		}
	}

	private static int setDisplaySlot(CommandSourceStack commandSourceStack, DisplaySlot displaySlot, Objective objective) throws CommandSyntaxException {
		Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
		if (scoreboard.getDisplayObjective(displaySlot) == objective) {
			throw ERROR_DISPLAY_SLOT_ALREADY_SET.create();
		} else {
			scoreboard.setDisplayObjective(displaySlot, objective);
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.scoreboard.objectives.display.set", displaySlot.getSerializedName(), objective.getDisplayName()), true
			);
			return 0;
		}
	}

	private static int setDisplayName(CommandSourceStack commandSourceStack, Objective objective, Component component) {
		if (!objective.getDisplayName().equals(component)) {
			objective.setDisplayName(component);
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.scoreboard.objectives.modify.displayname", objective.getName(), objective.getFormattedDisplayName()), true
			);
		}

		return 0;
	}

	private static int setRenderType(CommandSourceStack commandSourceStack, Objective objective, ObjectiveCriteria.RenderType renderType) {
		if (objective.getRenderType() != renderType) {
			objective.setRenderType(renderType);
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.modify.rendertype", objective.getFormattedDisplayName()), true);
		}

		return 0;
	}

	private static int removeObjective(CommandSourceStack commandSourceStack, Objective objective) {
		Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
		scoreboard.removeObjective(objective);
		commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.remove.success", objective.getFormattedDisplayName()), true);
		return scoreboard.getObjectives().size();
	}

	private static int addObjective(CommandSourceStack commandSourceStack, String string, ObjectiveCriteria objectiveCriteria, Component component) throws CommandSyntaxException {
		Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
		if (scoreboard.getObjective(string) != null) {
			throw ERROR_OBJECTIVE_ALREADY_EXISTS.create();
		} else {
			scoreboard.addObjective(string, objectiveCriteria, component, objectiveCriteria.getDefaultRenderType());
			Objective objective = scoreboard.getObjective(string);
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.add.success", objective.getFormattedDisplayName()), true);
			return scoreboard.getObjectives().size();
		}
	}

	private static int listObjectives(CommandSourceStack commandSourceStack) {
		Collection<Objective> collection = commandSourceStack.getServer().getScoreboard().getObjectives();
		if (collection.isEmpty()) {
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.list.empty"), false);
		} else {
			commandSourceStack.sendSuccess(
				() -> Component.translatable(
						"commands.scoreboard.objectives.list.success", collection.size(), ComponentUtils.formatList(collection, Objective::getFormattedDisplayName)
					),
				false
			);
		}

		return collection.size();
	}
}
