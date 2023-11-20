package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.commands.arguments.ObjectiveCriteriaArgument;
import net.minecraft.commands.arguments.OperationArgument;
import net.minecraft.commands.arguments.ScoreHolderArgument;
import net.minecraft.commands.arguments.ScoreboardSlotArgument;
import net.minecraft.commands.arguments.StyleArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.network.chat.numbers.FixedFormat;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreAccess;
import net.minecraft.world.scores.ScoreHolder;
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
										.then(
											Commands.literal("displayautoupdate")
												.then(
													Commands.argument("value", BoolArgumentType.bool())
														.executes(
															commandContext -> setDisplayAutoUpdate(
																	commandContext.getSource(), ObjectiveArgument.getObjective(commandContext, "objective"), BoolArgumentType.getBool(commandContext, "value")
																)
														)
												)
										)
										.then(
											addNumberFormats(
												Commands.literal("numberformat"),
												(commandContext, numberFormat) -> setObjectiveFormat(
														commandContext.getSource(), ObjectiveArgument.getObjective(commandContext, "objective"), numberFormat
													)
											)
										)
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
							Commands.literal("display")
								.then(
									Commands.literal("name")
										.then(
											Commands.argument("targets", ScoreHolderArgument.scoreHolders())
												.suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
												.then(
													Commands.argument("objective", ObjectiveArgument.objective())
														.then(
															Commands.argument("name", ComponentArgument.textComponent())
																.executes(
																	commandContext -> setScoreDisplay(
																			commandContext.getSource(),
																			ScoreHolderArgument.getNamesWithDefaultWildcard(commandContext, "targets"),
																			ObjectiveArgument.getObjective(commandContext, "objective"),
																			ComponentArgument.getComponent(commandContext, "name")
																		)
																)
														)
														.executes(
															commandContext -> setScoreDisplay(
																	commandContext.getSource(),
																	ScoreHolderArgument.getNamesWithDefaultWildcard(commandContext, "targets"),
																	ObjectiveArgument.getObjective(commandContext, "objective"),
																	null
																)
														)
												)
										)
								)
								.then(
									Commands.literal("numberformat")
										.then(
											Commands.argument("targets", ScoreHolderArgument.scoreHolders())
												.suggests(ScoreHolderArgument.SUGGEST_SCORE_HOLDERS)
												.then(
													addNumberFormats(
														Commands.argument("objective", ObjectiveArgument.objective()),
														(commandContext, numberFormat) -> setScoreNumberFormat(
																commandContext.getSource(),
																ScoreHolderArgument.getNamesWithDefaultWildcard(commandContext, "targets"),
																ObjectiveArgument.getObjective(commandContext, "objective"),
																numberFormat
															)
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

	private static ArgumentBuilder<CommandSourceStack, ?> addNumberFormats(
		ArgumentBuilder<CommandSourceStack, ?> argumentBuilder, ScoreboardCommand.NumberFormatCommandExecutor numberFormatCommandExecutor
	) {
		return argumentBuilder.then(Commands.literal("blank").executes(commandContext -> numberFormatCommandExecutor.run(commandContext, BlankFormat.INSTANCE)))
			.then(Commands.literal("fixed").then(Commands.argument("contents", ComponentArgument.textComponent()).executes(commandContext -> {
				Component component = ComponentArgument.getComponent(commandContext, "contents");
				return numberFormatCommandExecutor.run(commandContext, new FixedFormat(component));
			})))
			.then(Commands.literal("styled").then(Commands.argument("style", StyleArgument.style()).executes(commandContext -> {
				Style style = StyleArgument.getStyle(commandContext, "style");
				return numberFormatCommandExecutor.run(commandContext, new StyledFormat(style));
			})))
			.executes(commandContext -> numberFormatCommandExecutor.run(commandContext, null));
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
		CommandSourceStack commandSourceStack, Collection<ScoreHolder> collection, SuggestionsBuilder suggestionsBuilder
	) {
		List<String> list = Lists.<String>newArrayList();
		Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();

		for (Objective objective : scoreboard.getObjectives()) {
			if (objective.getCriteria() == ObjectiveCriteria.TRIGGER) {
				boolean bl = false;

				for (ScoreHolder scoreHolder : collection) {
					ReadOnlyScoreInfo readOnlyScoreInfo = scoreboard.getPlayerScoreInfo(scoreHolder, objective);
					if (readOnlyScoreInfo == null || readOnlyScoreInfo.isLocked()) {
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

	private static int getScore(CommandSourceStack commandSourceStack, ScoreHolder scoreHolder, Objective objective) throws CommandSyntaxException {
		Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
		ReadOnlyScoreInfo readOnlyScoreInfo = scoreboard.getPlayerScoreInfo(scoreHolder, objective);
		if (readOnlyScoreInfo == null) {
			throw ERROR_NO_VALUE.create(objective.getName(), scoreHolder.getFeedbackDisplayName());
		} else {
			commandSourceStack.sendSuccess(
				() -> Component.translatable(
						"commands.scoreboard.players.get.success", scoreHolder.getFeedbackDisplayName(), readOnlyScoreInfo.value(), objective.getFormattedDisplayName()
					),
				false
			);
			return readOnlyScoreInfo.value();
		}
	}

	private static Component getFirstTargetName(Collection<ScoreHolder> collection) {
		return ((ScoreHolder)collection.iterator().next()).getFeedbackDisplayName();
	}

	private static int performOperation(
		CommandSourceStack commandSourceStack,
		Collection<ScoreHolder> collection,
		Objective objective,
		OperationArgument.Operation operation,
		Collection<ScoreHolder> collection2,
		Objective objective2
	) throws CommandSyntaxException {
		Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
		int i = 0;

		for (ScoreHolder scoreHolder : collection) {
			ScoreAccess scoreAccess = scoreboard.getOrCreatePlayerScore(scoreHolder, objective);

			for (ScoreHolder scoreHolder2 : collection2) {
				ScoreAccess scoreAccess2 = scoreboard.getOrCreatePlayerScore(scoreHolder2, objective2);
				operation.apply(scoreAccess, scoreAccess2);
			}

			i += scoreAccess.get();
		}

		if (collection.size() == 1) {
			int j = i;
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.scoreboard.players.operation.success.single", objective.getFormattedDisplayName(), getFirstTargetName(collection), j),
				true
			);
		} else {
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.scoreboard.players.operation.success.multiple", objective.getFormattedDisplayName(), collection.size()), true
			);
		}

		return i;
	}

	private static int enableTrigger(CommandSourceStack commandSourceStack, Collection<ScoreHolder> collection, Objective objective) throws CommandSyntaxException {
		if (objective.getCriteria() != ObjectiveCriteria.TRIGGER) {
			throw ERROR_NOT_TRIGGER.create();
		} else {
			Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
			int i = 0;

			for (ScoreHolder scoreHolder : collection) {
				ScoreAccess scoreAccess = scoreboard.getOrCreatePlayerScore(scoreHolder, objective);
				if (scoreAccess.locked()) {
					scoreAccess.unlock();
					i++;
				}
			}

			if (i == 0) {
				throw ERROR_TRIGGER_ALREADY_ENABLED.create();
			} else {
				if (collection.size() == 1) {
					commandSourceStack.sendSuccess(
						() -> Component.translatable("commands.scoreboard.players.enable.success.single", objective.getFormattedDisplayName(), getFirstTargetName(collection)),
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

	private static int resetScores(CommandSourceStack commandSourceStack, Collection<ScoreHolder> collection) {
		Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();

		for (ScoreHolder scoreHolder : collection) {
			scoreboard.resetAllPlayerScores(scoreHolder);
		}

		if (collection.size() == 1) {
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.reset.all.single", getFirstTargetName(collection)), true);
		} else {
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.reset.all.multiple", collection.size()), true);
		}

		return collection.size();
	}

	private static int resetScore(CommandSourceStack commandSourceStack, Collection<ScoreHolder> collection, Objective objective) {
		Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();

		for (ScoreHolder scoreHolder : collection) {
			scoreboard.resetSinglePlayerScore(scoreHolder, objective);
		}

		if (collection.size() == 1) {
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.scoreboard.players.reset.specific.single", objective.getFormattedDisplayName(), getFirstTargetName(collection)),
				true
			);
		} else {
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.scoreboard.players.reset.specific.multiple", objective.getFormattedDisplayName(), collection.size()), true
			);
		}

		return collection.size();
	}

	private static int setScore(CommandSourceStack commandSourceStack, Collection<ScoreHolder> collection, Objective objective, int i) {
		Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();

		for (ScoreHolder scoreHolder : collection) {
			scoreboard.getOrCreatePlayerScore(scoreHolder, objective).set(i);
		}

		if (collection.size() == 1) {
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.scoreboard.players.set.success.single", objective.getFormattedDisplayName(), getFirstTargetName(collection), i),
				true
			);
		} else {
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.scoreboard.players.set.success.multiple", objective.getFormattedDisplayName(), collection.size(), i), true
			);
		}

		return i * collection.size();
	}

	private static int setScoreDisplay(
		CommandSourceStack commandSourceStack, Collection<ScoreHolder> collection, Objective objective, @Nullable Component component
	) {
		Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();

		for (ScoreHolder scoreHolder : collection) {
			scoreboard.getOrCreatePlayerScore(scoreHolder, objective).display(component);
		}

		if (component == null) {
			if (collection.size() == 1) {
				commandSourceStack.sendSuccess(
					() -> Component.translatable(
							"commands.scoreboard.players.display.name.clear.success.single", getFirstTargetName(collection), objective.getFormattedDisplayName()
						),
					true
				);
			} else {
				commandSourceStack.sendSuccess(
					() -> Component.translatable("commands.scoreboard.players.display.name.clear.success.multiple", collection.size(), objective.getFormattedDisplayName()),
					true
				);
			}
		} else if (collection.size() == 1) {
			commandSourceStack.sendSuccess(
				() -> Component.translatable(
						"commands.scoreboard.players.display.name.set.success.single", component, getFirstTargetName(collection), objective.getFormattedDisplayName()
					),
				true
			);
		} else {
			commandSourceStack.sendSuccess(
				() -> Component.translatable(
						"commands.scoreboard.players.display.name.set.success.multiple", component, collection.size(), objective.getFormattedDisplayName()
					),
				true
			);
		}

		return collection.size();
	}

	private static int setScoreNumberFormat(
		CommandSourceStack commandSourceStack, Collection<ScoreHolder> collection, Objective objective, @Nullable NumberFormat numberFormat
	) {
		Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();

		for (ScoreHolder scoreHolder : collection) {
			scoreboard.getOrCreatePlayerScore(scoreHolder, objective).numberFormatOverride(numberFormat);
		}

		if (numberFormat == null) {
			if (collection.size() == 1) {
				commandSourceStack.sendSuccess(
					() -> Component.translatable(
							"commands.scoreboard.players.display.numberFormat.clear.success.single", getFirstTargetName(collection), objective.getFormattedDisplayName()
						),
					true
				);
			} else {
				commandSourceStack.sendSuccess(
					() -> Component.translatable(
							"commands.scoreboard.players.display.numberFormat.clear.success.multiple", collection.size(), objective.getFormattedDisplayName()
						),
					true
				);
			}
		} else if (collection.size() == 1) {
			commandSourceStack.sendSuccess(
				() -> Component.translatable(
						"commands.scoreboard.players.display.numberFormat.set.success.single", getFirstTargetName(collection), objective.getFormattedDisplayName()
					),
				true
			);
		} else {
			commandSourceStack.sendSuccess(
				() -> Component.translatable(
						"commands.scoreboard.players.display.numberFormat.set.success.multiple", collection.size(), objective.getFormattedDisplayName()
					),
				true
			);
		}

		return collection.size();
	}

	private static int addScore(CommandSourceStack commandSourceStack, Collection<ScoreHolder> collection, Objective objective, int i) {
		Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
		int j = 0;

		for (ScoreHolder scoreHolder : collection) {
			ScoreAccess scoreAccess = scoreboard.getOrCreatePlayerScore(scoreHolder, objective);
			scoreAccess.set(scoreAccess.get() + i);
			j += scoreAccess.get();
		}

		if (collection.size() == 1) {
			int k = j;
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.scoreboard.players.add.success.single", i, objective.getFormattedDisplayName(), getFirstTargetName(collection), k),
				true
			);
		} else {
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.scoreboard.players.add.success.multiple", i, objective.getFormattedDisplayName(), collection.size()), true
			);
		}

		return j;
	}

	private static int removeScore(CommandSourceStack commandSourceStack, Collection<ScoreHolder> collection, Objective objective, int i) {
		Scoreboard scoreboard = commandSourceStack.getServer().getScoreboard();
		int j = 0;

		for (ScoreHolder scoreHolder : collection) {
			ScoreAccess scoreAccess = scoreboard.getOrCreatePlayerScore(scoreHolder, objective);
			scoreAccess.set(scoreAccess.get() - i);
			j += scoreAccess.get();
		}

		if (collection.size() == 1) {
			int k = j;
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.scoreboard.players.remove.success.single", i, objective.getFormattedDisplayName(), getFirstTargetName(collection), k),
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
		Collection<ScoreHolder> collection = commandSourceStack.getServer().getScoreboard().getTrackedPlayers();
		if (collection.isEmpty()) {
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.list.empty"), false);
		} else {
			commandSourceStack.sendSuccess(
				() -> Component.translatable(
						"commands.scoreboard.players.list.success", collection.size(), ComponentUtils.formatList(collection, ScoreHolder::getFeedbackDisplayName)
					),
				false
			);
		}

		return collection.size();
	}

	private static int listTrackedPlayerScores(CommandSourceStack commandSourceStack, ScoreHolder scoreHolder) {
		Object2IntMap<Objective> object2IntMap = commandSourceStack.getServer().getScoreboard().listPlayerScores(scoreHolder);
		if (object2IntMap.isEmpty()) {
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.players.list.entity.empty", scoreHolder.getFeedbackDisplayName()), false);
		} else {
			commandSourceStack.sendSuccess(
				() -> Component.translatable("commands.scoreboard.players.list.entity.success", scoreHolder.getFeedbackDisplayName(), object2IntMap.size()), false
			);
			Object2IntMaps.fastForEach(
				object2IntMap,
				entry -> commandSourceStack.sendSuccess(
						() -> Component.translatable("commands.scoreboard.players.list.entity.entry", ((Objective)entry.getKey()).getFormattedDisplayName(), entry.getIntValue()),
						false
					)
			);
		}

		return object2IntMap.size();
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

	private static int setDisplayAutoUpdate(CommandSourceStack commandSourceStack, Objective objective, boolean bl) {
		if (objective.displayAutoUpdate() != bl) {
			objective.setDisplayAutoUpdate(bl);
			if (bl) {
				commandSourceStack.sendSuccess(
					() -> Component.translatable("commands.scoreboard.objectives.modify.displayAutoUpdate.enable", objective.getName(), objective.getFormattedDisplayName()),
					true
				);
			} else {
				commandSourceStack.sendSuccess(
					() -> Component.translatable("commands.scoreboard.objectives.modify.displayAutoUpdate.disable", objective.getName(), objective.getFormattedDisplayName()),
					true
				);
			}
		}

		return 0;
	}

	private static int setObjectiveFormat(CommandSourceStack commandSourceStack, Objective objective, @Nullable NumberFormat numberFormat) {
		objective.setNumberFormat(numberFormat);
		if (numberFormat != null) {
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.modify.objectiveFormat.set", objective.getName()), true);
		} else {
			commandSourceStack.sendSuccess(() -> Component.translatable("commands.scoreboard.objectives.modify.objectiveFormat.clear", objective.getName()), true);
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
			scoreboard.addObjective(string, objectiveCriteria, component, objectiveCriteria.getDefaultRenderType(), false, null);
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

	@FunctionalInterface
	public interface NumberFormatCommandExecutor {
		int run(CommandContext<CommandSourceStack> commandContext, @Nullable NumberFormat numberFormat) throws CommandSyntaxException;
	}
}
