package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Collection;
import java.util.List;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

public class AdvancementCommands {
	private static final SuggestionProvider<CommandSourceStack> SUGGEST_ADVANCEMENTS = (commandContext, suggestionsBuilder) -> {
		Collection<Advancement> collection = commandContext.getSource().getServer().getAdvancements().getAllAdvancements();
		return SharedSuggestionProvider.suggestResource(collection.stream().map(Advancement::getId), suggestionsBuilder);
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
															getAdvancements(ResourceLocationArgument.getAdvancement(commandContext, "advancement"), AdvancementCommands.Mode.ONLY)
														)
												)
												.then(
													Commands.argument("criterion", StringArgumentType.greedyString())
														.suggests(
															(commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(
																	ResourceLocationArgument.getAdvancement(commandContext, "advancement").getCriteria().keySet(), suggestionsBuilder
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
															getAdvancements(ResourceLocationArgument.getAdvancement(commandContext, "advancement"), AdvancementCommands.Mode.FROM)
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
															getAdvancements(ResourceLocationArgument.getAdvancement(commandContext, "advancement"), AdvancementCommands.Mode.UNTIL)
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
															getAdvancements(ResourceLocationArgument.getAdvancement(commandContext, "advancement"), AdvancementCommands.Mode.THROUGH)
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
															getAdvancements(ResourceLocationArgument.getAdvancement(commandContext, "advancement"), AdvancementCommands.Mode.ONLY)
														)
												)
												.then(
													Commands.argument("criterion", StringArgumentType.greedyString())
														.suggests(
															(commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(
																	ResourceLocationArgument.getAdvancement(commandContext, "advancement").getCriteria().keySet(), suggestionsBuilder
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
															getAdvancements(ResourceLocationArgument.getAdvancement(commandContext, "advancement"), AdvancementCommands.Mode.FROM)
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
															getAdvancements(ResourceLocationArgument.getAdvancement(commandContext, "advancement"), AdvancementCommands.Mode.UNTIL)
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
															getAdvancements(ResourceLocationArgument.getAdvancement(commandContext, "advancement"), AdvancementCommands.Mode.THROUGH)
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
		CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, AdvancementCommands.Action action, Collection<Advancement> collection2
	) {
		int i = 0;

		for (ServerPlayer serverPlayer : collection) {
			i += action.perform(serverPlayer, collection2);
		}

		if (i == 0) {
			if (collection2.size() == 1) {
				if (collection.size() == 1) {
					throw new CommandRuntimeException(
						new TranslatableComponent(
							action.getKey() + ".one.to.one.failure",
							((Advancement)collection2.iterator().next()).getChatComponent(),
							((ServerPlayer)collection.iterator().next()).getDisplayName()
						)
					);
				} else {
					throw new CommandRuntimeException(
						new TranslatableComponent(action.getKey() + ".one.to.many.failure", ((Advancement)collection2.iterator().next()).getChatComponent(), collection.size())
					);
				}
			} else if (collection.size() == 1) {
				throw new CommandRuntimeException(
					new TranslatableComponent(action.getKey() + ".many.to.one.failure", collection2.size(), ((ServerPlayer)collection.iterator().next()).getDisplayName())
				);
			} else {
				throw new CommandRuntimeException(new TranslatableComponent(action.getKey() + ".many.to.many.failure", collection2.size(), collection.size()));
			}
		} else {
			if (collection2.size() == 1) {
				if (collection.size() == 1) {
					commandSourceStack.sendSuccess(
						new TranslatableComponent(
							action.getKey() + ".one.to.one.success",
							((Advancement)collection2.iterator().next()).getChatComponent(),
							((ServerPlayer)collection.iterator().next()).getDisplayName()
						),
						true
					);
				} else {
					commandSourceStack.sendSuccess(
						new TranslatableComponent(action.getKey() + ".one.to.many.success", ((Advancement)collection2.iterator().next()).getChatComponent(), collection.size()),
						true
					);
				}
			} else if (collection.size() == 1) {
				commandSourceStack.sendSuccess(
					new TranslatableComponent(action.getKey() + ".many.to.one.success", collection2.size(), ((ServerPlayer)collection.iterator().next()).getDisplayName()),
					true
				);
			} else {
				commandSourceStack.sendSuccess(new TranslatableComponent(action.getKey() + ".many.to.many.success", collection2.size(), collection.size()), true);
			}

			return i;
		}
	}

	private static int performCriterion(
		CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, AdvancementCommands.Action action, Advancement advancement, String string
	) {
		int i = 0;
		if (!advancement.getCriteria().containsKey(string)) {
			throw new CommandRuntimeException(new TranslatableComponent("commands.advancement.criterionNotFound", advancement.getChatComponent(), string));
		} else {
			for (ServerPlayer serverPlayer : collection) {
				if (action.performCriterion(serverPlayer, advancement, string)) {
					i++;
				}
			}

			if (i == 0) {
				if (collection.size() == 1) {
					throw new CommandRuntimeException(
						new TranslatableComponent(
							action.getKey() + ".criterion.to.one.failure", string, advancement.getChatComponent(), ((ServerPlayer)collection.iterator().next()).getDisplayName()
						)
					);
				} else {
					throw new CommandRuntimeException(
						new TranslatableComponent(action.getKey() + ".criterion.to.many.failure", string, advancement.getChatComponent(), collection.size())
					);
				}
			} else {
				if (collection.size() == 1) {
					commandSourceStack.sendSuccess(
						new TranslatableComponent(
							action.getKey() + ".criterion.to.one.success", string, advancement.getChatComponent(), ((ServerPlayer)collection.iterator().next()).getDisplayName()
						),
						true
					);
				} else {
					commandSourceStack.sendSuccess(
						new TranslatableComponent(action.getKey() + ".criterion.to.many.success", string, advancement.getChatComponent(), collection.size()), true
					);
				}

				return i;
			}
		}
	}

	private static List<Advancement> getAdvancements(Advancement advancement, AdvancementCommands.Mode mode) {
		List<Advancement> list = Lists.<Advancement>newArrayList();
		if (mode.parents) {
			for (Advancement advancement2 = advancement.getParent(); advancement2 != null; advancement2 = advancement2.getParent()) {
				list.add(advancement2);
			}
		}

		list.add(advancement);
		if (mode.children) {
			addChildren(advancement, list);
		}

		return list;
	}

	private static void addChildren(Advancement advancement, List<Advancement> list) {
		for (Advancement advancement2 : advancement.getChildren()) {
			list.add(advancement2);
			addChildren(advancement2, list);
		}
	}

	static enum Action {
		GRANT("grant") {
			@Override
			protected boolean perform(ServerPlayer serverPlayer, Advancement advancement) {
				AdvancementProgress advancementProgress = serverPlayer.getAdvancements().getOrStartProgress(advancement);
				if (advancementProgress.isDone()) {
					return false;
				} else {
					for (String string : advancementProgress.getRemainingCriteria()) {
						serverPlayer.getAdvancements().award(advancement, string);
					}

					return true;
				}
			}

			@Override
			protected boolean performCriterion(ServerPlayer serverPlayer, Advancement advancement, String string) {
				return serverPlayer.getAdvancements().award(advancement, string);
			}
		},
		REVOKE("revoke") {
			@Override
			protected boolean perform(ServerPlayer serverPlayer, Advancement advancement) {
				AdvancementProgress advancementProgress = serverPlayer.getAdvancements().getOrStartProgress(advancement);
				if (!advancementProgress.hasProgress()) {
					return false;
				} else {
					for (String string : advancementProgress.getCompletedCriteria()) {
						serverPlayer.getAdvancements().revoke(advancement, string);
					}

					return true;
				}
			}

			@Override
			protected boolean performCriterion(ServerPlayer serverPlayer, Advancement advancement, String string) {
				return serverPlayer.getAdvancements().revoke(advancement, string);
			}
		};

		private final String key;

		Action(String string2) {
			this.key = "commands.advancement." + string2;
		}

		public int perform(ServerPlayer serverPlayer, Iterable<Advancement> iterable) {
			int i = 0;

			for (Advancement advancement : iterable) {
				if (this.perform(serverPlayer, advancement)) {
					i++;
				}
			}

			return i;
		}

		protected abstract boolean perform(ServerPlayer serverPlayer, Advancement advancement);

		protected abstract boolean performCriterion(ServerPlayer serverPlayer, Advancement advancement, String string);

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
