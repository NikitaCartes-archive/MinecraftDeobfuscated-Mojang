package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class AdvancementCommands {
	private static final DynamicCommandExceptionType ERROR_NO_ACTION_PERFORMED = new DynamicCommandExceptionType(object -> (Component)object);
	private static final Dynamic2CommandExceptionType ERROR_CRITERION_NOT_FOUND = new Dynamic2CommandExceptionType(
		(object, object2) -> Component.translatable("commands.advancement.criterionNotFound", object, object2)
	);
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_ADVANCEMENTS = (commandContext, suggestionsBuilder) -> {
		Collection<AdvancementHolder> collection = commandContext.getSource().getServer().getAdvancements().getAllAdvancements();
		return SharedSuggestionProvider.suggestResource(collection.stream().map(AdvancementHolder::id), suggestionsBuilder);
	};

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("advancement")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.literal("grant")
						.then(
							Commands.argument("targets", EntityArgument.players())
								.then(
									Commands.literal("only")
										.then(
											Commands.argument("advancement", ResourceLocationArgument.id())
												.suggests(SUGGEST_ADVANCEMENTS)
												.executes(
													commandContext -> perform(
															commandContext.getSource(),
															EntityArgument.getPlayers(commandContext, "targets"),
															AdvancementCommands.Action.GRANT,
															getAdvancements(commandContext, ResourceLocationArgument.getAdvancement(commandContext, "advancement"), AdvancementCommands.Mode.ONLY)
														)
												)
												.then(
													Commands.argument("criterion", StringArgumentType.greedyString())
														.suggests(
															(commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(
																	ResourceLocationArgument.getAdvancement(commandContext, "advancement").value().criteria().keySet(), suggestionsBuilder
																)
														)
														.executes(
															commandContext -> performCriterion(
																	commandContext.getSource(),
																	EntityArgument.getPlayers(commandContext, "targets"),
																	AdvancementCommands.Action.GRANT,
																	ResourceLocationArgument.getAdvancement(commandContext, "advancement"),
																	StringArgumentType.getString(commandContext, "criterion")
																)
														)
												)
										)
								)
								.then(
									Commands.literal("from")
										.then(
											Commands.argument("advancement", ResourceLocationArgument.id())
												.suggests(SUGGEST_ADVANCEMENTS)
												.executes(
													commandContext -> perform(
															commandContext.getSource(),
															EntityArgument.getPlayers(commandContext, "targets"),
															AdvancementCommands.Action.GRANT,
															getAdvancements(commandContext, ResourceLocationArgument.getAdvancement(commandContext, "advancement"), AdvancementCommands.Mode.FROM)
														)
												)
										)
								)
								.then(
									Commands.literal("until")
										.then(
											Commands.argument("advancement", ResourceLocationArgument.id())
												.suggests(SUGGEST_ADVANCEMENTS)
												.executes(
													commandContext -> perform(
															commandContext.getSource(),
															EntityArgument.getPlayers(commandContext, "targets"),
															AdvancementCommands.Action.GRANT,
															getAdvancements(commandContext, ResourceLocationArgument.getAdvancement(commandContext, "advancement"), AdvancementCommands.Mode.UNTIL)
														)
												)
										)
								)
								.then(
									Commands.literal("through")
										.then(
											Commands.argument("advancement", ResourceLocationArgument.id())
												.suggests(SUGGEST_ADVANCEMENTS)
												.executes(
													commandContext -> perform(
															commandContext.getSource(),
															EntityArgument.getPlayers(commandContext, "targets"),
															AdvancementCommands.Action.GRANT,
															getAdvancements(commandContext, ResourceLocationArgument.getAdvancement(commandContext, "advancement"), AdvancementCommands.Mode.THROUGH)
														)
												)
										)
								)
								.then(
									Commands.literal("everything")
										.executes(
											commandContext -> perform(
													commandContext.getSource(),
													EntityArgument.getPlayers(commandContext, "targets"),
													AdvancementCommands.Action.GRANT,
													commandContext.getSource().getServer().getAdvancements().getAllAdvancements()
												)
										)
								)
						)
				)
				.then(
					Commands.literal("revoke")
						.then(
							Commands.argument("targets", EntityArgument.players())
								.then(
									Commands.literal("only")
										.then(
											Commands.argument("advancement", ResourceLocationArgument.id())
												.suggests(SUGGEST_ADVANCEMENTS)
												.executes(
													commandContext -> perform(
															commandContext.getSource(),
															EntityArgument.getPlayers(commandContext, "targets"),
															AdvancementCommands.Action.REVOKE,
															getAdvancements(commandContext, ResourceLocationArgument.getAdvancement(commandContext, "advancement"), AdvancementCommands.Mode.ONLY)
														)
												)
												.then(
													Commands.argument("criterion", StringArgumentType.greedyString())
														.suggests(
															(commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(
																	ResourceLocationArgument.getAdvancement(commandContext, "advancement").value().criteria().keySet(), suggestionsBuilder
																)
														)
														.executes(
															commandContext -> performCriterion(
																	commandContext.getSource(),
																	EntityArgument.getPlayers(commandContext, "targets"),
																	AdvancementCommands.Action.REVOKE,
																	ResourceLocationArgument.getAdvancement(commandContext, "advancement"),
																	StringArgumentType.getString(commandContext, "criterion")
																)
														)
												)
										)
								)
								.then(
									Commands.literal("from")
										.then(
											Commands.argument("advancement", ResourceLocationArgument.id())
												.suggests(SUGGEST_ADVANCEMENTS)
												.executes(
													commandContext -> perform(
															commandContext.getSource(),
															EntityArgument.getPlayers(commandContext, "targets"),
															AdvancementCommands.Action.REVOKE,
															getAdvancements(commandContext, ResourceLocationArgument.getAdvancement(commandContext, "advancement"), AdvancementCommands.Mode.FROM)
														)
												)
										)
								)
								.then(
									Commands.literal("until")
										.then(
											Commands.argument("advancement", ResourceLocationArgument.id())
												.suggests(SUGGEST_ADVANCEMENTS)
												.executes(
													commandContext -> perform(
															commandContext.getSource(),
															EntityArgument.getPlayers(commandContext, "targets"),
															AdvancementCommands.Action.REVOKE,
															getAdvancements(commandContext, ResourceLocationArgument.getAdvancement(commandContext, "advancement"), AdvancementCommands.Mode.UNTIL)
														)
												)
										)
								)
								.then(
									Commands.literal("through")
										.then(
											Commands.argument("advancement", ResourceLocationArgument.id())
												.suggests(SUGGEST_ADVANCEMENTS)
												.executes(
													commandContext -> perform(
															commandContext.getSource(),
															EntityArgument.getPlayers(commandContext, "targets"),
															AdvancementCommands.Action.REVOKE,
															getAdvancements(commandContext, ResourceLocationArgument.getAdvancement(commandContext, "advancement"), AdvancementCommands.Mode.THROUGH)
														)
												)
										)
								)
								.then(
									Commands.literal("everything")
										.executes(
											commandContext -> perform(
													commandContext.getSource(),
													EntityArgument.getPlayers(commandContext, "targets"),
													AdvancementCommands.Action.REVOKE,
													commandContext.getSource().getServer().getAdvancements().getAllAdvancements()
												)
										)
								)
						)
				)
		);
	}

	private static int perform(
		CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, AdvancementCommands.Action action, Collection<AdvancementHolder> collection2
	) throws CommandSyntaxException {
		int i = 0;

		for (ServerPlayer serverPlayer : collection) {
			i += action.perform(serverPlayer, collection2);
		}

		if (i == 0) {
			if (collection2.size() == 1) {
				if (collection.size() == 1) {
					throw ERROR_NO_ACTION_PERFORMED.create(
						Component.translatable(
							action.getKey() + ".one.to.one.failure",
							Advancement.name((AdvancementHolder)collection2.iterator().next()),
							((ServerPlayer)collection.iterator().next()).getDisplayName()
						)
					);
				} else {
					throw ERROR_NO_ACTION_PERFORMED.create(
						Component.translatable(action.getKey() + ".one.to.many.failure", Advancement.name((AdvancementHolder)collection2.iterator().next()), collection.size())
					);
				}
			} else if (collection.size() == 1) {
				throw ERROR_NO_ACTION_PERFORMED.create(
					Component.translatable(action.getKey() + ".many.to.one.failure", collection2.size(), ((ServerPlayer)collection.iterator().next()).getDisplayName())
				);
			} else {
				throw ERROR_NO_ACTION_PERFORMED.create(Component.translatable(action.getKey() + ".many.to.many.failure", collection2.size(), collection.size()));
			}
		} else {
			if (collection2.size() == 1) {
				if (collection.size() == 1) {
					commandSourceStack.sendSuccess(
						() -> Component.translatable(
								action.getKey() + ".one.to.one.success",
								Advancement.name((AdvancementHolder)collection2.iterator().next()),
								((ServerPlayer)collection.iterator().next()).getDisplayName()
							),
						true
					);
				} else {
					commandSourceStack.sendSuccess(
						() -> Component.translatable(
								action.getKey() + ".one.to.many.success", Advancement.name((AdvancementHolder)collection2.iterator().next()), collection.size()
							),
						true
					);
				}
			} else if (collection.size() == 1) {
				commandSourceStack.sendSuccess(
					() -> Component.translatable(action.getKey() + ".many.to.one.success", collection2.size(), ((ServerPlayer)collection.iterator().next()).getDisplayName()),
					true
				);
			} else {
				commandSourceStack.sendSuccess(() -> Component.translatable(action.getKey() + ".many.to.many.success", collection2.size(), collection.size()), true);
			}

			return i;
		}
	}

	private static int performCriterion(
		CommandSourceStack commandSourceStack,
		Collection<ServerPlayer> collection,
		AdvancementCommands.Action action,
		AdvancementHolder advancementHolder,
		String string
	) throws CommandSyntaxException {
		int i = 0;
		Advancement advancement = advancementHolder.value();
		if (!advancement.criteria().containsKey(string)) {
			throw ERROR_CRITERION_NOT_FOUND.create(Advancement.name(advancementHolder), string);
		} else {
			for (ServerPlayer serverPlayer : collection) {
				if (action.performCriterion(serverPlayer, advancementHolder, string)) {
					i++;
				}
			}

			if (i == 0) {
				if (collection.size() == 1) {
					throw ERROR_NO_ACTION_PERFORMED.create(
						Component.translatable(
							action.getKey() + ".criterion.to.one.failure",
							string,
							Advancement.name(advancementHolder),
							((ServerPlayer)collection.iterator().next()).getDisplayName()
						)
					);
				} else {
					throw ERROR_NO_ACTION_PERFORMED.create(
						Component.translatable(action.getKey() + ".criterion.to.many.failure", string, Advancement.name(advancementHolder), collection.size())
					);
				}
			} else {
				if (collection.size() == 1) {
					commandSourceStack.sendSuccess(
						() -> Component.translatable(
								action.getKey() + ".criterion.to.one.success",
								string,
								Advancement.name(advancementHolder),
								((ServerPlayer)collection.iterator().next()).getDisplayName()
							),
						true
					);
				} else {
					commandSourceStack.sendSuccess(
						() -> Component.translatable(action.getKey() + ".criterion.to.many.success", string, Advancement.name(advancementHolder), collection.size()), true
					);
				}

				return i;
			}
		}
	}

	private static List<AdvancementHolder> getAdvancements(
		CommandContext<CommandSourceStack> commandContext, AdvancementHolder advancementHolder, AdvancementCommands.Mode mode
	) {
		AdvancementTree advancementTree = commandContext.getSource().getServer().getAdvancements().tree();
		AdvancementNode advancementNode = advancementTree.get(advancementHolder);
		if (advancementNode == null) {
			return List.of(advancementHolder);
		} else {
			List<AdvancementHolder> list = new ArrayList();
			if (mode.parents) {
				for (AdvancementNode advancementNode2 = advancementNode.parent(); advancementNode2 != null; advancementNode2 = advancementNode2.parent()) {
					list.add(advancementNode2.holder());
				}
			}

			list.add(advancementHolder);
			if (mode.children) {
				addChildren(advancementNode, list);
			}

			return list;
		}
	}

	private static void addChildren(AdvancementNode advancementNode, List<AdvancementHolder> list) {
		for (AdvancementNode advancementNode2 : advancementNode.children()) {
			list.add(advancementNode2.holder());
			addChildren(advancementNode2, list);
		}
	}

	static enum Action {
		GRANT("grant") {
			@Override
			protected boolean perform(ServerPlayer serverPlayer, AdvancementHolder advancementHolder) {
				AdvancementProgress advancementProgress = serverPlayer.getAdvancements().getOrStartProgress(advancementHolder);
				if (advancementProgress.isDone()) {
					return false;
				} else {
					for (String string : advancementProgress.getRemainingCriteria()) {
						serverPlayer.getAdvancements().award(advancementHolder, string);
					}

					return true;
				}
			}

			@Override
			protected boolean performCriterion(ServerPlayer serverPlayer, AdvancementHolder advancementHolder, String string) {
				return serverPlayer.getAdvancements().award(advancementHolder, string);
			}
		},
		REVOKE("revoke") {
			@Override
			protected boolean perform(ServerPlayer serverPlayer, AdvancementHolder advancementHolder) {
				AdvancementProgress advancementProgress = serverPlayer.getAdvancements().getOrStartProgress(advancementHolder);
				if (!advancementProgress.hasProgress()) {
					return false;
				} else {
					for (String string : advancementProgress.getCompletedCriteria()) {
						serverPlayer.getAdvancements().revoke(advancementHolder, string);
					}

					return true;
				}
			}

			@Override
			protected boolean performCriterion(ServerPlayer serverPlayer, AdvancementHolder advancementHolder, String string) {
				return serverPlayer.getAdvancements().revoke(advancementHolder, string);
			}
		};

		private final String key;

		Action(String string2) {
			this.key = "commands.advancement." + string2;
		}

		public int perform(ServerPlayer serverPlayer, Iterable<AdvancementHolder> iterable) {
			int i = 0;

			for (AdvancementHolder advancementHolder : iterable) {
				if (this.perform(serverPlayer, advancementHolder)) {
					i++;
				}
			}

			return i;
		}

		protected abstract boolean perform(ServerPlayer serverPlayer, AdvancementHolder advancementHolder);

		protected abstract boolean performCriterion(ServerPlayer serverPlayer, AdvancementHolder advancementHolder, String string);

		protected String getKey() {
			return this.key;
		}
	}

	static enum Mode {
		ONLY(false, false),
		THROUGH(true, true),
		FROM(false, true),
		UNTIL(true, false),
		EVERYTHING(true, true);

		final boolean parents;
		final boolean children;

		private Mode(boolean bl, boolean bl2) {
			this.parents = bl;
			this.children = bl2;
		}
	}
}
